---
type: reference
title: "Disambiguating IUnifiedRepository Exceptions: General Approach"
description: Shared helper functions, the time-of-check race, known gaps, and exception-type notes for public-API-only IUnifiedRepository exception disambiguation.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Disambiguating `IUnifiedRepository` exceptions via public API calls

Companion to [Unified Repository Architecture Overview](../../../architecture/unified-repository/overview.md).
The [exception taxonomy](../exception-taxonomy.md) distinguishes:

- not-found/no-read failures: `null`/`false`, or generic/method-specific
  `UnifiedRepositoryException`;
- ABS and native JCR access denials: `UnifiedRepositoryAccessDeniedException`; and
- custom voter denials: often a silent `null`.

**This file exists because that cause-chain inspection is not something application code
should rely on.** It is useful for understanding *why* the ambiguity exists, and for
diagnostic logging, but:

- `getCause()`/`getCause().getCause()` structure is an internal implementation detail of
  the current `ExceptionLoggingDecorator` → `JcrTemplate` → `SessionFactoryUtils` wiring
  (main doc [JcrTemplate exception translation layer](../../../architecture/unified-repository/layer-jcr-template-exception-translation.md)). Nothing in `IUnifiedRepository`'s public method signatures documents
  or promises this shape. A library upgrade, a Pentaho patch, or even an unrelated
  refactor could change it silently.
- Branching production logic on `instanceof` checks against classes several hops down an
  undeclared cause chain is brittle by construction.

Instead, this file shows how to disambiguate every ambiguous case using only **public,
documented `IUnifiedRepository` API calls** — proactive existence/permission checks made
either before the risky call or from inside a `catch` block, using the exact same methods
the calling code already depends on elsewhere.

## The general approach

1. **Catch the outer public type first.** `UnifiedRepositoryAccessDeniedException` vs.
   generic/method-specific `UnifiedRepositoryException` vs. a `null`/boolean return are
   materially different outcomes.
2. **Always catch `UnifiedRepositoryAccessDeniedException` (`URADE`) before generic
   `UnifiedRepositoryException`.** `URADE` is a subtype. It can mean either a missing ABS
   action or a native JCR denial on a specific resource. For `updateAcl`, it can also be
   the direct `ACL_MANAGEMENT` check. Use operation-specific resource follow-up checks;
   if none reproduce, the remaining explanation is ABS denial or a state race.
3. **To refine why a failure occurred, make a targeted follow-up call** using
   one of these two public primitives:
   - `unifiedRepository.getFileById(id)` / `getFile(path)` → `null` means "not found, or
     the caller has no `jcr:read` on it" (the two are still indistinguishable from each
     other via public API — see main doc [JcrRepositoryFileDao layer](../../../architecture/unified-repository/layer-jcr-repository-file-dao.md)/[JcrTemplate exception translation layer](../../../architecture/unified-repository/layer-jcr-template-exception-translation.md) for why).
   - `unifiedRepository.hasAccess(path, EnumSet.of(permission))` → `false` means "the
     caller lacks that permission on that node" (`RepositoryFilePermission.READ`,
     `WRITE`, `DELETE`, `ACL_MANAGEMENT`, or `ALL`).
4. **Treat the follow-up call as diagnostic, not authoritative.** It runs *after* the
   original operation failed, so it describes the state at that later instant, not
   necessarily the instant of the original failure (see "Time-of-check race" below).
5. **Some Pentaho-layer gaps have no public-API equivalent at all** (see "Known gaps"
   below) — for those, the best a caller can do is narrow the possibilities and report
   the remainder as inconclusive, rather than fabricate false precision.

### Shared helper functions

```java
import java.util.EnumSet;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

static boolean isFoundAndReadable(IUnifiedRepository repo, String path) {
    return repo.getFile(path) != null;
}

static boolean isFoundAndReadable(IUnifiedRepository repo, java.io.Serializable fileId) {
    return repo.getFileById(fileId) != null;
}

static boolean canWrite(IUnifiedRepository repo, String path) {
    return repo.hasAccess(path, EnumSet.of(RepositoryFilePermission.WRITE));
}

static boolean canDelete(IUnifiedRepository repo, String path) {
    return repo.hasAccess(path, EnumSet.of(RepositoryFilePermission.DELETE));
}

static boolean canManageAcl(IUnifiedRepository repo, String path) {
    return repo.hasAccess(path, EnumSet.of(RepositoryFilePermission.ACL_MANAGEMENT));
}
```

