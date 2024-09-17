/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002 - 2021 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.platform.osgi;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.osgi.KarafBoot.CLEAN_KARAF_CACHE;

@RunWith( MockitoJUnitRunner.class )
public class KarafBootTest {

  private static File configProps;
  KarafBoot boot;

  @Mock
  IApplicationContext appContext;

  @Mock
  IPentahoSession session;

  @Mock
  KarafInstance karafInstance;

  static File tmpDir;
  static File lockFile = new File( Paths.get( System.getProperty( "java.io.tmpdir" ), Const.KARAF_BOOT_LOCK_FILE ).toUri() );

  @BeforeClass
  public static void init() throws Exception {
    Properties props = new Properties();
    props.setProperty( Constants.FRAMEWORK_BEGINNING_STARTLEVEL, "100" );
    props.setProperty( "karaf.framework", "felix" );

    Path karafBootTest = Files.createTempDirectory( Paths.get( "." ), "KarafBootTest", new FileAttribute[ 0 ] );
    tmpDir = karafBootTest.toFile();
    configProps = new File( tmpDir, "system/karaf/etc/config.properties" );
    configProps.getParentFile().mkdirs();

    new File( tmpDir, "system/karaf/system" ).mkdirs();
    new File( tmpDir, "system/karaf/caches" ).mkdirs();

    try ( FileOutputStream fileOutputStream = new FileOutputStream( configProps ) ) {
      props.store( fileOutputStream, "Minimal properties for test KarafBoot without issue" );
    }
    System.setProperty( Const.KARAF_BOOT_LOCK_WAIT_TIME, "3000" );
    System.setProperty( Const.KARAF_WAIT_FOR_BOOT_LOCK_FILE, "true" );
  }

  @AfterClass
  public static void cleanUp() throws IOException {
    if ( tmpDir.exists() ) {
      FileUtils.deleteDirectory( tmpDir );
    }
    System.setProperty( Const.KARAF_WAIT_FOR_BOOT_LOCK_FILE, "false" );
  }

  @Before
  public void setUp() throws Exception {
    PentahoSystem.setApplicationContext( appContext );
    when( appContext.getSolutionRootPath() ).thenReturn( tmpDir.getPath() );
    boot = spy( new KarafBoot() );
    // ensure no boot lock exists
    lockFile.delete();
  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.clearObjectFactory();
    boot.shutdown();
    lockFile.delete();
  }

  @Test
  public void testStartup_noKarafPortsYaml() throws Exception {
    doReturn( karafInstance ).when( boot ).createAndProcessKarafInstance( nullable( String.class ) );
    assertFalse( boot.startup( session ) );
  }

  @Test
  public void testStartup() throws Exception {
    String origKarafHome = System.getProperty( "karaf.home" );
    try {
      System.getProperties().remove( "karaf.home" );

      doReturn( karafInstance ).when( boot ).createAndProcessKarafInstance( nullable( String.class ) );
      boolean startup = boot.startup( session );

      // can't see if it started since we aren't actually starting up karaf, return value will be false
      assertFalse( startup );
      assertEquals( tmpDir.getAbsolutePath(), new File( System.getProperty( "karaf.home" ) ).getParentFile().getParentFile().getAbsolutePath() );
    } finally {
      if ( origKarafHome == null ) {
        System.getProperties().remove( "karaf.home" );
      } else {
        System.setProperty( "karaf.home", origKarafHome );
      }
    }
  }

