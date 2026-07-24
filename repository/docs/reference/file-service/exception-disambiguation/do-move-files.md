---
type: reference
title: Disambiguating doMoveFiles
description: Public-API-only disambiguation recipe for `FileService`'s doMoveFiles operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating doMoveFiles

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
    // ExceptionLoggingDecorator (main doc [ExceptionLoggingDecorator layer](../../../architecture/unified-repository/layer-exception-logging-decorator.md)) wraps ANY IllegalArgumentException thrown
    // inside moveFile() (e.g. the destination's PARENT also missing) into a generic URE
    // BEFORE it ever reaches this catch — it lands in the InternalError branch below
    // instead. If this branch fires at all, it was thrown by something outside that
    // wrapped call (not identified in FileService's own code for this method).
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.create action at all (moveFile's ABS action, main doc
    // [IUnifiedRepository access-control summary table](../../unified-repository/summary-table-per-method.md)) — thrown before any specific id is touched. Per-file SOURCE/DESTINATION
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
