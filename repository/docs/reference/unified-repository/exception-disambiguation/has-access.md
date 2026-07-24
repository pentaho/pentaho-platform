---
type: reference
title: Disambiguating hasAccess
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s hasAccess operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating hasAccess

**`hasAccess`** — never throws for not-found (returns `false` uniformly for any
permission set, including for a node that doesn't exist). There is nothing to
disambiguate: `hasAccess` **is** the check, and no other public call adds information.
