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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
  private final String user             = "us&r!";
  private final String password         = "pass&word&!&amp;";

  @Before
  public void before() throws DatabaseDialectException {
    when( dialectService.getDialect( connection ) ).thenReturn( driverLocatorDialect );
    when( connection.getDatabaseType() ).thenReturn( databaseType );
    when( connection.getDatabaseType().getShortName() ).thenReturn( "SomeDBType" );
    when( driverLocatorDialect.getNativeDriver() ).thenReturn( nativeDriverName );
    when( driverLocatorDialect.getURLWithExtraOptions( connection ) ).thenReturn( jdbcUrl );
    when( plainDialect.getNativeDriver() ).thenReturn( nativeDriverName );
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
  public void testCreatePoolNoDialectService() {
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
  public void testCreateDatasourceNoDialect() {
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
  public void testCreateDatasourceNoClassName() {
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
  public void testThatFailedDriverInitThrowsInConvert() {
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
  public void testDialectWithoutLocatorAndDriverNotPresent() {
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
  public void testDialectWithNoDriverSpecified() {
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
  public void testNoDialectService() {
    try {
      PooledDatasourceHelper.convert( connection, () -> null );
      fail( "Expected an exception.  No dialect service." );
    } catch ( Exception e ) {
      assertThat( e, instanceOf( DBDatasourceServiceException.class ) );
    }
  }

  @Test( expected = DriverNotInitializedException.class )
  public void testDriverNotInitialized() throws DBDatasourceServiceException {
    when( dialectService.getDialect( connection ) ).thenReturn( driverLocatorDialect );
    when( ( (IDriverLocator) driverLocatorDialect ).initialize( nativeDriverName ) ).thenReturn( false );
    PooledDatasourceHelper.convert( connection, () -> dialectService );
  }

  @Test
  public void testConnectionFactory_MySQL() {
    IDatabaseConnection connection = mock( IDatabaseConnection.class );
    doReturn( StringEscapeUtils.escapeHtml( user ) ).when( connection ).getUsername();
    doReturn( StringEscapeUtils.escapeHtml( password ) ).when( connection ).getPassword();

    ConnectionFactory factory = PooledDatasourceHelper.getConnectionFactory( connection, "jdbc:mysql://localhost" );

    Properties props = ((DriverManagerConnectionFactory) factory).getProperties();
    assertEquals( user, props.getProperty( "user" ) );
    assertEquals( password, props.getProperty( "password" ) );
  }

  @Test
  public void testConnectionFactory_MariaDB() {
    IDatabaseConnection connection = mock( IDatabaseConnection.class );
    doReturn( StringEscapeUtils.escapeHtml( user ) ).when( connection ).getUsername();
    doReturn( StringEscapeUtils.escapeHtml( password ) ).when( connection ).getPassword();

    ConnectionFactory factory = PooledDatasourceHelper.getConnectionFactory( connection, "jdbc:mariadb://localhost" );

    Properties props = ((DriverManagerConnectionFactory) factory).getProperties();
    assertEquals( user, props.getProperty( "user" ) );
    assertEquals( password, props.getProperty( "password" ) );
  }

  @Test
  public void testConnectionFactory_MicrosoftSQL() {
    IDatabaseConnection connection = mock( IDatabaseConnection.class );
    doReturn( StringEscapeUtils.escapeHtml( user ) ).when( connection ).getUsername();
    doReturn( StringEscapeUtils.escapeHtml( password ) ).when( connection ).getPassword();

    ConnectionFactory factory = PooledDatasourceHelper.getConnectionFactory( connection, "jdbc:microsoft:sqlserver://localhost" );

    Properties props = ((DriverManagerConnectionFactory) factory).getProperties();
    assertEquals( user, props.getProperty( "user" ) );
    assertEquals( password, props.getProperty( "password" ) );

  }

  // region getJndiDataSource(..)
  static abstract class BaseJndiScenario {
    public final Context context;
    public final List<String> allowedJndiSchemes;

    public BaseJndiScenario() {
      this( null );
    }

    public BaseJndiScenario( List<String> allowedJndiSchemes ) {
      this.context = mock( Context.class );
      this.allowedJndiSchemes = allowedJndiSchemes != null ? allowedJndiSchemes : List.of( "java" );
    }

    public void registerResource( String fullName, Object resource ) {
      try {
        when( context.lookup( fullName ) ).thenReturn( resource );
      } catch ( NamingException e ) {
        // Does not happen during testing.
        throw new RuntimeException( e );
      }
    }

    public DataSource getJndiDataSource( String name ) throws DBDatasourceServiceException {
      return PooledDatasourceHelper.getJndiDataSource( context, name, allowedJndiSchemes );
    }
  }

  static class Tomcat1JndiScenario extends BaseJndiScenario {
    public final String DATA_SOURCE_NON_EXISTING_FULL_NAME = "java:comp/env/jdbc/FOOO";

    public final String DATA_SOURCE_1_NAME = "DataSource1";
    public final String DATA_SOURCE_1_JAVA_RELATIVE_NAME = "comp/env/jdbc/" + DATA_SOURCE_1_NAME;
    public final String DATA_SOURCE_1_FULL_NAME = "java:" + DATA_SOURCE_1_JAVA_RELATIVE_NAME;
    public final DataSource dataSource1;

    public final String DATA_SOURCE_2_NAME = "DataSource2";
    public final String DATA_SOURCE_2_JAVA_RELATIVE_NAME = "comp/env/jdbc/" + DATA_SOURCE_2_NAME;
    public final String DATA_SOURCE_2_FULL_NAME = "java:" + DATA_SOURCE_2_JAVA_RELATIVE_NAME;
    public final DataSource dataSource2;

    public final String DATA_SOURCE_3_RMI_FULL_NAME = "rmi:DataSource3";
    public final DataSource dataSource3;

    public final String NOT_DATA_SOURCE_NAME = "NotADataSource";
    public final Object notADataSource;

    public Tomcat1JndiScenario() {
      this( null );
    }

    public Tomcat1JndiScenario( List<String> allowedJndiSchemes ) {
      super( allowedJndiSchemes );

      dataSource1 = mock( DataSource.class );
      registerResource( DATA_SOURCE_1_FULL_NAME, dataSource1 );

      dataSource2 = mock( DataSource.class );
      registerResource( DATA_SOURCE_2_FULL_NAME, dataSource2 );

      dataSource3 = mock( DataSource.class );
      registerResource( DATA_SOURCE_3_RMI_FULL_NAME, dataSource3 );

      notADataSource = mock( Object.class );
      registerResource( NOT_DATA_SOURCE_NAME, notADataSource );
    }
  }

  static class OtherJndiScenario extends BaseJndiScenario {
    public final String DATA_SOURCE_1_NAME = "DataSource1";
    public final String DATA_SOURCE_1_FULL_NAME = "jdbc/" + DATA_SOURCE_1_NAME;
    public final DataSource dataSource1;

    public OtherJndiScenario() {
      dataSource1 = mock( DataSource.class );
      registerResource( DATA_SOURCE_1_FULL_NAME, dataSource1 );
    }
  }

  @Test
  public void testGetJndiDataSourceResolvesExistingDataSourceGivenFullJndiName() throws Exception {
    Tomcat1JndiScenario scenario = new Tomcat1JndiScenario();

    DataSource ds = scenario.getJndiDataSource( scenario.DATA_SOURCE_1_FULL_NAME );

    assertSame( scenario.dataSource1, ds );
  }

  @Test
  public void testGetJndiDataSourceResolvesDistinctDataSourcesGivenFullJndiNames() throws Exception {
    Tomcat1JndiScenario scenario = new Tomcat1JndiScenario();

    assertSame( scenario.dataSource1, scenario.getJndiDataSource( scenario.DATA_SOURCE_1_FULL_NAME ) );
    assertSame( scenario.dataSource2, scenario.getJndiDataSource( scenario.DATA_SOURCE_2_FULL_NAME ) );
  }

  @Test
  public void testGetJndiDataSourceResolvesExistingDataSourceGivenName() throws Exception {
    Tomcat1JndiScenario scenario = new Tomcat1JndiScenario();

    DataSource ds = scenario.getJndiDataSource( scenario.DATA_SOURCE_1_NAME );

    assertSame( scenario.dataSource1, ds );
  }

  @Test
  public void testGetJndiDataSourceResolvesExistingDataSourceGivenJavaSchemeRelativeName() throws Exception {
    Tomcat1JndiScenario scenario = new Tomcat1JndiScenario();

    DataSource ds = scenario.getJndiDataSource( scenario.DATA_SOURCE_1_JAVA_RELATIVE_NAME );

    assertSame( scenario.dataSource1, ds );
  }


  @Test
  public void testGetJndiDataSourceResolvesExistingDataSourceGivenFullJdbcName() throws Exception {
    OtherJndiScenario scenario = new OtherJndiScenario();

    DataSource ds = scenario.getJndiDataSource( scenario.DATA_SOURCE_1_FULL_NAME );

    assertSame( scenario.dataSource1, ds );
  }

  @Test
  public void testGetJndiDataSourceResolvesExistingDataSourceGivenJdbcRelativeName() throws Exception {
    OtherJndiScenario scenario = new OtherJndiScenario();

    DataSource ds = scenario.getJndiDataSource( scenario.DATA_SOURCE_1_NAME );

    assertSame( scenario.dataSource1, ds );
  }

  @Test( expected = DBDatasourceServiceException.class )
  public void testGetJndiDataSourceThrowsGivenNameOfNonExistingResource() throws Exception {
    Tomcat1JndiScenario scenario = new Tomcat1JndiScenario();

    scenario.getJndiDataSource( scenario.DATA_SOURCE_NON_EXISTING_FULL_NAME );
  }

  @Test( expected = DBDatasourceServiceException.class )
  public void testGetJndiDataSourceThrowsGivenNameOfExistingResourceNotDataSource() throws Exception {
    Tomcat1JndiScenario scenario = new Tomcat1JndiScenario();

    scenario.getJndiDataSource( scenario.NOT_DATA_SOURCE_NAME );
  }

  @Test( expected = DBDatasourceServiceException.class )
  public void testGetJndiDataSourceThrowsGivenNameWithDisallowedScheme() throws Exception {
    Tomcat1JndiScenario scenario = new Tomcat1JndiScenario();

    scenario.getJndiDataSource( scenario.DATA_SOURCE_3_RMI_FULL_NAME );

    verify( scenario.context, never() ).lookup( anyString() );
  }

  @Test( expected = DBDatasourceServiceException.class )
  public void testGetJndiDataSourceThrowsGivenNameWithDisallowedJavaScheme() throws Exception {
    // This tests that it is possible to disable the java scheme via configuration!

    Tomcat1JndiScenario scenario = new Tomcat1JndiScenario( List.of( "rmi" ) );

    scenario.getJndiDataSource( scenario.DATA_SOURCE_1_NAME );

    verify( scenario.context, never() ).lookup( anyString() );
  }
  // endregion

  @After
  public void after() {
    if ( mp != null ) {
      mp.stop();
    }
  }
}
