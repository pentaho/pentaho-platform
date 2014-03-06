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

package org.pentaho.platform.web.hsqldb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.Server;
import org.hsqldb.ServerConfiguration;
import org.hsqldb.ServerConstants;
import org.hsqldb.lib.FileUtil;
import org.hsqldb.persist.HsqlProperties;
import org.pentaho.platform.web.hsqldb.messages.Messages;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The purpose of this Java class is to startup a HSQLDB databases. This class should not installed in production
 * environments. This is for samples / demoing only.
 * 
 * You typically will configure a ServletContext parameter in your web.xml, named "hsqldb-databases" with a value that
 * follows this format: dbName@../path/to/database,@otherDbName@../path/to/other/database
 * 
 */
public class HsqlDatabaseStarterBean {

  private static final Log logger = LogFactory.getLog( HsqlDatabaseStarterBean.class );

  private Server hsqlServer;

  private int port = 9001; // Default Port
  private int failoverPort = port;

  private Map<String, String> databases = new LinkedHashMap<String, String>();

  private boolean allowFailoverToDefaultPort;

  protected boolean checkPort() {
    if ( ( port < 0 ) || ( port > 65535 ) ) {
      if ( allowFailoverToDefaultPort ) {
        logger.error( Messages.getErrorString( "HsqlDatabaseStarterBean.ERROR_0004_INVALID_PORT", "" + failoverPort ) ); //$NON-NLS-1$
        port = failoverPort;
      } else {
        return logFailure( "HsqlDatabaseStarterBean.ERROR_0004_INVALID_PORT", "" + failoverPort ); //$NON-NLS-1$
      }
    }

    try {
      ServerSocket sock = new ServerSocket( port );
      sock.close();
    } catch ( IOException ex1 ) {
      if ( port == failoverPort ) {
        return logFailure( "HsqlDatabaseStarterBean.ERROR_0006_DEFAULT_PORT_IN_USE" ); //$NON-NLS-1$
      } else {
        if ( allowFailoverToDefaultPort ) {
          logger.error( Messages.getErrorString(
              "HsqlDatabaseStarterBean.ERROR_0005_SPECIFIED_PORT_IN_USE", Integer.toString( port ), "" + failoverPort ) ); //$NON-NLS-1$
          port = failoverPort;
          try {
            ServerSocket sock = new ServerSocket( port );
            sock.close();
          } catch ( IOException ex2 ) {
            return logFailure( "HsqlDatabaseStarterBean.ERROR_0006_DEFAULT_PORT_IN_USE" ); //$NON-NLS-1$
          }
        } else {
          return logFailure(
              "HsqlDatabaseStarterBean.ERROR_0008_SPECIFIED_PORT_IN_USE_NO_FAILOVER", Integer.toString( port ) ); //$NON-NLS-1$
        }
      }
    }
    return true;
  }

  // Facilitate test cases
  protected HsqlProperties getServerProperties( String[] args ) {
    // From HSQLDB Server.java main method...
    String propsPath = FileUtil.canonicalOrAbsolutePath( "server" ); //$NON-NLS-1$
    HsqlProperties fileProps = ServerConfiguration.getPropertiesFromFile( propsPath );
    HsqlProperties props = fileProps == null ? new HsqlProperties() : fileProps;
    HsqlProperties stringProps = null;
    try {
      stringProps = HsqlProperties.argArrayToProps( args, ServerConstants.SC_KEY_PREFIX );
      props.addProperties( stringProps );
    } catch ( ArrayIndexOutOfBoundsException ex ) {
      logger.error( Messages.getErrorString( "HsqlDatabaseStarterBean.ERROR_0001_INVALID_PARAMETERS" ) ); //$NON-NLS-1$
      ex.printStackTrace();
      logger.warn( Messages.getString( "HsqlDatabaseStarterBean.WARN_NO_DATABASES" ) ); //$NON-NLS-1$
      return null;
    }
    return props;
  }

  // Facilitate test cases
  protected Server getNewHSQLDBServer() {
    return new Server();
  }

