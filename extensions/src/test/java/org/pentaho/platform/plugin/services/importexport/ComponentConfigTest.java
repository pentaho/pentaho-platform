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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Test cases for ComponentConfig
 */
public class ComponentConfigTest {

  @Test
  public void testFullSystemBackup() {
    ComponentConfig config = ComponentConfig.fullSystem();

    assertTrue( config.isIncludeContent() );
    assertTrue( config.isIncludeUsers() );
    assertTrue( config.isIncludeDatasources() );
    assertTrue( config.isIncludeMetastore() );
    assertTrue( config.isIncludeSchedules() );
    assertTrue( config.isIncludeUserSettings() );
    assertTrue( config.isIncludeMondrian() );

    assertTrue( config.isValid() );
    assertEquals( 7, config.getComponentCount() );
  }

  @Test
  public void testContentOnlyBackup() {
    ComponentConfig config = ComponentConfig.contentOnly();

    assertTrue( config.isIncludeContent() );
    assertFalse( config.isIncludeUsers() );
    assertFalse( config.isIncludeDatasources() );
    assertFalse( config.isIncludeMetastore() );
    assertFalse( config.isIncludeSchedules() );
    assertFalse( config.isIncludeUserSettings() );
    assertFalse( config.isIncludeMondrian() );

    assertTrue( config.isValid() );
    assertEquals( 1, config.getComponentCount() );
  }

  @Test
  public void testSecurityOnlyBackup() {
    ComponentConfig config = ComponentConfig.securityOnly();

    assertFalse( config.isIncludeContent() );
    assertTrue( config.isIncludeUsers() );
    assertFalse( config.isIncludeDatasources() );
    assertFalse( config.isIncludeMetastore() );
    assertFalse( config.isIncludeSchedules() );
    assertFalse( config.isIncludeUserSettings() );
    assertFalse( config.isIncludeMondrian() );

    assertTrue( config.isValid() );
    assertEquals( 1, config.getComponentCount() );
  }

  @Test
  public void testDataSourceBackup() {
    ComponentConfig config = ComponentConfig.dataSource();

    assertFalse( config.isIncludeContent() );
    assertFalse( config.isIncludeUsers() );
    assertTrue( config.isIncludeDatasources() );
    assertTrue( config.isIncludeMetastore() );
    assertFalse( config.isIncludeSchedules() );
    assertFalse( config.isIncludeUserSettings() );
    assertTrue( config.isIncludeMondrian() );

    assertTrue( config.isValid() );
    assertEquals( 3, config.getComponentCount() );
  }

  @Test
  public void testCustomConfiguration() {
    ComponentConfig config = new ComponentConfig( "Custom Backup" );
    config.setIncludeContent( true );
    config.setIncludeUsers( true );
    config.setIncludeDatasources( false );
    config.setIncludeMetastore( true );
    config.setIncludeSchedules( false );
    config.setIncludeUserSettings( false );
    config.setIncludeMondrian( false );

    assertTrue( config.isIncludeContent() );
    assertTrue( config.isIncludeUsers() );
    assertFalse( config.isIncludeDatasources() );
    assertTrue( config.isIncludeMetastore() );
    assertFalse( config.isIncludeSchedules() );
    assertFalse( config.isIncludeUserSettings() );
    assertFalse( config.isIncludeMondrian() );

    assertTrue( config.isValid() );
    assertEquals( 3, config.getComponentCount() );
    assertEquals( "Custom Backup", config.getBackupName() );
  }

  @Test
  public void testGetEnabledComponents() {
    ComponentConfig config = ComponentConfig.contentOnly();
    config.setIncludeSchedules( true );

    List<String> components = config.getEnabledComponents();
    assertNotNull( components );
    assertEquals( 2, components.size() );
    assertTrue( components.contains( "CONTENT" ) );
    assertTrue( components.contains( "SCHEDULES" ) );
  }

  @Test
  public void testToMap() {
    ComponentConfig config = ComponentConfig.securityOnly();
    Map<String, Boolean> map = config.toMap();

    assertNotNull( map );
    assertFalse( map.get( "content" ) );
    assertTrue( map.get( "users" ) );
    assertFalse( map.get( "datasources" ) );
    assertFalse( map.get( "metastore" ) );
    assertFalse( map.get( "schedules" ) );
    assertFalse( map.get( "userSettings" ) );
    assertFalse( map.get( "mondrian" ) );
  }

  @Test
  public void testInvalidConfiguration() {
    ComponentConfig config = new ComponentConfig();
    config.setIncludeContent( false );
    config.setIncludeUsers( false );
    config.setIncludeDatasources( false );
    config.setIncludeMetastore( false );
    config.setIncludeSchedules( false );
    config.setIncludeUserSettings( false );
    config.setIncludeMondrian( false );

    assertFalse( config.isValid() );
  }

  @Test
  public void testToString() {
    ComponentConfig config = ComponentConfig.fullSystem();
    String str = config.toString();

    assertNotNull( str );
    assertTrue( str.contains( "ComponentConfig" ) );
    assertTrue( str.contains( "Full System Backup" ) );
  }

  @Test
  public void testSerialization() {
    ComponentConfig config = ComponentConfig.dataSource();
    config.setBackupName( "Test Backup" );
    config.setDescription( "Test Description" );

    // Verify all properties are set correctly
    assertEquals( "Test Backup", config.getBackupName() );
    assertEquals( "Test Description", config.getDescription() );
    assertNotNull( config.getCreatedTimestamp() );
    assertEquals( 1, config.getVersion() );
  }
}
