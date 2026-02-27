/*
 * ! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.plugin.services.pluginmgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class SystemPathXmlPluginProviderTest {

  SystemPathXmlPluginProvider systemPathXmlPluginProvider;

  @Before
  public void setup() {
    systemPathXmlPluginProvider = new SystemPathXmlPluginProvider();
  }

  @Test
  public void testStripDateStampFromFolderName_withDateStamp() {
    String folderName = "backup-2024-6-01";
    String expected = "backup";
    assertEquals( expected, systemPathXmlPluginProvider.stripDateStampFromFolderName( folderName ) );
  }

  @Test
  public void testStripDateStampFromFolderName_withoutDateStamp() {
    String folderName = "backup";
    String expected = "backup";
    assertEquals( expected, systemPathXmlPluginProvider.stripDateStampFromFolderName( folderName ) );
  }

  @Test
  public void testStripDateStampFromFolderName_emptyString() {
    String folderName = "";
    String expected = "";
    assertEquals( expected, systemPathXmlPluginProvider.stripDateStampFromFolderName( folderName ) );
  }

  @Test
  public void testStripDateStampFromFolderName_nullInput() {
    assertNull( systemPathXmlPluginProvider.stripDateStampFromFolderName( null ) );
  }

  @Test
  public void testStripDateStampFromFolderName_multipleUnderscores() {
    String folderName = "data_backup-2024-0601";
    String expected = "data_backup";
    assertEquals( expected, systemPathXmlPluginProvider.stripDateStampFromFolderName( folderName ) );
  }

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void testProcessDirectory_withPluginXml() throws Exception {
    File pluginDir = tempFolder.newFolder( "testPlugin" );
    File pluginXml = new File( pluginDir, "plugin.xml" );
    pluginXml.createNewFile();
    List<IPlatformPlugin> plugins = new ArrayList<>();
    IPentahoSession session = Mockito.mock( IPentahoSession.class );
    // statically mock the ActionSequenceResource to always return an inputStream to the plugin.xml file
    try ( MockedStatic<ActionSequenceResource> mockedStatic =
      Mockito.mockStatic( ActionSequenceResource.class ) ) {
      String pluginXmlText = "<plugin title=\"testPlugin\" name=\"testPlugin\"></plugin>";
      // create an input stream from the pluginXmlText
      mockedStatic.when( () -> ActionSequenceResource.getInputStream( anyString(), any() ) )
        .thenReturn( new ByteArrayInputStream( pluginXmlText.getBytes() ) );
      systemPathXmlPluginProvider.processDirectory( plugins, pluginDir, session );
      // Verify that the plugin was added
      assertFalse( plugins.isEmpty() );
      assertEquals( 1, plugins.size() );
    }
  }

  @Test
  public void testProcessDirectory_withDeleteMarker() throws Exception {
    File pluginDir = tempFolder.newFolder( "deletePlugin" );
    File pluginXml = new File( pluginDir, "plugin.xml" );
    pluginXml.createNewFile();
    File deleteMarker = new File( pluginDir, ".plugin-manager-delete" );
    File ignoreMarker = new File( pluginDir, ".kettle-ignore" );
    deleteMarker.createNewFile();
    ignoreMarker.createNewFile();
    List<IPlatformPlugin> plugins = new ArrayList<>();
    IPentahoSession session = Mockito.mock( IPentahoSession.class );
    systemPathXmlPluginProvider.processDirectory( plugins, pluginDir, session );
    // Directory should be deleted
    assertFalse( pluginDir.exists() );
    assertTrue( plugins.isEmpty() );
  }

  @Test
  public void testProcessDirectory_withIgnoreMarker() throws Exception {
    File pluginDir = tempFolder.newFolder( "ignorePlugin" );
    File pluginXml = new File( pluginDir, "plugin.xml" );
    pluginXml.createNewFile();
    File ignoreMarker = new File( pluginDir, ".kettle-ignore" );
    ignoreMarker.createNewFile();
    List<IPlatformPlugin> plugins = new ArrayList<>();
    IPentahoSession session = Mockito.mock( IPentahoSession.class );
    systemPathXmlPluginProvider.processDirectory( plugins, pluginDir, session );
    // pluginDir should still exist
    assertTrue( pluginDir.exists() );
    // Should not add plugin
    assertTrue( plugins.isEmpty() );
  }

  /*
   * This test sets up an existing analyzer plugin folder that has already been marked for deletion by the
   * plugin manager, as well as a new plugin folder with a date stamp.
   * The test verifies that the old folder is deleted, the new folder is renamed to the old folder name,
   * and the contents of the old folder are deleted, including the plugin.xml file.
   * It also checks that the plugin.xml in the renamed folder does not contain the old plugin title.
   */
  @Test
  public void testProcessDirectory_renameFolderWithDateStampAndDeleteOld() throws Exception {
    // Create parent system folder
    File systemFolder = tempFolder.getRoot();

    // Create old plugin folder "analyzer" with ignore and delete markers
    File oldPluginFolder = new File( systemFolder, "analyzer" );
    oldPluginFolder.mkdir();
    new File( oldPluginFolder, ".kettle-ignore" ).createNewFile();
    new File( oldPluginFolder, ".plugin-manager-delete" ).createNewFile();
    // Create plugin.xml in old folder
    File oldPluginXml = new File( oldPluginFolder, "plugin.xml" );
    oldPluginXml.createNewFile();
    // Write a simple plugin.xml content
    try ( FileWriter writer = new FileWriter( oldPluginXml ) ) {
      writer.write( "<plugin title=\"analyzerOLD\" name=\"analyzerOLD\"></plugin>" );
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    // Create new plugin folder with date stamp "analyzer-2025-01-01" and plugin.xml
    File newPluginFolder = new File( systemFolder, "analyzer-2025-01-01" );
    newPluginFolder.mkdir();
    File pluginXml = new File( newPluginFolder, "plugin.xml" );
    pluginXml.createNewFile();
    // Write a simple plugin.xml content
    try ( FileWriter writer = new FileWriter( pluginXml ) ) {
      writer.write( "<plugin title=\"analyzer\" name=\"analyzer\"></plugin>" );
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    List<IPlatformPlugin> plugins = new ArrayList<>();
    IPentahoSession session = Mockito.mock( IPentahoSession.class );

    // Mock ActionSequenceResource to return a valid plugin.xml input stream
    try ( MockedStatic<ActionSequenceResource> mockedStatic =
      Mockito.mockStatic( ActionSequenceResource.class ) ) {
      mockedStatic.when( () -> ActionSequenceResource.getInputStream( anyString(), any() ) )
        .thenReturn( new ByteArrayInputStream( "<plugin title=\"analyzer\" name=\"analyzer\"></plugin>".getBytes() ) );

      systemPathXmlPluginProvider.processDirectory( plugins, newPluginFolder, session );

      // timestamp should be removed from the folder name
      assertFalse( newPluginFolder.exists() );
      // New folder should be renamed to "analyzer"
      File renamedFolder = new File( systemFolder, "analyzer" );
      assertTrue( renamedFolder.exists() );
      // verify that the contents of the old folder were deleted
      assertFalse( ( new File( renamedFolder, ".kettle-ignore" ).exists() ) );
      assertFalse( ( new File( renamedFolder, ".plugin-manager-delete" ).exists() ) );
      // verify that the plugin.xml remaining does not contain the old plugin title
      File renamedPluginXml = new File( renamedFolder, "plugin.xml" );
      assertTrue( renamedPluginXml.exists() );
      try ( BufferedReader reader = new BufferedReader( new FileReader( renamedPluginXml ) ) ) {
        String line;
        boolean foundOld = false;
        while ( ( line = reader.readLine() ) != null ) {
          if ( line.contains( "OLD" ) ) {
            foundOld = true;
            break;
          }
        }
        assertFalse( foundOld );
      } catch ( Exception e ) {
        e.printStackTrace();
      }
    }
  }


  /*
  * Ensure that we do not change anything if we don't detect both the ignore and delete files in a plugin folder.
  */
  @Test
  public void testProcessDirectory_errorRenamingFolder() throws Exception {
    // Create parent system folder
    File systemFolder = tempFolder.getRoot();

    // Create old plugin folder "analyzer" with ignore and delete markers
    File oldPluginFolder = new File( systemFolder, "analyzer" );
    oldPluginFolder.mkdir();
    new File( oldPluginFolder, ".kettle-ignore" ).createNewFile();

    // Create plugin.xml in old folder
    File oldPluginXml = new File( oldPluginFolder, "plugin.xml" );
    oldPluginXml.createNewFile();
    // Write a simple plugin.xml content
    try ( FileWriter writer = new FileWriter( oldPluginXml ) ) {
      writer.write( "<plugin title=\"analyzerOLD\" name=\"analyzerOLD\"></plugin>" );
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    // Create new plugin folder with date stamp "analyzer-2025-01-01" and plugin.xml
    File newPluginFolder = new File( systemFolder, "analyzer-2025-01-01" );
    newPluginFolder.mkdir();
    File pluginXml = new File( newPluginFolder, "plugin.xml" );
    pluginXml.createNewFile();
    // Write a simple plugin.xml content
    try ( FileWriter writer = new FileWriter( pluginXml ) ) {
      writer.write( "<plugin title=\"analyzer\" name=\"analyzer\"></plugin>" );
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    List<IPlatformPlugin> plugins = new ArrayList<>();
    IPentahoSession session = Mockito.mock( IPentahoSession.class );

    // Mock ActionSequenceResource to return a valid plugin.xml input stream
    try ( MockedStatic<ActionSequenceResource> mockedStatic =
      Mockito.mockStatic( ActionSequenceResource.class ) ) {
      mockedStatic.when( () -> ActionSequenceResource.getInputStream( anyString(), any() ) )
        .thenReturn( new ByteArrayInputStream( "<plugin title=\"analyzer\" name=\"analyzer\"></plugin>".getBytes() ) );

      systemPathXmlPluginProvider.processDirectory( plugins, newPluginFolder, session );

      // new folder name should be unchanged
      assertTrue( newPluginFolder.exists() );
      // old folder still exists too
      File renamedFolder = new File( systemFolder, "analyzer" );
      assertTrue( renamedFolder.exists() );
      // verify that the contents of the old folder are still there
      assertTrue( ( new File( renamedFolder, ".kettle-ignore" ).exists() ) );
      // verify that the plugin.xml remaining does not contain the old plugin title
      File renamedPluginXml = new File( renamedFolder, "plugin.xml" );
      assertTrue( renamedPluginXml.exists() );
      try ( BufferedReader reader = new BufferedReader( new FileReader( renamedPluginXml ) ) ) {
        String line;
        boolean foundOld = false;
        while ( ( line = reader.readLine() ) != null ) {
          if ( line.contains( "OLD" ) ) {
            foundOld = true;
            break;
          }
        }
        assertTrue( foundOld );
      } catch ( Exception e ) {
        e.printStackTrace();
      }
    }
  }

  /*
   * Verifies that a single folder with a date stamp in its name is renamed to the base name
   * without the date stamp, and that the plugin.xml file is processed correctly.
   * This represents the scenario where a plugin is installed by the plugin manager without having 
   * an old version to delete.
   */
  @Test
  public void testProcessDirectory_renameFolderWithDateStamp() throws Exception {
    // Create parent system folder
    File systemFolder = tempFolder.getRoot();

    // Create new plugin folder with date stamp "analyzer-2025-01-01" and plugin.xml
    File newPluginFolder = new File( systemFolder, "analyzer-2025-01-01" );
    newPluginFolder.mkdir();
    File pluginXml = new File( newPluginFolder, "plugin.xml" );
    pluginXml.createNewFile();

    List<IPlatformPlugin> plugins = new ArrayList<>();
    IPentahoSession session = Mockito.mock( IPentahoSession.class );

    // Mock ActionSequenceResource to return a valid plugin.xml input stream
    try ( MockedStatic<ActionSequenceResource> mockedStatic =
      Mockito.mockStatic( ActionSequenceResource.class ) ) {
      String pluginXmlText = "<plugin title=\"analyzer\" name=\"analyzer\"></plugin>";
      mockedStatic.when( () -> ActionSequenceResource.getInputStream( anyString(), any() ) )
        .thenReturn( new java.io.ByteArrayInputStream( pluginXmlText.getBytes() ) );

      systemPathXmlPluginProvider.processDirectory( plugins, newPluginFolder, session );

      // timestamp should be removed from the folder name
      assertFalse( newPluginFolder.exists() );
      // New folder should be renamed to "analyzer"
      File renamedFolder = new File( systemFolder, "analyzer" );
      assertTrue( renamedFolder.exists() );
      assertFalse( plugins.isEmpty() );
      assertEquals( 1, plugins.size() );
    }
  }

  /*
   * This test forces the condition where a plugin with a given ID already exists in the plugins list.
   * It verifies that the plugin manager does not add a duplicate plugin and logs an appropriate message.
   */
  @Test
  public void testProcessDirectory_duplicatePluginId() throws Exception {
    File pluginDir = tempFolder.newFolder( "duplicatePlugin" );
    File pluginXml = new File( pluginDir, "plugin.xml" );
    pluginXml.createNewFile();
    List<IPlatformPlugin> plugins = new ArrayList<>();
    IPentahoSession session = Mockito.mock( IPentahoSession.class );
    // Add a plugin with the same ID to the list
    IPlatformPlugin existingPlugin = Mockito.mock( IPlatformPlugin.class );
    Mockito.when( existingPlugin.getId() ).thenReturn( "duplicatePlugin" );
    plugins.add( existingPlugin );
    // statically mock the ActionSequenceResource to always return an inputStream to the plugin.xml file
    try ( MockedStatic<ActionSequenceResource> mockedStatic =
      Mockito.mockStatic( ActionSequenceResource.class ) ) {
      String pluginXmlText = "<plugin title=\"duplicatePlugin\" name=\"duplicatePlugin\"></plugin>";
      // create an input stream from the pluginXmlText
      mockedStatic.when( () -> ActionSequenceResource.getInputStream( anyString(), any() ) )
        .thenReturn( new ByteArrayInputStream( pluginXmlText.getBytes() ) );
      systemPathXmlPluginProvider.processDirectory( plugins, pluginDir, session );
      // Verify that the plugin was not added again
      assertEquals( 1, plugins.size() );
      assertTrue( PluginMessageLogger.getAll().stream().anyMatch( msg -> msg.contains( "A plugin has already been registered by name duplicatePlugin" ) ) );
    }
  }
}
