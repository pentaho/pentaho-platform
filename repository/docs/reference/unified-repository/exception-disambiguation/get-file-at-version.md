---
type: reference
title: Disambiguating getFileAtVersion
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s getFileAtVersion operation(s).
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating getFileAtVersion

**`getFileAtVersion`** (read-only, single target):

```java
try {
    RepositoryFile f = unifiedRepository.getFileAtVersion(fileId, versionId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS denial or native JCR denial. No public version-specific permission check exists.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, fileId)) {
        // file itself not found / no jcr:read
    } else {
        // the file is readable now, but this specific VERSION lookup still failed —
        // version history is mutable, so this may be a race, or the version itself
        // may be gone; not further diagnosable via public API (read-only op, so a
        // write-denial explanation doesn't apply)
        throw e;
    }
}
```
