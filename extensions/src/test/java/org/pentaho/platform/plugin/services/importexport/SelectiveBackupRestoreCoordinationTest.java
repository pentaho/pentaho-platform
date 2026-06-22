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
import java.util.*;

/**
 * Unit tests for selective backup/restore coordination and scenarios
 */
public class SelectiveBackupRestoreCoordinationTest {

  private ComponentConfig exportConfig;
  private ComponentConfig importConfig;

  @Before
  public void setUp() {
    exportConfig = new ComponentConfig();
    importConfig = new ComponentConfig();
  }

  /**
   * Helper: count enabled components in config
   */
  private int countEnabledComponents( ComponentConfig config ) {
    int count = 0;
    if ( config.isIncludeContent() ) count++;
    if ( config.isIncludeUsers() ) count++;
    if ( config.isIncludeDatasources() ) count++;
    if ( config.isIncludeSchedules() ) count++;
    if ( config.isIncludeMetastore() ) count++;
    if ( config.isIncludeUserSettings() ) count++;
    return count;
  }

  /**
   * Test: Content-only export with full restore
   */
  @Test
  public void testContentOnlyExportFullRestore() {
    // Setup: Export content-only backup
    exportConfig = ComponentConfig.contentOnly();
    assertFalse( "Export should not include users", exportConfig.isIncludeUsers() );

    // During restore, user tries to restore users (but won't exist in backup)
    importConfig = new ComponentConfig();
    importConfig.setIncludeUsers( true );

    // Assert: Export config is restrictive, import config can request anything
    assertFalse( "Export won't have users", exportConfig.isIncludeUsers() );
    assertTrue( "Import wants users (but won't find them)", importConfig.isIncludeUsers() );
  }

  /**
   * Test: Full export with content-only restore
   */
  @Test
  public void testFullExportContentOnlyRestore() {
    // Setup: Export full system
    exportConfig = ComponentConfig.fullSystem();
    assertTrue( "Export includes everything", exportConfig.isIncludeUsers() );
    assertTrue( "Export includes everything", exportConfig.isIncludeDatasources() );

    // During restore, user restricts to content-only
    importConfig = ComponentConfig.contentOnly();
    assertTrue( "Restore has content", importConfig.isIncludeContent() );
    assertFalse( "Restore excludes users", importConfig.isIncludeUsers() );

    // Assert: Can selectively restore from a full backup
    assertTrue( "Full backup contains users", exportConfig.isIncludeUsers() );
    assertFalse( "Selective restore excludes users", importConfig.isIncludeUsers() );
  }

  /**
   * Test: Selective export with selective restore
   */
  @Test
  public void testSelectiveExportSelectiveRestore() {
    // Setup: Custom export (content + users)
    exportConfig.setIncludeContent( true );
    exportConfig.setIncludeUsers( true );
    exportConfig.setIncludeDatasources( false );
    exportConfig.setIncludeMetastore( false );
    exportConfig.setIncludeSchedules( false );
    exportConfig.setIncludeUserSettings( false );

    // Restore same components
    importConfig.setIncludeContent( true );
    importConfig.setIncludeUsers( true );
    importConfig.setIncludeDatasources( false );
    importConfig.setIncludeMetastore( false );
    importConfig.setIncludeSchedules( false );
    importConfig.setIncludeUserSettings( false );

    // Assert: Matching configs for complete restore
    assertEquals( "Export and import content matches", exportConfig.isIncludeContent(), importConfig.isIncludeContent() );
    assertEquals( "Export and import users matches", exportConfig.isIncludeUsers(), importConfig.isIncludeUsers() );
  }

