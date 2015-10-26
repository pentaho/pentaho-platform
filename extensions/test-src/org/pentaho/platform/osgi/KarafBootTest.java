/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

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
    FileUtils.deleteDirectory( tmpDir );
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
  }

  @Test
  public void testStartup_noKarafPortsYaml() throws Exception {
    assertFalse( boot.startup( session ) );
  }

  @Test
  public void testStartup() throws Exception {
    KarafBoot karafBoot = spy( boot );
    doReturn( karafInstance ).when( karafBoot ).createAndProcessKarafInstance( anyString() );

    boolean startup = karafBoot.startup( session );
    verify( karafInstance ).start();

    // can't see if it started since we aren't actually starting up karaf, return value will be false
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
  public void testConfigureSystemProperties() throws Exception {

  }

  @Test
  public void testCanOpenConfigPropertiesForEdit() throws Exception {

  }

  @Test
  public void testExpandSystemPackages() throws Exception {

  }

  @Test
  public void testShutdown() throws Exception {
    boot.shutdown();
  }

}
