/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.LdapSearch;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
  List<GrantedAuthority> auths;
  List<String> extraRoles;
  GrantedAuthority grantedAuthority;
  GrantedAuthority extraRoleGrantedAuthority;

  @Before
  public void setUp() throws Exception {
    service = new DefaultLdapUserRoleListService();
    auths = new ArrayList<>();
    grantedAuthority = new GrantedAuthorityImpl( "Administrator" );
    extraRoleGrantedAuthority = new GrantedAuthorityImpl( "Authenticated" );
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

    auths.add( new GrantedAuthorityImpl( "test" ) );
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

    auths.add( new GrantedAuthorityImpl( "test" ) );
    auths.add( new GrantedAuthorityImpl( "Authenticated" ) );

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

    when( roleNameUtils.getPrincipleName( anyString() ) ).thenReturn( "rock star" );
    when( roleMapper.fromPentahoRole( "rock star" ) ).thenReturn( "power user" );
    when( usersInRoleSearch.search( any( filterArgs.getClass() ) ) ).thenReturn( powerUsers );

    List<String> foundUsers = service.getUsersInRole( JcrTenantUtils.getDefaultTenant(), "rock star" );

    assertNotNull( foundUsers );
    assertEquals( 2, foundUsers.size() );
    verify( usernameComparator ).compare( anyString(), anyString() );
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
    when( userNameUtils.getPrincipleName( anyString() ) ).thenReturn( "joe" );

    auths.add( new GrantedAuthorityImpl( "test" ) );

    when( userDetails.getAuthorities() ).thenReturn( auths.toArray( new GrantedAuthority[]{} ) );

    List<String> foundRoles = service.getRolesForUser( JcrTenantUtils.getDefaultTenant(), "admin" );
    assertNotNull( foundRoles );
    assertEquals( 3, foundRoles.size() );
  }
}
