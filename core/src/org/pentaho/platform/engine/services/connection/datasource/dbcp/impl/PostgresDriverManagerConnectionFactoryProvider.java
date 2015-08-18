package org.pentaho.platform.engine.services.connection.datasource.dbcp.impl;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.IConnectionFactoryProvider;

import java.util.Properties;

/**
 * This class holds some Postgres-specific logic. To configure it, add <tt>dbcp-postgres</tt> block to the
 * configuration. More details about supported properties see at documentation of {@linkplain
 * #create(IDatabaseConnection, IDatabaseDialect, String)}
 *
 * @author Andrey Khayrutdinov
 */
class PostgresDriverManagerConnectionFactoryProvider implements IConnectionFactoryProvider {


  /**
   * Looks for some properties specific for Postgres driver and creates a factory respecting these properties. They are
   * listed here: <a href="https://jdbc.postgresql.org/documentation/head/connect.html">Connecting to the Database</a>
   *
   * @param connection database connection
   * @param dialect    should be PostgreSQLDatabaseDialect
   * @param url        connection url
   * @return DBCP's {@linkplain DriverManagerConnectionFactory} with properly configured connection factory inside
   */
  @Override
  public ConnectionFactory create( IDatabaseConnection connection, IDatabaseDialect dialect, String url ) {
    Properties props = new Properties();
    props.put( "user", connection.getUsername() );
    props.put( "password", connection.getPassword() );

    /*
     * loginTimeout = int
     * Specify how long to wait for establishment of a database connection. The timeout is specified in seconds.
     */
    String loginTimeout = PentahoSystem.getSystemSetting( "dbcp-postgres/loginTimeout", null );
    if ( loginTimeout != null ) {
      props.put( "loginTimeout", loginTimeout );
    }

    return new DriverManagerConnectionFactory( url, props );
  }
}
