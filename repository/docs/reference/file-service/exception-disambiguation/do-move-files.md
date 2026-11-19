---
type: reference
title: Disambiguating doMoveFiles
description: Public-API-only disambiguation recipe for FileService doMoveFiles.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating doMoveFiles

`doMoveFiles` explicitly rethrows `IllegalArgumentException` and `URADE`; every other
exception becomes a cause-less `InternalError`.

```java
try {
    fileService.doMoveFiles(destPathId, sourceFileIdsCsv);
} catch (FileNotFoundException e) {
    // Destination folder failed FileService's initial lookup.
} catch (UnifiedRepositoryAccessDeniedException e) {
    String destination = FileUtils.idToPath(destPathId);
    if (!canWrite(unifiedRepository, destination)) {
        // Native JCR denial on destination.
    }
    for (String fileId : sourceFileIdsCsv.split(",")) {
        RepositoryFile source = unifiedRepository.getFileById(fileId);
        if (source != null && !canWrite(unifiedRepository, source.getPath())) {
            // Native JCR denial on this source.
        }
    }
    // Otherwise: repository.create ABS denial, source-parent denial, or race.
} catch (IllegalArgumentException e) {
    // An IllegalArgumentException thrown outside ExceptionLoggingDecorator.
} catch (InternalError e) {
    // Source not found/unreadable, destination-parent validation failure, or another
    // non-access repository failure. Original type and cause were discarded.
}
```
