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
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class PentahoOAuthUserDetailsService implements UserDetailsService, OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;

  private IUserRoleListService userRoleListService;

  public PentahoOAuthUserDetailsService( IUserRoleListService userRoleListService ) {
    this.oauth2UserService = new DefaultOAuth2UserService(); // Use the default OAuth2UserService
    this.userRoleListService = userRoleListService;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws OAuth2AuthenticationException {
    if ( StringUtils.equals( username,  "admin-/pentaho/tenant0" ) || StringUtils.equals( username, "admin" ) ) {
      String role = PentahoSystem
              .getSystemSetting("acl-voter/admin-role", "Administrator"); //$NON-NLS-1$//$NON-NLS-2$
      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add( new SimpleGrantedAuthority( role ) );
      return new User( username, "ignored", true, true,
              true, true, authorities );
    } else {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      List<GrantedAuthority> authorities = new ArrayList<>();
      for ( String role : userRoleListService.getRolesForUser( null, authentication.getName() ) ) {
        authorities.add( new SimpleGrantedAuthority( role ) );
      }
      return new User( username, "ignored", true, true,
              true, true, authorities );
    }
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    // Use Spring Security's default OAuth2UserService to load the OAuth2User
    OAuth2User oAuth2User = oauth2UserService.loadUser(userRequest);

    // Extract attributes from the OAuth2 user (e.g., Google, Facebook)
    Map<String, Object> attributes = oAuth2User.getAttributes();

    // Assuming roles are present in the "roles" or "groups" claim (can vary based on OAuth2 provider)
    List<String> roles = (List<String>) attributes.get("roles"); // Google may use "roles" or "groups"

    // If roles are not present, assign a default role (customize as needed)
    if (roles == null || roles.isEmpty()) {
      roles = List.of("USER");  // Default role if none are found
    }

    // Map roles into Spring Security's GrantedAuthority format
    List<GrantedAuthority> authorities = roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())) // Spring expects roles to be prefixed with "ROLE_"
            .collect(Collectors.toList());

    // Return an OAuth2User with the granted authorities (roles) and attributes
    return new DefaultOAuth2User(
            authorities,          // The authorities (roles) for this user
            attributes,           // The attributes (e.g., email, profile info)
            "sub"                 // The primary claim for the user identifier (typically "sub" for OAuth2)
    );
  }

}