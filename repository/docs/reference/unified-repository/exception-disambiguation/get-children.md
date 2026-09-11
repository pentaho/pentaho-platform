---
type: reference
title: Disambiguating getChildren
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s getChildren operation(s).
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating getChildren

**`getChildren`** (folder-level not-found only; individual unreadable children are
silently omitted from the list, never reported as an exception):

```java
try {
    List<RepositoryFile> kids = unifiedRepository.getChildren(folderId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS denial or native JCR denial.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, folderId)) {
        // the FOLDER itself not found / unreadable
    } else {
        throw e; // folder is readable now — likely a race; not further diagnosable
    }
}
```
