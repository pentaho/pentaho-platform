---
type: reference
title: Disambiguating permanentlyDeleteFile
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s permanentlyDeleteFile operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating permanentlyDeleteFile

**`permanentlyDeleteFile`** — same not-found/no-delete shape as `deleteFile`, **plus** a
third, entirely distinct, non-access, non-ambiguous failure mode that must be caught
separately (and first) rather than run through the disambiguation logic at all:

```java
try {
    unifiedRepository.permanentlyDeleteFile(fileId, "comment");
} catch (UnifiedRepositoryReferentialIntegrityException e) {
    // Unambiguous, non-access condition: other JCR nodes still hold references to this
    // file (main doc §2.1/§2.4.8) — thrown as an explicit pre-check before any JCR
    // remove call is attempted. No follow-up call needed; the exception type alone
    // already tells you exactly what happened. Do NOT run this through
    // isFoundAndReadable()/canDelete() — the file is not being denied, it is being
    // protected from an integrity violation.
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
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file == null) {
        // not found / unreadable
    } else if (!canDelete(unifiedRepository, file.getPath())) {
        // no delete access on the file itself
    } else {
        // PARENT-level jcr:removeChildNodes gap (§2.4.8), unchecked — inconclusive
        throw e;
    }
}
```
