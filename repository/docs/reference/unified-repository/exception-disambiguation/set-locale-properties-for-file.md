---
type: reference
title: Disambiguating setLocalePropertiesForFile*
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s setLocalePropertiesForFile* operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating setLocalePropertiesForFile*

**`setLocalePropertiesForFile*`** (not-found is confounded with `null` via
`getFileById`, so only the write-denial branch reaches the `catch` block):

```java
RepositoryFile f = unifiedRepository.getFileById(fileId); // null: not found / no read
try {
    unifiedRepository.setLocalePropertiesForFile(f, locale, props);
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
    if (!canWrite(unifiedRepository, f.getPath())) {
        // no write access
    } else {
        throw e;
    }
}
```
