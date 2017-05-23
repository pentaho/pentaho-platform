/*
 * Copyright 2002 - 2017 Pentaho Corporation.  All rights reserved.
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
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import mondrian.olap.CacheControl;
import mondrian.olap.MondrianServer;
import mondrian.rolap.RolapConnection;
import mondrian.rolap.RolapConnectionProperties;
import mondrian.rolap.agg.AggregationManager;
import mondrian.xmla.XmlaHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olap4j.OlapConnection;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.action.olap.IOlapServiceException;
import org.pentaho.platform.plugin.action.olap.impl.OlapServiceImpl;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.util.messages.LocaleHelper;

public class OlapServiceImplTest {

  IUnifiedRepository repository;
  String mondrianFolderPath;
  String olapFolderPath;
  IPentahoSession session;
  IOlapService olapService;
  private MondrianServer server;
  private XmlaHandler.XmlaExtra mockXmlaExtra;

  /**
   * Default implementation of the hook which grants all access.
   * Override and assign to field 'accessMock' to implement your test
   * security policies.
   */
  class DefaultAccessImpl {
    public boolean hasAccess(
      String path,
      EnumSet<RepositoryFilePermission> perms,
      IPentahoSession session ) {
      return true;
    }
  }

  /**
   * Replace this field with your own custom implementation
   * of the access rules when starting access rights tests.
   */
  DefaultAccessImpl accessMock = new DefaultAccessImpl();

  @Before
  public void setUp() throws Exception {

    repository = org.mockito.Mockito.mock( IUnifiedRepository.class );

    // Stub /etc/mondrian
    mondrianFolderPath =
      ClientRepositoryPaths.getEtcFolderPath()
      + RepositoryFile.SEPARATOR
      + "mondrian";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFolder( repository, mondrianFolderPath );

    // Stub /etc/olap-servers
    olapFolderPath =
      ClientRepositoryPaths.getEtcFolderPath()
      + RepositoryFile.SEPARATOR
      + "olap-servers";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFolder( repository, olapFolderPath );

    // Create a session as admin.
    session = new StandaloneSession( "admin" );

    server = org.mockito.Mockito.mock( MondrianServer.class );

    AggregationManager aggManagerMock = org.mockito.Mockito.mock( AggregationManager.class );
    CacheControl cacheControlMock = org.mockito.Mockito.mock( CacheControl.class );
    org.mockito.Mockito.doReturn( aggManagerMock ).when( server ).getAggregationManager();
    org.mockito.Mockito.doReturn( cacheControlMock ).when( aggManagerMock ).getCacheControl(
      org.mockito.Mockito.any( RolapConnection .class ), org.mockito.Mockito.any( PrintWriter.class ) );

    mockXmlaExtra = org.mockito.Mockito.mock( XmlaHandler.XmlaExtra.class );

    // Create the olap service. Make sure to override hasAccess with the
    // mock version.
    olapService = new OlapServiceImpl( repository, server ) {
      public boolean hasAccess(
        String path,
        EnumSet<RepositoryFilePermission> perms,
        IPentahoSession session ) {
        return accessMock.hasAccess( path, perms, session );
      }

      @Override
      protected XmlaHandler.XmlaExtra getXmlaExtra( final OlapConnection connection ) throws SQLException {
        return mockXmlaExtra;
      }
    };
  }

  @After public void tearDown() throws Exception {
    accessMock = new DefaultAccessImpl();
    repository = null;
    olapService = null;
    session = null;
  }

  private void stubCatalogValues() {
    stubHostedServer();
    stubRemoteServer();
  }


  private void stubRemoteServer() {
    final String testServerPath =
      mondrianFolderPath
        + RepositoryFile.SEPARATOR
        + "myHostedServer";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
            .stubGetChildren( repository, olapFolderPath, "myServer" );
    final String remoteServerPath =
      olapFolderPath
        + RepositoryFile.SEPARATOR
        + "myServer";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFolder( repository, remoteServerPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
            .stubGetChildren( repository, remoteServerPath, "metadata" );
    final String remoteMetadataPath =
      testServerPath
        + RepositoryFile.SEPARATOR
        + "metadata";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFile( repository, remoteMetadataPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetData( repository, remoteMetadataPath + RepositoryFile.SEPARATOR + "myServer", "server",
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                .pathPropertyPair( "/server/name", "myServer" ),
            org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                    .pathPropertyPair( "/server/user", "myUser" ),
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                .pathPropertyPair( "/server/password", "myPassword" ),
            org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                    .pathPropertyPair( "/server/URL", "myUrl" ),
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                .pathPropertyPair( "/server/className", "someClass" ) );
  }

  private void stubHostedServer() {
    stubHostedServers( "myHostedServer" );
  }

  private void stubHostedServers( String... serverNames ) {
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren( repository, mondrianFolderPath, serverNames );
    for ( String serverName : serverNames ) {
      final String testServerPath = mondrianFolderPath + RepositoryFile.SEPARATOR + serverName;
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFolder( repository, testServerPath );
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren( repository, testServerPath, "metadata" );
      final String metadataPath = testServerPath + RepositoryFile.SEPARATOR + "metadata";
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFile( repository, metadataPath );
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
              .stubGetData( repository, metadataPath, "catalog",
                      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                              .pathPropertyPair( "/catalog/definition", "mondrian:/SteelWheels" ),
          org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                  .pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SteelWheels;" ) );
    }
  }

  /**
   * Verifies that we can create locally hosted mondrian instances.
   */
  @Test
  public void testImportHosted() throws Exception {
    final String testFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "test-server";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFileDoesNotExist( repository, testFolderPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubCreateFolder( repository, testFolderPath );
    final String metadataPath = testFolderPath + RepositoryFile.SEPARATOR + "metadata";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubCreateFile( repository, metadataPath );

    final InputStream is =
      new FileInputStream(
        new File( "test-src/solution/security/steelwheels.mondrian.xml" ) );

    olapService.addHostedCatalog(
      "test-server",
      "Provider=mondrian;DataSource=SampleData",
      is,
      true,
      session );

    org.mockito.Mockito.verify( repository ).createFile( org.mockito.Mockito.eq(
            org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject( testFolderPath ) ),
        org.mockito.Mockito.argThat( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.isLikeFile(
                org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeFileObject( metadataPath ) ) ),
            org.mockito.Mockito.argThat(
            org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.hasData(
                    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                            "/catalog/definition", "mondrian:/" + "test-server" ),
                org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                        "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SampleData" ) ) ),
        org.mockito.Mockito.anyString() );

    org.mockito.Mockito.verify( repository ).createFile( org.mockito.Mockito.eq(
            org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject( testFolderPath ) ),
        org.mockito.Mockito.argThat( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.isLikeFile(
                org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeFileObject(
                        testFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
        org.mockito.Mockito.any( IRepositoryFileData.class ), org.mockito.Mockito.anyString() );
  }

  /**
   * Verifies that we can add generic olap4j connections.
   */
  @Test
  public void testImportOlap4j() throws Exception {
    final String testFolderPath = olapFolderPath + RepositoryFile.SEPARATOR + "test-server-2";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubCreateFolder( repository, testFolderPath );

    final String metadataPath = testFolderPath + RepositoryFile.SEPARATOR + "metadata";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubCreateFile( repository, metadataPath );


    olapService.addOlap4jCatalog( "test-server-2", "class-name", "url", "user", "password", new Properties(), true,
        session );

    org.mockito.Mockito.verify( repository ).createFile(
      org.mockito.Mockito.eq( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject(
              testFolderPath ) ),
      org.mockito.Mockito.argThat(
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.isLikeFile(
                org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeFileObject( metadataPath ) ) ),
      org.mockito.Mockito.argThat( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.hasData(
              org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                      "/server/name", "test-server-2" ),
              org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                      "/server/className", "class-name" ),
              org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                      .pathPropertyPair( "/server/URL", "url" ),
              org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                      "/server/user", "user" ), org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                      .pathPropertyPair( "/server/password", "password" ) ) ),
      org.mockito.Mockito.anyString() );
  }

  /**
   * Validates getting a list of remote catalogs.
   */
  @Test
  public void testGetOlap4jCatalogs() throws Exception {
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
            .stubGetChildren( repository, olapFolderPath, "myServer" );

    // Stub /etc/olap-servers/myServer
    final String testServerPath =
      olapFolderPath
      + RepositoryFile.SEPARATOR
      + "myServer";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFolder( repository, testServerPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
            .stubGetChildren( repository, testServerPath, "metadata" );

    // Stub /etc/olap-servers/myServer/metadata
    final String metadataPath =
      testServerPath
      + RepositoryFile.SEPARATOR
      + "metadata";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFile( repository, metadataPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetData(
            repository, metadataPath + RepositoryFile.SEPARATOR + "myServer", "server",
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/server/name", "myServer" ), org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                    .pathPropertyPair( "/server/user", "myUser" ),
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/server/password", "myPassword" ), org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                    .pathPropertyPair( "/server/URL", "myUrl" ),
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/server/className", "someClass" ) );

    // Get a list of catalogs.
    final List<String> catalogs =
      olapService.getCatalogNames( session );

    org.junit.Assert.assertEquals( 1, catalogs.size() );
    org.junit.Assert.assertEquals( "myServer", catalogs.get( 0 ) );

    org.mockito.Mockito.verify( repository ).getChildren( org.mockito.Mockito.eq(
            org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject( olapFolderPath ) ) );

    // Now check for non-existent catalogs
    try {
      olapService.getConnection( "someName", session );
      org.junit.Assert.fail();
    } catch ( IOlapServiceException e ) {
      org.junit.Assert.assertEquals(
        IOlapServiceException.Reason.GENERAL, e.getReason() );
      org.junit.Assert.assertEquals(
        "MondrianCatalogHelper.ERROR_0015 - Catalog someName not found",
        e.getMessage() );
    }
  }

  /**
   * Validates getting a list of hosted catalogs.
   */
  @Test
  public void testGetHostedCatalogs() throws Exception {
    stubHostedServer();

    // Get a list of catalogs.
    final List<String> catalogs =
      olapService.getCatalogNames( session );

    org.junit.Assert.assertEquals( 1, catalogs.size() );
    org.junit.Assert.assertEquals( "myHostedServer", catalogs.get( 0 ) );

    org.mockito.Mockito.verify( repository ).getChildren(
      org.mockito.Mockito.eq( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject(
              mondrianFolderPath ) ) );

    // Now check for non-existent catalogs
    try {
      olapService.getConnection( "someName", session );
      org.junit.Assert.fail();
    } catch ( IOlapServiceException e ) {
      org.junit.Assert.assertEquals(
        IOlapServiceException.Reason.GENERAL, e.getReason() );
      org.junit.Assert.assertEquals(
        "MondrianCatalogHelper.ERROR_0015 - Catalog someName not found",
        e.getMessage() );
    }
  }

  /**
   * Validates getting a list of hosted catalogs. They must come
   * back sorted correctly.
   */
  @Test
  public void testGetAllCatalogs() throws Exception {
    stubCatalogValues();

    // Get a list of catalogs.
    final List<String> catalogs =
      olapService.getCatalogNames( session );

    org.junit.Assert.assertEquals( 2, catalogs.size() );
    org.junit.Assert.assertEquals( "myHostedServer", catalogs.get( 0 ) );
    org.junit.Assert.assertEquals( "myServer", catalogs.get( 1 ) );

    org.mockito.Mockito.verify( repository ).getChildren(
      org.mockito.Mockito.eq( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject(
              mondrianFolderPath ) ) );
    org.mockito.Mockito.verify( repository ).getChildren(
      org.mockito.Mockito.eq( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject(
              olapFolderPath ) ) );
  }

  /**
   * Validates getting a list of remote catalogs.
   */
  @Test
  public void testRemoveOlap4jCatalogs() throws Exception {
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren(
            repository, olapFolderPath, "myServer" );

    // Stub /etc/olap-servers/myServer
    final String testServerPath =
      olapFolderPath
      + RepositoryFile.SEPARATOR
      + "myServer";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFolder( repository, testServerPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren(
            repository, testServerPath, "metadata" );

    // Stub /etc/olap-servers/myServer/metadata
    final String metadataPath =
      testServerPath
      + RepositoryFile.SEPARATOR
      + "metadata";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFile( repository, metadataPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetData(
      repository,
      metadataPath
      + RepositoryFile.SEPARATOR
      + "myServer",
      "server",
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair( "/server/name", "myServer" ),
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair( "/server/user", "myUser" ),
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
              "/server/password", "myPassword" ),
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair( "/server/URL", "myUrl" ),
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
              "/server/className", "someClass" ) );

    // Get a list of catalogs.
    final List<String> catalogs =
      olapService.getCatalogNames( session );

    org.junit.Assert.assertEquals( 1, catalogs.size() );
    org.junit.Assert.assertEquals( "myServer", catalogs.get( 0 ) );

    org.mockito.Mockito.verify( repository ).getChildren(
      org.mockito.Mockito.eq( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject(
              olapFolderPath ) ) );

    // Now delete it.
    olapService.removeCatalog( "myServer", session );

    // Check if the repo was modified.
    org.mockito.Mockito.verify( repository ).deleteFile(
      org.mockito.Mockito.eq( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject(
              testServerPath ) ), org.mockito.Mockito.eq( true ),
            org.mockito.Mockito.anyString( ) );

    // Now check for non-existent catalogs
    try {
      olapService.removeCatalog( "someName", session );
      org.junit.Assert.fail();
    } catch ( IOlapServiceException e ) {
      org.junit.Assert.assertEquals(
        IOlapServiceException.Reason.GENERAL, e.getReason() );
      org.junit.Assert.assertEquals(
        "MondrianCatalogHelper.ERROR_0015 - Catalog someName not found",
        e.getMessage() );
    }
  }

  /**
   * Validates getting a list of hosted catalogs without having access to them.
   */
  @Test
  public void testGetHostedCatalogsNoReadAccess() throws Exception {
    stubHostedServer();

    // Stub the security
    accessMock = new DefaultAccessImpl() {
      public boolean hasAccess(
        String catalogName,
        EnumSet<RepositoryFilePermission> perms,
        IPentahoSession session ) {
        if ( !perms.contains( RepositoryFilePermission.READ ) ) {
          org.junit.Assert.fail();
        }
        if ( catalogName.equals( "myHostedServer" ) ) {
          return false;
        }
        return true;
      }
    };

    // Get a list of catalogs.
    final List<String> catalogs =
      olapService.getCatalogNames( session );

    org.junit.Assert.assertEquals( 0, catalogs.size() );

    org.mockito.Mockito.verify( repository ).getChildren(
      org.mockito.Mockito.eq( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject(
              mondrianFolderPath ) ) );

    // Now try obtaining it anyways.
    try {
      olapService.getConnection( "myHostedServer", session );
      org.junit.Assert.fail();
    } catch ( IOlapServiceException e ) {
      org.junit.Assert.assertEquals(
        IOlapServiceException.Reason.ACCESS_DENIED, e.getReason() );
      org.junit.Assert.assertTrue(
        e.getMessage().contains( "OlapServiceImpl.ERROR_0003" ) );
    }
  }

  /**
   * Validates getting a list of remote catalogs if we don't have access to them.
   */
  @Test
  public void testGetOlap4jCatalogsWithoutAccess() throws Exception {

    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren(
            repository, olapFolderPath, "myServer" );

    // Stub /etc/olap-servers/myServer
    final String testServerPath =
      olapFolderPath
      + RepositoryFile.SEPARATOR
      + "myServer";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFolder( repository, testServerPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren(
            repository, testServerPath, "metadata" );

    // Stub /etc/olap-servers/myServer/metadata
    final String metadataPath =
      testServerPath
      + RepositoryFile.SEPARATOR
      + "metadata";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFile( repository, metadataPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetData(
            repository, metadataPath + RepositoryFile.SEPARATOR + "myServer", "server",
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/server/name", "myServer" ), org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                    .pathPropertyPair( "/server/user", "myUser" ),
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/server/password", "myPassword" ), org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                    .pathPropertyPair( "/server/URL", "myUrl" ),
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/server/className", "someClass" ) );

    // Stub the security
    accessMock = new DefaultAccessImpl() {
      public boolean hasAccess(
        String path,
        EnumSet<RepositoryFilePermission> perms,
        IPentahoSession session ) {
        if ( !perms.contains( RepositoryFilePermission.READ ) ) {
          org.junit.Assert.fail();
        }
        return false;
      }
    };

    // Get a list of catalogs.
    final List<String> catalogs =
      olapService.getCatalogNames( session );

    org.junit.Assert.assertEquals( 0, catalogs.size() );

    org.mockito.Mockito.verify( repository ).getChildren(
      org.mockito.Mockito.eq( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject(
              olapFolderPath ) ) );

    // Now try obtaining it anyways.
    try {
      olapService.getConnection( "myServer", session );
      org.junit.Assert.fail();
    } catch ( IOlapServiceException e ) {
      org.junit.Assert.assertEquals(
        IOlapServiceException.Reason.ACCESS_DENIED, e.getReason() );
      org.junit.Assert.assertTrue(
        e.getMessage().contains( "OlapServiceImpl.ERROR_0003" ) );
    }
  }

  /**
   * Validates getting a list of all catalogs, but we can only access
   * the generic ones.
   */
  @Test
  public void testGetAllCatalogsWithoutAccessToHostedOnes() throws Exception {

    // Stub the hosted server
    stubCatalogValues();

    // Stub the security
    accessMock = new DefaultAccessImpl() {
      public boolean hasAccess(
        String path,
        EnumSet<RepositoryFilePermission> perms,
        IPentahoSession session ) {
        if ( path.equals( "myServer" ) ) {
          return true;
        }
        return false;
      }
    };

    // Get a list of catalogs.
    final List<String> catalogs =
      olapService.getCatalogNames( session );

    org.junit.Assert.assertEquals( 1, catalogs.size() );
    org.junit.Assert.assertEquals( "myServer", catalogs.get( 0 ) );

    org.mockito.Mockito.verify( repository ).getChildren(
      org.mockito.Mockito.eq( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject(
              mondrianFolderPath ) ) );
    org.mockito.Mockito.verify( repository ).getChildren(
      org.mockito.Mockito.eq( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject(
              olapFolderPath ) ) );
  }

  /**
   * Validates getting a list of remote catalogs.
   */
  @Test
  public void testRemoveOlap4jCatalogsWithoutPermission() throws Exception {

    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren(
            repository, olapFolderPath, "myServer" );

    // Stub /etc/olap-servers/myServer
    final String testServerPath =
      olapFolderPath
      + RepositoryFile.SEPARATOR
      + "myServer";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFolder( repository, testServerPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren(
            repository, testServerPath, "metadata" );

    // Stub /etc/olap-servers/myServer/metadata
    final String metadataPath =
      testServerPath
      + RepositoryFile.SEPARATOR
      + "metadata";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFile( repository, metadataPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetData(
      repository,
      metadataPath
      + RepositoryFile.SEPARATOR
      + "myServer",
      "server",
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
              "/server/name", "myServer" ),
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair( "/server/user", "myUser" ),
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
              "/server/password", "myPassword" ),
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair( "/server/URL", "myUrl" ),
      org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
              "/server/className", "someClass" ) );

    // Get a list of catalogs.
    final List<String> catalogs =
      olapService.getCatalogNames( session );

    org.junit.Assert.assertEquals( 1, catalogs.size() );
    org.junit.Assert.assertEquals( "myServer", catalogs.get( 0 ) );

    org.mockito.Mockito.verify( repository ).getChildren(
      org.mockito.Mockito.eq( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject(
              olapFolderPath ) ) );

    // Stub the security
    accessMock = new DefaultAccessImpl() {
      public boolean hasAccess(
        String path,
        EnumSet<RepositoryFilePermission> perms,
        IPentahoSession session ) {
        if ( perms.contains( RepositoryFilePermission.DELETE )
          && path.equals( "myServer" ) ) {
          return false;
        }
        return true;
      }
    };

    // Try to delete it. We expect it to fail.
    try {
      olapService.removeCatalog( "myServer", session );
      org.junit.Assert.fail();
    } catch ( IOlapServiceException e ) {
      org.junit.Assert.assertEquals(
        IOlapServiceException.Reason.ACCESS_DENIED, e.getReason() );
      org.junit.Assert.assertTrue(
        e.getMessage().contains( "OlapServiceImpl.ERROR_0003" ) );
    }

    // Make sure we didn't invoke the delete method.
    org.mockito.Mockito.verify( repository, org.mockito.Mockito.never() )
            .deleteFile( (RepositoryFile) org.mockito.Mockito.anyObject(), org.mockito.Mockito.anyString() );
  }

  /**
   * Verifies that we can create locally hosted mondrian instances.
   */
  @Test
  public void testImportHostedOverwriteFlag() throws Exception {

    final String testFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "myHostedServer";

    // Stub the hosted server
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren(
            repository, mondrianFolderPath, "myHostedServer" );
    final String testServerPath =
      mondrianFolderPath
      + RepositoryFile.SEPARATOR
      + "myHostedServer";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFolder( repository, testServerPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren(
            repository, testServerPath, "metadata" );
    final String metadataPath =
      testServerPath
      + RepositoryFile.SEPARATOR
      + "metadata";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFile( repository, metadataPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFile(
            repository, testFolderPath + RepositoryFile.SEPARATOR + "schema.xml" );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetData(
            repository, metadataPath, "catalog",
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/catalog/definition", "mondrian:/SteelWheels" ),
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SteelWheels;" ) );

    final InputStream is =
      new FileInputStream(
        new File( "test-src/solution/security/steelwheels.mondrian.xml" ) );

    // Try to save it without the overwrite flag. We expect it to fail.
    try {
      olapService.addHostedCatalog(
        "myHostedServer",
        "Provider=mondrian;DataSource=SampleData",
        is,
        false,
        session );
      org.junit.Assert.fail();
    } catch ( IOlapServiceException e ) {
      org.junit.Assert.assertEquals(
        IOlapServiceException.Reason.ALREADY_EXISTS, e.getReason() );
      org.junit.Assert.assertTrue(
        e.getMessage().contains( "OlapServiceImpl.ERROR_0004" ) );
    }

    // Make sure we didn't invoke the update or write methods.
    org.mockito.Mockito.verify( repository, org.mockito.Mockito.never() ).updateFile(
      (RepositoryFile) org.mockito.Mockito.anyObject(),
      (IRepositoryFileData) org.mockito.Mockito.anyObject(),
      org.mockito.Mockito.anyString() );
    org.mockito.Mockito.verify( repository, org.mockito.Mockito.never() ).createFile(
      (RepositoryFile) org.mockito.Mockito.anyObject(),
      (RepositoryFile) org.mockito.Mockito.anyObject(),
      (IRepositoryFileData) org.mockito.Mockito.anyObject(),
      org.mockito.Mockito.anyString() );

    // Now do it again.
    olapService.addHostedCatalog(
      "myHostedServer",
      "Provider=mondrian;DataSource=SampleData",
      is,
      true,
      session );

    org.mockito.Mockito.verify( repository ).updateFile(
      org.mockito.Mockito.argThat( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.isLikeFile(
              org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeFileObject( metadataPath ) ) ),
      org.mockito.Mockito.argThat( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.hasData(
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/catalog/definition", "mondrian:/" + "myHostedServer" ),
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SampleData" )
      ) ), org.mockito.Mockito.anyString() );

    org.mockito.Mockito.verify( repository ).updateFile(
        org.mockito.Mockito.argThat( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.isLikeFile(
                org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeFileObject(
                        testFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
        org.mockito.Mockito.any( IRepositoryFileData.class ), org.mockito.Mockito.anyString() );
  }

  /**
   * Verifies that we can create locally hosted mondrian instances.
   */
  @Test
  public void testImportGenericOverwriteFlag() throws Exception {

    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren(
            repository, olapFolderPath, "myServer" );

    // Stub /etc/olap-servers/myServer
    final String testServerPath =
      olapFolderPath
      + RepositoryFile.SEPARATOR
      + "myServer";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFolder( repository, testServerPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren(
            repository, testServerPath, "metadata" );

    // Stub /etc/olap-servers/myServer/metadata
    final String metadataPath =
      testServerPath
      + RepositoryFile.SEPARATOR
      + "metadata";
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFile( repository, metadataPath );
    org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetData(
            repository, metadataPath + RepositoryFile.SEPARATOR + "myServer", "server",
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/server/name", "myServer" ), org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                    .pathPropertyPair( "/server/user", "myUser" ),
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/server/password", "myPassword" ), org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                    .pathPropertyPair( "/server/URL", "myUrl" ),
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                "/server/className", "someClass" ) );

    // Try to save it without the overwrite flag. We expect it to fail.
    try {
      olapService.addOlap4jCatalog(
        "myServer",
        "class-name",
        "url",
        "user",
        "password",
        new Properties(),
        false,
        session );
      org.junit.Assert.fail();
    } catch ( IOlapServiceException e ) {
      org.junit.Assert.assertEquals(
        IOlapServiceException.Reason.ALREADY_EXISTS, e.getReason() );
      org.junit.Assert.assertTrue(
        e.getMessage().contains( "OlapServiceImpl.ERROR_0004" ) );
    }

    // Make sure we didn't invoke the update or write methods.
    org.mockito.Mockito.verify( repository, org.mockito.Mockito.never() ).updateFile(
      (RepositoryFile) org.mockito.Mockito.anyObject(),
      (IRepositoryFileData) org.mockito.Mockito.anyObject(),
      org.mockito.Mockito.anyString() );
    org.mockito.Mockito.verify( repository, org.mockito.Mockito.never() ).createFile(
            (RepositoryFile) org.mockito.Mockito.anyObject(), (RepositoryFile) org.mockito.Mockito.anyObject(),
        (IRepositoryFileData) org.mockito.Mockito.anyObject(), org.mockito.Mockito.anyString() );

    // Now do it again.
    olapService.addOlap4jCatalog( "myServer", "class-name", "url", "user", "password", new Properties(), true, session );

    org.mockito.Mockito.verify( repository ).updateFile(
      org.mockito.Mockito.argThat(
        org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.isLikeFile(
                org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeFileObject( metadataPath ) ) ),
      org.mockito.Mockito.argThat( org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.hasData(
              org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                      "/server/name", "myServer" ),
              org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                      "/server/className", "class-name" ),
              org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                      "/server/URL", "url" ),
              org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair(
                      "/server/user", "user" ), org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils
                      .pathPropertyPair( "/server/password", "password" ) ) ),
      org.mockito.Mockito.anyString() );
  }

  @Test
  public void testFlushesAllConnections() throws Exception {
    stubHostedServer();
    final Properties properties = new Properties();
    properties.put( RolapConnectionProperties.Locale.name(), getLocale().toString() );
    OlapConnection conn = org.mockito.Mockito.mock( OlapConnection.class );
    org.mockito.Mockito.when( server.getConnection( "Pentaho", "myHostedServer", null, properties ) )
            .thenReturn( conn );
    olapService.flushAll( session );
    org.mockito.Mockito.verify( mockXmlaExtra ).flushSchemaCache( conn );
  }

  @Test
  public void testFlushStopsAfterFirstHosted() throws Exception {
    stubHostedServers( "myHostedServer", "myHostedServer2" );

    final Properties properties = new Properties();
    properties.put( RolapConnectionProperties.Locale.name(), getLocale().toString() );
    OlapConnection conn = org.mockito.Mockito.mock( OlapConnection.class );
    org.mockito.Mockito.when( server.getConnection( "Pentaho", "myHostedServer", null, properties ) )
            .thenReturn( conn );
    olapService.flushAll( session );
    org.mockito.Mockito.verify( mockXmlaExtra, org.mockito.Mockito.times( 1 ) ).flushSchemaCache( conn );
  }

  @Test
  public void testFlushProceedsOnException() throws Exception {
    stubHostedServer();
    final Properties properties = new Properties();
    properties.put(
      RolapConnectionProperties.Locale.name(),
      getLocale().toString() );
    OlapConnection conn = org.mockito.Mockito.mock( OlapConnection.class );
    org.mockito.Mockito.when( server.getConnection( "Pentaho", "myHostedServer", null, properties ) )
            .thenReturn( conn );
    org.mockito.Mockito.when( conn.isWrapperFor( org.mockito.Mockito.any( Class.class ) ) ).thenReturn( true );
    final RolapConnection rolapConn = org.mockito.Mockito.mock( RolapConnection.class );
    org.mockito.Mockito.when( conn.unwrap( org.mockito.Mockito.any( Class.class ) ) ).thenReturn( rolapConn );
    org.mockito.Mockito.when( rolapConn.getCacheControl( org.mockito.Mockito.any( PrintWriter.class ) ) ).thenThrow(
      new RuntimeException( "something happend" ) );
    try {
      olapService.flushAll( session );
    } catch ( IOlapServiceException e ) {
      org.junit.Assert.fail( "Exception shouldn't have made it this far." );
    }
  }

  private static Locale getLocale() {
    final Locale locale = LocaleHelper.getLocale();
    if ( locale != null ) {
      return locale;
    } else {
      return Locale.getDefault();
    }
  }
}
