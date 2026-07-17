# Disambiguating `IUnifiedRepository` exceptions via public API calls

Companion to [`unified-repository-access-control.md`](./unified-repository-access-control.md)
(referred to below as "the main doc"). That document's §4 shows that a not-found/no-read
condition and a no-write/no-delete condition often surface as the **exact same** outer
exception classes (`UnifiedRepositoryException` wrapping `DataRetrievalFailureException`),
and that they can be told apart by inspecting `getCause().getCause()`.

**This file exists because that cause-chain inspection is not something application code
should rely on.** It is useful for understanding *why* the ambiguity exists, and for
diagnostic logging, but:

- `getCause()`/`getCause().getCause()` structure is an internal implementation detail of
  the current `ExceptionLoggingDecorator` → `JcrTemplate` → `SessionFactoryUtils` wiring
  (main doc §2.7). Nothing in `IUnifiedRepository`'s public method signatures documents
  or promises this shape. A library upgrade, a Pentaho patch, or even an unrelated
  refactor could change it silently.
- Branching production logic on `instanceof` checks against classes several hops down an
  undeclared cause chain is brittle by construction.

Instead, this file shows how to disambiguate every ambiguous case using only **public,
documented `IUnifiedRepository` API calls** — proactive existence/permission checks made
either before the risky call or from inside a `catch` block, using the exact same methods
the calling code already depends on elsewhere.

## The general approach

1. **The outer exception class itself is public API and needs no disambiguation help.**
   `UnifiedRepositoryAccessDeniedException` vs. generic `UnifiedRepositoryException` vs. a
   `null`/boolean return are all part of the declared, documented behavior of
   `IUnifiedRepository` — catch by type as usual.
2. **Always catch `UnifiedRepositoryAccessDeniedException` (`URADE`) *before* a generic
   `catch (UnifiedRepositoryException e)`, never rely on the generic block to also handle
   it.** `URADE` **is a subtype** of `UnifiedRepositoryException`, so a bare
   `catch (UnifiedRepositoryException e)` silently catches `URADE` too. For every method
   in this file **except `updateAcl`**, `URADE` can *only* be the coarse ABS-level action
   check (main doc §2.2), thrown by the AOP interceptor before the target method body —
   and hence the specific file — is ever touched; the per-file, no-write-or-delete-access
   condition for those methods always surfaces as the plain generic class instead (main
   doc §2.4/§2.7). If `URADE` falls through into the generic block's per-file follow-up
   logic (`getFileById`/`canWrite`/etc.), that logic is checking the *wrong* thing — the
   file will typically look perfectly fine (found, readable, writable), and the snippet's
   fallback `throw e`/"race or unaccounted-for failure" branch fires, mis-reporting a
   coarse, global permission problem as an inconclusive per-file one. Every snippet below
   catches `URADE` explicitly, ahead of the generic case, for this reason — `updateAcl` is
   the sole exception, since there `URADE` is genuinely ambiguous between the ABS-level and
   per-file checks and needs the follow-up call to tell them apart (see its own snippet).
3. **To refine *why* a generic failure occurred, make a targeted follow-up call** using
   one of these two public primitives:
   - `unifiedRepository.getFileById(id)` / `getFile(path)` → `null` means "not found, or
     the caller has no `jcr:read` on it" (the two are still indistinguishable from each
     other via public API — see main doc §2.4/§2.7 for why).
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
  of cause-chain inspection (main doc §4) or design around idempotent retries instead of
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
  `permanentlyDeleteFile`, `moveFile`'s source parent — see main doc §2.4.8, marked `⚠`)
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
  §2.1): `UnifiedRepositoryFileExistsException` (`undeleteFile` only), 
  `UnifiedRepositoryReferentialIntegrityException` (`permanentlyDeleteFile` only), and
  `UnifiedRepositoryMalformedNameException` (`setFileMetadata` only, and only for
  metadata *keys*, not file/folder names). These should be caught **explicitly and
  before** the generic disambiguation logic — they are never about access control, so
  running them through `isFoundAndReadable()`/`canWrite()`/etc. would be misleading.
