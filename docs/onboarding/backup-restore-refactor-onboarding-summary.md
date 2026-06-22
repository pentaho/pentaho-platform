# Backup/Restore Refactor Onboarding Summary

This document summarizes the major changes made across the backup/restore refactor files on branch `backup-restore-refactor`.

## Overall architecture shift

The codebase is moving toward a **helper-based backup/restore architecture**.

- `FileService` orchestrates backup and restore requests.
- `PentahoPlatformExporter` orchestrates export helpers.
- `SolutionImportHandler` orchestrates import helpers.
- Specialized helpers own component-specific behavior for:
  - repository content
  - users and roles
  - metadata
  - Mondrian
  - metastore
  - locale handling

Major themes across the changes:

- selective backup and restore support
- improved path decoding and backward compatibility
- improved schedule dependency handling
- better ACL and ownership preservation during restore
- metrics and logging improvements
- separation of concerns between orchestration and component logic

---

## 1. `extensions/src/main/java/org/pentaho/platform/plugin/services/importer/SolutionImportHandler.java`

### Summary
`SolutionImportHandler` has been refactored from a mostly monolithic importer into a restore orchestrator.

### Key changes
- introduced helper-based import registration and execution
- added selective restore awareness using `ImportSession` component overrides
- added metrics collection and final metrics reporting
- improved path normalization to support encoded ZIP entry names
- added schedule dependency import support via bundle lookup
- added schedule owner user import delegation
- updated compatibility delegation methods to pass handler context explicitly

### Why it matters
This class is now the central coordinator for restore behavior rather than the sole owner of all restore logic.

---

## 2. `extensions/src/main/java/org/pentaho/platform/plugin/services/importer/helper/UsersAndRolesImportHelper.java`

### Summary
User and role restore logic was extracted from `SolutionImportHandler` into a dedicated helper.

### Key changes
- imports users, roles, user settings, and global user settings
- supports selective restore via `isIncludeUsers()`
- explicitly propagates `SolutionImportHandler` through import call chains
- ensures home folders are created or verified using `ITenantManager`
- adds `importScheduleOwnerUser(...)` to restore schedule owners and their roles
- adds more detailed restore logging and metrics

### Why it matters
This helper is now the primary place for user/role restore logic and fixes earlier restore issues around missing handler context and missing home folders.

---

## 3. `extensions/src/main/java/org/pentaho/platform/plugin/services/importer/LocaleImportHandler.java`

### Summary
Locale restore logic was updated to better handle encoded paths in backup bundles.

### Key changes
- decodes locale paths before repository lookup
- tries decoded path first and original path second
- supports both newer URL-encoded backups and older literal-path backups

### Why it matters
This improves backward compatibility and fixes locale restore failures caused by path encoding differences.

---

## 4. `extensions/src/main/java/org/pentaho/platform/plugin/services/importer/RepositoryFileImportFileHandler.java`

### Summary
Core repository file import logic was enhanced to decode paths and support fallback lookup behavior.

### Key changes
- decodes ZIP entry path and file name before repository operations
- updates bundle path/name to decoded values for consistency
- falls back to original path/name if decoded lookup fails
- preserves ACL and metadata application behavior
- improves parent/root lookup behavior

### Why it matters
This class is a low-level import executor, so its path-handling improvements are essential for making restore work correctly across old and new backup formats.

---

## 5. `extensions/src/main/java/org/pentaho/platform/plugin/services/importer/SolutionImportHandler.java.bak`

### Summary
This appears to be a backup snapshot of an intermediate `SolutionImportHandler` implementation.

### Key changes
- preserves an earlier version of `SolutionImportHandler`
- appears to predate some of the final handler-propagation and path-matching changes

### Why it matters
This is most likely a development artifact and should be reviewed before merge to determine whether it should be removed.

---

## 6. `extensions/src/main/java/org/pentaho/platform/web/http/api/resources/services/FileService.java`

### Summary
`FileService` now acts as the service-layer entry point for backup and restore orchestration.

### Key changes
- formalized full system backup flow in `systemBackup(...)`
- formalized full system restore flow in `systemRestore(...)`
- added selective backup support in `selectiveBackup(...)`
- added selective restore support in `selectiveRestore(...)`
- validates log file paths and uses fallback log files when needed
- stores component overrides on `ImportSession` for selective restore filtering
- explicitly clears `ImportSession` component overrides after restore completes
- updated filename decoding to use `ExportFileNameEncoder`

### Why it matters
This file is now the top-level bridge between REST/CLI requests and the importer/exporter orchestration layer.

---

## 7. `extensions/src/main/java/org/pentaho/platform/plugin/services/exporter/PentahoPlatformExporter.java`

### Summary
`PentahoPlatformExporter` has become the export-side orchestration layer for helper-based backup.

### Key changes
- registers built-in export helpers
- supports selective export via `ComponentConfig`
- added `runAllExportHelpers()`
- added `exportFileByPath(...)` for dependency-aware export
- added metrics, inventory, and summary logging
- added public helper-facing accessors for shared exporter state
- added generated content filtering using `lineage-id`
- preserved deprecated stub methods for backward compatibility
- added `getFixedZipEntryName(...)` for helper use

