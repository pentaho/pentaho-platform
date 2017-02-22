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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils;
import org.pentaho.platform.util.Base64PasswordService;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.plugin.UserRoleMapperIT.TestUserRoleListService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    Mockito.when( olapService.getConnection( Mockito.any( String.class ), Mockito.any( IPentahoSession.class ) ) )
      .thenReturn( olapConn );
    Mockito.when( rolapConn.getSchema() ).thenReturn( mondrianSchema );
    Mockito.when( rolapConn.getCacheControl( Mockito.any( PrintWriter.class ) ) )
      .thenReturn( mondrianCacheControl );
    Mockito.when( olapConn.unwrap( Connection.class ) )
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
    Mockito.reset( repo );
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

    helper = Mockito.spy( helper );

    IPentahoSession session = Mockito.mock( IPentahoSession.class );
    Mockito.doNothing().when( helper ).init( session );

    helper.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, false, null, session );
  }

  @Test( expected = MondrianException.class )
  public void testAddCatalogWithException() {
    initMondrianCatalogsCache();
    MondrianCatalogHelper helperSpy = Mockito.spy( helper );
    Mockito.doThrow( new MondrianException() ).when( helperSpy ).reInit( Mockito.any( IPentahoSession.class ) );

    IPentahoSession session = Mockito.mock( IPentahoSession.class );
    Mockito.doNothing().when( helperSpy ).init( session );

    MondrianCatalog cat = createTestCatalog();
    MondrianCatalogRepositoryHelper repositoryHelper = Mockito.mock( MondrianCatalogRepositoryHelper.class );
    Mockito.doReturn( repositoryHelper ).when( helperSpy ).getMondrianCatalogRepositoryHelper();
    try {
      helperSpy.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, true, null, session );
    } catch ( MondrianException e ) {
      // verifying the repository rolled back and the cache reinitialized
      Mockito.verify( repositoryHelper, Mockito.times( 1 ) ).deleteHostedCatalog( Mockito.anyString() );
      Mockito.verify( helperSpy, Mockito.times( 2 ) ).reInit( Mockito.any( IPentahoSession.class ) );
    }
    helperSpy.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, true, null, session );
  }

  @Test
  public void testAddCatalog() throws Exception {
    final String mondrianFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
    UnifiedRepositoryTestUtils.stubGetFolder( repo, mondrianFolderPath );
    UnifiedRepositoryTestUtils.stubGetChildren( repo, mondrianFolderPath ); // return no children
    final String steelWheelsFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + CATALOG_NAME;
    UnifiedRepositoryTestUtils.stubGetFileDoesNotExist( repo, steelWheelsFolderPath );
    UnifiedRepositoryTestUtils.stubCreateFolder( repo, steelWheelsFolderPath );
    final String metadataPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "metadata";
    UnifiedRepositoryTestUtils.stubCreateFile( repo, metadataPath );

    final String olapFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "olap-servers";
    UnifiedRepositoryTestUtils.stubGetFolder( repo, olapFolderPath );
    UnifiedRepositoryTestUtils.stubGetChildren( repo, olapFolderPath ); // return no children

    IPentahoSession session = new StandaloneSession( "admin" );

    MondrianCatalog cat = createTestCatalog();
    File file = new File( "test-src/solution/test/charts/steelwheels.mondrian.xml" );
    String mondrianSchema = IOUtils.toString( new FileInputStream( file ) );
    session.setAttribute( "MONDRIAN_SCHEMA_XML_CONTENT", mondrianSchema );

    MondrianCatalogHelper helperSpy = Mockito.spy( helper );
    IAclNodeHelper aclNodeHelper = Mockito.mock( IAclNodeHelper.class );
    Mockito.doNothing().when( aclNodeHelper ).setAclFor( Mockito.any( RepositoryFile.class ), Mockito.any( RepositoryFileAcl.class ) );
    Mockito.doReturn( aclNodeHelper ).when( helperSpy ).getAclHelper();
    Mockito.doReturn( true ).when( helperSpy ).catalogExists( Mockito.any( MondrianCatalog.class ), Mockito.eq( session ) );

    helperSpy.addCatalog( cat, true, session );

    Mockito.verify( repo ).createFile(
      Mockito.eq( UnifiedRepositoryTestUtils.makeIdObject( steelWheelsFolderPath ) ),
      Mockito.argThat( UnifiedRepositoryTestUtils.isLikeFile( UnifiedRepositoryTestUtils.makeFileObject( metadataPath ) ) ),
      Mockito.argThat( UnifiedRepositoryTestUtils.hasData( UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/definition", "mondrian:/" + cat.getName() ), UnifiedRepositoryTestUtils.pathPropertyPair(
            "/catalog/datasourceInfo", cat.getDataSourceInfo() ) ) ), Mockito.anyString()
    );

    Mockito.verify( repo ).createFile( Mockito.eq( UnifiedRepositoryTestUtils.makeIdObject( steelWheelsFolderPath ) ),
      Mockito.argThat( UnifiedRepositoryTestUtils.isLikeFile( UnifiedRepositoryTestUtils.makeFileObject( steelWheelsFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
      Mockito.any( IRepositoryFileData.class ), Mockito.anyString() );


    // cache should be cleared for this schema only
    Mockito.verify( olapService, Mockito.times( 1 ) ).getConnection( CATALOG_NAME, session );
    Mockito.verify( mondrianCacheControl, Mockito.times( 1 ) ).flushSchema( this.mondrianSchema );
  }

  @Test
  public void addCatalog_WithAcl() throws Exception {
    initMondrianCatalogsCache();

    MondrianCatalogHelper helperSpy = Mockito.spy( helper );

    IPentahoSession session = Mockito.mock( IPentahoSession.class );
    Mockito.doNothing().when( helperSpy ).init( session );

    Mockito.doReturn( Collections.<MondrianCatalog>emptyList() ).when( helperSpy ).getCatalogs( session );
    Mockito.doReturn( null ).when( helperSpy ).makeSchema( Mockito.anyString() );

    MondrianCatalog cat = createTestCatalog();
    RepositoryFileAcl acl = Mockito.mock( RepositoryFileAcl.class );

    IAclNodeHelper aclHelper = Mockito.mock( IAclNodeHelper.class );
    Mockito.doNothing().when( aclHelper ).setAclFor( Mockito.any( RepositoryFile.class ), Mockito.eq( acl ) );
    Mockito.doReturn( aclHelper ).when( helperSpy ).getAclHelper();
    Mockito.doReturn( null ).when( helperSpy ).makeSchema( CATALOG_NAME );
    Mockito.doReturn( true ).when( helperSpy ).catalogExists( Mockito.any( MondrianCatalog.class ), Mockito.eq( session ) );

    MondrianCatalogRepositoryHelper repositoryHelper = Mockito.mock( MondrianCatalogRepositoryHelper.class );
    Mockito.doReturn( repositoryHelper ).when( helperSpy ).getMondrianCatalogRepositoryHelper();

    helperSpy.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, true, acl, session );
    Mockito.verify( aclHelper, Mockito.times( 1 ) ).setAclFor( Mockito.any( RepositoryFile.class ), Mockito.eq( acl ) );

    Mockito.doNothing().when( aclHelper ).setAclFor( Mockito.any( RepositoryFile.class ), Mockito.any( RepositoryFileAcl.class ) );
    helperSpy.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, true, null, session );
    Mockito.verify( aclHelper, Mockito.times( 2 ) ).setAclFor( Mockito.any( RepositoryFile.class ), Mockito.any( RepositoryFileAcl.class ) );
  }

  @Test
  public void testImportSchemaWithSpaceDoesFlush() throws Exception {
    String TMP_FILE_PATH = File.separatorChar + "test" + File.separatorChar + "analysis" + File.separatorChar;
    String uploadDir = PentahoSystem.getApplicationContext().getSolutionPath( TMP_FILE_PATH );
    File mondrianFile = new File( uploadDir + File.separatorChar + "SuperBacon.mondrian.xml" );

    final String mondrianFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
    final String sampleDataFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "super bacon";
    final String metadataPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "metadata";

    UnifiedRepositoryTestUtils.stubGetFolder( repo, mondrianFolderPath );
    UnifiedRepositoryTestUtils.stubGetChildren( repo, mondrianFolderPath ); // return no children

    UnifiedRepositoryTestUtils.stubGetFileDoesNotExist( repo, sampleDataFolderPath );
    UnifiedRepositoryTestUtils.stubCreateFolder( repo, sampleDataFolderPath );

    UnifiedRepositoryTestUtils.stubCreateFile( repo, metadataPath );

    final String olapFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "olap-servers";
    UnifiedRepositoryTestUtils.stubGetFolder( repo, olapFolderPath );
    UnifiedRepositoryTestUtils.stubGetChildren( repo, olapFolderPath ); // return no children

    helper.importSchema( mondrianFile, "super bacon", "" );

    // cache should be cleared for this schema only
    Mockito.verify( olapService, Mockito.times( 1 ) ).getConnection( "super bacon",  null );
    Mockito.verify( mondrianCacheControl, Mockito.times( 1 ) ).flushSchema( mondrianSchema );
  }

  @Test
  public void testImportSchema() throws Exception {
    String TMP_FILE_PATH = File.separatorChar + "test" + File.separatorChar + "analysis" + File.separatorChar;
    String uploadDir = PentahoSystem.getApplicationContext().getSolutionPath( TMP_FILE_PATH );
    File mondrianFile = new File( uploadDir + File.separatorChar + "SampleData.mondrian.xml" );

    final String mondrianFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
    final String sampleDataFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SampleData";
    final String metadataPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "metadata";

    UnifiedRepositoryTestUtils.stubGetFolder( repo, mondrianFolderPath );
    UnifiedRepositoryTestUtils.stubGetChildren( repo, mondrianFolderPath ); // return no children

    UnifiedRepositoryTestUtils.stubGetFileDoesNotExist( repo, sampleDataFolderPath );
    UnifiedRepositoryTestUtils.stubCreateFolder( repo, sampleDataFolderPath );

    UnifiedRepositoryTestUtils.stubCreateFile( repo, metadataPath );

    final String olapFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "olap-servers";
    UnifiedRepositoryTestUtils.stubGetFolder( repo, olapFolderPath );
    UnifiedRepositoryTestUtils.stubGetChildren( repo, olapFolderPath ); // return no children

    helper.importSchema( mondrianFile, "SampleData", "" );

    Mockito.verify( repo ).createFile( Mockito.eq( UnifiedRepositoryTestUtils.makeIdObject( sampleDataFolderPath ) ),
      Mockito.argThat( UnifiedRepositoryTestUtils.isLikeFile( UnifiedRepositoryTestUtils.makeFileObject( metadataPath ) ) ),
      Mockito.argThat( UnifiedRepositoryTestUtils.hasData( UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/definition", "mondrian:/" + "SampleData" ),
                            UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SampleData" ) ) ),
      Mockito.anyString() );
    Mockito.verify( repo ).createFile( Mockito.eq( UnifiedRepositoryTestUtils.makeIdObject( sampleDataFolderPath ) ),
      Mockito.argThat( UnifiedRepositoryTestUtils.isLikeFile( UnifiedRepositoryTestUtils.makeFileObject( sampleDataFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
      Mockito.any( IRepositoryFileData.class ), Mockito.anyString() );

    // cache should be cleared for this schema only
    Mockito.verify( olapService, Mockito.times( 1 ) ).getConnection( "SampleData",  null );
    Mockito.verify( mondrianCacheControl, Mockito.times( 1 ) ).flushSchema( mondrianSchema );
  }

  @Test
  public void testListCatalog() throws Exception {
    File file1 = new File( "test-src/solution/test/charts/steelwheels.mondrian.xml" );
    String mondrianSchema1 = IOUtils.toString( new FileInputStream( file1 ) );
    File file2 = new File( "test-src/solution/samples/reporting/SampleData.mondrian.xml" );
    String mondrianSchema2 = IOUtils.toString( new FileInputStream( file2 ) );

    final String mondrianFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
    UnifiedRepositoryTestUtils.stubGetFolder( repo, mondrianFolderPath );
    UnifiedRepositoryTestUtils.stubGetChildren( repo, mondrianFolderPath, "SampleData/", "SteelWheels/" ); // return two child folders

    final String olapFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "olap-servers";
    UnifiedRepositoryTestUtils.stubGetFolder( repo, olapFolderPath );
    UnifiedRepositoryTestUtils.stubGetChildren( repo, olapFolderPath ); // return no children

    final String sampleDataFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SampleData";
    final String sampleDataMetadataPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "metadata";
    final String sampleDataSchemaPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "schema.xml";
    UnifiedRepositoryTestUtils.stubGetFile( repo, sampleDataMetadataPath );
    UnifiedRepositoryTestUtils.stubGetData( repo, sampleDataMetadataPath, "catalog", UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/definition",
        "mondrian:/SampleData" ), UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/datasourceInfo",
        "Provider=mondrian;DataSource=SampleData;" ) );
    UnifiedRepositoryTestUtils.stubGetFile( repo, sampleDataSchemaPath );
    UnifiedRepositoryTestUtils.stubGetData( repo, sampleDataSchemaPath, mondrianSchema2 );

    final String steelWheelsFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SteelWheels";
    final String steelWheelsMetadataPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "metadata";
    final String steelWheelsSchemaPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "schema.xml";
    final String steelWheelsAnnotationsPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "annotations.xml";
    UnifiedRepositoryTestUtils.stubGetFile( repo, steelWheelsMetadataPath );
    UnifiedRepositoryTestUtils.stubGetData( repo, steelWheelsMetadataPath, "catalog", UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/definition",
        "mondrian:/SteelWheels" ), UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/datasourceInfo",
        "Provider=mondrian;DataSource=SteelWheels;" ) );
    UnifiedRepositoryTestUtils.stubGetFile( repo, steelWheelsSchemaPath );
    UnifiedRepositoryTestUtils.stubGetData( repo, steelWheelsSchemaPath, mondrianSchema1 );
    UnifiedRepositoryTestUtils.stubGetFile( repo, steelWheelsAnnotationsPath );
    UnifiedRepositoryTestUtils.stubGetData( repo, steelWheelsAnnotationsPath, "<annotations></annotations>" );

    IPentahoSession session = new StandaloneSession( "admin" );

    helper = Mockito.spy( helper );

    IAclNodeHelper aclHelper = Mockito.mock( IAclNodeHelper.class );
    Mockito.when( aclHelper.canAccess( Mockito.any( RepositoryFile.class ), Mockito.any( EnumSet.class ) ) ).thenReturn( true );
    Mockito.doReturn( aclHelper ).when( helper ).getAclHelper();

    MondrianCatalog[] testCatalogs = new MondrianCatalog[] { Mockito.spy( createTestCatalog() ), Mockito.spy( createTestCatalog() ) };
    Mockito.doReturn( true ).when( testCatalogs[ 0 ] ).isJndi();
    Mockito.doReturn( false ).when( testCatalogs[ 1 ] ).isJndi();
    Mockito.doReturn( Arrays.asList( testCatalogs ) ).when( helper ).getCatalogs( session );
    Answer<String> answer = new Answer<String>() {
      @Override public String answer( final InvocationOnMock invocation ) throws Throwable {
        try {
          return (String) invocation.callRealMethod();
        } catch ( Exception throwable ) {
          if ( throwable.getCause() instanceof ClassCastException ) {
            Assert.fail( "should not get ClassCastException here " );
          }
          throw throwable;
        }
      }
    };
    Mockito.doAnswer( answer ).when( helper ).docAtUrlToString( Mockito.any( String.class ), Mockito.any( IPentahoSession.class ) );

    List<MondrianCatalog> cats = helper.listCatalogs( session, true );
    Assert.assertEquals( 1, cats.size() );

    Mockito.verify( mondrianCacheControl, Mockito.never() ).flushSchema( mondrianSchema );
  }

  @Test
  public void testRemoveCatalog() throws Exception {
    File file1 = new File( "test-src/solution/test/charts/steelwheels.mondrian.xml" );
    String mondrianSchema1 = IOUtils.toString( new FileInputStream( file1 ) );

    final String mondrianFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
    UnifiedRepositoryTestUtils.stubGetFolder( repo, mondrianFolderPath );
    UnifiedRepositoryTestUtils.stubGetChildren( repo, mondrianFolderPath, "SteelWheels/" );

    final String steelWheelsFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SteelWheels";
    final String steelWheelsMetadataPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "metadata";
    final String steelWheelsSchemaPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "schema.xml";
    UnifiedRepositoryTestUtils.stubGetFile( repo, steelWheelsMetadataPath );
    UnifiedRepositoryTestUtils.stubGetData( repo, steelWheelsMetadataPath, "catalog",
            UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/definition", "mondrian:/SteelWheels" ),
            UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SteelWheels;" ) );
    UnifiedRepositoryTestUtils.stubGetFile( repo, steelWheelsSchemaPath );
    UnifiedRepositoryTestUtils.stubGetData( repo, steelWheelsSchemaPath, mondrianSchema1 );

    UnifiedRepositoryTestUtils.stubGetFolder( repo, steelWheelsFolderPath );

    IPentahoSession session = new StandaloneSession( "admin" );

    helper = Mockito.spy( helper );
    IAclNodeHelper aclHelper = Mockito.mock( IAclNodeHelper.class );
    Mockito.when( aclHelper.canAccess( Mockito.any( RepositoryFile.class ), Mockito.any( EnumSet.class ) ) ).thenReturn( true );
    Mockito.doReturn( aclHelper ).when( helper ).getAclHelper();

    MondrianCatalogRepositoryHelper repositoryHelper = Mockito.mock( MondrianCatalogRepositoryHelper.class );
    Mockito.doReturn( repositoryHelper ).when( helper ).getMondrianCatalogRepositoryHelper();

    helper.removeCatalog( "mondrian:/SteelWheels", session );

    Mockito.verify( repo ).deleteFile( Mockito.eq( UnifiedRepositoryTestUtils.makeIdObject( steelWheelsFolderPath ) ), Mockito.eq( true ), Mockito.anyString() );

    // cache should be cleared for this schema only
    Mockito.verify( olapService, Mockito.times( 1 ) ).getConnection( CATALOG_NAME, session );
    Mockito.verify( mondrianCacheControl, Mockito.times( 1 ) ).flushSchema( this.mondrianSchema );
  }

  @Test
  public void removeCatalog_WithAcl() throws Exception {
    IPentahoSession session = Mockito.mock( IPentahoSession.class );

    helper = Mockito.spy( helper );
    Mockito.doReturn( createTestCatalog() ).when( helper ).getCatalog( Mockito.eq( CATALOG_NAME ), Mockito.eq( session ) );
    Mockito.doNothing().when( helper ).reInit( Mockito.eq( session ) );

    MondrianCatalogRepositoryHelper repositoryHelper = Mockito.mock( MondrianCatalogRepositoryHelper.class );
    Mockito.doReturn( repositoryHelper ).when( helper ).getMondrianCatalogRepositoryHelper();

    IAclNodeHelper aclHelper = Mockito.mock( IAclNodeHelper.class );
    Mockito.when( aclHelper.canAccess( Mockito.any( RepositoryFile.class ), Mockito.any( EnumSet.class ) ) ).thenReturn( true );
    Mockito.doReturn( aclHelper ).when( helper ).getAclHelper();

    RepositoryFile file = Mockito.mock( RepositoryFile.class );
    Mockito.when( file.getId() ).thenReturn( "1" );
    Mockito.when( repo.getFile( "/etc/mondrian/" + CATALOG_NAME ) ).thenReturn( file );

    helper.removeCatalog( CATALOG_NAME, session );

    Mockito.verify( aclHelper ).removeAclFor( Mockito.any( RepositoryFile.class ) );
  }

  @Test( expected = MondrianCatalogServiceException.class )
  public void removeCatalog_WhenProhibited() throws Exception {
    IPentahoSession session = Mockito.mock( IPentahoSession.class );

    helper = Mockito.spy( helper );
    Mockito.doReturn( createTestCatalog() ).when( helper ).getCatalog( Mockito.eq( CATALOG_NAME ), Mockito.eq( session ) );
    Mockito.doNothing().when( helper ).reInit( Mockito.eq( session ) );

    MondrianCatalogRepositoryHelper repositoryHelper = Mockito.mock( MondrianCatalogRepositoryHelper.class );
    Mockito.doReturn( repositoryHelper ).when( helper ).getMondrianCatalogRepositoryHelper();

    IAclNodeHelper aclHelper = Mockito.mock( IAclNodeHelper.class );
    Mockito.when( aclHelper.canAccess( Mockito.any( RepositoryFile.class ), Mockito.any( EnumSet.class ) ) ).thenReturn( false );
    Mockito.doReturn( aclHelper ).when( helper ).getAclHelper();

    helper.removeCatalog( CATALOG_NAME, session );
  }

  @Test
  public void testDspApplied_WhenDataSourceInfoContainsDynamicSchemaProcessorParameter() throws Exception {
    final String replaceTemplate = "REPLACE_TEMPLATE";
    final String
        DATA_SOURCE_INFO_WITH_DSP =
        "DynamicSchemaProcessor=org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelperIT$DynamicDSPTest";
    final String SCHEMA_MOCK = "Test Schema Mock " + replaceTemplate;

    MondrianCatalogHelper helperSpy = Mockito.spy( helper );
    IPentahoSession session = new StandaloneSession( "admin" );
    String schema = helperSpy.applyDSP( session, DATA_SOURCE_INFO_WITH_DSP, SCHEMA_MOCK );
    Assert.assertNotNull( schema );
    Assert.assertTrue( schema.contains( "REPLACE_TOKEN" ) );
    Mockito.verify( helperSpy, Mockito.never() ).docAtUrlToString( Mockito.anyString(), Mockito.any( IPentahoSession.class ) );

  }

  @Test
  public void testNoDspApplied_WhenNoDynamicSchemaProcessorParameterInDataSourceInfo() throws Exception {
    final String replaceTemplate = "REPLACE_TEMPLATE";
    final String DATA_SOURCE_INFO_WITHOUT_DSP = "DataSource=TestDataSource";
    final String SCHEMA_MOCK = "Test Schema Mock " + replaceTemplate;

    MondrianCatalogHelper helperSpy = Mockito.spy( helper );
    Mockito.doReturn( SCHEMA_MOCK ).when( helperSpy ).docAtUrlToString( Mockito.anyString(), Mockito.any( IPentahoSession.class ) );

    IPentahoSession session = new StandaloneSession( "admin" );
    String schema = helperSpy.applyDSP( session, DATA_SOURCE_INFO_WITHOUT_DSP, SCHEMA_MOCK );
    Assert.assertNotNull( schema );
    Mockito.verify( helperSpy ).docAtUrlToString( Mockito.anyString(), Mockito.any( IPentahoSession.class ) );
    Assert.assertFalse( schema.contains( "REPLACE_TOKEN" ) );

  }

  @Test
  public void testGetCatalog() throws Exception {
    String catalogName = "SteelWheels";

    MondrianCatalog cat = Mockito.mock( MondrianCatalog.class );
    Mockito.doReturn( catalogName ).when( cat ).getName();
    IPentahoSession session = Mockito.mock( IPentahoSession.class );

    MondrianCatalogHelper helperSpy = Mockito.spy( helper );
    Mockito.doReturn( true ).when( helperSpy ).hasAccess( Mockito.eq( cat ), Mockito.any( RepositoryFilePermission.class ) );
    Mockito.doNothing().when( helperSpy ).init( session );
    Mockito.doReturn( cat ).when( helperSpy ).getCatalogFromCache( Mockito.anyString(), Mockito.eq( session ) );

    Assert.assertEquals( helperSpy.getCatalog( catalogName, session ).getName(), catalogName );
    Mockito.verify( helperSpy ).hasAccess( Mockito.eq( cat ), Mockito.any( RepositoryFilePermission.class ) );

    Mockito.doReturn( false ).when( helperSpy ).hasAccess( Mockito.eq( cat ), Mockito.any( RepositoryFilePermission.class ) );
    Assert.assertNull( helperSpy.getCatalog( catalogName, session ) );
  }

  public static class DynamicDSPTest implements DynamicSchemaProcessor {

    @Override
    public String processSchema( String s, PropertyList propertylist ) throws Exception {
      return s.replaceAll( "REPLACE_TEMPLATE", "REPLACE_TOKEN" );
    }

  }

  @Test
  public void testGenerateInMemoryDatasourcesXml_NullEtcMondrianFolder() throws Exception {
    MondrianCatalogHelper helperMock = Mockito.mock( MondrianCatalogHelper.class );
    IUnifiedRepository unifiedRepositoryMock = Mockito.mock( IUnifiedRepository.class );

    Mockito.doReturn( null ).when( unifiedRepositoryMock ).getFile( Mockito.any( String.class ) );
    Mockito.doCallRealMethod().when( helperMock ).generateInMemoryDatasourcesXml( Mockito.any( IUnifiedRepository.class ) );

    String result = helperMock.generateInMemoryDatasourcesXml( unifiedRepositoryMock );
    Assert.assertNull( result, null );
  }
}
