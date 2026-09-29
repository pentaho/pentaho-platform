---
type: reference
title: getAllDeletedFiles / getReservedChars (No Disambiguation Needed)
description: Why `getAllDeletedFiles` and `getReservedChars` need no exception disambiguation.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# getAllDeletedFiles / getReservedChars (No Disambiguation Needed)

**`getAllDeletedFiles` / `getReservedChars`** — no disambiguation needed: neither has an
ABS guard, a voter check, nor a documented failure path; inaccessible entries in
`getAllDeletedFiles` are silently filtered by the Jackrabbit session itself, and
`getReservedChars` is pure in-memory data with no repository access at all.
