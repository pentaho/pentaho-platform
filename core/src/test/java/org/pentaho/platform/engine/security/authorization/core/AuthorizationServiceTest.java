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
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationErrorDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IImpliedAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.ImpliedAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.exceptions.AuthorizationRequestCycleException;
import org.pentaho.platform.engine.security.authorization.core.exceptions.AuthorizationRequestUndefinedActionException;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class AuthorizationServiceTest {
  private IAuthorizationActionService actionService;
  private IAuthorizationRule rootRule;
  private IAuthorizationOptions options;
  private AuthorizationService service;
  private IAuthorizationRequest request;
  private IAuthorizationAction action;

  private static MockedStatic<LogFactory> logFactoryMockedStatic;
  private static Log logger;

  @BeforeClass
  public static void init() {
    logger = mock( Log.class );
    when( logger.isDebugEnabled() ).thenReturn( true );

    logFactoryMockedStatic = mockStatic( LogFactory.class );
    logFactoryMockedStatic
      .when( () -> LogFactory.getLog( AuthorizationService.class ) )
      .thenReturn( logger );
  }

  @Before
  public void setUp() {
    actionService = mock( IAuthorizationActionService.class );
    rootRule = mock( IAuthorizationRule.class );
    options = mock( IAuthorizationOptions.class );

    action = mock( IAuthorizationAction.class );
    when( action.getName() ).thenReturn( "action" );
    when( actionService.getAction( "action" ) ).thenReturn( Optional.of( action ) );

    service = new AuthorizationService( actionService, rootRule );

    request = new AuthorizationRequest(
      new AuthorizationUser( "user", Set.of( new AuthorizationRole( "role" ) ) ),
      action );
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

  @Test
  public void testAuthorizeRuleDelegatesToSpecifiedRule() {

    IAuthorizationRule customRule = mock( IAuthorizationRule.class );
    IAuthorizationDecision customDecision = mock( IAuthorizationDecision.class );

    when( customRule.authorize( eq( request ), any( IAuthorizationContext.class ) ) )
      .thenReturn( Optional.of( customDecision ) );

    Optional<IAuthorizationDecision> decision = service.authorizeRule( request, customRule, options );

    assertTrue( decision.isPresent() );
    assertEquals( customDecision, decision.get() );
  }

  @Test
  public void testAuthorizeDeniesIfRequestActionIsUndefined() {
    when( actionService.getAction( "action" ) ).thenReturn( Optional.empty() );

    var decision = service.authorize( request, options );

    assertNotNull( decision );
    assertFalse( decision.isGranted() );
    assertTrue( decision instanceof IAuthorizationErrorDecision );

    var errorDecision = (IAuthorizationErrorDecision) decision;
    assertTrue( errorDecision.getCause() instanceof AuthorizationRequestUndefinedActionException );
  }

  @Test
  public void testAuthorizeResolvesRequestActionToRegisteredOne() {

    // An unregistered action is provided in the request, having the same name as a registered action.
    var unregisteredAction = mock( IAuthorizationAction.class );
    when( unregisteredAction.getName() ).thenReturn( "action" );

    request = new AuthorizationRequest(
      new AuthorizationUser( "user", Set.of( new AuthorizationRole( "role" ) ) ),
      unregisteredAction );

    var decision = service.authorize( request, options );

    assertNotNull( decision );
    assertNotSame( decision.getRequest(), request );
    assertSame( action, decision.getRequest().getAction() );
  }

  @Test
  public void testRuleGetsCorrectRequestAndContext() {

    when( rootRule.authorize( any( IAuthorizationRequest.class ), any( IAuthorizationContext.class ) ) )
      .thenAnswer( ( answer ) -> {
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

    var action2 = mock( IAuthorizationAction.class );
    when( action2.getName() ).thenReturn( "action2" );
    when( actionService.getAction( "action2" ) ).thenReturn( Optional.of( action2 ) );

    var request2 = request.withAction( action2 );

    IAuthorizationRule innerRule = ( request, context ) ->
      Optional.of( decision2 );

    IAuthorizationRule outerRule = ( request, context ) -> {
      var result = context.authorizeRule( request2, innerRule );
      return result.map( decision -> new ImpliedAuthorizationDecision( request, decision2 ) );
    };

    var service = new AuthorizationService( actionService, outerRule );

    var decision = service.authorize( request, options );

    assertNotNull( decision );
    assertTrue( decision instanceof IImpliedAuthorizationDecision );
    assertSame( decision2, ( (IImpliedAuthorizationDecision) decision ).getImpliedFromDecision() );
  }

  @Test
  public void testRuleCanCallAuthorizeRule() {
    var innerDecision = mock( IAuthorizationDecision.class );

    IAuthorizationRule innerRule = ( request, context ) ->
      Optional.of( innerDecision );

    IAuthorizationRule outerRule = ( request, context ) ->
      context.authorizeRule( request, innerRule );

    var service = new AuthorizationService( actionService, outerRule );

    var decision = service.authorize( request, options );
    assertSame( innerDecision, decision );
  }

  @Test
  public void testContextDetectsRequestsCyclesAndDeniesDecision() {

    IAuthorizationRule outerRule = ( request, context ) -> {
      var innerDecision = context.authorize( request );
      assertNotNull( innerDecision );
      assertFalse( innerDecision.isGranted() );
      assertTrue( innerDecision instanceof IAuthorizationErrorDecision );
      assertTrue(
        ( (IAuthorizationErrorDecision) innerDecision ).getCause() instanceof AuthorizationRequestCycleException );

      return Optional.of( innerDecision );
    };

    var service = new AuthorizationService( actionService, outerRule );
    var decision = service.authorize( request, options );

    assertNotNull( decision );
    assertFalse( decision.isGranted() );
    assertTrue( decision instanceof IAuthorizationErrorDecision );
    assertTrue( ( (IAuthorizationErrorDecision) decision ).getCause() instanceof AuthorizationRequestCycleException );
  }

  @Test
  public void testRuleExceptionHandledAsErrorDecision() {
    var ruleException = mock( RuntimeException.class );

    IAuthorizationRule rule = ( request, context ) -> {
      throw ruleException;
    };

    var service = new AuthorizationService( actionService, rule );

    var decision = service.authorize( request, options );

    assertNotNull( decision );
    assertFalse( decision.isGranted() );
    assertTrue( decision instanceof IAuthorizationErrorDecision );
    assertSame( ruleException, ( (IAuthorizationErrorDecision) decision ).getCause() );
  }
}
