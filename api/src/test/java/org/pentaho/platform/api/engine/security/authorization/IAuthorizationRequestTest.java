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

package org.pentaho.platform.api.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pentaho.platform.api.engine.IAuthorizationAction;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class IAuthorizationRequestTest {
  private IAuthorizationUser mockUser;
  private IAuthorizationRole mockRole;
  private IAuthorizationAction mockAction;

  @BeforeEach
  public void setUp() {
    mockUser = mock( IAuthorizationUser.class );
    mockRole = mock( IAuthorizationRole.class );
    mockAction = mock( IAuthorizationAction.class );
  }

  @Test
  void testGetPrincipalAsUser() {
    IAuthorizationRequest request = spy( new TestAuthorizationRequest( mockUser, mockAction ) );

    Optional<IAuthorizationUser> user = request.getPrincipalAsUser();

    assertTrue( user.isPresent() );
    assertSame( mockUser, user.get() );
  }

  @Test
  void testGetPrincipalAsUserWhenRole() {
    IAuthorizationRequest request = spy( new TestAuthorizationRequest( mockRole, mockAction ) );

    assertSame( Optional.empty(), request.getPrincipalAsUser() );
  }

  @Test
  void testGetPrincipalAsRole() {
    IAuthorizationRequest request = spy( new TestAuthorizationRequest( mockRole, mockAction ) );

    Optional<IAuthorizationRole> role = request.getPrincipalAsRole();

    assertTrue( role.isPresent() );
    assertSame( mockRole, role.get() );
  }

  @Test
  void testGetPrincipalAsRoleWhenUser() {
    IAuthorizationRequest request = spy( new TestAuthorizationRequest( mockUser, mockAction ) );

    assertSame( Optional.empty(), request.getPrincipalAsRole() );
  }

  @Test
  void testGetAllRolesWhenUser() {
    IAuthorizationRole role1 = mock( IAuthorizationRole.class );
    IAuthorizationRole role2 = mock( IAuthorizationRole.class );
    Set<IAuthorizationRole> userRoles = Set.of( role1, role2 );
    when( mockUser.getRoles() ).thenReturn( userRoles );
    IAuthorizationRequest request = spy( new TestAuthorizationRequest( mockUser, mockAction ) );

    assertEquals( userRoles, request.getAllRoles() );
  }

  @Test
  void testGetAllRolesWhenRole() {
    IAuthorizationRequest request = spy( new TestAuthorizationRequest( mockRole, mockAction ) );

    assertEquals( Set.of( mockRole ), request.getAllRoles() );
  }

  @Test
  void testGetAllRolesWhenOtherPrincipal() {
    IAuthorizationPrincipal otherPrincipal = mock( IAuthorizationPrincipal.class );
    IAuthorizationRequest request = spy( new TestAuthorizationRequest( otherPrincipal, mockAction ) );

    assertTrue( request.getAllRoles().isEmpty() );
  }

  private static class TestAuthorizationRequest implements IAuthorizationRequest {
    private final IAuthorizationPrincipal principal;
    private final IAuthorizationAction action;

    public TestAuthorizationRequest( IAuthorizationPrincipal principal, IAuthorizationAction action ) {
      this.principal = principal;
      this.action = action;
    }

    @Override
    @NonNull
    public IAuthorizationPrincipal getPrincipal() {
      return principal;
    }

    @Override
    @NonNull
    public IAuthorizationAction getAction() {
      return action;
    }

    @Override
    @NonNull
    public IAuthorizationRequest withAction( @NonNull IAuthorizationAction action ) {
      throw new UnsupportedOperationException();
    }
  }
}
