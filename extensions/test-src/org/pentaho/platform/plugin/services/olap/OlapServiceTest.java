/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 *
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.platform.plugin.services.olap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.action.olap.IOlapServiceException;
import org.pentaho.platform.plugin.action.olap.impl.OlapServiceImpl;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.*;

public class OlapServiceTest extends TestCase {

  private IUnifiedRepository repository;
  private String mondrianFolderPath;
  private String olapFolderPath;
  private IPentahoSession session;
  private IOlapService olapService;

  @Override
  protected void setUp() throws Exception {

    repository = mock( IUnifiedRepository.class );

    // Stub /etc/mondrian
    mondrianFolderPath =
      ClientRepositoryPaths.getEtcFolderPath()
      + RepositoryFile.SEPARATOR
      + "mondrian";
    stubGetFolder( repository, mondrianFolderPath );

    // Stub /etc/olap-servers
    olapFolderPath =
      ClientRepositoryPaths.getEtcFolderPath()
      + RepositoryFile.SEPARATOR
      + "olap-servers";
    stubGetFolder( repository, olapFolderPath );

    // Create a session as admin.
    session = new StandaloneSession( "admin" );

    // Create the olap service.
    olapService = new OlapServiceImpl( repository );
  }

  protected void tearDown() throws Exception {
    repository = null;
    olapService = null;
    session = null;
    super.tearDown();
  }

