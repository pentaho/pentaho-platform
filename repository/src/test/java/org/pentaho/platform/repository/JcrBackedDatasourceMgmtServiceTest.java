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
 * Copyright 2006 - 2017 Hitachi Vantara.  All rights reserved.
 */

package org.pentaho.platform.repository;

import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.*;

@SuppressWarnings( "nls" )
public class JcrBackedDatasourceMgmtServiceTest {

  // ~ Instance fields
  // =================================================================================================

  // ~ Static fields/initializers
  // ======================================================================================

  private static final String EXP_DBMETA_NAME = "haha";

  private static final String EXP_DBMETA_HOSTNAME = "acme";

  private static final String EXP_DBMETA_PORT = "10521";

  private static final String EXP_UPDATED_DBMETA_NAME = "hahaUpdated";

  private static final String EXP_UPDATED_DBMETA_HOSTNAME = "acmeUpdated";

  private static final String EXP_UPDATED_DBMETA_PORT = "10522";

  private static final List<Character> reservedChars = Collections.emptyList();

  public JcrBackedDatasourceMgmtServiceTest() {
    super();
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
    if ( !KettleEnvironment.isInitialized() ) {
      throw new Exception( "Kettle Environment not initialized" );
    }
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCreateDatasource() throws Exception {
    final String parentFolderId = "123";
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // stub out get parent folder
    doReturn( new RepositoryFile.Builder( parentFolderId, "databases" ).folder( true ).build() ).when( repo ).getFile(
        "/etc/pdi/databases" );
    doReturn( reservedChars ).when( repo ).getReservedChars();

    IDatasourceMgmtService datasourceMgmtService =
        new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() );

    IDatabaseConnection databaseConnection = createDatabaseConnection( EXP_DBMETA_NAME );
    datasourceMgmtService.createDatasource( databaseConnection );

    verify( repo ).createFile( eq( parentFolderId ),
        argThat( isLikeFile( new RepositoryFile.Builder( EXP_DBMETA_NAME + ".kdb" ).build() ) ),
        argThat( hasData( pathPropertyPair( "/databaseMeta/HOST_NAME", EXP_DBMETA_HOSTNAME ) ) ), anyString() );
  }

  @Test
  public void testDeleteDatasourceWithName() throws Exception {
    final String fileId = "456";
    final String databasesFolderPath = "/etc/pdi/databases";
    final String dotKdb = ".kdb";
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // stub out get parent folder
    doReturn( new RepositoryFile.Builder( "123", "databases" ).folder( true ).build() ).when( repo ).getFile(
        databasesFolderPath );
    doReturn( reservedChars ).when( repo ).getReservedChars();
    // stub out get file to delete
    doReturn( new RepositoryFile.Builder( fileId, EXP_DBMETA_NAME + dotKdb ).build() ).when( repo ).getFile(
        databasesFolderPath + RepositoryFile.SEPARATOR + EXP_DBMETA_NAME + dotKdb );
    IDatasourceMgmtService datasourceMgmtService =
        new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() );

    datasourceMgmtService.deleteDatasourceByName( EXP_DBMETA_NAME );

