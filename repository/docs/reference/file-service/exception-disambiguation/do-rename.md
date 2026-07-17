---
type: reference
title: Disambiguating doRename
description: Public-API-only disambiguation recipe for `FileService`'s doRename operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating doRename

**`doRename`** (declared `throws Exception`; has no null-check on the looked-up source
file — FileService doc [FileService role and general shape](../../../architecture/file-service/layer-file-service.md) pattern 4 — so a source that vanished/became unreadable between a
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
    // doc [FileService access-control summary table](../summary-table-per-operation.md)) — per-file WRITE denial on the source does NOT surface this way.
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
