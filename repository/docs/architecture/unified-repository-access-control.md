---
type: architecture
title: Unified Repository Access Control Analysis
description: Bean composition, layer-by-layer access-control enforcement, and exception taxonomy for IUnifiedRepository.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Unified Repository Access Control Analysis

> Source files analysed:
> - `repository.spring.xml` – Spring bean configuration (AOP proxies, interceptors, ACL voters, ABS bindings)
> - `DefaultUnifiedRepository.java` – public API implementation
> - `ExceptionLoggingDecorator.java` – outermost `unifiedRepository` bean
> - `JcrRepositoryFileDao.java` – JCR DAO (file operations)
> - `JcrRepositoryFileAclDao.java` – JCR DAO (ACL operations)
> - `DefaultDeleteHelper.java` – JCR DAO helper for delete/undelete (source of `RepositoryFileDaoFileExistsException`/`RepositoryFileDaoReferentialIntegrityException`)
> - `RepositoryAccessVoterManager.java` – file-level voter manager

---

## 1. Bean composition and call chain

```
Caller
  └─► unifiedRepository                       (ExceptionLoggingDecorator)
        └─► unifiedRepositoryProxy             (ProxyFactoryBean with AOP chain)
              ├─ unifiedRepositoryTransactionInterceptor   (JCR transaction)
              ├─ unifiedRepositoryMethodInterceptor        (Spring Security method security)
              └─► unifiedRepositoryTarget      (DefaultUnifiedRepository)
                    ├─► repositoryFileDao      (JcrRepositoryFileDao)
                    │     └─► JCR session      (Jackrabbit, opened with user credentials)
                    └─► repositoryFileAclDao   (JcrRepositoryFileAclDao)
```

AOP interceptors in `unifiedRepositoryProxy` are applied outermost-first:
the transaction interceptor starts the JCR transaction, then the method security
interceptor performs the ABS check, then the target method executes.

---

## 2. Layer-by-layer access control

### 2.1 `ExceptionLoggingDecorator` (`unifiedRepository` bean)

Role: exception translation and logging only – no access decision is made here.

Every method call is wrapped in `callLogThrow()`, which:

1. Iterates the full exception cause chain (most-specific-cause first).
2. Looks up the exception class name in the converter map configured in `repository.spring.xml`.
3. If none matched, and the calling method supplied a **method-specific fallback
   constructor** (see below), throws that subtype instead.
4. Otherwise throws a generic `UnifiedRepositoryException`.

Configured converters (checked first, against **every** class in the cause chain, not
just the direct cause — so these take priority even over a method-specific fallback):

| Source exception | Converted to | Thrown by (only) |
|---|---|---|
| `org.springframework.security.access.AccessDeniedException` | `UnifiedRepositoryAccessDeniedException` | Any ABS-guarded method (§2.2) |
| `RepositoryFileDaoFileExistsException` | `UnifiedRepositoryFileExistsException` | **`undeleteFile`** only — thrown when a file/folder already exists at the deleted item's original path (`DefaultDeleteHelper.undeleteFile`) |
| `RepositoryFileDaoReferentialIntegrityException` | `UnifiedRepositoryReferentialIntegrityException` | **`permanentlyDeleteFile`** only — thrown when other JCR nodes still hold references to the file being permanently deleted (`DefaultDeleteHelper.permanentlyDeleteFile`) |
| `RepositoryFileDaoMalformedNameException` | `UnifiedRepositoryMalformedNameException` | **`setFileMetadata`** only — thrown when a metadata **key** (not a file/folder name) contains reserved characters (`JcrRepositoryFileUtils.setMetadataItemForFile` → `checkName()`). The equivalent `checkName()` calls for file/folder names elsewhere in `JcrRepositoryFileUtils` are commented-out dead code, so no other method can throw this. |

> **Important:** `javax.jcr.AccessDeniedException` (thrown at the JCR DAO layer for certain
> write-access violations) is **not** in this map — and neither is the class it actually
> arrives here as. Before reaching `ExceptionLoggingDecorator`, `JcrTemplate` translates it
> (and `PathNotFoundException`/`ItemNotFoundException`) into
> `org.springframework.dao.DataRetrievalFailureException` (see §2.7). That class is also not
> in the map, so — unless a method-specific fallback constructor applies (see below) — it
> propagates as a generic `UnifiedRepositoryException`, not as
> `UnifiedRepositoryAccessDeniedException` — but its cause chain still contains the original
> `javax.jcr.AccessDeniedException`, two hops down (`getCause().getCause()`).

#### Method-specific fallback constructors (not a generic `UnifiedRepositoryException`!)

Three call sites in `ExceptionLoggingDecorator` pass an extra `exceptionConstructor`
argument to `callLogThrow()`, which is used **only if no converter-map entry matched**,
in place of the generic `UnifiedRepositoryException`:

| Method | Fallback exception (instead of generic `UnifiedRepositoryException`) |
|---|---|
| `createFile` (both overloads — with and without an explicit ACL) | `UnifiedRepositoryCreateFileException` |
| `updateFile` | `UnifiedRepositoryUpdateFileException` |

**`createFolder` and `updateFolder` do *not* get this treatment** — despite being the
folder-equivalents of the methods above, they still throw the plain generic
`UnifiedRepositoryException` for the same not-found/no-write conditions (see §3).

Both `UnifiedRepositoryCreateFileException` and `UnifiedRepositoryUpdateFileException`
extend `UnifiedRepositoryException` directly (no other state/behavior added beyond a
message prefix), so a `catch (UnifiedRepositoryException e)` still catches them via
ordinary polymorphism — but code that wants to report the *most specific* declared type
should catch these first. Because this substitution only changes the **outer** exception
class, it has no effect on the cause-chain depth/shape documented in §2.7/§4 — the
not-found-vs-no-write disambiguation by innermost cause still applies identically underneath
either wrapper.

---

### 2.2 `unifiedRepositoryMethodInterceptor` (AOP method-level security)

This is an `org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor`
that performs **Action-Based Security (ABS)** checks before any method executes.

The access decision manager is `businessAccessDecisionManager`
(`UnanimousBased` with a single `authorizationPolicyVoter`).
The voter evaluates `VOTE_AUTHZ_POLICY_<action>` attributes against the current user's
granted logical roles (stored in JCR, managed via `roleAuthorizationPolicyRoleBindingDao`).

When the check fails the interceptor throws
`org.springframework.security.access.AccessDeniedException`, which is caught by
`ExceptionLoggingDecorator` and re-thrown as `UnifiedRepositoryAccessDeniedException`.

#### 2.2.1 Protected methods and required ABS actions

The ABS action required is **global** (not per-file): it expresses a system-level
capability, not a per-resource permission.

**Require `org.pentaho.repository.read`**

| Method |
|---|
| `getFile` (all overloads) |
| `getFileById` (all overloads) |
| `getFileAtVersion` |
| `getChildren` (all overloads) |
| `getTree` (all overloads) |
| `getDataForRead` |
| `getDataForReadInBatch` |
| `getDataAtVersionForRead` |
| `getDataForExecute` |
| `getDataForExecuteInBatch` |
| `getDataAtVersionForExecute` |
| `getAcl` |
| `getEffectiveAces` (both overloads) |
| `hasAccess` |
| `canUnlockFile` |
| `getVersionSummary` |
| `getVersionSummaryInBatch` |
| `getVersionSummaries` |
| `getDeletedFiles` (both overloads) |
| `getReferrers` |
| `getFileMetadata` |
| `getAvailableLocalesForFileById` |
| `getAvailableLocalesForFileByPath` |
| `getAvailableLocalesForFile` |
| `getLocalePropertiesForFileById` |
| `getLocalePropertiesForFileByPath` |
| `getLocalePropertiesForFile` |

**Require `org.pentaho.repository.create`**

| Method |
|---|
| `createFile` (both overloads) |
| `createFolder` (both overloads) |
| `updateFile` |
| `updateFolder` |
| `updateAcl` |
| `lockFile` |
| `unlockFile` |
| `restoreFileAtVersion` |
| `deleteFile` (both overloads) |
| `deleteFileAtVersion` |
| `undeleteFile` |
| `moveFile` |
| `copyFile` |
| `setFileMetadata` |
| `setLocalePropertiesForFileById` |
| `setLocalePropertiesForFileByPath` |
| `setLocalePropertiesForFile` |
| `deleteLocalePropertiesForFile` |

