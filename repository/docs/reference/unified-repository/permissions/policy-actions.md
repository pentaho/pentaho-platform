---
type: reference
title: IUnifiedRepository Layer 1 Policy Actions
description: System-level, action-based policy checks (IAuthorizationPolicy) performed by high-level callers before touching the repository, distinct from the per-file ACL checks.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Layer 1 Policy Actions (IAuthorizationPolicy)

These are role-based policy checks, separate from per-file ACLs. Checked before any repository operation in high-level callers (e.g., `DashboardRenderer`).

| Action | Meaning |
|---|---|
| `RepositoryCreateAction.NAME` | User can create/edit repository content |

Configured via Pentaho security roles. Implementation: `IAuthorizationPolicy.isAllowed(String actionName)`.

## Relationship To The Method-Interceptor's ABS Check

This is a **caller-side** check: it runs in the caller (e.g. `DashboardRenderer`) before the
repository is invoked at all, and it is action-based rather than per-file. It is distinct
from the **internal** ABS (action-based-security) check performed by
[`RepositoryFilePermissionInterceptor`](../../../architecture/unified-repository/layer-method-interceptor.md)
for every protected `IUnifiedRepository` method call — that check maps each method to a
required `IAuthorizationAction` and is enforced regardless of which caller invokes the
method. A caller can pass this Layer 1 policy check and still be denied by the
method-interceptor's ABS check (or vice versa), since they evaluate different actions at
different points in the call chain.
