---
type: reference
title: Disambiguating getFile / getFileById / getData* / getAvailableLocalesForFile* / getLocalePropertiesForFile*
description: Public-API-only disambiguation recipe for `IUnifiedRepository`'s getFile / getFileById / getData* / getAvailableLocalesForFile* / getLocalePropertiesForFile* operation(s).
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Disambiguating getFile / getFileById / getData* / getAvailableLocalesForFile* / getLocalePropertiesForFile*

**`getFile` / `getFileById` (all overloads), `getData*`, `getAvailableLocalesForFile*`,
`getLocalePropertiesForFile*`** — no exception is thrown at all; not-found, no-read, and
custom-voter-denied all confound into the same silent `null` (main doc §3/§4). **There is
no follow-up call that helps here**: these methods *are* the check, so calling any of them
again on the same path returns the same ambiguous `null`. This ambiguity is unresolvable
via public API.

```java
RepositoryFile file = unifiedRepository.getFileById(fileId);
if (file == null) {
    // Not found, no jcr:read, or a custom accessVoterManager voter denied READ.
    // No further public-API call can distinguish between these three.
}
```
