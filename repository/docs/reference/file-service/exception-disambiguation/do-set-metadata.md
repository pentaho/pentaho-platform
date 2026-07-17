---
type: reference
title: Disambiguating doSetMetadata
description: Public-API-only disambiguation recipe for `FileService`'s doSetMetadata operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating doSetMetadata

**`doSetMetadata`** (declared `throws GeneralSecurityException`; has **no** not-found
pre-check at all — a concurrently-missing file produces an uncaught
`NullPointerException`; before its custom access rule even runs, it internally resolves
the file/ACL via `getRepoWs()`, which is still subject to the ABS-level `repository.read`
check — so an unchecked `URADE` can also propagate; the rule itself is a custom one, not
`hasAccess(ACL_MANAGEMENT)` — see "Known gaps" above):

```java
// Pre-check existence yourself; doSetMetadata() gives nothing to catch for this:
if (fileService.getRepoWs().getFile(FileUtils.idToPath(pathId)) == null) {
    // not found / not readable — must be checked BEFORE calling doSetMetadata(),
    // there is no post-hoc signal to recover this from (it would surface as an
    // uncaught NullPointerException instead)
}

try {
    fileService.doSetMetadata(pathId, metadata);
} catch (UnifiedRepositoryAccessDeniedException e) {
    // ABS-level only: doSetMetadata's OWN internal getFile()/getAcl() calls (used to
    // resolve the file/owner before its custom rule even runs) require repository.read —
    // if that ABS action is denied, URADE propagates here unchecked, BEFORE the custom
    // ACL-management rule is ever evaluated. Not related to GeneralSecurityException below.
} catch (GeneralSecurityException e) {
    // doSetMetadata's own custom rule failed. To find out WHICH branch of that rule
    // failed, reimplement it exactly (FileService.doSetMetadata source):
    RepositoryFileAcl acl = unifiedRepository.getAcl(fileId);
    boolean isOwner = acl != null && currentUserName.equals(acl.getOwner().getName());
    boolean hasAdminTriple = policy.isAllowed(RepositoryReadAction.NAME)
        && policy.isAllowed(RepositoryCreateAction.NAME)
        && policy.isAllowed(AdministerSecurityAction.NAME);
    if (isOwner) {
        // Owner — should have passed; if we're here, a race changed ownership/ACL
        // between doSetMetadata's own check and this follow-up.
    } else if (hasAdminTriple) {
        // Has the admin triple — should also have passed; same race caveat as above.
    } else {
        // Neither: check the ACL's own ACEs for an explicit ACL_MANAGEMENT/ALL grant to
        // the current user — doSetMetadata resolves EFFECTIVE aces if the ACL inherits.
        List<RepositoryFileAce> aces = acl.isEntriesInheriting()
            ? unifiedRepository.getEffectiveAces(fileId)
            : acl.getAces();
        boolean hasExplicitGrant = aces.stream().anyMatch(ace ->
            ace.getSid().equals(currentUserSid)
                && (ace.getPermissions().contains(RepositoryFilePermission.ACL_MANAGEMENT)
                    || ace.getPermissions().contains(RepositoryFilePermission.ALL)));
        if (!hasExplicitGrant) {
            // Confirms: genuinely denied — none of the three rule branches pass.
        } else {
            // Grant found now but rule still failed originally: a race.
        }
    }
}
```
