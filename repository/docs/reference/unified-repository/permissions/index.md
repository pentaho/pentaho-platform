# IUnifiedRepository Permissions And Access Control

The ACL and JCR-privilege model enforced by `IUnifiedRepository`'s access-control
layers (see the [architecture](../../../architecture/unified-repository/index.md)),
used directly by `IUnifiedRepository` callers and transitively by `FileService`.

## Mechanics

- [IUnifiedRepository Permission Enum To JCR Privilege Mapping](jcr-privilege-mapping.md) - How the Pentaho RepositoryFilePermission enum (READ/WRITE/DELETE/ACL_MANAGEMENT/ALL) maps to native JCR privileges.
- [IUnifiedRepository ACL Model](acl-model.md) - How RepositoryFileAcl, RepositoryFileSid, and ACL metadata are structured and stored in JCR.
- [IUnifiedRepository ACL Inheritance](inheritance.md) - How ACL inheritance is resolved between a node and its ancestors, including getEffectiveAces.
- [IUnifiedRepository Magic ACEs](magic-aces.md) - The three sources of ephemeral, never-persisted ACEs (owner ACE, inheritance transformation, config.yaml ACEs) injected at privilege-evaluation time by PentahoEntryCollector.
- [IUnifiedRepository Access-Control Pre-Check Layer](pre-check-layer.md) - Why the voter/aclDao pre-check layer exists, the per-operation JCR privilege table it must mirror, and why DELETE behaves asymmetrically between a node and its parent.
- [IUnifiedRepository Layer 1 Policy Actions](policy-actions.md) - System-level, action-based policy checks (IAuthorizationPolicy) performed by high-level callers before touching the repository, distinct from the per-file ACL checks.
- [IUnifiedRepository Permission Model Key Classes](key-classes.md) - Quick-reference table of the classes that implement the permission model, their location, and their role.
- [IUnifiedRepository hasAccess Call Chain](hasaccess-call-chain.md) - The full call chain from DefaultUnifiedRepository.hasAccess() down to the native Jackrabbit privilege check.

## Semantics And Use Cases

- [IUnifiedRepository Permission Hierarchy (UI Vs API)](permission-hierarchy.md) - The cumulative READ/WRITE/DELETE/ACL_MANAGEMENT/ALL hierarchy enforced by the sharing UI, and what the API additionally allows.
- [What Each IUnifiedRepository Permission Means](permission-meanings.md) - What READ, WRITE, DELETE, ACL_MANAGEMENT, and ALL each allow on a file versus a folder.
- [IUnifiedRepository Deletion Rules](deletion-rules.md) - The three rules that govern who can delete a file or folder: owner precedence, implicit child-delete via WRITE, and explicit ACL boundaries.
- [Common IUnifiedRepository Permission Configurations](common-configurations.md) - Worked ACL configurations for common scenarios (read-only trees, protected files, protected subtrees) and a summary of who can delete what.
- [Unsupported IUnifiedRepository Permission Use Cases](unsupported-use-cases.md) - Permission scenarios common in other systems (sticky bit, explicit deny, execute permission, copy protection, etc.) that the Pentaho repository permission model cannot express.

## Known Issues

- [IUnifiedRepository Permission Model Known Issues](known-issues.md) - Current inconsistent, surprising, or likely-unintended access-control behaviors in the repository permission model, with suggested fixes and risk analysis.
