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

import org.junit.Test;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class Http401UnauthorizedEntryPointTest {

  @Test
  public void test_commence_shouldSend401WithoutWwwAuthenticateHeader() throws IOException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    AuthenticationException authException = mock( AuthenticationException.class );

    Http401UnauthorizedEntryPoint entryPoint = new Http401UnauthorizedEntryPoint();
    entryPoint.commence( request, response, authException );

    verify( response ).sendError( 401, "Unauthorized" );
    verify( response, never() ).addHeader( eq( "WWW-Authenticate" ), anyString() );
  }
}
