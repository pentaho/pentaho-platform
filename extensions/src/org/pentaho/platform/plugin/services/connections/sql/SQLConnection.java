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

package org.pentaho.platform.plugin.services.connections.sql;

import org.pentaho.commons.connection.ILimitableConnection;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PentahoSystemException;
import org.pentaho.platform.engine.core.system.IPentahoLoggingConnection;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author wseyler
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class SQLConnection implements IPentahoLoggingConnection, ILimitableConnection {
  protected Connection nativeConnection;

  /*
   * private static int connectionCtr = 0;
   */
  // private int myCtr;
  /** keep track of any created statements for closing at the end */
  ArrayList<Statement> stmts = new ArrayList<Statement>();

  /** keep track of any created result sets for closing at the end */
  ArrayList<IPentahoResultSet> resultSets = new ArrayList<IPentahoResultSet>();

  IPentahoResultSet sqlResultSet = null;

  ILogger logger = null;

  private int timeOut = -1; // in seconds

  private int maxRows = -1;

  private int fetchSize = -1;

  private boolean readOnly;

  private boolean forcedForwardOnly = false;

  private boolean fallBackToNonscrollableOnError = true;

  public static final int RESULTSET_SCROLLABLE = ResultSet.TYPE_SCROLL_INSENSITIVE;

  public static final int RESULTSET_FORWARDONLY = ResultSet.TYPE_FORWARD_ONLY;

  public static final int CONCUR_READONLY = ResultSet.CONCUR_READ_ONLY;

  public static final int CONCUR_UPDATABLE = ResultSet.CONCUR_UPDATABLE;

  /*
   * private synchronized void bump() { connectionCtr++; }
   */
  String lastQuery = null;

  public SQLConnection() {
    super();
  }

  public void setLogger( final ILogger logger ) {
    this.logger = logger;
  }

  public void setProperties( Properties props ) {
    // TODO: consolidate this into connect()
    String jndiName = props.getProperty( IPentahoConnection.JNDI_NAME_KEY );
    if ( jndiName != null ) {
      initWithJNDI( jndiName );
    } else {
      connect( props );
    }
  }

  // Added by Arijit Chatterjee.Sets the value of timeout
  /**
   * Sets the valid of the timeout (in seconds)
   */
  public void setQueryTimeout( final int timeInSec ) {
    timeOut = timeInSec;
  }

  /**
   * Sets the connection object to readonly.
   * 
   * @param value
   */
  public void setReadOnly( final boolean value ) {
    this.readOnly = value;
  }

  // Added by Arijit Chatterjee. gets the value of timeout
  /**
   * Returns the query timeout value (in seconds)
   */
  public int getQueryTimeout() {
    return this.timeOut;
  }

  public SQLConnection( final String driverName, final String location, final String userName, final String password,
      final ILogger logger ) {
    super();
    this.logger = logger;
    init( driverName, location, userName, password );
  }

  protected void init( final String driverName, final String location, final String userName, final String password ) {
    // bump();
    try {
      /*
       * TODO This is where we use the java.sql package to provide a SQL connection object back to the caller
       */
      Driver driver = null;
      try {
        driver = DriverManager.getDriver( location );
      } catch ( Exception e ) {
        // if we don't find this connection, it isn't registered, so we'll try to find it on the classpath
      }
      if ( driver == null ) {
        Class driverClass = Class.forName( driverName );
        driver = (Driver) driverClass.newInstance();
        DriverManager.registerDriver( driver );
      }
      Properties info = new Properties();
      info.put( "user", userName == null ? "" : userName ); //$NON-NLS-1$
      info.put( "password", password == null ? "" : password ); //$NON-NLS-1$
      nativeConnection = captureConnection( driver.connect( location, info ) );
      if ( nativeConnection == null ) {
        logger.error( Messages.getInstance().getErrorString(
          "ConnectFactory.ERROR_0001_INVALID_CONNECTION2", driverName, location ) ); //$NON-NLS-1$
      } else {
        enhanceConnection( nativeConnection );
      }
    } catch ( Throwable t ) {
      logger.error( Messages.getInstance().getErrorString(
          "ConnectFactory.ERROR_0001_INVALID_CONNECTION2", driverName, location ), t ); //$NON-NLS-1$
      close(); // do not allow connection to be used as it might not be enhanced
    }
  }

  public boolean initialized() {
    return nativeConnection != null;
  }

  /**
   * return datasource type SQL
   * 
   * @return datasource type
   */
  public String getDatasourceType() {
    return IPentahoConnection.SQL_DATASOURCE;
  }

  protected void initWithJNDI( final String jndiName ) {
    // bump();
    // myCtr = connectionCtr;
    try {
      IDBDatasourceService datasourceService = PentahoSystem.getObjectFactory().get( IDBDatasourceService.class, null );
      DataSource dataSource = datasourceService.getDataSource( jndiName );
      if ( dataSource != null ) {
        nativeConnection = captureConnection( dataSource.getConnection() );
        if ( nativeConnection == null ) {
          logger.error( Messages.getInstance()
              .getErrorString( "ConnectFactory.ERROR_0001_INVALID_CONNECTION", jndiName ) ); //$NON-NLS-1$
          // clear datasource cache
          datasourceService.clearDataSource( jndiName );
        } else {
          enhanceConnection( nativeConnection );
        }
      } else {
        logger
            .error( Messages.getInstance().getErrorString( "ConnectFactory.ERROR_0001_INVALID_CONNECTION",
              jndiName ) ); //$NON-NLS-1$
        // clear datasource cache
        datasourceService.clearDataSource( jndiName );
      }
    } catch ( Exception e ) {
      logger.error(
          Messages.getInstance().getErrorString( "ConnectFactory.ERROR_0001_INVALID_CONNECTION", jndiName ), e ); //$NON-NLS-1$
      close(); // do not allow connection to be used as it might not be enhanced
      // clear datasource cache
      try {
        IDBDatasourceService datasourceService =
            PentahoSystem.getObjectFactory().get( IDBDatasourceService.class, null );
        datasourceService.clearDataSource( jndiName );
      } catch ( ObjectFactoryException objface ) {
        logger.error( Messages.getInstance().getErrorString(
          "ConnectFactory.ERROR_0002_UNABLE_TO_FACTORY_OBJECT=Unable to factory object", jndiName ), e ); //$NON-NLS-1$
      }
    }
  }

  /**
   * Allows the native SQL Connection to be enhanced in a subclass. Best used when a connection needs to be enhanced
   * with an "effective user"
   * 
   * @param connection
   */
  protected void enhanceConnection( Connection connection ) throws SQLException {
  }

  /**
   * Allows enhancements to the native SQL Connection to be removed in a subclass. Best used when a connection needs to
   * be enhanced with an "effective user"
   * 
   * @param connection
   */
  protected void unEnhanceConnection( Connection connection ) throws SQLException {
  }

  /**
   * Allow wrapping/proxying of the native SQL connection by a subclass. Best used when a connection needs to be be
   * enhanced or proxied for Single Signon or possibly tenanting.
   * 
   * @param connection
   * @return
   */
  protected Connection captureConnection( Connection connection ) throws SQLException {
    return connection;
  }

  /**
   * Allows the native SQL Statement to be enhanced by a subclass. Examples may be to allow additional information like
   * a user to be bound to the statement.
   * 
   * @param statement
   */
  protected void enhanceStatement( Statement statement ) throws SQLException {
  }

  /**
   * iterate over and close all statements. Remove each statement from the list.
   */
  private void closeStatements() {
    Iterator iter = stmts.iterator();
    while ( iter.hasNext() ) {
      Statement stmt = (Statement) iter.next();
      if ( stmt != null ) {
        try {
          stmt.close();
        } catch ( Exception ignored ) {
          //ignored
        }
      }
      iter.remove();
    }
  }

  /**
   * iterate over and close all resultsets. Remove each result set from the list.
   */
  private void closeResultSets() {
    Iterator iter = resultSets.iterator();
    while ( iter.hasNext() ) {
      IPentahoResultSet rset = (IPentahoResultSet) iter.next();
      if ( rset != null ) {
        try {
          rset.close();
        } catch ( Exception ignored ) {
          //ignored
        }
      }
      iter.remove();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#close()
   */
  public void close() {
    closeResultSets();
    closeStatements();
    if ( nativeConnection != null ) {
      try {
        unEnhanceConnection( nativeConnection );
        if ( getReadOnly() ) {
          try {
            // Reset the readonly on the native connection before closing
            nativeConnection.setReadOnly( false );
          } catch ( SQLException ignored ) {
            //ignored
          }
        }
        nativeConnection.close();
      } catch ( SQLException e ) {
        logger.error( null, e );
      }
    }
    nativeConnection = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#getLastQuery()
   */
  public String getLastQuery() {
    return lastQuery;
  }

  /**
   * Executes the specified query.
   * 
   * @param query
   *          the query to execute
   * @return the resultset from the query
   * @throws SQLException
   *           indicates an error running the query
   * @throws InterruptedException
   *           indicates that the query took longer than the allowed timeout value
   * @throws PentahoSystemException
   */
  public IPentahoResultSet executeQuery( final String query ) throws SQLException, InterruptedException,
    PentahoSystemException {
    return executeQuery( query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
  }

  /**
   * Executes the specified query with the defined parameters
   * 
   * @param query
   *          the query to be executed
   * @param scrollType
   * @param concur
   * @return the result set of data for the query
   * @throws SQLException
   *           indicates an error running the query
   * @throws InterruptedException
   *           indicates the query took longer than allowable by the query timeout
   * @throws PentahoSystemException
   */
  public IPentahoResultSet executeQuery( final String query, final int scrollType, final int concur )
    throws SQLException, InterruptedException, PentahoSystemException {

    if ( this.getReadOnly() ) {
      try {
        nativeConnection.setReadOnly( true );
      } catch ( Exception ignored ) {
        //ignored
      }
    }

    // Create a statement for a scrollable resultset.
    Statement stmt = null;
    ResultSet resultSet = null;
    try {

      stmt = nativeConnection.createStatement( scrollType, concur );
      stmts.add( stmt );
      enhanceStatement( stmt );
      setStatementLimitations( stmt );
      if ( logger != null && logger.getLoggingLevel() == ILogger.DEBUG ) {
        logger.debug( "SQLConnection.executeQuery:" + query ); //$NON-NLS-1$
      }
      resultSet = stmt.executeQuery( query );

    } catch ( Exception e ) {
      // We're going to assume that the problem MIGHT be that a scrolling resultset isn't supported
      // on this connection, then try to fix it up...
      if ( ( scrollType == ResultSet.TYPE_SCROLL_INSENSITIVE ) && ( isFallBackToNonscrollableOnError() ) ) {
        // FORCE forward only
        stmt = nativeConnection.createStatement( ResultSet.TYPE_FORWARD_ONLY, concur );
        stmts.add( stmt );
        enhanceStatement( stmt );
        setStatementLimitations( stmt );
        if ( logger != null && logger.getLoggingLevel() == ILogger.DEBUG ) {
          logger.debug( "SQLConnection.executeQuery(e):" + query ); //$NON-NLS-1$
        }
        resultSet = stmt.executeQuery( query );
        setForcedForwardOnly( true );
      }
    }
    sqlResultSet = new SQLResultSet( resultSet, this );
    // add to list of resultsets for cleanup later.
    resultSets.add( sqlResultSet );
    lastQuery = query;
    return sqlResultSet;
  }

  public IPentahoResultSet prepareAndExecuteQuery( final String query, final List parameters ) throws SQLException {
    return prepareAndExecuteQuery( query, parameters, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
  }

  /**
   * The purpose of this method is to set limitations such as fetchSize and maxrows on the provided statement. If the
   * JDBC driver does not support the setting and throws an Exception, we will re-throw iff the limit was explicitly
   * set.
   * 
   * @param stmt
   *          Either a Statement or PreparedStatement
   * @throws SQLException
   *           , UnsupportedOperationException
   */
  protected void setStatementLimitations( Statement stmt ) throws SQLException {

    if ( this.getFetchSize() >= 0 ) {
      try {
        stmt.setFetchSize( this.getFetchSize() );
      } catch ( Exception ex ) {
        if ( ex instanceof SQLException ) {
          throw (SQLException) ex;
        } else {
          // exception here means either the number was out of bounds or
          // the driver doesn't support this setter.
          throw new UnsupportedOperationException( Messages.getInstance().getErrorString(
            "SQLConnection.ERROR_0003_FETCHSIZE_NOT_SET", Integer.toString( this.getFetchSize() ) ), ex ); //$NON-NLS-1$
        }
      }
    }

    if ( this.getMaxRows() >= 0 ) {
      try {
        stmt.setMaxRows( this.getMaxRows() );
      } catch ( Exception ex ) {
        if ( ex instanceof SQLException ) {
          throw (SQLException) ex;
        } else {
          // exception here means either the number was out of bounds or
          // the driver doesn't support this setter.
          throw new UnsupportedOperationException( Messages.getInstance().getErrorString(
            "SQLConnection.ERROR_0002_ROWLIMIT_NOT_SET", Integer.toString( this.getMaxRows() ) ), ex ); //$NON-NLS-1$
        }
      }
    }

    if ( this.getQueryTimeout() >= 0 ) {
      try {
        stmt.setQueryTimeout( this.getQueryTimeout() );
      } catch ( Exception e ) {
        if ( e instanceof SQLException ) {
          throw (SQLException) e;
        } else {
          throw new UnsupportedOperationException( Messages.getInstance().getErrorString(
            "SQLConnection.ERROR_0001_TIMEOUT_NOT_SET", Integer.toString( this.getQueryTimeout() ) ), e ); //$NON-NLS-1$
        }
      }
    }
  }

  public IPentahoResultSet prepareAndExecuteQuery( final String query, final List parameters, final int scrollType,
      final int concur ) throws SQLException {

    if ( this.getReadOnly() ) {
      try {
        nativeConnection.setReadOnly( true );
      } catch ( Exception ignored ) {
        //ignored
      }
    }

    // Create a prepared statement
    PreparedStatement pStmt = null;
    ResultSet resultSet = null;
    try {
      if ( logger != null && logger.getLoggingLevel() == ILogger.DEBUG ) {
        logger.debug( "SQLConnection.prepareAndExecuteQuery:" + query ); //$NON-NLS-1$
      }

      pStmt = nativeConnection.prepareStatement( query, scrollType, concur );
      // add to stmts list for closing when connection closes
      stmts.add( pStmt );
      enhanceStatement( pStmt );
      setStatementLimitations( pStmt );
      for ( int i = 0; i < parameters.size(); i++ ) {
        pStmt.setObject( i + 1, parameters.get( i ) );
      }
      resultSet = pStmt.executeQuery();

    } catch ( Exception e ) {
      // attempt to remove the offending statement...
      stmts.remove( pStmt );
      if ( ( scrollType == ResultSet.TYPE_SCROLL_INSENSITIVE ) && ( isFallBackToNonscrollableOnError() ) ) {
        // FORCE forward only
        if ( logger != null && logger.getLoggingLevel() == ILogger.DEBUG ) {
          logger.debug( "SQLConnection.prepareAndExecuteQuery(e):" + query ); //$NON-NLS-1$
        }
        pStmt = nativeConnection.prepareStatement( query, ResultSet.TYPE_FORWARD_ONLY, concur );
        // add to stmts list for closing when connection closes
        stmts.add( pStmt );
        enhanceStatement( pStmt );
        setStatementLimitations( pStmt );
        for ( int i = 0; i < parameters.size(); i++ ) {
          pStmt.setObject( i + 1, parameters.get( i ) );
        }
        resultSet = pStmt.executeQuery();
        setForcedForwardOnly( true );
      }
    }

    sqlResultSet = new SQLResultSet( resultSet, this );
    // add to list of resultsets for cleanup later.
    resultSets.add( sqlResultSet );
    lastQuery = query;
    return sqlResultSet;
  }

  public boolean preparedQueriesSupported() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#isClosed()
   */
  public boolean isClosed() {
    try {
      return nativeConnection.isClosed();
    } catch ( SQLException e ) {
      logger.error( null, e );
    }
    return true; // assume since we couldn't get here if it
    // was open then we must be closed.
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#isReadOnly()
   * 
   * Right now this archetecture only support selects (read only)
   */
  public boolean isReadOnly() {
    return true;
  }

  public void clearWarnings() {
    try {
      nativeConnection.clearWarnings();
    } catch ( SQLException e ) {
      logger.error( null, e );
    }
  }

  public IPentahoResultSet getResultSet() {
    return sqlResultSet;
  }

  public boolean connect( final Properties props ) {
    close();
    String jndiName = props.getProperty( IPentahoConnection.JNDI_NAME_KEY );
    if ( ( jndiName != null ) && ( jndiName.length() > 0 ) ) {
      initWithJNDI( jndiName );
    } else {
      String driver = props.getProperty( IPentahoConnection.DRIVER_KEY );
      String provider = props.getProperty( IPentahoConnection.LOCATION_KEY );
      String userName = props.getProperty( IPentahoConnection.USERNAME_KEY );
      String password = props.getProperty( IPentahoConnection.PASSWORD_KEY );
      init( driver, provider, userName, password );
      String query = props.getProperty( IPentahoConnection.QUERY_KEY );
      if ( ( query != null ) && ( query.length() > 0 ) ) {
        try {
          executeQuery( query );
        } catch ( Exception e ) {
          logger.error( null, e );
        }
      }
    }
    return ( ( nativeConnection != null ) && !isClosed() );
  }

  public int execute( final String query ) throws SQLException {
    return execute( query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
  }

  public int execute( final String query, final int scrollType, final int concur ) throws SQLException {

    // Create a statement for a scrollable resultset.
    Statement stmt = nativeConnection.createStatement( scrollType, concur );

    // add to stmts list for closing when connection closes
    enhanceStatement( stmt );
    stmts.add( stmt );

    setStatementLimitations( stmt );

    if ( logger != null && logger.getLoggingLevel() == ILogger.DEBUG ) {
      logger.debug( "SQLConnection.execute:" + query ); //$NON-NLS-1$
    }

    int result = stmt.executeUpdate( query );
    lastQuery = query;
    return result;
  }

  /**
   * @return Returns the nativeConnection.
   */
  public Connection getNativeConnection() {
    return nativeConnection;
  }

  /**
   * @return Returns the fetchSize.
   */
  public int getFetchSize() {
    return fetchSize;
  }

  /**
   * @param fetchSize
   *          The fetchSize to set.
   */
  public void setFetchSize( final int fetchSize ) {
    this.fetchSize = fetchSize;
  }

  /**
   * @return Returns the maxRows.
   */
  public int getMaxRows() {
    return maxRows;
  }

  /**
   * @param maxRows
   *          The maxRows to set.
   */
  public void setMaxRows( final int maxRows ) {
    this.maxRows = maxRows;
  }

  /**
   * Returns the state of the readonly flag
   * 
   * @return true if the connection is set to readonly
   */
  public boolean getReadOnly() {
    return this.readOnly;
  }

  public void setFallBackToNonscrollableOnError( boolean fallBackToNonscrollableOnError ) {
    this.fallBackToNonscrollableOnError = fallBackToNonscrollableOnError;
  }

  public boolean isFallBackToNonscrollableOnError() {
    return fallBackToNonscrollableOnError;
  }

  public boolean isForcedForwardOnly() {
    return forcedForwardOnly;
  }

  public void setForcedForwardOnly( boolean forcedForwardOnly ) {
    this.forcedForwardOnly = forcedForwardOnly;
  }

}
