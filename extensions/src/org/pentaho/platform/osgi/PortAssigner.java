package org.pentaho.platform.osgi;

import java.io.IOException;
import java.net.ServerSocket;

public class PortAssigner {
  private int numberOfPorts;

  public PortAssigner( int numberOfPorts ) {
    this.numberOfPorts = numberOfPorts;
  }

  public int[] assignPorts() {
    final ServerSocket[] availableSockets = new ServerSocket[numberOfPorts];
    
    final int[] openPorts = new int[numberOfPorts];
    for ( int i = 0; i < numberOfPorts; i++ ) {
      availableSockets[i] = findFreeSocket();
      openPorts[i] = availableSockets[i].getLocalPort();
    }
    //We got all the open ports, now close the sockets
    for ( int i = 0; i < numberOfPorts; i++ ) {
      try {
        availableSockets[i].close();
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
    return openPorts;
  }

  private ServerSocket findFreeSocket() {
    ServerSocket socket = null;
    try {
      socket = new ServerSocket( 0 );
      socket.setReuseAddress( true );
      return socket;
    } catch ( IOException e ) {
      throw new IllegalStateException( "No sockets available for assignment" );
    }
  }
}
