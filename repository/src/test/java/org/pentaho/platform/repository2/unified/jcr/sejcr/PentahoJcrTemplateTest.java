/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.repository2.unified.jcr.sejcr;

import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.GuavaCachePoolPentahoJcrSessionFactory.USAGE_COUNT;

/**
 * Unit tests for {@link PentahoJcrTemplate}'s session usage accounting, callback exception translation, and cleanup.
 * Mock sessions isolate template lifecycle behavior from repository I/O; real transaction/repository interactions
 * are exercised by the integration tests.
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoJcrTemplateTest {

  private PentahoJcrTemplate jcrTemplate = new PentahoJcrTemplate();

  private AtomicInteger usedUsageCount = new AtomicInteger( 0 );
  private AtomicInteger releasedUsageCount = new AtomicInteger( 0 );
  private AtomicInteger factoryProtectionUsageCount = new AtomicInteger( 0 );

  @Mock private SessionFactory sessionFactory;
  @Mock private Session session;
  @Mock private JcrCallback action;
  @Mock private JcrCallback nestedAction;

  /** Configures the mock factory/session and distinct counters for each template accounting phase. */
  @Before public void before() throws RepositoryException {
    when( sessionFactory.getSession() ).thenReturn( session );
    // Now with factory protection decrement, getAttribute is called 3 times per execute():
    // 1. useSession() - increments
    // 2. releaseSession() - decrements  
    // 3. decrementFactoryProtection() - decrements (factory protection release)
    when( session.getAttribute( USAGE_COUNT ) )
      .thenReturn( usedUsageCount, releasedUsageCount, factoryProtectionUsageCount );
    jcrTemplate.setSessionFactory( sessionFactory );
    jcrTemplate.setAllowCreate( true );
  }

  /** Verifies one callback starts usage and releases both callback and factory-protection counts. */
  @Test
  public void executeUpdatesUsageCount() throws IOException, RepositoryException {
    jcrTemplate.setSessionFactory( sessionFactory );

    jcrTemplate.execute( action, true );

    verify( action ).doInJcr( session );
    // Now with factory protection: useSession (1 call) + releaseSession (1 call) + decrementFactoryProtection (1 call) = 3 total
    verify( session, times( 3 ) ).getAttribute( USAGE_COUNT );

    assertThat( "first usage increments", usedUsageCount.get(), equalTo( 1 ) );
    assertThat( "second usage decrements", releasedUsageCount.get(), equalTo( -1 ) );
    assertThat( "third factory protection decrements", factoryProtectionUsageCount.get(), equalTo( -1 ) );
  }

  /** Verifies nested callbacks balance their independent usage and factory-protection accounting in order. */
  @Test
  public void executeUpdatesUsageCountNestedExec() throws IOException, RepositoryException {
    jcrTemplate.setSessionFactory( sessionFactory );

    // With factory protection: each execute() calls getAttribute 3 times.
    // Actual call order for a nested execute (release precedes factory-protection release in each finally):
    //   1. outer useSession()                -> increment
    //   2. inner useSession()                -> increment
    //   3. inner releaseSession()            -> decrement
    //   4. inner decrementFactoryProtection()-> decrement
    //   5. outer releaseSession()            -> decrement
    //   6. outer decrementFactoryProtection()-> decrement
    // The stub below maps each getAttribute call to the counter for that exact step, so an
    // ordering regression (e.g. factory-release running before release) would change the results.
    when( session.getAttribute( USAGE_COUNT ) )
      .thenReturn( usedUsageCount, usedUsageCount, releasedUsageCount,
                   factoryProtectionUsageCount, releasedUsageCount, factoryProtectionUsageCount );

    when( action.doInJcr( session ) )
      .thenAnswer( s -> {
        jcrTemplate.execute( nestedAction, true );
        return null;
      } );

    jcrTemplate.execute( action, true );

    verify( action ).doInJcr( session );
    verify( nestedAction ).doInJcr( session );
    // 6 calls: outer execute (3) + inner execute (3)
    verify( session, times( 6 ) ).getAttribute( USAGE_COUNT );
    assertThat( "first usage increments twice (nested)", usedUsageCount.get(), equalTo( 2 ) );
    assertThat( "second usage decrements twice (nested)", releasedUsageCount.get(), equalTo( -2 ) );
    assertThat( "third factory protection decrements twice (nested)", factoryProtectionUsageCount.get(), equalTo( -2 ) );
  }

  /** Verifies callback security failures are translated to Spring's access-denied exception with their cause. */
  @Test
  public void securityExceptionIsConvertedToAccessDeniedException() throws IOException, RepositoryException {
    when( action.doInJcr( session ) ).thenThrow( new SecurityException( "access denied" ) );

    try {
      jcrTemplate.execute( action, true );
      fail( "A security exception should be translated to an access-denied exception" );
    } catch ( org.springframework.security.access.AccessDeniedException expected ) {
      assertThat( expected.getCause(), instanceOf( SecurityException.class ) );
    }
  }

  /** Verifies diagnostic-attribute failures during cleanup neither mask the callback failure nor block logout. */
  @Test
  public void unavailableDiagnosticsDoNotMaskCallbackFailureOrPreventRetiredSessionLogout()
      throws IOException, RepositoryException {
    CachedJcrSession cachedSession = new CachedJcrSession( session );
    assertTrue( cachedSession.acquire() );
    RuntimeException diagnosticFailure = new IllegalStateException( "Session attributes became unavailable" );
    RuntimeException callbackFailure = new IllegalStateException( "Callback failed" );
    when( session.isLive() ).thenReturn( true );
    when( session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) )
      .thenReturn( cachedSession ).thenThrow( diagnosticFailure );
    when( session.getAttribute( USAGE_COUNT ) ).thenReturn( usedUsageCount ).thenThrow( diagnosticFailure );
    when( action.doInJcr( session ) ).thenAnswer( invocation -> {
      cachedSession.retire();
      throw callbackFailure;
    } );

    RuntimeException actual = assertThrows( RuntimeException.class, () -> jcrTemplate.execute( action, true ) );

    assertSame( "Cleanup must preserve the application's original failure", callbackFailure, actual );
    verify( session, times( 1 ) ).getAttribute( CachedJcrSession.SESSION_ATTRIBUTE );
    verify( session, times( 1 ) ).logout();
  }

  /** Verifies a failed usage start does not decrement an unstarted use but still releases factory protection. */
  @Test
  public void failedUsageStartReleasesFactoryProtectionWithoutDecrementingAnUnstartedUse()
      throws IOException, RepositoryException {
    CachedJcrSession cachedSession = new CachedJcrSession( session );
    assertTrue( cachedSession.acquire() );
    cachedSession.retire();
    AtomicInteger factoryCount = new AtomicInteger( 1 );
    RuntimeException usageFailure = new IllegalStateException( "Cannot start diagnostic usage" );
    when( session.isLive() ).thenReturn( true );
    when( session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) ).thenReturn( cachedSession );
    when( session.getAttribute( USAGE_COUNT ) ).thenThrow( usageFailure ).thenReturn( factoryCount );

    RuntimeException actual = assertThrows( RuntimeException.class, () -> jcrTemplate.execute( action, true ) );

    assertSame( usageFailure, actual );
    assertEquals( "Only the factory increment exists and must be released", 0, factoryCount.get() );
    verify( action, never() ).doInJcr( session );
    verify( session, times( 2 ) ).getAttribute( USAGE_COUNT );
    verify( session, times( 1 ) ).logout();
  }

  /** Verifies factory acquisition failure propagates unchanged without touching a session or invoking the callback. */
  @Test
  public void failedFactoryAcquisitionDoesNotMaskOriginalExceptionOrTouchASession() throws RepositoryException {
    RuntimeException acquisitionFailure = new IllegalStateException( "Factory unavailable" );
    when( sessionFactory.getSession() ).thenThrow( acquisitionFailure );

    RuntimeException actual = assertThrows( RuntimeException.class, () -> jcrTemplate.execute( action, true ) );

    assertSame( acquisitionFailure, actual );
    verifyNoInteractions( session, action );
  }

  /**
   * Verifies a Spring-bound session survives eviction between callbacks: it is closed only when the bound
   * synchronization completes, and only once, even though the callbacks released their own leases.
   */
  @Test
  public void boundSessionStaysOpenUntilTransactionCompletionEvenAfterRetirement() throws Exception {
    CachedJcrSession cachedSession = new CachedJcrSession( session );
    assertTrue( cachedSession.acquire() );
    when( session.isLive() ).thenReturn( true );
    when( session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) ).thenReturn( cachedSession );
    when( action.doInJcr( session ) ).thenAnswer( invocation -> {
      cachedSession.retire();
      return null;
    } );

    bindSessionToTransaction();
    try {
      jcrTemplate.execute( action, true );

      verify( session, never() ).logout();
      completeSynchronizations();
      verify( session, times( 1 ) ).logout();
    } finally {
      unbindSessionFromTransaction();
    }
  }

  /**
   * Verifies sequential callbacks in one transaction share a single synchronization, reuse the bound session without
   * a new factory handoff, and leave the retired session open until the transaction completes.
   */
  @Test
  public void callbacksInOneTransactionShareOneSynchronizationAndReuseTheBoundSession() throws Exception {
    CachedJcrSession cachedSession = new CachedJcrSession( session );
    assertTrue( cachedSession.acquire() );
    when( session.isLive() ).thenReturn( true );
    when( session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) ).thenReturn( cachedSession );

    bindSessionToTransaction();
    try {
      jcrTemplate.execute( action, true );
      jcrTemplate.execute( nestedAction, true );

      verify( action ).doInJcr( session );
      verify( nestedAction ).doInJcr( session );
      assertEquals( "Both callbacks must be retained by one synchronization", 1,
        TransactionSynchronizationManager.getSynchronizations().size() );

      cachedSession.retire();
      verify( session, never() ).logout();
      completeSynchronizations();
      verify( session, times( 1 ) ).logout();
    } finally {
      unbindSessionFromTransaction();
    }
  }

  /** Verifies a session whose final lease was already released is refused instead of being revived. */
  @Test
  public void sessionWhoseFinalLeaseWasReleasedCannotBeUsedAgain() throws Exception {
    CachedJcrSession cachedSession = new CachedJcrSession( session );
    when( session.isLive() ).thenReturn( true );
    cachedSession.retire();
    when( session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) ).thenReturn( cachedSession );

    IllegalStateException actual =
      assertThrows( IllegalStateException.class, () -> jcrTemplate.execute( action, true ) );

    assertTrue( actual.getMessage().contains( "final lease was released" ) );
    verify( action, never() ).doInJcr( any() );
    verify( session, times( 1 ) ).logout();
  }

  /** Verifies a bound session that cannot be retained for its transaction fails instead of being used unprotected. */
  @Test
  public void boundSessionThatCannotBeRetainedForItsTransactionFails() throws Exception {
    CachedJcrSession cachedSession = spy( new CachedJcrSession( session ) );
    doReturn( true ).when( cachedSession ).consumeFactoryLease();
    doReturn( false ).when( cachedSession ).acquireForBoundUse();
    when( session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) ).thenReturn( cachedSession );

    bindSessionToTransaction();
    try {
      IllegalStateException actual =
        assertThrows( IllegalStateException.class, () -> jcrTemplate.execute( action, true ) );

      assertTrue( actual.getMessage().contains( "Spring-bound" ) );
      verify( action, never() ).doInJcr( any() );
    } finally {
      unbindSessionFromTransaction();
    }
  }

  /** Verifies a failure while closing an uncached session is contained and does not change the callback result. */
  @Test
  public void failureClosingAnUncachedSessionDoesNotAffectTheCallbackResult() throws Exception {
    when( action.doInJcr( session ) ).thenReturn( "result" );
    doThrow( new IllegalStateException( "logout failed" ) ).when( session ).logout();

    assertEquals( "result", jcrTemplate.execute( action, true ) );

    verify( session, times( 1 ) ).logout();
  }

  /** Verifies a session without a usage-count attribute is still used and released safely. */
  @Test
  public void sessionWithoutUsageCountAttributeIsStillExecuted() throws Exception {
    when( session.getAttribute( USAGE_COUNT ) ).thenReturn( null );
    when( action.doInJcr( session ) ).thenReturn( "result" );

    assertEquals( "result", jcrTemplate.execute( action, true ) );

    verify( action ).doInJcr( session );
  }

  /** Binds the mock session to the template's session factory inside an active synchronization scope. */
  private void bindSessionToTransaction() {
    TransactionSynchronizationManager.initSynchronization();
    SessionHolder holder = new SessionHolder( session );
    // Prevents SessionFactoryUtils from registering its own synchronization for this holder.
    holder.setSynchronizedWithTransaction( true );
    TransactionSynchronizationManager.bindResource( sessionFactory, holder );
  }

  /** Removes the binding and synchronization scope created by {@link #bindSessionToTransaction()}. */
  private void unbindSessionFromTransaction() {
    TransactionSynchronizationManager.unbindResource( sessionFactory );
    TransactionSynchronizationManager.clearSynchronization();
  }

  /** Completes every synchronization registered in the current scope as if the transaction had committed. */
  private void completeSynchronizations() {
    List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
    for ( TransactionSynchronization synchronization : synchronizations ) {
      synchronization.afterCompletion( TransactionSynchronization.STATUS_COMMITTED );
    }
  }

}
