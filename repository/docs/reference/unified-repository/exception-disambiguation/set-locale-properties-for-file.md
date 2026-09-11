---
type: reference
title: Disambiguating setLocalePropertiesForFile
description: Public-API-only disambiguation recipe for IUnifiedRepository locale-property writes.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating setLocalePropertiesForFile*

```java
RepositoryFile file = unifiedRepository.getFileById(fileId);
if (file == null) {
    return; // Not found / no read.
}
try {
    unifiedRepository.setLocalePropertiesForFile(file, locale, props);
} catch (UnifiedRepositoryAccessDeniedException e) {
    if (!canWrite(unifiedRepository, file.getPath())) {
        // Native JCR locale-property write denial.
    } else {
        throw e; // ABS denial or race.
    }
} catch (UnifiedRepositoryException e) {
    throw e; // Non-access failure.
}
```
