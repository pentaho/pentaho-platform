---
type: reference
title: Disambiguating getFileAtVersion
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s getFileAtVersion operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating getFileAtVersion

**`getFileAtVersion`** (read-only, single target):

```java
try {
    RepositoryFile f = unifiedRepository.getFileAtVersion(fileId, versionId);
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
    if (!isFoundAndReadable(unifiedRepository, fileId)) {
        // file itself not found / no jcr:read
    } else {
        // the file is readable now, but this specific VERSION lookup still failed —
        // version history is mutable, so this may be a race, or the version itself
        // may be gone; not further diagnosable via public API (read-only op, so a
        // write-denial explanation doesn't apply)
        throw e;
    }
}
```
