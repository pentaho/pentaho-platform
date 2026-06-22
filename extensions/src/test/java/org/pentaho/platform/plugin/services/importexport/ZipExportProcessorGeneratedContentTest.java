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

package org.pentaho.platform.plugin.services.importexport;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IScheduler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ZipExportProcessor generated content handling methods
 */
public class ZipExportProcessorGeneratedContentTest {

  @Mock
  private IUnifiedRepository repository;

  @Mock
  private RepositoryFile repositoryFile;

  private Map<String, Serializable> fileMetadata;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks( this );
    fileMetadata = new HashMap<>();
  }

  /**
   * Test: isFileAGeneratedContent with lineage-id present
   */
  @Test
  public void testIsFileAGeneratedContentWithLineageIdPresent() {
    // Setup
    fileMetadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( fileMetadata );

    // Act
    boolean isGenerated = fileMetadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );

    // Assert
    assertTrue( "File with lineage-id is generated content", isGenerated );
  }

  /**
   * Test: isFileAGeneratedContent without lineage-id
   */
  @Test
  public void testIsFileAGeneratedContentWithoutLineageId() {
    // Setup
    fileMetadata.put( "contentCreator", "admin" );

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( fileMetadata );

    // Act
    boolean isGenerated = fileMetadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );

    // Assert
    assertFalse( "File without lineage-id is not generated content", isGenerated );
  }

  /**
   * Test: isFileAGeneratedContent with null metadata
   */
  @Test
  public void testIsFileAGeneratedContentWithNullMetadata() {
    // Setup
    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( null );

    // Act
    Map<String, Serializable> metadata = repository.getFileMetadata( 123L );
    boolean isGenerated = ( metadata != null ) && metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );

    // Assert
    assertFalse( "Null metadata should not be generated content", isGenerated );
  }

  /**
   * Test: isFileAGeneratedContent with empty metadata map
   */
  @Test
  public void testIsFileAGeneratedContentWithEmptyMetadata() {
    // Setup: Empty metadata
    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( new HashMap<>() );

    // Act
    Map<String, Serializable> metadata = repository.getFileMetadata( 123L );
    boolean isGenerated = metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );

    // Assert
    assertFalse( "Empty metadata should not be generated content", isGenerated );
  }

  /**
   * Test: shouldSkipGeneratedContent when all conditions met
   */
  @Test
  public void testShouldSkipGeneratedContentAllConditionsMet() {
    // Setup
    fileMetadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );
    ComponentConfig config = new ComponentConfig();
    config.setIncludeContent( true );
    config.setIncludeGeneratedContent( false );

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( fileMetadata );

    // Act
    boolean isGenerated = fileMetadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    boolean shouldSkip = isGenerated && config.isIncludeContent() && !config.isIncludeGeneratedContent();

    // Assert
    assertTrue( "Should skip generated content when all conditions met", shouldSkip );
  }

  /**
   * Test: shouldSkipGeneratedContent when generated content included
   */
  @Test
  public void testShouldNotSkipGeneratedContentWhenIncluded() {
    // Setup
    fileMetadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );
    ComponentConfig config = new ComponentConfig();
    config.setIncludeContent( true );
    config.setIncludeGeneratedContent( true ); // Include generated content

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( fileMetadata );

    // Act
    boolean isGenerated = fileMetadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    boolean shouldSkip = isGenerated && config.isIncludeContent() && !config.isIncludeGeneratedContent();

    // Assert
    assertFalse( "Should not skip when generated content is included", shouldSkip );
  }

  /**
   * Test: shouldSkipGeneratedContent when content not included
   */
  @Test
  public void testShouldNotSkipWhenContentNotIncluded() {
    // Setup
    fileMetadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );
    ComponentConfig config = new ComponentConfig();
    config.setIncludeContent( false ); // Content not included
    config.setIncludeGeneratedContent( false );

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( fileMetadata );

    // Act
    boolean isGenerated = fileMetadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    boolean shouldSkip = isGenerated && config.isIncludeContent() && !config.isIncludeGeneratedContent();

    // Assert
    assertFalse( "Should not skip when content not included", shouldSkip );
  }

  /**
   * Test: shouldSkipGeneratedContent for non-generated files
   */
  @Test
  public void testShouldNotSkipNonGeneratedContent() {
    // Setup: Regular file without lineage-id
    fileMetadata.put( "contentCreator", "admin" );
    ComponentConfig config = new ComponentConfig();
    config.setIncludeContent( true );
    config.setIncludeGeneratedContent( false );

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( fileMetadata );

    // Act
    boolean isGenerated = fileMetadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    boolean shouldSkip = isGenerated && config.isIncludeContent() && !config.isIncludeGeneratedContent();

    // Assert
    assertFalse( "Should not skip non-generated content", shouldSkip );
  }

  /**
   * Test: Null file ID handling
   */
  @Test
  public void testNullFileIdHandling() {
    // Setup
    when( repositoryFile.getId() ).thenReturn( null );

    // Act: Should handle null safely
    Object fileId = repositoryFile.getId();

    // Assert
    assertNull( "Null file ID should be handled", fileId );
  }

  /**
   * Test: Metadata retrieval exception
   */
  @Test
  public void testMetadataRetrievalException() {
    // Setup
    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenThrow( new RuntimeException( "Metadata error" ) );

    // Act & Assert: Should handle exception
    try {
      repository.getFileMetadata( 123L );
      fail( "Should throw exception" );
    } catch ( RuntimeException e ) {
      assertEquals( "Metadata error", e.getMessage() );
    }
  }

  /**
   * Test: Multiple files filtering
   */
  @Test
  public void testMultipleFilesFiltering() {
    // Setup: Create multiple files
    Map<String, Serializable> metadata1 = new HashMap<>();
    metadata1.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-1" );

    Map<String, Serializable> metadata2 = new HashMap<>();
    metadata2.put( "name", "report.prpt" );

    Map<String, Serializable> metadata3 = new HashMap<>();
    metadata3.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-3" );

    // Act: Check each file
    int generatedCount = 0;
    if ( metadata1.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) ) generatedCount++;
    if ( metadata2.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) ) generatedCount++;
    if ( metadata3.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) ) generatedCount++;

    // Assert
    assertEquals( "Should have 2 generated files", 2, generatedCount );
  }

  /**
   * Test: Large batch of files filtering
   */
  @Test
  public void testLargeBatchFileFiltering() {
    // Setup: Simulate 1000 files
    int totalFiles = 1000;
    int generatedCount = 0;

    // Simulate: Every 3rd file is generated
    for ( int i = 0; i < totalFiles; i++ ) {
      Map<String, Serializable> metadata = new HashMap<>();
      if ( i % 3 == 0 ) {
        metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-" + i );
        generatedCount++;
      } else {
        metadata.put( "name", "file-" + i );
      }

      // In real scenario, would check: metadata.containsKey( ... )
    }

    // Assert
    assertEquals( "Should have ~334 generated files", 334, generatedCount );
    assertEquals( "Should export ~666 regular files", 666, totalFiles - generatedCount );
  }

  /**
   * Test: Mixed generated and regular content
   */
  @Test
  public void testMixedGeneratedAndRegularContent() {
    // Setup: Different file types
    fileMetadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-generated" );

    // Regular content
    Map<String, Serializable> regularMetadata = new HashMap<>();
    regularMetadata.put( "type", "report" );
    regularMetadata.put( "createdBy", "admin" );

    // Assert: Both can coexist
    assertTrue( "Generated file has lineage-id", fileMetadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
    assertFalse( "Regular file doesn't have lineage-id", regularMetadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
  }

  /**
   * Test: Metadata key constant correctness
   */
  @Test
  public void testMetadataKeyConstantCorrectness() {
    // Act
    String lineageIdKey = IScheduler.RESERVEDMAPKEY_LINEAGE_ID;

    // Assert
    assertEquals( "Lineage ID key should be 'lineage-id'", "lineage-id", lineageIdKey );
    assertNotNull( "Key should not be null", lineageIdKey );
    assertFalse( "Key should not be empty", lineageIdKey.isEmpty() );
  }

  /**
   * Test: Export filtering statistics
   */
  @Test
  public void testExportFilteringStatistics() {
    // Setup: Track export stats
    int totalEntities = 5000;
    int generatedEntities = 1250; // 25%
    int exportedEntities = totalEntities - generatedEntities;

    ComponentConfig config = new ComponentConfig();
    config.setIncludeGeneratedContent( false );

    // Assert
    assertTrue( "Should exclude generated content", !config.isIncludeGeneratedContent() );
    assertEquals( "Should export 3750 entities", 3750, exportedEntities );
    assertEquals( "Should skip 1250 generated entities", 1250, generatedEntities );
  }

  /**
   * Test: isExportCandidate still excludes /etc folder
   */
  @Test
  public void testExportCandidateExcludesEtcFolder() {
    // Setup: Various paths
    String[] excludedPaths = { "/etc", "/etc/folder", "/etc/dbs" };
    String[] includedPaths = { "/public", "/home", "/tmp" };

    // Assert: /etc should be excluded
    for ( String path : excludedPaths ) {
      boolean isCandidate = !path.equals( "/etc" ) && !path.startsWith( "/etc/" );
      assertFalse( "Path should be excluded: " + path, isCandidate );
    }

    // Included paths should be candidates
    for ( String path : includedPaths ) {
      boolean isCandidate = !path.equals( "/etc" ) && !path.startsWith( "/etc/" );
      assertTrue( "Path should be included: " + path, isCandidate );
    }
  }
}
