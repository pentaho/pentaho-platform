---
type: reference
title: IUnifiedRepository ACL Model
description: How RepositoryFileAcl, RepositoryFileSid, and ACL metadata are structured and stored in JCR.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# ACL Model

```mermaid
classDiagram
    class RepositoryFileAcl {
        owner : RepositoryFileSid
        isEntriesInheriting : boolean
        aces : RepositoryFileAce[]
    }
    class RepositoryFileAce {
        sid : RepositoryFileSid
        permissions : EnumSet~RepositoryFilePermission~
    }
    class RepositoryFileSid {
        <<USER or ROLE>>
        name : String
    }
    RepositoryFileAcl "1" --> "1" RepositoryFileSid : owner
    RepositoryFileAcl "1" --> "*" RepositoryFileAce : aces
    RepositoryFileAce "1" --> "1" RepositoryFileSid : sid
```

## RepositoryFileAcl
- Attached to every node (file or folder).
- Has an **owner** (`RepositoryFileSid`).
- Has a list of **ACEs** (`RepositoryFileAce`): each ACE is a `(RepositoryFileSid, EnumSet<RepositoryFilePermission>)` pair.
- Has **`isEntriesInheriting`** flag (see Inheritance below).

## RepositoryFileSid
- Represents a user or role.
- Type: `USER` or `ROLE`.

## Stored in JCR
- ACL metadata (owner, entriesInheriting flag) stored as a special ACE entry via `AclMetadata` / `JcrRepositoryFileAclUtils`.
- The actual JCR ACL is stored using Jackrabbit's `AccessControlList` on the node path.


