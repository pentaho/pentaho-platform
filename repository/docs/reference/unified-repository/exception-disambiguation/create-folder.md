---
type: reference
title: Disambiguating createFolder
description: Public-API-only disambiguation recipe for IUnifiedRepository createFolder.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating createFolder

```java
try {
    RepositoryFile created =
        unifiedRepository.createFolder(parentFolderId, folder, "comment");
    if (created == null) {
        // A registered access voter denied WRITE on the parent.
    }
} catch (UnifiedRepositoryAccessDeniedException e) {
    RepositoryFile parent = unifiedRepository.getFileById(parentFolderId);
    if (parent != null && !canWrite(unifiedRepository, parent.getPath())) {
        // Native JCR denial on parent folder.
    } else {
        throw e; // ABS denial, race, or an unrepresented JCR privilege.
    }
} catch (UnifiedRepositoryException e) {
    if (unifiedRepository.getFileById(parentFolderId) == null) {
        // Parent folder not found / unreadable.
    } else {
        throw e; // Non-access failure.
    }
}
```

Unlike `createFile`, `createFolder` has no method-specific fallback wrapper.
