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
