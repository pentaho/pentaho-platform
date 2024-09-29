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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;

public class DefaultLdapUserDetailsService extends LdapUserDetailsService {

  ITenantedPrincipleNameResolver userNameUtils;

  public DefaultLdapUserDetailsService( LdapUserSearch userSearch, LdapAuthoritiesPopulator authoritiesPopulator,
      ITenantedPrincipleNameResolver userNameUtils ) {
    super( userSearch, authoritiesPopulator );
    this.userNameUtils = userNameUtils;
  }

  @Override
  public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
    return super.loadUserByUsername( userNameUtils.getPrincipleName( username ) );
  }

}
