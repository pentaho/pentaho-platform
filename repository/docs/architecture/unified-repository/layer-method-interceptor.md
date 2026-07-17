---
type: architecture
title: Method Interceptor Layer
description: AOP method-level security enforced by `unifiedRepositoryMethodInterceptor`, including protected methods, ABS actions, and uncovered methods.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# `unifiedRepositoryMethodInterceptor` (AOP method-level security)

This is an `org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor`
that performs **Action-Based Security (ABS)** checks before any method executes.

The access decision manager is `businessAccessDecisionManager`
(`UnanimousBased` with a single `authorizationPolicyVoter`).
The voter evaluates `VOTE_AUTHZ_POLICY_<action>` attributes against the current user's
granted logical roles (stored in JCR, managed via `roleAuthorizationPolicyRoleBindingDao`).

When the check fails the interceptor throws
`org.springframework.security.access.AccessDeniedException`, which is caught by
`ExceptionLoggingDecorator` and re-thrown as `UnifiedRepositoryAccessDeniedException`.

## Protected methods and required ABS actions

The ABS action required is **global** (not per-file): it expresses a system-level
capability, not a per-resource permission.

**Require `org.pentaho.repository.read`**

| Method |
|---|
| `getFile` (all overloads) |
| `getFileById` (all overloads) |
| `getFileAtVersion` |
| `getChildren` (all overloads) |
| `getTree` (all overloads) |
| `getDataForRead` |
| `getDataForReadInBatch` |
| `getDataAtVersionForRead` |
| `getDataForExecute` |
| `getDataForExecuteInBatch` |
| `getDataAtVersionForExecute` |
| `getAcl` |
| `getEffectiveAces` (both overloads) |
| `hasAccess` |
| `canUnlockFile` |
| `getVersionSummary` |
| `getVersionSummaryInBatch` |
| `getVersionSummaries` |
| `getDeletedFiles` (both overloads) |
| `getReferrers` |
| `getFileMetadata` |
| `getAvailableLocalesForFileById` |
| `getAvailableLocalesForFileByPath` |
| `getAvailableLocalesForFile` |
| `getLocalePropertiesForFileById` |
| `getLocalePropertiesForFileByPath` |
| `getLocalePropertiesForFile` |

**Require `org.pentaho.repository.create`**

| Method |
|---|
| `createFile` (both overloads) |
| `createFolder` (both overloads) |
| `updateFile` |
| `updateFolder` |
| `updateAcl` |
| `lockFile` |
| `unlockFile` |
| `restoreFileAtVersion` |
| `deleteFile` (both overloads) |
| `deleteFileAtVersion` |
| `undeleteFile` |
| `moveFile` |
| `copyFile` |
| `setFileMetadata` |
| `setLocalePropertiesForFileById` |
| `setLocalePropertiesForFileByPath` |
| `setLocalePropertiesForFile` |
| `deleteLocalePropertiesForFile` |

## Methods NOT covered by the method interceptor

The following methods have **no ABS check** and therefore no method-level access gate:

| Method | Notes |
|---|---|
| `getReservedChars` | Returns static metadata; no file access involved |
| `getAllDeletedFiles` | Retrieves all deleted files; no ABS guard |

These methods still go through the JCR session (user-credentials-based) and
the `RepositoryAccessVoterManager` at the DAO level.

## ABS action hierarchy (derived rules configured in `repository.spring.xml`)

```
administerSecurity  ──► repositoryCreate  ──► repositoryRead
```

The admin role has all actions via immutable role binding.
The default `Authenticated` role is bootstrapped with both `repositoryRead` and
`repositoryCreate`.

---

