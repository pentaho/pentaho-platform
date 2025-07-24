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


package org.pentaho.platform.settings;

import java.util.HashSet;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

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
