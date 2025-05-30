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


package org.pentaho.platform.web.http.security;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.web.http.security.UserNavigationAwareAuthenticationEntryPoint.HEADER_SEC_FETCH_DEST;
import static org.pentaho.platform.web.http.security.UserNavigationAwareAuthenticationEntryPoint.HEADER_SEC_FETCH_MODE;
import static org.pentaho.platform.web.http.security.UserNavigationAwareAuthenticationEntryPoint.HEADER_SEC_FETCH_SITE;
import static org.pentaho.platform.web.http.security.UserNavigationAwareAuthenticationEntryPoint.HEADER_SEC_FETCH_USER;
import static org.pentaho.platform.web.http.security.UserNavigationAwareAuthenticationEntryPoint.HEADER_SF_TRUE;

public class UserNavigationAwareAuthenticationEntryPointTest {

  private static final String HEADER_SF_FALSE = "?0";

  private AuthenticationEntryPoint userEntryPoint;
  private AuthenticationEntryPoint apiEntryPoint;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private AuthenticationException authException;

  @Before
  public void setUp() {
    userEntryPoint = mock( AuthenticationEntryPoint.class );
    apiEntryPoint = mock( AuthenticationEntryPoint.class );
    request = mock( HttpServletRequest.class );
    response = mock( HttpServletResponse.class );
    authException = mock( AuthenticationException.class );
  }

  private void expectEntryPoint(
    String secFetchUser,
    String secFetchDest,
    String secFetchMode,
    String secFetchSite,
    boolean expectUserEntryPoint
  ) throws ServletException, IOException {
    when( request.getHeader( HEADER_SEC_FETCH_USER ) ).thenReturn( secFetchUser );
    when( request.getHeader( HEADER_SEC_FETCH_DEST ) ).thenReturn( secFetchDest );
    when( request.getHeader( HEADER_SEC_FETCH_MODE ) ).thenReturn( secFetchMode );
    when( request.getHeader( HEADER_SEC_FETCH_SITE ) ).thenReturn( secFetchSite );

    var entryPoint = new UserNavigationAwareAuthenticationEntryPoint( userEntryPoint, apiEntryPoint );
    entryPoint.commence( request, response, authException );

    if ( expectUserEntryPoint ) {
      verify( userEntryPoint ).commence( request, response, authException );
      verifyNoInteractions( apiEntryPoint );
    } else {
      verify( apiEntryPoint ).commence( request, response, authException );
      verifyNoInteractions( userEntryPoint );
    }

    clearInvocations( apiEntryPoint, userEntryPoint );
  }

  @Test
  public void testWhenNoSecFetchHeadersArePresentDelegatesToAPIEntryPoint()
    throws ServletException, IOException {
    expectEntryPoint( null, null, null, null, false );
  }

  @Test
  public void testWhenSecFetchUserHeaderIsPresentWithFalseValueAndNoOtherHeadersPresentDelegatesToAPIEntryPoint()
    throws ServletException, IOException {
    expectEntryPoint( HEADER_SF_FALSE, null, null, null, false );
  }

  @Test
  public void testWhenSecFetchUserHeaderIsPresentWithTrueValueDelegatesToUserEntryPoint()
    throws ServletException, IOException {
    expectEntryPoint( HEADER_SF_TRUE, null, null, null, true );
  }

  @Test
  public void testOtherUserNavigationCasesWhenSecFetchUserIsNotPresentDelegatesToUserEntryPoint()
    throws ServletException, IOException {
    expectEntryPoint( null, "document", "navigate", "same-origin", true );
    expectEntryPoint( null, "iframe", "navigate", "none", true );
  }

  @Test
  public void testOtherNotUserNavigationCasesWhenSecFetchUserIsNotPresentDelegatesToAPIEntryPoint()
    throws ServletException, IOException {
    expectEntryPoint( null, "image", "navigate", "same-origin", false );
    expectEntryPoint( null, "document", null, "same-origin", false );
    expectEntryPoint( null, "document", "cors", "same-origin", false );
    expectEntryPoint( null, "document", "navigate", "cross-site", false );
  }
}