  @Test
  public void testStartupNotWritableConfigProps() throws Exception {
    String origKarafHome = System.getProperty( "karaf.home" );
    try {
      System.getProperties().remove( "karaf.home" );
      configProps.setWritable( false );
      try ( FileOutputStream fileOutputStream = new FileOutputStream(
          new File( tmpDir, "system/karaf/etc/custom.properties" ) ) ) {
        fileOutputStream
            .write( "org.osgi.framework.system.packages.extra=prop".getBytes( Charset.forName( "UTF-8" ) ) );
      }

      doReturn( karafInstance ).when( boot ).createAndProcessKarafInstance( nullable( String.class ) );
      boolean startup = boot.startup( session );

      // can't see if it started since we aren't actually starting up karaf, return value will be false
      assertFalse( startup );

      File karafHome = new File( System.getProperty( "karaf.home" ) );

      assertNotEquals( tmpDir, karafHome.getParentFile().getParentFile() );
      assertTrue( new File( karafHome, "system" ).exists() );
      assertFalse( new File( karafHome, "caches" ).exists() );
    } finally {
      if ( origKarafHome == null ) {
        System.getProperties().remove( "karaf.home" );
      } else {
        System.setProperty( "karaf.home", origKarafHome );
      }

      configProps.setWritable( true );
    }
  }

  @Test
  public void testStartupSetKarafDestFolder() throws Exception {
    String origKarafHome = System.getProperty( "karaf.home" );
    File tempDirectory = Files.createTempDirectory( Paths.get( "." ), "test" ).toFile();
    tempDirectory.delete();

    try {
      System.getProperties().remove( "karaf.home" );
      System.setProperty( KarafBoot.PENTAHO_KARAF_ROOT_COPY_DEST_FOLDER, tempDirectory.getAbsolutePath() );
      try ( FileOutputStream fileOutputStream = new FileOutputStream(
          new File( tmpDir, "system/karaf/etc/custom.properties" ) ) ) {
        fileOutputStream
            .write( "org.osgi.framework.system.packages.extra=prop".getBytes( Charset.forName( "UTF-8" ) ) );
      }

      doReturn( karafInstance ).when( boot ).createAndProcessKarafInstance( nullable( String.class ) );
      boolean startup = boot.startup( session );

      // can't see if it started since we aren't actually starting up karaf, return value will be false
      assertFalse( startup );
      File karafHome = new File( System.getProperty( "karaf.home" ) );

      try {
        assertEquals( tempDirectory.getCanonicalFile(), karafHome.getCanonicalFile() ); // Wasn't working in Windows with ~1 file names
      } catch ( AssertionError ae ) {
        // don't throw an assertion error just yet; OSX likes to add its own files/folders into
        // a folder ( think .DS_Store file or .Trash folder ), and also has its saying on the
        // paths we can use ( think /private/var or /private/tmp instead of /var or /tmp )

        if ( isOSX() ) {
          assertEquals( ( "/private" + tempDirectory.getAbsolutePath() ), karafHome.getAbsolutePath() );
        } else {
          throw ae;
        }
      }

      assertTrue( new File( karafHome, "system" ).exists() );
      assertFalse( new File( karafHome, "caches" ).exists() );
    } finally {
      if ( origKarafHome == null ) {
        System.getProperties().remove( "karaf.home" );
      } else {
        System.setProperty( "karaf.home", origKarafHome );
      }

      System.getProperties().remove( KarafBoot.PENTAHO_KARAF_ROOT_COPY_DEST_FOLDER );
      FileUtils.deleteDirectory( tempDirectory );
    }
  }

