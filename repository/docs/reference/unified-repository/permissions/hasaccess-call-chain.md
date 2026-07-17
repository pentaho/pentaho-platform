---
type: reference
title: IUnifiedRepository hasAccess Call Chain
description: The full call chain from DefaultUnifiedRepository.hasAccess() down to the native Jackrabbit privilege check.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Summary: Full Call Chain for `hasAccess`

```
DefaultUnifiedRepository.hasAccess(path, permissions)
  └─ JcrRepositoryFileAclDao.hasAccess(relPath, permissions)
       └─ [JCR session callback]
            ├─ DefaultPermissionConversionHelper.pentahoPermissionsToPrivileges(session, permissions)
            │    └─ looks up permissionEnumToPrivilegeNamesMap
            │    └─ calls session.getAccessControlManager().privilegeFromName(privName)
            │    └─ returns Privilege[]
            └─ session.getAccessControlManager().hasPrivileges(absPath, Privilege[])
                 └─ Jackrabbit walks JCR node tree, resolving inherited ACLs
                 └─ returns boolean
```

