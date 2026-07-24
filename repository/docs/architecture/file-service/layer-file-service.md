---
type: architecture
title: FileService Role And General Shape
description: FileService's role, general shape, and how ABS-level (coarse, global) denials surface to callers.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# `FileService` — role and general shape

Unlike `ExceptionLoggingDecorator` (main doc [ExceptionLoggingDecorator layer](../unified-repository/layer-exception-logging-decorator.md)), `FileService` is **not** a pure
exception-translation decorator. It is a service class with real business logic on top of
the repository, and its exception handling is **inconsistent method-by-method** — there is
no single policy. Four distinct patterns are observed:

1. **Pure pass-through** (e.g. `doDeleteFiles`, `doDeleteFilesPermanent`, `doGetDeletedFiles`,
   `doGetMetadata`): whatever `getRepoWs()` throws propagates unchanged (i.e. the exact
   `URADE`/`URE` taxonomy from the main doc applies).
2. **Explicit not-found pre-check, exception otherwise pass-through** (e.g. `doMoveFiles`,
   `setFileAcls`, `doGetContentCreator`): calls `getRepoWs().getFile(...)` first and throws
   `FileNotFoundException` (a **`FileService`-specific class**, not a JCR/repository one) if
   the result is `null` — but does **not** distinguish "does not exist" from "exists but
   not readable" (same confounding as `getFile`/`getFileById` in the main doc, just
   re-surfaced under a different exception type).
3. **Catch-and-swallow into a generic type, losing the original exception** (e.g.
   `doMoveFiles`, `doRestoreFiles`): explicitly re-throws `IllegalArgumentException` and
   `UnifiedRepositoryAccessDeniedException` unchanged, but wraps **any other** exception
   — including the generic `UnifiedRepositoryException` and all its subclasses documented
   in the main doc — into a bare `java.lang.InternalError` with **no message and no
   cause set**. This is a materially lossier contract than `IUnifiedRepository`'s.
4. **No null-check at all before dereferencing** (e.g. `doSetMetadata`, `doRename`,
   `doGetFileAcl`): calls `getRepoWs().getFile(...)` and immediately dereferences the
   result (`.getId()`, `.getPath()`) without checking for `null` — a not-found/no-read
   condition surfaces as an uncaught `NullPointerException`, exactly the same programming
   defect already documented for `IUnifiedRepository.updateAcl()` in the main doc, just
   recurring independently at this layer.

## How ABS-level (coarse, global) denials are surfaced

`FileService` has **no single, consistent mechanism** for signalling a coarse ABS-action
denial, distinctly from a per-file JCR/ACL denial. Depending on the method, one of (at
least) four different things happens:

1. **The underlying `IUnifiedRepository` AOP interceptor still applies**, unchanged,
   whenever a method reaches `getRepoWs()`/`unifiedRepository` ([DefaultUnifiedRepositoryWebService layer](layer-default-unified-repository-web-service.md)): it throws `URADE`
   *before* the target method body — and hence the specific file — is ever touched (main
   doc [DefaultUnifiedRepositoryWebService layer](layer-default-unified-repository-web-service.md)). Since, per the main doc, the *per-file* write/delete denial for virtually
   every method (everything except `updateAcl`) instead surfaces as the plain generic
   `UnifiedRepositoryException`, **catching `URADE` specifically at this boundary is, by
   itself, already unambiguous evidence of the ABS-level check having failed** — the same
   reasoning the main disambiguation doc's "URADE-gobbling" fix relies on.
2. **Some methods expose the check as a plain boolean/string, not an exception at all** —
   `doGetCanCreate()` (wraps `getPolicy().isAllowed(RepositoryCreateAction.NAME)`),
   `doGetCanPublish()`, `doGetCanDownloadWithWhitelist()`/`doGetCanGetFileContent()`. These
   throw nothing; it is entirely the caller's responsibility to check the result *before*
   invoking the operation and decide what to do if it is `false` ([RepositoryFileProvider worked example](worked-example-repository-file-provider.md)'s worked example
   shows `RepositoryFileProvider` doing exactly this ahead of `renameFile`/`copyFile`/
   `moveFile`/`doCreateDirSafe`).
3. **`doCopyFiles` performs the *same* `RepositoryCreateAction` ABS check a second time,
   itself, redundantly** — at the very top of the method, before ever constructing a
   `CopyFilesOperation` ([CopyFilesOperation layer](layer-copy-files-operation.md)) — and, if denied, throws a bare `IllegalArgumentException()`
   with no message. This is a **materially misleading exception type**: the *same*
   `IllegalArgumentException` class is *also* thrown by `CopyFilesOperation`'s own
   constructor/`execute()` for genuinely invalid-argument conditions (missing/non-folder
   destination directory, [CopyFilesOperation layer](layer-copy-files-operation.md)) that have nothing to do with permissions. A caller cannot
   tell these two apart by exception type alone — it must additionally call
   `doGetCanCreate()` itself to know whether an observed `IllegalArgumentException` from
   `doCopyFiles` means "no create permission at all" or "bad destination argument".
4. **`doSetMetadata` uses yet another shape**: neither `hasAccess`-style, nor a plain ABS
   action check, but a custom rule (owner, **or** the same
   `repository.read`+`repository.create`+`administer.security` triple used by
   `doCanAdminister()`, **or** an explicit `ACL_MANAGEMENT`/`ALL` ACE — [FileService contract divergence](../../reference/file-service/contract-divergence.md) point 3),
   reported as a `GeneralSecurityException` that gives no signal about which branch of
   the rule actually failed.
5. Elsewhere in `FileService` (methods unrelated to `RepositoryFileProvider`, e.g.
   `systemBackup`/`systemRestore`), the same kind of coarse check
   (`doCanAdminister()` — the same read+create+administerSecurity triple) instead throws
   a plain `java.lang.SecurityException` — a **fifth** distinct shape, mentioned here only
   to illustrate how little consistency exists across the class as a whole.

In short: **whether an ABS-level denial is even visible as a distinct condition — and, if
so, what type it has — depends entirely on which of the five mechanisms above the specific
method being called happens to use.** There is no single exception type or boolean flag
that means "ABS-level" across all of `FileService`.

