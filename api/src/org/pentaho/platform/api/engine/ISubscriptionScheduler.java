/*
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
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.api.engine;

import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.repository.ISchedule;

/**
 * Interface class between the subscription repository and the implementation of the
 * scheduler.
 * 
 * @author dmoran
 * 
 */

public interface ISubscriptionScheduler {

  /**
   * Synchronizes schedules between the subscription repos and the scheduling system
   * 
   * @param oldSchedule
   *            The name of the schedule to modify or delete. If null, then the
   *            operation is treated as an add.
   * 
   * @param newSchedule
   *            The schedule to modify or add. If null, then the operation is
   *            treated as an delete.
   * 
   * @return true if successfull
   */
  public IScheduledJob syncSchedule(String oldScheduleReference, ISchedule newSchedule) throws SubscriptionSchedulerException;

  /**
   * Synchronizes schedules between the subscription repos and the scheduling system
   * 
   * @param newSchedules
   *            The list of schedules that should exist. Any schedules not in the
   *            list should be deleted
   * 
   * @return list of exception messages
   */
  @SuppressWarnings("unchecked")
  public List syncSchedule(List newSchedules) throws Exception;

  /**
   * Returns a List of all IScheduledJobs that are currently in the subscription
   * scheduling system
   * 
   */
  @SuppressWarnings("unchecked")
  public List getScheduledJobs();

  /**
   * Returns the IScheduledJob for the passed in schedule reference from the scheduling system
   * @throws Exception 
   * 
   */
  public IScheduledJob getScheduledJob(String schedRef) throws SubscriptionSchedulerException;

  public Map<String,IScheduledJob> getScheduledJobMap() throws Exception;

  /**
   * Pause the job.
   * NOTE: in the quartz implementation, the value of the <param>jobName</param>
   * parameter should be the trigger name associated with the job to be paused.
   * 
   * @param jobName
   * @return
   * @throws Exception
   */
  public IScheduledJob pauseJob(String jobName) throws Exception;

  /**
   * Resume the job.
   * NOTE: in the quartz implementation, the value of the <param>jobName</param>
   * parameter should be the trigger name associated with the job to be resumed.
   * 
   * @param jobName
   * @return
   * @throws Exception
   */
  public IScheduledJob resumeJob(String jobName) throws Exception;

  /**
   * Execute the job.
   * NOTE: in the quartz implementation, the value of the <param>jobName</param>
   * parameter should be the trigger name associated with the job to be executed.
   * 
   * @param jobName
   * @return
   * @throws Exception
   */
  public IScheduledJob executeJob(String jobName) throws Exception;

  /**
   * Delete the job.
   * NOTE: in the quartz implementation, the value of the <param>jobName</param>
   * parameter should be the trigger name associated with the job to be deleted.
   * 
   * @param jobName
   * @return
   * @throws Exception
   */
  public IScheduledJob deleteJob(String jobName) throws Exception;

  public IScheduledJob scheduleJob(ISchedule schedule) throws Exception;

  public int getSchedulerState() throws Exception;

  public void pauseScheduler() throws Exception;

  public void resumeScheduler() throws Exception;

  public String getCronSummary(String cron) throws Exception;
}
