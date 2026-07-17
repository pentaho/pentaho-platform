---
type: architecture
title: "Worked Example: RepositoryFileProvider Exception Mapping"
description: Worked example of how `RepositoryFileProvider` maps FileService exceptions.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Worked example: `RepositoryFileProvider`'s exception mapping

This subsection is not about `FileService` itself, but about how one real caller
(`RepositoryFileProvider`, from the Generic File System) reacts to what `FileService`/
`IUnifiedRepository` throw — included because it is a concrete, verified illustration of
which of the divergences in [FileService contract divergence](../../reference/file-service/contract-divergence.md) actually matter to a caller in practice. It translates
those exceptions into its own (GFS) exception hierarchy (all extend
`org.pentaho.platform.api.genericfile.exception.OperationFailedException`, itself a checked
`Exception`):

| GFS exception | Extends | Typical trigger in this provider |
|---|---|---|
| `NotFoundException` | `OperationWithPathFailedException` → `OperationFailedException` | `null` result from `getFile`/`doesExist`/`doGetTree`, or caught `FileNotFoundException` from `FileService` |
| `AccessControlException` | `OperationFailedException` | Global ABS action denied (`doGetCanCreate()` false) — i.e. an **ABS-level** denial, checked *before* calling the operation, not a per-file JCR denial |
| `ResourceAccessDeniedException` | `OperationWithPathFailedException` → `OperationFailedException` | Caught `UnifiedRepositoryAccessDeniedException` — i.e. a **per-file** JCR/ACL denial |
| `ConflictException` | `OperationFailedException` | Pre-check: destination path already exists (rename/copy/move) |
| `InvalidOperationException` | `OperationFailedException` | Invalid new name, invalid path, folder-vs-file content-type mismatch |
| `InvalidPathException` | `OperationFailedException` | Malformed `GenericFilePath`, caught `FileService.InvalidNameException` |
| `OperationFailedException` (direct) | `Exception` | Catch-all: any other exception, including the lossy `InternalError` from [FileService role and general shape](layer-file-service.md) pattern 3 |

Two access-denial exceptions exist at this layer for a reason: `AccessControlException` and
`ResourceAccessDeniedException` are **not interchangeable** — the former means "you may
never do this kind of operation at all" (ABS/global), the latter means "you specifically
cannot do this to this file" (per-file JCR ACL). Several methods check both, in that order
(global first, then per-file) — e.g. `renameFile`, `copyFile`, `moveFile` all pre-check
`doGetCanCreate()` before attempting the operation, then separately catch
`UnifiedRepositoryAccessDeniedException` from the operation itself.

---

