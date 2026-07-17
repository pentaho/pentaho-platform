---
type: reference
title: IUnifiedRepository Permission Model Key Classes
description: Quick-reference table of the classes that implement the permission model, their location, and their role.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Key Classes Reference

| Class | Location | Role |
|---|---|---|
| `RepositoryFilePermission` | `api/.../repository2/unified/` | Permission enum (READ/WRITE/DELETE/ACL_MANAGEMENT/ALL) |
| `IPentahoJCRPrivilege` | `api/.../repository2/unified/` | Custom JCR privilege constant (`pho:aclManagement`) |
| `DefaultUnifiedRepository` | `repository/.../unified/` | `IUnifiedRepository` impl; entry point; enforces `updateAcl` check |
| `JcrRepositoryFileDao` | `repository/.../unified/jcr/` | JCR file operations; enforces per-op `accessVoterManager` checks |
| `JcrRepositoryFileAclDao` | `repository/.../unified/jcr/` | JCR ACL operations; implements `hasAccess()` via JCR `AccessControlManager` |
| `DefaultPermissionConversionHelper` | `repository/.../unified/jcr/` | Maps `RepositoryFilePermission` ↔ JCR `Privilege` |
| `JcrRepositoryFileAclUtils` | `repository/.../unified/jcr/` | ACL metadata, inheritance resolution, ACE expansion |
| `IAccessVoterManager` | `repository/.../unified/jcr/` | Plugin voter interface |

