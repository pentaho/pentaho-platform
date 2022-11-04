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

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;

public class DefaultLdapUserDetailsService extends LdapUserDetailsService {

  ITenantedPrincipleNameResolver userNameUtils;
  private LdapAuthoritiesPopulator authoritiesPopulator;
  private static final String SPRING_ABS_PATH_PREFIX = "file:";

  public DefaultLdapUserDetailsService( LdapUserSearch userSearch, LdapAuthoritiesPopulator authoritiesPopulator,
      ITenantedPrincipleNameResolver userNameUtils ) {
    super( userSearch, authoritiesPopulator );
    this.userNameUtils = userNameUtils;
    this.authoritiesPopulator = authoritiesPopulator;
  }

  @Override
  public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
	  UserDetails userDetails = null;
	  try {
		   userDetails = super.loadUserByUsername( userNameUtils.getPrincipleName( username ) );
	  }
		catch (UsernameNotFoundException uNFE) {
			FilterBasedLdapUserSearch basedLdapUserSearch = new FilterBasedLdapUserSearch(
					"ou=goodguys,dc=dccomic,dc=com", "uid={0}", contextSource());
			LdapUserDetailsService detailsService = new LdapUserDetailsService(basedLdapUserSearch,authoritiesPopulator);
			userDetails = detailsService.loadUserByUsername(userNameUtils.getPrincipleName(username));
		}
    return userDetails;
  }
  

  @Bean("ReferralUserDetailsContextSource")
  public DefaultSpringSecurityContextSource contextSource() {
      DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource("ldap://localhost:10389/");
      contextSource.setUserDn("uid=admin,ou=goodguys,dc=dccomic,dc=com");
      contextSource.setPassword("password");
      contextSource.afterPropertiesSet();
      return contextSource;
  }

}
