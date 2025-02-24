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


package org.pentaho.platform.api.scheduler2;

import java.util.List;

/**
 * @author wseyler
 * 
 *         Interface for managing Block-outs (time when schedules should NOT be executed)
 */
public interface IBlockoutManager {
  public static final String DURATION_PARAM = "DURATION_PARAM"; //$NON-NLS-1$

  public static final String TIME_ZONE_PARAM = "TIME_ZONE_PARAM"; //$NON-NLS-1$

  public static final String BLOCK_OUT_JOB_NAME = "BlockoutAction"; //$NON-NLS-1$

  public static final String SCHEDULED_FIRE_TIME = "scheduledFireTime";

  /**
   * @return a list of jobs essentially should be used instead of getBlockOuts which is deprecated
   */
  List<IJob> getBlockOutJobs();
  /**
   * @param scheduleJobTrigger
   *          {@link IJobTrigger}
   * @return whether the {@link IJobTrigger} will fire, at least once, given the current list of {@link IJobTrigger}s
   * @throws SchedulerException
   */
  boolean willFire( IJobTrigger scheduleJobTrigger );
  /**
   * @return true if there are no current blockOuts active at the moment this method is called
   * @throws SchedulerException
   */
  boolean shouldFireNow();
  /**
   * @param scheduleJobTrigger
   *          {@link IJobTrigger}
   * @return whether the {@link IJobTrigger} is blocked, at least partially, by at least a single {@link IJobTrigger},
   *         provided the list of registered {@link IJobTrigger}s
   * @throws SchedulerException
   */
  boolean isPartiallyBlocked( IJobTrigger scheduleJobTrigger );
}
