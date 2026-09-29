---
type: reference
title: Disambiguating setFileMetadata
description: Public-API-only disambiguation recipe for IUnifiedRepository setFileMetadata.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating setFileMetadata

```java
try {
    unifiedRepository.setFileMetadata(fileId, metadataMap);
} catch (UnifiedRepositoryMalformedNameException e) {
    // Non-access condition: malformed metadata key.
} catch (UnifiedRepositoryAccessDeniedException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file != null && !canWrite(unifiedRepository, file.getPath())) {
        // Native JCR metadata-write denial.
    } else {
        throw e; // ABS denial or race.
    }
} catch (UnifiedRepositoryException e) {
    if (unifiedRepository.getFileById(fileId) == null) {
        // Not found / unreadable.
    } else {
        throw e; // Non-access failure.
    }
}
```
