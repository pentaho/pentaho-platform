---
type: reference
title: Disambiguating restoreFileAtVersion
description: Public-API-only disambiguation recipe for IUnifiedRepository restoreFileAtVersion.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating restoreFileAtVersion

```java
try {
    unifiedRepository.restoreFileAtVersion(fileId, versionId, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file != null && !canWrite(unifiedRepository, file.getPath())) {
        // Native JCR restore denial; WRITE is only the closest public proxy.
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
