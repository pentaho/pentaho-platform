package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import java.util.HashMap;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.commons.dbcp.PoolingDataSource;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.engine.core.MicroPlatform;

public class PooledDatasourceHelperTest extends TestCase {
  MicroPlatform mp;

  public PooledDatasourceHelperTest() {
  }

  protected void setUp() throws Exception {
  }

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
