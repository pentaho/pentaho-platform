package org.pentaho.platform.engine.services.connection.datasource.dbcp.impl;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.IConnectionFactoryProvider;
import org.pentaho.platform.util.logging.Logger;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * This class holds some Oracle-specific logic. To configure it, add <tt>dbcp-oracle</tt> block to the configuration.
 * More details about supported properties see at documentation of {@linkplain #create(IDatabaseConnection,
 * IDatabaseDialect, String)}
 *
 * @author Andrey Khayrutdinov
 */
class OracleConnectionFactoryProvider implements IConnectionFactoryProvider {

  /**
   * Looks for some properties specific for Oracle driver and creates a factory respecting these properties. They are
   * listed here: <a href="http://docs.oracle.com/cd/E11882_01/appdev.112/e13995/oracle/jdbc/OracleDriver
   * .html">Connection Properties Recognized by Oracle JDBC Drivers</a>. <tt>LoginTimeout</tt> is mentioned here: <a
   * href="https://docs.oracle.com/cd/E21764_01/web.1111/e13753/usedriver .htm#JDBCD115">Specifying Connection
   * Properties</a>
   *
   * @param connection database connection
   * @param dialect    should be OracleDatabaseDialect
   * @param url        connection url
   * @return DBCP's {@linkplain DataSourceConnectionFactory} with properly configured connection factory inside
   */
  @Override
  public ConnectionFactory create( IDatabaseConnection connection, IDatabaseDialect dialect, String url ) {

    Properties props = new Properties();
    props.put( "user", connection.getUsername() );
    props.put( "password", connection.getPassword() );

    /*
     * When you specify a LoginTimeout connection property and the connection is not created immediately, the request
     * waits for the time you specify. If the connection cannot be created within the time specified, the driver
     * throws an SQL exception.
     *
     * Measured in seconds
     */
    String loginTimeout = PentahoSystem.getSystemSetting( "dbcp-oracle/LoginTimeout", null );
    if ( loginTimeout != null ) {
      props.put( "LoginTimeout", loginTimeout );
    }

    DataSource oracleDs;
    try {
      @SuppressWarnings( "unchecked" )
      Class<? extends DataSource> oracleDsClass =
        (Class<? extends DataSource>) Class.forName( "oracle.jdbc.pool.OracleDataSource" );

      oracleDs = oracleDsClass.newInstance();

      oracleDsClass.getMethod( "setURL", String.class ).invoke( oracleDs, url );
      oracleDsClass.getMethod( "setConnectionProperties", Properties.class ).invoke( oracleDs, props );
    } catch ( Exception e ) {
      Logger.error( getClass(), e.getMessage(), e );
      return DefaultConnectionFactoryProvider.defaultFactory( connection, url );
    }

    return new DataSourceConnectionFactory( oracleDs );
  }
}
