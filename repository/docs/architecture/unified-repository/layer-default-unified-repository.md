---
type: architecture
title: DefaultUnifiedRepository Target Bean
description: Role of `DefaultUnifiedRepository` as the target bean behind the interceptor and decorator layers.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# `DefaultUnifiedRepository` (target bean)

The target mostly delegates straight to the DAOs. The only explicit access check at
this layer is in **`updateAcl()`**:

```java
// DefaultUnifiedRepository.updateAcl()
RepositoryFile file = getFileById(acl.getId());
if (!hasAccess(file.getPath(), EnumSet.of(RepositoryFilePermission.ACL_MANAGEMENT))) {
    throw new UnifiedRepositoryAccessDeniedException(...);
}
```

- **Check**: `IRepositoryFileAclDao.hasAccess()` → Jackrabbit native ACL privilege check.
- **Exception thrown**: `UnifiedRepositoryAccessDeniedException` **directly** (not via Spring Security, not via the converter map).
- **Note**: If the file is inaccessible to the caller (returns `null` from `getFileById`
  due to the confounding behaviour described below), this call will throw a
  `NullPointerException` before reaching the explicit access check.

---

