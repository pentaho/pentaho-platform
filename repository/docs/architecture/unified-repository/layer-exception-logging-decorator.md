---
type: architecture
title: ExceptionLoggingDecorator Layer
description: How the unifiedRepository bean surfaces and translates repository exceptions.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# `ExceptionLoggingDecorator` (`unifiedRepository` bean)

Role: exception translation and logging only. It makes no access decision.

Every method call is wrapped in `callLogThrow()`, which:

1. Collects the complete exception cause chain and examines the root cause first.
2. Looks up each exact exception class name in the converter map from
   `repository.spring.xml`.
3. Uses the first matching converter.
4. If no converter matches, uses the method-specific fallback constructor where supplied.
5. Otherwise throws generic `UnifiedRepositoryException`.

## Converter map

| Source exception | Converted to | Sources |
|---|---|---|
| `org.springframework.security.access.AccessDeniedException` | `UnifiedRepositoryAccessDeniedException` | ABS method-security denials **and** JCR `AccessDeniedException` translated by `PentahoJcrTemplate` |
| `RepositoryFileDaoFileExistsException` | `UnifiedRepositoryFileExistsException` | `undeleteFile` |
| `RepositoryFileDaoReferentialIntegrityException` | `UnifiedRepositoryReferentialIntegrityException` | `permanentlyDeleteFile` |
| `RepositoryFileDaoMalformedNameException` | `UnifiedRepositoryMalformedNameException` | `setFileMetadata` metadata-key validation |

The access-denial entry has two materially different inputs:

- `unifiedRepositoryMethodInterceptor` throws Spring Security
  `AccessDeniedException` when an ABS action is missing.
- `PentahoJcrTemplate` wraps `javax.jcr.AccessDeniedException` in the same Spring
  Security exception when native JCR permission enforcement rejects an operation.

Both become `UnifiedRepositoryAccessDeniedException`. Callers cannot classify that public
type as ABS-only.

For JCR denial, the cause chain is:

```text
UnifiedRepositoryAccessDeniedException
└── org.springframework.security.access.AccessDeniedException
    └── javax.jcr.AccessDeniedException
```

For ABS denial, the Spring Security exception normally has no JCR cause.

## Method-specific fallback constructors

Fallback constructors apply only when no converter matched:

| Method | Fallback exception |
|---|---|
| `createFile` (both overloads) | `UnifiedRepositoryCreateFileException` |
| `updateFile` | `UnifiedRepositoryUpdateFileException` |

Consequently, uncaught `ItemNotFoundException`/`PathNotFoundException` can use these
method-specific wrappers, but JCR access denial does not: the Spring Security converter
matches first and produces `UnifiedRepositoryAccessDeniedException`.

`createFolder` and `updateFolder` have no method-specific fallback and use generic
`UnifiedRepositoryException` for unmatched failures.
