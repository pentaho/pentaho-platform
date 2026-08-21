---
type: reference
title: IUnifiedRepository Exception Taxonomy
description: Exception types IUnifiedRepository callers observe and the distinct causes behind them.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Exception taxonomy visible to callers

## Access denial

`UnifiedRepositoryAccessDeniedException` (`URADE`) has three proven sources:

| Source | Direct cause | Nested cause | Meaning |
|---|---|---|---|
| Method-security interceptor | `org.springframework.security.access.AccessDeniedException` | None | Required ABS action is missing. |
| Native JCR enforcement | `org.springframework.security.access.AccessDeniedException` | `javax.jcr.AccessDeniedException` | A resource-specific JCR privilege is missing. `PentahoJcrTemplate` creates the Spring Security wrapper; `ExceptionLoggingDecorator` converts it to `URADE`. |
| `DefaultUnifiedRepository.updateAcl()` | None | None | Explicit `ACL_MANAGEMENT` check failed; `URADE` is thrown directly. |

Therefore `URADE` is never safe to interpret as ABS-only. For mutating methods it can
mean either global method authorization or a per-resource JCR denial.

Converter-map matching examines the full cause chain and takes precedence over
method-specific fallback wrappers. Thus JCR access denial from `createFile` or
`updateFile` also becomes `URADE`, not `UnifiedRepositoryCreateFileException` or
`UnifiedRepositoryUpdateFileException`.

## Not found or unreadable

Jackrabbit hides nodes without `jcr:read` by throwing the same
`PathNotFoundException`/`ItemNotFoundException` used for genuinely absent nodes.

Two outcomes exist:

- DAO helpers that catch the JCR exception return `null`, `false`, or omit the node.
- If the exception escapes the callback, base `JcrTemplate` translation produces:

```text
UnifiedRepositoryException
└── org.springframework.dao.DataRetrievalFailureException
    └── javax.jcr.PathNotFoundException or javax.jcr.ItemNotFoundException
```

`createFile` and `updateFile` replace the outer generic exception with
`UnifiedRepositoryCreateFileException` and `UnifiedRepositoryUpdateFileException`
respectively when no converter-map entry matches.

## Custom access-voter denial

The default `RepositoryAccessVoterManager` has no voters. If a plugin registers one,
several DAO methods short-circuit with `null` when that voter denies READ, WRITE, or
DELETE. No JCR operation occurs, so no exception reaches `PentahoJcrTemplate`.

Callers must treat documented `null` returns independently from `URADE`.

## Other specific exceptions

| Exception | Operation | Condition |
|---|---|---|
| `UnifiedRepositoryFileExistsException` | `undeleteFile` | Item exists at original path. |
| `UnifiedRepositoryReferentialIntegrityException` | `permanentlyDeleteFile` | Other nodes still reference target. |
| `UnifiedRepositoryMalformedNameException` | `setFileMetadata` | Metadata key contains reserved characters. |
| `UnifiedRepositoryCreateFileException` | `createFile` | Unmatched failure, including uncaught not-found/no-read; not JCR access denial. |
| `UnifiedRepositoryUpdateFileException` | `updateFile` | Unmatched failure, including uncaught not-found/no-read; not JCR access denial. |

All extend `UnifiedRepositoryException`.

## Diagnostic cause inspection

Cause inspection can distinguish the two converted `URADE` paths:

```java
try {
    unifiedRepository.createFolder(parentId, folder, null);
} catch (UnifiedRepositoryAccessDeniedException e) {
    Throwable springCause = e.getCause();
    Throwable jcrCause = springCause != null ? springCause.getCause() : null;
    if (jcrCause instanceof javax.jcr.AccessDeniedException) {
        // Native JCR resource-level denial.
    } else if (springCause instanceof org.springframework.security.access.AccessDeniedException) {
        // ABS method-security denial.
    } else {
        // Direct URADE, currently updateAcl's ACL_MANAGEMENT check.
    }
}
```

This shape is verified implementation behavior, not a stable public contract. Production
classification should prefer public follow-up permission checks where possible; see
[exception disambiguation](exception-disambiguation/general-approach.md).
