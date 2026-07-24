---
type: reference
title: Disambiguating doDeleteFiles / doDeleteFilesPermanent
description: Public-API-only disambiguation recipe for `FileService`'s doDeleteFiles / doDeleteFilesPermanent operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating doDeleteFiles / doDeleteFilesPermanent

**`doDeleteFiles` / `doDeleteFilesPermanent`** (pure pass-through to
`getRepoWs().deleteFile(...)`/`deleteFileWithPermanentFlag(...)` in a loop over a
comma-separated id list; declared `throws Exception`, so the original
`UnifiedRepositoryException`/`URADE` type is preserved — but the failing id within the
batch is not identified):

```java
try {
    fileService.doDeleteFilesPermanent(fileIdsCsv);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.create action at all (deleteFile/permanentFlag's ABS
    // action, main doc [IUnifiedRepository access-control summary table](../../unified-repository/summary-table-per-method.md)) — thrown before any specific id in the batch is even touched.
    // Per-file DELETE denial does NOT surface this way (see the generic catch below).
} catch (Exception e) {
    // check each id individually — the batch call gives no per-id signal:
    for (String fileId : fileIdsCsv.split(",")) {
        if (!isFoundAndReadable(unifiedRepository, fileId)) {
            // this id: not found, or no jcr:read
        } else if (!canDelete(unifiedRepository, unifiedRepository.getFileById(fileId).getPath())) {
            // this id: no jcr:remove(ChildNodes)
        }
        // if neither, this id looks fine right now — the original failure was something
        // else (e.g. permanentlyDeleteFile's referential-integrity conflict, or a
        // parent/.trash privilege gap — main doc [per-node JCR privilege requirements and Magic ACE caveats](../../../architecture/unified-repository/layer-jcr-repository-file-dao.md#per-node-jcr-privilege-requirements-and-magic-ace-caveats), known gaps above); not further
        // diagnosable, and possibly a race if this wasn't actually the failing id
    }
}
```
