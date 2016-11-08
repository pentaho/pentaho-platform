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
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.database.DatabaseDialectException;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.IDriverLocator;
import org.pentaho.database.dialect.GenericDatabaseDialect;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.google.common.collect.ImmutableMap;

@RunWith( MockitoJUnitRunner.class )
public class PooledDatasourceHelperTest {
  private static final String SOLUTION_PATH = "src/test/resources/solution";
  private MicroPlatform       mp;

  @Mock( extraInterfaces = { IDriverLocator.class } )
  private IDatabaseDialect        driverLocatorDialect;
  @Mock
  private IDatabaseDialect        plainDialect;
  @Mock
  private IDatabaseConnection     connection;
  @Mock
  private IDatabaseDialectService dialectService;
  @Mock
  private IDatabaseType           databaseType;

  private final String nativeDriverName = "some.native.driver";
  private final String jdbcUrl          = "jdbc:some://server:port";

  @Before
  public void before() throws DatabaseDialectException {
    when( dialectService.getDialect( connection ) ).thenReturn( driverLocatorDialect );
    when( connection.getDatabaseType() ).thenReturn( databaseType );
    when( connection.getDatabaseType().getShortName() ).thenReturn( "SomeDBType" );
    when( driverLocatorDialect.getNativeDriver() ).thenReturn( nativeDriverName );
    when( driverLocatorDialect.getURLWithExtraOptions( connection ) ).thenReturn( jdbcUrl );
    when( plainDialect.getNativeDriver() ).thenReturn( nativeDriverName );
    when( plainDialect.getURLWithExtraOptions( connection ) ).thenReturn( jdbcUrl );
  }

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
    final HashMap<String, String> attrs = new HashMap<>();
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_DRIVER_CLASS, "org.postgresql.Driver" );
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_URL, "jdbc:postgresql://localhost:5432/hibernate" );
    con.setAttributes( attrs );
    try {
      PooledDatasourceHelper.setupPooledDataSource( con );
      fail( "Expecting the exception to be thrown" );
    } catch ( DBDatasourceServiceException ex ) {
      assertNotNull( ex );
    }
  }

  @Test
  public void testCreatePoolNoDialect() throws Exception {
    DatabaseDialectService dialectService = new DatabaseDialectService( false );
    mp = new MicroPlatform( SOLUTION_PATH );
    mp.defineInstance( IDatabaseDialectService.class, dialectService );
    mp.start();

    final DatabaseConnection con = new DatabaseConnection();
    con.setId( "Postgres" );
    con.setName( "Postgres" );
    con.setAccessType( DatabaseAccessType.NATIVE );
    con.setUsername( "pentaho_user" );
    con.setPassword( "password" );
    final HashMap<String, String> attrs = new HashMap<>();
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_DRIVER_CLASS, "" );
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_URL, "jdbc:postgresql://localhost:5432/hibernate" );
    con.setAttributes( attrs );

    try {
      PooledDatasourceHelper.setupPooledDataSource( con );
      fail( "Expecting the exception to be thrown" );
    } catch ( DBDatasourceServiceException ex ) {
      assertNotNull( ex );
    }

  }

  @Test
  public void testCreatePoolNoClassName() throws Exception {
    DatabaseDialectService dialectService = new DatabaseDialectService( false );
    final DatabaseTypeHelper databaseTypeHelper = new DatabaseTypeHelper( dialectService.getDatabaseTypes() );
    mp = new MicroPlatform( SOLUTION_PATH );
    mp.defineInstance( IDatabaseDialectService.class, dialectService );
    mp.start();

    final DatabaseConnection con = new DatabaseConnection();
    con.setId( "Postgres" );
    con.setName( "Postgres" );
    con.setAccessType( DatabaseAccessType.NATIVE );
    con.setDatabaseType( databaseTypeHelper.getDatabaseTypeByShortName( "GENERIC" ) );
    con.setUsername( "pentaho_user" );
    con.setPassword( "password" );
    final HashMap<String, String> attrs = new HashMap<>();
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_DRIVER_CLASS, "" );
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_URL, "jdbc:postgresql://localhost:5432/hibernate" );
    con.setAttributes( attrs );

    try {
      PooledDatasourceHelper.setupPooledDataSource( con );
      fail( "Expecting the exception to be thrown" );
    } catch ( DBDatasourceServiceException ex ) {
      assertNotNull( ex );
    }

  }

  @Test
  public void testCreateDatasourceNoDialect() throws Exception {
    DatabaseDialectService dialectService = new DatabaseDialectService( false );

    final DatabaseConnection con = new DatabaseConnection();
    con.setId( "Postgres" );
    con.setName( "Postgres" );
    con.setAccessType( DatabaseAccessType.NATIVE );
    con.setUsername( "pentaho_user" );
    con.setPassword( "password" );
    final HashMap<String, String> attrs = new HashMap<>();
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_DRIVER_CLASS, "org.postgresql.Driver" );
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_URL, "jdbc:postgresql://localhost:5432/hibernate" );
    con.setAttributes( attrs );
    try {
      PooledDatasourceHelper.convert( con, () -> dialectService );
      fail( "Expecting the exception to be thrown" );
    } catch ( DBDatasourceServiceException ex ) {
      assertNotNull( ex );
    }
  }

  @Test
  public void testCreateDatasourceNoClassName() throws Exception {
    DatabaseDialectService dialectService = new DatabaseDialectService( false );
    final DatabaseTypeHelper databaseTypeHelper = new DatabaseTypeHelper( dialectService.getDatabaseTypes() );

    final DatabaseConnection con = new DatabaseConnection();
    con.setId( "Postgres" );
    con.setName( "Postgres" );
    con.setAccessType( DatabaseAccessType.NATIVE );
    con.setDatabaseType( databaseTypeHelper.getDatabaseTypeByShortName( "GENERIC" ) );
    con.setUsername( "pentaho_user" );
    con.setPassword( "password" );
    final HashMap<String, String> attrs = new HashMap<>();
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_DRIVER_CLASS, "" );
    attrs.put( DatabaseConnection.ATTRIBUTE_CUSTOM_URL, "jdbc:postgresql://localhost:5432/hibernate" );
    con.setAttributes( attrs );

    try {
      PooledDatasourceHelper.convert( con, () -> dialectService );
      fail( "Expecting the exception to be thrown" );
    } catch ( DBDatasourceServiceException ex ) {
      assertNotNull( ex );
    }

  }

  @Test
  public void testThatFailedDriverInitThrowsInConvert() throws DBDatasourceServiceException {
    when( ( (IDriverLocator) driverLocatorDialect ).initialize( nativeDriverName ) ).thenReturn( false );
    try {
      PooledDatasourceHelper.convert( connection, () -> dialectService );
      fail( "Expected exception" );
    } catch ( Exception e ) {
      assertThat( e.getMessage(), containsString( nativeDriverName ) );
    }
  }

  @Test
  public void testSuccessfulDriverInitInConvertNonGeneric() throws DBDatasourceServiceException {
    when( ( (IDriverLocator) driverLocatorDialect ).initialize( nativeDriverName ) ).thenReturn( true );
    PooledDatasourceHelper.convert( connection, () -> dialectService );
    verify( ( (IDriverLocator) driverLocatorDialect ), times( 1 ) ).initialize( nativeDriverName );
  }

  @Test
  public void testSuccessfulDriverInitInConvertGeneric() throws DBDatasourceServiceException {
    when( databaseType.getShortName() ).thenReturn( "GENERIC" );
    when( connection.getAttributes() ).thenReturn( ImmutableMap.of(
        GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS, nativeDriverName ) );
    when( connection.getUsername() ).thenReturn( "suzy" );
    when( connection.getPassword() ).thenReturn( "password" );
    when( ( (IDriverLocator) driverLocatorDialect ).initialize( nativeDriverName ) ).thenReturn( true );
    DriverManagerDataSource dataSource =
        (DriverManagerDataSource) PooledDatasourceHelper.convert( connection, () -> dialectService );
    verify( ( (IDriverLocator) driverLocatorDialect ), times( 1 ) ).initialize( nativeDriverName );
    assertThat( dataSource.getUrl(), is( jdbcUrl ) );
    assertThat( dataSource.getUsername(), is( "suzy" ) );
    assertThat( dataSource.getPassword(), is( "password" ) );
  }

  @Test
  public void testDialectWithoutLocatorAndDriverNotPresent() throws DBDatasourceServiceException {
    when( dialectService.getDialect( connection ) ).thenReturn( plainDialect );
    try {
      PooledDatasourceHelper.convert( connection, () -> dialectService );
      fail( "Expected exception, driver class should not be present." );
    } catch ( Exception e ) {
      assertThat( e, instanceOf( DBDatasourceServiceException.class ) );
      assertThat( e.getCause().getMessage(), containsString( nativeDriverName ) );
    }
  }

  @Test
  public void testDialectWithNoDriverSpecified() throws DBDatasourceServiceException {
    when( dialectService.getDialect( connection ) ).thenReturn( driverLocatorDialect );
    when( driverLocatorDialect.getNativeDriver() ).thenReturn( "" );
    try {
      PooledDatasourceHelper.convert( connection, () -> dialectService );
      fail( "Expected exception, driver class not specified in dialect." );
    } catch ( Exception e ) {
      assertThat( e, instanceOf( DBDatasourceServiceException.class ) );
    }
  }

  @Test
  public void testNoDialectService() throws DBDatasourceServiceException {
    try {
      PooledDatasourceHelper.convert( connection, () -> null );
      fail( "Expected an exception.  No dialect service." );
    } catch ( Exception e ) {
      assertThat( e, instanceOf( DBDatasourceServiceException.class ) );
    }
  }

  @After
  public void after() {
    if ( mp != null ) {
      mp.stop();
    }
  }
}
