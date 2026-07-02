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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;
import org.pentaho.platform.settings.Service;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * 
 * @author tkafalas
 *
 */
public class KarafInstancePortFactoryTest {


  public static final String TEST_RES = "src/test/resources";

  @Before
  public void initialize() {
    ServerPortRegistry.clear();
  }

  @Test
  public void processTest() throws IOException, KarafInstanceResolverException {
    ArgumentCaptor<Service> serviceCaptor = ArgumentCaptor.forClass( Service.class );

    KarafInstance instance = new KarafInstance("./" + TEST_RES + "/KarafInstanceTest", "./" + TEST_RES + "/KarafInstanceTest/KarafPorts.yaml", "default" );

    boolean port1, port2, port3, port4;
    port1 = port2 = port3 = port4 = false;
    for ( ServerPort port : ServerPortRegistry.getPorts() ) {
      KarafInstancePort prop = (KarafInstancePort) port;
      switch ( prop.getId() ) {
        case "KARAF_PORT":
          port1 = true;
          assertEquals( "karaf.port", prop.getPropertyName() );
          assertEquals( "Karaf Port", prop.getFriendlyName() );
          KarafInstancePort portProp = prop;
          assertEquals( new Integer( 18801 ), portProp.getStartPort() );
          assertEquals( "karaf", prop.getServiceName() );
          break;

        case "OSGI_SERVICE_PORT":
          port2 = true;
          break;

        case "RMI_SERVER_PORT":
          port3 = true;
          break;

        case "RMI_REGISTRY_PORT":
          port4 = true;
          break;

        default:
          fail( "Registered Unknown ID " + prop.getId() );
      }
    }

    // All 4 records came in?
    assertTrue( "All port records did not come in", port1 && port2 && port3 && port4 );

    boolean service1, service2;
    service1 = service2 = false;
    for ( Service service : ServerPortRegistry.getServices() ) {
      switch ( service.getServiceName() ) {
        case "karaf":
          service1 = true;
          assertEquals( "Karaf service ports", service.getServiceDescription() );
          break;
        case "rmi":
          service2 = true;
          break;

        default:
          fail( "Registered Unknown service " + service.getServiceName() );

      }
    }

    assertTrue( "All service records did not come in", service1 && service2 );
  }
}
