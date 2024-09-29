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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.connections.mondrian;

import mondrian.olap.Util;
import mondrian.parser.TokenMgrError;
import mondrian.rolap.RolapConnectionProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.olap4j.OlapConnection;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * MDXOlap4jConnection implements IPentahoConenction to support olap4j connections to any olap4j provider. Developers
 * may subclass MDXOlap4jConnection to unwrap the olap4j connection to directly manipulate the underlying connection.  An example of this is 
 * setting a DelegatingRole in the case of Mondrian.
 * 
 * @author Benny Chow
 * @version $Id: $
 * @created Jan 9, 2013
 * @updated $DateTime: $
 */
public class MDXOlap4jConnection implements IPentahoConnection {

  static final Log log = LogFactory.getLog( MDXOlap4jConnection.class );

  /**
   * Underlying connection to OLAP system.
   */
  protected OlapConnection connection = null;

  /**
   * Closes the connection.
   */
  public void close() {
    try {
      if ( connection != null ) {
        connection.close();
      }
    } catch ( SQLException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Sets the properties to be used when the connection is made. The standard keys for the properties are defined in
   * this interface.
   * 
   * @param props Properties to be used for creating connection.
   *    This particular method relies on the following properties: url, driver, user, password.
   */
  public boolean connect( Properties props ) {

    String url = props.getProperty( "url" );
    String driver = props.getProperty( "driver" );

    // Fetch the user/password out of the properties so we can pass them
    // as actual JDBC parameters.
    String user = props.getProperty( "user", null );
    String password = props.getProperty( "password", null );

    try {
      if ( connection != null ) {
        connection.close();
      }

      // For Mondrian olap4j driver, we will also do role mapping.
      if ( url.startsWith( "jdbc:mondrian" ) ) {
        Util.PropertyList connectProperties = Util.parseConnectString( url );
        MDXConnection.mapPlatformRolesToMondrianRolesHelper( connectProperties );
        url = connectProperties.toString();
      }

      // Make sure the driver is loaded into the thread's classloader.
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      if ( contextClassLoader == null ) {
        contextClassLoader = getClass().getClassLoader();
      }
      contextClassLoader.loadClass( driver );

      // Create the connection through JDBC.
      java.sql.Connection sqlConnection = DriverManager.getConnection( url, user, password );

      // Unwrap into OlapConnection.
      connection = sqlConnection.unwrap( org.olap4j.OlapConnection.class );

    } catch ( PentahoAccessControlException e ) {
      log.info( Messages.getInstance().getErrorString( "MDXConnection.ERROR_0002_INVALID_CONNECTION",
              "driver=" + driver + ";url=" + getLogUrl( url ) ), e );
      return false;
    } catch ( Exception | TokenMgrError e ) {
      log.error( Messages.getInstance().getErrorString( "MDXConnection.ERROR_0002_INVALID_CONNECTION",
          "driver=" + driver + ";url=" + getLogUrl( url ) ), e );
      return false;
    }

    return true;
  }

  /**
   * Removes password from connection url
   */
  private String getLogUrl( String url ) {
    Util.PropertyList connectProperties = Util.parseConnectString( url );
    connectProperties.remove( RolapConnectionProperties.JdbcPassword.name() );
    return connectProperties.toString();
  }

  /**
   * Executes the specified query against the connection.
   * @param query
   *          SQL-like query string. May be data source specific.
   * @return Returns result set of the query.
   */
  public IPentahoResultSet executeQuery( String arg0 ) {
    throw new UnsupportedOperationException();
  }

  /**Returns resultset of the last executed query.
   * @return Returns the resultset from the last query executed.
   */
  public IPentahoResultSet getResultSet() {
    throw new UnsupportedOperationException();
  }

  /**Checks if the connection is initialized or not.
   * @return Returns true if the connection has been properly initialized.
   */
  public boolean initialized() {
    return connection != null;
  }

  /**Checks if the connection is closed or not.
   * @return Returns true if this connection has been closed.
   */
  public boolean isClosed() {
    if ( connection == null ) {
      throw new IllegalStateException();
    }

    try {
      return connection.isClosed();
    } catch ( SQLException e ) {
      throw new RuntimeException( e );
    }
  }

  /**Checks if the connection is read only or not.
   * @return Returns true if this connection is read only.
   * 
   *         NOTE: Current implementation for all connections is read only.
   */
  public boolean isReadOnly() {
    if ( connection == null ) {
      throw new IllegalStateException();
    }

    try {
      return connection.isReadOnly();
    } catch ( SQLException e ) {
      throw new RuntimeException( e );
    }
  }

  /**Builds the query based on the pattern and parameters list and executes it against the connection.
   * @param arg0
   *          SQL-like query string. May be data source specific.
   * @param arg1
   *          List of objects to bind into prepared query.
   * @return Returns result set of the query.
   */
  public IPentahoResultSet prepareAndExecuteQuery( String arg0, List arg1 ) throws Exception {
    throw new UnsupportedOperationException();
  }

  /**Checks if the given connection supports prepared queries. 
   * 
   * @return Returns true if the connection supports prepared queries.
   */
  public boolean preparedQueriesSupported() {
    return false;
  }

  /**
   * Sets size of the fetch buffer used when retrieving rows from the underlying database.
   * 
   * @param fetchSize The buffer size.
   */
  public void setFetchSize( int arg0 ) {
    throw new UnsupportedOperationException();
  }

  /**
   * Sets maximum rows that will be returned by the next query.
   * 
   * @param maxRows Maximum rows that are returned by the next query.
   */
  public void setMaxRows( int arg0 ) {
    throw new UnsupportedOperationException();
  }

  /**
   * Sets the properties to be used when the connection is made.  It connects to the OLAP system described by the properties.
   * The standard keys for the properties are defined in
   * IPentahoConnection interface.
   * 
   * @param props Properties to be used when the connection is made.
   */
  public void setProperties( Properties props ) {
    this.connect( props );
  }

  /**
   * Clears any warnings cached by the connection.
   */
  public void clearWarnings() {

  }

  /**
   * Returns the type of connection.
   * 
   * @return Returns the type of the connection.
   */
  public String getDatasourceType() {
    return IPentahoConnection.MDX_OLAP4J_DATASOURCE;
  }

  /**Retrieves the string representation of the last executed query.
   * @return Returns the last query string executed.
   */
  public String getLastQuery() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the underlying connection to OLAP system.
   * @return Returns the underlying connection.
   */
  public OlapConnection getConnection() {
    return connection;
  }
}
