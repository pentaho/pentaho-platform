---
type: reference
title: Repository Permission Semantics And Use Cases
description: What each permission means in practice, deletion rules, common configurations, and use cases the model cannot express.
status: active
timestamp: 2026-07-17T17:09:07Z
---

# Permission Semantics & Use Cases

> This section is a standalone guide. It covers what each permission does in practice, how to configure common scenarios, and how to control deletion boundaries.
>
> See also: [Repository Permission Model](./repository-permission-model.md) for the underlying JCR/ACL mechanics and [Repository Permission Model Known Issues](./repository-permission-known-issues.md) for inconsistent or likely-unintended behaviors.

---

## Permission hierarchy (UI vs API)

The **File Share/Permissions UI** enforces a cumulative hierarchy:

```
ALL  ⊃  DELETE  ⊃  WRITE  ⊃  READ
```

Selecting a higher permission in the UI automatically includes all lower ones. `ACL_MANAGEMENT` is only available as part of `ALL`.

This hierarchy is **enforced only by the UI**. The backend API and direct ACL management accept any arbitrary combination. The distinctions below apply to what is expressible via the UI unless otherwise noted.

| Expressible via UI | READ | WRITE | DELETE | ACL_MANAGEMENT | ALL |
|---|:---:|:---:|:---:|:---:|:---:|
| READ only | ✅ | | | | |
| WRITE (+ READ) | ✅ | ✅ | | | |
| DELETE (+ WRITE + READ) | ✅ | ✅ | ✅ | | |
| ALL (+ DELETE + WRITE + READ + ACL_MANAGEMENT) | ✅ | ✅ | ✅ | ✅ | ✅ |
| ACL_MANAGEMENT without ALL | API only | | | ✅ | |
| DELETE without WRITE | API only | | ✅ | | |

---

## What each permission means

### On a **file**

| Permission | What it allows |
|---|---|
| `READ` | View and open the file. See it in folder listings. |
| `WRITE` | Edit the file's content. Update its title/description. Rename it (rename is a move). Move it to another folder. |
| `DELETE` | Delete the file. Only relevant when the file has its own explicit ACL (i.e. "Inherit Permissions" is off). When "Inherit Permissions" is on, the parent folder's `WRITE` already covers deletion — see below. |
| `ACL_MANAGEMENT` | Change who has access to the file (add/remove/modify ACEs). Does not imply the ability to read or edit the file. |
| `ALL` | All of the above. |

### On a **folder**

| Permission | What it allows |
|---|---|
| `READ` | See the folder in listings. List its direct children (those that also grant READ, by inheritance or explicitly). Does **not** allow creating or modifying anything inside. |
| `WRITE` | Create files and subfolders inside it. Edit, rename, and move items inside it. **Implicitly allows deleting any child that inherits permissions from this folder** — see the Deletion Rules section below. |
| `DELETE` | Delete this folder itself (only when "Inherit Permissions" is off). **Via the UI, DELETE always includes WRITE** (hierarchy enforced). This means granting DELETE on a folder via the UI also implicitly enables deletion of all its inheriting children — not just the folder itself. See the note below. |
| `ACL_MANAGEMENT` | Change who has access to this folder. Does not imply read or write access. Only available via ALL in the UI. |
| `ALL` | All of the above. |

> **Via the UI, DELETE always brings WRITE.** The child-deletion side effect comes entirely from WRITE (via the inheritance injection rule) — not from DELETE. DELETE's own contribution is narrow: the ability to delete the folder itself. WRITE is what enables deletion of all inheriting descendants. So granting DELETE via the UI = WRITE (child delete) + DELETE (self delete).
>
> If only the folder itself should be deletable (not its children), this requires API-level ACL management: grant DELETE without WRITE on `F` directly. Even then, WRITE on `F`'s parent is still required by JCR for the move-to-trash operation.

