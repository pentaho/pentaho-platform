---
type: reference
title: Disambiguating doGetMetadata
description: Public-API-only disambiguation recipe for `FileService`'s doGetMetadata operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating doGetMetadata

**`doGetMetadata`** (declared `throws FileNotFoundException` via an explicit not-found
pre-check; everything else propagates as the underlying `getRepoWs().getFileMetadata()`
call throws it — i.e. `UnifiedRepositoryAccessDeniedException`/`UnifiedRepositoryException`
unchanged, same as the main doc's `getFileMetadata` row):

```java
try {
    fileService.doGetMetadata(pathId);
} catch (FileNotFoundException e) {
    // doGetMetadata's own explicit pre-check — unambiguous.
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.read action at all (getFileMetadata's ABS action,
    // main doc [IUnifiedRepository access-control summary table](../../unified-repository/summary-table-per-method.md)) — per-file no-read does NOT surface this way (it's generic URE,
    // handled in the catch below, alongside the not-found race check).
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, FileUtils.idToPath(pathId))) {
        // race: file became unreadable/was deleted after doGetMetadata's own pre-check
    } else {
        throw e; // some other non-access repository failure
    }
}
```
