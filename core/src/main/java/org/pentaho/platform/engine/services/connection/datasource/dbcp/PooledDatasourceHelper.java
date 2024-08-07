/*!
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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
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
import org.pentaho.platform.util.logging.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.Isolation;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.time.Duration;
import java.util.List;
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
    GenericObjectPool pool = initializeObjectPool( attributes, databaseConnection, dialect );
    configurePool( databaseConnection, dialect, attributes, pool );

    return pool;
  }

  private static void configurePool( IDatabaseConnection databaseConnection, IDatabaseDialect dialect, Map<String, String> attributes, GenericObjectPool pool ) throws Exception {
    // Configure Max Connections
    pool.setMaxTotal( databaseConnection.getMaximumPoolSize() );

    // Configure connection pool properties
    int maxIdleConnection = getIntegerPropertyValue( attributes, IDBDatasourceService.MAX_IDLE_KEY, PentahoSystem.getSystemSetting( "dbcp-defaults/max-idle-conn", null) );
    int minIdleConnection = getIntegerPropertyValue( attributes, IDBDatasourceService.MIN_IDLE_KEY, PentahoSystem.getSystemSetting( "dbcp-defaults/min-idle-conn", null) );
    int maxActiveConnection = getIntegerPropertyValue( attributes, IDBDatasourceService.MAX_ACTIVE_KEY, PentahoSystem.getSystemSetting( "dbcp-defaults/max-act-conn", null) );
    long waitTime = getLongPropertyValue( attributes, IDBDatasourceService.MAX_WAIT_KEY, PentahoSystem.getSystemSetting( "dbcp-defaults/wait", null) );
    boolean testWhileIdle = getBooleanPropertyValue( attributes, IDBDatasourceService.TEST_WHILE_IDLE, PentahoSystem.getSystemSetting( "dbcp-defaults/test-while-idle", null) );
    boolean testOnBorrow = getBooleanPropertyValue( attributes, IDBDatasourceService.TEST_ON_BORROW, PentahoSystem.getSystemSetting( "dbcp-defaults/test-on-borrow", null) );
    boolean testOnReturn = getBooleanPropertyValue( attributes, IDBDatasourceService.TEST_ON_RETURN, PentahoSystem.getSystemSetting( "dbcp-defaults/test-on-return", null) );

    // Tuning the connection pool
    pool.setMaxTotal( maxActiveConnection );
    pool.setMaxIdle( maxIdleConnection );
    pool.setMaxWait( Duration.ofMillis( waitTime ) );
    pool.setMinIdle( minIdleConnection );
    pool.setTestWhileIdle( testWhileIdle );
    pool.setTestOnReturn( testOnReturn );
    pool.setTestOnBorrow( testOnBorrow );
    pool.setTestWhileIdle( testWhileIdle );
    setTimeBetweenEvictionRunsMillis( attributes, pool );

    Logger.debug( PooledDatasourceHelper.class, "Pool defaults to " + maxActiveConnection + " max active/"
        + maxIdleConnection + "max idle" + "with " + waitTime + "wait time"//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        + " idle connections." ); //$NON-NLS-1$

    // initialize the pool to X connections
    prePopulatePool( pool, maxIdleConnection, databaseConnection.getInitialPoolSize() );

    Logger.debug( PooledDatasourceHelper.class, "Pool now has " + pool.getNumActive() + " active/"
        + pool.getNumIdle() + " idle connections." ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  private static void prePopulatePool( GenericObjectPool pool, int maxIdleConnection, int initialPoolSize ) throws Exception {
    String prePopulatePoolStr = PentahoSystem.getSystemSetting( "dbcp-defaults/pre-populate-pool", null );
    if ( Boolean.parseBoolean( prePopulatePoolStr ) || initialPoolSize > 0 ) {
      int initialConnections = Math.max( maxIdleConnection, initialPoolSize );
      for ( int i = 0; i < initialConnections; ++i ) {
        pool.addObject();
      }
      Logger.debug( PooledDatasourceHelper.class,
              "Pool has been pre-populated with " + initialConnections + " connections" );
    }
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

  private static void setTimeBetweenEvictionRunsMillis( Map<String, String> attributes, GenericObjectPool pool ) {
    if ( NumberUtils.isNumber( attributes.get( IDBDatasourceService.TIME_BETWEEN_EVICTION_RUNS_MILLIS ) ) ) {
      pool.setTimeBetweenEvictionRunsMillis( Long.parseLong( attributes
          .get( IDBDatasourceService.TIME_BETWEEN_EVICTION_RUNS_MILLIS ) ) );
    }
  }

  private static GenericObjectPool initializeObjectPool( Map<String, String> attributes,
                                                         IDatabaseConnection databaseConnection,
                                                         IDatabaseDialect dialect ) {
    String url = getUrl( databaseConnection, dialect );
    String validQuery = getValidQuery( attributes );
    /*
     * ConnectionFactory creates connections on behalf of the pool. Here, we use the DriverManagerConnectionFactory
     * because that essentially uses DriverManager as the source of connections.
     */
    ConnectionFactory factory = getConnectionFactory( databaseConnection, url );
    /*
     * Puts pool-specific wrappers on factory connections. For clarification: "[PoolableConnection]Factory," not
     * "Poolable[ConnectionFactory]."
     */
    PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory( factory, null );
    GenericObjectPool<PoolableConnection> genericObjectPool = new GenericObjectPool( poolableConnectionFactory );
    poolableConnectionFactory.setPool( genericObjectPool );

    boolean defaultReadOnly =
      attributes.containsKey( IDBDatasourceService.DEFAULT_READ_ONLY )
        && Boolean.parseBoolean( attributes.get( IDBDatasourceService.DEFAULT_READ_ONLY ) ); // default to false

    boolean defaultAutoCommit =
      !attributes.containsKey( IDBDatasourceService.DEFAULT_AUTO_COMMIT )
        || Boolean.parseBoolean( attributes.get( IDBDatasourceService.DEFAULT_AUTO_COMMIT ) );

    poolableConnectionFactory.setValidationQuery( validQuery );
    poolableConnectionFactory.setDefaultReadOnly( defaultReadOnly );
    poolableConnectionFactory.setDefaultAutoCommit( defaultAutoCommit );

    if ( attributes.containsKey( IDBDatasourceService.DEFAULT_TRANSACTION_ISOLATION )
      && !IDBDatasourceService.TRANSACTION_ISOLATION_NONE_VALUE.equalsIgnoreCase( attributes
      .get( IDBDatasourceService.DEFAULT_TRANSACTION_ISOLATION ) ) ) {
      Isolation isolationLevel =
        Isolation.valueOf( attributes.get( IDBDatasourceService.DEFAULT_TRANSACTION_ISOLATION ) );

      poolableConnectionFactory.setDefaultTransactionIsolation( isolationLevel.value() );
    }

    if ( attributes.containsKey( IDBDatasourceService.DEFAULT_CATALOG ) ) {
      poolableConnectionFactory.setDefaultCatalog( attributes.get( IDBDatasourceService.DEFAULT_CATALOG ) );
    }

    return genericObjectPool;
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

  // region getJndiDataSource(..)
  /**
   * Looks up a data source with a given name in the JNDI naming context.
   *
   * @param dsName The data source name.
   * @return The data source; never {@code null}.
   * @throws DBDatasourceServiceException If the data source name is not from a valid namespace, or is not defined.
   * @throws ClassCastException           If the data source name resolves to a resource which does not implement
   *                                      {@link DataSource}.
   */
  public static DataSource getJndiDataSource( String dsName ) throws DBDatasourceServiceException {
    return getJndiDataSource( createJndiContext(), dsName, PentahoSystem.getAllowedDatasourceJndiSchemesList() );
  }

  protected static Context createJndiContext() throws DBDatasourceServiceException {
    try {
      return new InitialContext();
    } catch ( NamingException e ) {
      throw new DBDatasourceServiceException( e );
    }
  }

  /**
   * Looks up a data source given its name in a JNDI naming context, constraining a name with scheme to be one in a
   * given list.
   * <p>
   * The following variations of {@code dsName} are attempted in order:
   * <ol>
   *   <li>{@code dsName}</li>
   *   <li>{@code "java:" + dsName}</li>
   *   <li>{@code "java:comp/env/jdbc/" + dsName}</li>
   *   <li>{@code "jdbc/" + dsName}</li>
   * </ol>
   *
   * @param context            The naming context.
   * @param dsName             The data source name.
   * @param allowedJndiSchemes The list of JNDI schemes that a data source name with scheme may have.
   * @return The data source; never {@code null}.
   * @throws DBDatasourceServiceException If the data source name is not from a valid namespace, is not defined, or the
   *                                      name resolves to a resource which does not implement {@link DataSource}.
   */
  public static DataSource getJndiDataSource( Context context, String dsName, List<String> allowedJndiSchemes )
    throws DBDatasourceServiceException {

    String[] candidateNames = {
      // First, try what they ask for...
      dsName,
      // Need this for Jboss.
      "java:" + dsName,
      // Tomcat
      "java:comp/env/jdbc/" + dsName,
      // Others?
      "jdbc/" + dsName
    };

    try {
      return lookupJndiDataSourceByNames( context, candidateNames, allowedJndiSchemes );
    } catch ( NamingException | ClassCastException e ) {
      throw new DBDatasourceServiceException( e );
    }
  }

  /**
   * Looks up a data source in a naming context given its candidate names.
   * <p>
   * Returns the data source corresponding to the first candidate name which is from a valid namespace and is defined.
   *
   * @param context            The naming context.
   * @param candidateNames     The candidate data source names. Must be non-empty.
   * @param allowedJndiSchemes The list of allowed JNDI schemes of valid data source names.
   * @return The data source resource; never {@code null}.
   * @throws NamingException    If none of the data source names are from a valid namespace or exist in the naming
   *                            context.
   * @throws ClassCastException If any of the data source names resolves to a resource which does not implement
   *                            {@link DataSource}.
   */
  private static DataSource lookupJndiDataSourceByNames( Context context,
                                                         String[] candidateNames,
                                                         List<String> allowedJndiSchemes )
    throws NamingException {

    assert candidateNames.length > 0;

    NamingException firstNe = null;

    for ( String dsName : candidateNames ) {
      try {
        return lookupJndiDataSourceByName( context, dsName, allowedJndiSchemes );
      } catch ( NamingException e ) {
        if ( firstNe == null ) {
          // Keep to throw in the end if none found.
          firstNe = e;
        }
      }
    }

    // Always non-null.
    throw firstNe;
  }

  /**
   * Looks up a data source by name in a naming context.
   *
   * @param context            The naming context.
   * @param dsName             The data source name.
   * @param allowedJndiSchemes The list of allowed JNDI schemes of valid data source names.
   * @return The data source resource; never {@code null}.
   * @throws NamingException    If the given data source name is not from a valid namespace, or if it does not exist
   *                            in the given context.
   * @throws ClassCastException If the given data source name resolves is that of a resource which does not implement
   *                            {@link DataSource}.
   */
  private static DataSource lookupJndiDataSourceByName( Context context,
                                                        String dsName,
                                                        List<String> allowedJndiSchemes ) throws NamingException {

    // Validate dsName belongs to valid namespace.
    validateJndiDataSourceName( dsName, allowedJndiSchemes );

    DataSource ds = (DataSource) context.lookup( dsName );
    if ( ds == null ) {
      throw new NamingException(
        Messages.getInstance().getErrorString( "PooledDatasourceHelper.ERROR_0010_DATASOURCE_NOT_FOUND", dsName ) );
    }

    return ds;
  }

  /**
   * Validates a data source name.
   *
   * @param dsName             The data source name.
   * @param allowedJndiSchemes The list of allowed JNDI schemes of valid data source names.
   * @throws NamingException If the data source name is invalid. Specifically, if it has a scheme which is not valid.
   */
  private static void validateJndiDataSourceName( String dsName, List<String> allowedJndiSchemes )
    throws NamingException {
    if ( StringUtils.isEmpty( dsName ) ) {
      return;
    }

    String scheme = getJndiScheme( dsName );
    if ( scheme == null ) {
      // Name with no URL scheme are assumed valid.
      return;
    }

    if ( allowedJndiSchemes != null && !allowedJndiSchemes.contains( scheme ) ) {
      throw new NamingException(
        Messages.getInstance()
          .getErrorString( "PooledDatasourceHelper.ERROR_0011_DATASOURCE_NAME_INVALID_SCHEME", dsName ) );
    }
  }

  private static String getJndiScheme( String name ) {
    int colonIndex = name.indexOf( ':' );
    if ( colonIndex <= 0 ) {
      return null;
    }

    int slashIndex = name.indexOf( '/' );
    if ( slashIndex != -1 && colonIndex >= slashIndex ) {
      return null;
    }

    return name.substring( 0, colonIndex );
  }
  // endregion
}
