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

package org.pentaho.platform.web.http.api.resources.services;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;

import java.io.IOException;

/**
 * Service class for checking authentication provider level
 */
public class FeatureValidationService {

  protected ISystemConfig systemConfig;

  public FeatureValidationService( ISystemConfig systemConfig ) {
    this.systemConfig = systemConfig;
  }

  /**
   * Check if the feature is enabled for a authentication provider
   *
   * @param feature the feature to check
   * @return true if the feature is enabled, false otherwise
   * @throws IOException if an error occurs while reading the configuration
   */
  public boolean isFeatureEnabled( String feature ) throws IOException {
    IConfiguration config = this.systemConfig.getConfiguration( "security" );
    String provider = config.getProperties().getProperty( "provider" );

    switch ( feature ) {
      case "activate":
        return StringUtils.equals( "jackrabbit", provider );
      case "process":
      case "change_password":
        return StringUtils.equals( "jackrabbit", provider ) || StringUtils.equals( "super", provider );
      default:
        return false;
    }
  }

}
