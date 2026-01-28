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
 * The listener interface for receiving notification when a Component execution has completed.
 * <p>
 * At the moment, all Component executions are synchronous, so the notification is sent as when the execution has
 * truly completed. In the near future, when asynchronous executions are implemented, notification may be sent as
 * soon as the execution has launched the asynchronous thread and has returned (in the case of an asynchronous
 * execution).
 */
public interface IActionCompleteListener {

  /**
   * Invoked when a Component execution has completed
   * 
   * @param runtime
   *          the runtime context associated with this action
   */
  public void actionComplete( IRuntimeContext runtime );

}
