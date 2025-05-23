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


package org.pentaho.test.platform.engine.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import junit.framework.TestCase;

public abstract class BaseTestCase extends TestCase {
  public static final String SOLUTION_PATH = "src/test/resources/solution"; //$NON-NLS-1$

  public static final String SCOPE_REQUEST = "request"; //$NON-NLS-1$

  public static final String SCOPE_SESSION = "session"; //$NON-NLS-1$

  private String solutionPath;

  final String SYSTEM_FOLDER = "/system"; //$NON-NLS-1$

  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml"; //$NON-NLS-1$

  StandaloneApplicationContext applicationContext = null;

  IPentahoSession session;

  public BaseTestCase() {
    init( getSolutionPath() );
  }

  public BaseTestCase( String solutionPath ) {
    init( solutionPath );
  }

  protected void init( String solnPath ) {
    PentahoSystem.setSystemSettingsService( new PathBasedSystemSettings() );
    applicationContext = new StandaloneApplicationContext( solnPath, "" ); //$NON-NLS-1$
    applicationContext.setFullyQualifiedServerURL( getFullyQualifiedServerURL() );
    String inContainer = System.getProperty( "incontainer", "false" ); //$NON-NLS-1$ //$NON-NLS-2$
    if ( inContainer.equalsIgnoreCase( "false" ) ) { //$NON-NLS-1$
      // Setup simple-jndi for datasources
      System.setProperty( "java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory" ); //$NON-NLS-1$ //$NON-NLS-2$
      System.setProperty( "org.osjava.sj.root", getSolutionPath() + "/system/simple-jndi" ); //$NON-NLS-1$ //$NON-NLS-2$
      System.setProperty( "org.osjava.sj.delimiter", "/" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    String objectFactoryCreatorCfgFile = getSolutionPath() + SYSTEM_FOLDER + "/" + DEFAULT_SPRING_CONFIG_FILE_NAME; //$NON-NLS-1$

    IPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
    pentahoObjectFactory.init( objectFactoryCreatorCfgFile, null );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );
    PentahoSystem.init( applicationContext );
    session = new StandaloneSession( "system" ); //$NON-NLS-1$
  }

  protected InputStream getInputStreamFromOutput( String solnPath, String testName, String extension ) {
    String path =
        PentahoSystem.getApplicationContext().getFileOutputPath( solnPath + "test/tmp/" + testName + extension ); //$NON-NLS-1$
    File f = new File( path );
    if ( f.exists() ) {
      try {
        FileInputStream fis = new FileInputStream( f );
        return fis;
      } catch ( Exception ignored ) {
        return null;
      }
    } else {
      return null;
    }
  }

  protected OutputStream getOutputStream( String solnPath, String testName, String extension ) {
    OutputStream outputStream = null;
    try {
      IApplicationContext appContext = PentahoSystem.getApplicationContext();
      String outputPath = solnPath + "/test/tmp"; //$NON-NLS-1$
      String tmpDir = appContext.getFileOutputPath( outputPath );
      // String tmpDir = PentahoSystem.getApplicationContext().getFileOutputPath(SOLUTION_PATH +"test/tmp");
      // //$NON-NLS-1$
      File file = new File( tmpDir );
      file.mkdirs();
      String path =
          PentahoSystem.getApplicationContext().getFileOutputPath( solnPath + "/test/tmp/" + testName + extension ); //$NON-NLS-1$
      outputStream = new FileOutputStream( path );
    } catch ( FileNotFoundException e ) {
      // ignored
    }
    return outputStream;
  }

  public IRuntimeContext run( String actionSequencePath, String actionSequence, IParameterProvider parameterProvider,
      IOutputHandler outputHandler ) {
    List messages = new ArrayList();
    String instanceId = null;
    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, session );
    solutionEngine.setLoggingLevel( ILogger.ERROR );
    solutionEngine.init( session );
    HashMap parameterProviderMap = new HashMap();
    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( "" );
    IRuntimeContext runtimeContext = null;
    Reader reader = null;
    try {
      File file = new File( actionSequencePath + actionSequence );
      StringBuilder str = new StringBuilder();
      reader = new FileReader( file );
      char[] buffer = new char[4096];
      int n = reader.read( buffer );
      while ( n != -1 ) {
        str.append( buffer, 0, n );
        n = reader.read( buffer );
      }
      String xactionStr = str.toString();

      solutionEngine.setSession( session );
      runtimeContext =
          solutionEngine.execute( xactionStr, actionSequence, "action sequence test", false, true, instanceId, false, //$NON-NLS-1$
              parameterProviderMap, null, null, urlFactory, messages );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    } finally {
      if ( reader != null ) {
        try {
          reader.close();
        } catch ( Exception ex ) {
          ex.printStackTrace();
          assertTrue( ex.getMessage(), false );
        }
      }
    }
    return runtimeContext;
  }

  public String getSolutionPath() {
    return solutionPath;
  }

  public String getFullyQualifiedServerURL() {
    return "http://localhost:8080/pentaho/"; //$NON-NLS-1$
  }

  public IPentahoSession getPentahoSession() {
    return session;
  }

  public void setPentahoSession( IPentahoSession session ) {
    this.session = session;
  }
}
