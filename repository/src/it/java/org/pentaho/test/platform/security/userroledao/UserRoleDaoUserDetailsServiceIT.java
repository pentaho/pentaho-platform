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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.security.userroledao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link UserRoleDaoUserDetailsService}.
 * 
 * @author mlowery
 */
@RunWith( SpringJUnit4ClassRunner.class )
@SuppressWarnings( "nls" )
public class UserRoleDaoUserDetailsServiceIT extends DefaultUnifiedRepositoryBase {

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
    assertTrue( userFromService.getAuthorities().size() == 4 );

    GrantedAuthority[] auths = new GrantedAuthority[4];
    userFromService.getAuthorities().toArray( auths );

    assertTrue( auths[0].getAuthority().equals( ROLE_0 )
        || auths[0].getAuthority().equals( ROLE_3 )
        || auths[0].getAuthority().equals( ROLE_2 )
        || auths[0].getAuthority().equals( ROLE_1 ) );
    assertTrue( auths[1].getAuthority().equals( ROLE_0 )
        || auths[1].getAuthority().equals( ROLE_3 )
        || auths[1].getAuthority().equals( ROLE_2 )
        || auths[1].getAuthority().equals( ROLE_1 ) );
    assertTrue( auths[2].getAuthority().equals( ROLE_0 )
        || auths[2].getAuthority().equals( ROLE_3 )
        || auths[2].getAuthority().equals( ROLE_2 )
        || auths[2].getAuthority().equals( ROLE_1 ) );
    assertTrue( auths[3].getAuthority().equals( ROLE_0 )
        || auths[3].getAuthority().equals( ROLE_3 )
        || auths[3].getAuthority().equals( ROLE_2 )
        || auths[3].getAuthority().equals( ROLE_1 ) );

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
