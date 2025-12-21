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
 * Classes that implement this interface can be notified about key system events, in order to perform one-time
 * operations such as initialization, maintenance or other tasks.
 * 
 * @version 1.0
 */
public interface IPentahoSystemListener {

  /**
   * Notification of system startup. This event fires at the end of system initialization, after all system
   * components have started successfully.
   */
  public boolean startup( IPentahoSession session );

  /**
   * Notification of system shutdown. This event fires right before the server context is shutdown.
   */
  public void shutdown();

}
