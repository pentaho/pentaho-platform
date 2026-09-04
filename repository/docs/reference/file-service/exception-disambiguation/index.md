# FileService Exception Disambiguation

## General Approach

- [Disambiguating FileService Exceptions: General Approach](general-approach.md) - Exception/error package legend, shared helpers, the time-of-check race, and known gaps for public-API-only FileService exception disambiguation.

## Per-Operation Recipes

- [Disambiguating doCopyFiles](do-copy-files.md) - Public-API-only disambiguation recipe for `FileService`'s doCopyFiles operation(s).
- [Disambiguating doCreateDirSafe](do-create-dir-safe.md) - Public-API-only disambiguation recipe for `FileService`'s doCreateDirSafe operation(s).
- [Disambiguating doDeleteFiles / doDeleteFilesPermanent](do-delete-files.md) - Public-API-only disambiguation recipe for `FileService`'s doDeleteFiles / doDeleteFilesPermanent operation(s).
- [doGetDeletedFiles (No Disambiguation Needed)](do-get-deleted-files.md) - Why doGetDeletedFiles needs no exception disambiguation - inaccessible entries are silently filtered.
- [Disambiguating doGetFileAcl](do-get-file-acl.md) - Public-API-only disambiguation recipe for `FileService`'s doGetFileAcl operation(s).
- [Disambiguating doGetMetadata](do-get-metadata.md) - Public-API-only disambiguation recipe for `FileService`'s doGetMetadata operation(s).
- [Disambiguating doGetTree](do-get-tree.md) - Public-API-only disambiguation recipe for `FileService`'s doGetTree operation(s).
- [Disambiguating doMoveFiles](do-move-files.md) - Public-API-only disambiguation recipe for `FileService`'s doMoveFiles operation(s).
- [Disambiguating doRename](do-rename.md) - Public-API-only disambiguation recipe for `FileService`'s doRename operation(s).
- [Disambiguating doRestoreFiles](do-restore-files.md) - Public-API-only disambiguation recipe for `FileService`'s doRestoreFiles operation(s).
- [Disambiguating doSetMetadata](do-set-metadata.md) - Public-API-only disambiguation recipe for `FileService`'s doSetMetadata operation(s).
- [Disambiguating doesExist / isFolder / doGetIsVisible](does-exist-is-folder.md) - Public-API-only disambiguation recipe for `FileService`'s doesExist / isFolder / doGetIsVisible operation(s).
- [getRepository (Raw IUnifiedRepository Access)](get-repository.md) - How getRepository exposes the raw IUnifiedRepository for direct calls that bypass FileService's own exception handling.
- [Disambiguating hasAccess](has-access.md) - Public-API-only disambiguation recipe for `FileService`'s hasAccess operation(s).
- [Disambiguating setFileAcls](set-file-acls.md) - Public-API-only disambiguation recipe for `FileService`'s setFileAcls operation(s).