  /**
   * Verifies that we can create locally hosted mondrian instances.
   */
  public void testImportHosted() throws Exception {

    final String testFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "test-server";
    stubGetFileDoesNotExist( repository, testFolderPath );
    stubCreateFolder( repository, testFolderPath );
    final String metadataPath = testFolderPath + RepositoryFile.SEPARATOR + "metadata";
    stubCreateFile( repository, metadataPath );

    final IPentahoSession session = new StandaloneSession( "admin" );
    final IOlapService olapService = new OlapServiceImpl( repository );

    final InputStream is =
      new FileInputStream(
        new File( "test-src/solution/security/steelwheels.mondrian.xml" ) );

    olapService.addHostedCatalog(
      "test-server",
      "Provider=mondrian;DataSource=SampleData",
      is,
      true,
      session );

    verify( repository ).createFile(
      eq( makeIdObject( testFolderPath ) ),
      argThat( isLikeFile( makeFileObject( metadataPath ) ) ),
      argThat( hasData(
        pathPropertyPair( "/catalog/definition", "mondrian:/" + "test-server" ),
        pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SampleData" )
      ) ), anyString() );

    verify( repository ).createFile( eq( makeIdObject( testFolderPath ) ),
      argThat( isLikeFile( makeFileObject( testFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
      any( IRepositoryFileData.class ), anyString() );
  }

  /**
   * Verifies that we can add generic olap4j connections.
   */
  public void testImportOlap4j() throws Exception {

    final String testFolderPath = olapFolderPath + RepositoryFile.SEPARATOR + "test-server-2";
    stubCreateFolder( repository, testFolderPath );

    final String metadataPath = testFolderPath + RepositoryFile.SEPARATOR + "metadata";
    stubCreateFile( repository, metadataPath );


    olapService.addOlap4jCatalog(
      "test-server-2",
      "class-name",
      "url",
      "user",
      "password",
      new Properties(),
      true,
      session );

    verify( repository ).createFile(
      eq( makeIdObject( testFolderPath ) ),
      argThat(
        isLikeFile( makeFileObject( metadataPath ) ) ),
      argThat(
        hasData(
          pathPropertyPair( "/server/name", "test-server-2" ),
          pathPropertyPair( "/server/className", "class-name" ),
          pathPropertyPair( "/server/URL", "url" ),
          pathPropertyPair( "/server/user", "user" ),
          pathPropertyPair( "/server/password", "password" ) ) ),
      anyString() );
  }

  /**
   * Validates getting a list of remote catalogs.
   */
  public void testGetOlap4jCatalogs() throws Exception {

    stubGetChildren( repository, olapFolderPath, "myServer" );

    // Stub /etc/olap-servers/myServer
    final String testServerPath =
      olapFolderPath
      + RepositoryFile.SEPARATOR
      + "myServer";
    stubGetFolder( repository, testServerPath );
    stubGetChildren( repository, testServerPath, "metadata" );

    // Stub /etc/olap-servers/myServer/metadata
    final String metadataPath =
      testServerPath
      + RepositoryFile.SEPARATOR
      + "metadata";
    stubGetFile( repository, metadataPath );
    stubGetData(
      repository,
      metadataPath
      + RepositoryFile.SEPARATOR
      + "myServer",
      "server",
      pathPropertyPair( "/server/name", "myServer" ),
      pathPropertyPair( "/server/user", "myUser" ),
      pathPropertyPair( "/server/password", "myPassword" ),
      pathPropertyPair( "/server/URL", "myUrl" ),
      pathPropertyPair( "/server/className", "someClass" ) );

    // Get a list of catalogs.
    final List<String> catalogs = olapService.getCatalogs( session );

    assertEquals( 1, catalogs.size() );
    assertEquals( "myServer", catalogs.get( 0 ) );

    verify( repository ).getChildren(
      eq( makeIdObject( olapFolderPath ) ) );

    // Now check for non-existent catalogs
    try {
      olapService.getConnection( "someName", session );
      fail();
    } catch ( IOlapServiceException e ) {
      assertEquals(
        "MondrianCatalogHelper.ERROR_0015 - Catalog someName not found",
        e.getMessage() );
    }
  }

  /**
   * Validates getting a list of hosted catalogs.
   */
  public void testGetHostedCatalogs() throws Exception {

    stubGetChildren( repository, mondrianFolderPath, "myHostedServer" );

    // Stub /etc/mondrian/myHostedServer
    final String testServerPath =
      mondrianFolderPath
      + RepositoryFile.SEPARATOR
      + "myHostedServer";
    stubGetFolder( repository, testServerPath );
    stubGetChildren( repository, testServerPath, "metadata" );

    // Stub /etc/mondrian/myServer/myHostedServer
    final String metadataPath =
      testServerPath
      + RepositoryFile.SEPARATOR
      + "metadata";
    stubGetFile( repository, metadataPath );
    stubGetData(
      repository,
      metadataPath,
      "catalog",
      pathPropertyPair( "/catalog/definition", "mondrian:/SteelWheels" ),
      pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SteelWheels;" ) );

    // Get a list of catalogs.
    final List<String> catalogs = olapService.getCatalogs( session );

    assertEquals( 1, catalogs.size() );
    assertEquals( "myHostedServer", catalogs.get( 0 ) );

    verify( repository ).getChildren(
      eq( makeIdObject( mondrianFolderPath ) ) );

    // Now check for non-existent catalogs
    try {
      olapService.getConnection( "someName", session );
      fail();
    } catch ( IOlapServiceException e ) {
      assertEquals(
        "MondrianCatalogHelper.ERROR_0015 - Catalog someName not found",
        e.getMessage() );
    }
  }

  /**
   * Validates getting a list of hosted catalogs. They must come
   * back sorted correctly.
   */
  public void testGetAllCatalogs() throws Exception {

    // Stub the hosted server
    stubGetChildren( repository, mondrianFolderPath, "myHostedServer" );
    final String testServerPath =
      mondrianFolderPath
      + RepositoryFile.SEPARATOR
      + "myHostedServer";
    stubGetFolder( repository, testServerPath );
    stubGetChildren( repository, testServerPath, "metadata" );
    final String metadataPath =
      testServerPath
      + RepositoryFile.SEPARATOR
      + "metadata";
    stubGetFile( repository, metadataPath );
    stubGetData(
      repository,
      metadataPath,
      "catalog",
      pathPropertyPair( "/catalog/definition", "mondrian:/SteelWheels" ),
      pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SteelWheels;" ) );

    // Stub the remote server
    stubGetChildren( repository, olapFolderPath, "myServer" );
    final String remoteServerPath =
      olapFolderPath
      + RepositoryFile.SEPARATOR
      + "myServer";
    stubGetFolder( repository, remoteServerPath );
    stubGetChildren( repository, remoteServerPath, "metadata" );
    final String remoteMetadataPath =
      testServerPath
      + RepositoryFile.SEPARATOR
      + "metadata";
    stubGetFile( repository, remoteMetadataPath );
    stubGetData(
      repository,
      remoteMetadataPath
      + RepositoryFile.SEPARATOR
      + "myServer",
      "server",
      pathPropertyPair( "/server/name", "myServer" ),
      pathPropertyPair( "/server/user", "myUser" ),
      pathPropertyPair( "/server/password", "myPassword" ),
      pathPropertyPair( "/server/URL", "myUrl" ),
      pathPropertyPair( "/server/className", "someClass" ) );

    // Get a list of catalogs.
    final List<String> catalogs = olapService.getCatalogs( session );

    assertEquals( 2, catalogs.size() );
    assertEquals( "myHostedServer", catalogs.get( 0 ) );
    assertEquals( "myServer", catalogs.get( 1 ) );

    verify( repository ).getChildren(
      eq( makeIdObject( mondrianFolderPath ) ) );
    verify( repository ).getChildren(
      eq( makeIdObject( olapFolderPath ) ) );
  }

  /**
   * Validates getting a list of remote catalogs.
   */
  public void testRemoveOlap4jCatalogs() throws Exception {

    stubGetChildren( repository, olapFolderPath, "myServer" );

    // Stub /etc/olap-servers/myServer
    final String testServerPath =
      olapFolderPath
      + RepositoryFile.SEPARATOR
      + "myServer";
    stubGetFolder( repository, testServerPath );
    stubGetChildren( repository, testServerPath, "metadata" );

    // Stub /etc/olap-servers/myServer/metadata
    final String metadataPath =
      testServerPath
      + RepositoryFile.SEPARATOR
      + "metadata";
    stubGetFile( repository, metadataPath );
    stubGetData(
      repository,
      metadataPath
      + RepositoryFile.SEPARATOR
      + "myServer",
      "server",
      pathPropertyPair( "/server/name", "myServer" ),
      pathPropertyPair( "/server/user", "myUser" ),
      pathPropertyPair( "/server/password", "myPassword" ),
      pathPropertyPair( "/server/URL", "myUrl" ),
      pathPropertyPair( "/server/className", "someClass" ) );

    // Get a list of catalogs.
    final List<String> catalogs = olapService.getCatalogs( session );

    assertEquals( 1, catalogs.size() );
    assertEquals( "myServer", catalogs.get( 0 ) );

    verify( repository ).getChildren(
      eq( makeIdObject( olapFolderPath ) ) );

    // Now delete it.
    olapService.removeCatalog( "myServer", session );

    // Check if the repo was modified.
    verify( repository ).deleteFile(
      argThat( isLikeFile( makeFileObject( testServerPath ) ) ),
      anyString() );

    // Now check for non-existent catalogs
    try {
      olapService.removeCatalog( "someName", session );
      fail();
    } catch ( IOlapServiceException e ) {
      assertEquals(
        "MondrianCatalogHelper.ERROR_0015 - Catalog someName not found",
        e.getMessage() );
    }
  }
}
