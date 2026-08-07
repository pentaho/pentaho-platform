---
type: reference
title: Disambiguating doRename
description: Public-API-only disambiguation recipe for FileService doRename.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating doRename

`doRename` does not null-check its source lookup. Pre-check source existence to avoid an
unhelpful `NullPointerException`.

```java
String path = FileUtils.idToPath(pathId);
if (fileService.getRepoWs().getFile(path) == null) {
    return; // Source not found / unreadable.
}
try {
    fileService.doRename(pathId, newName);
} catch (IllegalArgumentException e) {
    // Renamed-to path already exists.
} catch (UnifiedRepositoryAccessDeniedException e) {
    if (!canWrite(unifiedRepository, path)) {
        // Native JCR denial on source.
    } else {
        throw e; // ABS denial, destination/source-parent denial, or race.
    }
} catch (UnifiedRepositoryException e) {
    if (fileService.getRepoWs().getFile(path) == null) {
        // Source vanished or became unreadable.
    } else {
        throw e; // Non-access failure.
    }
}
```
