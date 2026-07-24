---
type: reference
title: IUnifiedRepository Permission Model Known Issues
description: Current inconsistent, surprising, or likely-unintended access-control behaviors in the repository permission model, with suggested fixes and risk analysis.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Questionable / Likely Unintended Permission Behaviors

The items below are behaviors that emerge from the [ACL model](./acl-model.md), its
[inheritance rules](./inheritance.md), and [Magic ACEs](./magic-aces.md), but are either
inconsistent with how most file systems work, internally contradictory, or likely
unintentional side-effects of the design.

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

**What happens:** `JcrRepositoryFileDao.deleteFile()` calls `JcrRepositoryFileAclDao.hasAccess(file.getPath(), {DELETE})`, which evaluates `jcr:removeNode` on the file. But the actual operation is `session.move(file → .trash)`, which requires `jcr:removeChildNodes` on the **source parent folder** at the JCR level — a different privilege on a different node. This double-check flow — the [pre-check layer's](./pre-check-layer.md) voter runs first (and can silently veto), then `aclDao.hasAccess()` runs — was designed so the pre-check always mirrors what JCR will require; this is one place where it doesn't.

**Result:** A user may pass the Pentaho DELETE check but then fail at the JCR level because they lack WRITE on the file's parent folder. The error surfaces as a raw Jackrabbit `AccessDeniedException`, not a Pentaho-level one. This defeats the purpose of the pre-check layer: errors are not early or meaningful.

**Contrast with `permanentlyDeleteFile()`:** that calls `fileNode.remove()`, requiring `jcr:removeNode` on the file itself — correctly aligned with the Pentaho DELETE check. Only the soft-delete path (move to `.trash`) has this mismatch.

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

**What happens:** When a node is evaluated for privileges, `PentahoEntryCollector.getEntries()` walks up to the nearest non-inheriting ancestor and uses its ACEs. If that ancestor has `jcr:removeChildNodes` (WRITE), the injection adds `jcr:removeNode` to the effective ACE set for the node being evaluated. Crucially, this fires for **any** inheriting descendant — not just direct children — because each descendant independently walks all the way to the nearest explicit-ACL ancestor. This is intentional (see the [inheritance transformation](./magic-aces.md)) but not obvious from the ACL UI.

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

**What happens:** `PentahoEntryCollector.addOwnerAce()` injects `jcr:all` for the node owner (see [Magic ACEs](./magic-aces.md)). `pho:aclManagement` is a custom privilege registered outside the JCR privilege tree and is **not** included in `jcr:all`. The owner cannot toggle "Inherit Permissions" or change ACEs without an explicit `ALL` ACE granted by an admin. This is permanent as long as they remain the owner, and is not visible via `JcrRepositoryFileAclDao.getAcl()`.

**Why this is surprising:** In virtually every other system, resource ownership implies permission to manage access to that resource (Unix: owner controls `chmod`; Windows NTFS: owner has "Take Ownership" and can set DACLs; Google Drive: creator has sharing rights).

**Suggested fix:** Change `PentahoEntryCollector.addOwnerAce()` to inject both `jcr:all` and `pho:aclManagement` for the owner, instead of only `jcr:all`.

**Risk / Impact:**
- **Security risk of not fixing:** Low-Medium — owners of shared resources (e.g., system-created folders) would not expect to be locked out of their own ACL. But it also means admins can grant ownership without granting ACL control, which may be desired in some deployments.
- **Risk of fixing:** Medium. Behavioral change: all resource owners would gain ACL management on their own nodes. Deployments where ownership is used as a way to scope delete rights (but not permission management) would be affected. The change is isolated to `addOwnerAce()` — small surface area, but the semantic impact is broad.

---

## ⚠️ WRITE without READ is expressible via API

**What happens:** `WRITE` maps in [`DefaultPermissionConversionHelper.initMaps()`](./jcr-privilege-mapping.md) to `jcr:modifyProperties`, `jcr:addChildNodes`, etc. — none of which include `jcr:read`. If an ACL is set via the API (bypassing the UI hierarchy) granting WRITE but not READ, the user can modify or overwrite a file they cannot open or read.

**Why it matters:** This is only reachable via the API, since the UI enforces `WRITE ⊃ READ` (see the [permission hierarchy](./permission-hierarchy.md)). But programmatic ACL management (scripts, REST API) can produce this state. The result is a user who can blindly overwrite content without being able to verify what they are changing.

**Precedent in other systems:** Write-without-read is actually a deliberate, documented pattern elsewhere:
- **Unix/Linux:** `chmod 200 file` — write without read on a file. `chmod 300 dir` (write+execute, no read) is the classic **drop-box** directory: users can deposit files but cannot list or read what others have submitted.
- **Windows NTFS:** Write and Read are separate ACL bits; granting Write without Read is supported.
- **AWS S3:** `s3:PutObject` without `s3:GetObject` — write-only bucket; common for log submission or telemetry pipelines where submitters must not read each other's data.
- **POSIX ACLs:** Support arbitrary permission combinations explicitly.

In those systems, write-without-read is **intentional** and serves real use cases (drop boxes, submit-only pipelines). In Pentaho it is an **accidental gap** — no feature was designed around it, the UI actively prevents it, and it is only reachable through API misuse. Notably, the drop-box pattern that makes write-without-read useful in Unix also requires a sticky bit, which Pentaho does not support (see [Unsupported Use Cases](./unsupported-use-cases.md)).

**Suggested fix:** Enforce the `WRITE ⊃ READ` hierarchy server-side in `JcrRepositoryFileAclDao` (or `DefaultUnifiedRepository.updateAcl()`): if WRITE is present in an ACE, automatically add READ if not already present.

**Risk / Impact:**
- **Security risk of not fixing:** Low — the UI already prevents this; API misuse is the only vector, and write-without-read does not grant access to data the user couldn't otherwise reach.
- **Risk of fixing:** Low. Server-side normalization of ACEs is a small, well-scoped change. The only risk is silently promoting ACEs that were intentionally set to WRITE-only via the API, though no legitimate Pentaho use case for that exists.

---

## ⚠️ WRITE's JCR privileges include `jcr:modifyAccessControl`

**What happens:** The [permission-enum-to-JCR-privilege mapping](./jcr-privilege-mapping.md) includes `jcr:modifyAccessControl` under `WRITE`. This means a user with WRITE technically has the native JCR privilege needed to modify the node's JCR-level ACL directly, even though the intended Pentaho-level path for ACL changes is `DefaultUnifiedRepository.updateAcl()`, which separately enforces `ACL_MANAGEMENT`.

**Why it matters:** Any code path that reaches the JCR `AccessControlManager` directly (bypassing `updateAcl()`) would only need WRITE, not `ACL_MANAGEMENT`, to change a node's ACL — a narrower Pentaho-level permission than intended for ACL changes.

**Risk / Impact:** Low today — `updateAcl()` is the only supported path for ACL changes, and it enforces `ACL_MANAGEMENT` correctly. This is a latent gap in the privilege mapping that only becomes a real risk if new code calls the JCR `AccessControlManager` directly instead of going through `updateAcl()`.

---

## ⚠️ Magic ACEs are invisible via `getAcl()`

**What happens:** `JcrRepositoryFileAclDao.getAcl()` strips `IPentahoInternalPrincipal` entries via `JcrRepositoryFileAclUtils.removeAclMetadata()`. None of the three [Magic ACE](./magic-aces.md) sources — the owner ACE, the inheritance transformation, or config-yaml ACEs — are ever stored in JCR or returned by `getAcl()`.

**Why it matters:** The effective permissions a user has can be significantly larger than what `getAcl()` reports. Any tooling, audit report, or admin UI that relies on `getAcl()` to answer "who can do what to this file" will under-report access, sometimes substantially (e.g. missing that every ancestor's WRITE grantee can delete this node, or that the owner can do anything short of ACL management).

**Risk / Impact:** Medium for auditability and security review — without also modeling the Magic ACE rules, no report derived from `getAcl()` alone can be trusted as a complete access list.