#### 2.2.2 Methods NOT covered by the method interceptor

The following methods have **no ABS check** and therefore no method-level access gate:

| Method | Notes |
|---|---|
| `getReservedChars` | Returns static metadata; no file access involved |
| `getAllDeletedFiles` | Retrieves all deleted files; no ABS guard |

These methods still go through the JCR session (user-credentials-based) and
the `RepositoryAccessVoterManager` at the DAO level.

#### 2.2.3 ABS action hierarchy (derived rules configured in `repository.spring.xml`)

```
administerSecurity  ──► repositoryCreate  ──► repositoryRead
```

The admin role has all actions via immutable role binding.
The default `Authenticated` role is bootstrapped with both `repositoryRead` and
`repositoryCreate`.

---

### 2.3 `DefaultUnifiedRepository` (target bean)

The target mostly delegates straight to the DAOs. The only explicit access check at
this layer is in **`updateAcl()`**:

```java
// DefaultUnifiedRepository.updateAcl()
RepositoryFile file = getFileById(acl.getId());
if (!hasAccess(file.getPath(), EnumSet.of(RepositoryFilePermission.ACL_MANAGEMENT))) {
    throw new UnifiedRepositoryAccessDeniedException(...);
}
```

- **Check**: `IRepositoryFileAclDao.hasAccess()` → Jackrabbit native ACL privilege check.
- **Exception thrown**: `UnifiedRepositoryAccessDeniedException` **directly** (not via Spring Security, not via the converter map).
- **Note**: If the file is inaccessible to the caller (returns `null` from `getFileById`
  due to the confounding behaviour described below), this call will throw a
  `NullPointerException` before reaching the explicit access check.

---

### 2.4 `JcrRepositoryFileDao` – file-path-level access

Two independent enforcement mechanisms operate at this layer:

- **`accessVoterManager` (`RepositoryAccessVoterManager`)** — an explicit Pentaho extension point.
- **Jackrabbit native JCR session ACL enforcement** — implicit, always active for every JCR API call.

#### 2.4.1 `accessVoterManager` has no voters in the default configuration

The Spring bean definition is:

```xml
<bean id="repositoryAccessVoterManager"
      class="org.pentaho.platform.repository2.unified.RepositoryAccessVoterManager">
  <constructor-arg ref="authorizationPolicy"/>
  <constructor-arg ref="repositoryAdminUsername"/>
</bean>
```

This constructor leaves `voters` as `null`. The guard in `hasAccess()` is:

```java
if (voters != null && ...) { /* evaluate voters */ }
return true;  // always reached when voters == null
```

**`accessVoterManager.hasAccess()` therefore always returns `true` in the default
configuration.** All `null`-return checks in the DAO that are conditioned on the voter
result are no-ops unless custom `IRepositoryAccessVoter` implementations are registered
at runtime via `registerVoter()`.

In the default installation the per-file read/write gate is enforced exclusively by
Jackrabbit (see §2.4.2).

#### 2.4.2 Jackrabbit native JCR session ACL enforcement

The JCR session obtained via `jcrTemplate` is always opened with the **current user's
credentials** (`PentahoSessionCredentialsStrategy`). Jackrabbit enforces its own ACLs
on every JCR API call made through that session:

- **Read operations** (`session.getItem`, `session.getNodeByIdentifier`,
  `folderNode.getNodes()`, etc.): nodes the user cannot read are invisible. The API
  throws `PathNotFoundException` / `ItemNotFoundException` — not `AccessDeniedException`
  — so they look like absent nodes to the caller.
- **Write/mutate operations** (`session.save()`, workspace `move`/`copy`, version
  manager calls): if the user lacks the required JCR privilege,
  **`javax.jcr.AccessDeniedException` is thrown**. Neither this exception nor the
  `org.springframework.dao.DataRetrievalFailureException` it gets translated into by
  `JcrTemplate` (§2.7) is in the `ExceptionLoggingDecorator` converter map, so it surfaces
  to callers as a generic `UnifiedRepositoryException` — with the original
  `javax.jcr.AccessDeniedException` two `getCause()` hops down.

This enforcement is **universal**: it applies to every method in the DAO that opens a
JCR session, with no exceptions and no admin bypass at the JCR API level (the JCR admin
bypass only applies to the separate `jcrAdminTemplate` used for user/role management,
not to `repositoryFileDao`).

#### 2.4.3 Bypass for privileged users (`accessVoterManager` only)

The `accessVoterManager` voter loop is **skipped entirely** when:

- The current user has the `administerSecurity` ABS action, **or**
- The session username equals `repositoryAdminUsername`.

Since the voter manager already has no voters by default, this bypass only matters when
custom voters have been registered. It does **not** bypass Jackrabbit's own JCR
enforcement.

#### 2.4.4 Kiosk mode

Many write operations begin with:

```java
if (isKioskEnabled()) {
    throw new RuntimeException("Access denied");
}
```

This is a global write-lock, independent of ACLs or user identity.
Affected methods: `createFile`, `createFolder`, `updateFile`, `updateFolder`,
`lockFile`, `unlockFile`, `deleteFile`, `deleteFileAtVersion`, `permanentlyDeleteFile`,
`undeleteFile`, `moveFile`, `copyFile`.

---

#### 2.4.5 Read operations – the "not-found confounding" pattern

For all read paths the DAO **returns `null` instead of throwing an exception** when a
file is inaccessible. The caller cannot distinguish "file does not exist" from "file
exists but you have no read access".

In the **default configuration** this behaviour comes entirely from **Jackrabbit native
session enforcement**: a node the user cannot read throws `PathNotFoundException` /
`ItemNotFoundException`, which the DAO catches and converts to `null`. The
`accessVoterManager` check that follows is a no-op (no voters).

If custom voters are registered, the `accessVoterManager` check provides an additional
software-layer filter on top of the JCR filter — allowing access to be restricted
beyond what the JCR ACL alone would enforce.

| Method / Internal helper | Jackrabbit native result | `accessVoterManager` result |
|---|---|---|
| `internalGetFile(session, absPath, …)` | `PathNotFoundException` → `null` | voter denies READ → `null` |
| `internalGetFileById(fileId, …)` | `ItemNotFoundException` → `null` | voter denies READ → `null` |
| `getData(fileId, versionId, …)` | *(data already fetched)* | voter denies READ → `null` |

Public entry points that use these helpers (and therefore inherit `null`-on-denied behaviour):
`getFile` (all overloads), `getFileById` (all overloads),
`getDataForRead`, `getDataAtVersionForRead`, `getDataForExecute`, `getDataAtVersionForExecute`.

> **Why the confounding?** A user with no read permission must not learn whether a path
> exists at all. Exposing "file exists, but access denied" leaks information about the
> repository structure to the caller.

#### 2.4.6 Children and tree traversal

**`getChildren`** (`JcrRepositoryFileUtils.getChildren`): does **not** call
`accessVoterManager`. It calls `session.getNodeByIdentifier()` for the folder then
`folderNode.getNodes()` for children. Jackrabbit natively omits any child node the user
cannot read — no `null` return, those children simply do not appear in the list. There
is no explicit Pentaho-layer voter check.

**`getTree`** (`JcrRepositoryFileUtils.getTreeByNode`): **does** call
`accessVoterManager.hasAccess(READ)` for every node it visits (root and each recursive
child). A node for which the voter denies access returns `null` (excluded from the
tree). Jackrabbit native filtering still applies first at the JCR API level.

---

#### 2.4.7 Write operations – mixed behaviour

Every write operation that reaches JCR will be subject to **Jackrabbit native ACL
enforcement** at `session.save()` (or earlier JCR calls). If the user lacks the
required JCR privilege, `javax.jcr.AccessDeniedException` is thrown. Per §2.7, `JcrTemplate`
translates this into `org.springframework.dao.DataRetrievalFailureException` before it
reaches `ExceptionLoggingDecorator`; since neither class is in the converter map, it
surfaces as a generic `UnifiedRepositoryException` (with `javax.jcr.AccessDeniedException`
two `getCause()` hops down — see §4).

