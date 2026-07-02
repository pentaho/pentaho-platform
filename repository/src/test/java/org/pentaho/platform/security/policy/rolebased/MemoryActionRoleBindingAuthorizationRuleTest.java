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

package org.pentaho.platform.security.policy.rolebased;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationPrincipal;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRole;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;

public class MemoryActionRoleBindingAuthorizationRuleTest {

  private IAuthorizationContext mockContext;
  private IAuthorizationRequest mockRequest;

  private IAuthorizationRole roleUser;
  private IAuthorizationRole roleAdmin;

  private Map<String, Set<String>> roleBindings;

  /**
   * Creates an ordered set from the given elements, preserving insertion order.
   * This prevents random test failures caused by Set.of() not preserving order.
   */
  @SafeVarargs
  private static <T> Set<T> orderedSetOf( T... elements ) {
    LinkedHashSet<T> set = new LinkedHashSet<>();
    Collections.addAll( set, elements );
    return set;
  }

  @Before
  public void setUp() {
    mockContext = mock( IAuthorizationContext.class );
    var mockOptions = mock( IAuthorizationOptions.class );
    var mockPrincipal = mock( IAuthorizationPrincipal.class );

    // Create test roles
    roleUser = new AuthorizationRole( "ROLE_USER" );
    roleAdmin = new AuthorizationRole( "ROLE_ADMIN" );

    // Set up context with options
    when( mockContext.getOptions() ).thenReturn( mockOptions );
    when( mockOptions.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.FULL );

    // Create test request
    var action = createTestAction( "read" );

    mockRequest = mock( AuthorizationRequest.class );
    when( mockRequest.getAction() ).thenReturn( action );
    when( mockRequest.getPrincipal() ).thenReturn( mockPrincipal );

    // Initialize role bindings map
    roleBindings = new HashMap<>();
  }

  // region Constructor Tests

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRoleBindingsThrows() {
    //noinspection DataFlowIssue
    new MemoryActionRoleBindingAuthorizationRule( null );
  }
  // endregion

  // region Memory-Based Binding Tests

  @Test
  public void testBindingsFromMapAreUsedForRoleLookup() {
    roleBindings.put( "ROLE_USER", orderedSetOf( "read", "write" ) );
    roleBindings.put( "ROLE_ADMIN", orderedSetOf( "delete", "admin" ) );

    var rule = new MemoryActionRoleBindingAuthorizationRule( roleBindings );

    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertTrue( decision.isGranted() );
    assertTrue( decision.getBoundRoles().contains( roleUser ) );
  }

  @Test
  public void testEmptyBindingsMapResultsInNoAccess() {
    // Empty map
    var rule = new MemoryActionRoleBindingAuthorizationRule( roleBindings );

    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser, roleAdmin ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() );
    assertTrue( decision.getBoundRoles().isEmpty() );
  }

  @Test
  public void testRoleNotInMapResultsInNoAccess() {
    // Only ROLE_ADMIN in map
    roleBindings.put( "ROLE_ADMIN", orderedSetOf( "read", "admin" ) );

    var rule = new MemoryActionRoleBindingAuthorizationRule( roleBindings );

    // Request with ROLE_USER which is not in the map
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() );
    assertTrue( decision.getBoundRoles().isEmpty() );
  }

  @Test
  public void testRoleWithEmptyActionsSetResultsInNoAccess() {
    // Role exists in map but has no actions
    roleBindings.put( "ROLE_USER", orderedSetOf() );

    var rule = new MemoryActionRoleBindingAuthorizationRule( roleBindings );

    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() );
    assertTrue( decision.getBoundRoles().isEmpty() );
  }

  @Test
  public void testMapWithMultipleActionsForRoleIsCheckedCorrectly() {
    roleBindings.put( "ROLE_USER", orderedSetOf( "write", "delete", "admin" ) );
    // "read" is not in the set

    var rule = new MemoryActionRoleBindingAuthorizationRule( roleBindings );

    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() ); // "read" not found
    assertTrue( decision.getBoundRoles().isEmpty() );
  }

  @Test
  public void testMapWithMatchingActionGrantsAccess() {
    roleBindings.put( "ROLE_USER", orderedSetOf( "read", "write", "delete" ) );
    // "read" is in the set

    var rule = new MemoryActionRoleBindingAuthorizationRule( roleBindings );

    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertTrue( decision.isGranted() ); // "read" found
    assertEquals( 1, decision.getBoundRoles().size() );
    assertTrue( decision.getBoundRoles().contains( roleUser ) );
  }
  // endregion
}
