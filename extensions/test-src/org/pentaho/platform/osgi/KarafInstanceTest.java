/*
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
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.osgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.settings.PortAssigner;
import org.pentaho.platform.settings.ServerPortRegistry;

/**
 * 
 * @author tkafalas
 *
 */
public class KarafInstanceTest {
  private String TEST_CACHE_FOLDER = "./bin/test/cacheTest";

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
      FileUtils.deleteDirectory( folder );
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
    KarafInstancePortFactory.karafInstance = null;
    PortAssigner.getInstance().clear();
    ServerPortRegistry.clear();
    
    //Now start up the instance
    KarafInstance instance = new KarafInstance( TEST_CACHE_FOLDER );
    new KarafInstancePortFactory( "./test-res/KarafInstanceTest/KarafPorts.yaml" ).process();
    instance.start();

    assertEquals( expectedInstanceNumber, instance.getInstanceNumber() );
    assertTrue( instance.getCachePath().endsWith( "data" + expectedInstanceNumber ) );
    for ( String id : instance.getPortIds() ) {
      assertEquals( instance.getPort( id ).getValue(), Integer.valueOf( System.getProperty( instance.getPort( id )
          .getPropertyName() ) ) );
    }
    File cacheFolder = new File( instance.getCachePath() );
    assertTrue( cacheFolder.exists() );
    return instance;
  }
}
