---
type: reference
title: Disambiguating deleteFile
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s deleteFile operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating deleteFile

**`deleteFile`** (single target the caller passes in, but the underlying JCR privilege
denial can come from three different nodes — hits two of the "known gaps" above):

```java
try {
    unifiedRepository.deleteFile(fileId, "comment");
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
        // not found / no jcr:read
    } else if (!canDelete(unifiedRepository, file.getPath())) {
        // no delete access on the FILE itself
    } else {
        // file is deletable per hasAccess(), yet the call still failed. The remaining
        // explanations (main doc §2.4.8) are jcr:removeChildNodes on the PARENT, or
        // jcr:addChildNodes on the .trash folder — neither is checkable via public
        // API (known gaps above); treat as inconclusive.
        throw e;
    }
}
```
