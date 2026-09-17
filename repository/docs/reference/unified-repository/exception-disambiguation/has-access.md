---
type: reference
title: Disambiguating hasAccess
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s hasAccess operation(s).
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating hasAccess

**`hasAccess`** returns `false` for a missing path, an unreadable path, or a path that
lacks any requested privilege. Those resource outcomes are intentionally indistinguishable;
`hasAccess` **is** the check, and no other public call adds information.

This does not make the method exception-free. The `repository.read` ABS interceptor runs
before the resource check and can throw `UnifiedRepositoryAccessDeniedException`. A
non-access repository failure can also produce `UnifiedRepositoryException`. Neither case
is a negative permission result: the method failed before returning a boolean.

```java
try {
    boolean allowed = unifiedRepository.hasAccess(path, permissions);
    if (!allowed) {
        // Path is missing or unreadable, or at least one requested privilege is absent.
        // These outcomes are intentionally indistinguishable.
    }
} catch (UnifiedRepositoryAccessDeniedException e) {
    // Operation-wide denial: repository.read ABS rejected the query before it ran.
} catch (UnifiedRepositoryException e) {
    // Non-access repository failure; no permission result was produced.
}
```
