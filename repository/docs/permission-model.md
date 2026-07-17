# Pentaho Repository Permission Model

## Layers Overview

```
Caller (DashboardRenderer, REST endpoint, etc.)
  │
  ├─ [LAYER 1] IAuthorizationPolicy.isAllowed(actionName)
  │     Pentaho system-level policy (e.g. RepositoryCreateAction)
  │     Checked BEFORE touching the repository.
  │
  └─ IUnifiedRepository (DefaultUnifiedRepository)
       │
       ├─ [LAYER 2a] IAccessVoterManager.hasAccess(file, permission, acl, session)
       │     Plugin-extensible voter. Default impl is a no-op (accepts everything).
       │     Intended for application-level rules above JCR — not for ACL reads.
       │
       └─ [LAYER 2b] IRepositoryFileAclDao.hasAccess(path, EnumSet<RepositoryFilePermission>)
              JcrRepositoryFileAclDao
              │
              └─ session.getAccessControlManager().hasPrivileges(absPath, jcrPrivileges)
                    Native Jackrabbit JCR privilege check.
```

---

## Pentaho Permission Enum → JCR Privileges

Defined in `RepositoryFilePermission`. Mapped in `DefaultPermissionConversionHelper.initMaps()`.

| `RepositoryFilePermission` | JCR Privileges |
|---|---|
| `READ` | `jcr:read`, `jcr:readAccessControl` |
| `WRITE` | `jcr:addChildNodes`, `jcr:removeChildNodes`, `jcr:modifyProperties`, `jcr:nodeTypeManagement`, `jcr:modifyAccessControl`, `jcr:versionManagement`, `jcr:lockManagement` |
| `DELETE` | `jcr:removeNode` |
| `ACL_MANAGEMENT` | `{http://www.pentaho.org/jcr/2.0}aclManagement` (custom Pentaho privilege) |
| `ALL` | `jcr:all` + `pho:aclManagement` |

> **Note on DELETE:** Comment in source says `jcr:removeNode` was also required for WRITE, so DELETE was considered redundant if you had WRITE. The enum still exists and is used in delete checks.

---

## ACL Model

### RepositoryFileAcl
- Attached to every node (file or folder).
- Has an **owner** (`RepositoryFileSid`).
- Has a list of **ACEs** (`RepositoryFileAce`): each ACE is a `(RepositoryFileSid, EnumSet<RepositoryFilePermission>)` pair.
- Has **`isEntriesInheriting`** flag (see Inheritance below).

### RepositoryFileSid
- Represents a user or role.
- Type: `USER` or `ROLE`.

### Stored in JCR
- ACL metadata (owner, entriesInheriting flag) stored as a special ACE entry via `AclMetadata` / `JcrRepositoryFileAclUtils`.
- The actual JCR ACL is stored using Jackrabbit's `AccessControlList` on the node path.

---

## Inheritance

- When `RepositoryFileAcl.isEntriesInheriting() == true`:  
  No explicit ACEs are stored for that node in JCR (only the metadata ACE — see Magic ACEs below).  
  `PentahoEntryCollector` walks up to the nearest non-inheriting ancestor and uses its ACEs.
- When `isEntriesInheriting() == false`:  
  The node has explicit ACEs stored in JCR. Jackrabbit uses them directly for privilege evaluation.

