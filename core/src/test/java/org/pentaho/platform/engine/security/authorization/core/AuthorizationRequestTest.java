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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class AuthorizationRequestTest {

  private AuthorizationUser user;

  @Before
  public void setUp() {
    user = createTestUser();
  }

  @Test
  public void testConstructorAndGetters() {
    var action = createTestAction( "read" );

    var request = new AuthorizationRequest( user, action );

    assertEquals( user, request.getUser() );
    assertEquals( action, request.getAction() );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullUserThrows() {
    var action = createTestAction( "read" );
    //noinspection DataFlowIssue
    new AuthorizationRequest( null, action );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullActionThrows() {
    //noinspection DataFlowIssue
    new AuthorizationRequest( user, null );
  }

  @Test
  public void testWithAction() {
    var action1 = createTestAction( "read" );
    var action2 = createTestAction( "write" );

    var request1 = new AuthorizationRequest( user, action1 );
    var request2 = request1.withAction( action2 );

    assertEquals( user, request2.getUser() );
    assertEquals( action2, request2.getAction() );
    assertNotSame( request1, request2 );
  }

  @Test
  public void testEqualsAndHashCode() {
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

    var notRequest = new Object();
    assertNotEquals( request1, notRequest );
  }

  @Test
  public void testToStringFormat() {
    var action = createTestAction( "read" );

    var request = new AuthorizationRequest( user, action );
    var result = request.toString();

    assertTrue( result.contains( "AuthorizationRequest" ) );
    assertTrue( result.contains( "test-user" ) );
    assertTrue( result.contains( "read" ) );
  }
}
