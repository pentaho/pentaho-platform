/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository2.unified;

import java.util.Map;

/**
 * Repository Content Converter Handler manages the repository content converter in the platform
 * 
 * 
 * @author rmansoor
 * 
 */
public interface IRepositoryContentConverterHandler {

  /**
   * Retrieves a map of content converters. Key is the extension and the value is the converter for that extension.
   * 
   * @return map of converters and extensions
   */
  Map<String, Converter> getConverters();

  /**
   * Retrieves a particular converter based on the extension provided
   * 
   * @param extension
   * @return content converter
   */
  Converter getConverter( String extension );

  /**
   * Adds a new converter with the extension to the platform
   * 
   * @param extension
   * @param converter
   */
  void addConverter( String extension, Converter converter );

}
