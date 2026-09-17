---
type: reference
title: IUnifiedRepository Permission Hierarchy (UI Vs API)
description: The cumulative READ/WRITE/DELETE/ACL_MANAGEMENT/ALL hierarchy enforced by the sharing UI, and what the API additionally allows.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Permission hierarchy (UI vs API)

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
