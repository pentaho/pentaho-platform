---
type: architecture
title: Unified Repository Access Control Design Observations
description: Key design observations explaining IUnifiedRepository access-control behavior.
status: active
timestamp: 2026-08-07T00:00:00Z
---

# Key design observations

## Not-found confounding comes from Jackrabbit

In default configuration `accessVoterManager` has no voters and is a no-op. For reads,
Jackrabbit hides inaccessible nodes using `PathNotFoundException` or
`ItemNotFoundException`. DAO helpers that catch those exceptions return `null`, making
"missing" and "unreadable" intentionally indistinguishable.

## Jackrabbit is the universal per-resource enforcement layer

DAO methods use a JCR session opened with current-user credentials. Jackrabbit enforces
native ACLs on every call, independent of ABS method security and Pentaho's optional
access-voter extension point.

## JCR write denials surface as `UnifiedRepositoryAccessDeniedException`

Native mutation denial throws `javax.jcr.AccessDeniedException`.
`PentahoJcrTemplate` deliberately converts it to Spring Security
`AccessDeniedException`, and `ExceptionLoggingDecorator` converts that to
`UnifiedRepositoryAccessDeniedException`.

This makes JCR write/delete/lock/version denial distinguishable by outer type from an
uncaught not-found/no-read failure, which instead follows the base
`DataRetrievalFailureException` path into generic or method-specific
`UnifiedRepositoryException`.

`UnifiedRepositoryAccessDeniedException` itself remains ambiguous between:

- missing ABS action, and
- native JCR permission denial on a resource.

The nested JCR cause can identify the latter diagnostically, but cause-chain shape is not a
public API contract. Public-API recipes use follow-up ABS and resource checks instead.

## `accessVoterManager` is an extension point

The shipped configuration supplies no voters. Plugins can register voters to impose
additional restrictions. Several DAO methods return `null` when a custom voter denies
instead of reaching Jackrabbit; this silent path is separate from native JCR denial.

## `updateAcl` has a distinct direct denial

`DefaultUnifiedRepository.updateAcl()` explicitly checks `ACL_MANAGEMENT` and throws
`UnifiedRepositoryAccessDeniedException` directly. Unlike converted ABS/JCR denials, that
exception has no cause.

## `getChildren` and `getTree` differ

`getChildren` relies on Jackrabbit filtering. `getTree` also calls
`accessVoterManager.hasAccess(READ)` for every visited node, allowing custom voters to
prune further.
