# BISERVER-15667: JCR cached-session regression investigation

**Ticket:** BISERVER-15667 (pull request #6371). The branch is still named `BISERVER-14405-session-cleanup`: it was created for the earlier BISERVER-14405 key and keeps that name so the open pull request is unchanged. All commits and this document use BISERVER-15667.
**Investigation date:** 2026-10-07 (review follow-ups: 2026-10-13)
**Repository:** `pentaho-platform`, module `repository`
**Purpose:** Adversarial review of deferred JCR session cleanup, including ownership correctness, failure handling, concurrency, load and compatibility.

## Executive summary

The original defect was that cache eviction could log out a Jackrabbit session while a template callback or Spring transaction still owned it. Testing also uncovered regressions in the proposed deferred-cleanup change itself: Spring-bound sessions could be closed between callbacks, and cache acquisition could retry indefinitely under continuous eviction. These were repaired and covered.

Other verified repairs include failed refresh/factory-handoff/fallback cleanup, applying configured cache size/TTL before constructing the cache, and avoiding an unsafe `SimpleCredentials` cast in DEBUG logging when `GuestCredentials` triggers direct fallback.

Pull request review then found three further problems, all addressed (see [Pull request review follow-ups](#pull-request-review-follow-ups)): the eviction cleanup pool was unbounded, `REQUIRES_NEW` transactions could be handed the cached session of the transaction they suspend because the transaction manager was never wired into the cache, and several tests asserted asynchronous cleanup without waiting for it.

The final full local run of `mvn --batch-mode -o -pl repository -DrunITs verify` passed **424 unit tests and 167 integration tests**, with zero failures, errors or skips. Earlier coverage and benchmark figures below were measured before the review follow-ups and are kept as historical evidence; they are not a SonarQube analysis.

## Ticket, branch and baseline

- Ticket: BISERVER-15667. Branch: `BISERVER-14405-session-cleanup` (name retained from the earlier key; see above).
- Base: `master` at `a5b097979c42db131345c2e55e4e06dd1e37615c`.
- Baseline and fixed real-repository tests share a legacy HSQL/Jackrabbit fixture. Run them sequentially, not concurrently.

## Pull request review follow-ups

Review of pull request #6371 (Copilot and SonarQube) raised the following, each now fixed and covered.

### Unbounded eviction cleanup threads

Eviction cleanup ran on a cached thread pool, so overlapping slow or stuck `logout()` calls could each start a new native thread. It now runs on a fixed pool of `MAX_CONCURRENT_EVICTION_CLEANUPS` (4) daemon threads that time out when idle. Cleanups beyond that wait in an unbounded queue: a retired session must always be logged out eventually, so cleanup is never rejected, dropped, or run on the thread that triggered the eviction (which would reintroduce acquisition latency). The queue grows only as fast as entries are evicted and holds only retired holders awaiting logout. `concurrentEvictionCleanupsAreBoundedQueuedAndAllCompleted` holds every logout behind a latch and asserts the pool bound, the queued remainder, and eventual completion.

### Transaction manager not wired into the session cache

`GuavaCachePoolPentahoJcrSessionFactory` can bypass its cache while a transaction is being created, but the production configuration never gave it the transaction manager: the default delegate passed `null`, and `repository.spring.xml` declared a plain `LocalTransactionManager`. History shows this is not new to this branch. `PentahoTransactionManager` and the wiring were added in BACKLOG-11059 (2016); the 2017 assembly move (`692c857268`) carried an older copy of the XML and dropped the wiring, so `master` has behaved this way since.

The consequence is real: a `REQUIRES_NEW` transaction begun while another transaction is suspended on the same thread is handed the cached session the suspended transaction still owns, because the thread-bound guard cannot see a session whose holder Spring has unbound for the suspension. The new integration test showed the inner transaction failing to begin (`CannotCreateTransaction`), and sharing would otherwise let the inner commit save the outer transaction's unsaved changes.

The fix is deliberately narrow:

- `PentahoTransactionManager.setSessionFactory` registers the manager with its `CredentialsStrategySessionFactory`, which passes it to its cache delegate, whether that delegate is created lazily or injected. `repository.spring.xml` and the deployed `pentaho-solutions/system/repository.spring.xml` now use `PentahoTransactionManager` for the user and admin managers.
- A new `isCreatingIndependentTransaction()` is true only while a `REQUIRES_NEW` transaction is being begun, and only then does the cache factory log in directly. `REQUIRED`, `NESTED` and `SUPPORTS` keep using the cached session exactly as before.
- The original 2016 predicate, `isCreatingTransaction()`, also bypassed the cache for `REQUIRED` and `NESTED`. That was not restored: it would make every transaction perform a fresh repository login, a platform-wide cost and behavior change that is out of scope here. It was tried, and one existing integration test (`JcrAclNodeHelperIT.canDeleteTargetIfAclNodeRemoved`) depends on the cached behavior through a lazily loaded `RepositoryFileProxy`. Restoring it is better handled as a separate, measured change.

Covered by `springConfigurationWiresTheTransactionManagerToTheSessionCache` and `requiresNewTransactionDoesNotShareOrCommitTheSuspendedTransactionsChanges` (both fail against the previous XML), plus unit tests for the registration paths and the bypass.

### Asynchronous cleanup in tests

Because the removal listener runs asynchronously, assertions that a session was already closed immediately after an eviction were racy. Unit tests now wait on a logout-completion latch, and the real-repository and stress integration tests wait for closure with a bounded deadline. The latency test no longer uses `Thread.sleep` or a wall-clock threshold: the slow logout is held behind a latch while an unrelated acquisition runs on another thread. The deterministic bounded-retry test also retires the returned holder directly, since the removal listener no longer retires it before the next acquisition.

### SonarQube findings

The seven open issues were fixed: narrower exception types instead of `throws Exception`, `assertNotSame`, removal of `Thread.sleep` and redundant Mockito `eq(...)`, an unnecessary `throws` clause, and the deprecated JUnit `assertThat`. New-code coverage was raised with unit tests for the Spring-bound lease and unusable-session paths in `PentahoJcrTemplate`, failed-handoff cleanup in `CredentialsStrategySessionFactory`, closed-session replacement, configuration fallbacks, and the transaction-manager wiring. A new SonarQube analysis is needed to confirm the gate; none has been run against this revision locally.

## Lifecycle design and changes

### Holder and eviction

[CachedJcrSession.java](repository/src/main/java/org/pentaho/platform/repository2/unified/jcr/sejcr/CachedJcrSession.java) is the per-session lifecycle coordinator. It tracks active-use leases, pending factory-to-template handoffs, retirement, and whether logout has started. State transitions are synchronized per holder; `isLive()` and `logout()` execute outside that monitor. Once retired, the session is logged out when the final active owner releases it. Logout initiation is exactly once, and a closed session is not logged out again.

### Cache factory

[GuavaCachePoolPentahoJcrSessionFactory.java](repository/src/main/java/org/pentaho/platform/repository2/unified/jcr/sejcr/GuavaCachePoolPentahoJcrSessionFactory.java):

- Retires the holder from the real Guava removal listener instead of immediately logging out a potentially active session.
- Builds the cache only after reading configured size and TTL; zero size or TTL uses one managed, uncached login rather than cache/retry churn.
- Limits acquisition to three attempts, then uses a managed direct-login fallback.
- Rolls back unhanded leases on stale, failed or evicted acquisitions.
- Retires fallback sessions immediately; callback and optional Spring-binding leases determine final disposal.
- Keeps per-thread credential partitioning and uses a static immutable cache key with primitive `long` thread ID.
- Handles `GuestCredentials` safely in DEBUG usage logging.

### Factory handoff and template ownership

[CredentialsStrategySessionFactory.java](repository/src/main/java/org/pentaho/platform/repository2/unified/jcr/sejcr/CredentialsStrategySessionFactory.java) rolls back lifecycle and diagnostic protection if proxy/listener handoff fails.

[PentahoJcrTemplate.java](repository/src/main/java/org/pentaho/platform/repository2/unified/jcr/sejcr/PentahoJcrTemplate.java) retains the lifecycle holder locally, distinguishes factory handoff from bound-session reuse, retains a separate Spring transaction lease until `afterCompletion()`, and releases that lease exactly once. Cleanup failures in diagnostic counters do not suppress holder release or replace the callback exception. A synchronization remains associated with a suspended transaction until Spring resumes/completes it.

## Reproduced regressions and red/green evidence

1. **Premature close between Spring callbacks.** A real read-only `SUPPORTS` synchronization was evicted after one callback. The proposed implementation closed the session before Spring finished owning it; the regression assertion failed. A Spring-binding lease through `afterCompletion()` fixes it. Sequential and nested callbacks now reuse the live bound session.
2. **Unbounded acquisition retries.** A deterministic decorator around the real loading cache evicts each returned entry before acquisition. The unbounded implementation continued logging in until the test safety stop; the baseline bounded probe passed. Three attempts followed by an uncached fallback bound the work.
3. **DEBUG `GuestCredentials` failure.** The managed fallback diagnostic path assumed `SimpleCredentials`; temporarily restoring that cast reproduced `ClassCastException`. The safe type check is retained and its DEBUG test passes.
4. **Failure cleanup gaps.** Added tests cover refresh failure/disposal, callback failure on fallback, factory listener handoff failure, unavailable diagnostic attributes, usage-start failure and session acquisition failure.

The separate baseline worktree contains comparison probes, including assertions intentionally expected to fail against the vulnerable implementation. It is not the fixed checkout or part of the fixed branch's normal test discovery. The temporary unsafe-cast red experiment was reverted; no temporary test source was left in the fixed checkout.

## Test inventory: unit tests versus integration tests

Tests are split between Maven Surefire unit tests and opt-in Failsafe integration tests:

- `repository/src/test/java/**` classes named `*Test` run with **Surefire**.
- `repository/src/it/java/**` classes named `*IT` are added and run with **Failsafe** when `-DrunITs` activates the `test-integration` profile.
- The factory/cache/template stress cases use lightweight observable sessions; the holder stress case exercises concurrent holder lifecycle at high cardinality; the lifecycle IT uses a real repository. These are complementary, not substitutes for one another.
- The standalone benchmark is a `main()` method in the factory stress IT source, not a separate temporary test file.

### Added/expanded Surefire coverage

| Class | Executed tests | Main coverage |
|---|---:|---|
| [CachedJcrSessionTest.java](repository/src/test/java/org/pentaho/platform/repository2/unified/jcr/sejcr/CachedJcrSessionTest.java) | 11 | Lease/handoff balance, retirement, nested bound use, no revival, already-closed session, logout failure, unmatched lease rollback and race. |
| [GuavaCachePoolPentahoJcrSessionFactoryTest.java](repository/src/test/java/org/pentaho/platform/repository2/unified/jcr/sejcr/GuavaCachePoolPentahoJcrSessionFactoryTest.java) | 16 | Guest credentials/DEBUG, configured capacity and configuration fallbacks, zero size/TTL, bounded retries, refresh/fallback and listener failure, closed-session replacement, `REQUIRES_NEW` bypass, non-blocking and bounded asynchronous eviction. |
| [GuavaCachePoolPentahoJcrRaceConditionTest.java](repository/src/test/java/org/pentaho/platform/repository2/unified/jcr/sejcr/GuavaCachePoolPentahoJcrRaceConditionTest.java) | 8 | Counter/lifecycle interactions, independent-transaction bypass, failed refresh and nested operations. |
| [PentahoJcrTemplateTest.java](repository/src/test/java/org/pentaho/platform/repository2/unified/jcr/sejcr/PentahoJcrTemplateTest.java) | 12 | Existing usage/security checks, cleanup/acquisition fault tests, Spring-bound lease retention until transaction completion, shared synchronization, unusable and unprotectable sessions, and contained cleanup failures. |
| [CredentialsStrategySessionFactoryTest.java](repository/src/test/java/org/pentaho/platform/repository2/unified/jcr/sejcr/CredentialsStrategySessionFactoryTest.java) | 10 | Lazy and injected cache delegates, transaction-manager registration, and failed proxy/listener handoff cleanup. |
| [PentahoTransactionManagerTest.java](repository/src/test/java/org/pentaho/platform/repository2/unified/jcr/sejcr/PentahoTransactionManagerTest.java) | 4 | Transaction-definition capture, creation predicates, `REQUIRES_NEW`-only independence and factory registration. |

Other existing Surefire classes also ran. Final Surefire XML totals are **424 tests / 74 classes**, zero failures/errors/skips. The two stress classes (three tests total) no longer run during the default unit-test phase.

### Real-JCR Failsafe coverage

[GuavaCachePoolPentahoJcrOrphanedSessionIT.java](repository/src/it/java/org/pentaho/platform/repository2/unified/jcr/sejcr/GuavaCachePoolPentahoJcrOrphanedSessionIT.java) executed **13 tests**, including inherited startup. It covers ordinary/callback-failure eviction, externally closed sessions, sequential/nested read-only callbacks, commit and rollback, concurrent real sessions, escaped lazy repository files and binary streams, outer/inner `REQUIRES_NEW` suspension/resumption, the Spring wiring of the transaction manager, and that a `REQUIRES_NEW` transaction neither shares nor commits the suspended transaction's changes.

The opt-in stress ITs are [GuavaCachePoolPentahoJcrSessionStressIT.java](repository/src/it/java/org/pentaho/platform/repository2/unified/jcr/sejcr/GuavaCachePoolPentahoJcrSessionStressIT.java) (two 50,000-operation correctness scenarios plus the benchmark `main()` entry point) and [CachedJcrSessionStressIT.java](repository/src/it/java/org/pentaho/platform/repository2/unified/jcr/sejcr/CachedJcrSessionStressIT.java) (200,000 simultaneously live holders).

The real concurrent case uses **12 workers × 16 rounds = 192 real sessions**. The fixed implementation passes; the baseline leaves evicted real sessions orphaned instead of closing them after callbacks. The other relevant lifecycle and compatibility cases pass on both revisions.

The final full Failsafe run recorded **167 tests / 17 classes**, with zero failures, errors or skips.

## Full validation and coverage

- The final full repository validation, `mvn --batch-mode -o -pl repository -DrunITs verify`, passed **424 UT / 167 IT** with zero failures, errors or skips.
- Earlier measurements, taken before the pull request review follow-ups and kept as historical evidence: a full run passed **399 UT / 165 IT** in about **2:51** (before the factory stress-loop reduction from 200,000 to 50,000 operations per scenario), and a full instrumented run with a Java-21-compatible JaCoCo 0.8.12 agent scoped to the lifecycle package passed **402 UT / 162 IT** in about **3:02**.
- JaCoCo 0.8.8 inherited from the build cannot instrument Java 21 runtime classes. Global JaCoCo 0.8.12 instrumentation also caused unrelated legacy SOAP tests to fail because SOAP serialization tried to call a getter for the injected `$jacocoData` field. No SOAP test or production code was changed: rerunning with instrumentation scoped to `org.pentaho.platform.repository2.unified.jcr.sejcr.*` avoided this tooling interaction, restored all SOAP tests, and produced fresh coverage data.
- Earlier local current-diff estimate, comparing JaCoCo against `a5b0979` and the production tree at that time:
  - 195/218 changed coverable lines covered (**89.4%**).
  - 89/112 changed branch outcomes covered (**79.5%**).
  - Combined line/branch estimate: **86.1%**.
  - By class: `CachedJcrSession` 91.8%; cache factory 90.6%; credential factory 77.3%; template 77.1%.
- These denominators differ from SonarQube's new-code baseline, which counts only unit-test coverage and reported **73.8%** for the analysis preceding the review follow-ups. The local estimate does not guarantee that Sonar's gate will pass; a new SonarQube analysis of the current head is required.

## Performance and high-cardinality runtime

### Opt-in Failsafe stress-test cost

The latest Failsafe reports after the operation-count reduction show:

- The factory IT originally ran 200,000 operations per scenario and took 0.597 s. It now runs 50,000 per scenario and took **0.423 s** for both tests in the latest Failsafe report (the high-cardinality eviction case was 0.259 s). This cuts operation volume by 75% and reported class time by about 29%; fixed setup/scheduling cost means time does not fall by 75%. The separate 200,000-simultaneously-live-holder IT took **0.206 s**. All three therefore took about **0.629 s** combined test-class time. A standalone, non-instrumented 512 MiB JUnit run before the move/reduction reported 0.637 s; process launch and Maven compilation are excluded.
- **Why 50,000 operations remains useful:** the active-eviction case has 32 workers and invalidates from inside an active callback every 256 callbacks per worker. With 50,000 operations, each worker reaches about six eviction points (roughly 192 worker-triggered invalidations in total), and each invalidation is immediately followed by live-session assertions. This gives repeated concurrent lifecycle checks without using 200,000 loop iterations. The separate 200,000-live-holder IT still exercises the explicit object-cardinality target.

The module itself takes longer to initialize: the full UT+IT build took about **2:51** before the operation-count-only reduction; the subsequent focused Failsafe verification took about **1:29** including the 399 unit tests Maven also ran. The 200,000 objects/sessions in lightweight stress fixtures are not 200,000 real Jackrabbit connections or native threads.

### Matched standalone comparison

Two JVM processes per revision were run in alternating baseline/fixed order. Each used a 512 MiB G1 heap, the same JDK/dependencies and byte-identical benchmark source; each process did two warmups and five measured 400,000-operation rounds for each workload. That is ten measured samples per workload/revision. Logged workload time was approximately **6.62 s baseline** and **6.46 s fixed**, including warmups; measured rounds alone were **5.92 s** and **5.85 s** respectively. Add a small JVM launch/harness overhead; these are not Maven build times.

| Workload median | Baseline | Fixed | Interpretation |
|---|---:|---:|---|
| Cache-hit throughput | 5.07 M ops/s | 5.53 M ops/s | Apparent +9.1%, but noisy; baseline samples ranged 4.00–5.63 M, fixed 4.05–5.81 M. Do not claim a reliable speedup. |
| Cache-hit p99 | 8.52 μs | 6.29 μs | Local lightweight-session measurement only. |
| Cache-hit allocation / 400k ops | 70.20 MiB | 61.05 MiB | Lower in this harness. |
| Churn throughput | 805,155 ops/s | 803,524 ops/s | −0.2%, essentially unchanged. |
| Churn p99 | 286.415 μs | 286.00 μs | Essentially unchanged. |
| Churn allocation / 400k ops | 267.12 MiB | 218.39 MiB | −18.2% in this harness. |
| Churn worker CPU | 3,021 ms | 3,066 ms | +1.5%; GC time was 40 ms for both medians. |

This is a comparative lifecycle microbenchmark, not a storage-I/O benchmark, production capacity test, memory sizing exercise, or proof about hundreds of thousands of real concurrent sessions.

## Remaining boundaries and cautions

- `RepositoryFileProxy` has a pre-existing live-session fast path that can operate on its retained `Node` without going through the template. Closed-session lazy reload and returned-stream compatibility were tested, but the live-getter/eviction race is not claimed to be comprehensively solved.
- Non-`SessionImpl` JCR sessions cannot be attached to the current lifecycle holder in the same way. Plain-template/raw factory consumers and transactions that never enter the cooperating template were not exhaustively established as safe.
- A contained underlying `logout()` exception preserves exactly-once initiation but cannot guarantee the repository successfully freed that resource.
- Do not infer disk-backed throughput, a universal absence of regressions, or capacity for 200,000 real concurrent repository sessions from these tests.
- `REQUIRED` and `NESTED` transactions still use the per-thread cached session, as on `master`; only `REQUIRES_NEW` is isolated. Restoring the full 2016 behavior (a direct login for every transaction) would need its own performance measurement and an adjustment of `JcrAclNodeHelperIT.canDeleteTargetIfAclNodeRemoved`.
