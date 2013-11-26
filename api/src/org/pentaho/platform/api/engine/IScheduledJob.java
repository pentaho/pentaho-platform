/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.engine;

import java.util.Date;

public interface IScheduledJob {

  public static final int STATE_NORMAL = 0;

  public static final int STATE_PAUSED = 1;

  /**
   * The trigger has no remaining fire-times in its schedule. </p>
   */
  public static final int STATE_COMPLETE = 2;

  /**
   * A <code>ScheduledJob</code> arrives at the error state when the scheduler attempts to fire it, but cannot due
   * to an error creating and executing its related job. Often this is due to the <code>Job</code>'s class not
   * existing in the classpath. </p>
   * 
   * <p>
   * When the ScheduledJob is in the error state, the scheduler will make no attempts to fire it.
   * </p>
   */
  public static final int STATE_ERROR = 3;

  /**
   * A <code>ScheduledJob</code> arrives at the blocked state when the job that it is associated with is a
   * <code>StatefulJob</code> and it is currently executing. </p>
   * 
   * @see StatefulJob
   */
  public static final int STATE_BLOCKED = 4;

  /**
   * <p>
   * Indicates that the <code>ScheduledJob</code> does not exist.
   * </p>
   */
  public static final int STATE_NONE = -1;

  public Date getNextTriggerTime();

  public Date getLastTriggerTime();

  public int getExecutionState();

  public String getDescription();

  public String getUniqueId();

  public String getErrorMessage();

}
