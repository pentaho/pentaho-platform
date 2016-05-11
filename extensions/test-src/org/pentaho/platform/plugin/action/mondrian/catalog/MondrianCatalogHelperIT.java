/*!
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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.action.mondrian.catalog;

import mondrian.olap.CacheControl;
import mondrian.olap.Connection;
import mondrian.olap.MondrianException;
import mondrian.olap.Schema;
import mondrian.olap.Util.PropertyList;
import mondrian.spi.DynamicSchemaProcessor;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.olap4j.OlapConnection;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.MondrianSchemaAnnotator;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.services.cache.CacheManager;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.util.Base64PasswordService;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.plugin.UserRoleMapperIT.TestUserRoleListService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.*;

@SuppressWarnings( "nls" )
public class MondrianCatalogHelperIT {

  private static final String CATALOG_NAME = "SteelWheels";

  // ~ Instance fields
  // =================================================================================================

  private static MicroPlatform booter;
  private ICacheManager cacheMgr;

  private MondrianCatalogHelper helper;

  @Mock private IUnifiedRepository repo;

  @Mock private  IOlapService olapService;

  @Mock private CacheControl mondrianCacheControl;

  @Mock private Schema mondrianSchema;

  @Mock private OlapConnection olapConn;

  @Mock private Connection rolapConn;


  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks( this );

    when( olapService.getConnection( any( String.class ), any( IPentahoSession.class ) ) )
      .thenReturn( olapConn );
    when( rolapConn.getSchema() ).thenReturn( mondrianSchema );
    when( rolapConn.getCacheControl( any( PrintWriter.class ) ) )
      .thenReturn( mondrianCacheControl );
    when( olapConn.unwrap( Connection.class ) )
      .thenReturn( rolapConn );

    booter = new MicroPlatform( "test-src/solution" );
    booter.define( IPasswordService.class, Base64PasswordService.class, Scope.GLOBAL );
    booter.define( IDatabaseConnection.class, DatabaseConnection.class, Scope.GLOBAL );
    booter.define( IDatabaseDialectService.class, DatabaseDialectService.class, Scope.GLOBAL );
    booter.define( IAclAwareMondrianCatalogService.class, MondrianCatalogHelper.class, Scope.GLOBAL );
    booter.define( ICacheManager.class, CacheManager.class, Scope.GLOBAL );
    booter.define( IUserRoleListService.class, TestUserRoleListService.class, Scope.GLOBAL );
    booter.defineInstance( IUnifiedRepository.class, repo );
    booter.defineInstance( IOlapService.class, olapService );
    booter.defineInstance(
        "inlineModeling",
        new MondrianSchemaAnnotator() {
          @Override
          public InputStream getInputStream(
              final InputStream schemaInputStream, final InputStream annotationsInputStream ) {
            return schemaInputStream;
          }
      } );
    booter.setSettingsProvider( new SystemSettings() );
    booter.start();

    cacheMgr = PentahoSystem.getCacheManager( null );
    helper = (MondrianCatalogHelper) PentahoSystem.get( IAclAwareMondrianCatalogService.class );
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
    reset( repo );
    cacheMgr.clearRegionCache( MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION );
    booter.stop();
  }

  private void initMondrianCatalogsCache() {
    initMondrianCatalogsCache( new HashMap<String, MondrianCatalog>(  ) );
  }

  private void initMondrianCatalogsCache( Map<String, MondrianCatalog> map ) {
    cacheMgr.addCacheRegion( MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION );
    cacheMgr.putInRegionCache( MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION, LocaleHelper.getLocale().toString(), map );
  }


  private MondrianCatalog createTestCatalog() {
    MondrianSchema schema = new MondrianSchema( CATALOG_NAME, null );
    return  new MondrianCatalog( CATALOG_NAME, "Provider=mondrian;DataSource=SampleData;",
        "solution:test/charts/steelwheels.mondrian.xml", schema );
  }

  @Test( expected = MondrianCatalogServiceException.class )
  public void addCatalog_WhenAlreadyExists() throws Exception {
    MondrianCatalog cat = createTestCatalog();
    initMondrianCatalogsCache( Collections.singletonMap( CATALOG_NAME, cat ) );

    helper = spy( helper );

    IPentahoSession session = mock( IPentahoSession.class );
    doNothing().when( helper ).init( session );

    helper.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, false, null, session );
  }

  @Test( expected = MondrianException.class )
  public void testAddCatalogWithException() {
    initMondrianCatalogsCache();
    MondrianCatalogHelper helperSpy = spy( helper );
    doThrow( new MondrianException() ).when( helperSpy ).reInit( any( IPentahoSession.class ) );

    IPentahoSession session = mock( IPentahoSession.class );
    doNothing().when( helperSpy ).init( session );

    MondrianCatalog cat = createTestCatalog();
    MondrianCatalogRepositoryHelper repositoryHelper = mock( MondrianCatalogRepositoryHelper.class );
    doReturn( repositoryHelper ).when( helperSpy ).getMondrianCatalogRepositoryHelper();
    try {
      helperSpy.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, true, null, session );
    } catch ( MondrianException e ) {
      // verifying the repository rolled back and the cache reinitialized
      verify( repositoryHelper, times( 1 ) ).deleteHostedCatalog( anyString() );
      verify( helperSpy, times( 2 ) ).reInit( any( IPentahoSession.class ) );
    }
    helperSpy.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, true, null, session );
  }

  @Test
  public void testAddCatalog() throws Exception {
    final String mondrianFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
    stubGetFolder( repo, mondrianFolderPath );
    stubGetChildren( repo, mondrianFolderPath ); // return no children
    final String steelWheelsFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + CATALOG_NAME;
    stubGetFileDoesNotExist( repo, steelWheelsFolderPath );
    stubCreateFolder( repo, steelWheelsFolderPath );
    final String metadataPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "metadata";
    stubCreateFile( repo, metadataPath );

    final String olapFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "olap-servers";
    stubGetFolder( repo, olapFolderPath );
    stubGetChildren( repo, olapFolderPath ); // return no children

    IPentahoSession session = new StandaloneSession( "admin" );

    MondrianCatalog cat = createTestCatalog();
    File file = new File( "test-src/solution/test/charts/steelwheels.mondrian.xml" );
    String mondrianSchema = IOUtils.toString( new FileInputStream( file ) );
    session.setAttribute( "MONDRIAN_SCHEMA_XML_CONTENT", mondrianSchema );

    MondrianCatalogHelper helperSpy = spy( helper );
    IAclNodeHelper aclNodeHelper = mock( IAclNodeHelper.class );
    doNothing().when( aclNodeHelper ).setAclFor( any( RepositoryFile.class ), any( RepositoryFileAcl.class ) );
    doReturn( aclNodeHelper ).when( helperSpy ).getAclHelper();
    doReturn( true ).when( helperSpy ).catalogExists( any( MondrianCatalog.class ), eq( session ) );

    helperSpy.addCatalog( cat, true, session );

    verify( repo ).createFile(
        eq( makeIdObject( steelWheelsFolderPath ) ),
        argThat( isLikeFile( makeFileObject( metadataPath ) ) ),
        argThat( hasData( pathPropertyPair( "/catalog/definition", "mondrian:/" + cat.getName() ), pathPropertyPair(
            "/catalog/datasourceInfo", cat.getDataSourceInfo() ) ) ), anyString()
    );

    verify( repo ).createFile( eq( makeIdObject( steelWheelsFolderPath ) ),
        argThat( isLikeFile( makeFileObject( steelWheelsFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
        any( IRepositoryFileData.class ), anyString() );


    // cache should be cleared for this schema only
    verify( olapService, times( 1 ) ).getConnection( CATALOG_NAME, session );
    verify( mondrianCacheControl, times( 1 ) ).flushSchema( this.mondrianSchema );
  }

  @Test
  public void addCatalog_WithAcl() throws Exception {
    initMondrianCatalogsCache();

    MondrianCatalogHelper helperSpy = spy( helper );

    IPentahoSession session = mock( IPentahoSession.class );
    doNothing().when( helperSpy ).init( session );

    doReturn( Collections.<MondrianCatalog>emptyList() ).when( helperSpy ).getCatalogs( session );
    doReturn( null ).when( helperSpy ).makeSchema( anyString() );

    MondrianCatalog cat = createTestCatalog();
    RepositoryFileAcl acl = mock( RepositoryFileAcl.class );

    IAclNodeHelper aclHelper = mock( IAclNodeHelper.class );
    doNothing().when( aclHelper ).setAclFor( any( RepositoryFile.class ), eq( acl ) );
    doReturn( aclHelper ).when( helperSpy ).getAclHelper();
    doReturn( null ).when( helperSpy ).makeSchema( CATALOG_NAME );
    doReturn( true ).when( helperSpy ).catalogExists( any( MondrianCatalog.class ), eq( session ) );

    MondrianCatalogRepositoryHelper repositoryHelper = mock( MondrianCatalogRepositoryHelper.class );
    doReturn( repositoryHelper ).when( helperSpy ).getMondrianCatalogRepositoryHelper();

    helperSpy.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, true, acl, session );
    verify( aclHelper, times( 1 ) ).setAclFor( any( RepositoryFile.class ), eq( acl ) );

    doNothing().when( aclHelper ).setAclFor( any( RepositoryFile.class ), any( RepositoryFileAcl.class ) );
    helperSpy.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, true, null, session );
    verify( aclHelper, times( 2 ) ).setAclFor( any( RepositoryFile.class ), any( RepositoryFileAcl.class ) );
  }

  @Test
  public void testImportSchemaWithSpaceDoesFlush() throws Exception {
    String TMP_FILE_PATH = File.separatorChar + "test" + File.separatorChar + "analysis" + File.separatorChar;
    String uploadDir = PentahoSystem.getApplicationContext().getSolutionPath( TMP_FILE_PATH );
    File mondrianFile = new File( uploadDir + File.separatorChar + "SuperBacon.mondrian.xml" );

    final String mondrianFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
    final String sampleDataFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "super bacon";
    final String metadataPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "metadata";

    stubGetFolder( repo, mondrianFolderPath );
    stubGetChildren( repo, mondrianFolderPath ); // return no children

    stubGetFileDoesNotExist( repo, sampleDataFolderPath );
    stubCreateFolder( repo, sampleDataFolderPath );

    stubCreateFile( repo, metadataPath );

    final String olapFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "olap-servers";
    stubGetFolder( repo, olapFolderPath );
    stubGetChildren( repo, olapFolderPath ); // return no children

    helper.importSchema( mondrianFile, "super bacon", "" );

    // cache should be cleared for this schema only
    verify( olapService, times( 1 ) ).getConnection( "super bacon",  null );
    verify( mondrianCacheControl, times( 1 ) ).flushSchema( mondrianSchema );
  }

  @Test
  public void testImportSchema() throws Exception {
    String TMP_FILE_PATH = File.separatorChar + "test" + File.separatorChar + "analysis" + File.separatorChar;
    String uploadDir = PentahoSystem.getApplicationContext().getSolutionPath( TMP_FILE_PATH );
    File mondrianFile = new File( uploadDir + File.separatorChar + "SampleData.mondrian.xml" );

    final String mondrianFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
    final String sampleDataFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SampleData";
    final String metadataPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "metadata";

    stubGetFolder( repo, mondrianFolderPath );
    stubGetChildren( repo, mondrianFolderPath ); // return no children

    stubGetFileDoesNotExist( repo, sampleDataFolderPath );
    stubCreateFolder( repo, sampleDataFolderPath );

    stubCreateFile( repo, metadataPath );

    final String olapFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "olap-servers";
    stubGetFolder( repo, olapFolderPath );
    stubGetChildren( repo, olapFolderPath ); // return no children

    helper.importSchema( mondrianFile, "SampleData", "" );

    verify( repo ).createFile( eq( makeIdObject( sampleDataFolderPath ) ),
        argThat( isLikeFile( makeFileObject( metadataPath ) ) ), argThat(
            hasData( pathPropertyPair( "/catalog/definition", "mondrian:/" + "SampleData" ),
                pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SampleData" ) ) ),
        anyString() );
    verify( repo ).createFile( eq( makeIdObject( sampleDataFolderPath ) ),
        argThat( isLikeFile( makeFileObject( sampleDataFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
        any( IRepositoryFileData.class ), anyString() );

    // cache should be cleared for this schema only
    verify( olapService, times( 1 ) ).getConnection( "SampleData",  null );
    verify( mondrianCacheControl, times( 1 ) ).flushSchema( mondrianSchema );
  }

  @Test
  public void testListCatalog() throws Exception {
    File file1 = new File( "test-src/solution/test/charts/steelwheels.mondrian.xml" );
    String mondrianSchema1 = IOUtils.toString( new FileInputStream( file1 ) );
    File file2 = new File( "test-src/solution/samples/reporting/SampleData.mondrian.xml" );
    String mondrianSchema2 = IOUtils.toString( new FileInputStream( file2 ) );

    final String mondrianFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
    stubGetFolder( repo, mondrianFolderPath );
    stubGetChildren( repo, mondrianFolderPath, "SampleData/", "SteelWheels/" ); // return two child folders

    final String olapFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "olap-servers";
    stubGetFolder( repo, olapFolderPath );
    stubGetChildren( repo, olapFolderPath ); // return no children

    final String sampleDataFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SampleData";
    final String sampleDataMetadataPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "metadata";
    final String sampleDataSchemaPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "schema.xml";
    stubGetFile( repo, sampleDataMetadataPath );
    stubGetData( repo, sampleDataMetadataPath, "catalog", pathPropertyPair( "/catalog/definition",
        "mondrian:/SampleData" ), pathPropertyPair( "/catalog/datasourceInfo",
        "Provider=mondrian;DataSource=SampleData;" ) );
    stubGetFile( repo, sampleDataSchemaPath );
    stubGetData( repo, sampleDataSchemaPath, mondrianSchema2 );

    final String steelWheelsFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SteelWheels";
    final String steelWheelsMetadataPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "metadata";
    final String steelWheelsSchemaPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "schema.xml";
    final String steelWheelsAnnotationsPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "annotations.xml";
    stubGetFile( repo, steelWheelsMetadataPath );
    stubGetData( repo, steelWheelsMetadataPath, "catalog", pathPropertyPair( "/catalog/definition",
        "mondrian:/SteelWheels" ), pathPropertyPair( "/catalog/datasourceInfo",
        "Provider=mondrian;DataSource=SteelWheels;" ) );
    stubGetFile( repo, steelWheelsSchemaPath );
    stubGetData( repo, steelWheelsSchemaPath, mondrianSchema1 );
    stubGetFile( repo, steelWheelsAnnotationsPath );
    stubGetData( repo, steelWheelsAnnotationsPath, "<annotations></annotations>" );

    IPentahoSession session = new StandaloneSession( "admin" );

    helper = spy( helper );

    IAclNodeHelper aclHelper = mock( IAclNodeHelper.class );
    when( aclHelper.canAccess( any( RepositoryFile.class ), any( EnumSet.class ) ) ).thenReturn( true );
    doReturn( aclHelper ).when( helper ).getAclHelper();

    MondrianCatalog[] testCatalogs = new MondrianCatalog[] { spy( createTestCatalog() ), spy( createTestCatalog() ) };
    doReturn( true ).when( testCatalogs[ 0 ] ).isJndi();
    doReturn( false ).when( testCatalogs[ 1 ] ).isJndi();
    doReturn( asList( testCatalogs ) ).when( helper ).getCatalogs( session );
    Answer<String> answer = new Answer<String>() {
      @Override public String answer( final InvocationOnMock invocation ) throws Throwable {
        try {
          return (String) invocation.callRealMethod();
        } catch ( Exception throwable ) {
          if ( throwable.getCause() instanceof ClassCastException ) {
            fail( "should not get ClassCastException here " );
          }
          throw throwable;
        }
      }
    };
    doAnswer( answer ).when( helper ).docAtUrlToString( any( String.class ), any( IPentahoSession.class ) );

    List<MondrianCatalog> cats = helper.listCatalogs( session, true );
    assertEquals( 1, cats.size() );

    verify( mondrianCacheControl, never() ).flushSchema( mondrianSchema );
  }

  @Test
  public void testRemoveCatalog() throws Exception {
    File file1 = new File( "test-src/solution/test/charts/steelwheels.mondrian.xml" );
    String mondrianSchema1 = IOUtils.toString( new FileInputStream( file1 ) );

    final String mondrianFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
    stubGetFolder( repo, mondrianFolderPath );
    stubGetChildren( repo, mondrianFolderPath, "SteelWheels/" );

    final String steelWheelsFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SteelWheels";
    final String steelWheelsMetadataPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "metadata";
    final String steelWheelsSchemaPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "schema.xml";
    stubGetFile( repo, steelWheelsMetadataPath );
    stubGetData( repo, steelWheelsMetadataPath, "catalog",
        pathPropertyPair( "/catalog/definition", "mondrian:/SteelWheels" ),
        pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SteelWheels;" ) );
    stubGetFile( repo, steelWheelsSchemaPath );
    stubGetData( repo, steelWheelsSchemaPath, mondrianSchema1 );

    stubGetFolder( repo, steelWheelsFolderPath );

    IPentahoSession session = new StandaloneSession( "admin" );

    helper = spy( helper );
    IAclNodeHelper aclHelper = mock( IAclNodeHelper.class );
    when( aclHelper.canAccess( any( RepositoryFile.class ), any( EnumSet.class ) ) ).thenReturn( true );
    doReturn( aclHelper ).when( helper ).getAclHelper();

    MondrianCatalogRepositoryHelper repositoryHelper = mock( MondrianCatalogRepositoryHelper.class );
    doReturn( repositoryHelper ).when( helper ).getMondrianCatalogRepositoryHelper();

    helper.removeCatalog( "mondrian:/SteelWheels", session );

    verify( repo ).deleteFile( eq( makeIdObject( steelWheelsFolderPath ) ), eq( true ), anyString() );

    // cache should be cleared for this schema only
    verify( olapService, times( 1 ) ).getConnection( CATALOG_NAME, session );
    verify( mondrianCacheControl, times( 1 ) ).flushSchema( this.mondrianSchema );
  }

  @Test
  public void removeCatalog_WithAcl() throws Exception {
    IPentahoSession session = mock( IPentahoSession.class );

    helper = spy( helper );
    doReturn( createTestCatalog() ).when( helper ).getCatalog( eq( CATALOG_NAME ), eq( session ) );
    doNothing().when( helper ).reInit( eq( session ) );

    MondrianCatalogRepositoryHelper repositoryHelper = mock( MondrianCatalogRepositoryHelper.class );
    doReturn( repositoryHelper ).when( helper ).getMondrianCatalogRepositoryHelper();

    IAclNodeHelper aclHelper = mock( IAclNodeHelper.class );
    when( aclHelper.canAccess( any( RepositoryFile.class ), any( EnumSet.class ) ) ).thenReturn( true );
    doReturn( aclHelper ).when( helper ).getAclHelper();

    RepositoryFile file = mock( RepositoryFile.class );
    when( file.getId() ).thenReturn( "1" );
    when( repo.getFile( "/etc/mondrian/" + CATALOG_NAME ) ).thenReturn( file );

    helper.removeCatalog( CATALOG_NAME, session );

    verify( aclHelper ).removeAclFor( any( RepositoryFile.class ) );
  }

  @Test( expected = MondrianCatalogServiceException.class )
  public void removeCatalog_WhenProhibited() throws Exception {
    IPentahoSession session = mock( IPentahoSession.class );

    helper = spy( helper );
    doReturn( createTestCatalog() ).when( helper ).getCatalog( eq( CATALOG_NAME ), eq( session ) );
    doNothing().when( helper ).reInit( eq( session ) );

    MondrianCatalogRepositoryHelper repositoryHelper = mock( MondrianCatalogRepositoryHelper.class );
    doReturn( repositoryHelper ).when( helper ).getMondrianCatalogRepositoryHelper();

    IAclNodeHelper aclHelper = mock( IAclNodeHelper.class );
    when( aclHelper.canAccess( any( RepositoryFile.class ), any( EnumSet.class ) ) ).thenReturn( false );
    doReturn( aclHelper ).when( helper ).getAclHelper();

    helper.removeCatalog( CATALOG_NAME, session );
  }

  @Test
  public void testDspApplied_WhenDataSourceInfoContainsDynamicSchemaProcessorParameter() throws Exception {
    final String replaceTemplate = "REPLACE_TEMPLATE";
    final String
        DATA_SOURCE_INFO_WITH_DSP =
        "DynamicSchemaProcessor=org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelperIT$DynamicDSPTest";
    final String SCHEMA_MOCK = "Test Schema Mock " + replaceTemplate;

    MondrianCatalogHelper helperSpy = spy( helper );
    IPentahoSession session = new StandaloneSession( "admin" );
    String schema = helperSpy.applyDSP( session, DATA_SOURCE_INFO_WITH_DSP, SCHEMA_MOCK );
    assertNotNull( schema );
    assertTrue( schema.contains( "REPLACE_TOKEN" ) );
    verify( helperSpy, never() ).docAtUrlToString( anyString(), any( IPentahoSession.class ) );

  }

  @Test
  public void testNoDspApplied_WhenNoDynamicSchemaProcessorParameterInDataSourceInfo() throws Exception {
    final String replaceTemplate = "REPLACE_TEMPLATE";
    final String DATA_SOURCE_INFO_WITHOUT_DSP = "DataSource=TestDataSource";
    final String SCHEMA_MOCK = "Test Schema Mock " + replaceTemplate;

    MondrianCatalogHelper helperSpy = spy( helper );
    doReturn( SCHEMA_MOCK ).when( helperSpy ).docAtUrlToString( anyString(), any( IPentahoSession.class ) );

    IPentahoSession session = new StandaloneSession( "admin" );
    String schema = helperSpy.applyDSP( session, DATA_SOURCE_INFO_WITHOUT_DSP, SCHEMA_MOCK );
    assertNotNull( schema );
    verify( helperSpy ).docAtUrlToString( anyString(), any( IPentahoSession.class ) );
    assertFalse( schema.contains( "REPLACE_TOKEN" ) );

  }

  @Test
  public void testGetCatalog() throws Exception {
    String catalogName = "SteelWheels";

    MondrianCatalog cat = mock( MondrianCatalog.class );
    doReturn( catalogName ).when( cat ).getName();
    IPentahoSession session = mock( IPentahoSession.class );

    MondrianCatalogHelper helperSpy = spy( helper );
    doReturn( true ).when( helperSpy ).hasAccess( eq( cat ), any( RepositoryFilePermission.class ) );
    doNothing().when( helperSpy ).init( session );
    doReturn( cat ).when( helperSpy ).getCatalogFromCache( anyString(), eq( session ) );

    assertEquals( helperSpy.getCatalog( catalogName, session ).getName(), catalogName );
    verify( helperSpy ).hasAccess( eq( cat ), any( RepositoryFilePermission.class ) );

    doReturn( false ).when( helperSpy ).hasAccess( eq( cat ), any( RepositoryFilePermission.class ) );
    assertNull( helperSpy.getCatalog( catalogName, session ) );
  }

  public static class DynamicDSPTest implements DynamicSchemaProcessor {

    @Override
    public String processSchema( String s, PropertyList propertylist ) throws Exception {
      return s.replaceAll( "REPLACE_TEMPLATE", "REPLACE_TOKEN" );
    }

  }

  @Test
  public void testGenerateInMemoryDatasourcesXml_NullEtcMondrianFolder() throws Exception {
    MondrianCatalogHelper helperMock = mock( MondrianCatalogHelper.class );
    IUnifiedRepository unifiedRepositoryMock = mock( IUnifiedRepository.class );

    doReturn( null ).when( unifiedRepositoryMock ).getFile( any( String.class ) );
    doCallRealMethod().when( helperMock ).generateInMemoryDatasourcesXml( any( IUnifiedRepository.class ) );

    String result = helperMock.generateInMemoryDatasourcesXml( unifiedRepositoryMock );
    assertNull( result, null );
  }
}
