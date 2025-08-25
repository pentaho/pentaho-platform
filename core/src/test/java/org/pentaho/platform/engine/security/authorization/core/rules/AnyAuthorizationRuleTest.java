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

package org.pentaho.platform.engine.security.authorization.core.rules;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.ICompositeAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.assertCompositeDecisionContainsExactly;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createMockDecision;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createMockRule;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class AnyAuthorizationRuleTest {

  private IAuthorizationRequest request;
  private IAuthorizationContext context;
  private IAuthorizationOptions options;

  private IAuthorizationRule<IAuthorizationRequest> mockRule1;
  private IAuthorizationRule<IAuthorizationRequest> mockRule2;
  private IAuthorizationRule<IAuthorizationRequest> mockRule3;
  private IAuthorizationRule<IAuthorizationRequest> mockRule4;

  private IAuthorizationDecision mockGrantedDecision1;
  private IAuthorizationDecision mockGrantedDecision2;
  private IAuthorizationDecision mockDeniedDecision1;
  private IAuthorizationDecision mockDeniedDecision2;
  private IAuthorizationDecision mockDeniedDecision3;

  @Before
  public void setUp() {
    var user = createTestUser();
    var action = createTestAction( "read" );

    request = new AuthorizationRequest( user, action );

    // Set up context and options mocking
    options = mock( IAuthorizationOptions.class );
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );

    context = mock( IAuthorizationContext.class );
    when( context.getOptions() ).thenReturn( options );

    // Create mock rules
    mockRule1 = createMockRule();
    mockRule2 = createMockRule();
    mockRule3 = createMockRule();
    mockRule4 = createMockRule();

    // Create mock decisions
    mockGrantedDecision1 = createMockDecision( request, true );
    mockGrantedDecision2 = createMockDecision( request, true );
    mockDeniedDecision1 = createMockDecision( request, false );
    mockDeniedDecision2 = createMockDecision( request, false );
    mockDeniedDecision3 = createMockDecision( request, false );
  }


  // region Tests for AuthorizationDecisionReportingMode.SETTLED

  @Test
  public void testSettledModeWithSingleGrantedRuleReturnsExactDecision() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1 ) );
    var result = anyRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get().isGranted() );
    assertFalse( result.get() instanceof ICompositeAuthorizationDecision );
    assertSame( mockGrantedDecision1, result.get() );
  }

  @Test
  public void testSettledModeWithSingleDeniedRuleReturnsExactDecision() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1 ) );
    var result = anyRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertFalse( result.get().isGranted() );
    assertFalse( result.get() instanceof ICompositeAuthorizationDecision );
    assertSame( mockDeniedDecision1, result.get() );
  }

  @Test
  public void testSettledModeWithSingleAbstainingRuleAbstains() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.empty() );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1 ) );
    var result = anyRule.authorize( request, context );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testSettledModeWithAllDeniedRulesReturnsCompositeDecision() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockDeniedDecision2 ) );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1, mockRule2 ) );
    var result = anyRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertFalse( result.get().isGranted() );
    assertCompositeDecisionContainsExactly( result.get(), mockDeniedDecision1, mockDeniedDecision2 );
  }

  @Test
  public void testSettledModeStopsEarlyOnFirstGrantAndReturnsExactDecision() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1, mockRule2 ) );
    var result = anyRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get().isGranted() );
    assertFalse( result.get() instanceof ICompositeAuthorizationDecision );
    assertSame( mockGrantedDecision1, result.get() );

    // Verify that mockRule2 was never evaluated due to early termination.
    verify( context, never() ).authorizeRule( request, mockRule2 );
  }

  @Test
  public void testSettledModeWithMixedDeniedAndAbstainingRulesReturnsCompositeDecision() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.empty() ); // abstains
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockDeniedDecision2 ) );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3 ) );
    var result = anyRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertFalse( result.get().isGranted() );
    // Only non-abstaining decisions should be included
    assertCompositeDecisionContainsExactly( result.get(), mockDeniedDecision1, mockDeniedDecision2 );
  }

  @Test
  public void testSettledModeWithMixedGrantedAndAbstainingRulesReturnsFirstGrant() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.empty() ); // abstains
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3 ) );
    var result = anyRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get().isGranted() );
    // Should stop at first grant and return that exact decision
    assertFalse( result.get() instanceof ICompositeAuthorizationDecision );
    assertSame( mockGrantedDecision1, result.get() );
  }

  @Test
  public void testSettledModeWithAllAbstainingRulesAbstains() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.empty() );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.empty() );
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.empty() );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3 ) );
    var result = anyRule.authorize( request, context );

    assertFalse( result.isPresent() );
  }
  // endregion

  // region Tests for AuthorizationDecisionReportingMode.SAME_GRANTED_STATUS
  @Test
  public void testSameGrantedStatusModeWithAllDeniedRulesReturnsDeniedCompositeDecision() {
    // Configure context for SAME_GRANTED_STATUS mode
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SAME_GRANTED_STATUS );

    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockDeniedDecision2 ) );
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockDeniedDecision3 ) );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3 ) );
    var result = anyRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertFalse( result.get().isGranted() );

    // Should create composite decision since all have same granted status
    assertCompositeDecisionContainsExactly(
      result.get(),
      mockDeniedDecision1, mockDeniedDecision2, mockDeniedDecision3 );
  }

  @Test
  public void testSameGrantedStatusModeWithAllGrantedRulesReturnsGrantedCompositeDecision() {
    // Configure context for SAME_GRANTED_STATUS mode
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SAME_GRANTED_STATUS );

    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockGrantedDecision2 ) );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1, mockRule2 ) );
    var result = anyRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get().isGranted() );

    // Should create composite decision since all have same granted status (granted)
    assertCompositeDecisionContainsExactly( result.get(), mockGrantedDecision1, mockGrantedDecision2 );
  }

  @Test
  public void testSameGrantedStatusModeWithMixedStatusReturnsCompositeOfGrantedDecisions() {
    // Configure context for SAME_GRANTED_STATUS mode
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SAME_GRANTED_STATUS );

    // Scenario: denied -> granted -> denied -> granted
    // Should collect the granted decisions into a composite after the transition
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockDeniedDecision2 ) );
    when( context.authorizeRule( request, mockRule4 ) ).thenReturn( Optional.of( mockGrantedDecision2 ) );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3, mockRule4 ) );
    var result = anyRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get().isGranted() );

    // Should create composite decision with the granted decisions.
    assertCompositeDecisionContainsExactly( result.get(), mockGrantedDecision1, mockGrantedDecision2 );
  }
  // endregion

  // region Tests for AuthorizationDecisionReportingMode.FULL
  @Test
  public void testFullModeWithMixedStatusReturnsGrantedCompositeOfAllDecisions() {
    // Configure context for FULL mode
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.FULL );

    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockDeniedDecision2 ) );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3 ) );
    var result = anyRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get().isGranted() );

    // Should create composite decision including all decisions
    assertCompositeDecisionContainsExactly(
      result.get(),
      mockDeniedDecision1, mockGrantedDecision1, mockDeniedDecision2 );
  }

  @Test
  public void testFullModeWithAllDeniedReturnsDeniedCompositeOfAllDecisions() {
    // Configure context for FULL mode
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.FULL );

    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockDeniedDecision2 ) );
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockDeniedDecision3 ) );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3 ) );
    var result = anyRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertFalse( result.get().isGranted() );

    // Should create composite decision with all denied decisions
    assertCompositeDecisionContainsExactly(
      result.get(),
      mockDeniedDecision1, mockDeniedDecision2, mockDeniedDecision3 );
  }
  // endregion

  // region Tests comparing behavior across reporting modes
  @Test
  public void testDifferentModesWithSameInputProduceDifferentDecisionStructures() {
    // Test that same rule configuration produces different decision structures based on reporting mode

    // Setup: two denied rules followed by one granted rule
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockDeniedDecision2 ) );
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );
    when( context.authorizeRule( request, mockRule4 ) ).thenReturn( Optional.of( mockGrantedDecision2 ) );

    var anyRule = new AnyAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3, mockRule4 ) );

    // Test SETTLED mode - should stop early and return single decision
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    var settledResult = anyRule.authorize( request, context );
    assertTrue( settledResult.isPresent() );
    assertSame( mockGrantedDecision1, settledResult.get() );

    // Test SAME_GRANTED_STATUS mode - should replace on status change
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SAME_GRANTED_STATUS );
    var sameStatusResult = anyRule.authorize( request, context );
    assertTrue( sameStatusResult.isPresent() );
    assertTrue( sameStatusResult.get().isGranted() );
    assertCompositeDecisionContainsExactly( sameStatusResult.get(), mockGrantedDecision1, mockGrantedDecision2 );

    // Test FULL mode - should collect all decisions
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.FULL );
    var fullResult = anyRule.authorize( request, context );
    assertTrue( fullResult.isPresent() );
    assertTrue( fullResult.get().isGranted() );
    assertCompositeDecisionContainsExactly(
      fullResult.get(),
      mockDeniedDecision1, mockDeniedDecision2, mockGrantedDecision1, mockGrantedDecision2 );
  }
  // endregion
}
