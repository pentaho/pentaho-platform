---
type: architecture
title: PentahoJcrTemplate Exception Translation Layer
description: How PentahoJcrTemplate translates JCR exceptions between the native session and ExceptionLoggingDecorator.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# `PentahoJcrTemplate` exception translation

Repository DAO callbacks run inside
`org.pentaho.platform.repository2.unified.jcr.sejcr.PentahoJcrTemplate`, not the
unmodified `org.springframework.extensions.jcr.JcrTemplate`.

`PentahoJcrTemplate.execute()` catches exceptions escaping a `JcrCallback` and applies
`pentahoConvertJcrAccessException()` before delegating other exceptions to the base
`JcrTemplate` converter.

## Access-denial translation

`PentahoJcrTemplate` has explicit branches for both:

- `javax.jcr.AccessDeniedException`, and
- runtime `java.security.AccessControlException`.

Both become `org.springframework.security.access.AccessDeniedException`, preserving the
original exception as the cause. `ExceptionLoggingDecorator` then finds that Spring
Security exception in the cause chain and applies `AccessDeniedExceptionConverter`.

The public result is:

```text
UnifiedRepositoryAccessDeniedException
└── org.springframework.security.access.AccessDeniedException
    └── javax.jcr.AccessDeniedException
```

This chain represents a JCR resource-level denial. It is distinct from an ABS
method-security denial, whose chain normally ends at the Spring Security exception:

```text
UnifiedRepositoryAccessDeniedException
└── org.springframework.security.access.AccessDeniedException
```

Therefore `UnifiedRepositoryAccessDeniedException` is **not ABS-only**. It also reports
native JCR write, delete, lock, version, and access-control denials that escape a callback
as `javax.jcr.AccessDeniedException`.

## Other JCR exceptions

Exceptions not handled by Pentaho's access-specific branches delegate to the base
`JcrTemplate` converter. Relevant mappings include:

| JCR exception | Spring data-access exception |
|---|---|
| `PathNotFoundException` | `DataRetrievalFailureException` |
| `ItemNotFoundException` | `DataRetrievalFailureException` |
| `query.InvalidQueryException` | `DataRetrievalFailureException` |
| `InvalidSerializedDataException` | `DataRetrievalFailureException` |
| `nodetype.ConstraintViolationException` | `DataIntegrityViolationException` |
| `ItemExistsException` | `DataIntegrityViolationException` |
| `ReferentialIntegrityException` | `DataIntegrityViolationException` |
| `version.VersionException` | `DataIntegrityViolationException` |
| `InvalidItemStateException` | `ConcurrencyFailureException` |
| `lock.LockException` | `ConcurrencyFailureException` |
| `LoginException` | `DataAccessResourceFailureException` |
| `NoSuchWorkspaceException` | `DataAccessResourceFailureException` |
| `NamespaceException`, `nodetype.NoSuchNodeTypeException`, `UnsupportedRepositoryOperationException`, `ValueFormatException` | `InvalidDataAccessApiUsageException` |
| Any other `RepositoryException` | `JcrSystemException` |

For an uncaught not-found exception, the public chain is:

```text
UnifiedRepositoryException
└── org.springframework.dao.DataRetrievalFailureException
    └── javax.jcr.PathNotFoundException or javax.jcr.ItemNotFoundException
```

`createFile` and `updateFile` substitute their method-specific outer wrappers when no
converter-map entry matches. Access denial still becomes
`UnifiedRepositoryAccessDeniedException`, because converter-map matches take precedence
over those fallback constructors.

## Exceptions consumed inside callbacks

Translation occurs only when an exception escapes the callback. Several DAO methods catch
`PathNotFoundException` or `ItemNotFoundException` internally and return `null` or `false`;
those paths produce no exception. See
[JcrRepositoryFileDao layer](layer-jcr-repository-file-dao.md) and
[exception taxonomy](../../reference/unified-repository/exception-taxonomy.md).
