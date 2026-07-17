---
type: reference
title: Disambiguating doCreateDirSafe
description: Public-API-only disambiguation recipe for `FileService`'s doCreateDirSafe operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating doCreateDirSafe

**`doCreateDirSafe`** (declared `throws InvalidNameException` for its own name
validation; `createFolder()` per intermediate segment can throw either the ABS-level
`URADE`, or a generic `URE` for a per-segment WRITE denial — different exception types):

```java
try {
    fileService.doCreateDirSafe(pathId);
} catch (FileService.InvalidNameException e) {
    // unambiguous, not access-control related.
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.create action at all (createFolder's ABS action,
    // main doc §3) — thrown before any specific segment is even looked up. Per-segment
    // WRITE denial does NOT surface this way (see the generic catch below).
} catch (UnifiedRepositoryException e) {
    // URADE from createFolder() on SOME segment of the path (doCreateDirFor creates
    // missing intermediate folders one at a time, deepest-first is NOT guaranteed —
    // it walks the path top-down). Find which one:
    String[] segments = FileUtils.idToPath(pathId).split("/");
    StringBuilder currentPath = new StringBuilder();
    for (String segment : segments) {
        if (segment.isEmpty()) continue;
        currentPath.append('/').append(segment);
        if (!canWrite(unifiedRepository, currentPath.toString())) {
            // this is (approximately) the first ancestor segment whose PARENT denies
            // WRITE — subject to the same time-of-check race as any other follow-up here
            break;
        }
    }
}
```
