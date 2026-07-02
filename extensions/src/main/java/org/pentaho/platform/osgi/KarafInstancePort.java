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


package org.pentaho.platform.osgi;

import org.pentaho.platform.settings.ServerPort;

/**
 * @author tkafalas
 */
public class KarafInstancePort extends ServerPort {

  private String propertyName; // property name associated with the port
  private KarafInstance karafInstance;

  public KarafInstancePort( KarafInstance karafInstance, String id, String propertyName, String friendlyName,
                            int startPort, String serviceName ) {
    super( id, friendlyName, startPort, serviceName );
    this.propertyName = propertyName;
    this.karafInstance = karafInstance;
  }

  public String getPropertyName() {
    return propertyName;
  }

  @Override public void setAssignedPort( Integer assignedPort ) {
    super.setAssignedPort( assignedPort );
    System.setProperty( propertyName, getAssignedPort().toString() );
  }

  /**
   * Free's up the port resources. Call this method when the port can be reused.
   */
  @Override
  public void releasePort() {
    super.releasePort();
    System.getProperties().remove( propertyName );
  }
}
