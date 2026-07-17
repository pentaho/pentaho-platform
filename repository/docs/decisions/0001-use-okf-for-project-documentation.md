---
type: decision
title: Use OKF For Repository Access-Control Project Documentation
description: Adopt the Open Knowledge Format (Pentaho profile) to structure the repository/FileService access-control documentation.
status: accepted
created: 2026-07-17
timestamp: 2026-07-17T00:00:00Z
---

# Context

This directory held five large, flat Markdown files analyzing repository and
`FileService` access control (`permission-model.md`,
`unified-repository-access-control.md`,
`unified-repository-access-control-disambiguation.md`,
`file-service-access-control.md`,
`file-service-access-control-disambiguation.md`). They mixed several kinds of
content in the same files — intended layer-by-layer design (architecture),
stable current-state facts (reference), and a standalone "use-cases and known
issues" appendix — with no machine-checkable structure, making it hard for
people or agents to tell design intent apart from current fact or to navigate
directly to the right concept.

# Decision

Restructure this directory as one Open Knowledge Format bundle under the
Pentaho profile:

- `architecture/` — `unified-repository-access-control.md` and
  `file-service-access-control.md` keep the bean-composition and
  layer-by-layer analysis of each subsystem.
- `reference/` — `permission-model.md` is split into three focused concepts
  (`repository-permission-model.md`, `repository-permission-semantics.md`,
  `repository-permission-known-issues.md`), and the two disambiguation
  cookbooks are renamed to `unified-repository-exception-disambiguation.md`
  and `file-service-exception-disambiguation.md`. All five records document
  current, stable facts (API behavior, permission semantics, known bugs, and
  exception-detection recipes) rather than design intent.
- `decisions/` — this record, as the first ADR for the bundle.

No `standards/` or `runbooks/` group is created yet: none of the existing
content is normative policy or an operational procedure with rollback and
escalation steps.

# Alternatives Considered

- **Keep the flat file layout.** Rejected — it does not distinguish
  architecture from reference, offers no machine-checkable frontmatter, and
  forces readers through very large files to find one fact.
- **One giant reference document per subsystem instead of splitting
  `permission-model.md`.** Rejected — the source file already contained three
  independently useful concepts (model mechanics, semantics/use-cases, known
  issues) with distinct audiences and update cadences.
- **Classify the disambiguation docs as `runbook`.** Rejected — they document
  current facts about how to detect API conditions (a cookbook for
  consumers), not an operational procedure with preconditions, rollback, or
  escalation.

# Consequences

- Each concept file now carries OKF frontmatter (`type`, `title`,
  `description`, `status`, `timestamp`) and can be validated with
  `scripts/validate.py --level pentaho`.
- Cross-links between the architecture and reference docs were updated to the
  new paths and filenames.
- Future updates to permission mechanics, semantics, or known issues can be
  made independently without touching unrelated content in the same file.

# Related Documents

- [Unified Repository Access Control Analysis](../architecture/unified-repository-access-control.md)
- [FileService Access Control Analysis](../architecture/file-service-access-control.md)
- [Repository Permission Model](../reference/repository-permission-model.md)
- [Repository Permission Semantics And Use Cases](../reference/repository-permission-semantics.md)
- [Repository Permission Model Known Issues](../reference/repository-permission-known-issues.md)
- [Disambiguating IUnifiedRepository Exceptions Via Public API Calls](../reference/unified-repository-exception-disambiguation.md)
- [Disambiguating FileService Exceptions Via Public API Calls](../reference/file-service-exception-disambiguation.md)
