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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.HashSet;

/**
 * This class is intended as a helper class for the {@link ServerPortRegistry}. While you can use this class to assign
 * an open port, it will not be formally registered with the platform and will not be known to certain web services. The
 * preferred way to get an open port for the platform is to create a {@link ServerPort} object and register it with
 * {@link ServerPortRegistry.addPort}. The physical port number can then be assigned with
 * {@link ServerPort#assignPort()}.
 * 
 * @author tkafalas
 *
 */
public class PortAssigner {

  // The usedPorts variable accumulates ports that have already been assigned
  // by this instance and blocks them being assigned again. Can also be
  // pre-populated to create a list of reversed ports for this server instance.
  private final HashSet<Integer> usedPorts = new HashSet<Integer>();

  // The usedPortsExternal variable keeps ports that were reserved by other
  // server instances. We don't want this server to use them if they have not
  // been opened yet.
  private final HashSet<Integer> usedPortsExternal = new HashSet<Integer>();

  static PortAssigner instance = new PortAssigner();

  private PortAssigner() {
  }

  public static PortAssigner getInstance() {
    if ( instance == null ) {
      instance = new PortAssigner();
    }
    return instance;
  }

  public int[] assignPorts( int numberOfPorts ) throws IOException {

    final int[] openPorts = new int[numberOfPorts];
    for ( int i = 0; i < numberOfPorts; i++ ) {
      do {
        ServerSocket socket;
        do {
          socket = findFreeSocket();
          socket.close();
        } while ( usedPortsExternal.contains( socket.getLocalPort() ) );
        openPorts[i] = socket.getLocalPort();
      } while ( !usedPorts.add( openPorts[i] ) );
    }
    return openPorts;
  }

  public int getFreePort() throws IOException {
    int port;
    do {
      ServerSocket socket = findFreeSocket();
      port = socket.getLocalPort();
      try {
        socket.close();
      } catch ( IOException e ) {
        // Can't imagine why we can't close the port we just opened.
        // If someone manually closed the channel, ignore it.
        e.printStackTrace();
      }
    } while ( usedPortsExternal.contains( port ) || !usedPorts.add( port ) );
    return port;
  }

  /**
   * Return the first free port number within a range. If no port is found in the range, returns any unused port not
   * present in the <code>getUsedPorts()</code> or <code>getUsedPortsExternal()</code> list.
   * 
   * @param startPort
   *          lowest port number in range
   * @param endPort
   *          highest port number in range
   * @return
   * @throws IOException
   *           If port could not be obtained.
   * @throws IllegalStateException
   *           if start or end ports are invalid.
   */
  public int getFreePortFromRange( int startPort, int endPort ) throws IOException {
    if ( startPort <= 0 || endPort < startPort ) {
      throw new IllegalArgumentException( "StartPort must be positive and less than endPort" );
    }
    for ( int port = startPort; port <= endPort; port++ ) {
      if ( !isPortInUse( port ) ) {
        usedPorts.add( port );
        return port;
      }
    }
    return getFreePort();
  }

  private ServerSocket findFreeSocket() throws IOException {
    ServerSocket socket = null;
    socket = new ServerSocket( 0 );
    socket.setReuseAddress( true );
    return socket;
  }

  /**
   * Check if the port is in use, ( physically opened, or in the used lists)
   */
  private boolean isPortInUse( int port ) {
    if ( usedPortsExternal.contains( port ) || usedPorts.contains( port ) ) {
      return true;
    }
    ServerSocket socket = null;
    DatagramSocket dsSocket = null;
    try {
      socket = new ServerSocket( port );
      socket.setReuseAddress( true );
      dsSocket = new DatagramSocket( port );
      dsSocket.setReuseAddress( true );
      return false;
    } catch ( IOException e ) {
      // port is in use
      return true;
    } finally {
      if ( dsSocket != null ) {
        dsSocket.close();
      }
      try {
        if ( socket != null ) {
          socket.close();
        }
      } catch ( Exception e ) {
        // Should not happen
      }
    }
  }

  /**
   * 
   * @return A list of ports assigned by this instance. Add ports to this HashSet to block ports from being used.
   */
  public HashSet<Integer> getUsedPorts() {
    return usedPorts;
  }

  /**
   * 
   * @return A cloned list of ports assigned by external server instances (other JVMs).
   */
  public HashSet<Integer> getUsedPortsExternal() {
    return new HashSet<Integer>( usedPortsExternal );
  }

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

  public void readUsedPortFile( String filePath ) throws FileNotFoundException, IOException {
    // Read File to get used Ports
    File usedPortFile = new File( filePath );
    RandomAccessFile fileStore = new RandomAccessFile( usedPortFile, "r" );
    String ports = "";
    try {
      // getLock( fileStore);
      ports = fileStore.readUTF();
    } catch ( OverlappingFileLockException e ) {
      // File is already locked in this thread or virtual machine. This should not happen.
    } finally {
      fileStore.close();
    }

    String[] portArray = ports.split( "," );
    for ( String port : portArray ) {
      usedPortsExternal.add( Integer.parseInt( port ) );
    }
    return;
  }

  /**
   * Clear all open ports.  Assumes all ports have been released.
   * Used for Junit test to simulate a new JVM.
   */
  public void clear() {
    usedPorts.clear();
    usedPortsExternal.clear();
  }
}
