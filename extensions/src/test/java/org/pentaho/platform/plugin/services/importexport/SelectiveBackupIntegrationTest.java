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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.services.exporter.PentahoPlatformExporter;

/**
 * Integration tests for selective backup/restore functionality
 */
public class SelectiveBackupIntegrationTest {

  private IUnifiedRepository mockRepository;
  private PentahoPlatformExporter exporter;
  private RepositoryFile rootFile;

  @Before
  public void setUp() {
    mockRepository = mock( IUnifiedRepository.class );
    exporter = new PentahoPlatformExporter( mockRepository );

    // Create mock root file
    rootFile = mock( RepositoryFile.class );
    when( rootFile.getPath() ).thenReturn( "/" );
    when( rootFile.isFolder() ).thenReturn( true );
    when( mockRepository.getFile( "/" ) ).thenReturn( rootFile );
  }

  @Test
  public void testFullSystemBackupInitialization() {
    ComponentConfig config = ComponentConfig.fullSystem();
    exporter.setComponentConfig( config );

    Object retrievedConfig = exporter.getComponentConfig();
    assertNotNull( retrievedConfig );
    assertTrue( retrievedConfig instanceof ComponentConfig );
    assertTrue( ( ( ComponentConfig ) retrievedConfig ).isIncludeContent() );
    assertTrue( ( ( ComponentConfig ) retrievedConfig ).isIncludeUsers() );
  }

  @Test
  public void testContentOnlyBackupInitialization() {
    ComponentConfig config = ComponentConfig.contentOnly();
    exporter.setComponentConfig( config );

    Object retrievedConfig = exporter.getComponentConfig();
    assertNotNull( retrievedConfig );
    assertTrue( retrievedConfig instanceof ComponentConfig );
    assertTrue( ( ( ComponentConfig ) retrievedConfig ).isIncludeContent() );
  }

  @Test
  public void testMultipleBackupProfilesCanBeCombined() {
    // Create a custom profile: Content + Users
    ComponentConfig config = new ComponentConfig( "Content and Security" );
    config.setIncludeContent( true );
    config.setIncludeUsers( true );
    config.setIncludeDatasources( false );
    config.setIncludeMetastore( false );
    config.setIncludeSchedules( false );
    config.setIncludeUserSettings( false );
    config.setIncludeMondrian( false );

    exporter.setComponentConfig( config );

    Object retrievedConfig = exporter.getComponentConfig();
    assertNotNull( retrievedConfig );
    assertTrue( retrievedConfig instanceof ComponentConfig );
    assertTrue( ( ( ComponentConfig ) retrievedConfig ).isValid() );
    assertTrue( ( ( ComponentConfig ) retrievedConfig ).isIncludeContent() );
    assertTrue( ( ( ComponentConfig ) retrievedConfig ).isIncludeUsers() );
  }

  @Test
  public void testBackupConfigurationMetadata() {
    ComponentConfig config = new ComponentConfig( "Metadata Backup" );
    config.setDescription( "Backup of metadata and analysis schemas" );
    config.setIncludeContent( false );
    config.setIncludeDatasources( true );
    config.setIncludeMondrian( true );

    exporter.setComponentConfig( config );

    Object retrievedConfig = exporter.getComponentConfig();
    assertNotNull( retrievedConfig );
    assertTrue( "Metadata Backup".equals( ( ( ComponentConfig ) retrievedConfig ).getBackupName() ) );
    assertTrue( ( ( ComponentConfig ) retrievedConfig ).getDescription().contains( "metadata" ) );
  }

  @Test
  public void testBackupComponentSelectionValidation() {
    ComponentConfig config = ComponentConfig.securityOnly();

    // Verify only security components are selected
    assertTrue( config.isValid() );
    assertTrue( config.isIncludeUsers() );
    assertTrue( config.getEnabledComponents().contains( "USERS_AND_ROLES" ) );
  }

  @Test
  public void testDataSourceBackupProfile() {
    ComponentConfig config = ComponentConfig.dataSource();
    exporter.setComponentConfig( config );

    Object retrievedConfig = exporter.getComponentConfig();
    assertNotNull( retrievedConfig );
    assertTrue( ( ( ComponentConfig ) retrievedConfig ).isIncludeDatasources() );
    assertTrue( ( ( ComponentConfig ) retrievedConfig ).isIncludeMetastore() );
    assertTrue( ( ( ComponentConfig ) retrievedConfig ).isIncludeMondrian() );
  }

  @Test
  public void testInfrastructureBackupProfile() {
    ComponentConfig config = ComponentConfig.infrastructure();
    exporter.setComponentConfig( config );

    Object retrievedConfig = exporter.getComponentConfig();
    assertNotNull( retrievedConfig );
    assertTrue( ( ( ComponentConfig ) retrievedConfig ).isIncludeSchedules() );
    assertTrue( ( ( ComponentConfig ) retrievedConfig ).isIncludeUserSettings() );
  }

  @Test
  public void testComponentCountCalculation() {
    ComponentConfig config = ComponentConfig.fullSystem();
    assertTrue( config.getComponentCount() == 7 );

    config = ComponentConfig.contentOnly();
    assertTrue( config.getComponentCount() == 1 );

    config = ComponentConfig.dataSource();
    assertTrue( config.getComponentCount() == 3 );
  }

  @Test
  public void testBackupConfigurationMapConversion() {
    ComponentConfig config = ComponentConfig.securityOnly();
    java.util.Map<String, Boolean> map = config.toMap();

    assertNotNull( map );
    assertTrue( map.containsKey( "users" ) );
    assertTrue( map.get( "users" ) );
  }
}
