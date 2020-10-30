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

import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.security.userrole.ldap.PentahoCachingLdapAuthoritiesPopulator;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.Collection;
import java.util.HashSet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
    doReturn( authList ).when( mockPopulator ).getGrantedAuthorities( any(), anyString() );

    cachingLdapAuthoritiesPopulator.getGrantedAuthorities( mockDirContextOperations, "fred" );
    cachingLdapAuthoritiesPopulator.getGrantedAuthorities( mockDirContextOperations, "fred" );
    cachingLdapAuthoritiesPopulator.getGrantedAuthorities( mockDirContextOperations, "fred" );

    verify( mockPopulator, times( 1 ) ).getGrantedAuthorities( any(), anyString() );
  }

  @Test
  public void testGetRegionName() {
    PentahoCachingLdapAuthoritiesPopulator cachingLdapAuthoritiesPopulator
      = new PentahoCachingLdapAuthoritiesPopulator( mockPopulator );

    cachingLdapAuthoritiesPopulator.setCacheRegionName( "testRegionName" );

    assertEquals( "testRegionName", cachingLdapAuthoritiesPopulator.getCacheRegionName() );
  }
}
