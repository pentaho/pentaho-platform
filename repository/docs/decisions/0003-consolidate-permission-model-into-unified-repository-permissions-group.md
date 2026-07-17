---
type: decision
title: "Consolidate Permission Model Into Unified-Repository Permissions Group"
description: Merge the standalone repository-permission-model/semantics/known-issues reference docs into a new reference/unified-repository/permissions/ subgroup, deduplicating content already covered by the architecture layer docs.
status: accepted
created: 2026-07-18
---

# Context

[Decision 0002](0002-modularize-architecture-and-reference-docs-by-subsystem.md) split the
`architecture/` and `reference/` groups into per-layer and per-operation concepts organized
under `unified-repository/` and `file-service/` subsystem subgroups. It did not touch the
three top-level permission-model reference docs
(`reference/repository-permission-model.md`, `-semantics.md`, `-known-issues.md`), which
predated that split and remained flat, large documents.

This left two problems:

1. **Duplication.** `repository-permission-model.md`'s "Purpose of the Pre-Check Layer"
   table (per-operation JCR privilege requirements) nearly duplicated the "2.4.8" table in
   `architecture/unified-repository/layer-jcr-repository-file-dao.md`. Its "Notable Gaps /
   Gotchas" list substantially overlapped a richer "Questionable / Likely Unintended
   Behaviors" write-up in `repository-permission-known-issues.md`, and some gaps were
   already independently covered elsewhere (kiosk mode, `updateAcl` check location).
2. **Unclear naming.** Neither the top-level file names nor their internal section headings
   made it obvious that this content was specifically about ACLs, JCR privileges, and
   security enforcement — the context that motivated the original split (`0001`) was
   getting lost as the docs were reorganized around subsystems and layers instead of
   around permissions.

# Decision

Move and split the three top-level files into a new
`reference/unified-repository/permissions/` subgroup, following the same per-concept
splitting approach as `0002`:

- 13 concept files split from the old model/semantics docs (`jcr-privilege-mapping.md`,
  `acl-model.md`, `inheritance.md`, `magic-aces.md`, `pre-check-layer.md`,
  `policy-actions.md`, `key-classes.md`, `hasaccess-call-chain.md`,
  `permission-hierarchy.md`, `permission-meanings.md`, `deletion-rules.md`,
  `common-configurations.md`, `unsupported-use-cases.md`), each retitled with an explicit
  `IUnifiedRepository Permission...` / `IUnifiedRepository ACL...` prefix so the subject is
  unambiguous from the title alone.
- One consolidated `known-issues.md`, merging the "Notable Gaps" and "Questionable /
  Likely Unintended Behaviors" write-ups into a single deduplicated list. Items already
  covered by architecture docs (kiosk mode, `updateAcl` check location) were dropped in
  favor of a link; two previously-uncaptured facts (WRITE implies
  `jcr:modifyAccessControl`; Magic ACEs are invisible via `getAcl()`) were added.
- `architecture/unified-repository/layer-jcr-repository-file-dao.md`'s "2.4.8" section was
  trimmed to a short summary that links to `permissions/pre-check-layer.md` and
  `permissions/magic-aces.md` instead of repeating their tables and explanations, per the
  Pentaho profile rule that types "must not duplicate ownership" — reference is now the
  single source of truth for the privilege table, and architecture keeps only the framing
  of how it affects the interceptor's own exception behavior.
- `policy-actions.md` was kept as a distinct concept (not merged into
  `layer-method-interceptor.md`) because it describes a caller-side,
  action-based policy check that runs before the repository is even invoked, as opposed to
  the interceptor's per-method internal ABS check — a clarifying cross-link was added in
  both directions instead of merging.
- The old top-level files were deleted (`git rm`) once their content was fully
  redistributed.

# Alternatives Considered

- **Leave the three files where they were, only fix cross-links.** Rejected: this would
  leave the naming problem unresolved and perpetuate the duplicated pre-check table.
- **Merge the permission docs directly into the architecture layer docs instead of keeping
  a separate reference subgroup.** Rejected: permission-model content is current fact
  (What is true now?), which the Pentaho profile assigns to `reference`, not
  `architecture` (How is the system intended to fit together?). Collapsing them would blur
  that distinction and make the architecture docs much longer.
- **Keep `known-issues.md` as two separate documents (gaps vs. questionable behaviors).**
  Rejected: the two lists described mostly the same underlying issues from different
  angles; keeping them separate would keep the duplication the user flagged.

# Consequences

- `reference/unified-repository/permissions/` is now the single, clearly-named home for
  all ACL/JCR-privilege/security-related reference facts for `IUnifiedRepository`, indexed
  from `reference/unified-repository/index.md` under a new "Permissions And Access
  Control" section.
- `architecture/unified-repository/layer-jcr-repository-file-dao.md` is shorter and no
  longer duplicates the privilege table; readers needing full details follow the links
  into `permissions/`.
- Future permission-related facts should be added to or split further within
  `permissions/`, not reintroduced into the architecture layer docs.

# Related Documents

- [0001: Use OKF For Repository Access-Control Project Documentation](0001-use-okf-for-project-documentation.md)
- [0002: Modularize Architecture And Reference Docs By Subsystem](0002-modularize-architecture-and-reference-docs-by-subsystem.md)
- [Permissions Index](../reference/unified-repository/permissions/index.md)
- [IUnifiedRepository Access-Control Pre-Check Layer](../reference/unified-repository/permissions/pre-check-layer.md)
- [JCR RepositoryFileDao Layer](../architecture/unified-repository/layer-jcr-repository-file-dao.md)
