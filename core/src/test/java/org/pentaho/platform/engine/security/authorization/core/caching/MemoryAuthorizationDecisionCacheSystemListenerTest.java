/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.engine.security.authorization.core.caching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.engine.IPentahoSession;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/**
 * Tests for {@link MemoryAuthorizationDecisionCacheSystemListener}.
 */
public class MemoryAuthorizationDecisionCacheSystemListenerTest {

  private MemoryAuthorizationDecisionCache cache;
  private MemoryAuthorizationDecisionCacheSystemListener listener;
  private IPentahoSession session;

  private static MockedStatic<LogFactory> logFactoryMockedStatic;
  private static Log logger;

  @BeforeClass
  public static void initLogging() {
    logger = mock( Log.class );
    when( logger.isTraceEnabled() ).thenReturn( true );

    logFactoryMockedStatic = mockStatic( LogFactory.class );
    logFactoryMockedStatic
      .when( () -> LogFactory.getLog( MemoryAuthorizationDecisionCacheSystemListener.class ) )
      .thenReturn( logger );
  }

  @AfterClass
  public static void tearDownLogging() {
    logFactoryMockedStatic.close();
  }

  @Before
  public void setUp() {
    // Reset the logger mock to prevent cumulative call counts across tests
    reset( logger );
    when( logger.isTraceEnabled() ).thenReturn( true );

    cache = mock( MemoryAuthorizationDecisionCache.class );
    session = mock( IPentahoSession.class );
    listener = new MemoryAuthorizationDecisionCacheSystemListener( cache );
  }

  // region Constructor Tests

  @Test
  public void testConstructor_CreatesListener() {
    assertNotNull( listener );
  }

  @Test( expected = NullPointerException.class )
  @SuppressWarnings( "DataFlowIssue" )
  public void testConstructor_NullCache_ThrowsException() {
    new MemoryAuthorizationDecisionCacheSystemListener( null );
  }

  // endregion

  // region Startup Tests

  @Test
  public void testStartup_ReturnsTrue() {
    boolean result = listener.startup( session );

    assertTrue( result );
  }

  @Test
  public void testStartup_LogsAtTraceLevel() {
    listener.startup( session );

    verify( logger, times( 1 ) ).isTraceEnabled();
    verify( logger, times( 1 ) ).trace( "Started authorization decision cache" );
  }

  // endregion

  // region Shutdown Tests

  @Test
  public void testShutdown_ClosesCache() throws Exception {
    listener.shutdown();

    verify( cache, times( 1 ) ).close();
  }

  @Test
  public void testShutdown_LogsTraceMessages() {
    listener.shutdown();

    verify( logger, times( 2 ) ).isTraceEnabled();
    verify( logger, times( 1 ) ).trace( "Shutting down authorization decision cache..." );
    verify( logger, times( 1 ) ).trace( "Shut down authorization decision cache successfully" );
  }

  @Test
  public void testShutdown_WhenCacheCloseThrowsException_LogsError() throws Exception {
    Exception testException = new Exception( "Test exception" );
    doThrow( testException ).when( cache ).close();

    listener.shutdown();

    verify( cache, times( 1 ) ).close();
    verify( logger, times( 1 ) ).error( "Error closing authorization decision cache", testException );
  }

  @Test
  public void testShutdown_WhenCacheCloseThrowsException_CompletesSuccessfully() throws Exception {
    doThrow( new RuntimeException( "Test exception" ) ).when( cache ).close();

    // Should not throw - should catch and log
    listener.shutdown();

    // Verify error was logged
    verify( logger, times( 1 ) ).error(
      eq( "Error closing authorization decision cache" ),
      any( RuntimeException.class ) );
  }

  @Test
  public void testShutdown_WhenCacheCloseThrowsException_StillLogsSuccessMessage() throws Exception {
    doThrow( new Exception( "Test exception" ) ).when( cache ).close();

    listener.shutdown();

    // Even after exception, should still log the "successfully" message
    verify( logger, times( 1 ) ).trace( "Shut down authorization decision cache successfully" );
  }

  // endregion

  // region Integration Tests

  @Test
  public void testStartupFollowedByShutdown() throws Exception {
    boolean startupResult = listener.startup( session );
    assertTrue( startupResult );

    listener.shutdown();

    verify( cache, times( 1 ) ).close();
  }

  // endregion
}
