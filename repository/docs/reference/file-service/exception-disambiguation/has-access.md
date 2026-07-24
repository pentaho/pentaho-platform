---
type: reference
title: Disambiguating hasAccess
description: Public-API-only disambiguation recipe for `FileService`'s hasAccess operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating hasAccess

**`hasAccess`** — the public `unifiedRepository.hasAccess()` (used directly by
`RepositoryFileProvider`, not via `FileService`, which has no equivalent wrapper): same
as the main doc's `hasAccess` row — `false` for not-found and for no-access are the same
value and not distinguishable from `false` alone.
