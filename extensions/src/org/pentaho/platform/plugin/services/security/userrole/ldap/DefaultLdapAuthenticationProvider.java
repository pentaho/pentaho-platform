/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.ldap.LdapAuthoritiesPopulator;
import org.springframework.security.providers.ldap.LdapAuthenticationProvider;
import org.springframework.security.providers.ldap.LdapAuthenticator;

public class DefaultLdapAuthenticationProvider extends LdapAuthenticationProvider {

  private IAuthenticationRoleMapper roleMapper;
  private String authenticatedRole;

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
  protected GrantedAuthority[] loadUserAuthorities( DirContextOperations userData, String username, String password ) {
    GrantedAuthority[] authorities = super.loadUserAuthorities( userData, username, password );
    if ( roleMapper != null ) {
      for ( int i = 0; i < authorities.length; i++ ) {
        if ( authorities[i] != null ) {
          authorities[i] = new GrantedAuthorityImpl( roleMapper.toPentahoRole( authorities[i].getAuthority() ) );
        }
      }
    }
    return authorities;
  }

  @Override
  public Authentication authenticate( Authentication authentication ) throws AuthenticationException {
    final Authentication authenticate = super.authenticate( authentication );
    for ( GrantedAuthority authority : authenticate.getAuthorities() ) {
      if ( authority.getAuthority().equals( authenticatedRole ) ) {
        return authenticate;
      }
    }
    throw new AuthenticationServiceException( "The user doesn't have '" + authenticatedRole + "' role." );
  }

  private void setAuthenticatedRole( String authenticatedRole ) {
    this.authenticatedRole = authenticatedRole == null
        ? PentahoSystem.get( String.class, "singleTenantAuthenticatedAuthorityName", null ) : authenticatedRole;
  }
}
