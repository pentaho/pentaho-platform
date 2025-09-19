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


package org.pentaho.platform.api.engine;

import java.io.IOException;

/**
 * User: nbaker Date: 4/2/13
 */
public interface ISystemConfig {
  IConfiguration getConfiguration( String configId );

  String getProperty( String placeholder );

  /**
   * Gets the value of a property if it is defined, falling back to a given default value, otherwise.
   *
   * @param placeholder A property placeholder in the format {@code "configId.property"}.
   * @param defaultValue The value returned when a property is not defined, i.e.,
   *                     the value returned by {@link IConfiguration#getProperties()} and then by
   *                     {@link java.util.Properties#getProperty(String)} is {@code null}.
   * @return The value of the given property, when defined; {@code null} otherwise.
   */
  default String getProperty( String placeholder, String defaultValue ) {
    String value = getProperty( placeholder );
    return value != null ? value : defaultValue;
  }

  void registerConfiguration( IConfiguration configuration ) throws IOException;

  IConfiguration[] listConfigurations();

}
