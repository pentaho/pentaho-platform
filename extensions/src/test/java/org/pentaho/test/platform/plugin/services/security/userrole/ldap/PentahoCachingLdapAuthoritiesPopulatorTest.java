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
import org.pentaho.platform.plugin.services.security.userrole.ldap.PentahoCachingLdapAuthoritiesPopulator;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.Collection;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertEquals;

/**
 * Tests the <code>PentahoCachingLdapAuthoritiesPopulator</code> class.
 * 
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoCachingLdapAuthoritiesPopulatorTest {

  @Mock LdapAuthoritiesPopulator mockPopulator;
  @Mock DirContextOperations mockDirContextOperations;

  @Test
  public void testGetGrantedAuthorities() {
    PentahoCachingLdapAuthoritiesPopulator cachingLdapAuthoritiesPopulator
      = new PentahoCachingLdapAuthoritiesPopulator( mockPopulator );
    SimpleGrantedAuthority role1 = new SimpleGrantedAuthority( "someRole" );
    Collection<SimpleGrantedAuthority> authList = new HashSet<>();
    authList.add( role1 );
    doReturn( authList ).when( mockPopulator ).getGrantedAuthorities( any(), nullable( String.class ) );

    cachingLdapAuthoritiesPopulator.getGrantedAuthorities( mockDirContextOperations, "fred" );
    cachingLdapAuthoritiesPopulator.getGrantedAuthorities( mockDirContextOperations, "fred" );
    cachingLdapAuthoritiesPopulator.getGrantedAuthorities( mockDirContextOperations, "fred" );

    verify( mockPopulator, times( 1 ) ).getGrantedAuthorities( any(), nullable( String.class ) );
  }

  @Test
  public void testGetRegionName() {
    PentahoCachingLdapAuthoritiesPopulator cachingLdapAuthoritiesPopulator
      = new PentahoCachingLdapAuthoritiesPopulator( mockPopulator );

    cachingLdapAuthoritiesPopulator.setCacheRegionName( "testRegionName" );

    assertEquals( "testRegionName", cachingLdapAuthoritiesPopulator.getCacheRegionName() );
  }
}
