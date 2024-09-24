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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.settings;

import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


/**
 * 
 * @author Joao L. M. Pereira
 *
 */
public class PortFileManagerTest {

  @Test
  public void testAddAndRemovePorts() throws Exception {
    PortFileManager.instance = null;
    PortFileManager portFileManager = PortFileManager.getInstance();
    // Add mockPorts
    Integer[] mockPorts = new Integer[] { 1, 2, 3, 4, 5, 15, 20 };
    for ( Integer mockPort : mockPorts ) {
      assertTrue( portFileManager.addPort( mockPort ) );
    }

    assertTrue( portFileManager.addPort( mockPorts[0] ) == false );

    // Remove mockPorts, and verify if the port existed
    assertTrue( portFileManager.removePort( 1 ) );
    assertTrue( portFileManager.removePort( 3 ) );
    assertTrue( portFileManager.removePort( 15 ) );
    assertTrue( portFileManager.removePort( 6 ) == false );
    assertTrue( portFileManager.removePort( 100 ) == false );
    assertTrue( portFileManager.removePort( null ) == false );
  }

  @Test
  public void testWriteUsedPortFile() throws FileNotFoundException, IOException {
    PortFileManager.instance = null;
    PortFileManager portFileManager = PortFileManager.getInstance();
    Integer[] mockPorts = new Integer[] { 1, 2, 3, 4, 5, 15, 20 };
    for ( Integer mockPort : mockPorts ) {
      portFileManager.addPort( mockPort );
    }
    String testFolder = "./bin/test/cacheTest";
    File folder = new File( testFolder );
    if ( !folder.exists() ) {
      folder.mkdirs();
    }
    // Write the mockPorts to file
    portFileManager.writeUsedPortFile( testFolder + "/testUsedPorts" );

    // verify that the mockPorts were well written
    @SuppressWarnings( "unchecked" )
    List<Integer> mockPortsList = new ArrayList<Integer>( Arrays.asList( mockPorts ) );
    testPortsWereWrittenToPortFile( testFolder + "/testUsedPorts", mockPortsList );
  }

  public static void testPortsWereWrittenToPortFile( String portFilePath, Collection<Integer> argPortsList )
    throws IOException {
    List<Integer> portsList = new ArrayList<Integer>( argPortsList );
    FileInputStream usedPortFile = null;
    DataInputStream dis = null;
    try {
      usedPortFile = new FileInputStream( portFilePath );
      dis = new DataInputStream( usedPortFile );
      StringBuilder fileContent = new StringBuilder();

      while ( dis.available() > 0 ) {
        fileContent.append( dis.readUTF() );
      }
      String[] stringPorts = fileContent.toString().split( "," );
      for ( String stringPort : stringPorts ) {
        Integer portFromFile = Integer.parseInt( stringPort.trim() );
        assertTrue( portsList.remove( portFromFile ) );
      }

    } catch ( IOException e ) {
      Assert.fail( e.getMessage() );
    } finally {
      if ( usedPortFile != null ) {
        usedPortFile.close();
      }
      if ( dis != null ) {
        dis.close();
      }
    }
  }

}