    verify( repo ).deleteFile( eq( fileId ), eq( true ), anyString() );
  }

  @Test
  public void testUpdateDatasourceWithName() throws Exception {
    final String fileId = "456";
    final String databasesFolderPath = "/etc/pdi/databases";
    final String dotKdb = ".kdb";
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // stub out get parent folder
    doReturn( new RepositoryFile.Builder( "123", "databases" ).folder( true ).build() ).when( repo ).getFile(
        databasesFolderPath );
    doReturn( reservedChars ).when( repo ).getReservedChars();
    // stub out get file to update
    RepositoryFile f =
        new RepositoryFile.Builder( fileId, EXP_DBMETA_NAME + dotKdb ).path(
            databasesFolderPath + RepositoryFile.SEPARATOR + EXP_DBMETA_NAME + dotKdb ).build();
    doReturn( f ).when( repo ).getFile( databasesFolderPath + RepositoryFile.SEPARATOR + EXP_DBMETA_NAME + dotKdb );
    // stub out update file which requires a file to be returned
    doReturn( f ).when( repo )
        .updateFile( any( RepositoryFile.class ), any( NodeRepositoryFileData.class ), anyString() );
    IDatasourceMgmtService datasourceMgmtService =
        new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() );

    IDatabaseConnection databaseConnection = createDatabaseConnection( EXP_DBMETA_NAME );
    updateDatabaseConnection( databaseConnection );
    datasourceMgmtService.updateDatasourceByName( EXP_DBMETA_NAME, databaseConnection );

    verify( repo ).updateFile( argThat( isLikeFile( new RepositoryFile.Builder( EXP_DBMETA_NAME + ".kdb" ).build() ) ),
        argThat( hasData( pathPropertyPair( "/databaseMeta/HOST_NAME", EXP_UPDATED_DBMETA_HOSTNAME ) ) ), anyString() );
  }

  @Test
  public void testDatasourceNotFound() throws Exception {
    final String datasourceName = "not_here";
    final String dotKdb = ".kdb";
    final String fileName = datasourceName + dotKdb;
    final String databasesFolderPath = "/etc/pdi/databases";
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // stub out get parent folder
    doReturn( new RepositoryFile.Builder( "123", "databases" ).folder( true ).build() ).when( repo ).getFile(
        databasesFolderPath );
    doReturn( reservedChars ).when( repo ).getReservedChars();
    // stub out get file not found
    doReturn( null ).when( repo ).getFile( databasesFolderPath + RepositoryFile.SEPARATOR + fileName );
    IDatasourceMgmtService datasourceMgmtService =
        new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() );

    Assert.assertNull( datasourceMgmtService.getDatasourceByName( datasourceName ) );
  }

  @Test
  public void testGetDatasources() throws Exception {
    final String fileId = "456";
    final String databasesFolderPath = "/etc/pdi/databases";
    final String dotKdb = ".kdb";
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // stub out get parent folder
    doReturn( new RepositoryFile.Builder( "123", "databases" ).folder( true ).build() ).when( repo ).getFile(
        databasesFolderPath );
    doReturn( reservedChars ).when( repo ).getReservedChars();
    // stub out get file to update
    RepositoryFile f =
        new RepositoryFile.Builder( fileId, EXP_DBMETA_NAME + dotKdb ).path(
            databasesFolderPath + RepositoryFile.SEPARATOR + EXP_DBMETA_NAME + dotKdb ).build();
    doReturn( f ).when( repo ).getFile( databasesFolderPath + RepositoryFile.SEPARATOR + EXP_DBMETA_NAME + dotKdb );

    final String EXP_HOST_NAME = "hello";
    DataNode rootNode = new DataNode( "databaseMeta" );
    rootNode.setProperty( "TYPE", "Hypersonic" ); // required
    rootNode.setProperty( "HOST_NAME", EXP_HOST_NAME );
    rootNode.addNode( "attributes" ); // required
    doReturn( new NodeRepositoryFileData( rootNode ) ).when( repo ).getDataForRead( eq( fileId ),
        eq( NodeRepositoryFileData.class ) );

    IDatasourceMgmtService datasourceMgmtService =
        new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() );
    IDatabaseConnection conn = datasourceMgmtService.getDatasourceByName( EXP_DBMETA_NAME );

    assertEquals( EXP_HOST_NAME, conn.getHostname() );
  }

  private IDatabaseConnection createDatabaseConnection( final String dbName ) throws Exception {
    IDatabaseConnection dbConnection = new DatabaseConnection();
    dbConnection.setName( dbName );
    dbConnection.setHostname( EXP_DBMETA_HOSTNAME );
    dbConnection.setDatabaseType( mockDatabaseType( "Hypersonic" ) );
    dbConnection.setAccessType( DatabaseAccessType.NATIVE );
    dbConnection.setDatabasePort( EXP_DBMETA_PORT );
    return dbConnection;
  }

  private void updateDatabaseConnection( IDatabaseConnection dbConnection ) throws Exception {
    dbConnection.setName( EXP_UPDATED_DBMETA_NAME );
    dbConnection.setHostname( EXP_UPDATED_DBMETA_HOSTNAME );
    dbConnection.setDatabaseType( mockDatabaseType( "Generic database" ) );
    dbConnection.setAccessType( DatabaseAccessType.JNDI );
    dbConnection.setDatabasePort( EXP_UPDATED_DBMETA_PORT );
  }

  private IDatabaseType mockDatabaseType( final String shortName ) {
    IDatabaseType dbType = mock( IDatabaseType.class );
    doReturn( shortName ).when( dbType ).getShortName();
    return dbType;
  }

}
