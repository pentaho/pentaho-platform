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

import mondrian.xmla.DataSourcesConfig;
import mondrian.xmla.DataSourcesConfig.Catalog;
import mondrian.xmla.DataSourcesConfig.Catalogs;
import mondrian.xmla.DataSourcesConfig.DataSource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.olap4j.impl.ArrayMap;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.util.FileHelper;
import org.pentaho.platform.util.XmlTestConstants;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION;

@RunWith( MockitoJUnitRunner.class )
public class MondrianCatalogHelperTest {

  private static final String DEFINITION = "mondrian:";

  private final IUnifiedRepository unifiedRepository = mock( IUnifiedRepository.class );
  private final IPasswordService passwordService = mock( IPasswordService.class );
  private final IOlapService olapService = mock( IOlapService.class );

  private final MondrianCatalogRepositoryHelper mcrh =
    spy( new MondrianCatalogRepositoryHelper( unifiedRepository, passwordService, false ) );

  private final MondrianCatalogHelper mch =
    Mockito.spy( new MondrianCatalogHelper( false, null, null, unifiedRepository, olapService ) {
      protected boolean hasAccess( MondrianCatalog cat, RepositoryFilePermission permission ) {
        return true;
      }

      public void setAclFor( String catalogName, RepositoryFileAcl acl ) {
        // do nothing
      }

      protected synchronized MondrianCatalogRepositoryHelper getMondrianCatalogRepositoryHelper() {
        return mcrh;
      }

    } );

  //  private Object cacheValue = null;

  private final ArrayMap<Object, Object> catalogs = new ArrayMap<>();

  DataSourcesConfig.DataSources dsList;

  @Mock
  RepositoryFile mockRepositoryFolder;
  @Mock
  RepositoryFile mockMetadataFolder;
  @Mock
  DataNode metadataNode;
  @Mock
  NodeRepositoryFileData mockIRepositoryFileData;

