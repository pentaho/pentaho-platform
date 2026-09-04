---
type: reference
title: Disambiguating doGetTree
description: Public-API-only disambiguation recipe for `FileService`'s doGetTree operation(s).
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating doGetTree

**`doGetTree`** (collapses everything — not-found, no-read, and `URADE` from either
ABS or native JCR denial — to `null`; its internal
`catch (UnifiedRepositoryException e)` catches `URADE` too; disambiguate by
bypassing `FileService` and asking `unifiedRepository` directly about the root path):

```java
RepositoryFileTreeDto tree = fileService.doGetTree(pathId, depth, filter, showHidden, includeAcls);
if (tree == null) {
    try {
        if (!isFoundAndReadable(unifiedRepository, FileUtils.idToPath(pathId))) {
            // ROOT path not found, or not readable (still ambiguous between the two — see
            // main doc). This is the one case doGetTree's null collapses that we CAN recover.
        } else {
            // Root path is found and readable right now: the original null was caused by
            // something else entirely (any other UnifiedRepositoryException doGetTree caught
            // internally, then swallowed) — not further diagnosable via public API; likely a
            // race, or a non-access-control repository failure doGetTree's swallow-to-null
            // behavior hid from us.
        }
    } catch (UnifiedRepositoryAccessDeniedException e) {
        // Follow-up itself failed. Could be repository.read ABS denial or native JCR
        // denial; doGetTree already discarded the original cause.
    }
}
```
