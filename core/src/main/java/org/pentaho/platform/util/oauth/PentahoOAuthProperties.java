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

package org.pentaho.platform.util.oauth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class PentahoOAuthProperties {

  private static final Log logger = LogFactory.getLog( PentahoOAuthProperties.class );

  Properties properties;

  private static ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );

  public String getValue( String key ) {

    if ( Objects.isNull( properties ) ) {
      IConfiguration config = systemConfig.getConfiguration( "oauth" );

      try {
        properties = config.getProperties();
      } catch ( IOException e ) {
        properties = new Properties();
        logger.error( e );
      }
    }

    return properties.getProperty( key );
  }

}
