---
type: reference
title: Disambiguating moveFile
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s moveFile operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating moveFile

**`moveFile`** (incl. rename) — dual-target; unlike the cause-chain approach in the main
doc, distinguishing source vs. destination here requires two explicit follow-up calls
rather than falling out "for free" from the exception shape:


```java
try {
    unifiedRepository.moveFile(fileId, destAbsPath, "comment");
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
            // DESTINATION'S PARENT folder not found / unreadable. (The destination
            // path itself not existing is the normal rename/move case and is NOT an
            // error — only a missing/unreadable parent is.)
        } else if (!canWrite(unifiedRepository, source.getPath())) {
            // SOURCE not writable
        } else if (!canWrite(unifiedRepository, destParentPath)) {
            // DESTINATION folder not writable
        } else {
            // both source and destination look readable/writable per these checks —
            // remaining explanations are the source PARENT's jcr:removeChildNodes
            // (unchecked at the Pentaho layer, main doc [per-node JCR privilege requirements and Magic ACE caveats](../../../architecture/unified-repository/layer-jcr-repository-file-dao.md#per-node-jcr-privilege-requirements-and-magic-ace-caveats)) or a race; not
            // further diagnosable via public API.
            throw e;
        }
    }
}
```
