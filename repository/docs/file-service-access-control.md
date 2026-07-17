# FileService Access Control Analysis

> Companion to `unified-repository-access-control.md` (the `IUnifiedRepository` layer) and
> `unified-repository-access-control-disambiguation.md` (public-API disambiguation
> strategies for that layer). This document covers the **`FileService`** layer itself —
> its own access-control-relevant logic and exception-handling behavior, on top of the
> `IUnifiedRepository` bean it wraps.
>
> The Generic File System (GFS) `RepositoryFileProvider` is used throughout only as a
> **worked example of a real caller** — it is the concrete illustration for which
> `FileService` operations matter in practice, and for how a caller might mix
> `FileService` calls with direct `IUnifiedRepository` calls. It is not itself the
> subject of this document.
>
> Source files analysed:
> - `FileService.java` (`pentaho-platform/extensions/.../web/http/api/resources/services`) —
>   the class under analysis.
> - `DefaultUnifiedRepositoryWebService.java` (`pentaho-platform/repository/.../unified/webservices`) —
>   `FileService`'s `getRepoWs()` return type.
> - `CopyFilesOperation.java` (`pentaho-platform/extensions/.../web/http/api/resources/operations`) —
>   helper used by `FileService.doCopyFiles`.
> - `RepositoryFileProvider.java` (`pentaho-generic-file-system/impl/.../providers/repository`) —
>   used only as the worked example described above.

---


## 1. Bean composition and call chain

```
RepositoryFileProvider (GFS)
  ├─► unifiedRepository                         (same bean documented in the main doc)
  │     used directly for: getFile, getAcl (owner lookup), hasAccess
  │
  └─► fileService                               (FileService, or CustomFileService subclass)
        used for: doCreateDirSafe, doGetTree, isPathValid, doGetCanGetFileContent,
                   getRepositoryFileInputStream/OutputStream, doGetDeletedFiles,
                   doDeleteFilesPermanent, doDeleteFiles, doRestoreFiles, doGetCanCreate,
                   doesExist, isValidFileName, doRename, doCopyFiles, doMoveFiles,
                   doGetMetadata, doSetMetadata, doGetFileAcl, setFileAcls, getRepository()
        │
        └─► getRepoWs()                          (DefaultUnifiedRepositoryWebService)
              │  DTO-translation pass-through only — see §2.2. No extra access-control
              │  logic, no extra exception handling beyond a few unrelated ETC-folder /
              │  mime-type checks.
              └─► unifiedRepository               (same bean, same rules as main doc)
```

