---
type: reference
title: Disambiguating deleteFileAtVersion
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s deleteFileAtVersion operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating deleteFileAtVersion

**`deleteFileAtVersion`** (same shape as `deleteFile`):

```java
try {
    unifiedRepository.deleteFileAtVersion(fileId, versionId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file == null) {
        // not found / unreadable
    } else if (!canDelete(unifiedRepository, file.getPath())) {
        // no delete access on the file itself
    } else {
        throw e;
    }
}
```
