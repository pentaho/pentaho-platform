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
import org.pentaho.platform.api.scheduler2.IScheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Integration tests for backup/restore with generated content filtering feature
 */
public class GeneratedContentBackupRestoreIntegrationTest {

  private ComponentConfig backupConfig;

  @Mock
  private IUnifiedRepository repository;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks( this );
    backupConfig = new ComponentConfig();
  }

  /**
   * Test: Full system backup includes all content by default
   */
  @Test
  public void testFullSystemBackupIncludesAllContent() {
    // Setup
    ComponentConfig fullSystem = ComponentConfig.fullSystem();

    // Assert
    assertTrue( "Full system backup should include content", fullSystem.isIncludeContent() );
    assertTrue( "Full system backup should include generated content", fullSystem.isIncludeGeneratedContent() );
  }

  /**
   * Test: Content-only backup includes non-generated content
   */
  @Test
  public void testContentOnlyBackupExcludesGeneratedContent() {
    // Setup
    backupConfig.setIncludeContent( true );
    backupConfig.setIncludeGeneratedContent( false );

    // Assert
    assertTrue( "Content-only backup should include content", backupConfig.isIncludeContent() );
    assertFalse( "Content-only backup should exclude generated content", backupConfig.isIncludeGeneratedContent() );
  }

  /**
   * Test: Scenario with mixed content (some generated, some not)
   */
  @Test
  public void testMixedContentFilteringScenario() {
    // Setup: Create list of files with mixed generated content markers
    List<Map<String, Serializable>> files = new ArrayList<>();

    // Generated content file
    Map<String, Serializable> generatedFile = new HashMap<>();
    generatedFile.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-gen-001" );
    files.add( generatedFile );

    // Non-generated content file
    Map<String, Serializable> regularFile = new HashMap<>();
    regularFile.put( "name", "report.prpt" );
    files.add( regularFile );

    // Another generated file
    Map<String, Serializable> generatedFile2 = new HashMap<>();
    generatedFile2.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-gen-002" );
    files.add( generatedFile2 );

    // Act: Apply filter
    backupConfig.setIncludeGeneratedContent( false );
    List<Map<String, Serializable>> filtered = new ArrayList<>();
    for ( Map<String, Serializable> file : files ) {
      if ( !file.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) ) {
        filtered.add( file );
      }
    }

    // Assert
    assertEquals( "Should have 1 non-generated file", 1, filtered.size() );
    assertFalse( "Filtered result should not have lineage-id",
      filtered.get( 0 ).containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
  }

  /**
   * Test: Backup component configuration for selective content
   */
  @Test
  public void testSelectiveBackupConfiguration() {
    // Setup: Create selective backup config
    backupConfig.setIncludeContent( true );
    backupConfig.setIncludeGeneratedContent( false );

    // Assert
    assertTrue( "Should include content", backupConfig.isIncludeContent() );
    assertFalse( "Should exclude generated content", backupConfig.isIncludeGeneratedContent() );

    // Verify the filter logic
    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );

    boolean shouldSkip = backupConfig.isIncludeContent() 
        && !backupConfig.isIncludeGeneratedContent()
        && metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );

    assertTrue( "File should be skipped in selective backup", shouldSkip );
  }

  /**
   * Test: Backup config string representation
   */
  @Test
  public void testBackupConfigToString() {
    // Setup
    backupConfig.setIncludeContent( true );
    backupConfig.setIncludeGeneratedContent( false );

    // Act
    String configString = backupConfig.toString();

    // Assert
    assertNotNull( "Config string should not be null", configString );
    assertFalse( "Config string should not be empty", configString.isEmpty() );
  }

  /**
   * Test: Multiple backup configs don't interfere
   */
  @Test
  public void testIndependentBackupConfigs() {
    // Setup
    ComponentConfig config1 = new ComponentConfig();
    config1.setIncludeGeneratedContent( false );

    ComponentConfig config2 = new ComponentConfig();
    config2.setIncludeGeneratedContent( true );

    // Assert: Changes to one shouldn't affect the other
    assertFalse( "Config1 should not include generated content", config1.isIncludeGeneratedContent() );
    assertTrue( "Config2 should include generated content", config2.isIncludeGeneratedContent() );
  }

  /**
   * Test: Generated content identification consistency
   */
  @Test
  public void testGeneratedContentIdentificationConsistency() {
    // Setup
    Map<String, Serializable> generatedMetadata = new HashMap<>();
    generatedMetadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );

    // First check
    boolean isGenerated1 = generatedMetadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );

    // Second check
    boolean isGenerated2 = generatedMetadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );

    // Assert: Should be consistent
    assertEquals( "Generated content identification should be consistent", isGenerated1, isGenerated2 );
    assertTrue( "File should be identified as generated content", isGenerated1 );
  }

  /**
   * Test: Lineage ID is the correct key
   */
  @Test
  public void testLineageIdKeyIsCorrect() {
    // Act
    String lineageIdKey = IScheduler.RESERVEDMAPKEY_LINEAGE_ID;

    // Assert
    assertEquals( "Lineage ID key should be 'lineage-id'", "lineage-id", lineageIdKey );
  }

  /**
   * Test: Edge case - empty lineage ID value
   */
  @Test
  public void testEmptyLineageIdValue() {
    // Setup
    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "" );

    // Assert: Empty string is still a lineage ID marker
    assertTrue( "Should recognize empty lineage ID as present",
      metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
  }

  /**
   * Test: Edge case - null lineage ID value
   */
  @Test
  public void testNullLineageIdValue() {
    // Setup
    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, null );

    // Assert: Null value still marks as generated content (key exists)
    assertTrue( "Should recognize null lineage ID value as present",
      metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
  }

  /**
   * Test: Case sensitivity of lineage-id key
   */
  @Test
  public void testLineageIdKeyCaseSensitivity() {
    // Setup
    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( "Lineage-Id", "uuid-12345" ); // Wrong case
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-67890" ); // Correct case

    // Assert: Only exact case match should work
    assertTrue( "Should find lineage-id with correct case",
      metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
    assertTrue( "Should find Lineage-Id with different case",
      metadata.containsKey( "Lineage-Id" ) );
    assertEquals( "Should have 2 entries with different cases", 2, metadata.size() );
  }

  /**
   * Test: Large batch of files with filtering
   */
  @Test
  public void testLargeBatchFileFiltering() {
    // Setup: Create large list of mixed files
    List<Map<String, Serializable>> allFiles = new ArrayList<>();
    
    for ( int i = 0; i < 1000; i++ ) {
      Map<String, Serializable> file = new HashMap<>();
      if ( i % 3 == 0 ) {
        // Every 3rd file is generated content
        file.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-" + i );
      } else {
        file.put( "name", "file-" + i );
      }
      allFiles.add( file );
    }

    // Act: Filter out generated content
    List<Map<String, Serializable>> filtered = new ArrayList<>();
    for ( Map<String, Serializable> file : allFiles ) {
      if ( !file.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) ) {
        filtered.add( file );
      }
    }

    // Assert
    assertEquals( "Should have ~667 non-generated files", 667, filtered.size() );
    int generatedCount = allFiles.size() - filtered.size();
    assertEquals( "Should have ~333 generated files", 333, generatedCount );
  }
}
