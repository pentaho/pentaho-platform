---
type: reference
title: FileService Contract Divergence From IUnifiedRepository
description: Where FileService's exception and access-control contract diverges from IUnifiedRepository's.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Where `FileService`'s contract diverges from `IUnifiedRepository`'s

These are the concrete, source-verified differences a caller must account for when
switching mental models from the main doc to this one:

1. **Loss of exception identity via `InternalError`.** `doMoveFiles` and `doRestoreFiles`
   both catch every exception except `IllegalArgumentException`/`URADE` and rethrow a bare
   `InternalError()` — no message, no `initCause`. Any of the main doc's other exception
   types (`URE`, `UnifiedRepositoryReferentialIntegrityException`,
   `UnifiedRepositoryFileExistsException`, etc.) reaching these two methods is
   **indistinguishable from a genuine not-found condition** by the time it reaches
   `RepositoryFileProvider`.
2. **`doGetTree` silently returns `null` instead of throwing**, even though it inspects the
   exact two-hop cause chain internally (only to choose a log level) — a caller cannot tell
   "root path not found", "root path not readable", and "any other repository error"
   apart at all; all three produce the same `NotFoundException` in `RepositoryFileProvider`.
3. **`doSetMetadata` reimplements its own ACL-management gate**, independent of
   `IUnifiedRepository.updateAcl`'s `hasAccess(ACL_MANAGEMENT)` check (main doc [DefaultUnifiedRepository target bean](../../architecture/unified-repository/layer-default-unified-repository.md)): it
   grants access if the caller is the ACL owner, **or** holds all three of
   `repository.read` + `repository.create` + `administer.security` ABS actions, **or** has an
   ACE explicitly granting `ACL_MANAGEMENT`/`ALL`. This is a materially different (and, for
   the ABS-triple-action branch, more permissive) rule than the JCR `aclManagement`
   privilege check the main doc documents for `updateAcl` — and it does **not** exhibit the
   main doc's Owner-ACE gap, since it special-cases the owner explicitly.
4. **Recurring null-dereference defects.** `doRename`, `doGetFileAcl`, and `doSetMetadata`
   all call `getRepoWs().getFile(...)`/`getRepository().getFile(...)` and immediately
   dereference the result without a null-check — the same defect pattern the main doc
   documents for `IUnifiedRepository.updateAcl()`. In `renameFile`/`getFileAcl`,
   `RepositoryFileProvider` happens to pre-check `doesExist()` first, narrowing (but not
   eliminating, due to the time-of-check/time-of-use race — see the disambiguation doc's
   "race" caveat) the window in which this defect is reachable. `setFileMetadata` has
   **no** such pre-check, so a concurrently-deleted/unreadable file reliably produces an
   uncaught `NullPointerException` that is not translated into any GFS exception type at
   all.
5. **`doCreateDirSafe` auto-creates missing intermediate folder segments** (via
   `doCreateDirFor`'s per-segment loop). This is different from `IUnifiedRepository
   .createFolder()` (main doc), which requires the immediate parent to already exist and
   throws if it does not. A caller relying on this doc's parent-must-exist semantics for
   plain `createFolder` should not assume the same for GFS `doCreateDirSafe`.
6. **`CopyFilesOperation`'s not-found condition is inverted** relative to
   `IUnifiedRepository.copyFile`'s: it requires the **destination directory itself** to
   already exist ([CopyFilesOperation layer](../../architecture/file-service/layer-copy-files-operation.md)), whereas the main doc's `copyFile` only errors when the
   destination's *parent* is missing.
7. **Two different "access denied" exception types with different scopes** exist at the GFS
   layer (`AccessControlException` for ABS/global denials, `ResourceAccessDeniedException`
   for per-file denials) where the main doc has only one (`URADE`) — see [JcrRepositoryFileDao layer](../../architecture/unified-repository/layer-jcr-repository-file-dao.md)'s mapping
   table. When both a global and a per-file check exist for the same operation (e.g.
   `renameFile`, `copyFile`, `moveFile`), the GFS caller must know which exception type it
   is looking at to know *which* permission needs to be granted.

---

