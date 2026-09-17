---
type: reference
title: IUnifiedRepository Permission Enum To JCR Privilege Mapping
description: How the Pentaho RepositoryFilePermission enum (READ/WRITE/DELETE/ACL_MANAGEMENT/ALL) maps to native JCR privileges.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Pentaho Permission Enum → JCR Privileges

Defined in `RepositoryFilePermission`. Mapped in `DefaultPermissionConversionHelper.initMaps()`.

| `RepositoryFilePermission` | JCR Privileges |
|---|---|
| `READ` | `jcr:read`, `jcr:readAccessControl` |
| `WRITE` | `jcr:addChildNodes`, `jcr:removeChildNodes`, `jcr:modifyProperties`, `jcr:nodeTypeManagement`, `jcr:modifyAccessControl`, `jcr:versionManagement`, `jcr:lockManagement` |
| `DELETE` | `jcr:removeNode` |
| `ACL_MANAGEMENT` | `{http://www.pentaho.org/jcr/2.0}aclManagement` (custom Pentaho privilege) |
| `ALL` | `jcr:all` + `pho:aclManagement` |

> **Note on DELETE:** Comment in source says `jcr:removeNode` was also required for WRITE, so DELETE was considered redundant if you had WRITE. The enum still exists and is used in delete checks.

