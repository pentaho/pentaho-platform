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

import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;;

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
  private Set<ServerPort> serverPorts = new ConcurrentHashMap().newKeySet();

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
