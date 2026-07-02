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


package org.pentaho.platform.osgi;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.settings.PortFileManager;
import org.pentaho.platform.settings.PortFileManagerTest;
import org.pentaho.platform.settings.ServerPortRegistry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author tkafalas
 */
public class KarafInstanceTest {
  private String TEST_CACHE_FOLDER = "./bin/test/cacheTest";
  private String USED_PORT_FILENAME = "PortsAssigned.txt";

  @Before
  public void setUp() throws IOException {
    // remove a prior test run, if any
    File folder = new File( TEST_CACHE_FOLDER );
    if ( folder.exists() ) {
      FileUtils.deleteDirectory( folder );
    }
  }

  @After
  public void tearDown() throws IOException {
    // clean up
    File folder = new File( TEST_CACHE_FOLDER );
    if ( folder.exists() ) {
      folder.deleteOnExit();
    }
  }

  @Test
  public void testKarafInstance() throws Exception {
    KarafInstance karafInstance1 = createTestInstance( 1 );
    KarafInstance karafInstance2 = createTestInstance( 2 );
    KarafInstance karafInstance3 = createTestInstance( 3 );

    // Test instance Re-use
    karafInstance2.close();
    karafInstance2 = createTestInstance( 2 );
    KarafInstance karafInstance4 = createTestInstance( 4 );

    //close all instance
    karafInstance1.close();
    karafInstance2.close();
    karafInstance3.close();
    karafInstance4.close();
  }

  private KarafInstance createTestInstance( int expectedInstanceNumber ) throws Exception {
    //Simulate a new JVM
    ServerPortRegistry.clear();
    PortFileManager.getInstance().clear();

    //Now start up the instance
    final KarafInstance instance =
      new KarafInstance( TEST_CACHE_FOLDER, "./src/test/resources/KarafInstanceTest/KarafPorts.yaml", "default" );
    instance.assignPortsAndCreateCache();

    assertEquals( "NOTE: this test will fail if Karaf is running elsewhere on the system",
      expectedInstanceNumber, instance.getInstanceNumber() );
    assertTrue( instance.getCachePath().endsWith( "data-" + expectedInstanceNumber ) );
    for ( String id : instance.getPortIds() ) {
      assertEquals( instance.getPort( id ).getAssignedPort(),
        Integer.valueOf( System.getProperty( instance.getPort( id )
          .getPropertyName() ) ) );
    }
    File cacheFolder = new File( instance.getCachePath() );
    assertTrue( cacheFolder.exists() );

    testPortFile( instance );
    return instance;
  }

  private void testPortFile( KarafInstance instance ) throws IOException {
    List<Integer> instancePorts = new ArrayList<Integer>();
    for ( KarafInstancePort port : instance.getPorts() ) {
      instancePorts.add( port.getAssignedPort() );
    }
    PortFileManagerTest.testPortsWereWrittenToPortFile( instance.getCachePath() + "/" + USED_PORT_FILENAME,
      instancePorts );
  }
}
