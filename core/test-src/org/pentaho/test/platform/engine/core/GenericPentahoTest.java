/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 3 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2005 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.test.platform.engine.core;

import junit.framework.TestCase;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

public class GenericPentahoTest extends TestCase {
  private static final String SOLUTION_PATH = "solution.path"; //$NON-NLS-1$

  private String solutionPath;

  private static final String PROPERTIES_FILE_NAME = "testsettings.properties"; //$NON-NLS-1$

  private static Properties properties = null;

  public GenericPentahoTest( String arg0 ) {
    super( arg0 );
    init();
  }

  public GenericPentahoTest() {
    super();
    init();
  }

  private void init() {
    InputStream s = null;
    // start with a default solution path
    solutionPath = "test-res/solution"; //$NON-NLS-1$
    try {
      URL url = ClassLoader.getSystemResource( PROPERTIES_FILE_NAME );
      if ( url != null ) {
        s = url.openStream();
      }
    } catch ( IOException e1 ) {
      System.out.println( "Error loding the properties file " + PROPERTIES_FILE_NAME ); //$NON-NLS-1$
    }
    if ( null != s ) {
      properties = new Properties();
      try {
        properties.load( s );
        solutionPath = properties.getProperty( SOLUTION_PATH, null );
      } catch ( IOException e ) {
        System.out.println( "Error loding the properties file " + PROPERTIES_FILE_NAME ); //$NON-NLS-1$
      }
    }
  }

  public ISolutionEngine getSolutionEngine( String path ) {

    StandaloneApplicationContext applicationContext = new StandaloneApplicationContext( path, "" ); //$NON-NLS-1$
    if ( !PentahoSystem.getInitializedOK() ) {
      PentahoSystem.init( applicationContext );
      assertTrue( "PentahoSystem did not initialize", PentahoSystem.getInitializedOK() ); //$NON-NLS-1$
    }

    IPentahoSession session = new StandaloneSession( "system" ); //$NON-NLS-1$
    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, session );
    assertNotNull( "SolutionEngine is null", solutionEngine ); //$NON-NLS-1$
    solutionEngine.setLoggingLevel( ILogger.ERROR );
    solutionEngine.init( session );

    try {
      solutionEngine.setSession( session );
      return solutionEngine;
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }
    return null;
  }

  protected InputStream getInputStreamFromOutput( String testName, String extension ) {
    String path = PentahoSystem.getApplicationContext().getFileOutputPath( "test/tmp/" + testName + extension ); //$NON-NLS-1$
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

  protected OutputStream getOutputStream( String testName, String extension ) {
    OutputStream outputStream = null;
    try {
      String tmpDir = PentahoSystem.getApplicationContext().getFileOutputPath( "test/tmp" ); //$NON-NLS-1$
      File file = new File( tmpDir );
      file.mkdirs();
      String path = PentahoSystem.getApplicationContext().getFileOutputPath( "test/tmp/" + testName + extension ); //$NON-NLS-1$
      outputStream = new FileOutputStream( path );
    } catch ( FileNotFoundException e ) {
      //ignored
    }
    return outputStream;
  }

  @SuppressWarnings( "null" )
  public boolean compare( String testName, String extension ) {

    InputStream goldenStream = null;
    InputStream tmpFileStream = null;

    String filePath = PentahoSystem.getApplicationContext().getFileOutputPath( "test/tmp/" + testName + extension ); //$NON-NLS-1$
    try {
      File file = new File( filePath );
      tmpFileStream = new FileInputStream( file );
    } catch ( Throwable e ) {
      // this gets caught in the assert below
    }
    assertNotNull( Messages.getInstance().getString( "GenericTest.USER_TEST_FILE_INVALID", filePath ), tmpFileStream ); //$NON-NLS-1$

    filePath = PentahoSystem.getApplicationContext().getFileOutputPath( "test/golden/" + testName + extension ); //$NON-NLS-1$
    try {
      File file = new File( filePath );
      goldenStream = new FileInputStream( file );
    } catch ( Throwable e ) {
      // this gets caught in the assert below
    }
    assertNotNull( Messages.getInstance().getString( "GenericTest.USER_TEST_FILE_INVALID", filePath ), goldenStream ); //$NON-NLS-1$
    assertNotNull( Messages.getInstance().getString( "GenericTest.USER_TEST_FILE_INVALID", filePath ), tmpFileStream ); //$NON-NLS-1$

    // compare the two files

    int goldPos = 0;
    int tmpPos = 0;
    byte[] goldBuffer = new byte[2048];
    byte[] tmpBuffer = new byte[2048];
    int filePosition = 0;
    try {
      // read the start of both files
      goldPos = goldenStream.read( goldBuffer );
      tmpPos = tmpFileStream.read( tmpBuffer );
      // assume lock-step
      if ( goldPos != tmpPos ) {
        System.out.println( Messages.getInstance().getString( "GenericTest.USER_FILE_POINTERS_NOT_IN_STEP" ) ); //$NON-NLS-1$
        return false;
      }
      while ( goldPos > 0 && tmpPos > 0 ) {
        for ( int index = 0; index < goldPos; index++ ) {
          assertEquals(
              Messages.getInstance().getString(
                  "GenericTest.USER_FILES_DIFFER", Integer.toString( filePosition + index ) ), goldBuffer[index], tmpBuffer[index] ); //$NON-NLS-1$
        }
        filePosition += goldPos;
        goldPos = goldenStream.read( goldBuffer );
        tmpPos = tmpFileStream.read( tmpBuffer );

      }
    } catch ( Exception e ) {
      return false;
    } finally {
      if ( goldenStream != null ) {
        try {
          goldenStream.close();
        } catch ( Exception e ) {
          return false;
        }
      }
      if ( tmpFileStream != null ) {
        try {
          tmpFileStream.close();
        } catch ( Exception e ) {
          return false;
        }
      }
    }
    return true;

  }

  public String getSolutionPath() {
    return solutionPath;
  }

  public void testNothing() {
    assertTrue( true );
  }

}
