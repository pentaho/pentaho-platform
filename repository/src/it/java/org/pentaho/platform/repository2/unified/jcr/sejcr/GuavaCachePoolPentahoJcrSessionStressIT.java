/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.platform.repository2.unified.jcr.sejcr;

import org.apache.jackrabbit.core.SessionImpl;
import org.junit.Test;
import org.objenesis.ObjenesisStd;
import org.springframework.extensions.jcr.JcrCallback;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.security.auth.Subject;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Exercises the actual factory, Guava cache, credential proxy, and template with cheap, observable session stand-ins.
 * No JCR storage I/O is simulated: the real-repository ITs cover that boundary separately. A bounded worker pool is
 * intentional; hundreds of thousands of objects must not imply hundreds of thousands of native threads or mocks.
 * This opt-in load test stays out of the default Surefire unit-test run.
 */
public class GuavaCachePoolPentahoJcrSessionStressIT {
  private static final ObjenesisStd ALLOCATOR = new ObjenesisStd();
  // The opt-in correctness stress needs repeated cross-worker eviction, not benchmark-grade sample volume.
  private static final int STRESS_OPERATIONS = 50_000;

  /** Verifies active callbacks remain usable and all sessions are retired exactly once during high-cardinality churn. */
  @Test
  public void highCardinalityEvictionDoesNotCloseActiveCallbacksOrOrphanSessions() throws Exception {
    runWorkload( 32, 256, STRESS_OPERATIONS, true );
  }

  /** Verifies repeated cache hits reuse one session per worker and balance all callback leases. */
  @Test
  public void cacheHitsRemainReusableAndBalanceAllLeases() throws Exception {
    Result result = runWorkload( 16, 1, STRESS_OPERATIONS, false );
    assertEquals( "One cached session per worker should serve repeated operations", 16, result.created );
  }

  /**
   * Runs the cache-hit and cache-churn workloads as a comparative benchmark.
   * Use identical JVM flags and this identical source on both worktrees; output includes throughput, latency
   * percentiles, allocation, worker CPU, garbage-collection time, and session lifecycle counts.
   *
   * @param args optional operation count followed by measured-round count
   * @throws Exception if workload setup, execution, or validation fails
   */
  public static void main( String[] args ) throws Exception {
    int operations = args.length > 0 ? Integer.parseInt( args[ 0 ] ) : 200_000;
    int rounds = args.length > 1 ? Integer.parseInt( args[ 1 ] ) : 3;
    runWorkload( 16, 1, operations / 4, false, false );
    runWorkload( 32, 256, operations / 4, false, false );
    for ( int round = 0; round < rounds; round++ ) {
      runWorkload( 16, 1, operations, false, false );
      runWorkload( 32, 256, operations, false, false );
    }
  }

  /**
   * Allocates and initializes a lightweight session without invoking Jackrabbit's repository-opening constructor.
   *
   * @param metrics shared lifecycle counters updated by the session
   * @return a live session owned by the current thread
   */
  static LightweightSession newSession( Metrics metrics ) {
    // The fixture overrides every method used here. Bypass Jackrabbit's storage-opening constructor, just as Mockito
    // does, but without per-invocation recording or hundreds of thousands of heavyweight mock objects.
    LightweightSession session = ALLOCATOR.newInstance( LightweightSession.class );
    session.live = true;
    session.ownerThread = Thread.currentThread().getId();
    session.metrics = metrics;
    session.usageCount = new AtomicInteger();
    metrics.created.incrementAndGet();
    return session;
  }

  /** Runs a validated workload and returns its session-creation count. */
  private static Result runWorkload( int workers, int usersPerWorker, int operations, boolean evictActive )
      throws Exception {
    return runWorkload( workers, usersPerWorker, operations, evictActive, true );
  }

  /**
   * Executes one measured workload, optionally checking lifecycle invariants after final cache invalidation.
   * The workload owns its executor and always shuts it down in {@code finally}.
   *
   * @param workers number of bounded worker threads
   * @param usersPerWorker distinct credential identities selected by each worker
   * @param operations total callback executions to measure
   * @param evictActive whether callbacks trigger cache invalidation during the run
   * @param validate whether assertion failures abort the run and final lifecycle counts are checked
   * @return lifecycle result for the run
   * @throws Exception if setup, workers, validation, or teardown fail
   */
  private static Result runWorkload( int workers, int usersPerWorker, int operations, boolean evictActive,
                                    boolean validate ) throws Exception {
    Workload workload = new Workload( workers, usersPerWorker, operations, evictActive, validate );
    try {
      workload.submitWorkers();
      long elapsed = workload.awaitWorkers();
      workload.cacheFactory.invalidateCachedSessions();
      workload.validateSessions();
      Arrays.sort( workload.durations );
      Result result = new Result( workload.metrics.created.get() );
      workload.printResult( elapsed );
      return result;
    } finally {
      workload.cleanup();
    }
  }

