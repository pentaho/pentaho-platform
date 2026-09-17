---
type: reference
title: Disambiguating doGetFileAcl
description: Public-API-only disambiguation recipe for FileService doGetFileAcl.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating doGetFileAcl

`doGetFileAcl` does not null-check its target lookup.

```java
String path = FileUtils.idToPath(pathId);
if (fileService.getRepoWs().getFile(path) == null) {
    return; // Not found / unreadable; avoids doGetFileAcl's NullPointerException.
}
try {
    fileService.doGetFileAcl(pathId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // repository.read ABS denial OR native jcr:readAccessControl denial.
    // No RepositoryFilePermission represents readAccessControl.
    throw e;
} catch (UnifiedRepositoryException e) {
    // Non-access repository failure.
    throw e;
}
```
