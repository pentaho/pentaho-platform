---
type: reference
title: Disambiguating getVersionSummary / getVersionSummaryInBatch / getVersionSummaries
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s getVersionSummary / getVersionSummaryInBatch / getVersionSummaries operation(s).
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating getVersionSummary / getVersionSummaryInBatch / getVersionSummaries

**`getVersionSummary` / `getVersionSummaryInBatch` / `getVersionSummaries`** (read-only;
hits the "individual version readability" gap — there is no public call to check a
specific version node independently of the file itself):

```java
try {
    VersionSummary vs = unifiedRepository.getVersionSummary(fileId, versionId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS denial or native JCR denial on version data.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, fileId)) {
        // file not found / unreadable
    } else {
        // file itself is readable, but this SPECIFIC VERSION may be unreadable or
        // gone — not diagnosable further via public API
        throw e;
    }
}
```
