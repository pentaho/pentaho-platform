package org.pentaho.platform.osgi;

import org.junit.Test;
import static junit.framework.Assert.assertTrue;

public class PortAssignerTest {

  @Test
  public void testPortAssignnment() throws Exception {
    int numberOfPorts = 15;
    PortAssigner portAssigner = new PortAssigner( numberOfPorts );
    int[] ports = portAssigner.assignPorts();
    for ( int i = 0; i < numberOfPorts; i++ ) {
      assertTrue ( "ports should be greater than zero", ports[i] > 0 );
      int j = i + 1;
      while ( j < numberOfPorts ) {
        assertTrue ( "More than one port with value of " + ports[i], ports[i] != ports[j] );
        j++;
      }
    }
  }
}
