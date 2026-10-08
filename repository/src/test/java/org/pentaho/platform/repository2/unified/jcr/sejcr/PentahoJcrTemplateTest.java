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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.GuavaCachePoolPentahoJcrSessionFactory.USAGE_COUNT;

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

  @Test
  public void failedFactoryAcquisitionDoesNotMaskOriginalExceptionOrTouchASession() throws RepositoryException {
    RuntimeException acquisitionFailure = new IllegalStateException( "Factory unavailable" );
    when( sessionFactory.getSession() ).thenThrow( acquisitionFailure );

    RuntimeException actual = assertThrows( RuntimeException.class, () -> jcrTemplate.execute( action, true ) );

    assertSame( acquisitionFailure, actual );
    verifyNoInteractions( session, action );
  }

}
