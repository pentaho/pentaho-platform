---
type: reference
title: Disambiguating FileService Exceptions Via Public API Calls
description: Public-API-only snippets for detecting the real cause behind FileService's inconsistent, legacy exception surface, complementing the architecture analysis for API consumers.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating `FileService` exceptions via public API calls

Companion to
[`file-service-access-control.md`](../architecture/file-service-access-control.md)
(referred to below as "the FileService doc") and, transitively, to
[`unified-repository-access-control.md`](../architecture/unified-repository-access-control.md) /
[`unified-repository-exception-disambiguation.md`](./unified-repository-exception-disambiguation.md)
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

## Per-operation snippets

All snippets below call `fileService` (and, where `FileService` itself offers no better
signal, `unifiedRepository`) **directly** — this is the code `RepositoryFileProvider`
itself would run internally, before ever translating anything into a GFS-layer
exception. `pathId` is the colon-separated id `FileService` expects (as returned by
`FileUtils.idToPath`/its inverse); `fileId` is the raw repository file id.

**`doesExist` / `isFolder` / `doGetIsVisible`** (and any other `FileService` method that
internally calls `getRepoWs().getFile(...)` and checks for `null`) — none of these three
methods catch anything at all (FileService doc, `getFile`/`getFileById` row): a per-file
not-found/no-read confounds into the same `null`/`false` as at the `IUnifiedRepository`
level, but the **ABS-level `repository.read` check is not swallowed here** — since nothing
inside these three methods catches `URADE`, it propagates straight out as an unchecked
exception, even though none of them *declare* throwing anything:

```java
try {
    RepositoryFileDto file = fileService.getRepoWs().getFile(FileUtils.idToPath(pathId));
    if (file == null) {
        // Not found, no jcr:read, or a custom accessVoterManager voter denied READ.
        // Same as the main disambiguation doc's getFile/getFileById row — unresolvable
        // via public API, whether reached through FileService or IUnifiedRepository directly.
    }
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.read action at all. Unlike getFile()'s "null" case
    // above, this is unambiguous — it fires before any specific file is even looked up.
}
```

**`doGetTree`** (collapses everything — not-found, no-read, **and ABS-level `URADE`
alike** — to `null`; its internal `catch (UnifiedRepositoryException e)` catches `URADE`
too, so the ABS-level check is swallowed exactly like any other cause; disambiguate by
bypassing `FileService` and asking `unifiedRepository` directly about the root path):

```java
RepositoryFileTreeDto tree = fileService.doGetTree(pathId, depth, filter, showHidden, includeAcls);
if (tree == null) {
    try {
        if (!isFoundAndReadable(unifiedRepository, FileUtils.idToPath(pathId))) {
            // ROOT path not found, or not readable (still ambiguous between the two — see
            // main doc). This is the one case doGetTree's null collapses that we CAN recover.
        } else {
            // Root path is found and readable right now: the original null was caused by
            // something else entirely (any other UnifiedRepositoryException doGetTree caught
            // internally, then swallowed) — not further diagnosable via public API; likely a
            // race, or a non-access-control repository failure doGetTree's swallow-to-null
            // behavior hid from us.
        }
    } catch (UnifiedRepositoryAccessDeniedException e) {
        // This follow-up call ITSELF requires repository.read — if the ORIGINAL doGetTree
        // failure was the ABS-level check, this same check fires again here (same
        // session/user), confirming it. Unambiguous: no repository.read action at all.
    }
}
```

**`doDeleteFiles` / `doDeleteFilesPermanent`** (pure pass-through to
`getRepoWs().deleteFile(...)`/`deleteFileWithPermanentFlag(...)` in a loop over a
comma-separated id list; declared `throws Exception`, so the original
`UnifiedRepositoryException`/`URADE` type is preserved — but the failing id within the
batch is not identified):

