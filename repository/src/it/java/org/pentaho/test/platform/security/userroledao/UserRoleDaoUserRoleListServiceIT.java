/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.security.userroledao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserDetailsService;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserRoleListService;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test for {@link UserRoleDaoUserRoleListService}.
 *
 * @author mlowery
 */
@RunWith( SpringJUnit4ClassRunner.class )
@SuppressWarnings( "nls" )
public class UserRoleDaoUserRoleListServiceIT extends DefaultUnifiedRepositoryBase {
  public static final int DEFAULT_ROLE_COUNT = 4;
  public static final int DEFAULT_USER_COUNT = 1; // admin
  public static final String MAIN_TENANT_1 = "maintenant1";
  public static final String MAIN_TENANT_2 = "maintenant2";

  private Logger logger = LogManager.getLogger( UserRoleDaoUserRoleListServiceIT.class );

  private ITenant mainTenant_1;
  private ITenant mainTenant_2;

  public static final String PASSWORD_2 = "password2"; //$NON-NLS-1$
  public static final String PASSWORD_3 = "password3"; //$NON-NLS-1$
  public static final String PASSWORD_4 = "password4"; //$NON-NLS-1$
  public static final String PASSWORD_5 = "password5"; //$NON-NLS-1$
  public static final String PASSWORD_6 = "password6"; //$NON-NLS-1$
  public static final String PASSWORD_7 = "password7"; //$NON-NLS-1$
  public static final String PASSWORD_8 = "password8"; //$NON-NLS-1$

  public static final String USER_2 = "jim"; //$NON-NLS-1$
  public static final String USER_3 = "sally"; //$NON-NLS-1$
  public static final String USER_4 = "suzy"; //$NON-NLS-1$
  public static final String USER_5 = "nancy"; //$NON-NLS-1$
  public static final String USER_6 = "john"; //$NON-NLS-1$
  public static final String USER_7 = "jane"; //$NON-NLS-1$
  public static final String USER_8 = "jerry"; //$NON-NLS-1$

  public static final String ROLE_1 = "SalesMgr"; //$NON-NLS-1$
  public static final String ROLE_2 = "IT"; //$NON-NLS-1$

  public static final String ROLE_3 = "Sales"; //$NON-NLS-1$
  public static final String ROLE_4 = "Developer"; //$NON-NLS-1$
  public static final String ROLE_5 = "CEO"; //$NON-NLS-1$
  public static final String ROLE_6 = "Finance"; //$NON-NLS-1$
  public static final String ROLE_7 = "Marketing"; //$NON-NLS-1$

  public static final String USER_DESCRIPTION_2 = "User Description 2"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_3 = "User Description 3"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_4 = "User Description 4"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_5 = "User Description 5"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_6 = "User Description 6"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_7 = "User Description 7"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_8 = "User Description 8"; //$NON-NLS-1$

  public static final String ROLE_DESCRIPTION_1 = "Role Description 1"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_2 = "Role Description 2"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_3 = "Role Description 3"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_4 = "Role Description 4"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_5 = "Role Description 5"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_6 = "Role Description 6"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_7 = "Role Description 7"; //$NON-NLS-1$

  @Test
  public void testGetAllAuthorities() {
    loginAsSysTenantAdmin();
    mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    mainTenant_2 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );

    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createRole( mainTenant_1, ROLE_1, ROLE_DESCRIPTION_1, null );
    userRoleDao.createRole( mainTenant_1, ROLE_2, ROLE_DESCRIPTION_2, null );
    userRoleDao.createRole( mainTenant_1, ROLE_3, ROLE_DESCRIPTION_3, null );
    logout();
    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createRole( mainTenant_2, ROLE_4, ROLE_DESCRIPTION_4, null );
    userRoleDao.createRole( mainTenant_2, ROLE_5, ROLE_DESCRIPTION_5, null );
    userRoleDao.createRole( mainTenant_2, ROLE_6, ROLE_DESCRIPTION_6, null );
    userRoleDao.createRole( mainTenant_2, ROLE_7, ROLE_DESCRIPTION_7, null );

