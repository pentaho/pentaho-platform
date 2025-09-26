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
 * Provides a simple interface for Portlet URL's (<tt>PortletURL</tt> objects).
 * 
 * @author mbatchel
 */

public interface IPentahoUrl {

  /**
   * Sets a URL parameter. Since the portlet URL handles all the URL parameter construction, talking to a generic
   * interface seems more extensible.
   * 
   * @param name
   *          Name of the parameter to set
   * @param value
   *          Value to set the parameter to
   */
  public void setParameter( String name, String value );

  /**
   * @return the full URL with the parameters incorporated
   */
  public String getUrl();

}
