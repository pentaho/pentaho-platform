---
type: architecture
title: DefaultUnifiedRepositoryWebService Layer
description: Role of `DefaultUnifiedRepositoryWebService` (`getRepoWs()`) in the FileService call chain.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# `DefaultUnifiedRepositoryWebService` (`getRepoWs()`)

Role: DTO translation only (`RepositoryFile` ↔ `RepositoryFileDto`, `RepositoryFileAcl` ↔
`RepositoryFileAclDto`, etc.) via `RepositoryFileAdapter`/`RepositoryFileAclAdapter`. Every
method is a one-line call into the injected `IUnifiedRepository` plus adapter conversion.
There is **no `try`/`catch` around any of these calls** (the handful of `catch`/`throw`
statements in the class guard unrelated things — ETC-folder access and mime-type
whitelisting on `createFile`/`updateFile`). Consequently:

> Every exception the main doc documents for `IUnifiedRepository` (`URADE`, `URE` and its 5
> subclasses, and the `NullPointerException` defect on `updateAcl`) propagates through
> `getRepoWs()` completely unchanged. This layer can be treated as transparent for
> access-control purposes.

