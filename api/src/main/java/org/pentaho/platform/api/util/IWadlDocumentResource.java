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
