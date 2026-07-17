---
type: reference
title: Disambiguating setFileAcls
description: Public-API-only disambiguation recipe for `FileService`'s setFileAcls operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating setFileAcls

**`setFileAcls`** (declared `throws FileNotFoundException` via an explicit pre-check;
`UnifiedRepositoryAccessDeniedException` from the underlying `updateAcl()` propagates
unchecked — this is the **one** method in this document where `URADE` is genuinely
ambiguous between ABS-level and per-file, mirroring the main doc's `updateAcl` row/snippet
exactly, since `setFileAcls` is a thin wrapper around it):

```java
try {
    fileService.setFileAcls(pathId, acl);
} catch (FileNotFoundException e) {
    // setFileAcls's own explicit pre-check — unambiguous.
} catch (UnifiedRepositoryAccessDeniedException e) {
    // Could be the coarse ABS-level "repository.create" action check, OR the
    // file-specific ACL_MANAGEMENT gate in DefaultUnifiedRepository — both throw the
    // same class (main doc's updateAcl row: the sole exception to "URADE is always
    // ABS-level" in this whole document). Confirm which one with a follow-up call:
    RepositoryFile f = fileService.getRepoWs().getFile(FileUtils.idToPath(pathId));
    if (f != null && !canManageAcl(unifiedRepository, f.getPath())) {
        // file exists/readable but caller lacks ACL_MANAGEMENT on it — matches the
        // file-specific gate. Note the Owner-ACE gap (main doc [per-node JCR privilege requirements and Magic ACE caveats](../../../architecture/unified-repository/layer-jcr-repository-file-dao.md#per-node-jcr-privilege-requirements-and-magic-ace-caveats)): file owners
        // are NOT exempt from this check.
    } else {
        // more likely the coarse ABS-level check failed instead — this is a global
        // permission, not a per-file one, and cannot be confirmed with a follow-up call.
    }
}
```
