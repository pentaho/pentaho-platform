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

import java.util.Set;

import org.eclipse.jetty.util.ConcurrentHashSet;

/**
 * This serves to group {@link ServerPort} objects under an arbitrary name. It also allows a user friendly description
 * of the service category to be supplied. Some web services will provide information on ServerPorts grouped by the
 * {@link Service} they are under. To fully activate the Service it must be registered with
 * {@link ServerPortRegistry#addService(Service)}
 * 
 * @author tkafalas
 *
 */
public class Service {

  private String serviceName;
  private String serviceDescription;
  private ConcurrentHashSet<ServerPort> serverPorts = new ConcurrentHashSet<ServerPort>();

  public Service( String serviceName, String serviceDescription ) {
    this.serviceName = serviceName;
    this.serviceDescription = serviceDescription;
  }

  void add( ServerPort serverPort ) {
    if ( serverPort.getServiceName() != serviceName ) {
      throw new IllegalStateException( "Attempt to add a port with a different service name" );
    }
    serverPorts.add( serverPort );
  }

  Set<ServerPort> getServerPorts() {
    return serverPorts;
  }

  public String getServiceDescription() {
    return serviceDescription;
  }

  public void setServiceDescription( String serviceDescription ) {
    this.serviceDescription = serviceDescription;
  }

  public String getServiceName() {
    return serviceName;
  }

}