  @Test
  public void testStartupSetKarafDestFolderTransient() throws Exception {
    String origKarafHome = System.getProperty( "karaf.home" );
    File tempDirectory = Files.createTempDirectory( Paths.get( "." ), "test" ).toFile();

    try {
      System.getProperties().remove( "karaf.home" );
      System.setProperty( KarafBoot.PENTAHO_KARAF_ROOT_COPY_DEST_FOLDER, tempDirectory.getAbsolutePath() );
      System.setProperty( KarafBoot.PENTAHO_KARAF_ROOT_TRANSIENT, "true" );

      try ( FileOutputStream fileOutputStream = new FileOutputStream(
          new File( tmpDir, "system/karaf/etc/custom.properties" ) ) ) {
        fileOutputStream
            .write( "org.osgi.framework.system.packages.extra=prop".getBytes( Charset.forName( "UTF-8" ) ) );
      }

      doReturn( karafInstance ).when( boot ).createAndProcessKarafInstance( nullable( String.class ) );
      boolean startup = boot.startup( session );

      // can't see if it started since we aren't actually starting up karaf, return value will be false
      assertFalse( startup );

      File karafHome = new File( System.getProperty( "karaf.home" ) );

      assertEquals( tempDirectory.getAbsoluteFile().getParent(), karafHome.getAbsoluteFile().getParent() );
      assertTrue( karafHome.getName().startsWith( tempDirectory.getName() ) );
      assertTrue( new File( karafHome, "system" ).exists() );
      assertFalse( new File( karafHome, "caches" ).exists() );
    } finally {
      if ( origKarafHome == null ) {
        System.getProperties().remove( "karaf.home" );
      } else {
        System.setProperty( "karaf.home", origKarafHome );
      }

      System.getProperties().remove( KarafBoot.PENTAHO_KARAF_ROOT_COPY_DEST_FOLDER );
      System.getProperties().remove( KarafBoot.PENTAHO_KARAF_ROOT_TRANSIENT );

      FileUtils.deleteDirectory( tempDirectory );
    }
  }

  @Test
  public void testStartup_withOsxAppRootDir() throws Exception {
    when( appContext.getSolutionRootPath() ).thenReturn( null );
    doReturn( karafInstance ).when( boot ).createAndProcessKarafInstance( nullable( String.class ) );

    System.setProperty( "osx.app.root.dir", tmpDir.getPath() );
    doReturn( KettleClientEnvironment.ClientType.KITCHEN ).when( boot ).getClientType();

    boolean startup = boot.startup( session );

    // can't see if it started since we aren't actually starting up karaf, return value will be false
    assertFalse( startup );
  }

  @Test
  public void testBuildExtraKettleEtc() throws Exception {
    assertEquals( "/etc-carte", boot.translateToExtraKettleEtc( KettleClientEnvironment.ClientType.CARTE ) );
    assertEquals( "/etc-default", boot.translateToExtraKettleEtc( KettleClientEnvironment.ClientType.PENTAHO_SERVER ) );
    assertEquals( "/etc-kitchen", boot.translateToExtraKettleEtc( KettleClientEnvironment.ClientType.KITCHEN ) );
    assertEquals( "/etc-pan", boot.translateToExtraKettleEtc( KettleClientEnvironment.ClientType.PAN ) );
    assertEquals( "/etc-spoon", boot.translateToExtraKettleEtc( KettleClientEnvironment.ClientType.SPOON ) );
    assertEquals( "/etc-default", boot.translateToExtraKettleEtc( null ) );
  }

  @Test
  public void testConfigureSystemProperties_karafHome() throws Exception {
    testConfigureSystemProperties( "karaf.home", "karaf.home" );
  }

  @Test
  public void testConfigureSystemProperties_karafBase() throws Exception {
    testConfigureSystemProperties( "karaf.base", "karaf.base" );
  }

  @Test
  public void testConfigureSystemProperties_karafHistory() throws Exception {
    testConfigureSystemProperties( "karaf.history", "karaf.history" );
  }

  @Test
  public void testConfigureSystemProperties_karafInstances() throws Exception {
    testConfigureSystemProperties( "karaf.instances", "karaf.instances" );
  }

  @Test
  public void testConfigureSystemProperties_startLocalConsole() throws Exception {
    testConfigureSystemProperties( "karaf.startLocalConsole", "karaf.startLocalConsole" );
  }

  @Test
  public void testConfigureSystemProperties_startRemoteShell() throws Exception {
    testConfigureSystemProperties( "karaf.startRemoteShell", "karaf.startRemoteShell" );
  }

  @Test
  public void testConfigureSystemProperties_karafLock() throws Exception {
    testConfigureSystemProperties( "karaf.lock", "karaf.lock" );
  }

