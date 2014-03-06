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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.sampledata;

import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.web.hsqldb.HsqlDatabaseStarterBean;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings( "nls" )
public class HsqldbStarterBeanTest {

  private int findAvailablePort( int startFrom ) {
    int portTry = startFrom;
    boolean found = false;
    while ( !found ) {
      try {
        ServerSocket sock = new ServerSocket( portTry ); // lock the port
        found = true; // break out of the loop.
        sock.close();
        return portTry;
      } catch ( IOException ex ) {
        portTry++;
        if ( portTry > 65535 ) {
          throw new RuntimeException( "Out of ports to try" );
        }
      }
    }
    return -1;
  }

  @Test
  public void testStarterBeanPort() {
    ExposedSampleDatabaseStarterBean starterBean = new ExposedSampleDatabaseStarterBean();

    // Test without allowing failover first.
    starterBean.setAllowPortFailover( false );

    // Invalid or In Use Ports
    starterBean.setPort( -1 );
    Assert.assertFalse( starterBean.checkPort() ); // Should fail - Invalid port (negative)
    starterBean.setPort( 65539 );
    Assert.assertFalse( starterBean.checkPort() ); // Should fail - Invalid port (too high)
    // Find an open port (starting above the default port), block it, and try to start on that port
    int portTry = findAvailablePort( 9011 );

    try {
      ServerSocket sock = new ServerSocket( portTry ); // lock the port
      starterBean.setPort( portTry );
      Assert.assertFalse( starterBean.checkPort() ); // should fail - port in use
      sock.close();
    } catch ( IOException ex ) {
      // something went wrong. blow out here.
      Assert.fail( ex.getMessage() );
    }

    // Test failover
    int failoverPort = findAvailablePort( 9999 );

    starterBean.setAllowPortFailover( true );
    starterBean.setFailoverPort( failoverPort );

    // Invalid or In Use Ports
    starterBean.setPort( -1 );
    Assert.assertTrue( starterBean.checkPort() ); // Should failover to failoverPort - Invalid port (negative)
    Assert.assertEquals( failoverPort, starterBean.getPort() );

    starterBean.setPort( 65539 );
    Assert.assertTrue( starterBean.checkPort() ); // Should failover to failoverPort - Invalid port (too high)
    Assert.assertEquals( failoverPort, starterBean.getPort() );

    // Block the port again, and try to start on that port
    try {
      ServerSocket sock = new ServerSocket( portTry ); // lock the port
      // sock.bind(null);
      starterBean.setPort( portTry );
      Assert.assertTrue( starterBean.checkPort() ); // should failover - port in use to failover to failoverPort
      Assert.assertEquals( failoverPort, starterBean.getPort() );
      sock.close();
    } catch ( IOException ex ) {
      Assert.fail( ex.getMessage() );
    }

  }

  @Test
  public void testDefaultParameters() {
    ExposedSampleDatabaseStarterBean starterBean = new ExposedSampleDatabaseStarterBean();
    ArrayList<String> defaultArguments = starterBean.getStartupArguments();
    Assert.assertEquals( "-no_system_exit", defaultArguments.get( 0 ) );
    Assert.assertEquals( "true", defaultArguments.get( 1 ) );
  }

  @Test
  public void testOverrideParameters() {
    ExposedSampleDatabaseStarterBean starterBean = new ExposedSampleDatabaseStarterBean();
    starterBean.setPort( 2000 );
    Map<String, String> databases = new LinkedHashMap<String, String>();
    databases.put( "databasename0", "location0" );
    databases.put( "databasename1", "location1" );
    starterBean.setDatabases( databases );
    ArrayList<String> overriddenArguments = starterBean.getStartupArguments();
    Assert.assertEquals( "-port", overriddenArguments.get( 0 ) );
    Assert.assertEquals( "2000", overriddenArguments.get( 1 ) );
    Assert.assertEquals( "-no_system_exit", overriddenArguments.get( 2 ) );
    Assert.assertEquals( "true", overriddenArguments.get( 3 ) );
    Assert.assertEquals( "-database.0", overriddenArguments.get( 4 ) );
    Assert.assertEquals( "location0", overriddenArguments.get( 5 ) );
    Assert.assertEquals( "-dbname.0", overriddenArguments.get( 6 ) );
    Assert.assertEquals( "databasename0", overriddenArguments.get( 7 ) );
    Assert.assertEquals( "-database.1", overriddenArguments.get( 8 ) );
    Assert.assertEquals( "location1", overriddenArguments.get( 9 ) );
    Assert.assertEquals( "-dbname.1", overriddenArguments.get( 10 ) );
    Assert.assertEquals( "databasename1", overriddenArguments.get( 11 ) );
  }

