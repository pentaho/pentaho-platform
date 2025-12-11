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
