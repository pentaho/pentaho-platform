---
type: reference
title: "Disambiguating FileService Exceptions: General Approach"
description: Exception/error package legend, shared helpers, the time-of-check race, and known gaps for public-API-only FileService exception disambiguation.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating `FileService` exceptions via public API calls

Companion to
[FileService Architecture Overview](../../../architecture/file-service/overview.md)
(referred to below as "the FileService doc") and, transitively, to
[Unified Repository Architecture Overview](../../../architecture/unified-repository/overview.md) /
[Unified Repository exception disambiguation](../../unified-repository/exception-disambiguation/index.md)
(the "main doc" / "main disambiguation doc") for the underlying `IUnifiedRepository`
layer.

This file is written from the point of view of a caller of `FileService` **itself** —
i.e. the same vantage point `RepositoryFileProvider` has: direct access to both the
`fileService` bean and the raw `unifiedRepository` bean it wraps. All snippets below call
`fileService.doXxx(...)` methods directly and catch whatever `FileService` itself
declares/throws — they do **not** go through any higher-level GFS
(`IGenericFileProvider`) exception type.

The FileService doc's §3/§4 show that, at this layer, ambiguity is **worse** than at the
`IUnifiedRepository` layer, for two reasons:

1. Wherever `FileService` passes an `IUnifiedRepository` exception through unchanged, the
   main doc's ambiguity (not-found vs. no-read; no-write vs. no-delete) still applies
   verbatim.
2. `FileService` **adds its own, additional confounding** on top: `doMoveFiles`/
   `doRestoreFiles` collapse almost everything non-`URADE` into a bare `InternalError`
   (losing type information the main disambiguation doc's cause-based reasoning would
   otherwise rely on); `doGetTree` collapses not-found/no-read/anything-else into a
   silent `null`; `doSetMetadata`'s access check is not `hasAccess(ACL_MANAGEMENT)` at
   all, so the main disambiguation doc's `canManageAcl()` helper does not apply to it.

As with the main disambiguation doc, **this file avoids `getCause()` inspection
entirely** and relies only on public, documented follow-up calls — either on
`FileService`'s own public methods, or by falling back to the `IUnifiedRepository`
helpers from the main disambiguation doc when a `FileService` method offers no better
alternative of its own.

### Legend — exception/error packages

Several of the types caught below are generic-sounding names shared with other packages;
here is exactly which class each one is, since `FileService` mixes JDK exceptions,
generic Java errors, and Pentaho-specific ones more freely than the `IUnifiedRepository`
layer does:

| Name used below | Fully qualified class | Notes |
|---|---|---|
| `FileNotFoundException` | `java.io.FileNotFoundException` | Declared/thrown directly by several `FileService` methods (`doMoveFiles`, `doGetMetadata`, `setFileAcls`, `doGetContentCreator`, `doGetFileOrDir`) as an explicit not-found pre-check — **not** related to `javax.jcr.PathNotFoundException`/`ItemNotFoundException` from the main doc, and not the same instance that flows through `getRepoWs()`. |
| `InternalError` | `java.lang.InternalError` | The JDK's own (usually JVM-internal) unchecked `Error`, re-purposed by `doMoveFiles`/`doRestoreFiles` as a catch-all wrapper with no message and no cause set (FileService doc §2.1 pattern 3). Being an `Error`, not an `Exception`, it also won't be caught by a `catch (Exception e)` unless matched explicitly. |
| `GeneralSecurityException` | `java.security.GeneralSecurityException` | Declared/thrown by `doSetMetadata` when its own custom ACL-management rule denies the caller — unrelated to `UnifiedRepositoryAccessDeniedException`. |
| `IllegalArgumentException` | `java.lang.IllegalArgumentException` | Thrown directly by `doRename` (destination name collision) and by `CopyFilesOperation`'s constructor/`execute()` (invalid arguments, missing/non-folder destination directory) — a plain JDK unchecked exception, not a repository-specific type. |
| `UnifiedRepositoryException` / `UnifiedRepositoryAccessDeniedException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException` / `.UnifiedRepositoryAccessDeniedException` | Same classes documented in the main doc — `FileService` propagates them unchanged wherever it does not itself intercept and rewrap them (FileService doc §2.2). |
| `FileService.InvalidNameException` | `org.pentaho.platform.web.http.api.resources.services.FileService.InvalidNameException` | A `public static class` **nested inside `FileService` itself** (extends plain `java.lang.Exception`) — declared/thrown only by `doCreateDirSafe`/`isValidFolderName`/`isValidFileName`'s validation. Not related to any JCR or repository-level name-validation exception. |
| `NullPointerException` | `java.lang.NullPointerException` | The recurring not-a-real-exception defect (FileService doc §2.1-4/§4 point 4) — an unchecked JDK exception, never declared, thrown implicitly whenever a `null` lookup result is dereferenced without a check. |

## The general approach

1. **Prefer a `FileService`-level follow-up check when one exists** — e.g.
   `fileService.doesExist(pathId)`, `unifiedRepository.hasAccess(path, permissions)`
   (`FileService` has no wrapper of its own for this), or `fileService.doGetCanCreate()` /
   `doGetCanGetFileContent()` for the ABS-level checks this layer performs before ever
   calling into the repository.