  private void testConfigureSystemProperties( String propertyName, String expected ) throws Exception {
    System.setProperty( propertyName, expected );

    boot.setCustomProperties( new Properties() );
    boot.configureSystemProperties( "solutionRootPath", "root" );

    // check that property does not override
    assertEquals( expected, System.getProperty( propertyName ) );
  }

  @Test
  public void testClearDataCacheSetting() throws Exception {
    // clear Karaf's property to avoid tests' interdependency
    System.clearProperty( "karaf.data" );

    PentahoSystem.init( new StandaloneApplicationContext( TestResourceLocation.TEST_RESOURCES + "/karafBootTest", "." ) );

    final File root = Files.createTempDirectory( Paths.get( "." ), "root" ).toFile();
    final File caches = new File( root, "caches" );

    caches.mkdir();

    for ( int i = 0; i < 5; i++ ) {
      File clientTypeFolder = new File( caches, "client" + i );
      clientTypeFolder.mkdir();

      for ( int y = 0; y < 3; y++ ) {
        new File( clientTypeFolder, "data-" + y ).mkdir();
      }
    }

    FileUtils.copyDirectory(
      new File( TestResourceLocation.TEST_RESOURCES + "/karafBootTest/system/karaf/etc" ),
      new File( root, "etc" )
    );

    final Properties initialCustomProperties = boot.readCustomProperties( root.getPath() );
    initialCustomProperties.setProperty( CLEAN_KARAF_CACHE, "true" );

    boot.setCustomProperties( initialCustomProperties );
    boot.cleanCachesIfFlagSet( root.getPath() );

    // Check that all data directories are gone
    for ( int i = 0; i < 5; i++ ) {
      File clientTypeFolder = new File( caches, "client" + i );
      File[] files = clientTypeFolder.listFiles();

      assertNotNull( files );
      assertEquals( 0, files.length );
    }

    // check if 'org.pentaho.clean.karaf.cache' property was set to false and stored in the config file.
    final Properties finalConfigProperties = boot.readCustomProperties( root.getPath() );
    assertEquals( "false", finalConfigProperties.getProperty( CLEAN_KARAF_CACHE ) );

    if ( root.exists() ) {
      FileUtils.deleteDirectory( root );
    }
  }

  @Test
  public void deleteRecursiveIfExistsTest() {
    File dir = new File( "dir" );
    dir.mkdir();
    File file = new File( dir,  "file" );
    try {
      file.createNewFile();

      boot.deleteRecursiveIfExists( dir );

      assertNull( dir.listFiles() );
    } catch ( IOException e ) {
      fail( "Couldn't create file to test deleteRecursiveIfExists()" );
    }
  }

  @Test
  public void verifyBootLockFailure() throws Exception {
    assertTrue( lockFile.createNewFile() );
    FileChannel fileChannel = FileChannel.open( lockFile.toPath(), Set.of( StandardOpenOption.WRITE ) );
    fileChannel.write( ByteBuffer.wrap( boot.getCurrentPid().getBytes() ) );
    assertFalse( boot.waitForBootLock() );
  }

  @Test
  public void verifyBootLockSuccessBehavior() throws Exception {
    assertTrue( boot.waitForBootLock() );
  }

  private boolean isOSX() {
    final String MAC_OS_BASE_NAME = "mac";

    try {
      String osName = System.getProperty( "os.name", "unknown" /* fallback value */ ).toLowerCase( Locale.ENGLISH );
      return osName.startsWith( MAC_OS_BASE_NAME ); // osName is NPE-safe
    } catch ( Throwable t ) {
      // do not propagate any errors upwards; this is a quick-&-simple helper method,
      // solely due to the fact that OSX likes to add its own files/folders into a folder
      // ( think .DS_Store or .Trash ), and also has its saying on the paths we can use
      // ( think /private/var or /private/tmp instead of /var or /tmp )
    }
    return false;
  }
}
