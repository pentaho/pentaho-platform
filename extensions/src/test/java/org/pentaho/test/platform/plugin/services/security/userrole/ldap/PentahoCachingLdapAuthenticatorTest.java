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


package org.pentaho.test.platform.plugin.services.security.userrole.ldap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.plugin.services.security.userrole.ldap.PentahoCachingLdapAuthenticator;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticator;


import static org.junit.Assert.assertEquals;
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
