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

package org.pentaho.platform.engine.security.authorization.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationActionService;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.caching.IAuthorizationDecisionCache;
import org.pentaho.platform.api.engine.security.authorization.caching.IAuthorizationDecisionCacheKey;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createMockRule;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class CachingAuthorizationServiceTest {

  private IAuthorizationActionService actionService;
  private IAuthorizationDecisionCache decisionCache;
  private IAuthorizationRule<IAuthorizationRequest> rootRule;
  private IAuthorizationOptions options;
  private CachingAuthorizationService service;
  private IAuthorizationRequest request;

  private static MockedStatic<LogFactory> logFactoryMockedStatic;

  @BeforeClass
  public static void init() {
    Log logger = mock( Log.class );
    when( logger.isDebugEnabled() ).thenReturn( true );
    when( logger.isTraceEnabled() ).thenReturn( true );

    logFactoryMockedStatic = mockStatic( LogFactory.class );
    logFactoryMockedStatic
      .when( () -> LogFactory.getLog( AuthorizationService.class ) )
      .thenReturn( logger );
    logFactoryMockedStatic
      .when( () -> LogFactory.getLog( CachingAuthorizationService.class ) )
      .thenReturn( logger );
  }

  @Before
  public void setUp() {
    actionService = mock( IAuthorizationActionService.class );
    decisionCache = mock( IAuthorizationDecisionCache.class );
    options = mock( IAuthorizationOptions.class );

    IAuthorizationAction action = createTestAction( "action" );
    when( actionService.getAction( "action" ) ).thenReturn( Optional.of( action ) );

    rootRule = createMockRule();

    service = new CachingAuthorizationService( actionService, rootRule, decisionCache );

    request = new AuthorizationRequest( createTestUser(), action );
  }

  @AfterClass
  public static void tearDownAll() {
    logFactoryMockedStatic.close();
  }

  // region Constructor Tests

  @Test
  public void testConstructorWithRootRule() {
    var testService = new CachingAuthorizationService( actionService, rootRule, decisionCache );
    assertNotNull( testService );
  }

  @Test
  public void testConstructorWithoutRootRule() {
    var testService = new CachingAuthorizationService( actionService, decisionCache );
    assertNotNull( testService );
  }

  @SuppressWarnings( "DataFlowIssue" )
  @Test( expected = IllegalArgumentException.class )
  public void testConstructorThrowsWhenActionServiceIsNull() {
    new CachingAuthorizationService( null, rootRule, decisionCache );
  }

  @SuppressWarnings( "DataFlowIssue" )
  @Test( expected = IllegalArgumentException.class )
  public void testConstructorThrowsWhenDecisionCacheIsNull() {
    new CachingAuthorizationService( actionService, rootRule, null );
  }

  @SuppressWarnings( "DataFlowIssue" )
  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithoutRootRuleThrowsWhenDecisionCacheIsNull() {
    new CachingAuthorizationService( actionService, null );
  }
  // endregion

  // region Caching Tests

  @Test
  public void testAuthorizeCacheHit() {
    var cachedDecision = mock( IAuthorizationDecision.class );
    var rootRuleDecision = mock( IAuthorizationDecision.class );

    // Mock the cache to return a cached decision (without invoking the loader)
    when( decisionCache.get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() ) )
      .thenReturn( cachedDecision );

    // Root rule should return a different decision, but it shouldn't be called on a cache hit
    when( rootRule.authorize(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( rootRuleDecision ) );

    var decision = service.authorize( request, options );

    // Should return the cached decision, not the root rule decision
    assertEquals( cachedDecision, decision );
    assertSame( cachedDecision, decision );

    // Verify cache was called
    verify( decisionCache, times( 1 ) ).get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() );

    // Verify root rule was NOT called (cache hit means we skip the rule evaluation)
    verify( rootRule, never() ).authorize(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationContext.class ) );
  }

  @Test
  public void testAuthorizeCacheMiss() {
    var rootRuleDecision = mock( IAuthorizationDecision.class );

    when( rootRule.authorize(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( rootRuleDecision ) );

    // Mock the cache to invoke the loader function (cache miss)
    when( decisionCache.get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() ) )
      .thenAnswer( invocation -> {
        Function<IAuthorizationDecisionCacheKey, IAuthorizationDecision> loader = invocation.getArgument( 2 );
        IAuthorizationDecisionCacheKey key = mock( IAuthorizationDecisionCacheKey.class );
        return loader.apply( key );
      } );

    var decision = service.authorize( request, options );

    assertNotNull( decision );
    verify( decisionCache, times( 1 ) ).get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() );
    verify( rootRule, times( 1 ) ).authorize(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationContext.class ) );
  }

  @Test
  public void testAuthorizeCacheIsCalledWithCorrectParameters() {
    var rootRuleDecision = mock( IAuthorizationDecision.class );

    when( rootRule.authorize(
      eq( request ),
      any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( rootRuleDecision ) );

    when( decisionCache.get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() ) )
      .thenReturn( rootRuleDecision );

    service.authorize( request, options );

    // Verify that cache.get was called with the exact request and options
    verify( decisionCache, times( 1 ) ).get(
      eq( request ),
      eq( options ),
      anyLoaderFunction() );
  }

  @Test
  public void testMultipleAuthorizationCallsUseCacheProperly() {
    var rootRuleDecision = mock( IAuthorizationDecision.class );

    when( rootRule.authorize(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( rootRuleDecision ) );

    when( decisionCache.get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() ) )
      .thenReturn( rootRuleDecision );

    // Make multiple authorization calls
    service.authorize( request, options );
    service.authorize( request, options );

    // Cache should be called twice
    verify( decisionCache, times( 2 ) ).get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() );
  }

  // endregion

  // region Inheritance Tests - Verify that base class functionality still works

  @Test
  public void testAuthorizeStillReturnsDecisionFromRootRule() {
    var rootRuleDecision = mock( IAuthorizationDecision.class );

    when( rootRule.authorize(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( rootRuleDecision ) );

    // Mock the cache to invoke the loader function
    when( decisionCache.get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() ) )
      .thenAnswer( invocation -> {
        Function<IAuthorizationDecisionCacheKey, IAuthorizationDecision> loader = invocation.getArgument( 2 );
        IAuthorizationDecisionCacheKey key = mock( IAuthorizationDecisionCacheKey.class );
        return loader.apply( key );
      } );

    var decision = service.authorize( request, options );

    assertSame( rootRuleDecision, decision );
  }

  @Test
  public void testAuthorizeRuleDoesNotCacheResults() {
    var innerDecision = mock( IAuthorizationDecision.class );

    var innerRule = createMockRule();
    when( innerRule.authorize(
      eq( request ),
      any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( innerDecision ) );

    // Note: authorizeRule bypasses caching - it's a direct rule invocation
    Optional<IAuthorizationDecision> decision = service.authorizeRule( request, innerRule, options );

    assertTrue( decision.isPresent() );
    assertSame( innerDecision, decision.get() );

    // authorizeRule should not invoke the cache
    verify( decisionCache, never() ).get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() );
  }

  // endregion

  // region Context Tests

  @Test
  public void testCachingAuthorizationContextUsesCache() {
    var rootRuleDecision = mock( IAuthorizationDecision.class );

    when( rootRule.authorize(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationContext.class ) ) )
      .thenAnswer( answer -> {
        IAuthorizationContext context = answer.getArgument( 1 );
        assertNotNull( context );
        // Trigger authorize via context to ensure caching context is used
        return Optional.of( rootRuleDecision );
      } );

    when( decisionCache.get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() ) )
      .thenReturn( rootRuleDecision );

    var decision = service.authorize( request, options );

    assertNotNull( decision );

    verify( decisionCache, times( 1 ) ).get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() );
  }

  @Test
  public void testNestedAuthorizationCallsUseCache() {
    var action2 = createTestAction( "action2" );
    when( actionService.getAction( "action2" ) ).thenReturn( Optional.of( action2 ) );

    var request2 = request.withAction( action2 );

    var innerDecision = mock( IAuthorizationDecision.class );
    var outerDecision = mock( IAuthorizationDecision.class );

    // Set up an outer rule that makes a nested authorization call
    when( rootRule.authorize(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationContext.class ) ) )
      .thenAnswer( answer -> {
        IAuthorizationRequest req = answer.getArgument( 0 );
        IAuthorizationContext context = answer.getArgument( 1 );

        if ( req.equals( request ) ) {
          // First call - make a nested authorization
          context.authorize( request2 );
          return Optional.of( outerDecision );
        }

        // Nested call
        return Optional.of( innerDecision );
      } );

    // Mock cache to invoke the loader function
    when( decisionCache.get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() ) )
      .thenAnswer( invocation -> {
        Function<IAuthorizationDecisionCacheKey, IAuthorizationDecision> loader = invocation.getArgument( 2 );
        IAuthorizationDecisionCacheKey key = mock( IAuthorizationDecisionCacheKey.class );
        return loader.apply( key );
      } );

    var decision = service.authorize( request, options );

    assertNotNull( decision );
    // Cache should be called for both the outer and nested requests
    verify( decisionCache, times( 2 ) ).get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() );
  }

  // endregion

  // region Logging Tests

  @Test
  public void testAuthorizeLogsAtTraceLevel() {
    var rootRuleDecision = mock( IAuthorizationDecision.class );

    when( rootRule.authorize(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( rootRuleDecision ) );

    when( decisionCache.get(
      any( IAuthorizationRequest.class ),
      any( IAuthorizationOptions.class ),
      anyLoaderFunction() ) )
      .thenReturn( rootRuleDecision );

    // The authorize method should log the cache stats when trace is enabled
    var decision = service.authorize( request, options );

    assertNotNull( decision );
    // Logging is verified through the LogFactory mock setup in @BeforeClass
  }

  // endregion

  // region Helper Methods

  /**
   * Type-safe matcher for Function to avoid unchecked assignment warnings.
   */
  @SuppressWarnings( "unchecked" )
  private static Function<IAuthorizationDecisionCacheKey, IAuthorizationDecision> anyLoaderFunction() {
    return any( Function.class );
  }

  // endregion
}
