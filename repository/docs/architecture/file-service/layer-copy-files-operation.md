---
type: architecture
title: CopyFilesOperation Layer
description: Role of `CopyFilesOperation`, used only by `FileService.doCopyFiles`.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# `CopyFilesOperation` (used only by `FileService.doCopyFiles`)

`doCopyFiles` itself performs a `RepositoryCreateAction` ABS check *before* constructing a
`CopyFilesOperation` at all (see the ABS box above, point 3) — a first, distinct source of
`IllegalArgumentException` unrelated to the one below.

`CopyFilesOperation`'s own constructor then performs its own eager validation and throws
`IllegalArgumentException` (not `URADE`/`URE`) if: the repository/web-service/source-list
arguments are `null`, the source list is empty, `destDirPath` is `null`, **or the
destination directory does not exist** (pre-checked via `getRepoWs().getFile(destDirPath)`).
Note the direction: this is the **opposite** of `IUnifiedRepository.copyFile`'s own
not-found condition (main doc [IUnifiedRepository access-control summary table](../../reference/unified-repository/summary-table-per-method.md)), which only reports an error when the destination's
*parent* is missing — here, the destination folder itself must already exist. Inside `execute()`, missing/unreadable source IDs are logged and skipped. Native JCR
denials from destination writes, ACL reads, metadata reads/writes, or overwrite-mode ACL
updates surface as `UnifiedRepositoryAccessDeniedException` through
`PentahoJcrTemplate`. `MODE_OVERWRITE` can also receive the direct `updateAcl`
`ACL_MANAGEMENT` denial. `MODE_RENAME` explicitly throws the same public type when a
custom access voter makes a file create return `null`.

> **Net effect:** a caller of `doCopyFiles` who catches `IllegalArgumentException` cannot
> tell, from the type alone, whether the cause was "no `repository.create` ABS action at
> all" (`doCopyFiles`'s own check) or "the destination directory is missing/not a folder"
> (`CopyFilesOperation`'s constructor check). Deep-folder-copy validation can also throw
> `IllegalArgumentException` after a custom voter returns `null` from `createFolder`.
