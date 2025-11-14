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

package org.pentaho.platform.engine.security.authorization.core.resources;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class GenericAuthorizationResourceTest {

  @Test
  public void testConstructorAndGetters() {
    var resource = new GenericAuthorizationResource( "file", "report123" );
    assertEquals( "file", resource.getType() );
    assertEquals( "report123", resource.getId() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullTypeThrows() {
    //noinspection DataFlowIssue
    new GenericAuthorizationResource( null, "report123" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithEmptyTypeThrows() {
    new GenericAuthorizationResource( "", "report123" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullIdThrows() {
    //noinspection DataFlowIssue
    new GenericAuthorizationResource( "file", null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithEmptyIdThrows() {
    new GenericAuthorizationResource( "file", "" );
  }

  @Test
  public void testEqualsAndHashCode() {
    var resource1 = new GenericAuthorizationResource( "file", "report123" );
    var resource2 = new GenericAuthorizationResource( "file", "report123" );
    var resource3 = new GenericAuthorizationResource( "folder", "report123" );

    var notResource = new Object();
    assertNotEquals( resource1, notResource );

    assertEquals( resource1, resource2 );
    assertNotEquals( resource1, resource3 );
    assertEquals( resource1.hashCode(), resource2.hashCode() );
    assertNotEquals( resource1.hashCode(), resource3.hashCode() );
  }

  @Test
  public void testToStringFormat() {
    var resource = new GenericAuthorizationResource( "file", "report123" );
    assertTrue( resource.toString().contains( "file" ) );
    assertTrue( resource.toString().contains( "report123" ) );
  }
}
