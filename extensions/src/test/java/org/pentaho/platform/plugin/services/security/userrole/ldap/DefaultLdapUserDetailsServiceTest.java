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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.search.LdapUserSearch;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/30/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class DefaultLdapUserDetailsServiceTest {

  @Mock LdapUserSearch userSearch;
  @Mock LdapAuthoritiesPopulator authPop;
  @Mock ITenantedPrincipleNameResolver usernameUtils;

  @Test
  public void testLoadUserByUsername() {
    DefaultLdapUserDetailsService service = spy( new DefaultLdapUserDetailsService( userSearch, authPop, usernameUtils ) );
    when( usernameUtils.getPrincipleName( nullable( String.class ) ) ).thenReturn( "admin" );
    try {
      service.loadUserByUsername( "JOE" );
    } catch ( NullPointerException npe ) {
      // expected since we aren't configuring everything required by the super class
      // but we can verify the right stuff got called
      verify( usernameUtils ).getPrincipleName( "JOE" );
    }
  }
}
