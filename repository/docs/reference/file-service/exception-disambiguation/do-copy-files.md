---
type: reference
title: Disambiguating doCopyFiles
description: Public-API-only disambiguation recipe for `FileService`'s doCopyFiles operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating doCopyFiles

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
