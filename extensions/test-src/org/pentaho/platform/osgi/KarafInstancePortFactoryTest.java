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
 * Copyright 2015 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.osgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;
import org.pentaho.platform.settings.Service;
import org.yaml.snakeyaml.Yaml;

/**
 * 
 * @author tkafalas
 *
 */
public class KarafInstancePortFactoryTest {


  @Before
  public void initialize() {
    ServerPortRegistry.clear();
  }

  @Test
  public void processTest() throws IOException, KarafInstanceResolverException {
    ArgumentCaptor<Service> serviceCaptor = ArgumentCaptor.forClass( Service.class );

    KarafInstance instance = new KarafInstance( "./test-res/KarafInstanceTest", "./test-res/KarafInstanceTest/KarafPorts.yaml", "default" );

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
