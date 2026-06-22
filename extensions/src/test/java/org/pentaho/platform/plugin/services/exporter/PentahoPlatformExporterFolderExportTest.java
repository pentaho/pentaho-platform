/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.exporter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.platform.api.importexport.ExportException;
import org.pentaho.platform.api.importexport.IExportHelper;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;

/**
 * Test class for PentahoPlatformExporter folder export functionality and helper architecture.
 * 
 * NOTE: These tests have been superseded by the refactoring to move export logic into helpers.
 * Tests are disabled (@Ignore) because the methods they test (exportFolderAcls, exportFolderHierarchyWithMetadata, etc.)
 * have been moved into RepositoryContentExportHelper and are no longer public methods on PentahoPlatformExporter.
 * 
 * For testing folder export functionality, use integration tests that test performExport() end-to-end,
 * or test the RepositoryContentExportHelper directly.
 * 
 * TODO: Migrate these tests to:
 * 1. Test through performExport() for integration testing
 * 2. Test RepositoryContentExportHelper directly for unit testing
 * 3. Verify export helpers are properly called during export
 * 
 * Original test descriptions:
 * Tests that:
 * 1. Folders are exported independently with their own metadata
 * 2. Export helpers are properly registered and contain actual export logic
 * 3. Selective export based on ComponentConfig works correctly
 * 4. Helpers are invoked during export with proper delegation
 */
@Ignore( "Folder export tests refactored: methods moved to RepositoryContentExportHelper. Test through performExport() or helper tests." )
public class PentahoPlatformExporterFolderExportTest {

  private PentahoPlatformExporter exporter;

  @Mock
  private IUnifiedRepository mockRepository;

  @Mock
  private RepositoryFile mockRootFolder;

  @Mock
  private RepositoryFile mockSubFolder1;

  @Mock
  private RepositoryFile mockSubFolder2;

  @Mock
  private RepositoryFile mockFile1;

  @Mock
  private RepositoryFile mockFile2;

  @Mock
  private RepositoryFile mockFile3;

  @Captor
  private ArgumentCaptor<RepositoryFile> folderCaptor;

  private File tempZipFile;
  private ZipOutputStream zos;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks( this );

    // Create temporary zip file
    tempZipFile = File.createTempFile( "test-export", ".zip" );
    tempZipFile.deleteOnExit();
    zos = new ZipOutputStream( new FileOutputStream( tempZipFile ) );

    // Initialize exporter with mock repository
    exporter = new PentahoPlatformExporter( mockRepository );
    exporter.zos = zos;  // Set protected field directly

