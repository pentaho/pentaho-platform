---
type: reference
title: Disambiguating createFolder
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s createFolder operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating createFolder

**`createFolder`** (same target/shape as `createFile`, but throws the plain generic
`UnifiedRepositoryException` — it does **not** get the `UnifiedRepositoryCreateFileException`
treatment, main doc §2.1):

```java
try {
    unifiedRepository.createFolder(parentFolderId, folder, "comment");
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
    RepositoryFile parent = unifiedRepository.getFileById(parentFolderId);
    if (parent == null) {
        // PARENT folder not found / unreadable
    } else if (!canWrite(unifiedRepository, parent.getPath())) {
        // parent exists/readable, but caller cannot write into it
    } else {
        throw e; // race, or a non-access failure
    }
}
```
