---
type: reference
title: Disambiguating createFile
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s createFile operation(s).
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating createFile

**`createFile`** (only node involved is the **parent**; note the exception *type* itself
— see "A note on exception types" above): 

```java
try {
    RepositoryFile created =
        unifiedRepository.createFile(parentFolderId, file, data, "comment");
    if (created == null) {
        // A registered accessVoterManager voter denied WRITE on the parent.
        // Default configuration has no voters, so this path is normally unreachable.
    }
} catch (UnifiedRepositoryAccessDeniedException e) {
    RepositoryFile parent = unifiedRepository.getFileById(parentFolderId);
    if (parent != null && !canWrite(unifiedRepository, parent.getPath())) {
        // Native JCR denial: parent lacks the privilege needed to add a child.
    } else {
        throw e; // ABS denial, race, or a JCR privilege not represented by WRITE.
    }
} catch (UnifiedRepositoryException e) {
    RepositoryFile parent = unifiedRepository.getFileById(parentFolderId);
    if (parent == null) {
        // Parent not found / unreadable. Usually UnifiedRepositoryCreateFileException.
    } else {
        throw e; // Non-access failure.
    }
}
```
