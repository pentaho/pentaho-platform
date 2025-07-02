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

package org.pentaho.platform.plugin.action.mondrian.catalog;

import mondrian.i18n.LocalizingDynamicSchemaProcessor;
import mondrian.olap.CacheControl;
import mondrian.olap.Connection;
import mondrian.olap.Schema;
import mondrian.olap.Util.PropertyList;
import mondrian.spi.DynamicSchemaProcessor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.olap4j.OlapConnection;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.plugin.UserRoleMapperIT;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;


/**
 * This class tests the MondrianCatalogHelper integrated at a (Micro)Platform environment.
 */
@SuppressWarnings( "nls" )
public class MondrianCatalogHelperIT {

  private static final String CATALOG_NAME = "SteelWheels";
  private static final String mondrianFolderPath =
    ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";


  // ~ Instance fields
  // =================================================================================================

  /**
   * Catalogs repository.
   * SolutionRepositoryVfsFileObject (see
   * https://github.com/pentaho/pentaho-platform/blob/535817c1a4dac371e7d5f53cba4af8aac3aa2a1a/repository/src/main
   * /java/org/pentaho/platform/repository/solution/filebased/SolutionRepositoryVfsFileObject.java#L54)
   * uses the catalog as a static instance. As a consequence, we need to have the catalog defined statically and
   * reset it before each test setup.
   */
  static IUnifiedRepository repo;

  // Micro platform used to support the tests/code execution. Is initialized and loaded at test setup phase
  private static MicroPlatform testPlatform;
  // Cache manager. Used at the platform setup
  private ICacheManager cacheMgr;
  // Pentaho session
  private IPentahoSession session;
  // Instance of the MondrianCatalogHelper used for the tests. It is initialized with the platform value (set at
  // setup phase)
  private MondrianCatalogHelper helper;
  // Olap service
  @Mock private  IOlapService olapService;

  @Mock private CacheControl mondrianCacheControl;

  // Mondrian schema returned by the mondrian connection
  @Mock private Schema mondrianSchema;

  @BeforeClass
  public static void setUpClass() {
    repo = mock( IUnifiedRepository.class );
  }


  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks( this );

    // Olap4j connection
    var olapConn = Mockito.mock( OlapConnection.class );
    var rolapConn = Mockito.mock( Connection.class );
    when( olapService.getConnection( any( String.class ), any() ) )
      .thenReturn( olapConn );
    when( rolapConn.getSchema() ).thenReturn( mondrianSchema );
    when( rolapConn.getCacheControl( any() ) )
      .thenReturn( mondrianCacheControl );
    when( olapConn.unwrap( any() ) )
      .thenReturn( rolapConn );

    testPlatform = new MicroPlatform( TestResourceLocation.TEST_RESOURCES + "/solution" );

    var localizingDynamicSchemaProcessor = mock( LocalizingDynamicSchemaProcessor.class );
    doAnswer( invocation -> new String( ( (InputStream) invocation.getArgument( 2 ) ).readAllBytes() ) )
      .when( localizingDynamicSchemaProcessor )
      .filter( nullable( String.class ), any(), any() );

    // repository
    testPlatform.defineInstance( IUnifiedRepository.class, repo );

    // OlapService / Mondrian
    testPlatform.defineInstance( IOlapService.class, olapService );
    // needed for a correct catalog loading process
    testPlatform.defineInstance( "singleTenantAdminUserName", "admin" );

    // Password service
    var mockPasswordService = mock( IPasswordService.class );
    when( mockPasswordService.encrypt( anyString() ) ).then( returnsFirstArg() );
    testPlatform.defineInstance( IPasswordService.class, mockPasswordService );

    var mondrianCatalogHelper =
      new MondrianCatalogHelper( false, null, localizingDynamicSchemaProcessor );

    testPlatform.defineInstance( IAclAwareMondrianCatalogService.class, mondrianCatalogHelper );

