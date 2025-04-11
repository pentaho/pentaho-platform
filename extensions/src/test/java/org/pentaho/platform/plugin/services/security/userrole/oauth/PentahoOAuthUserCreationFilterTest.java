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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PentahoOAuthUserCreationFilterTest {

    @Mock
    private PentahoOAuthUserRoleService userRoleListService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private ServletRequest request;

    @Mock
    private ServletResponse response;

    @Test
    public void createsUserWhenOAuthEnabledAndAuthenticationIsOAuth2Token() throws Exception {
        Authentication authentication = mock(OAuth2AuthenticationToken.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        PentahoOAuthUserCreationFilter pentahoOAuthUserCreationFilter = new PentahoOAuthUserCreationFilter(userRoleListService);
        try (MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic(PentahoOAuthUtility.class)) {
            pentahoOAuthUtility.when(PentahoOAuthUtility::isOAuthEnabled).thenReturn(true);
            pentahoOAuthUserCreationFilter.doFilter(request, response, filterChain);

            verify(userRoleListService).createUser((OAuth2AuthenticationToken) authentication);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    public void doesNotCreateUserWhenOAuthDisabled() throws Exception {
        Authentication authentication = mock(OAuth2AuthenticationToken.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        PentahoOAuthUserCreationFilter pentahoOAuthUserCreationFilter = new PentahoOAuthUserCreationFilter(userRoleListService);
        try (MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic(PentahoOAuthUtility.class)) {
            pentahoOAuthUtility.when(PentahoOAuthUtility::isOAuthEnabled).thenReturn(false);
            pentahoOAuthUserCreationFilter.doFilter(request, response, filterChain);

            verifyNoInteractions(userRoleListService);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    public void doesNotCreateUserWhenAuthenticationIsNotOAuth2Token() throws Exception {
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        PentahoOAuthUserCreationFilter pentahoOAuthUserCreationFilter = new PentahoOAuthUserCreationFilter(userRoleListService);
        try (MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic(PentahoOAuthUtility.class)) {
            pentahoOAuthUtility.when(PentahoOAuthUtility::isOAuthEnabled).thenReturn(true);
            pentahoOAuthUserCreationFilter.doFilter(request, response, filterChain);

            verifyNoInteractions(userRoleListService);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    public void proceedsWithFilterChainWhenNoAuthenticationPresent() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);

        PentahoOAuthUserCreationFilter pentahoOAuthUserCreationFilter = new PentahoOAuthUserCreationFilter(userRoleListService);
        try (MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic(PentahoOAuthUtility.class)) {
            pentahoOAuthUtility.when(PentahoOAuthUtility::isOAuthEnabled).thenReturn(true);
            pentahoOAuthUserCreationFilter.doFilter(request, response, filterChain);

            verifyNoInteractions(userRoleListService);
            verify(filterChain).doFilter(request, response);
        }
    }
}
