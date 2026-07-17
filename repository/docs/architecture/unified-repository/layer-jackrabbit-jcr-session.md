---
type: architecture
title: Jackrabbit JCR Session Layer
description: Native Jackrabbit JCR session behavior underlying repository access control.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Jackrabbit JCR session (native layer)

The JCR session is opened using `PentahoSessionCredentialsStrategy`, which extracts
the current Pentaho session credentials and logs into Jackrabbit as the actual user.

- Jackrabbit enforces its own ACL at every JCR API call made through the session.
- When a user has no read access to a node, `session.getItem(path)` throws
  `PathNotFoundException` and `session.getNodeByIdentifier(id)` throws
  `ItemNotFoundException` — the **same exceptions thrown when the node genuinely does
  not exist**. This is standard JCR/Jackrabbit behaviour: inaccessible nodes are
  completely invisible to the session. There is no way to distinguish "absent" from
  "access denied" at the JCR API level, and this is the primary source of the
  "not-found confounding" for read operations.
- When a user lacks write/delete/lock privilege, mutating JCR operations throw
  `javax.jcr.AccessDeniedException`. Per §2.7, `JcrTemplate` translates this — along with
  `PathNotFoundException`/`ItemNotFoundException` above — into the same
  `org.springframework.dao.DataRetrievalFailureException`. Since that class is not in the
  `ExceptionLoggingDecorator` converter map, it surfaces as a generic
  `UnifiedRepositoryException`, indistinguishable at the outer-exception level from a
  not-found/no-read condition (see §4 for how to disambiguate via the cause chain).
- `session.getAccessControlManager().hasPrivileges()` (used by `JcrRepositoryFileAclDao.hasAccess()`
  and therefore by `DefaultUnifiedRepository.updateAcl()`) explicitly checks whether
  specific JCR privileges are held on a given path, returning `false` for
  `PathNotFoundException`.

---

