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
