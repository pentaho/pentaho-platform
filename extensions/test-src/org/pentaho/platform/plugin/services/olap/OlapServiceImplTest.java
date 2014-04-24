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
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import mondrian.olap.MondrianServer;
import mondrian.rolap.RolapConnection;
import mondrian.rolap.RolapConnectionProperties;
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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.*;

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

  @Before public void setUp() throws Exception {

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

    server = mock( MondrianServer.class );

    mockXmlaExtra = mock( XmlaHandler.XmlaExtra.class );

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
  }

  private void stubHostedServer() {
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
  }

  /**
   * Verifies that we can create locally hosted mondrian instances.
   */
  @Test
  public void testImportHosted() throws Exception {
    final String testFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "test-server";
    stubGetFileDoesNotExist( repository, testFolderPath );
    stubCreateFolder( repository, testFolderPath );
    final String metadataPath = testFolderPath + RepositoryFile.SEPARATOR + "metadata";
    stubCreateFile( repository, metadataPath );

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
  @Test
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
  @Test
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
    final List<String> catalogs =
      olapService.getCatalogNames( session );

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
        IOlapServiceException.Reason.GENERAL, e.getReason() );
      assertEquals(
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
        IOlapServiceException.Reason.GENERAL, e.getReason() );
      assertEquals(
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
  @Test
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
    final List<String> catalogs =
      olapService.getCatalogNames( session );

    assertEquals( 1, catalogs.size() );
    assertEquals( "myServer", catalogs.get( 0 ) );

    verify( repository ).getChildren(
      eq( makeIdObject( olapFolderPath ) ) );

    // Now delete it.
    olapService.removeCatalog( "myServer", session );

    // Check if the repo was modified.
    verify( repository ).deleteFile(
      eq( makeIdObject( testServerPath ) ), eq( true ),
      anyString( ) );

    // Now check for non-existent catalogs
    try {
      olapService.removeCatalog( "someName", session );
      fail();
    } catch ( IOlapServiceException e ) {
      assertEquals(
        IOlapServiceException.Reason.GENERAL, e.getReason() );
      assertEquals(
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
          fail();
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

    assertEquals( 0, catalogs.size() );

    verify( repository ).getChildren(
      eq( makeIdObject( mondrianFolderPath ) ) );

    // Now try obtaining it anyways.
    try {
      olapService.getConnection( "myHostedServer", session );
      fail();
    } catch ( IOlapServiceException e ) {
      assertEquals(
        IOlapServiceException.Reason.ACCESS_DENIED, e.getReason() );
      assertTrue(
        e.getMessage().contains( "OlapServiceImpl.ERROR_0003" ) );
    }
  }

  /**
   * Validates getting a list of remote catalogs if we don't have access to them.
   */
  @Test
  public void testGetOlap4jCatalogsWithoutAccess() throws Exception {

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

    // Stub the security
    accessMock = new DefaultAccessImpl() {
      public boolean hasAccess(
        String path,
        EnumSet<RepositoryFilePermission> perms,
        IPentahoSession session ) {
        if ( !perms.contains( RepositoryFilePermission.READ ) ) {
          fail();
        }
        return false;
      }
    };

    // Get a list of catalogs.
    final List<String> catalogs =
      olapService.getCatalogNames( session );

    assertEquals( 0, catalogs.size() );

    verify( repository ).getChildren(
      eq( makeIdObject( olapFolderPath ) ) );

    // Now try obtaining it anyways.
    try {
      olapService.getConnection( "myServer", session );
      fail();
    } catch ( IOlapServiceException e ) {
      assertEquals(
        IOlapServiceException.Reason.ACCESS_DENIED, e.getReason() );
      assertTrue(
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

    assertEquals( 1, catalogs.size() );
    assertEquals( "myServer", catalogs.get( 0 ) );

    verify( repository ).getChildren(
      eq( makeIdObject( mondrianFolderPath ) ) );
    verify( repository ).getChildren(
      eq( makeIdObject( olapFolderPath ) ) );
  }

  /**
   * Validates getting a list of remote catalogs.
   */
  @Test
  public void testRemoveOlap4jCatalogsWithoutPermission() throws Exception {

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
    final List<String> catalogs =
      olapService.getCatalogNames( session );

    assertEquals( 1, catalogs.size() );
    assertEquals( "myServer", catalogs.get( 0 ) );

    verify( repository ).getChildren(
      eq( makeIdObject( olapFolderPath ) ) );

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
      fail();
    } catch ( IOlapServiceException e ) {
      assertEquals(
        IOlapServiceException.Reason.ACCESS_DENIED, e.getReason() );
      assertTrue(
        e.getMessage().contains( "OlapServiceImpl.ERROR_0003" ) );
    }

    // Make sure we didn't invoke the delete method.
    verify( repository, never() ).deleteFile(
      (RepositoryFile) anyObject(),
      anyString() );
  }

  /**
   * Verifies that we can create locally hosted mondrian instances.
   */
  @Test
  public void testImportHostedOverwriteFlag() throws Exception {

    final String testFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "myHostedServer";

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
    stubGetFile( repository, testFolderPath + RepositoryFile.SEPARATOR + "schema.xml" );
    stubGetData(
      repository,
      metadataPath,
      "catalog",
      pathPropertyPair( "/catalog/definition", "mondrian:/SteelWheels" ),
      pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SteelWheels;" ) );

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
      fail();
    } catch ( IOlapServiceException e ) {
      assertEquals(
        IOlapServiceException.Reason.ALREADY_EXISTS, e.getReason() );
      assertTrue(
        e.getMessage().contains( "OlapServiceImpl.ERROR_0004" ) );
    }

    // Make sure we didn't invoke the update or write methods.
    verify( repository, never() ).updateFile(
      (RepositoryFile) anyObject(),
      (IRepositoryFileData) anyObject(),
      anyString() );
    verify( repository, never() ).createFile(
      (RepositoryFile) anyObject(),
      (RepositoryFile) anyObject(),
      (IRepositoryFileData) anyObject(),
      anyString() );

    // Now do it again.
    olapService.addHostedCatalog(
      "myHostedServer",
      "Provider=mondrian;DataSource=SampleData",
      is,
      true,
      session );

    verify( repository ).updateFile(
      argThat( isLikeFile( makeFileObject( metadataPath ) ) ),
      argThat( hasData(
        pathPropertyPair( "/catalog/definition", "mondrian:/" + "myHostedServer" ),
        pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SampleData" )
      ) ), anyString() );

    verify( repository ).updateFile(
      argThat( isLikeFile( makeFileObject( testFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
      any( IRepositoryFileData.class ),
      anyString() );
  }

  /**
   * Verifies that we can create locally hosted mondrian instances.
   */
  @Test
  public void testImportGenericOverwriteFlag() throws Exception {

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
      fail();
    } catch ( IOlapServiceException e ) {
      assertEquals(
        IOlapServiceException.Reason.ALREADY_EXISTS, e.getReason() );
      assertTrue(
        e.getMessage().contains( "OlapServiceImpl.ERROR_0004" ) );
    }

    // Make sure we didn't invoke the update or write methods.
    verify( repository, never() ).updateFile(
      (RepositoryFile) anyObject(),
      (IRepositoryFileData) anyObject(),
      anyString() );
    verify( repository, never() ).createFile(
      (RepositoryFile) anyObject(),
      (RepositoryFile) anyObject(),
      (IRepositoryFileData) anyObject(),
      anyString() );

    // Now do it again.
    olapService.addOlap4jCatalog(
      "myServer",
      "class-name",
      "url",
      "user",
      "password",
      new Properties(),
      true,
      session );

    verify( repository ).updateFile(
      argThat(
        isLikeFile( makeFileObject( metadataPath ) ) ),
      argThat(
        hasData(
          pathPropertyPair( "/server/name", "myServer" ),
          pathPropertyPair( "/server/className", "class-name" ),
          pathPropertyPair( "/server/URL", "url" ),
          pathPropertyPair( "/server/user", "user" ),
          pathPropertyPair( "/server/password", "password" ) ) ),
      anyString() );
  }

  @Test
  public void testFlushesAllConnections() throws Exception {
    stubHostedServer();
    final Properties properties = new Properties();
    properties.put(
      RolapConnectionProperties.Locale.name(),
      getLocale().toString() );
    OlapConnection conn = mock( OlapConnection.class );
    when( server.getConnection( "Pentaho", "myHostedServer", null, properties ) ).thenReturn( conn );
    olapService.flushAll( session );
    verify( mockXmlaExtra ).flushSchemaCache( conn );
  }

  @Test
  public void testFlushProceedsOnException() throws Exception {
    stubHostedServer();
    final Properties properties = new Properties();
    properties.put(
      RolapConnectionProperties.Locale.name(),
      getLocale().toString() );
    OlapConnection conn = mock( OlapConnection.class );
    when( server.getConnection( "Pentaho", "myHostedServer", null, properties ) ).thenReturn( conn );
    when( conn.isWrapperFor( any( Class.class ) ) ).thenReturn( true );
    final RolapConnection rolapConn = mock( RolapConnection.class );
    when( conn.unwrap( any( Class.class ) ) ).thenReturn( rolapConn );
    when( rolapConn.getCacheControl( any( PrintWriter.class ) ) ).thenThrow(
      new RuntimeException( "something happend" ) );
    try {
      olapService.flushAll( session );
    } catch ( IOlapServiceException e ) {
      fail( "Exception shouldn't have made it this far." );
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
