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
 * Copyright (c) 2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.plugin.services.security.userrole.ldap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.plugin.services.security.userrole.ldap.PentahoCachingLdapAuthenticator;
import org.pentaho.platform.plugin.services.security.userrole.ldap.PentahoCachingLdapAuthoritiesPopulator;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the <code>PentahoCachingLdapAuthoritiesPopulator</code> class.
 * 
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoCachingLdapAuthenticatorTest {

  @Mock LdapAuthenticator mockAuthenticator;
  @Mock Authentication mockAuthentication1;
  @Mock Authentication mockAuthentication2;
  @Mock DirContextOperations mockDirContextOperations1;
  @Mock DirContextOperations mockDirContextOperations2;

  @Test
  public void testGetGrantedAuthorities() {
    PentahoCachingLdapAuthenticator cachingLdapAuthenticator
      = new PentahoCachingLdapAuthenticator( mockAuthenticator );

    when( mockAuthenticator.authenticate( mockAuthentication1 ) ).thenReturn( mockDirContextOperations1 );
    when( mockAuthenticator.authenticate( mockAuthentication2 ) ).thenReturn( mockDirContextOperations2 );
    when( mockAuthentication1.getPrincipal() ).thenReturn( "fred" );
    when( mockAuthentication2.getPrincipal() ).thenReturn( "barney" );

    cachingLdapAuthenticator.authenticate( mockAuthentication1 );
    cachingLdapAuthenticator.authenticate( mockAuthentication2 );
    cachingLdapAuthenticator.authenticate( mockAuthentication1 );
    cachingLdapAuthenticator.authenticate( mockAuthentication2 );
    cachingLdapAuthenticator.authenticate( mockAuthentication2 );
    cachingLdapAuthenticator.authenticate( mockAuthentication1 );

    // ensure the cache catches subsequent calls for the same auth
    verify( mockAuthenticator, times( 1 ) ).authenticate( mockAuthentication1 );
    verify( mockAuthenticator, times( 1 ) ).authenticate( mockAuthentication2 );
  }

  @Test
  public void testGetRegionName() {
    PentahoCachingLdapAuthenticator cachingLdapAuthenticator
      = new PentahoCachingLdapAuthenticator( mockAuthenticator );

    cachingLdapAuthenticator.setCacheRegionName( "testRegionName" );

    assertEquals( "testRegionName", cachingLdapAuthenticator.getCacheRegionName() );
  }

  @Test
  public void testSetPasswordHashMethod() {
    PentahoCachingLdapAuthenticator cachingLdapAuthenticator
      = new PentahoCachingLdapAuthenticator( mockAuthenticator );

    cachingLdapAuthenticator.setPasswordHashMethod( "SHA-1" );

    assertEquals( "SHA-1", cachingLdapAuthenticator.getPasswordHashMethod() );
  }
}
