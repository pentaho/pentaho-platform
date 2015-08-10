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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.platform.settings.Service;

/**
 * 
 * @author tkafalas
 *
 */
public class KarafInstancePortFactoryTest {
  KarafInstance mockInstance = mock( KarafInstance.class );

  @Before
  public void initialize() {
    KarafInstancePortFactory.karafInstance = mockInstance;
  }

  @Test
  public void parseLineTest() throws Exception {
    KarafInstancePortFactory importer =
        new KarafInstancePortFactory( "./test-res/KarafInstanceTest/KarafPorts.csv" );

    String[] fields;
    fields = importer.parseLine( "property,id,  property.name, friendly name" );
    assertEquals( "property", fields[0] );
    assertEquals( "id", fields[1] );
    assertEquals( "property.name", fields[2] );
    assertEquals( "friendly name", fields[3] );

    fields = importer.parseLine( "\"property\",\"id\",\"property.name\",\"friendly,name,with,commas\"" );
    assertEquals( "property", fields[0] );
    assertEquals( "id", fields[1] );
    assertEquals( "property.name", fields[2] );
    assertEquals( "friendly,name,with,commas", fields[3] );
    assertEquals( null, fields[4] );
    assertEquals( null, fields[5] );
    assertEquals( null, fields[6] );

  }

  /**
   * File Contains<p>
   * <code>service,FOO,service description <p>
   *  port,"ZILCH_PORT","org.pentaho.zilch","Zilch Port", 61111, 61112</code>
   **/
  @Test
  public void processTest() throws FileNotFoundException, IOException {
    ArgumentCaptor<KarafInstancePort> portCaptor = ArgumentCaptor.forClass( KarafInstancePort.class );
    ArgumentCaptor<Service> serviceCaptor = ArgumentCaptor.forClass( Service.class );

    KarafInstancePortFactory importer = new KarafInstancePortFactory( "./test-res/KarafInstanceTest/PropertyTest.csv" );
    importer.process();
    verify( mockInstance, times( 1 ) ).registerPort( portCaptor.capture() );
    verify( mockInstance, times( 1 ) ).registerService( serviceCaptor.capture() );

    for ( KarafInstancePort prop : portCaptor.getAllValues() ) {
      switch ( prop.getId() ) { //So we can expand test to multiple records
        case "ZILCH_PORT":
          assertEquals( "org.pentaho.zilch", prop.getPropertyName() );
          assertEquals( "Zilch Port", prop.getFriendlyName() );
          KarafInstancePort portProp = (KarafInstancePort) prop;
          assertEquals( new Integer( 61111 ), portProp.getStartPort() );
          assertEquals( new Integer( 61112 ), portProp.getEndPort() );
          break;
        default:
          fail( "Registered Unknown ID " + prop.getId() );
      }
    }

    for ( Service service : serviceCaptor.getAllValues() ) {
      switch ( service.getServiceName() ) {
        case "FOO":
          assertEquals("service description", service.getServiceDescription() );
          break;

        default:
          fail( "Registered Unknown service " + service.getServiceName() );

      }
    }
  }
}
