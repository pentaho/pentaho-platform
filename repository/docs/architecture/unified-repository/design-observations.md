---
type: architecture
title: Unified Repository Access Control Design Observations
description: Key design observations explaining why IUnifiedRepository's access-control layers behave the way they do.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Key design observations

## The "not-found confounding" is enforced by Jackrabbit, not by Pentaho code

In the default configuration the `accessVoterManager` has no voters and is a no-op.
The `null` returns in `internalGetFile` and `internalGetFileById` on access denial are
caused by Jackrabbit throwing `PathNotFoundException` / `ItemNotFoundException`, which
the DAO catches and converts to `null`. The `accessVoterManager.hasAccess(READ)`
checks that follow are dead code unless custom voters are registered.

## Jackrabbit is the universal per-file enforcement layer

Every DAO method that executes JCR operations uses a user-credential session. Jackrabbit
enforces its own ACLs on every call with no opt-out. For writes this means
`javax.jcr.AccessDeniedException` is always a possible outcome even when
`accessVoterManager` passes.

## `accessVoterManager` is a pure extension point

Its purpose is to allow plugins to impose additional restrictions beyond what JCR ACLs
alone would enforce (e.g., based on content type, tenant policy, etc.). In the shipped
product it has no effect.

## Write denials from JCR surface as generic exceptions — and look identical to not-found errors

`javax.jcr.AccessDeniedException` is not in the `ExceptionLoggingDecorator` converter map.
Worse, by the time it reaches `ExceptionLoggingDecorator` it has already been translated by
`JcrTemplate` into `org.springframework.dao.DataRetrievalFailureException` (§2.7) — the
**exact same class** that `PathNotFoundException`/`ItemNotFoundException` are translated
into. So a write-denial and a not-found/no-read condition both surface as
`UnifiedRepositoryException` wrapping a `DataRetrievalFailureException`, and are only
distinguishable by inspecting the innermost cause (`getCause().getCause()`, see §4). Callers
that want to detect access denial specifically for write operations must catch
`UnifiedRepositoryException`, walk to the root cause, and check for `javax.jcr.AccessDeniedException`
— or call `hasAccess()` proactively before performing the operation.

## `updateAcl` is the only method with an explicit per-file check at the `DefaultUnifiedRepository` level

All other per-file access decisions are delegated to the JCR DAO layer and ultimately
to Jackrabbit's own ACL enforcement.

## `getChildren` vs `getTree` differ in voter usage

`getChildren` has no `accessVoterManager` call; filtering is purely Jackrabbit native.
`getTree` calls `accessVoterManager.hasAccess(READ)` for every node it visits — which
in the default config is also a no-op, but would allow custom voters to prune the tree
beyond what JCR ACLs allow.

## The confounding is unnecessary when the caller already knows the file exists

For operations where the caller already has READ access (they must, to know the file
exists), the silent `null` return in write/delete paths is less justified. These methods
still return `null` when a custom voter denies, making it harder for callers to
distinguish a permission denial from a programming error.

---