### Why it matters
This class now fills the same orchestration role on export that `SolutionImportHandler` fills on import.

---

## 8. `extensions/src/main/java/org/pentaho/platform/plugin/services/exporter/helper/RepositoryContentExportHelper.java`

### Summary
Repository content export was simplified and aligned with the exporter helper model.

### Key changes
- now depends only on exporter rather than repository-specific state
- respects `isIncludeContent()` backup profile selection
- delegates traversal/export logic to exporter APIs
- improves root folder ZIP entry handling

### Why it matters
This reduces duplicated traversal logic and keeps content export aligned with exporter orchestration.

---

## 9. `extensions/src/main/java/org/pentaho/platform/plugin/services/importer/helper/RepositoryFilesImportHelper.java`

### Summary
Repository content restore logic is now centralized in a dedicated helper with selective restore awareness.

### Key changes
- filters on `isIncludeContent()`
- imports manifest folder entities before files to preserve ACLs and ownership
- decodes bundle names and paths
- skips generated content when requested
- skips metadata `.xmi` files if datasources are excluded
- processes locale files after file import
- records file import metrics

### Why it matters
This helper is the main content restore engine and is key to preserving structure, ACLs, and correct selective restore behavior.

---

## 10. `extensions/src/main/java/org/pentaho/platform/plugin/services/exporter/helper/MetadataExportHelper.java`

### Summary
Metadata export is now encapsulated as a self-contained helper.

### Key changes
- filters on `isIncludeDatasources()`
- removed unnecessary stored component config field
- exports metadata model files into the backup ZIP
- records metrics for metadata export

### Why it matters
This simplifies metadata export logic and keeps helper behavior consistent with the new architecture.

---

## 11. `extensions/src/main/java/org/pentaho/platform/plugin/services/exporter/helper/MetastoreExportHelper.java`

### Summary
Metastore export was consolidated into a dedicated helper.

### Key changes
- lazy-loads and caches metastore locally
- exports metastore to temp XML metastore, zips it, and adds it to backup
- records export metrics
- removed unnecessary stored component config field

### Why it matters
This makes metastore export logic self-contained and easier to reason about.

---

## 12. `extensions/src/main/java/org/pentaho/platform/plugin/services/exporter/helper/MondrianExportHelper.java`

### Summary
Mondrian export is now owned by a dedicated helper.

### Key changes
- filters on `isIncludeMondrian()`
- exports catalog schema files and annotations
- writes Mondrian metadata into the manifest
- removed unnecessary stored component config field
- records export metrics

### Why it matters
This keeps Mondrian-specific export logic out of the exporter core and aligns it with the helper model.

---

## 13. `extensions/src/main/java/org/pentaho/platform/plugin/services/exporter/helper/UsersAndRolesExportHelper.java`

### Summary
User and role export logic is now encapsulated as a dedicated helper.

### Key changes
- filters on `isIncludeUsers()`
- exports users, user settings, global settings, and roles
- adds `exportUserAndRole(...)` for dependency-aware export
- adds `exportScheduleOwnersAndRoles(...)` for schedule-specific dependency export
- records user and role export metrics

### Why it matters
This helper is the export-side counterpart to `UsersAndRolesImportHelper` and enables more targeted user/role export scenarios.

---

## Recommended mental model for a new engineer

A good way to understand the refactor is:

### Request/service layer
- `FileService`
  - validates inputs
  - sets log behavior
  - creates backup/restore request context
  - pushes selective restore state into `ImportSession`

### Export orchestration
- `PentahoPlatformExporter`
  - manages export lifecycle
  - runs component-specific export helpers
  - tracks metrics and inventory

### Import orchestration
- `SolutionImportHandler`
  - preprocesses bundle contents
  - manages helper execution
  - coordinates restore-specific logic like schedules and path normalization

### Component-specific helpers
- content: `RepositoryContentExportHelper`, `RepositoryFilesImportHelper`
- users/roles: `UsersAndRolesExportHelper`, `UsersAndRolesImportHelper`
- metadata: `MetadataExportHelper`
- Mondrian: `MondrianExportHelper`
- metastore: `MetastoreExportHelper`
- locale-specific import behavior: `LocaleImportHandler`

---

## Important risks / review points

- `SolutionImportHandler.java.bak` may be accidental and should be reviewed before merge.
- Path encoding/decoding changes are critical and should be regression tested with:
  - older backups with literal spaces
  - newer backups with URL-encoded names
  - schedule-linked content paths
- Selective restore relies on `ImportSession` state, so session cleanup is important.
- ACL and ownership behavior should be verified for:
  - manifest-created folders
  - restored repository files
  - schedule dependencies
  - user home folders

---

## Short reusable summary

This refactor restructures backup and restore into a helper-based architecture. `FileService` orchestrates backup/restore requests, `PentahoPlatformExporter` orchestrates export helpers, and `SolutionImportHandler` orchestrates import helpers. Specialized helpers now handle users/roles, repository content, metadata, Mondrian, metastore, and locale logic. The key technical goals are selective backup/restore, backward-compatible path handling, better schedule dependency handling, and improved ACL/ownership fidelity.
