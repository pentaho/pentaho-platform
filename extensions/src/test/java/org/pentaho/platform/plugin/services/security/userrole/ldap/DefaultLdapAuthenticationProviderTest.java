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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 10/28/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class DefaultLdapAuthenticationProviderTest {

  DefaultLdapAuthenticationProvider ldapAuthProvider;

  @Mock LdapAuthenticator authenticator;
  @Mock IAuthenticationRoleMapper roleMapper;
  @Mock LdapAuthoritiesPopulator authoritiesPopulator;
  @Mock Authentication auth;
  @Mock UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;

  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void testConstructors() throws Exception {
    ldapAuthProvider = new DefaultLdapAuthenticationProvider( authenticator, roleMapper );
    ldapAuthProvider = new DefaultLdapAuthenticationProvider( authenticator, authoritiesPopulator, roleMapper );
    ldapAuthProvider = new DefaultLdapAuthenticationProvider( authenticator, authoritiesPopulator, roleMapper, "admin" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testAuthenticate_badArgs() throws Exception {
    ldapAuthProvider = new DefaultLdapAuthenticationProvider( authenticator, roleMapper );
    ldapAuthProvider.authenticate( auth );
  }

  @Test
  public void testAuthenticate() throws Exception {
    ldapAuthProvider = new DefaultLdapAuthenticationProvider( authenticator, authoritiesPopulator, roleMapper, "admin" );
    when( usernamePasswordAuthenticationToken.getName() ).thenReturn( "admin" );
    when( usernamePasswordAuthenticationToken.getCredentials() ).thenReturn( "p@$$w0rd" );

    DirContextOperations dirContextOps = mock( DirContextOperations.class );
    when( authenticator.authenticate( usernamePasswordAuthenticationToken ) ).thenReturn( dirContextOps );
    Collection grantedAuthorities = Arrays.asList( new GrantedAuthority[]{ new SimpleGrantedAuthority( "admin" ) } );

    when( authoritiesPopulator.getGrantedAuthorities( dirContextOps, "admin" ) ).thenReturn( grantedAuthorities );

    UserDetailsContextMapper contextMapper = mock( UserDetailsContextMapper.class );
    ldapAuthProvider.setUserDetailsContextMapper( contextMapper );
    UserDetails userDetails = mock( UserDetails.class );
    when( userDetails.getAuthorities() ).thenReturn( grantedAuthorities );
    when( contextMapper.mapUserFromContext( any( DirContextOperations.class ), anyString(), any( grantedAuthorities.getClass() ) ) ).thenReturn( userDetails );
    when( roleMapper.toPentahoRole( anyString() ) ).thenReturn( "admin" );

    Authentication result = ldapAuthProvider.authenticate( usernamePasswordAuthenticationToken );

    assertNotNull( result );
    assertEquals( "p@$$w0rd", result.getCredentials().toString() );
  }
}
