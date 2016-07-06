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


package org.pentaho.platform.repository;

import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;

import java.util.HashMap;
import java.util.Map;

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
  public void testDataNodeToDatabaseConnection() {
    IDatabaseConnection name = databaseHelper.dataNodeToDatabaseConnection( null, "name", createDataNode() );
    Map<String, String> extraOptionsOrder = name.getExtraOptionsOrder();
    assertNotNull( extraOptionsOrder );
    assertTrue( extraOptionsOrder.size() > 0 );
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
