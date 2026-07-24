---
type: architecture
title: ExceptionLoggingDecorator Layer
description: How the `ExceptionLoggingDecorator` (`unifiedRepository` bean) surfaces and translates repository exceptions.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# `ExceptionLoggingDecorator` (`unifiedRepository` bean)

Role: exception translation and logging only – no access decision is made here.

Every method call is wrapped in `callLogThrow()`, which:

1. Iterates the full exception cause chain (most-specific-cause first).
2. Looks up the exception class name in the converter map configured in `repository.spring.xml`.
3. If none matched, and the calling method supplied a **method-specific fallback
   constructor** (see below), throws that subtype instead.
4. Otherwise throws a generic `UnifiedRepositoryException`.

Configured converters (checked first, against **every** class in the cause chain, not
just the direct cause — so these take priority even over a method-specific fallback):

| Source exception | Converted to | Thrown by (only) |
|---|---|---|
| `org.springframework.security.access.AccessDeniedException` | `UnifiedRepositoryAccessDeniedException` | Any ABS-guarded method ([Method Interceptor layer](layer-method-interceptor.md)) |
| `RepositoryFileDaoFileExistsException` | `UnifiedRepositoryFileExistsException` | **`undeleteFile`** only — thrown when a file/folder already exists at the deleted item's original path (`DefaultDeleteHelper.undeleteFile`) |
| `RepositoryFileDaoReferentialIntegrityException` | `UnifiedRepositoryReferentialIntegrityException` | **`permanentlyDeleteFile`** only — thrown when other JCR nodes still hold references to the file being permanently deleted (`DefaultDeleteHelper.permanentlyDeleteFile`) |
| `RepositoryFileDaoMalformedNameException` | `UnifiedRepositoryMalformedNameException` | **`setFileMetadata`** only — thrown when a metadata **key** (not a file/folder name) contains reserved characters (`JcrRepositoryFileUtils.setMetadataItemForFile` → `checkName()`). The equivalent `checkName()` calls for file/folder names elsewhere in `JcrRepositoryFileUtils` are commented-out dead code, so no other method can throw this. |

> **Important:** `javax.jcr.AccessDeniedException` (thrown at the JCR DAO layer for certain
> write-access violations) is **not** in this map — and neither is the class it actually
> arrives here as. Before reaching `ExceptionLoggingDecorator`, `JcrTemplate` translates it
> (and `PathNotFoundException`/`ItemNotFoundException`) into
> `org.springframework.dao.DataRetrievalFailureException` (see [JcrTemplate exception translation layer](layer-jcr-template-exception-translation.md)). That class is also not
> in the map, so — unless a method-specific fallback constructor applies (see below) — it
> propagates as a generic `UnifiedRepositoryException`, not as
> `UnifiedRepositoryAccessDeniedException` — but its cause chain still contains the original
> `javax.jcr.AccessDeniedException`, two hops down (`getCause().getCause()`).

## Method-specific fallback constructors (not a generic `UnifiedRepositoryException`!)

Three call sites in `ExceptionLoggingDecorator` pass an extra `exceptionConstructor`
argument to `callLogThrow()`, which is used **only if no converter-map entry matched**,
in place of the generic `UnifiedRepositoryException`:

| Method | Fallback exception (instead of generic `UnifiedRepositoryException`) |
|---|---|
| `createFile` (both overloads — with and without an explicit ACL) | `UnifiedRepositoryCreateFileException` |
| `updateFile` | `UnifiedRepositoryUpdateFileException` |

**`createFolder` and `updateFolder` do *not* get this treatment** — despite being the
folder-equivalents of the methods above, they still throw the plain generic
`UnifiedRepositoryException` for the same not-found/no-write conditions (see [IUnifiedRepository access-control summary table](../../reference/unified-repository/summary-table-per-method.md)).

Both `UnifiedRepositoryCreateFileException` and `UnifiedRepositoryUpdateFileException`
extend `UnifiedRepositoryException` directly (no other state/behavior added beyond a
message prefix), so a `catch (UnifiedRepositoryException e)` still catches them via
ordinary polymorphism — but code that wants to report the *most specific* declared type
should catch these first. Because this substitution only changes the **outer** exception
class, it has no effect on the cause-chain depth/shape documented in [JcrTemplate exception translation layer](layer-jcr-template-exception-translation.md)/[IUnifiedRepository exception taxonomy](../../reference/unified-repository/exception-taxonomy.md) — the
not-found-vs-no-write disambiguation by innermost cause still applies identically underneath
either wrapper.

---