- **`createFile` and `updateFile`** (but *not* `createFolder`/`updateFolder`) substitute
  `UnifiedRepositoryCreateFileException`/`UnifiedRepositoryUpdateFileException` for what
  would otherwise be the generic `UnifiedRepositoryException`, for the *same*
  not-found/no-write conditions covered elsewhere in this file (main doc §2.1). Since both
  subclasses extend `UnifiedRepositoryException`, a `catch (UnifiedRepositoryException e)`
  still catches them — but code that wants to report the most specific declared type
  should catch these explicitly.

All five subclasses live in `org.pentaho.platform.api.repository2.unified`, alongside
`UnifiedRepositoryException` and `UnifiedRepositoryAccessDeniedException` themselves.

---

## Per-operation snippets

**`getFile` / `getFileById` (all overloads), `getData*`, `getAvailableLocalesForFile*`,
`getLocalePropertiesForFile*`** — no exception is thrown at all; not-found, no-read, and
custom-voter-denied all confound into the same silent `null` (main doc §3/§4). **There is
no follow-up call that helps here**: these methods *are* the check, so calling any of them
again on the same path returns the same ambiguous `null`. This ambiguity is unresolvable
via public API.

```java
RepositoryFile file = unifiedRepository.getFileById(fileId);
if (file == null) {
    // Not found, no jcr:read, or a custom accessVoterManager voter denied READ.
    // No further public-API call can distinguish between these three.
}
```

**`getFileAtVersion`** (read-only, single target):

```java
try {
    RepositoryFile f = unifiedRepository.getFileAtVersion(fileId, versionId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, fileId)) {
        // file itself not found / no jcr:read
    } else {
        // the file is readable now, but this specific VERSION lookup still failed —
        // version history is mutable, so this may be a race, or the version itself
        // may be gone; not further diagnosable via public API (read-only op, so a
        // write-denial explanation doesn't apply)
        throw e;
    }
}
```

**`getChildren`** (folder-level not-found only; individual unreadable children are
silently omitted from the list, never reported as an exception):

```java
try {
    List<RepositoryFile> kids = unifiedRepository.getChildren(folderId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, folderId)) {
        // the FOLDER itself not found / unreadable
    } else {
        throw e; // folder is readable now — likely a race; not further diagnosable
    }
}
```

**`getTree`** (root-path not-found only; unreadable descendants are silently pruned):

```java
try {
    RepositoryFileTree tree = unifiedRepository.getTree(new RepositoryRequest(rootPath, null, -1, null));
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, rootPath)) {
        // the ROOT path itself not found / unreadable
    } else {
        throw e;
    }
}
```

**`getAcl` / `getEffectiveAces`** — hits the `jcr:readAccessControl` known gap above:

