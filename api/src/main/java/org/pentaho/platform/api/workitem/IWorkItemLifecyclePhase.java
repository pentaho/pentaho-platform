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


package org.pentaho.platform.api.workitem;

/**
 * A common interface for work item lifecycle phases.
 */
public interface IWorkItemLifecyclePhase {

  /**
   * Returns the string representation of this phase.
   *
   * @return the string representation of this phase
   */
  String toString();

}
