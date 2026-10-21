/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.repository2.unified.jcr.sejcr;

import org.apache.jackrabbit.api.XASession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by nbaker on 7/21/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class CredentialsStrategySessionFactoryTest {

  private Session sessionProxy;

  @Mock
  private Session underlyingSession;

  @Test
  public void createSessionProxy() throws Exception {
    assertThat( sessionProxy, allOf( instanceOf( XASession.class ), instanceOf( Session.class ) ) );
  }

  @Before
  public void setup() {
    CredentialsStrategySessionFactory factory =
        new CredentialsStrategySessionFactory( mock( Repository.class ), mock( CredentialsStrategy.class ) );
    sessionProxy = factory.createSessionProxy( underlyingSession );
  }

  @Test
  public void testSessionProxy() throws Exception {
    CredentialsStrategySessionFactory.LogoutSuppressingInvocationHandler concreteProxy =
        (CredentialsStrategySessionFactory.LogoutSuppressingInvocationHandler)
            Proxy.getInvocationHandler( sessionProxy );
    TestLogoutHandler logoutHandler = new TestLogoutHandler();
    logoutHandler.setLogout( false );
    concreteProxy.setLogoutDelegate( logoutHandler );

    // This should be swallowed
    sessionProxy.logout();
    verify( underlyingSession, times( 0 ) ).logout();

    logoutHandler.setLogout( true );
    when( underlyingSession.isLive() ).thenReturn( true );
    // This should hit the real session
    sessionProxy.logout();
    verify( underlyingSession, times( 1 ) ).logout();

  }

  /**
   * Verifies a second logout through the proxy is not forwarded once the session is closed, as happens when the
   * transaction manager closes its bound proxy after the cached-session lease already logged the session out.
   */
  @Test
  public void repeatedLogoutOfAnAlreadyClosedSessionIsNotForwarded() {
    when( underlyingSession.isLive() ).thenReturn( true, false );

    sessionProxy.logout();
    sessionProxy.logout();

    verify( underlyingSession, times( 1 ) ).logout();
  }

  /** Verifies the lazily created default delegate is cache-backed and wired to this factory and its transaction manager. */
  @Test
  public void defaultCacheDelegateIsCreatedOnceAndReceivesTheRegisteredTransactionManager() {
    CredentialsStrategySessionFactory factory = newFactory();
    PentahoTransactionManager manager = new PentahoTransactionManager();
    factory.setTransactionManager( manager );

    PentahoJcrSessionFactory delegate = factory.getSessionFactory();

    assertThat( delegate, instanceOf( GuavaCachePoolPentahoJcrSessionFactory.class ) );
    assertSame( manager, ReflectionTestUtils.getField( delegate, "transactionManager" ) );
    assertSame( factory, ReflectionTestUtils.getField( delegate, "credentialsStrategySessionFactory" ) );
    assertSame( "The default delegate must be created only once", delegate, factory.getSessionFactory() );
  }

  /** Verifies a transaction manager registered after the delegate exists is passed on to it. */
  @Test
  public void transactionManagerRegisteredAfterTheDelegateExistsIsPassedToIt() {
    CredentialsStrategySessionFactory factory = newFactory();
    PentahoJcrSessionFactory delegate = factory.getSessionFactory();
    assertNull( ReflectionTestUtils.getField( delegate, "transactionManager" ) );
    PentahoTransactionManager manager = new PentahoTransactionManager();

    factory.setTransactionManager( manager );

    assertSame( manager, ReflectionTestUtils.getField( delegate, "transactionManager" ) );
  }

  /** Verifies an injected cache delegate is wired to this factory and to an already registered transaction manager. */
  @Test
  public void injectedCacheDelegateReceivesTheRegisteredTransactionManager() {
    CredentialsStrategySessionFactory factory = newFactory();
    PentahoTransactionManager manager = new PentahoTransactionManager();
    factory.setTransactionManager( manager );
    GuavaCachePoolPentahoJcrSessionFactory injected =
      new GuavaCachePoolPentahoJcrSessionFactory( mock( Repository.class ), "workspace" );

    factory.setSessionFactory( injected );

    assertSame( manager, ReflectionTestUtils.getField( injected, "transactionManager" ) );
    assertSame( factory, ReflectionTestUtils.getField( injected, "credentialsStrategySessionFactory" ) );
  }

  /** Verifies a delegate that is not cache-backed is used as supplied and ignores transaction-manager registration. */
  @Test
  public void nonCacheDelegateIsUsedAsSuppliedWhenATransactionManagerIsRegistered() {
    CredentialsStrategySessionFactory factory = newFactory();
    PentahoJcrSessionFactory custom = credentials -> underlyingSession;

    factory.setSessionFactory( custom );
    factory.setTransactionManager( new PentahoTransactionManager() );

    assertSame( custom, factory.getSessionFactory() );
  }

  /** Verifies a direct session is logged out when the proxy/listener handoff fails, and the failure propagates. */
  @Test
  public void failedHandoffLogsOutADirectSession() {
    RepositoryException failure = new RepositoryException( "listener registration failed" );
    CredentialsStrategySessionFactory factory = failingHandoffFactory( credentials -> underlyingSession, failure );

    RepositoryException actual = assertThrows( RepositoryException.class, factory::getSession );

    assertSame( failure, actual );
    verify( underlyingSession, times( 1 ) ).logout();
  }

  /** Verifies a cached session's diagnostic count and lease are rolled back when the handoff fails. */
  @Test
  public void failedHandoffRollsBackACachedSessionsCountAndLease() {
    RepositoryException failure = new RepositoryException( "listener registration failed" );
    CachedJcrSession holder = new CachedJcrSession( underlyingSession );
    assertTrue( holder.acquire() );
    AtomicInteger usageCount = new AtomicInteger( 1 );
    when( underlyingSession.isLive() ).thenReturn( true );
    when( underlyingSession.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) ).thenReturn( holder );
    when( underlyingSession.getAttribute( GuavaCachePoolPentahoJcrSessionFactory.USAGE_COUNT ) )
      .thenReturn( usageCount );
    CredentialsStrategySessionFactory factory = failingHandoffFactory( credentials -> underlyingSession, failure );

    assertSame( failure, assertThrows( RepositoryException.class, factory::getSession ) );

    assertEquals( "The factory's diagnostic increment must be rolled back", 0, usageCount.get() );
    verify( underlyingSession, times( 1 ) ).logout();
  }

  /** Verifies a cached session without a usage counter is still retired and released when the handoff fails. */
  @Test
  public void failedHandoffReleasesACachedSessionThatHasNoUsageCounter() {
    RepositoryException failure = new RepositoryException( "listener registration failed" );
    CachedJcrSession holder = new CachedJcrSession( underlyingSession );
    assertTrue( holder.acquire() );
    when( underlyingSession.isLive() ).thenReturn( true );
    when( underlyingSession.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) ).thenReturn( holder );
    CredentialsStrategySessionFactory factory = failingHandoffFactory( credentials -> underlyingSession, failure );

    assertSame( failure, assertThrows( RepositoryException.class, factory::getSession ) );

    verify( underlyingSession, times( 1 ) ).logout();
  }

  /**
   * Verifies the lifecycle lease is still released, so the retired session is logged out, when the diagnostic usage
   * counter cannot be read during handoff cleanup, and that the original handoff failure still propagates.
   * Only the repository session is a mock, because a real Jackrabbit session cannot be made to fail on one attribute.
   */
  @Test
  public void failedHandoffReleasesTheLeaseWhenTheUsageCounterCannotBeRead() {
    RepositoryException failure = new RepositoryException( "listener registration failed" );
    CachedJcrSession holder = new CachedJcrSession( underlyingSession );
    assertTrue( holder.acquire() );
    when( underlyingSession.isLive() ).thenReturn( true );
    when( underlyingSession.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) ).thenReturn( holder );
    when( underlyingSession.getAttribute( GuavaCachePoolPentahoJcrSessionFactory.USAGE_COUNT ) )
      .thenThrow( new IllegalStateException( "usage counter is unavailable" ) );
    CredentialsStrategySessionFactory factory = failingHandoffFactory( credentials -> underlyingSession, failure );

    assertSame( failure, assertThrows( RepositoryException.class, factory::getSession ) );

    verify( underlyingSession, times( 1 ) ).logout();
    assertFalse( "The released holder must not be reusable", holder.acquire() );
  }

  /** Verifies a failure while discarding the session is contained and the original handoff failure still propagates. */
  @Test
  public void failureDiscardingASessionDoesNotMaskTheHandoffFailure() {
    RepositoryException failure = new RepositoryException( "listener registration failed" );
    when( underlyingSession.getAttribute( any( String.class ) ) )
      .thenThrow( new IllegalStateException( "session attributes are unavailable" ) );
    CredentialsStrategySessionFactory factory = failingHandoffFactory( credentials -> underlyingSession, failure );

    assertSame( failure, assertThrows( RepositoryException.class, factory::getSession ) );

    verify( underlyingSession, never() ).logout();
  }

  /** Creates a factory with no delegate configured. */
  private CredentialsStrategySessionFactory newFactory() {
    return new CredentialsStrategySessionFactory( mock( Repository.class ), mock( CredentialsStrategy.class ) );
  }

  /**
   * Creates a factory whose proxy/listener handoff always fails with the supplied exception.
   *
   * @param delegate session source used by the factory
   * @param failure exception thrown while registering listeners
   * @return the factory
   */
  private CredentialsStrategySessionFactory failingHandoffFactory( PentahoJcrSessionFactory delegate,
                                                                   RepositoryException failure ) {
    CredentialsStrategySessionFactory factory = new CredentialsStrategySessionFactory( mock( Repository.class ),
      new ConstantCredentialsStrategy( new SimpleCredentials( "user", new char[ 0 ] ) ) ) {
      @Override
      protected Session addListeners( Session session ) throws RepositoryException {
        throw failure;
      }
    };
    factory.setSessionFactory( delegate );
    return factory;
  }

  private class TestLogoutHandler
      implements CredentialsStrategySessionFactory.LogoutSuppressingInvocationHandler.LogoutDelegate {


    private boolean logout;

    @Override public boolean shouldLogout() {
      return logout;
    }

    public void setLogout( boolean logout ) {

      this.logout = logout;
    }
  }

}