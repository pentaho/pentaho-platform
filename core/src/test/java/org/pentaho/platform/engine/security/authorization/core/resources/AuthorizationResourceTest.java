package org.pentaho.platform.engine.security.authorization.core.resources;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class AuthorizationResourceTest {

  @Test
  public void testConstructorAndGetters() {
    var resource = new AuthorizationResource( "file", "report123" );
    assertEquals( "file", resource.getType() );
    assertEquals( "report123", resource.getId() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullTypeThrows() {
    //noinspection DataFlowIssue
    new AuthorizationResource( null, "report123" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithEmptyTypeThrows() {
    new AuthorizationResource( "", "report123" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullIdThrows() {
    //noinspection DataFlowIssue
    new AuthorizationResource( "file", null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithEmptyIdThrows() {
    new AuthorizationResource( "file", "" );
  }

  @Test
  public void testEqualsAndHashCode() {
    var resource1 = new AuthorizationResource( "file", "report123" );
    var resource2 = new AuthorizationResource( "file", "report123" );
    var resource3 = new AuthorizationResource( "folder", "report123" );

    assertEquals( resource1, resource2 );
    assertNotEquals( resource1, resource3 );
    assertEquals( resource1.hashCode(), resource2.hashCode() );
    assertNotEquals( resource1.hashCode(), resource3.hashCode() );
  }

  @Test
  public void testToStringFormat() {
    var resource = new AuthorizationResource( "file", "report123" );
    assertTrue( resource.toString().contains( "file" ) );
    assertTrue( resource.toString().contains( "report123" ) );
  }
}
