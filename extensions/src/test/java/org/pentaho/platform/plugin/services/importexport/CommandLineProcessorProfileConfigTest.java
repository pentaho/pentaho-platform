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

package org.pentaho.platform.plugin.services.importexport;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Focused tests for the profile-to-{@link ComponentConfig} wiring inside
 * {@link CommandLineProcessor}: that {@code --backup-profile} / {@code --restore-profile}
 * map to the correct components, that an unknown profile yields no config, and that a
 * restore falls back to {@code --backup-profile} when {@code --restore-profile} is absent.
 *
 * <p>These exercise the real private builders ({@code buildBackupComponentConfig} /
 * {@code buildRestoreComponentConfig}) via reflection, complementing the existing tests
 * that only validate the {@link ComponentConfig} factory methods in isolation.</p>
 */
public class CommandLineProcessorProfileConfigTest {

  private ComponentConfig backupConfig( String... args ) throws Exception {
    CommandLineProcessor clp = new CommandLineProcessor( args );
    Method m = CommandLineProcessor.class.getDeclaredMethod( "buildBackupComponentConfig" );
    m.setAccessible( true );
    return (ComponentConfig) m.invoke( clp );
  }

  private ComponentConfig restoreConfig( String... args ) throws Exception {
    CommandLineProcessor clp = new CommandLineProcessor( args );
    Method m = CommandLineProcessor.class.getDeclaredMethod( "buildRestoreComponentConfig" );
    m.setAccessible( true );
    return (ComponentConfig) m.invoke( clp );
  }

  // ---------------------------------------------------------------------------
  // Backup profile mapping
  // ---------------------------------------------------------------------------

  @Test
  public void testBackupProfileFullSystemIncludesEverything() throws Exception {
    ComponentConfig config = backupConfig( "--backup", "--backup-profile=FULL_SYSTEM" );

    assertTrue( config.isIncludeContent() );
    assertTrue( config.isIncludeUsers() );
    assertTrue( config.isIncludeDatasources() );
    assertTrue( config.isIncludeMetastore() );
    assertTrue( config.isIncludeSchedules() );
    assertTrue( config.isIncludeUserSettings() );
    assertTrue( config.isIncludeMondrian() );
    assertEquals( 7, config.getComponentCount() );
  }

  @Test
  public void testBackupProfileContentOnly() throws Exception {
    ComponentConfig config = backupConfig( "--backup", "--backup-profile=CONTENT_ONLY" );

    assertTrue( config.isIncludeContent() );
    assertFalse( config.isIncludeUsers() );
    assertFalse( config.isIncludeDatasources() );
    assertFalse( config.isIncludeSchedules() );
    assertTrue( config.isIncludeGeneratedContent() ); // included by default for this profile
    assertEquals( 1, config.getComponentCount() );
  }

  @Test
  public void testBackupProfileSecurityMapsToUsersOnly() throws Exception {
    ComponentConfig config = backupConfig( "--backup", "--backup-profile=SECURITY" );

    assertTrue( config.isIncludeUsers() );
    assertFalse( config.isIncludeContent() );
    assertFalse( config.isIncludeDatasources() );
    assertFalse( config.isIncludeSchedules() );
    assertTrue( config.isValid() );
    assertEquals( 1, config.getComponentCount() );
  }

  @Test
  public void testBackupProfileDataSource() throws Exception {
    ComponentConfig config = backupConfig( "--backup", "--backup-profile=DATA_SOURCE" );

    assertTrue( config.isIncludeDatasources() );
    assertTrue( config.isIncludeMetastore() );
    assertTrue( config.isIncludeMondrian() );
    assertFalse( config.isIncludeContent() );
    assertFalse( config.isIncludeUsers() );
    assertEquals( 3, config.getComponentCount() );
  }

  @Test
  public void testBackupProfileSchedules() throws Exception {
    ComponentConfig config = backupConfig( "--backup", "--backup-profile=SCHEDULES" );

    assertTrue( config.isIncludeSchedules() );
    assertFalse( config.isIncludeContent() );
    assertFalse( config.isIncludeUsers() );
    assertFalse( config.isIncludeUserSettings() );
    assertEquals( 1, config.getComponentCount() );
  }

  @Test
  public void testBackupProfileSettings() throws Exception {
    ComponentConfig config = backupConfig( "--backup", "--backup-profile=SETTINGS" );

    assertTrue( config.isIncludeUserSettings() );
    assertFalse( config.isIncludeContent() );
    assertFalse( config.isIncludeSchedules() );
    assertEquals( 1, config.getComponentCount() );
  }

  @Test
  public void testBackupProfileIsCaseInsensitive() throws Exception {
    ComponentConfig config = backupConfig( "--backup", "--backup-profile=security" );

    assertTrue( config.isIncludeUsers() );
    assertFalse( config.isIncludeContent() );
  }

  @Test
  public void testUnknownBackupProfileReturnsNull() throws Exception {
    assertNull( backupConfig( "--backup", "--backup-profile=NO_SUCH_PROFILE" ) );
  }

  @Test
  public void testBackupWithoutProfileOrFlagsReturnsNull() throws Exception {
    // No profile and no granular flags -> null signals a full backup downstream.
    assertNull( backupConfig( "--backup" ) );
  }

  @Test
  public void testIncludeGeneratedContentOverrideOnContentProfile() throws Exception {
    ComponentConfig config =
        backupConfig( "--backup", "--backup-profile=CONTENT_ONLY", "--include-generated-content=false" );

    assertTrue( config.isIncludeContent() );
    assertFalse( config.isIncludeGeneratedContent() );
  }

  // ---------------------------------------------------------------------------
  // Restore profile mapping + fallback
  // ---------------------------------------------------------------------------

  @Test
  public void testRestoreProfileContentOnly() throws Exception {
    ComponentConfig config = restoreConfig( "--restore", "--restore-profile=CONTENT_ONLY" );

    assertTrue( config.isIncludeContent() );
    assertFalse( config.isIncludeUsers() );
  }

  @Test
  public void testRestoreProfileSecurity() throws Exception {
    ComponentConfig config = restoreConfig( "--restore", "--restore-profile=SECURITY" );

    assertTrue( config.isIncludeUsers() );
    assertFalse( config.isIncludeContent() );
  }

  @Test
  public void testRestoreFallsBackToBackupProfileWhenRestoreProfileAbsent() throws Exception {
    // Only --backup-profile is supplied (e.g. a dual-purpose config file reused for restore).
    // The restore builder must reuse it.
    ComponentConfig config = restoreConfig( "--restore", "--backup-profile=SECURITY" );

    assertTrue( config.isIncludeUsers() );
    assertFalse( config.isIncludeContent() );
    assertEquals( 1, config.getComponentCount() );
  }

  @Test
  public void testRestoreProfileWinsOverBackupProfile() throws Exception {
    // When both are present, --restore-profile takes precedence over --backup-profile.
    ComponentConfig config =
        restoreConfig( "--restore", "--restore-profile=SECURITY", "--backup-profile=DATA_SOURCE" );

    assertTrue( config.isIncludeUsers() );
    assertFalse( config.isIncludeDatasources() );
    assertFalse( config.isIncludeContent() );
  }

  @Test
  public void testRestoreUnknownProfileReturnsNull() throws Exception {
    assertNull( restoreConfig( "--restore", "--restore-profile=NO_SUCH_PROFILE" ) );
  }
}