Before reaching `session.save()`, some methods perform an `accessVoterManager` check
(no-op in default config) that can short-circuit with a silent `null` return if a
custom voter denies access.

**`accessVoterManager` check present → silent `null` return if a voter denies
(bypasses the JCR write and its exception)**

| Method | Permission checked by voter | Notes |
|---|---|---|
| `internalCreateFile` | `WRITE` on parent folder | Returns `null`; JCR never reached |
| `internalCreateFolder` | `WRITE` on parent folder | Returns `null`; JCR never reached |
| `internalUpdateFile` | `WRITE` on the file itself | Returns `null`; JCR never reached |
| `undeleteFile` | `WRITE` on the file | Returns `null`; JCR never reached |
| `deleteFile` (voter path) | `DELETE` on the file | Returns `null`; secondary JCR check below is also bypassed |
| `deleteFileAtVersion` | `DELETE` on the file | Returns `null`; JCR never reached |
| `permanentlyDeleteFile` | `DELETE` on the file | Returns `null`; JCR never reached |

> In the default configuration (no voters) the voter check always passes, so these
> methods always proceed to the JCR operations and Jackrabbit is the actual enforcement
> mechanism.

**No `accessVoterManager` check — only Jackrabbit native enforcement**

| Method | Denial result |
|---|---|
| `updateFolder` | `javax.jcr.AccessDeniedException` → generic `UnifiedRepositoryException` |
| `setFileMetadata` | `javax.jcr.AccessDeniedException` → generic `UnifiedRepositoryException` |
| `setLocalePropertiesForFile*` | `javax.jcr.AccessDeniedException` → generic `UnifiedRepositoryException` |
| `deleteLocalePropertiesForFile` | `javax.jcr.AccessDeniedException` → generic `UnifiedRepositoryException` |
| `lockFile` / `unlockFile` | `javax.jcr.AccessDeniedException` → generic `UnifiedRepositoryException` |
| `restoreFileAtVersion` | `javax.jcr.AccessDeniedException` → generic `UnifiedRepositoryException` |

**Exception thrown explicitly before `session.save()` (in addition to the eventual JCR check)**

| Method | Condition | Exception thrown |
|---|---|---|
| `deleteFile` | `aclDao.hasAccess(DELETE)` returns `false` (second check, after voter) | `javax.jcr.AccessDeniedException` → generic `UnifiedRepositoryException` |
| `internalCopyOrMove` (move) | `accessVoterManager.hasAccess(WRITE)` denies on source | `javax.jcr.AccessDeniedException` → generic `UnifiedRepositoryException` |
| `internalCopyOrMove` (move or copy) | `accessVoterManager.hasAccess(WRITE)` denies on destination | `javax.jcr.AccessDeniedException` → generic `UnifiedRepositoryException` |

> Note: for `deleteFile`, when the voter check passes (default: always) but
> `aclDao.hasAccess(DELETE)` fails, this is the one case where an explicit
> `javax.jcr.AccessDeniedException` is thrown at the Pentaho layer before `session.save()`
> is reached. It still surfaces as a generic `UnifiedRepositoryException` because of the
> missing converter.

---

#### 2.4.8 Per-node JCR privilege requirements and Magic ACE caveats

> Sourced from the companion analysis in `../reference/repository-permission-model.md` — included here because it
> directly affects whether the `javax.jcr.AccessDeniedException` in §2.4.7 fires **at all**,
> not just what it looks like once thrown.

The Pentaho-layer voter/aclDao pre-checks (§2.4.1, §2.5) only ever evaluate **one** node per
operation (the file, or the destination folder). The underlying JCR call frequently touches
**additional** nodes that have no corresponding Pentaho-layer pre-check — a gap marked `⚠`
below. If the user is missing the JCR privilege on one of those un-checked nodes, JCR throws
`javax.jcr.AccessDeniedException` there instead, with the same downstream translation and
surfacing as a generic `UnifiedRepositoryException` (§2.7, §4) — but for a node the caller
may not have expected to need permission on.

| Operation | JCR call | Node | Pentaho-layer check | JCR privilege required (after Magic ACE injection) |
|---|---|---|---|---|
| `createFile` / `createFolder` | `node.addNode()` | parent folder | `WRITE` (voter) | `jcr:addChildNodes` |
| `updateFile` | `node.setProperty()` | file | `WRITE` (voter) | `jcr:modifyProperties` |
| `deleteFile` (soft) | `session.move(file → .trash)` | file | `DELETE` (voter + `aclDao`) | `jcr:removeNode` ¹ |
| `deleteFile` (soft) | `session.move(file → .trash)` | ⚠ source parent | *(none)* | `jcr:removeChildNodes` |
| `deleteFile` (soft) | `session.move(file → .trash)` | ⚠ `.trash` folder | *(none)* | `jcr:addChildNodes` |
| `permanentlyDeleteFile` | `fileNode.remove()` | file | `DELETE` (voter) | `jcr:removeNode` |
| `permanentlyDeleteFile` | `fileNode.remove()` | ⚠ source parent | *(none)* | `jcr:removeChildNodes` |
| `moveFile` / rename | `workspace.move(src, dest)` | file (source) | `WRITE` (voter) | — |
| `moveFile` / rename | `workspace.move(src, dest)` | dest folder | `WRITE` (voter) | `jcr:addChildNodes` |
| `moveFile` / rename | `workspace.move(src, dest)` | ⚠ source parent | *(none)* | `jcr:removeChildNodes` |
| `copyFile` | `workspace.copy(src, dest)` | dest folder | `WRITE` (voter) | `jcr:addChildNodes` |
| `copyFile` | `workspace.copy(src, dest)` | ⚠ source file | *(none — no check at either layer)* | *(none — see §3 `copyFile` row)* |
| `undeleteFile` | `session.move(.trash → orig)` | file | `WRITE` (voter) | — |
| `updateAcl` | ACL update | file | `ACL_MANAGEMENT` (`aclDao`, in `DefaultUnifiedRepository`) | `pho:aclManagement` |
| `lockFile` / `unlockFile` | JCR lock | — | kiosk only | `jcr:lockManagement` |

¹ `deleteFile` moves the file to `.trash`; the Pentaho `aclDao.hasAccess(file, DELETE)` check
asks for `jcr:removeNode` on the file itself. Whether this passes depends on Magic ACE
injection — see below.

**Practical consequence for §3's summary table:** a caller who has `DELETE` on a file but
lacks `jcr:removeChildNodes` on its *parent* (or `jcr:addChildNodes` on `.trash`) will still
receive `AccessDeniedException` → `URE` from `deleteFile` even though the Pentaho-layer
checks on the file itself all passed. The same applies to `jcr:removeChildNodes` on the
source parent for `moveFile`/`permanentlyDeleteFile`. These are additional, *unchecked*
failure points beyond what the table's "No-write-or-delete-access" column's named check
covers.

##### Magic ACE injection can make a JCR privilege check pass unexpectedly

`PentahoEntryCollector` (invoked by both `aclDao.hasAccess()` and native JCR
`hasPrivileges()`/enforcement) injects two kinds of ACEs that are **not** visible via
`JcrRepositoryFileAclDao.getAcl()`:

1. **Inheritance transformation**: if the node's ACL is set to inherit
   (`isEntriesInheriting=true`), the parent folder's `jcr:removeChildNodes` privilege is
   injected as `jcr:removeNode` on the node itself. This is why `DELETE` on an inheriting
   child can succeed even when `DELETE` on the parent folder itself would not (the parent
   folder's own ACL evaluation doesn't get this transformation).
2. **Owner ACE**: the creator of a node always has `jcr:all` injected for that node,
   regardless of any explicit ACE — this covers read, write, delete, lock, and version
   management.

Both mean an "expected" `AccessDeniedException` at one of the JCR calls in the table above
may **not** occur even though the visible ACL (via `getAcl()`) suggests it should — and,
conversely, that a caller cannot rely on `getAcl()` alone to predict whether a write/delete
operation will succeed.

##### Owner-ACE gap: owners can still get `URADE` from `updateAcl`

