/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.web.http.api.resources.services.UserRoleDaoService;
import org.pentaho.test.mock.MockPentahoUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

public class UserRoleDaoResource_RolesUpdatedTest {
  private UserRoleDaoResource resource;
  private IPentahoSession session;
  private IUserRoleDao userRoleDao;

  private static final String SESSION_USER_NAME = "admin";
  private static final String NON_SESSION_USER_NAME = "pat";
  private static final String ROLE_NAME_DEVELOPER = "Developer";
  private static final String ROLE_NAME_ADMINISTRATOR = "Administrator";
  private static final String ROLE_NAME_POWER_USER = "Power User";
  private static final String ROLE_NAME_REPORT_AUTHOR = "Report Author";
  private static final String DEFAULT_STRING = "<def>";

  private static final List<String> allRoles =
    Arrays.asList( ROLE_NAME_DEVELOPER, ROLE_NAME_POWER_USER, ROLE_NAME_REPORT_AUTHOR, ROLE_NAME_ADMINISTRATOR );

  @Before
  public void setUp() {
    UserRoleDaoService service = mock( UserRoleDaoService.class );
    doReturn( new RoleListWrapper( allRoles ) ).when( service ).getRolesForUser( eq( SESSION_USER_NAME ) );
    resource =
      new UserRoleDaoResource( mock( IRoleAuthorizationPolicyRoleBindingDao.class ), mock( ITenantManager.class ),
        new ArrayList<String>(), ROLE_NAME_ADMINISTRATOR, service );

    session = new StandaloneSession( SESSION_USER_NAME );
    resource = spy( resource );
    doReturn( session ).when( resource ).getSession();
    userRoleDao = mock( IUserRoleDao.class );
    doReturn( new ArrayList<IPentahoRole>() ).when( userRoleDao ).getRoles( any( ITenant.class ) );
    doReturn( mock( ITenant.class ) ).when( resource ).getTenant( nullable( String.class ) );
    doReturn( userRoleDao ).when( resource ).getUserRoleDao();
    doReturn( true ).when( resource ).canAdminister();
  }

  @Test
  public void sessionAttributeIsSetCorrectly_WhenRolesAreUpdated() {
    resource.updateRolesForCurrentSession();

    List<GrantedAuthority> authorities = new ArrayList<>();
    allRoles.forEach( role -> authorities.add( new SimpleGrantedAuthority( role ) ) );

    Collection<? extends GrantedAuthority> seessionAuthoritys = (Collection) session.getAttribute( IPentahoSession.SESSION_ROLES );
    assertEquals( authorities, seessionAuthoritys );
  }

  @Test
  public void rolesUpdated_WhenAssigningRoles_ToSessionUser() {
    resource.assignRolesToUser( SESSION_USER_NAME, ROLE_NAME_DEVELOPER );

    verify( resource ).updateRolesForCurrentSession();
  }

  @Test
  public void rolesNotUpdated_WhenAssigningRoles_ToNonSessionUser() {
    resource.assignRolesToUser( NON_SESSION_USER_NAME, ROLE_NAME_DEVELOPER );

    verify( resource, never() ).updateRolesForCurrentSession();
  }

  @Test
  public void rolesUpdated_WhenAssigningAllRoles_ToSessionUser() {
    resource.assignAllRolesToUser( DEFAULT_STRING, SESSION_USER_NAME );

    verify( resource ).updateRolesForCurrentSession();
  }

  @Test
  public void rolesNotUpdated_WhenAssigningAllRoles_ToNonSessionUser() {
    resource.assignAllRolesToUser( DEFAULT_STRING, NON_SESSION_USER_NAME );

    verify( resource, never() ).updateRolesForCurrentSession();
  }

  @Test
  public void rolesUpdated_WhenRemoveRoles_FromSessionUser() {
    resource.removeRolesFromUser( SESSION_USER_NAME, ROLE_NAME_DEVELOPER );

    verify( resource ).updateRolesForCurrentSession();
  }

  @Test
  public void rolesNotUpdated_WhenRemoveRoles_FromNonSessionUser() {
    resource.removeRolesFromUser( NON_SESSION_USER_NAME, ROLE_NAME_DEVELOPER );

    verify( resource, never() ).updateRolesForCurrentSession();
  }


