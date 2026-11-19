---
type: reference
title: Disambiguating doCreateDirSafe
description: Public-API-only disambiguation recipe for FileService doCreateDirSafe.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating doCreateDirSafe

`doCreateDirFor` walks path segments top-down. For each missing segment it calls
`createFolder(parentId, ...)`.

```java
try {
    fileService.doCreateDirSafe(pathId);
} catch (FileService.InvalidNameException e) {
    // Invalid path/name; not access control.
} catch (UnifiedRepositoryAccessDeniedException e) {
    if (!canCreateAnything(fileService)) {
        // Missing global repository.create ABS action.
    } else {
        String path = FileUtils.idToPath(pathId);
        String parentPath = "/";
        for (String segment : path.split("/")) {
            if (segment.isEmpty()) {
                continue;
            }
            String candidate = "/".equals(parentPath)
                ? parentPath + segment
                : parentPath + "/" + segment;
            if (unifiedRepository.getFile(candidate) == null) {
                if (!canWrite(unifiedRepository, parentPath)) {
                    // Native JCR denial: this first missing segment cannot be added to parent.
                }
                break;
            }
            parentPath = candidate;
        }
        // If no resource check reproduces denial: repository.read ABS denial,
        // state race, or an unrepresented privilege.
    }
} catch (UnifiedRepositoryException e) {
    // Non-access repository failure: map to the caller's generic/unknown failure.
    throw new OperationFailedException(e);
}
```

`UnifiedRepositoryAccessDeniedException` extends `UnifiedRepositoryException`, so the
specific catch above handles both ABS denial and native JCR access denial. A remaining
`UnifiedRepositoryException` can still occur, but it does not identify an access failure.
At the `RepositoryFileProvider` boundary it maps directly to `OperationFailedException`.
`createFolderCore()` performs its nearest-writable-ancestor check only for
`UnifiedRepositoryAccessDeniedException`.

If a custom `accessVoterManager` voter denies `createFolder`, that repository call returns
`null` instead of throwing. `doCreateDirFor` does not check the result: denial on the final
segment can be reported as success, while denial before another segment can cause a later
`NullPointerException`.
