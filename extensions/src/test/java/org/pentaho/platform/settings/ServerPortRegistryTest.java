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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ServerPortRegistryTest {
  @Test
  public void addPortTest() {
    ServerPortRegistry.clear();
    Service service = new Service( "foo", "foo description" );
    ServerPortRegistry.addService( service );

    ServerPort serverPort1 = new ServerPort( "id1", "description", 60001, "foo" ); // fully specified
    ServerPort serverPort2 = new ServerPort( "id2", "description", 60101, "bla" ); // No service pre-defined
    ServerPort serverPort3 = new ServerPort( "id3", "description", 60201 ); // No service name defined
    ServerPortRegistry.addPort( serverPort1 );
    ServerPortRegistry.addPort( serverPort2 );
    ServerPortRegistry.addPort( serverPort3 );

    assertEquals( serverPort1, ServerPortRegistry.getPort( "id1" ) );
    assertEquals( serverPort2, ServerPortRegistry.getPort( "id2" ) );
    assertEquals( serverPort3, ServerPortRegistry.getPort( "id3" ) );

    assertEquals( "Unknown Description", ServerPortRegistry.getService(
        ServerPortRegistry.getPort( "id2" ).getServiceName() ).getServiceDescription() );
    assertEquals( "", ServerPortRegistry.getService( ServerPortRegistry.getPort( "id3" ).getServiceName() )
        .getServiceName() );
    assertEquals( "Unknown Description", ServerPortRegistry.getService(
        ServerPortRegistry.getPort( "id3" ).getServiceName() ).getServiceDescription() );
  }

  @Test
  public void addServiceTest() {
    ServerPortRegistry.clear();
    Service service = new Service( "foo", "foo description" );
    ServerPortRegistry.addService( service );

    Service readService = ServerPortRegistry.getService( "foo" );
    assertEquals( service.getServiceDescription(), readService.getServiceDescription() );
  }

  @Test
  public void removePortTest() {
    ServerPortRegistry.clear();
    Service service = new Service( "foo", "foo description" );
    ServerPortRegistry.addService( service );
    ServerPort serverPort1 = new ServerPort( "id1", "description", 60001, "foo" );
    ServerPort serverPort2 = new ServerPort( "id2", "description", 60101, "foo" );
    ServerPortRegistry.addPort( serverPort1 );
    ServerPortRegistry.addPort( serverPort2 );

    assertEquals( 2, ServerPortRegistry.getService( "foo" ).getServerPorts().size() );
    ServerPortRegistry.removePort( serverPort1 );
    assertEquals( 1, ServerPortRegistry.getService( "foo" ).getServerPorts().size() );
    assertEquals( null, ServerPortRegistry.getPort( "id1" ) );

  }

}
