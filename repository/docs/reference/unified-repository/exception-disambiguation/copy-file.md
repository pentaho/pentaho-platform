---
type: reference
title: Disambiguating copyFile
description: Public-API-only disambiguation recipe for IUnifiedRepository copyFile.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating copyFile

```java
try {
    unifiedRepository.copyFile(fileId, destAbsPath, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    String destParentPath = destAbsPath.substring(0, destAbsPath.lastIndexOf('/'));
    if (!canWrite(unifiedRepository, destParentPath)) {
        // Native JCR denial on destination parent.
    } else {
        throw e; // ABS denial, another native copy privilege, or race.
    }
} catch (UnifiedRepositoryException e) {
    String destParentPath = destAbsPath.substring(0, destAbsPath.lastIndexOf('/'));
    if (unifiedRepository.getFileById(fileId) == null) {
        // Source not found / unreadable.
    } else if (!isFoundAndReadable(unifiedRepository, destParentPath)) {
        // Destination parent not found / unreadable.
    } else {
        throw e; // Non-access failure.
    }
}
```

`copyFile` checks WRITE on destination, not source.
