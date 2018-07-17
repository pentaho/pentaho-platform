/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
