---
type: reference
title: Disambiguating restoreFileAtVersion
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s restoreFileAtVersion operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating restoreFileAtVersion

**`restoreFileAtVersion`** (single target; hits the "no dedicated restore permission" gap):

```java
try {
    unifiedRepository.restoreFileAtVersion(fileId, versionId, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc [Method Interceptor layer](../../../architecture/unified-repository/layer-method-interceptor.md)/[IUnifiedRepository access-control summary table](../summary-table-per-method.md), this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile f = unifiedRepository.getFileById(fileId);
    if (f == null) {
        // not found / unreadable
    } else if (!canWrite(unifiedRepository, f.getPath())) {
        // no write access — again only a proxy; the version manager's restore()
        // privilege isn't separately exposed via RepositoryFilePermission
    } else {
        throw e;
    }
}
```