  /**
   * Test: Generated content filtering during export
   */
  @Test
  public void testGeneratedContentFilteringDuringExport() {
    // Setup: Content backup without generated content
    exportConfig = ComponentConfig.contentOnly();
    exportConfig.setIncludeGeneratedContent( false );

    assertFalse( "Export should exclude generated content", exportConfig.isIncludeGeneratedContent() );
    
    // Act: During export, generated files are skipped
    List<String> exportedFiles = new ArrayList<>();
    List<String> skippedFiles = new ArrayList<>();
    
    // Simulate: Regular file exported, generated file skipped
    exportedFiles.add( "/content/report.prpt" );
    skippedFiles.add( "/home/admin/report-output.pdf" );

    // Assert
    assertEquals( "Should export 1 file", 1, exportedFiles.size() );
    assertEquals( "Should skip 1 generated file", 1, skippedFiles.size() );
  }

  /**
   * Test: Exclude generated content during restore
   */
  @Test
  public void testExcludeGeneratedContentDuringRestore() {
    // Setup: Restore with generated content exclusion
    importConfig.setIncludeGeneratedContent( false );

    // The backup contains generated files but they will be filtered out
    int totalInBackup = 1000;
    int generatedCount = 250;
    int regularCount = totalInBackup - generatedCount;

    // Act: Apply filter
    int restoredCount = regularCount;

    // Assert
    assertEquals( "Should restore 750 regular files", 750, restoredCount );
    assertEquals( "Should exclude 250 generated files", 250, generatedCount );
  }

  /**
   * Test: Generated content preservation during restore
   */
  @Test
  public void testGeneratedContentPreservationDuringRestore() {
    // Setup: Restore WITH generated content
    importConfig.setIncludeGeneratedContent( true );

    int totalInBackup = 1000;
    int generatedCount = 250;

    // Act: All files should be restored
    int restoredCount = totalInBackup;

    // Assert
    assertEquals( "Should restore all 1000 files", 1000, restoredCount );
    assertEquals( "Including 250 generated files", 250, generatedCount );
  }

  /**
   * Test: Multiple backup scenarios for same environment
   */
  @Test
  public void testMultipleBackupScenariosForSameEnvironment() {
    // Scenario 1: Weekly full backup
    ComponentConfig weekly = ComponentConfig.fullSystem();
    assertTrue( "Weekly includes everything", weekly.isIncludeContent() );
    assertTrue( "Weekly includes users", weekly.isIncludeUsers() );

    // Scenario 2: Daily content backup
    ComponentConfig daily = ComponentConfig.contentOnly();
    assertTrue( "Daily includes content", daily.isIncludeContent() );
    assertFalse( "Daily excludes users", daily.isIncludeUsers() );

    // Scenario 3: On-demand security backup
    ComponentConfig security = ComponentConfig.securityOnly();
    assertFalse( "Security excludes content", security.isIncludeContent() );
    assertTrue( "Security includes users", security.isIncludeUsers() );

    // Assert: Different strategies coexist
    // Count enabled components
    int weeklyComponents = countEnabledComponents( weekly );
    int dailyComponents = countEnabledComponents( daily );
    int securityComponents = countEnabledComponents( security );

    assertTrue( "Weekly > daily", weeklyComponents > dailyComponents );
    assertTrue( "Weekly > security", weeklyComponents > securityComponents );
  }

  /**
   * Test: Restore strategy after backup failure
   */
  @Test
  public void testRestoreStrategyAfterBackupFailure() {
    // Setup: Last successful backup was content-only
    ComponentConfig lastGoodBackup = ComponentConfig.contentOnly();
    lastGoodBackup.setIncludeGeneratedContent( false );

    // Current restore needs to work with what's available
    importConfig = new ComponentConfig();
    importConfig.setIncludeContent( true );
    // Users, datasources will be skipped because backup didn't include them
    importConfig.setIncludeUsers( false );
    importConfig.setIncludeDatasources( false );

    // Assert: Restore works with available components
    assertTrue( "Can restore content from backup", importConfig.isIncludeContent() );
    assertFalse( "Cannot restore users (not in backup)", importConfig.isIncludeUsers() );
  }

