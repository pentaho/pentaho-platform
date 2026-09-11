---
type: reference
title: Disambiguating getReferrers
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s getReferrers operation(s).
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating getReferrers

**`getReferrers`** (read-only, single target):

```java
try {
    List<RepositoryFile> referrers = unifiedRepository.getReferrers(fileId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS denial or native JCR denial.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, fileId)) {
        // file node not found / unreadable
    } else {
        throw e;
    }
}
```