> **Inheriting DELETE is redundant when the parent also has WRITE.** WRITE on a parent already injects `jcr:removeNode` on all inheriting children via `PentahoEntryCollector` — DELETE's `jcr:removeNode` would be a duplicate. DELETE's primary unique effect is enabling deletion of the node that *holds* the explicit ACL itself (where the injection never fires).
>
> **Exception — DELETE without WRITE (API only):** No injection fires (no `jcr:removeChildNodes`), but inheriting children still receive `jcr:removeNode` directly via standard JCR ACL inheritance. Inheriting files can therefore be deleted via DELETE alone, without WRITE. This is the one case where DELETE has distinct downstream value — a delete-only grant with no create/modify rights. Since the UI always bundles WRITE with DELETE, this is only reachable via the API.
>
> **API edge case — DELETE without WRITE on a parent:** children inherit `jcr:removeNode` (can be deleted) but not `jcr:addChildNodes` / `jcr:removeChildNodes` (cannot create or modify content inside them). A deletable-but-immutable subtree. No practical use case; unreachable via the UI.

---

## Deletion rules

### Rule 1 — Owner always wins (except ACL management)

The user who **created** a file or folder is its owner. The owner always has full JCR access to that item — including the right to read, edit, and delete it — regardless of what the ACL says. This is enforced silently at the JCR level and is not visible in the ACL editor.

**Exception:** ownership does **not** grant `ACL_MANAGEMENT`. The owner magic ACE injects `jcr:all`, which is a JCR-native aggregate privilege. `pho:aclManagement` is a custom Pentaho privilege registered outside the JCR tree, so `jcr:all` does not include it. An owner who does not have an explicit `ALL` ACE will **not** be able to change permissions on their own node — the UI will not show the permissions panel for them.

### Rule 2 — WRITE on a folder = implicit delete on its children

If a user has `WRITE` on folder `F`, they can delete **any child of `F` that inherits permissions from `F`** (i.e. "Inherit Permissions" is on). This propagates recursively: if child folder `C` also has "Inherit Permissions" on, the user can delete `C`'s children too, and so on down the tree.

This is not a bug — it is by design. The rationale: if you can add and remove children from a folder, you can remove any individual child.

### Rule 3 — Explicit ACL = independent deletion boundary

A file or folder with **"Inherit Permissions" off** (its own explicit ACL) is a **deletion boundary**. The implicit delete grant from the parent's `WRITE` does **not** flow across this boundary. To delete such a node, the user must have:

- `DELETE` explicitly on that node, **or**
- `ALL` explicitly on that node, **or**
- be the **owner** of that node.

Having `WRITE` on the parent is **not enough** to delete a child that has its own explicit ACL.

---

## Common configurations

### Read-only access to a folder tree

Grant `READ` on the top folder. Enable "Inherit Permissions" on all descendants (the default when content is created). The user can browse and open files but cannot create, edit, or delete anything.

### Read-write access — create, edit, delete anything inside

Grant `WRITE` on the top folder. Enable "Inherit Permissions" on descendants. The user can create, edit, rename, move, and delete any item in the tree.

### Read-write but protect a specific file from deletion

1. Open the specific file's permissions.
2. Turn off "Inherit Permissions" (creates an explicit ACL boundary).
3. Grant `WRITE` (but **not** `DELETE`) explicitly.

The user can read and edit the file's content but cannot delete it. The parent folder's `WRITE` no longer reaches across the boundary.

> The user's `WRITE` on the file also means they have `jcr:removeChildNodes` on it — but since the file has no inheriting children, this has no effect.

### Allow deletion of a specific folder (but not the rest of the parent's content)

You want user U to delete folder `F` (which has its own explicit ACL), without giving U write access to the rest of `F`'s parent directory:

1. Ensure `F` has "Inherit Permissions" off.
2. Grant `DELETE` to user U on `F` explicitly (via the UI: this also forces `WRITE` on `F`).
3. Also ensure U has `WRITE` on `F`'s parent — required at the JCR level for the move-to-trash operation (`jcr:removeChildNodes` on the parent). Without it, the deletion fails even if `DELETE` is granted on `F`.

> **Via the UI, granting DELETE on `F` also grants WRITE on `F`** (due to the UI hierarchy). WRITE on `F` + the inheritance injection rule means U can also delete all of `F`'s inheriting children — not just `F` itself. If you need DELETE on `F` without enabling child deletion, use API-level ACL management to grant DELETE only (without WRITE).

> If granting `WRITE` on the parent is too broad, check whether sibling files/folders have their own explicit ACLs protecting them.

### Protect an entire subtree from deletion

To prevent anyone (except the owner and admins) from deleting a folder `F` and everything inside it:

