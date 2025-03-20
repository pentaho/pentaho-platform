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
import org.pentaho.platform.engine.core.system.PentahoSystem;
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

/**
 * PentahoOAuthLoginAuthenticationFilter is a custom filter that extends the OAuth2LoginAuthenticationFilter.
 * It is used to handle OAuth2 login authentication and set the roles in the PentahoSession.
 */
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

  /**
   * This method is used to check if the request is an OAuth2 request and if so, it will call the
   * super.doFilter method. If not, it will call the chain.doFilter method.
   *
   * @param servletRequest  The request object
   * @param servletResponse The response object
   * @param chain          The filter chain
   */
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

  /**
   * This method is used to attempt authentication and convert the OAuth2AuthenticationToken to a
   * PentahoOAuthAuthenticationToken and set the roles in the PentahoSession.
   *
   * @param request  The request object
   * @param response The response object
   * @return The authentication object
   */
  @Override
  public Authentication attemptAuthentication( HttpServletRequest request, HttpServletResponse response )
    throws AuthenticationException {
    Authentication authentication = super.attemptAuthentication( request, response );
    if ( authentication instanceof OAuth2AuthenticationToken ) {
      return getOAuth2AuthenticationToken( authentication );
    }
    return authentication;
  }

  /**
   * This method is used to convert the OAuth2AuthenticationToken to a PentahoOAuthAuthenticationToken
   * and set the roles in the PentahoSession.
   *
   * @param authentication The authentication object
   * @return The OAuth2AuthenticationToken
   */
  public OAuth2AuthenticationToken getOAuth2AuthenticationToken( Authentication authentication ) {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();

    OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
    Set<GrantedAuthority> pentahoAuthorities = new HashSet<>();

    List<String> oauthRoles = oAuth2AuthenticationToken.getPrincipal().getAttribute( "roles" );
    if ( CollectionUtils.isNotEmpty( oauthRoles ) ) {
      pentahoAuthorities.add( new SimpleGrantedAuthority(
        PentahoSystem.get( String.class, "singleTenantAuthenticatedAuthorityName", null ) ) );
      oauthRoles.forEach( role ->
        pentahoAuthorities.add( new SimpleGrantedAuthority( roleMapper.toPentahoRole( role ) ) ) );
    }

    Collection<GrantedAuthority> oauthAuthorities = oAuth2AuthenticationToken.getAuthorities();
    if ( CollectionUtils.isNotEmpty( oauthAuthorities ) ) {
      pentahoAuthorities.add( new SimpleGrantedAuthority(
        PentahoSystem.get( String.class, "singleTenantAuthenticatedAuthorityName", null ) ) );
    }

    pentahoSession.setAttribute( IPentahoSession.SESSION_ROLES, pentahoAuthorities );

    return new OAuth2AuthenticationToken(
      oAuth2AuthenticationToken.getPrincipal(),
      pentahoAuthorities,
      oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
    );
  }

}