```java
try {
    fileService.doDeleteFilesPermanent(fileIdsCsv);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.create action at all (deleteFile/permanentFlag's ABS
    // action, main doc §3) — thrown before any specific id in the batch is even touched.
    // Per-file DELETE denial does NOT surface this way (see the generic catch below).
} catch (Exception e) {
    // check each id individually — the batch call gives no per-id signal:
    for (String fileId : fileIdsCsv.split(",")) {
        if (!isFoundAndReadable(unifiedRepository, fileId)) {
            // this id: not found, or no jcr:read
        } else if (!canDelete(unifiedRepository, unifiedRepository.getFileById(fileId).getPath())) {
            // this id: no jcr:remove(ChildNodes)
        }
        // if neither, this id looks fine right now — the original failure was something
        // else (e.g. permanentlyDeleteFile's referential-integrity conflict, or a
        // parent/.trash privilege gap — main doc §2.4.8, known gaps above); not further
        // diagnosable, and possibly a race if this wasn't actually the failing id
    }
}
```

**`doRestoreFiles`** (declared `throws InternalError`; `UnifiedRepositoryAccessDeniedException`
is explicitly re-thrown unchanged, everything else — including a genuine not-found —
becomes a bare `InternalError` with no message/cause):

```java
try {
    fileService.doRestoreFiles(fileIdsCsv);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.create action at all (undeleteFile's ABS action,
    // main doc §3) — per-file WRITE denial does NOT surface this way (see below).
} catch (InternalError e) {
    // Everything else collapsed here — including genuine per-file WRITE denial (main
    // doc's undeleteFile row: uncaught AccessDeniedException at session.move() → generic
    // URE → InternalError). Disambiguate per id, same batch caveat as above:
    for (String fileId : fileIdsCsv.split(",")) {
        if (!isFoundAndReadable(unifiedRepository, fileId)) {
            // this id: the deleted item itself not found / not readable
        } else if (!canWrite(unifiedRepository, unifiedRepository.getFileById(fileId).getPath())) {
            // this id: no WRITE on the restore target
        } else {
            // this id looks readable/writable now, but restore still failed for some
            // other reason (e.g. a pre-existing file/folder at the restore destination —
            // main doc's undeleteFile row: RepositoryFileDaoFileExistsException — but that
            // specific type information was already destroyed by InternalError; cannot
            // confirm)
        }
    }
}
```

**`doRename`** (declared `throws Exception`; has no null-check on the looked-up source
file — FileService doc §2.1-4 — so a source that vanished/became unreadable between a
caller's own pre-check and this call surfaces as an uncaught `NullPointerException`
rather than any declared type):

```java
// Pre-check yourself; doRename() gives no better signal for the source than this:
if (fileService.getRepoWs().getFile(FileUtils.idToPath(pathId)) == null) {
    // source not found / not readable — check BEFORE calling doRename(), since a NPE
    // inside doRename() itself is not a meaningful "not found" signal to catch
}

try {
    fileService.doRename(pathId, newName);
} catch (IllegalArgumentException e) {
    // doRename's own pre-check: a file/folder already exists at the renamed-to path.
    // Not an access-control condition.
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.create action at all (moveFile's ABS action, main
    // doc §3) — per-file WRITE denial on the source does NOT surface this way.
} catch (Exception e) {
    if (fileService.getRepoWs().getFile(FileUtils.idToPath(pathId)) == null) {
        // confirms a race: source vanished/became unreadable after our own pre-check
    } else if (!canWrite(unifiedRepository, FileUtils.idToPath(pathId))) {
        // source found/readable but not writable: WRITE denied on the source (main doc's
        // moveFile row — surfaces as generic URE here, not URADE)
    } else {
        throw e; // some other, non-access-denied failure
    }
}
```

**`doCopyFiles`** (performs its **own** redundant `RepositoryCreateAction` ABS check
before ever constructing a `CopyFilesOperation`, throwing the exact same
`IllegalArgumentException` type that `CopyFilesOperation`'s constructor also throws for
genuinely invalid arguments — FileService doc §2.1's "ABS box" point 3 / §2.3; the
*source* is not pre-checked at all and its `ItemNotFoundException`/`URE` propagates as an
unchecked `UnifiedRepositoryException`, since neither `doCopyFiles` nor
`CopyFilesOperation` declares or catches it):

