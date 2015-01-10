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
 * Copyright 2006 - 2014 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp.PoolingDataSource;
import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.test.platform.engine.core.MicroPlatform;

public class PooledDatasourceHelperTest {
  MicroPlatform mp;

  @Test
  public void testSetupPooledDataSourceForJNDI() {
    try {
      IDatabaseConnection databaseConnection = mock( IDatabaseConnection.class );
      when( databaseConnection.getAccessType() ).thenReturn( DatabaseAccessType.JNDI );
      PooledDatasourceHelper.setupPooledDataSource( databaseConnection );
      fail( "Expecting the exception to be thrown" );
    } catch ( DBDatasourceServiceException ex ) {
      assertNotNull( ex );
    }
  }

  @Test
  public void testCreatePoolNoDialectService() throws Exception {
    DatabaseDialectService dialectService = new DatabaseDialectService( false );
    final DatabaseTypeHelper databaseTypeHelper = new DatabaseTypeHelper( dialectService.getDatabaseTypes() );
    final DatabaseConnection con = new DatabaseConnection();
    con.setId( "Postgres" );
    con.setName( "Postgres" );
    con.setAccessType( DatabaseAccessType.NATIVE );
    con.setDatabaseType( databaseTypeHelper.getDatabaseTypeByShortName( "GENERIC" ) );
    con.setUsername( "pentaho_user" );
    con.setPassword( "password" );
    final HashMap<String, String> attrs = new HashMap<String, String>();
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_DRIVER_CLASS, "org.postgresql.Driver" );
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_URL, "jdbc:postgresql://localhost:5432/hibernate" );
    con.setAttributes( attrs );
    try {
      final PoolingDataSource poolingDataSource = PooledDatasourceHelper.setupPooledDataSource( con );
      fail( "Expecting the exception to be thrown" );
    } catch ( DBDatasourceServiceException ex ) {
      assertNotNull( ex );
    }
  }

  @Test
  public void testCreatePoolNoDialect() throws Exception {
    DatabaseDialectService dialectService = new DatabaseDialectService( false );
    final DatabaseTypeHelper databaseTypeHelper = new DatabaseTypeHelper( dialectService.getDatabaseTypes() );
    mp = new MicroPlatform();
    mp.defineInstance( IDatabaseDialectService.class, dialectService );
    mp.start();

    final DatabaseConnection con = new DatabaseConnection();
    con.setId( "Postgres" );
    con.setName( "Postgres" );
    con.setAccessType( DatabaseAccessType.NATIVE );
    con.setUsername( "pentaho_user" );
    con.setPassword( "password" );
    final HashMap<String, String> attrs = new HashMap<String, String>();
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_DRIVER_CLASS, "" );
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_URL, "jdbc:postgresql://localhost:5432/hibernate" );
    con.setAttributes( attrs );

    try {
      final PoolingDataSource poolingDataSource = PooledDatasourceHelper.setupPooledDataSource( con );
      fail( "Expecting the exception to be thrown" );
    } catch ( DBDatasourceServiceException ex ) {
      assertNotNull( ex );
    }

  }

  @Test
  public void testCreatePoolNoClassName() throws Exception {
    DatabaseDialectService dialectService = new DatabaseDialectService( false );
    final DatabaseTypeHelper databaseTypeHelper = new DatabaseTypeHelper( dialectService.getDatabaseTypes() );
    mp = new MicroPlatform();
    mp.defineInstance( IDatabaseDialectService.class, dialectService );
    mp.start();

    final DatabaseConnection con = new DatabaseConnection();
    con.setId( "Postgres" );
    con.setName( "Postgres" );
    con.setAccessType( DatabaseAccessType.NATIVE );
    con.setDatabaseType( databaseTypeHelper.getDatabaseTypeByShortName( "GENERIC" ) );
    con.setUsername( "pentaho_user" );
    con.setPassword( "password" );
    final HashMap<String, String> attrs = new HashMap<String, String>();
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_DRIVER_CLASS, "" );
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_URL, "jdbc:postgresql://localhost:5432/hibernate" );
    con.setAttributes( attrs );

    try {
      final PoolingDataSource poolingDataSource = PooledDatasourceHelper.setupPooledDataSource( con );
      fail( "Expecting the exception to be thrown" );
    } catch ( DBDatasourceServiceException ex ) {
      assertNotNull( ex );
    }

  }

  @Test
  public void testCreateDatasourceNoDialect() throws Exception {
    DatabaseDialectService dialectService = new DatabaseDialectService( false );
    final DatabaseTypeHelper databaseTypeHelper = new DatabaseTypeHelper( dialectService.getDatabaseTypes() );
    mp = new MicroPlatform();
    mp.defineInstance( IDatabaseDialectService.class, dialectService );
    mp.start();
    final DatabaseConnection con = new DatabaseConnection();
    con.setId( "Postgres" );
    con.setName( "Postgres" );
    con.setAccessType( DatabaseAccessType.NATIVE );
    con.setUsername( "pentaho_user" );
    con.setPassword( "password" );
    final HashMap<String, String> attrs = new HashMap<String, String>();
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_DRIVER_CLASS, "org.postgresql.Driver" );
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_URL, "jdbc:postgresql://localhost:5432/hibernate" );
    con.setAttributes( attrs );
    try {
      final DataSource dataSource = PooledDatasourceHelper.convert( con );
      fail( "Expecting the exception to be thrown" );
    } catch ( DBDatasourceServiceException ex ) {
      assertNotNull( ex );
    }
  }

  @Test
  public void testCreateDatasourceNoClassName() throws Exception {
    DatabaseDialectService dialectService = new DatabaseDialectService( false );
    final DatabaseTypeHelper databaseTypeHelper = new DatabaseTypeHelper( dialectService.getDatabaseTypes() );
    mp = new MicroPlatform();
    mp.defineInstance( IDatabaseDialectService.class, dialectService );
    mp.start();

    final DatabaseConnection con = new DatabaseConnection();
    con.setId( "Postgres" );
    con.setName( "Postgres" );
    con.setAccessType( DatabaseAccessType.NATIVE );
    con.setDatabaseType( databaseTypeHelper.getDatabaseTypeByShortName( "GENERIC" ) );
    con.setUsername( "pentaho_user" );
    con.setPassword( "password" );
    final HashMap<String, String> attrs = new HashMap<String, String>();
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_DRIVER_CLASS, "" );
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_URL, "jdbc:postgresql://localhost:5432/hibernate" );
    con.setAttributes( attrs );

    try {
      final DataSource dataSource = PooledDatasourceHelper.convert( con );
      fail( "Expecting the exception to be thrown" );
    } catch ( DBDatasourceServiceException ex ) {
      assertNotNull( ex );
    }

  }

}
