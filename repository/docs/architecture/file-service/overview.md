---
type: architecture
title: FileService Access Control Overview
description: Bean composition and call chain for FileService access-control enforcement, built atop IUnifiedRepository.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# FileService Access Control Analysis

> Companion to [`unified-repository/index.md`](../unified-repository/index.md) (the `IUnifiedRepository` layer) and
> [`unified-repository-exception-disambiguation`](../../reference/unified-repository/exception-disambiguation/index.md) (public-API disambiguation
> strategies for that layer). This document covers the **`FileService`** layer itself —
> its own access-control-relevant logic and exception-handling behavior, on top of the
> `IUnifiedRepository` bean it wraps.
>
> The Generic File System (GFS) `RepositoryFileProvider` is used throughout only as a
> **worked example of a real caller** — it is the concrete illustration for which
> `FileService` operations matter in practice, and for how a caller might mix
> `FileService` calls with direct `IUnifiedRepository` calls. It is not itself the
> subject of this document.
>
> Source files analysed:
> - `FileService.java` (`pentaho-platform/extensions/.../web/http/api/resources/services`) —
>   the class under analysis.
> - `DefaultUnifiedRepositoryWebService.java` (`pentaho-platform/repository/.../unified/webservices`) —
>   `FileService`'s `getRepoWs()` return type.
> - `CopyFilesOperation.java` (`pentaho-platform/extensions/.../web/http/api/resources/operations`) —
>   helper used by `FileService.doCopyFiles`.
> - `RepositoryFileProvider.java` (`pentaho-generic-file-system/impl/.../providers/repository`) —
>   used only as the worked example described above.

---


## Bean composition and call chain

```
RepositoryFileProvider (GFS)
  ├─► unifiedRepository                         (same bean documented in the main doc)
  │     used directly for: getFile, getAcl (owner lookup), hasAccess
  │
  └─► fileService                               (FileService, or CustomFileService subclass)
        used for: doCreateDirSafe, doGetTree, isPathValid, doGetCanGetFileContent,
                   getRepositoryFileInputStream/OutputStream, doGetDeletedFiles,
                   doDeleteFilesPermanent, doDeleteFiles, doRestoreFiles, doGetCanCreate,
                   doesExist, isValidFileName, doRename, doCopyFiles, doMoveFiles,
                   doGetMetadata, doSetMetadata, doGetFileAcl, setFileAcls, getRepository()
        │
        └─► getRepoWs()                          (DefaultUnifiedRepositoryWebService)
              │  DTO-translation pass-through only — see [DefaultUnifiedRepositoryWebService layer](layer-default-unified-repository-web-service.md). No extra access-control
              │  logic, no extra exception handling beyond a few unrelated ETC-folder /
              │  mime-type checks.
              └─► unifiedRepository               (same bean, same rules as main doc)
```

**Key structural fact:** `RepositoryFileProvider` does **not** use a single, consistent API.
Per the class's own comment: *"The file service wraps a unified repository and provides
additional functionality."* For each operation it picks whichever of `fileService` /
`unifiedRepository` best fits — sometimes for convenience (DTOs, ID-encoding, path
validation), sometimes because `FileService` implements extra business logic
(`doGetCanGetFileContent`'s whitelist, `doSetMetadata`'s hidden-flag handling,
`doCreateDirSafe`'s parent auto-creation). This means the **effective exception contract
for a given GFS operation depends on which of the two paths was chosen**, and the two
paths do not always behave the same way for the same not-found/no-access condition (see
[FileService exception disambiguation recipes](../../reference/file-service/exception-disambiguation/index.md)).

`RepositoryFileProvider` also injects a `CustomFileService` subclass instead of a plain
`FileService`, purely to force `getRepoWs()`, `getRepositoryFileInputStream`, and
`getRepositoryFileOutputStream` to use the **specific** `IUnifiedRepository` instance the
provider was constructed with (rather than a static lookup via `PentahoSystem.get(...)`) —
this is a wiring detail, not an access-control difference.

---

