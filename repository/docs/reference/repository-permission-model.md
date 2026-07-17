---
type: reference
title: Repository Permission Model
description: Layer-by-layer facts about how repository permissions map to JCR privileges, ACLs, inheritance, and magic ACEs.
status: active
timestamp: 2026-07-17T17:09:07Z
---

# Pentaho Repository Permission Model

> See also: [Repository Permission Semantics And Use Cases](./repository-permission-semantics.md) for what each permission means in practice, and [Repository Permission Model Known Issues](./repository-permission-known-issues.md) for inconsistent or likely-unintended behaviors.

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

