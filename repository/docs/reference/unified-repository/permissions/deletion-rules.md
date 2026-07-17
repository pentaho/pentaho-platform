---
type: reference
title: IUnifiedRepository Deletion Rules
description: "The three rules that govern who can delete a file or folder: owner precedence, implicit child-delete via WRITE, and explicit ACL boundaries."
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Deletion rules

## Rule 1 — Owner always wins (except ACL management)

The user who **created** a file or folder is its owner. The owner always has full JCR access to that item — including the right to read, edit, and delete it — regardless of what the ACL says. This is enforced silently at the JCR level and is not visible in the ACL editor.

**Exception:** ownership does **not** grant `ACL_MANAGEMENT`. The owner magic ACE injects `jcr:all`, which is a JCR-native aggregate privilege. `pho:aclManagement` is a custom Pentaho privilege registered outside the JCR tree, so `jcr:all` does not include it. An owner who does not have an explicit `ALL` ACE will **not** be able to change permissions on their own node — the UI will not show the permissions panel for them.

## Rule 2 — WRITE on a folder = implicit delete on its children

If a user has `WRITE` on folder `F`, they can delete **any child of `F` that inherits permissions from `F`** (i.e. "Inherit Permissions" is on). This propagates recursively: if child folder `C` also has "Inherit Permissions" on, the user can delete `C`'s children too, and so on down the tree.

This is not a bug — it is by design. The rationale: if you can add and remove children from a folder, you can remove any individual child.

## Rule 3 — Explicit ACL = independent deletion boundary

A file or folder with **"Inherit Permissions" off** (its own explicit ACL) is a **deletion boundary**. The implicit delete grant from the parent's `WRITE` does **not** flow across this boundary. To delete such a node, the user must have:

- `DELETE` explicitly on that node, **or**
- `ALL` explicitly on that node, **or**
- be the **owner** of that node.

Having `WRITE` on the parent is **not enough** to delete a child that has its own explicit ACL.

---
