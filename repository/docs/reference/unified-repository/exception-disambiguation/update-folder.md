---
type: reference
title: Disambiguating updateFolder
description: Public-API-only disambiguation recipe for IUnifiedRepository updateFolder.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating updateFolder

```java
try {
    unifiedRepository.updateFolder(folder, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    if (!canWrite(unifiedRepository, folder.getPath())) {
        // Native JCR write denial on folder.
    } else {
        throw e; // ABS denial or race.
    }
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, folder.getId())) {
        // Folder concurrently deleted or became unreadable.
    } else {
        throw e; // Non-access failure.
    }
}
```
