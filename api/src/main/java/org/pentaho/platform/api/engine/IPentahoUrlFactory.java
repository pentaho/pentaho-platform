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

/**
 * Provides an interface around getting a URL object which can be used to construct a URL managing the parameters
 * that need to be built up.
 * 
 * @author jdixon
 * 
 */

public interface IPentahoUrlFactory {

  /**
   * Gets a builder for action URLs. Action URLs are constructed for tasks like drill-down.
   * 
   * @return the action URL
   */
  public IPentahoUrl getActionUrlBuilder();

  /**
   * Returns a URL builder for contructing URLs that are generated for user feedback (e.g. parameter input forms).
   * 
   * @return The URL builder
   */
  public IPentahoUrl getDisplayUrlBuilder();

}
