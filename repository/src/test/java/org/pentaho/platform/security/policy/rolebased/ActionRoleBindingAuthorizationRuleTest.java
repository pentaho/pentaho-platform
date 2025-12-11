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
import org.pentaho.platform.engine.security.authorization.core.resources.GenericAuthorizationResource;
import org.pentaho.platform.engine.security.authorization.core.resources.ResourceAuthorizationRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;

public class ActionRoleBindingAuthorizationRuleTest {

  private IRoleAuthorizationPolicyRoleBindingDao mockRoleBindingDao;
  private IAuthorizationContext mockContext;
  private IAuthorizationOptions mockOptions;
  private IAuthorizationPrincipal mockPrincipal;
  private IAuthorizationRequest mockRequest;

  private IAuthorizationRole roleUser;
  private IAuthorizationRole roleAdmin;
  private IAuthorizationRole roleManager;

  private ActionRoleBindingAuthorizationRule rule;

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
    mockRoleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
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

    rule = new ActionRoleBindingAuthorizationRule( mockRoleBindingDao );
  }

  // region Constructor Tests

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRoleBindingDaoThrows() {
    //noinspection DataFlowIssue
    new ActionRoleBindingAuthorizationRule( null );
  }
  // endregion

  @Test
  public void testGetRequestType() {
    assertEquals( IAuthorizationRequest.class, rule.getRequestType() );
  }

  // region authorize Tests
  @Test
  public void testAuthorizeWithResourceRequestAbstains() {
    // Create a resource authorization request
    var action = createTestAction( "read" );
    var resource = new GenericAuthorizationResource( "file", "report123" );
    var resourceRequest = new ResourceAuthorizationRequest( mockPrincipal, action, resource );

    var result = rule.authorize( resourceRequest, mockContext );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testAuthorizeWithNoRolesReturnsEmptyBoundRoles() {
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
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( Arrays.asList( "read", "write" ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );

    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertTrue( decision.isGranted() );
    assertEquals( 1, decision.getBoundRoles().size() );
    assertTrue( decision.getBoundRoles().contains( roleUser ) );

    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_USER" ) );
  }

  @Test
  public void testAuthorizeWithSingleRoleWithoutBindingDeniesAccess() {
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );

    // "read" not included
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( Arrays.asList( "write", "delete" ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() );
    assertTrue( decision.getBoundRoles().isEmpty() );

    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_USER" ) );
  }

  @Test
  public void testAuthorizeWithMultipleRolesFullModeReturnsAllBoundRoles() {
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser, roleAdmin, roleManager ) );
    when( mockOptions.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.FULL );

    // roleUser has binding, roleAdmin doesn't, roleManager has binding
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( Arrays.asList( "read", "write" ) );
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_ADMIN" ) ) )
      .thenReturn( Arrays.asList( "delete", "admin" ) ); // "read" not included
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_MANAGER" ) ) )
      .thenReturn( Arrays.asList( "read", "manage" ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );

    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertTrue( decision.isGranted() );
    assertEquals( 2, decision.getBoundRoles().size() );
    assertTrue( decision.getBoundRoles().contains( roleUser ) );
    assertTrue( decision.getBoundRoles().contains( roleManager ) );
    assertFalse( decision.getBoundRoles().contains( roleAdmin ) );

    // Verify all roles were checked
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_USER" ) );
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_ADMIN" ) );
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_MANAGER" ) );
  }

  @Test
  public void testAuthorizeWithMultipleRolesSettledModeStopsAtFirstMatch() {
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser, roleAdmin, roleManager ) );
    when( mockOptions.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );

    // roleUser has binding - should stop here in SETTLED mode
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( Arrays.asList( "read", "write" ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );

    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertTrue( decision.isGranted() );
    assertEquals( 1, decision.getBoundRoles().size() );
    assertTrue( decision.getBoundRoles().contains( roleUser ) );

    // Verify only first role was checked in SETTLED mode
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_USER" ) );
    verify( mockRoleBindingDao, never() ).getBoundLogicalRoleNames( List.of( "ROLE_ADMIN" ) );
    verify( mockRoleBindingDao, never() ).getBoundLogicalRoleNames( List.of( "ROLE_MANAGER" ) );
  }

  @Test
  public void testAuthorizeWithMultipleRolesSettledModeContinuesUntilMatch() {
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser, roleAdmin, roleManager ) );
    when( mockOptions.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );

    // roleUser no binding, roleAdmin has binding - should stop at roleAdmin
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( Arrays.asList( "write", "delete" ) ); // "read" not included
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_ADMIN" ) ) )
      .thenReturn( Arrays.asList( "read", "admin" ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );

    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertTrue( decision.isGranted() );
    assertEquals( 1, decision.getBoundRoles().size() );
    assertTrue( decision.getBoundRoles().contains( roleAdmin ) );

    // Verify first two roles were checked, but not the third
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_USER" ) );
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_ADMIN" ) );
    verify( mockRoleBindingDao, never() ).getBoundLogicalRoleNames( List.of( "ROLE_MANAGER" ) );
  }

  @Test
  public void testAuthorizeWithAllRolesLackingBindingDeniesAccess() {
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser, roleAdmin ) );

    // Neither role has binding for "read"
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( Arrays.asList( "write", "delete" ) );
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_ADMIN" ) ) )
      .thenReturn( Arrays.asList( "admin", "manage" ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() );
    assertTrue( decision.getBoundRoles().isEmpty() );
  }
  // endregion
}
