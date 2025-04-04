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
 * Manages Hitachi Vantara Request Context
 * <p>
 * For now the only information stored in the IPentahoRequestContext is teh context path. In future more
 * information can be added to this
 * 
 */
public interface IPentahoRequestContext {
  /**
   * Returns the portion of the request URI that indicates the context of the request. The context path always
   * comes first in a request URI. The path starts with a "/" character and end with a "/" character. For servlets
   * in the default (root) context, this method returns "/". The container does not decode this string. It is
   * possible that a servlet container may match a context by more than one context path. In such cases this method
   * will return the actual context path used by the request
   */
  public String getContextPath();
}
