---
type: reference
title: Disambiguating updateFolder
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s updateFolder operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating updateFolder

**`updateFolder`** (same target/shape as `updateFile`, but throws the plain generic
`UnifiedRepositoryException` — no method-specific wrapper, main doc [ExceptionLoggingDecorator layer](../../../architecture/unified-repository/layer-exception-logging-decorator.md)):

```java
try {
    unifiedRepository.updateFolder(folder, "comment");
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
    if (!isFoundAndReadable(unifiedRepository, folder.getId())) {
        // folder concurrently deleted, or became unreadable, between calls
    } else if (!canWrite(unifiedRepository, folder.getPath())) {
        // folder exists/readable, caller cannot write it
    } else {
        throw e;
    }
}
```
