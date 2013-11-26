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

package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.action.IAction;

/**
 * An object that allows for the scheduling of IActions on the Pentaho platform
 * 
 * @author arodriguez
 */
public interface IScheduler {
  public enum SchedulerStatus {
    RUNNING, PAUSED, STOPPED
  };

  /**
   * Schedules a job to be run at one or more times in the future.
   * 
   * @param jobName
   *          the user defined name for the job
   * @param action
   *          the action to be run at the scheduled time(s)
   * @param jobParams
   *          the parameters to be passed to the action when it runs
   * @param trigger
   *          the time(s) at which to run the action
   * @return the scheduled job
   * @throws SchedulerException
   *           If the job could not be scheduled
   */
  public Job createJob( String jobName, Class<? extends IAction> action, Map<String, Serializable> jobParams,
      IJobTrigger trigger ) throws SchedulerException;

  /**
   * Schedules a job to be run at one or more times in the future.
   * 
   * @param jobName
   *          the user defined name for the job
   * @param actionId
   *          the bean Id of the action to be run as defined an a plugin's plugin.xml
   * @param jobParams
   *          the parameters to be passed to the action when it runs
   * @param trigger
   *          the time(s) at which to run the action
   * @return the scheduled job
   * @throws SchedulerException
   *           If the job could not be scheduled
   */
  public Job createJob( String jobName, String actionId, Map<String, Serializable> jobParams, IJobTrigger trigger )
    throws SchedulerException;

  /**
   * Schedules a job to be run at one or more times in the future.
   * 
   * @param jobName
   *          the user defined name for the job
   * @param action
   *          the action to be run at the scheduled time(s)
   * @param jobParams
   *          the parameters to be passed to the action when it runs
   * @param trigger
   *          the time(s) at which to run the action
   * @param outputStreamProvider
   *          if the action being scheduled expects to write to an output stream, at the time of action execution the
   *          this provider will be used to create the stream that passed to the action.
   * @return the scheduled job
   * @throws SchedulerException
   *           If the job could not be scheduled
   */
  public Job createJob( String jobName, Class<? extends IAction> action, Map<String, Serializable> jobParams,
      IJobTrigger trigger, IBackgroundExecutionStreamProvider outputStreamProvider ) throws SchedulerException;

  /**
   * Schedules a job to be run at one or more times in the future.
   * 
   * @param jobName
   *          the user defined name for the job
   * @param actionId
   *          the bean Id of the action to be run as defined an a plugin's plugin.xml
   * @param jobParams
   *          the parameters to be passed to the action when it runs
   * @param trigger
   *          the time(s) at which to run the action
   * @param outputStreamProvider
   *          if the action being scheduled expects to write to an output stream, at the time of action execution the
   *          this provider will be used to create the stream that passed to the action.
   * @return the scheduled job
   * @throws SchedulerException
   *           If the job could not be scheduled
   */
  public Job createJob( String jobName, String actionId, Map<String, Serializable> jobParams, IJobTrigger trigger,
      IBackgroundExecutionStreamProvider outputStreamProvider ) throws SchedulerException;

  /**
   * Updates both the parameters and trigger to be used to execute an existing scheduled action.
   * 
   * @param jobId
   *          the ID of an existing scheduled job
   * @param jobParams
   *          the parameters to be passed to the action when it runs
   * @param trigger
   *          the time(s) at which to run the action
   * @throws SchedulerException
   *           If the job could not be updated
   */
  public void updateJob( String jobId, Map<String, Serializable> jobParams, IJobTrigger trigger )
    throws SchedulerException;

  /**
   * Removes the specified job from the list of scheduled jobs
   * 
   * @param jobId
   *          the job to be removed
   */
  public void removeJob( String jobId ) throws SchedulerException;;

  /**
   * Prevents the specified job from running in the future. The job remains in the list of scheduled jobs in a "paused"
   * state. If the job is currently running, the currently running job is allowed to complete.
   * 
   * @param jobId
   *          the job to be paused
   */
  public void pauseJob( String jobId ) throws SchedulerException;

