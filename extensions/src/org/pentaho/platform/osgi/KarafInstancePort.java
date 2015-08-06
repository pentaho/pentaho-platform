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

package org.pentaho.platform.osgi;

import org.pentaho.platform.settings.ServerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author tkafalas
 *
 */
public class KarafInstancePort extends ServerPort {

  private String propertyName; // property name associated with the port

  public KarafInstancePort( String id, String propertyName, String friendlyName, int startPort, int endPort, String serviceName ) {
    super( id, friendlyName, startPort, endPort, serviceName );
    this.propertyName = propertyName;
  }

  public String getPropertyName() {
    return propertyName;
  }
  
  /**
   * Assigns an port number from the range defined. If no port number is available from the defined range an arbitrary
   * free port will be assigned.
   * 
   * @return The port number allocated.
   */
  @Override
  public Integer assignPort() {
    super.assignPort();
    System.setProperty( propertyName, "" + getValue() );
    return getValue();
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