2. **Fall back to the main disambiguation doc's helpers** (`isFoundAndReadable`,
   `canWrite`, `canDelete`, `canManageAcl` — all operate on the raw
   `IUnifiedRepository`/`unifiedRepository` bean, which `RepositoryFileProvider` always
   has a reference to alongside `fileService`) whenever a `FileService` call's failure
   has degraded to a point where only the underlying repository state can still be
   inspected (e.g. after an `InternalError`).
3. **Treat every follow-up as diagnostic, not authoritative** — same time-of-check race
   caveat as the main disambiguation doc, compounded here because some `FileService`
   methods (`doRestoreFiles`, `doMoveFiles`) call the repository in a loop across
   multiple file IDs before failing, so a follow-up check cannot even tell you *which*
   file in the batch was the actual cause, only that *at least one* of them currently
   fails the check.
4. **Some `FileService`-introduced conditions cannot be disambiguated by any public call
   at all** — most notably, anything that was already collapsed into a bare
   `InternalError` with no message and no cause. In those cases, the honest answer is
   "narrowed to: not-found, or some other non-access-denied repository failure,
   indistinguishable further" — do not fabricate more precision than the API allows.
5. **A `UnifiedRepositoryAccessDeniedException` reaching a `FileService` caller is (with
   exactly one exception) *always* the coarse ABS-level action check, never a per-file
   denial** — same rule as the main disambiguation doc's point 2, inherited unchanged
   through this layer. Per the main doc's summary table (§3) and exception taxonomy (§4),
   every `IUnifiedRepository` method used by `FileService` below reports per-file
   write/delete/read-ACL denial as the **generic** `UnifiedRepositoryException`, never
   `URADE` — the **sole exception is `updateAcl`** (wrapped by `setFileAcls`), where
   `DefaultUnifiedRepository` throws `URADE` **directly** for both the ABS check *and* a
   per-file `ACL_MANAGEMENT` denial, indistinguishable by type alone. Everywhere else in
   this document, treat a caught `URADE` as unambiguous ABS-level evidence and do **not**
   run per-file follow-up checks against it — those belong in the generic/`InternalError`
   branch instead, where per-file denial actually surfaces.

### Shared helpers

In addition to the main disambiguation doc's `isFoundAndReadable`/`canWrite`/`canDelete`/
`canManageAcl` (unchanged, operate on `unifiedRepository`), this layer adds:

```java
import org.pentaho.platform.web.http.api.resources.services.FileService;

// "Found" at the FileService level — confounds not-found with no-read, exactly like
// isFoundAndReadable() at the IUnifiedRepository level, but usable when only a
// path-id string (not a RepositoryFile/fileId) is at hand.
static boolean fileServiceExists(FileService fileService, String pathId) {
    return fileService.doesExist(pathId);
}

// Global (ABS) create permission — distinguishes "no permission to do this kind of
// operation at all" from a per-file/per-path denial reported by a specific method call.
static boolean canCreateAnything(FileService fileService) {
    return Boolean.parseBoolean(fileService.doGetCanCreate());
}

// Global (ABS + whitelist) content-download permission, used internally by
// FileService's own content-serving logic.
static boolean canDownloadContent(FileService fileService, String fileName) {
    return fileService.doGetCanGetFileContent(fileName);
}
```

### Time-of-check race

Unchanged from the main disambiguation doc, with one addition: several `FileService`
methods used here (`doMoveFiles`, `doRestoreFiles`, `doDeleteFiles`,
`doDeleteFilesPermanent`) operate on a **comma-separated batch of file IDs** in a loop,
stopping at the first failure. A follow-up check made after the loop has already failed
can only tell you the aggregate state of the *whole* batch as it stands *now* — not which
specific ID in the original list caused the failure, nor its state at the time of failure.
If you need per-item precision, check each ID individually **before** calling the batch
method, accepting that a race can still occur between your check and the batch call.

### Known gaps (no public API exists to check these)

All of the main disambiguation doc's gaps still apply when the failure passes through
unchanged from `IUnifiedRepository` (`jcr:readAccessControl`, lock/restore-specific
privileges, `jcr:removeChildNodes` on a parent, `.trash` folder access). In addition, at
this layer:

- **Once an exception has been collapsed into a bare `InternalError`** (`doMoveFiles`'s
  and `doRestoreFiles`'s catch-all branch, FileService doc §2.1 pattern 3), there is no
  way to recover which specific repository-level exception it originally was. The best a
  caller can do is run the generic not-found/no-write follow-up checks below and report
  the remainder as "some other repository failure".
- **`doSetMetadata`'s custom ACL-management rule** (FileService doc §4 point 3: owner, or
  all three of `repository.read`+`repository.create`+`administer.security`, or an
  explicit `ACL_MANAGEMENT`/`ALL` ACE) has no single public method that evaluates it
  directly. `doSetMetadata()` only throws a plain `GeneralSecurityException` either way
  — a caller wanting to predict the outcome must reimplement the same combination of
  checks (`unifiedRepository.getAcl(fileId).getOwner()`, `policy.isAllowed(...)` for the
  three ABS actions, or inspecting the ACL's own ACEs) rather than relying on a single
  existing helper.
- **`doGetTree`'s internal two-hop cause inspection is invisible to callers** — it only
  ever affects which log level `FileService` itself uses; the outcome any caller of
  `doGetTree` observes is always the same `null`, whether the root path was missing,
  unreadable, or something else entirely failed. A follow-up
  `unifiedRepository.getFile(rootPath)`/`hasAccess(rootPath, READ)` check (bypassing
  `FileService` entirely) is the only way to recover a not-found-vs-no-read signal here.

---

