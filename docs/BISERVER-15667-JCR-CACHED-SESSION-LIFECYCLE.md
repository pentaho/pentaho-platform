# BISERVER-15667: JCR cached-session lifecycle

This document records the problem behind BISERVER-15667 (pull request #6371), why it happened, what was changed, how the change was verified, and what it deliberately does not cover. It is written for someone who finds this code later and needs to know why it looks the way it does.

All code is in module `repository`, package `org.pentaho.platform.repository2.unified.jcr.sejcr`.

## 1. Background: how sessions are cached

Jackrabbit sessions are expensive to create, so `GuavaCachePoolPentahoJcrSessionFactory` keeps a Guava `LoadingCache` of sessions keyed by *(user, thread)*. A session is handed to `PentahoJcrTemplate.execute(...)`, which runs a callback with it. A session can also be bound to a Spring transaction (or to a read-only `SUPPORTS` synchronization) and then be used by several callbacks over the life of that operation.

The cache evicts by size (`cache-size`, default 100) and by idle time (`cache-ttl`, default 300 seconds). Those values are read from the `repository` system configuration in the factory constructor, via `ISystemConfig.getConfiguration( REPOSITORY_CONFIG_ID )`, before the Guava cache is created. The production defaults live in [assemblies/pentaho-solutions/src/main/resources/pentaho-solutions/system/repository.spring.properties](../assemblies/pentaho-solutions/src/main/resources/pentaho-solutions/system/repository.spring.properties), where they are defined as `cache-size=100` and `cache-ttl=300`. Eviction can happen at any moment, on any thread that touches the cache.

## 2. The problem

Before this change, ownership of a cached session was tracked only by a `usage_count` session attribute. When Guava evicted an entry, the removal listener logged the session out only if it was live and the counter was zero; otherwise it logged "may be orphaned" and did nothing.

That has two failure modes, and neither can be fixed by tuning the counter:

- **Closing a session that is in use.** The counter is incremented by the factory *after* the cache returns the session, and again by the template. An eviction between those steps, or a bound session between two callbacks, sees a count of zero and logs the session out underneath its owner. Symptoms are "session has been closed" style failures in the middle of an operation.
- **Leaking a session that was in use.** When the listener skips the logout because the count is non-zero, nothing ever logs the session out later, because the owner has no way to learn that the entry was evicted. The Jackrabbit session stays open until the JVM exits.

The cache holds a bare `Session`, so there is nowhere to record "this was evicted, close it when the last user is done" and no atomic way to decide between "in use" and "free".

### Further defects found while fixing it

Reproducing and reviewing the fix exposed other defects, in the original code or in intermediate versions of the fix. Each is covered by a test:

| Defect | Consequence |
|---|---|
| Cache size and TTL were read *after* the cache was built (a field initializer used the defaults) | Configured `cache-size` / `cache-ttl` were ignored because the values were being loaded from `ISystemConfig` too late |
| `credentialsStrategySessionFactory` was never assigned, so the thread-bound-session guard compared against `null` | The guard that stops a transaction-bound session being handed out again never fired |
| The thread-bound guard compared the cached session with the object Spring binds, which is the logout-suppressing proxy from `CredentialsStrategySessionFactory` | Even once wired, the guard could never match in production; it now unwraps the proxy before comparing |
| A transaction begin takes a factory lease that only a template callback consumes | A read-only transaction that never ran a callback kept the lease forever, so the session could not be closed after eviction; `PentahoTransactionManager` now hands an unconsumed lease back on completion |
| A session whose `refresh` failed stayed in the cache | Every later caller got the same broken session |
| Direct-login fallback sessions, and sessions whose proxy or listener setup failed, were never closed | Leaked sessions |
| Eviction logout ran synchronously inside Guava's removal notification | An unrelated caller loading its own session paid for another user's slow `logout()` |
| Cleanup ran on an unbounded cached thread pool | Slow or stuck logouts could each start a native thread |
| A bound session was closed between two callbacks of the same Spring operation | Premature close (found in the first version of the fix) |
| Cache acquisition retried without limit when every returned entry was evicted | Unbounded logins under continuous eviction (found in the first version of the fix) |
| DEBUG logging cast credentials to `SimpleCredentials` | `ClassCastException` for `GuestCredentials` |
| In CE, `PentahoTransactionManager` was not wired into the session cache | A `REQUIRES_NEW` transaction could be handed the session of the transaction it suspended (section 4.5) |
| `RepositoryFileProxy` objects are resolved lazily, after the callback that created them, and nothing kept their cached session open | Once eviction closed that session, the lazy read failed: `This session has been closed` logged as ERROR, a missing value, or an HTTP 500 on a listing (section 4.6) |

## 3. Solution overview

Each cached session is wrapped in a `CachedJcrSession` that counts who is using it and remembers whether it was evicted. Eviction *retires* the holder; the session is logged out exactly once, when the last user lets go. Everything else follows from that.

```mermaid
stateDiagram-v2
    [*] --> Live: loaded by the cache
    Live --> Retired: evicted, invalidated, or found unusable
    Retired --> LoggedOut: last lease released
    Live --> LoggedOut: retired with no leases
```

### 3.1 Leases

| Lease | Taken by | Released by |
|---|---|---|
| Factory hand-off (`acquire()`) | The cache factory when it returns a cached session | Consumed by the template (`consumeFactoryLease()`), or rolled back (`releaseUnhandedFactoryLease()`) if the hand-off fails |
| Callback use | The template, for the duration of `execute(...)` | `release()` in the template's `finally` block |
| Bound use (`acquireForBoundUse()`) | The template, when the session is already Spring-bound or is bound by this call | A transaction synchronization, in `afterCompletion`, once per holder per transaction scope |
| Lazy read (`SessionLease`) | `RepositoryFileProxy`, for the duration of one lazy read outside any callback | `SessionLease.close()` in the proxy's `try`-with-resources; closing the last lease of a retired session logs it out |

Retirement only blocks *new* cache acquisitions. A callback that already holds the session, or a nested callback on a session Spring has bound, may still extend it while an earlier lease keeps it alive. A holder whose final lease has been released cannot be revived.

### 3.2 Components

- **`CachedJcrSession`**: the per-session state machine (`acquire`, `consumeFactoryLease`, `acquireForBoundUse`, `release`, `releaseUnhandedFactoryLease`, `retire`). State changes are synchronized per holder; `isLive()` and `logout()` run outside the monitor. Logout is claimed at most once, a session that is already closed is not logged out again, and a failing `logout()` is contained.
- **`GuavaCachePoolPentahoJcrSessionFactory`**:
  - Caches holders, not bare sessions; the loader attaches the holder and the legacy `usage_count` diagnostic counter to each session.
  - Reads size and TTL *before* building the cache. A size or TTL of zero selects a managed direct login instead of caching.
  - The removal listener retires holders asynchronously (section 4.1).
  - Sends credentials that are not `SimpleCredentials` (for example `GuestCredentials`) straight to a managed direct login, because the cache key is built from `SimpleCredentials`.
  - Tries the cache at most three times (a lookup can lose a race with eviction), then falls back to a managed direct login. Fallback sessions are tracked and retired up front so they close when the callback and any Spring binding finish.
  - Discards entries whose session is closed, already bound to a transaction, or fails `refresh`, rolling back the factory lease.
- **`CredentialsStrategySessionFactory`**: wires itself and the transaction manager into its cache delegate, whether the delegate is created lazily or injected. If proxy creation or listener registration fails, the session's diagnostic count is rolled back and its lease is released even if that counter cannot be read (or a direct session is logged out), and the original exception is rethrown. The logout-suppressing proxy forwards `logout()` only while the session is still live: Spring runs transaction synchronizations (which can release the last lease and log the session out) before the transaction manager closes its bound proxy, so the manager's later `logout()` must not reach the session a second time.
- **`PentahoJcrTemplate`**: distinguishes the factory hand-off from reuse of a bound session; registers a `TransactionSynchronization` that holds the bound lease until `afterCompletion` (so a session stays open between callbacks, across nested callbacks and across a suspended transaction); releases leases in `finally`; and never lets cleanup failures replace the callback's own exception.
- **`PentahoTransactionManager`**: registers itself with its session factory and reports when a `REQUIRES_NEW` transaction is being begun (section 4.5).
- **`SessionLease`** (public, same package): a short claim on a cached session for code that uses it outside a template callback. `tryAcquire( session )` returns a lease, a no-op lease for a session without a lifecycle holder, or nothing when the session is already closed or closing. It reuses `acquireForBoundUse()`, so no new lifecycle state exists.
- **`RepositoryFileProxy`**: each lazy read holds a `SessionLease` while it runs. If no lease can be taken, or the session is no longer live, it reloads its node through the template exactly as before (section 4.6).

## 4. Design decisions

### 4.1 Eviction cleanup is asynchronous, on a bounded pool

Guava runs removal notifications on whichever thread triggered the eviction. `logout()` can do repository I/O, so the listener hands cleanup to a shared executor, and an unrelated caller is never delayed by another user's slow logout.

The pool has `MAX_CONCURRENT_EVICTION_CLEANUPS` (4) daemon threads that time out when idle. Cleanups beyond four wait in an **unbounded queue**. That is deliberate: a retired session must always be logged out eventually, so a cleanup is never rejected or dropped (that would leak the session) and never run on the evicting thread (that would bring back the latency). The queue only holds retired holders and grows only as fast as entries are evicted. The cost is that four stuck logouts silently stall cleanup until they return; there is no metric or warning for queue depth.

Because cleanup is asynchronous, **code and tests must not assume a session is closed the instant it is evicted**. Tests wait on a logout latch or a bounded deadline.

### 4.2 Why a holder rather than a smarter counter

Deciding "in use or free" and "evicted or not" has to be one atomic step, and the party that finishes last has to be the one that closes the session. A counter on the session cannot do either: the eviction listener and the last user race, and neither can tell the other what it saw. The holder serializes both decisions.

### 4.3 Bounded retries and managed fallback

Under continuous eviction, retrying the cache forever would log in forever. Three attempts then a direct login bounds the work and still gives the caller a session whose lifecycle is managed.

### 4.4 Failures during cleanup never mask the real failure

Releasing leases, decrementing diagnostic counters, logging out and discarding a failed hand-off are all contained: a failure is logged and the callback's own exception (or result) is what the caller sees. A missing diagnostic attribute must not prevent the lease from being released.

### 4.5 `REQUIRES_NEW` transactions get their own session

`PentahoTransactionManager` was introduced in 2016 so the cache could tell that a transaction was being created, and it was wired into the Spring configuration. This applies to the Community Edition (CE) only. In CE, the 2017 assembly restructuring carried an older copy of `repository.spring.xml` that still used `LocalTransactionManager`, so the wiring was lost and CE has run without it since.

The Enterprise Edition (`pentaho-platform-ee`) was not affected: its own `repository.spring.xml` still declares `PentahoTransactionManager` for both the user and the admin transaction manager, and passes `jcrTransactionManager` to the user session factory's cache. EE therefore needs no configuration change for this fix.

In CE, without the wiring, a `REQUIRES_NEW` transaction begun while another is suspended on the same thread was given the cached session the suspended transaction still owned. Spring unbinds the outer transaction's session holder during the suspension, so the thread-bound guard cannot see it. In practice the inner transaction fails to begin (`CannotCreateTransaction`); if it did begin on the shared session, its commit would save the outer transaction's unsaved changes.

The fix restores the wiring in CE, narrowly:

- `PentahoTransactionManager.setSessionFactory` registers the manager with its `CredentialsStrategySessionFactory`, which passes it to the cache delegate. `repository/src/main/resources/repository.spring.xml` and the deployed `assemblies/pentaho-solutions/.../system/repository.spring.xml` now declare `PentahoTransactionManager` for the user and admin managers.
- `isCreatingIndependentTransaction()` is true only while a `REQUIRES_NEW` transaction is being begun. Only then does the cache factory perform a direct login that never enters the cache.
- **`REQUIRED`, `NESTED` and `SUPPORTS` keep using the cached session in CE, as before.** The 2016 predicate, `isCreatingTransaction()`, also bypassed the cache for `REQUIRED` and `NESTED`. Restoring that would make every transaction log in afresh, a platform-wide cost and behavior change. When it was tried, one existing integration test (`JcrAclNodeHelperIT.canDeleteTargetIfAclNodeRemoved`) failed, because it relies on a lazily loaded `RepositoryFileProxy` keeping a live session. If it is ever restored it needs its own performance measurement and that test adjusted.

### 4.6 Lazy reads hold a lease instead of racing eviction

`RepositoryFileProxy` is returned to callers and resolves its fields (name, title, dates, metadata, id) on first use, often while the response is being built, long after the callback ended. Its `executeOperation` checked `session.isLive()` and then used the session. Nothing kept the session open between the two steps, so eviction could log it out in between. Because every lazy getter catches `RepositoryException` and logs it, the symptom was an ERROR line plus a missing value, and sometimes an `IllegalStateException` from Jackrabbit's access manager that surfaced as an HTTP 500.

The read now takes a `SessionLease` first. Eviction can retire the session while the read runs, but the logout waits for the lease to close. If the lease cannot be taken (the session is already closed or closing) or the session is not live, the proxy reloads its node through the template, as it already did for a closed session.

A retry was considered and rejected. The getters swallow the exception inside the operation, so a retry in `executeOperation` would first require reworking about 25 getters. A grace period before logout only narrows the window. The lease closes it with an existing primitive and about 30 changed lines.

A retired session whose last lease is a lazy read is logged out on the reading thread when that read ends, so the next lazy read on the same proxy reloads. That costs one reload per lazy read under constant eviction, which is the expected price of a tiny cache.

## 5. Behavior and configuration notes

- `cache-size` and `cache-ttl` (repository system configuration) now take effect. Setting either to `0` disables caching: each use is one managed direct login that closes when its callback and any Spring binding finish.
- A session that Spring has bound to a transaction stays open until the transaction completes, even if it is evicted meanwhile.
- A session logged out outside the cache is discarded and replaced on the next acquisition.
- The Spring files under `extensions/src/test/resources` and `extensions/src/it/resources` are test harness copies and still use `LocalTransactionManager`; they do not get the `REQUIRES_NEW` isolation.

## 6. Verification

### 6.1 Test layout

- **Unit tests** (`repository/src/test`, Surefire): `CachedJcrSessionTest` (lease transitions, retirement, no revival, logout failures, concurrent retirement against final release), `SessionLeaseTest` (lease on tracked, untracked and closed sessions, deferred logout, idempotent close), `RepositoryFileProxyLeaseTest` (a lazy read holds its lease, reloads when the session is closing, keeps the old behavior for untracked sessions, releases the lease when the read fails), `GuavaCachePoolPentahoJcrSessionFactoryTest` and `GuavaCachePoolPentahoJcrRaceConditionTest` (configuration, bounded retries, refresh and fallback cleanup, closed-session replacement, the bounded and non-blocking asynchronous eviction, the `REQUIRES_NEW` bypass), `PentahoJcrTemplateTest` (usage accounting, security translation, bound-lease retention until completion, unusable and unprotectable sessions, contained cleanup failures), `CredentialsStrategySessionFactoryTest` (delegate wiring, failed hand-off cleanup), `PentahoTransactionManagerTest`.
- **Integration tests** (`repository/src/it`, Failsafe, activated with `-DrunITs`): `GuavaCachePoolPentahoJcrOrphanedSessionIT` runs against a real Jackrabbit repository: eviction during and after callbacks, failing callbacks, externally closed sessions, read-only and nested bound callbacks, commit and rollback, 12 workers × 16 rounds of concurrent real sessions, lazily loaded files and binary streams that outlive their session, `REQUIRES_NEW` suspension and resumption, the Spring wiring, and `REQUIRES_NEW` isolation. `RepositoryFileProxyEvictionIT` runs against a real Jackrabbit repository too: one test retires the session at the first repository access inside each lazy read (the deterministic form of eviction landing between the liveness check and the read), and one reads proxies from 8 threads while another thread keeps evicting the cache. `GuavaCachePoolPentahoJcrSessionStressIT` and `CachedJcrSessionStressIT` are opt-in stress cases (50,000 operations per scenario; 200,000 simultaneously live holders). They use lightweight session fixtures, not 200,000 real Jackrabbit sessions.

To run everything: `mvn --batch-mode -o -pl repository -DrunITs verify`.

### 6.2 Evidence that the tests detect the defects

- Against the original implementation, the real-repository concurrency case leaves evicted sessions open after their callbacks, and a read-only `SUPPORTS` operation had its session closed between callbacks by the first version of the fix. Both fail without the fix.
- With the old XML, the two `REQUIRES_NEW` integration tests fail: the wiring check, and the isolation test, which fails with `CannotCreateTransaction`.
- Temporarily restoring the unchecked `SimpleCredentials` cast reproduces the `GuestCredentials` DEBUG failure.
- Reverting only the `RepositoryFileProxy` change makes both `RepositoryFileProxyEvictionIT` cases fail (the lazy name comes back `null`; created dates and ids go missing under concurrent eviction) and two `RepositoryFileProxyLeaseTest` cases fail.

### 6.3 Results for this change

The full module run passed 437 unit tests and 170 integration tests with no failures, errors or skips.

**Manual validation on a running server.** A Pentaho 11.1 server was driven by 60 users and 60 parallel clients (2,400 calls of create, list, properties, download and tree per run), with the cache cut to 3 entries and a 2 s TTL to force constant eviction. The pre-change row is master with only its hard-coded cache defaults reduced, because the original code ignores the configured values. Each row is one run, so read the numbers as indicative.

| Build | Calls with HTTP 200 | HTTP 500 | `RepositoryFileProxy` errors logged | "session has been closed" lines |
|---|---|---|---|---|
| Pre-change code | 1,233 | 582 | 37 | 541 |
| This PR without the lazy-read lease | 2,271 | 120 | 661 | 706 |
| This PR with the lazy-read lease | 2,400 | 0 | 0 | 0 |

With the default cache (100 entries, 300 s) both the pre-change code and this PR returned every call with 200 at 60 and at 150 parallel clients. Without the lazy-read lease, this PR logged one "session has been closed" in the 150-client run. Live session objects fall back to a bounded number a few seconds after the load stops (about 127 with the tiny cache), so there is no leak. A reading taken immediately after the load is inflated while eviction cleanup is still running.

A microbenchmark comparing the original and the fixed lifecycle logic with lightweight sessions (two JVM forks per revision, identical harness, 400,000 operations per sample) showed no regression: cache-hit throughput was within run-to-run noise (about 5.1 M versus 5.5 M ops/s), churn throughput was unchanged (about 805 K ops/s), and allocation per 400,000 operations was lower (churn: about 267 MiB versus 218 MiB). It measured lifecycle bookkeeping only and was taken before the bounded eviction pool and the `REQUIRES_NEW` bypass; it says nothing about repository I/O, disk-backed throughput or real concurrent sessions.

## 7. Known limits

- `RepositoryFileProxy` lazy reads are protected against eviction by a `SessionLease` (section 4.6). Other objects that outlive their callback and read from the session later, such as returned binary streams, are not leased. Closed-session reload and returned-stream compatibility are tested, but a race between such a read and eviction is not claimed to be solved. A session that something outside the lease model logs out directly, such as the transaction manager closing its bound proxy, can still be closed under a lazy read; the proxy then reloads.
- Sessions that are not Jackrabbit `SessionImpl` cannot carry the holder attribute, so they are not lifecycle-tracked. Raw consumers of the factory and transactions that never enter `PentahoJcrTemplate` were not established as safe.
- A `logout()` that throws is contained and logged, and exactly-once initiation is preserved, but the repository may not have freed that session.
- Eviction cleanup has no queue-depth metric (section 4.1).
- `REQUIRED` and `NESTED` transactions still share the thread's cached session (section 4.5).
