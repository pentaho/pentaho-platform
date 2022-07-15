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
 * Copyright (c) 2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import com.hitachivantara.security.web.impl.service.util.MultiReadHttpServletRequestWrapper;
import com.hitachivantara.security.web.service.csrf.servlet.CsrfValidator;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IServiceOperationAwareContentGenerator;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GenericServletTest {
  private static final String SAMPLE_CONTENT_GENERATOR_ID = "sample-content-generator-id";
  private static final String SAMPLE_CONTENT_GENERATOR_CMD = "/sample-cmd/a/b";
  private static final String SAMPLE_CUSTOM_OPERATION_NAME = "sample-operation-name";
  private static final String SAMPLE_PATH_INFO = "/" + SAMPLE_CONTENT_GENERATOR_ID + SAMPLE_CONTENT_GENERATOR_CMD;
  private static final String SAMPLE_PATH_INFO_NO_CMD = "/" + SAMPLE_CONTENT_GENERATOR_ID;
  private static final String SAMPLE_PLUGIN_ID = "sample-plugin-id";
  private static final String SAMPLE_CONTEXT_PATH = "/pentaho/";
  private GenericServlet servletSpy;
  private CsrfValidator csrfValidatorMock;
  private HttpServletRequest requestMock;
  private HttpServletRequestWrapper requestWrapperMock;
  private HttpServletResponse responseMock;
  private IPentahoSession sessionMock;
  private MockedStatic<PentahoSystem> pentahoSystemMockedStatic;
  private IPluginManager pluginManagerMock;
  private IContentGenerator contentGeneratorMock;
  private ServletOutputStream outputStreamMock;
  private MockedStatic<PentahoRequestContextHolder> pentahoRequestContextHolderMockedStatic;
  private MockedStatic<Messages> messagesMockedStatic;
  private Messages messagesMock;
  private IPentahoRequestContext requestContextMock;
  private MockedStatic<MultiReadHttpServletRequestWrapper> multiReadHttpServletRequestWrapperMockedStatic;

  @Before
  public void beforeEach() throws PluginBeanException, IOException, ServletException {
    sessionMock = mock( IPentahoSession.class );
    contentGeneratorMock = mock( IContentGenerator.class );
    outputStreamMock = mock( ServletOutputStream.class );

    requestMock = mock( HttpServletRequest.class );
    when( requestMock.getPathInfo() ).thenReturn( SAMPLE_PATH_INFO );

    requestWrapperMock = mock( HttpServletRequestWrapper.class );
    when( requestWrapperMock.getHeaderNames() ).thenReturn( Collections.emptyEnumeration() );

    multiReadHttpServletRequestWrapperMockedStatic = mockStatic( MultiReadHttpServletRequestWrapper.class );
    multiReadHttpServletRequestWrapperMockedStatic
      .when( () -> MultiReadHttpServletRequestWrapper.wrap( requestMock ) )
      .thenReturn( requestWrapperMock );

    csrfValidatorMock = mock( CsrfValidator.class );
    when( csrfValidatorMock.validateRequestOfOperation( eq( requestWrapperMock ), any(), any() ) )
      .thenReturn( requestWrapperMock );

    responseMock = mock( HttpServletResponse.class );
    when( responseMock.getOutputStream() ).thenReturn( outputStreamMock );

    requestContextMock = mock( IPentahoRequestContext.class );
    when( requestContextMock.getContextPath() ).thenReturn( SAMPLE_CONTEXT_PATH );

    pluginManagerMock = mock( IPluginManager.class );
    when( pluginManagerMock.getServicePlugin( any() ) ).thenReturn( SAMPLE_PLUGIN_ID );
    when( pluginManagerMock.isStaticResource( any() ) ).thenReturn( false );
    when( pluginManagerMock.getBean( SAMPLE_CONTENT_GENERATOR_ID ) ).thenReturn( contentGeneratorMock );

    pentahoSystemMockedStatic = mockStatic( PentahoSystem.class );
    pentahoSystemMockedStatic
      .when( () -> PentahoSystem.get( eq( IPluginManager.class ), any() ) )
      .thenReturn( pluginManagerMock );

    pentahoRequestContextHolderMockedStatic = mockStatic( PentahoRequestContextHolder.class );
    pentahoRequestContextHolderMockedStatic
      .when( PentahoRequestContextHolder::getRequestContext )
      .thenReturn( requestContextMock );

    // The error message is the error message key.
    messagesMock = mock( Messages.class );
    when( messagesMock.getErrorString( any(), any() ) )
      .thenAnswer( (Answer<String>) invocationOnMock -> invocationOnMock.getArguments()[ 0 ].toString() );
    when( messagesMock.getErrorString( any() ) )
      .thenAnswer( (Answer<String>) invocationOnMock -> invocationOnMock.getArguments()[ 0 ].toString() );

    messagesMockedStatic = mockStatic( Messages.class );
    messagesMockedStatic
      .when( Messages::getInstance )
      .thenReturn( messagesMock );

    servletSpy = spy( new GenericServlet( csrfValidatorMock ) );
    doReturn( sessionMock ).when( servletSpy ).getPentahoSession( requestMock );
  }

  @After
  public void afterEach() {
    if ( pentahoSystemMockedStatic != null ) {
      pentahoSystemMockedStatic.close();
      pentahoSystemMockedStatic = null;
    }

    if ( pentahoRequestContextHolderMockedStatic != null ) {
      pentahoRequestContextHolderMockedStatic.close();
      pentahoRequestContextHolderMockedStatic = null;
    }

    if ( messagesMockedStatic != null ) {
      messagesMockedStatic.close();
      messagesMockedStatic = null;
    }

    if ( multiReadHttpServletRequestWrapperMockedStatic != null ) {
      multiReadHttpServletRequestWrapperMockedStatic.close();
      multiReadHttpServletRequestWrapperMockedStatic = null;
    }
  }

  private void verifyWriteErrorText( String errorKey ) throws IOException {
    verify( messagesMock, times( 1 ) ).getErrorString( eq( errorKey ), any() );
    verify( servletSpy, times( 1 ) ).error( any() );
    verify( outputStreamMock, times( 1 ) ).write( any() );
  }

  // region doGet/doPost
  @Test
  public void testDoGetWhenPathInfoIsEmptyThenRespondsWith403() throws ServletException, IOException {
    when( requestMock.getPathInfo() ).thenReturn( "" );
    servletSpy.doGet( requestMock, responseMock );
    verify( responseMock, times( 1 ) ).sendError( HttpStatus.SC_FORBIDDEN );

    clearInvocations( responseMock );

    when( requestMock.getPathInfo() ).thenReturn( null );
    servletSpy.doGet( requestMock, responseMock );
    verify( responseMock, times( 1 ) ).sendError( HttpStatus.SC_FORBIDDEN );
  }

  @Test
  public void testDoGetWhenPathInfoIsNotAStaticResourceThenIsHandledAsContentGenerator() throws Exception {

    servletSpy.doGet( requestMock, responseMock );

    verify( contentGeneratorMock, times( 1 ) ).createContent();
  }

  @Test
  public void testDoGetWhenContentGeneratorIsNotDefinedThenWritesErrorToOutput() throws Exception {

    when( pluginManagerMock.getBean( SAMPLE_CONTENT_GENERATOR_ID ) ).thenReturn( null );

    servletSpy.doGet( requestMock, responseMock );

    verify( contentGeneratorMock, never() ).createContent();

    verifyWriteErrorText( "GenericServlet.ERROR_0002_BAD_GENERATOR" );
  }

  @Test
  public void testDoGetWhenPathInfoHasNoCmdCorrectlyIdentifiesContentGeneratorId() throws Exception {

    when( requestMock.getPathInfo() ).thenReturn( SAMPLE_PATH_INFO_NO_CMD );

    servletSpy.doGet( requestMock, responseMock );

    verify( pluginManagerMock, times( 1 ) ).getBean( SAMPLE_CONTENT_GENERATOR_ID );

    verify( contentGeneratorMock, times( 1 ) ).createContent();
  }

  // region CSRF validation
  @Test
  public void testDoGetWhenContentGeneratorAndCsrfValidationFailsThenSendsErrorForbidden() throws Exception {

    AccessDeniedException accessDeniedException = mock( AccessDeniedException.class );
    when( csrfValidatorMock.validateRequestOfOperation( eq( requestWrapperMock ), any(), any() ) )
      .thenThrow( accessDeniedException );

    servletSpy.doGet( requestMock, responseMock );

    verify( responseMock, times( 1 ) ).sendError( HttpStatus.SC_FORBIDDEN );
    verify( contentGeneratorMock, never() ).createContent();
  }

  @Test
  public void testDoGetWhenContentGeneratorIsNotServiceOperationAwareUsesCmdWithoutSlashAsServiceOperationName()
    throws Exception {

    servletSpy.doGet( requestMock, responseMock );

    verify( csrfValidatorMock, times( 1 ) )
      .validateRequestOfOperation( eq( requestWrapperMock ), any(), eq( SAMPLE_CONTENT_GENERATOR_CMD.substring( 1 ) ) );

    verify( contentGeneratorMock, times( 1 ) ).createContent();
  }

  @Test
  public void testDoGetWhenContentGeneratorIsNotServiceOperationAwareAndNullCmdUsesNullServiceOperationName()
    throws Exception {

    when( requestMock.getPathInfo() ).thenReturn( SAMPLE_PATH_INFO_NO_CMD );

    servletSpy.doGet( requestMock, responseMock );

    verify( csrfValidatorMock, times( 1 ) )
      .validateRequestOfOperation( eq( requestWrapperMock ), any(), eq( null ) );

    verify( contentGeneratorMock, times( 1 ) ).createContent();
  }

  @Test
  public void testDoGetWhenContentGeneratorIsServiceOperationAwareGetsServiceOperationName() throws Exception {
    contentGeneratorMock = mock( IServiceOperationAwareContentGenerator.class );
    when( ( (IServiceOperationAwareContentGenerator) contentGeneratorMock ).getServiceOperationName() )
      .thenReturn( SAMPLE_CUSTOM_OPERATION_NAME );

    when( pluginManagerMock.getBean( SAMPLE_CONTENT_GENERATOR_ID ) ).thenReturn( contentGeneratorMock );

    servletSpy.doGet( requestMock, responseMock );

    verify( csrfValidatorMock, times( 1 ) )
      .validateRequestOfOperation( eq( requestWrapperMock ), any(), eq( SAMPLE_CUSTOM_OPERATION_NAME ) );

    verify( contentGeneratorMock, times( 1 ) ).createContent();
  }
  // endregion

  // endregion
}
