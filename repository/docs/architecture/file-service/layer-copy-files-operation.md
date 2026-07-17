---
type: architecture
title: CopyFilesOperation Layer
description: Role of `CopyFilesOperation`, used only by `FileService.doCopyFiles`.
status: active
timestamp: 2026-07-17T00:00:00Z
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
not-found condition (main doc §3), which only reports an error when the destination's
*parent* is missing — here, the destination folder itself must already exist. Inside
`execute()`, an explicit `UnifiedRepositoryAccessDeniedException` can also be thrown
directly (not via the converter map) for certain owner/permission checks internal to the
copy operation.

> **Net effect:** a caller of `doCopyFiles` who catches `IllegalArgumentException` cannot
> tell, from the type alone, whether the cause was "no `repository.create` ABS action at
> all" (`doCopyFiles`'s own check) or "the destination directory is missing/not a folder"
> (`CopyFilesOperation`'s check) — both raise the exact same class, with no message set
> either way. A `doGetCanCreate()` follow-up call is the only way to tell them apart.

