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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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

  // write the unit tests for the below cases
  @Test
  public void testWhenSecFetchUserHeaderIsAbsentDelegatesToAPIEntryPoint() throws ServletException, IOException {
    var entryPoint = new UserNavigationAwareAuthenticationEntryPoint( userEntryPoint, apiEntryPoint );

    when( request.getHeader( HEADER_SEC_FETCH_USER ) ).thenReturn( null );

    entryPoint.commence( request, response, authException );

    verify( apiEntryPoint ).commence( request, response, authException );
    verifyNoInteractions( userEntryPoint );
  }

  @Test
  public void testWhenSecFetchUserHeaderIsPresentWithFalseValueDelegatesToAPIEntryPoint()
    throws ServletException, IOException {
    var entryPoint = new UserNavigationAwareAuthenticationEntryPoint( userEntryPoint, apiEntryPoint );

    when( request.getHeader( HEADER_SEC_FETCH_USER ) ).thenReturn( HEADER_SF_FALSE );

    entryPoint.commence( request, response, authException );

    verify( apiEntryPoint ).commence( request, response, authException );
    verifyNoInteractions( userEntryPoint );
  }

  @Test
  public void testWhenSecFetchUserHeaderIsPresentWithTrueValueDelegatesToUserEntryPoint()
    throws ServletException, IOException {
    var entryPoint = new UserNavigationAwareAuthenticationEntryPoint( userEntryPoint, apiEntryPoint );

    when( request.getHeader( HEADER_SEC_FETCH_USER ) ).thenReturn( HEADER_SF_TRUE );

    entryPoint.commence( request, response, authException );

    verify( userEntryPoint ).commence( request, response, authException );
    verifyNoInteractions( apiEntryPoint );
  }
}