  /**
   * Starts hsqldb databases.
   * 
   * @return true if the server was started properly.
   */
  public boolean start() {
    if ( !checkPort() ) {
      return false;
    }
    ArrayList<String> startupArguments = getStartupArguments();

    String[] args = startupArguments.toArray( new String[] {} );

    if ( logger.isTraceEnabled() ) {
      logger.trace( "Assembled parameters" ); //$NON-NLS-1$
      for ( int i = 0; i < args.length; i++ ) {
        logger.trace( String.format( "  args[%d]=%s", i, args[i] ) ); //$NON-NLS-1$
      }
    }

    HsqlProperties props = getServerProperties( args );
    if ( props == null ) { // If props failed, return
      return false;
    }

    hsqlServer = getNewHSQLDBServer();

    try {
      hsqlServer.setProperties( props );
    } catch ( Exception e ) {
      logger.error( Messages.getErrorString( "HsqlDatabaseStarterBean.ERROR_0002_INVALID_CONFIGURATION" ) ); //$NON-NLS-1$
      e.printStackTrace();
      logger.warn( Messages.getString( "HsqlDatabaseStarterBean.WARN_NO_DATABASES" ) ); //$NON-NLS-1$
      return false;
    }
    hsqlServer.start();
    return hsqlServer.getState() == ServerConstants.SERVER_STATE_ONLINE;
  }

  /**
   * Stops the hsqldb databases.
   * 
   * @return true if the server stopped properly.
   */
  public boolean stop() {

    if ( hsqlServer != null ) {
      try {
        logger.debug( "Stopping embedded hsqldb databases" ); //$NON-NLS-1$
        logger.debug( "Signaling connection close..." ); //$NON-NLS-1$
        hsqlServer.signalCloseAllServerConnections();
        logger.debug( "Stopping server listener threads.." ); //$NON-NLS-1$
        hsqlServer.stop();
        int times = 0;
        logger.debug( "Waiting for embedded server to complete shut down tasks..." ); //$NON-NLS-1$
        // Give it about 15 or so seconds to quit...
        while ( ( hsqlServer.getState() != ServerConstants.SERVER_STATE_SHUTDOWN ) && ( times < 100 ) ) {
          try {
            Thread.sleep( times + 1 * 100 );
          } catch ( InterruptedException e ) {
            //ignore
          }
          times++;
        }
        if ( hsqlServer.getState() != ServerConstants.SERVER_STATE_SHUTDOWN ) {
          logger.error( Messages.getErrorString( "HsqlDatabaseStarterBean.ERROR_0003_DID_NOT_STOP" ) ); //$NON-NLS-1$
          return false;
        }
        return true;
      } finally {
        hsqlServer = null;
      }
    }
    return true;
  }

  private boolean logFailure( final String errorId ) {
    logger.error( Messages.getErrorString( errorId ) );
    logger.warn( Messages.getString( "HsqlDatabaseStarterBean.WARN_NO_DATABASES" ) ); //$NON-NLS-1$
    return false;
  }

  private boolean logFailure( final String errorId, String param ) {
    logger.error( Messages.getErrorString( errorId, param ) );
    logger.warn( Messages.getString( "HsqlDatabaseStarterBean.WARN_NO_DATABASES" ) ); //$NON-NLS-1$
    return false;
  }

  protected ArrayList<String> getStartupArguments() {
    ArrayList<String> rtnArgsList = new ArrayList<String>();

    if ( port != 9001 ) {
      rtnArgsList.add( "-port" ); //$NON-NLS-1$
      rtnArgsList.add( Integer.toString( port ) );
    }
    // Prevent system.exit(0);
    rtnArgsList.add( "-no_system_exit" ); //$NON-NLS-1$
    rtnArgsList.add( "true" ); //$NON-NLS-1$

    int idx = 0;
    for ( Map.Entry<String, String> entry : databases.entrySet() ) {
      rtnArgsList.add( "-database." + idx ); //$NON-NLS-1$
      rtnArgsList.add( entry.getValue() );

      rtnArgsList.add( "-dbname." + idx ); //$NON-NLS-1$
      rtnArgsList.add( entry.getKey() );

      logger.debug( MessageFormat.format(
          "Hsqldb database {0} configured to start with name {1}", entry.getValue(), entry.getKey() ) ); //$NON-NLS-1$
      idx++;
    }

    return rtnArgsList;
  }

  /*
   * Getters and Setters
   */

  public void setPort( int value ) {
    port = value;
  }

  public int getPort() {
    return port;
  }

  public void setFailoverPort( int value ) {
    failoverPort = value;
  }

  public int getFailoverPort() {
    return failoverPort;
  }

  public Map<String, String> getDatabases() {
    return databases;
  }

  public void setDatabases( Map<String, String> databases ) {
    this.databases = databases;
  }

  public void setAllowPortFailover( boolean value ) {
    allowFailoverToDefaultPort = value;
  }

  public boolean getAllowPortFailover() {
    return allowFailoverToDefaultPort;
  }

}
