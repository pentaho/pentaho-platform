---
type: reference
title: Disambiguating getTree
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s getTree operation(s).
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating getTree

**`getTree`** (root-path not-found only; unreadable descendants are silently pruned):

```java
try {
    RepositoryFileTree tree = unifiedRepository.getTree(new RepositoryRequest(rootPath, null, -1, null));
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS denial or native JCR denial.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, rootPath)) {
        // the ROOT path itself not found / unreadable
    } else {
        throw e;
    }
}
```
