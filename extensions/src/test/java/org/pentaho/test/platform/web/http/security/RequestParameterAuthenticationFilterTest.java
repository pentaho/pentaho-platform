/*!
 *
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
 *
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.web.http.security;

import com.hitachivantara.security.web.service.csrf.servlet.CsrfValidator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.web.http.security.RequestParameterAuthenticationFilter;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.web.http.security.RequestParameterAuthenticationFilter.CSRF_OPERATION_NAME;

public class RequestParameterAuthenticationFilterTest {

  private RequestParameterAuthenticationFilter filter;

  private AuthenticationManager authManagerMock;

  private AuthenticationEntryPoint authenticationEntryPointMock;

  private FilterChain mockChain;

  @Before
  public void beforeTest() throws KettleException, IOException {
    KettleClientEnvironment.init();
    filter = new RequestParameterAuthenticationFilter();
    authManagerMock = mock( AuthenticationManager.class );
    authenticationEntryPointMock = mock( AuthenticationEntryPoint.class );
    mockChain = mock( FilterChain.class );
    filter.setAuthenticationManager( authManagerMock );
    filter.setAuthenticationEntryPoint( authenticationEntryPointMock );

    final Properties properties = new Properties();
    properties.setProperty( "requestParameterAuthenticationEnabled", "true" );
    IConfiguration config = mock( IConfiguration.class );
    ISystemConfig mockISystemConfig = mock( ISystemConfig.class );
    mockISystemConfig.registerConfiguration( config );
    filter.setSystemConfig( mockISystemConfig );
    doReturn( config ).when( mockISystemConfig ).getConfiguration( "security" );
    doReturn( properties ).when( config ).getProperties();
  }

  @Test
  public void userNamePasswordEncrypted() throws IOException, ServletException {
    final MockHttpServletRequest request =
      new MockHttpServletRequest(
        "GET",
        "http://localhost:9080/pentaho-di/kettle/executeTrans/?rep=dev&userid=admin&password=Encrypted"
          + "%202be98afc86aa7f2e4bb18bd63c99dbdde&trans=/home/admin/Trans1" );
    request.addParameter( "userid", "admin" );
    request.addParameter( "password", "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde" );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );
    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken( "admin", "password" );
    authRequest.setDetails( new WebAuthenticationDetails( request ) );
    verify( authManagerMock ).authenticate( Mockito.eq( authRequest ) );

  }

  @Test
  public void userNamePasswordUnencrypted() throws IOException, ServletException {
    final MockHttpServletRequest request =
      new MockHttpServletRequest( "GET",
        "http://localhost:9080/pentaho-di/kettle/executeTrans/?rep=dev&userid=admin&password=password&trans=/home"
          + "/admin/Trans1" );
    request.addParameter( "userid", "admin" );
    request.addParameter( "password", "password" );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );
    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken( "admin", "password" );
    authRequest.setDetails( new WebAuthenticationDetails( request ) );
    verify( authManagerMock ).authenticate( Mockito.eq( authRequest ) );

  }


  @Test
  public void testSetCsrfValidatorRespectsIt() {

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );
    RequestParameterAuthenticationFilter filter = new RequestParameterAuthenticationFilter();

    // ---

    filter.setCsrfValidator( csrfValidatorMock );

    // ---

    assertSame( csrfValidatorMock, filter.getCsrfValidator() );
  }

  private void testWhenCsrfValidationFailsWithGivenExceptionThenRethrows( @NonNull Throwable validateError )
    throws ServletException, IOException {

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );

    final MockHttpServletRequest request =
      new MockHttpServletRequest( "GET",
        "http://localhost:9080/pentaho-di/kettle/executeTrans" );

    request.addParameter( "userid", "admin" );
    request.addParameter( "password", "password" );

    filter.setAuthenticationManager( authManagerMock );
    filter.setCsrfValidator( csrfValidatorMock );

    when(
      csrfValidatorMock.validateRequestOfMutationOperation(
        any( HttpServletRequest.class ),
        eq( filter.getClass() ),
        anyString() ) )
      .thenThrow( validateError );

    // ---

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

  }

  @Test
  public void testWhenCsrfValidationFailsWithAuthenticationExceptionThenDelegatesToAuthenticationEntryPoint()
    throws ServletException, IOException {

    final MockHttpServletRequest request =
      new MockHttpServletRequest( "GET",
        "http://localhost:9080/pentaho-di/kettle/executeTrans" );

    request.addParameter( "userid", "admin" );
    request.addParameter( "password", "password" );

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );

    AccessDeniedException error = mock( AccessDeniedException.class );

    when( csrfValidatorMock.validateRequestOfMutationOperation(
      any( HttpServletRequest.class ),
      eq( filter.getClass() ),
      anyString() ) )
      .thenThrow( error );

    filter.setAuthenticationManager( authManagerMock );
    filter.setCsrfValidator( csrfValidatorMock );

    // ---

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    // ---

    ArgumentCaptor<AuthenticationException> authErrorCaptor
      = ArgumentCaptor.forClass( AuthenticationException.class );
    verify( authenticationEntryPointMock, times( 1 ) )
      .commence(
        any( HttpServletRequest.class ),
        any( HttpServletResponse.class ),
        authErrorCaptor.capture() );

    assertSame( error, authErrorCaptor.getValue().getCause() );


  }

  @Test( expected = IOException.class )
  public void testWhenCsrfValidationFailsWithIOExceptionThenRethrows() throws ServletException, IOException {

    IOException error = mock( IOException.class );

    testWhenCsrfValidationFailsWithGivenExceptionThenRethrows( error );
  }

  @Test( expected = ServletException.class )
  public void testWhenCsrfValidationFailsWithServletExceptionThenRethrows() throws ServletException, IOException {

    ServletException error = mock( ServletException.class );

    testWhenCsrfValidationFailsWithGivenExceptionThenRethrows( error );
  }

  @Test
  public void testCsrfValidationIsCalledWithCorrectOperationId() throws ServletException, IOException {

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );
    final MockHttpServletRequest request =
      new MockHttpServletRequest( "GET",
        "http://localhost:9080/pentaho-di/kettle/executeTrans" );

    request.addParameter( "userid", "admin" );
    request.addParameter( "password", "password" );

    filter.setAuthenticationManager( authManagerMock );
    filter.setCsrfValidator( csrfValidatorMock );

    // ---

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    // ---

    verify( csrfValidatorMock, times( 1 ) )
      .validateRequestOfMutationOperation(
        any( HttpServletRequest.class ),
        eq( filter.getClass() ),
        eq( CSRF_OPERATION_NAME ) );
  }

  @Test
  public void testWhenCsrfValidationSucceedsThenDelegatesToFilterChain() throws ServletException, IOException {

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );

    final MockHttpServletRequest request =
      new MockHttpServletRequest( "GET",
        "http://localhost:9080/pentaho-di/kettle/executeTrans" );

    request.addParameter( "userid", "admin" );
    request.addParameter( "password", "password" );

    filter.setAuthenticationManager( authManagerMock );
    filter.setCsrfValidator( csrfValidatorMock );
    filter.setIgnoreFailure( true );

    AccessDeniedException error = mock( AccessDeniedException.class );

    when( csrfValidatorMock.validateRequestOfMutationOperation(
      any( HttpServletRequest.class ),
      eq( filter.getClass() ),
      anyString() ) )
      .thenThrow( error );

    // ---

    filter.doFilter( request, new MockHttpServletResponse(), mockChain );

    // ---

    verify( mockChain, times( 1 ) ).doFilter( any( HttpServletRequest.class ),
      any( HttpServletResponse.class ) );
  }
}
