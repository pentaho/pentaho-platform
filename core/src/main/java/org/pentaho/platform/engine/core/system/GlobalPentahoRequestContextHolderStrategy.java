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


package org.pentaho.platform.engine.core.system;

import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoRequestContextHolderStrategy;

/**
 * A {@code static} field-based implementation of {@link IPentahoRequestContextHolderStrategy}.
 * 
 * <p>
 * This means that all instances in the JVM share the same {@code IPentahoRequestContext}. This is generally useful
 * with rich clients, such as Swing.
 * </p>
 * 
 * @author rmansoor
 */
public class GlobalPentahoRequestContextHolderStrategy implements IPentahoRequestContextHolderStrategy {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private static IPentahoRequestContext requestContext;

  // ~ Constructors
  // ====================================================================================================

  public GlobalPentahoRequestContextHolderStrategy() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public IPentahoRequestContext getRequestContext() {
    return requestContext;
  }

  public void removeRequestContext() {
    requestContext = null;
  }

  public void setRequestContext( IPentahoRequestContext inRequestContext ) {
    requestContext = inRequestContext;
  }

}
