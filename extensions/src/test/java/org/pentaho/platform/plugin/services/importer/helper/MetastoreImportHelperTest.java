/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.importer.helper;

import org.apache.commons.logging.Log;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.SolutionImportHandler;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetaStore;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class MetastoreImportHelperTest {

  private static MockedStatic<PentahoSystem> pentahoSystemMock;

  private MetastoreImportHelper helper;
  private SolutionImportHandler handler;
  private ImportSession importSession;
  private Log mockLogger;
  private ImportExportMetrics metrics;
  private IPlatformImporter mockImporter;

  @BeforeClass
  public static void beforeAll() {
    pentahoSystemMock = mockStatic( PentahoSystem.class );
  }

  @AfterClass
  public static void afterAll() {
    pentahoSystemMock.close();
  }

  @Before
  public void setUp() throws Exception {
    helper = new MetastoreImportHelper();
    handler = spy( new SolutionImportHandler( List.of() ) );

    // Mock ImportSession
    importSession = mock( ImportSession.class );
    mockLogger = mock( Log.class );
    when( importSession.getLogger() ).thenReturn( mockLogger );
    when( importSession.getComponentOverrides() ).thenReturn( null );
    doReturn( importSession ).when( handler ).getImportSession();

    // Mock metrics
    metrics = mock( ImportExportMetrics.class );
    Field metricsField = SolutionImportHandler.class.getDeclaredField( "metrics" );
    metricsField.setAccessible( true );
    metricsField.set( handler, metrics );

    // Mock importer
    mockImporter = mock( IPlatformImporter.class );
    pentahoSystemMock.when( () -> PentahoSystem.get( IPlatformImporter.class ) ).thenReturn( mockImporter );

    // Set performing restore to true
    handler.setPerformingRestore( true );
  }

  @Test
  public void testGetName() {
    String name = helper.getName();
    assertEquals( "Metastore Import Helper", name );
  }

  @Test
  public void testSetOverwriteFile_DefaultFalse() throws Exception {
    // Access private field to verify default
    Field field = MetastoreImportHelper.class.getDeclaredField( "overwriteFile" );
    field.setAccessible( true );
    assertFalse( (boolean) field.get( helper ) );
  }

  @Test
  public void testSetOverwriteFile_ToTrue() throws Exception {
    helper.setOverwriteFile( true );
    Field field = MetastoreImportHelper.class.getDeclaredField( "overwriteFile" );
    field.setAccessible( true );
    assertTrue( (boolean) field.get( helper ) );
  }

  @Test
  public void testSetOverwriteFile_ToFalse() throws Exception {
    helper.setOverwriteFile( true );
    helper.setOverwriteFile( false );
    Field field = MetastoreImportHelper.class.getDeclaredField( "overwriteFile" );
    field.setAccessible( true );
    assertFalse( (boolean) field.get( helper ) );
  }

  @Test
  public void testShouldExecute_NullConfig_ReturnsTrue() {
    assertTrue( helper.shouldExecute( null ) );
  }

  @Test
  public void testShouldExecute_ComponentConfigIncludeTrue_ReturnsTrue() {
    ComponentConfig config = mock( ComponentConfig.class );
    when( config.isIncludeMetastore() ).thenReturn( true );
    assertTrue( helper.shouldExecute( config ) );
  }

  @Test
  public void testShouldExecute_ComponentConfigIncludeFalse_ReturnsFalse() {
    ComponentConfig config = mock( ComponentConfig.class );
    when( config.isIncludeMetastore() ).thenReturn( false );
    assertFalse( helper.shouldExecute( config ) );
  }

  @Test
  public void testShouldExecute_UnknownType_ReturnsTrue() {
    assertTrue( helper.shouldExecute( "unknown_type" ) );
  }

  @Test
  public void testShouldExecute_IntegerType_ReturnsTrue() {
    assertTrue( helper.shouldExecute( 42 ) );
  }

  @Test
  public void testDoImport_NullManifest_SkipsImportAndLogs() throws Exception {
    when( importSession.getManifest() ).thenReturn( null );

    helper.doImport( handler );

    verify( mockLogger ).debug( "Manifest is null - skipping metastore import" );
    verify( mockImporter, never() ).importFile( any() );
  }

  @Test
  public void testDoImport_ShouldExecuteFalse_SkipsImportWithoutError() throws Exception {
    ComponentConfig config = mock( ComponentConfig.class );
    when( config.isIncludeMetastore() ).thenReturn( false );
    when( importSession.getComponentOverrides() ).thenReturn( config );

    helper.doImport( handler );

    verify( mockImporter, never() ).importFile( any() );
  }

  @Test
  public void testDoImport_ManifestWithoutMetastore_SkipsImport() throws Exception {
    ExportManifest manifest = new ExportManifest();
    when( importSession.getManifest() ).thenReturn( manifest );

    helper.doImport( handler );

    verify( mockImporter, never() ).importFile( any() );
  }

  @Test
  public void testDoImport_SuccessfulImport_ImporterCalledAndMetricsRecorded() throws Exception {
    // Prepare manifest with metastore entry
    ExportManifest manifest = new ExportManifest();
    ExportManifestMetaStore metaStore = new ExportManifestMetaStore( "metastore.zip", "metastore", "Metastore backup" );
    manifest.setMetaStore( metaStore );
    when( importSession.getManifest() ).thenReturn( manifest );

    // Prepare file bundle
    IRepositoryFileBundle fileBundle = mock( IRepositoryFileBundle.class );
    RepositoryFile repoFile = new RepositoryFile.Builder( "metastore.zip" ).build();
    when( fileBundle.getFile() ).thenReturn( repoFile );
    when( fileBundle.getInputStream() ).thenReturn( new ByteArrayInputStream( "test data".getBytes() ) );

    // Inject files into handler
    Field filesField = SolutionImportHandler.class.getDeclaredField( "files" );
    filesField.setAccessible( true );
    filesField.set( handler, List.of( fileBundle ) );

    // Mock cachedImports
    Field cachedImportsField = SolutionImportHandler.class.getDeclaredField( "cachedImports" );
    cachedImportsField.setAccessible( true );
    cachedImportsField.set( handler, new HashMap<>() );

    helper.doImport( handler );

    verify( mockImporter ).importFile( any() );
    verify( metrics ).recordSuccess( eq( ImportExportMetrics.Category.METASTORE ) );
  }

  @Test
  public void testDoImport_FileNotFound_RecordsFailure() throws Exception {
    ExportManifest manifest = new ExportManifest();
    ExportManifestMetaStore metaStore = new ExportManifestMetaStore( "missing-metastore.zip", "metastore", "desc" );
    manifest.setMetaStore( metaStore );
    when( importSession.getManifest() ).thenReturn( manifest );

    // Empty files list - no matching file
    Field filesField = SolutionImportHandler.class.getDeclaredField( "files" );
    filesField.setAccessible( true );
    filesField.set( handler, Collections.emptyList() );

    helper.doImport( handler );

    verify( mockImporter, never() ).importFile( any() );
    verify( metrics ).recordFailure(
      eq( ImportExportMetrics.Category.METASTORE ),
      eq( "metastore" ),
      eq( "Import failed" )
    );
  }

  @Test
  public void testDoImport_WithOverwriteFile() throws Exception {
    helper.setOverwriteFile( true );

    ExportManifest manifest = new ExportManifest();
    ExportManifestMetaStore metaStore = new ExportManifestMetaStore( "metastore.zip", "metastore", "desc" );
    manifest.setMetaStore( metaStore );
    when( importSession.getManifest() ).thenReturn( manifest );

    // Prepare file bundle
    IRepositoryFileBundle fileBundle = mock( IRepositoryFileBundle.class );
    RepositoryFile repoFile = new RepositoryFile.Builder( "metastore.zip" ).build();
    when( fileBundle.getFile() ).thenReturn( repoFile );
    when( fileBundle.getInputStream() ).thenReturn( new ByteArrayInputStream( "test data".getBytes() ) );

    Field filesField = SolutionImportHandler.class.getDeclaredField( "files" );
    filesField.setAccessible( true );
    filesField.set( handler, List.of( fileBundle ) );

    Field cachedImportsField = SolutionImportHandler.class.getDeclaredField( "cachedImports" );
    cachedImportsField.setAccessible( true );
    cachedImportsField.set( handler, new HashMap<>() );

    helper.doImport( handler );

    verify( mockImporter ).importFile( any() );
  }

  @Test
  public void testDoImport_MultipleFilesWithCorrectMatch() throws Exception {
    ExportManifest manifest = new ExportManifest();
    ExportManifestMetaStore metaStore = new ExportManifestMetaStore( "metastore.zip", "metastore", "desc" );
    manifest.setMetaStore( metaStore );
    when( importSession.getManifest() ).thenReturn( manifest );

    // Create multiple file bundles
    List<IRepositoryFileBundle> fileBundles = new ArrayList<>();

    // Add a different file first
    IRepositoryFileBundle otherBundle = mock( IRepositoryFileBundle.class );
    RepositoryFile otherFile = new RepositoryFile.Builder( "other.zip" ).build();
    when( otherBundle.getFile() ).thenReturn( otherFile );
    fileBundles.add( otherBundle );

    // Add the metastore file
    IRepositoryFileBundle metastoreBundle = mock( IRepositoryFileBundle.class );
    RepositoryFile metastoreFile = new RepositoryFile.Builder( "metastore.zip" ).build();
    when( metastoreBundle.getFile() ).thenReturn( metastoreFile );
    when( metastoreBundle.getInputStream() ).thenReturn( new ByteArrayInputStream( "metastore data".getBytes() ) );
    fileBundles.add( metastoreBundle );

    // Add another file after (not used, but tests iteration)
    fileBundles.add( mock( IRepositoryFileBundle.class ) );

    Field filesField = SolutionImportHandler.class.getDeclaredField( "files" );
    filesField.setAccessible( true );
    filesField.set( handler, fileBundles );

    Field cachedImportsField = SolutionImportHandler.class.getDeclaredField( "cachedImports" );
    cachedImportsField.setAccessible( true );
    cachedImportsField.set( handler, new HashMap<>() );

    helper.doImport( handler );

    verify( mockImporter ).importFile( any() );
    verify( metrics ).recordSuccess( eq( ImportExportMetrics.Category.METASTORE ) );
  }

  @Test
  public void testDoImport_WithComponentOverridesRespectsSetting() throws Exception {
    ComponentConfig config = mock( ComponentConfig.class );
    when( config.isIncludeMetastore() ).thenReturn( false );
    when( importSession.getComponentOverrides() ).thenReturn( config );

    helper.doImport( handler );

    // Should skip because shouldExecute returns false
    verify( mockImporter, never() ).importFile( any() );
  }

  @Test
  public void testDoImport_ExceptionHandling() throws Exception {
    ExportManifest manifest = new ExportManifest();
    ExportManifestMetaStore metaStore = new ExportManifestMetaStore( "metastore.zip", "metastore", "desc" );
    manifest.setMetaStore( metaStore );
    when( importSession.getManifest() ).thenReturn( manifest );

    IRepositoryFileBundle fileBundle = mock( IRepositoryFileBundle.class );
    RepositoryFile repoFile = new RepositoryFile.Builder( "metastore.zip" ).build();
    when( fileBundle.getFile() ).thenReturn( repoFile );
    when( fileBundle.getInputStream() ).thenThrow( new RuntimeException( "IO Error" ) );

    Field filesField = SolutionImportHandler.class.getDeclaredField( "files" );
    filesField.setAccessible( true );
    filesField.set( handler, List.of( fileBundle ) );

    Field cachedImportsField = SolutionImportHandler.class.getDeclaredField( "cachedImports" );
    cachedImportsField.setAccessible( true );
    cachedImportsField.set( handler, new HashMap<>() );

    helper.doImport( handler );

    verify( metrics ).recordFailure(
      eq( ImportExportMetrics.Category.METASTORE ),
      eq( "metastore" ),
      eq( "Import failed" )
    );
  }

  @Test
  public void testDoImport_PerformingRestoreFalse_NoLogging() throws Exception {
    handler.setPerformingRestore( false );

    ExportManifest manifest = new ExportManifest();
    ExportManifestMetaStore metaStore = new ExportManifestMetaStore( "metastore.zip", "metastore", "desc" );
    manifest.setMetaStore( metaStore );
    when( importSession.getManifest() ).thenReturn( manifest );

    IRepositoryFileBundle fileBundle = mock( IRepositoryFileBundle.class );
    RepositoryFile repoFile = new RepositoryFile.Builder( "metastore.zip" ).build();
    when( fileBundle.getFile() ).thenReturn( repoFile );
    when( fileBundle.getInputStream() ).thenReturn( new ByteArrayInputStream( "test".getBytes() ) );

    Field filesField = SolutionImportHandler.class.getDeclaredField( "files" );
    filesField.setAccessible( true );
    filesField.set( handler, List.of( fileBundle ) );

    Field cachedImportsField = SolutionImportHandler.class.getDeclaredField( "cachedImports" );
    cachedImportsField.setAccessible( true );
    cachedImportsField.set( handler, new HashMap<>() );

    helper.doImport( handler );

    // Info logging should not occur when performingRestore is false
    verify( mockLogger, never() ).info( "Starting metastore import..." );
  }

  @Test
  public void testImportMetaStore_NullMetaStore_RecordsSkip() throws Exception {
    // Set handler on helper
    Field handlerField = MetastoreImportHelper.class.getDeclaredField( "solutionImportHandler" );
    handlerField.setAccessible( true );
    handlerField.set( helper, handler );

    helper.importMetaStore( null );

    verify( metrics ).recordSkip(
      eq( ImportExportMetrics.Category.METASTORE ),
      eq( "metastore" ),
      eq( "Not included in export" )
    );
  }

  @Test
  public void testImportMetaStore_ValidEntry_ImportsSuccessfully() throws Exception {
    Field handlerField = MetastoreImportHelper.class.getDeclaredField( "solutionImportHandler" );
    handlerField.setAccessible( true );
    handlerField.set( helper, handler );

    ExportManifestMetaStore metaStore = new ExportManifestMetaStore( "metastore.zip", "metastore", "desc" );

    IRepositoryFileBundle fileBundle = mock( IRepositoryFileBundle.class );
    RepositoryFile repoFile = new RepositoryFile.Builder( "metastore.zip" ).build();
    when( fileBundle.getFile() ).thenReturn( repoFile );
    when( fileBundle.getInputStream() ).thenReturn( new ByteArrayInputStream( "test data".getBytes() ) );

    Field filesField = SolutionImportHandler.class.getDeclaredField( "files" );
    filesField.setAccessible( true );
    filesField.set( handler, List.of( fileBundle ) );

    Field cachedImportsField = SolutionImportHandler.class.getDeclaredField( "cachedImports" );
    cachedImportsField.setAccessible( true );
    cachedImportsField.set( handler, new HashMap<>() );

    helper.importMetaStore( metaStore );

    verify( mockImporter ).importFile( any() );
    verify( metrics ).recordSuccess( eq( ImportExportMetrics.Category.METASTORE ) );
  }

  @Test
  public void testImportMetaStore_FileNameCaseSensitive() throws Exception {
    ExportManifest manifest = new ExportManifest();
    // Request uppercase filename
    ExportManifestMetaStore metaStore = new ExportManifestMetaStore( "METASTORE.ZIP", "metastore", "desc" );
    manifest.setMetaStore( metaStore );
    when( importSession.getManifest() ).thenReturn( manifest );

    // Provide lowercase filename
    IRepositoryFileBundle fileBundle = mock( IRepositoryFileBundle.class );
    RepositoryFile repoFile = new RepositoryFile.Builder( "metastore.zip" ).build();
    when( fileBundle.getFile() ).thenReturn( repoFile );

    Field filesField = SolutionImportHandler.class.getDeclaredField( "files" );
    filesField.setAccessible( true );
    filesField.set( handler, List.of( fileBundle ) );

    helper.doImport( handler );

    // Should not find the file due to case sensitivity
    verify( mockImporter, never() ).importFile( any() );
    verify( metrics ).recordFailure( eq( ImportExportMetrics.Category.METASTORE ), eq( "metastore" ),
      eq( "Import failed" ) );
  }

  @Test
  public void testImportMetaStore_EmptyFilesList() throws Exception {
    ExportManifest manifest = new ExportManifest();
    ExportManifestMetaStore metaStore = new ExportManifestMetaStore( "metastore.zip", "metastore", "desc" );
    manifest.setMetaStore( metaStore );
    when( importSession.getManifest() ).thenReturn( manifest );

    Field filesField = SolutionImportHandler.class.getDeclaredField( "files" );
    filesField.setAccessible( true );
    filesField.set( handler, new ArrayList<>() );

    helper.doImport( handler );

    verify( metrics ).recordFailure( eq( ImportExportMetrics.Category.METASTORE ), eq( "metastore" ),
      eq( "Import failed" ) );
  }
}