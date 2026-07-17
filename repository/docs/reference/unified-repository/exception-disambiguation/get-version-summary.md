---
type: reference
title: Disambiguating getVersionSummary / getVersionSummaryInBatch / getVersionSummaries
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s getVersionSummary / getVersionSummaryInBatch / getVersionSummaries operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating getVersionSummary / getVersionSummaryInBatch / getVersionSummaries

**`getVersionSummary` / `getVersionSummaryInBatch` / `getVersionSummaries`** (read-only;
hits the "individual version readability" gap — there is no public call to check a
specific version node independently of the file itself):

```java
try {
    VersionSummary vs = unifiedRepository.getVersionSummary(fileId, versionId);
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
    if (!isFoundAndReadable(unifiedRepository, fileId)) {
        // file not found / unreadable
    } else {
        // file itself is readable, but this SPECIFIC VERSION may be unreadable or
        // gone — not diagnosable further via public API
        throw e;
    }
}
```
