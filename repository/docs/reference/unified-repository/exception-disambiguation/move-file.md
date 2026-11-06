---
type: reference
title: Disambiguating moveFile
description: Public-API-only disambiguation recipe for IUnifiedRepository moveFile.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating moveFile

```java
try {
    unifiedRepository.moveFile(fileId, destAbsPath, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    RepositoryFile source = unifiedRepository.getFileById(fileId);
    String sourceParentPath = source == null
        ? null
        : source.getPath().substring(0, source.getPath().lastIndexOf('/'));
    String destParentPath = destAbsPath.substring(0, destAbsPath.lastIndexOf('/'));
    if (source != null && !canDelete(unifiedRepository, source.getPath())) {
        // Native JCR jcr:removeNode denial on source.
    } else if (sourceParentPath != null && !canWrite(unifiedRepository, sourceParentPath)) {
        // Native JCR jcr:removeChildNodes denial on source parent.
    } else if (!canWrite(unifiedRepository, destParentPath)) {
        // Native JCR jcr:addChildNodes denial on destination parent.
    } else {
        // ABS denial, custom source-WRITE voter denial, or race.
        throw e;
    }
} catch (UnifiedRepositoryException e) {
    RepositoryFile source = unifiedRepository.getFileById(fileId);
    String destParentPath = destAbsPath.substring(0, destAbsPath.lastIndexOf('/'));
    if (source == null) {
        // Source not found / unreadable.
    } else if (!isFoundAndReadable(unifiedRepository, destParentPath)) {
        // Destination parent not found / unreadable.
    } else {
        throw e; // Non-access failure.
    }
}
```

`JcrRepositoryFileDao` also asks `accessVoterManager` for `WRITE` on the source
file. That check is a no-op in the default configuration and cannot be reproduced
reliably through `IUnifiedRepository.hasAccess()`. Native Jackrabbit move
authorization instead requires removal access on the source, removal of a child
from the source parent, and addition of a child to the destination parent.
