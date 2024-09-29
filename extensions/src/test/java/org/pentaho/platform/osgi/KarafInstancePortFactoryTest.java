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