  @Test
  public void testLoadCatalogsIntoCache() {
    setupDsObjects();
    ICacheManager testCacheManager = new TestICacheManager();

    try ( MockedStatic<PentahoSystem> pentahoSystem = mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.getCacheManager( any() ) ).thenReturn( testCacheManager );

      mch.loadCatalogsIntoCache( dsList, null );

      Object cacheValue =
        testCacheManager.getFromRegionCache( MONDRIAN_CATALOG_CACHE_REGION, Locale.getDefault().toString() );

      Assert.assertTrue( cacheValue instanceof MondrianCatalogCache );
      Map<?, ?> map = ( (MondrianCatalogCache) cacheValue ).getCatalogs();

      for ( Object item : map.values() ) {
        Assert.assertTrue( item instanceof MondrianCatalog );
        MondrianCatalog catalog = (MondrianCatalog) item;
        assertEquals( DEFINITION, catalog.getDefinition() );
      }

      // each entry is added twice to the cache (one with the name and other with the format "mondrian:/<name>")
      assertEquals( Arrays.stream( dsList.dataSources )
        .mapToInt( ds -> ds.catalogs.catalogs.length )
        .sum() * 2, map.size() );
    }
  }

  @Test( timeout = 2000, expected = SAXException.class )
  public void shouldNotFailAndReturnNullWhenMaliciousXmlIsGiven()
    throws IOException, ParserConfigurationException, SAXException {
    mch.getMondrianXmlDocument( new StringBufferInputStream( XmlTestConstants.MALICIOUS_XML ) );
    fail();
  }

  @Test
  public void shouldNotFailAndReturnNotNullWhenLegalXmlIsGiven() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<slave_config>"
      + "</slave_config>";

    assertNotNull( mch.getMondrianXmlDocument( new StringBufferInputStream( xml ) ) );
  }

  // PPP-4192
  @Test
  public void testInconsistentSynchronizationOfDataSourcesConfig() throws Exception {

    MondrianCatalogHelperTestable mondrianCatalogHelper = new MondrianCatalogHelperTestable();

    AtomicReference<Boolean> failed = new AtomicReference<>();
    failed.set( Boolean.FALSE );

    ExecutorService executorService = Executors.newFixedThreadPool( 2 );

    executorService.execute( () -> {
      System.out.println( "validateSynchronizedDataSourcesConfig()" );
      try {
        mondrianCatalogHelper.validateSynchronizedDataSourcesConfig();
      } catch ( InterruptedException e ) {
        e.printStackTrace();
        failed.set( Boolean.TRUE );
      } catch ( Exception e ) {
        e.printStackTrace();
        failed.set( Boolean.TRUE );
      }
    } );

    executorService.execute( () -> {
      for ( int i = 0; i < 25; i++ ) {
        try {
          Thread.sleep( 10 );
        } catch ( InterruptedException e ) {
          e.printStackTrace();
          failed.set( Boolean.TRUE );
        }
        mondrianCatalogHelper.setDataSourcesConfig( "b" );
      }
      System.out.println( "setDataSourcesConfig() - Finished" );
    } );

    executorService.shutdown();

    if ( executorService.awaitTermination( 5000, TimeUnit.MILLISECONDS ) ) {
      if ( failed.get() ) {
        System.out.println( "Failed multi-thread execution" );
        Assert.fail();
      }
      return;
    }
    System.out.println( "Failed by timeout" );
    Assert.fail();
  }

  @Test
  public void testGetCatalog() throws Exception {
    var schemaName = "dummySchemaName";
    var schemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><cube name=\"cube2\"/></schema>", schemaName );
    var dataSourceInfo = "dummyDataSourceInfo";

    try ( MockedStatic<PentahoSystem> pentahoSystem = mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.getCacheManager( eq( null ) ) ).thenReturn( new TestICacheManager() );

      setupRepository( schemaName, dataSourceInfo );
      setupMondrianCatalogHelperMock( schemaName, schemaXML );

      MondrianCatalog cat = mch.getCatalog( schemaName, null );

      assertNotNull( cat );
      assertEquals( schemaName, cat.getName() );
      assertEquals( String.format( "mondrian:/%s", schemaName ), cat.getDefinition() );
      assertEquals( dataSourceInfo, cat.getDataSourceInfo() );
      assertEquals( 2, cat.getSchema().getCubes().size() );
    }
  }

  @Test
  public void testGetCatalogPreviouslyLoaded() {
    var schemaName = "dummySchemaName";
    var schemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><cube name=\"cube2\"/></schema>", schemaName );
    var dataSourceName = "dummyDataSourceName";
    var definition = String.format( "mondrian:/%s", schemaName );
    var dataSourceInfo = "dummyDataSourceInfo";

    try ( MockedStatic<PentahoSystem> pentahoSystem = mockStatic( PentahoSystem.class ) ) {
      setupCacheManager( pentahoSystem, new ArrayList<>( List.of(
        new MondrianCatalog( schemaName, dataSourceName, definition, new MondrianSchema( schemaName,
          List.of( new MondrianCube( "cube1", "cube1" ), new MondrianCube( "cube2", "cube2" ),
            new MondrianCube( "cube3", "cube3" ) ) ) )
      ) ) );
      setupRepository( schemaName, dataSourceInfo );
      setupMondrianCatalogHelperMock( schemaName, schemaXML );

      MondrianCatalog originalCatalog = mch.getCatalog( schemaName, null );

      assertNotNull( originalCatalog );
      assertEquals( schemaName, originalCatalog.getName() );
      assertEquals( definition, originalCatalog.getDefinition() );
      assertEquals( 3, originalCatalog.getSchema().getCubes().size() );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @Test
  public void testGetCatalogSchemaAsStreamWithoutApplyingAnnotations() {
    var catalogName = "catalog";
    var schemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><cube name=\"cube2\"/></schema>", catalogName );
    var annotatedSchemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><annotation/><cube name=\"cube2\"/></schema>",
        catalogName );
    var annotationsXml = "<annotations> <annotation>  <name>annotation name</name> </annotation> </annotations>";
    doReturn( Map.of(
      "schema.xml", new ByteArrayInputStream( schemaXML.getBytes() ),
      "schema.annotated.xml", new ByteArrayInputStream( annotatedSchemaXML.getBytes() ),
      "annotations.xml", new ByteArrayInputStream( annotationsXml.getBytes() )
    ) ).when(
      mcrh ).getMondrianSchemaFiles( catalogName );

    var res = mch.getCatalogSchemaAsStream( catalogName, false );

    assertNotNull( res );
    assertEquals( schemaXML, FileHelper.getStringFromInputStream( res ) );
  }

  @Test
  public void testGetCatalogSchemaAsStreamApplyingAnnotations() {
    var catalogName = "catalog";
    var schemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><cube name=\"cube2\"/></schema>", catalogName );
    var annotatedSchemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><annotation/><cube name=\"cube2\"/></schema>",
        catalogName );
    var annotationsXml = "<annotations> <annotation>  <name>annotation name</name> </annotation> </annotations>";
    doReturn( Map.of(
      "schema.xml", new ByteArrayInputStream( schemaXML.getBytes() ),
      "schema.annotated.xml", new ByteArrayInputStream( annotatedSchemaXML.getBytes() ),
      "annotations.xml", new ByteArrayInputStream( annotationsXml.getBytes() )
    ) ).when(
      mcrh ).getMondrianSchemaFiles( catalogName );

    var res = mch.getCatalogSchemaAsStream( catalogName, true );

    assertNotNull( res );
    assertEquals( annotatedSchemaXML, FileHelper.getStringFromInputStream( res ) );
  }

  @Test
  public void testGetCatalogAnnotationsAsStream() {
    var catalogName = "catalog";
    var schemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><cube name=\"cube2\"/></schema>", catalogName );
    var annotationsXml = "<annotations> <annotation>  <name>annotation name</name> </annotation> </annotations>";
    doReturn( Map.of(
      "schema.xml", new ByteArrayInputStream( schemaXML.getBytes() ),
      "annotations.xml", new ByteArrayInputStream( annotationsXml.getBytes() )
    ) ).when(
      mcrh ).getMondrianSchemaFiles( catalogName );

    var res = mch.getCatalogAnnotationsAsStream( catalogName );

    assertNotNull( res );
  }

  @Test
  public void testGetCatalogAnnotationsAsStringWithNoAnnotations() {
    var catalogName = "catalog";
    var schemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><cube name=\"cube2\"/></schema>", catalogName );

    doReturn( Map.of( "schema.xml", new ByteArrayInputStream( schemaXML.getBytes() ) ) ).when(
      mcrh ).getMondrianSchemaFiles( catalogName );

    var res = mch.getCatalogAnnotationsAsStream( catalogName );

    assertNull( res );
  }

  private void setupCacheManager( MockedStatic<PentahoSystem> pentahoSystem, List<MondrianCatalog> catalogs ) {
    var cacheMgr = new TestICacheManager();
    var mondrianCatalogCache = new MondrianCatalogCache();
    mondrianCatalogCache.getMondrianCatalogCacheState().setFullyLoaded( true );

    for ( MondrianCatalog catalog : catalogs ) {
      mondrianCatalogCache.getCatalogs().put( String.format( "mondrian:/%s", catalog.getName() ), catalog );
      mondrianCatalogCache.getCatalogs().put( catalog.getName(), catalog );
    }

    cacheMgr.putInRegionCache( MONDRIAN_CATALOG_CACHE_REGION, Locale.getDefault(), mondrianCatalogCache );

    pentahoSystem.when( () -> PentahoSystem.getCacheManager( eq( null ) ) ).thenReturn( cacheMgr );
  }

  private void setupRepository( String schemaName, String dataSourceInfo ) {
    when( unifiedRepository.getFile( "/etc/mondrian" ) ).thenReturn( mockRepositoryFolder );
    when( unifiedRepository.getFile( String.format( "/etc/mondrian/%s/metadata", schemaName ) ) ).thenReturn(
      mockMetadataFolder );
    //TODO: may need to handle the return null case, not quite sure how to though...
    when( unifiedRepository.getDataForRead( any(), any() ) ).thenReturn( mockIRepositoryFileData );

      when( mockIRepositoryFileData.getNode() ).thenReturn( metadataNode );

      when( metadataNode.getProperty( "datasourceInfo" ) ).thenReturn(
        new DataProperty( "datasourceInfo", dataSourceInfo, DataNode.DataPropertyType.STRING ) );
      when( metadataNode.getProperty( "definition" ) ).thenReturn(
        new DataProperty( "definition", String.format( "mondrian:/%s", schemaName ),
          DataNode.DataPropertyType.STRING ) );
  }

  private void setupMondrianCatalogHelperMock( String schemaName, String schemaXML ) throws Exception {
    doReturn( schemaXML ).when( mch ).docAtUrlToString( eq( String.format( "mondrian:/%s", schemaName ) ) );

    ArgumentCaptor<InputStream> captor = ArgumentCaptor.forClass( InputStream.class );

    doAnswer( invocation -> {
      System.out.println( new String( captor.getValue().readAllBytes() ) );
      return null; // Adjust return value if needed
    } ).when( mcrh ).addHostedCatalog( captor.capture(), any(), any() );

    doNothing().when( mch ).flushCacheForCatalog( eq( schemaName ), any() );
  }

  // Check that adding a catalog (that is not at the cache) with no override option is performed successfully. It is
  // possible to access it after.
  @Test
  public void testAddCatalogNewEntry() throws Exception {
    var dataSourceInfo = "dummyDataSourceInfo";
    var schemaName = "dummySchemaName";
    var schemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><cube name=\"cube2\"/></schema>", schemaName );

    try ( MockedStatic<PentahoSystem> pentahoSystem = mockStatic( PentahoSystem.class ) ) {

      setupRepository( schemaName, dataSourceInfo );
      setupCacheManager( pentahoSystem, new ArrayList<>() );
      setupMondrianCatalogHelperMock( schemaName, schemaXML );

      var catalog =
        new MondrianCatalog( schemaName, null, "dummy", new MondrianSchema( schemaName, new ArrayList<>() ) );
      mch.addCatalog( new ByteArrayInputStream( schemaXML.getBytes( StandardCharsets.UTF_8 ) ), catalog, true,
        null );

      MondrianCatalog cat = mch.getCatalog( schemaName, null );

      // Cache was previously loaded, so there is no need to do that
      verify( mch, times( 0 ) ).loadCatalogsIntoCache( any(), any() );

      assertNotNull( cat );
      assertEquals( schemaName, cat.getName() );
      assertEquals( String.format( "mondrian:/%s", schemaName ), cat.getDefinition() );
      assertEquals( dataSourceInfo, cat.getDataSourceInfo() );
      assertEquals( 2, cat.getSchema().getCubes().size() );
    }
  }

  // Check that adding a catalog (that is already loaded at the cache) with no override option, shall throw an exception
  @Test
  public void testAddingCatalogAlreadyAtCacheWithNoOverrideThrowException() throws Exception {
    var schemaName = "dummySchemaName";
    var schemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><cube name=\"cube2\"/></schema>", schemaName );
    var dataSourceInfo = "dummyDataSourceInfo";
    var definition = "";

    try ( MockedStatic<PentahoSystem> pentahoSystem = mockStatic( PentahoSystem.class ) ) {

      setupCacheManager( pentahoSystem, new ArrayList<>( List.of(
        new MondrianCatalog( schemaName, dataSourceInfo, definition, null )
      ) ) );
      setupRepository( schemaName, dataSourceInfo );
      setupMondrianCatalogHelperMock( schemaName, schemaXML );

      var catalog =
        new MondrianCatalog( schemaName, dataSourceInfo, definition, null );

      MondrianCatalogServiceException e = assertThrows( MondrianCatalogServiceException.class,
        () -> mch.addCatalog( new ByteArrayInputStream( schemaXML.getBytes( StandardCharsets.UTF_8 ) ), catalog,
          false,
          null ) );

      // Cache was previously loaded, so there is no need to do that
      verify( mch, times( 0 ) ).loadCatalogsIntoCache( any(), any() );
      assertEquals( MondrianCatalogServiceException.Reason.ALREADY_EXISTS, e.getReason() );
    }
  }

  // Check that adding a catalog (that is already loaded at the cache) with no override option and with a different
  // datasource, shall throw an exception
  @Test
  public void testAddingCatalogAlreadyAtCacheWithNoOverrideAndDifferentDataSourceInfoThrowException2()
    throws Exception {
    var schemaName = "dummySchemaName";
    var schemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><cube name=\"cube2\"/></schema>", schemaName );
    var dataSourceInfo = "dummyDataSourceInfo";
    var definition = "";

    try ( MockedStatic<PentahoSystem> pentahoSystem = mockStatic( PentahoSystem.class ) ) {

      setupCacheManager( pentahoSystem, new ArrayList<>( List.of() ) );
      setupRepository( schemaName, dataSourceInfo );
      setupMondrianCatalogHelperMock( schemaName, schemaXML );

      var catalog =
        new MondrianCatalog( schemaName, dataSourceInfo + "_new", definition, null );

      mch.addCatalog( new ByteArrayInputStream( schemaXML.getBytes( StandardCharsets.UTF_8 ) ), catalog, false,
        null );

      MondrianCatalogServiceException e = assertThrows( MondrianCatalogServiceException.class,
        () -> mch.addCatalog( new ByteArrayInputStream( schemaXML.getBytes( StandardCharsets.UTF_8 ) ), catalog,
          false,
          null ) );

      // Cache was previously loaded, so there is no need to do that
      verify( mch, times( 0 ) ).loadCatalogsIntoCache( any(), any() );
      assertEquals( MondrianCatalogServiceException.Reason.XMLA_SCHEMA_NAME_EXISTS, e.getReason() );

    }
  }

  // Check that adding a catalog (that is already loaded at the cache) with override option, update its info
  @Test
  public void testAddingCatalogAlreadyAtCacheWithOverrideUpdatesIt() throws Exception {
    var schemaName = "dummySchemaName";
    var schemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><cube name=\"cube2\"/></schema>", schemaName );
    var dataSourceName = "dummyDataSourceName";
    var definition = String.format( "mondrian:/%s", schemaName );
    var dataSourceInfo = "dummyDataSourceInfo";

    try ( MockedStatic<PentahoSystem> pentahoSystem = mockStatic( PentahoSystem.class ) ) {

      setupCacheManager( pentahoSystem, new ArrayList<>( List.of(
        new MondrianCatalog( schemaName, dataSourceName, definition, new MondrianSchema( schemaName,
          List.of( new MondrianCube( "cube1", "cube1" ), new MondrianCube( "cube2", "cube2" ),
            new MondrianCube( "cube3", "cube3" ) ) ) )
      ) ) );
      setupRepository( schemaName, dataSourceInfo );
      setupMondrianCatalogHelperMock( schemaName, schemaXML );

      MondrianCatalog originalCatalog = mch.getCatalog( schemaName, null );

      mch.addCatalog( new ByteArrayInputStream( schemaXML.getBytes( StandardCharsets.UTF_8 ) ),
        new MondrianCatalog( schemaName, dataSourceName, definition, null ), true,
        null );

      MondrianCatalog finalCatalog = mch.getCatalog( schemaName, null );
      // Cache is not loaded as it was previously loaded
      verify( mch, times( 0 ) ).loadCatalogsIntoCache( any(), any() );

      assertNotNull( originalCatalog );
      assertEquals( schemaName, originalCatalog.getName() );
      assertEquals( definition, originalCatalog.getDefinition() );
      assertEquals( 3, originalCatalog.getSchema().getCubes().size() );

      assertNotNull( finalCatalog );
      assertEquals( schemaName, finalCatalog.getName() );
      assertEquals( definition, finalCatalog.getDefinition() );
      assertEquals( 2, finalCatalog.getSchema().getCubes().size() );
    }
  }


