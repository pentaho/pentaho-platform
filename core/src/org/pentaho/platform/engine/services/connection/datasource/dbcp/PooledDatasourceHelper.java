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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.lang.math.NumberUtils;
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

import javax.sql.DataSource;
import java.util.Map;

public class PooledDatasourceHelper {

  public static PoolingDataSource setupPooledDataSource( IDatabaseConnection databaseConnection )
    throws DBDatasourceServiceException {
    PoolingDataSource poolingDataSource = null;
    String driverClass = null;
    String url = null;
    try {
      ICacheManager cacheManager = PentahoSystem.getCacheManager( null );
      IDatabaseDialectService databaseDialectService = PentahoSystem.get( IDatabaseDialectService.class );
      if(databaseDialectService == null) {
        Logger.warn( PooledDatasourceHelper.class,
            "Unable to pool datasource with name " + databaseConnection.getName() + "Cause:Database Dialect Service is not available" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return null;
      }
      IDatabaseDialect dialect = databaseDialectService.getDialect( databaseConnection );
      if(dialect == null || dialect.getDatabaseType() == null) {
        Logger.warn( PooledDatasourceHelper.class,
            "Unable to pool datasource with name " + databaseConnection.getName() + "Cause:Unable to find Database Dialect" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

      // Read default connecion pooling parameter
      String maxdleConn = PentahoSystem.getSystemSetting( "dbcp-defaults/max-idle-conn", null ); //$NON-NLS-1$ 
      String minIdleConn = PentahoSystem.getSystemSetting( "dbcp-defaults/min-idle-conn", null ); //$NON-NLS-1$    
      String maxActConn = PentahoSystem.getSystemSetting( "dbcp-defaults/max-act-conn", null ); //$NON-NLS-1$
      String validQuery = null;
      String whenExhaustedAction = PentahoSystem.getSystemSetting( "dbcp-defaults/when-exhausted-action", null ); //$NON-NLS-1$
      String wait = PentahoSystem.getSystemSetting( "dbcp-defaults/wait", null ); //$NON-NLS-1$
      String testWhileIdleValue = PentahoSystem.getSystemSetting( "dbcp-defaults/test-while-idle", null ); //$NON-NLS-1$
      String testOnBorrowValue = PentahoSystem.getSystemSetting( "dbcp-defaults/test-on-borrow", null ); //$NON-NLS-1$
      String testOnReturnValue = PentahoSystem.getSystemSetting( "dbcp-defaults/test-on-return", null ); //$NON-NLS-1$
      boolean testWhileIdle =
          !StringUtil.isEmpty( testWhileIdleValue ) ? Boolean.parseBoolean( testWhileIdleValue ) : false;
      boolean testOnBorrow =
          !StringUtil.isEmpty( testOnBorrowValue ) ? Boolean.parseBoolean( testOnBorrowValue ) : false;
      boolean testOnReturn =
          !StringUtil.isEmpty( testOnReturnValue ) ? Boolean.parseBoolean( testOnReturnValue ) : false;
      int maxActiveConnection = -1;
      long waitTime = -1;
      byte whenExhaustedActionType = -1;
      int minIdleConnection = !StringUtil.isEmpty( minIdleConn ) ? Integer.parseInt( minIdleConn ) : -1;
      int maxIdleConnection = !StringUtil.isEmpty( maxdleConn ) ? Integer.parseInt( maxdleConn ) : -1;

      Map<String, String> attributes = databaseConnection.getConnectionPoolingProperties();

      if ( attributes.containsKey( IDBDatasourceService.MAX_ACTIVE_KEY ) ) {
        maxActiveConnection = Integer.parseInt( attributes.get( IDBDatasourceService.MAX_ACTIVE_KEY ) );
      } else {
        if ( !StringUtil.isEmpty( maxActConn ) ) {
          maxActiveConnection = Integer.parseInt( maxActConn );
        }
      }
      if ( attributes.containsKey( IDBDatasourceService.MAX_WAIT_KEY ) ) {
        waitTime = Integer.parseInt( attributes.get( IDBDatasourceService.MAX_WAIT_KEY ) );
      } else {
        if ( !StringUtil.isEmpty( wait ) ) {
          waitTime = Long.parseLong( wait );
        }
      }
      if(attributes.containsKey( IDBDatasourceService.MIN_IDLE_KEY )  
        && NumberUtils.isDigits( attributes.get( IDBDatasourceService.MIN_IDLE_KEY ) ) ) {
          minIdleConnection = Integer.parseInt( attributes.get( IDBDatasourceService.MIN_IDLE_KEY ) );
      }
      if(attributes.containsKey( IDBDatasourceService.MAX_IDLE_KEY )  
        && NumberUtils.isDigits( attributes.get( IDBDatasourceService.MAX_IDLE_KEY ) ) ) {
          maxIdleConnection = Integer.parseInt( attributes.get( IDBDatasourceService.MAX_IDLE_KEY ) );
      }
      if ( attributes.containsKey( IDBDatasourceService.QUERY_KEY ) ) {
        validQuery = attributes.get( IDBDatasourceService.QUERY_KEY );
      }
      if ( !StringUtil.isEmpty( whenExhaustedAction ) ) {
        whenExhaustedActionType = Byte.parseByte( whenExhaustedAction );
      } else {
        whenExhaustedActionType = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
      }
      poolingDataSource = new PoolingDataSource();
      Class.forName( driverClass );
      // As the name says, this is a generic pool; it returns basic Object-class objects.
      final GenericObjectPool pool = new GenericObjectPool( null );
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
      /*
       * ConnectionFactory creates connections on behalf of the pool. Here, we use the
       * DriverManagerConnectionFactory because that essentially uses DriverManager as the source of connections.
       */
      ConnectionFactory factory =
          new DriverManagerConnectionFactory( url, databaseConnection.getUsername(), databaseConnection.getPassword() );

      /*
       * Puts pool-specific wrappers on factory connections. For clarification: "[PoolableConnection]Factory," not
       * "Poolable[ConnectionFactory]."
       */
      PoolableConnectionFactory pcf = new PoolableConnectionFactory( factory, // ConnectionFactory
          pool, // ObjectPool
          null, // KeyedObjectPoolFactory
          validQuery, // String (validation query)
          false, // boolean (default to read-only?)
          true // boolean (default to auto-commit statements?)
      );

      /*
       * initialize the pool to X connections
       */
      Logger
          .debug(
              PooledDatasourceHelper.class,
              "Pool defaults to " + maxActiveConnection + " max active/" + maxIdleConnection + "max idle" + "with " + waitTime + "wait time"//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                  + " idle connections." ); //$NON-NLS-1$

      for ( int i = 0; i < maxIdleConnection; ++i ) {
        pool.addObject();
      }
      Logger.debug( PooledDatasourceHelper.class,
          "Pool now has " + pool.getNumActive() + " active/" + pool.getNumIdle() + " idle connections." ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      /*
       * All of this is wrapped in a DataSource, which client code should already know how to handle (since it's
       * the same class of object they'd fetch via the container's JNDI tree
       */
      poolingDataSource.setPool( pool );

      // store the pool, so we can get to it later
      cacheManager.putInRegionCache( IDBDatasourceService.JDBC_POOL, databaseConnection.getName(), pool );
      return ( poolingDataSource );
    } catch ( Exception e ) {
      throw new DBDatasourceServiceException( e );
    }
  }

  public static DataSource convert( IDatabaseConnection databaseConnection ) {
    BasicDataSource basicDatasource = new BasicDataSource();
    IDatabaseDialectService databaseDialectService =
        PentahoSystem.get( IDatabaseDialectService.class, PentahoSessionHolder.getSession() );
    IDatabaseDialect dialect = databaseDialectService.getDialect( databaseConnection );
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
    Map<String, String> attributes = databaseConnection.getAttributes();
    if ( attributes.containsKey( IDBDatasourceService.MAX_ACTIVE_KEY ) ) {
      String value = attributes.get( IDBDatasourceService.MAX_ACTIVE_KEY );
      basicDatasource.setMaxActive( Integer.parseInt( value ) );
    }
    if ( attributes.containsKey( IDBDatasourceService.MAX_WAIT_KEY ) ) {
      String value = attributes.get( IDBDatasourceService.MAX_WAIT_KEY );
      basicDatasource.setMaxWait( Integer.parseInt( value ) );
    }
    if ( attributes.containsKey( IDBDatasourceService.MAX_IDLE_KEY ) ) {
      String value = attributes.get( IDBDatasourceService.MAX_IDLE_KEY );
      basicDatasource.setMaxIdle( Integer.parseInt( value ) );
    }
    if ( attributes.containsKey( IDBDatasourceService.MIN_IDLE_KEY ) ) {
      String value = attributes.get( IDBDatasourceService.MIN_IDLE_KEY );
      basicDatasource.setMinIdle( Integer.parseInt( value ) );
    }
    if ( attributes.containsKey( IDBDatasourceService.QUERY_KEY ) ) {
      basicDatasource.setValidationQuery( attributes.get( IDBDatasourceService.QUERY_KEY ) );
    }
    return basicDatasource;
  }
}
