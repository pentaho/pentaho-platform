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

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * A {@code static} field-based implementation of {@link IPentahoSessionHolderStrategy}.
 * 
 * <p>
 * This means that all instances in the JVM share the same {@code IPentahoSession}. This is generally useful with
 * rich clients, such as Swing.
 * </p>
 * 
 * @author mlowery
 */
public class GlobalPentahoSessionHolderStrategy implements IPentahoSessionHolderStrategy {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private static IPentahoSession session;

  // ~ Constructors
  // ====================================================================================================

  public GlobalPentahoSessionHolderStrategy() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public IPentahoSession getSession() {
    return session;
  }

  public void removeSession() {
    session = null;
  }

  public void setSession( IPentahoSession insession ) {
    session = insession;
  }

}
