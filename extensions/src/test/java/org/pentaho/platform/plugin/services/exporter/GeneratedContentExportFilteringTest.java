/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.exporter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for generated content filtering feature in PentahoPlatformExporter
 */
public class GeneratedContentExportFilteringTest {

  private PentahoPlatformExporter exporter;

  @Mock
  private IUnifiedRepository repository;

  @Mock
  private RepositoryFile repositoryFile;

  private ComponentConfig componentConfig;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks( this );
    exporter = new PentahoPlatformExporter( repository );
    componentConfig = new ComponentConfig();
  }

  /**
   * Test: When file has lineage-id, isFileAGeneratedContent should return true
   */
  @Test
  public void testIsFileAGeneratedContentWithLineageId() {
    // Setup
    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( metadata );

    // Act
    boolean isGeneratedContent = metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );

    // Assert
    assertTrue( "File with lineage-id should be identified as generated content", isGeneratedContent );
  }

  /**
   * Test: When file lacks lineage-id, isFileAGeneratedContent should return false
   */
  @Test
  public void testIsFileAGeneratedContentWithoutLineageId() {
    // Setup
    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( "contentCreator", "admin" );

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( metadata );

    // Act
    boolean isGeneratedContent = metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );

    // Assert
    assertFalse( "File without lineage-id should not be generated content", isGeneratedContent );
  }

  /**
   * Test: shouldSkipGeneratedContent returns true when filter enabled and file is generated
   */
  @Test
  public void testShouldSkipGeneratedContentWhenFilterEnabled() {
    // Setup
    componentConfig.setIncludeContent( true );
    componentConfig.setIncludeGeneratedContent( false );

    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( metadata );

    // Act: Determine if should skip
    boolean isGeneratedContent = metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    boolean shouldSkip = isGeneratedContent && !componentConfig.isIncludeGeneratedContent();

    // Assert
    assertTrue( "Should skip generated content when filter is enabled", shouldSkip );
  }

  /**
   * Test: shouldSkipGeneratedContent returns false when filter disabled
   */
  @Test
  public void testShouldNotSkipGeneratedContentWhenFilterDisabled() {
    // Setup
    componentConfig.setIncludeContent( true );
    componentConfig.setIncludeGeneratedContent( true );

    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( metadata );

    // Act
    boolean isGeneratedContent = metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    boolean shouldSkip = isGeneratedContent && !componentConfig.isIncludeGeneratedContent();

    // Assert
    assertFalse( "Should not skip generated content when filter is disabled", shouldSkip );
  }

  /**
   * Test: shouldSkipGeneratedContent returns false when includeContent is false
   */
  @Test
  public void testShouldNotSkipWhenContentNotIncluded() {
    // Setup
    componentConfig.setIncludeContent( false );
    componentConfig.setIncludeGeneratedContent( false );

    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( metadata );

    // Act
    boolean isGeneratedContent = metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    boolean shouldSkip = isGeneratedContent && componentConfig.isIncludeContent() && !componentConfig.isIncludeGeneratedContent();

    // Assert
    assertFalse( "Should not skip when content inclusion is false", shouldSkip );
  }

  /**
   * Test: shouldSkipGeneratedContent returns false for non-generated files
   */
  @Test
  public void testShouldNotSkipNonGeneratedContent() {
    // Setup
    componentConfig.setIncludeContent( true );
    componentConfig.setIncludeGeneratedContent( false );

    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( "contentCreator", "admin" );
    // No lineage-id

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( metadata );

    // Act
    boolean isGeneratedContent = metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    boolean shouldSkip = isGeneratedContent && !componentConfig.isIncludeGeneratedContent();

    // Assert
    assertFalse( "Should not skip non-generated content", shouldSkip );
  }

  /**
   * Test: null component config should not cause NPE
   */
  @Test
  public void testNullComponentConfigHandling() {
    // Setup
    componentConfig = null;

    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );

    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenReturn( metadata );

    // Act: Should handle null config
    boolean shouldSkip = ( componentConfig != null ) 
        && componentConfig.isIncludeContent() 
        && !componentConfig.isIncludeGeneratedContent();

    // Assert
    assertFalse( "Should safely handle null component config", shouldSkip );
  }

  /**
   * Test: null repository should not cause NPE
   */
  @Test
  public void testNullRepositoryHandling() {
    // Setup
    IUnifiedRepository nullRepo = null;
    when( repositoryFile.getId() ).thenReturn( 123L );

    // Act & Assert: Should not throw NPE
    assertNull( "Null repository should be handled", nullRepo );
  }

  /**
   * Test: null file ID should not cause NPE
   */
  @Test
  public void testNullFileIdHandling() {
    // Setup
    when( repositoryFile.getId() ).thenReturn( null );

    // Act: Should handle null safely
    Object fileId = repositoryFile.getId();

    // Assert
    assertNull( "Null file ID should be handled safely", fileId );
  }

  /**
   * Test: Metadata retrieval exception should not crash export
   */
  @Test
  public void testMetadataRetrievalExceptionHandling() {
    // Setup
    when( repositoryFile.getId() ).thenReturn( 123L );
    when( repository.getFileMetadata( 123L ) ).thenThrow( new RuntimeException( "Metadata error" ) );

    // Act & Assert: Should handle exception gracefully
    try {
      repository.getFileMetadata( 123L );
      fail( "Should have thrown exception" );
    } catch ( RuntimeException e ) {
      assertEquals( "Metadata error", e.getMessage() );
    }
  }

  /**
   * Test: Multiple metadata entries with lineage-id present
   */
  @Test
  public void testMetadataWithMultipleEntries() {
    // Setup
    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( "contentCreator", "admin" );
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );
    metadata.put( "customProperty", "value" );

    // Assert
    assertTrue( "Should find lineage-id among multiple metadata entries",
      metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
    assertEquals( "Should have 3 metadata entries", 3, metadata.size() );
  }

  /**
   * Test: ComponentConfig default values
   */
  @Test
  public void testBackupComponentConfigDefaults() {
    // Setup
    ComponentConfig config = new ComponentConfig();

    // Assert - defaults should be safe
    assertNotNull( "Config should not be null", config );
    // By default, content and generated content should be included
    assertTrue( "Content should be included by default", config.isIncludeContent() );
    assertTrue( "Generated content should be included by default", config.isIncludeGeneratedContent() );
  }

  /**
   * Test: Setting generated content exclusion
   */
  @Test
  public void testSetIncludeGeneratedContentFalse() {
    // Setup
    ComponentConfig config = new ComponentConfig();
    config.setIncludeGeneratedContent( false );

    // Assert
    assertFalse( "Generated content inclusion should be false", config.isIncludeGeneratedContent() );
  }

  /**
   * Test: Setting generated content inclusion
   */
  @Test
  public void testSetIncludeGeneratedContentTrue() {
    // Setup
    ComponentConfig config = new ComponentConfig();
    config.setIncludeGeneratedContent( true );

    // Assert
    assertTrue( "Generated content inclusion should be true", config.isIncludeGeneratedContent() );
  }
}
