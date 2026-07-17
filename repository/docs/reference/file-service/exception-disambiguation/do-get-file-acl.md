---
type: reference
title: Disambiguating doGetFileAcl
description: Public-API-only disambiguation recipe for `FileService`'s doGetFileAcl operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating doGetFileAcl

**`doGetFileAcl`** (no declared exception and no null-check at all — a not-found/no-read
condition on the target file surfaces as an uncaught `NullPointerException`; both the ABS
`repository.read` check and a genuine per-file `jcr:readAccessControl` denial from
`getRepoWs().getAcl()` propagate unchecked, but as **different** exception types):

```java
// Pre-check yourself; doGetFileAcl() gives no better signal for not-found:
if (fileService.getRepoWs().getFile(FileUtils.idToPath(pathId)) == null) {
    // not found / not readable — check BEFORE calling doGetFileAcl()
}

try {
    fileService.doGetFileAcl(pathId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: no repository.read action at all (getAcl's ABS action, main doc
    // [IUnifiedRepository access-control summary table](../../unified-repository/summary-table-per-method.md)) — thrown before the file's own ACL is even looked up.
} catch (UnifiedRepositoryException e) {
    // File found/readable, but the ACL read itself still failed — hits the
    // jcr:readAccessControl known gap from the main disambiguation doc: no public call
    // confirms this specifically beyond "file is found/readable but ACL read still failed".
}
```
