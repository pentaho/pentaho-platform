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

import edu.umd.cs.findbugs.annotations.NonNull;
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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRuleOverrider;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IDerivedAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.DerivedAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.exceptions.AuthorizationRequestCycleException;
import org.pentaho.platform.engine.security.authorization.core.exceptions.AuthorizationRequestUndefinedActionException;
import org.pentaho.platform.engine.security.authorization.core.exceptions.AuthorizationRuleException;

import java.util.Optional;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createMockRule;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class AuthorizationServiceTest {

  /**
   * Test subclass of IAuthorizationRequest for testing request type compatibility.
   */
  private static class SpecificAuthorizationRequest extends AuthorizationRequest {
    public SpecificAuthorizationRequest( AuthorizationUser user, IAuthorizationAction action ) {
      super( user, action );
    }
  }

  private IAuthorizationActionService actionService;
  private IAuthorizationRule<IAuthorizationRequest> rootRule;
  private IAuthorizationOptions options;
  private AuthorizationService service;
  private IAuthorizationRequest request;
  private IAuthorizationAction action;

  private static MockedStatic<LogFactory> logFactoryMockedStatic;

  @BeforeClass
  public static void init() {
    Log logger = mock( Log.class );
    when( logger.isDebugEnabled() ).thenReturn( true );

    logFactoryMockedStatic = mockStatic( LogFactory.class );
    logFactoryMockedStatic
      .when( () -> LogFactory.getLog( AuthorizationService.class ) )
      .thenReturn( logger );
  }

  @Before
  public void setUp() {
    actionService = mock( IAuthorizationActionService.class );
    options = mock( IAuthorizationOptions.class );

    action = createTestAction( "action" );
    when( actionService.getAction( "action" ) ).thenReturn( Optional.of( action ) );

    rootRule = createMockRule();

    service = new AuthorizationService( actionService, rootRule );

    request = new AuthorizationRequest( createTestUser(), action );
  }

  @AfterClass
  public static void tearDownAll() {
    logFactoryMockedStatic.close();
  }

  @Test
  public void testAuthorizeReturnsDecisionFromRootRule() {
    var rootRuleDecision = mock( IAuthorizationDecision.class );

    when( rootRule.authorize( eq( request ), any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( rootRuleDecision ) );

    var decision = service.authorize( request, options );

    assertEquals( rootRuleDecision, decision );
  }

  @Test
  public void testAuthorizeReturnsDefaultDecisionIfRootRuleAbstains() {

    when( rootRule.authorize( eq( request ), any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.empty() );

    var decision = service.authorize( request, options );

    assertNotNull( decision );
    assertFalse( decision.isGranted() );
  }

  @Test( expected = AuthorizationRequestUndefinedActionException.class )
  public void testAuthorizeThrowsIfRequestActionIsUndefined() {
    when( actionService.getAction( "action" ) ).thenReturn( Optional.empty() );

    service.authorize( request, options );
  }

  @Test
  public void testAuthorizeResolvesRequestActionToRegisteredOne() {

    // An unregistered action is provided in the request, having the same name as a registered action.
    var unregisteredAction = createTestAction( "action" );

    request = new AuthorizationRequest( createTestUser(), unregisteredAction );

    var decision = service.authorize( request, options );

    assertNotNull( decision );
    assertNotSame( decision.getRequest(), request );
    assertSame( action, decision.getRequest().getAction() );
  }

  @Test
  public void testRuleGetsCorrectRequestAndContext() {

    when( rootRule.authorize( any( IAuthorizationRequest.class ), any( IAuthorizationContext.class ) ) )
      .thenAnswer( answer -> {
        // Verify that the rule receives the correct request and context.
        IAuthorizationRequest requestArg = answer.getArgument( 0 );
        IAuthorizationContext contextArg = answer.getArgument( 1 );

        assertNotNull( requestArg );
        assertNotNull( contextArg );

        assertSame( request, requestArg );
        assertSame( options, contextArg.getOptions() );
        assertSame( service, contextArg.getService() );

        return Optional.empty(); // Rule abstains
      } );

    var decision = service.authorize( request, options );

    assertNotNull( decision );
  }

  @Test
  public void testRuleCanCallAuthorizeWithDifferentRequest() {
    var decision2 = mock( IAuthorizationDecision.class );

    var action2 = createTestAction( "action2" );
    when( actionService.getAction( "action2" ) ).thenReturn( Optional.of( action2 ) );

    var request2 = request.withAction( action2 );

    var innerRule = createMockRule();
    when( innerRule.authorize( eq( request2 ), any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( decision2 ) );

    var outerRule = createMockRuleWithAnswer( ( req, context ) -> {
      var result = context.authorizeRule( request2, innerRule );
      return result.map( decision -> new DerivedAuthorizationDecision( request, decision2 ) );
    } );

    var testService = new AuthorizationService( actionService, outerRule );

    var decision = testService.authorize( request, options );

    assertNotNull( decision );
    assertTrue( decision instanceof IDerivedAuthorizationDecision );
    assertSame( decision2, ( (IDerivedAuthorizationDecision) decision ).getDerivedFromDecision() );
  }

  @Test
  public void testRuleCanCallAuthorizeRule() {
    var innerDecision = mock( IAuthorizationDecision.class );

    var innerRule = createMockRule();
    when( innerRule.authorize( eq( request ), any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( innerDecision ) );

    var outerRule = createMockRuleWithAnswer( ( req, context ) -> {
      return context.authorizeRule( request, innerRule );
    } );

    var testService = new AuthorizationService( actionService, outerRule );

    var decision = testService.authorize( request, options );
    assertSame( innerDecision, decision );
  }

  @Test( expected = AuthorizationRequestCycleException.class )
  public void testContextDetectsRequestsCyclesAndThrows() {
    var outerRule = createMockRuleWithAnswer( ( req, context ) -> {
      // Sending same request without change results in a cycle.
      return Optional.of( context.authorize( request ) );
    } );

    var testService = new AuthorizationService( actionService, outerRule );
    testService.authorize( request, options );
  }

  @Test( expected = AuthorizationRuleException.class )
  public void testRuleExceptionHandledIsWrappedInAuthorizationRuleException() {
    var ruleException = mock( RuntimeException.class );
    var rule = createMockRule();
    when( rule.authorize( any( IAuthorizationRequest.class ), any( IAuthorizationContext.class ) ) )
      .thenThrow( ruleException );

    var testService = new AuthorizationService( actionService, rule );

    testService.authorize( request, options );
  }


  // region authorizeRule tests
  @Test
  public void testAuthorizeRuleDelegatesToSpecifiedRule() {
    IAuthorizationDecision customDecision = mock( IAuthorizationDecision.class );

    IAuthorizationRule<IAuthorizationRequest> customRule = createMockRule();
    when( customRule.authorize( eq( request ), any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( customDecision ) );

    Optional<IAuthorizationDecision> decision = service.authorizeRule( request, customRule, options );

    assertTrue( decision.isPresent() );
    assertEquals( customDecision, decision.get() );
  }


  @Test
  public void testAuthorizeRuleReturnsEmptyWhenRequestTypeNotSupported() {
    // Create a rule that only accepts a specific request type
    @SuppressWarnings( "unchecked" )
    IAuthorizationRule<SpecificAuthorizationRequest> specificRule = mock( IAuthorizationRule.class );
    when( specificRule.getRequestType() ).thenReturn( SpecificAuthorizationRequest.class );

    // Use a standard IAuthorizationRequest (which is not assignable to SpecificAuthorizationRequest)
    Optional<IAuthorizationDecision> decision = service.authorizeRule( request, specificRule, options );

    // Should return empty because request type is not supported
    assertFalse( decision.isPresent() );
  }

  @Test
  public void testAuthorizeRuleProcessesWhenRequestTypeIsSupported() {
    IAuthorizationDecision expectedDecision = mock( IAuthorizationDecision.class );

    // Create a rule that accepts IAuthorizationRequest (which our request implements)
    IAuthorizationRule<IAuthorizationRequest> compatibleRule = createMockRule();
    when( compatibleRule.authorize( eq( request ), any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( expectedDecision ) );

    Optional<IAuthorizationDecision> decision = service.authorizeRule( request, compatibleRule, options );

    // Should process the rule and return the decision
    assertTrue( decision.isPresent() );
    assertEquals( expectedDecision, decision.get() );
  }

  @Test
  public void testAuthorizeRuleWorksWithSubclassRequestTypes() {
    IAuthorizationDecision expectedDecision = mock( IAuthorizationDecision.class );

    // Create a specific request subclass
    SpecificAuthorizationRequest specificRequest = new SpecificAuthorizationRequest(
      createTestUser(),
      action );

    // Create a rule that accepts the base IAuthorizationRequest type
    IAuthorizationRule<IAuthorizationRequest> baseRule = createMockRule();
    when( baseRule.authorize( eq( specificRequest ), any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( expectedDecision ) );

    Optional<IAuthorizationDecision> decision = service.authorizeRule( specificRequest, baseRule, options );

    // Should work because SpecificAuthorizationRequest is assignable to IAuthorizationRequest
    assertTrue( decision.isPresent() );
    assertEquals( expectedDecision, decision.get() );
  }
  // endregion

  // region Helper methods

  /**
   * Creates a mock authorization rule that uses a lambda to provide custom behavior.
   *
   * @param authorizeFunction The lambda function that implements the authorize behavior
   * @return A properly configured mock IAuthorizationRule
   */
  private IAuthorizationRule<IAuthorizationRequest> createMockRuleWithAnswer(
    @NonNull
    BiFunction<IAuthorizationRequest, IAuthorizationContext, Optional<IAuthorizationDecision>> authorizeFunction ) {

    IAuthorizationRule<IAuthorizationRequest> rule = createMockRule();
    when( rule.authorize( any( IAuthorizationRequest.class ), any( IAuthorizationContext.class ) ) )
      .thenAnswer( answer -> {
        IAuthorizationRequest req = answer.getArgument( 0 );
        IAuthorizationContext ctx = answer.getArgument( 1 );
        return authorizeFunction.apply( req, ctx );
      } );

    return rule;
  }
  // endregion

  // region Authorization Rule Override Tests
  @Test
  public void testAuthorizeWithRuleOverride() {
    // Mock the override rule overrider and its behavior
    IAuthorizationRuleOverrider overrider = mock( IAuthorizationRuleOverrider.class );
    IAuthorizationRule<IAuthorizationRequest> overriddenRule = createMockRule();
    IAuthorizationDecision overrideDecision = mock( IAuthorizationDecision.class );

    // Adjust the mocking setup to explicitly cast the overridden rule to the expected generic type
    doReturn( overriddenRule ).when( overrider ).override( rootRule );
    doReturn( Optional.of( overrideDecision ) )
      .when( overriddenRule )
      .authorize( eq( request ), any( IAuthorizationContext.class ) );

    // Configure options to specify the override rule overrider
    doReturn( overrider ).when( options ).getAuthorizationRuleOverrider();

    // Perform authorization
    var decision = service.authorize( request, options );

    // Verify that the override rule was applied
    assertSame( overrideDecision, decision );
  }

  @Test
  public void testAuthorizeWithoutRuleOverride() {
    // Mock the root rule and its decision
    IAuthorizationDecision rootDecision = mock( IAuthorizationDecision.class );

    doReturn( Optional.of( rootDecision ) )
      .when( rootRule )
      .authorize( eq( request ), any( IAuthorizationContext.class ) );

    // Configure options to not specify any override rule overrider
    doReturn( null ).when( options ).getAuthorizationRuleOverrider();

    // Perform authorization
    var decision = service.authorize( request, options );

    // Verify that the root rule was applied
    assertNotNull( decision );
    assertEquals( rootDecision, decision );
  }
  // endregion
}
