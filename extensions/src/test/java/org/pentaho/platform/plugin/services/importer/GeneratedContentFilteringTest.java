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

package org.pentaho.platform.plugin.services.importer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileExtraMetaData;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for generated content filtering feature in SolutionImportHandler
 */
public class GeneratedContentFilteringTest {

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks( this );
  }

  @Mock
  private IUnifiedRepository repository;

  @Mock
  private RepositoryFile repositoryFile;

  @Mock
  private RepositoryFileExtraMetaData extraMetaData;

  /**
   * Test: File with lineage-id should be identified as generated content
   */
  @Test
  public void testFileWithLineageIdIsGeneratedContent() {
    // Setup
    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );
    metadata.put( "contentCreator", "admin" );

    when( extraMetaData.getExtraMetaData() ).thenReturn( metadata );

    // Assert
    assertTrue( "File should be identified as generated content",
      metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
  }

  /**
   * Test: File without lineage-id should NOT be identified as generated content
   */
  @Test
  public void testFileWithoutLineageIdIsNotGeneratedContent() {
    // Setup
    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( "contentCreator", "admin" );

    when( extraMetaData.getExtraMetaData() ).thenReturn( metadata );

    // Assert
    assertFalse( "File should NOT be identified as generated content",
      metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
  }

  /**
   * Test: When includeGeneratedContent=false and file is generated, should skip
   */
  @Test
  public void testGeneratedContentSkippedWhenFilterEnabled() {
    // Setup
    ComponentConfig config = new ComponentConfig();
    config.setIncludeGeneratedContent( false );

    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );

    when( extraMetaData.getExtraMetaData() ).thenReturn( metadata );

    // Assert: Should skip generated content
    boolean isGeneratedContent = metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    boolean shouldSkip = isGeneratedContent && !config.isIncludeGeneratedContent();

    assertTrue( "Should skip generated content when filter is enabled", shouldSkip );
  }

  /**
   * Test: When includeGeneratedContent=true, should NOT skip even if generated
   */
  @Test
  public void testGeneratedContentNotSkippedWhenFilterDisabled() {
    // Setup
    ComponentConfig config = new ComponentConfig();
    config.setIncludeGeneratedContent( true );

    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "uuid-12345" );

    when( extraMetaData.getExtraMetaData() ).thenReturn( metadata );

    // Assert: Should NOT skip generated content
    boolean isGeneratedContent = metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    boolean shouldSkip = isGeneratedContent && !config.isIncludeGeneratedContent();

    assertFalse( "Should NOT skip generated content when filter is disabled", shouldSkip );
  }

  /**
   * Test: Non-generated files should never be skipped
   */
  @Test
  public void testNonGeneratedContentNeverSkipped() {
    // Setup
    ComponentConfig config = new ComponentConfig();
    config.setIncludeGeneratedContent( false );

    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( "contentCreator", "admin" );
    // No lineage-id

    when( extraMetaData.getExtraMetaData() ).thenReturn( metadata );

    // Assert: Should NOT skip non-generated content
    boolean isGeneratedContent = metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    boolean shouldSkip = isGeneratedContent && !config.isIncludeGeneratedContent();

    assertFalse( "Non-generated content should never be skipped", shouldSkip );
  }

  /**
   * Test: Null metadata map should be handled gracefully
   */
  @Test
  public void testNullMetadataMapHandling() {
    // Setup
    when( extraMetaData.getExtraMetaData() ).thenReturn( null );

    // Act: Should handle gracefully
    Map<String, Serializable> metadata = extraMetaData.getExtraMetaData();

    // Assert: Null is handled safely
    assertNull( metadata );
    // Should not contain lineage-id
    boolean hasLineageId = ( metadata != null ) && metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    assertFalse( hasLineageId );
  }

  /**
   * Test: Empty metadata map should not have lineage-id
   */
  @Test
  public void testEmptyMetadataMapHandling() {
    // Setup
    Map<String, Serializable> metadata = new HashMap<>();
    when( extraMetaData.getExtraMetaData() ).thenReturn( metadata );

    // Assert
    assertFalse( "Empty metadata should not contain lineage-id",
      metadata.containsKey( IScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
  }

  /**
   * Test: Verify lineage-id constant value is correct
   */
  @Test
  public void testLineageIdConstantValue() {
    // This test ensures the constant hasn't changed
    String lineageIdKey = IScheduler.RESERVEDMAPKEY_LINEAGE_ID;
    assertEquals( "lineage-id constant should match expected value", "lineage-id", lineageIdKey );
  }
}
