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

import java.util.List;

public interface IPentahoSystemListeners {

  public void notifySystemListenersOfStartup() throws PentahoSystemException;

  public void shutdown();

  public void addLogoutListener( final ILogoutListener listener );

  public ILogoutListener remove( final ILogoutListener listener );

  public void invokeLogoutListeners( final IPentahoSession session );

  /**
   * Registers custom handlers that are notified of both system startup and system shutdown events.
   * 
   * @param systemListeners
   *          the system event handlers
   */
  public void setSystemListeners( List<IPentahoSystemListener> systemListeners );

}