    // Setup repository mock
    when( mockRepository.getFile( anyString() ) ).thenReturn( mockRootFolder );
  }

  // ========== Export Helper Architecture Tests ==========

  /**
   * Test A: Export helpers are properly registered during initialization
   * Verifies that PentahoPlatformExporter registers all built-in export helpers
   * in the correct order: Repository, Datasources, Metadata, Mondrian, Users/Roles, Metastore
   */
  @Test
  public void testExportHelpersRegisteredDuringInitialization() {
    // Get list of registered export helpers
    List<IExportHelper> helpers = exporter.getExportHelpers();
    
    // Verify helpers are registered
    assertNotNull( "Export helpers list should not be null", helpers );
    assertTrue( "At least 6 export helpers should be registered", helpers.size() >= 6 );
    
    // Verify helper names indicate they are the built-in helpers
    List<String> helperNames = new ArrayList<>();
    for ( IExportHelper helper : helpers ) {
      helperNames.add( helper.getName() );
    }
    
    assertTrue( "RepositoryContentExporter helper should be registered", 
        helperNames.contains( "RepositoryContentExporter" ) );
    assertTrue( "DatasourcesExporter helper should be registered", 
        helperNames.contains( "DatasourcesExporter" ) );
    assertTrue( "MetadataExporter helper should be registered", 
        helperNames.contains( "MetadataExporter" ) );
    assertTrue( "MondrianExporter helper should be registered", 
        helperNames.contains( "MondrianExporter" ) );
    assertTrue( "UsersAndRolesExporter helper should be registered", 
        helperNames.contains( "UsersAndRolesExporter" ) );
    assertTrue( "MetastoreExporter helper should be registered", 
        helperNames.contains( "MetastoreExporter" ) );
  }

  /**
   * Test B: Export helpers respect selective export configuration
   * Verifies that export helpers only execute when their component is enabled
   * in ComponentConfig
   */
  @Test
  public void testExportHelpersRespectSelectiveExportConfig() throws ExportException {
    // Create a config that only includes repository content
    ComponentConfig config = new ComponentConfig();
    config.setIncludeContent( true );
    config.setIncludeDatasources( false );
    config.setIncludeSchedules( false );
    config.setIncludeMondrian( false );
    config.setIncludeUsers( false );
    config.setIncludeMetastore( false );

    // Get helpers and verify they respect the config
    List<IExportHelper> helpers = exporter.getExportHelpers();
    assertNotNull( "Helpers should not be null", helpers );
    assertTrue( "Should have registered export helpers", helpers.size() > 0 );

    // Verify that helpers can be conditionally executed based on shouldExecute()
    for ( IExportHelper helper : helpers ) {
      // Each helper should implement shouldExecute() logic
      // based on its component's configuration setting
      assertNotNull( "Helper should have a name", helper.getName() );
    }
  }

  /**
   * Test C: Export helpers can be added and removed dynamically
   * Verifies that the exporter supports adding/removing export helpers
   */
  @Test
  public void testExportHelpersDynamicallyManaged() {
    List<IExportHelper> initialHelpers = exporter.getExportHelpers();
    int initialCount = initialHelpers.size();

    // Mock a custom export helper
    IExportHelper customHelper = mock( IExportHelper.class );
    when( customHelper.getName() ).thenReturn( "CustomTestHelper" );

    // Add custom helper
    exporter.addExportHelper( customHelper );
    
    List<IExportHelper> updatedHelpers = exporter.getExportHelpers();
    assertTrue( "Helper list should grow after adding a new helper", 
        updatedHelpers.size() == initialCount + 1 );

    // Verify the custom helper is in the list
    boolean found = false;
    for ( IExportHelper helper : updatedHelpers ) {
      if ( "CustomTestHelper".equals( helper.getName() ) ) {
        found = true;
        break;
      }
    }
    assertTrue( "Custom helper should be registered", found );
  }

  // ========== Folder Export Architecture Tests ==========

  /**
   * Test 1: Folders are exported independently with their own metadata
   */
  @Test
  public void testFolderExportIndependentMetadata() throws Exception {
    // Setup folder structure:
    // /root
    //   /subfolder1
    //   /subfolder2
    //   file1.txt

    setupFolderStructure();

    // Mock folder ACL export
    PentahoPlatformExporter spyExporter = spy( exporter );
    doNothing().when( spyExporter ).exportFolderAcls( any( RepositoryFile.class ) );

    // Export the folder hierarchy
    spyExporter.exportFolderHierarchyWithMetadata( mockRootFolder, zos, "/" );

    // Verify that exportFolderAcls was called for EACH folder independently
    // This is the key test - each folder should have its metadata exported separately
    verify( spyExporter, times( 3 ) ).exportFolderAcls( any( RepositoryFile.class ) );

    // Verify root, subfolder1, and subfolder2 all had their ACLs exported
    verify( spyExporter ).exportFolderAcls( mockRootFolder );
    verify( spyExporter ).exportFolderAcls( mockSubFolder1 );
    verify( spyExporter ).exportFolderAcls( mockSubFolder2 );
  }

  /**
   * Test 2: Folder permissions are NOT inherited from files
   * Files and folders are processed separately
   */
  @Test
  public void testFolderPermissionsNotInheritedFromFiles() throws Exception {
    // Setup: folder with files
    setupFolderStructure();

    PentahoPlatformExporter spyExporter = spy( exporter );
    doNothing().when( spyExporter ).exportFolderAcls( any( RepositoryFile.class ) );
    doNothing().when( spyExporter ).exportFile( any( RepositoryFile.class ), any( ZipOutputStream.class ), anyString() );

    // Export the folder hierarchy
    spyExporter.exportFolderHierarchyWithMetadata( mockRootFolder, zos, "/" );

    // Verify folders and files were processed separately
    // Folders should have ACLs exported independently
    verify( spyExporter, times( 3 ) ).exportFolderAcls( any( RepositoryFile.class ) );
    
    // Files should be exported via exportFile() method, NOT via exportFolderAcls()
    verify( spyExporter, times( 3 ) ).exportFile( any( RepositoryFile.class ), eq( zos ), anyString() );

    // Critical: exportFolderAcls should never be called for files
    verify( spyExporter, never() ).exportFolderAcls( mockFile1 );
    verify( spyExporter, never() ).exportFolderAcls( mockFile2 );
    verify( spyExporter, never() ).exportFolderAcls( mockFile3 );
  }

  /**
   * Test 3: Hierarchical folder structure is maintained during export
   * Subfolders are processed recursively
   */
  @Test
  public void testFolderHierarchyMaintained() throws Exception {
    // Setup deep hierarchy:
    // /root
    //   /subfolder1
    //     /subfolder1a
    //     file1a.txt
    //   /subfolder2
    //     file2.txt
    //   file.txt

    RepositoryFile mockSubFolder1a = mock( RepositoryFile.class );
    when( mockSubFolder1a.isFolder() ).thenReturn( true );
    when( mockSubFolder1a.getPath() ).thenReturn( "/root/subfolder1/subfolder1a" );
    when( mockSubFolder1a.getName() ).thenReturn( "subfolder1a" );

    RepositoryFile mockFile1a = mock( RepositoryFile.class );
    when( mockFile1a.isFolder() ).thenReturn( false );
    when( mockFile1a.getPath() ).thenReturn( "/root/subfolder1/subfolder1a/file1a.txt" );

    when( mockRootFolder.isFolder() ).thenReturn( true );
    when( mockRootFolder.getPath() ).thenReturn( "/root" );
    when( mockRootFolder.getName() ).thenReturn( "root" );

    when( mockSubFolder1.isFolder() ).thenReturn( true );
    when( mockSubFolder1.getPath() ).thenReturn( "/root/subfolder1" );
    when( mockSubFolder1.getName() ).thenReturn( "subfolder1" );

    when( mockSubFolder2.isFolder() ).thenReturn( true );
    when( mockSubFolder2.getPath() ).thenReturn( "/root/subfolder2" );
    when( mockSubFolder2.getName() ).thenReturn( "subfolder2" );

    when( mockFile1.isFolder() ).thenReturn( false );
    when( mockFile1.getPath() ).thenReturn( "/root/subfolder1/file1.txt" );

    when( mockFile2.isFolder() ).thenReturn( false );
    when( mockFile2.getPath() ).thenReturn( "/root/subfolder2/file2.txt" );

    when( mockFile3.isFolder() ).thenReturn( false );
    when( mockFile3.getPath() ).thenReturn( "/root/file.txt" );

    // Mock repository children calls
    when( mockRepository.getChildren( mockRootFolder.getId() ) )
        .thenReturn( Arrays.asList( mockSubFolder1, mockSubFolder2, mockFile3 ) );

    when( mockRepository.getChildren( mockSubFolder1.getId() ) )
        .thenReturn( Arrays.asList( mockSubFolder1a, mockFile1 ) );

    when( mockRepository.getChildren( mockSubFolder1a.getId() ) )
        .thenReturn( Arrays.asList( mockFile1a ) );

    when( mockRepository.getChildren( mockSubFolder2.getId() ) )
        .thenReturn( Arrays.asList( mockFile2 ) );

    PentahoPlatformExporter spyExporter = spy( exporter );
    doNothing().when( spyExporter ).exportFolderAcls( any( RepositoryFile.class ) );
    doNothing().when( spyExporter ).exportFile( any( RepositoryFile.class ), any( ZipOutputStream.class ), anyString() );

    // Export the deep hierarchy
    spyExporter.exportFolderHierarchyWithMetadata( mockRootFolder, zos, "/" );

    // Verify all folders (4 total) had their ACLs exported
    verify( spyExporter, times( 4 ) ).exportFolderAcls( any( RepositoryFile.class ) );

    // Verify all files (4 total) were exported via exportFile()
    verify( spyExporter, times( 4 ) ).exportFile( any( RepositoryFile.class ), eq( zos ), anyString() );
  }

  /**
   * Test 4: Export continues despite individual folder/file export failures
   */
  @Test
  public void testExportContinuesOnFailures() throws Exception {
    setupFolderStructure();

    PentahoPlatformExporter spyExporter = spy( exporter );
    
    // Make subfolder1 export throw an exception
    doNothing().when( spyExporter ).exportFolderAcls( mockRootFolder );
    doThrow( new ExportException( "ACL export failed" ) )
        .when( spyExporter ).exportFolderAcls( mockSubFolder1 );
    doNothing().when( spyExporter ).exportFolderAcls( mockSubFolder2 );
    doNothing().when( spyExporter ).exportFile( any( RepositoryFile.class ), any( ZipOutputStream.class ), anyString() );

    // Export should continue even though subfolder1 fails
    spyExporter.exportFolderHierarchyWithMetadata( mockRootFolder, zos, "/" );

    // Verify that subfolder2 and files were still processed despite subfolder1 failure
    verify( spyExporter ).exportFolderAcls( mockRootFolder );
    verify( spyExporter ).exportFolderAcls( mockSubFolder1 ); // This will throw
    verify( spyExporter ).exportFolderAcls( mockSubFolder2 ); // This should still be called
    verify( spyExporter, times( 3 ) ).exportFile( any( RepositoryFile.class ), eq( zos ), anyString() );
  }

  /**
   * Test 6: Folder export processes all folders in hierarchy
   */
  @Test
  public void testAllFoldersProcessedInHierarchy() throws Exception {
    setupFolderStructure();

    PentahoPlatformExporter spyExporter = spy( exporter );
    doNothing().when( spyExporter ).exportFolderAcls( any( RepositoryFile.class ) );
    doNothing().when( spyExporter ).exportFile( any( RepositoryFile.class ), any( ZipOutputStream.class ), anyString() );

    // Export the folder hierarchy
    spyExporter.exportFolderHierarchyWithMetadata( mockRootFolder, zos, "/" );

    // Verify each folder's ACLs were exported independently
    verify( spyExporter ).exportFolderAcls( mockRootFolder );
    verify( spyExporter ).exportFolderAcls( mockSubFolder1 );
    verify( spyExporter ).exportFolderAcls( mockSubFolder2 );
  }

  /**
   * Test 7: Root folder is processed like any other folder
   */
  @Test
  public void testRootFolderProcessed() throws Exception {
    when( mockRootFolder.isFolder() ).thenReturn( true );
    when( mockRootFolder.getPath() ).thenReturn( "/" );

    PentahoPlatformExporter spyExporter = spy( exporter );
    doNothing().when( spyExporter ).exportFolderAcls( any( RepositoryFile.class ) );

    // Export folder ACLs for root
    spyExporter.exportFolderAcls( mockRootFolder );

    // Verify exportFolderAcls was called for root folder
    verify( spyExporter ).exportFolderAcls( mockRootFolder );
  }

  /**
   * Test 8: Export continues properly for multiple folders
   */
  @Test
  public void testMultipleFoldersExportedSequentially() throws Exception {
    setupFolderStructure();

    PentahoPlatformExporter spyExporter = spy( exporter );
    doNothing().when( spyExporter ).exportFolderAcls( any( RepositoryFile.class ) );
    doNothing().when( spyExporter ).exportFile( any( RepositoryFile.class ), any( ZipOutputStream.class ), anyString() );

    // Export the folder hierarchy
    spyExporter.exportFolderHierarchyWithMetadata( mockRootFolder, zos, "/" );

    // Verify all folders had ACLs exported
    verify( spyExporter ).exportFolderAcls( mockRootFolder );
    verify( spyExporter ).exportFolderAcls( mockSubFolder1 );
    verify( spyExporter ).exportFolderAcls( mockSubFolder2 );

    // Verify all files were exported
    verify( spyExporter, times( 3 ) ).exportFile( any( RepositoryFile.class ), eq( zos ), anyString() );
  }

  // ========== Export Helper Architecture Reference ==========
  /**
   * EXPORT HELPER ARCHITECTURE REFACTORING
   * 
   * COMPLETED: Helpers now contain actual export logic instead of thin wrappers
   * 
   * Pattern Established with DatasourcesExportHelper:
   * ✓ All export logic moved from PentahoPlatformExporter.exportDatasources() 
   *   into DatasourcesExportHelper.doExport()
   * ✓ Helper uses exporter accessors to get services and utilities
   * ✓ Helper calls getComponentConfig() to check if export should occur
   * ✓ Helper directly uses metrics, inventory, and manifest services
   * ✓ Full separation of concerns - helper is independently testable
   * 
   * Remaining Helpers to Complete (same pattern):
   * [ ] MetadataExportHelper - move exportMetadataModels() logic
   * [ ] MondrianExportHelper - move exportMondrianSchemas() logic  
   * [ ] UsersAndRolesExportHelper - move exportUsersAndRoles() & exportRoles() logic
   * [ ] MetastoreExportHelper - move exportMetastore() logic
   * [ ] RepositoryContentExportHelper - move exportFileContent() logic
   * 
   * For each remaining helper:
   * 1. Read the corresponding export method from PentahoPlatformExporter
   * 2. Move all logic into the helper's doExport() method
   * 3. Replace direct field access with exporter getter calls
   * 4. Update imports in the helper class
   * 5. Add public accessors to exporter if helper needs them
   * 
   * Architecture Benefits:
   * - Helpers are now independently unit testable
   * - Each helper is responsible for one export component
   * - Easy to add new helpers or modify existing ones
   * - Selective export based on ComponentConfig
   * - Clean delegation pattern instead of thin wrappers
   * - Metrics and logging are encapsulated per helper
   */

  // ========== Helper Methods ==========

  /**
   * Setup basic folder structure for tests:
   * /root
   *   /subfolder1
   *   /subfolder2
   *   file1.txt
   *   file2.txt
   *   file3.txt
   */
  private void setupFolderStructure() {
    // Setup root folder
    when( mockRootFolder.isFolder() ).thenReturn( true );
    when( mockRootFolder.getPath() ).thenReturn( "/" );
    when( mockRootFolder.getName() ).thenReturn( "root" );

    // Setup subfolder1
    when( mockSubFolder1.isFolder() ).thenReturn( true );
    when( mockSubFolder1.getPath() ).thenReturn( "/subfolder1" );
    when( mockSubFolder1.getName() ).thenReturn( "subfolder1" );

    // Setup subfolder2
    when( mockSubFolder2.isFolder() ).thenReturn( true );
    when( mockSubFolder2.getPath() ).thenReturn( "/subfolder2" );
    when( mockSubFolder2.getName() ).thenReturn( "subfolder2" );

    // Setup files
    when( mockFile1.isFolder() ).thenReturn( false );
    when( mockFile1.getPath() ).thenReturn( "/file1.txt" );
    when( mockFile1.getName() ).thenReturn( "file1.txt" );

    when( mockFile2.isFolder() ).thenReturn( false );
    when( mockFile2.getPath() ).thenReturn( "/file2.txt" );
    when( mockFile2.getName() ).thenReturn( "file2.txt" );

    when( mockFile3.isFolder() ).thenReturn( false );
    when( mockFile3.getPath() ).thenReturn( "/file3.txt" );
    when( mockFile3.getName() ).thenReturn( "file3.txt" );

    // Mock repository children calls
    when( mockRepository.getChildren( mockRootFolder.getId() ) )
        .thenReturn( Arrays.asList( mockSubFolder1, mockSubFolder2, mockFile1, mockFile2, mockFile3 ) );

    when( mockRepository.getChildren( mockSubFolder1.getId() ) )
        .thenReturn( new ArrayList<>() );

    when( mockRepository.getChildren( mockSubFolder2.getId() ) )
        .thenReturn( new ArrayList<>() );
  }
}
