---
type: architecture
title: FileService Disambiguation Strategy Design Observations
description: Design observations on why the IUnifiedRepository disambiguation approach needs adaptation at the FileService layer.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Notes for building a disambiguation strategy at this layer

A dedicated companion group,
[FileService exception disambiguation](../../reference/file-service/exception-disambiguation/index.md),
provides full per-operation, public-API-only disambiguation snippets for this layer
(mirroring [Unified Repository exception disambiguation](../../reference/unified-repository/exception-disambiguation/index.md) for the main doc). The
notes below summarize why the underlying `IUnifiedRepository` disambiguation approach
needs adaptation here.

The public-API disambiguation approach from
[Unified Repository exception disambiguation: general approach](../../reference/unified-repository/exception-disambiguation/general-approach.md) (using `hasAccess()`/`getFileById()`
follow-up calls) remains valid **wherever a `FileService` call is a pure pass-through to
`getRepoWs()`** ([FileService role and general shape](layer-file-service.md) pattern 1/2) or where `RepositoryFileProvider` calls
`unifiedRepository` directly (`getFile`, `getAcl`, `hasAccess`). It is **not sufficient** on
its own for methods affected by [FileService contract divergence](../../reference/file-service/contract-divergence.md)'s divergences, because:

- Anything funneled through `InternalError` (`restoreFile`, and `moveFile`'s non-`URADE`/
  non-`IllegalArgumentException` branch) has already lost the information needed to tell
  "not found" from "some other repository failure" — a follow-up `getFileById()`/
  `hasAccess()` call is still valid and necessary here (it does not depend on the exception
  that was thrown), but note the **first** attempt already destroyed any chance of a
  future doc distinguishing via caught type alone.
- `getTree`'s `null` return needs a **separate** follow-up call (e.g.
  `unifiedRepository.getFile(basePath)`/`hasAccess(basePath, READ)`) purely to tell
  not-found from no-read from "some other problem", since `doGetTree` itself gives no
  usable signal beyond `null`.
- `setFileMetadata`'s access check is `doSetMetadata`'s own custom rule ([FileService contract divergence](../../reference/file-service/contract-divergence.md) point 3), not
  `hasAccess(ACL_MANAGEMENT)` — a follow-up disambiguation must replicate *that* rule
  (owner-or-triple-ABS-action-or-explicit-ACE), not the JCR privilege used elsewhere.
