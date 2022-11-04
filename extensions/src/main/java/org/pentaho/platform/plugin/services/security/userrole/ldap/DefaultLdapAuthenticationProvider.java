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

import org.apache.commons.io.FilenameUtils;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public class DefaultLdapAuthenticationProvider extends LdapAuthenticationProvider {

  private IAuthenticationRoleMapper roleMapper;
  private String authenticatedRole;
  private DefaultSpringSecurityContextSource contextSource;

  public DefaultLdapAuthenticationProvider( LdapAuthenticator authenticator, IAuthenticationRoleMapper roleMapper ) {
    super( authenticator );
    this.roleMapper = roleMapper;
    setAuthenticatedRole( null );
  }

  public DefaultLdapAuthenticationProvider( LdapAuthenticator authenticator,
      LdapAuthoritiesPopulator authoritiesPopulator, IAuthenticationRoleMapper roleMapper ) {
    super( authenticator, authoritiesPopulator );
    this.roleMapper = roleMapper;
    setAuthenticatedRole( null );
  }
  
  public DefaultLdapAuthenticationProvider( LdapAuthenticator authenticator,
      LdapAuthoritiesPopulator authoritiesPopulator, IAuthenticationRoleMapper roleMapper, String authenticatedRole ) {
    super( authenticator, authoritiesPopulator );
    this.roleMapper = roleMapper;
    setAuthenticatedRole( authenticatedRole );
  }

  /**
   * We need to iterate through the authorities and map them to pentaho security equivalent
   */
  @Override
  protected Collection<? extends GrantedAuthority> loadUserAuthorities( DirContextOperations userData, String username, String password ) {
    GrantedAuthority[] authorities = super.loadUserAuthorities( userData, username, password ).toArray( new GrantedAuthority[]{} );
    if ( roleMapper != null ) {
      for ( int i = 0; i < authorities.length; i++ ) {
        if ( authorities[i] != null ) {
          authorities[i] = new SimpleGrantedAuthority( roleMapper.toPentahoRole( authorities[i].getAuthority() ) );
        }
      }
    }
    return Arrays.asList( authorities );
  }

  @Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Authentication authenticate = null;
		try {
			authenticate = super.authenticate(authentication);
		} catch (AuthenticationException e) {
			LdapAuthenticationProvider ldapAuthenticationProvider = createReferralAuth(authentication);
			authenticate = ldapAuthenticationProvider.authenticate(authentication);
		}
		for (GrantedAuthority authority : authenticate.getAuthorities()) {
			if (authority.getAuthority().equals(authenticatedRole)) {
				return authenticate;
			}
		}
		throw new AuthenticationServiceException("The user doesn't have '" + authenticatedRole + "' role.");
	}

  private LdapAuthenticationProvider createReferralAuth(Authentication authentication) {
	  BindAuthenticator authenticator = bindAuthenticator();
	  DefaultLdapAuthoritiesPopulator defaultLdapAuthoritiesPopulator = new DefaultLdapAuthoritiesPopulator(this.contextSource , "OU=roles,DC=dccomic,DC=com");
	  defaultLdapAuthoritiesPopulator.setGroupSearchFilter("roleOccupant={0}");
	  defaultLdapAuthoritiesPopulator.setConvertToUpperCase(false);
	  defaultLdapAuthoritiesPopulator.setRolePrefix("");
	  defaultLdapAuthoritiesPopulator.setDefaultRole("Authenticated");
	  return new LdapAuthenticationProvider(authenticator ,defaultLdapAuthoritiesPopulator);
  }
  
  @Bean("ReferralAuthenticationContextSource")
  public DefaultSpringSecurityContextSource contextSource() {
      DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource("ldap://localhost:10389/");
      contextSource.setUserDn("uid=admin,ou=goodguys,dc=dccomic,dc=com");
      contextSource.setPassword("password");
      contextSource.afterPropertiesSet();
      this.contextSource = contextSource;
      return contextSource;
  }

  @Bean("ReferralBindAuthenticator")
  public BindAuthenticator bindAuthenticator() {
	  DefaultSpringSecurityContextSource contextSource = contextSource();
	  FilterBasedLdapUserSearch basedLdapUserSearch = new FilterBasedLdapUserSearch("ou=goodguys,dc=dccomic,dc=com", "uid={0}", contextSource);
	  BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
	  bindAuthenticator.setUserSearch(basedLdapUserSearch);
      return bindAuthenticator;
  }
  

  private void setAuthenticatedRole( String authenticatedRole ) {
    this.authenticatedRole = authenticatedRole == null
        ? PentahoSystem.get( String.class, "singleTenantAuthenticatedAuthorityName", null ) : authenticatedRole;
  }
}
