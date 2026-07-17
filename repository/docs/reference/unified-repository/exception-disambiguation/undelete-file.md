---
type: reference
title: Disambiguating undeleteFile
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s undeleteFile operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating undeleteFile

**`undeleteFile`** (single target — the deleted node — **plus** an entirely distinct,
non-access, non-ambiguous failure mode caught separately and first, same pattern as
`permanentlyDeleteFile` above):

```java
try {
    unifiedRepository.undeleteFile(fileId, "comment");
} catch (UnifiedRepositoryFileExistsException e) {
    // Unambiguous, non-access condition: a file/folder now exists at the deleted item's
    // original path (main doc §2.1) — thrown before the session.move() that would
    // restore it there. No follow-up call needed; do NOT run this through
    // isFoundAndReadable()/canWrite() — it has nothing to do with access control.
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file == null) {
        // deleted node not found / unreadable
    } else if (!canWrite(unifiedRepository, file.getPath())) {
        // no write access (WRITE is the closest public proxy for the session.move()
        // privilege actually checked)
    } else {
        throw e;
    }
}
```
