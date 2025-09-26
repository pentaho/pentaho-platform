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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationPrincipal;
import org.pentaho.platform.api.engine.security.authorization.resources.IAuthorizationResource;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestAction;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createTestUser;

public class ResourceAuthorizationRequestTest {

  private IAuthorizationPrincipal principal;
  private IAuthorizationAction action;
  private IAuthorizationResource resource;

  @Before
  public void setUp() {
    principal = createTestUser();
    action = createTestAction( "read" );
    resource = new GenericAuthorizationResource( "file", "report123" );
  }

  @Test
  public void testConstructorAndGetters() {
    var request = new ResourceAuthorizationRequest( principal, action, resource );

    assertEquals( principal, request.getPrincipal() );
    assertEquals( action, request.getAction() );
    assertEquals( resource, request.getResource() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullPrincipalThrows() {
    //noinspection DataFlowIssue
    new ResourceAuthorizationRequest( null, action, resource );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullActionThrows() {
    //noinspection DataFlowIssue
    new ResourceAuthorizationRequest( principal, null, resource );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullResourceThrows() {
    //noinspection DataFlowIssue
    new ResourceAuthorizationRequest( principal, action, null );
  }

  @Test
  public void testWithAction() {
    var action2 = createTestAction( "write" );

    var request1 = new ResourceAuthorizationRequest( principal, action, resource );
    var request2 = request1.withAction( action2 );

    assertNotSame( request1, request2 );

    assertEquals( principal, request2.getPrincipal() );
    assertEquals( action2, request2.getAction() );
    assertEquals( resource, request2.getResource() );
  }

  @Test
  public void testWithResource() {
    var resource2 = new GenericAuthorizationResource( "folder", "folder456" );

    var request1 = new ResourceAuthorizationRequest( principal, action, resource );
    var request2 = request1.withResource( resource2 );

    assertNotSame( request1, request2 );

    assertEquals( principal, request2.getPrincipal() );
    assertEquals( action, request2.getAction() );
    assertEquals( resource2, request2.getResource() );
  }

  @Test
  public void testAsGeneral() {
    var request = new ResourceAuthorizationRequest( principal, action, resource );
    var generalRequest = request.asGeneral();

    assertNotSame( request, generalRequest );

    assertEquals( principal, generalRequest.getPrincipal() );
    assertEquals( action, generalRequest.getAction() );
    assertFalse( generalRequest instanceof IResourceAuthorizationRequest );
  }

  @Test
  public void testEqualsAndHashCode() {
    var resource2 = new GenericAuthorizationResource( "file", "report123" );
    var resource3 = new GenericAuthorizationResource( "folder", "folder456" );

    var request1 = new ResourceAuthorizationRequest( principal, action, resource );
    var request2 = new ResourceAuthorizationRequest( principal, action, resource2 );
    var request3 = new ResourceAuthorizationRequest( principal, action, resource3 );

    var notRequest = new Object();
    assertNotEquals( request1, notRequest );

    assertEquals( request1, request2 );
    assertNotEquals( request1, request3 );
    assertEquals( request1.hashCode(), request2.hashCode() );
    assertNotEquals( request1.hashCode(), request3.hashCode() );
  }

  @Test
  public void testToStringFormat() {
    var request = new ResourceAuthorizationRequest( principal, action, resource );
    var result = request.toString();

    assertTrue( result.contains( "ResourceAuthorizationRequest" ) );
    assertTrue( result.contains( "test-user" ) );
    assertTrue( result.contains( "read" ) );
    assertTrue( result.contains( "file" ) );
    assertTrue( result.contains( "report123" ) );
  }
}