  /**
   * Allows previously paused jobs to resume running in the future.
   * 
   * @param jobId
   *          the job to be resumed
   */
  public void resumeJob( String jobId ) throws SchedulerException;

  /**
   * Fetches a Job by jobId
   * 
   * @param jobId
   *          the job to be returned
   */
  public Job getJob( String jobId ) throws SchedulerException;

  /**
   * Triggers the given quartz job by jobId to be executed immediately
   * 
   * @param jobId
   *          the job to be executed
   */
  public void triggerNow( String jobId ) throws SchedulerException;

  /**
   * Sets when a particular subject is allowed to schedule jobs.
   * 
   * @param subject
   *          the subject to which the subject applies
   * @param window
   *          the window of time at which the scheduler is available
   */
  public void setSubjectAvailabilityWindow( IScheduleSubject subject, ComplexJobTrigger window );

  /**
   * Replaces the scheduler availability map with the provided availability map.
   * 
   * @param windows
   *          the new scheduler availability map
   */
  public void setAvailabilityWindows( Map<IScheduleSubject, ComplexJobTrigger> windows );

  /**
   * Gets the scheduler availability window to the specified subject
   * 
   * @param subject
   *          the subject whose window is being requested
   * @return the subject's availability window
   */
  public ComplexJobTrigger getSubjectAvailabilityWindow( IScheduleSubject subject );

  /**
   * Gets the scheduler availability window for all subjects for whom a window has been set
   * 
   * @return the scheduler availability map
   */
  public Map<IScheduleSubject, ComplexJobTrigger> getAvailabilityWindows();

  /**
   * Pauses the entire scheduler, which prevents all scheduled jobs from running. Any currently running jobs are allowed
   * to complete. Note that the "paused" state of individual jobs is not changed by this call.
   */
  public void pause() throws SchedulerException;

  /**
   * Allows the scheduler to process scheduled jobs. Note that the "paused" state of individual jobs is not changed by
   * this call.
   */
  public void start() throws SchedulerException;

  /**
   * Shuts the scheduler down so it will process no more jobs. The implementation will decide if this means kill jobs in
   * progress or let them finish.
   */
  public void shutdown() throws SchedulerException;

  /**
   * Sets the minimum time that must elapse between runs of any jobs. For example if set to "5" then a job may not be
   * scheduled to run less than 5 seconds apart.
   * 
   * @param subject
   *          the subject to which the interval applies
   * @param the
   *          interval in seconds
   */
  public void setMinScheduleInterval( IScheduleSubject subject, int intervalInSeconds );

  /**
   * Get the minimum time that must elapse between job runs.
   * 
   * @param subject
   *          the subject whose min interval is being requested return the minimum interval or null if no interval has
   *          been set
   */
  public Integer getMinScheduleInterval( IScheduleSubject subject );

  /**
   * Lists currently scheduled jobs.
   * 
   * @param filter
   *          the filter to use to determine which jobs to return. If null all scheduled jobs are return.
   * @return the scheduled jobs
   */
  public List<Job> getJobs( IJobFilter filter ) throws SchedulerException;

  /**
   * Returns a history of the runs for a particular job.
   * 
   * @param jobId
   *          the job for which to query it's execution history
   * @return the execution history for the given job
   */
  public List<IJobResult> getJobHistory( String jobId );

  /**
   * Returns the current scheduler status.
   * 
   * @return the scheduler status
   */
  public SchedulerStatus getStatus() throws SchedulerException;

  public void addListener( ISchedulerListener listener );

  /**
   * Not intended for public use.
   * 
   * @param actionBean
   * @param actionUser
   * @param params
   * @param streamProvider
   */
  public void fireJobCompleted( final IAction actionBean, final String actionUser,
      final Map<String, Serializable> params, IBackgroundExecutionStreamProvider streamProvider );
}
