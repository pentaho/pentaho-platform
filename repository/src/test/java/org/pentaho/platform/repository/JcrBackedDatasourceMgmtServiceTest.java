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

import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.*;

@SuppressWarnings( "nls" )
@RunWith( MockitoJUnitRunner.class )
public class JcrBackedDatasourceMgmtServiceTest {

  // ~ Instance fields
  // =================================================================================================

  // ~ Static fields/initializers
  // ======================================================================================

  private static final String EXP_FILE_ID = "456";

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

    JcrBackedDatasourceMgmtService spy = spy( new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() ) );
    when( spy.hasDataAccessPermission() ).thenReturn( true );

    IDatabaseConnection databaseConnection = createDatabaseConnection( EXP_DBMETA_NAME );
    spy.createDatasource( databaseConnection );

    verify( repo ).createFile( eq( parentFolderId ),
      argThat( isLikeFile( new RepositoryFile.Builder( EXP_DBMETA_NAME + ".kdb" ).build() ) ),
      argThat( hasData( pathPropertyPair( "/databaseMeta/HOST_NAME", EXP_DBMETA_HOSTNAME ) ) ), nullable( String.class ) );
  }

  @Test( expected = DatasourceMgmtServiceException.class )
  public void testCreateDatasourceNoAccess() throws Exception {
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
      argThat( hasData( pathPropertyPair( "/databaseMeta/HOST_NAME", EXP_DBMETA_HOSTNAME ) ) ),
      nullable( String.class ) );
  }

  @Rule
  public ExpectedException datasourceException = ExpectedException.none();

  @Test
  public void testDeleteDatasourceWithName() throws Exception {
    testDeleteDatasourceWithName(false );
  }

  @Test
  public void testDeleteDatasourceWithNameExceptionNullFile() throws Exception {
    testDeleteDatasourceWithName(true );
  }

  @Test( expected = DatasourceMgmtServiceException.class )
  public void testDeleteDatasourceWithNameNoAccess() throws Exception {
    testDeleteDatasourceWithNameNoAccess( false );
  }

  private void testDeleteDatasourceWithName( boolean throwException ) throws Exception {
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
    JcrBackedDatasourceMgmtService spy = spy( new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() ) );
    when( spy.hasDataAccessPermission() ).thenReturn( true );
    if ( throwException ) {
      deleteDatasourceWithNameThrowException( repo );
    }
    spy.deleteDatasourceByName( EXP_DBMETA_NAME );
    verify( repo ).deleteFile( eq( fileId ), eq( true ), nullable( String.class ) );
  }

  private void testDeleteDatasourceWithNameNoAccess( boolean throwException ) throws Exception {
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
    if ( throwException ) {
      deleteDatasourceWithNameThrowException( repo );
    }
    datasourceMgmtService.deleteDatasourceByName( EXP_DBMETA_NAME );
    verify( repo ).deleteFile( eq( fileId ), eq( true ), nullable( String.class ) );
  }

  @Test
  public void testDeleteDatasourceWithId() throws Exception {
    testDeleteDatasourceWithId( false );
  }

  @Test
  public void testDeleteDatasourceWithIdNullFile() throws Exception {
    testDeleteDatasourceWithId( true );
  }

  @Test( expected = DatasourceMgmtServiceException.class )
  public void testDeleteDatasourceWithIdNoAccess() throws Exception {
    testDeleteDatasourceWithIdNoAccess( false );
  }

  private void testDeleteDatasourceWithId( boolean throwException ) throws Exception {
    final String dotKdb = ".kdb";
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // stub out get parent folder
    doReturn( new RepositoryFile.Builder( "123", "databases" ).folder( true ).build() ).when( repo ).getFileById(
      EXP_FILE_ID );
    doReturn( reservedChars ).when( repo ).getReservedChars();
    // stub out get file to delete
    doReturn( new RepositoryFile.Builder( EXP_FILE_ID, EXP_DBMETA_NAME + dotKdb ).build() ).when( repo ).getFileById(
      EXP_FILE_ID);
    JcrBackedDatasourceMgmtService spy = spy( new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() ) );
    when( spy.hasDataAccessPermission() ).thenReturn( true );
    if ( throwException ) {
      deleteDatasourceWithIdThrowException( repo );
    }
    spy.deleteDatasourceById( EXP_FILE_ID );
    verify( repo ).deleteFile( eq( EXP_FILE_ID ), eq( true ), nullable( String.class ) );
  }

  private void testDeleteDatasourceWithIdNoAccess( boolean throwException ) throws Exception {
    final String dotKdb = ".kdb";
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // stub out get parent folder
    doReturn( new RepositoryFile.Builder( "123", "databases" ).folder( true ).build() ).when( repo ).getFileById(
      EXP_FILE_ID );
    doReturn( reservedChars ).when( repo ).getReservedChars();
    // stub out get file to delete
    doReturn( new RepositoryFile.Builder( EXP_FILE_ID, EXP_DBMETA_NAME + dotKdb ).build() ).when( repo ).getFileById(
      EXP_FILE_ID );
    IDatasourceMgmtService datasourceMgmtService =
      new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() );
    if ( throwException ) {
      deleteDatasourceWithIdThrowException( repo );
    }
    datasourceMgmtService.deleteDatasourceById( EXP_FILE_ID );
    verify( repo ).deleteFile( eq( EXP_FILE_ID ), eq( true ), nullable( String.class ) );
  }

  @Test
  public void testgetDatasourceById() throws Exception {
    testgetDatasourceById( false );
  }

  @Test
  public void testgetDatasourceByIdException() throws Exception {
    testgetDatasourceById( true );
  }

  private void testgetDatasourceById( boolean throwException ) throws Exception {
    final String dotKdb = ".kdb";
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    NodeRepositoryFileData nodeRep = mock ( NodeRepositoryFileData.class );
    DataNode dataNode = mock( DataNode.class );
    // stub out get parent folder
    doReturn( new RepositoryFile.Builder( "123", "databases" ).folder( true ).build() ).when( repo ).getFileById(
      EXP_FILE_ID );
    doReturn( reservedChars ).when( repo ).getReservedChars();
    // stub out get file to delete
    doReturn( new RepositoryFile.Builder( EXP_FILE_ID, EXP_DBMETA_NAME + dotKdb ).build() ).when( repo ).getFileById(
      EXP_FILE_ID);
    doReturn( nodeRep ).when(repo).getDataForRead( any(), any() );
    doReturn( dataNode ).when( nodeRep ).getNode();
    IDatasourceMgmtService datasourceMgmtService =
      new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() );

    if( throwException ) {
      getDatasourceWithIdThrowException( repo );
    }
    assertNotNull( datasourceMgmtService.getDatasourceById( EXP_FILE_ID ) );
  }

  private void deleteDatasourceWithIdThrowException( IUnifiedRepository repo ) {
    when( repo.getFileById( nullable( String.class ) ) ).thenThrow( UnifiedRepositoryException.class );
    datasourceException.expect(DatasourceMgmtServiceException.class);
    datasourceException.expectMessage("DatasourceMgmtService.ERROR_0002 - Error occurred during deleting the datasource " + EXP_FILE_ID + ". Cause: null");
  }

  private void getDatasourceWithIdThrowException( IUnifiedRepository repo ) {
    when( repo.getFileById( nullable( String.class ) ) ).thenThrow( UnifiedRepositoryException.class );
    datasourceException.expect(DatasourceMgmtServiceException.class);
    datasourceException.expectMessage("DatasourceMgmtService.ERROR_0004 - Error occurred during retrieving the datasource " + EXP_FILE_ID + ". Cause: {1}");
  }

  private void deleteDatasourceWithNameThrowException( IUnifiedRepository repo ) {
    when( repo.getFile( nullable( String.class ) ) ).thenThrow( UnifiedRepositoryException.class );
    datasourceException.expect( DatasourceMgmtServiceException.class );
    datasourceException.expectMessage("DatasourceMgmtService.ERROR_0002 - Error occurred during deleting the datasource " + EXP_DBMETA_NAME + ". Cause: null");
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
      .updateFile( nullable( RepositoryFile.class ), nullable( NodeRepositoryFileData.class ), nullable( String.class ) );
    JcrBackedDatasourceMgmtService spy = spy( new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() ) );

    IDatabaseConnection databaseConnection = createDatabaseConnection( EXP_DBMETA_NAME );
    when( spy.hasDataAccessPermission() ).thenReturn( true );
    updateDatabaseConnection( databaseConnection );
    spy.updateDatasourceByName( EXP_DBMETA_NAME, databaseConnection );

    verify( repo ).updateFile( argThat( isLikeFile( new RepositoryFile.Builder( EXP_DBMETA_NAME + ".kdb" ).build() ) ),
      argThat( hasData( pathPropertyPair( "/databaseMeta/HOST_NAME", EXP_UPDATED_DBMETA_HOSTNAME ) ) ), nullable( String.class ) );
  }

  @Test( expected = DatasourceMgmtServiceException.class )
  public void testUpdateDatasourceWithNameNoAccess() throws Exception {
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
      .updateFile( nullable( RepositoryFile.class ), nullable( NodeRepositoryFileData.class ), nullable( String.class ) );
    IDatasourceMgmtService datasourceMgmtService =
      new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() );

    IDatabaseConnection databaseConnection = createDatabaseConnection( EXP_DBMETA_NAME );
    updateDatabaseConnection( databaseConnection );
    datasourceMgmtService.updateDatasourceByName( EXP_DBMETA_NAME, databaseConnection );

    verify( repo ).updateFile( argThat( isLikeFile( new RepositoryFile.Builder( EXP_DBMETA_NAME + ".kdb" ).build() ) ),
      argThat( hasData( pathPropertyPair( "/databaseMeta/HOST_NAME", EXP_UPDATED_DBMETA_HOSTNAME ) ) ), nullable( String.class ) );
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