---
type: reference
title: Disambiguating lockFile and unlockFile
description: Public-API-only disambiguation recipe for IUnifiedRepository lock and unlock operations.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating lockFile / unlockFile

```java
try {
    unifiedRepository.lockFile(fileId, "message");
} catch (UnifiedRepositoryAccessDeniedException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file != null && !canWrite(unifiedRepository, file.getPath())) {
        // Native JCR lock denial; WRITE is only the closest public proxy.
    } else {
        throw e; // ABS denial, lock-specific denial, or race.
    }
} catch (UnifiedRepositoryException e) {
    if (unifiedRepository.getFileById(fileId) == null) {
        // Not found / unreadable.
    } else {
        throw e; // Non-access failure.
    }
}
```
