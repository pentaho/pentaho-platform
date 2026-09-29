---
type: reference
title: Disambiguating getFileMetadata
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s getFileMetadata operation(s).
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating getFileMetadata

**`getFileMetadata`** (read-only single target — only the not-found branch ever applies):

```java
try {
    Map<String, Serializable> metadata = unifiedRepository.getFileMetadata(fileId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS denial or native JCR denial.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, fileId)) {
        // not found / unreadable
    } else {
        throw e;
    }
}
```
