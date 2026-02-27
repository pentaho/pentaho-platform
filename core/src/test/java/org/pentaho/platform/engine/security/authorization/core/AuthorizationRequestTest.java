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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class AuthorizationRequestTest {

  private AuthorizationUser user;
  private AuthorizationRole role;

  @Before
  public void setUp() {
    user = createTestUser();
    role = new AuthorizationRole( "test-role" );
  }

  @Test
  public void testConstructorAndGettersWithUser() {
    var action = createTestAction( "read" );

    var request = new AuthorizationRequest( user, action );

    assertEquals( user, request.getPrincipal() );
    assertEquals( action, request.getAction() );
  }

  @Test
  public void testConstructorAndGettersWithRole() {
    var action = createTestAction( "read" );

    var request = new AuthorizationRequest( role, action );

    assertEquals( role, request.getPrincipal() );
    assertEquals( action, request.getAction() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullPrincipalThrows() {
    var action = createTestAction( "read" );
    //noinspection DataFlowIssue
    new AuthorizationRequest( null, action );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullActionThrows() {
    //noinspection DataFlowIssue
    new AuthorizationRequest( user, null );
  }

  @Test
  public void testWithActionPreservesPrincipal() {
    var action1 = createTestAction( "read" );
    var action2 = createTestAction( "write" );

    var request1 = new AuthorizationRequest( user, action1 );
    var request2 = request1.withAction( action2 );

    assertEquals( user, request2.getPrincipal() );
    assertEquals( action2, request2.getAction() );
    assertNotSame( request1, request2 );
  }

  @Test
  public void testWithActionWithRolePrincipal() {
    var action1 = createTestAction( "read" );
    var action2 = createTestAction( "write" );

    var request1 = new AuthorizationRequest( role, action1 );
    var request2 = request1.withAction( action2 );

    assertEquals( role, request2.getPrincipal() );
    assertEquals( action2, request2.getAction() );
    assertNotSame( request1, request2 );
  }

  @Test
  public void testEqualsAndHashCodeWithSamePrincipalTypes() {
    var action1 = createTestAction( "read" );
    var action2 = createTestAction( "read" );
    var action3 = createTestAction( "write" );

    var request1 = new AuthorizationRequest( user, action1 );
    var request2 = new AuthorizationRequest( user, action2 );
    var request3 = new AuthorizationRequest( user, action3 );

    assertEquals( request1, request2 );
    assertEquals( request1.hashCode(), request2.hashCode() );

    // Different action.
    assertNotEquals( request1, request3 );
    assertNotEquals( request1.hashCode(), request3.hashCode() );
  }

  @Test
  public void testEqualsAndHashCodeWithDifferentPrincipalTypes() {
    var action = createTestAction( "read" );

    var userRequest = new AuthorizationRequest( user, action );
    var roleRequest = new AuthorizationRequest( role, action );

    // Different principal types should not be equal
    assertNotEquals( userRequest, roleRequest );
    assertNotEquals( userRequest.hashCode(), roleRequest.hashCode() );
  }

  @Test
  public void testEqualsAndHashCodeWithDifferentPrincipalsOfSameType() {
    var action = createTestAction( "read" );
    var otherUser = createTestUser( "other-user", "Administrator" );

    var request1 = new AuthorizationRequest( user, action );
    var request2 = new AuthorizationRequest( otherUser, action );

    // Different principals of same type should not be equal
    assertNotEquals( request1, request2 );
    assertNotEquals( request1.hashCode(), request2.hashCode() );
  }

  @Test
  public void testEqualsWithNonAuthorizationRequest() {
    var action = createTestAction( "read" );
    var request = new AuthorizationRequest( user, action );
    var notRequest = new Object();

    assertNotEquals( request, notRequest );
  }

  @Test
  public void testEqualsWithNull() {
    var request = new AuthorizationRequest( user, createTestAction( "read" ) );
    //noinspection SimplifiableJUnitAssertion,ConstantConditions
    assertFalse( request.equals( null ) );
  }

  @Test
  public void testToStringFormatWithUser() {
    var action = createTestAction( "read" );

    var request = new AuthorizationRequest( user, action );
    var result = request.toString();

    assertTrue( result.contains( "AuthorizationRequest" ) );
    assertTrue( result.contains( "test-user" ) );
    assertTrue( result.contains( "read" ) );
  }

  @Test
  public void testToStringFormatWithRole() {
    var action = createTestAction( "read" );

    var request = new AuthorizationRequest( role, action );
    var result = request.toString();

    assertTrue( result.contains( "AuthorizationRequest" ) );
    assertTrue( result.contains( "test-role" ) );
    assertTrue( result.contains( "read" ) );
  }
}
