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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "Service" )
@XmlAccessorType( XmlAccessType.PROPERTY )
public class ServiceDto {
  private static final long serialVersionUID = 0;
  private String serviceName;
  private String serviceDescription;
  private HashSet<ServerPortDto> serverPorts = new HashSet<ServerPortDto>();

  public ServiceDto() {

  }

  public ServiceDto( Service service ) {
    this.serviceName = service.getServiceName();
    this.serviceDescription = service.getServiceDescription();
    for ( ServerPort serverPort : service.getServerPorts() ) {
      this.serverPorts.add( new ServerPortDto( serverPort ) );
    }
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName( String serviceName ) {
    this.serviceName = serviceName;
  }

  public String getServiceDescription() {
    return serviceDescription;
  }

  public void setServiceDescription( String serviceDescription ) {
    this.serviceDescription = serviceDescription;
  }

  public HashSet<ServerPortDto> getServerPorts() {
    return serverPorts;
  }

  public void setServerPorts( HashSet<ServerPortDto> serverPorts ) {
    this.serverPorts = serverPorts;
  }

}
