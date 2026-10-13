# BISERVER-14405: JCR cached-session regression investigation

**Investigation date:** 2026-10-07
**Repository:** `pentaho-platform`, module `repository`
**Purpose:** Adversarial review of deferred JCR session cleanup, including ownership correctness, failure handling, concurrency, load and compatibility.

## Executive summary

The original defect was that cache eviction could log out a Jackrabbit session while a template callback or Spring transaction still owned it. Testing also uncovered regressions in the proposed deferred-cleanup change itself: Spring-bound sessions could be closed between callbacks, and cache acquisition could retry indefinitely under continuous eviction. These were repaired and covered.

Other verified repairs include failed refresh/factory-handoff/fallback cleanup, applying configured cache size/TTL before constructing the cache, and avoiding an unsafe `SimpleCredentials` cast in DEBUG logging when `GuestCredentials` triggers direct fallback.

The last full local run before reducing the factory stress-loop count passed **399 unit tests and 165 integration tests**, with zero failures, errors or skips. After that reduction, all 399 unit tests and the three stress IT cases passed in a focused run; the other ITs were not rerun after the count-only change. High-cardinality tests run from the opt-in Failsafe source root rather than the default unit-test suite. A local JaCoCo calculation on the production diff estimated **86.1% combined line/branch coverage**; this uses a local base-to-working-tree diff and **is not a SonarQube analysis**. The published PR scan remains red at 73.7% new-code coverage with five issues on the earlier remote revision.

## Repository and safety state

- Fixed checkout: branch `BISERVER-14405-session-cleanup`, local `HEAD ffa4afa015`.
- Base checkout: `/tmp/pentaho-platform-biserver-14405-leak-it`, at `a5b097979c42db131345c2e55e4e06dd1e37615c`.
- Published draft PR #6371 was read-only verified at remote head `a2935d4c5b8722b76484010662fcb14ffaf36ca9`, based on `master`. The remote differs from the local working HEAD.
- **All additional source and test changes described here are local and uncommitted.** Several added test files currently appear as untracked in the fixed checkout, so they need to be deliberately included if the local work is later committed.
- No remote changes were made during this investigation.
- Baseline and fixed real-repository tests share a legacy HSQL/Jackrabbit fixture. Run them sequentially, not concurrently.

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
| [CachedJcrSessionTest.java](repository/src/test/java/org/pentaho/platform/repository2/unified/jcr/sejcr/CachedJcrSessionTest.java) | 10 | Lease/handoff balance, retirement, nested bound use, no revival, already-closed session, logout failure and race. |
| [GuavaCachePoolPentahoJcrSessionFactoryTest.java](repository/src/test/java/org/pentaho/platform/repository2/unified/jcr/sejcr/GuavaCachePoolPentahoJcrSessionFactoryTest.java) | 8 | Guest credentials/DEBUG, configured capacity, zero size/TTL, bounded retries, refresh/fallback and listener failure. |
| [GuavaCachePoolPentahoJcrRaceConditionTest.java](repository/src/test/java/org/pentaho/platform/repository2/unified/jcr/sejcr/GuavaCachePoolPentahoJcrRaceConditionTest.java) | 8 | Counter/lifecycle interactions, transaction-creation bypass, failed refresh and nested operations. |
| [PentahoJcrTemplateTest.java](repository/src/test/java/org/pentaho/platform/repository2/unified/jcr/sejcr/PentahoJcrTemplateTest.java) | 6 | Existing usage/security checks plus three cleanup/acquisition fault tests. |

Other existing Surefire classes also ran. Final Surefire XML totals are **399 tests / 76 classes**, zero failures/errors/skips. The two stress classes (three tests total) no longer run during the default unit-test phase.

### Real-JCR Failsafe coverage

[GuavaCachePoolPentahoJcrOrphanedSessionIT.java](repository/src/it/java/org/pentaho/platform/repository2/unified/jcr/sejcr/GuavaCachePoolPentahoJcrOrphanedSessionIT.java) executed **11 tests**, including inherited startup. It covers ordinary/callback-failure eviction, externally closed sessions, sequential/nested read-only callbacks, commit and rollback, concurrent real sessions, escaped lazy repository files and binary streams, and outer/inner `REQUIRES_NEW` suspension/resumption.

The opt-in stress ITs are [GuavaCachePoolPentahoJcrSessionStressIT.java](repository/src/it/java/org/pentaho/platform/repository2/unified/jcr/sejcr/GuavaCachePoolPentahoJcrSessionStressIT.java) (two 50,000-operation correctness scenarios plus the benchmark `main()` entry point) and [CachedJcrSessionStressIT.java](repository/src/it/java/org/pentaho/platform/repository2/unified/jcr/sejcr/CachedJcrSessionStressIT.java) (200,000 simultaneously live holders).

The real concurrent case uses **12 workers × 16 rounds = 192 real sessions**. The fixed implementation passes; the baseline leaves evicted real sessions orphaned instead of closing them after callbacks. The other relevant lifecycle and compatibility cases pass on both revisions.

The full Failsafe run before the operation-count reduction recorded **165 tests / 17 classes**, with zero failures, errors or skips. The three opt-in stress tests account for the increase from the earlier 162 tests / 15 classes.

## Full validation and coverage

- A full repository validation with `mvn --batch-mode -o -pl repository -DrunITs verify` passed **399 UT / 165 IT** in about **2:51**, with zero failures, errors or skips. This full run preceded the factory stress-loop reduction from 200,000 to 50,000 operations per scenario.
- After reducing the factory stress-loop count, a focused Failsafe verify passed all 399 UTs and the three selected stress IT cases; the unaffected non-stress ITs were not rerun after this count-only change. Their last full-suite result is from the preceding run.
- A full instrumented run with a Java-21-compatible JaCoCo 0.8.12 agent scoped to the lifecycle package passed **402 UT / 162 IT** in about **3:02**. This preceded moving the stress tests to Failsafe.
- JaCoCo 0.8.8 inherited from the build cannot instrument Java 21 runtime classes. Global JaCoCo 0.8.12 instrumentation also caused unrelated legacy SOAP tests to fail because SOAP serialization tried to call a getter for the injected `$jacocoData` field. No SOAP test or production code was changed: rerunning with instrumentation scoped to `org.pentaho.platform.repository2.unified.jcr.sejcr.*` avoided this tooling interaction, restored all SOAP tests, and produced fresh coverage data.
- Local current-diff estimate, comparing JaCoCo against `a5b0979` and the current production working tree:
  - 195/218 changed coverable lines covered (**89.4%**).
  - 89/112 changed branch outcomes covered (**79.5%**).
  - Combined line/branch estimate: **86.1%**.
  - By class: `CachedJcrSession` 91.8%; cache factory 90.6%; credential factory 77.3%; template 77.1%.
- These denominators differ from the published PR Sonar new-code baseline; the local estimate does not guarantee that Sonar's gate will pass. The remote scan remains **73.7% coverage and five issues**, analyzed on `a2935d4` on 2026-10-06. No new scan was uploaded.
- Final `git diff --check` passed. Current local test files are retained; they were not committed or pushed.

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

