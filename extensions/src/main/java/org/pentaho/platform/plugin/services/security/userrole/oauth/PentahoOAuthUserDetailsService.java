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

package org.pentaho.platform.plugin.services.security.userrole.oauth;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public class PentahoOAuthUserDetailsService implements UserDetailsService {

  private IUserRoleListService userRoleListService;

  public PentahoOAuthUserDetailsService( IUserRoleListService userRoleListService ) {
    this.userRoleListService = userRoleListService;
  }

  @Override
  public UserDetails loadUserByUsername( String username ) throws OAuth2AuthenticationException {
    List<GrantedAuthority> authorities = new ArrayList<>();
    if ( StringUtils.equals( username,  "admin-/pentaho/tenant0" ) || StringUtils.equals( username, "admin" ) ) {
      String role = PentahoSystem
              .getSystemSetting("acl-voter/admin-role", "Administrator"); //$NON-NLS-1$//$NON-NLS-2$
      authorities.add( new SimpleGrantedAuthority( role ) );
    } else {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      for ( String role : userRoleListService.getRolesForUser( null, authentication.getName() ) ) {
        authorities.add( new SimpleGrantedAuthority( role ) );
      }
    }
    return new User( username, "password", true, true,
      true, true, authorities );
  }

}