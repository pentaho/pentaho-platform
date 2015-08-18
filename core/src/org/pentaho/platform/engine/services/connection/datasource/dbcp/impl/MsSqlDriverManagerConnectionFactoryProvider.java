package org.pentaho.platform.engine.services.connection.datasource.dbcp.impl;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.IConnectionFactoryProvider;

import java.util.Properties;

/**
 * This class holds some MsSql-specific logic. To configure it, add <tt>dbcp-mssql</tt> block to the configuration. More
 * details about supported properties see at documentation of {@linkplain #create(IDatabaseConnection, IDatabaseDialect,
 * String)}
 *
 * @author Andrey Khayrutdinov
 */
class MsSqlDriverManagerConnectionFactoryProvider implements IConnectionFactoryProvider {

  /**
   * Looks for some properties specific for MsSql driver and creates a factory respecting these properties. They are
   * listed here: <a href="http://jtds.sourceforge.net/faq.html">jTDS JDBC Driver</a>
   *
   * @param connection database connection
   * @param dialect    should be MSSQLDatabaseDialect
   * @param url        connection url
   * @return DBCP's {@linkplain DriverManagerConnectionFactory} with properly configured connection factory inside
   */
  @Override
  public ConnectionFactory create( IDatabaseConnection connection, IDatabaseDialect dialect, String url ) {
    Properties props = new Properties();
    props.put( "user", connection.getUsername() );
    props.put( "password", connection.getPassword() );

    /*
     * loginTimeout (default - 0 for TCP/IP connections or 20 for named pipe connections)
     *
     * The amount of time to wait (in seconds) for a successful connection before timing out.
     * If a TCP/IP connection is used to connect to the database and Java 1.4 or newer is being used, the loginTimeout
     * parameter is used to set the initial connection timeout when initially opening a new socket. A value of zero
     * (the default) causes the connection to wait indefinitely, e.g.,until a connection is established or an error
     * occurs. See also socketTimeout.
     *
     * If a named pipe connection is used (namedPipe is true) and loginTimeout is greater than zero, the value of
     * loginTimeout is used for the length of the retry period when "All pipe instances are busy" error messages are
     * received while attempting to connect to the server. If loginTimeout is zero (the default), a value of 20
     * seconds is used for the named pipe retry period.
     */
    String loginTimeout = PentahoSystem.getSystemSetting( "dbcp-mssql/loginTimeout", null );
    if ( loginTimeout != null ) {
      props.put( "loginTimeout", loginTimeout );
    }

    return new DriverManagerConnectionFactory( url, props );
  }
}