  /**
   * Owns one benchmark run's repository stand-in, cache, template, worker coordination, and measurements.
   * Worker initialization is performed before the timed operation loop; cleanup invalidates the cache and stops
   * the bounded executor even when a worker or assertion fails.
   */
  private static final class Workload {
    private final int workers;
    private final int usersPerWorker;
    private final int operations;
    private final boolean evictActive;
    private final boolean validate;
    private final Metrics metrics = new Metrics();
    private final AtomicInteger failures = new AtomicInteger();
    private final GuavaCachePoolPentahoJcrSessionFactory cacheFactory;
    private final ThreadLocal<SimpleCredentials> credentials;
    private final PentahoJcrTemplate template;
    private final ExecutorService executor;
    private final List<Future<?>> futures;
    private final CountDownLatch ready;
    private final CountDownLatch start;
    private final long[] durations;
    private final AtomicLong allocated;
    private final AtomicLong cpuNanos;
    private final ThreadMXBean threadMetrics;
    private final long gcBefore;

    /** Constructs an isolated run context and initializes its bounded worker pool and measurement state. */
    private Workload( int workers, int usersPerWorker, int operations, boolean evictActive, boolean validate )
        throws Exception {
      this.workers = workers;
      this.usersPerWorker = usersPerWorker;
      this.operations = operations;
      this.evictActive = evictActive;
      this.validate = validate;
      Repository repository = newRepository( metrics );
      cacheFactory = new GuavaCachePoolPentahoJcrSessionFactory( repository, "workspace" );
      credentials = new ThreadLocal<>();
      CredentialsStrategySessionFactory factory =
        new CredentialsStrategySessionFactory( repository, credentials::get );
      factory.setSessionFactory( cacheFactory );
      factory.afterPropertiesSet();
      template = new PentahoJcrTemplate();
      template.setSessionFactory( factory );
      template.setAllowCreate( true );
      executor = Executors.newFixedThreadPool( workers );
      futures = new ArrayList<>();
      ready = new CountDownLatch( workers );
      start = new CountDownLatch( 1 );
      durations = new long[ operations ];
      allocated = new AtomicLong();
      cpuNanos = new AtomicLong();
      threadMetrics = ManagementFactory.getThreadMXBean();
      gcBefore = gcMillis();
    }

    /** Submits one worker for each disjoint range of measured operation indexes. */
    private void submitWorkers() {
      for ( int worker = 0; worker < workers; worker++ ) {
        int from = operations * worker / workers;
        int to = operations * ( worker + 1 ) / workers;
        futures.add( executor.submit( () -> runWorker( from, to ) ) );
      }
    }

    /**
     * Creates this worker's credential set and callback, then waits for the coordinated workload start.
     *
     * @param from inclusive first operation index assigned to this worker
     * @param to exclusive end operation index assigned to this worker
     */
    private void runWorker( int from, int to ) {
      SimpleCredentials[] users = new SimpleCredentials[ usersPerWorker ];
      for ( int user = 0; user < users.length; user++ ) {
        users[ user ] = new SimpleCredentials( "user-" + user, new char[ 0 ] );
      }
      int[] callbacks = { 0 };
      JcrCallback action = session -> {
        assertTrue( "Eviction must not close an active callback", session.isLive() );
        if ( evictActive && ++callbacks[ 0 ] % 256 == 0 ) {
          cacheFactory.invalidateCachedSessions();
        }
        assertTrue( "The callback must remain live after concurrent cache churn", session.isLive() );
        return null;
      };
      ready.countDown();
      try {
        if ( !start.await( 30, TimeUnit.SECONDS ) ) {
          throw new AssertionError( "Timed out waiting for stress workers" );
        }
        runOperations( users, action, from, to );
      } catch ( InterruptedException e ) {
        Thread.currentThread().interrupt();
        throw new AssertionError( "Stress worker interrupted", e );
      } finally {
        credentials.remove();
      }
    }

    /** Measures per-worker allocation and CPU while selecting credentials and executing the assigned operations. */
    private void runOperations( SimpleCredentials[] users, JcrCallback action, int from, int to )
        throws InterruptedException {
      long allocatedBefore = allocatedBytes( threadMetrics );
      long cpuBefore = threadMetrics.isCurrentThreadCpuTimeSupported() ? threadMetrics.getCurrentThreadCpuTime() : 0;
      for ( int operation = from; operation < to; operation++ ) {
        if ( Thread.currentThread().isInterrupted() ) {
          throw new InterruptedException( "Stress worker cancelled" );
        }
        credentials.set( users[ ( operation - from ) % users.length ] );
        executeOperation( action, operation );
      }
      allocated.addAndGet( allocatedBytes( threadMetrics ) - allocatedBefore );
      if ( threadMetrics.isCurrentThreadCpuTimeSupported() ) {
        cpuNanos.addAndGet( threadMetrics.getCurrentThreadCpuTime() - cpuBefore );
      }
    }