The owner ACE (`jcr:all`) does **not** include the custom `pho:aclManagement` privilege.
This means the creator of a file — who passes every JCR write/delete/lock/version check via
the owner ACE — can still receive `UnifiedRepositoryAccessDeniedException` from `updateAcl`
(§2.3, §3) unless they also hold an explicit ACE granting `ACL_MANAGEMENT`. This is a
permanent condition for as long as they remain the owner and is not visible in `getAcl()`'s
output (Magic ACEs are stripped by `JcrRepositoryFileAclUtils.removeAclMetadata()`).

---

### 2.5 `JcrRepositoryFileAclDao` – `hasAccess()`

Used directly by `DefaultUnifiedRepository.updateAcl()` and internally by
`JcrRepositoryFileDao.deleteFile()`.

```java
// JcrRepositoryFileAclDao.hasAccess()
session.getAccessControlManager().hasPrivileges(absPath, privs);
// PathNotFoundException → returns false (not an exception)
```

This is a **Jackrabbit native privilege check** on the JCR `AccessControlManager`.
Returns `false` (not an exception) when the path does not exist.

---

### 2.6 Jackrabbit JCR session (native layer)

The JCR session is opened using `PentahoSessionCredentialsStrategy`, which extracts
the current Pentaho session credentials and logs into Jackrabbit as the actual user.

- Jackrabbit enforces its own ACL at every JCR API call made through the session.
- When a user has no read access to a node, `session.getItem(path)` throws
  `PathNotFoundException` and `session.getNodeByIdentifier(id)` throws
  `ItemNotFoundException` — the **same exceptions thrown when the node genuinely does
  not exist**. This is standard JCR/Jackrabbit behaviour: inaccessible nodes are
  completely invisible to the session. There is no way to distinguish "absent" from
  "access denied" at the JCR API level, and this is the primary source of the
  "not-found confounding" for read operations.
- When a user lacks write/delete/lock privilege, mutating JCR operations throw
  `javax.jcr.AccessDeniedException`. Per §2.7, `JcrTemplate` translates this — along with
  `PathNotFoundException`/`ItemNotFoundException` above — into the same
  `org.springframework.dao.DataRetrievalFailureException`. Since that class is not in the
  `ExceptionLoggingDecorator` converter map, it surfaces as a generic
  `UnifiedRepositoryException`, indistinguishable at the outer-exception level from a
  not-found/no-read condition (see §4 for how to disambiguate via the cause chain).
- `session.getAccessControlManager().hasPrivileges()` (used by `JcrRepositoryFileAclDao.hasAccess()`
  and therefore by `DefaultUnifiedRepository.updateAcl()`) explicitly checks whether
  specific JCR privileges are held on a given path, returning `false` for
  `PathNotFoundException`.

---

### 2.7 `JcrTemplate` exception translation — the layer between the JCR session and `ExceptionLoggingDecorator`

> This layer is not mentioned in §2.4/§2.6 above but is essential to understanding what
> `ExceptionLoggingDecorator` actually receives, and therefore what callers see.

Every DAO method body runs inside `jcrTemplate.execute(new JcrCallback() { ... })`
(`JcrTemplate` from `org.springframework.extensions.jcr`, the `se-jcr` library). The
`JcrCallback.doInJcr()` method is declared to throw the **checked** `javax.jcr.RepositoryException`.
`JcrTemplate.execute()` catches any `RepositoryException` escaping the callback and calls
`SessionFactoryUtils.translateException()`, which maps it — by `instanceof`, most specific
JCR exception type first — to an **unchecked** `org.springframework.dao.DataAccessException`
subtype, wrapping the original JCR exception as its cause. This translation applies **regardless
of whether the JCR exception was thrown natively by Jackrabbit or thrown explicitly by Pentaho
DAO code inside the callback** (e.g. the manual `throw new AccessDeniedException(...)` in
`deleteFile()` and `internalCopyOrMove()` — see §2.4.7).

The relevant mappings (from `SessionFactoryUtils.translateException()`):

| JCR exception (`javax.jcr.*`) | Translated to (`org.springframework.dao.*`) |
|---|---|
| `AccessDeniedException` | `DataRetrievalFailureException` |
| `PathNotFoundException` | `DataRetrievalFailureException` |
| `ItemNotFoundException` | `DataRetrievalFailureException` |
| `nodetype.ConstraintViolationException` | `DataIntegrityViolationException` |
| `ItemExistsException` | `DataIntegrityViolationException` |
| `ReferentialIntegrityException` | `DataIntegrityViolationException` |
| `version.VersionException` | `DataIntegrityViolationException` |
| `InvalidItemStateException` | `ConcurrencyFailureException` |
| `lock.LockException` | `ConcurrencyFailureException` |
| `LoginException` | `DataAccessResourceFailureException` |
| `NoSuchWorkspaceException` | `DataAccessResourceFailureException` |
| `query.InvalidQueryException` | `DataRetrievalFailureException` |
| `InvalidSerializedDataException` | `DataRetrievalFailureException` |
| `NamespaceException` / `nodetype.NoSuchNodeTypeException` / `UnsupportedRepositoryOperationException` / `ValueFormatException` | `InvalidDataAccessApiUsageException` |
| *(anything else)* | `org.springframework.extensions.jcr.JcrSystemException` |

> **Critical consequence:** `javax.jcr.AccessDeniedException`, `javax.jcr.PathNotFoundException`,
> and `javax.jcr.ItemNotFoundException` are **all three translated to the same class**,
> `org.springframework.dao.DataRetrievalFailureException`. At the `ExceptionLoggingDecorator`
> boundary, a write/delete-denial and a not-found/no-read condition that escapes the JCR
> callback are **indistinguishable by outer exception type alone** — the underlying JCR
> exception (available via `.getCause()` on the `DataRetrievalFailureException`) is the only
> way to tell them apart. See §4 for how this plays out in the exception taxonomy and how to
> disambiguate.
>
> This translation only happens for exceptions that **escape the `JcrCallback`**. Several DAO
> methods (`internalGetFile`, `internalGetFileById`, `JcrRepositoryFileAclDao.hasAccess()`)
> catch `PathNotFoundException` / `ItemNotFoundException` **inside** the callback and convert
> them to a `null` / `false` return value before they ever reach `JcrTemplate` — for those
> methods no exception is thrown or translated at all (see §2.4.5, §2.5).

Because `org.springframework.dao.DataRetrievalFailureException` (and the other
`org.springframework.dao.*` types above) are **not** in the `ExceptionLoggingDecorator`
converter map (§2.1), they always fall through to the generic
`throw new UnifiedRepositoryException(message, e)` branch, where `e` is exactly the exception
`callLogThrow()` caught — i.e. the `DataRetrievalFailureException` itself, not the JCR
exception. This means:

```
UnifiedRepositoryException                              (thrown by ExceptionLoggingDecorator)
 └─ cause: org.springframework.dao.DataRetrievalFailureException   (thrown by JcrTemplate)
     └─ cause: javax.jcr.AccessDeniedException                     (or PathNotFoundException / ItemNotFoundException)
```

i.e. the generic `UnifiedRepositoryException`'s **direct** cause is the Spring `dao` exception,
and the JCR exception is one level further down (`getCause().getCause()`), **not** the direct
cause. See §4 for the corrected taxonomy and a snippet to unwrap this chain.

---

## 3. Summary table – per method

Columns:
- **ABS action**: required global action checked by `unifiedRepositoryMethodInterceptor` _before_ the method runs.
- **ABS denial**: exception thrown when the ABS check fails. Applies to every call regardless of which file is involved.
- **`accessVoterManager` check**: explicit Pentaho-layer voter check inside the DAO. *No-op in default config (no voters).*
- **Not-found-or-no-read-access**: what the caller gets when the target node (or, where noted, a related node such as a parent/destination) does not exist **or** the user has no `jcr:read` privilege on it. Per §2.4.2/§2.7, JCR makes these two cases indistinguishable; some DAO methods swallow this internally (silent `null`), others let it propagate and it is translated by `JcrTemplate` into a generic `URE`.
- **No-write-or-delete-access**: what the caller gets when the target node **does** exist and is readable, but the user lacks the JCR privilege required to write/delete/lock/version it (or the Pentaho `accessVoterManager`/`aclDao` pre-check denies it).