```java
try {
    fileService.doCopyFiles(destPathId, FileService.MODE_RENAME, sourceFileIdsCsv);
} catch (IllegalArgumentException e) {
    // Two, by-type-indistinguishable causes — disambiguate with follow-up calls:
    if (!canCreateAnything(fileService)) {
        // Cause 1: doCopyFiles's OWN ABS check failed (no repository.create action at
        // all). This is a global, non-file-specific permission, so — same request,
        // same user — it would have already been false at the moment doCopyFiles ran;
        // no meaningful time-of-check race here.
    } else if (!fileServiceExists(fileService, destPathId) || !fileService.isFolder(destPathId)) {
        // Cause 2: CopyFilesOperation's OWN validation (FileService doc §2.3) — the
        // destination does not exist, or exists but is not a folder. Not an
        // access-control condition — the opposite direction from IUnifiedRepository's
        // copyFile, which only errors on a missing destination PARENT.
    } else {
        // Neither check reproduces a failure now: most likely a time-of-check race (the
        // destination's existence/folder-ness changed between the original call and this
        // diagnostic check, e.g. someone else created/removed/replaced it) — not further
        // diagnosable via public API.
    }
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.create action at all (copyFile's ABS action, main doc
    // §3) — per-file destination WRITE denial does NOT surface this way (see below).
} catch (UnifiedRepositoryException e) {
    // Uncaught by doCopyFiles/CopyFilesOperation, so it reaches us as-is — could be
    // SOURCE not-found/no-read, or DESTINATION write-denial (both generic URE here):
    for (String fileId : sourceFileIdsCsv.split(",")) {
        if (!isFoundAndReadable(unifiedRepository, fileId)) {
            // this id: source not found / no jcr:read
        }
    }
    if (!canWrite(unifiedRepository, FileUtils.idToPath(destPathId))) {
        // DESTINATION not writable (main doc's copyFile row)
    }
}
```

**`doMoveFiles`** (declared `throws FileNotFoundException` for an explicit destination
pre-check; `UnifiedRepositoryAccessDeniedException` is re-thrown unchanged from the per-id
loop; everything else — including a genuine source-not-found, or the destination's
*parent* also missing — becomes a bare `InternalError`):

```java
try {
    fileService.doMoveFiles(destPathId, sourceFileIdsCsv);
} catch (FileNotFoundException e) {
    // doMoveFiles's own explicit pre-check: DESTINATION folder not found. Unambiguous.
} catch (IllegalArgumentException e) {
    // Declared as re-thrown by doMoveFiles's own catch clause, but in practice this is
    // effectively dead code for the underlying moveFile() call: the repository.spring.xml
    // exceptionConverterMap has no entry for java.lang.IllegalArgumentException, so
    // ExceptionLoggingDecorator (main doc §2.1) wraps ANY IllegalArgumentException thrown
    // inside moveFile() (e.g. the destination's PARENT also missing) into a generic URE
    // BEFORE it ever reaches this catch — it lands in the InternalError branch below
    // instead. If this branch fires at all, it was thrown by something outside that
    // wrapped call (not identified in FileService's own code for this method).
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.create action at all (moveFile's ABS action, main doc
    // §3) — thrown before any specific id is touched. Per-file SOURCE/DESTINATION
    // write-denial does NOT surface this way — see the InternalError branch below,
    // where it actually lands (wrapped, since it's a generic URE at the JCR layer).
} catch (InternalError e) {
    // catch-all: SOURCE not found/no-read for one of the ids, SOURCE/DESTINATION
    // write-denial (generic URE, main doc's moveFile row), destination's PARENT also
    // missing (IllegalArgumentException wrapped to generic URE — see above), or any
    // other non-URADE JCR failure on the move itself. Per-id, same batch caveat as
    // doDeleteFiles* above:
    if (!canWrite(unifiedRepository, FileUtils.idToPath(destPathId))) {
        // DESTINATION not writable
    }
    String destParentPath = FileUtils.idToPath(destPathId);
    destParentPath = destParentPath.substring(0, destParentPath.lastIndexOf('/'));
    if (!isFoundAndReadable(unifiedRepository, destParentPath)) {
        // DESTINATION's PARENT not found / unreadable
    }
    for (String fileId : sourceFileIdsCsv.split(",")) {
        if (!isFoundAndReadable(unifiedRepository, fileId)) {
            // this id: confirms not found / no jcr:read
        } else if (!canWrite(unifiedRepository, unifiedRepository.getFileById(fileId).getPath())) {
            // this id: SOURCE not writable
        }
        // otherwise: readable/writable now, original cause destroyed by InternalError,
        // not further diagnosable
    }
}
```