/*
  // Check that adding a catalog (that is already loaded at the cache) with override option, update its info
  @Test
  public void testAddingCatalogAlreadyWithOverrideUpdatesIt2() throws Exception {


    var schemaName = "dummySchemaName";
    var schemaXML =
      String.format( "<schema name=\"%s\"><cube name=\"cube1\"/><cube name=\"cube2\"/></schema>", schemaName );
    var dataSourceName = "dummyDataSourceName";
    var definition = String.format( "mondrian:/%s", schemaName );


    try ( MockedStatic<PentahoSystem> pentahoSystem = mockStatic( PentahoSystem.class ) ) {

      ArgumentCaptor<InputStream> captor = ArgumentCaptor.forClass( InputStream.class );


      doAnswer( invocation -> {
        System.out.println( new String( captor.getValue().readAllBytes() ) );
        return null; // Adjust return value if needed
      } ).when( mcrh ).addHostedCatalog( captor.capture(), any(), any() );


      var cacheMgr = new TestICacheManager();

      var mondrianCatalog =
        new MondrianCatalog( schemaName, dataSourceName + "1", definition, new MondrianSchema( schemaName,
          List.of( new MondrianCube( "cube1", "cube1" ), new MondrianCube( "cube2", "cube2" ),
            new MondrianCube( "cube3", "cube3" ) ) ) );

      var mondrianCatalogCache = new MondrianCatalogCache();
      mondrianCatalogCache.getMondrianCatalogCacheState().setFullyLoaded( true );
      mondrianCatalogCache.getCatalogs()
        .put( String.format( "mondrian:/%s", schemaName ), mondrianCatalog );
      mondrianCatalogCache.getCatalogs()
        .put( String.format( schemaName, schemaName ), mondrianCatalog );

      cacheMgr.putInRegionCache( MONDRIAN_CATALOG_CACHE_REGION, "en", mondrianCatalogCache );

      pentahoSystem.when( () -> PentahoSystem.getCacheManager( eq( null ) ) ).thenReturn( cacheMgr );

      IUnifiedRepository unifiedRepository = mock( IUnifiedRepository.class );
      when( unifiedRepository.getFile( "/etc/mondrian" ) ).thenReturn( mockRepositoryFolder );
      when( unifiedRepository.getFile( "/etc/mondrian/name/metadata" ) ).thenReturn( mockMetatdataFolder );
      //TODO: may need to handle the return null case, not quite sure how to though...
      when( unifiedRepository.getDataForRead( any(), any() ) ).thenReturn( mockIRepositoryFileData );
      when( unifiedRepository.getChildren( any( Serializable.class ) ) ).thenReturn(
        singletonList( mockRepositoryFile ) );

      pentahoSystem.when( () -> PentahoSystem.get( any(), eq( null ) ) ).thenReturn( unifiedRepository );


      when( mockRepositoryFolder.getId() ).thenReturn( 1 );
      when( mockRepositoryFile.getName() ).thenReturn( "name" );
      when( mockMetatdataFolder.getId() ).thenReturn( 2 );
      when( mockIRepositoryFileData.getNode() ).thenReturn( metadataNode );
      //      when( metadataNode.getProperty( "datasourceInfo" ) ).thenReturn(
      //        new DataProperty( "datasourceInfo", "datasourceInfo", DataNode.DataPropertyType.STRING ) );
      when( metadataNode.getProperty( "datasourceInfo" ) ).thenReturn(
        new DataProperty( "datasourceInfo", null, DataNode.DataPropertyType.STRING ) );

      when( metadataNode.getProperty( "definition" ) ).thenReturn(
        new DataProperty( "definition", definition,
          DataNode.DataPropertyType.STRING ) );


      doReturn( schemaXML ).when( mch ).docAtUrlToString( eq( String.format( "mondrian:/%s", schemaName ) ), any() );
      doNothing().when( mch ).flushCacheForCatalog( eq( schemaName ), any() );

      //TODO: seems redundant, can probably remove
      mch.catalogRepositoryHelper = mockMondrianCatalogRepositoryHelper;

      var stream =
        new StringBufferInputStream( schemaXML );

      var catalog =
        new MondrianCatalog( schemaName, dataSourceName, definition,
          new MondrianSchema( schemaName, new ArrayList<>() ) );

      MondrianCatalog originalCat = mch.getCatalog( schemaName, null );

      mch.addCatalog( stream, catalog, false, null );

      //      catalog = new MondrianCatalog( "name", "dummy", "dummy", new MondrianSchema( "teste - step2", new
      //      ArrayList<>() ) );
      //      mch.addCatalog( stream, catalog, true, null );


      MondrianCatalog cat = mch.getCatalog( schemaName, null );
      verify( mockRepositoryFile, times( 1 ) ).getName();


      // Cache is not loaded as it was previously loaded
      verify( mch, times( 0 ) ).loadCatalogsIntoCache( any(), any() );
      assertNotNull( originalCat );
      assertEquals( schemaName, originalCat.getName() );
      assertEquals( definition, originalCat.getDefinition() );
      assertEquals( 3, originalCat.getSchema().getCubes().size() );


      // Cache is not loaded as it was previously loaded
      verify( mch, times( 0 ) ).loadCatalogsIntoCache( any(), any() );
      assertNotNull( cat );
      assertEquals( schemaName, cat.getName() );
      assertEquals( definition, cat.getDefinition() );
      assertEquals( 2, cat.getSchema().getCubes().size() );
    }
  }

*/

  private void setupDsObjects() {
    dsList = new DataSourcesConfig.DataSources();

    dsList.dataSources = new DataSource[ 1 ];
    DataSource ds = new DataSource();
    dsList.dataSources[ 0 ] = ds;

    ds.catalogs = new Catalogs();
    ds.catalogs.catalogs = new Catalog[ 1 ];

    Catalog ct = new Catalog();
    ct.definition = DEFINITION;

    ds.catalogs.catalogs[ 0 ] = ct;
  }

  class MondrianCatalogHelperTestable extends MondrianCatalogHelper {

    public synchronized void validateSynchronizedDataSourcesConfig() throws Exception {
      setDataSourcesConfig( "a" );
      for ( int i = 0; i < 25; i++ ) {
        Thread.sleep( 20 );
        if ( !"a".equals( getDataSourcesConfig() ) ) {
          throw new Exception( "Another thread changed dataSourcesConfig value" );
        }
      }
      System.out.println( "validateSynchronizedDataSourcesConfig() - Finished" );
    }
  }

  class TestICacheManager implements ICacheManager {

    @Override public void cacheStop() {

    }

    @Override public void killSessionCache( IPentahoSession session ) {

    }

    @Override public void killSessionCaches() {

    }

    @Override public void putInSessionCache( IPentahoSession session, String key, Object value ) {

    }

    @Override public void clearCache() {

    }

    @Override public void removeFromSessionCache( IPentahoSession session, String key ) {

    }

    @Override public Object getFromSessionCache( IPentahoSession session, String key ) {
      return null;
    }

    @Override public boolean cacheEnabled() {
      return false;
    }

    @Override public void putInGlobalCache( Object key, Object value ) {

    }

    @Override public Object getFromGlobalCache( Object key ) {
      return null;
    }

    @Override public void removeFromGlobalCache( Object key ) {

    }

    @Override public boolean cacheEnabled( String region ) {
      return true;
    }

    @Override public void onLogout( IPentahoSession session ) {

    }

    @Override public boolean addCacheRegion( String region ) {
      return true;
    }

    @Override public boolean addCacheRegion( String region, Properties cacheProperties ) {
      return true;
    }

    @Override public void clearRegionCache( String region ) {

    }

    @Override public void removeRegionCache( String region ) {

    }

    @Override public void putInRegionCache( String region, Object key, Object value ) {
      catalogs.put( region.concat( "-" + key.toString() ), value );
    }

    @Override public Object getFromRegionCache( String region, Object key ) {
      return catalogs.get( region.concat( "-" + key.toString() ) );
    }

    @Override public Set getAllEntriesFromRegionCache( String region ) {
      return null;
    }

    @Override public Set getAllKeysFromRegionCache( String region ) {
      return null;
    }

    @Override public List getAllValuesFromRegionCache( String region ) {
      return null;
    }

    @Override public void removeFromRegionCache( String region, Object key ) {

    }
  }
}
