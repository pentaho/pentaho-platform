---
type: reference
title: Disambiguating permanentlyDeleteFile
description: Public-API-only disambiguation recipe for IUnifiedRepository permanentlyDeleteFile.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating permanentlyDeleteFile

```java
try {
    unifiedRepository.permanentlyDeleteFile(fileId, "comment");
} catch (UnifiedRepositoryReferentialIntegrityException e) {
    // Non-access condition: live references protect the target.
} catch (UnifiedRepositoryAccessDeniedException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file != null && !canDelete(unifiedRepository, file.getPath())) {
        // Native JCR delete denial.
    } else {
        throw e; // ABS denial, source-parent denial, or race.
    }
} catch (UnifiedRepositoryException e) {
    if (unifiedRepository.getFileById(fileId) == null) {
        // Not found / unreadable.
    } else {
        throw e; // Non-access failure.
    }
}
```
