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

import edu.umd.cs.findbugs.annotations.NonNull;
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
import org.pentaho.platform.engine.security.authorization.core.resources.GenericAuthorizationResource;
import org.pentaho.platform.engine.security.authorization.core.resources.ResourceAuthorizationRequest;

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

public class AbstractActionRoleBindingAuthorizationRuleTest {

  private IAuthorizationContext mockContext;
  private IAuthorizationOptions mockOptions;
  private IAuthorizationPrincipal mockPrincipal;
  private IAuthorizationRequest mockRequest;

  private IAuthorizationRole roleUser;
  private IAuthorizationRole roleAdmin;
  private IAuthorizationRole roleManager;

  private Map<String, Set<String>> roleActionBindings;

  /**
   * Test implementation of AbstractActionRoleBindingAuthorizationRule for testing purposes.
   */
  private static class TestActionRoleBindingAuthorizationRule extends AbstractActionRoleBindingAuthorizationRule {
    private final Map<String, Set<String>> roleActionBindings;

    public TestActionRoleBindingAuthorizationRule( Map<String, Set<String>> roleActionBindings ) {
      this.roleActionBindings = roleActionBindings;
    }

    @Override
    protected boolean hasRoleActionBinding( @NonNull IAuthorizationRole role, @NonNull String actionName ) {
      return roleActionBindings.getOrDefault( role.getName(), Set.of() ).contains( actionName );
    }
  }

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
    mockOptions = mock( IAuthorizationOptions.class );
    mockPrincipal = mock( IAuthorizationPrincipal.class );

    // Create test roles
    roleUser = new AuthorizationRole( "ROLE_USER" );
    roleAdmin = new AuthorizationRole( "ROLE_ADMIN" );
    roleManager = new AuthorizationRole( "ROLE_MANAGER" );

    // Set up context with options
    when( mockContext.getOptions() ).thenReturn( mockOptions );
    when( mockOptions.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.FULL );

    // Create test request
    var action = createTestAction( "read" );

    mockRequest = mock( AuthorizationRequest.class );
    when( mockRequest.getAction() ).thenReturn( action );
    when( mockRequest.getPrincipal() ).thenReturn( mockPrincipal );

    // Initialize role bindings map
    roleActionBindings = new HashMap<>();
  }

  @Test
  public void testGetRequestType() {
    var rule = new TestActionRoleBindingAuthorizationRule( roleActionBindings );
    assertEquals( IAuthorizationRequest.class, rule.getRequestType() );
  }

  // region authorize Tests
  @Test
  public void testAuthorizeWithResourceRequestAbstains() {
    var rule = new TestActionRoleBindingAuthorizationRule( roleActionBindings );

    // Create a resource authorization request
    var action = createTestAction( "read" );
    var resource = new GenericAuthorizationResource( "file", "report123" );
    var resourceRequest = new ResourceAuthorizationRequest( mockPrincipal, action, resource );

    var result = rule.authorize( resourceRequest, mockContext );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testAuthorizeWithNoRolesReturnsEmptyBoundRoles() {
    var rule = new TestActionRoleBindingAuthorizationRule( roleActionBindings );

    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf() );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    assertTrue( result.get() instanceof ActionRoleBindingAuthorizationDecision );

    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() );
    assertTrue( decision.getBoundRoles().isEmpty() );
  }

  @Test
  public void testAuthorizeWithSingleRoleHavingBindingGrantsAccess() {
    roleActionBindings.put( "ROLE_USER", orderedSetOf( "read", "write" ) );

    var rule = new TestActionRoleBindingAuthorizationRule( roleActionBindings );

    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );

    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertTrue( decision.isGranted() );
    assertEquals( 1, decision.getBoundRoles().size() );
    assertTrue( decision.getBoundRoles().contains( roleUser ) );
  }

  @Test
  public void testAuthorizeWithSingleRoleWithoutBindingDeniesAccess() {
    roleActionBindings.put( "ROLE_USER", orderedSetOf( "write", "delete" ) );
    // "read" not included

    var rule = new TestActionRoleBindingAuthorizationRule( roleActionBindings );

    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() );
    assertTrue( decision.getBoundRoles().isEmpty() );
  }

  @Test
  public void testAuthorizeWithMultipleRolesFullModeReturnsAllBoundRoles() {
    // roleUser has binding, roleAdmin doesn't, roleManager has binding
    roleActionBindings.put( "ROLE_USER", orderedSetOf( "read", "write" ) );
    roleActionBindings.put( "ROLE_ADMIN", orderedSetOf( "delete", "admin" ) );
    roleActionBindings.put( "ROLE_MANAGER", orderedSetOf( "read", "manage" ) );

    var rule = new TestActionRoleBindingAuthorizationRule( roleActionBindings );

    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser, roleAdmin, roleManager ) );
    when( mockOptions.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.FULL );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );

    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertTrue( decision.isGranted() );
    assertEquals( 2, decision.getBoundRoles().size() );
    assertTrue( decision.getBoundRoles().contains( roleUser ) );
    assertTrue( decision.getBoundRoles().contains( roleManager ) );
    assertFalse( decision.getBoundRoles().contains( roleAdmin ) );
  }

  @Test
  public void testAuthorizeWithMultipleRolesSettledModeStopsAtFirstMatch() {
    // roleUser has binding - should stop here in SETTLED mode
    roleActionBindings.put( "ROLE_USER", orderedSetOf( "read", "write" ) );
    roleActionBindings.put( "ROLE_MANAGER", orderedSetOf( "read", "manage" ) );

    var rule = new TestActionRoleBindingAuthorizationRule( roleActionBindings );

    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser, roleAdmin, roleManager ) );
    when( mockOptions.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );

    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertTrue( decision.isGranted() );
    assertEquals( 1, decision.getBoundRoles().size() );
    assertTrue( decision.getBoundRoles().contains( roleUser ) );
  }

  @Test
  public void testAuthorizeWithMultipleRolesSettledModeContinuesUntilMatch() {
    // roleUser no binding, roleAdmin has binding - should stop at roleAdmin
    roleActionBindings.put( "ROLE_USER", orderedSetOf( "write", "delete" ) );
    roleActionBindings.put( "ROLE_ADMIN", orderedSetOf( "read", "admin" ) );
    roleActionBindings.put( "ROLE_MANAGER", orderedSetOf( "read", "manage" ) );

    var rule = new TestActionRoleBindingAuthorizationRule( roleActionBindings );

    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser, roleAdmin, roleManager ) );
    when( mockOptions.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );

    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertTrue( decision.isGranted() );
    assertEquals( 1, decision.getBoundRoles().size() );
    assertTrue( decision.getBoundRoles().contains( roleAdmin ) );
  }

  @Test
  public void testAuthorizeWithAllRolesLackingBindingDeniesAccess() {
    // Neither role has binding for "read"
    roleActionBindings.put( "ROLE_USER", orderedSetOf( "write", "delete" ) );
    roleActionBindings.put( "ROLE_ADMIN", orderedSetOf( "admin", "manage" ) );

    var rule = new TestActionRoleBindingAuthorizationRule( roleActionBindings );

    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser, roleAdmin ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() );
    assertTrue( decision.getBoundRoles().isEmpty() );
  }
  // endregion
}
