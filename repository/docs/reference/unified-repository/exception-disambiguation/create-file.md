---
type: reference
title: Disambiguating createFile
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s createFile operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating createFile

**`createFile`** (only node involved is the **parent**; note the exception *type* itself
— see "A note on exception types" above): 

```java
try {
    unifiedRepository.createFile(parentFolderId, file, data, "comment");
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
    // createFile's not-found/no-write conditions actually surface as
    // UnifiedRepositoryCreateFileException, not the plain generic class — catching the
    // supertype here still works, but catch the specific type first if you want that
    // distinction visible in your own code.
    RepositoryFile parent = unifiedRepository.getFileById(parentFolderId);
    if (parent == null) {
        // PARENT folder not found / unreadable
    } else if (!canWrite(unifiedRepository, parent.getPath())) {
        // parent exists/readable, but caller cannot write into it (proxy for
        // the actual jcr:addChildNodes privilege — see main doc §2.4.8)
    } else {
        throw e; // race, or a non-access failure
    }
}
```
