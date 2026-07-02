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


package org.pentaho.platform.web.http;

import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.platform.engine.core.system.PentahoSystem;

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
    } catch ( Exception e ) {
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
    } catch ( Exception e ) {
      return null;
    }

  }

}
