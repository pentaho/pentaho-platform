---
type: reference
title: Disambiguating setFileMetadata
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s setFileMetadata operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating setFileMetadata

**`setFileMetadata`** (single target; **plus** an entirely distinct, non-access,
non-ambiguous failure mode for malformed metadata *keys*, caught separately and first):

```java
try {
    unifiedRepository.setFileMetadata(fileId, metadataMap);
} catch (UnifiedRepositoryMalformedNameException e) {
    // Unambiguous, non-access condition: one of the metadataMap KEYS (not the file/folder
    // name — despite what the exception name suggests) contains reserved characters
    // (main doc §2.1). No follow-up call needed; do NOT run this through
    // isFoundAndReadable()/canWrite() — it has nothing to do with access control.
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
    RepositoryFile f = unifiedRepository.getFileById(fileId);
    if (f == null) {
        // not found / unreadable
    } else if (!canWrite(unifiedRepository, f.getPath())) {
        // no write access
    } else {
        throw e;
    }
}
```
