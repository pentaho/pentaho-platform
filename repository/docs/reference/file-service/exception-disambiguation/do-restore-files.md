---
type: reference
title: Disambiguating doRestoreFiles
description: Public-API-only disambiguation recipe for FileService doRestoreFiles.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating doRestoreFiles

`doRestoreFiles` rethrows `URADE` unchanged. Every other exception becomes a cause-less
`InternalError`.

```java
try {
    fileService.doRestoreFiles(fileIdsCsv);
} catch (UnifiedRepositoryAccessDeniedException e) {
    for (String fileId : fileIdsCsv.split(",")) {
        RepositoryFile file = unifiedRepository.getFileById(fileId);
        if (file != null && !canWrite(unifiedRepository, file.getPath())) {
            // Native JCR restore/move denial for this candidate.
        }
    }
    // Otherwise: repository.create ABS denial, target-parent denial, or race.
} catch (InternalError e) {
    // Not-found/unreadable, destination conflict, or another non-access failure.
    // doRestoreFiles discarded original type and cause.
}
```
