---
type: architecture
title: JcrRepositoryFileAclDao hasAccess Layer
description: How `JcrRepositoryFileAclDao.hasAccess()` evaluates permissions.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# `JcrRepositoryFileAclDao` – `hasAccess()`

Used directly by `DefaultUnifiedRepository.updateAcl()` and internally by
`JcrRepositoryFileDao.deleteFile()`.

```java
// JcrRepositoryFileAclDao.hasAccess()
session.getAccessControlManager().hasPrivileges(absPath, privs);
// PathNotFoundException → returns false (not an exception)
```

This is a **Jackrabbit native privilege check** on the JCR `AccessControlManager`.
Returns `false` (not an exception) when the path does not exist.

---

