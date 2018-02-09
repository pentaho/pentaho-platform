/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
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
