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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds a collection of of all server ports that are formally registered to the platform. To get an open
 * port for the platform, create a {@link ServerPort} object and register it with {@link ServerPortRegistry.addPort}.
 * The physical port number can then be assigned with {@link ServerPort#assignPort()}.
 * 
 * @author tkafalas
 *
 */
public class ServerPortRegistry {
  static ServerPortRegistry instance = new ServerPortRegistry();
  private static ConcurrentHashMap<String, ServerPort> serverPorts;
  private static ConcurrentHashMap<String, Service> services;
  private static Logger logger = LoggerFactory.getLogger( ServerPortRegistry.class );

  private ServerPortRegistry() {
    clear();
  }

  /**
   * Add a port to the Server's port registry. To ensure that no other server instance on this host uses the same port,
   * the port should be opened when karaf starts, or, if karaf has started, the socket should be opened immediately
   * after calling this method.
   * 
   * @param serverPort
   */
  public static void addPort( ServerPort serverPort ) {
    if ( serverPorts.get( serverPort.getId() ) != null && serverPorts.get( serverPort.getId() ).getAssignedPort() != null ) {
      throw new IllegalStateException( "Another port has already been assigned with this ID." );
    }

    // add to port list
    serverPorts.put( serverPort.getId(), serverPort );
    String serviceName = serverPort.getServiceName();

    // add to service
    Service service = services.get( serviceName );
    if ( service == null ) {
      logger.warn( "Server Port added with no service.  Adding service without description" );
      // add the service (with no description) on the fly rather than throw an exception
      service = new Service( serverPort.getServiceName(), "Unknown Description" );
      services.put( serviceName, service );
    }
    Set<ServerPort> servicePorts = service.getServerPorts();
    servicePorts.add( serverPort );
  }

  /**
   * Adds a {@link ServicePortService} to the {@link ServerPortRegistry}. ServerPorts can be grouped by
   * Service
   * 
   * @param service
   *          The Service to register. Once registered, ServerPorts can be associated with this
   *          Service.
   */
  public static void addService( Service service ) {
    if ( !services.contains( service.getServiceName() ) ) {
      services.put( service.getServiceName(), service );
    }
  }

  /**
   * Removes a port from the ServerPortRegistry and any Service it belongs to.
   * 
   * @param serverPort
   */
  public static void removePort( ServerPort serverPort ) {
    Service service = services.get( serverPort.getServiceName() );
    service.getServerPorts().remove( serverPort );
    serverPorts.remove( serverPort.getId() );
  }

  /**
   * Clear the service port registry. Should only be called if resources have been closed.
   */
  public static void clear() {
    serverPorts = new ConcurrentHashMap<String, ServerPort>();
    services = new ConcurrentHashMap<String, Service>();
  }

  /**
   * Returns the {@link Service} associated with the given service name or null, if none exist.
   * 
   * @param serviceName
   *          The service name.
   */
  public static Service getService( String serviceName ) {
    return services.get( serviceName );
  }

  /**
   * Returns the {@link ServerPort} associated with the given portId or null, none exist.
   * 
   * @param portId
   *          The id of the server port.
   */
  public static ServerPort getPort( String portId ) {
    return serverPorts.get( portId );
  }

  /**
   * Returns all registered {@link Service}s.
   */
  public static Set<Service> getServices() {
    return new HashSet<Service>( services.values() );
  }

  /**
   * Returns all registered {@link ServerPort}s.
   * 
   * @return
   */
  public static Set<ServerPort> getPorts() {
    return new HashSet<ServerPort>( serverPorts.values() );
  }

}