```java
try {
    RepositoryFileAcl acl = unifiedRepository.getAcl(fileId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
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

**`hasAccess`** — never throws for not-found (returns `false` uniformly for any
permission set, including for a node that doesn't exist). There is nothing to
disambiguate: `hasAccess` **is** the check, and no other public call adds information.

**`createFile`** (only node involved is the **parent**; note the exception *type* itself
— see "A note on exception types" above): 

```java
try {
    unifiedRepository.createFile(parentFolderId, file, data, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    // createFile's not-found/no-write conditions actually surface as
    // UnifiedRepositoryCreateFileException, not the plain generic class — catching the
    // supertype here still works, but catch the specific type first if you want that
    // distinction visible in your own code.
    RepositoryFile parent = unifiedRepository.getFileById(parentFolderId);
    if (parent == null) {
        // PARENT folder not found / unreadable
    } else if (!canWrite(unifiedRepository, parent.getPath())) {
        // parent exists/readable, but caller cannot write into it (proxy for
        // the actual jcr:addChildNodes privilege — see main doc §2.4.8)
    } else {
        throw e; // race, or a non-access failure
    }
}
```

**`createFolder`** (same target/shape as `createFile`, but throws the plain generic
`UnifiedRepositoryException` — it does **not** get the `UnifiedRepositoryCreateFileException`
treatment, main doc §2.1):

```java
try {
    unifiedRepository.createFolder(parentFolderId, folder, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile parent = unifiedRepository.getFileById(parentFolderId);
    if (parent == null) {
        // PARENT folder not found / unreadable
    } else if (!canWrite(unifiedRepository, parent.getPath())) {
        // parent exists/readable, but caller cannot write into it
    } else {
        throw e; // race, or a non-access failure
    }
}
```

**`updateFile`** (single target — the file itself; also gets a method-specific exception
type, `UnifiedRepositoryUpdateFileException`, instead of the plain generic class):

```java
try {
    unifiedRepository.updateFile(file, data, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    // Actually thrown as UnifiedRepositoryUpdateFileException for this method (main doc
    // §2.1); catching the supertype here still works.
    if (!isFoundAndReadable(unifiedRepository, file.getId())) {
        // file concurrently deleted, or became unreadable, between calls
    } else if (!canWrite(unifiedRepository, file.getPath())) {
        // file exists/readable, caller cannot write it
    } else {
        throw e;
    }
}
```

**`updateFolder`** (same target/shape as `updateFile`, but throws the plain generic
`UnifiedRepositoryException` — no method-specific wrapper, main doc §2.1):

```java
try {
    unifiedRepository.updateFolder(folder, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, folder.getId())) {
        // folder concurrently deleted, or became unreadable, between calls
    } else if (!canWrite(unifiedRepository, folder.getPath())) {
        // folder exists/readable, caller cannot write it
    } else {
        throw e;
    }
}
```

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

**`deleteFile`** (single target the caller passes in, but the underlying JCR privilege
denial can come from three different nodes — hits two of the "known gaps" above):

```java
try {
    unifiedRepository.deleteFile(fileId, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file == null) {
        // not found / no jcr:read
    } else if (!canDelete(unifiedRepository, file.getPath())) {
        // no delete access on the FILE itself
    } else {
        // file is deletable per hasAccess(), yet the call still failed. The remaining
        // explanations (main doc §2.4.8) are jcr:removeChildNodes on the PARENT, or
        // jcr:addChildNodes on the .trash folder — neither is checkable via public
        // API (known gaps above); treat as inconclusive.
        throw e;
    }
}
```

**`deleteFileAtVersion`** (same shape as `deleteFile`):

```java
try {
    unifiedRepository.deleteFileAtVersion(fileId, versionId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file == null) {
        // not found / unreadable
    } else if (!canDelete(unifiedRepository, file.getPath())) {
        // no delete access on the file itself
    } else {
        throw e;
    }
}
```

**`permanentlyDeleteFile`** — same not-found/no-delete shape as `deleteFile`, **plus** a
third, entirely distinct, non-access, non-ambiguous failure mode that must be caught
separately (and first) rather than run through the disambiguation logic at all:

```java
try {
    unifiedRepository.permanentlyDeleteFile(fileId, "comment");
} catch (UnifiedRepositoryReferentialIntegrityException e) {
    // Unambiguous, non-access condition: other JCR nodes still hold references to this
    // file (main doc §2.1/§2.4.8) — thrown as an explicit pre-check before any JCR
    // remove call is attempted. No follow-up call needed; the exception type alone
    // already tells you exactly what happened. Do NOT run this through
    // isFoundAndReadable()/canDelete() — the file is not being denied, it is being
    // protected from an integrity violation.
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file == null) {
        // not found / unreadable
    } else if (!canDelete(unifiedRepository, file.getPath())) {
        // no delete access on the file itself
    } else {
        // PARENT-level jcr:removeChildNodes gap (§2.4.8), unchecked — inconclusive
        throw e;
    }
}
```

**`undeleteFile`** (single target — the deleted node — **plus** an entirely distinct,
non-access, non-ambiguous failure mode caught separately and first, same pattern as
`permanentlyDeleteFile` above):

```java
try {
    unifiedRepository.undeleteFile(fileId, "comment");
} catch (UnifiedRepositoryFileExistsException e) {
    // Unambiguous, non-access condition: a file/folder now exists at the deleted item's
    // original path (main doc §2.1) — thrown before the session.move() that would
    // restore it there. No follow-up call needed; do NOT run this through
    // isFoundAndReadable()/canWrite() — it has nothing to do with access control.
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile file = unifiedRepository.getFileById(fileId);
    if (file == null) {
        // deleted node not found / unreadable
    } else if (!canWrite(unifiedRepository, file.getPath())) {
        // no write access (WRITE is the closest public proxy for the session.move()
        // privilege actually checked)
    } else {
        throw e;
    }
}
```

**`moveFile`** (incl. rename) — dual-target; unlike the cause-chain approach in the main
doc, distinguishing source vs. destination here requires two explicit follow-up calls
rather than falling out "for free" from the exception shape:


```java
try {
    unifiedRepository.moveFile(fileId, destAbsPath, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile source = unifiedRepository.getFileById(fileId);
    if (source == null) {
        // SOURCE not found / no jcr:read
    } else {
        String destParentPath = destAbsPath.substring(0, destAbsPath.lastIndexOf('/'));
        boolean destParentExists = isFoundAndReadable(unifiedRepository, destParentPath);
        if (!destParentExists) {
            // DESTINATION'S PARENT folder not found / unreadable. (The destination
            // path itself not existing is the normal rename/move case and is NOT an
            // error — only a missing/unreadable parent is.)
        } else if (!canWrite(unifiedRepository, source.getPath())) {
            // SOURCE not writable
        } else if (!canWrite(unifiedRepository, destParentPath)) {
            // DESTINATION folder not writable
        } else {
            // both source and destination look readable/writable per these checks —
            // remaining explanations are the source PARENT's jcr:removeChildNodes
            // (unchecked at the Pentaho layer, main doc §2.4.8) or a race; not
            // further diagnosable via public API.
            throw e;
        }
    }
}
```

**`copyFile`** — same not-found/parent-folder shape as `moveFile`, but there is no
source-write check to make, because the source is never write/read-checked by this
operation at all (main doc §3 `copyFile` row and permission-model.md's "Notable Gaps"):

```java
try {
    unifiedRepository.copyFile(fileId, destAbsPath, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile source = unifiedRepository.getFileById(fileId);
    if (source == null) {
        // SOURCE not found / no jcr:read
    } else {
        String destParentPath = destAbsPath.substring(0, destAbsPath.lastIndexOf('/'));
        boolean destParentExists = isFoundAndReadable(unifiedRepository, destParentPath);
        if (!destParentExists) {
            // DESTINATION'S PARENT folder not found / unreadable
        } else if (!canWrite(unifiedRepository, destParentPath)) {
            // DESTINATION folder not writable (there is nothing to check on the
            // source side — copyFile never checks source write access)
        } else {
            throw e; // race, or an otherwise-unaccounted-for failure
        }
    }
}
```

**`lockFile` / `unlockFile`** (single target; hits the "no dedicated lock permission" gap):

```java
try {
    unifiedRepository.lockFile(fileId, "message");
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile f = unifiedRepository.getFileById(fileId);
    if (f == null) {
        // not found / unreadable
    } else if (!canWrite(unifiedRepository, f.getPath())) {
        // no write access — WRITE is only a proxy here; there is no dedicated
        // RepositoryFilePermission for the lock privilege JCR actually checks
    } else {
        throw e;
    }
}
```

**`canUnlockFile`** (read-only, returns a boolean; not exception-shaped) — nothing to
disambiguate: a `false` result already tells you everything this method reports.

**`getVersionSummary` / `getVersionSummaryInBatch` / `getVersionSummaries`** (read-only;
hits the "individual version readability" gap — there is no public call to check a
specific version node independently of the file itself):

```java
try {
    VersionSummary vs = unifiedRepository.getVersionSummary(fileId, versionId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, fileId)) {
        // file not found / unreadable
    } else {
        // file itself is readable, but this SPECIFIC VERSION may be unreadable or
        // gone — not diagnosable further via public API
        throw e;
    }
}
```

**`restoreFileAtVersion`** (single target; hits the "no dedicated restore permission" gap):

```java
try {
    unifiedRepository.restoreFileAtVersion(fileId, versionId, "comment");
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile f = unifiedRepository.getFileById(fileId);
    if (f == null) {
        // not found / unreadable
    } else if (!canWrite(unifiedRepository, f.getPath())) {
        // no write access — again only a proxy; the version manager's restore()
        // privilege isn't separately exposed via RepositoryFilePermission
    } else {
        throw e;
    }
}
```

**`setFileMetadata`** (single target; **plus** an entirely distinct, non-access,
non-ambiguous failure mode for malformed metadata *keys*, caught separately and first):

```java
try {
    unifiedRepository.setFileMetadata(fileId, metadataMap);
} catch (UnifiedRepositoryMalformedNameException e) {
    // Unambiguous, non-access condition: one of the metadataMap KEYS (not the file/folder
    // name — despite what the exception name suggests) contains reserved characters
    // (main doc §2.1). No follow-up call needed; do NOT run this through
    // isFoundAndReadable()/canWrite() — it has nothing to do with access control.
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile f = unifiedRepository.getFileById(fileId);
    if (f == null) {
        // not found / unreadable
    } else if (!canWrite(unifiedRepository, f.getPath())) {
        // no write access
    } else {
        throw e;
    }
}
```

**`getFileMetadata`** (read-only single target — only the not-found branch ever applies):

```java
try {
    Map<String, Serializable> metadata = unifiedRepository.getFileMetadata(fileId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, fileId)) {
        // not found / unreadable
    } else {
        throw e;
    }
}
```

**`setLocalePropertiesForFile*`** (not-found is confounded with `null` via
`getFileById`, so only the write-denial branch reaches the `catch` block):

```java
RepositoryFile f = unifiedRepository.getFileById(fileId); // null: not found / no read
try {
    unifiedRepository.setLocalePropertiesForFile(f, locale, props);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!canWrite(unifiedRepository, f.getPath())) {
        // no write access
    } else {
        throw e;
    }
}
```

**`deleteLocalePropertiesForFile`** (single target):

```java
try {
    unifiedRepository.deleteLocalePropertiesForFile(repositoryFile, locale);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    RepositoryFile f = unifiedRepository.getFileById(repositoryFile.getId());
    if (f == null) {
        // not found / unreadable
    } else if (!canWrite(unifiedRepository, f.getPath())) {
        // no write access
    } else {
        throw e;
    }
}
```

**`getReferrers`** (read-only, single target):

```java
try {
    List<RepositoryFile> referrers = unifiedRepository.getReferrers(fileId);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // UnifiedRepositoryAccessDeniedException IS-A UnifiedRepositoryException, so a bare
    // `catch (UnifiedRepositoryException e)` below would silently swallow this too.
    // Per main doc §2.2/§3, this is (for every method except `updateAcl`) ALWAYS the
    // coarse ABS-level action check, thrown by the AOP interceptor before the target
    // method body — and hence the file's own — even runs. It has nothing to do with
    // this specific file, so none of the per-file follow-up checks below apply to it;
    // re-throw (or report) it as a distinct, unambiguous, global-permission condition.
    throw e;
} catch (UnifiedRepositoryException e) {
    if (!isFoundAndReadable(unifiedRepository, fileId)) {
        // file node not found / unreadable
    } else {
        throw e;
    }
}
```

**`getAllDeletedFiles` / `getReservedChars`** — no disambiguation needed: neither has an
ABS guard, a voter check, nor a documented failure path; inaccessible entries in
`getAllDeletedFiles` are silently filtered by the Jackrabbit session itself, and
`getReservedChars` is pure in-memory data with no repository access at all.
