---
type: reference
title: Disambiguating doDeleteFiles and doDeleteFilesPermanent
description: Public-API-only disambiguation recipe for FileService batch deletion.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating doDeleteFiles / doDeleteFilesPermanent

Both methods pass repository exceptions through unchanged, but do not identify the failing
ID in the batch.

```java
try {
    fileService.doDeleteFilesPermanent(fileIdsCsv);
} catch (UnifiedRepositoryAccessDeniedException e) {
    for (String fileId : fileIdsCsv.split(",")) {
        RepositoryFile file = unifiedRepository.getFileById(fileId);
        if (file != null && !canDelete(unifiedRepository, file.getPath())) {
            // Native JCR DELETE denial for this candidate.
        }
    }
    // Otherwise: repository.create ABS denial, parent/.trash denial, or race.
} catch (UnifiedRepositoryException e) {
    for (String fileId : fileIdsCsv.split(",")) {
        if (!isFoundAndReadable(unifiedRepository, fileId)) {
            // Candidate not found / unreadable.
        }
    }
    // Or a non-access failure such as referential integrity.
}
```