    List<String> systemRoles = Arrays.asList( USERNAME_ADMIN );
    List<String> extraRoles = Arrays.asList( AUTHENTICATED_ROLE_NAME, ANONYMOUS_ROLE_NAME );
    String adminRole = USERNAME_ADMIN;

    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    UserRoleDaoUserRoleListService service =
      new UserRoleDaoUserRoleListService( userRoleDao, userDetailsService, userNameUtils, systemRoles,
        extraRoles, adminRole );
    userDetailsService.setUserRoleDao( userRoleDao );
    logout();
    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );

    List<String> allRolesForDefaultTenant = service.getAllRoles();
    List<String> allRolesForTenant = service.getAllRoles( mainTenant_2 );
    logger.info( "allRolesForDefaultTenant.size() ==" + allRolesForDefaultTenant.size() );
    logger.info( "allRolesForTenant.size() ==" + allRolesForTenant.size() );


    assertTrue( allRolesForDefaultTenant.size() == 2 + DEFAULT_ROLE_COUNT );
    assertEquals( 3 + DEFAULT_ROLE_COUNT, allRolesForTenant.size() );

    logout();
    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    allRolesForDefaultTenant = service.getAllRoles();
    allRolesForTenant = service.getAllRoles( mainTenant_1 );
    assertTrue( allRolesForDefaultTenant.size() == 3 + DEFAULT_ROLE_COUNT );
    assertEquals( 2 + DEFAULT_ROLE_COUNT, allRolesForTenant.size() );

    allRolesForTenant = service.getAllRoles( mainTenant_2 );
    assertEquals( 3 + DEFAULT_ROLE_COUNT, allRolesForTenant.size() );

    allRolesForTenant = service.getAllRoles( mainTenant_1 );
    assertEquals( 2 + DEFAULT_ROLE_COUNT, allRolesForTenant.size() );

    logout();
    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );

    allRolesForTenant = service.getAllRoles( mainTenant_1 );
    assertEquals( 2 + DEFAULT_ROLE_COUNT, allRolesForTenant.size() );

    allRolesForTenant = service.getAllRoles( mainTenant_2 );
    assertEquals( 3 + DEFAULT_ROLE_COUNT, allRolesForTenant.size() );

    cleanupUserAndRoles( mainTenant_1 );
    cleanupUserAndRoles( mainTenant_2 );
  }

  @Test
  public void testGetAllUsernames() {
    loginAsSysTenantAdmin();
    mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    mainTenant_2 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );

    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( mainTenant_1, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null );
    userRoleDao.createUser( mainTenant_1, USER_3, PASSWORD_3, USER_DESCRIPTION_3, null );
    userRoleDao.createUser( null, userNameUtils.getPrincipleId( mainTenant_1, USER_4 ), PASSWORD_4,
      USER_DESCRIPTION_4, null );
    userRoleDao.createUser( null, USER_5, PASSWORD_5, USER_DESCRIPTION_5, null );
    userRoleDao.createUser( null, userNameUtils.getPrincipleId( mainTenant_1, USER_6 ), PASSWORD_6,
      USER_DESCRIPTION_6, null );
    logout();
    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( mainTenant_2, USER_7, PASSWORD_7, USER_DESCRIPTION_7, null );
    userRoleDao.createUser( null, USER_8, PASSWORD_8, USER_DESCRIPTION_8, null );
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );

    List<String> systemRoles = Arrays.asList( USERNAME_ADMIN );
    List<String> extraRoles = Arrays.asList( AUTHENTICATED_ROLE_NAME, ANONYMOUS_ROLE_NAME );
    String adminRole = USERNAME_ADMIN;

    UserRoleDaoUserRoleListService service =
      new UserRoleDaoUserRoleListService( userRoleDao, userDetailsService, userNameUtils, systemRoles,
        extraRoles, adminRole );
    service.setUserRoleDao( userRoleDao );
    service.setUserDetailsService( userDetailsService );

    logout();
    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    List<String> allUserForDefaultTenant = service.getAllUsers();
    List<String> allUserForTenant = service.getAllUsers( mainTenant_2 );

    assertEquals( 5 + DEFAULT_USER_COUNT, allUserForDefaultTenant.size() );
    assertEquals( 2 + DEFAULT_USER_COUNT, allUserForTenant.size() );
    logout();
    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    allUserForDefaultTenant = service.getAllUsers();
    allUserForTenant = service.getAllUsers( mainTenant_1 );

    assertTrue( allUserForDefaultTenant.size() == 2 + DEFAULT_USER_COUNT );
    assertEquals( 5 + DEFAULT_USER_COUNT, allUserForTenant.size() );

    allUserForTenant = service.getAllUsers( mainTenant_1 );

    assertEquals( 5 + DEFAULT_USER_COUNT, allUserForTenant.size() );
    allUserForTenant = service.getAllUsers( mainTenant_2 );
    assertEquals( 2 + DEFAULT_USER_COUNT, allUserForTenant.size() );
    logout();

    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    allUserForTenant = service.getAllUsers( mainTenant_1 );
    assertEquals( 5 + DEFAULT_USER_COUNT, allUserForTenant.size() );

    allUserForTenant = service.getAllUsers( mainTenant_2 );
    assertEquals( 2 + DEFAULT_USER_COUNT, allUserForTenant.size() );

    cleanupUserAndRoles( mainTenant_1 );
    cleanupUserAndRoles( mainTenant_2 );
  }

  @Test
  public void testGetAuthoritiesForUser() {
    loginAsSysTenantAdmin();
    mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    mainTenant_2 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );

    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( mainTenant_1, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null );
    userRoleDao.createUser( null, userNameUtils.getPrincipleId( mainTenant_1, USER_3 ), PASSWORD_3,
      USER_DESCRIPTION_3, null );
    userRoleDao.createUser( null, USER_4, PASSWORD_4, USER_DESCRIPTION_4, null );
    logout();

    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( mainTenant_2, USER_5, PASSWORD_5, USER_DESCRIPTION_5, null );
    userRoleDao.createUser( null, userNameUtils.getPrincipleId( mainTenant_2, USER_6 ), PASSWORD_6,
      USER_DESCRIPTION_6, null );

    logout();

    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createRole( mainTenant_1, ROLE_1, ROLE_DESCRIPTION_1, null );
    userRoleDao.createRole( null, roleNameUtils.getPrincipleId( mainTenant_1, ROLE_2 ), ROLE_DESCRIPTION_2,
      null );
    userRoleDao.createRole( null, ROLE_3, ROLE_DESCRIPTION_3, null );
    logout();

    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createRole( mainTenant_2, ROLE_4, ROLE_DESCRIPTION_4, null );
    userRoleDao.setUserRoles( null, USER_5, new String[]{ ROLE_4 } );
    userRoleDao.setUserRoles( null, userNameUtils.getPrincipleId( mainTenant_2, USER_6 ),
      new String[]{ ROLE_4 } );
    logout();
    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.setUserRoles( mainTenant_1, USER_2, new String[]{ ROLE_1, ROLE_2, ROLE_3 } );

    List<String> systemRoles = Arrays.asList( USERNAME_ADMIN );

    try {
      userRoleDao.setUserRoles( mainTenant_1, USER_3, new String[]{ ROLE_2, ROLE_3, ROLE_4 } );
    } catch ( Throwable th ) {
      assertNotNull( th );
    }

    try {
      userRoleDao.setUserRoles( mainTenant_1, USER_4, new String[]{ ROLE_2, ROLE_4 } );
      fail( "Exception should be thrown" );
    } catch ( Throwable th ) {
      assertNotNull( th );
    }
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );
    userDetailsService.setDefaultRole( tenantAuthenticatedRoleName );

    List<String> extraRoles = Arrays.asList( AUTHENTICATED_ROLE_NAME, ANONYMOUS_ROLE_NAME );
    String adminRole = USERNAME_ADMIN;

    UserRoleDaoUserRoleListService service =
      new UserRoleDaoUserRoleListService( userRoleDao, userDetailsService, userNameUtils, systemRoles,
        extraRoles, adminRole );
    service.setUserDetailsService( userDetailsService );

    logout();
    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    List<String> rolesForUser_2 = service.getRolesForUser( mainTenant_1, USER_2 );
    List<String> rolesForUser_2_1 = service.getRolesForUser( null, USER_2 );
    List<String> rolesForUser_2_1_1 =
      service.getRolesForUser( null, userNameUtils.getPrincipleId( mainTenant_1, USER_2 ) );
    List<String> rolesForUser_3 = service.getRolesForUser( mainTenant_1, USER_3 );
    List<String> rolesForUser_4 = service.getRolesForUser( mainTenant_1, USER_4 );

    assertTrue( rolesForUser_2.size() == 4 );
    assertTrue( rolesForUser_2_1.size() == 4 );
    assertTrue( rolesForUser_2_1_1.size() == 4 );
    assertTrue( rolesForUser_3.size() == 3 );
    assertTrue( rolesForUser_4.size() == 2 );

    cleanupUserAndRoles( mainTenant_1 );
    cleanupUserAndRoles( mainTenant_2 );
  }

  @Test
  public void testGetUsernamesInRole() {
    loginAsSysTenantAdmin();
    mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_1, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );
    mainTenant_2 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_2, tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_2, USERNAME_ADMIN, PASSWORD, "", new String[]{ tenantAdminRoleName } );

    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( mainTenant_1, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null );
    userRoleDao.createUser( null, USER_3, PASSWORD_3, USER_DESCRIPTION_3, null );
    userRoleDao.createUser( null, userNameUtils.getPrincipleId( mainTenant_1, USER_4 ), PASSWORD_4,
      USER_DESCRIPTION_4, null );
    userRoleDao.createUser( mainTenant_1, USER_5, PASSWORD_5, USER_DESCRIPTION_5, null );
    userRoleDao.createUser( mainTenant_1, USER_6, PASSWORD_6, USER_DESCRIPTION_6, null );
    logout();
    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( mainTenant_2, USER_7, PASSWORD_7, USER_DESCRIPTION_7, null );
    userRoleDao.createUser( mainTenant_2, USER_8, PASSWORD_8, USER_DESCRIPTION_8, null );
    logout();
    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createRole( mainTenant_1, ROLE_1, ROLE_DESCRIPTION_1, null );
    userRoleDao.createRole( null, ROLE_2, ROLE_DESCRIPTION_2, null );
    userRoleDao.createRole( null, roleNameUtils.getPrincipleId( mainTenant_1, ROLE_3 ), ROLE_DESCRIPTION_3,
      null );
    logout();
    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createRole( mainTenant_2, ROLE_4, ROLE_DESCRIPTION_4, null );
    logout();
    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.setRoleMembers( null, ROLE_1, new String[]{ USER_2, USER_3, USER_4 } );
    userRoleDao.setRoleMembers( mainTenant_1, ROLE_2, new String[]{ USER_5, USER_6, USER_7 } );
    userRoleDao.setRoleMembers( null, roleNameUtils.getPrincipleId( mainTenant_1, ROLE_3 ), new String[]{
      USER_2, USER_4, USER_6 } );
    logout();
    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.setRoleMembers( null, ROLE_4, new String[]{ USER_3, USER_5, USER_7 } );
    logout();
    login( USERNAME_ADMIN, mainTenant_1, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );
    userDetailsService.setDefaultRole( tenantAuthenticatedRoleName );
    List<String> systemRoles = new ArrayList<String>();
    systemRoles.add( USERNAME_ADMIN );

    List<String> extraRoles = Arrays.asList( AUTHENTICATED_ROLE_NAME, ANONYMOUS_ROLE_NAME );
    String adminRole = USERNAME_ADMIN;

    UserRoleDaoUserRoleListService service =
      new UserRoleDaoUserRoleListService( userRoleDao, userDetailsService, userNameUtils, systemRoles,
        extraRoles, adminRole );

    List<String> usersInRole_1 = service.getUsersInRole( mainTenant_1, ROLE_1 );
    List<String> usersInRole_2 = service.getUsersInRole( null, ROLE_2 );
    List<String> usersInRole_3 =
      service.getUsersInRole( null, roleNameUtils.getPrincipleId( mainTenant_1, ROLE_3 ) );

    logout();
    login( USERNAME_ADMIN, mainTenant_2, new String[]{ tenantAdminRoleName, tenantAuthenticatedRoleName } );

    List<String> usersInRole_4 = service.getUsersInRole( mainTenant_2, ROLE_4 );

    assertTrue( usersInRole_1.size() == 3 );
    assertTrue( usersInRole_2.size() == 2 );
    assertTrue( usersInRole_3.size() == 3 );
    assertTrue( usersInRole_4.size() == 1 );

    logout();

    cleanupUserAndRoles( mainTenant_1 );
    cleanupUserAndRoles( mainTenant_2 );
  }

}
