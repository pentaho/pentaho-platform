---
type: reference
title: IUnifiedRepository Exception Taxonomy
description: The exception types IUnifiedRepository callers observe and how to tell same-looking exceptions apart.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Exception taxonomy visible to callers

> **Class name legend** (simple names used below; only the Spring Security one is
> spelled out in full every time, to avoid confusion with the unrelated JCR exception
> of the same simple name):
>
> | Simple name used below | Fully qualified name |
> |---|---|
> | `UnifiedRepositoryAccessDeniedException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException` |
> | `UnifiedRepositoryException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException` |
> | `UnifiedRepositoryCreateFileException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryCreateFileException` (extends `UnifiedRepositoryException`) |
> | `UnifiedRepositoryUpdateFileException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryUpdateFileException` (extends `UnifiedRepositoryException`) |
> | `UnifiedRepositoryFileExistsException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryFileExistsException` (extends `UnifiedRepositoryException`) |
> | `UnifiedRepositoryMalformedNameException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryMalformedNameException` (extends `UnifiedRepositoryException`) |
> | `UnifiedRepositoryReferentialIntegrityException` | `org.pentaho.platform.api.repository2.unified.UnifiedRepositoryReferentialIntegrityException` (extends `UnifiedRepositoryException`) |
> | *(Spring Security)* `org.springframework.security.access.AccessDeniedException` | *(always spelled out — never abbreviated, to distinguish from the JCR exception below)* |
> | `AccessDeniedException` (JCR) | `javax.jcr.AccessDeniedException` |
> | `PathNotFoundException` | `javax.jcr.PathNotFoundException` |
> | `ItemNotFoundException` | `javax.jcr.ItemNotFoundException` |
> | `DataRetrievalFailureException` | `org.springframework.dao.DataRetrievalFailureException` |

For every row below marked "**direct cause**", `exception.getCause()` returns exactly that
class (one hop). For every row marked "**two-hop cause**", `exception.getCause()` returns
the intermediate class, and `exception.getCause().getCause()` returns the JCR exception —
see §2.7 for why this extra hop exists (the `JcrTemplate` → `SessionFactoryUtils.translateException()`
translation layer).

> **This table does not yet cover every distinct exception *type*, only the ambiguous
> not-found/no-write pair.** Five more `UnifiedRepositoryException` subclasses exist and are
> thrown for well-defined, **non**-ambiguous, **non**-access-control conditions — they should
> be caught (and, in code, checked) *before* falling back to generic disambiguation logic:
>
> | Exception (all extend `UnifiedRepositoryException`) | Thrown by (only) | Direct cause | When |
> |---|---|---|---|
> | `UnifiedRepositoryFileExistsException` | `undeleteFile` | `RepositoryFileDaoFileExistsException` (one hop — this DAO exception is a plain `RuntimeException`, not a `javax.jcr.RepositoryException`, so `JcrTemplate` never touches it, §2.7) | A file/folder already exists at the deleted item's original path. Not an access-control condition at all — do not run this through not-found/no-write disambiguation logic. |
> | `UnifiedRepositoryReferentialIntegrityException` | `permanentlyDeleteFile` | `RepositoryFileDaoReferentialIntegrityException` (one hop, same reasoning) | Other JCR nodes still hold references to the file being permanently deleted; thrown as an explicit pre-check *before* any JCR remove call. Not an access-control condition. |
> | `UnifiedRepositoryMalformedNameException` | `setFileMetadata` | `RepositoryFileDaoMalformedNameException` (one hop, same reasoning) | A metadata **key** (not the file/folder name) contains reserved characters. Not an access-control condition, and — despite the name — unrelated to file/folder naming (the equivalent checks for file/folder names elsewhere are dead code, §2.1). |
> | `UnifiedRepositoryCreateFileException` | `createFile` (both overloads) | *Same as the generic `URE` rows below* — this is a drop-in replacement for the outer class only, used **only** when no converter-map entry matched (§2.1) | Any not-found/no-write condition that would otherwise be a generic `URE` for `createFolder`. The cause-chain shape/depth underneath is identical to the generic-`URE` rows below — only the outermost class differs. |
> | `UnifiedRepositoryUpdateFileException` | `updateFile` | *Same as the generic `URE` rows below*, same caveat | Any not-found/no-write condition that would otherwise be a generic `URE` for `updateFolder`. |


