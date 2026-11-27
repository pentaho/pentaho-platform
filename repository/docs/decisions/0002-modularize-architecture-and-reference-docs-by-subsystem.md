---
type: decision
title: Modularize Architecture And Reference Docs By Subsystem
description: Split the two subsystem architecture docs and two exception-disambiguation cookbooks into per-layer and per-operation concepts, grouped by subsystem.
status: accepted
created: 2026-07-17
timestamp: 2026-07-17T00:00:00Z
---

# Context

After the initial OKF restructuring ([0001](./0001-use-okf-for-project-documentation.md)),
four concepts remained very large single files:

- `architecture/unified-repository-access-control.md` (~800 lines covering bean
  composition, seven distinct enforcement layers, a summary table, an
  exception taxonomy, and design observations)
- `architecture/file-service-access-control.md` (~340 lines covering bean
  composition, four layers/worked-examples, a summary table, contract
  divergence, and design observations)
- `reference/unified-repository-exception-disambiguation.md` (~850 lines: a
  general approach plus 27 per-operation disambiguation recipes)
- `reference/file-service-exception-disambiguation.md` (~580 lines: a general
  approach plus 15 per-operation disambiguation recipes)

Each file mixed multiple independently useful concepts (one per layer, one per
API operation) in a single document, making it hard to link directly to, or
update, one layer or one operation without touching unrelated content.

# Decision

Group each subsystem's architecture and reference concepts under a dedicated
subdirectory, and split each oversized file along its natural section
boundaries:

- `architecture/unified-repository/` — `overview.md` (bean composition and
  call chain), one `layer-*.md` per enforcement layer (seven files), and
  `design-observations.md`.
- `architecture/file-service/` — `overview.md`, one file per layer/worked
  example (four files), and `design-observations.md`.
- `reference/unified-repository/` — `summary-table-per-method.md`,
  `exception-taxonomy.md`, and an `exception-disambiguation/` subgroup with
  `general-approach.md` plus one file per API operation (27 files).
- `reference/file-service/` — `summary-table-per-operation.md`,
  `contract-divergence.md`, and an `exception-disambiguation/` subgroup with
  `general-approach.md` plus one file per API operation (15 files).

The permission-model reference docs (`repository-permission-model.md`,
`repository-permission-semantics.md`, `repository-permission-known-issues.md`)
stay at the top of `reference/` since they are shared across both subsystems,
not specific to one.

# Alternatives Considered

- **Split only by subsystem, keep each subsystem's content as one file per
  group.** Rejected — the individual files were still hundreds of lines long
  and mixed many independent layers or operations.
- **Split the disambiguation "per-operation snippets" section only down to a
  handful of grouped files (e.g. by read/write/delete category) instead of one
  file per operation.** Rejected in favor of one file per operation — each
  operation's disambiguation recipe is independently referenced by API
  consumers who care about one specific call, and the source material was
  already organized as one self-contained snippet per operation.
- **Keep `2.4 JcrRepositoryFileDao` split further into its eight numbered
  sub-sections** (access voter, native ACL enforcement, bypass, kiosk mode,
  read/write behavior, per-node privileges). Rejected — these sub-sections
  describe one cohesive component's behavior and are tightly cross-referential;
  splitting them further would fragment a single concept rather than separate
  genuinely independent ones.

# Consequences

- Cross-references that used section numbers (e.g. "§2.4", "main doc §3") in
  the original monolithic files are no longer resolvable to a heading number
  and were left as prose in the split files; some may need manual follow-up
  linking to the correct new file as they are noticed.
- Each new file carries its own OKF frontmatter and can be validated
  independently with `scripts/validate.py --level pentaho`.
- Group `index.md` files at each new subdirectory list their concepts so
  readers can navigate by layer or by operation instead of scrolling a large
  file.

# Related Documents

- [0001: Use OKF For Repository Access-Control Project Documentation](./0001-use-okf-for-project-documentation.md)
- [Unified Repository Architecture](../architecture/unified-repository/index.md)
- [FileService Architecture](../architecture/file-service/index.md)
- [Unified Repository Reference](../reference/unified-repository/index.md)
- [FileService Reference](../reference/file-service/index.md)