    testPlatform.define( IUserRoleListService.class, UserRoleMapperIT.TestUserRoleListService.class,
      IPentahoDefinableObjectFactory.Scope.GLOBAL );

    testPlatform.start();

    session = mock( IPentahoSession.class );
    cacheMgr = PentahoSystem.getCacheManager( session );
    helper = (MondrianCatalogHelper) PentahoSystem.get( IAclAwareMondrianCatalogService.class );
  }

  @After
  public void tearDown() {
    // cleanup to avoid to leave garbage/mocked definitions
    Mockito.reset( repo );
    cacheMgr.clearRegionCache( MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION );
    testPlatform.stop();
  }

  private void initMondrianCatalogsCache() {
    initMondrianCatalogsCache( new HashMap<>() );
  }

  private void initMondrianCatalogsCache( Map<String, MondrianCatalog> map ) {
    MondrianCatalogCache mondrianCatalogCache = new MondrianCatalogCache();
    mondrianCatalogCache.setCatalogs( map );

    cacheMgr.addCacheRegion( MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION );
    cacheMgr.putInRegionCache( MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION, LocaleHelper.getLocale().toString(),
      mondrianCatalogCache );
  }

  // This method is used to initialize the olap folder at the repository mock
  private void repositoryMockOlapServerFolder() {
    final String olapFolderPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "olap-servers";
    UnifiedRepositoryTestUtils.stubGetFolder( repo, olapFolderPath );
    UnifiedRepositoryTestUtils.stubGetChildren( repo, olapFolderPath ); // return no children
  }

  // This method is used to initialize the repository mock for the mondrian folder
  private void repositoryMockMondrianFolder( String... elements ) {
    UnifiedRepositoryTestUtils.stubGetFolder( repo, mondrianFolderPath );
    UnifiedRepositoryTestUtils.stubGetChildren( repo, mondrianFolderPath, elements );
  }

  private void repositoryMockSteelWheelsEntry( String dataSourceInfo ) throws IOException {
    File file1 = new File( TestResourceLocation.TEST_RESOURCES + "/solution/test/charts/steelwheels.mondrian.xml" );
    String mondrianSchema1 = new String( new FileInputStream( file1 ).readAllBytes() );
    repositoryMockEntry( "SteelWheels", mondrianSchema1, dataSourceInfo );
  }

  private void repositoryMockSampleDataEntry() throws IOException {
    File file2 =
      new File( TestResourceLocation.TEST_RESOURCES + "/solution/samples/reporting/SampleData.mondrian.xml" );
    String mondrianSchema2 = new String( new FileInputStream( file2 ).readAllBytes() );
    repositoryMockEntry( "SampleData", mondrianSchema2, "Provider=mondrian;DataSource=SampleData;" );
  }

  private void repositoryMockEntry( String entryName, String schemaXML, String dataSourceInfo ) {
    final String steelWheelsFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + entryName;
    final String steelWheelsMetadataPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "metadata";
    final String steelWheelsSchemaPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "schema.xml";
    UnifiedRepositoryTestUtils.stubGetFile( repo, steelWheelsMetadataPath );
    // Exclude from JNDI = No DataSource at datasourceInfo
    UnifiedRepositoryTestUtils.stubGetData( repo, steelWheelsMetadataPath, "catalog",
      UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/definition",
        "mondrian:/" + entryName ), UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/datasourceInfo",
        dataSourceInfo ) );
    UnifiedRepositoryTestUtils.stubGetFile( repo, steelWheelsSchemaPath );
    UnifiedRepositoryTestUtils.stubGetData( repo, steelWheelsSchemaPath, schemaXML );

    UnifiedRepositoryTestUtils.stubGetFolder( repo, steelWheelsFolderPath );
  }


  private MondrianCatalog createTestCatalog() {
    MondrianSchema schema = new MondrianSchema( CATALOG_NAME, null );
    return  new MondrianCatalog( CATALOG_NAME, "Provider=mondrian;DataSource=SampleData;",
        "solution:test/charts/steelwheels.mondrian.xml", schema );
  }

  @Test( expected = MondrianCatalogServiceException.class )
  public void testAddCatalog_WhenAlreadyExists() {
    MondrianCatalog cat = createTestCatalog();
    initMondrianCatalogsCache( Collections.singletonMap( CATALOG_NAME, cat ) );

    helper = spy( helper );

    doNothing().when( helper ).initIfNotFullyLoaded( session );

    helper.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, false, null, session );
  }

  @Test
  public void testAddCatalog() throws Exception {
    repositoryMockMondrianFolder(); // empty - no children
    repositoryMockOlapServerFolder();

    final String steelWheelsFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + CATALOG_NAME;
    UnifiedRepositoryTestUtils.stubGetFileDoesNotExist( repo, steelWheelsFolderPath );
    UnifiedRepositoryTestUtils.stubCreateFolder( repo, steelWheelsFolderPath );
    final String metadataPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "metadata";
    UnifiedRepositoryTestUtils.stubCreateFile( repo, metadataPath );

    session = new StandaloneSession( "admin" );

    MondrianCatalog cat = createTestCatalog();
    File file = new File( TestResourceLocation.TEST_RESOURCES + "/solution/test/charts/steelwheels.mondrian.xml" );
    session.setAttribute( "MONDRIAN_SCHEMA_XML_CONTENT", new String( new FileInputStream( file ).readAllBytes() ) );

    MondrianCatalogHelper helperSpy = spy( helper );

    IAclNodeHelper aclNodeHelper = mock( IAclNodeHelper.class );
    doNothing().when( aclNodeHelper ).setAclFor( any( RepositoryFile.class ), any( RepositoryFileAcl.class ) );
    doReturn( aclNodeHelper ).when( helperSpy ).getAclHelper();

    doReturn( true ).when( helperSpy ).catalogExists( any( MondrianCatalog.class ), eq( session ) );

    helperSpy.addCatalog( cat, true, session );

    verify( repo ).createFile(
        eq( UnifiedRepositoryTestUtils.makeIdObject( steelWheelsFolderPath ) ),
        argThat( UnifiedRepositoryTestUtils.isLikeFile( UnifiedRepositoryTestUtils.makeFileObject( metadataPath ) ) ),
        argThat( UnifiedRepositoryTestUtils.hasData( UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/definition", "mondrian:/" + cat.getName() ), UnifiedRepositoryTestUtils.pathPropertyPair(
            "/catalog/datasourceInfo", cat.getDataSourceInfo() ) ) ), nullable( String.class )
    );

    verify( repo ).createFile( eq( UnifiedRepositoryTestUtils.makeIdObject( steelWheelsFolderPath ) ),
        argThat( UnifiedRepositoryTestUtils.isLikeFile( UnifiedRepositoryTestUtils.makeFileObject( steelWheelsFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
        any( IRepositoryFileData.class ), nullable( String.class ) );


    // cache should be cleared for this schema only
    verify( olapService, times( 1 ) ).getConnection( CATALOG_NAME, session );
    verify( mondrianCacheControl, times( 1 ) ).flushSchema( this.mondrianSchema );
  }

  @Test
  public void testAddCatalog_WithAcl() {
    initMondrianCatalogsCache();
    MondrianCatalogHelper helperSpy = spy( helper );

    doNothing().when( helperSpy ).initIfNotFullyLoaded( session );

    doReturn( Collections.<MondrianCatalog>emptyList() ).when( helperSpy ).getCatalogs( session );
    doReturn( null ).when( helperSpy ).makeSchema( nullable( String.class ) );

    MondrianCatalog cat = createTestCatalog();
    RepositoryFileAcl acl = mock( RepositoryFileAcl.class );

    IAclNodeHelper aclHelper = mock( IAclNodeHelper.class );
    doNothing().when( aclHelper ).setAclFor( any( RepositoryFile.class ), eq( acl ) );
    doReturn( aclHelper ).when( helperSpy ).getAclHelper();
    doReturn( null ).when( helperSpy ).makeSchema( CATALOG_NAME );
    doReturn( true ).when( helperSpy ).catalogExists( any( MondrianCatalog.class ), eq( session ) );

    MondrianCatalogRepositoryHelper repositoryHelper = mock( MondrianCatalogRepositoryHelper.class );
    doReturn( mock( RepositoryFile.class ) ).when( repositoryHelper ).getMondrianCatalogFile( cat.getName() );
    doReturn( repositoryHelper ).when( helperSpy ).getMondrianCatalogRepositoryHelper();

    helperSpy.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, true, acl, session );
    verify( aclHelper, times( 1 ) ).setAclFor( any( RepositoryFile.class ), eq( acl ) );

    doNothing().when( aclHelper ).setAclFor( any( RepositoryFile.class ), any( RepositoryFileAcl.class ) );
    helperSpy.addCatalog( new ByteArrayInputStream( new byte[0] ), cat, true, null, session );
    verify( aclHelper, times( 1 ) ).setAclFor( any( RepositoryFile.class ), eq( null ) );
  }

  @Test
  public void testListCatalog() throws Exception {
    repositoryMockMondrianFolder( "SampleData/", "SteelWheels/" );
    repositoryMockOlapServerFolder();
    repositoryMockSteelWheelsEntry( "Provider=mondrian;" );
    repositoryMockSampleDataEntry();

    MondrianCatalogHelper helperSpy = spy( helper );

    IAclNodeHelper aclHelper = mock( IAclNodeHelper.class );
    when( aclHelper.canAccess( any(), any( EnumSet.class ) ) ).thenReturn( true );
    doReturn( aclHelper ).when( helperSpy ).getAclHelper();
    Answer<String> answer = invocation -> {
      try {
        return (String) invocation.callRealMethod();
      } catch ( Exception throwable ) {
        if ( throwable.getCause() instanceof ClassCastException ) {
          fail( "should not get ClassCastException here " );
        }
        throw throwable;
      }
    };
    doAnswer( answer ).when( helperSpy ).docAtUrlToString( any() );

    List<MondrianCatalog> cats = helper.listCatalogs( new StandaloneSession( "admin" ), true );
    assertEquals( 1, cats.size() );
    assertEquals( "mondrian:/SampleData", cats.get( 0 ).getDefinition() );
    assertEquals( "SampleData", cats.get( 0 ).getName() );

    verify( mondrianCacheControl, never() ).flushSchema( mondrianSchema );
  }

  @Test
  public void testImportSchemaWithSpaceDoesFlush() {
    repositoryMockMondrianFolder(); // empty - no children
    repositoryMockOlapServerFolder();

    String TMP_FILE_PATH = File.separatorChar + "test" + File.separatorChar + "analysis" + File.separatorChar;
    String uploadDir = PentahoSystem.getApplicationContext().getSolutionPath( TMP_FILE_PATH );
    File mondrianFile = new File( uploadDir + File.separatorChar + "SuperBacon.mondrian.xml" );

    final String sampleDataFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "super bacon";
    final String metadataPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "metadata";

    UnifiedRepositoryTestUtils.stubGetFileDoesNotExist( repo, sampleDataFolderPath );
    UnifiedRepositoryTestUtils.stubCreateFolder( repo, sampleDataFolderPath );

    UnifiedRepositoryTestUtils.stubCreateFile( repo, metadataPath );

    helper.importSchema( mondrianFile, "super bacon", "" );

    // cache should be cleared for this schema only
    verify( olapService, times( 1 ) ).getConnection( "super bacon", null );
    verify( mondrianCacheControl, times( 1 ) ).flushSchema( mondrianSchema );
  }

  @Test
  public void testImportSchema() {
    repositoryMockMondrianFolder(); // empty - no children
    repositoryMockOlapServerFolder();

    String TMP_FILE_PATH = File.separatorChar + "test" + File.separatorChar + "analysis" + File.separatorChar;
    String uploadDir = PentahoSystem.getApplicationContext().getSolutionPath( TMP_FILE_PATH );
    File mondrianFile = new File( uploadDir + File.separatorChar + "SampleData.mondrian.xml" );

    final String sampleDataFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SampleData";
    final String metadataPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "metadata";

    UnifiedRepositoryTestUtils.stubGetFileDoesNotExist( repo, sampleDataFolderPath );
    UnifiedRepositoryTestUtils.stubCreateFolder( repo, sampleDataFolderPath );

    UnifiedRepositoryTestUtils.stubCreateFile( repo, metadataPath );

    helper.importSchema( mondrianFile, "SampleData", "" );

    verify( repo ).createFile( eq( UnifiedRepositoryTestUtils.makeIdObject( sampleDataFolderPath ) ),
      argThat( UnifiedRepositoryTestUtils.isLikeFile( UnifiedRepositoryTestUtils.makeFileObject( metadataPath ) ) ),
      argThat(
        UnifiedRepositoryTestUtils.hasData(
          UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/definition", "mondrian:/" + "SampleData" ),
          UnifiedRepositoryTestUtils.pathPropertyPair( "/catalog/datasourceInfo",
            "Provider=mondrian;DataSource=SampleData" ) ) ),
      nullable( String.class ) );
    verify( repo ).createFile( eq( UnifiedRepositoryTestUtils.makeIdObject( sampleDataFolderPath ) ),
      argThat( UnifiedRepositoryTestUtils.isLikeFile(
        UnifiedRepositoryTestUtils.makeFileObject( sampleDataFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
      any( IRepositoryFileData.class ), nullable( String.class ) );

    // cache should be cleared for this schema only
    verify( olapService, times( 1 ) ).getConnection( "SampleData", null );
    verify( mondrianCacheControl, times( 1 ) ).flushSchema( mondrianSchema );
  }

  @Test
  public void testRemoveCatalog() throws Exception {
    MondrianCatalog cat = createTestCatalog();
    var catalogMap = new HashMap<String, MondrianCatalog>() {{
      put(CATALOG_NAME, cat);
    }};
    initMondrianCatalogsCache( catalogMap );

    repositoryMockMondrianFolder( "SteelWheels/" );

    final String steelWheelsFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SteelWheels";
    repositoryMockSteelWheelsEntry( "Provider=mondrian;DataSource=SteelWheels;" );

    IPentahoSession testSession = new StandaloneSession( "admin" );

    MondrianCatalogHelper helperSpy = spy( helper );
    IAclNodeHelper aclHelper = mock( IAclNodeHelper.class );
    when( aclHelper.canAccess( any(), any( EnumSet.class ) ) ).thenReturn( true );
    doReturn( aclHelper ).when( helperSpy ).getAclHelper();

    MondrianCatalogRepositoryHelper repositoryHelper = mock( MondrianCatalogRepositoryHelper.class );
    doReturn( mock( RepositoryFile.class ) ).when( repositoryHelper )
      .getMondrianCatalogFile( any() );
    doReturn( repositoryHelper ).when( helperSpy ).getMondrianCatalogRepositoryHelper();

    helperSpy.removeCatalog( CATALOG_NAME, testSession );

    verify( repo ).deleteFile( eq( UnifiedRepositoryTestUtils.makeIdObject( steelWheelsFolderPath ) ), eq( true ), nullable( String.class ) );

    // cache should be cleared for this schema only
    verify( olapService, times( 1 ) ).getConnection( CATALOG_NAME, testSession );
    verify( mondrianCacheControl, times( 1 ) ).flushSchema( this.mondrianSchema );
    verify( aclHelper ).removeAclFor( any( RepositoryFile.class ) );
  }

  @Test( expected = MondrianCatalogServiceException.class )
  public void removeCatalog_WhenProhibited() {
    helper = spy( helper );
    doReturn( createTestCatalog() ).when( helper ).getCatalog( CATALOG_NAME, session );
    doNothing().when( helper ).reInit( session );

    MondrianCatalogRepositoryHelper repositoryHelper = mock( MondrianCatalogRepositoryHelper.class );
    doReturn( repositoryHelper ).when( helper ).getMondrianCatalogRepositoryHelper();

    IAclNodeHelper aclHelper = mock( IAclNodeHelper.class );
    when( aclHelper.canAccess( any(), any( EnumSet.class ) ) ).thenReturn( false );
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
    String schema = helperSpy.applyDSP( DATA_SOURCE_INFO_WITH_DSP, SCHEMA_MOCK );
    assertNotNull( schema );
    assertTrue( schema.contains( "REPLACE_TOKEN" ) );
    verify( helperSpy, never() ).docAtUrlToString( nullable( String.class ) );

  }

  @Test
  public void testNoDspApplied_WhenNoDynamicSchemaProcessorParameterInDataSourceInfo() throws Exception {
    final String replaceTemplate = "REPLACE_TEMPLATE";
    final String DATA_SOURCE_INFO_WITHOUT_DSP = "DataSource=TestDataSource";
    final String SCHEMA_MOCK = "Test Schema Mock " + replaceTemplate;

    MondrianCatalogHelper helperSpy = spy( helper );
    doReturn( SCHEMA_MOCK ).when( helperSpy )
      .docAtUrlToString( nullable( String.class ) );

    String schema = helperSpy.applyDSP( DATA_SOURCE_INFO_WITHOUT_DSP, SCHEMA_MOCK );
    assertNotNull( schema );
    verify( helperSpy ).docAtUrlToString( nullable( String.class ) );
    assertFalse( schema.contains( "REPLACE_TOKEN" ) );

  }

  @Test
  public void testGetCatalog() {
    String catalogName = "SteelWheels";

    MondrianCatalog cat = mock( MondrianCatalog.class );
    doReturn( catalogName ).when( cat ).getName();

    MondrianCatalogHelper helperSpy = spy( helper );
    doReturn( true ).when( helperSpy ).hasAccess( eq( cat ), any( RepositoryFilePermission.class ) );
    doNothing().when( helperSpy ).initIfNotFullyLoaded( session );
    doReturn( cat ).when( helperSpy ).getCatalogFromCache( nullable( String.class ), eq( session ) );

    assertEquals( catalogName, helperSpy.getCatalog( catalogName, session ).getName() );
    verify( helperSpy ).hasAccess( eq( cat ), any( RepositoryFilePermission.class ) );

    doReturn( false ).when( helperSpy ).hasAccess( eq( cat ), any( RepositoryFilePermission.class ) );
    assertNull( helperSpy.getCatalog( catalogName, session ) );
  }

  @Test
  public void testGenerateInMemoryDataSourcesXml_NullEtcMondrianFolder() {
    MondrianCatalogHelper helperMock = mock( MondrianCatalogHelper.class );
    IUnifiedRepository unifiedRepositoryMock = mock( IUnifiedRepository.class );

    doReturn( null ).when( unifiedRepositoryMock ).getFile( any( String.class ) );
    doCallRealMethod().when( helperMock ).generateInMemoryDatasourcesXml( any( IUnifiedRepository.class ) );

    String result = helperMock.generateInMemoryDatasourcesXml( unifiedRepositoryMock );
    assertNull( result, null );
  }

  // used at testDspApplied_WhenDataSourceInfoContainsDynamicSchemaProcessorParameter
  public static class DynamicDSPTest implements DynamicSchemaProcessor {

    @Override
    public String processSchema( String s, PropertyList propertylist ) {
      return s.replaceAll( "REPLACE_TEMPLATE", "REPLACE_TOKEN" );
    }
  }
}