### Time-of-check race

None of the checks above are atomic with the original operation. Between the original
call failing and the follow-up call running, another session could create the file, change
its ACL, or delete it. In a highly concurrent system this means:

- A follow-up check can occasionally give an answer that doesn't match what actually
  caused the original failure (e.g. `isFoundAndReadable` now returns `true` because
  someone else just created the file, even though it didn't exist a moment ago).
- This is an inherent limitation of any check-then-act pattern without inspecting the
  actual cause — not a defect in the approach, just an honest caveat. If you need
  certainty rather than a best-effort diagnosis, you must accept either the brittleness
  of cause-chain inspection (main doc [IUnifiedRepository exception taxonomy](../exception-taxonomy.md)) or design around idempotent retries instead of
  root-cause classification.

### Known gaps (no public API exists to check these)

- **`jcr:readAccessControl`** (needed by `getAcl`/`getEffectiveAces`) has no corresponding
  `RepositoryFilePermission` value — the enum only exposes `READ`, `WRITE`, `DELETE`,
  `ACL_MANAGEMENT`, `ALL` (`READ_ACL`/`WRITE_ACL` are explicitly commented out in the
  enum's own source, by design). A caller cannot ask `hasAccess()` about this privilege.
- **Lock-specific and version-restore-specific JCR privileges** (`lockFile`/`unlockFile`,
  `restoreFileAtVersion`) also have no dedicated `RepositoryFilePermission` value; `WRITE`
  is the closest public proxy, but it is not guaranteed to map to the exact privilege JCR
  checks internally for these operations.
- **`jcr:removeChildNodes` on a *parent* folder** (relevant to `deleteFile`,
  `permanentlyDeleteFile`, `moveFile`'s source parent — see main doc [per-node JCR privilege requirements and Magic ACE caveats](../../../architecture/unified-repository/layer-jcr-repository-file-dao.md#per-node-jcr-privilege-requirements-and-magic-ace-caveats), marked `⚠`)
  can only be checked via `hasAccess()` if the caller happens to already know the parent's
  path and calls `hasAccess(parentPath, WRITE)` explicitly — `IUnifiedRepository` does not
  do this for you, and the closest public permission (`WRITE`) is again only a proxy.
- **The internal `.trash` folder** (relevant to `deleteFile`) is not addressable through
  any `IUnifiedRepository` method at all, so its `jcr:addChildNodes` privilege can never
  be checked from application code.

Where a snippet below hits one of these gaps, it says so explicitly rather than guessing.

### A note on exception types

Not every ambiguous failure surfaces as the plain, generic `UnifiedRepositoryException`.
Two independent things can substitute a more specific subclass, and both are **public,
declared API** — no cause inspection needed to know about them, just an accurate `catch`:

- **Well-defined, non-ambiguous, non-access-control conditions** get their own dedicated
  subclass via `ExceptionLoggingDecorator`'s converter map (checked first, see main doc
  [ExceptionLoggingDecorator layer](../../../architecture/unified-repository/layer-exception-logging-decorator.md)): `UnifiedRepositoryFileExistsException` (`undeleteFile` only), 
  `UnifiedRepositoryReferentialIntegrityException` (`permanentlyDeleteFile` only), and
  `UnifiedRepositoryMalformedNameException` (`setFileMetadata` only, and only for
  metadata *keys*, not file/folder names). These should be caught **explicitly and
  before** the generic disambiguation logic — they are never about access control, so
  running them through `isFoundAndReadable()`/`canWrite()`/etc. would be misleading.
- **`createFile` and `updateFile`** (but not `createFolder`/`updateFolder`) substitute
  `UnifiedRepositoryCreateFileException`/`UnifiedRepositoryUpdateFileException` for an
  unmatched generic failure, including uncaught not-found/no-read. They do **not**
  substitute for JCR access denial because `AccessDeniedExceptionConverter` matches first
  and emits `URADE` (main doc [ExceptionLoggingDecorator layer](../../../architecture/unified-repository/layer-exception-logging-decorator.md)). Since both
  subclasses extend `UnifiedRepositoryException`, a `catch (UnifiedRepositoryException e)`
  still catches them — but code that wants to report the most specific declared type
  should catch these explicitly.

All five subclasses live in `org.pentaho.platform.api.repository2.unified`, alongside
`UnifiedRepositoryException` and `UnifiedRepositoryAccessDeniedException` themselves.

---
