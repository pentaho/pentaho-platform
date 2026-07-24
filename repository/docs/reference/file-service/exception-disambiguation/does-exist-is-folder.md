---
type: reference
title: Disambiguating doesExist / isFolder / doGetIsVisible
description: Public-API-only disambiguation recipe for `FileService`'s doesExist / isFolder / doGetIsVisible operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating doesExist / isFolder / doGetIsVisible

**`doesExist` / `isFolder` / `doGetIsVisible`** (and any other `FileService` method that
internally calls `getRepoWs().getFile(...)` and checks for `null`) — none of these three
methods catch anything at all (FileService doc, `getFile`/`getFileById` row): a per-file
not-found/no-read confounds into the same `null`/`false` as at the `IUnifiedRepository`
level, but the **ABS-level `repository.read` check is not swallowed here** — since nothing
inside these three methods catches `URADE`, it propagates straight out as an unchecked
exception, even though none of them *declare* throwing anything:

```java
try {
    RepositoryFileDto file = fileService.getRepoWs().getFile(FileUtils.idToPath(pathId));
    if (file == null) {
        // Not found, no jcr:read, or a custom accessVoterManager voter denied READ.
        // Same as the main disambiguation doc's getFile/getFileById row — unresolvable
        // via public API, whether reached through FileService or IUnifiedRepository directly.
    }
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.read action at all. Unlike getFile()'s "null" case
    // above, this is unambiguous — it fires before any specific file is even looked up.
}
```
