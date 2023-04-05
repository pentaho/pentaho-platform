/*!
 *
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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.AbandonedObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.pentaho.database.DatabaseDialectException;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.IDriverLocator;
import org.pentaho.database.dialect.GenericDatabaseDialect;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.plugin.action.kettle.PoolingManagedDataSource;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.logging.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.Isolation;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

public class PooledDatasourceHelper {

  public static PoolingDataSource setupPooledDataSource( IDatabaseConnection databaseConnection )
    throws DBDatasourceServiceException {
    
    try {
      if ( databaseConnection.getAccessType().equals( DatabaseAccessType.JNDI ) ) {
        throwDBDatasourceServiceException( databaseConnection.getName(), "PooledDatasourceHelper.ERROR_0008_UNABLE_TO_POOL_DATASOURCE_IT_IS_JNDI" );
      }
      IDatabaseDialect dialect = getDatabaseDialect( databaseConnection );
      String driverClass = getDriverClass( databaseConnection, dialect );
      loadDriverClass( databaseConnection, dialect, driverClass );

      PoolingManagedDataSource poolingDataSource = new PoolingManagedDataSource( databaseConnection, dialect );

      ICacheManager cacheManager = PentahoSystem.getCacheManager( null );
      cacheManager.putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, databaseConnection.getName(), poolingDataSource );
      return poolingDataSource;
    } catch ( Exception e ) {
      throw new DBDatasourceServiceException( e );
    }
  }

  private static void loadDriverClass( IDatabaseConnection databaseConnection, IDatabaseDialect dialect, String driverClass ) throws ClassNotFoundException {
    if ( dialect instanceof IDriverLocator ) {
      if ( !( (IDriverLocator) dialect ).initialize( driverClass ) ) {
        throw new DriverNotInitializedException( Messages.getInstance()
          .getErrorString( "PooledDatasourceHelper.ERROR_0009_UNABLE_TO_POOL_DATASOURCE_CANT_INITIALIZE",
            databaseConnection.getName(), driverClass ) );
      }
    } else {
      Class.forName( driverClass );
    }
  }

  public static GenericObjectPool createGenericPool( IDatabaseConnection databaseConnection, IDatabaseDialect dialect, Map<String, String> attributes ) throws Exception {
    // As the name says, this is a generic pool; it returns basic Object-class objects.
    GenericObjectPool pool = initializeObjectPool( attributes );
    configurePool( databaseConnection, dialect, attributes, pool );

    return pool;
  }

  private static void configurePool( IDatabaseConnection databaseConnection, IDatabaseDialect dialect, Map<String, String> attributes, GenericObjectPool pool ) throws Exception {
    int maxIdleConnection = getIntegerPropertyValue( attributes, IDBDatasourceService.MAX_IDLE_KEY, PentahoSystem.getSystemSetting( "dbcp-defaults/max-idle-conn", null) );
    int minIdleConnection = getIntegerPropertyValue( attributes, IDBDatasourceService.MIN_IDLE_KEY, PentahoSystem.getSystemSetting( "dbcp-defaults/min-idle-conn", null) );
    int maxActiveConnection = getIntegerPropertyValue( attributes, IDBDatasourceService.MAX_ACTIVE_KEY, PentahoSystem.getSystemSetting( "dbcp-defaults/max-act-conn", null) );
    long waitTime = getLongPropertyValue( attributes, IDBDatasourceService.MAX_WAIT_KEY, PentahoSystem.getSystemSetting( "dbcp-defaults/wait", null) );
    boolean testWhileIdle = getBooleanPropertyValue( attributes, IDBDatasourceService.TEST_WHILE_IDLE, PentahoSystem.getSystemSetting( "dbcp-defaults/test-while-idle", null) );
    boolean testOnBorrow = getBooleanPropertyValue( attributes, IDBDatasourceService.TEST_ON_BORROW, PentahoSystem.getSystemSetting( "dbcp-defaults/test-on-borrow", null) );
    boolean testOnReturn = getBooleanPropertyValue( attributes, IDBDatasourceService.TEST_ON_RETURN, PentahoSystem.getSystemSetting( "dbcp-defaults/test-on-return", null) );
    byte whenExhaustedActionType = getWhenExhaustedActionType();

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
    setTimeBetweenEvictionRunsMillis( attributes, pool );

    /*
     * Puts pool-specific wrappers on factory connections. For clarification: "[PoolableConnection]Factory," not
     * "Poolable[ConnectionFactory]."
     */
    setupPoolableConnectionFactory( databaseConnection, dialect, attributes, pool );

    Logger.debug( PooledDatasourceHelper.class, "Pool defaults to " + maxActiveConnection + " max active/"
        + maxIdleConnection + "max idle" + "with " + waitTime + "wait time"//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        + " idle connections." ); //$NON-NLS-1$

    // initialize the pool to X connections
    prePopulatePool( pool, maxIdleConnection );

    Logger.debug( PooledDatasourceHelper.class, "Pool now has " + pool.getNumActive() + " active/"
        + pool.getNumIdle() + " idle connections." ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  private static void prePopulatePool( GenericObjectPool pool, int maxIdleConnection ) throws Exception {
    String prePopulatePoolStr = PentahoSystem.getSystemSetting( "dbcp-defaults/pre-populate-pool", null );
    if ( Boolean.parseBoolean( prePopulatePoolStr ) ) {
      for (int i = 0; i < maxIdleConnection; ++i ) {
        pool.addObject();
      }
      Logger.debug( PooledDatasourceHelper.class,
              "Pool has been pre-populated with " + maxIdleConnection + " connections" );
    }
  }

  private static void setupPoolableConnectionFactory( IDatabaseConnection databaseConnection, IDatabaseDialect dialect, Map<String, String> attributes, GenericObjectPool pool ) {
    String url = getUrl( databaseConnection, dialect );
    String validQuery = getValidQuery( attributes );
    /*
     * ConnectionFactory creates connections on behalf of the pool. Here, we use the DriverManagerConnectionFactory
     * because that essentially uses DriverManager as the source of connections.
     */
    ConnectionFactory factory = getConnectionFactory( databaseConnection, url );

    boolean defaultReadOnly =
            attributes.containsKey( IDBDatasourceService.DEFAULT_READ_ONLY )
                    && Boolean.parseBoolean( attributes.get( IDBDatasourceService.DEFAULT_READ_ONLY ) ); // default to false

    boolean defaultAutoCommit =
            !attributes.containsKey( IDBDatasourceService.DEFAULT_AUTO_COMMIT )
                    || Boolean.parseBoolean( attributes.get( IDBDatasourceService.DEFAULT_AUTO_COMMIT ) ); // default to true

    KeyedObjectPoolFactory kopf = getKeyedObjectPoolFactory( attributes, pool );

    PoolableConnectionFactory pcf = new PoolableConnectionFactory(
            factory, // ConnectionFactory
            pool, // ObjectPool
            kopf, // KeyedObjectPoolFactory
            validQuery, // String (validation query)
            defaultReadOnly, // boolean (default to read-only?)
            defaultAutoCommit // boolean (default to auto-commit statements?)
    );

    if ( attributes.containsKey( IDBDatasourceService.DEFAULT_TRANSACTION_ISOLATION )
            && !IDBDatasourceService.TRANSACTION_ISOLATION_NONE_VALUE.equalsIgnoreCase( attributes
            .get( IDBDatasourceService.DEFAULT_TRANSACTION_ISOLATION ) ) ) {
      Isolation isolationLevel =
          Isolation.valueOf( attributes.get( IDBDatasourceService.DEFAULT_TRANSACTION_ISOLATION ) );

        pcf.setDefaultTransactionIsolation( isolationLevel.value() );
    }

    if ( attributes.containsKey( IDBDatasourceService.DEFAULT_CATALOG ) ) {
      pcf.setDefaultCatalog( attributes.get( IDBDatasourceService.DEFAULT_CATALOG ) );
    }
  }

  private static byte getWhenExhaustedActionType() {
    String whenExhaustedAction = PentahoSystem.getSystemSetting( "dbcp-defaults/when-exhausted-action", null ); //$NON-NLS-1$
    return !StringUtil.isEmpty( whenExhaustedAction ) ? Byte.parseByte( whenExhaustedAction )
            : GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
  }

  private static String getValidQuery( Map<String, String> attributes ) {
    return attributes.get( IDBDatasourceService.QUERY_KEY );
  }

  private static String getUrl( IDatabaseConnection databaseConnection, IDatabaseDialect dialect ) {
    try {
      return dialect.getURLWithExtraOptions( databaseConnection );
    } catch ( DatabaseDialectException e ) {
      return null;
    }
  }

  private static String getDriverClass( IDatabaseConnection databaseConnection, IDatabaseDialect dialect ) throws DBDatasourceServiceException {
    String driverClass;
    if ( databaseConnection.getDatabaseType().getShortName().equals( "GENERIC" ) ) { //$NON-NLS-1$
      driverClass = databaseConnection.getAttributes().get( GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS );
    } else {
      driverClass = dialect.getNativeDriver();
    }

    if ( StringUtils.isEmpty( driverClass ) ) {
      throwDBDatasourceServiceException( databaseConnection.getName(), "PooledDatasourceHelper.ERROR_0006_UNABLE_TO_POOL_DATASOURCE_NO_CLASSNAME" );
    }

    return driverClass;
  }

  private static KeyedObjectPoolFactory getKeyedObjectPoolFactory( Map<String, String> attributes, GenericObjectPool pool) {
    if ( Boolean.parseBoolean( attributes.get( IDBDatasourceService.POOL_PREPARED_STATEMENTS ) ) ) {

      int maxOpenPreparedStatements = -1; // unlimited

      if ( NumberUtils.isNumber( attributes.get( IDBDatasourceService.MAX_OPEN_PREPARED_STATEMENTS ) ) ) {

        maxOpenPreparedStatements =
            Integer.parseInt( attributes.get( IDBDatasourceService.MAX_OPEN_PREPARED_STATEMENTS ) );
      }

      return new GenericKeyedObjectPoolFactory( null, pool.getMaxActive(), pool.getWhenExhaustedAction(), pool
              .getMaxWait(), pool.getMaxIdle(), maxOpenPreparedStatements );
    }
    return null;
  }

  private static void setTimeBetweenEvictionRunsMillis( Map<String, String> attributes, GenericObjectPool pool ) {
    if ( NumberUtils.isNumber( attributes.get( IDBDatasourceService.TIME_BETWEEN_EVICTION_RUNS_MILLIS ) ) ) {
      pool.setTimeBetweenEvictionRunsMillis( Long.parseLong( attributes
          .get( IDBDatasourceService.TIME_BETWEEN_EVICTION_RUNS_MILLIS ) ) );
    }
  }

  private static GenericObjectPool initializeObjectPool( Map<String, String> attributes ) {
    // if removedAbandoned = true, then an AbandonedObjectPool object will take GenericObjectPool's place
    if ( Boolean.parseBoolean( attributes.get( IDBDatasourceService.REMOVE_ABANDONED ) ) ) {
      return new GenericObjectPool( null );
    }
    AbandonedConfig config = new AbandonedConfig();
    config.setRemoveAbandoned( Boolean.parseBoolean( attributes.get( IDBDatasourceService.REMOVE_ABANDONED ) ) );

    if ( attributes.containsKey( IDBDatasourceService.LOG_ABANDONED ) ) {
      config.setLogAbandoned( Boolean.parseBoolean( attributes.get( IDBDatasourceService.LOG_ABANDONED ) ) );
    }

    if ( NumberUtils.isNumber( attributes.get( IDBDatasourceService.REMOVE_ABANDONED_TIMEOUT ) ) ) {
      config.setRemoveAbandonedTimeout( Integer.parseInt( attributes
          .get( IDBDatasourceService.REMOVE_ABANDONED_TIMEOUT ) ) );
    }

    return new AbandonedObjectPool( null, config );
  }

  private static String getPropertyValue( Map<String, String> attributes, String key, String defaultValue ) {
    if ( attributes.containsKey( key ) ){
      return attributes.get( key );
    }
    return defaultValue;
  }

  private static int getIntegerPropertyValue( Map<String, String> attributes, String key, String defaultValue ) {
    return Integer.parseInt( getPropertyValue( attributes, key, defaultValue ) );
  }

  private static long getLongPropertyValue( Map<String, String> attributes, String key, String defaultValue ) {
    return Long.parseLong( getPropertyValue( attributes, key, defaultValue ) );
  }

  private static boolean getBooleanPropertyValue( Map<String, String> attributes, String key, String defaultValue ) {
    return Boolean.parseBoolean( getPropertyValue( attributes, key, defaultValue ) );
  }

  private static IDatabaseDialect getDatabaseDialect( IDatabaseConnection databaseConnection ) throws DBDatasourceServiceException {
    IDatabaseDialectService databaseDialectService = PentahoSystem.get( IDatabaseDialectService.class );
    if ( databaseDialectService == null ) {
      throwDBDatasourceServiceException( databaseConnection.getName(), "PooledDatasourceHelper.ERROR_0005_UNABLE_TO_POOL_DATASOURCE_NO_DIALECT_SERVICE" );
    }
    IDatabaseDialect dialect = databaseDialectService.getDialect( databaseConnection );
    if ( dialect == null || dialect.getDatabaseType() == null ) {
      throwDBDatasourceServiceException( databaseConnection.getName(), "PooledDatasourceHelper.ERROR_0004_UNABLE_TO_POOL_DATASOURCE_NO_DIALECT" );
    }
    return dialect;
  }

  private static void throwDBDatasourceServiceException( String dbName, String errorString ) throws DBDatasourceServiceException {
    throw new DBDatasourceServiceException( Messages.getInstance().getErrorString( errorString, dbName ) );
  }

  protected static ConnectionFactory getConnectionFactory( IDatabaseConnection databaseConnection, String url ) {
    Properties props = new Properties();
    props.put( "user", StringEscapeUtils.unescapeHtml( databaseConnection.getUsername() ) );
    props.put( "password", StringEscapeUtils.unescapeHtml( databaseConnection.getPassword() ) );

    if ( url.startsWith( "jdbc:mysql:" ) || ( url.startsWith( "jdbc:mariadb:" ) ) ) {
      props.put( "connectTimeout", "5000" );
    }
    return new DriverManagerConnectionFactory( url, props );
  }

  public static DataSource convert( IDatabaseConnection databaseConnection ) throws DBDatasourceServiceException {
    return convert( databaseConnection,  () -> PentahoSystem.get(
      IDatabaseDialectService.class, PentahoSessionHolder.getSession() ) );
  }

  @VisibleForTesting
  static DataSource convert( IDatabaseConnection databaseConnection, Supplier<IDatabaseDialectService> dialectSupplier )
    throws DBDatasourceServiceException {
    DriverManagerDataSource basicDatasource = new DriverManagerDataSource(); // From Spring
    IDatabaseDialect dialect = Optional.ofNullable( dialectSupplier.get() )
      .orElseThrow( () -> new DBDatasourceServiceException(
        Messages.getInstance().getErrorString(
          "PooledDatasourceHelper.ERROR_0001_DATASOURCE_CANNOT_LOAD_DIALECT_SVC" )
      ) ).getDialect( databaseConnection );
    if ( databaseConnection.getDatabaseType() == null && dialect == null ) {
      // We do not have enough information to create a DataSource. Throwing exception
      throw new DBDatasourceServiceException( Messages.getInstance().getErrorString(
          "PooledDatasourceHelper.ERROR_0001_DATASOURCE_CREATE_ERROR_NO_DIALECT", databaseConnection.getName() ) );
    }

    if ( databaseConnection.getDatabaseType().getShortName().equals( "GENERIC" ) ) { //$NON-NLS-1$
      String driverClassName =
          databaseConnection.getAttributes().get( GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS );
      if ( !StringUtils.isEmpty( driverClassName ) ) {
        initDriverClass( basicDatasource, dialect, driverClassName, databaseConnection.getName() );
      } else {
        // We do not have enough information to create a DataSource. Throwing exception
        throw new DBDatasourceServiceException( Messages.getInstance().getErrorString(
            "PooledDatasourceHelper.ERROR_0002_DATASOURCE_CREATE_ERROR_NO_CLASSNAME", databaseConnection.getName() ) );
      }

    } else {
      if ( !StringUtils.isEmpty( dialect.getNativeDriver() ) ) {
        initDriverClass( basicDatasource, dialect, dialect.getNativeDriver(), databaseConnection.getName() );
      } else {
        // We do not have enough information to create a DataSource. Throwing exception
        throw new DBDatasourceServiceException( Messages.getInstance().getErrorString(
            "PooledDatasourceHelper.ERROR_0003_DATASOURCE_CREATE_ERROR_NO_DRIVER", databaseConnection.getName() ) );
      }
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

  /**
   * For dialects which implement IDriverLocator, this method will use the provided
   * initialize() implementation.  For all others, will initialize drivers via the call to
   * {@link DriverManagerDataSource#setDriverClassName(String)} (which internally uses Class.forName())
   * @throws DBDatasourceServiceException
   */
  private static void initDriverClass( DriverManagerDataSource driverManagerDataSource, IDatabaseDialect dialect,
                                       String driverClassName,
                                       String databaseConnectionName ) throws DBDatasourceServiceException {
    if ( dialect instanceof IDriverLocator ) {
      if ( !( (IDriverLocator) dialect ).initialize( driverClassName ) ) {
        throw new DriverNotInitializedException( Messages.getInstance()
          .getErrorString( "PooledDatasourceHelper.ERROR_0009_UNABLE_TO_POOL_DATASOURCE_CANT_INITIALIZE",
            databaseConnectionName, driverClassName ) );
      }
      return;
    }
    try {
      driverManagerDataSource.setDriverClassName( driverClassName );
    } catch ( Throwable th ) {
      throw new DBDatasourceServiceException( Messages.getInstance().getErrorString(
        "PooledDatasourceHelper.ERROR_0002_DATASOURCE_CREATE_ERROR_NO_CLASSNAME", databaseConnectionName ), th );
    }
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
        // ignored
      }
      try {
        // Tomcat
        lkup = ctx.lookup( "java:comp/env/jdbc/" + dsName ); //$NON-NLS-1$
        if ( lkup != null ) {
          rtn = (DataSource) lkup;
          return rtn;
        }
      } catch ( NamingException ignored ) {
        // ignored
      }
      try {
        // Others?
        lkup = ctx.lookup( "jdbc/" + dsName ); //$NON-NLS-1$
        if ( lkup != null ) {
          rtn = (DataSource) lkup;
          return rtn;
        }
      } catch ( NamingException ignored ) {
        // ignored
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
