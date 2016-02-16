/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 10/23/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class KarafBootTest {

  KarafBoot boot;

  @Mock IApplicationContext appContext;
  @Mock IPentahoSession session;
  @Mock KarafInstance karafInstance;

  static File tmpDir;

  @BeforeClass
  public static void init() throws Exception {
    Path karafBootTest = Files.createTempDirectory( "KarafBootTest", new FileAttribute[ 0 ] );
    tmpDir = karafBootTest.toFile();
  }

  @AfterClass
  public static void cleanUp() throws IOException {
    if ( tmpDir.exists() ) {
      FileUtils.deleteDirectory( tmpDir );
    }
  }

  @Before
  public void setUp() throws Exception {
    PentahoSystem.setApplicationContext( appContext );
    when( appContext.getSolutionRootPath() ).thenReturn( tmpDir.getPath() );
    boot = new KarafBoot();
  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.clearObjectFactory();
    boot.shutdown();
  }

  @Test
  public void testStartup_noKarafPortsYaml() throws Exception {
    KarafBoot karafBoot = spy( boot );
    doReturn( karafInstance ).when( karafBoot ).createAndProcessKarafInstance( anyString() );
    assertFalse( karafBoot.startup( session ) );
  }

  @Test
  public void testStartup() throws Exception {
    KarafBoot karafBoot = spy( boot );
    doReturn( karafInstance ).when( karafBoot ).createAndProcessKarafInstance( anyString() );

    boolean startup = karafBoot.startup( session );
    verify( karafInstance ).start();
    // can't see if it started since we aren't actually starting up karaf, return value will be false
    assertFalse( startup );
  }

  @Test
  public void testStartup_withOsxAppRootDir() throws Exception {
    KarafBoot karafBoot = spy( boot );
    when( appContext.getSolutionRootPath() ).thenReturn( null );
    doReturn( karafInstance ).when( karafBoot ).createAndProcessKarafInstance( anyString() );
    System.setProperty( "osx.app.root.dir", tmpDir.getPath() );
    doReturn( KettleClientEnvironment.ClientType.KITCHEN ).when( karafBoot ).getClientType();

    boolean startup = karafBoot.startup( session );
    verify( karafInstance ).start();

    // can't see if it started since we aren't actually starting up karaf, return value will be false
    assertFalse( startup );
  }

  @Test
  public void testBuildExtraKettleEtc() throws Exception {
    assertEquals( "/etc-carte", boot.translateToExtraKettleEtc( KettleClientEnvironment.ClientType.CARTE ) );
    assertEquals( "/etc-default", boot.translateToExtraKettleEtc( KettleClientEnvironment.ClientType.DI_SERVER ) );
    assertEquals( "/etc-kitchen", boot.translateToExtraKettleEtc( KettleClientEnvironment.ClientType.KITCHEN ) );
    assertEquals( "/etc-pan", boot.translateToExtraKettleEtc( KettleClientEnvironment.ClientType.PAN ) );
    assertEquals( "/etc-spoon", boot.translateToExtraKettleEtc( KettleClientEnvironment.ClientType.SPOON ) );
    assertNull( boot.translateToExtraKettleEtc( null ) );
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

  @Test
  public void testConfigureSystemProperties_karafData() throws Exception {
    testConfigureSystemProperties( "karaf.data", "karaf.data" );
  }

  private void testConfigureSystemProperties( String propertyName, String expected ) throws Exception {
    //set property
    System.setProperty( propertyName, expected );
    KarafBoot karafBoot = new KarafBoot();
    karafBoot.configureSystemProperties( "solutionRootPath", "root" );
    //check that property does not everrided
    assertEquals( expected, System.getProperty( propertyName ) );
  }


  @Test
  public void testClearDataCacheSetting() throws Exception {

    PentahoSystem.init( new StandaloneApplicationContext( "test-res/karafBootTest", "." ) );
    //set property
    KarafBoot karafBoot = new KarafBoot();
    karafBoot.configureSystemProperties( "test-res/osgiSystem/system", "test-res/karafBootTest/system/karaf" );

    File nf = new File( "test-res/karafBootTest/system/karaf/data/testFile.txt" );
    nf.mkdirs();
    nf.createNewFile();
    assertTrue( nf.exists() );

    Properties config = new Properties();
    File configFile = new File( "test-res/karafBootTest/system/karaf/etc/custom.properties" );
    config.load( new FileInputStream( configFile ) );
    config.setProperty( "org.pentaho.clean.karaf.cache", "true" );
    config.store( new FileOutputStream( configFile ), "setting stage" );

    karafBoot.cleanCacheIfFlagSet( "test-res/karafBootTest/system/karaf/" );
    assertFalse( nf.exists() );

    config.load( new FileInputStream( configFile ) );
    assertEquals( "false", config.getProperty( "org.pentaho.clean.karaf.cache" ) );

  }

}
