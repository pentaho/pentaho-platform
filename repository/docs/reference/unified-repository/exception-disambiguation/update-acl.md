---
type: reference
title: Disambiguating updateAcl
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s updateAcl operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating updateAcl

**`updateAcl`** — entirely public-API disambiguation, no cause inspection needed at all:

```java
try {
    unifiedRepository.updateAcl(acl);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // Could be the coarse ABS-level "repository.create" action check, OR the
    // file-specific ACL_MANAGEMENT gate in DefaultUnifiedRepository — both throw the
    // same class. Confirm which one with a follow-up call:
    RepositoryFile f = unifiedRepository.getFileById(acl.getId());
    if (f != null && !canManageAcl(unifiedRepository, f.getPath())) {
        // file exists/readable but caller lacks ACL_MANAGEMENT on it — matches the
        // file-specific gate. Note the Owner-ACE gap (main doc §2.4.8): file owners
        // are NOT exempt from this check.
    } else {
        // more likely the coarse ABS-level check failed instead — this is a global
        // permission, not a per-file one, and cannot be confirmed with a follow-up
        // call (see main doc §2.2)
    }
} catch (NullPointerException e) {
    // Defect-shaped signal, not a documented exception (main doc §3 `updateAcl` row).
    if (unifiedRepository.getFileById(acl.getId()) == null) {
        // confirms: file not found / no jcr:read triggered this
    } else {
        throw e; // unexpected — investigate further
    }
}
```
