---
type: reference
title: Disambiguating doGetMetadata
description: Public-API-only disambiguation recipe for FileService doGetMetadata.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating doGetMetadata

```java
try {
    fileService.doGetMetadata(pathId);
} catch (FileNotFoundException e) {
    // Explicit FileService pre-check.
} catch (UnifiedRepositoryAccessDeniedException e) {
    // repository.read ABS denial or native JCR access denial.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, FileUtils.idToPath(pathId))) {
        // File vanished or became unreadable after pre-check.
    } else {
        throw e; // Non-access repository failure.
    }
}
```
