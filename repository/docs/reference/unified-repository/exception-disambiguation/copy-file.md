---
type: reference
title: Disambiguating copyFile
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s copyFile operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating copyFile

**`copyFile`** — same not-found/parent-folder shape as `moveFile`, but there is no
source-write check to make, because the source is never write/read-checked by this
operation at all (main doc [IUnifiedRepository access-control summary table](../summary-table-per-method.md) `copyFile` row and the [Permission Model Known Issues](../permissions/known-issues.md)):

```java
try {
    unifiedRepository.copyFile(fileId, destAbsPath, "comment");
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
    RepositoryFile source = unifiedRepository.getFileById(fileId);
    if (source == null) {
        // SOURCE not found / no jcr:read
    } else {
        String destParentPath = destAbsPath.substring(0, destAbsPath.lastIndexOf('/'));
        boolean destParentExists = isFoundAndReadable(unifiedRepository, destParentPath);
        if (!destParentExists) {
            // DESTINATION'S PARENT folder not found / unreadable
        } else if (!canWrite(unifiedRepository, destParentPath)) {
            // DESTINATION folder not writable (there is nothing to check on the
            // source side — copyFile never checks source write access)
        } else {
            throw e; // race, or an otherwise-unaccounted-for failure
        }
    }
}
```
