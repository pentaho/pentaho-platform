---
type: reference
title: Disambiguating doRestoreFiles
description: Public-API-only disambiguation recipe for `FileService`'s doRestoreFiles operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating doRestoreFiles

**`doRestoreFiles`** (declared `throws InternalError`; `UnifiedRepositoryAccessDeniedException`
is explicitly re-thrown unchanged, everything else — including a genuine not-found —
becomes a bare `InternalError` with no message/cause):

```java
try {
    fileService.doRestoreFiles(fileIdsCsv);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.create action at all (undeleteFile's ABS action,
    // main doc [IUnifiedRepository access-control summary table](../../unified-repository/summary-table-per-method.md)) — per-file WRITE denial does NOT surface this way (see below).
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
