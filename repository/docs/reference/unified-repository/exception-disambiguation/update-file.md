---
type: reference
title: Disambiguating updateFile
description: Public-API-only disambiguation recipe for IUnifiedRepository updateFile.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating updateFile

```java
try {
    RepositoryFile updated = unifiedRepository.updateFile(file, data, "comment");
    if (updated == null) {
        // A registered access voter denied WRITE.
    }
} catch (UnifiedRepositoryAccessDeniedException e) {
    if (!canWrite(unifiedRepository, file.getPath())) {
        // Native JCR write denial on file.
    } else {
        throw e; // ABS denial or race.
    }
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, file.getId())) {
        // File concurrently deleted or became unreadable.
        // Usually UnifiedRepositoryUpdateFileException.
    } else {
        throw e; // Non-access failure.
    }
}
```
