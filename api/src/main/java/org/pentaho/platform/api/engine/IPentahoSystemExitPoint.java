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
 * System Exit Point implementors are called with the action on a thread completes. This action gets invoked
 * through the PentahoSystem static method <code>systemExitPoint().</code> The exit point is mainly implemented to
 * handle relational object persistence through Hibernate, but other objects that need to setup and teardown
 * objects and state could add themselves to the ApplicationContext list of entry and exit point notifications.
 * 
 */

public interface IPentahoSystemExitPoint {

  /**
   * Perform any system cleanup actions after the thread executes.
   */
  public void systemExitPoint();
}
