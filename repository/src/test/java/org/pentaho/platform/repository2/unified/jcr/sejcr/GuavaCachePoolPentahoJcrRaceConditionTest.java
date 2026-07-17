/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Pentaho, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.repository2.unified.jcr.sejcr;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.SessionFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.GuavaCachePoolPentahoJcrSessionFactory.USAGE_COUNT;

/**
 * Test suite for the JCR session race-condition fix (BISERVER-15667).
 *
 * <p>Unlike a pure arithmetic check on a local counter, these tests drive the real
 * {@link PentahoJcrTemplate#execute(JcrCallback, boolean)} code path so that the production
 * {@code useSession()}, {@code releaseSession()} and {@code decrementFactoryProtection()} logic
 * actually operates on a live {@link AtomicInteger} {@code usage_count} session attribute.</p>
 *
 * <p>The bug: {@link GuavaCachePoolPentahoJcrSessionFactory} did not increment {@code usage_count}
 * when handing out a cached session, but the template incremented/decremented it. The count therefore
 * returned to {@code 0} as soon as the template finished, letting the cache removal listener evict and
 * {@code logout()} a session that was still logically owned by the factory retrieval, producing
 * intermittent "session closed" errors.</p>
 *
 * <p>The fix: the factory increments {@code usage_count} on retrieval and the template releases that
 * "factory protection" in {@link PentahoJcrTemplate#execute(JcrCallback, boolean)}. The invariant these
 * tests protect is that after {@code execute()} completes the count is only back to {@code 0} (i.e. the
 * session becomes evictable) once BOTH the factory retrieval and the template are done with it.</p>
 *
 * <p>Note: the factory's actual cache retrieval requires a live Jackrabbit {@code Repository}, which is
 * out of scope for a unit test, so the factory-side {@code usage_count} increment is simulated by
 * incrementing the shared attribute immediately before each {@code execute()} (mirroring the contract in
 * {@link GuavaCachePoolPentahoJcrSessionFactory#getSession(javax.jcr.Credentials)}).</p>
 */
@RunWith( MockitoJUnitRunner.class )
public class GuavaCachePoolPentahoJcrRaceConditionTest {

  private PentahoJcrTemplate jcrTemplate;

  /** Live usage-count attribute shared by the mock session; the production code mutates this directly. */
  private final AtomicInteger usageCount = new AtomicInteger( 0 );

  @Mock private SessionFactory sessionFactory;
  @Mock private Session session;
  @Mock private JcrCallback action;
  @Mock private JcrCallback nestedAction;

  @Before
  public void before() throws RepositoryException {
    usageCount.set( 0 );
    when( sessionFactory.getSession() ).thenReturn( session );
    when( session.getAttribute( USAGE_COUNT ) ).thenReturn( usageCount );

    jcrTemplate = new PentahoJcrTemplate();
    jcrTemplate.setSessionFactory( sessionFactory );
    jcrTemplate.setAllowCreate( true );
  }

  /** Simulates the factory incrementing {@code usage_count} when it hands out a cached session. */
  private void simulateFactoryRetrieval() {
    usageCount.incrementAndGet();
  }

  /**
   * After a single template execution, the factory protection is released and the session returns to an
   * evictable state (count == 0) - but only after the whole {@code execute()} finished.
   */
  @Test
  public void singleOperationLeavesSessionEvictableOnlyAfterExecute() throws Exception {
    simulateFactoryRetrieval();

    jcrTemplate.execute( action, true );

    assertEquals( "Session should be safe for eviction after execute completes", 0, usageCount.get() );
  }

  /**
   * While the callback is running, the session must be protected (count &gt; 0) so the removal listener
   * would refuse to log it out. This asserts against the real count observed from inside {@code doInJcr}.
   */
  @Test
  public void sessionIsProtectedWhileOperationRuns() throws Exception {
    simulateFactoryRetrieval();

    AtomicInteger observedDuringOp = new AtomicInteger( -1 );
    when( action.doInJcr( session ) ).thenAnswer( inv -> {
      observedDuringOp.set( usageCount.get() );
      return null;
    } );

    jcrTemplate.execute( action, true );

    assertTrue( "Session must be protected (usage_count > 0) while the operation runs",
      observedDuringOp.get() > 0 );
    assertEquals( "Session becomes evictable only after execute", 0, usageCount.get() );
  }

  /**
   * Nested executes on the same session must remain balanced: after the inner execute completes the
   * session is still protected by the outer template, and the count only returns to 0 once the outer
   * execute finishes.
   */
  @Test
  public void nestedOperationsRemainBalanced() throws Exception {
    simulateFactoryRetrieval(); // outer factory retrieval

    when( action.doInJcr( session ) ).thenAnswer( inv -> {
      simulateFactoryRetrieval(); // inner factory retrieval (nested getSession)
      jcrTemplate.execute( nestedAction, true );
      assertTrue( "Session still protected by outer template after inner execute", usageCount.get() > 0 );
      return null;
    } );

    jcrTemplate.execute( action, true );

    assertEquals( "Balanced back to evictable after nested executes", 0, usageCount.get() );
  }

  /**
   * Regression guard for the original bug: WITHOUT the factory increment the count would already be 0
   * after the template released it, even though the factory retrieval still owned the session. Here we
   * drive the real template WITHOUT simulating the factory retrieval and confirm the count goes negative,
   * which the production code flags as an imbalance - demonstrating why the factory increment is required.
   */
  @Test
  public void withoutFactoryIncrementCountGoesNegative() throws Exception {
    // No simulateFactoryRetrieval(): mimics the buggy factory that did not increment.
    jcrTemplate.execute( action, true );

    assertTrue( "Missing factory increment leaves the count unbalanced (negative)", usageCount.get() < 0 );
  }

  /**
   * Stress test: many threads each perform (factory retrieval + real template execute). The shared
   * usage_count must return to exactly 0. Any assertion/exception raised on a worker thread is captured
   * and re-reported on the main thread, because an AssertionError thrown in a child thread does NOT fail
   * the JUnit test on its own.
   */
  @Test
  public void concurrentExecutesRemainBalanced() throws Exception {
    int numThreads = 10;
    int opsPerThread = 100;
    // Use a real no-op callback rather than a Mockito mock: mocks are not guaranteed thread-safe,
    // which would make this stress test flaky when invoked from multiple threads.
    JcrCallback noOpCallback = s -> null;

    AtomicReference<Throwable> workerFailure = new AtomicReference<>();
    Thread[] threads = new Thread[ numThreads ];
    for ( int t = 0; t < numThreads; t++ ) {
      threads[ t ] = new Thread( () -> {
        try {
          for ( int i = 0; i < opsPerThread; i++ ) {
            simulateFactoryRetrieval();
            jcrTemplate.execute( noOpCallback, true );
          }
        } catch ( Throwable th ) {
          workerFailure.compareAndSet( null, th );
        }
      } );
      threads[ t ].start();
    }

    for ( Thread t : threads ) {
      // Bound the wait so a stuck worker fails the test instead of hanging the build indefinitely.
      t.join( TimeUnit.SECONDS.toMillis( 30 ) );
      if ( t.isAlive() ) {
        fail( "Worker thread did not finish within timeout - possible deadlock or hang" );
      }
    }

    if ( workerFailure.get() != null ) {
      // Rethrow with the captured Throwable as the cause so the worker's stack trace is preserved.
      throw new AssertionError( "Worker thread failed", workerFailure.get() );
    }
    assertEquals( "All balanced factory + template operations return the count to 0", 0, usageCount.get() );
  }
}
