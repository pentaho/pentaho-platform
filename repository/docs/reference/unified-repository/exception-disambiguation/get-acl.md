---
type: reference
title: Disambiguating getAcl / getEffectiveAces
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s getAcl / getEffectiveAces operation(s).
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating getAcl / getEffectiveAces

**`getAcl` / `getEffectiveAces`** — hits the `jcr:readAccessControl` known gap above:

```java
try {
    RepositoryFileAcl acl = unifiedRepository.getAcl(fileId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS repository.read denial or native jcr:readAccessControl denial.
    // RepositoryFilePermission has no read-ACL equivalent, so public API cannot refine it.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, fileId)) {
        // file not found / no jcr:read
    } else {
        // file exists and is readable, but the ACL read still failed. Most likely
        // explanation is a missing jcr:readAccessControl privilege, but there is NO
        // public IUnifiedRepository call to confirm this specifically (known gap
        // above) — report as "readable file, ACL access denied or other failure"
        // without further certainty.
        throw e;
    }
}
```
