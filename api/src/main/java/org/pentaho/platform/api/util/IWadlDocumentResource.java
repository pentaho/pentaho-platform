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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Exposes a SpringResource Interface to access wadl extension files
 */
public interface IWadlDocumentResource {

  /**
   * Gets the InputStream of the referenced SpringResource
   *
   * @return InputStream with the file that SpringResource points
   * @throws IOException
   */
  InputStream getResourceAsStream() throws IOException;

  /**
   * Identifies if the resource we are fetching is from a plugin or not
   *
   * @return true if the resource is being retrieved from a plugin
   */
  boolean isFromPlugin();

  /**
   * Gets the pluginId where the SpringResource is being retrieved
   *
   * @return String with the pluginId
   */
  String getPluginId();
}