    /** Times one template callback and records benchmark-mode failures without hiding their count. */
    private void executeOperation( JcrCallback action, int operation ) {
      // Time execution and failure accounting, not credential selection; no per-operation result allocation.
      long before = System.nanoTime();
      try {
        template.execute( action, true );
      } catch ( RuntimeException | AssertionError e ) {
        if ( validate ) {
          throw e;
        }
        // The comparative benchmark reports baseline correctness failures, never silently drops them.
        failures.incrementAndGet();
      }
      durations[ operation ] = System.nanoTime() - before;
    }

    /** Waits for all workers, releases the start gate, and returns elapsed wall-clock time. */
    private long awaitWorkers() throws Exception {
      assertTrue( "All stress workers must be ready", ready.await( 30, TimeUnit.SECONDS ) );
      long startNanos = System.nanoTime();
      start.countDown();
      long deadline = startNanos + TimeUnit.SECONDS.toNanos( 60 );
      for ( Future<?> future : futures ) {
        future.get( Math.max( 1, deadline - System.nanoTime() ), TimeUnit.NANOSECONDS );
      }
      return System.nanoTime() - startNanos;
    }

    /**
     * Checks session creation, closure, and duplicate-logout totals when correctness validation is enabled. Eviction
     * cleanup is asynchronous, so closures are awaited for a bounded time before the totals are compared.
     */
    private void validateSessions() {
      if ( validate ) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos( 30 );
        while ( metrics.closed.get() < metrics.created.get() && System.nanoTime() < deadline ) {
          LockSupport.parkNanos( TimeUnit.MILLISECONDS.toNanos( 10 ) );
        }
        assertEquals( "All sessions must be disposed after callbacks and final cache invalidation",
          metrics.created.get(), metrics.closed.get() );
        assertEquals( "Session logout must happen at most once", 0, metrics.duplicateLogouts.get() );
      }
    }

    /**
     * Writes one locale-stable benchmark row, including latency, resource, and lifecycle measurements.
     * This is an intentional CLI benchmark protocol: metrics must reach stdout even without an SLF4J provider.
     */
    @SuppressWarnings( "java:S106" )
    private void printResult( long elapsed ) {
      System.out.printf( Locale.ROOT,
        "JCR-STRESS ops=%d workers=%d usersPerWorker=%d activeEviction=%s elapsedMs=%.2f opsPerSec=%.0f "
          + "p50us=%.2f p95us=%.2f p99us=%.2f allocatedMiB=%.2f workerCpuMs=%.2f gcMs=%d created=%d closed=%d errors=%d%n",
        operations, workers, usersPerWorker, evictActive, elapsed / 1_000_000.0,
        operations * 1_000_000_000.0 / elapsed, durations[ operations / 2 ] / 1000.0,
        durations[ operations * 95 / 100 ] / 1000.0, durations[ operations * 99 / 100 ] / 1000.0,
        allocated.get() / 1048576.0, cpuNanos.get() / 1_000_000.0, gcMillis() - gcBefore,
        metrics.created.get(), metrics.closed.get(), failures.get() );
    }

    /** Cancels remaining workers, awaits executor termination, and invalidates cached sessions. */
    private void cleanup() throws InterruptedException {
      start.countDown();
      futures.forEach( future -> future.cancel( true ) );
      executor.shutdownNow();
      assertTrue( "The bounded worker pool must terminate", executor.awaitTermination( 30, TimeUnit.SECONDS ) );
      cacheFactory.invalidateCachedSessions();
    }

    /**
     * Creates a minimal dynamic repository proxy that returns lightweight sessions for login calls.
     *
     * @param metrics shared counters passed to sessions created by this repository
     * @return repository stand-in that implements only the methods exercised by this workload
     */
    private static Repository newRepository( Metrics metrics ) {
      return (Repository) Proxy.newProxyInstance( Repository.class.getClassLoader(),
        new Class<?>[] { Repository.class }, ( proxy, method, args ) -> {
          if ( "login".equals( method.getName() ) ) {
            return newSession( metrics );
          }
          if ( "hashCode".equals( method.getName() ) ) {
            return System.identityHashCode( proxy );
          }
          if ( "equals".equals( method.getName() ) ) {
            return proxy == args[ 0 ];
          }
          if ( "getDescriptor".equals( method.getName() ) || "toString".equals( method.getName() ) ) {
            return "test-owned lightweight repository";
          }
          throw new UnsupportedOperationException( method.getName() );
        } );
    }

