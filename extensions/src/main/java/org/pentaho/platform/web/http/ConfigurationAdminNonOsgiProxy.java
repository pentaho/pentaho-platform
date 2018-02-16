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

package org.pentaho.platform.web.http;

import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Dictionary;

/**
 * This class serves as a away to gain access to OSGI ConfigurationAdmin capabilities,
 * more precisely to the configuration properties.
 *
 * We used reflection, here to avoid the problem that arises in runtime
 * where the implementation returned by `PentahoSystem.get( ConfigurationAdmin.class )`
 * is the one present in the OSGI side (`org.apache.felix.cm.impl.ConfigurationAdminImpl`)
 * and not the one present in the legacy side (`org.osgi.service.cm.ConfigurationAdmin`),
 * which is the one we are actually importing and trying to use.
 */
public class ConfigurationAdminNonOsgiProxy {
  private Object configurationAdmin;

  public ConfigurationAdminNonOsgiProxy() {
    this.configurationAdmin = PentahoSystem.get( ConfigurationAdmin.class );
  }

  /**
   * Wraps the call to `getProperties` method from the configuration related to the given {@code persistenceID }.
   *
   * @param persistenceID - configuration's file id.
   *
   * @return the properties in the configuration file, or `null` if none is found.
   */
  public Dictionary<String, Object> getProperties( String persistenceID ) {
    Object configuration = getConfiguration( persistenceID );
    if ( configuration == null ) {
        return null;
    }

    try {
      Method getProperties = configuration.getClass().getMethod( "getProperties" );

      return (Dictionary<String, Object>) getProperties.invoke( configuration );
    } catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
      return null;
    }

  }

  /**
   * Wraps the call to `getConfiguration` method from the OSGI configuration admin.
   *
   * @param persistenceID - configuration's file id.
   *
   * @return the configuration related to the given {@code persistenceID }, or `null` if none is found.
   */
  private Object getConfiguration( String persistenceID ) {
    try {
      Method getConfiguration = this.configurationAdmin.getClass().getMethod( "getConfiguration", String.class );

      return getConfiguration.invoke( this.configurationAdmin, persistenceID );
    } catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
      return null;
    }

  }

}
