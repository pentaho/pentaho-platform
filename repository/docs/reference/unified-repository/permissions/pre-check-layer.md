---
type: reference
title: IUnifiedRepository Access-Control Pre-Check Layer
description: Why the voter/aclDao pre-check layer exists, the per-operation JCR privilege table it must mirror, and why DELETE behaves asymmetrically between a node and its parent.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Purpose of the Pre-Check Layer (aclDao + voter)

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

## Why `DELETE` on an inherited node passes when `DELETE` on its parent does not

The `aclDao.hasAccess(file, {DELETE})` check evaluates `jcr:removeNode` via Jackrabbit's `hasPrivileges()`. `PentahoEntryCollector` runs before the evaluation returns and injects Magic ACEs:

1. **Inheritance transformation**: if `file.isEntriesInheriting=true`, the parent folder's `jcr:removeChildNodes` gets injected as `jcr:removeNode` on `file`. `hasPrivileges(file, [jcr:removeNode])` = **true**.
2. **Owner ACE**: if the current user is the owner of `file`, `jcr:all` is injected → includes `jcr:removeNode` → **true**.

Neither fires when evaluating `hasPrivileges` on the **parent folder itself** (which has `isEntriesInheriting=false` and was not created by the user). Hence the asymmetry: WRITE on a folder lets you delete its children but not the folder itself.

