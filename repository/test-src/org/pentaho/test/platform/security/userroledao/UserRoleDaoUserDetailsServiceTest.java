/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.security.userroledao;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserDetailsService;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link UserRoleDaoUserDetailsService}.
 * 
 * @author mlowery
 */
@RunWith( SpringJUnit4ClassRunner.class )
@SuppressWarnings( "nls" )
public class UserRoleDaoUserDetailsServiceTest extends DefaultUnifiedRepositoryBase {

  private static final String USERNAME = USERNAME_ADMIN; //$NON-NLS-1$

  public static final String MAIN_TENANT_1 = "maintenant1";
  public static final String PASSWORD_2 = "password2"; //$NON-NLS-1$

  public static final String USER_2 = "jim"; //$NON-NLS-1$

  public static final String ROLE_0 = "Authenticated"; //$NON-NLS-1$

  public static final String ROLE_1 = "SalesMgr"; //$NON-NLS-1$
  public static final String ROLE_2 = "IT"; //$NON-NLS-1$
  public static final String ROLE_3 = "Sales"; //$NON-NLS-1$

  public static final String USER_DESCRIPTION_2 = "User Description 2"; //$NON-NLS-1$

  public static final String ROLE_DESCRIPTION_1 = "Role Description 1"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_2 = "Role Description 2"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_3 = "Role Description 3"; //$NON-NLS-1$

  @BeforeClass
  public static void setUpClass() throws Exception {
    DefaultUnifiedRepositoryBase.setUpClass();

    FileUtils.deleteDirectory( new File( "/tmp/repository/jackrabbit-test-TRUNK" ) );
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    DefaultUnifiedRepositoryBase.tearDownClass();
  }

  @Test( expected = UsernameNotFoundException.class )
  public void testLoadUserByUsernameUsernameNotFound() {
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );
    userDetailsService.loadUserByUsername( USERNAME );
  }

  @Test
  public void testLoadUserByUsername() {
    loginAsSysTenantAdmin();
    ITenant mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminRoleName,
            tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_1, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );
    login( USERNAME_ADMIN, mainTenant_1, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    IPentahoUser pentahoUser = userRoleDao.createUser( mainTenant_1, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null );
    IPentahoRole pentahoRole = userRoleDao.createRole( mainTenant_1, ROLE_1, ROLE_DESCRIPTION_1, null );
    pentahoRole = userRoleDao.createRole( mainTenant_1, ROLE_2, ROLE_DESCRIPTION_2, null );
    pentahoRole = userRoleDao.createRole( mainTenant_1, ROLE_3, ROLE_DESCRIPTION_3, null );
    userRoleDao.setUserRoles( mainTenant_1, USER_2, new String[] { ROLE_1, ROLE_2, ROLE_3 } );

    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );
    userDetailsService.setDefaultRole( tenantAuthenticatedRoleName );
    UserDetails userFromService = userDetailsService.loadUserByUsername( USER_2 );

    assertTrue( userFromService.getUsername().equals( USER_2 ) );
    assertTrue( userFromService.getPassword() != null );
    assertTrue( userFromService.isEnabled() == true );
    assertTrue( userFromService.getAuthorities().length == 4 );

    assertTrue( userFromService.getAuthorities()[0].getAuthority().equals( ROLE_0 )
        || userFromService.getAuthorities()[0].getAuthority().equals( ROLE_3 )
        || userFromService.getAuthorities()[0].getAuthority().equals( ROLE_2 )
        || userFromService.getAuthorities()[0].getAuthority().equals( ROLE_1 ) );
    assertTrue( userFromService.getAuthorities()[1].getAuthority().equals( ROLE_0 )
        || userFromService.getAuthorities()[1].getAuthority().equals( ROLE_3 )
        || userFromService.getAuthorities()[1].getAuthority().equals( ROLE_2 )
        || userFromService.getAuthorities()[1].getAuthority().equals( ROLE_1 ) );
    assertTrue( userFromService.getAuthorities()[2].getAuthority().equals( ROLE_0 )
        || userFromService.getAuthorities()[2].getAuthority().equals( ROLE_3 )
        || userFromService.getAuthorities()[2].getAuthority().equals( ROLE_2 )
        || userFromService.getAuthorities()[2].getAuthority().equals( ROLE_1 ) );
    assertTrue( userFromService.getAuthorities()[3].getAuthority().equals( ROLE_0 )
        || userFromService.getAuthorities()[3].getAuthority().equals( ROLE_3 )
        || userFromService.getAuthorities()[3].getAuthority().equals( ROLE_2 )
        || userFromService.getAuthorities()[3].getAuthority().equals( ROLE_1 ) );

    cleanupUserAndRoles( mainTenant_1 );
  }

  @Test
  public void testLoadUserByUsernameNoRoles() {
    loginAsSysTenantAdmin();
    ITenant mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminRoleName,
            tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_1, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );
    login( USERNAME_ADMIN, mainTenant_1, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    IPentahoUser pentahoUser = userRoleDao.createUser( mainTenant_1, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null );

    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );
    try {
      userDetailsService.loadUserByUsername( USER_2 );
    } catch ( UsernameNotFoundException unnf ) {
      assertNotNull( unnf );
    }

    cleanupUserAndRoles( mainTenant_1 );
  }
}
