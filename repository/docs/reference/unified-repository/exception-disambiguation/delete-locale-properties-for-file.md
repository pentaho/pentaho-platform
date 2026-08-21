---
type: reference
title: Disambiguating deleteLocalePropertiesForFile
description: Public-API-only disambiguation recipe for IUnifiedRepository locale-property deletion.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating deleteLocalePropertiesForFile

```java
try {
    unifiedRepository.deleteLocalePropertiesForFile(repositoryFile, locale);
} catch (UnifiedRepositoryAccessDeniedException e) {
    RepositoryFile file = unifiedRepository.getFileById(repositoryFile.getId());
    if (file != null && !canWrite(unifiedRepository, file.getPath())) {
        // Native JCR locale-property delete denial.
    } else {
        throw e; // ABS denial or race.
    }
} catch (UnifiedRepositoryException e) {
    if (unifiedRepository.getFileById(repositoryFile.getId()) == null) {
        // Not found / unreadable.
    } else {
        throw e; // Non-access failure.
    }
}
```
