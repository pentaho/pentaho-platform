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


package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
  public void testConstructors() {
    ldapAuthProvider = new DefaultLdapAuthenticationProvider( authenticator, roleMapper );
    ldapAuthProvider = new DefaultLdapAuthenticationProvider( authenticator, authoritiesPopulator, roleMapper );
    ldapAuthProvider = new DefaultLdapAuthenticationProvider( authenticator, authoritiesPopulator, roleMapper, "admin" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testAuthenticate_badArgs() {
    ldapAuthProvider = new DefaultLdapAuthenticationProvider( authenticator, roleMapper );
    ldapAuthProvider.authenticate( auth );
  }

  @Test
  public void testAuthenticate() {
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
    when( contextMapper.mapUserFromContext( any( DirContextOperations.class ), nullable( String.class ), any( grantedAuthorities.getClass() ) ) ).thenReturn( userDetails );
    when( roleMapper.toPentahoRole( nullable( String.class ) ) ).thenReturn( "admin" );

    Authentication result = ldapAuthProvider.authenticate( usernamePasswordAuthenticationToken );

    assertNotNull( result );
    assertEquals( "p@$$w0rd", result.getCredentials().toString() );
  }
}
