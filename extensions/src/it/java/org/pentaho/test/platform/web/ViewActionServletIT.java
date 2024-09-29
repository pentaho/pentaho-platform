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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.web;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.scheduler.BackgroundExecutionException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.web.servlet.ViewAction;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.utils.TestResourceLocation;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.ViewAction</code>.
 * 
 * @author mlowery
 */
public class ViewActionServletIT {

  private HttpServletRequest request;
  private HttpServletResponse response;
  private ViewAction servlet;
  private final MicroPlatform mp = new MicroPlatform( TestResourceLocation.TEST_RESOURCES + "/web-servlet-solution" );

  @Before
  public void setUp() throws PlatformInitializationException {
    request = mock( HttpServletRequest.class );
    when( request.getMethod() ).thenReturn( "GET" );

    response = mock( HttpServletResponse.class );

    servlet = spy( new ViewAction() );

    mp.defineInstance( IUnifiedRepository.class, mock( IUnifiedRepository.class ) );
    mp.start();
    PentahoSessionHolder.setSession( new StandaloneSession( "test" ) );
  }

  /**
   * This test covers execution without background parameter. In that case <code>servlet.error()</code>
   * wouldn't be executed.
   *
   * @throws ServletException
   * @throws IOException
   */
  @Test
  public void testBackgroundExecutionNoExecutor() throws ServletException, IOException {
    when( request.getParameter( eq( "background" ) ) ).thenReturn( Boolean.TRUE.toString() );
    final String actionPath = "test-path";
    when( request.getParameter( eq( "path" ) ) ).thenReturn( actionPath );
    final String instanceID = "instance-id";
    when( request.getParameter( eq( "instance-id" ) ) ).thenReturn( instanceID );


    final ServletOutputStream outputStream = mock( ServletOutputStream.class );
    when( response.getOutputStream() ).thenReturn( outputStream );

    final ISolutionEngine solutionEngine = mock( ISolutionEngine.class );
    final IRuntimeContext runtime = mock( IRuntimeContext.class );
    when( runtime.getStatus() ).thenReturn( IRuntimeContext.RUNTIME_STATUS_SUCCESS );
    when( solutionEngine.execute( eq( actionPath ), eq( servlet.getClass().getName() ), eq( false ), eq( true ),
      eq( instanceID ), eq( true ), any( Map.class ), any( IOutputHandler.class ), any( IActionCompleteListener.class ),
      any( IPentahoUrlFactory.class ), any( List.class ) ) ).thenReturn( runtime );
    mp.defineInstance( ISolutionEngine.class, solutionEngine );
    final IMessageFormatter messageFormatter = mock( IMessageFormatter.class );
    mp.defineInstance( IMessageFormatter.class, messageFormatter );

    servlet.service( request, response );

    verify( servlet ).error( matches( ".*ERROR_0001.*" ) );
    verify( outputStream ).write( any( byte[].class ) );
    verify( messageFormatter ).formatSuccessMessage( eq( "text/html" ), eq( runtime ), any( StringBuffer.class ),
      anyBoolean(), anyBoolean() );
  }

  @Test
  public void testBackgroundExecution() throws ServletException, IOException, BackgroundExecutionException {
    when( request.getParameter( eq( "background" ) ) ).thenReturn( Boolean.TRUE.toString() );

    final PrintWriter printWriter = mock( PrintWriter.class );
    when( response.getWriter() ).thenReturn( printWriter );

    final IBackgroundExecution backgroundExecution = mock( IBackgroundExecution.class );
    mp.defineInstance( IBackgroundExecution.class, backgroundExecution );

    servlet.service( request, response );

    verify( backgroundExecution ).backgroundExecuteAction( eq( PentahoSessionHolder.getSession() ),
      any( IParameterProvider.class ) );
    verify( printWriter, atLeast( 1 ) ).print( nullable( String.class ) );
    verify( response ).setHeader( eq( "background_execution" ), eq( "true" ) );
  }

}
