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
    var mockPrincipal = mock( IAuthorizationPrincipal.class );

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

  // region DAO Integration Tests

  @Test
  public void testDaoIsCalledForEachRoleInFullMode() {
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser, roleAdmin, roleManager ) );
    when( mockOptions.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.FULL );

    // Set up DAO responses
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( Arrays.asList( "read", "write" ) );
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_ADMIN" ) ) )
      .thenReturn( Arrays.asList( "delete", "admin" ) );
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_MANAGER" ) ) )
      .thenReturn( Arrays.asList( "read", "manage" ) );

    rule.authorize( mockRequest, mockContext );

    // Verify DAO was called for all roles in FULL mode
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_USER" ) );
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_ADMIN" ) );
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_MANAGER" ) );
  }

  @Test
  public void testDaoIsNotCalledForRemainingRolesInSettledModeAfterMatch() {
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser, roleAdmin, roleManager ) );
    when( mockOptions.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );

    // First role has binding - should stop here in SETTLED mode
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( Arrays.asList( "read", "write" ) );

    rule.authorize( mockRequest, mockContext );

    // Verify only first role was checked in SETTLED mode
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_USER" ) );
    verify( mockRoleBindingDao, never() ).getBoundLogicalRoleNames( List.of( "ROLE_ADMIN" ) );
    verify( mockRoleBindingDao, never() ).getBoundLogicalRoleNames( List.of( "ROLE_MANAGER" ) );
  }

  @Test
  public void testDaoIsContinuouslyCalledInSettledModeUntilMatch() {
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser, roleAdmin, roleManager ) );
    when( mockOptions.getDecisionReportingMode() ).thenReturn( AuthorizationDecisionReportingMode.SETTLED );

    // First role has no binding, second role has binding - should stop at second
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( Arrays.asList( "write", "delete" ) );
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_ADMIN" ) ) )
      .thenReturn( Arrays.asList( "read", "admin" ) );

    rule.authorize( mockRequest, mockContext );

    // Verify first two roles were checked, but not the third
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_USER" ) );
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_ADMIN" ) );
    verify( mockRoleBindingDao, never() ).getBoundLogicalRoleNames( List.of( "ROLE_MANAGER" ) );
  }

  @Test
  public void testDaoIsCalledWithCorrectRoleName() {
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( List.of( "read" ) );

    rule.authorize( mockRequest, mockContext );

    // Verify the DAO was called with the exact role name
    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_USER" ) );
  }

  @Test
  public void testDaoEmptyResultIsHandledCorrectly() {
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( Collections.emptyList() );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() );
    assertTrue( decision.getBoundRoles().isEmpty() );

    verify( mockRoleBindingDao, times( 1 ) ).getBoundLogicalRoleNames( List.of( "ROLE_USER" ) );
  }

  @Test
  public void testDaoReturnedActionsAreCheckedForMatch() {
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );

    // DAO returns actions but not the requested one
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( Arrays.asList( "write", "delete", "admin" ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertFalse( decision.isGranted() );
    assertTrue( decision.getBoundRoles().isEmpty() );
  }

  @Test
  public void testDaoReturnedActionsIncludingMatchGrantsAccess() {
    when( mockRequest.getAllRoles() ).thenReturn( orderedSetOf( roleUser ) );

    // DAO returns actions including the requested one
    when( mockRoleBindingDao.getBoundLogicalRoleNames( List.of( "ROLE_USER" ) ) )
      .thenReturn( Arrays.asList( "read", "write", "delete" ) );

    var result = rule.authorize( mockRequest, mockContext );

    assertTrue( result.isPresent() );
    var decision = (ActionRoleBindingAuthorizationDecision) result.get();
    assertTrue( decision.isGranted() );
    assertEquals( 1, decision.getBoundRoles().size() );
    assertTrue( decision.getBoundRoles().contains( roleUser ) );
  }
  // endregion
}
