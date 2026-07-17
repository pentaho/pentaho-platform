---
type: reference
title: Disambiguating updateFile
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s updateFile operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating updateFile

**`updateFile`** (single target — the file itself; also gets a method-specific exception
type, `UnifiedRepositoryUpdateFileException`, instead of the plain generic class):

```java
try {
    unifiedRepository.updateFile(file, data, "comment");
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
    // Actually thrown as UnifiedRepositoryUpdateFileException for this method (main doc
    // §2.1); catching the supertype here still works.
    if (!isFoundAndReadable(unifiedRepository, file.getId())) {
        // file concurrently deleted, or became unreadable, between calls
    } else if (!canWrite(unifiedRepository, file.getPath())) {
        // file exists/readable, caller cannot write it
    } else {
        throw e;
    }
}
```
