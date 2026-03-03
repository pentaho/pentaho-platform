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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class ActionRoleBindingAuthorizationDecisionTest {

  private IAuthorizationRequest request;
  private IAuthorizationRole roleUser;
  private IAuthorizationRole roleAdmin;
  private IAuthorizationRole roleManager;

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
    var user = createTestUser();
    var action = createTestAction( "read" );
    request = new AuthorizationRequest( user, action );

    // Create test roles
    roleUser = new AuthorizationRole( "ROLE_USER" );
    roleAdmin = new AuthorizationRole( "ROLE_ADMIN" );
    roleManager = new AuthorizationRole( "ROLE_MANAGER" );
  }

  // region Constructor Tests

  @Test
  public void testConstructorWithEmptyRolesCreatesDeniedDecision() {
    Set<IAuthorizationRole> emptyRoles = orderedSetOf();
    var decision = new ActionRoleBindingAuthorizationDecision( request, emptyRoles );

    assertEquals( request, decision.getRequest() );
    assertFalse( decision.isGranted() );
    assertTrue( decision.getBoundRoles().isEmpty() );
  }

  @Test
  public void testConstructorWithSingleRoleCreatesGrantedDecision() {
    var singleRole = orderedSetOf( roleUser );
    var decision = new ActionRoleBindingAuthorizationDecision( request, singleRole );

    assertEquals( request, decision.getRequest() );
    assertTrue( decision.isGranted() );
    assertEquals( 1, decision.getBoundRoles().size() );
    assertTrue( decision.getBoundRoles().contains( roleUser ) );
  }

  @Test
  public void testConstructorWithMultipleRolesCreatesGrantedDecision() {
    var multipleRoles = orderedSetOf( roleUser, roleAdmin, roleManager );
    var decision = new ActionRoleBindingAuthorizationDecision( request, multipleRoles );

    assertEquals( request, decision.getRequest() );
    assertTrue( decision.isGranted() );
    assertEquals( 3, decision.getBoundRoles().size() );
    assertTrue( decision.getBoundRoles().contains( roleUser ) );
    assertTrue( decision.getBoundRoles().contains( roleAdmin ) );
    assertTrue( decision.getBoundRoles().contains( roleManager ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRequestThrows() {
    var roles = orderedSetOf( roleUser );
    //noinspection DataFlowIssue
    new ActionRoleBindingAuthorizationDecision( null, roles );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullRolesThrows() {
    //noinspection DataFlowIssue
    new ActionRoleBindingAuthorizationDecision( request, null );
  }

  // endregion

  // region getBoundRoles Tests

  @Test
  public void testGetBoundRolesReturnsUnmodifiableSet() {
    var roles = orderedSetOf( roleUser, roleAdmin );
    var decision = new ActionRoleBindingAuthorizationDecision( request, roles );

    var boundRoles = decision.getBoundRoles();
    assertNotNull( boundRoles );
    assertEquals( 2, boundRoles.size() );

    // Verify the returned set is unmodifiable
    try {
      //noinspection DataFlowIssue
      boundRoles.add( roleManager );
      fail( "Should have thrown UnsupportedOperationException" );
    } catch ( UnsupportedOperationException e ) {
      // Expected behavior
    }
  }

  @Test
  public void testGetBoundRolesPreservesOrder() {
    var roles = orderedSetOf( roleManager, roleUser, roleAdmin );
    var decision = new ActionRoleBindingAuthorizationDecision( request, roles );

    var boundRoles = decision.getBoundRoles();
    var rolesList = new ArrayList<>( boundRoles );

    assertEquals( roleManager, rolesList.get( 0 ) );
    assertEquals( roleUser, rolesList.get( 1 ) );
    assertEquals( roleAdmin, rolesList.get( 2 ) );
  }
  // endregion

  // region getShortJustification Tests
  @Test
  public void testGetShortJustificationGrantedContainsActionDisplayName() {
    var decision = new ActionRoleBindingAuthorizationDecision( request, orderedSetOf( roleUser ) );

    // Template: "Has **{1}** permission" — {1} is the action's localizedDisplayName ("read-display-name")
    var justification = decision.getShortJustification();

    assertTrue( decision.isGranted() );
    assertTrue( justification.contains( "read-display-name" ) );
    assertTrue( justification.contains( "Has" ) );
    assertTrue( justification.contains( "permission" ) );
  }
  // endregion

  // region toString Tests
  @Test
  public void testToStringWithEmptyRoles() {
    Set<IAuthorizationRole> emptyRoles = orderedSetOf();
    var decision = new ActionRoleBindingAuthorizationDecision( request, emptyRoles );

    var result = decision.toString();
    assertNotNull( result );
    assertTrue( result.contains( "ActionRoleBindingAuthorizationDecision" ) );
    assertTrue( result.contains( "Denied" ) );
    assertTrue( result.contains( "roles:" ) );
  }

  @Test
  public void testToStringWithMultipleRoles() {
    var multipleRoles = orderedSetOf( roleUser, roleAdmin, roleManager );
    var decision = new ActionRoleBindingAuthorizationDecision( request, multipleRoles );

    var result = decision.toString();
    assertNotNull( result );
    assertTrue( result.contains( "ActionRoleBindingAuthorizationDecision" ) );
    assertTrue( result.contains( "Granted" ) );
    assertTrue( result.contains( "roles:" ) );
    assertTrue( result.contains( "ROLE_USER" ) );
    assertTrue( result.contains( "ROLE_ADMIN" ) );
    assertTrue( result.contains( "ROLE_MANAGER" ) );
  }
  // endregion
}