    /** Returns current-thread allocated bytes when the running JVM exposes the extended management API. */
    private static long allocatedBytes( ThreadMXBean metrics ) {
      if ( metrics instanceof com.sun.management.ThreadMXBean ) {
        com.sun.management.ThreadMXBean extended = (com.sun.management.ThreadMXBean) metrics;
        if ( extended.isThreadAllocatedMemorySupported() && extended.isThreadAllocatedMemoryEnabled() ) {
          return extended.getThreadAllocatedBytes( Thread.currentThread().getId() );
        }
      }
      return 0;
    }

    /** Returns the cumulative collection time reported by the JVM's garbage collectors. */
    private static long gcMillis() {
      return ManagementFactory.getGarbageCollectorMXBeans().stream()
        .mapToLong( collector -> Math.max( 0, collector.getCollectionTime() ) ).sum();
    }
  }

  /** Thread-safe counters shared by the repository and its lightweight sessions. */
  static final class Metrics {
    /** Number of lightweight sessions created by repository login calls. */
    final AtomicInteger created = new AtomicInteger();
    /** Number of sessions whose first logout transitioned them to closed. */
    final AtomicInteger closed = new AtomicInteger();
    /** Number of redundant logout attempts observed after a session was already closed. */
    final AtomicInteger duplicateLogouts = new AtomicInteger();
  }

  /** Immutable summary returned by one workload, currently containing the number of created sessions. */
  private static final class Result {
    /** Total sessions created during the workload. */
    final int created;

    /** Creates a result containing the number of sessions created during the workload. */
    private Result( int created ) {
      this.created = created;
    }
  }

  /**
   * Minimal observable Jackrabbit session stand-in for cache and template lifecycle tests.
   * The allocator bypasses the superclass constructor; overrides implement only the session operations exercised by
   * the workload and enforce thread ownership on refresh.
   */
  static class LightweightSession extends SessionImpl {
    /** Volatile liveness flag observed by concurrent workers and eviction cleanup. */
    volatile boolean live;
    /** Number of logout calls, used to detect duplicate cleanup attempts. */
    int logoutCalls;
    /** Thread that owns this session; refresh verifies that it is not shared across workers. */
    long ownerThread;
    /** Shared counters updated when this session is created or closed. */
    Metrics metrics;
    /** Lifecycle-holder value exposed through the Jackrabbit attribute API. */
    Object lifecycleAttribute;
    /** Diagnostic factory/template usage counter exposed as a session attribute. */
    AtomicInteger usageCount;

    /** Must not be called; instances are allocated with Objenesis to avoid opening Jackrabbit storage. */
    private LightweightSession() throws RepositoryException {
      super( null, (Subject) null, null );
      throw new UnsupportedOperationException( "Instantiate only with the test allocator" );
    }

    /** Returns the fixture's lifecycle or usage-count attribute. */
    @Override
    public Object getAttribute( String name ) {
      if ( "usage_count".equals( name ) ) {
        return usageCount;
      }
      return "cached_jcr_session".equals( name ) ? lifecycleAttribute : null;
    }

    /** Stores the fixture's lifecycle or usage-count attribute. */
    @Override
    public void setAttribute( String name, Object value ) {
      if ( "usage_count".equals( name ) ) {
        usageCount = (AtomicInteger) value;
      } else if ( "cached_jcr_session".equals( name ) ) {
        lifecycleAttribute = value;
      } else {
        throw new UnsupportedOperationException( name );
      }
    }

    /** Rejects refresh after logout and detects accidental cross-thread session reuse. */
    @Override
    public void refresh( boolean keepChanges ) throws RepositoryException {
      if ( !live ) {
        throw new RepositoryException( "Session was closed before refresh" );
      }
      assertEquals( "Cache sessions must not be shared between worker threads", ownerThread,
        Thread.currentThread().getId() );
    }

    /** Reports the fixture's volatile live state. */
    @Override
    public boolean isLive() {
      return live;
    }

    /** Records each logout attempt and updates shared closed/duplicate counters. */
    @Override
    public synchronized void logout() {
      logoutCalls++;
      if ( live ) {
        live = false;
        metrics.closed.incrementAndGet();
      } else {
        metrics.duplicateLogouts.incrementAndGet();
      }
    }

    /** Returns a stable diagnostic label without relying on Jackrabbit initialization. */
    @Override
    public String toString() {
      return "LightweightSession-" + System.identityHashCode( this );
    }
  }
}
