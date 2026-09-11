---
type: reference
title: IUnifiedRepository Access Control Summary Table
description: Per-method summary of enforcement layer, required action, and exception outcomes for IUnifiedRepository.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Summary table – per method

Columns:
- **ABS action**: required global action checked by `unifiedRepositoryMethodInterceptor` _before_ the method runs.
- **ABS denial**: exception thrown when the ABS check fails. Applies to every call regardless of which file is involved.
- **`accessVoterManager` check**: explicit Pentaho-layer voter check inside the DAO. *No-op in default config (no voters).*
- **Not-found-or-no-read-access**: what the caller gets when the target node (or, where noted, a related node such as a parent/destination) does not exist **or** the user has no `jcr:read` privilege on it. Per [Jackrabbit native JCR session ACL enforcement](../../architecture/unified-repository/layer-jcr-repository-file-dao.md#jackrabbit-native-jcr-session-acl-enforcement)/[JcrTemplate exception translation layer](../../architecture/unified-repository/layer-jcr-template-exception-translation.md), JCR makes these two cases indistinguishable; some DAO methods swallow this internally (silent `null`), others let it propagate and it is translated by `JcrTemplate` into a generic `URE`.
- **No-write-or-delete-access**: what the caller gets when the target node **does** exist and is readable, but the user lacks the JCR privilege required to write/delete/lock/version it (or the Pentaho `accessVoterManager`/`aclDao` pre-check denies it).

`URADE` = `UnifiedRepositoryAccessDeniedException`  
`URE` = `UnifiedRepositoryException` (generic)  
`AccessDeniedException` (JCR) = `javax.jcr.AccessDeniedException`  
`PathNotFoundException` = `javax.jcr.PathNotFoundException`  
`ItemNotFoundException` = `javax.jcr.ItemNotFoundException`  
`DataRetrievalFailureException` = `org.springframework.dao.DataRetrievalFailureException` (the wrapper used for uncaught JCR not-found exceptions)

> Uncaught `PathNotFoundException`/`ItemNotFoundException` follow the
> `URE` → `DataRetrievalFailureException` → JCR exception chain. JCR
> `AccessDeniedException` follows a different Pentaho-specific chain and becomes `URADE`.

| Method | ABS action | ABS denial | `accessVoterManager` check | Not-found-or-no-read-access | No-write-or-delete-access |
|---|---|---|---|---|---|
| `getFile` / `getFileById` (all overloads) | `repository.read` | `URADE` | READ on file → **`null`** if voter denies | <ul><li>`PathNotFoundException` / `ItemNotFoundException` caught inside the DAO callback → **`null`** (no exception; confounded with not-found)</li></ul> | N/A (read-only) |
| `getFileAtVersion` | `repository.read` | `URADE` | None | <ul><li>`ItemNotFoundException` **not** caught → propagates → `URE` (cause: `DataRetrievalFailureException` → `ItemNotFoundException`)</li></ul> | N/A (read-only) |
| `getChildren` | `repository.read` | `URADE` | None | <ul><li>**Folder itself** not found/no-read: `ItemNotFoundException` not caught (`session.getNodeByIdentifier`) → `URE`</li><li>**Individual children** not readable: Jackrabbit silently omits them from `folderNode.getNodes()` — no exception, entry simply absent from the returned list</li></ul> | N/A (read-only) |
| `getTree` | `repository.read` | `URADE` | READ on each visited node → excluded (no-op by default) | <ul><li>**Root path** not found/no-read: `PathNotFoundException` not caught (`session.getItem` in `getTree()`) → `URE`</li><li>**Descendant nodes**: voter check (no-op) + Jackrabbit silently omits unreadable children — no exception, node simply absent from the tree</li></ul> | N/A (read-only) |
| `getData*` (`getDataForRead`, `getDataAtVersionForRead`, `getDataForExecute`, `getDataAtVersionForExecute`, batch variants) | `repository.read` | `URADE` | READ on file (via `internalGetFileById`) → **`null`** if voter denies | <ul><li>`PathNotFoundException` / `ItemNotFoundException` caught inside `internalGetFileById` → **`null`** (confounded with not-found)</li></ul> | N/A (read-only) |
| `getAcl` / `getEffectiveAces` | `repository.read` | `URADE` | None | <ul><li>Node not found/no `jcr:read`: uncaught `ItemNotFoundException` → `URE`</li><li>Node readable but caller lacks `jcr:readAccessControl`: JCR `AccessDeniedException` → `URADE`</li></ul> | N/A (no write involved) |
| `hasAccess` | `repository.read` | `URADE` | None (is the check itself) | <ul><li>`PathNotFoundException` caught → returns `false` (no exception, for **any** permission set requested)</li></ul> | <ul><li>Returns `false` (same mechanism; `hasAccess` does not distinguish READ from WRITE/DELETE in its not-found handling)</li></ul> |
| `createFile` | `repository.create` | `URADE` | WRITE on **parent** → **`null`** if voter denies | <ul><li>Parent not found/no-read: `getFileById(parentId)` → `null`; voter check skipped; JCR node lookup throws uncaught `ItemNotFoundException` → `UnifiedRepositoryCreateFileException`</li></ul> | <ul><li>Custom WRITE voter denial → `null` (no-op by default)</li><li>Native `jcr:addChildNodes` denial → JCR `AccessDeniedException` → `URADE`; converter-map priority prevents the fallback `UnifiedRepositoryCreateFileException`</li></ul> |
| `createFolder` | `repository.create` | `URADE` | WRITE on **parent** → **`null`** if voter denies | <ul><li>Same not-found path as `createFile`, but no method-specific fallback: uncaught `ItemNotFoundException` → generic `URE`</li></ul> | <ul><li>Custom WRITE voter denial → `null` (no-op by default)</li><li>Native `jcr:addChildNodes` denial → JCR `AccessDeniedException` → `URADE`</li></ul> |
| `updateFile` | `repository.create` | `URADE` | WRITE on file → **`null`** if voter denies | <ul><li>If file vanished, uncaught `ItemNotFoundException` from ACL lookup → `UnifiedRepositoryUpdateFileException`</li></ul> | <ul><li>Custom WRITE voter denial → `null` (no-op by default)</li><li>Native write denial → JCR `AccessDeniedException` → `URADE`; converter-map priority prevents fallback wrapper</li></ul> |
| `updateFolder` | `repository.create` | `URADE` | **None** | <ul><li>Uncaught `ItemNotFoundException` if node vanished → generic `URE`</li></ul> | <ul><li>Native write denial → JCR `AccessDeniedException` → `URADE`</li></ul> |
| `updateAcl` | `repository.create` | `URADE` | None in DAO; explicit `hasAccess(ACL_MANAGEMENT)` in `DefaultUnifiedRepository` | <ul><li>`getFileById(acl.getId())` returns `null` if not found/no-read → **`NullPointerException`** at `file.getPath()` in `DefaultUnifiedRepository.updateAcl()` (uncaught programming defect, not a Pentaho/JCR access exception)</li></ul> | <ul><li>`hasAccess(ACL_MANAGEMENT)` returns `false` → `URADE` thrown **directly** by `DefaultUnifiedRepository` (no `Throwable` cause at all — not routed through `ExceptionLoggingDecorator`'s converter map)</li><li>**Owner-ACE gap** ([per-node JCR privilege requirements and Magic ACE caveats](../../architecture/unified-repository/layer-jcr-repository-file-dao.md#per-node-jcr-privilege-requirements-and-magic-ace-caveats)): the file's owner always passes every JCR write/delete check via the injected `jcr:all` owner ACE, but that ACE does **not** include `pho:aclManagement` — so an owner can still get `URADE` here unless separately granted `ACL_MANAGEMENT`</li></ul> |
| `deleteFile` (soft) | `repository.create` | `URADE` | DELETE → **`null`** if voter denies | <ul><li>Not-found/no-read falls through to uncaught `ItemNotFoundException`/`PathNotFoundException` → `URE`</li></ul> | <ul><li>Custom DELETE voter denial → `null` (no-op by default)</li><li>Explicit file DELETE denial or native denial on source parent/`.trash` → JCR `AccessDeniedException` → `URADE`</li></ul> |
| `deleteFileAtVersion` | `repository.create` | `URADE` | DELETE → **`null`** if voter denies | <ul><li>Not-found falls through to uncaught `ItemNotFoundException` → `URE`</li></ul> | <ul><li>Custom DELETE voter denial → `null`</li><li>Native remove denial → JCR `AccessDeniedException` → `URADE`</li></ul> |
| `permanentlyDeleteFile` | `repository.create` | `URADE` | DELETE → **`null`** if voter denies | <ul><li>Not-found falls through to uncaught `ItemNotFoundException` → `URE`</li></ul> | <ul><li>Custom DELETE voter denial → `null`</li><li>Native delete/source-parent denial → JCR `AccessDeniedException` → `URADE`</li><li>Live references → `UnifiedRepositoryReferentialIntegrityException`</li></ul> |
| `undeleteFile` | `repository.create` | `URADE` | WRITE → **`null`** if voter denies | <ul><li>Deleted node not found/no-read: uncaught `ItemNotFoundException` → `URE`</li></ul> | <ul><li>Custom WRITE voter denial → `null`</li><li>Native move denial → JCR `AccessDeniedException` → `URADE`</li><li>Target exists → `UnifiedRepositoryFileExistsException`</li></ul> |
| `moveFile` (incl. rename) | `repository.create` | `URADE` | WRITE on source + destination | <ul><li>Source not found/no-read: uncaught `ItemNotFoundException` → `URE`</li><li>Destination parent missing/unreadable: `IllegalArgumentException` → `URE`</li></ul> | <ul><li>Explicit source/destination WRITE denial or native workspace/source-parent denial → JCR `AccessDeniedException` → `URADE`</li></ul> |
| `copyFile` | `repository.create` | `URADE` | WRITE on **destination only** | <ul><li>Source not found/no-read: uncaught `ItemNotFoundException` → `URE`</li><li>Destination parent missing/unreadable: `IllegalArgumentException` → `URE`</li></ul> | <ul><li>Explicit destination WRITE denial or native workspace denial → JCR `AccessDeniedException` → `URADE`</li><li>Source write is not checked</li></ul> |
| `lockFile` / `unlockFile` | `repository.create` | `URADE` | **None** | <ul><li>Uncaught `ItemNotFoundException` → `URE`</li></ul> | <ul><li>JCR lock/unlock denial → `URADE`</li></ul> |
| `canUnlockFile` | `repository.read` | `URADE` | **None** | <ul><li>`ItemNotFoundException` if node inaccessible (uncaught) → `URE`</li></ul> | N/A (read-only check) |
| `getVersionSummary` / `getVersionSummaryInBatch` / `getVersionSummaries` | `repository.read` | `URADE` | **None** | <ul><li>Uncaught `ItemNotFoundException` → `URE`</li><li>JCR `AccessDeniedException` for a protected version node → `URADE`</li></ul> | N/A (read-only) |
| `restoreFileAtVersion` | `repository.create` | `URADE` | **None** | <ul><li>Uncaught `ItemNotFoundException` → `URE`</li></ul> | <ul><li>JCR version-restore denial → `URADE`</li></ul> |
| `setFileMetadata` | `repository.create` | `URADE` | **None** | <ul><li>Uncaught `ItemNotFoundException`/`PathNotFoundException` → `URE`</li></ul> | <ul><li>Native write denial → `URADE`</li><li>Malformed metadata key → `UnifiedRepositoryMalformedNameException`</li></ul> |
| `getFileMetadata` | `repository.read` | `URADE` | **None** | <ul><li>`ItemNotFoundException`/`PathNotFoundException` if node inaccessible (uncaught) → `URE`</li></ul> | N/A (read-only) |
| `getAvailableLocalesForFile*` | `repository.read` | `URADE` | READ (via `getFileById`) → **`null`** if voter denies | <ul><li>`PathNotFoundException`/`ItemNotFoundException` caught inside `getFileById` → **`null`** (confounded with not-found; downstream logic on `null` input just returns an empty list)</li></ul> | N/A (read-only) |
| `getLocalePropertiesForFile*` | `repository.read` | `URADE` | READ (via `getFileById`) → **`null`** if voter denies | <ul><li>Same as above: **`null`** input from `getFileById`, method returns `null` (data read from the in-memory `RepositoryFile`, no further JCR call)</li></ul> | N/A (read-only) |
| `setLocalePropertiesForFile*` | `repository.create` | `URADE` | READ on file (via `getFileById`) → **`null`** if voter denies; no WRITE voter | <ul><li>Caught `PathNotFoundException`/`ItemNotFoundException` → `null`</li></ul> | <ul><li>Native write denial → `URADE`</li></ul> |
| `deleteLocalePropertiesForFile` | `repository.create` | `URADE` | **None** | <ul><li>Uncaught `ItemNotFoundException`/`PathNotFoundException` → `URE`</li></ul> | <ul><li>Native write denial → `URADE`</li></ul> |
| `getReferrers` | `repository.read` | `URADE` | **None** | <ul><li>`ItemNotFoundException` if file node inaccessible (uncaught) → `URE`</li></ul> | N/A (read-only, though it internally does an unconditional `session.save()`) |
| `getAllDeletedFiles` | *(none — no ABS guard)* | — | **None** | <ul><li>Jackrabbit session filtering; inaccessible entries silently excluded — no exception</li></ul> | N/A |
| `getReservedChars` | *(none — no ABS guard)* | — | **None** | None (pure in-memory) | N/A |

---
