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
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationActionService;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationPrincipal;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;
import org.pentaho.platform.engine.security.authorization.core.resources.GenericAuthorizationResource;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthorizationRequestBuilderTest {
  private IAuthorizationActionService actionServiceMock;
  private IAuthorizationPrincipal currentPrincipal;
  private IAuthorizationAction actionMock;
  private AuthorizationRequestBuilder builder;

  @Before
  public void setUp() {
    actionMock = mock( IAuthorizationAction.class );
    actionServiceMock = mock( IAuthorizationActionService.class );

    currentPrincipal = new AuthorizationUser( "current-user", Set.of(
      new AuthorizationRole( "Administrator" ) ) );

    builder = new AuthorizationRequestBuilder( actionServiceMock, () -> currentPrincipal );
  }

  private void mockActionName( String name ) {
    when( actionServiceMock.getAction( name ) ).thenReturn( Optional.of( actionMock ) );
    when( actionMock.getName() ).thenReturn( name );
  }

  @Test
  public void testResolveUndefinedAction() {
    when( actionServiceMock.getAction( "undefined-action" ) )
      .thenReturn( Optional.empty() );

    var request = builder
      .action( "undefined-action" )
      .build();

    assertNotNull( request );
    assertEquals( "undefined-action", request.getAction().getName() );
  }

  @Test
  public void testResolveActionByName() {
    mockActionName( "read" );

    var request = builder.action( "read" ).build();

    assertNotNull( request );
    assertEquals( "read", request.getAction().getName() );
  }

  @Test
  public void testActionWithExplicitUser() {
    mockActionName( "write" );

    IAuthorizationRequest req = builder
      .action( "write" )
      .user( new AuthorizationUser( "another", Collections.singleton( new AuthorizationRole( "editor" ) ) ) )
      .build();

    assertNotNull( req );
    assertEquals( "another", req.getPrincipal().getName() );
    assertEquals( "write", req.getAction().getName() );
  }

  @Test
  public void testActionWithExplicitRole() {
    mockActionName( "write" );

    IAuthorizationRequest req = builder
      .action( "write" )
      .role( new AuthorizationRole( "editor" ) )
      .build();

    assertNotNull( req );
    assertEquals( "editor", req.getPrincipal().getName() );
    assertEquals( "write", req.getAction().getName() );
  }

  @Test
  public void testActionWithExplicitRoleName() {
    mockActionName( "write" );

    IAuthorizationRequest req = builder
      .action( "write" )
      .role( "editor" )
      .build();

    assertNotNull( req );
    assertEquals( "editor", req.getPrincipal().getName() );
    assertEquals( "write", req.getAction().getName() );
  }

  @Test
  public void testActionWithExplicitRoleNameEmpty() {
    mockActionName( "write" );

    AuthorizationRequestBuilder.WithActionBuilder withActionBuilder = builder
      .action( "write" );

    assertThrows( IllegalArgumentException.class, () -> withActionBuilder.role( "" ) );
  }

  @Test
  public void testActionWithCurrentPrincipal() {
    mockActionName( "delete" );

    IAuthorizationRequest req = builder
      .action( "delete" )
      .build();

    assertNotNull( req );
    assertEquals( "current-user", req.getPrincipal().getName() );
    assertEquals( "delete", req.getAction().getName() );
  }

  @Test
  public void testResourceBuilderWithResourceObject() {
    mockActionName( "update" );

    IResourceAuthorizationRequest req = builder
      .action( "update" )
      .resource( new GenericAuthorizationResource( "type", "id" ) )
      .build();

    assertNotNull( req );
    assertEquals( "update", req.getAction().getName() );
    assertEquals( "current-user", req.getPrincipal().getName() );
    assertEquals( "type", req.getResource().getType() );
    assertEquals( "id", req.getResource().getId() );
  }

  @Test
  public void testResourceBuilderWithTypeAndId() {
    mockActionName( "view" );

    IResourceAuthorizationRequest req = builder
      .action( "view" )
      .resource( "type", "id" )
      .build();

    assertNotNull( req );
    assertEquals( "view", req.getAction().getName() );
    assertEquals( "current-user", req.getPrincipal().getName() );
    assertEquals( "type", req.getResource().getType() );
    assertEquals( "id", req.getResource().getId() );
  }

  @Test
  public void testActionWithUserPrincipal() {
    mockActionName( "admin" );

    IAuthorizationRequest req = builder
      .action( "admin" )
      .principal( new AuthorizationUser( "another", Collections.singleton( new AuthorizationRole( "Administrator" ) ) )  )
      .build();

    assertNotNull( req );
    assertEquals( "another", req.getPrincipal().getName() );
    assertEquals( "admin", req.getAction().getName() );
  }

  @Test
  public void testActionWithRolePrincipal() {
    mockActionName( "admin" );

    IAuthorizationRequest req = builder
      .action( "admin" )
      .principal( new AuthorizationRole( "Administrator" ) )
      .build();

    assertNotNull( req );
    assertEquals( "Administrator", req.getPrincipal().getName() );
    assertEquals( "admin", req.getAction().getName() );
  }
}