`URADE` = `UnifiedRepositoryAccessDeniedException`  
`URE` = `UnifiedRepositoryException` (generic)  
`AccessDeniedException` (JCR) = `javax.jcr.AccessDeniedException`  
`PathNotFoundException` = `javax.jcr.PathNotFoundException`  
`ItemNotFoundException` = `javax.jcr.ItemNotFoundException`  
`DataRetrievalFailureException` = `org.springframework.dao.DataRetrievalFailureException` (see §2.7 — the wrapper both JCR not-found exceptions **and** JCR `AccessDeniedException` are translated to)

> Where the table says "→ `URE`" for a JCR-level condition, the full cause chain is always
> `URE` → `DataRetrievalFailureException` → the specific JCR exception named in that cell
> (see §2.7 and §4). Only the innermost cause distinguishes a not-found/no-read condition
> from a no-write/no-delete condition — both wrap into the same outer two exception types.

| Method | ABS action | ABS denial | `accessVoterManager` check | Not-found-or-no-read-access | No-write-or-delete-access |
|---|---|---|---|---|---|
| `getFile` / `getFileById` (all overloads) | `repository.read` | `URADE` | READ on file → **`null`** if voter denies | <ul><li>`PathNotFoundException` / `ItemNotFoundException` caught inside the DAO callback → **`null`** (no exception; confounded with not-found)</li></ul> | N/A (read-only) |
| `getFileAtVersion` | `repository.read` | `URADE` | None | <ul><li>`ItemNotFoundException` **not** caught → propagates → `URE` (cause: `DataRetrievalFailureException` → `ItemNotFoundException`)</li></ul> | N/A (read-only) |
| `getChildren` | `repository.read` | `URADE` | None | <ul><li>**Folder itself** not found/no-read: `ItemNotFoundException` not caught (`session.getNodeByIdentifier`) → `URE`</li><li>**Individual children** not readable: Jackrabbit silently omits them from `folderNode.getNodes()` — no exception, entry simply absent from the returned list</li></ul> | N/A (read-only) |
| `getTree` | `repository.read` | `URADE` | READ on each visited node → excluded (no-op by default) | <ul><li>**Root path** not found/no-read: `PathNotFoundException` not caught (`session.getItem` in `getTree()`) → `URE`</li><li>**Descendant nodes**: voter check (no-op) + Jackrabbit silently omits unreadable children — no exception, node simply absent from the tree</li></ul> | N/A (read-only) |
| `getData*` (`getDataForRead`, `getDataAtVersionForRead`, `getDataForExecute`, `getDataAtVersionForExecute`, batch variants) | `repository.read` | `URADE` | READ on file (via `internalGetFileById`) → **`null`** if voter denies | <ul><li>`PathNotFoundException` / `ItemNotFoundException` caught inside `internalGetFileById` → **`null`** (confounded with not-found)</li></ul> | N/A (read-only) |
| `getAcl` / `getEffectiveAces` | `repository.read` | `URADE` | None | <ul><li>Node not found: `ItemNotFoundException` not caught (`session.getNodeByIdentifier` in `toAcl()`/`getEffectiveAces()`) → `URE`</li><li>Node exists but caller lacks `jcr:readAccessControl`: JCR throws `AccessDeniedException` instead — translated to the **same** `URE` (see §2.7), indistinguishable from not-found without inspecting the innermost cause</li></ul> | N/A (no write involved) |
| `hasAccess` | `repository.read` | `URADE` | None (is the check itself) | <ul><li>`PathNotFoundException` caught → returns `false` (no exception, for **any** permission set requested)</li></ul> | <ul><li>Returns `false` (same mechanism; `hasAccess` does not distinguish READ from WRITE/DELETE in its not-found handling)</li></ul> |
| `createFile` | `repository.create` | `URADE` | WRITE on **parent** → **`null`** if voter denies | <ul><li>Parent not found/no-read: `getFileById(parentId)` → `null`; the WRITE-voter check is then **skipped entirely** (only runs `if (parentRepositoryFile != null)`) and execution falls through to the JCR node lookup in `createFileNode`, which throws `ItemNotFoundException` (uncaught) → **`UnifiedRepositoryCreateFileException`** (not generic `URE` — this method has a method-specific fallback wrapper, §2.1)</li></ul> | <ul><li>Parent found but no `jcr:addChildNodes`: WRITE-voter → **`null`** (no-op by default)</li><li>Else: `AccessDeniedException` at `session.save()` (uncaught) → **`UnifiedRepositoryCreateFileException`** — this is the only JCR privilege actually required (see §2.4.8); no other node is touched</li></ul> |
| `createFolder` | `repository.create` | `URADE` | WRITE on **parent** → **`null`** if voter denies | <ul><li>Same not-found pattern as `createFile`, but this method has **no** fallback-constructor override (§2.1): `getFileById(parentId)` → `null`, WRITE-voter skipped, falls through to `createFolderNode`, which throws `ItemNotFoundException` (uncaught) → generic `URE`</li></ul> | <ul><li>Parent found but no `jcr:addChildNodes`: WRITE-voter → **`null`** (no-op by default)</li><li>Else: `AccessDeniedException` at `session.save()` (uncaught) → generic `URE`</li></ul> |
| `updateFile` | `repository.create` | `URADE` | WRITE on file → **`null`** if voter denies | <ul><li>Caller supplies the `RepositoryFile` directly (no lookup-by-id in this call); if the file was concurrently deleted, `aclDao.getAcl(file.getId())` → `ItemNotFoundException` (uncaught) → **`UnifiedRepositoryUpdateFileException`** (not generic `URE` — method-specific fallback wrapper, §2.1)</li></ul> | <ul><li>WRITE-voter → **`null`** (no-op by default)</li><li>Else: `AccessDeniedException` at `session.save()` (uncaught) → **`UnifiedRepositoryUpdateFileException`**</li></ul> |
| `updateFolder` | `repository.create` | `URADE` | **None** | <ul><li>Same pattern as `updateFile`, but no fallback-constructor override (§2.1): `ItemNotFoundException` if node vanished between calls (uncaught) → generic `URE`</li></ul> | <ul><li>**No voter check at all** — `AccessDeniedException` at `session.save()` (uncaught) → generic `URE`</li></ul> |
| `updateAcl` | `repository.create` | `URADE` | None in DAO; explicit `hasAccess(ACL_MANAGEMENT)` in `DefaultUnifiedRepository` | <ul><li>`getFileById(acl.getId())` returns `null` if not found/no-read → **`NullPointerException`** at `file.getPath()` in `DefaultUnifiedRepository.updateAcl()` (uncaught programming defect, not a Pentaho/JCR access exception)</li></ul> | <ul><li>`hasAccess(ACL_MANAGEMENT)` returns `false` → `URADE` thrown **directly** by `DefaultUnifiedRepository` (no `Throwable` cause at all — not routed through `ExceptionLoggingDecorator`'s converter map)</li><li>**Owner-ACE gap** (§2.4.8): the file's owner always passes every JCR write/delete check via the injected `jcr:all` owner ACE, but that ACE does **not** include `pho:aclManagement` — so an owner can still get `URADE` here unless separately granted `ACL_MANAGEMENT`</li></ul> |
| `deleteFile` (soft) | `repository.create` | `URADE` | DELETE → **`null`** if voter denies | <ul><li>`getFileById(fileId)` → `null` if not found/no-read; the `if (fileToBeDeleted != null)` guard means the voter/`aclDao.hasAccess` checks are **skipped**, and execution proceeds straight to `deleteHelper.deleteFile(...)` / `session.save()`, which throws `ItemNotFoundException` or `PathNotFoundException` (uncaught) → `URE`</li></ul> | <ul><li>DELETE-voter → **`null`** (no-op by default)</li><li>Voter passes but `aclDao.hasAccess(DELETE)` fails: explicit `AccessDeniedException` thrown (uncaught) → `URE` — this check only covers `jcr:removeNode` on the **file**; Magic ACE inheritance/owner injection (§2.4.8) can make it pass even without an explicit grant</li><li>Both pass but JCR itself denies at `session.save()`: `AccessDeniedException` (uncaught) → `URE` — this can fire for `jcr:removeChildNodes` on the **source parent** or `jcr:addChildNodes` on `.trash`, neither of which has a Pentaho-layer pre-check (§2.4.8, marked `⚠`)</li></ul> |
| `deleteFileAtVersion` | `repository.create` | `URADE` | DELETE → **`null`** if voter denies | <ul><li>Same guarded pattern as `deleteFile`: not-found falls through to JCR lookup → `ItemNotFoundException` (uncaught) → `URE`</li></ul> | <ul><li>DELETE-voter → **`null`** (no-op by default)</li><li>Else: `AccessDeniedException` at JCR remove call (uncaught) → `URE`</li></ul> |
| `permanentlyDeleteFile` | `repository.create` | `URADE` | DELETE → **`null`** if voter denies | <ul><li>Same guarded pattern as `deleteFile`: not-found falls through to JCR lookup → `ItemNotFoundException` (uncaught) → `URE`</li></ul> | <ul><li>DELETE-voter → **`null`** (no-op by default)</li><li>Also requires `jcr:removeChildNodes` on the **source parent** (§2.4.8, unchecked at the Pentaho layer) — denial there surfaces as `AccessDeniedException` (uncaught) → `URE`</li><li>**Distinct, unambiguous, non-access condition**: if other JCR nodes still hold references to this file, `DefaultDeleteHelper.permanentlyDeleteFile` throws `RepositoryFileDaoReferentialIntegrityException` **before** any JCR remove call — converted (top priority in the converter map, §2.1) to **`UnifiedRepositoryReferentialIntegrityException`**, not the generic `URE`/not-found-or-no-write ambiguity at all</li></ul> |
| `undeleteFile` | `repository.create` | `URADE` | WRITE → **`null`** if voter denies | <ul><li>Not-found on the deleted node: JCR lookup throws `ItemNotFoundException` (uncaught) → `URE`</li></ul> | <ul><li>WRITE-voter → **`null`** (no-op by default)</li><li>Else: `AccessDeniedException` at `session.move()` (uncaught) → `URE`</li><li>**Distinct, unambiguous, non-access condition**: if a file/folder now exists at the deleted item's original path, `DefaultDeleteHelper.undeleteFile` throws `RepositoryFileDaoFileExistsException` **before** the `session.move()` — converted (top priority, §2.1) to **`UnifiedRepositoryFileExistsException`**, not the generic `URE`/not-found-or-no-write ambiguity at all</li></ul> |
| `moveFile` (incl. rename) | `repository.create` | `URADE` | WRITE on source + dest | <ul><li>**Source** not found/no-read: `getFileById(fileId)` → `null`, but (unlike create/delete) the code does **not** guard on this — `aclDao.getAcl(fileId)` is called unconditionally and throws `ItemNotFoundException` (uncaught) → `URE`</li><li>**Destination's parent folder** not found/no-read: the destination path itself not existing is the normal case (e.g. a rename); it only becomes an error if the destination's *parent* is **also** missing/unreadable — that raises `IllegalArgumentException` (uncaught) → `URE`, not a JCR/access exception</li></ul> | <ul><li>**Source** WRITE denied: explicit `AccessDeniedException` thrown by DAO before the JCR call (uncaught) → `URE`</li><li>**Destination** WRITE denied: explicit `AccessDeniedException` thrown by DAO (uncaught) → `URE`</li><li>Pre-checks pass but JCR itself denies at `workspace.move()`: `AccessDeniedException` (uncaught) → `URE` — can also fire for `jcr:removeChildNodes` on the **source parent**, which has no Pentaho-layer pre-check at all (§2.4.8, marked `⚠`)</li></ul> |
| `copyFile` | `repository.create` | `URADE` | WRITE on **dest only** — source is **not** checked at either layer (see ../reference/repository-permission-model.md §"Notable Gaps") | <ul><li>**Source** not found/no-read: no pre-check exists; `session.getNodeByIdentifier(fileId)` is called directly and throws `ItemNotFoundException` (uncaught) → `URE`</li><li>**Destination's parent folder** not found/no-read: same condition and `IllegalArgumentException` outcome as `moveFile` above (the destination path itself not existing is normal and not an error)</li></ul> | <ul><li>**Destination** WRITE denied: explicit `AccessDeniedException` thrown by DAO (uncaught) → `URE`, or JCR itself denies at `workspace.copy()` → `URE`</li><li>Source read/write is never checked, so a user with no READ on the source can still copy it if they have WRITE on the destination</li></ul> |
| `lockFile` / `unlockFile` | `repository.create` | `URADE` | **None** | <ul><li>`ItemNotFoundException` if node vanished (uncaught) → `URE`</li></ul> | <ul><li>**No voter check** — `AccessDeniedException` at JCR lock/unlock call (uncaught) → `URE`</li></ul> |
| `canUnlockFile` | `repository.read` | `URADE` | **None** | <ul><li>`ItemNotFoundException` if node inaccessible (uncaught) → `URE`</li></ul> | N/A (read-only check) |
| `getVersionSummary` / `getVersionSummaryInBatch` / `getVersionSummaries` | `repository.read` | `URADE` | **None** | <ul><li>`ItemNotFoundException` if node inaccessible (uncaught) → `URE`</li><li>Node exists but a specific version node is unreadable: JCR may instead throw `AccessDeniedException`, translated to the same `URE`</li></ul> | N/A (read-only) |
| `restoreFileAtVersion` | `repository.create` | `URADE` | **None** | <ul><li>`ItemNotFoundException` if node inaccessible (uncaught) → `URE`</li></ul> | <ul><li>**No voter check** — `AccessDeniedException` at the version manager `restore()` call (uncaught) → `URE`</li></ul> |
| `setFileMetadata` | `repository.create` | `URADE` | **None** | <ul><li>`ItemNotFoundException`/`PathNotFoundException` if node inaccessible (uncaught) → `URE`</li></ul> | <ul><li>**No voter check** — `AccessDeniedException` at `session.save()` (uncaught) → `URE`</li><li>**Distinct, unambiguous, non-access condition**: if any metadata **key** (not the file/folder name) contains reserved characters, `JcrRepositoryFileUtils.setMetadataItemForFile` calls `checkName()` and throws `RepositoryFileDaoMalformedNameException` — converted (top priority, §2.1) to **`UnifiedRepositoryMalformedNameException`**. This is the *only* method that can throw it: the equivalent `checkName()` calls guarding file/folder names elsewhere in `JcrRepositoryFileUtils` are commented-out dead code.</li></ul> |
| `getFileMetadata` | `repository.read` | `URADE` | **None** | <ul><li>`ItemNotFoundException`/`PathNotFoundException` if node inaccessible (uncaught) → `URE`</li></ul> | N/A (read-only) |
| `getAvailableLocalesForFile*` | `repository.read` | `URADE` | READ (via `getFileById`) → **`null`** if voter denies | <ul><li>`PathNotFoundException`/`ItemNotFoundException` caught inside `getFileById` → **`null`** (confounded with not-found; downstream logic on `null` input just returns an empty list)</li></ul> | N/A (read-only) |
| `getLocalePropertiesForFile*` | `repository.read` | `URADE` | READ (via `getFileById`) → **`null`** if voter denies | <ul><li>Same as above: **`null`** input from `getFileById`, method returns `null` (data read from the in-memory `RepositoryFile`, no further JCR call)</li></ul> | N/A (read-only) |
| `setLocalePropertiesForFile*` | `repository.create` | `URADE` | READ on file (via `getFileById`) → **`null`** if voter denies; **no WRITE voter check** | <ul><li>`PathNotFoundException`/`ItemNotFoundException` caught inside `getFileById` → **`null`** (confounded with not-found)</li></ul> | <ul><li>**No WRITE voter check** — `AccessDeniedException` at `session.save()` (uncaught) → `URE`</li></ul> |
| `deleteLocalePropertiesForFile` | `repository.create` | `URADE` | **None** | <ul><li>`ItemNotFoundException`/`PathNotFoundException` if node inaccessible (uncaught) → `URE`</li></ul> | <ul><li>**No voter check** — `AccessDeniedException` at `session.save()` (uncaught) → `URE`</li></ul> |
| `getReferrers` | `repository.read` | `URADE` | **None** | <ul><li>`ItemNotFoundException` if file node inaccessible (uncaught) → `URE`</li></ul> | N/A (read-only, though it internally does an unconditional `session.save()`) |
| `getAllDeletedFiles` | *(none — no ABS guard)* | — | **None** | <ul><li>Jackrabbit session filtering; inaccessible entries silently excluded — no exception</li></ul> | N/A |
| `getReservedChars` | *(none — no ABS guard)* | — | **None** | None (pure in-memory) | N/A |

---

## 4. Exception taxonomy visible to callers

> **Class name legend** (simple names used below; only the Spring Security one is
> spelled out in full every time, to avoid confusion with the unrelated JCR exception
> of the same simple name):
>
> | Simple name used below | Fully qualified name |
> |---|---|
> | `UnifiedRepositoryAccessDeniedException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException` |
> | `UnifiedRepositoryException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException` |
> | `UnifiedRepositoryCreateFileException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryCreateFileException` (extends `UnifiedRepositoryException`) |
> | `UnifiedRepositoryUpdateFileException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryUpdateFileException` (extends `UnifiedRepositoryException`) |
> | `UnifiedRepositoryFileExistsException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryFileExistsException` (extends `UnifiedRepositoryException`) |
> | `UnifiedRepositoryMalformedNameException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryMalformedNameException` (extends `UnifiedRepositoryException`) |
> | `UnifiedRepositoryReferentialIntegrityException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryReferentialIntegrityException` (extends `UnifiedRepositoryException`) |
> | *(Spring Security)* `org.springframework.security.access.AccessDeniedException` | *(always spelled out — never abbreviated, to distinguish from the JCR exception below)* |
> | `AccessDeniedException` (JCR) | `javax.jcr.AccessDeniedException` |
> | `PathNotFoundException` | `javax.jcr.PathNotFoundException` |
> | `ItemNotFoundException` | `javax.jcr.ItemNotFoundException` |
> | `DataRetrievalFailureException` | `org.springframework.dao.DataRetrievalFailureException` |

For every row below marked "**direct cause**", `exception.getCause()` returns exactly that
class (one hop). For every row marked "**two-hop cause**", `exception.getCause()` returns
the intermediate class, and `exception.getCause().getCause()` returns the JCR exception —
see §2.7 for why this extra hop exists (the `JcrTemplate` → `SessionFactoryUtils.translateException()`
translation layer).

> **This table does not yet cover every distinct exception *type*, only the ambiguous
> not-found/no-write pair.** Five more `UnifiedRepositoryException` subclasses exist and are
> thrown for well-defined, **non**-ambiguous, **non**-access-control conditions — they should
> be caught (and, in code, checked) *before* falling back to generic disambiguation logic:
>
> | Exception (all extend `UnifiedRepositoryException`) | Thrown by (only) | Direct cause | When |
> |---|---|---|---|
> | `UnifiedRepositoryFileExistsException` | `undeleteFile` | `RepositoryFileDaoFileExistsException` (one hop — this DAO exception is a plain `RuntimeException`, not a `javax.jcr.RepositoryException`, so `JcrTemplate` never touches it, §2.7) | A file/folder already exists at the deleted item's original path. Not an access-control condition at all — do not run this through not-found/no-write disambiguation logic. |
> | `UnifiedRepositoryReferentialIntegrityException` | `permanentlyDeleteFile` | `RepositoryFileDaoReferentialIntegrityException` (one hop, same reasoning) | Other JCR nodes still hold references to the file being permanently deleted; thrown as an explicit pre-check *before* any JCR remove call. Not an access-control condition. |
> | `UnifiedRepositoryMalformedNameException` | `setFileMetadata` | `RepositoryFileDaoMalformedNameException` (one hop, same reasoning) | A metadata **key** (not the file/folder name) contains reserved characters. Not an access-control condition, and — despite the name — unrelated to file/folder naming (the equivalent checks for file/folder names elsewhere are dead code, §2.1). |
> | `UnifiedRepositoryCreateFileException` | `createFile` (both overloads) | *Same as the generic `URE` rows below* — this is a drop-in replacement for the outer class only, used **only** when no converter-map entry matched (§2.1) | Any not-found/no-write condition that would otherwise be a generic `URE` for `createFolder`. The cause-chain shape/depth underneath is identical to the generic-`URE` rows below — only the outermost class differs. |
> | `UnifiedRepositoryUpdateFileException` | `updateFile` | *Same as the generic `URE` rows below*, same caveat | Any not-found/no-write condition that would otherwise be a generic `URE` for `updateFolder`. |


| Exception thrown to caller | Direct cause (`getCause()`) | Underlying cause (`getCause().getCause()`) | When |
|---|---|---|---|
| `UnifiedRepositoryAccessDeniedException` | *(direct cause)* Spring Security's `org.springframework.security.access.AccessDeniedException` | *(none — Spring Security throws it with no wrapped cause)* | <ul><li>ABS method-level check fails in `unifiedRepositoryMethodInterceptor` (user lacks required global action)</li><li>This is the **only** case where `UnifiedRepositoryAccessDeniedException`'s cause is the Spring Security exception, because it is the only entry in the `ExceptionLoggingDecorator` converter map (§2.1) that targets `UnifiedRepositoryAccessDeniedException`</li></ul> |
| `UnifiedRepositoryAccessDeniedException` | *(no cause at all — `getCause()` is `null`)* | — | <ul><li>`updateAcl` — caller lacks `ACL_MANAGEMENT` on the specific file. Thrown directly by `DefaultUnifiedRepository`, bypassing `ExceptionLoggingDecorator`'s converter map entirely (§2.3)</li><li>**Disambiguation**: if `getCause()` is `null`, it is this direct-throw case, not the ABS-check case above</li></ul> |
| `null` return value | — | — | <ul><li>`accessVoterManager` voter denies READ/WRITE/DELETE (requires custom voters; no-op in default config — see §2.4.1)</li><li>**or** Jackrabbit's `PathNotFoundException`/`ItemNotFoundException` was caught **inside** the DAO callback and converted to `null` before ever reaching `ExceptionLoggingDecorator` (only for the specific methods listed in §2.4.5/§3 — `getFile`, `getFileById`, `getData*`, and the locale-property methods that route through them)</li><li>**Disambiguation**: a `null` return by itself does not distinguish "not found" from "no read access" from "voter denied" — all three are silent</li></ul> |
| Generic `UnifiedRepositoryException` (or `UnifiedRepositoryCreateFileException`/`UnifiedRepositoryUpdateFileException` — see note above the table) | *(two-hop cause)* `DataRetrievalFailureException` | `AccessDeniedException` (JCR) | <ul><li>Single-target write/mutate operations (`session.save()`, lock/version manager calls) where the user lacks the required JCR privilege on **the one node involved** — `createFolder` (parent; generic `URE`), `createFile` (parent; **`UnifiedRepositoryCreateFileException`**, not generic `URE`), `updateFolder` (generic `URE`), `updateFile` (**`UnifiedRepositoryUpdateFileException`**, not generic `URE`), `setFileMetadata`, `setLocalePropertiesForFile*`, `deleteLocalePropertiesForFile`, `lockFile`/`unlockFile`, `restoreFileAtVersion`</li><li>`deleteFile` — the explicit `aclDao.hasAccess(DELETE)` pre-check throws this for **the file itself** (not source/destination — single target)</li><li>`moveFile` — applies to **either or both paths**: the explicit `accessVoterManager.hasAccess(WRITE)` pre-check throws it for the **source** file, for the **destination** folder, or (if both pre-checks pass) the underlying `workspace.move()` JCR call can still throw it for either the source or the destination</li><li>`copyFile` — applies to the **destination** folder only (explicit pre-check or the underlying `workspace.copy()` JCR call); the **source** is never subject to a write/JCR-privilege check at all (see §3 `copyFile` row and ../reference/repository-permission-model.md's "Notable Gaps")</li></ul> |
| Generic `UnifiedRepositoryException` (or `UnifiedRepositoryCreateFileException`/`UnifiedRepositoryUpdateFileException` — see note above the table) | *(two-hop cause)* `DataRetrievalFailureException` | `PathNotFoundException` or `ItemNotFoundException` | <ul><li>Single-target read (or read-before-write) operations where the node genuinely does not exist **or** the user has no `jcr:read` privilege on it, and the DAO method did **not** catch the not-found exception internally — `getFileAtVersion`, `getChildren`'s folder lookup, `getTree`'s root lookup, `getAcl`, `getEffectiveAces`, `canUnlockFile`, `getVersionSummary*`, `getFileMetadata`, `getReferrers`, `restoreFileAtVersion`, `lockFile`/`unlockFile`, `setFileMetadata`, `deleteLocalePropertiesForFile`, `createFolder`/`updateFolder` (generic `URE`), `createFile`/`updateFile` (method-specific wrapper instead of generic `URE` — see §3)</li><li>`moveFile` — applies to the **source** file only: `aclDao.getAcl(fileId)` is called unconditionally and throws uncaught if the source is not found/unreadable. The **destination** does *not* go through this path — see the `IllegalArgumentException` row below</li><li>`copyFile` — applies to the **source** file only: `session.getNodeByIdentifier(fileId)` is called directly and throws uncaught if the source is not found/unreadable (recall source access is otherwise unchecked). The **destination** does *not* go through this path either</li></ul> |
| Generic `UnifiedRepositoryException` | `IllegalArgumentException` or `NullPointerException` | *(none)* | <ul><li>`moveFile` / `copyFile` — applies to the **destination's parent folder**, not the destination path itself (the destination path not existing is the normal case, e.g. a rename): if the destination doesn't exist *and* its parent folder is also missing/unreadable, an `Assert.isTrue` failure raises `IllegalArgumentException` (not a JCR/access exception at all — a distinct failure mode from the source-side `ItemNotFoundException` row above)</li><li>`updateAcl` — `NullPointerException` when `getFileById(acl.getId())` returns `null` (file not found/no-read) and the code dereferences `file.getPath()` before the ACL check runs (see §3 `updateAcl` row)</li></ul> |
| Generic `UnifiedRepositoryException` | Any other uncaught exception | *(varies; often none)* | Unexpected errors / programming defects at any layer, not access-control related. |

### Disambiguating same-looking exceptions

Because a write-denial and a not-found/no-read condition can both surface as the exact same
outer pair of classes (`UnifiedRepositoryException` → `DataRetrievalFailureException`), callers
that need to tell them apart must inspect the **innermost** cause:

```java
try {
    unifiedRepository.updateFolder(folder, "comment");
} catch (UnifiedRepositoryException e) {
    // Exactly two hops, per §2.7/§4 — URE -> DataRetrievalFailureException -> JCR
    // exception. Do NOT use a generic "walk to the bottom" root-cause helper here:
    // the chain is documented to be exactly this deep, and inspecting getCause()
    // directly (rather than the true root) keeps the check honest about that.
    Throwable underlying = (e.getCause() != null) ? e.getCause().getCause() : null;
    if (underlying instanceof javax.jcr.AccessDeniedException) {
        // user lacks JCR write privilege
    } else if (underlying instanceof javax.jcr.PathNotFoundException
            || underlying instanceof javax.jcr.ItemNotFoundException) {
        // node does not exist, or user has no read access to it (still indistinguishable from each other)
    }
    // else: some other underlying failure (e.getCause() may itself be null, or a
    // different intermediate class than DataRetrievalFailureException — see §2.7)
}
```

`ExceptionLoggingDecorator` itself uses a similar technique internally
(`ExceptionUtils.getThrowableList(e)`, reversed) to find a matching converter — see §2.1
and §2.7. That decorator-internal usage walks the full list because it must match
*any* converter-mapped class at *any* depth; the caller-side check above is narrower and
deliberately fixed at exactly two hops, because that is the depth the doc has verified for
every generic `UnifiedRepositoryException` (see §4).

> **This cause-chain depth is not a stable, public contract.** It reflects the current
> `se-jcr`/Jackrabbit/`ExceptionLoggingDecorator` wiring verified in this document, but
> `getCause()` structure is an internal implementation detail that could change with a
> library upgrade or an unrelated refactor — nothing in `IUnifiedRepository`'s public
> signatures documents or promises it. The snippet above is useful for *understanding*
> and for diagnostic logging, but application code that branches on it is brittle.
> For a disambiguation strategy that only uses public, documented `IUnifiedRepository`
> API calls (proactive `getFileById`/`hasAccess` checks instead of cause inspection),
> see **[`unified-repository-exception-disambiguation.md`](../reference/unified-repository-exception-disambiguation.md)**,
> which covers every method in the §3 table.

---

## 5. Key design observations

### The "not-found confounding" is enforced by Jackrabbit, not by Pentaho code

In the default configuration the `accessVoterManager` has no voters and is a no-op.
The `null` returns in `internalGetFile` and `internalGetFileById` on access denial are
caused by Jackrabbit throwing `PathNotFoundException` / `ItemNotFoundException`, which
the DAO catches and converts to `null`. The `accessVoterManager.hasAccess(READ)`
checks that follow are dead code unless custom voters are registered.

### Jackrabbit is the universal per-file enforcement layer

Every DAO method that executes JCR operations uses a user-credential session. Jackrabbit
enforces its own ACLs on every call with no opt-out. For writes this means
`javax.jcr.AccessDeniedException` is always a possible outcome even when
`accessVoterManager` passes.

### `accessVoterManager` is a pure extension point

Its purpose is to allow plugins to impose additional restrictions beyond what JCR ACLs
alone would enforce (e.g., based on content type, tenant policy, etc.). In the shipped
product it has no effect.

### Write denials from JCR surface as generic exceptions — and look identical to not-found errors

`javax.jcr.AccessDeniedException` is not in the `ExceptionLoggingDecorator` converter map.
Worse, by the time it reaches `ExceptionLoggingDecorator` it has already been translated by
`JcrTemplate` into `org.springframework.dao.DataRetrievalFailureException` (§2.7) — the
**exact same class** that `PathNotFoundException`/`ItemNotFoundException` are translated
into. So a write-denial and a not-found/no-read condition both surface as
`UnifiedRepositoryException` wrapping a `DataRetrievalFailureException`, and are only
distinguishable by inspecting the innermost cause (`getCause().getCause()`, see §4). Callers
that want to detect access denial specifically for write operations must catch
`UnifiedRepositoryException`, walk to the root cause, and check for `javax.jcr.AccessDeniedException`
— or call `hasAccess()` proactively before performing the operation.

### `updateAcl` is the only method with an explicit per-file check at the `DefaultUnifiedRepository` level

All other per-file access decisions are delegated to the JCR DAO layer and ultimately
to Jackrabbit's own ACL enforcement.

### `getChildren` vs `getTree` differ in voter usage

`getChildren` has no `accessVoterManager` call; filtering is purely Jackrabbit native.
`getTree` calls `accessVoterManager.hasAccess(READ)` for every node it visits — which
in the default config is also a no-op, but would allow custom voters to prune the tree
beyond what JCR ACLs allow.

### The confounding is unnecessary when the caller already knows the file exists

For operations where the caller already has READ access (they must, to know the file
exists), the silent `null` return in write/delete paths is less justified. These methods
still return `null` when a custom voter denies, making it harder for callers to
distinguish a permission denial from a programming error.

---

## Appendix A: Per-operation disambiguation snippets

Moved to a dedicated file — **[`unified-repository-exception-disambiguation.md`](../reference/unified-repository-exception-disambiguation.md)** —
which provides a public-API-only (no cause-chain inspection) disambiguation snippet for
every method in the §3 summary table, plus the reasoning for why cause-chain
inspection (as shown informationally in §4) is unsuitable for production application code.

## Appendix B: `FileService` / `RepositoryFileProvider` layer

The `IUnifiedRepository` bean documented above is also consumed indirectly, via
`FileService`, by the Generic File System (GFS) `RepositoryFileProvider`. `FileService`
adds its own (inconsistent) exception-handling and access-check logic on top of the rules
documented here — see **[`file-service-access-control.md`](./file-service-access-control.md)**
for the full analysis.
