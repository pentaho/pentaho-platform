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
import static org.junit.Assert.*;

/**
 * Unit tests for selective restore with component override functionality
 */
public class SelectiveRestoreComponentOverrideTest {

  private ComponentConfig baseConfig;
  private ComponentConfig overrideConfig;

  @Before
  public void setUp() {
    baseConfig = ComponentConfig.contentOnly();
    overrideConfig = new ComponentConfig();
  }

  /**
   * Test: Override content-only to include datasources
   */
  @Test
  public void testOverrideContentOnlyWithDatasources() {
    // Setup: Start with content-only
    assertFalse( "Content-only should not include datasources initially",
      baseConfig.isIncludeDatasources() );

    // Act: Override to include datasources
    baseConfig.setIncludeDatasources( true );

    // Assert
    assertTrue( "Content should still be included", baseConfig.isIncludeContent() );
    assertTrue( "Datasources should now be included", baseConfig.isIncludeDatasources() );
    assertFalse( "Users should still not be included", baseConfig.isIncludeUsers() );
  }

  /**
   * Test: Override content-only to include users and schedules
   */
  @Test
  public void testOverrideContentOnlyWithMultipleComponents() {
    // Setup
    ComponentConfig override = ComponentConfig.contentOnly();

    // Act: Override to include users and schedules
    override.setIncludeUsers( true );
    override.setIncludeSchedules( true );

    // Assert
    assertTrue( "Content should be included", override.isIncludeContent() );
    assertTrue( "Users should be included", override.isIncludeUsers() );
    assertTrue( "Schedules should be included", override.isIncludeSchedules() );
    assertFalse( "Datasources should not be included", override.isIncludeDatasources() );
  }

  /**
   * Test: Disable component from full system backup
   */
  @Test
  public void testDisableComponentFromFullSystem() {
    // Setup: Start with full system
    ComponentConfig limited = ComponentConfig.fullSystem();
    assertTrue( "Full system should include content", limited.isIncludeContent() );

    // Act: Remove datasources from the backup
    limited.setIncludeDatasources( false );

    // Assert
    assertTrue( "Content should still be included", limited.isIncludeContent() );
    assertTrue( "Users should still be included", limited.isIncludeUsers() );
    assertFalse( "Datasources should be excluded", limited.isIncludeDatasources() );
    assertTrue( "Schedules should still be included", limited.isIncludeSchedules() );
  }

  /**
   * Test: Custom configuration from scratch
   */
  @Test
  public void testCustomConfigurationFromScratch() {
    // Setup: Start with defaults (all included)
    ComponentConfig custom = new ComponentConfig();

    // Act: Configure exactly what we want
    custom.setIncludeContent( true );
    custom.setIncludeUsers( true );
    custom.setIncludeDatasources( false );
    custom.setIncludeMetastore( false );
    custom.setIncludeSchedules( true );
    custom.setIncludeUserSettings( false );
    custom.setIncludeGeneratedContent( false );

    // Assert
    assertTrue( "Content should be included", custom.isIncludeContent() );
    assertTrue( "Users should be included", custom.isIncludeUsers() );
    assertFalse( "Datasources should not be included", custom.isIncludeDatasources() );
    assertFalse( "Metastore should not be included", custom.isIncludeMetastore() );
    assertTrue( "Schedules should be included", custom.isIncludeSchedules() );
    assertFalse( "User settings should not be included", custom.isIncludeUserSettings() );
    assertFalse( "Generated content should not be included", custom.isIncludeGeneratedContent() );
  }

  /**
   * Test: Toggling components on and off
   */
  @Test
  public void testTogglingComponentsOnOff() {
    // Setup
    ComponentConfig toggle = ComponentConfig.contentOnly();

    // Act & Assert: Toggle users
    assertFalse( "Users initially excluded", toggle.isIncludeUsers() );
    toggle.setIncludeUsers( true );
    assertTrue( "Users should be included after toggle", toggle.isIncludeUsers() );
    toggle.setIncludeUsers( false );
    assertFalse( "Users should be excluded after second toggle", toggle.isIncludeUsers() );
  }

  /**
   * Test: Selective restore component strategy
   */
  @Test
  public void testSelectiveRestoreComponentStrategy() {
    // Scenario: Restore content and users, but not system components
    ComponentConfig restore = new ComponentConfig();
    restore.setIncludeContent( true );
    restore.setIncludeUsers( true );
    restore.setIncludeDatasources( false );
    restore.setIncludeMetastore( false );
    restore.setIncludeSchedules( false );
    restore.setIncludeUserSettings( false );

    // Assert the strategy
    assertTrue( "Should restore content", restore.isIncludeContent() );
    assertTrue( "Should restore users", restore.isIncludeUsers() );
    int includedCount = 0;
    if ( restore.isIncludeContent() ) includedCount++;
    if ( restore.isIncludeUsers() ) includedCount++;
    if ( restore.isIncludeDatasources() ) includedCount++;
    if ( restore.isIncludeMetastore() ) includedCount++;
    if ( restore.isIncludeSchedules() ) includedCount++;
    if ( restore.isIncludeUserSettings() ) includedCount++;

    assertEquals( "Should have 2 included components", 2, includedCount );
  }

