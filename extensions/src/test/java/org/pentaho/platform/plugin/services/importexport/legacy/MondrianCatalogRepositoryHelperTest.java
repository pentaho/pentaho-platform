/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.plugin.services.importexport.legacy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.pentaho.platform.api.repository.RepositoryException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper.ENCRYPTED_DATASOURCE_INFO_PROPERTY;
import static org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper.ETC_MONDRIAN_JCR_FOLDER;

public class MondrianCatalogRepositoryHelperTest {

  @Mock
  IUnifiedRepository repository;

  @Spy
  TestPasswordService passwordService;

  MondrianCatalogRepositoryHelper mondrianCatalogRepositoryHelper;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks( this );
    RepositoryFile repositoryFile = mock( RepositoryFile.class );
    when( repository.getFile( nullable( String.class ) ) ).thenReturn( repositoryFile );
    mondrianCatalogRepositoryHelper = new MondrianCatalogRepositoryHelper( repository, passwordService );
  }

  @Test( expected = RepositoryException.class )
  public void testGetMondrianSchemaFilesInvalidCatalogName() {
    String testCatalogName = "testCatalogName";
    when( repository.getFile( ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + testCatalogName ) ).thenReturn( null );
    mondrianCatalogRepositoryHelper.getMondrianSchemaFiles( testCatalogName );
  }

  static class TestPasswordService implements IPasswordService {
    private static final String TOKEN = "encrypted";

    @Override
    public String encrypt( String password ) {
      return TOKEN + password;
    }

    @Override
    public String decrypt( String encryptedPassword ) {
      return encryptedPassword.replace( TOKEN, "" );
    }
  }

  /**
   * Test that if the decryption of the datasource info fails, dataSourceInfo is set to null
   */
  @Test
  public void testDataSourceDecryptedWithException() {
    String testCatalogName = "testCatalogName";
    String testDataSourceInfo = "dummyDataSourceInfo";
    RepositoryFile mockMetadataFolder = mock( RepositoryFile.class );
    NodeRepositoryFileData mockMetadataFileData = mock( NodeRepositoryFileData.class );
    DataNode mockDataNode = mock( DataNode.class );

    when( repository.getFile( String.format( "/etc/mondrian/%s/metadata", testCatalogName ) ) ).thenReturn(
      mockMetadataFolder );
    when( repository.getDataForRead( any(), any() ) ).thenReturn( mockMetadataFileData );

    when( mockMetadataFolder.getId() ).thenReturn( 2 );
    when( mockMetadataFileData.getNode() ).thenReturn( mockDataNode );

    when( mockDataNode.getProperty( "datasourceInfo" ) ).thenReturn(
      new DataProperty( "datasourceInfo", new TestPasswordService().encrypt( testDataSourceInfo ),
        DataNode.DataPropertyType.STRING ) );
    when( mockDataNode.getProperty( "definition" ) ).thenReturn(
      new DataProperty( "definition", "",
        DataNode.DataPropertyType.STRING ) );
    when( mockDataNode.getProperty( ENCRYPTED_DATASOURCE_INFO_PROPERTY ) ).thenReturn(
      new DataProperty( ENCRYPTED_DATASOURCE_INFO_PROPERTY, true,
        DataNode.DataPropertyType.BOOLEAN ) );

    doThrow( PasswordServiceException.class ).when( passwordService ).decrypt( any() );

    var catalog = mondrianCatalogRepositoryHelper.getHostedCatalogInfo( testCatalogName );
    assertNull( catalog.dataSourceInfo );
    verify( passwordService, times( 1 ) ).decrypt( any() );
  }


  /**
   * Test that id datasource info is encrypted, it is decrypted when read
   */
  @Test
  public void testDataSourceIsDecryptedWhenRead() {
    String testCatalogName = "testCatalogName";
    String testDataSourceInfo = "dummyDataSourceInfo";
    RepositoryFile mockMetadataFolder = mock( RepositoryFile.class );
    NodeRepositoryFileData mockMetadataFileData = mock( NodeRepositoryFileData.class );
    DataNode mockDataNode = mock( DataNode.class );

    when( repository.getFile( String.format( "/etc/mondrian/%s/metadata", testCatalogName ) ) ).thenReturn(
      mockMetadataFolder );
    when( repository.getDataForRead( any(), any() ) ).thenReturn( mockMetadataFileData );

    when( mockMetadataFolder.getId() ).thenReturn( 2 );
    when( mockMetadataFileData.getNode() ).thenReturn( mockDataNode );

    when( mockDataNode.getProperty( "datasourceInfo" ) ).thenReturn(
      new DataProperty( "datasourceInfo", new TestPasswordService().encrypt( testDataSourceInfo ),
        DataNode.DataPropertyType.STRING ) );
    when( mockDataNode.getProperty( "definition" ) ).thenReturn(
      new DataProperty( "definition", "",
        DataNode.DataPropertyType.STRING ) );
    when( mockDataNode.getProperty( ENCRYPTED_DATASOURCE_INFO_PROPERTY ) ).thenReturn(
      new DataProperty( ENCRYPTED_DATASOURCE_INFO_PROPERTY, true,
        DataNode.DataPropertyType.BOOLEAN ) );

    var catalog = mondrianCatalogRepositoryHelper.getHostedCatalogInfo( testCatalogName );
    assertEquals( testDataSourceInfo, catalog.dataSourceInfo );
    verify( passwordService, times( 1 ) ).decrypt( any() );
  }

  /**
   * Test that catalogs without encrypted datasource info are supported. Information that is not
   * encrypted is explicitly marked as not encrypted
   */
  @Test
  public void testDataSourceExplicitlyNotEncryptedIsSupported() {
    String testCatalogName = "testCatalogName";
    String testDataSourceInfo = "dummyDataSourceInfo";
    RepositoryFile mockMetadataFolder = mock( RepositoryFile.class );
    NodeRepositoryFileData mockMetadataFileData = mock( NodeRepositoryFileData.class );
    DataNode mockDataNode = mock( DataNode.class );

    when( repository.getFile( String.format( "/etc/mondrian/%s/metadata", testCatalogName ) ) ).thenReturn(
      mockMetadataFolder );
    when( repository.getDataForRead( any(), any() ) ).thenReturn( mockMetadataFileData );

    when( mockMetadataFolder.getId() ).thenReturn( 2 );
    when( mockMetadataFileData.getNode() ).thenReturn( mockDataNode );

    when( mockDataNode.getProperty( "datasourceInfo" ) ).thenReturn(
      new DataProperty( "datasourceInfo", testDataSourceInfo,
        DataNode.DataPropertyType.STRING ) );
    when( mockDataNode.getProperty( "definition" ) ).thenReturn(
      new DataProperty( "definition", "",
        DataNode.DataPropertyType.STRING ) );
    when( mockDataNode.getProperty( ENCRYPTED_DATASOURCE_INFO_PROPERTY ) ).thenReturn(
      new DataProperty( ENCRYPTED_DATASOURCE_INFO_PROPERTY, false,
        DataNode.DataPropertyType.BOOLEAN ) );

    var catalog = mondrianCatalogRepositoryHelper.getHostedCatalogInfo( testCatalogName );
    assertEquals( testDataSourceInfo, catalog.dataSourceInfo );
    verify( passwordService, times( 0 ) ).decrypt( any() );
  }

  /**
   * Test that catalogs without encrypted datasource info are supported. Information that is not
   * encrypted is implicitly obtained as there isn't an "encrypted" property (old definition)
   */
  @Test
  public void testDataSourceImplicitlyNotEncryptedIsSupported() {
    String testCatalogName = "testCatalogName";
    String testDataSourceInfo = "dummyDataSourceInfo";
    RepositoryFile mockMetadataFolder = mock( RepositoryFile.class );
    NodeRepositoryFileData mockMetadataFileData = mock( NodeRepositoryFileData.class );
    DataNode mockDataNode = mock( DataNode.class );

    when( repository.getFile( String.format( "/etc/mondrian/%s/metadata", testCatalogName ) ) ).thenReturn(
      mockMetadataFolder );
    when( repository.getDataForRead( any(), any() ) ).thenReturn( mockMetadataFileData );

    when( mockMetadataFolder.getId() ).thenReturn( 2 );
    when( mockMetadataFileData.getNode() ).thenReturn( mockDataNode );

    when( mockDataNode.getProperty( "datasourceInfo" ) ).thenReturn(
      new DataProperty( "datasourceInfo", testDataSourceInfo,
        DataNode.DataPropertyType.STRING ) );
    when( mockDataNode.getProperty( "definition" ) ).thenReturn(
      new DataProperty( "definition", "",
        DataNode.DataPropertyType.STRING ) );
    var catalog = mondrianCatalogRepositoryHelper.getHostedCatalogInfo( testCatalogName );
    assertEquals( testDataSourceInfo, catalog.dataSourceInfo );
    verify( passwordService, times( 0 ) ).decrypt( any() );
  }

}
