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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.olap;

import mondrian.olap.CacheControl;
import mondrian.olap.MondrianServer;
import mondrian.rolap.RolapConnection;
import mondrian.rolap.RolapConnectionProperties;
import mondrian.rolap.RolapSchema;
import mondrian.rolap.agg.AggregationManager;
import mondrian.xmla.XmlaHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.hasData;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.isLikeFile;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeFileObject;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.makeIdObject;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubCreateFile;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubCreateFolder;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetData;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFile;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFileDoesNotExist;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFolder;

@RunWith ( MockitoJUnitRunner.class )
public class OlapServiceImplTest {

  String mondrianFolderPath;
  String olapFolderPath;
  IPentahoSession session;
  IOlapService olapService;

  @Mock IUnifiedRepository repository;
  @Mock private MondrianServer server;
  @Mock private XmlaHandler.XmlaExtra mockXmlaExtra;
  @Mock AggregationManager aggManager;
  @Mock CacheControl cacheControl;

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

    doReturn( aggManager ).when( server ).getAggregationManager();
    doReturn( cacheControl ).when( aggManager ).getCacheControl(
      nullable( RolapConnection .class ), nullable( PrintWriter.class ) );

    // Create the olap service. Make sure to override hasAccess with the
    // mock version.
    olapService = spy( new OlapServiceImpl( repository, server ) {
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
    } );
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
    stubGetData( repository, remoteMetadataPath + RepositoryFile.SEPARATOR + "myServer", "server",
        pathPropertyPair( "/server/name", "myServer" ), pathPropertyPair( "/server/user", "myUser" ),
        pathPropertyPair( "/server/password", "myPassword" ), pathPropertyPair( "/server/URL", "myUrl" ),
        pathPropertyPair( "/server/className", "someClass" ) );
  }

  private void stubHostedServer() {
    stubHostedServers( "myHostedServer" );
  }

  private void stubHostedServers( String... serverNames ) {
    stubGetChildren( repository, mondrianFolderPath, serverNames );
    for ( String serverName : serverNames ) {
      final String testServerPath = mondrianFolderPath + RepositoryFile.SEPARATOR + serverName;
      stubGetFolder( repository, testServerPath );
      stubGetChildren( repository, testServerPath, "metadata" );
      final String metadataPath = testServerPath + RepositoryFile.SEPARATOR + "metadata";
      stubGetFile( repository, metadataPath );
      stubGetData( repository, metadataPath, "catalog", pathPropertyPair( "/catalog/definition", "mondrian:/SteelWheels" ),
          pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SteelWheels;" ) );
    }
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

    final InputStream is = this.getClass().getResourceAsStream( "/solution/security/steelwheels.mondrian.xml" );


    olapService.addHostedCatalog(
      "test-server",
      "Provider=mondrian;DataSource=SampleData",
      is,
      true,
      session );

    verify( repository ).createFile( eq( makeIdObject( testFolderPath ) ),
        argThat( isLikeFile( makeFileObject( metadataPath ) ) ), argThat(
            hasData( pathPropertyPair( "/catalog/definition", "mondrian:/" + "test-server" ),
                pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SampleData" ) ) ),
        nullable( String.class ) );

    verify( repository ).createFile( eq( makeIdObject( testFolderPath ) ),
        argThat( isLikeFile( makeFileObject( testFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
        nullable( IRepositoryFileData.class ), nullable( String.class ) );
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


    olapService.addOlap4jCatalog( "test-server-2", "class-name", "url", "user", "password", new Properties(), true,
        session );

    verify( repository ).createFile(
      eq( makeIdObject( testFolderPath ) ),
      argThat(
        isLikeFile( makeFileObject( metadataPath ) ) ),
      argThat( hasData( pathPropertyPair( "/server/name", "test-server-2" ),
              pathPropertyPair( "/server/className", "class-name" ), pathPropertyPair( "/server/URL", "url" ),
              pathPropertyPair( "/server/user", "user" ), pathPropertyPair( "/server/password", "password" ) ) ),
      nullable( String.class ) );
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
    stubGetData( repository, metadataPath + RepositoryFile.SEPARATOR + "myServer", "server",
        pathPropertyPair( "/server/name", "myServer" ), pathPropertyPair( "/server/user", "myUser" ),
        pathPropertyPair( "/server/password", "myPassword" ), pathPropertyPair( "/server/URL", "myUrl" ),
        pathPropertyPair( "/server/className", "someClass" ) );

    // Get a list of catalogs.
    final List<String> catalogs =
      olapService.getCatalogNames( session );

    assertEquals( 1, catalogs.size() );
    assertEquals( "myServer", catalogs.get( 0 ) );

    verify( repository ).getChildren( eq( makeIdObject( olapFolderPath ) ) );

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
      nullable( String.class ) );

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
    stubGetData( repository, metadataPath + RepositoryFile.SEPARATOR + "myServer", "server",
        pathPropertyPair( "/server/name", "myServer" ), pathPropertyPair( "/server/user", "myUser" ),
        pathPropertyPair( "/server/password", "myPassword" ), pathPropertyPair( "/server/URL", "myUrl" ),
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
    verify( repository, never() ).deleteFile( any(), nullable( String.class ) );
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
    stubGetData( repository, metadataPath, "catalog",
        pathPropertyPair( "/catalog/definition", "mondrian:/SteelWheels" ),
        pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SteelWheels;" ) );

    final InputStream is = this.getClass().getResourceAsStream( "/solution/security/steelwheels.mondrian.xml" );


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
      any(),
      any(),
      nullable( String.class ) );
    verify( repository, never() ).createFile(
      any(),
      any(),
      any(),
      nullable( String.class ) );

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
      ) ), nullable( String.class ) );

    verify( repository ).updateFile(
        argThat( isLikeFile( makeFileObject( testFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
        nullable( IRepositoryFileData.class ), nullable( String.class ) );
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
    stubGetData( repository, metadataPath + RepositoryFile.SEPARATOR + "myServer", "server",
        pathPropertyPair( "/server/name", "myServer" ), pathPropertyPair( "/server/user", "myUser" ),
        pathPropertyPair( "/server/password", "myPassword" ), pathPropertyPair( "/server/URL", "myUrl" ),
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
      any(),
      any(),
      nullable( String.class ) );
    verify( repository, never() ).createFile( any(), any(), any(), nullable( String.class ) );

    // Now do it again.
    olapService.addOlap4jCatalog( "myServer", "class-name", "url", "user", "password", new Properties(), true, session );

    verify( repository ).updateFile(
      argThat(
        isLikeFile( makeFileObject( metadataPath ) ) ),
      argThat( hasData( pathPropertyPair( "/server/name", "myServer" ),
              pathPropertyPair( "/server/className", "class-name" ), pathPropertyPair( "/server/URL", "url" ),
              pathPropertyPair( "/server/user", "user" ), pathPropertyPair( "/server/password", "password" ) ) ),
      nullable( String.class ) );
  }

  @Test
  public void flushSingleSchemaCache() throws Exception {
    OlapConnection connection = mock( OlapConnection.class );
    doReturn( connection ).when( olapService ).getConnection( "schemaX", session );

    RolapConnection rc = mock( RolapConnection.class );
    doReturn( rc ).when( connection ).unwrap( RolapConnection.class );
    doReturn( cacheControl ).when( rc ).getCacheControl( nullable( PrintWriter.class ) );

    RolapSchema schema = mock( RolapSchema.class );
    doReturn( schema ).when( rc ).getSchema();

    olapService.flush( session, "schemaX" );
    verify( cacheControl, times( 1 ) ).flushSchema( schema );
  }

  @Test
  public void flushSingleSchemaCacheThrowsException() throws Exception {
    try {
      olapService.flush( session, "schemaX" );
      fail();
    } catch ( IOlapServiceException e ) {
      verify( cacheControl, times( 0 ) ).flushSchema( nullable( RolapSchema.class ) );
      assertEquals( "MondrianCatalogHelper.ERROR_0019 - Failed to flush schema schemaX", e.getMessage() );
    }
  }

  @Test
  public void flushAllFlushesSchemaCache() {
    stubHostedServers( "myHostedServer", "myHostedServer2" );
    olapService.flushAll( session );
    verify( cacheControl, times( 1 ) ).flushSchemaCache();
  }

  @Test
  public void testFlushProceedsOnException() throws Exception {
    stubHostedServer();
    final Properties properties = new Properties();
    properties.put(
      RolapConnectionProperties.Locale.name(),
      getLocale().toString() );
    try {
      olapService.flushAll( session );
    } catch ( IOlapServiceException e ) {
      fail( "Exception shouldn't have made it this far." );
    }
  }

  private static Locale getLocale() {
    return LocaleHelper.getLocale();
  }
}
