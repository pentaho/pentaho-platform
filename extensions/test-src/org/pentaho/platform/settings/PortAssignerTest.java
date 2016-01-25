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
package org.pentaho.platform.settings;

import static org.junit.Assert.assertTrue;

import java.io.File;
import org.junit.Test;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * 
 * @author tkafalas
 *
 */
public class PortAssignerTest {

  @Test
  public void testMultiplePortAssignnment() throws Exception {
    PortAssigner.instance = null;
    PortAssigner portAssigner = PortAssigner.getInstance();
    int numberOfPorts = 15;
    int[] ports = portAssigner.assignPorts( numberOfPorts );
    for ( int i = 0; i < numberOfPorts; i++ ) {
      assertTrue( "ports should be greater than zero", ports[i] > 0 );
      assertTrue( portAssigner.getUsedPorts().contains( ports[i] ) );
      int j = i + 1;
      while ( j < numberOfPorts ) {
        assertTrue( "More than one port with value of " + ports[i], ports[i] != ports[j] );
        j++;
      }
    }
  }

  @Test
  public void testSinglePortAssignment() throws Exception {
    PortAssigner.instance = null;
    PortAssigner portAssigner = PortAssigner.getInstance();

    int port = portAssigner.getFreePort();
    assertTrue( port > 0 );
    assertTrue( portAssigner.getUsedPorts().contains( port ) );
  }

  /**
   * This test should cause at least one port returned to be outside the range (because I'm asking for 4 ports but one
   * of the ports is already used).
   * 
   * @throws Exception
   */
  @Test
  public void testRangeOfPorts1() throws Exception {
    PortAssigner.instance = null;
    PortAssigner portAssigner = PortAssigner.getInstance();

    portAssigner.getUsedPorts().add( 60002 );
    int port;
    for ( int i = 0; i < 6; i++ ) {
      port = portAssigner.getFreePortFromRange( 60000, 60003 );
      assertTrue( port != 60002 );
      if ( i > 3 ) {
        assertTrue( port < 60000 || port > 60003 );
      }
    }
  }

  @SuppressWarnings( { "unchecked" } )
  @Test
  public void testUsedPortFile() throws Exception {
    PortAssigner.instance = null;
    PortAssigner portAssigner = PortAssigner.getInstance();
    Integer[] mockPorts = new Integer[] { 1, 2, 3, 4, 5 };
    portAssigner.getUsedPorts().addAll( Arrays.asList( mockPorts ) );
    String testFolder = "./bin/test/cacheTest";
    File folder = new File( testFolder );
    if ( !folder.exists() ) {
      folder.mkdirs();
    }
    // Write the mockPorts to file
    portAssigner.writeUsedPortFile( testFolder + "/testUsedPorts" );

    // read the mockPorts back into new portAssigner instance
    PortAssigner.instance = null;
    portAssigner = PortAssigner.getInstance();
    portAssigner.readUsedPortFile( testFolder + "/testUsedPorts" );

    for ( Integer port : mockPorts ) {
      assertTrue( portAssigner.getUsedPortsExternal().contains( port ) );
    }

  }

}
