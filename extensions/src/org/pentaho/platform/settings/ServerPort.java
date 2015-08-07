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

package org.pentaho.platform.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This immutable class contains information on variable port that is assigned by this class. This class will use the
 * <code>PortAssigner</code> to assign a port from the defined range, when the {@link #assignPort()} is called. The port
 * will be considered reserved for the duration of JVM, unless the {@link #releasePort()} is called.
 * 
 * @author tkafalas
 *
 */
public class ServerPort {
  private String id;
  private String serviceName = ""; // default Service Name to empty string
  private Integer value;
  private String friendlyName;
  private Integer startPort;
  private Integer endPort;
  Logger logger = LoggerFactory.getLogger( getClass() );

  /**
   * 
   * @param id
   *          A unique Id associated with this port
   * @param friendlyName
   *          A friendly descriptive name associated with this port
   * @param startPort
   *          The first port number in a range of available ports. If null an unused port ill be assigned.
   * @param endPort
   *          The last port number in a range of available ports. If null and the startPort then there is no upper
   *          boundary.
   */
  public ServerPort( String id, String friendlyName, Integer startPort, Integer endPort ) {
    this.id = id;
    this.friendlyName = friendlyName;
    this.startPort = startPort;
    this.endPort = endPort;
  }

  /**
   * 
   * @param id
   *          A unique Id associated with this port
   * @param friendlyName
   *          A friendly descriptive name associated with this port
   * @param startPort
   *          The first port number in a range of available ports. If null an unused port ill be assigned.
   * @param endPort
   *          The last port number in a range of available ports. If null and the startPort then there is no upper
   *          boundary.
   * @param serviceName
   *          The serviceName associated with this port
   */
  public ServerPort( String id, String friendlyName, Integer startPort, Integer endPort, String serviceName ) {
    this( id, friendlyName, startPort, endPort );
    this.serviceName = serviceName;
  }

  public String getId() {
    return id;
  }

  public Integer getValue() {
    return value;
  }

  public String getFriendlyName() {
    return friendlyName == null || friendlyName.trim().length() == 0 ? id : friendlyName;
  }

  protected void setFriendlyName( String friendlyName ) {
    this.friendlyName = friendlyName;
  }

  public Integer getStartPort() {
    return startPort;
  }

  public Integer getEndPort() {
    return endPort;
  }

  public String getServiceName() {
    return serviceName;
  }

  /**
   * Assigns a port number from the range defined. If no port number is available from the defined range an arbitrary
   * free port will be assigned.  It is illegal to assign a port number if the ServerPort has not been registered with
   * the {@link ServerPortRegistry}.
   * 
   * @return The port number allocated.
   * @throws IllegalStateException If the port cannot be obtained or the object has not been registered with the {@link ServerPortRegistry}.
   */
  public Integer assignPort() throws IllegalStateException {
    if ( ServerPortRegistry.getPort(id) != this ){
      throw new IllegalStateException("Attempt to assign a port to ServerPort that has not been registered.");
    }
    try {
      if ( startPort != null ) {
        value = PortAssigner.getInstance().getFreePortFromRange( startPort, endPort );
      } else {
        value = PortAssigner.getInstance().getFreePort();
      }
    } catch ( Exception e ) {
      logger.error( "Could not assign port for " + getId(), e );
    }
    return value;
  }

  /**
   * Free's up the port resource for re-use. Call this method when the port can be reused.
   */
  public void releasePort() {
    PortAssigner.getInstance().getUsedPorts().remove( value );
    value = null;
  }

}
