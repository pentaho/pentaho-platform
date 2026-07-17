---
type: reference
title: Common IUnifiedRepository Permission Configurations
description: Worked ACL configurations for common scenarios (read-only trees, protected files, protected subtrees) and a summary of who can delete what.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Common configurations

## Read-only access to a folder tree

Grant `READ` on the top folder. Enable "Inherit Permissions" on all descendants (the default when content is created). The user can browse and open files but cannot create, edit, or delete anything.

## Read-write access — create, edit, delete anything inside

Grant `WRITE` on the top folder. Enable "Inherit Permissions" on descendants. The user can create, edit, rename, move, and delete any item in the tree.

## Read-write but protect a specific file from deletion

1. Open the specific file's permissions.
2. Turn off "Inherit Permissions" (creates an explicit ACL boundary).
3. Grant `WRITE` (but **not** `DELETE`) explicitly.

The user can read and edit the file's content but cannot delete it. The parent folder's `WRITE` no longer reaches across the boundary.

> The user's `WRITE` on the file also means they have `jcr:removeChildNodes` on it — but since the file has no inheriting children, this has no effect.

## Allow deletion of a specific folder (but not the rest of the parent's content)

You want user U to delete folder `F` (which has its own explicit ACL), without giving U write access to the rest of `F`'s parent directory:

1. Ensure `F` has "Inherit Permissions" off.
2. Grant `DELETE` to user U on `F` explicitly (via the UI: this also forces `WRITE` on `F`).
3. Also ensure U has `WRITE` on `F`'s parent — required at the JCR level for the move-to-trash operation (`jcr:removeChildNodes` on the parent). Without it, the deletion fails even if `DELETE` is granted on `F`.

> **Via the UI, granting DELETE on `F` also grants WRITE on `F`** (due to the UI hierarchy). WRITE on `F` + the inheritance injection rule means U can also delete all of `F`'s inheriting children — not just `F` itself. If you need DELETE on `F` without enabling child deletion, use API-level ACL management to grant DELETE only (without WRITE).

> If granting `WRITE` on the parent is too broad, check whether sibling files/folders have their own explicit ACLs protecting them.

## Protect an entire subtree from deletion

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

## Protect a specific folder from deletion while keeping its contents writable

1. Turn off "Inherit Permissions" on folder `F`.
2. Grant `WRITE` (but **not** `DELETE`) to the user on `F`.

The user can create, edit, and delete items **inside** `F` (since `WRITE` on `F` triggers the implicit child-delete injection for `F`'s inheriting children), but **cannot delete `F` itself** (no `DELETE`, not the owner, and the injection only applies to `F`'s children, not to `F`).

---

# Summary: can I delete X?

| User has… | Target X | "Inherit Permissions" on X | Can delete X? |
|---|---|---|---|
| `WRITE` on X's parent | file or folder | on (inheriting) | ✅ Yes — parent WRITE + inheritance injection |
| `WRITE` on X's parent | file or folder | **off** (own ACL) | ❌ No — injection does not cross ACL boundary |
| `DELETE` on X | file or folder | off (own ACL) | ✅ Yes — but also needs `WRITE` on X's parent for the JCR move-to-trash |
| Owner of X | file or folder | on or off | ✅ Yes — owner magic ACE grants `jcr:all` (includes `jcr:removeNode`) |
| `WRITE` on X (not parent) | folder | off (own ACL) | ❌ No — `WRITE` on X grants child-delete inside X, not deletion of X itself |
| `ALL` on X | file or folder | on or off | ✅ Yes |

---