**Key structural fact:** `RepositoryFileProvider` does **not** use a single, consistent API.
Per the class's own comment: *"The file service wraps a unified repository and provides
additional functionality."* For each operation it picks whichever of `fileService` /
`unifiedRepository` best fits — sometimes for convenience (DTOs, ID-encoding, path
validation), sometimes because `FileService` implements extra business logic
(`doGetCanGetFileContent`'s whitelist, `doSetMetadata`'s hidden-flag handling,
`doCreateDirSafe`'s parent auto-creation). This means the **effective exception contract
for a given GFS operation depends on which of the two paths was chosen**, and the two
paths do not always behave the same way for the same not-found/no-access condition (see
§5).

`RepositoryFileProvider` also injects a `CustomFileService` subclass instead of a plain
`FileService`, purely to force `getRepoWs()`, `getRepositoryFileInputStream`, and
`getRepositoryFileOutputStream` to use the **specific** `IUnifiedRepository` instance the
provider was constructed with (rather than a static lookup via `PentahoSystem.get(...)`) —
this is a wiring detail, not an access-control difference.

---

## 2. Layer-by-layer access control

### 2.1 `FileService` — role and general shape

Unlike `ExceptionLoggingDecorator` (main doc §2.1), `FileService` is **not** a pure
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

#### How ABS-level (coarse, global) denials are surfaced

`FileService` has **no single, consistent mechanism** for signalling a coarse ABS-action
denial, distinctly from a per-file JCR/ACL denial. Depending on the method, one of (at
least) four different things happens:

1. **The underlying `IUnifiedRepository` AOP interceptor still applies**, unchanged,
   whenever a method reaches `getRepoWs()`/`unifiedRepository` (§2.2): it throws `URADE`
   *before* the target method body — and hence the specific file — is ever touched (main
   doc §2.2). Since, per the main doc, the *per-file* write/delete denial for virtually
   every method (everything except `updateAcl`) instead surfaces as the plain generic
   `UnifiedRepositoryException`, **catching `URADE` specifically at this boundary is, by
   itself, already unambiguous evidence of the ABS-level check having failed** — the same
   reasoning the main disambiguation doc's "URADE-gobbling" fix relies on.
2. **Some methods expose the check as a plain boolean/string, not an exception at all** —
   `doGetCanCreate()` (wraps `getPolicy().isAllowed(RepositoryCreateAction.NAME)`),
   `doGetCanPublish()`, `doGetCanDownloadWithWhitelist()`/`doGetCanGetFileContent()`. These
   throw nothing; it is entirely the caller's responsibility to check the result *before*
   invoking the operation and decide what to do if it is `false` (§2.4's worked example
   shows `RepositoryFileProvider` doing exactly this ahead of `renameFile`/`copyFile`/
   `moveFile`/`doCreateDirSafe`).
3. **`doCopyFiles` performs the *same* `RepositoryCreateAction` ABS check a second time,
   itself, redundantly** — at the very top of the method, before ever constructing a
   `CopyFilesOperation` (§2.3) — and, if denied, throws a bare `IllegalArgumentException()`
   with no message. This is a **materially misleading exception type**: the *same*
   `IllegalArgumentException` class is *also* thrown by `CopyFilesOperation`'s own
   constructor/`execute()` for genuinely invalid-argument conditions (missing/non-folder
   destination directory, §2.3) that have nothing to do with permissions. A caller cannot
   tell these two apart by exception type alone — it must additionally call
   `doGetCanCreate()` itself to know whether an observed `IllegalArgumentException` from
   `doCopyFiles` means "no create permission at all" or "bad destination argument".
4. **`doSetMetadata` uses yet another shape**: neither `hasAccess`-style, nor a plain ABS
   action check, but a custom rule (owner, **or** the same
   `repository.read`+`repository.create`+`administer.security` triple used by
   `doCanAdminister()`, **or** an explicit `ACL_MANAGEMENT`/`ALL` ACE — §4 point 3),
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

### 2.2 `DefaultUnifiedRepositoryWebService` (`getRepoWs()`)

Role: DTO translation only (`RepositoryFile` ↔ `RepositoryFileDto`, `RepositoryFileAcl` ↔
`RepositoryFileAclDto`, etc.) via `RepositoryFileAdapter`/`RepositoryFileAclAdapter`. Every
method is a one-line call into the injected `IUnifiedRepository` plus adapter conversion.
There is **no `try`/`catch` around any of these calls** (the handful of `catch`/`throw`
statements in the class guard unrelated things — ETC-folder access and mime-type
whitelisting on `createFile`/`updateFile`). Consequently:

> Every exception the main doc documents for `IUnifiedRepository` (`URADE`, `URE` and its 5
> subclasses, and the `NullPointerException` defect on `updateAcl`) propagates through
> `getRepoWs()` completely unchanged. This layer can be treated as transparent for
> access-control purposes.

### 2.3 `CopyFilesOperation` (used only by `FileService.doCopyFiles`)

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

### 2.4 Worked example: `RepositoryFileProvider`'s exception mapping

This subsection is not about `FileService` itself, but about how one real caller
(`RepositoryFileProvider`, from the Generic File System) reacts to what `FileService`/
`IUnifiedRepository` throw — included because it is a concrete, verified illustration of
which of the divergences in §4 actually matter to a caller in practice. It translates
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
| `OperationFailedException` (direct) | `Exception` | Catch-all: any other exception, including the lossy `InternalError` from §2.1 pattern 3 |

Two access-denial exceptions exist at this layer for a reason: `AccessControlException` and
`ResourceAccessDeniedException` are **not interchangeable** — the former means "you may
never do this kind of operation at all" (ABS/global), the latter means "you specifically
cannot do this to this file" (per-file JCR ACL). Several methods check both, in that order
(global first, then per-file) — e.g. `renameFile`, `copyFile`, `moveFile` all pre-check
`doGetCanCreate()` before attempting the operation, then separately catch
`UnifiedRepositoryAccessDeniedException` from the operation itself.

---

## 3. Summary table – per `FileService` operation

The rightmost "GFS caller sees" style framing has been kept only where it clarifies what a
real caller observes; the primary subject of each row is the `FileService` method itself.

Columns:
- **Underlying call(s)**: which of `fileService` / `unifiedRepository` is used, and which
  method(s) on it.
- **Not-found-or-no-read-access**: what the *GFS caller* (`RepositoryFileProvider`) observes
  — not the raw `FileService` exception — for source/destination not-found or no-read.
- **No-write-or-delete-access**: same, for write/delete/rename/move/copy/ACL-manage denial.

`URADE` = `UnifiedRepositoryAccessDeniedException`. `URE` = `UnifiedRepositoryException`
(generic or subclass — see main doc §4 for the JCR-level cause chain, still valid whenever
the call passes through `getRepoWs()`/`unifiedRepository` unchanged).

| GFS operation | Underlying call(s) | Not-found-or-no-read-access | No-write-or-delete-access |
|---|---|---|---|
| `getFile` | `unifiedRepository.getFile()` directly (not `fileService`) | <ul><li>Caught `UnifiedRepositoryAccessDeniedException` → `ResourceAccessDeniedException` (ABS-level "no repository.read at all" case, explicitly commented in source as distinct from per-file denial)</li><li>`null` result (file missing, or per-file JCR read denied — main doc's not-found/no-read confounding) → `NotFoundException`</li><li>Any other `UnifiedRepositoryException` → `OperationFailedException` (source comment: "Should never happen, `null` is returned instead")</li></ul> | N/A (read-only) |
| `getTree` (`getTreeCore`) | `fileService.doGetTree()` → `getRepoWs().getTreeFromRequest()` | <ul><li>`doGetTree` internally catches `UnifiedRepositoryException`, inspects the exact two-hop cause (`e.getCause().getCause() instanceof PathNotFoundException`) purely to pick a **log level** (debug vs error) — either way it returns `null`</li><li>`RepositoryFileProvider` sees `null` (root path not found, **or** no read access, **or** any other `UnifiedRepositoryException` at all — all three collapse to the same outcome) → `NotFoundException`</li><li>Descendant nodes the caller cannot read are silently omitted from the tree by Jackrabbit (main doc §2.4.3/§2.4.6) — no exception either way</li></ul> | N/A (read-only) |
| `getFileContent` (compressed=false) | `getNativeFile` (below) then `fileService.doGetCanGetFileContent()` + `fileService.getRepositoryFileInputStream()` | <ul><li>Same as `getFile` (via `getNativeFile`)</li><li>`RepositoryFileInputStream` constructor: `FileNotFoundException` → `NotFoundException`</li></ul> | <ul><li>Whitelist/publish check fails (`doGetCanGetFileContent` false) → `ResourceAccessDeniedException` — this is a **content-type policy** check, not a JCR privilege</li></ul> |
| `getFileContent` (compressed=true) | `fileService.isPathValid()` + `SystemUtils.canDownload()` + export stream | <ul><li>Invalid path (`/etc`, `/system`) → `InvalidOperationException` (not `NotFoundException` — treated as a bad request, not a missing file)</li></ul> | <ul><li>Global download permission denied → `AccessControlException`</li><li>Per-path download denied → `ResourceAccessDeniedException`</li></ul> |
| `getNativeFile` (helper, used by `getFile`/`getFileContent`/`doesFolderExist`) | `unifiedRepository.getFile()` | Same three cases as `getFile` above | N/A |
| `doesFolderExist` | `getNativeFile` | `NotFoundException` from `getNativeFile` is caught and turned into `false` (not propagated) | N/A |
| `getDeletedFiles` | `fileService.doGetDeletedFiles()` → `getRepoWs().getDeletedFiles()` | Inaccessible entries silently excluded by Jackrabbit (main doc `getAllDeletedFiles` row) — no exception | N/A |
| `deleteFilePermanently` | `fileService.doDeleteFilesPermanent()` (pass-through pattern, §2.1-1) | Any exception (including not-found `URE`) → `OperationFailedException` (this operation does **not** special-case `URADE` — everything, access-denied included, becomes generic `OperationFailedException`) | Same — no distinct handling of write/delete denial vs. not-found |
| `deleteFile` (soft or permanent, dispatch on `permanent` flag) | `fileService.doDeleteFiles()` / `doDeleteFilesPermanent()` (pass-through pattern) | Same as above — all exceptions collapse to `OperationFailedException` | Same |
| `restoreFile` | `fileService.doRestoreFiles()` (catch-and-swallow pattern, §2.1-3) | <ul><li>Caught `UnifiedRepositoryAccessDeniedException` → `AccessControlException`</li><li>Caught `InternalError` (i.e. **any other exception**, including a genuine not-found `URE`) → `OperationFailedException` — not-found and "some other JCR write problem" are indistinguishable here, and the original exception type/cause is lost at the `InternalError` step</li></ul> | Folded into the same `InternalError`/`AccessControlException` split above — write-denial and not-found are only distinguished from *access*-denial (`URADE`), not from each other |
| `renameFile` | `fileService.doesExist()` (pre-check) + `fileService.isValidFileName()` + `fileService.doRename()` | <ul><li>`doesExist()` false → `NotFoundException` (explicit pre-check by the provider, before calling `doRename`)</li><li>Inside `doRename` itself: if the pre-check somehow missed a since-deleted/unreadable file, `fileToBeRenamed.getPath()` on a `null` result throws `NullPointerException` (§2.1-4 defect) → caught by the provider's generic `catch (Exception e)` → `OperationFailedException`</li></ul> | <ul><li>Global create permission denied (`doGetCanCreate()` false) → `AccessControlException`</li><li>New name invalid → `InvalidOperationException`</li><li>Destination (renamed-to) path already exists → `ConflictException` (GFS-level pre-check; internally, `doRename` would otherwise throw `IllegalArgumentException` for the same condition — the provider never lets that surface, since it pre-checks first)</li><li>Caught `UnifiedRepositoryAccessDeniedException` from the underlying `moveFile` → `ResourceAccessDeniedException`</li></ul> |
| `copyFile` | `fileService.doesExist()` (pre-checks) + `fileService.doCopyFiles()` → `CopyFilesOperation` | <ul><li>Destination folder does not exist (`doesExist()` false) → `NotFoundException` (GFS-level pre-check)</li><li>Source not found/no-read: **not pre-checked** by the provider; surfaces from `CopyFilesOperation`/JCR as `ItemNotFoundException` → `URE` (main doc `copyFile` row) → caught by the provider's generic `catch (IllegalArgumentException e)` only if it happens to be that type, otherwise falls through uncaught by any of this method's specific catches (only `URADE` and `IllegalArgumentException` are caught) — **not wrapped into `OperationFailedException`** here, propagates as-is</li></ul> | <ul><li>Global create permission denied → `AccessControlException`</li><li>New copy's target path already exists → `ConflictException` (GFS-level pre-check)</li><li>Caught `UnifiedRepositoryAccessDeniedException` → `ResourceAccessDeniedException`</li><li>Caught `IllegalArgumentException` (this provider already pre-checked `doGetCanCreate()` itself, so in practice this only reaches `doCopyFiles`'s *own*, redundant `RepositoryCreateAction` check — §2.1's ABS box, point 3 — in the narrow race where permission is revoked between the two checks; otherwise it is `CopyFilesOperation`'s destination-directory validation, §2.3) → `OperationFailedException` either way — the provider does not distinguish the two</li></ul> |
| `moveFile` | `fileService.doesExist()` (pre-check) + `fileService.doMoveFiles()` (explicit not-found pre-check + catch-and-swallow pattern, §2.1-2/3) | <ul><li>Destination folder not found: `doMoveFiles` throws `FileNotFoundException` explicitly → caught → `NotFoundException`</li><li>Source not found/no-read: not pre-checked here either; `getRepoWs().moveFile()` throws `ItemNotFoundException` → `URE` → falls into `doMoveFiles`'s `catch (Exception e)` branch (since it's neither `IllegalArgumentException` nor `URADE`) → wrapped in `InternalError` → caught by the provider's own `catch (InternalError e)` → `OperationFailedException` (source not-found is **indistinguishable** from any other non-`URADE` JCR failure)</li></ul> | <ul><li>Global create permission denied → `AccessControlException`</li><li>Move target path already exists → `ConflictException` (GFS-level pre-check)</li><li>Caught `UnifiedRepositoryAccessDeniedException` (source **or** destination write-denial, main doc `moveFile` row) → `ResourceAccessDeniedException`</li><li>Any other JCR write failure (e.g. destination's parent missing → `IllegalArgumentException` from the underlying `moveFile`) → propagates as-is from `doMoveFiles` (it is explicitly re-thrown, not wrapped) — **not** turned into `InternalError`/`OperationFailedException` the way a generic `URE` would be</li></ul> |
| `getFileMetadata` | `fileService.doGetMetadata()` (explicit not-found pre-check, pass-through otherwise) | <ul><li>`null` file → `FileNotFoundException` (explicit) → caught → `NotFoundException`</li></ul> | <ul><li>Caught `UnifiedRepositoryAccessDeniedException` → `ResourceAccessDeniedException`</li><li>Any other `UnifiedRepositoryException` → `OperationFailedException`</li></ul> |
| `setFileMetadata` | `fileService.doSetMetadata()` (custom ACL-management check — see §5) | <ul><li>`null` file: **not checked** — `getRepoWs().getAcl(file.getId())` throws `NullPointerException` immediately → **not caught by any of this GFS method's `catch` clauses** (only `GeneralSecurityException` is caught) → propagates as an unchecked exception out of the provider entirely</li></ul> | <ul><li>`doSetMetadata`'s own custom check (owner, or all three of `repository.read`+`repository.create`+`administer.security` ABS actions, or an ACE granting `ACL_MANAGEMENT`/`ALL` — **not** a call to `hasAccess(ACL_MANAGEMENT)` nor the JCR `aclManagement` privilege the main doc's `updateAcl` row relies on) fails → `GeneralSecurityException` → caught → `AccessControlException`</li></ul> |
| `getFileAcl` | `fileService.doesExist()` (pre-check) + `fileService.doGetFileAcl()` | <ul><li>`doesExist()` false → `NotFoundException` (GFS-level pre-check)</li><li>If the pre-check races with a concurrent delete: `doGetFileAcl` has **no null-check** (§2.1-4) — `file.getId()` on `null` throws `NullPointerException` → falls into the provider's `catch (Exception e)` → `OperationFailedException`</li></ul> | <ul><li>Caught `UnifiedRepositoryAccessDeniedException` (from `getRepoWs().getAcl()`, i.e. `ItemNotFoundException`/`AccessDeniedException` for `jcr:readAccessControl` — main doc `getAcl` row) → `ResourceAccessDeniedException`</li></ul> |
| `setFileAcl` | `fileService.setFileAcls()` (explicit not-found pre-check, pass-through otherwise) | <ul><li>`null` file → `FileNotFoundException` (explicit) → caught → `NotFoundException`</li></ul> | <ul><li>Invalid principal in the ACL → `InvalidOperationException` (GFS-level pre-check, before calling `setFileAcls`)</li><li>Caught `UnifiedRepositoryAccessDeniedException` (main doc `updateAcl` row — including the **Owner-ACE gap**: an owner can still be denied here unless separately granted `ACL_MANAGEMENT`) → `ResourceAccessDeniedException`</li></ul> |
| `hasAccess` / `owns` | `unifiedRepository.hasAccess()` directly | `false` (JCR `PathNotFoundException` caught internally, main doc `hasAccess` row) — same result whether not-found or no-read | `false` — same mechanism, does not distinguish READ from WRITE/DELETE permission sets requested |
| owner lookup (`getOwnerByFileId`, used when converting native files/DTOs for display) | `unifiedRepository.getAcl()` directly | `null` ACL (not found/no `jcr:readAccessControl`) → owner silently reported as `null` — no exception at all, this is a display-only helper | N/A |
| `doCreateDirSafe` (folder creation, incl. auto-vivifying missing intermediate segments) | `fileService.doCreateDirSafe()` → `doCreateDirFor()` → `getRepoWs().createFolder()` per missing segment | <ul><li>Invalid name (contains `/`, `\`, is `.`/`..`, reserved chars, or fails `doGetReservedChars()`-based validation) → `FileService.InvalidNameException` → caught by the provider → `InvalidPathException`</li></ul> | <ul><li>Caught `UnifiedRepositoryAccessDeniedException` (from `createFolder` on whichever segment lacks `jcr:addChildNodes`, main doc `createFolder` row) → `AccessControlException` (note: mapped to the **global** GFS exception here, not `ResourceAccessDeniedException`, even though the underlying denial is per-node)</li></ul> |
| `getRepository()` (raw `IUnifiedRepository`, used only for `ZipExportProcessor` export) | `fileService.getRepository()` | N/A (no access check here — delegated entirely to the export processor, out of scope of this doc) | N/A |

---

## 4. Where `FileService`'s contract diverges from `IUnifiedRepository`'s

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
   `IUnifiedRepository.updateAcl`'s `hasAccess(ACL_MANAGEMENT)` check (main doc §2.3): it
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
   already exist (§2.3), whereas the main doc's `copyFile` only errors when the
   destination's *parent* is missing.
7. **Two different "access denied" exception types with different scopes** exist at the GFS
   layer (`AccessControlException` for ABS/global denials, `ResourceAccessDeniedException`
   for per-file denials) where the main doc has only one (`URADE`) — see §2.4's mapping
   table. When both a global and a per-file check exist for the same operation (e.g.
   `renameFile`, `copyFile`, `moveFile`), the GFS caller must know which exception type it
   is looking at to know *which* permission needs to be granted.

---

## 5. Notes for building a disambiguation strategy at this layer

A dedicated companion file,
[`file-service-access-control-disambiguation.md`](./file-service-access-control-disambiguation.md),
provides full per-operation, public-API-only disambiguation snippets for this layer
(mirroring `unified-repository-access-control-disambiguation.md` for the main doc). The
notes below summarize why the underlying `IUnifiedRepository` disambiguation approach
needs adaptation here.

The public-API disambiguation approach from
`unified-repository-access-control-disambiguation.md` (using `hasAccess()`/`getFileById()`
follow-up calls) remains valid **wherever a `FileService` call is a pure pass-through to
`getRepoWs()`** (§2.1 pattern 1/2) or where `RepositoryFileProvider` calls
`unifiedRepository` directly (`getFile`, `getAcl`, `hasAccess`). It is **not sufficient** on
its own for methods affected by §4's divergences, because:

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
- `setFileMetadata`'s access check is `doSetMetadata`'s own custom rule (§4 point 3), not
  `hasAccess(ACL_MANAGEMENT)` — a follow-up disambiguation must replicate *that* rule
  (owner-or-triple-ABS-action-or-explicit-ACE), not the JCR privilege used elsewhere.
