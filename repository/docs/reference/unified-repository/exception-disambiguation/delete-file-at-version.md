---
type: reference
title: Disambiguating deleteFileAtVersion
description: Public-API-only disambiguation recipe for IUnifiedRepository deleteFileAtVersion.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating deleteFileAtVersion

```java
try {
    unifiedRepository.deleteFileAtVersion(fileId, versionId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file != null && !canDelete(unifiedRepository, file.getPath())) {
        // Native JCR delete denial.
    } else {
        throw e; // ABS denial, version-specific denial, or race.
    }
} catch (UnifiedRepositoryException e) {
    if (unifiedRepository.getFileById(fileId) == null) {
        // Not found / unreadable.
    } else {
        throw e; // Non-access failure.
    }
}
```
