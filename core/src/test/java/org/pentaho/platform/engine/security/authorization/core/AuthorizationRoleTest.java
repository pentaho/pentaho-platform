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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class AuthorizationRoleTest {

  @Test
  public void testConstructorAndGetters() {
    var role = new AuthorizationRole( "Administrator" );
    assertEquals( "Administrator", role.getName() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullNameThrows() {
    //noinspection DataFlowIssue
    new AuthorizationRole( null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithEmptyNameThrows() {
    new AuthorizationRole( "" );
  }

  @Test
  public void testEqualsAndHashCode() {
    var role1 = new AuthorizationRole( "Administrator" );
    var role2 = new AuthorizationRole( "Administrator" );
    var role3 = new AuthorizationRole( "Power User" );

    var notRole = new Object();
    assertNotEquals( role1, notRole );

    assertEquals( role1, role2 );
    assertNotEquals( role1, role3 );
    assertEquals( role1.hashCode(), role2.hashCode() );
    assertNotEquals( role1.hashCode(), role3.hashCode() );
  }

  @Test
  public void testToStringFormat() {
    AuthorizationRole role = new AuthorizationRole( "Administrator" );
    assertTrue( role.toString().contains( "Administrator" ) );
  }
}
