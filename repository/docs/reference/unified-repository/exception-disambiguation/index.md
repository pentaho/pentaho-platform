# Unified Repository Exception Disambiguation

## General Approach

- [Disambiguating IUnifiedRepository Exceptions: General Approach](general-approach.md) - Shared helper functions, the time-of-check race, known gaps, and exception-type notes for public-API-only IUnifiedRepository exception disambiguation.

## Per-Operation Recipes

- [canUnlockFile (No Disambiguation Needed)](can-unlock-file.md) - Why canUnlockFile needs no exception disambiguation - it is a read-only boolean check, not exception-shaped.
- [Disambiguating copyFile](copy-file.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s copyFile operation(s).
- [Disambiguating createFile](create-file.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s createFile operation(s).
- [Disambiguating createFolder](create-folder.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s createFolder operation(s).
- [Disambiguating deleteFileAtVersion](delete-file-at-version.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s deleteFileAtVersion operation(s).
- [Disambiguating deleteFile](delete-file.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s deleteFile operation(s).
- [Disambiguating deleteLocalePropertiesForFile](delete-locale-properties-for-file.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s deleteLocalePropertiesForFile operation(s).
- [Disambiguating getAcl / getEffectiveAces](get-acl.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s getAcl / getEffectiveAces operation(s).
- [Disambiguating getChildren](get-children.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s getChildren operation(s).
- [Disambiguating getFileAtVersion](get-file-at-version.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s getFileAtVersion operation(s).
- [Disambiguating getFileMetadata](get-file-metadata.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s getFileMetadata operation(s).
- [Disambiguating getFile / getFileById / getData* / getAvailableLocalesForFile* / getLocalePropertiesForFile*](get-file.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s getFile / getFileById / getData* / getAvailableLocalesForFile* / getLocalePropertiesForFile* operation(s).
- [Disambiguating getReferrers](get-referrers.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s getReferrers operation(s).
- [Disambiguating getTree](get-tree.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s getTree operation(s).
- [Disambiguating getVersionSummary / getVersionSummaryInBatch / getVersionSummaries](get-version-summary.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s getVersionSummary / getVersionSummaryInBatch / getVersionSummaries operation(s).
- [Disambiguating hasAccess](has-access.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s hasAccess operation(s).
- [Disambiguating lockFile / unlockFile](lock-unlock-file.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s lockFile / unlockFile operation(s).
- [getAllDeletedFiles / getReservedChars (No Disambiguation Needed)](misc-no-disambiguation-needed.md) - Why `getAllDeletedFiles` and `getReservedChars` need no exception disambiguation.
- [Disambiguating moveFile](move-file.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s moveFile operation(s).
- [Disambiguating permanentlyDeleteFile](permanently-delete-file.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s permanentlyDeleteFile operation(s).
- [Disambiguating restoreFileAtVersion](restore-file-at-version.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s restoreFileAtVersion operation(s).
- [Disambiguating setFileMetadata](set-file-metadata.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s setFileMetadata operation(s).
- [Disambiguating setLocalePropertiesForFile*](set-locale-properties-for-file.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s setLocalePropertiesForFile* operation(s).
- [Disambiguating undeleteFile](undelete-file.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s undeleteFile operation(s).
- [Disambiguating updateAcl](update-acl.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s updateAcl operation(s).
- [Disambiguating updateFile](update-file.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s updateFile operation(s).
- [Disambiguating updateFolder](update-folder.md) - Public-API-only disambiguation recipe for `IUnifiedRepository`'s updateFolder operation(s).
