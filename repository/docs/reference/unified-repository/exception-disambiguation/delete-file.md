---
type: reference
title: Disambiguating deleteFile
description: Public-API-only disambiguation recipe for IUnifiedRepository deleteFile.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating deleteFile

```java
try {
    unifiedRepository.deleteFile(fileId, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file != null && !canDelete(unifiedRepository, file.getPath())) {
        // Native JCR DELETE denial on file.
    } else {
        // ABS denial, source-parent/.trash denial, or race.
        throw e;
    }
} catch (UnifiedRepositoryException e) {
    if (unifiedRepository.getFileById(fileId) == null) {
        // Not found / no jcr:read.
    } else {
        throw e; // Non-access failure.
    }
}
```

`hasAccess(DELETE)` cannot diagnose `jcr:removeChildNodes` on the source parent or
`jcr:addChildNodes` on `.trash`; both still surface as `URADE`.