**`doGetMetadata`** (declared `throws FileNotFoundException` via an explicit not-found
pre-check; everything else propagates as the underlying `getRepoWs().getFileMetadata()`
call throws it — i.e. `UnifiedRepositoryAccessDeniedException`/`UnifiedRepositoryException`
unchanged, same as the main doc's `getFileMetadata` row):

```java
try {
    fileService.doGetMetadata(pathId);
} catch (FileNotFoundException e) {
    // doGetMetadata's own explicit pre-check — unambiguous.
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.read action at all (getFileMetadata's ABS action,
    // main doc §3) — per-file no-read does NOT surface this way (it's generic URE,
    // handled in the catch below, alongside the not-found race check).
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, FileUtils.idToPath(pathId))) {
        // race: file became unreadable/was deleted after doGetMetadata's own pre-check
    } else {
        throw e; // some other non-access repository failure
    }
}
```

**`doSetMetadata`** (declared `throws GeneralSecurityException`; has **no** not-found
pre-check at all — a concurrently-missing file produces an uncaught
`NullPointerException`; before its custom access rule even runs, it internally resolves
the file/ACL via `getRepoWs()`, which is still subject to the ABS-level `repository.read`
check — so an unchecked `URADE` can also propagate; the rule itself is a custom one, not
`hasAccess(ACL_MANAGEMENT)` — see "Known gaps" above):

```java
// Pre-check existence yourself; doSetMetadata() gives nothing to catch for this:
if (fileService.getRepoWs().getFile(FileUtils.idToPath(pathId)) == null) {
    // not found / not readable — must be checked BEFORE calling doSetMetadata(),
    // there is no post-hoc signal to recover this from (it would surface as an
    // uncaught NullPointerException instead)
}

try {
    fileService.doSetMetadata(pathId, metadata);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: doSetMetadata's OWN internal getFile()/getAcl() calls (used to
    // resolve the file/owner before its custom rule even runs) require repository.read —
    // if that ABS action is denied, URADE propagates here unchecked, BEFORE the custom
    // ACL-management rule is ever evaluated. Not related to GeneralSecurityException below.
} catch (GeneralSecurityException e) {
    // doSetMetadata's own custom rule failed. To find out WHICH branch of that rule
    // failed, reimplement it exactly (FileService.doSetMetadata source):
    RepositoryFileAcl acl = unifiedRepository.getAcl(fileId);
    boolean isOwner = acl != null && currentUserName.equals(acl.getOwner().getName());
    boolean hasAdminTriple = policy.isAllowed(RepositoryReadAction.NAME)
        && policy.isAllowed(RepositoryCreateAction.NAME)
        && policy.isAllowed(AdministerSecurityAction.NAME);
    if (isOwner) {
        // Owner — should have passed; if we're here, a race changed ownership/ACL
        // between doSetMetadata's own check and this follow-up.
    } else if (hasAdminTriple) {
        // Has the admin triple — should also have passed; same race caveat as above.
    } else {
        // Neither: check the ACL's own ACEs for an explicit ACL_MANAGEMENT/ALL grant to
        // the current user — doSetMetadata resolves EFFECTIVE aces if the ACL inherits.
        List<RepositoryFileAce> aces = acl.isEntriesInheriting()
            ? unifiedRepository.getEffectiveAces(fileId)
            : acl.getAces();
        boolean hasExplicitGrant = aces.stream().anyMatch(ace ->
            ace.getSid().equals(currentUserSid)
                && (ace.getPermissions().contains(RepositoryFilePermission.ACL_MANAGEMENT)
                    || ace.getPermissions().contains(RepositoryFilePermission.ALL)));
        if (!hasExplicitGrant) {
            // Confirms: genuinely denied — none of the three rule branches pass.
        } else {
            // Grant found now but rule still failed originally: a race.
        }
    }
}
```

**`doGetFileAcl`** (no declared exception and no null-check at all — a not-found/no-read
condition on the target file surfaces as an uncaught `NullPointerException`; both the ABS
`repository.read` check and a genuine per-file `jcr:readAccessControl` denial from
`getRepoWs().getAcl()` propagate unchecked, but as **different** exception types):

```java
// Pre-check yourself; doGetFileAcl() gives no better signal for not-found:
if (fileService.getRepoWs().getFile(FileUtils.idToPath(pathId)) == null) {
    // not found / not readable — check BEFORE calling doGetFileAcl()
}

try {
    fileService.doGetFileAcl(pathId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.read action at all (getAcl's ABS action, main doc
    // §3) — thrown before the file's own ACL is even looked up.
} catch (UnifiedRepositoryException e) {
    // File found/readable, but the ACL read itself still failed — hits the
    // jcr:readAccessControl known gap from the main disambiguation doc: no public call
    // confirms this specifically beyond "file is found/readable but ACL read still failed".
}
```

**`setFileAcls`** (declared `throws FileNotFoundException` via an explicit pre-check;
`UnifiedRepositoryAccessDeniedException` from the underlying `updateAcl()` propagates
unchecked — this is the **one** method in this document where `URADE` is genuinely
ambiguous between ABS-level and per-file, mirroring the main doc's `updateAcl` row/snippet
exactly, since `setFileAcls` is a thin wrapper around it):

```java
try {
    fileService.setFileAcls(pathId, acl);
} catch (FileNotFoundException e) {
    // setFileAcls's own explicit pre-check — unambiguous.
} catch (UnifiedRepositoryAccessDeniedException e) {
    // Could be the coarse ABS-level "repository.create" action check, OR the
    // file-specific ACL_MANAGEMENT gate in DefaultUnifiedRepository — both throw the
    // same class (main doc's updateAcl row: the sole exception to "URADE is always
    // ABS-level" in this whole document). Confirm which one with a follow-up call:
    RepositoryFile f = fileService.getRepoWs().getFile(FileUtils.idToPath(pathId));
    if (f != null && !canManageAcl(unifiedRepository, f.getPath())) {
        // file exists/readable but caller lacks ACL_MANAGEMENT on it — matches the
        // file-specific gate. Note the Owner-ACE gap (main doc §2.4.8): file owners
        // are NOT exempt from this check.
    } else {
        // more likely the coarse ABS-level check failed instead — this is a global
        // permission, not a per-file one, and cannot be confirmed with a follow-up call.
    }
}
```

**`doCreateDirSafe`** (declared `throws InvalidNameException` for its own name
validation; `createFolder()` per intermediate segment can throw either the ABS-level
`URADE`, or a generic `URE` for a per-segment WRITE denial — different exception types):

```java
try {
    fileService.doCreateDirSafe(pathId);
} catch (FileService.InvalidNameException e) {
    // unambiguous, not access-control related.
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.create action at all (createFolder's ABS action,
    // main doc §3) — thrown before any specific segment is even looked up. Per-segment
    // WRITE denial does NOT surface this way (see the generic catch below).
} catch (UnifiedRepositoryException e) {
    // URADE from createFolder() on SOME segment of the path (doCreateDirFor creates
    // missing intermediate folders one at a time, deepest-first is NOT guaranteed —
    // it walks the path top-down). Find which one:
    String[] segments = FileUtils.idToPath(pathId).split("/");
    StringBuilder currentPath = new StringBuilder();
    for (String segment : segments) {
        if (segment.isEmpty()) continue;
        currentPath.append('/').append(segment);
        if (!canWrite(unifiedRepository, currentPath.toString())) {
            // this is (approximately) the first ancestor segment whose PARENT denies
            // WRITE — subject to the same time-of-check race as any other follow-up here
            break;
        }
    }
}
```

**`hasAccess`** — the public `unifiedRepository.hasAccess()` (used directly by
`RepositoryFileProvider`, not via `FileService`, which has no equivalent wrapper): same
as the main doc's `hasAccess` row — `false` for not-found and for no-access are the same
value and not distinguishable from `false` alone.

**`doGetDeletedFiles`** — no exception path at all; inaccessible entries are silently
omitted by Jackrabbit (same as the main doc's `getAllDeletedFiles` row). Nothing to
disambiguate.

**`getRepository()`** (raw `IUnifiedRepository`, used only to construct a
`ZipExportProcessor` for export) — out of scope; whatever it throws is whatever the main
doc/main disambiguation doc already document for the specific `IUnifiedRepository` method
the export processor happens to call internally.
