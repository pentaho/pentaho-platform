/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2019-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport;

import junit.framework.TestCase;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseAccessType;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseConnection;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseType;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.MapExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.PartitionDatabaseMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseConnectionConverterTest extends TestCase {

  // Values to be used on the tests
  private static final String CONNECT_SQL = "connectSql";
  private static final org.pentaho.database.model.DatabaseAccessType DATABASE_ACCESS_TYPE_MODEL =
    org.pentaho.database.model.DatabaseAccessType.OCI;
  private static final DatabaseAccessType DATABASE_ACCESS_TYPE_EXPORT = DatabaseAccessType.OCI;
  private static final org.pentaho.database.model.DatabaseAccessType SUPPORTED_DATABASE_ACCESS_TYPE_MODEL =
    org.pentaho.database.model.DatabaseAccessType.JNDI;
  private static final DatabaseAccessType SUPPORTED_DATABASE_ACCESS_TYPE_EXPORT = DatabaseAccessType.JNDI;
  private static final String DATABASE_NAME = "databaseName";
  private static final String DATABASE_PORT = "123456";

  private static final boolean CHANGED = false;
  private static final String DATABASE_TYPE_NAME = "databaseTypeName";
  private static final String DATABASE_TYPE_SHORT_NAME = "databaseTypeShortName";
  private static final String DATABASE_TYPE_DEFAULT_NAME = "databaseTypeDefaultName";
  private static final int DATABASE_TYPE_DEFAULT_PORT = 9182;
  private static final String DATABASE_TYPE_EXTRA_OPTIONS_HELP_URL = "databaseTypeExtraOptionsHelpUrl";

  private static final String DUMMY_MAP_KEY = "dummyMapKey_";
  private static final String DUMMY_MAP_VALUE = "dummyMapValue_";
  private static final String DUMMY_EXPORT_KEY = "dummyExportKey_";
  private static final String DUMMY_EXPORT_VALUE = "dummyExportValue_";

  private static final String DATA_TABLESPACE = "dataTablespace";
  private static final boolean FORCING_IDENTIFIERS_TO_LOWER_CASE = false;
  private static final boolean FORCING_IDENTIFIERS_TO_UPPER_CASE = false;
  private static final String HOSTNAME = "hostname";
  private static final String ID = "id";
  private static final String INDEX_TABLESPACE = "indexTablespace";
  private static final String INFORMIX_SERVERNAME = "informixServername";
  private static final int INITIAL_POOL_SIZE = 123;
  private static final int MAXIMUM_POOL_SIZE = 321;
  private static final String NAME = "name";
  private static final boolean PARTITIONED = false;
  private static final String PARTITIONING_INFORMATION_DATABASE_NAME = "partitioningInformationDatabaseName";
  private static final String PARTITIONING_INFORMATION_HOSTNAME = "partitioningInformationHostname";
  private static final String PARTITIONING_INFORMATION_PARTITION_ID = "partitioningInformationPartitionId";
  private static final String PARTITIONING_INFORMATION_PASSWORD = "partitioningInformationPassword";
  private static final String PARTITIONING_INFORMATION_PORT = "99887766";
  private static final String PARTITIONING_INFORMATION_USERNAME = "partitioningInformationUsername";
  private static final String PASSWORD = "password";
  private static final boolean QUOTE_ALL_FIELDS = false;
  private static final String SQL_SERVER_INSTANCE = "sqlServerInstance";
  private static final boolean STREAMING_RESULTS = false;
  private static final String USERNAME = "username";
  private static final boolean USING_CONNECTION_POOL = false;
  private static final boolean USING_DOUBLE_DECIMAL_AS_SCHEMA_TABLE_SEPARATOR = false;


  public void testModel2export() throws Exception {

    org.pentaho.database.model.IDatabaseConnection databaseConnection = buildDatabaseConnectionModel();

    // Convert

    DatabaseConnection databaseConnectionExport = DatabaseConnectionConverter.model2export( databaseConnection );


    // Asserts

    assertEquals( DATABASE_ACCESS_TYPE_EXPORT, databaseConnectionExport.getAccessType() );


    assertMapEquals2model( databaseConnection.getAttributes(), databaseConnectionExport.getAttributes() );

    assertEquals( CHANGED, databaseConnectionExport.isChanged() );

    assertMapEquals2model( databaseConnection.getConnectionPoolingProperties(),
      databaseConnectionExport.getConnectionPoolingProperties() );

    assertEquals( databaseConnection.getConnectSql(), databaseConnectionExport.getConnectSql() );
    assertEquals( databaseConnection.getDatabaseName(), databaseConnectionExport.getDatabaseName() );
    assertEquals( databaseConnection.getDatabasePort(), databaseConnectionExport.getDatabasePort() );

    assertDatabaseTypeEquals2model( databaseConnection.getDatabaseType(), databaseConnectionExport.getDatabaseType() );

    assertEquals( databaseConnection.getDataTablespace(), databaseConnectionExport.getDataTablespace() );

    // Note that the implementation of the "SQLServerInstance" uses the extraOptions map to store an information
    assertMapEquals2model( databaseConnection.getExtraOptions(), databaseConnectionExport.getExtraOptions() );
    assertMapEquals2model( databaseConnection.getExtraOptionsOrder(), databaseConnectionExport.getExtraOptionsOrder() );

    assertEquals( databaseConnection.isForcingIdentifiersToLowerCase(),
      databaseConnectionExport.isForcingIdentifiersToLowerCase() );
    assertEquals( databaseConnection.isForcingIdentifiersToUpperCase(),
      databaseConnectionExport.isForcingIdentifiersToUpperCase() );
    assertEquals( databaseConnection.getHostname(), databaseConnectionExport.getHostname() );
    assertEquals( databaseConnection.getId(), databaseConnectionExport.getId() );
    assertEquals( databaseConnection.getIndexTablespace(), databaseConnectionExport.getIndexTablespace() );
    assertEquals( databaseConnection.getInformixServername(), databaseConnectionExport.getInformixServername() );
    assertEquals( databaseConnection.getInitialPoolSize(), databaseConnectionExport.getInitialPoolSize() );
    assertEquals( databaseConnection.getMaximumPoolSize(), databaseConnectionExport.getMaximumPoolSize() );
    assertEquals( databaseConnection.getName(), databaseConnectionExport.getName() );
    assertEquals( databaseConnection.isPartitioned(), databaseConnectionExport.isPartitioned() );

    assertPartitionDatabaseMetaEquals2model( databaseConnection.getPartitioningInformation(),
      databaseConnectionExport.getPartitioningInformation() );

    assertEquals( databaseConnection.getPassword(), databaseConnectionExport.getPassword() );
    assertEquals( databaseConnection.isQuoteAllFields(), databaseConnectionExport.isQuoteAllFields() );
    assertEquals( databaseConnection.getSQLServerInstance(), databaseConnectionExport.getSQLServerInstance() );
    assertEquals( databaseConnection.isStreamingResults(), databaseConnectionExport.isStreamingResults() );
    assertEquals( databaseConnection.getUsername(), databaseConnectionExport.getUsername() );
    assertEquals( databaseConnection.isUsingDoubleDecimalAsSchemaTableSeparator(),
      databaseConnectionExport.isUsingDoubleDecimalAsSchemaTableSeparator() );
    assertEquals( databaseConnection.isUsingConnectionPool(), databaseConnectionExport.isUsingConnectionPool() );
  }

  public void testExport2model() throws Exception {

    DatabaseConnection databaseConnection = buildDatabaseConnectionExport();

    // Convert

    org.pentaho.database.model.IDatabaseConnection databaseConnectionModel =
      DatabaseConnectionConverter.export2model( databaseConnection );


    // Asserts

    assertEquals( DATABASE_ACCESS_TYPE_MODEL, databaseConnectionModel.getAccessType() );

    assertMapEquals2export( databaseConnection.getAttributes(), databaseConnectionModel.getAttributes(), 0 );

    assertEquals( databaseConnection.isChanged(), databaseConnection.isChanged() );

    assertMapEquals2export( databaseConnection.getConnectionPoolingProperties(),
      databaseConnectionModel.getConnectionPoolingProperties(), 0 );

    assertEquals( databaseConnection.getConnectSql(), databaseConnectionModel.getConnectSql() );
    assertEquals( databaseConnection.getDatabaseName(), databaseConnectionModel.getDatabaseName() );
    assertEquals( databaseConnection.getDatabasePort(), databaseConnectionModel.getDatabasePort() );

    assertDatabaseTypeEquals2export( databaseConnection.getDatabaseType(), databaseConnectionModel.getDatabaseType() );

    assertEquals( databaseConnection.getDataTablespace(), databaseConnectionModel.getDataTablespace() );

    // Note that the implementation of the "SQLServerInstance" uses the extraOptions map to store an information
    assertMapEquals2export( databaseConnection.getExtraOptions(), databaseConnectionModel.getExtraOptions(), 1 );
    assertMapEquals2export( databaseConnection.getExtraOptionsOrder(), databaseConnectionModel.getExtraOptionsOrder(),
      0 );

    assertEquals( databaseConnection.isForcingIdentifiersToLowerCase(),
      databaseConnection.isForcingIdentifiersToLowerCase() );
    assertEquals( databaseConnection.isForcingIdentifiersToUpperCase(),
      databaseConnection.isForcingIdentifiersToUpperCase() );
    assertEquals( databaseConnection.getHostname(), databaseConnectionModel.getHostname() );
    assertEquals( databaseConnection.getId(), databaseConnectionModel.getId() );
    assertEquals( databaseConnection.getIndexTablespace(), databaseConnectionModel.getIndexTablespace() );
    assertEquals( databaseConnection.getInformixServername(), databaseConnectionModel.getInformixServername() );
    assertEquals( databaseConnection.getInitialPoolSize(), databaseConnectionModel.getInitialPoolSize() );
    assertEquals( databaseConnection.getMaximumPoolSize(), databaseConnectionModel.getMaximumPoolSize() );
    assertEquals( databaseConnection.getName(), databaseConnectionModel.getName() );
    assertEquals( databaseConnection.isPartitioned(), databaseConnection.isPartitioned() );

    assertPartitionDatabaseMetaEquals2export( databaseConnection.getPartitioningInformation(),
      databaseConnectionModel.getPartitioningInformation() );

    assertEquals( databaseConnection.getPassword(), databaseConnectionModel.getPassword() );
    assertEquals( databaseConnection.isQuoteAllFields(), databaseConnection.isQuoteAllFields() );
    assertEquals( databaseConnection.getSQLServerInstance(), databaseConnectionModel.getSQLServerInstance() );
    assertEquals( databaseConnection.isStreamingResults(), databaseConnection.isStreamingResults() );
    assertEquals( databaseConnection.getUsername(), databaseConnectionModel.getUsername() );
    assertEquals( databaseConnection.isUsingDoubleDecimalAsSchemaTableSeparator(),
      databaseConnection.isUsingDoubleDecimalAsSchemaTableSeparator() );
    assertEquals( databaseConnection.isUsingConnectionPool(), databaseConnection.isUsingConnectionPool() );
  }

  protected org.pentaho.database.model.IDatabaseConnection buildDatabaseConnectionModel() {
    org.pentaho.database.model.IDatabaseConnection databaseConnection =
      new org.pentaho.database.model.DatabaseConnection();

    // "Simple" values
    databaseConnection.setAccessType( DATABASE_ACCESS_TYPE_MODEL );
    databaseConnection.setChanged( CHANGED );
    databaseConnection.setConnectSql( CONNECT_SQL );
    databaseConnection.setDatabaseName( DATABASE_NAME );
    databaseConnection.setDatabasePort( DATABASE_PORT );
    databaseConnection.setDataTablespace( DATA_TABLESPACE );
    databaseConnection.setForcingIdentifiersToLowerCase( FORCING_IDENTIFIERS_TO_LOWER_CASE );
    databaseConnection.setForcingIdentifiersToUpperCase( FORCING_IDENTIFIERS_TO_UPPER_CASE );
    databaseConnection.setHostname( HOSTNAME );
    databaseConnection.setId( ID );
    databaseConnection.setIndexTablespace( INDEX_TABLESPACE );
    databaseConnection.setInformixServername( INFORMIX_SERVERNAME );
    databaseConnection.setInitialPoolSize( INITIAL_POOL_SIZE );
    databaseConnection.setMaximumPoolSize( MAXIMUM_POOL_SIZE );
    databaseConnection.setName( NAME );
    databaseConnection.setPartitioned( PARTITIONED );
    databaseConnection.setPassword( PASSWORD );
    databaseConnection.setQuoteAllFields( QUOTE_ALL_FIELDS );
    // Note that the implementation of the "SQLServerInstance" uses the extraOptions map to store an information
    databaseConnection.setSQLServerInstance( SQL_SERVER_INSTANCE );
    databaseConnection.setStreamingResults( STREAMING_RESULTS );
    databaseConnection.setUsername( USERNAME );
    databaseConnection.setUsingConnectionPool( USING_CONNECTION_POOL );
    databaseConnection.setUsingDoubleDecimalAsSchemaTableSeparator( USING_DOUBLE_DECIMAL_AS_SCHEMA_TABLE_SEPARATOR );

    // "Complex" values

    Map<String, String> attributes = new HashMap<>();
    generateMapEntries( attributes, 5 );
    databaseConnection.setAttributes( attributes );

    Map<String, String> connectionPoolingProperties = new HashMap<>();
    generateMapEntries( connectionPoolingProperties, 2 );
    databaseConnection.setConnectionPoolingProperties( connectionPoolingProperties );

    List<org.pentaho.database.model.DatabaseAccessType> databaseTypeSupportedAccessTypes = new ArrayList<>();
    databaseTypeSupportedAccessTypes.add( SUPPORTED_DATABASE_ACCESS_TYPE_MODEL );
    org.pentaho.database.model.IDatabaseType databaseType =
      new org.pentaho.database.model.DatabaseType( DATABASE_TYPE_NAME, DATABASE_TYPE_SHORT_NAME,
        databaseTypeSupportedAccessTypes,
        DATABASE_TYPE_DEFAULT_PORT, DATABASE_TYPE_EXTRA_OPTIONS_HELP_URL );
    Map<String, String> databaseTypeDefaultOptions = new HashMap<>();
    generateMapEntries( databaseTypeDefaultOptions, 3 );
    databaseType.setDefaultOptions( databaseTypeDefaultOptions );
    databaseConnection.setDatabaseType( databaseType );

    // Note that the implementation of the "SQLServerInstance" uses the extraOptions map to store an information
    // Testing if the map already exists!
    Map<String, String> extraOptions = databaseConnection.getExtraOptions();
    // Create a new only if non-existent
    extraOptions = null != extraOptions ? extraOptions : new HashMap<>();
    generateMapEntries( extraOptions, 3 );
    databaseConnection.setExtraOptions( extraOptions );

    Map<String, String> extraOptionsOrder = new HashMap<>();
    generateMapEntries( extraOptionsOrder, 2 );
    databaseConnection.setExtraOptionsOrder( extraOptionsOrder );

    org.pentaho.database.model.PartitionDatabaseMeta partitioningInformation =
      new org.pentaho.database.model.PartitionDatabaseMeta();
    partitioningInformation.setDatabaseName( PARTITIONING_INFORMATION_DATABASE_NAME );
    partitioningInformation.setHostname( PARTITIONING_INFORMATION_HOSTNAME );
    partitioningInformation.setPartitionId( PARTITIONING_INFORMATION_PARTITION_ID );
    partitioningInformation.setPassword( PARTITIONING_INFORMATION_PASSWORD );
    partitioningInformation.setPort( PARTITIONING_INFORMATION_PORT );
    partitioningInformation.setUsername( PARTITIONING_INFORMATION_USERNAME );
    List<org.pentaho.database.model.PartitionDatabaseMeta> partitioningInformationList = new ArrayList<>();
    partitioningInformationList.add( partitioningInformation );
    databaseConnection.setPartitioningInformation( partitioningInformationList );

    return databaseConnection;
  }

  protected DatabaseConnection buildDatabaseConnectionExport() {
    DatabaseConnection databaseConnection = new DatabaseConnection();

    // "Simple" values
    databaseConnection.setAccessType( DATABASE_ACCESS_TYPE_EXPORT );
    databaseConnection.setChanged( CHANGED );
    databaseConnection.setConnectSql( CONNECT_SQL );
    databaseConnection.setDatabaseName( DATABASE_NAME );
    databaseConnection.setDatabasePort( DATABASE_PORT );
    databaseConnection.setDataTablespace( DATA_TABLESPACE );
    databaseConnection.setForcingIdentifiersToLowerCase( FORCING_IDENTIFIERS_TO_LOWER_CASE );
    databaseConnection.setForcingIdentifiersToUpperCase( FORCING_IDENTIFIERS_TO_UPPER_CASE );
    databaseConnection.setHostname( HOSTNAME );
    databaseConnection.setId( ID );
    databaseConnection.setIndexTablespace( INDEX_TABLESPACE );
    databaseConnection.setInformixServername( INFORMIX_SERVERNAME );
    databaseConnection.setInitialPoolSize( INITIAL_POOL_SIZE );
    databaseConnection.setMaximumPoolSize( MAXIMUM_POOL_SIZE );
    databaseConnection.setName( NAME );
    databaseConnection.setPartitioned( PARTITIONED );
    databaseConnection.setPassword( PASSWORD );
    databaseConnection.setQuoteAllFields( QUOTE_ALL_FIELDS );
    databaseConnection.setSQLServerInstance( SQL_SERVER_INSTANCE );
    databaseConnection.setStreamingResults( STREAMING_RESULTS );
    databaseConnection.setUsername( USERNAME );
    databaseConnection.setUsingConnectionPool( USING_CONNECTION_POOL );
    databaseConnection.setUsingDoubleDecimalAsSchemaTableSeparator( USING_DOUBLE_DECIMAL_AS_SCHEMA_TABLE_SEPARATOR );

    // "Complex" values

    DatabaseConnection.Attributes attributes = new DatabaseConnection.Attributes();
    generateMapExportEntries( attributes, 6 );
    databaseConnection.setAttributes( attributes );

    DatabaseConnection.ConnectionPoolingProperties connectionPoolingProperties =
      new DatabaseConnection.ConnectionPoolingProperties();
    generateMapExportEntries( connectionPoolingProperties, 10 );
    databaseConnection.setConnectionPoolingProperties( connectionPoolingProperties );

    DatabaseType databaseType = new DatabaseType();
    databaseType.setDefaultDatabaseName( DATABASE_TYPE_DEFAULT_NAME );
    databaseType.setDefaultDatabasePort( DATABASE_TYPE_DEFAULT_PORT );
    DatabaseType.DefaultOptions databaseTypeDefaultOptions = new DatabaseType.DefaultOptions();
    generateMapExportEntries( databaseTypeDefaultOptions, 5 );
    databaseType.setDefaultOptions( databaseTypeDefaultOptions );
    databaseType.setExtraOptionsHelpUrl( DATABASE_TYPE_EXTRA_OPTIONS_HELP_URL );
    databaseType.setName( DATABASE_TYPE_NAME );
    databaseType.setShortName( DATABASE_TYPE_SHORT_NAME );
    databaseType.getSupportedAccessTypes().add( SUPPORTED_DATABASE_ACCESS_TYPE_EXPORT );
    databaseConnection.setDatabaseType( databaseType );

    DatabaseConnection.ExtraOptions extraOptions = new DatabaseConnection.ExtraOptions();
    generateMapExportEntries( extraOptions, 2 );
    databaseConnection.setExtraOptions( extraOptions );

    DatabaseConnection.ExtraOptionsOrder extraOptionsOrder = new DatabaseConnection.ExtraOptionsOrder();
    generateMapExportEntries( extraOptionsOrder, 1 );
    databaseConnection.setExtraOptionsOrder( extraOptionsOrder );

    PartitionDatabaseMeta partitioningInformation = new PartitionDatabaseMeta();
    partitioningInformation.setDatabaseName( PARTITIONING_INFORMATION_DATABASE_NAME );
    partitioningInformation.setHostname( PARTITIONING_INFORMATION_HOSTNAME );
    partitioningInformation.setPartitionId( PARTITIONING_INFORMATION_PARTITION_ID );
    partitioningInformation.setPassword( PARTITIONING_INFORMATION_PASSWORD );
    partitioningInformation.setPort( PARTITIONING_INFORMATION_PORT );
    partitioningInformation.setUsername( PARTITIONING_INFORMATION_USERNAME );
    databaseConnection.getPartitioningInformation().add( partitioningInformation );

    return databaseConnection;
  }

  protected void generateMapEntries( Map<String, String> map, int entries ) {
    for ( int i = 0; i < entries; ++i ) {
      String entryNumber = String.valueOf( i );
      map.put( DUMMY_MAP_KEY + entryNumber, DUMMY_MAP_VALUE + entryNumber );
    }
  }

  protected void generateMapExportEntries( MapExport mapExport, int entries ) {
    for ( int i = 0; i < entries; ++i ) {
      String entryNumber = String.valueOf( i );
      MapExport.Entry exportEntry = new MapExport.Entry();
      exportEntry.setKey( DUMMY_EXPORT_KEY + entryNumber );
      exportEntry.setValue( DUMMY_EXPORT_VALUE + entryNumber );
      mapExport.getEntry().add( exportEntry );
    }
  }

  /**
   * Method to assert that the information on the DatabaseType that existed on the Model was correctly exported
   *
   * @param databaseTypeModel  model instance that contains the original information
   * @param databaseTypeExport export instance that should contain the information
   */
  protected void assertDatabaseTypeEquals2model( org.pentaho.database.model.IDatabaseType databaseTypeModel,
                                                 DatabaseType databaseTypeExport ) {
    assertNotNull( databaseTypeExport );
    assertEquals( databaseTypeModel.getName(), databaseTypeExport.getName() );
    assertEquals( databaseTypeModel.getShortName(), databaseTypeExport.getShortName() );
    List<DatabaseAccessType> supportedDatabaseAccessTypesExport = databaseTypeExport.getSupportedAccessTypes();
    assertNotNull( supportedDatabaseAccessTypesExport );
    assertEquals( databaseTypeModel.getSupportedAccessTypes().size(),
      supportedDatabaseAccessTypesExport.size() );
    // Check that all original values are present.
    for ( org.pentaho.database.model.DatabaseAccessType databaseAccessType : databaseTypeModel
      .getSupportedAccessTypes() ) {
      boolean found = false;
      for ( DatabaseAccessType databaseAccessTypeExport : supportedDatabaseAccessTypesExport ) {
        if ( databaseAccessType.getName().equals( databaseAccessTypeExport.name() )
          && databaseAccessType.getValue().equals( databaseAccessTypeExport.value() ) ) {
          found = true;
          break;
        }
      }
      assertTrue( found );
    }
    assertEquals( databaseTypeModel.getDefaultDatabasePort(), databaseTypeExport.getDefaultDatabasePort() );
    assertEquals( databaseTypeModel.getExtraOptionsHelpUrl(), databaseTypeExport.getExtraOptionsHelpUrl() );

    assertMapEquals2model( databaseTypeModel.getDefaultOptions(), databaseTypeExport.getDefaultOptions() );
  }

  /**
   * Method to assert that the information on the DatabaseType that existed on the Export was correctly imported
   *
   * @param databaseTypeExport export instance that contains the original information
   * @param databaseTypeModel  model instance that should contain the information
   */
  protected void assertDatabaseTypeEquals2export( DatabaseType databaseTypeExport,
                                                  org.pentaho.database.model.IDatabaseType databaseTypeModel ) {
    assertNotNull( databaseTypeModel );
    assertEquals( databaseTypeExport.getName(), databaseTypeModel.getName() );
    assertEquals( databaseTypeExport.getShortName(), databaseTypeModel.getShortName() );
    List<org.pentaho.database.model.DatabaseAccessType> supportedDatabaseAccessTypesModel =
      databaseTypeModel.getSupportedAccessTypes();
    assertNotNull( supportedDatabaseAccessTypesModel );
    assertEquals( databaseTypeExport.getSupportedAccessTypes().size(),
      supportedDatabaseAccessTypesModel.size() );
    // Check that all original values are present.
    for ( DatabaseAccessType databaseAccessType : databaseTypeExport
      .getSupportedAccessTypes() ) {
      boolean found = false;
      for ( org.pentaho.database.model.DatabaseAccessType databaseAccessTypeModel :
        supportedDatabaseAccessTypesModel ) {
        if ( databaseAccessType.name().equals( databaseAccessTypeModel.getName() )
          && databaseAccessType.value().equals( databaseAccessTypeModel.getValue() ) ) {
          found = true;
          break;
        }
      }
      assertTrue( found );
    }
    assertEquals( databaseTypeExport.getDefaultDatabasePort(), databaseTypeModel.getDefaultDatabasePort() );
    assertEquals( databaseTypeExport.getExtraOptionsHelpUrl(), databaseTypeModel.getExtraOptionsHelpUrl() );
    assertMapEquals2export( databaseTypeExport.getDefaultOptions(), databaseTypeModel.getDefaultOptions(), 0 );
  }

  /**
   * Method to assert that the information on the DatabaseType that existed on the Model was correctly exported
   *
   * @param partitionDatabaseMetaModelList  model instance that contains the original information
   * @param partitionDatabaseMetaExportList export instance that should contain the information
   */
  protected void assertPartitionDatabaseMetaEquals2model(
    List<org.pentaho.database.model.PartitionDatabaseMeta> partitionDatabaseMetaModelList,
    List<PartitionDatabaseMeta> partitionDatabaseMetaExportList ) {

    assertNotNull( partitionDatabaseMetaModelList );
    assertEquals( partitionDatabaseMetaExportList.size(), partitionDatabaseMetaModelList.size() );

    // Check that all original values are present.
    for ( PartitionDatabaseMeta partitionDatabaseMetaExport : partitionDatabaseMetaExportList ) {
      boolean found = false;
      for ( org.pentaho.database.model.PartitionDatabaseMeta partitionDatabaseMetaModel :
        partitionDatabaseMetaModelList ) {
        if ( partitionDatabaseMetaExport.getDatabaseName().equals( partitionDatabaseMetaModel.getDatabaseName() ) ) {
          assertEquals( partitionDatabaseMetaExport.getHostname(), partitionDatabaseMetaModel.getHostname() );
          assertEquals( partitionDatabaseMetaExport.getPartitionId(), partitionDatabaseMetaModel.getPartitionId() );
          assertEquals( partitionDatabaseMetaExport.getPassword(), partitionDatabaseMetaModel.getPassword() );
          assertEquals( partitionDatabaseMetaExport.getPort(), partitionDatabaseMetaModel.getPort() );
          assertEquals( partitionDatabaseMetaExport.getUsername(), partitionDatabaseMetaModel.getUsername() );
          found = true;
          break;
        }
      }
      assertTrue( found );
    }
  }

  /**
   * Method to assert that the information on the DatabaseType that existed on the Export was correctly imported
   *
   * @param partitionDatabaseMetaExportList export instance that contains the original information
   * @param partitionDatabaseMetaModelList  model instance that should contain the information
   */
  protected void assertPartitionDatabaseMetaEquals2export(
    List<PartitionDatabaseMeta> partitionDatabaseMetaExportList,
    List<org.pentaho.database.model.PartitionDatabaseMeta> partitionDatabaseMetaModelList ) {

    assertNotNull( partitionDatabaseMetaModelList );
    assertEquals( partitionDatabaseMetaExportList.size(), partitionDatabaseMetaModelList.size() );

    // Check that all original values are present.
    for ( PartitionDatabaseMeta partitionDatabaseMetaExport : partitionDatabaseMetaExportList ) {
      boolean found = false;
      for ( org.pentaho.database.model.PartitionDatabaseMeta partitionDatabaseMetaModel :
        partitionDatabaseMetaModelList ) {
        if ( partitionDatabaseMetaExport.getDatabaseName().equals( partitionDatabaseMetaModel.getDatabaseName() ) ) {
          assertEquals( partitionDatabaseMetaExport.getHostname(), partitionDatabaseMetaModel.getHostname() );
          assertEquals( partitionDatabaseMetaExport.getPartitionId(), partitionDatabaseMetaModel.getPartitionId() );
          assertEquals( partitionDatabaseMetaExport.getPassword(), partitionDatabaseMetaModel.getPassword() );
          assertEquals( partitionDatabaseMetaExport.getPort(), partitionDatabaseMetaModel.getPort() );
          assertEquals( partitionDatabaseMetaExport.getUsername(), partitionDatabaseMetaModel.getUsername() );
          found = true;
          break;
        }
      }
      assertTrue( found );
    }
  }

  /**
   * Method to assert that the information on a map that existed on the Model was correctly exported
   *
   * @param modelMap  model instance that contains the original information
   * @param exportMap export instance that should contain the information
   */
  protected void assertMapEquals2model( Map<String, String> modelMap, MapExport exportMap ) {
    assertNotNull( exportMap );
    List<MapExport.Entry> exportList = exportMap.getEntry();
    assertNotNull( exportList );
    assertEquals( modelMap.size(), exportList.size() );
    for ( Map.Entry<String, String> mapEntry : modelMap.entrySet() ) {
      boolean found = false;
      for ( MapExport.Entry exportEntry : exportList ) {
        if ( mapEntry.getKey().equals( exportEntry.getKey() ) && mapEntry.getValue()
          .equals( exportEntry.getValue() ) ) {
          found = true;
          break;
        }
      }
      assertTrue( found );
    }
  }

  /**
   * Method to assert that the information on a map that existed on the Export was correctly imported
   *
   * @param exportMap    export instance that contains the original information
   * @param modelMap     model instance that should contain the information
   * @param extraEntries value to add to the expected size (in certain cases there're extra entries)
   */
  protected void assertMapEquals2export( MapExport exportMap, Map<String, String> modelMap, int extraEntries ) {
    assertNotNull( modelMap );
    assertEquals( extraEntries + exportMap.getEntry().size(), modelMap.size() );
    for ( MapExport.Entry mapEntry : exportMap.getEntry() ) {
      boolean found = false;
      for ( Map.Entry<String, String> exportEntry : modelMap.entrySet() ) {
        if ( exportEntry.getKey().equals( mapEntry.getKey() ) && exportEntry.getValue()
          .equals( mapEntry.getValue() ) ) {
          found = true;
          break;
        }
      }
      assertTrue( found );
    }
  }
}