  /**
   * Test: Building configuration step by step
   */
  @Test
  public void testBuildingConfigStepByStep() {
    // Setup: Start minimal
    ComponentConfig builder = new ComponentConfig();
    builder.setIncludeContent( false );
    builder.setIncludeUsers( false );
    builder.setIncludeDatasources( false );
    builder.setIncludeMetastore( false );
    builder.setIncludeSchedules( false );
    builder.setIncludeUserSettings( false );

    // Act: Build up the configuration
    assertFalse( "Should start with nothing", builder.isIncludeContent() );

    builder.setIncludeContent( true );
    assertTrue( "After step 1", builder.isIncludeContent() );

    builder.setIncludeUsers( true );
    assertTrue( "After step 2", builder.isIncludeUsers() );

    builder.setIncludeSchedules( true );
    assertTrue( "After step 3", builder.isIncludeSchedules() );

    // Assert final state
    assertTrue( "Content should be included", builder.isIncludeContent() );
    assertTrue( "Users should be included", builder.isIncludeUsers() );
    assertTrue( "Schedules should be included", builder.isIncludeSchedules() );
    assertFalse( "Datasources should not be included", builder.isIncludeDatasources() );
  }

  /**
   * Test: Configuration composition
   */
  @Test
  public void testConfigurationComposition() {
    // Setup: Create base configurations
    ComponentConfig base = ComponentConfig.contentOnly();
    ComponentConfig addon = new ComponentConfig();

    // Act: "Compose" them (apply addon settings to base)
    if ( addon.isIncludeUsers() ) {
      base.setIncludeUsers( true );
    }
    if ( addon.isIncludeSchedules() ) {
      base.setIncludeSchedules( true );
    }

    // Assert
    assertTrue( "Base content preserved", base.isIncludeContent() );
    // addon has defaults, so this would include users and schedules
    assertTrue( "Addon users included", base.isIncludeUsers() );
    assertTrue( "Addon schedules included", base.isIncludeSchedules() );
  }

  /**
   * Test: Exclusive component groups
   */
  @Test
  public void testExclusiveComponentGroups() {
    // Scenario: Restore either users OR datasources, but not both
    ComponentConfig userOnly = new ComponentConfig();
    userOnly.setIncludeUsers( true );
    userOnly.setIncludeDatasources( false );

    ComponentConfig datasourceOnly = new ComponentConfig();
    datasourceOnly.setIncludeUsers( false );
    datasourceOnly.setIncludeDatasources( true );

    // Assert they are mutually exclusive in these configs
    assertTrue( "User config includes users", userOnly.isIncludeUsers() );
    assertFalse( "User config excludes datasources", userOnly.isIncludeDatasources() );

    assertFalse( "Datasource config excludes users", datasourceOnly.isIncludeUsers() );
    assertTrue( "Datasource config includes datasources", datasourceOnly.isIncludeDatasources() );
  }

  /**
   * Test: Configuration copy/clone behavior
   */
  @Test
  public void testConfigurationCopyBehavior() {
    // Setup: Create source config
    ComponentConfig source = ComponentConfig.securityOnly();

    // Act: Create new config and copy settings
    ComponentConfig copy = new ComponentConfig();
    copy.setIncludeContent( source.isIncludeContent() );
    copy.setIncludeUsers( source.isIncludeUsers() );
    copy.setIncludeDatasources( source.isIncludeDatasources() );
    copy.setIncludeMetastore( source.isIncludeMetastore() );
    copy.setIncludeSchedules( source.isIncludeSchedules() );
    copy.setIncludeUserSettings( source.isIncludeUserSettings() );

    // Assert: Copy should match source
    assertEquals( "Content should match", source.isIncludeContent(), copy.isIncludeContent() );
    assertEquals( "Users should match", source.isIncludeUsers(), copy.isIncludeUsers() );
    assertEquals( "Datasources should match", source.isIncludeDatasources(), copy.isIncludeDatasources() );
  }

  /**
   * Test: Component count validation
   */
  @Test
  public void testComponentCountValidation() {
    // Setup
    ComponentConfig config = new ComponentConfig();
    config.setIncludeContent( true );
    config.setIncludeUsers( true );
    config.setIncludeDatasources( false );

    // Act: Count included components
    int includedCount = 0;
    if ( config.isIncludeContent() ) includedCount++;
    if ( config.isIncludeUsers() ) includedCount++;
    if ( config.isIncludeDatasources() ) includedCount++;
    if ( config.isIncludeMetastore() ) includedCount++;
    if ( config.isIncludeSchedules() ) includedCount++;
    if ( config.isIncludeUserSettings() ) includedCount++;

    // Assert
    assertEquals( "Should have 5 included components (content, users, metastore, schedules, settings by default)", 5, includedCount );
  }
}
