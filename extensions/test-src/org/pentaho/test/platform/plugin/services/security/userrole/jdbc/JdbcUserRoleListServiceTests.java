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

package org.pentaho.test.platform.plugin.services.security.userrole.jdbc;

import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.security.userrole.jdbc.JdbcUserRoleListService;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.PopulatedDatabase;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.jdbc.JdbcDaoImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JdbcUserRoleListServiceTests {

  @Test
  public void testGetAllUsernames() throws Exception {
    JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
    dao.setAllUsernamesQuery( "SELECT DISTINCT(USERNAME) FROM USERS ORDER BY USERNAME" ); //$NON-NLS-1$
    dao.afterPropertiesSet();
    List<String> allUsers = dao.getAllUsers();
    assertTrue( "User List should not be empty", allUsers.size() > 0 ); //$NON-NLS-1$
    for ( String username : allUsers ) {
      System.out.println( "User: " + username ); //$NON-NLS-1$
    }
  }

  @Test
  public void testGetAllUsernamesForTenant() throws Exception {
    ITenant defaultTenant = new Tenant( "/pentaho/tenant0", true );
    login( "admin", defaultTenant );
    JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
    dao.setAllUsernamesQuery( "SELECT DISTINCT(USERNAME) FROM USERS ORDER BY USERNAME" ); //$NON-NLS-1$
    dao.afterPropertiesSet();
    List<String> allUsers = dao.getAllUsers( defaultTenant );
    assertTrue( "User List should not be empty", allUsers.size() > 0 ); //$NON-NLS-1$
    for ( String username : allUsers ) {
      System.out.println( "User: " + username ); //$NON-NLS-1$
    }
    try {
      allUsers = dao.getAllUsers( new Tenant( "/pentaho", true ) );
    } catch ( UnsupportedOperationException uoe ) {
      assertNotNull( uoe );
    }
  }

  @Test
  public void testGetAllAuthorities() throws Exception {
    JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
    dao.setAllAuthoritiesQuery( "SELECT DISTINCT(AUTHORITY) AS AUTHORITY FROM AUTHORITIES ORDER BY 1" ); //$NON-NLS-1$
    dao.afterPropertiesSet();
    List<String> auths = dao.getAllRoles();
    assertTrue( "Authorities list should not be empty", auths.size() > 0 ); //$NON-NLS-1$
    for ( String auth : auths ) {
      System.out.println( "Authority: " + auth ); //$NON-NLS-1$
    }
  }

  @Test
  public void testGetAllAuthoritiesForTenant() throws Exception {
    ITenant defaultTenant = new Tenant( "/pentaho/tenant0", true );
    login( "admin", defaultTenant );

    JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
    dao.setAllAuthoritiesQuery( "SELECT DISTINCT(AUTHORITY) AS AUTHORITY FROM AUTHORITIES ORDER BY 1" ); //$NON-NLS-1$
    dao.afterPropertiesSet();
    List<String> auths = dao.getAllRoles( defaultTenant );
    assertTrue( "Authorities list should not be empty", auths.size() > 0 ); //$NON-NLS-1$
    for ( String auth : auths ) {
      System.out.println( "Authority: " + auth ); //$NON-NLS-1$
    }

    try {
      auths = dao.getAllRoles( new Tenant( "/pentaho", true ) );
    } catch ( UnsupportedOperationException uoe ) {
      assertNotNull( uoe );
    }
  }

  @Test
  public void testGetAllAuthoritiesWithRolePrefix() throws Exception {
    JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
    dao.setAllAuthoritiesQuery( "SELECT DISTINCT(AUTHORITY) AS AUTHORITY FROM AUTHORITIES ORDER BY 1" ); //$NON-NLS-1$
    dao.setRolePrefix( "ARBITRARY_PREFIX_" ); //$NON-NLS-1$
    dao.afterPropertiesSet();
    List<String> auths = dao.getAllRoles();
    assertTrue( "Authorities list should not be empty", auths.size() > 0 ); //$NON-NLS-1$
    for ( String role : auths ) {
      System.out.println( "Authority with prefix: " + role ); //$NON-NLS-1$
    }
  }

  @Test
  public void testGetAllAuthoritiesWithRolePrefixForTenant() throws Exception {
    ITenant defaultTenant = new Tenant( "/pentaho/tenant0", true );
    login( "admin", defaultTenant );
    JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
    dao.setAllAuthoritiesQuery( "SELECT DISTINCT(AUTHORITY) AS AUTHORITY FROM AUTHORITIES ORDER BY 1" ); //$NON-NLS-1$
    dao.setRolePrefix( "ARBITRARY_PREFIX_" ); //$NON-NLS-1$
    dao.afterPropertiesSet();
    List<String> auths = dao.getAllRoles( defaultTenant );
    assertTrue( "Authorities list should not be empty", auths.size() > 0 ); //$NON-NLS-1$
    for ( String role : auths ) {
      System.out.println( "Authority with prefix: " + role ); //$NON-NLS-1$
    }

    try {
      auths = dao.getAllRoles( new Tenant( "/pentaho", true ) );
    } catch ( UnsupportedOperationException uoe ) {
      assertNotNull( uoe );
    }
  }

  @Test
  public void testGetAllUsernamesInRole() throws Exception {
    JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
    dao.setAllUsernamesInRoleQuery(
      "SELECT DISTINCT(USERNAME) AS USERNAME FROM AUTHORITIES WHERE AUTHORITY = ? ORDER BY 1" ); //$NON-NLS-1$
    dao.afterPropertiesSet();
    List<String> allUsers = dao.getUsersInRole( "ROLE_TELLER" ); //$NON-NLS-1$
    assertTrue( "User List should not be empty", allUsers.size() > 0 ); //$NON-NLS-1$
    for ( String username : allUsers ) {
      System.out.println( "ROLE_TELLER User: " + username ); //$NON-NLS-1$
    }
  }

  @Test
  public void testGetAllUsernamesInRoleForTenant() throws Exception {
    ITenant defaultTenant = new Tenant( "/pentaho/tenant0", true );
    login( "admin", defaultTenant );
    JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
    dao.setAllUsernamesInRoleQuery(
      "SELECT DISTINCT(USERNAME) AS USERNAME FROM AUTHORITIES WHERE AUTHORITY = ? ORDER BY 1" ); //$NON-NLS-1$
    dao.afterPropertiesSet();
    List<String> allUsers = dao.getUsersInRole( defaultTenant, "ROLE_TELLER" ); //$NON-NLS-1$
    assertTrue( "User List should not be empty", allUsers.size() > 0 ); //$NON-NLS-1$
    for ( String username : allUsers ) {
      System.out.println( "ROLE_TELLER User: " + username ); //$NON-NLS-1$
    }

    try {
      allUsers = dao.getUsersInRole( new Tenant( "/pentaho", true ), "ROLE_TELLER" );
    } catch ( UnsupportedOperationException uoe ) {
      assertNotNull( uoe );
    }

  }

  @Test
  public void testGetRolesForUser() throws Exception {
    JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
    dao.setUserDetailsService( makePopulatedJdbcDao() );
    dao.afterPropertiesSet();
    List<String> roles = dao.getRolesForUser( "rod" ); //$NON-NLS-1$
    assertTrue( roles.contains( "ROLE_TELLER" ) ); //$NON-NLS-1$
    assertTrue( roles.contains( "ROLE_SUPERVISOR" ) ); //$NON-NLS-1$

  }

  @Test
  public void testGetRolesForUserForTenant() throws Exception {
    ITenant defaultTenant = new Tenant( "/pentaho/tenant0", true );
    login( "admin", defaultTenant );
    JdbcUserRoleListService dao = makePopulatedJdbcUserRoleListService();
    dao.setUserDetailsService( makePopulatedJdbcDao() );
    dao.afterPropertiesSet();
    List<String> roles = dao.getRolesForUser( defaultTenant, "rod" ); //$NON-NLS-1$
    assertTrue( roles.contains( "ROLE_TELLER" ) ); //$NON-NLS-1$
    assertTrue( roles.contains( "ROLE_SUPERVISOR" ) ); //$NON-NLS-1$

    try {
      roles = dao.getRolesForUser( new Tenant( "/pentaho", true ), "rod" );
    } catch ( UnsupportedOperationException uoe ) {
      assertNotNull( uoe );
    }
  }

  protected JdbcUserRoleListService makePopulatedJdbcUserRoleListService() throws Exception {
    List<String> systemRoles = new ArrayList<String>();
    systemRoles.add( "Admin" );
    JdbcUserRoleListService dao = new JdbcUserRoleListService( makePopulatedJdbcDao(), systemRoles );
    dao.setDataSource( PopulatedDatabase.getDataSource() );
    return dao;
  }

  private JdbcDaoImpl makePopulatedJdbcDao() throws Exception {
    JdbcDaoImpl dao = new JdbcDaoImpl();
    dao.setDataSource( PopulatedDatabase.getDataSource() );
    dao.afterPropertiesSet();
    return dao;
  }

  /**
   * Logs in with given username.
   * 
   * @param username
   *          username of user
   * @param tenantId
   *          tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  protected void login( final String username, final ITenant tenant ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( username );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
    authList.add( new GrantedAuthorityImpl( "TenantAdmin" ) );
    authList.add( new GrantedAuthorityImpl( "Authenticated" ) );
    GrantedAuthority[] authorities = authList.toArray( new GrantedAuthority[0] );
    UserDetails userDetails = new User( username, password, true, true, true, true, authorities );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
  }

}
