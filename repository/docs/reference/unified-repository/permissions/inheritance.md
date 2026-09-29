---
type: reference
title: IUnifiedRepository ACL Inheritance
description: How ACL inheritance is resolved between a node and its ancestors, including getEffectiveAces.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Inheritance

```mermaid
flowchart BT
    G["Ancestor G<br/>isEntriesInheriting = false<br/>(explicit ACEs stored here)"]
    P["Parent P<br/>isEntriesInheriting = true"]
    X["Node X<br/>isEntriesInheriting = true"]
    X --> P --> G
    G -. "effective ACEs used for X" .-> X
```

- When `RepositoryFileAcl.isEntriesInheriting() == true`:  
  No explicit ACEs are stored for that node in JCR (only the metadata ACE — see Magic ACEs below).  
  `PentahoEntryCollector` walks up to the nearest non-inheriting ancestor and uses its ACEs.
- When `isEntriesInheriting() == false`:  
  The node has explicit ACEs stored in JCR. Jackrabbit uses them directly for privilege evaluation.


At check time, `session.getAccessControlManager().hasPrivileges(path, privs)` is called.  
**`PentahoEntryCollector`** (Jackrabbit's custom `EntryCollector`) intercepts this and injects Magic ACEs before returning the effective privilege set. See below.

## `getEffectiveAces(fileId, forceEntriesInheriting)`
- `forceEntriesInheriting=true`: walks up ancestor nodes collecting ACEs until it finds a node with `entriesInheriting=false`.
- Returns the merged effective set visible to the caller.

