/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 * 
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

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
   * @param blockOutJobId
   * @return a IBlockOutTrigger that represents the blockOut with the name blockOutName
   * @throws SchedulerException
   */
  IJobTrigger getBlockOut( String blockOutJobId );

  /**
   * @return a list of jobs essentially should be used instead of getBlockOuts which is deprecated
   */
  List<Job> getBlockOutJobs();

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
   * @param testBlockOutJobTrigger
   * @return the {@link List} of {@link IJobTrigger}s which would be blocked by the {@link IJobTrigger}
   * @throws SchedulerException
   */
  List<IJobTrigger> willBlockSchedules( IJobTrigger testBlockOutJobTrigger );

  /**
   * @param scheduleJobTrigger
   *          {@link IJobTrigger}
   * @return whether the {@link IJobTrigger} is blocked, at least partially, by at least a single {@link IJobTrigger},
   *         provided the list of registered {@link IJobTrigger}s
   * @throws SchedulerException
   */
  boolean isPartiallyBlocked( IJobTrigger scheduleJobTrigger );
}