1. Open `F`'s permissions. Turn off "Inherit Permissions".
2. Grant only `READ` on `F` (no `WRITE`, no `DELETE`).
3. Leave "Inherit Permissions" on for `F`'s children — they will inherit `READ` from `F`.

**Effect:**
- No one has `WRITE` on `F` → no `jcr:removeChildNodes` → the implicit delete injection never fires for any descendant.
- `F` itself has no `DELETE` ACE → cannot be deleted (unless you are its owner).
- The entire subtree is read-only.

If individual items inside the subtree still need to be editable:

1. Turn off "Inherit Permissions" on those specific items.
2. Grant `WRITE` explicitly on each one.
3. Their children (if any) will then be deletable by anyone with `WRITE` on them, but the rest of the subtree remains protected.

### Protect a specific folder from deletion while keeping its contents writable

1. Turn off "Inherit Permissions" on folder `F`.
2. Grant `WRITE` (but **not** `DELETE`) to the user on `F`.

The user can create, edit, and delete items **inside** `F` (since `WRITE` on `F` triggers the implicit child-delete injection for `F`'s inheriting children), but **cannot delete `F` itself** (no `DELETE`, not the owner, and the injection only applies to `F`'s children, not to `F`).

---

## Summary: can I delete X?

| User has… | Target X | "Inherit Permissions" on X | Can delete X? |
|---|---|---|---|
| `WRITE` on X's parent | file or folder | on (inheriting) | ✅ Yes — parent WRITE + inheritance injection |
| `WRITE` on X's parent | file or folder | **off** (own ACL) | ❌ No — injection does not cross ACL boundary |
| `DELETE` on X | file or folder | off (own ACL) | ✅ Yes — but also needs `WRITE` on X's parent for the JCR move-to-trash |
| Owner of X | file or folder | on or off | ✅ Yes — owner magic ACE grants `jcr:all` (includes `jcr:removeNode`) |
| `WRITE` on X (not parent) | folder | off (own ACL) | ❌ No — `WRITE` on X grants child-delete inside X, not deletion of X itself |
| `ALL` on X | file or folder | on or off | ✅ Yes |

---

## Unsupported use cases

These are permission scenarios common in other systems that **cannot be expressed** in the Pentaho repository model.

---

### ❌ Sticky bit — "add files, but only delete your own"

**What you'd want:** Users can create files in a shared folder. Each user can delete files they created (they own), but cannot delete files created by others. Equivalent to the Unix sticky bit (`chmod +t`).

**Why it doesn't work:**

Creating a file in folder `F` requires `WRITE` on `F`. `WRITE` maps to both `jcr:addChildNodes` **and** `jcr:removeChildNodes` — they are bundled into the same permission with no way to separate them. `jcr:removeChildNodes` on `F` triggers the inheritance injection, granting `jcr:removeNode` on every inheriting child of `F` — owned by anyone.

There is no "add-only" (`jcr:addChildNodes` without `jcr:removeChildNodes`) permission in the Pentaho model.

