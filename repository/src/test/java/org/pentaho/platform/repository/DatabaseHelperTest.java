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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */


package org.pentaho.platform.repository;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.BaseDatabaseMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DatabaseHelperTest {

  public static final String PROP_INDEX_TBS = "INDEX_TBS"; //$NON-NLS-1$

  public static final String PROP_DATA_TBS = "DATA_TBS"; //$NON-NLS-1$

  public static final String PROP_SERVERNAME = "SERVERNAME"; //$NON-NLS-1$

  public static final String PROP_PASSWORD = "PASSWORD"; //$NON-NLS-1$

  public static final String PROP_USERNAME = "USERNAME"; //$NON-NLS-1$

  public static final String PROP_PORT = "PORT"; //$NON-NLS-1$

  public static final String PROP_DATABASE_NAME = "DATABASE_NAME"; //$NON-NLS-1$

  public static final String PROP_HOST_NAME = "HOST_NAME"; //$NON-NLS-1$

  public static final String PROP_CONTYPE = "CONTYPE"; //$NON-NLS-1$

  public static final String PROP_TYPE = "TYPE"; //$NON-NLS-1$

  public static final String NODE_ROOT = "databaseMeta"; //$NON-NLS-1$

  public static final String NODE_ATTRIBUTES = "attributes"; //$NON-NLS-1$

  public static final String NODE_POOLING_PROPS = "poolProps"; //$NON-NLS-1$

  public static final String NODE_EXTRA_OPTIONS = "extraOptions"; //$NON-NLS-1$

  public static final String NODE_EXTRA_OPTIONS_ORDER = "extraOptionsOrder"; //$NON-NLS-1$

  public static final String PROP_CONNECT_SQL = "connectionSQL"; //$NON-NLS-1$

  public static final String PROP_INITIAL_POOL_SIZE = "initialPoolSize"; //$NON-NLS-1$

  public static final String PROP_MAX_POOL_SIZE = "maxPoolSize"; //$NON-NLS-1$

  public static final String PROP_IS_POOLING = "isPooling"; //$NON-NLS-1$

  public static final String PROP_IS_FORCING_TO_LOWER = "isForcingLower"; //$NON-NLS-1$

  public static final String PROP_IS_FORCING_TO_UPPER = "isForcingUpper"; //$NON-NLS-1$

  public static final String PROP_IS_QUOTE_FIELDS = "isQuoteFields"; //$NON-NLS-1$

  public static final String PROP_IS_DECIMAL_SEPERATOR = "isUsingDecimalSeperator"; //$NON-NLS-1$

  public static final String ATTRIBUTE_PORT_NUMBER = "PORT_NUMBER";

  final String DB_CONN_NAME = "DB_CONN_NAME";
  final String DB_NAME = "DB_NAME";
  final String DB_HOST = "DB_HOST";
  final String DB_PORT = "DB_PORT";
  final String DB_USERNAME = "DB_USERNAME";
  final String DB_PASSWORD = "DB_PASSWORD";
  final String DB_SERVER = "DB_SERVER";
  final String DB_TABLESPACE = "DB_TABLESPACE";
  final String DB_IDX_TABLESPACE = "DB_IDX_TABLESPACE";
  final String DB_CONN_SQL = "DB_CONN_SQL";
  final int DB_INIT_POOL_SIZE = 42;
  final int DB_MAX_POOL_SIZE = 43;
  final boolean DB_USING_POOL = true;
  final boolean DB_FORCING_LOWERCASE = true;
  final boolean DB_FORCING_UPPERCASE = false;
  final boolean DB_QUOTE_ALL = true;
  final boolean DB_DOUBLE_DECIMAL_SEPARATOR = true;

  private DatabaseHelper databaseHelper = new DatabaseHelper( new DatabaseDialectService() ) {

    @Override
    String decryptPassword( String passwordEncr ) {
      return passwordEncr;
    }

    @Override
    String encryptPassword( String password ) {
      return password;
    }
  };

  DataNode rootNode = new DataNode( NODE_ROOT );

  public void init() {
  }

  @Test
  public void testDatabaseConnectionToDataNodeAddsExtraOptions() {
    DataNode dataNode = databaseHelper.databaseConnectionToDataNode( createDatabaseConnection() );
    DataNode extraOptionsNode = dataNode.getNode( NODE_EXTRA_OPTIONS_ORDER );
    assertNotNull( extraOptionsNode );
    assertNotNull( extraOptionsNode.getProperties().iterator() );
    assertTrue( extraOptionsNode.getProperties().iterator().hasNext() );
  }

  @Test
  public void testDatabaseConnectionToDataNodePortSettingsLegacy() {
    DataNode dataNode = databaseHelper.databaseConnectionToDataNode( createDatabaseConnection() );
    DataNode attributesNode = dataNode.getNode( NODE_ATTRIBUTES );
    assertNotNull( attributesNode );
    Assert.assertEquals( "8080", attributesNode.getProperty( BaseDatabaseMeta.ATTRIBUTE_PORT_NUMBER ).getString() );
  }

  @Test
  public void testDatabaseConnectionToDataNodePortSettingsCurrent() {
    DatabaseConnection dbconn = createDatabaseConnection();
    dbconn.getAttributes().put( BaseDatabaseMeta.ATTRIBUTE_PORT_NUMBER, "${DBPORT}" );
    DataNode dataNode = databaseHelper.databaseConnectionToDataNode( dbconn );
    DataNode attributesNode = dataNode.getNode( NODE_ATTRIBUTES );
    assertNotNull( attributesNode );
    Assert.assertEquals( "${DBPORT}", attributesNode.getProperty( BaseDatabaseMeta.ATTRIBUTE_PORT_NUMBER ).getString() );
  }

  @Test
  public void testDataNodeToDatabaseConnection() {
    IDatabaseConnection name = databaseHelper.dataNodeToDatabaseConnection( null, "name", createDataNode() );
    Map<String, String> extraOptionsOrder = name.getExtraOptionsOrder();
    assertNotNull( extraOptionsOrder );
    assertTrue( extraOptionsOrder.size() > 0 );
  }

  @Test
  public void testDatabaseMetaToDatabaseConnectionWithVars() throws KettleException {
    final String DB_CONN_NAME_VAR = "DB_CONN_NAME_VAR";
    final String DB_NAME_VAR = "DB_NAME_VAR";
    final String DB_HOST_VAR = "DB_HOST_VAR";
    final String DB_PORT_VAR = "DB_PORT_VAR";
    final String DB_USERNAME_VAR = "DB_USERNAME_VAR";
    final String DB_PASSWORD_VAR = "DB_PASSWORD_VAR";
    final String DB_SERVER_VAR = "DB_SERVER_VAR";
    final String DB_TABLESPACE_VAR = "DB_TABLESPACE_VAR";
    final String DB_IDX_TABLESPACE_VAR = "DB_IDX_TABLESPACE_VAR";
    final String DB_CONN_SQL_VAR = "DB_CONN_SQL_VAR";
    final String CONN_VAL1_VAR = "CONN_VAL1_VAR";
    final String POOL_VAL1_VAR = "POOL_VAL1_VAR";
    final String EXTRA_VAL1_VAR = "EXTRA_VAL1_VAR";

    Properties dbAttributes = new Properties();

    dbAttributes.setProperty( "CONN_PROP1", wrapVar( CONN_VAL1_VAR ) );

    Properties dbPoolProps = new Properties();
    dbPoolProps.setProperty( "POOL_PROP1", wrapVar( POOL_VAL1_VAR ) );

    KettleClientEnvironment.init();

    DatabaseMeta dbMeta = new DatabaseMeta( DB_CONN_NAME, "GENERIC", "Native", DB_HOST, DB_NAME, DB_PORT, DB_USERNAME, DB_PASSWORD );
    Variables vars = new Variables();
    vars.setVariable( DB_CONN_NAME_VAR, DB_CONN_NAME );
    vars.setVariable( DB_NAME_VAR, DB_NAME );
    vars.setVariable( DB_HOST_VAR, DB_HOST );
    vars.setVariable( DB_PORT_VAR, DB_PORT );
    vars.setVariable( DB_USERNAME_VAR, DB_USERNAME );
    vars.setVariable( DB_PASSWORD_VAR, DB_PASSWORD );
    vars.setVariable( DB_SERVER_VAR, DB_SERVER );
    vars.setVariable( DB_TABLESPACE_VAR, DB_TABLESPACE );
    vars.setVariable( DB_IDX_TABLESPACE_VAR, DB_IDX_TABLESPACE );
    vars.setVariable( DB_CONN_SQL_VAR, DB_CONN_SQL );
    vars.setVariable( CONN_VAL1_VAR, "CONN_VAL1" );
    vars.setVariable( POOL_VAL1_VAR, "POOL_VAL1" );
    vars.setVariable( EXTRA_VAL1_VAR, "EXTRA_VAL1" );

    dbMeta.shareVariablesWith( vars );

    dbMeta.setName( wrapVar( DB_CONN_NAME_VAR ) );
    dbMeta.setHostname( wrapVar( DB_HOST_VAR ) );
    dbMeta.setDBPort( wrapVar( DB_PORT_VAR ) );
    dbMeta.setUsername( wrapVar( DB_USERNAME_VAR ) );
    dbMeta.setPassword( wrapVar( DB_PASSWORD_VAR ) );
    dbMeta.setServername( wrapVar( DB_SERVER_VAR ) );
    dbMeta.setDataTablespace( wrapVar( DB_TABLESPACE_VAR ) );
    dbMeta.setIndexTablespace( wrapVar( DB_IDX_TABLESPACE_VAR ) );
    dbMeta.setConnectSQL( wrapVar( DB_CONN_SQL_VAR ) );
    dbMeta.setInitialPoolSize( DB_INIT_POOL_SIZE );
    dbMeta.setMaximumPoolSize( DB_MAX_POOL_SIZE );
    dbMeta.setUsingConnectionPool( DB_USING_POOL );
    dbMeta.setForcingIdentifiersToLowerCase( DB_FORCING_LOWERCASE );
    dbMeta.setForcingIdentifiersToUpperCase( DB_FORCING_UPPERCASE );
    dbMeta.setQuoteAllFields( DB_QUOTE_ALL );
    dbMeta.setUsingDoubleDecimalAsSchemaTableSeparator( DB_DOUBLE_DECIMAL_SEPARATOR );
    dbMeta.getAttributes().putAll( dbAttributes );
    dbMeta.setConnectionPoolingProperties( dbPoolProps );
    dbMeta.addExtraOption( "FOO", "EXTRA_PROP1", wrapVar( EXTRA_VAL1_VAR ) );

    IDatabaseConnection testConnection = databaseHelper.databaseMetaToDatabaseConnection( dbMeta );
    assertEquals( "Conn name incorrect", DB_CONN_NAME, testConnection.getName() );
    assertEquals( "DB name incorrect", DB_NAME, testConnection.getDatabaseName() );
    assertEquals( "Host name incorrect", DB_HOST, testConnection.getHostname() );
    assertEquals( "Port incorrect", DB_PORT, testConnection.getDatabasePort() );
    assertEquals( "User name incorrect", DB_USERNAME, testConnection.getUsername() );
    assertEquals( "Password incorrect", DB_PASSWORD, testConnection.getPassword() );
    assertEquals( "Server name incorrect", DB_SERVER, testConnection.getInformixServername() );
    assertEquals( "DB tablespace incorrect", DB_TABLESPACE, testConnection.getDataTablespace() );
    assertEquals( "DB index tablespace incorrect", DB_IDX_TABLESPACE, testConnection.getIndexTablespace() );
    assertEquals( "Connect SQL incorrect", DB_CONN_SQL, testConnection.getConnectSql() );
    assertEquals( "Init pool size incorrect", DB_INIT_POOL_SIZE, testConnection.getInitialPoolSize() );
    assertEquals( "Max pool size incorrect", DB_MAX_POOL_SIZE, testConnection.getMaximumPoolSize() );
    assertEquals( "Using pool incorrect", DB_USING_POOL, testConnection.isUsingConnectionPool() );
    assertEquals( "Forcing lowercase incorrect", DB_FORCING_LOWERCASE, testConnection.isForcingIdentifiersToLowerCase() );
    assertEquals( "Forcing uppercase incorrect", DB_FORCING_UPPERCASE, testConnection.isForcingIdentifiersToUpperCase() );
    assertEquals( "Quote all fields incorrect", DB_QUOTE_ALL, testConnection.isQuoteAllFields() );
    assertEquals( "Using double decimal as schema table separator", DB_DOUBLE_DECIMAL_SEPARATOR, testConnection.isUsingDoubleDecimalAsSchemaTableSeparator() );
    assertEquals( "DB attributes incorrect", "CONN_VAL1", testConnection.getAttributes().get( "CONN_PROP1" ) );
    assertEquals( "Pool properties incorrect", "POOL_VAL1", testConnection.getConnectionPoolingProperties().get( "POOL_PROP1" ) );
    assertEquals( "Extra options incorrect", "EXTRA_VAL1", testConnection.getExtraOptions().get( "FOO.EXTRA_PROP1" ) );
  }

  private String wrapVar( String val ) {
    return "${" + val + "}";
  }

  @Test
  public void testDatabaseMetaToDatabaseConnection() throws KettleException {
    Properties dbAttributes = new Properties();
    dbAttributes.setProperty( "CONN_PROP1", "CONN_VAL1" );

    Properties dbPoolProps = new Properties();
    dbPoolProps.setProperty( "POOL_PROP1", "POOL_VAL1" );

    Map<String, String> dbExtraMap = new HashMap<>();
    dbExtraMap.put( "EXTRA_PROP1", "EXTRA_VAL1" );
    KettleClientEnvironment.init();

    DatabaseMeta dbMeta = new DatabaseMeta( DB_CONN_NAME, "GENERIC", "Native", DB_HOST, DB_NAME, DB_PORT, DB_USERNAME, DB_PASSWORD );

    dbMeta.setName( DB_CONN_NAME );
    dbMeta.setHostname( DB_HOST );
    dbMeta.setDBPort( DB_PORT );
    dbMeta.setUsername( DB_USERNAME );
    dbMeta.setPassword( DB_PASSWORD );
    dbMeta.setServername( DB_SERVER );
    dbMeta.setDataTablespace( DB_TABLESPACE );
    dbMeta.setIndexTablespace( DB_IDX_TABLESPACE );
    dbMeta.setConnectSQL( DB_CONN_SQL );
    dbMeta.setInitialPoolSize( DB_INIT_POOL_SIZE );
    dbMeta.setMaximumPoolSize( DB_MAX_POOL_SIZE );
    dbMeta.setUsingConnectionPool( DB_USING_POOL );
    dbMeta.setForcingIdentifiersToLowerCase( DB_FORCING_LOWERCASE );
    dbMeta.setForcingIdentifiersToUpperCase( DB_FORCING_UPPERCASE );
    dbMeta.setQuoteAllFields( DB_QUOTE_ALL );
    dbMeta.setUsingDoubleDecimalAsSchemaTableSeparator( DB_DOUBLE_DECIMAL_SEPARATOR );
    dbMeta.getAttributes().putAll( dbAttributes );
    dbMeta.setConnectionPoolingProperties( dbPoolProps );
    dbMeta.addExtraOption( "FOO", "EXTRA_PROP1", "EXTRA_VAL1" );

    IDatabaseConnection testConnection = databaseHelper.databaseMetaToDatabaseConnection( dbMeta );
    assertEquals( "Conn name incorrect", DB_CONN_NAME, testConnection.getName() );
    assertEquals( "DB name incorrect", DB_NAME, testConnection.getDatabaseName() );
    assertEquals( "Host name incorrect", DB_HOST, testConnection.getHostname() );
    assertEquals( "Port incorrect", DB_PORT, testConnection.getDatabasePort() );
    assertEquals( "User name incorrect", DB_USERNAME, testConnection.getUsername() );
    assertEquals( "Password incorrect", DB_PASSWORD, testConnection.getPassword() );
    assertEquals( "Server name incorrect", DB_SERVER, testConnection.getInformixServername() );
    assertEquals( "DB tablespace incorrect", DB_TABLESPACE, testConnection.getDataTablespace() );
    assertEquals( "DB index tablespace incorrect", DB_IDX_TABLESPACE, testConnection.getIndexTablespace() );
    assertEquals( "Connect SQL incorrect", DB_CONN_SQL, testConnection.getConnectSql() );
    assertEquals( "Init pool size incorrect", DB_INIT_POOL_SIZE, testConnection.getInitialPoolSize() );
    assertEquals( "Max pool size incorrect", DB_MAX_POOL_SIZE, testConnection.getMaximumPoolSize() );
    assertEquals( "Using pool incorrect", DB_USING_POOL, testConnection.isUsingConnectionPool() );
    assertEquals( "Forcing lowercase incorrect", DB_FORCING_LOWERCASE, testConnection.isForcingIdentifiersToLowerCase() );
    assertEquals( "Forcing uppercase incorrect", DB_FORCING_UPPERCASE, testConnection.isForcingIdentifiersToUpperCase() );
    assertEquals( "Quote all fields incorrect", DB_QUOTE_ALL, testConnection.isQuoteAllFields() );
    assertEquals( "Using double decimal as schema table separator", DB_DOUBLE_DECIMAL_SEPARATOR, testConnection.isUsingDoubleDecimalAsSchemaTableSeparator() );
    assertEquals( "DB attributes incorrect", "CONN_VAL1", testConnection.getAttributes().get( "CONN_PROP1" ) );
    assertEquals( "Pool properties incorrect", "POOL_VAL1", testConnection.getConnectionPoolingProperties().get( "POOL_PROP1" ) );
    assertEquals( "Extra options incorrect", "EXTRA_VAL1", testConnection.getExtraOptions().get( "FOO.EXTRA_PROP1" ) );
  }

  private DatabaseConnection createDatabaseConnection() {
    DatabaseConnection databaseConnection = new DatabaseConnection();

    databaseConnection.setDatabaseType( new DatabaseType() );
    databaseConnection.setName( "name" );
    databaseConnection.setId( "id" );
    databaseConnection.setAccessType( DatabaseAccessType.CUSTOM );

    databaseConnection.setHostname( "hostName" );
    databaseConnection.setDatabaseName( "databaseName" );
    databaseConnection.setDatabasePort( "8080" );
    databaseConnection.setUsername( "username" );
    databaseConnection.setPassword( "password" );
    databaseConnection.setInformixServername( "informixServername" );
    databaseConnection.setDataTablespace( "dataTablespace" );
    databaseConnection.setIndexTablespace( "indexTableSpace" );
    databaseConnection.setConnectSql( "connectSql" );
    databaseConnection.setInitialPoolSize( 1 );
    databaseConnection.setMaximumPoolSize( 1 );
    databaseConnection.setUsingConnectionPool( true );
    databaseConnection.setForcingIdentifiersToLowerCase( true );
    databaseConnection.setForcingIdentifiersToUpperCase( true );
    databaseConnection.setQuoteAllFields( true );
    databaseConnection.setUsingDoubleDecimalAsSchemaTableSeparator( true );

    Map<String, String> attributeMap = new HashMap<String, String>();
    attributeMap.put( "key", "value" );
    databaseConnection.setAttributes( attributeMap );

    Map<String, String> connectionPoolingMap = new HashMap<String, String>();
    connectionPoolingMap.put( "key", "value" );
    databaseConnection.setConnectionPoolingProperties( connectionPoolingMap );

    Map<String, String> extraOptions = new HashMap<String, String>();
    extraOptions.put( "key", "value" );
    databaseConnection.setExtraOptions( extraOptions );

    Map<String, String> extraOptionsOrder = new HashMap<String, String>();
    extraOptions.put( "key", "value" );
    databaseConnection.setExtraOptionsOrder( extraOptions );

    return databaseConnection;
  }

  private DataNode createDataNode() {
    DataNode rootNode = new DataNode( NODE_ROOT );

    rootNode.setProperty( PROP_TYPE, "portType" );
    rootNode.setProperty( PROP_CONTYPE, "contype" );
    rootNode.setProperty( PROP_HOST_NAME, "portType" );
    rootNode.setProperty( PROP_DATABASE_NAME, "databaseName" );
    rootNode.setProperty( PROP_PORT, 8080 );
    rootNode.setProperty( PROP_USERNAME, "username" );
    rootNode.setProperty( PROP_PASSWORD, "password" );
    rootNode.setProperty( PROP_SERVERNAME, "servername" );
    rootNode.setProperty( PROP_DATA_TBS, "dataTbs" );
    rootNode.setProperty( PROP_INDEX_TBS, "1" );
    rootNode.setProperty( PROP_CONNECT_SQL, "connectSql" );
    rootNode.setProperty( PROP_INITIAL_POOL_SIZE, "1" );
    rootNode.setProperty( PROP_MAX_POOL_SIZE, "1" );
    rootNode.setProperty( PROP_IS_POOLING, "true" );
    rootNode.setProperty( PROP_IS_FORCING_TO_LOWER, "true" );
    rootNode.setProperty( PROP_IS_FORCING_TO_UPPER, "true" );
    rootNode.setProperty( PROP_IS_QUOTE_FIELDS, "true" );
    rootNode.setProperty( PROP_IS_DECIMAL_SEPERATOR, "true" );

    DataNode attrNode = rootNode.addNode( NODE_ATTRIBUTES );
    attrNode.setProperty( "key", "value" );

    attrNode = rootNode.addNode( NODE_POOLING_PROPS );
    attrNode.setProperty( "key", "value" );

    attrNode = rootNode.addNode( NODE_EXTRA_OPTIONS );
    attrNode.setProperty( "key", "value" );

    attrNode = rootNode.addNode( NODE_EXTRA_OPTIONS_ORDER );
    attrNode.setProperty( "key", "value" );

    return rootNode;
  }
}
