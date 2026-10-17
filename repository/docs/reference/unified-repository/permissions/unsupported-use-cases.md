---
type: reference
title: Unsupported IUnifiedRepository Permission Use Cases
description: Permission scenarios common in other systems (sticky bit, explicit deny, execute permission, copy protection, etc.) that the Pentaho repository permission model cannot express.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Unsupported use cases

These are permission scenarios common in other systems that **cannot be expressed** in the Pentaho repository model.

---

## ❌ Sticky bit — "add files, but only delete your own"

**What you'd want:** Users can create files in a shared folder. Each user can delete files they created (they own), but cannot delete files created by others. Equivalent to the Unix sticky bit (`chmod +t`).

**Why it doesn't work:**

Creating a file in folder `F` requires `WRITE` on `F`. `WRITE` maps to both `jcr:addChildNodes` **and** `jcr:removeChildNodes` — they are bundled into the same permission with no way to separate them. `jcr:removeChildNodes` on `F` triggers the inheritance injection, granting `jcr:removeNode` on every inheriting child of `F` — owned by anyone.

There is no "add-only" (`jcr:addChildNodes` without `jcr:removeChildNodes`) permission in the Pentaho model.

**Partial workaround (heavy):** Give each file its own explicit ACL (`isEntriesInheriting=false`) with WRITE but no DELETE after it is created. This protects it from deletion by others (the injection doesn't cross ACL boundaries), but requires per-file ACL management. It does not scale and cannot be automated as a folder policy.

---

## ❌ WRITE without implicit child-delete

**What you'd want:** A user can create and edit content inside folder `F` but cannot delete any of its children.

**Why it doesn't work:**

`WRITE` is an atomic permission. It always includes `jcr:removeChildNodes`, which always triggers the `jcr:removeNode` injection on inheriting children. There is no "WRITE minus delete-children" in the model. You cannot grant `jcr:addChildNodes` independently of `jcr:removeChildNodes` through the Pentaho permission API.

---

## ❌ "Everyone except user X" (explicit DENY)

**What you'd want:** A role has READ on a folder, but one specific member of that role should be denied access to a particular file.

**Why it doesn't work:**

The Pentaho ACL API only stores **ALLOW** entries. `JcrRepositoryFileAclUtils.internalUpdateAcl` calls `acl.addAccessControlEntry(principal, privileges)` — the 2-argument form in Jackrabbit's `ACLTemplate` always creates an ALLOW entry. There is no supported way to write a DENY ACE via the Pentaho repository API.

To express the exception, the only option is to break the role-based grant and enumerate principals individually, or place an explicit ACL boundary and re-grant narrowly.

---

## ❌ List folder contents without reading file content

**What you'd want:** A user can see what files exist in a folder (names, metadata) without being able to open them. Common in shared directories where filenames are informational.

**Why it doesn't work:**

`READ` maps to `jcr:read` + `jcr:readAccessControl` — a single indivisible privilege that covers both node traversal/listing and reading node properties (i.e. file content). There is no separate "list" vs "open" distinction.

---

## ❌ Copy protection — "can read here but not copy out"

**What you'd want:** Users can open files from folder `F` but cannot copy them to another folder.

**Why it doesn't work:**

`copyFile` checks **only** WRITE on the destination folder. The source file is not permission-checked at the Pentaho layer, and Jackrabbit's `workspace.copy()` does not enforce source-read at the JCR level either. Any user who can reference a file by ID and has WRITE on a destination can copy the file, regardless of their permissions on the source.

---

## ❌ Move allowed, copy disallowed (or vice versa)

**What you'd want:** Grant the ability to move files out of a folder (removing them from the source) without allowing duplication via copy.

**Why it doesn't work:**

`moveFile` requires WRITE on both source and destination. `copyFile` requires only WRITE on destination (source not checked). There is no permission that covers "move" independently of the components it is made of. If you have WRITE on source and destination, both move and copy are available. You cannot allow one while blocking the other.

---

## ❌ Execute permission

**What you'd want:** Separate the ability to run/execute a resource (e.g. a report or script) from the ability to read its definition.

**Why it doesn't work:**

The `EXECUTE` value is commented out in `RepositoryFilePermission`. The original design notes reference [JCR-2446](http://issues.apache.org/jira/browse/JCR-2446), which explains that JCR has no native execute semantics. No execute permission exists in the model.

---

## ❌ Creator loses delete rights after upload

**What you'd want:** A "submission folder" — users can upload files but, once submitted, they cannot delete or modify them (e.g. for audit or compliance).

**Why it doesn't work:**

The user who creates a file becomes its **owner**. The owner always receives `jcr:all` via the owner magic ACE at privilege evaluation time, regardless of what the ACL says. This covers all JCR-native operations (read, write, delete) but does **not** include `pho:aclManagement` (a custom Pentaho privilege outside the JCR tree). Owners cannot change permissions on their own nodes unless they also have an explicit `ALL` ACE.

---

## ❌ Delete a folder without enabling deletion of its children (via UI)

**What you'd want:** Grant a user the ability to delete folder `F` itself, but prevent them from deleting `F`'s children.

**Why it doesn't work via the UI:**

The UI hierarchy forces `DELETE ⊃ WRITE`. Granting DELETE on `F` in the UI always also grants WRITE on `F`. WRITE on `F` maps to `jcr:removeChildNodes`, which triggers the inheritance injection in `PentahoEntryCollector` — granting `jcr:removeNode` on every inheriting child of `F`. So DELETE on `F` via the UI = delete `F` + delete all of `F`'s inheriting children.

**API-only workaround:** Programmatically set an ACL on `F` with DELETE but not WRITE. Even then, the JCR move-to-trash still requires `jcr:removeChildNodes` on `F`'s parent, so the parent's WRITE is still needed for the actual deletion.

---

## ❌ Owner cannot isolate their own folder from parent permissions

**What you'd want:** A user creates a folder inside a shared parent. They own it, so they should be able to "lock it down" — turn off "Inherit Permissions" and set their own ACL, preventing others who have WRITE on the parent from accessing its contents.

**Why it doesn't work:**

Changing the `isEntriesInheriting` flag (the "Inherit Permissions" toggle) requires `ACL_MANAGEMENT`. Ownership only grants `jcr:all`, which does **not** include `pho:aclManagement`. A user who merely created the folder — without an explicit `ALL` ACE — cannot modify its ACL or toggle inheritance, even though they own it.

**Consequence:** The folder inherits its parent's permissions by default and stays that way. Anyone with WRITE on the parent can delete the folder (via the inheritance injection rule), and the owner has no way to prevent this unless an admin grants them `ALL` on their own folder.

**Workaround:** An administrator must explicitly grant `ALL` to the user on the folder after creation, enabling the user to then manage its ACL independently.

---

