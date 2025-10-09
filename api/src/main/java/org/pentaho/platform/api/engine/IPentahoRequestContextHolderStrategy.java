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
 * A strategy for storing an IPentahoRequestContext against a thread.
 * 
 * <p>
 * Inspired by {@code org.springframework.security.context.SecurityContextHolderStrategy}.
 * </p>
 * 
 * @author rmansoor
 */
public interface IPentahoRequestContextHolderStrategy {

  /**
   * Sets the current request context.
   * 
   * @param requestContext
   *          request context to set
   */
  void setRequestContext( IPentahoRequestContext requestContext );

  /**
   * Returns the current request context.
   * 
   * @return requestContext
   */
  IPentahoRequestContext getRequestContext();

  /**
   * Clears the current request context.
   */
  void removeRequestContext();

}
