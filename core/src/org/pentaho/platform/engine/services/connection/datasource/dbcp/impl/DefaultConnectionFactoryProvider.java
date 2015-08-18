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

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.dialect.MSSQLServerDatabaseDialect;
import org.pentaho.database.dialect.MySQLDatabaseDialect;
import org.pentaho.database.dialect.OracleDatabaseDialect;
import org.pentaho.database.dialect.PostgreSQLDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.IConnectionFactoryProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * The default implementation of {@linkplain IConnectionFactoryProvider}.
 *
 * @author Andrey Khayrutdinov
 */
public class DefaultConnectionFactoryProvider implements IConnectionFactoryProvider {

  private final Map<String, IConnectionFactoryProvider> registeredProviders;

  public DefaultConnectionFactoryProvider() {
    this.registeredProviders = registerKnownDialects();
  }

  private static Map<String, IConnectionFactoryProvider> registerKnownDialects() {
    Map<String, IConnectionFactoryProvider> providers = new HashMap<>( 4 );

    String mysql = new MySQLDatabaseDialect().getDatabaseType().getName();
    providers.put( mysql, new MySqlDriverManagerConnectionFactoryProvider() );

    String oracle = new OracleDatabaseDialect().getDatabaseType().getName();
    providers.put( oracle, new OracleConnectionFactoryProvider() );

    String postgres = new PostgreSQLDatabaseDialect().getDatabaseType().getName();
    providers.put( postgres, new PostgresDriverManagerConnectionFactoryProvider() );

    String mssql = new MSSQLServerDatabaseDialect().getDatabaseType().getName();
    providers.put( mssql, new MsSqlDriverManagerConnectionFactoryProvider() );

    return providers;
  }

  @Override
  public ConnectionFactory create( IDatabaseConnection connection, IDatabaseDialect dialect, String url ) {
    IConnectionFactoryProvider provider = registeredProviders.get( dialect.getDatabaseType().getName() );
    return ( provider == null ) ? defaultFactory( connection, url ) : provider.create( connection, dialect, url );
  }

  public static DriverManagerConnectionFactory defaultFactory( IDatabaseConnection databaseConnection, String url ) {
    return new DriverManagerConnectionFactory( url, databaseConnection.getUsername(),
      databaseConnection.getPassword() );
  }
}
