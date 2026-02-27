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

import java.util.Collections;
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

public class AllAuthorizationRuleTest {

  private IAuthorizationRequest request;
  private IAuthorizationContext context;
  private IAuthorizationOptions options;

  private IAuthorizationRule<IAuthorizationRequest> mockRule1;
  private IAuthorizationRule<IAuthorizationRequest> mockRule2;
  private IAuthorizationRule<IAuthorizationRequest> mockRule3;
  private IAuthorizationRule<IAuthorizationRequest> mockRule4;

  private IAuthorizationDecision mockGrantedDecision1;
  private IAuthorizationDecision mockGrantedDecision2;
  private IAuthorizationDecision mockGrantedDecision3;
  private IAuthorizationDecision mockDeniedDecision1;
  private IAuthorizationDecision mockDeniedDecision2;

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
    mockGrantedDecision3 = createMockDecision( request, true );
    mockDeniedDecision1 = createMockDecision( request, false );
    mockDeniedDecision2 = createMockDecision( request, false );
  }

  @Test
  public void testAuthorizeWithEmptyRulesListAbstains() {
    var allRule = new AllAuthorizationRule( Collections.emptyList() );

    var result = allRule.authorize( request, context );

    assertFalse( result.isPresent() );
  }

  // region Tests for AuthorizationDecisionReportingMode.SETTLED

  @Test
  public void testSettledModeWithSingleGrantedRuleReturnsExactDecision() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );

    var allRule = new AllAuthorizationRule( List.of( mockRule1 ) );
    var result = allRule.authorize( request, context );

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

    var allRule = new AllAuthorizationRule( List.of( mockRule1 ) );
    var result = allRule.authorize( request, context );

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

    var allRule = new AllAuthorizationRule( List.of( mockRule1 ) );
    var result = allRule.authorize( request, context );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testSettledModeWithAllGrantedRulesReturnsCompositeDecision() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockGrantedDecision2 ) );

    var allRule = new AllAuthorizationRule( List.of( mockRule1, mockRule2 ) );
    var result = allRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get().isGranted() );
    assertCompositeDecisionContainsExactly( result.get(), mockGrantedDecision1, mockGrantedDecision2 );
  }

  @Test
  public void testSettledModeStopsEarlyOnFirstDenialAndReturnsExactDecision() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );

    var allRule = new AllAuthorizationRule( List.of( mockRule1, mockRule2 ) );
    var result = allRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertFalse( result.get().isGranted() );
    assertFalse( result.get() instanceof ICompositeAuthorizationDecision );
    assertSame( mockDeniedDecision1, result.get() );

    // Verify that mockRule2 was never evaluated due to early termination
    verify( context, never() ).authorizeRule( request, mockRule2 );
  }

  @Test
  public void testSettledModeWithMixedGrantedAndAbstainingRulesReturnsCompositeDecision() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.empty() ); // abstains
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockGrantedDecision3 ) );

    var allRule = new AllAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3 ) );
    var result = allRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get().isGranted() );
    // Only non-abstaining decisions should be included
    assertCompositeDecisionContainsExactly( result.get(), mockGrantedDecision1, mockGrantedDecision3 );
  }

  @Test
  public void testSettledModeWithMixedDeniedAndAbstainingRulesReturnsFirstDenial() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.empty() ); // abstains
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockGrantedDecision3 ) );

    var allRule = new AllAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3 ) );
    var result = allRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertFalse( result.get().isGranted() );
    // Should stop at first denial and return that exact decision
    assertFalse( result.get() instanceof ICompositeAuthorizationDecision );
    assertSame( mockDeniedDecision1, result.get() );
  }

  @Test
  public void testSettledModeWithAllAbstainingRulesAbstains() {
    // Configure context for SETTLED mode (default)
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.empty() );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.empty() );
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.empty() );

    var allRule = new AllAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3 ) );
    var result = allRule.authorize( request, context );

    assertFalse( result.isPresent() );
  }
  // endregion

  // region Tests for AuthorizationDecisionReportingMode.SAME_GRANTED_STATUS
  @Test
  public void testSameGrantedStatusModeWithAllGrantedRulesReturnsGrantedCompositeDecision() {
    // Configure context for SAME_GRANTED_STATUS mode
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SAME_GRANTED_STATUS );

    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockGrantedDecision2 ) );
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockGrantedDecision3 ) );

    var allRule = new AllAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3 ) );
    var result = allRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get().isGranted() );

    // Should create composite decision since all have same granted status
    assertCompositeDecisionContainsExactly(
      result.get(),
      mockGrantedDecision1, mockGrantedDecision2, mockGrantedDecision3 );
  }

  @Test
  public void testSameGrantedStatusModeWithAllDeniedRulesReturnsDeniedCompositeDecision() {
    // Configure context for SAME_GRANTED_STATUS mode
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SAME_GRANTED_STATUS );

    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockDeniedDecision2 ) );

    var allRule = new AllAuthorizationRule( List.of( mockRule1, mockRule2 ) );
    var result = allRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertFalse( result.get().isGranted() );

    // Should create composite decision since all have same granted status (denied)
    assertCompositeDecisionContainsExactly( result.get(), mockDeniedDecision1, mockDeniedDecision2 );
  }

  @Test
  public void testSameGrantedStatusModeWithMixedStatusReturnsCompositeOfDeniedDecisions() {
    // Configure context for SAME_GRANTED_STATUS mode
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SAME_GRANTED_STATUS );

    // Scenario: granted -> denied -> granted -> denied
    // Should collect the denied decisions into a composite after the transition
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockGrantedDecision2 ) );
    when( context.authorizeRule( request, mockRule4 ) ).thenReturn( Optional.of( mockDeniedDecision2 ) );

    var allRule = new AllAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3, mockRule4 ) );
    var result = allRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertFalse( result.get().isGranted() );

    // Should create composite decision with the denied decisions.
    assertCompositeDecisionContainsExactly( result.get(), mockDeniedDecision1, mockDeniedDecision2 );
  }
  // endregion

  // region Tests for AuthorizationDecisionReportingMode.FULL
  @Test
  public void testFullModeWithMixedStatusReturnsDeniedCompositeOfAllDecisions() {
    // Configure context for FULL mode
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.FULL );

    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockGrantedDecision3 ) );

    var allRule = new AllAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3 ) );
    var result = allRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertFalse( result.get().isGranted() ); // AllRule: denied if any rule denies

    // Should create composite decision including all decisions
    assertCompositeDecisionContainsExactly(
      result.get(),
      mockGrantedDecision1, mockDeniedDecision1, mockGrantedDecision3 );
  }

  @Test
  public void testFullModeWithAllGrantedReturnsGrantedCompositeOfAllDecisions() {
    // Configure context for FULL mode
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.FULL );

    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockGrantedDecision2 ) );
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockGrantedDecision3 ) );

    var allRule = new AllAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3 ) );
    var result = allRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get().isGranted() );

    // Should create composite decision with all granted decisions
    assertCompositeDecisionContainsExactly(
      result.get(),
      mockGrantedDecision1, mockGrantedDecision2, mockGrantedDecision3 );
  }
  // endregion

  // region Tests comparing behavior across reporting modes
  @Test
  public void testDifferentModesWithSameInputProduceDifferentDecisionStructures() {
    // Test that same rule configuration produces different decision structures based on reporting mode

    // Setup: two granted rules followed by one denied rule
    when( context.authorizeRule( request, mockRule1 ) ).thenReturn( Optional.of( mockGrantedDecision1 ) );
    when( context.authorizeRule( request, mockRule2 ) ).thenReturn( Optional.of( mockGrantedDecision2 ) );
    when( context.authorizeRule( request, mockRule3 ) ).thenReturn( Optional.of( mockDeniedDecision1 ) );
    when( context.authorizeRule( request, mockRule4 ) ).thenReturn( Optional.of( mockDeniedDecision2 ) );

    var allRule = new AllAuthorizationRule( List.of( mockRule1, mockRule2, mockRule3, mockRule4 ) );

    // Test SETTLED mode - should stop early and return single decision
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );
    var settledResult = allRule.authorize( request, context );
    assertTrue( settledResult.isPresent() );
    assertSame( mockDeniedDecision1, settledResult.get() );

    // Test SAME_GRANTED_STATUS mode - should replace on status change
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SAME_GRANTED_STATUS );
    var sameStatusResult = allRule.authorize( request, context );
    assertTrue( sameStatusResult.isPresent() );
    assertFalse( sameStatusResult.get().isGranted() );
    assertCompositeDecisionContainsExactly( sameStatusResult.get(), mockDeniedDecision1, mockDeniedDecision2 );

    // Test FULL mode - should collect all decisions
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.FULL );
    var fullResult = allRule.authorize( request, context );
    assertTrue( fullResult.isPresent() );
    assertFalse( fullResult.get().isGranted() );
    assertCompositeDecisionContainsExactly(
      fullResult.get(),
      mockGrantedDecision1, mockGrantedDecision2, mockDeniedDecision1, mockDeniedDecision2 );
  }
  // endregion
}
