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
 * System Entry Point implementors get called upon entry to the system after they've been registered with the
 * ApplicationContext (<tt>IApplicationContext</tt>). The method is called when entries to the system call
 * <code>PentahoSystem.systemEntryPoint();</code> The purpose of the entry point is to setup the environment as
 * necessary to handle relational object persistence, starting transactions, initializing objects, or whatever
 * needs to take place when some action starts in the server. Example invocations include action execution, agent
 * startup, etc.
 * 
 */

public interface IPentahoSystemEntryPoint {
  /**
   * Perform operations necessary upon entry to the system.
   */
  public void systemEntryPoint();

}
