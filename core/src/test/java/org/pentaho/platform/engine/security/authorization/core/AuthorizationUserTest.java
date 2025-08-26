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

import org.junit.Test;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRole;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AuthorizationUserTest {
  @Test
  public void testConstructorAndGetters() {
    var role = new AuthorizationRole( "Administrator" );
    var user = new AuthorizationUser( "test-user", Set.of( role ) );

    assertEquals( "test-user", user.getName() );
    assertEquals( 1, user.getRoles().size() );
    assertTrue( user.getRoles().contains( role ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullNameThrows() {
    //noinspection DataFlowIssue
    new AuthorizationUser(
      null,
      Set.of( new AuthorizationRole( "Administrator" ) ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithEmptyNameThrows() {
    new AuthorizationUser(
      "",
      Set.of( new AuthorizationRole( "Administrator" ) ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullRolesThrows() {
    //noinspection DataFlowIssue
    new AuthorizationUser( "test-user", null );
  }

  @Test
  public void testRolesAreUnmodifiable() {
    var role = new AuthorizationRole( "admin" );
    var roles = new HashSet<IAuthorizationRole>();
    roles.add( role );

    var user = new AuthorizationUser( "test-user", roles );
    var returnedRoles = user.getRoles();
    try {
      returnedRoles.add( new AuthorizationRole( "user" ) );
      fail( "Roles set should be unmodifiable" );
    } catch ( UnsupportedOperationException e ) {
      // expected
    }
  }
}
