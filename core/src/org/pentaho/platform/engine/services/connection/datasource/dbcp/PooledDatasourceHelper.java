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

package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.AbandonedObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.pentaho.database.DatabaseDialectException;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.dialect.GenericDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.logging.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.Isolation;

public class PooledDatasourceHelper {

  public static PoolingDataSource setupPooledDataSource( IDatabaseConnection databaseConnection )
    throws DBDatasourceServiceException {
    PoolingDataSource poolingDataSource = null;
    String driverClass = null;
    String url = null;
    try {
      ICacheManager cacheManager = PentahoSystem.getCacheManager( null );
      IDatabaseDialectService databaseDialectService = PentahoSystem.get( IDatabaseDialectService.class );
      if ( databaseDialectService == null ) {
        Logger.warn( PooledDatasourceHelper.class,
          "Unable to pool datasource with name " + databaseConnection.getName()
            + "Cause:Database Dialect Service is not available" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return null;
      }
      IDatabaseDialect dialect = databaseDialectService.getDialect( databaseConnection );
      if ( dialect == null || dialect.getDatabaseType() == null ) {
        Logger.warn( PooledDatasourceHelper.class,
          "Unable to pool datasource with name " + databaseConnection.getName()
            + "Cause:Unable to find Database Dialect" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return null;
      }
      if ( databaseConnection.getDatabaseType().getShortName().equals( "GENERIC" ) ) { //$NON-NLS-1$
        driverClass = databaseConnection.getAttributes().get( GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS );
      } else {
        driverClass = dialect.getNativeDriver();
      }
      try {
        url = dialect.getURLWithExtraOptions( databaseConnection );
      } catch ( DatabaseDialectException e ) {
        url = null;
      }

      // Read default connection pooling parameter
      String maxdleConn = PentahoSystem.getSystemSetting( "dbcp-defaults/max-idle-conn", null ); //$NON-NLS-1$ 
      String minIdleConn = PentahoSystem.getSystemSetting( "dbcp-defaults/min-idle-conn", null ); //$NON-NLS-1$    
      String maxActConn = PentahoSystem.getSystemSetting( "dbcp-defaults/max-act-conn", null ); //$NON-NLS-1$
      String validQuery = null;
      String whenExhaustedAction =
        PentahoSystem.getSystemSetting( "dbcp-defaults/when-exhausted-action", null ); //$NON-NLS-1$
      String wait = PentahoSystem.getSystemSetting( "dbcp-defaults/wait", null ); //$NON-NLS-1$
      String testWhileIdleValue = PentahoSystem.getSystemSetting( "dbcp-defaults/test-while-idle", null ); //$NON-NLS-1$
      String testOnBorrowValue = PentahoSystem.getSystemSetting( "dbcp-defaults/test-on-borrow", null ); //$NON-NLS-1$
      String testOnReturnValue = PentahoSystem.getSystemSetting( "dbcp-defaults/test-on-return", null ); //$NON-NLS-1$

      // property initialization
      boolean testWhileIdle =
        !StringUtil.isEmpty( testWhileIdleValue ) ? Boolean.parseBoolean( testWhileIdleValue ) : false;
      boolean testOnBorrow =
        !StringUtil.isEmpty( testOnBorrowValue ) ? Boolean.parseBoolean( testOnBorrowValue ) : false;
      boolean testOnReturn =
        !StringUtil.isEmpty( testOnReturnValue ) ? Boolean.parseBoolean( testOnReturnValue ) : false;
      int maxActiveConnection = !StringUtil.isEmpty( maxActConn ) ? Integer.parseInt( maxActConn ) : -1;
      long waitTime = !StringUtil.isEmpty( wait ) ? Integer.parseInt( wait ) : -1;
      byte whenExhaustedActionType = !StringUtil.isEmpty( whenExhaustedAction ) ?
        Byte.parseByte( whenExhaustedAction ) : GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
      int minIdleConnection = !StringUtil.isEmpty( minIdleConn ) ? Integer.parseInt( minIdleConn ) : -1;
      int maxIdleConnection = !StringUtil.isEmpty( maxdleConn ) ? Integer.parseInt( maxdleConn ) : -1;

      // setting properties according to user specifications 
      Map<String, String> attributes = databaseConnection.getConnectionPoolingProperties();

      if ( attributes.containsKey( IDBDatasourceService.MAX_ACTIVE_KEY )
        && NumberUtils.isNumber( attributes.get( IDBDatasourceService.MAX_ACTIVE_KEY ) ) ) {
        maxActiveConnection = Integer.parseInt( attributes.get( IDBDatasourceService.MAX_ACTIVE_KEY ) );
      }
      if ( attributes.containsKey( IDBDatasourceService.MAX_WAIT_KEY )
        && NumberUtils.isNumber( attributes.get( IDBDatasourceService.MAX_WAIT_KEY ) ) ) {
        waitTime = Integer.parseInt( attributes.get( IDBDatasourceService.MAX_WAIT_KEY ) );
      }
      if ( attributes.containsKey( IDBDatasourceService.MIN_IDLE_KEY )
        && NumberUtils.isNumber( attributes.get( IDBDatasourceService.MIN_IDLE_KEY ) ) ) {
        minIdleConnection = Integer.parseInt( attributes.get( IDBDatasourceService.MIN_IDLE_KEY ) );
      }
      if ( attributes.containsKey( IDBDatasourceService.MAX_IDLE_KEY )
        && NumberUtils.isNumber( attributes.get( IDBDatasourceService.MAX_IDLE_KEY ) ) ) {
        maxIdleConnection = Integer.parseInt( attributes.get( IDBDatasourceService.MAX_IDLE_KEY ) );
      }
      if ( attributes.containsKey( IDBDatasourceService.QUERY_KEY ) ) {
        validQuery = attributes.get( IDBDatasourceService.QUERY_KEY );
      }
      if ( attributes.containsKey( IDBDatasourceService.TEST_ON_BORROW ) ) {
        testOnBorrow = Boolean.parseBoolean( attributes.get( IDBDatasourceService.TEST_ON_BORROW ) );
      }
      if ( attributes.containsKey( IDBDatasourceService.TEST_ON_RETURN ) ) {
        testOnReturn = Boolean.parseBoolean( attributes.get( IDBDatasourceService.TEST_ON_RETURN ) );
      }
      if ( attributes.containsKey( IDBDatasourceService.TEST_WHILE_IDLE ) ) {
        testWhileIdle = Boolean.parseBoolean( attributes.get( IDBDatasourceService.TEST_WHILE_IDLE ) );
      }

      poolingDataSource = new PoolingDataSource();
      Class.forName( driverClass );
      // As the name says, this is a generic pool; it returns basic Object-class objects.
      GenericObjectPool pool = new GenericObjectPool( null );

      // if removedAbandoned = true, then an AbandonedObjectPool object will take GenericObjectPool's place
      if ( attributes.containsKey( IDBDatasourceService.REMOVE_ABANDONED )
        && true == Boolean.parseBoolean( attributes.get( IDBDatasourceService.REMOVE_ABANDONED ) ) ) {

        AbandonedConfig config = new AbandonedConfig();
        config.setRemoveAbandoned( Boolean.parseBoolean( attributes.get( IDBDatasourceService.REMOVE_ABANDONED ) ) );

        if ( attributes.containsKey( IDBDatasourceService.LOG_ABANDONED ) ) {
          config.setLogAbandoned( Boolean.parseBoolean( attributes.get( IDBDatasourceService.LOG_ABANDONED ) ) );
        }

        if ( attributes.containsKey( IDBDatasourceService.REMOVE_ABANDONED_TIMEOUT )
          && NumberUtils.isNumber( attributes.get( IDBDatasourceService.REMOVE_ABANDONED_TIMEOUT ) ) ) {
          config.setRemoveAbandonedTimeout(
            Integer.parseInt( attributes.get( IDBDatasourceService.REMOVE_ABANDONED_TIMEOUT ) ) );
        }

        pool = new AbandonedObjectPool( null, config );
      }

      pool.setWhenExhaustedAction( whenExhaustedActionType );

      // Tuning the connection pool
      pool.setMaxActive( maxActiveConnection );
      pool.setMaxIdle( maxIdleConnection );
      pool.setMaxWait( waitTime );
      pool.setMinIdle( minIdleConnection );
      pool.setTestWhileIdle( testWhileIdle );
      pool.setTestOnReturn( testOnReturn );
      pool.setTestOnBorrow( testOnBorrow );
      pool.setTestWhileIdle( testWhileIdle );

      if ( attributes.containsKey( IDBDatasourceService.TIME_BETWEEN_EVICTION_RUNS_MILLIS )
        && NumberUtils.isNumber( attributes.get( IDBDatasourceService.TIME_BETWEEN_EVICTION_RUNS_MILLIS ) ) ) {
        pool.setTimeBetweenEvictionRunsMillis(
          Long.parseLong( attributes.get( IDBDatasourceService.TIME_BETWEEN_EVICTION_RUNS_MILLIS ) ) );
      }
      
      /*
       * ConnectionFactory creates connections on behalf of the pool. Here, we use the
       * DriverManagerConnectionFactory because that essentially uses DriverManager as the source of connections.
       */
      ConnectionFactory factory =
        new DriverManagerConnectionFactory( url, databaseConnection.getUsername(), databaseConnection.getPassword() );


      boolean defaultReadOnly = attributes.containsKey( IDBDatasourceService.DEFAULT_READ_ONLY ) ?
        Boolean.parseBoolean( attributes.get( IDBDatasourceService.TEST_WHILE_IDLE ) ) : false; // default to false

      boolean defaultAutoCommit = attributes.containsKey( IDBDatasourceService.DEFAULT_AUTO_COMMIT ) ?
        Boolean.parseBoolean( attributes.get( IDBDatasourceService.DEFAULT_AUTO_COMMIT ) ) : true; // default to true

      KeyedObjectPoolFactory kopf = null;

      if ( attributes.containsKey( IDBDatasourceService.POOL_PREPARED_STATEMENTS )
        && true == Boolean.parseBoolean( attributes.get( IDBDatasourceService.POOL_PREPARED_STATEMENTS ) ) ) {

        int maxOpenPreparedStatements = -1; // unlimited

        if ( attributes.containsKey( IDBDatasourceService.MAX_OPEN_PREPARED_STATEMENTS )
          && NumberUtils.isNumber( attributes.get( IDBDatasourceService.MAX_OPEN_PREPARED_STATEMENTS ) ) ) {

          maxOpenPreparedStatements =
            Integer.parseInt( attributes.get( IDBDatasourceService.MAX_OPEN_PREPARED_STATEMENTS ) );
        }

        kopf = new GenericKeyedObjectPoolFactory(
          null,
          pool.getMaxActive(),
          pool.getWhenExhaustedAction(),
          pool.getMaxWait(),
          pool.getMaxIdle(),
          maxOpenPreparedStatements );
      }
            
      /*
       * Puts pool-specific wrappers on factory connections. For clarification: "[PoolableConnection]Factory," not
       * "Poolable[ConnectionFactory]."
       */
      PoolableConnectionFactory pcf = new PoolableConnectionFactory( factory, // ConnectionFactory
        pool, // ObjectPool
        kopf, // KeyedObjectPoolFactory
        validQuery, // String (validation query)
        defaultReadOnly, // boolean (default to read-only?)
        defaultAutoCommit // boolean (default to auto-commit statements?)
      );

      if ( attributes.containsKey( IDBDatasourceService.DEFAULT_TRANSACTION_ISOLATION )
        && !IDBDatasourceService.TRANSACTION_ISOLATION_NONE_VALUE
        .equalsIgnoreCase( attributes.get( IDBDatasourceService.DEFAULT_TRANSACTION_ISOLATION ) ) ) {
        Isolation isolationLevel =
          Isolation.valueOf( attributes.get( IDBDatasourceService.DEFAULT_TRANSACTION_ISOLATION ) );

        if ( isolationLevel != null ) {
          pcf.setDefaultTransactionIsolation( isolationLevel.value() );
        }
      }

      if ( attributes.containsKey( IDBDatasourceService.DEFAULT_CATALOG ) ) {
        pcf.setDefaultCatalog( attributes.get( IDBDatasourceService.DEFAULT_CATALOG ) );
      }
      
      /*
       * initialize the pool to X connections
       */
      Logger
        .debug(
          PooledDatasourceHelper.class,
          "Pool defaults to " + maxActiveConnection + " max active/" + maxIdleConnection + "max idle" + "with "
            + waitTime + "wait time"//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            + " idle connections." ); //$NON-NLS-1$

      for ( int i = 0; i < maxIdleConnection; ++i ) {
        pool.addObject();
      }
      Logger.debug( PooledDatasourceHelper.class,
        "Pool now has " + pool.getNumActive() + " active/" + pool.getNumIdle()
          + " idle connections." ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      /*
       * All of this is wrapped in a DataSource, which client code should already know how to handle (since it's
       * the same class of object they'd fetch via the container's JNDI tree
       */
      poolingDataSource.setPool( pool );

      if ( attributes.containsKey( IDBDatasourceService.ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED ) ) {
        poolingDataSource.setAccessToUnderlyingConnectionAllowed(
          Boolean.parseBoolean( attributes.get( IDBDatasourceService.ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED ) ) );
      }

      // store the pool, so we can get to it later
      cacheManager.putInRegionCache( IDBDatasourceService.JDBC_POOL, databaseConnection.getName(), pool );
      return ( poolingDataSource );
    } catch ( Exception e ) {
      throw new DBDatasourceServiceException( e );
    }
  }

  public static DataSource convert( IDatabaseConnection databaseConnection ) throws DBDatasourceServiceException {
    DriverManagerDataSource basicDatasource = new DriverManagerDataSource(); // From Spring
    IDatabaseDialectService databaseDialectService =
      PentahoSystem.get( IDatabaseDialectService.class, PentahoSessionHolder.getSession() );
    IDatabaseDialect dialect = databaseDialectService.getDialect( databaseConnection );
    if ( databaseConnection.getDatabaseType() == null && dialect == null ) {
      // We do not have enough information to create a DataSource. Throwing exception
      throw new DBDatasourceServiceException(
        "Unable to create DataSource for connection: " + databaseConnection.getName() );
    }

    if ( databaseConnection.getDatabaseType().getShortName().equals( "GENERIC" ) ) { //$NON-NLS-1$
      basicDatasource.setDriverClassName( databaseConnection.getAttributes().get(
        GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS ) );
    } else {
      basicDatasource.setDriverClassName( dialect.getNativeDriver() );
    }
    try {
      basicDatasource.setUrl( dialect.getURLWithExtraOptions( databaseConnection ) );
    } catch ( DatabaseDialectException e ) {
      basicDatasource.setUrl( null );
    }
    basicDatasource.setUsername( databaseConnection.getUsername() );
    basicDatasource.setPassword( databaseConnection.getPassword() );

    return basicDatasource;
  }

  public static DataSource getJndiDataSource( final String dsName ) throws DBDatasourceServiceException {

    try {
      InitialContext ctx = new InitialContext();
      Object lkup = null;
      DataSource rtn = null;
      NamingException firstNe = null;
      // First, try what they ask for...
      try {
        lkup = ctx.lookup( dsName );
        if ( lkup != null ) {
          rtn = (DataSource) lkup;
          return rtn;
        }
      } catch ( NamingException ignored ) {
        firstNe = ignored;
      }
      try {
        // Needed this for Jboss
        lkup = ctx.lookup( "java:" + dsName ); //$NON-NLS-1$
        if ( lkup != null ) {
          rtn = (DataSource) lkup;
          return rtn;
        }
      } catch ( NamingException ignored ) {
        //ignored
      }
      try {
        // Tomcat
        lkup = ctx.lookup( "java:comp/env/jdbc/" + dsName ); //$NON-NLS-1$
        if ( lkup != null ) {
          rtn = (DataSource) lkup;
          return rtn;
        }
      } catch ( NamingException ignored ) {
        //ignored
      }
      try {
        // Others?
        lkup = ctx.lookup( "jdbc/" + dsName ); //$NON-NLS-1$
        if ( lkup != null ) {
          rtn = (DataSource) lkup;
          return rtn;
        }
      } catch ( NamingException ignored ) {
        //ignored
      }
      if ( firstNe != null ) {
        throw new DBDatasourceServiceException( firstNe );
      }
      throw new DBDatasourceServiceException( dsName );
    } catch ( NamingException ne ) {
      throw new DBDatasourceServiceException( ne );
    }
  }

}
