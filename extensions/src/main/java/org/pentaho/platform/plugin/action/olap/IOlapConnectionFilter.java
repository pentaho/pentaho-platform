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


package org.pentaho.platform.plugin.action.olap;

import java.util.Properties;

/**
 * This interface can be implemented to modify the connection properties
 * before a connection is made.
 */
public interface IOlapConnectionFilter {
  /**
   * Will be called before opening an olap connection. Note that
   * 'user' and 'password' properties might get overridden downstream
   * if they have been set when creating the connection with the
   * {@link IOlapService}.
   * @param properties Properties used for the connection.
   */
  void filterProperties( Properties properties );
}