  @Test
  public void testStartServer() {
    ExposedSampleDatabaseStarterBean starterBean = new ExposedSampleDatabaseStarterBean();
    // Actually start the server on an available port.
    int port = findAvailablePort( 9001 ); // try default port first, and work up.
    starterBean.setPort( port );
    File tmpFolder = null;
    File sampleFile = null;
    File samplePropsFile = null;
    try {
      String tmpFolderName = System.getProperty( "java.io.tmpdir" ).replaceAll( "\\\\", "/" );
      tmpFolder = new File( tmpFolderName );
      sampleFile = File.createTempFile( "sampledata", ".script" );
      String fNameBase = sampleFile.getName().substring( 0, sampleFile.getName().length() - 7 );
      String propsFileName = fNameBase + ".properties";
      samplePropsFile = new File( tmpFolder.getCanonicalPath(), propsFileName );
      String loc = tmpFolderName + ( tmpFolderName.endsWith( "/" ) ? "" : "/" ) + fNameBase;

      Map<String, String> databases = new LinkedHashMap<String, String>();
      databases.put( "sampledata", loc );
      starterBean.setDatabases( databases );

      // We have a port, and a location in the users' tmp folder.
      Assert.assertTrue( starterBean.start() );
      String logOutput = starterBean.getLogOutput();
      Assert.assertTrue( logOutput.contains( "is online" ) );

      // Make sure that the port is now in use...
      try {
        ServerSocket sock = new ServerSocket( port );
        Assert.fail( "Port was available - server not really started" );
        sock.close();
      } catch ( IOException expected ) {
        //ignore
      }
      // OK, now, we can close...
      Assert.assertTrue( starterBean.stop() );

      logOutput = starterBean.getLogOutput(); // get the log output again..
      Assert.assertTrue( logOutput.contains( "Shutdown sequence completed" ) );

      // Let's make sure it really stopped
      try {
        ServerSocket sock = new ServerSocket( port );
        sock.close();
      } catch ( IOException expected ) {
        Assert.fail( "Port not available - server not really stopped.." );
      }

    } catch ( IOException ex ) {
      Assert.fail( ex.getMessage() );
    } finally {
      if ( ( sampleFile != null ) && ( sampleFile.exists() ) ) {
        sampleFile.delete();
      }
      if ( ( samplePropsFile != null ) && ( sampleFile.exists() ) ) {
        samplePropsFile.delete();
      }
    }
  }

  @Test
  public void testServerWontStop() {
    final ExposedServer testServer = new ExposedServer();
    testServer.setWontStop( true );

    ExposedSampleDatabaseStarterBean starterBean = new ExposedSampleDatabaseStarterBean() {
      protected Server getNewHSQLDBServer() {
        testServer.setLogWriter( logWriter );
        return testServer;
      }
    };
    // Actually start the server on an available port.
    int port = findAvailablePort( 9001 ); // try default port first, and work up.
    starterBean.setPort( port );
    File tmpFolder = null;
    File sampleFile = null;
    File samplePropsFile = null;
    try {
      String tmpFolderName = System.getProperty( "java.io.tmpdir" ).replaceAll( "\\\\", "/" );
      tmpFolder = new File( tmpFolderName );
      sampleFile = File.createTempFile( "sampledata", ".script" );
      String fNameBase = sampleFile.getName().substring( 0, sampleFile.getName().length() - 7 );
      String propsFileName = fNameBase + ".properties";
      samplePropsFile = new File( tmpFolder.getCanonicalPath(), propsFileName );
      String loc = tmpFolderName + ( tmpFolderName.endsWith( "/" ) ? "" : "/" ) + fNameBase;

      Map<String, String> databases = new LinkedHashMap<String, String>();
      databases.put( "sampledata", loc );
      starterBean.setDatabases( databases );

      // We have a port, and a location in the users' tmp folder.
      Assert.assertTrue( starterBean.start() );

      // Try to close and should fail... This may take a while.
      Assert.assertFalse( starterBean.stop() );
      // Now, let's close for real.
      testServer.setWontStop( false );
      Assert.assertTrue( starterBean.stop() );

    } catch ( IOException ex ) {
      Assert.fail( ex.getMessage() );
    } finally {
      if ( ( sampleFile != null ) && ( sampleFile.exists() ) ) {
        sampleFile.delete();
      }
      if ( ( samplePropsFile != null ) && ( sampleFile.exists() ) ) {
        samplePropsFile.delete();
      }
    }
  }

  private class ExposedServer extends Server {
    boolean wontStop = false;

    public void setWontStop( boolean value ) {
      wontStop = value;
    }

    public int stop() {
      if ( wontStop ) {
        return getState();
      } else {
        return super.stop();
      }
    }
  }

  private class ExposedSampleDatabaseStarterBean extends HsqlDatabaseStarterBean {
    StringWriter sw = new StringWriter();
    PrintWriter logWriter = new PrintWriter( sw );

    protected boolean checkPort() {
      return super.checkPort();
    }

    protected HsqlProperties getServerProperties( String[] args ) {
      return super.getServerProperties( args );
    }

    protected Server getNewHSQLDBServer() {
      Server rtn = super.getNewHSQLDBServer();
      rtn.setLogWriter( logWriter );
      return rtn;
    }

    public boolean start() {
      return super.start();
    }

    public boolean stop() {
      return super.stop();
    }

    protected ArrayList<String> getStartupArguments() {
      return super.getStartupArguments();
    }

    protected String getLogOutput() {
      return sw.toString();
    }

  }

}
