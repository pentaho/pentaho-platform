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
  private Integer assignedPort;
  private String friendlyName;
  private Integer startPort;


  /**
   * 
   * @param id
   *          A unique Id associated with this port
   * @param friendlyName
   *          A friendly descriptive name associated with this port
   * @param startPort
   *          The first port number in a range of available ports. If null an unused port ill be assigned.
   */
  public ServerPort( String id, String friendlyName, Integer startPort ) {
    this.id = id;
    this.friendlyName = friendlyName;
    this.startPort = startPort;
  }

  /**
   * 
   * @param id
   *          A unique Id associated with this port
   * @param friendlyName
   *          A friendly descriptive name associated with this port
   * @param startPort
   *          The first port number in a range of available ports. If null an unused port ill be assigned.
   * @param serviceName
   *          The serviceName associated with this port
   */
  public ServerPort( String id, String friendlyName, Integer startPort, String serviceName ) {
    this( id, friendlyName, startPort );
    this.serviceName = serviceName;
  }

  public String getId() {
    return id;
  }

  public Integer getAssignedPort() {
    return assignedPort;
  }

  public void setAssignedPort( Integer assignedPort ) {
    // Removes the old port and adds the new one into the PortFileManager
    PortFileManager.getInstance().removePort( this.assignedPort );
    PortFileManager.getInstance().addPort( assignedPort );
    this.assignedPort = assignedPort;
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

  public String getServiceName() {
    return serviceName;
  }

  /**
   * Free's up the port resource for re-use. Call this method when the port can be reused.
   */
  public void releasePort() {
    PortFileManager.getInstance().removePort( assignedPort );
    assignedPort = null;
  }

}
