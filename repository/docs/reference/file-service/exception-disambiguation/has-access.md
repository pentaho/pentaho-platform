---
type: reference
title: Disambiguating hasAccess
description: Public-API-only disambiguation recipe for `FileService`'s hasAccess operation(s).
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating hasAccess

**`hasAccess`** — the public `unifiedRepository.hasAccess()` (used directly by
`RepositoryFileProvider`, not via `FileService`, which has no equivalent wrapper): same
as the main doc's `hasAccess` row:

- missing, unreadable, or lacking requested resource privileges → `false`;
- `repository.read` ABS denial before the query → `UnifiedRepositoryAccessDeniedException`;
- other repository failure before a result → `UnifiedRepositoryException`.

`RepositoryFileProvider` maps the latter two to `AccessControlException` and
`OperationFailedException`, respectively. This preserves the `IGenericFileProvider` /
`IGenericFileService` contract and lets callers such as Browse Files handle both through
the declared `OperationFailedException` hierarchy.

```java
try {
    return unifiedRepository.hasAccess(path.toString(), repositoryPermissions);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // Operation-wide denial; the resource permission query did not run.
    throw new AccessControlException(e);
} catch (UnifiedRepositoryException e) {
    // Repository failed before producing true or false.
    throw new OperationFailedException(e);
}
```
