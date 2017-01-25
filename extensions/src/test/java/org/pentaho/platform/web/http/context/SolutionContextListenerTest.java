/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.web.http.context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.test.platform.engine.core.SimpleObjectFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/28/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class SolutionContextListenerTest {

  SolutionContextListener solutionContextListener;

  @Mock ServletContextEvent contextEvent;
  @Mock ServletContext context;
  @Mock ISystemConfig systemConfig;
  @Mock SimpleObjectFactory objectFactory;

  @Before
  public void setUp() throws Exception {
    solutionContextListener = new SolutionContextListener();
    when( contextEvent.getServletContext() ).thenReturn( context );
    PentahoSystem.registerObject( systemConfig );
  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.clearObjectFactory();
  }

  @Test( expected = RuntimeException.class )
  public void testContextInitialized_invalidSolutionPath() throws Exception {
    when( systemConfig.getProperty( anyString() ) ).thenReturn( null );

    File tempFile = File.createTempFile( "SolutionContextListenerTest", ".tmp" );
    tempFile.deleteOnExit();

    when( context.getRealPath( "" ) ).thenReturn( tempFile.getParent() );

    solutionContextListener.contextInitialized( contextEvent );
  }

  @Test
  public void testContextInitialized() throws Exception {

    when( systemConfig.getProperty( "server.encoding" ) ).thenReturn( "UTF-8" );
    when( systemConfig.getProperty( "server.text-direction" ) ).thenReturn( "leftToRight" );

    when( systemConfig.getProperty( "server.locale-language" ) ).thenReturn( "en" );
    when( systemConfig.getProperty( "server.locale-country" ) ).thenReturn( "CA" );

    when( systemConfig.getProperty( "server.param 1" ) ).thenReturn( "value 1" );

    when( systemConfig.getProperty( "server.pentahoObjectFactory" ) ).thenReturn( objectFactory.getClass().getName() );

    Path temp = Files.createTempDirectory( "temp" );
    File penSolution = new File( temp.toFile(), "pentaho-solutions/SolutionContextListenerTest.tmp" );
    penSolution.mkdirs();
    penSolution.deleteOnExit();
    when( context.getRealPath( "" ) ).thenReturn( temp.toString() );
    temp.toFile().deleteOnExit();

    List<String> paramNames = new ArrayList<>();
    paramNames.add( "param 1" );

    when( context.getInitParameterNames() ).thenReturn( Collections.enumeration( paramNames ) );

    solutionContextListener.contextInitialized( contextEvent );
    assertEquals( "en_CA", LocaleHelper.getLocale().toString() );
  }

  @Test
  public void testCreateWebApplicationContext() throws Exception {
    WebApplicationContext webApplicationContext =
      solutionContextListener.createWebApplicationContext( "http://localhost:8080/pentaho", context );
    assertNotNull( webApplicationContext );
  }

  @Test
  public void testShowInitializationMessage() throws Exception {
    PentahoSystem.registerObjectFactory( objectFactory );
    IVersionHelper versionHelper = mock( IVersionHelper.class );
    PentahoSystem.registerObject( versionHelper );
    when( versionHelper.getVersionInformation( PentahoSystem.class ) ).thenReturn( "version info" );

    when( objectFactory.objectDefined( IVersionHelper.class.getSimpleName() ) ).thenReturn( true );

    solutionContextListener.solutionPath = "solution-path";
    // it just prints out to standard out, nothing to verify

    ByteArrayOutputStream captureStdOut = new ByteArrayOutputStream();
    ByteArrayOutputStream captureStdErr = new ByteArrayOutputStream();

    System.setOut( new PrintStream( captureStdOut ) );
    System.setErr( new PrintStream( captureStdErr ) );

    solutionContextListener.showInitializationMessage( true, "http://localhost:8080/pentaho" );
    assertTrue( captureStdOut.toString().contains( "Pentaho BI Platform server is ready. (version info) Fully Qualified Server Url = "
        + "http://localhost:8080/pentaho, Solution Path = solution-path" ) );

    solutionContextListener.showInitializationMessage( false, "http://localhost:8080/pentaho" );
    assertTrue( captureStdErr.toString().contains( "Pentaho BI Platform server failed to properly initialize. The system will not be available for requests. "
        + "(version info) Fully Qualified Server Url = http://localhost:8080/pentaho, Solution Path = solution-path" ) );

  }

  @Test
  public void testGetContextPath() throws Exception {
    assertEquals( SolutionContextListener.contextPath, solutionContextListener.getContextPath() );
  }

  @Test
  public void testGetRootPath() throws Exception {
    assertEquals( SolutionContextListener.solutionPath, solutionContextListener.getRootPath() );
  }

  @Test
  public void testContextDestroyed() throws Exception {
    solutionContextListener.contextDestroyed( contextEvent );
    assertEquals( PentahoSystem.SYSTEM_NOT_INITIALIZED, PentahoSystem.getInitializedStatus() );
  }
}