At check time, `session.getAccessControlManager().hasPrivileges(path, privs)` is called.  
**`PentahoEntryCollector`** (Jackrabbit's custom `EntryCollector`) intercepts this and injects Magic ACEs before returning the effective privilege set. See below.

### `getEffectiveAces(fileId, forceEntriesInheriting)`
- `forceEntriesInheriting=true`: walks up ancestor nodes collecting ACEs until it finds a node with `entriesInheriting=false`.
- Returns the merged effective set visible to the caller.

---

## Magic ACEs

Magic ACEs are **ephemeral ACEs injected at privilege evaluation time** by `PentahoEntryCollector`. They are **never persisted** to JCR. They exist only in memory during a `hasPrivileges()` call and are invisible to `getAcl()`.

There are three sources of Magic ACEs:

### 1. Owner ACE (`addOwnerAce`)

Every node stores an **owner** in its ACL metadata (set during file/folder creation to the creating user).

When `hasPrivileges(nodeX, ...)` is evaluated:
- `PentahoEntryCollector` reads the owner from the first access-controlled ancestor of `nodeX`.
- The owner is injected as `MagicPrincipal` with **`jcr:all`** into the effective ACL (in-memory only).

**Effect:** The owner of any node always has `jcr:all` on it — including `jcr:removeNode`, `jcr:addChildNodes`, etc. — regardless of what the persisted ACEs say.

> **`jcr:all` ≠ Pentaho `ALL`.** `pho:aclManagement` is a custom privilege registered outside the JCR privilege tree. `jcr:all` does **not** include it. The owner magic ACE only injects `jcr:all`, so the owner does **not** automatically get `ACL_MANAGEMENT`. A node's owner can read, write, and delete it, but **cannot change its permissions** unless they also have an explicit `ALL` ACE on it (or are an admin).

### 2. Inheritance Transformation (`getEntries`, lines 168–190)

When a node `nodeX` has `isEntriesInheriting=true`, its effective ACEs come from the nearest non-inheriting ancestor (e.g., parent folder `P`).

**Rule:** If `P`'s ACL contains an ACE with `jcr:removeChildNodes` but **not** `jcr:removeNode`, `PentahoEntryCollector` **injects `jcr:removeNode`** for that principal into the effective ACE set for `nodeX`.

```java
// PentahoEntryCollector.getEntries(), lines 168–190
if ( !currentNode.isSame( node ) ) {   // node inherits; currentNode = non-inheriting ancestor
    for ( AccessControlEntry entry : acl.getEntries() ) {
        if ( has jcr:removeChildNodes && NOT jcr:removeNode ) {
            acl.addAccessControlEntry( principal, [jcr:removeNode] );  // in-memory only
        }
    }
}
```

**Rationale (from source comment):** *"If we're inheriting from another node, check to see if that node has removeChildNodes or addChildNodes permissions. This needs to transform to become addChild removeChild."*

**Effect:** WRITE on a folder implicitly grants `jcr:removeNode` on all its inheriting children. A user with WRITE on folder `P` can delete any direct or indirect child that inherits from `P`.

**Asymmetry:** This injection only fires when the node is inheriting (`!currentNode.isSame(node)`). It does NOT fire when evaluating access on `P` itself. So WRITE on `P` lets you delete children of `P` but **not `P` itself**.

### 3. Config-yaml Magic ACEs (`config.yaml` / `MagicAceDefinition`)

System-level RBAC-based ACEs loaded from `jcr/config.yaml` and injected dynamically for any user who holds the matching logical role. Not persisted.

| id | Logical Role | Privileges | Applies to |
|---|---|---|---|
| 0 | `org.pentaho.security.administerSecurity` | `jcr:all` | Tenant root + all children |
| 1 | `org.pentaho.repository.read` | `jcr:read`, `jcr:readAccessControl` | Tenant root itself + ancestors (NOT children) |
| 2 | `org.pentaho.repository.read` | `jcr:read`, `jcr:readAccessControl` | `/etc` + children (except `/etc/pdi/databases`) |
| 3 | `org.pentaho.repository.create` | `jcr:read`, `jcr:readAccessControl`, `jcr:write`, `jcr:modifyAccessControl`, `jcr:lockManagement`, `jcr:versionManagement`, `jcr:nodeTypeManagement` | `/etc` + children |
| 4 | `org.pentaho.security.publish` | same as id=3 | `/etc` + children |

> Config-yaml ACEs apply to the **`/etc` subtree and tenant root area only** — not to the general content tree (`/public`, user home folders, etc.).

---

## Purpose of the Pre-Check Layer (aclDao + voter)

`JcrRepositoryFileAclDao.hasAccess()` is not a parallel permission system — it directly calls `session.getAccessControlManager().hasPrivileges()`, the same Jackrabbit evaluator that the JCR operation itself would invoke. The same `PentahoEntryCollector` runs, the same Magic ACEs are injected, the same result is produced.

The pre-check layer exists for three reasons:

1. **Voter extension point.** `IAccessVoterManager` runs before the aclDao check and is the intended hook for application-level rules (RBAC, session-based policy, etc.) that sit above JCR's own model. It is not meant to read or evaluate the ACL — that is the aclDao's job. The default implementation is a no-op that accepts everything. If no custom voters are registered, this layer has no effect.

2. **Early, meaningful errors.** Letting the JCR operation throw yields a raw Jackrabbit `AccessDeniedException` referencing internal JCR paths. The pre-check throws a Pentaho-level exception referencing the file ID and operation, easier to surface to users and log coherently.

3. **Portability.** `IRepositoryFileAclDao` is an interface. The rest of `JcrRepositoryFileDao` does not depend on JCR semantics directly; the implementation could theoretically be swapped.

**Design intent:** the pre-check should mirror *exactly* the privileges the underlying JCR operation will require, so the pre-check is always authoritative and the JCR operation never produces a surprise denial. Where this breaks down — wrong privilege checked, wrong node checked, or check missing entirely — is where bugs arise.

---


Two layers fire per operation. Both must pass.

- **Pentaho voter** (`accessVoterManager`) — fires before the JCR call. Default impl is a no-op. Custom voters can enforce application-level rules (not ACL reads — those belong to the aclDao layer).
- **JCR native** (Jackrabbit `AccessControlManager` + `PentahoEntryCollector`) — fires inside the session operation, evaluates privileges including Magic ACEs.

`⚠` = gap between layers (Pentaho voter does not check this node; only JCR does).

| Operation | JCR call | Node | Pentaho check | JCR privilege (after Magic ACE injection) | Deny behaviour |
|---|---|---|---|---|---|
| **getFile** | `session.getItem()` | file | `READ` (voter) | `jcr:read` | returns null |
| **getTree** | `session.getItem()` | each node | `READ` (voter, per node) | `jcr:read` | skips node |
| **createFile** | `node.addNode()` | parent folder | `WRITE` (voter) | `jcr:addChildNodes` | returns null / JCR throws |
| **createFolder** | `node.addNode()` | parent folder | `WRITE` (voter) | `jcr:addChildNodes` | returns null / JCR throws |
| **updateFile** | `node.setProperty()` | file | `WRITE` (voter) | `jcr:modifyProperties` | returns null / JCR throws |
| **deleteFile** (soft) | `session.move(file → .trash)` | file | `DELETE` (voter + aclDao) | `jcr:removeNode` ¹ | null / `AccessDeniedException` |
| **deleteFile** (soft) | `session.move(file → .trash)` | ⚠ source parent | — | `jcr:removeChildNodes` | JCR throws |
| **deleteFile** (soft) | `session.move(file → .trash)` | ⚠ `.trash` folder | — | `jcr:addChildNodes` | JCR throws |
| **permanentlyDeleteFile** | `fileNode.remove()` | file | `DELETE` (voter) | `jcr:removeNode` | returns null / JCR throws |
| **permanentlyDeleteFile** | `fileNode.remove()` | ⚠ source parent | — | `jcr:removeChildNodes` | JCR throws |
| **moveFile** / **rename** | `workspace.move(src, dest)` | file (source) | `WRITE` (voter) | — | `AccessDeniedException` |
| **moveFile** / **rename** | `workspace.move(src, dest)` | dest folder | `WRITE` (voter) | `jcr:addChildNodes` | `AccessDeniedException` / JCR throws |
| **moveFile** / **rename** | `workspace.move(src, dest)` | ⚠ source parent | — | `jcr:removeChildNodes` | JCR throws |
| **copyFile** | `workspace.copy(src, dest)` | dest folder | `WRITE` (voter) | `jcr:addChildNodes` | `AccessDeniedException` / JCR throws |
| **copyFile** | `workspace.copy(src, dest)` | ⚠ source file | — | — ² | — |
| **undeleteFile** | `session.move(.trash → orig)` | file | `WRITE` (voter) | — | returns null |
| **updateAcl** | ACL update | file | `ACL_MANAGEMENT` (aclDao, in `DefaultUnifiedRepository`) | `pho:aclManagement` | throws |
| **getAcl** | ACL read | — | none | `jcr:readAccessControl` (implicit) | — |
| **lockFile / unlockFile** | JCR lock | — | kiosk only | `jcr:lockManagement` | throws on kiosk / JCR throws |

¹ `deleteFile` is implemented as `session.move(file → .trash)`. The Pentaho `aclDao.hasAccess(file, DELETE)` check asks for `jcr:removeNode` on the file. Whether this passes depends on Magic ACE injection (see below).  
² `copyFile` does not check source permissions at either layer. A user with WRITE on the destination can copy any file they can reference by ID, regardless of READ access on the source.

> **rename** = `moveFile` to same parent folder with a new name — identical permission checks.  
> **`WRITE`** maps to `jcr:addChildNodes` + `jcr:removeChildNodes` + `jcr:modifyProperties` + `jcr:nodeTypeManagement` + `jcr:modifyAccessControl` + `jcr:versionManagement` + `jcr:lockManagement` — so granting WRITE on a folder implicitly grants all those JCR privileges.

### Why `DELETE` on an inherited node passes when `DELETE` on its parent does not

The `aclDao.hasAccess(file, {DELETE})` check evaluates `jcr:removeNode` via Jackrabbit's `hasPrivileges()`. `PentahoEntryCollector` runs before the evaluation returns and injects Magic ACEs:

1. **Inheritance transformation**: if `file.isEntriesInheriting=true`, the parent folder's `jcr:removeChildNodes` gets injected as `jcr:removeNode` on `file`. `hasPrivileges(file, [jcr:removeNode])` = **true**.
2. **Owner ACE**: if the current user is the owner of `file`, `jcr:all` is injected → includes `jcr:removeNode` → **true**.

Neither fires when evaluating `hasPrivileges` on the **parent folder itself** (which has `isEntriesInheriting=false` and was not created by the user). Hence the asymmetry: WRITE on a folder lets you delete its children but not the folder itself.

---

## Notable Gaps / Gotchas

1. **WRITE on a folder = implicit DELETE on its children.** Via the Magic ACE inheritance transformation, any user with WRITE on folder `P` can delete any child of `P` that inherits its ACL. This is intentional (`PentahoEntryCollector.getEntries()` lines 168–190) but not obvious from the ACL UI.

2. **Owner always has `jcr:all` (but not `pho:aclManagement`).** The creator of any node gets `jcr:all` injected at privilege evaluation time (`PentahoEntryCollector.addOwnerAce()`). This covers all JCR-native operations (read, write, delete) but does **not** include the custom `pho:aclManagement` privilege. Owners cannot manage ACLs on their own nodes unless they also have an explicit `ALL` ACE. This is permanent as long as they remain the owner. Not visible via `JcrRepositoryFileAclDao.getAcl()`.

3. **`JcrRepositoryFileDao.copyFile()` does NOT check source permissions** at the Pentaho layer — only destination WRITE. A user with no READ on the source can trigger a copy by file ID if they have WRITE on the destination. Jackrabbit does not add a source-read check for `workspace.copy()`.

4. **`JcrRepositoryFileDao.deleteFile()` is a move to `.trash`**, not a true removal. The file is moved to the user's `.trash` folder via `session.move()`. `permanentlyDeleteFile()` calls `fileNode.remove()` and is the true deletion.

5. **`JcrRepositoryFileDao.deleteFile()` has double-check**: `accessVoterManager` is checked first (returns null silently if denied), then `JcrRepositoryFileAclDao.hasAccess()` throws `AccessDeniedException`. The voter can veto silently; JCR still enforces natively.

6. **`updateAcl` check is in `DefaultUnifiedRepository`**, not in `JcrRepositoryFileAclDao`. It explicitly calls `JcrRepositoryFileAclDao.hasAccess(file.getPath(), {ACL_MANAGEMENT})` before delegating to the DAO.

7. **Kiosk mode** (`JcrRepositoryFileDao.isKioskEnabled()`): blocks all write operations (create, update, delete, move, copy, lock) system-wide before any ACL check.

8. **`WRITE` includes `jcr:modifyAccessControl`** in the `DefaultPermissionConversionHelper` mapping — meaning a user with WRITE can technically modify the JCR-level ACL directly, though the Pentaho `updateAcl` path enforces `ACL_MANAGEMENT` separately.

9. **Magic ACEs are invisible.** `JcrRepositoryFileAclDao.getAcl()` strips `IPentahoInternalPrincipal` entries via `JcrRepositoryFileAclUtils.removeAclMetadata()`. Owner ACEs and config-yaml ACEs are never stored in JCR. The effective permissions a user has can therefore be significantly larger than what `getAcl()` returns.

---

## Layer 1 Policy Actions (IAuthorizationPolicy)

These are role-based policy checks, separate from per-file ACLs. Checked before any repository operation in high-level callers (e.g., `DashboardRenderer`).

| Action | Meaning |
|---|---|
| `RepositoryCreateAction.NAME` | User can create/edit repository content |

Configured via Pentaho security roles. Implementation: `IAuthorizationPolicy.isAllowed(String actionName)`.

---

## Key Classes Reference

| Class | Location | Role |
|---|---|---|
| `RepositoryFilePermission` | `api/.../repository2/unified/` | Permission enum (READ/WRITE/DELETE/ACL_MANAGEMENT/ALL) |
| `IPentahoJCRPrivilege` | `api/.../repository2/unified/` | Custom JCR privilege constant (`pho:aclManagement`) |
| `DefaultUnifiedRepository` | `repository/.../unified/` | `IUnifiedRepository` impl; entry point; enforces `updateAcl` check |
| `JcrRepositoryFileDao` | `repository/.../unified/jcr/` | JCR file operations; enforces per-op `accessVoterManager` checks |
| `JcrRepositoryFileAclDao` | `repository/.../unified/jcr/` | JCR ACL operations; implements `hasAccess()` via JCR `AccessControlManager` |
| `DefaultPermissionConversionHelper` | `repository/.../unified/jcr/` | Maps `RepositoryFilePermission` ↔ JCR `Privilege` |
| `JcrRepositoryFileAclUtils` | `repository/.../unified/jcr/` | ACL metadata, inheritance resolution, ACE expansion |
| `IAccessVoterManager` | `repository/.../unified/jcr/` | Plugin voter interface |

---

## Summary: Full Call Chain for `hasAccess`

```
DefaultUnifiedRepository.hasAccess(path, permissions)
  └─ JcrRepositoryFileAclDao.hasAccess(relPath, permissions)
       └─ [JCR session callback]
            ├─ DefaultPermissionConversionHelper.pentahoPermissionsToPrivileges(session, permissions)
            │    └─ looks up permissionEnumToPrivilegeNamesMap
            │    └─ calls session.getAccessControlManager().privilegeFromName(privName)
            │    └─ returns Privilege[]
            └─ session.getAccessControlManager().hasPrivileges(absPath, Privilege[])
                 └─ Jackrabbit walks JCR node tree, resolving inherited ACLs
                 └─ returns boolean
```

---

---

# Permission Semantics & Use Cases

> This section is a standalone guide. It covers what each permission does in practice, how to configure common scenarios, and how to control deletion boundaries.

---

## Permission hierarchy (UI vs API)

The **File Share/Permissions UI** enforces a cumulative hierarchy:

```
ALL  ⊃  DELETE  ⊃  WRITE  ⊃  READ
```

Selecting a higher permission in the UI automatically includes all lower ones. `ACL_MANAGEMENT` is only available as part of `ALL`.

This hierarchy is **enforced only by the UI**. The backend API and direct ACL management accept any arbitrary combination. The distinctions below apply to what is expressible via the UI unless otherwise noted.

| Expressible via UI | READ | WRITE | DELETE | ACL_MANAGEMENT | ALL |
|---|:---:|:---:|:---:|:---:|:---:|
| READ only | ✅ | | | | |
| WRITE (+ READ) | ✅ | ✅ | | | |
| DELETE (+ WRITE + READ) | ✅ | ✅ | ✅ | | |
| ALL (+ DELETE + WRITE + READ + ACL_MANAGEMENT) | ✅ | ✅ | ✅ | ✅ | ✅ |
| ACL_MANAGEMENT without ALL | API only | | | ✅ | |
| DELETE without WRITE | API only | | ✅ | | |

---

## What each permission means

### On a **file**

| Permission | What it allows |
|---|---|
| `READ` | View and open the file. See it in folder listings. |
| `WRITE` | Edit the file's content. Update its title/description. Rename it (rename is a move). Move it to another folder. |
| `DELETE` | Delete the file. Only relevant when the file has its own explicit ACL (i.e. "Inherit Permissions" is off). When "Inherit Permissions" is on, the parent folder's `WRITE` already covers deletion — see below. |
| `ACL_MANAGEMENT` | Change who has access to the file (add/remove/modify ACEs). Does not imply the ability to read or edit the file. |
| `ALL` | All of the above. |

### On a **folder**

| Permission | What it allows |
|---|---|
| `READ` | See the folder in listings. List its direct children (those that also grant READ, by inheritance or explicitly). Does **not** allow creating or modifying anything inside. |
| `WRITE` | Create files and subfolders inside it. Edit, rename, and move items inside it. **Implicitly allows deleting any child that inherits permissions from this folder** — see the Deletion Rules section below. |
| `DELETE` | Delete this folder itself (only when "Inherit Permissions" is off). **Via the UI, DELETE always includes WRITE** (hierarchy enforced). This means granting DELETE on a folder via the UI also implicitly enables deletion of all its inheriting children — not just the folder itself. See the note below. |
| `ACL_MANAGEMENT` | Change who has access to this folder. Does not imply read or write access. Only available via ALL in the UI. |
| `ALL` | All of the above. |

> **Via the UI, DELETE always brings WRITE.** The child-deletion side effect comes entirely from WRITE (via the inheritance injection rule) — not from DELETE. DELETE's own contribution is narrow: the ability to delete the folder itself. WRITE is what enables deletion of all inheriting descendants. So granting DELETE via the UI = WRITE (child delete) + DELETE (self delete).
>
> If only the folder itself should be deletable (not its children), this requires API-level ACL management: grant DELETE without WRITE on `F` directly. Even then, WRITE on `F`'s parent is still required by JCR for the move-to-trash operation.

> **Inheriting DELETE is redundant when the parent also has WRITE.** WRITE on a parent already injects `jcr:removeNode` on all inheriting children via `PentahoEntryCollector` — DELETE's `jcr:removeNode` would be a duplicate. DELETE's primary unique effect is enabling deletion of the node that *holds* the explicit ACL itself (where the injection never fires).
>
> **Exception — DELETE without WRITE (API only):** No injection fires (no `jcr:removeChildNodes`), but inheriting children still receive `jcr:removeNode` directly via standard JCR ACL inheritance. Inheriting files can therefore be deleted via DELETE alone, without WRITE. This is the one case where DELETE has distinct downstream value — a delete-only grant with no create/modify rights. Since the UI always bundles WRITE with DELETE, this is only reachable via the API.
>
> **API edge case — DELETE without WRITE on a parent:** children inherit `jcr:removeNode` (can be deleted) but not `jcr:addChildNodes` / `jcr:removeChildNodes` (cannot create or modify content inside them). A deletable-but-immutable subtree. No practical use case; unreachable via the UI.

---

## Deletion rules

### Rule 1 — Owner always wins (except ACL management)

The user who **created** a file or folder is its owner. The owner always has full JCR access to that item — including the right to read, edit, and delete it — regardless of what the ACL says. This is enforced silently at the JCR level and is not visible in the ACL editor.

**Exception:** ownership does **not** grant `ACL_MANAGEMENT`. The owner magic ACE injects `jcr:all`, which is a JCR-native aggregate privilege. `pho:aclManagement` is a custom Pentaho privilege registered outside the JCR tree, so `jcr:all` does not include it. An owner who does not have an explicit `ALL` ACE will **not** be able to change permissions on their own node — the UI will not show the permissions panel for them.

### Rule 2 — WRITE on a folder = implicit delete on its children

If a user has `WRITE` on folder `F`, they can delete **any child of `F` that inherits permissions from `F`** (i.e. "Inherit Permissions" is on). This propagates recursively: if child folder `C` also has "Inherit Permissions" on, the user can delete `C`'s children too, and so on down the tree.

This is not a bug — it is by design. The rationale: if you can add and remove children from a folder, you can remove any individual child.

### Rule 3 — Explicit ACL = independent deletion boundary

A file or folder with **"Inherit Permissions" off** (its own explicit ACL) is a **deletion boundary**. The implicit delete grant from the parent's `WRITE` does **not** flow across this boundary. To delete such a node, the user must have:

- `DELETE` explicitly on that node, **or**
- `ALL` explicitly on that node, **or**
- be the **owner** of that node.

Having `WRITE` on the parent is **not enough** to delete a child that has its own explicit ACL.

---

## Common configurations

### Read-only access to a folder tree

Grant `READ` on the top folder. Enable "Inherit Permissions" on all descendants (the default when content is created). The user can browse and open files but cannot create, edit, or delete anything.

### Read-write access — create, edit, delete anything inside

Grant `WRITE` on the top folder. Enable "Inherit Permissions" on descendants. The user can create, edit, rename, move, and delete any item in the tree.

### Read-write but protect a specific file from deletion

1. Open the specific file's permissions.
2. Turn off "Inherit Permissions" (creates an explicit ACL boundary).
3. Grant `WRITE` (but **not** `DELETE`) explicitly.

The user can read and edit the file's content but cannot delete it. The parent folder's `WRITE` no longer reaches across the boundary.

> The user's `WRITE` on the file also means they have `jcr:removeChildNodes` on it — but since the file has no inheriting children, this has no effect.

### Allow deletion of a specific folder (but not the rest of the parent's content)

You want user U to delete folder `F` (which has its own explicit ACL), without giving U write access to the rest of `F`'s parent directory:

1. Ensure `F` has "Inherit Permissions" off.
2. Grant `DELETE` to user U on `F` explicitly (via the UI: this also forces `WRITE` on `F`).
3. Also ensure U has `WRITE` on `F`'s parent — required at the JCR level for the move-to-trash operation (`jcr:removeChildNodes` on the parent). Without it, the deletion fails even if `DELETE` is granted on `F`.

> **Via the UI, granting DELETE on `F` also grants WRITE on `F`** (due to the UI hierarchy). WRITE on `F` + the inheritance injection rule means U can also delete all of `F`'s inheriting children — not just `F` itself. If you need DELETE on `F` without enabling child deletion, use API-level ACL management to grant DELETE only (without WRITE).

> If granting `WRITE` on the parent is too broad, check whether sibling files/folders have their own explicit ACLs protecting them.

### Protect an entire subtree from deletion

To prevent anyone (except the owner and admins) from deleting a folder `F` and everything inside it:

1. Open `F`'s permissions. Turn off "Inherit Permissions".
2. Grant only `READ` on `F` (no `WRITE`, no `DELETE`).
3. Leave "Inherit Permissions" on for `F`'s children — they will inherit `READ` from `F`.

**Effect:**
- No one has `WRITE` on `F` → no `jcr:removeChildNodes` → the implicit delete injection never fires for any descendant.
- `F` itself has no `DELETE` ACE → cannot be deleted (unless you are its owner).
- The entire subtree is read-only.

If individual items inside the subtree still need to be editable:

1. Turn off "Inherit Permissions" on those specific items.
2. Grant `WRITE` explicitly on each one.
3. Their children (if any) will then be deletable by anyone with `WRITE` on them, but the rest of the subtree remains protected.

### Protect a specific folder from deletion while keeping its contents writable

1. Turn off "Inherit Permissions" on folder `F`.
2. Grant `WRITE` (but **not** `DELETE`) to the user on `F`.

The user can create, edit, and delete items **inside** `F` (since `WRITE` on `F` triggers the implicit child-delete injection for `F`'s inheriting children), but **cannot delete `F` itself** (no `DELETE`, not the owner, and the injection only applies to `F`'s children, not to `F`).

---

## Summary: can I delete X?

| User has… | Target X | "Inherit Permissions" on X | Can delete X? |
|---|---|---|---|
| `WRITE` on X's parent | file or folder | on (inheriting) | ✅ Yes — parent WRITE + inheritance injection |
| `WRITE` on X's parent | file or folder | **off** (own ACL) | ❌ No — injection does not cross ACL boundary |
| `DELETE` on X | file or folder | off (own ACL) | ✅ Yes — but also needs `WRITE` on X's parent for the JCR move-to-trash |
| Owner of X | file or folder | on or off | ✅ Yes — owner magic ACE grants `jcr:all` (includes `jcr:removeNode`) |
| `WRITE` on X (not parent) | folder | off (own ACL) | ❌ No — `WRITE` on X grants child-delete inside X, not deletion of X itself |
| `ALL` on X | file or folder | on or off | ✅ Yes |

---

## Unsupported use cases

These are permission scenarios common in other systems that **cannot be expressed** in the Pentaho repository model.

---

### ❌ Sticky bit — "add files, but only delete your own"

**What you'd want:** Users can create files in a shared folder. Each user can delete files they created (they own), but cannot delete files created by others. Equivalent to the Unix sticky bit (`chmod +t`).

**Why it doesn't work:**

Creating a file in folder `F` requires `WRITE` on `F`. `WRITE` maps to both `jcr:addChildNodes` **and** `jcr:removeChildNodes` — they are bundled into the same permission with no way to separate them. `jcr:removeChildNodes` on `F` triggers the inheritance injection, granting `jcr:removeNode` on every inheriting child of `F` — owned by anyone.

There is no "add-only" (`jcr:addChildNodes` without `jcr:removeChildNodes`) permission in the Pentaho model.

**Partial workaround (heavy):** Give each file its own explicit ACL (`isEntriesInheriting=false`) with WRITE but no DELETE after it is created. This protects it from deletion by others (the injection doesn't cross ACL boundaries), but requires per-file ACL management. It does not scale and cannot be automated as a folder policy.

---

### ❌ WRITE without implicit child-delete

**What you'd want:** A user can create and edit content inside folder `F` but cannot delete any of its children.

**Why it doesn't work:**

`WRITE` is an atomic permission. It always includes `jcr:removeChildNodes`, which always triggers the `jcr:removeNode` injection on inheriting children. There is no "WRITE minus delete-children" in the model. You cannot grant `jcr:addChildNodes` independently of `jcr:removeChildNodes` through the Pentaho permission API.

---

### ❌ "Everyone except user X" (explicit DENY)

**What you'd want:** A role has READ on a folder, but one specific member of that role should be denied access to a particular file.

**Why it doesn't work:**

The Pentaho ACL API only stores **ALLOW** entries. `JcrRepositoryFileAclUtils.internalUpdateAcl` calls `acl.addAccessControlEntry(principal, privileges)` — the 2-argument form in Jackrabbit's `ACLTemplate` always creates an ALLOW entry. There is no supported way to write a DENY ACE via the Pentaho repository API.

To express the exception, the only option is to break the role-based grant and enumerate principals individually, or place an explicit ACL boundary and re-grant narrowly.

---

### ❌ List folder contents without reading file content

**What you'd want:** A user can see what files exist in a folder (names, metadata) without being able to open them. Common in shared directories where filenames are informational.

**Why it doesn't work:**

`READ` maps to `jcr:read` + `jcr:readAccessControl` — a single indivisible privilege that covers both node traversal/listing and reading node properties (i.e. file content). There is no separate "list" vs "open" distinction.

---

### ❌ Copy protection — "can read here but not copy out"

**What you'd want:** Users can open files from folder `F` but cannot copy them to another folder.

**Why it doesn't work:**

`copyFile` checks **only** WRITE on the destination folder. The source file is not permission-checked at the Pentaho layer, and Jackrabbit's `workspace.copy()` does not enforce source-read at the JCR level either. Any user who can reference a file by ID and has WRITE on a destination can copy the file, regardless of their permissions on the source.

---

### ❌ Move allowed, copy disallowed (or vice versa)

**What you'd want:** Grant the ability to move files out of a folder (removing them from the source) without allowing duplication via copy.

**Why it doesn't work:**

`moveFile` requires WRITE on both source and destination. `copyFile` requires only WRITE on destination (source not checked). There is no permission that covers "move" independently of the components it is made of. If you have WRITE on source and destination, both move and copy are available. You cannot allow one while blocking the other.

---

### ❌ Execute permission

**What you'd want:** Separate the ability to run/execute a resource (e.g. a report or script) from the ability to read its definition.

**Why it doesn't work:**

The `EXECUTE` value is commented out in `RepositoryFilePermission`. The original design notes reference [JCR-2446](http://issues.apache.org/jira/browse/JCR-2446), which explains that JCR has no native execute semantics. No execute permission exists in the model.

---

### ❌ Creator loses delete rights after upload

**What you'd want:** A "submission folder" — users can upload files but, once submitted, they cannot delete or modify them (e.g. for audit or compliance).

**Why it doesn't work:**

The user who creates a file becomes its **owner**. The owner always receives `jcr:all` via the owner magic ACE at privilege evaluation time, regardless of what the ACL says. This covers all JCR-native operations (read, write, delete) but does **not** include `pho:aclManagement` (a custom Pentaho privilege outside the JCR tree). Owners cannot change permissions on their own nodes unless they also have an explicit `ALL` ACE.

---

### ❌ Delete a folder without enabling deletion of its children (via UI)

**What you'd want:** Grant a user the ability to delete folder `F` itself, but prevent them from deleting `F`'s children.

**Why it doesn't work via the UI:**

The UI hierarchy forces `DELETE ⊃ WRITE`. Granting DELETE on `F` in the UI always also grants WRITE on `F`. WRITE on `F` maps to `jcr:removeChildNodes`, which triggers the inheritance injection in `PentahoEntryCollector` — granting `jcr:removeNode` on every inheriting child of `F`. So DELETE on `F` via the UI = delete `F` + delete all of `F`'s inheriting children.

**API-only workaround:** Programmatically set an ACL on `F` with DELETE but not WRITE. Even then, the JCR move-to-trash still requires `jcr:removeChildNodes` on `F`'s parent, so the parent's WRITE is still needed for the actual deletion.

---

### ❌ Owner cannot isolate their own folder from parent permissions

**What you'd want:** A user creates a folder inside a shared parent. They own it, so they should be able to "lock it down" — turn off "Inherit Permissions" and set their own ACL, preventing others who have WRITE on the parent from accessing its contents.

**Why it doesn't work:**

Changing the `isEntriesInheriting` flag (the "Inherit Permissions" toggle) requires `ACL_MANAGEMENT`. Ownership only grants `jcr:all`, which does **not** include `pho:aclManagement`. A user who merely created the folder — without an explicit `ALL` ACE — cannot modify its ACL or toggle inheritance, even though they own it.

**Consequence:** The folder inherits its parent's permissions by default and stays that way. Anyone with WRITE on the parent can delete the folder (via the inheritance injection rule), and the owner has no way to prevent this unless an admin grants them `ALL` on their own folder.

**Workaround:** An administrator must explicitly grant `ALL` to the user on the folder after creation, enabling the user to then manage its ACL independently.

---

# Questionable / Likely Unintended Behaviors

The items below are behaviors that emerge from the permission model but are either inconsistent with how most file systems work, internally contradictory, or likely unintentional side-effects of the design.

---

## 🐛 copyFile does not check source permissions

**What happens:** `JcrRepositoryFileDao.copyFile()` calls `internalCopyOrMove(fileId, destPath, versionMessage, copy=true)`. The `if (!copy)` guard means the source permission block is skipped entirely. No check is performed on the source file at the Pentaho layer. The underlying `workspace.copy()` in Jackrabbit also does not enforce source-read at the JCR level.

**Result:** Any user who can obtain a file's ID (e.g. from a URL, a search result, or a shared link) and has WRITE on any destination folder can copy that file — even if they have no explicit permission on the source file or its folder.

**Why this is a bug:** Every mainstream file system requires read access to the source in order to copy it:
- **Unix/Linux:** read on source file + execute (traverse) on source directory
- **Windows NTFS:** Read permission on source
- **Google Drive / SharePoint:** at least Viewer on the source
- **AWS S3:** `s3:GetObject` on source bucket/key

This is an information-disclosure vulnerability. A user with no granted access to a protected folder can exfiltrate its contents if they know file IDs.

**Suggested fix:** In `JcrRepositoryFileDao.internalCopyOrMove()`, add a `READ` check on the source file for both copy and move paths — or at minimum for copy, which currently has no source check at all:

```java
// inside internalCopyOrMove, before the copy branch
RepositoryFile sourceFile = getFileById(fileId);
RepositoryFileAcl sourceAcl = aclDao.getAcl(fileId);
if (!accessVoterManager.hasAccess(sourceFile, RepositoryFilePermission.READ, sourceAcl, PentahoSessionHolder.getSession())) {
    throw new AccessDeniedException(...);
}
```

**Risk / Impact:**
- **Security risk of not fixing:** High — information disclosure; any user with WRITE somewhere can read protected files via copy.
- **Risk of fixing:** Low–Medium. Well-scoped change inside one method. Behavioral change: users who relied (intentionally or not) on copying files they cannot read will be denied. Tests that copy without granting source READ will break and need updating. No JCR internals touched.

---

## 🐛 deleteFile (soft): Pentaho and JCR check different things

**What happens:** `JcrRepositoryFileDao.deleteFile()` calls `JcrRepositoryFileAclDao.hasAccess(file.getPath(), {DELETE})`, which evaluates `jcr:removeNode` on the file. But the actual operation is `session.move(file → .trash)`, which requires `jcr:removeChildNodes` on the **source parent folder** at the JCR level — a different privilege on a different node.

**Result:** A user may pass the Pentaho DELETE check but then fail at the JCR level because they lack WRITE on the file's parent folder. The error surfaces as a raw Jackrabbit `AccessDeniedException`, not a Pentaho-level one. This defeats the purpose of the pre-check layer: errors are not early or meaningful.

**Contrast with `permanentlyDeleteFile()`:** that calls `fileNode.remove()`, requiring `jcr:removeNode` on the file itself — correctly aligned with the Pentaho DELETE check. Only the soft-delete path has this mismatch.

**Suggested fix:** Either (a) add an explicit `WRITE` check on the source parent folder in `JcrRepositoryFileDao.deleteFile()` to match what JCR will enforce, or (b) change the pre-check to verify `jcr:removeChildNodes` on the parent directly:

```java
// add to deleteFile, before the session.move call
RepositoryFile parentFolder = getFileById(parentFolderId);
RepositoryFileAcl parentAcl = aclDao.getAcl(parentFolder.getId());
if (!accessVoterManager.hasAccess(parentFolder, RepositoryFilePermission.WRITE, parentAcl, PentahoSessionHolder.getSession())) {
    throw new AccessDeniedException(...);
}
```

**Risk / Impact:**
- **Security risk of not fixing:** Low — the JCR layer still denies the operation. The bug is a UX/diagnostic issue, not a security hole.
- **Risk of fixing:** Low. Purely additive check; the only new denial scenario is a user who had DELETE on the file but no WRITE on the parent (already denied by JCR anyway). Error message becomes clearer. No behavioral change for correctly configured ACLs.

---

## ⚠️ WRITE on a folder grants recursive descendant delete, not just direct children

**What happens:** When a node is evaluated for privileges, `PentahoEntryCollector.getEntries()` walks up to the nearest non-inheriting ancestor and uses its ACEs. If that ancestor has `jcr:removeChildNodes` (WRITE), the injection adds `jcr:removeNode` to the effective ACE set for the node being evaluated. Crucially, this fires for **any** inheriting descendant — not just direct children — because each descendant independently walks all the way to the nearest explicit-ACL ancestor.

Example: `F` (explicit ACL, WRITE) → `A` (inheriting) → `B` (inheriting) → `C` (inheriting)
- Evaluating `C`: walks to `F` (nearest explicit-ACL ancestor) → WRITE found → `jcr:removeNode` injected on `C` ✅
- Same for `B` and `A`.

A single WRITE grant on `F` silently grants delete on the entire inheriting subtree, regardless of depth.

**How other systems scope delete:**

| System | What grants delete on a child? | Scope |
|---|---|---|
| **Unix/Linux** | `write + execute` on the **immediate parent directory** | Direct parent only |
| **Windows NTFS** | Separate "Delete subfolders and files" bit, propagated explicitly via ACL inheritance | Explicit per-level propagation |
| **JCR standard** | `jcr:removeChildNodes` on the parent | Direct children only — `jcr:removeNode` is required on the item itself |
| **Pentaho** | WRITE (`jcr:removeChildNodes`) on **any ancestor** with an explicit ACL | Entire inheriting subtree |

Notably, **standard JCR already scopes `jcr:removeChildNodes` to direct children**. Pentaho's injection in `PentahoEntryCollector` is what extends this to the full subtree — a deliberate but non-standard extension of JCR semantics.

**Root cause in code:** `PentahoEntryCollector.getEntries()` triggers injection whenever `!currentNode.isSame(node)` — i.e. whenever the evaluated node is not the explicit-ACL node itself. This is true for any inheriting descendant at any depth.

**Suggested fix:** Change the injection condition from "any inheriting descendant" to "direct inheriting child only":

```java
// Current — fires for any inheriting descendant:
if (!currentNode.isSame(node)) {
    // inject jcr:removeNode
}

// Fixed — fires only when the explicit-ACL ancestor is the direct parent:
if (node.getParent().isSame(currentNode)) {
    // inject jcr:removeNode
}
```

With this change, WRITE on `F` would still grant delete on `F`'s direct children (`A`), but not on `A`'s children (`B`, `C`). To delete `B`, the user would need WRITE on `A` — matching standard Unix / JCR semantics. Intermediate folders with "Inherit Permissions" on would need WRITE granted at each level to propagate delete rights downward.

**Risk / Impact:**
- **Security risk of not fixing:** Medium. A WRITE grant on any shared top-level folder silently enables deletion of arbitrarily deep content beneath it. Admins granting WRITE to a team folder may not realise they are granting recursive delete across the whole subtree.
- **Risk of fixing:** High. This is a **breaking behavioral change**. Most deployments rely on the current model where WRITE on a top folder means "manage everything beneath it." Fixing this would require every such deployment to re-grant WRITE at each intermediate folder level, or restructure ACLs significantly. Not safe without a migration path and explicit opt-in. The correct approach would be a new permission flag or a server configuration option, not a silent behavior change.

---

## ⚠️ Owner cannot manage ACL on their own resource

**What happens:** `PentahoEntryCollector.addOwnerAce()` injects `jcr:all` for the node owner. `pho:aclManagement` is a custom privilege registered outside the JCR privilege tree and is **not** included in `jcr:all`. The owner cannot toggle "Inherit Permissions" or change ACEs without an explicit `ALL` ACE granted by an admin.

**Why this is surprising:** In virtually every other system, resource ownership implies permission to manage access to that resource (Unix: owner controls `chmod`; Windows NTFS: owner has "Take Ownership" and can set DACLs; Google Drive: creator has sharing rights).

**Suggested fix:** Change `PentahoEntryCollector.addOwnerAce()` to inject both `jcr:all` and `pho:aclManagement` for the owner, instead of only `jcr:all`.

**Risk / Impact:**
- **Security risk of not fixing:** Low-Medium — owners of shared resources (e.g., system-created folders) would not expect to be locked out of their own ACL. But it also means admins can grant ownership without granting ACL control, which may be desired in some deployments.
- **Risk of fixing:** Medium. Behavioral change: all resource owners would gain ACL management on their own nodes. Deployments where ownership is used as a way to scope delete rights (but not permission management) would be affected. The change is isolated to `addOwnerAce()` — small surface area, but the semantic impact is broad.

---

## ⚠️ WRITE without READ is expressible via API

**What happens:** `WRITE` maps in `DefaultPermissionConversionHelper.initMaps()` to `jcr:modifyProperties`, `jcr:addChildNodes`, etc. — none of which include `jcr:read`. If an ACL is set via the API (bypassing the UI hierarchy) granting WRITE but not READ, the user can modify or overwrite a file they cannot open or read.

**Why it matters:** This is only reachable via the API, since the UI enforces `WRITE ⊃ READ`. But programmatic ACL management (scripts, REST API) can produce this state. The result is a user who can blindly overwrite content without being able to verify what they are changing.

**Precedent in other systems:** Write-without-read is actually a deliberate, documented pattern elsewhere:
- **Unix/Linux:** `chmod 200 file` — write without read on a file. `chmod 300 dir` (write+execute, no read) is the classic **drop-box** directory: users can deposit files but cannot list or read what others have submitted.
- **Windows NTFS:** Write and Read are separate ACL bits; granting Write without Read is supported.
- **AWS S3:** `s3:PutObject` without `s3:GetObject` — write-only bucket; common for log submission or telemetry pipelines where submitters must not read each other's data.
- **POSIX ACLs:** Support arbitrary permission combinations explicitly.

In those systems, write-without-read is **intentional** and serves real use cases (drop boxes, submit-only pipelines). In Pentaho it is an **accidental gap** — no feature was designed around it, the UI actively prevents it, and it is only reachable through API misuse. Notably, the drop-box pattern that makes write-without-read useful in Unix also requires a sticky bit, which Pentaho does not support (see Unsupported Use Cases).

**Suggested fix:** Enforce the `WRITE ⊃ READ` hierarchy server-side in `JcrRepositoryFileAclDao` (or `DefaultUnifiedRepository.updateAcl()`): if WRITE is present in an ACE, automatically add READ if not already present.

**Risk / Impact:**
- **Security risk of not fixing:** Low — the UI already prevents this; API misuse is the only vector, and write-without-read does not grant access to data the user couldn't otherwise reach.
- **Risk of fixing:** Low. Server-side normalization of ACEs is a small, well-scoped change. The only risk is silently promoting ACEs that were intentionally set to WRITE-only via the API, though no legitimate Pentaho use case for that exists.
