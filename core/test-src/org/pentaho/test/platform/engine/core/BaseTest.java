/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.test.platform.engine.core;

import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.security.MockSecurityHelper;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Base class for platform integration tests. Uses Spring to populate required PentahoSystem dependencies (admin
 * plugins, system listeners, object factory, system settings, etc) and uses the
 * {@link StandaloneSpringPentahoObjectFactory} as the object factory implementation. Also inits the system.
 */
public class BaseTest extends GenericPentahoTest implements IActionCompleteListener, ILogger {

  protected static final boolean debug = PentahoSystem.debug;

  private int loggingLevel = ILogger.ERROR;

  private boolean initOk = false;

  private List<String> messages;
  final String SYSTEM_FOLDER = "/system"; //$NON-NLS-1$
  private IRuntimeContext context = null;

  public BaseTest( String arg0 ) {
    super( arg0 );
  }

  public BaseTest() {
    super();
  }

  public List<String> getMessages() {
    return messages;
  }

  public String getFullyQualifiedServerURL() {
    return "http://localhost:8080/pentaho/"; //$NON-NLS-1$
  }

  public void setUp() {

    // used by test repository impl such as FileSystemRepositoryFileDao
    System.setProperty( "solution.root.dir", getSolutionPath() );

    messages = TestManager.getMessagesList();
    if ( messages == null ) {
      messages = new ArrayList<String>();
    }

    if ( initOk ) {
      return;
    }

    PentahoSystem.setSystemSettingsService( new PathBasedSystemSettings() );

    if ( PentahoSystem.getApplicationContext() == null ) {
      StandaloneApplicationContext applicationContext = new StandaloneApplicationContext( getSolutionPath(), "" ); //$NON-NLS-1$
      // set the base url assuming there is a running server on port 8080
      applicationContext.setFullyQualifiedServerURL( getFullyQualifiedServerURL() );
      String inContainer = System.getProperty( "incontainer", "false" ); //$NON-NLS-1$ //$NON-NLS-2$
      if ( inContainer.equalsIgnoreCase( "false" ) ) { //$NON-NLS-1$
        // Setup simple-jndi for datasources
        System.setProperty( "java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory" ); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty( "org.osjava.sj.root", getSolutionPath() + "/system/simple-jndi" ); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty( "org.osjava.sj.delimiter", "/" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      ApplicationContext springApplicationContext = getSpringApplicationContext();

      IPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
      pentahoObjectFactory.init( null, springApplicationContext );
      PentahoSystem.registerObjectFactory( pentahoObjectFactory );

      // force Spring to inject PentahoSystem, there has got to be a better way than this, perhaps an alternate way
      // of
      // initting spring's app context
      springApplicationContext.getBean( "pentahoSystemProxy" ); //$NON-NLS-1$

      //Initialize SecurityHelper with a mock for testing
      SecurityHelper.setMockInstance(new MockSecurityHelper());

      initOk = PentahoSystem.init( applicationContext );
    } else {
      initOk = true;
    }

    assertTrue( Messages.getInstance().getString( "BaseTest.ERROR_0001_FAILED_INITIALIZATION" ), initOk ); //$NON-NLS-1$
  }

  private ApplicationContext getSpringApplicationContext() {
    //todo
    String[] fns =
    {
      "pentahoObjects.spring.xml", "adminPlugins.xml", "sessionStartupActions.xml", "systemListeners.xml", "pentahoSystemConfig.xml" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    GenericApplicationContext appCtx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader( appCtx );

    for ( String fn : fns ) {
      File f = new File( getSolutionPath() + SYSTEM_FOLDER + "/" + fn ); //$NON-NLS-1$
      if ( f.exists() ) {
        FileSystemResource fsr = new FileSystemResource( f );
        xmlReader.loadBeanDefinitions( fsr );
      }
    }

    String[] beanNames = appCtx.getBeanDefinitionNames();
    System.out.println( "Loaded Beans: " ); //$NON-NLS-1$
    for ( String n : beanNames ) {
      System.out.println( "bean: " + n ); //$NON-NLS-1$
    }
    return appCtx;
  }

  protected Map getRequiredListeners() {
    HashMap<String, String> listeners = new HashMap<String, String>();
    listeners.put( "globalObjects", "globalObjects" ); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  public void actionComplete( IRuntimeContext runtimeContext ) {

  }

  public void tearDown() {
    PentahoSystem.systemExitPoint();
  }

  public static void shutdown() {
    PentahoSystem.shutdown();
  }

  protected void startTest() {
    /*
     * Made this green-code because it wasn't currently being used for anything. But, it could come in handy at
     * some point. MB
     * 
     * Throwable th = new Throwable("Test"); //$NON-NLS-1$ StackTraceElement[] st = th.getStackTrace(); String name
     * = st[1].getClassName() + "." + st[1].getMethodName(); //$NON-NLS-1$ String description =
     * Messages.getInstance().getString(name + ".USER_DESCRIPTION"); //$NON-NLS-1$
     */
  }

  public void dispose() {
    if ( context != null ) {
      context.dispose();
    }
  }

  protected void finishTest() {
    dispose();
  }

  public IRuntimeContext run( String actionPath ) {
    assertTrue( initOk );

    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();

    return run( actionPath, parameterProvider );
  }

  public IRuntimeContext run( String actionPath, IParameterProvider parameterProvider ) {

    return run( actionPath, parameterProvider, null, null );
  }

  public IOutputHandler getOutputHandler( OutputStream stream ) {
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( stream, false );
    outputHandler.setOutputPreference( IOutputHandler.OUTPUT_TYPE_DEFAULT );
    return outputHandler;
  }

  public IOutputHandler getOutputHandler( IContentItem contentItem ) {
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( contentItem, false );
    outputHandler.setOutputPreference( IOutputHandler.OUTPUT_TYPE_DEFAULT );
    return outputHandler;
  }

  public IPentahoSession sessionStartup( IPentahoSession session ) {
    // Override to load startup actions. This returns an IPentahoSession
    // so subclasses can create a mock authenticated user session for
    // testing.
    PentahoSystem.globalStartup( session );
    return session;
  }

  public IRuntimeContext run( String actionPath, IParameterProvider parameterProvider, String testName,
      String fileExtension ) {
    assertTrue( initOk );

    OutputStream outputStream = null;
    if ( testName != null && fileExtension != null ) {
      outputStream = getOutputStream( testName, fileExtension );
    }
    IOutputHandler outputHandler = null;
    if ( outputStream != null ) {
      outputHandler = getOutputHandler( outputStream );
    } else {
      outputHandler = getOutputHandler( (OutputStream) null );
    }
    String instanceId = null;
    StandaloneSession initialSession =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    IPentahoSession session = sessionStartup( initialSession );
    if ( outputHandler != null ) {
      outputHandler.setSession( session );
    }

    return run( actionPath, instanceId, false, parameterProvider, outputHandler, session );
  }

  public IRuntimeContext run( String actionPath, String instanceId, boolean persisted,
      IParameterProvider parameterProvider, IOutputHandler outputHandler, IPentahoSession session ) {
    assertTrue( initOk );

    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, session );
    solutionEngine.setLoggingLevel( getLoggingLevel() );
    solutionEngine.init( session );

    return run( solutionEngine, actionPath, instanceId, persisted, parameterProvider, outputHandler );
  }

  public IRuntimeContext run( ISolutionEngine solutionEngine, String actionPath, String instanceId, boolean persisted,
      IParameterProvider parameterProvider, IOutputHandler outputHandler ) {
    assertTrue( initOk );

    info( Messages.getInstance().getString( "BaseTest.INFO_START_TEST_MSG", actionPath ) ); //$NON-NLS-1$
    info( actionPath );

    String baseUrl = ""; //$NON-NLS-1$  
    HashMap<String, IParameterProvider> parameterProviderMap = new HashMap<String, IParameterProvider>();
    parameterProviderMap.put( IParameterProvider.SCOPE_REQUEST, parameterProvider );

    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( baseUrl );

    dispose();
    context =
        solutionEngine
            .execute(
                actionPath,
                Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_TEST" ), false, true, instanceId, persisted, parameterProviderMap, outputHandler, this, urlFactory, messages ); //$NON-NLS-1$
    info( Messages.getInstance().getString( "BaseTest.INFO_FINISH_TEST_MSG", actionPath ) ); //$NON-NLS-1$

    // TODO compare message stack with saved version

    // TODO perform comparisons between genereated content and golden copies

    return context;
  }

  public int getLoggingLevel() {
    return loggingLevel;
  }

  public void setLoggingLevel( int logLevel ) {
    loggingLevel = logLevel;
  }

  public void trace( String message ) {
    messages.add( Messages.getInstance().getString( "Message.USER_DEBUG", message, getClass().getName() ) ); //$NON-NLS-1$
  }

  public void debug( String message ) {
    messages.add( Messages.getInstance().getString( "Message.USER_DEBUG", message, getClass().getName() ) ); //$NON-NLS-1$
  }

  public void info( String message ) {
    messages.add( Messages.getInstance().getString( "Message.USER_INFO", message, getClass().getName() ) ); //$NON-NLS-1$
  }

  public void warn( String message ) {
    messages.add( Messages.getInstance().getString( "Message.USER_WARNING", message, getClass().getName() ) ); //$NON-NLS-1$
  }

  public void error( String message ) {
    messages.add( Messages.getInstance().getString( "Message.USER_ERROR", message, getClass().getName() ) ); //$NON-NLS-1$
  }

  public void fatal( String message ) {
    messages.add( Messages.getInstance().getString( "Message.USER_ERROR", message, getClass().getName() ) ); //$NON-NLS-1$
  }

  public void trace( String message, Throwable error ) {
    messages.add( Messages.getInstance().getString( "Message.USER_DEBUG", message, getClass().getName() ) ); //$NON-NLS-1$
  }

  public void debug( String message, Throwable error ) {
    messages.add( Messages.getInstance().getString( "Message.USER_DEBUG", message, getClass().getName() ) ); //$NON-NLS-1$
  }

  public void info( String message, Throwable error ) {
    messages.add( Messages.getInstance().getString( "Message.USER_INFO", message, getClass().getName() ) ); //$NON-NLS-1$
  }

  public void warn( String message, Throwable error ) {
    messages.add( Messages.getInstance().getString( "Message.USER_WARNING", message, getClass().getName() ) ); //$NON-NLS-1$
  }

  public void error( String message, Throwable error ) {
    messages.add( Messages.getInstance().getString( "Message.USER_ERROR", message, getClass().getName() ) ); //$NON-NLS-1$
  }

  public void fatal( String message, Throwable error ) {
    messages.add( Messages.getInstance().getString( "Message.USER_ERROR", message, getClass().getName() ) ); //$NON-NLS-1$
  }

  public void testNothing() {
    assertTrue( true );
  }

}
