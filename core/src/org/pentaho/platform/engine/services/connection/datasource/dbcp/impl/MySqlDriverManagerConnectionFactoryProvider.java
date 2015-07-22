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
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.connection.datasource.dbcp.impl;

import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.IConnectionFactoryProvider;

import java.util.Properties;

/**
 * This class holds some MySql-specific logic. To configure it, add <tt>dbcp-mysql</tt> block to the configuration. More
 * details about supported properties see at documentation of {@linkplain #create(IDatabaseConnection, IDatabaseDialect,
 * String)}
 *
 * @author Andrey Khayrutdinov
 */
class MySqlDriverManagerConnectionFactoryProvider implements IConnectionFactoryProvider {

  /**
   * Looks for some properties specific for MySql driver and creates a factory respecting these properties. They are
   * listed here: <a href="http://dev.mysql.com/doc/connector-j/en/connector-j-reference-configuration-properties
   * .html">5.1 Driver/Datasource Class Names, URL Syntax and Configuration Properties for Connector/J</a>
   *
   * @param connection database connection
   * @param dialect    should be MSSQLServerDatabaseDialect
   * @param url        connection url
   * @return DBCP's {@linkplain DriverManagerConnectionFactory} with properly configured connection factory inside
   */
  @Override
  public DriverManagerConnectionFactory create( IDatabaseConnection connection, IDatabaseDialect dialect, String url ) {
    Properties props = new Properties();
    props.put( "user", connection.getUsername() );
    props.put( "password", connection.getPassword() );

    /*
     * socketTimeout
     * Timeout on network socket operations (0, the default means no timeout).
     */
    String socketTimeout = PentahoSystem.getSystemSetting( "dbcp-mysql/socketTimeout", null );
    if ( socketTimeout != null ) {
      props.put( "socketTimeout", socketTimeout );
    }

    /*
     * connectTimeout
     * Timeout for socket connect (in milliseconds), with 0 being no timeout. Only works on JDK-1.4 or newer.
     * Defaults to '0'.
     */
    String connectTimeout = PentahoSystem.getSystemSetting( "dbcp-mysql/connectTimeout", null );
    if ( connectTimeout != null ) {
      props.put( "connectTimeout", connectTimeout );
    }

    return new DriverManagerConnectionFactory( url, props );
  }
}
