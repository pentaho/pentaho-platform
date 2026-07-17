---
type: reference
title: What Each IUnifiedRepository Permission Means
description: What READ, WRITE, DELETE, ACL_MANAGEMENT, and ALL each allow on a file versus a folder.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# What each permission means

## On a **file**

| Permission | What it allows |
|---|---|
| `READ` | View and open the file. See it in folder listings. |
| `WRITE` | Edit the file's content. Update its title/description. Rename it (rename is a move). Move it to another folder. |
| `DELETE` | Delete the file. Only relevant when the file has its own explicit ACL (i.e. "Inherit Permissions" is off). When "Inherit Permissions" is on, the parent folder's `WRITE` already covers deletion — see below. |
| `ACL_MANAGEMENT` | Change who has access to the file (add/remove/modify ACEs). Does not imply the ability to read or edit the file. |
| `ALL` | All of the above. |

## On a **folder**

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
