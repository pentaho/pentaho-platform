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

import org.apache.commons.collections4.CollectionUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PentahoOAuthLoginAuthenticationFilter extends OAuth2LoginAuthenticationFilter {

  private IAuthenticationRoleMapper roleMapper;

  public PentahoOAuthLoginAuthenticationFilter( ClientRegistrationRepository clientRegistrationRepository,
                                                OAuth2AuthorizedClientService authorizedClientService,
                                                IAuthenticationRoleMapper roleMapper ) {
    super( clientRegistrationRepository, authorizedClientService );
    this.roleMapper = roleMapper;
  }

  public PentahoOAuthLoginAuthenticationFilter( ClientRegistrationRepository clientRegistrationRepository,
                                                OAuth2AuthorizedClientService authorizedClientService,
                                                String filterProcessesUrl,
                                                IAuthenticationRoleMapper roleMapper ) {
    super( clientRegistrationRepository, authorizedClientService, filterProcessesUrl );
    this.roleMapper = roleMapper;
  }

  public PentahoOAuthLoginAuthenticationFilter( ClientRegistrationRepository clientRegistrationRepository,
                                                OAuth2AuthorizedClientRepository authorizedClientRepository,
                                                String filterProcessesUrl,
                                                IAuthenticationRoleMapper roleMapper ) {
    super( clientRegistrationRepository, authorizedClientRepository, filterProcessesUrl );
    this.roleMapper = roleMapper;
  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain )
    throws IOException, ServletException {

    if ( PentahoOAuthUtility.isOAuthEnabled()
      && !PentahoOAuthUtility.isUserNamePasswordAuthentication( (HttpServletRequest) servletRequest ) ) {
      super.doFilter( servletRequest, servletResponse, chain );
    } else {
      chain.doFilter( servletRequest, servletResponse );
    }
  }

  @Override
  public Authentication attemptAuthentication( HttpServletRequest request, HttpServletResponse response )
    throws AuthenticationException {
    Authentication authentication = super.attemptAuthentication( request, response );
    if ( authentication instanceof OAuth2AuthenticationToken ) {
      IPentahoSession pentahoSession = PentahoSessionHolder.getSession();

      OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
      Set<GrantedAuthority> pentahoAuthorities = new HashSet<>();

      List<String> oauthRoles = oAuth2AuthenticationToken.getPrincipal().getAttribute( "roles" );
      if ( CollectionUtils.isNotEmpty( oauthRoles ) ) {
        pentahoAuthorities.add( new SimpleGrantedAuthority( "Authenticated" ) );
        oauthRoles.forEach( role ->
          pentahoAuthorities.add( new SimpleGrantedAuthority( roleMapper.toPentahoRole( role ) ) ) );
      }

      Collection<GrantedAuthority> oauthAuthorities = oAuth2AuthenticationToken.getAuthorities();
      if ( CollectionUtils.isNotEmpty( oauthAuthorities ) ) {
        pentahoAuthorities.add( new SimpleGrantedAuthority( "Authenticated" ) );
      }

      pentahoSession.setAttribute( IPentahoSession.SESSION_ROLES, pentahoAuthorities );

      return new OAuth2AuthenticationToken(
        oAuth2AuthenticationToken.getPrincipal(),
        pentahoAuthorities,
        oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
      );
    }
    return authentication;
  }

}
