---
type: reference
title: Disambiguating undeleteFile
description: Public-API-only disambiguation recipe for IUnifiedRepository undeleteFile.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating undeleteFile

```java
try {
    unifiedRepository.undeleteFile(fileId, "comment");
} catch (UnifiedRepositoryFileExistsException e) {
    // Non-access condition: target path is occupied.
} catch (UnifiedRepositoryAccessDeniedException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file != null && !canWrite(unifiedRepository, file.getPath())) {
        // Native JCR move/write denial.
    } else {
        throw e; // ABS denial, target-parent denial, or race.
    }
} catch (UnifiedRepositoryException e) {
    if (unifiedRepository.getFileById(fileId) == null) {
        // Deleted node not found / unreadable.
    } else {
        throw e; // Non-access failure.
    }
}
```
