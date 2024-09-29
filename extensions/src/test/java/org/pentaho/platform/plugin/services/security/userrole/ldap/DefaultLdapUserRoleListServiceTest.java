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

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.LdapSearch;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/29/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class DefaultLdapUserRoleListServiceTest {
  DefaultLdapUserRoleListService service;

  @Mock Comparator<String> usernameComparator;
  Comparator<String> roleComparator = String.CASE_INSENSITIVE_ORDER;
  @Mock LdapSearch usernameSearch;
  @Mock LdapSearch authSearch;
  @Mock LdapSearch usersInRoleSearch;
  @Mock IAuthenticationRoleMapper roleMapper;
  @Mock ITenant tenant;
  @Mock ITenantedPrincipleNameResolver roleNameUtils;
  @Mock UserDetailsService userDetailService;
  @Mock UserDetails userDetails;
  @Mock ITenantedPrincipleNameResolver userNameUtils;

  Object[] filterArgs = new Object[0];
  List auths;
  List<String> extraRoles;
  GrantedAuthority grantedAuthority;
  GrantedAuthority extraRoleGrantedAuthority;

  @Before
  public void setUp() throws Exception {
    service = new DefaultLdapUserRoleListService();
    auths = new ArrayList<>();
    grantedAuthority = new SimpleGrantedAuthority( "Administrator" );
    extraRoleGrantedAuthority = new SimpleGrantedAuthority( "Authenticated" );
    auths.add( grantedAuthority );
    auths.add( extraRoleGrantedAuthority );
    extraRoles = new ArrayList<>();
    extraRoles.add( "Authenticated" );
  }

  @Test
  public void testGettersAndSetters() throws Exception {
    assertNull( service.getExtraRoles() );
    assertNull( service.getSystemRoles() );

    service.setExtraRoles( extraRoles );
    service.setSystemRoles( extraRoles );

    assertEquals( extraRoles, service.getExtraRoles() );
    assertEquals( extraRoles, service.getSystemRoles() );

    // no-op, but call it for code coverage
    service.afterPropertiesSet();
  }

  @Test
  public void testGetAllRoles_noRoleMapper() throws Exception {
    service = new DefaultLdapUserRoleListService( usernameComparator, roleComparator );
    service.setAllAuthoritiesSearch( authSearch );
    service.setExtraRoles( extraRoles );

    auths.add( new SimpleGrantedAuthority( "test" ) );
    when( authSearch.search( any( filterArgs.getClass() ) ) ).thenReturn( auths );

    List<String> allRoles = service.getAllRoles();
    assertEquals( auths.size(), allRoles.size() );
  }

  @Test( expected = UnsupportedOperationException.class )
  public void testGetAllRoles_nonDefaultTenant() throws Exception {
    service.getAllRoles( tenant );
  }

  @Test
  public void testGetAllRoles_defaultTenant() throws Exception {
    DefaultLdapUserRoleListService spyService = spy( service );
    doReturn( extraRoles ).when( spyService ).getAllRoles();
    spyService.getAllRoles( JcrTenantUtils.getDefaultTenant() );
    verify( spyService ).getAllRoles();
  }

  @Test( expected = UnsupportedOperationException.class )
  public void testGetAllUsers_nonDefaultTenant() throws Exception {
    service.getAllUsers( tenant );
  }

  @Test
  public void testGetAllUsers_defaultTenant() throws Exception {
    DefaultLdapUserRoleListService spyService = spy( service );
    doReturn( null ).when( spyService ).getAllUsers();
    spyService.getAllUsers( JcrTenantUtils.getDefaultTenant() );
    verify( spyService ).getAllUsers();
  }

  @Test
  public void testGetAllRoles_withRoleMapper() throws Exception {
    service = new DefaultLdapUserRoleListService( usernameComparator, roleComparator, roleMapper );
    service.setRoleComparator( null );  // override it it to null, make sure later it is never used
    service.setAllAuthoritiesSearch( authSearch );
    service.setExtraRoles( extraRoles );

    when( roleMapper.toPentahoRole( "test" ) ).thenReturn( "Developer" );
    when( roleMapper.toPentahoRole( "Authenticated" ) ).thenReturn( "Authenticated" );
    when( roleMapper.toPentahoRole( "Administrator" ) ).thenReturn( "Admin" );

    auths.add( new SimpleGrantedAuthority( "test" ) );
    auths.add( new SimpleGrantedAuthority( "Authenticated" ) );

    when( authSearch.search( any( filterArgs.getClass() ) ) ).thenReturn( auths );

    List<String> allRoles = service.getAllRoles();
    assertEquals( 3, allRoles.size() );
  }

  @Test
  public void testGetAllUsers() throws Exception {
    List<String> foundUsers = Arrays.asList( new String[] { "admin", "suzy" } );
    when( usernameSearch.search( any( filterArgs.getClass() ) ) ).thenReturn( foundUsers );
    service.setAllUsernamesSearch( usernameSearch );
    service.setUsernameComparator( usernameComparator );
    List<String> users = service.getAllUsers();
    assertNotNull( users );
    assertEquals( foundUsers.size(), users.size() );
    for ( String user : foundUsers ) {
      assertTrue( users.contains( user ) );
    }
    verify( usernameComparator ).compare( "suzy", "admin" );
  }

  @Test( expected = UnsupportedOperationException.class )
  public void testGetUsersInRole_nonDefaultTenant() throws Exception {
    service.getUsersInRole( tenant, "any" );
  }

  @Test
  public void testGetUsersInRole() throws Exception {
    List<String> powerUsers = Arrays.asList( new String[] { "admin", "suzy" } );
    service = new DefaultLdapUserRoleListService( usernameComparator, roleComparator, roleMapper );
    service.setRoleNameUtils( roleNameUtils );
    assertEquals( roleNameUtils, service.getRoleNameUtils() );
    service.setUsernamesInRoleSearch( usersInRoleSearch );

    when( roleNameUtils.getPrincipleName( nullable( String.class ) ) ).thenReturn( "rock star" );
    when( roleMapper.fromPentahoRole( "rock star" ) ).thenReturn( "power user" );
    when( usersInRoleSearch.search( any( filterArgs.getClass() ) ) ).thenReturn( powerUsers );

    List<String> foundUsers = service.getUsersInRole( JcrTenantUtils.getDefaultTenant(), "rock star" );

    assertNotNull( foundUsers );
    assertEquals( 2, foundUsers.size() );
    verify( usernameComparator ).compare( nullable( String.class ), nullable( String.class ) );
  }

  @Test( expected = UnsupportedOperationException.class )
  public void testGetRolesForUser_nonDefaultTenant() throws Exception {
    service.getRolesForUser( tenant, "admin" );
  }

  @Test
  public void testGetRolesForUser() throws Exception {
    service = new DefaultLdapUserRoleListService( usernameComparator, roleComparator, roleMapper );
    service.setUserDetailsService( userDetailService );
    service.setUserNameUtils( userNameUtils );
    service.setExtraRoles( extraRoles );
    assertEquals( userNameUtils, service.getUserNameUtils() );
    when( userDetailService.loadUserByUsername( "joe" ) ).thenReturn( userDetails );
    when( userNameUtils.getPrincipleName( nullable( String.class ) ) ).thenReturn( "joe" );

    auths.add( new SimpleGrantedAuthority( "test" ) );

    when( userDetails.getAuthorities() ).thenReturn( auths );

    List<String> foundRoles = service.getRolesForUser( JcrTenantUtils.getDefaultTenant(), "admin" );
    assertNotNull( foundRoles );
    assertEquals( 3, foundRoles.size() );
  }
}