**Partial workaround (heavy):** Give each file its own explicit ACL (`isEntriesInheriting=false`) with WRITE but no DELETE after it is created. This protects it from deletion by others (the injection doesn't cross ACL boundaries), but requires per-file ACL management. It does not scale and cannot be automated as a folder policy.

---

### ❌ WRITE without implicit child-delete

**What you'd want:** A user can create and edit content inside folder `F` but cannot delete any of its children.

**Why it doesn't work:**

`WRITE` is an atomic permission. It always includes `jcr:removeChildNodes`, which always triggers the `jcr:removeNode` injection on inheriting children. There is no "WRITE minus delete-children" in the model. You cannot grant `jcr:addChildNodes` independently of `jcr:removeChildNodes` through the Pentaho permission API.

---

### ❌ "Everyone except user X" (explicit DENY)

**What you'd want:** A role has READ on a folder, but one specific member of that role should be denied access to a particular file.

**Why it doesn't work:**

The Pentaho ACL API only stores **ALLOW** entries. `JcrRepositoryFileAclUtils.internalUpdateAcl` calls `acl.addAccessControlEntry(principal, privileges)` — the 2-argument form in Jackrabbit's `ACLTemplate` always creates an ALLOW entry. There is no supported way to write a DENY ACE via the Pentaho repository API.

To express the exception, the only option is to break the role-based grant and enumerate principals individually, or place an explicit ACL boundary and re-grant narrowly.

---

### ❌ List folder contents without reading file content

**What you'd want:** A user can see what files exist in a folder (names, metadata) without being able to open them. Common in shared directories where filenames are informational.

**Why it doesn't work:**

`READ` maps to `jcr:read` + `jcr:readAccessControl` — a single indivisible privilege that covers both node traversal/listing and reading node properties (i.e. file content). There is no separate "list" vs "open" distinction.

---

### ❌ Copy protection — "can read here but not copy out"

**What you'd want:** Users can open files from folder `F` but cannot copy them to another folder.

**Why it doesn't work:**

`copyFile` checks **only** WRITE on the destination folder. The source file is not permission-checked at the Pentaho layer, and Jackrabbit's `workspace.copy()` does not enforce source-read at the JCR level either. Any user who can reference a file by ID and has WRITE on a destination can copy the file, regardless of their permissions on the source.

---

### ❌ Move allowed, copy disallowed (or vice versa)

**What you'd want:** Grant the ability to move files out of a folder (removing them from the source) without allowing duplication via copy.

**Why it doesn't work:**

`moveFile` requires WRITE on both source and destination. `copyFile` requires only WRITE on destination (source not checked). There is no permission that covers "move" independently of the components it is made of. If you have WRITE on source and destination, both move and copy are available. You cannot allow one while blocking the other.

---

### ❌ Execute permission

**What you'd want:** Separate the ability to run/execute a resource (e.g. a report or script) from the ability to read its definition.

**Why it doesn't work:**

The `EXECUTE` value is commented out in `RepositoryFilePermission`. The original design notes reference [JCR-2446](http://issues.apache.org/jira/browse/JCR-2446), which explains that JCR has no native execute semantics. No execute permission exists in the model.

---

### ❌ Creator loses delete rights after upload

**What you'd want:** A "submission folder" — users can upload files but, once submitted, they cannot delete or modify them (e.g. for audit or compliance).

**Why it doesn't work:**

The user who creates a file becomes its **owner**. The owner always receives `jcr:all` via the owner magic ACE at privilege evaluation time, regardless of what the ACL says. This covers all JCR-native operations (read, write, delete) but does **not** include `pho:aclManagement` (a custom Pentaho privilege outside the JCR tree). Owners cannot change permissions on their own nodes unless they also have an explicit `ALL` ACE.

---

### ❌ Delete a folder without enabling deletion of its children (via UI)

**What you'd want:** Grant a user the ability to delete folder `F` itself, but prevent them from deleting `F`'s children.

**Why it doesn't work via the UI:**

The UI hierarchy forces `DELETE ⊃ WRITE`. Granting DELETE on `F` in the UI always also grants WRITE on `F`. WRITE on `F` maps to `jcr:removeChildNodes`, which triggers the inheritance injection in `PentahoEntryCollector` — granting `jcr:removeNode` on every inheriting child of `F`. So DELETE on `F` via the UI = delete `F` + delete all of `F`'s inheriting children.

**API-only workaround:** Programmatically set an ACL on `F` with DELETE but not WRITE. Even then, the JCR move-to-trash still requires `jcr:removeChildNodes` on `F`'s parent, so the parent's WRITE is still needed for the actual deletion.

---

### ❌ Owner cannot isolate their own folder from parent permissions

**What you'd want:** A user creates a folder inside a shared parent. They own it, so they should be able to "lock it down" — turn off "Inherit Permissions" and set their own ACL, preventing others who have WRITE on the parent from accessing its contents.

**Why it doesn't work:**

Changing the `isEntriesInheriting` flag (the "Inherit Permissions" toggle) requires `ACL_MANAGEMENT`. Ownership only grants `jcr:all`, which does **not** include `pho:aclManagement`. A user who merely created the folder — without an explicit `ALL` ACE — cannot modify its ACL or toggle inheritance, even though they own it.

**Consequence:** The folder inherits its parent's permissions by default and stays that way. Anyone with WRITE on the parent can delete the folder (via the inheritance injection rule), and the owner has no way to prevent this unless an admin grants them `ALL` on their own folder.

**Workaround:** An administrator must explicitly grant `ALL` to the user on the folder after creation, enabling the user to then manage its ACL independently.

---

