---
type: reference
title: Disambiguating doCopyFiles
description: Public-API-only disambiguation recipe for FileService doCopyFiles.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating doCopyFiles

`doCopyFiles` first performs its own `RepositoryCreateAction` check and throws
`IllegalArgumentException` when that check fails. It then constructs and executes
`CopyFilesOperation`.

Missing or unreadable source IDs are silently skipped: `execute()` logs and continues when
`getFileById()` returns `null`.

```java
try {
    fileService.doCopyFiles(destPathId, mode, sourceFileIdsCsv);
} catch (IllegalArgumentException e) {
    if (!canCreateAnything(fileService)) {
        // doCopyFiles's own create-ABS check.
    } else if (!fileServiceExists(fileService, destPathId)
            || !fileService.isFolder(destPathId)) {
        // Constructor validation: destination missing/unreadable or not a folder.
    } else {
        // Deep-folder-copy validation can also throw this after a custom voter returned
        // null from createFolder; otherwise race or invalid internal argument.
        throw e;
    }
} catch (UnifiedRepositoryAccessDeniedException e) {
    if (!canCreateAnything(fileService)) {
        // Underlying create/update/metadata repository.create ABS denial after initial check.
    } else if (!canWrite(unifiedRepository, FileUtils.idToPath(destPathId))) {
        // Native JCR denial on destination folder.
    } else {
        // Other proven sources:
        // - underlying repository.read ABS denial;
        // - source ACL/metadata read denial;
        // - metadata write denial;
        // - MODE_OVERWRITE updateAcl ACL_MANAGEMENT denial;
        // - MODE_RENAME explicit null-create denial from a custom voter.
        throw e;
    }
} catch (UnifiedRepositoryException e) {
    // Non-access repository failure. Source missing/no-read is not here; it was skipped.
    throw e;
}
```

## Mode-specific null-return defects

When a custom access voter makes `createFile`/`createFolder`/`updateFile` return `null`:

- `MODE_RENAME` explicitly throws `URADE` for a null created file, but a null created
  folder reaches deep-copy validation first and can throw `IllegalArgumentException`.
- `MODE_NO_OVERWRITE` and `MODE_OVERWRITE` dereference the null result and can throw
  `NullPointerException`.

Native Jackrabbit denial does not use these null paths; it throws JCR
`AccessDeniedException`, which becomes `URADE`.
