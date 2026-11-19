# FileService Architecture

## Overview

- [FileService Access Control Overview](overview.md) - Bean composition and call chain for FileService access-control enforcement, built atop IUnifiedRepository.

## Layers

- [CopyFilesOperation Layer](layer-copy-files-operation.md) - Role of `CopyFilesOperation`, used only by `FileService.doCopyFiles`.
- [DefaultUnifiedRepositoryWebService Layer](layer-default-unified-repository-web-service.md) - Role of `DefaultUnifiedRepositoryWebService` (`getRepoWs()`) in the FileService call chain.
- [FileService Role And General Shape](layer-file-service.md) - FileService's role, general shape, and how ABS-level (coarse, global) denials surface to callers.

## Worked Examples

- [Worked Example: RepositoryFileProvider Exception Mapping](worked-example-repository-file-provider.md) - Worked example of how `RepositoryFileProvider` maps FileService exceptions.

## Design Observations

- [FileService Disambiguation Strategy Design Observations](design-observations.md) - Design observations on why the IUnifiedRepository disambiguation approach needs adaptation at the FileService layer.
