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


package org.pentaho.platform.workitem;

import org.pentaho.platform.api.workitem.IWorkItemLifecyclePhase;

/**
 * An enumeration of the known lifecycle events for the work item.
 */
public enum WorkItemLifecyclePhase implements IWorkItemLifecyclePhase {
  /**
   * The work item has been submitted for execution
   */
  SUBMITTED,
  /**
   * The work item has been dispatched to the component responsible for its execution
   */
  DISPATCHED,
  /**
   * The work item has been received by the component responsible for its execution
   */
  RECEIVED,
  /**
   * The work item execution has been rejected
   */
  REJECTED,
  /**
   * The work item execution is in progress
   */
  IN_PROGRESS,
  /**
   * The work item execution is in progress
   */
  COMPLETED,
  /**
   * The work item execution is in progress
   */
  COMPLETED_WITH_ERRORS,
  /**
   * The work item execution has succeeded
   */
  SUCCEEDED,
  /**
   * The work item execution has failed
   */
  FAILED,
  /**
   * The work item execution has been restarted
   */
  RESTARTED;
}