| Exception thrown to caller | Direct cause (`getCause()`) | Underlying cause (`getCause().getCause()`) | When |
|---|---|---|---|
| `UnifiedRepositoryAccessDeniedException` | *(direct cause)* Spring Security's `org.springframework.security.access.AccessDeniedException` | *(none — Spring Security throws it with no wrapped cause)* | <ul><li>ABS method-level check fails in `unifiedRepositoryMethodInterceptor` (user lacks required global action)</li><li>This is the **only** case where `UnifiedRepositoryAccessDeniedException`'s cause is the Spring Security exception, because it is the only entry in the `ExceptionLoggingDecorator` converter map (§2.1) that targets `UnifiedRepositoryAccessDeniedException`</li></ul> |
| `UnifiedRepositoryAccessDeniedException` | *(no cause at all — `getCause()` is `null`)* | — | <ul><li>`updateAcl` — caller lacks `ACL_MANAGEMENT` on the specific file. Thrown directly by `DefaultUnifiedRepository`, bypassing `ExceptionLoggingDecorator`'s converter map entirely (§2.3)</li><li>**Disambiguation**: if `getCause()` is `null`, it is this direct-throw case, not the ABS-check case above</li></ul> |
| `null` return value | — | — | <ul><li>`accessVoterManager` voter denies READ/WRITE/DELETE (requires custom voters; no-op in default config — see §2.4.1)</li><li>**or** Jackrabbit's `PathNotFoundException`/`ItemNotFoundException` was caught **inside** the DAO callback and converted to `null` before ever reaching `ExceptionLoggingDecorator` (only for the specific methods listed in §2.4.5/§3 — `getFile`, `getFileById`, `getData*`, and the locale-property methods that route through them)</li><li>**Disambiguation**: a `null` return by itself does not distinguish "not found" from "no read access" from "voter denied" — all three are silent</li></ul> |
| Generic `UnifiedRepositoryException` (or `UnifiedRepositoryCreateFileException`/`UnifiedRepositoryUpdateFileException` — see note above the table) | *(two-hop cause)* `DataRetrievalFailureException` | `AccessDeniedException` (JCR) | <ul><li>Single-target write/mutate operations (`session.save()`, lock/version manager calls) where the user lacks the required JCR privilege on **the one node involved** — `createFolder` (parent; generic `URE`), `createFile` (parent; **`UnifiedRepositoryCreateFileException`**, not generic `URE`), `updateFolder` (generic `URE`), `updateFile` (**`UnifiedRepositoryUpdateFileException`**, not generic `URE`), `setFileMetadata`, `setLocalePropertiesForFile*`, `deleteLocalePropertiesForFile`, `lockFile`/`unlockFile`, `restoreFileAtVersion`</li><li>`deleteFile` — the explicit `aclDao.hasAccess(DELETE)` pre-check throws this for **the file itself** (not source/destination — single target)</li><li>`moveFile` — applies to **either or both paths**: the explicit `accessVoterManager.hasAccess(WRITE)` pre-check throws it for the **source** file, for the **destination** folder, or (if both pre-checks pass) the underlying `workspace.move()` JCR call can still throw it for either the source or the destination</li><li>`copyFile` — applies to the **destination** folder only (explicit pre-check or the underlying `workspace.copy()` JCR call); the **source** is never subject to a write/JCR-privilege check at all (see §3 `copyFile` row and [Repository Permission Model](../repository-permission-model.md)'s "Notable Gaps")</li></ul> |
| Generic `UnifiedRepositoryException` (or `UnifiedRepositoryCreateFileException`/`UnifiedRepositoryUpdateFileException` — see note above the table) | *(two-hop cause)* `DataRetrievalFailureException` | `PathNotFoundException` or `ItemNotFoundException` | <ul><li>Single-target read (or read-before-write) operations where the node genuinely does not exist **or** the user has no `jcr:read` privilege on it, and the DAO method did **not** catch the not-found exception internally — `getFileAtVersion`, `getChildren`'s folder lookup, `getTree`'s root lookup, `getAcl`, `getEffectiveAces`, `canUnlockFile`, `getVersionSummary*`, `getFileMetadata`, `getReferrers`, `restoreFileAtVersion`, `lockFile`/`unlockFile`, `setFileMetadata`, `deleteLocalePropertiesForFile`, `createFolder`/`updateFolder` (generic `URE`), `createFile`/`updateFile` (method-specific wrapper instead of generic `URE` — see §3)</li><li>`moveFile` — applies to the **source** file only: `aclDao.getAcl(fileId)` is called unconditionally and throws uncaught if the source is not found/unreadable. The **destination** does *not* go through this path — see the `IllegalArgumentException` row below</li><li>`copyFile` — applies to the **source** file only: `session.getNodeByIdentifier(fileId)` is called directly and throws uncaught if the source is not found/unreadable (recall source access is otherwise unchecked). The **destination** does *not* go through this path either</li></ul> |
| Generic `UnifiedRepositoryException` | `IllegalArgumentException` or `NullPointerException` | *(none)* | <ul><li>`moveFile` / `copyFile` — applies to the **destination's parent folder**, not the destination path itself (the destination path not existing is the normal case, e.g. a rename): if the destination doesn't exist *and* its parent folder is also missing/unreadable, an `Assert.isTrue` failure raises `IllegalArgumentException` (not a JCR/access exception at all — a distinct failure mode from the source-side `ItemNotFoundException` row above)</li><li>`updateAcl` — `NullPointerException` when `getFileById(acl.getId())` returns `null` (file not found/no-read) and the code dereferences `file.getPath()` before the ACL check runs (see §3 `updateAcl` row)</li></ul> |
| Generic `UnifiedRepositoryException` | Any other uncaught exception | *(varies; often none)* | Unexpected errors / programming defects at any layer, not access-control related. |

## Disambiguating same-looking exceptions

Because a write-denial and a not-found/no-read condition can both surface as the exact same
outer pair of classes (`UnifiedRepositoryException` → `DataRetrievalFailureException`), callers
that need to tell them apart must inspect the **innermost** cause:

```java
try {
    unifiedRepository.updateFolder(folder, "comment");
} catch (UnifiedRepositoryException e) {
    // Exactly two hops, per §2.7/§4 — URE -> DataRetrievalFailureException -> JCR
    // exception. Do NOT use a generic "walk to the bottom" root-cause helper here:
    // the chain is documented to be exactly this deep, and inspecting getCause()
    // directly (rather than the true root) keeps the check honest about that.
    Throwable underlying = (e.getCause() != null) ? e.getCause().getCause() : null;
    if (underlying instanceof javax.jcr.AccessDeniedException) {
        // user lacks JCR write privilege
    } else if (underlying instanceof javax.jcr.PathNotFoundException
            || underlying instanceof javax.jcr.ItemNotFoundException) {
        // node does not exist, or user has no read access to it (still indistinguishable from each other)
    }
    // else: some other underlying failure (e.getCause() may itself be null, or a
    // different intermediate class than DataRetrievalFailureException — see §2.7)
}
```

`ExceptionLoggingDecorator` itself uses a similar technique internally
(`ExceptionUtils.getThrowableList(e)`, reversed) to find a matching converter — see §2.1
and §2.7. That decorator-internal usage walks the full list because it must match
*any* converter-mapped class at *any* depth; the caller-side check above is narrower and
deliberately fixed at exactly two hops, because that is the depth the doc has verified for
every generic `UnifiedRepositoryException` (see §4).

> **This cause-chain depth is not a stable, public contract.** It reflects the current
> `se-jcr`/Jackrabbit/`ExceptionLoggingDecorator` wiring verified in this document, but
> `getCause()` structure is an internal implementation detail that could change with a
> library upgrade or an unrelated refactor — nothing in `IUnifiedRepository`'s public
> signatures documents or promises it. The snippet above is useful for *understanding*
> and for diagnostic logging, but application code that branches on it is brittle.
> For a disambiguation strategy that only uses public, documented `IUnifiedRepository`
> API calls (proactive `getFileById`/`hasAccess` checks instead of cause inspection),
> see the **[Unified Repository exception disambiguation index](exception-disambiguation/index.md)**,
> which covers every method in the [summary table](summary-table-per-method.md).

---

