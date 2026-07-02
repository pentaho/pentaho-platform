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

package org.pentaho.platform.engine.security.authorization;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginManagerListener;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.rules.AllAuthorizationRule;
import org.pentaho.platform.engine.security.authorization.core.rules.AnyAuthorizationRule;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createMockDecision;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createMockRule;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class PentahoAuthorizationRuleLevelTest {

  private IAuthorizationRequest request;
  private IAuthorizationContext context;
  private IPluginManager mockPluginManager;

  private IAuthorizationRule<IAuthorizationRequest> mockLevelRule1;
  private IAuthorizationRule<IAuthorizationRequest> mockLevelRule2;
  private IAuthorizationRule<IAuthorizationRequest> mockPostRule1;
  private IAuthorizationRule<IAuthorizationRequest> mockPostRule2;

  private IAuthorizationDecision mockGrantedDecision;

  @Before
  public void setUp() {
    var user = createTestUser();
    var action = createTestAction( "read" );

    request = new AuthorizationRequest( user, action );

    // Set up context and options mocking
    var options = mock( IAuthorizationOptions.class );
    when( options.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );

    context = mock( IAuthorizationContext.class );
    when( context.getOptions() ).thenReturn( options );

    // Create mock plugin manager
    mockPluginManager = mock( IPluginManager.class );

    // Create mock rules using AuthorizationTestHelpers
    mockLevelRule1 = createMockRule();
    mockLevelRule2 = createMockRule();
    mockPostRule1 = createMockRule();
    mockPostRule2 = createMockRule();

    // Create mock decisions
    mockGrantedDecision = createMockDecision( request, true );
  }

  // region Constructor Tests
  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullPluginManagerThrows() {
    //noinspection DataFlowIssue
    new PentahoAuthorizationRuleLevel(
      null,
      PentahoAuthorizationRuleLevel.RuleLevelType.ALL,
      "test-level",
      Collections.emptyList()
    );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRuleLevelTypeThrows() {
    //noinspection DataFlowIssue
    new PentahoAuthorizationRuleLevel(
      mockPluginManager,
      null,
      "test-level",
      Collections.emptyList()
    );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullPostRulesThrows() {
    //noinspection DataFlowIssue
    new PentahoAuthorizationRuleLevel(
      mockPluginManager,
      PentahoAuthorizationRuleLevel.RuleLevelType.ALL,
      "test-level",
      null
    );
  }

  @Test
  public void testConstructorWithNullRuleLevelSucceeds() {
    // Should not throw - null rule level is allowed
    var ruleLevel = new PentahoAuthorizationRuleLevel(
      mockPluginManager,
      PentahoAuthorizationRuleLevel.RuleLevelType.ALL,
      null,
      Collections.emptyList()
    );

    assertEquals( IAuthorizationRequest.class, ruleLevel.getRequestType() );
  }

  @Test
  public void testConstructorWithEmptyPostRulesSucceeds() {
    var ruleLevel = new PentahoAuthorizationRuleLevel(
      mockPluginManager,
      PentahoAuthorizationRuleLevel.RuleLevelType.ALL,
      "test-level",
      Collections.emptyList()
    );

    assertEquals( IAuthorizationRequest.class, ruleLevel.getRequestType() );
  }

  @Test
  public void testConstructorAddsPluginManagerListener() {
    new PentahoAuthorizationRuleLevel(
      mockPluginManager,
      PentahoAuthorizationRuleLevel.RuleLevelType.ALL,
      "test-level",
      Collections.emptyList()
    );

    // Verify that the plugin manager listener was added
    verify( mockPluginManager, times( 1 ) ).addPluginManagerListener( any() );
  }
  // endregion

  @Test
  public void testAllRuleLevelTypeCreatesAllAuthorizationRule() {
    var objectRef = createMockObjectReference( mockLevelRule1, "test-level" );
    var mockRuleRefsSupplier = createMockRuleReferencesSupplier( List.of( objectRef ) );

    var ruleLevel = new PentahoAuthorizationRuleLevel(
      mockPluginManager,
      PentahoAuthorizationRuleLevel.RuleLevelType.ALL,
      ref -> "test-level".equals( ref.getAttributes().get( "ruleLevel" ) ),
      Collections.emptyList(),
      mockRuleRefsSupplier
    );

    var delegateRule = ruleLevel.getDelegateRule();

    assertTrue( delegateRule instanceof AllAuthorizationRule );
  }

  @Test
  public void testAllRuleLevelTypeWithPostRulesIncludesPostRules() {
    var objectRef = createMockObjectReference( mockLevelRule1, "test-level" );
    var mockRuleRefsSupplier = createMockRuleReferencesSupplier( List.of( objectRef ) );

    var ruleLevel = new PentahoAuthorizationRuleLevel(
      mockPluginManager,
      PentahoAuthorizationRuleLevel.RuleLevelType.ALL,
      ref -> "test-level".equals( ref.getAttributes().get( "ruleLevel" ) ),
      List.of( mockPostRule1 ),
      mockRuleRefsSupplier
    );

    var delegateRule = ruleLevel.getDelegateRule();

    assertTrue( delegateRule instanceof AllAuthorizationRule );

    // Test that the delegate rule works correctly with the combined rules
    when( context.authorizeRule( request, mockLevelRule1 ) ).thenReturn( Optional.of( mockGrantedDecision ) );
    when( context.authorizeRule( request, mockPostRule1 ) ).thenReturn( Optional.of( mockGrantedDecision ) );

    var result = delegateRule.authorize( request, context );

    assertTrue( result.isPresent() );
    assertTrue( result.get().isGranted() );

    // Verify both level rule and post rule were called
    verify( context, times( 1 ) ).authorizeRule( request, mockLevelRule1 );
    verify( context, times( 1 ) ).authorizeRule( request, mockPostRule1 );
  }

  @Test
  public void testAnyRuleLevelTypeCreatesAnyAuthorizationRule() {
    var objectRef = createMockObjectReference( mockLevelRule1, "test-level" );
    var mockRuleRefsSupplier = createMockRuleReferencesSupplier( List.of( objectRef ) );

    var ruleLevel = new PentahoAuthorizationRuleLevel(
      mockPluginManager,
      PentahoAuthorizationRuleLevel.RuleLevelType.ANY,
      ref -> "test-level".equals( ref.getAttributes().get( "ruleLevel" ) ),
      Collections.emptyList(),
      mockRuleRefsSupplier
    );

    var delegateRule = ruleLevel.getDelegateRule();

    assertTrue( delegateRule instanceof AnyAuthorizationRule );
  }

  @Test
  public void testRuleLevelFilteringWithSpecificLevel() {
    var objectRef1 = createMockObjectReference( mockLevelRule1, "level1" );
    var objectRef2 = createMockObjectReference( mockLevelRule2, "level2" );
    var mockRuleRefsSupplier = createMockRuleReferencesSupplier( List.of( objectRef1, objectRef2 ) );

    when( context.authorizeRule( request, mockLevelRule1 ) ).thenReturn( Optional.of( mockGrantedDecision ) );

    var ruleLevel = new PentahoAuthorizationRuleLevel(
      mockPluginManager,
      PentahoAuthorizationRuleLevel.RuleLevelType.ALL,
      ref -> "level1".equals( ref.getAttributes().get( "ruleLevel" ) ),
      Collections.emptyList(),
      mockRuleRefsSupplier
    );

    var result = ruleLevel.authorize( request, context );

    // Should only process rules with "level1", not "level2"
    assertTrue( result.isPresent() );
    assertTrue( result.get().isGranted() );

    // Verify only the level1 rule was called
    verify( context, times( 1 ) ).authorizeRule( request, mockLevelRule1 );
    verify( context, never() ).authorizeRule( request, mockLevelRule2 );
  }

  @Test
  public void testPluginManagerOnReloadUpdatesDelegateRule() {
    // Create initial supplier with one rule
    var objectRef1 = createMockObjectReference( mockLevelRule1, "test-level" );
    var mockRuleRefsSupplier = createMockRuleReferencesSupplier( List.of( objectRef1 ) );

    // Create rule level instance
    var ruleLevel = new PentahoAuthorizationRuleLevel(
      mockPluginManager,
      PentahoAuthorizationRuleLevel.RuleLevelType.ALL,
      ref -> "test-level".equals( ref.getAttributes().get( "ruleLevel" ) ),
      Collections.emptyList(),
      mockRuleRefsSupplier
    );

    // Capture the initial delegate rule
    var initialDelegateRule = ruleLevel.getDelegateRule();

    // Capture the plugin manager listener that was registered
    verify( mockPluginManager ).addPluginManagerListener( any() );

    // Get the actual listener that was registered
    var listenerCaptor = ArgumentCaptor.forClass( IPluginManagerListener.class );
    verify( mockPluginManager ).addPluginManagerListener( listenerCaptor.capture() );
    var registeredListener = listenerCaptor.getValue();

    // Trigger the onReload event
    registeredListener.onReload();

    // Capture the delegate rule after reload
    var updatedDelegateRule = ruleLevel.getDelegateRule();

    assertNotSame( initialDelegateRule, updatedDelegateRule );
  }

  // region Helper Methods for Creating Mock Suppliers and Object References
  @SuppressWarnings( { "unchecked", "rawtypes" } )
  private static Supplier<List<IPentahoObjectReference<IAuthorizationRule>>> createMockRuleReferencesSupplier(
    List<IPentahoObjectReference<IAuthorizationRule<IAuthorizationRequest>>> objectRefs ) {

    Supplier<List<IPentahoObjectReference<IAuthorizationRule>>> mockRuleRefsSupplier = mock( Supplier.class );
    when( mockRuleRefsSupplier.get() ).thenReturn( (List) objectRefs );
    return mockRuleRefsSupplier;
  }

  @SuppressWarnings( "unchecked" )
  private IPentahoObjectReference<IAuthorizationRule<IAuthorizationRequest>> createMockObjectReference(
    IAuthorizationRule<IAuthorizationRequest> rule,
    Map<String, Object> attributes ) {

    IPentahoObjectReference<IAuthorizationRule<IAuthorizationRequest>> objectRef =
      mock( IPentahoObjectReference.class );
    when( objectRef.getObject() ).thenReturn( rule );
    when( objectRef.getAttributes() ).thenReturn( attributes );

    return objectRef;
  }

  private IPentahoObjectReference<IAuthorizationRule<IAuthorizationRequest>> createMockObjectReference(
    IAuthorizationRule<IAuthorizationRequest> rule,
    String ruleLevel ) {

    return createMockObjectReference( rule, Map.of( "ruleLevel", ruleLevel ) );
  }
  // endregion

  // region Tests for buildLevelRulePredicate method
  @Test
  public void testBuildLevelRulePredicateWithSpecificLevel() {
    var predicate = PentahoAuthorizationRuleLevel.buildLevelRulePredicate( "level1", false );

    // Create object references with different rule levels
    var level1Ref = createMockObjectReference( mockLevelRule1, "level1" );
    var level2Ref = createMockObjectReference( mockLevelRule2, "level2" );
    var emptyLevelRef = createMockObjectReference( mockPostRule1, "" );
    var nullLevelRef = createMockObjectReference( mockPostRule2, Map.of() );

    // Only level1 should match
    assertTrue( "Should match rule with matching level", predicate.test( level1Ref ) );
    assertFalse( "Should not match rule with different level", predicate.test( level2Ref ) );
    assertFalse( "Should not match rule with empty level", predicate.test( emptyLevelRef ) );
    assertFalse( "Should not match rule with null level", predicate.test( nullLevelRef ) );
  }

  @Test
  public void testBuildLevelRulePredicateWithNullLevel() {
    var predicate = PentahoAuthorizationRuleLevel.buildLevelRulePredicate( null, false );

    // Create object references with different rule levels
    var level1Ref = createMockObjectReference( mockLevelRule1, "level1" );
    var emptyLevelRef = createMockObjectReference( mockPostRule1, "" );
    var nullLevelRef = createMockObjectReference( mockPostRule2, Map.of() );

    // Only empty/null levels should match
    assertFalse( "Should not match rule with specific level", predicate.test( level1Ref ) );
    assertTrue( "Should match rule with empty level", predicate.test( emptyLevelRef ) );
    assertTrue( "Should match rule with null level", predicate.test( nullLevelRef ) );
  }

  @Test
  public void testBuildLevelRulePredicateWithEmptyLevel() {
    var predicate = PentahoAuthorizationRuleLevel.buildLevelRulePredicate( "", false );

    // Create object references with different rule levels
    var level1Ref = createMockObjectReference( mockLevelRule1, "level1" );
    var emptyLevelRef = createMockObjectReference( mockPostRule1, "" );
    var nullLevelRef = createMockObjectReference( mockPostRule2, Map.of() );

    // Only empty/null levels should match
    assertFalse( "Should not match rule with specific level", predicate.test( level1Ref ) );
    assertTrue( "Should match rule with empty level", predicate.test( emptyLevelRef ) );
    assertTrue( "Should match rule with null level", predicate.test( nullLevelRef ) );
  }

  @Test
  public void testBuildLevelRulePredicateWithDefaultRuleLevel() {
    var predicate = PentahoAuthorizationRuleLevel.buildLevelRulePredicate( "level1", true );

    // Create object references with different rule levels
    var level1Ref = createMockObjectReference( mockLevelRule1, "level1" );
    var level2Ref = createMockObjectReference( mockLevelRule2, "level2" );
    var emptyLevelRef = createMockObjectReference( mockPostRule1, "" );
    var nullLevelRef = createMockObjectReference( mockPostRule2, Map.of() );

    // level1, empty, and null levels should match (since isDefaultRuleLevel=true)
    assertTrue( "Should match rule with matching level", predicate.test( level1Ref ) );
    assertFalse( "Should not match rule with different level", predicate.test( level2Ref ) );
    assertTrue( "Should match rule with empty level when default", predicate.test( emptyLevelRef ) );
    assertTrue( "Should match rule with null level when default", predicate.test( nullLevelRef ) );
  }

  @Test
  public void testBuildLevelRulePredicateWithNullDefaultRuleLevel() {
    var predicate = PentahoAuthorizationRuleLevel.buildLevelRulePredicate( null, true );

    // Create object references with different rule levels
    var level1Ref = createMockObjectReference( mockLevelRule1, "level1" );
    var emptyLevelRef = createMockObjectReference( mockPostRule1, "" );
    var nullLevelRef = createMockObjectReference( mockPostRule2, Map.of() );

    // Only empty/null levels should match
    assertFalse( "Should not match rule with specific level", predicate.test( level1Ref ) );
    assertTrue( "Should match rule with empty level", predicate.test( emptyLevelRef ) );
    assertTrue( "Should match rule with null level", predicate.test( nullLevelRef ) );
  }
  // endregion
}
