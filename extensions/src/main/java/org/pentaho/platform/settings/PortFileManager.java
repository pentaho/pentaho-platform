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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.HashSet;

/**
 * PortFileManager is a singleton that saves the ports in use and creates a file with a ports list when requested.
 * 
 * @author Joao L. M. Pereira
 *
 */
public class PortFileManager {

  // The usedPorts variable accumulates ports that have already been assigned
  // It is also used to create a list of ports in use to store in a file
  private final HashSet<Integer> usedPorts = new HashSet<Integer>();

  static PortFileManager instance = new PortFileManager();

  private PortFileManager() {
  }

  /**
   * Returns the PortFileManager instance, if it does not exists, creates a new one
   * 
   * @return the PortFileManager instance, it is a singleton
   */
  public static PortFileManager getInstance() {
    if ( instance == null ) {
      instance = new PortFileManager();
    }
    return instance;
  }

  /**
   * Add a new used port
   * 
   * @param port
   *          a port being used
   * @return true, if the port did not exist before
   */
  public boolean addPort( Integer port ) {
    return usedPorts.add( port );
  }

  /**
   * Removes a used port
   * 
   * @param port
   *          a port that was used, not anymore
   * @return true, if the port exist before
   */
  public boolean removePort( Integer port ) {
    return usedPorts.remove( port );
  }

  /**
   * Writes a port list delimited by comma into a specified file
   * 
   * @param filePath
   *          Path for the file to be written
   * @throws FileNotFoundException
   * @throws IOException
   */
  public void writeUsedPortFile( String filePath ) throws FileNotFoundException, IOException {
    // Create a list of comma separated port numbers
    StringBuilder sb = new StringBuilder();
    for ( Integer port : usedPorts ) {
      if ( sb.length() > 0 ) {
        sb.append( "," );
      }
      sb.append( port.toString() );
    }

    // Write it to file.
    File usedPortFile = new File( filePath );
    usedPortFile.getParentFile().mkdirs();
    RandomAccessFile fileStore = new RandomAccessFile( usedPortFile, "rw" );
    try {
      getLock( fileStore );
      fileStore.writeUTF( sb.toString() );
    } finally {
      fileStore.close();
    }
    return;
  }

  // Try to get a lock for 20 seconds
  private FileLock getLock( RandomAccessFile fileStore ) {
    int retry = 0;
    FileLock lock = null;
    try {
      do {
        lock = fileStore.getChannel().tryLock();
        if ( lock != null ) {
          return lock;
        }
        Thread.sleep( 1000 );
        retry++;
      } while ( retry < 20 );
      return null;
    } catch ( Exception e ) {
      return null;
    }
  }

  /**
   * Used for testing, simulates a new JVM
   */
  public void clear() {
    usedPorts.clear();
  }
}
