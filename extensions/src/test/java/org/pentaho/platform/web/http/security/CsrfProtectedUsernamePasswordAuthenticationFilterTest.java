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
 * Copyright (c) 2022 Hitachi Vantara. All rights reserved.
 */
package org.pentaho.platform.web.http.security;

import com.hitachivantara.security.web.service.csrf.servlet.CsrfValidator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.web.http.security.CsrfProtectedUsernamePasswordAuthenticationFilter.CSRF_OPERATION_NAME;

public class CsrfProtectedUsernamePasswordAuthenticationFilterTest {

  private HttpServletRequest requestMock;
  private HttpServletResponse responseMock;
  private AuthenticationManager authenticationManagerMock;

  @Before
  public void setUp() {
    authenticationManagerMock = mock( AuthenticationManager.class );
  }

  @Test
  public void testConstructorWithNoArgumentsHasNoCsrfValidator() {

    CsrfProtectedUsernamePasswordAuthenticationFilter filter = new CsrfProtectedUsernamePasswordAuthenticationFilter();

    // ---

    assertNull( filter.getCsrfValidator() );
  }

  @Test
  public void testConstructorWithGivenCsrfValidatorRespectsIt() {

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );

    // ---

    CsrfProtectedUsernamePasswordAuthenticationFilter filter
      = new CsrfProtectedUsernamePasswordAuthenticationFilter( csrfValidatorMock );

    // ---

    assertSame( csrfValidatorMock, filter.getCsrfValidator() );
  }

  @Test
  public void testSetCsrfValidatorRespectsIt() {

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );
    CsrfProtectedUsernamePasswordAuthenticationFilter filter = new CsrfProtectedUsernamePasswordAuthenticationFilter();

    // ---

    filter.setCsrfValidator( csrfValidatorMock );

    // ---

    assertSame( csrfValidatorMock, filter.getCsrfValidator() );
  }

  @Test
  public void testAttemptAuthenticationWithNoCsrfValidatorDelegatesToBaseClass() {

    requestMock = mock( HttpServletRequest.class );
    when( requestMock.getMethod() ).thenReturn( HttpMethod.POST );

    responseMock = mock( HttpServletResponse.class );

    CsrfProtectedUsernamePasswordAuthenticationFilter filter = new CsrfProtectedUsernamePasswordAuthenticationFilter();
    filter.setAuthenticationManager( authenticationManagerMock );

    // ---

    filter.attemptAuthentication( requestMock, responseMock );

    // ---

    verify( authenticationManagerMock, times( 1 ) ).authenticate( any() );
  }

  private void testAttemptAuthenticationFailsThenThrowsCsrfValidationAuthException( @NonNull Throwable validateError )
    throws ServletException, IOException {

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );

    requestMock = mock( HttpServletRequest.class );
    when( requestMock.getMethod() ).thenReturn( HttpMethod.POST );

    responseMock = mock( HttpServletResponse.class );

    CsrfProtectedUsernamePasswordAuthenticationFilter filter = new CsrfProtectedUsernamePasswordAuthenticationFilter();
    filter.setAuthenticationManager( authenticationManagerMock );
    filter.setCsrfValidator( csrfValidatorMock );

    when(
      csrfValidatorMock.validateRequestOfMutationOperation( eq( requestMock ), eq( filter.getClass() ), anyString() ) )
      .thenThrow( validateError );

    // ---

    filter.attemptAuthentication( requestMock, responseMock );
  }

  @Test
  public void testAttemptAuthenticationWhichThrowsAccessDeniedExceptionThenRethrowsAsCsrfValidationAuthException()
    throws ServletException, IOException {

    AccessDeniedException error = mock( AccessDeniedException.class );

    try {
      testAttemptAuthenticationFailsThenThrowsCsrfValidationAuthException( error );
      fail( "Should have thrown exception" );
    } catch ( CsrfValidationAuthenticationException ex ) {
      assertSame( error, ex.getCause() );
    }
  }

  @Test
  public void testAttemptAuthenticationWhichThrowsIOExceptionThenRethrowsAsInternalAuthenticationServiceException()
    throws ServletException, IOException {

    IOException error = mock( IOException.class );

    try {
      testAttemptAuthenticationFailsThenThrowsCsrfValidationAuthException( error );
      fail( "Should have thrown exception" );
    } catch ( InternalAuthenticationServiceException ex ) {
      assertSame( error, ex.getCause() );
    }
  }

  @Test
  public void testAttemptAuthenticationWhichThrowsServletExceptionThenRethrowsAsInternalAuthenticationServiceException()
    throws ServletException, IOException {

    ServletException error = mock( ServletException.class );

    try {
      testAttemptAuthenticationFailsThenThrowsCsrfValidationAuthException( error );
      fail( "Should have thrown exception" );
    } catch ( InternalAuthenticationServiceException ex ) {
      assertSame( error, ex.getCause() );
    }
  }

  @Test
  public void testAttemptAuthenticationIsCalledWithCorrectOperationName() throws ServletException, IOException {

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );

    requestMock = mock( HttpServletRequest.class );
    when( requestMock.getMethod() ).thenReturn( HttpMethod.POST );

    responseMock = mock( HttpServletResponse.class );

    CsrfProtectedUsernamePasswordAuthenticationFilter filter = new CsrfProtectedUsernamePasswordAuthenticationFilter();
    filter.setAuthenticationManager( authenticationManagerMock );
    filter.setCsrfValidator( csrfValidatorMock );

    // ---

    filter.attemptAuthentication( requestMock, responseMock );

    // ---

    verify( csrfValidatorMock, times( 1 ) )
      .validateRequestOfMutationOperation( requestMock, filter.getClass(), CSRF_OPERATION_NAME );
  }

  @Test
  public void testAttemptAuthenticationWhichPassesCsrfValidationDelegatesToBaseClass() {

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );

    requestMock = mock( HttpServletRequest.class );
    when( requestMock.getMethod() ).thenReturn( HttpMethod.POST );

    responseMock = mock( HttpServletResponse.class );

    CsrfProtectedUsernamePasswordAuthenticationFilter filter = new CsrfProtectedUsernamePasswordAuthenticationFilter();
    filter.setAuthenticationManager( authenticationManagerMock );
    filter.setCsrfValidator( csrfValidatorMock );

    // ---

    filter.attemptAuthentication( requestMock, responseMock );

    // ---

    verify( authenticationManagerMock, times( 1 ) ).authenticate( any() );
  }
}
