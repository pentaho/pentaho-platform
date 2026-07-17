---
type: architecture
title: JcrRepositoryFileDao Layer
description: File-path-level access control in `JcrRepositoryFileDao`, including the access voter, native ACL enforcement, kiosk mode, read/write behavior, and magic ACE caveats.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# `JcrRepositoryFileDao` – file-path-level access

Two independent enforcement mechanisms operate at this layer:

- **`accessVoterManager` (`RepositoryAccessVoterManager`)** — an explicit Pentaho extension point.
- **Jackrabbit native JCR session ACL enforcement** — implicit, always active for every JCR API call.

## 2.4.1 `accessVoterManager` has no voters in the default configuration

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

## 2.4.2 Jackrabbit native JCR session ACL enforcement

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

## 2.4.3 Bypass for privileged users (`accessVoterManager` only)

The `accessVoterManager` voter loop is **skipped entirely** when:

- The current user has the `administerSecurity` ABS action, **or**
- The session username equals `repositoryAdminUsername`.

Since the voter manager already has no voters by default, this bypass only matters when
custom voters have been registered. It does **not** bypass Jackrabbit's own JCR
enforcement.

## 2.4.4 Kiosk mode

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

## 2.4.5 Read operations – the "not-found confounding" pattern

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

## 2.4.6 Children and tree traversal

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

## 2.4.7 Write operations – mixed behaviour

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

## 2.4.8 Per-node JCR privilege requirements and Magic ACE caveats

The Pentaho-layer voter/aclDao pre-checks (§2.4.1, §2.5) only ever evaluate **one** node per
operation (the file, or the destination folder). The underlying JCR call frequently touches
**additional** nodes that have no corresponding Pentaho-layer pre-check. If the user is
missing the JCR privilege on one of those un-checked nodes, JCR throws
`javax.jcr.AccessDeniedException` there instead, with the same downstream translation and
surfacing as a generic `UnifiedRepositoryException` (§2.7, §4) — but for a node the caller
may not have expected to need permission on.

The full per-operation table of JCR calls, nodes touched, Pentaho-layer checks, and required
JCR privileges (after Magic ACE injection) is the authoritative
[access-control pre-check layer reference](../../reference/unified-repository/permissions/pre-check-layer.md) —
this directly affects whether the `javax.jcr.AccessDeniedException` from §2.4.7 fires **at
all**, not just what it looks like once thrown.

### Magic ACE injection can make a JCR privilege check pass unexpectedly

`PentahoEntryCollector` (invoked by both `aclDao.hasAccess()` and native JCR
`hasPrivileges()`/enforcement) injects ephemeral, never-persisted ACEs that are invisible to
`JcrRepositoryFileAclDao.getAcl()` — see the [Magic ACEs reference](../../reference/unified-repository/permissions/magic-aces.md)
for the full mechanics (owner ACE, inheritance transformation, and config-yaml ACEs). In
short: an "expected" `AccessDeniedException` at one of the JCR calls above may **not** occur
even though the visible ACL (via `getAcl()`) suggests it should — and, conversely, a caller
cannot rely on `getAcl()` alone to predict whether a write/delete operation will succeed.
This is why `DELETE` on an inheriting child can succeed even when `DELETE` on the parent
folder itself would not — see [why DELETE on an inherited node passes when DELETE on its parent does not](../../reference/unified-repository/permissions/pre-check-layer.md#why-delete-on-an-inherited-node-passes-when-delete-on-its-parent-does-not).

### Owner-ACE gap: owners can still get `URADE` from `updateAcl`

The owner ACE (`jcr:all`, see [Magic ACEs](../../reference/unified-repository/permissions/magic-aces.md)) does **not** include the custom `pho:aclManagement` privilege.
This means the creator of a file — who passes every JCR write/delete/lock/version check via
the owner ACE — can still receive `UnifiedRepositoryAccessDeniedException` from `updateAcl`
(§2.3, §3) unless they also hold an explicit ACE granting `ACL_MANAGEMENT`. This is a
permanent condition for as long as they remain the owner; see
[Owner cannot manage ACL on their own resource](../../reference/unified-repository/permissions/known-issues.md) for the full analysis.

---