  @Test
  public void rolesUpdated_WhenRemoveAllRoles_FromSessionUser() {
    resource.removeAllRolesFromUser( DEFAULT_STRING, SESSION_USER_NAME );

    verify( resource ).updateRolesForCurrentSession();
  }

  @Test
  public void rolesNotUpdated_WhenRemoveAllRoles_FromNonSessionUser() {
    resource.removeAllRolesFromUser( DEFAULT_STRING, NON_SESSION_USER_NAME );

    verify( resource, never() ).updateRolesForCurrentSession();
  }

  @Test
  public void rolesUpdated_WhenAssignToRole_SessionUser() {
    resource.assignUserToRole( DEFAULT_STRING, SESSION_USER_NAME, ROLE_NAME_POWER_USER );

    verify( resource ).updateRolesForCurrentSession();
  }

  @Test
  public void rolesNotUpdated_WhenAssignToRole_NonSessionUser() {
    resource.assignUserToRole( DEFAULT_STRING, NON_SESSION_USER_NAME, ROLE_NAME_POWER_USER );

    verify( resource, never() ).updateRolesForCurrentSession();
  }

  @Test
  public void rolesUpdated_WhenAssignToRole_NonSessionAndSessionUser() {
    resource.assignUserToRole( DEFAULT_STRING, NON_SESSION_USER_NAME + "\t" + SESSION_USER_NAME, ROLE_NAME_POWER_USER );

    verify( resource ).updateRolesForCurrentSession();
  }

  @Test
  public void rolesUpdated_WhenAssignAllUsersToRole_WithSessionUser() {
    ITenant tenant = mock( ITenant.class );
    doReturn( tenant ).when( resource ).getTenant( DEFAULT_STRING );
    IPentahoUser sessionUser = new MockPentahoUser( tenant, SESSION_USER_NAME, DEFAULT_STRING, DEFAULT_STRING, true );
    doReturn( Collections.singletonList( sessionUser ) ).when( userRoleDao ).getUsers( tenant );

    resource.assignAllUsersToRole( DEFAULT_STRING, ROLE_NAME_DEVELOPER );

    verify( resource ).updateRolesForCurrentSession();
  }

  @Test
  public void rolesNotUpdated_WhenAssignAllUsersToRole_WithNoSessionUser() {
    ITenant tenant = mock( ITenant.class );
    doReturn( tenant ).when( resource ).getTenant( DEFAULT_STRING );
    IPentahoUser nonSessionUser =
      new MockPentahoUser( tenant, NON_SESSION_USER_NAME, DEFAULT_STRING, DEFAULT_STRING, true );
    doReturn( Collections.singletonList( nonSessionUser ) ).when( userRoleDao ).getUsers( tenant );

    resource.assignAllUsersToRole( DEFAULT_STRING, ROLE_NAME_DEVELOPER );

    verify( resource, never() ).updateRolesForCurrentSession();
  }

  @Test
  public void rolesUpdated_WhenAssignAllUsersToRole_WithSessionAndNonSessionUser() {
    ITenant tenant = mock( ITenant.class );
    doReturn( tenant ).when( resource ).getTenant( DEFAULT_STRING );
    final IPentahoUser sessionUser =
      new MockPentahoUser( tenant, SESSION_USER_NAME, DEFAULT_STRING, DEFAULT_STRING, true );
    final IPentahoUser nonSessionUser =
      new MockPentahoUser( tenant, NON_SESSION_USER_NAME, DEFAULT_STRING, DEFAULT_STRING, true );

    List<IPentahoUser> users = Arrays.asList( sessionUser, nonSessionUser );

    doReturn( users ).when( userRoleDao ).getUsers( tenant );

    resource.assignAllUsersToRole( DEFAULT_STRING, ROLE_NAME_DEVELOPER );

    verify( resource ).updateRolesForCurrentSession();
  }


  @Test
  public void rolesUpdated_WhenRemoveAllUsersFromRole() {
    resource.removeAllUsersFromRole( DEFAULT_STRING, ROLE_NAME_DEVELOPER );
    verify( resource ).updateRolesForCurrentSession();
  }


  @Test
  public void rolesUpdated_WhenAnyRoleIsDeleted() {
    resource.deleteRoles( ROLE_NAME_DEVELOPER );

    verify( resource ).updateRolesForCurrentSession();
  }
}