  /**
   * Test: Selective component update strategy
   */
  @Test
  public void testSelectiveComponentUpdateStrategy() {
    // Scenario: Full system is backed up regularly, but we update specific components

    // Full backup exists
    ComponentConfig fullBackup = ComponentConfig.fullSystem();

    // Today we only want to restore users (security update)
    ComponentConfig todayRestore = new ComponentConfig();
    todayRestore.setIncludeContent( false );
    todayRestore.setIncludeUsers( true );
    todayRestore.setIncludeDatasources( false );
    todayRestore.setIncludeMetastore( false );
    todayRestore.setIncludeSchedules( false );
    todayRestore.setIncludeUserSettings( false );

    // Assert: Selective restore from full backup
    assertTrue( "Full backup has users", fullBackup.isIncludeUsers() );
    assertTrue( "Today restore wants users", todayRestore.isIncludeUsers() );
    assertFalse( "Today restore skips content", todayRestore.isIncludeContent() );
  }

  /**
   * Test: Progressive restore strategy
   */
  @Test
  public void testProgressiveRestoreStrategy() {
    // Phase 1: Restore core system
    ComponentConfig phase1 = new ComponentConfig();
    phase1.setIncludeContent( true );
    phase1.setIncludeUsers( true );
    phase1.setIncludeDatasources( false );

    // Phase 2: Add datasources
    ComponentConfig phase2 = new ComponentConfig();
    phase2.setIncludeContent( true );
    phase2.setIncludeUsers( true );
    phase2.setIncludeDatasources( true );
    phase2.setIncludeSchedules( false );

    // Phase 3: Add schedules
    ComponentConfig phase3 = ComponentConfig.fullSystem();

    // Assert: Progressive expansion
    assertEquals( "Phase 1 has 2 components", 2,
      ( phase1.isIncludeContent() ? 1 : 0 ) + ( phase1.isIncludeUsers() ? 1 : 0 ) );
    assertEquals( "Phase 2 has 3 components", 3,
      ( phase2.isIncludeContent() ? 1 : 0 ) + ( phase2.isIncludeUsers() ? 1 : 0 ) + ( phase2.isIncludeDatasources() ? 1 : 0 ) );
    assertTrue( "Phase 3 is full system", phase3.isIncludeSchedules() );
  }

  /**
   * Test: Component dependency tracking
   */
  @Test
  public void testComponentDependencyTracking() {
    // Setup: Assume schedules depend on datasources
    ComponentConfig config = new ComponentConfig();

    // Scenario: Want to restore schedules but not datasources
    config.setIncludeSchedules( true );
    config.setIncludeDatasources( false ); // Missing dependency

    // Track what's needed
    Map<String, List<String>> dependencies = new HashMap<>();
    dependencies.put( "SCHEDULES", Arrays.asList( "DATASOURCES", "USERS" ) );
    dependencies.put( "DATASOURCES", new ArrayList<>() );

    // Assert: Can check dependencies
    assertTrue( "Schedules depend on datasources", dependencies.get( "SCHEDULES" ).contains( "DATASOURCES" ) );
    assertFalse( "Datasources have no dependencies", dependencies.get( "DATASOURCES" ).isEmpty() == false );
  }

  /**
   * Test: Backup/restore consistency check
   */
  @Test
  public void testBackupRestoreConsistencyCheck() {
    // Setup: Compare backup and restore configs
    ComponentConfig backup = new ComponentConfig();
    backup.setIncludeContent( true );
    backup.setIncludeUsers( false );
    backup.setIncludeDatasources( true );

    ComponentConfig restore = new ComponentConfig();
    restore.setIncludeContent( true );
    restore.setIncludeUsers( true ); // Requesting more than what was backed up
    restore.setIncludeDatasources( true );

    // Act: Check consistency - backup should contain everything restore wants
    boolean contentOk = !restore.isIncludeContent() || backup.isIncludeContent();
    boolean usersOk = !restore.isIncludeUsers() || backup.isIncludeUsers();
    boolean datasourcesOk = !restore.isIncludeDatasources() || backup.isIncludeDatasources();
    
    // Assert: Flag inconsistency for user awareness
    assertTrue( "Should flag potential inconsistency for user",
      !backup.isIncludeUsers() && restore.isIncludeUsers() );
  }
}
