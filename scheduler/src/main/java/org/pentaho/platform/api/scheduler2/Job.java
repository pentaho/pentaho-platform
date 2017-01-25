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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A {@link Job} is a representation of the union between an action to be performed, data to be supplied, and a schedule
 * upon which the action will be fired. The scheduling system is responsible for creating {@link Job}s via
 * {@link IScheduler#createJob(String, Class, Map, JobTrigger)}. Jobs are likely persistent, at least for the life of a
 * {@link IScheduler} instance. In other words, an {@link IScheduler} instance should never forget about a Job it has
 * created, unless the Job has been removed via {@link IScheduler#removeJob(Job)}.
 * <p>
 * Note: once the scheduler engine processes a job run, it will create a new {@link IJobResult}, which will contain full
 * historical information about job runs. {@link Job} will contain only minimal of such temporal information.
 * 
 * @author aphillips
 */
@XmlRootElement
public class Job {

  public enum JobState {
    NORMAL, PAUSED, COMPLETE, ERROR, BLOCKED, UNKNOWN
  };

  JobTrigger jobTrigger;

  Map<String, Serializable> jobParams = new HashMap<String, Serializable>();

  Date lastRun;

  Date nextRun;

  @XmlTransient
  String schedulableClass;

  String jobId;

  String userName;

  String jobName;

  String groupName;

  JobState state = JobState.UNKNOWN;

  /**
   * @return the trigger that determines when the job executes
   */
  public JobTrigger getJobTrigger() {
    return jobTrigger;
  }

  /**
   * @return the map containing the parameters to be passed to the action executed by this job
   */
  @XmlJavaTypeAdapter( JobParamsAdapter.class )
  public Map<String, Serializable> getJobParams() {
    return jobParams;
  }

  /**
   * @return the last time this job ran or null if the job has not run yet.
   */
  public Date getLastRun() {
    return lastRun;
  }

  /**
   * @return the next time the job is scheduled to run or null if the job will not run again.
   */
  public Date getNextRun() {
    return nextRun;
  }

  /**
   * @return the class name of the IAction that will be executed by this job.
   */
  @XmlTransient
  public String getSchedulableClass() {
    return schedulableClass;
  }

  /**
   * @return the id that uniquely defines this job.
   */
  public String getJobId() {
    return jobId;
  }

  /**
   * @return the user defined name of this job.
   */
  public String getJobName() {
    return jobName;
  }

  /**
   * @return the user that scheduled this job
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @return the group name of this job
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * Sets the trigger used to determine when this job runs.
   * 
   * @param jobTrigger
   *          the job trigger
   */
  public void setJobTrigger( JobTrigger jobTrigger ) {
    this.jobTrigger = jobTrigger;
  }

  /**
   * Sets the parameters that will be passed to the scheduled IAction when the job executes.
   * 
   * @param jobParams
   *          the parameters to be passed to the IAction
   */
  public void setJobParams( Map<String, Serializable> jobParams ) {
    if ( jobParams != this.jobParams ) {
      this.jobParams.clear();
      if ( jobParams != null ) {
        this.jobParams.putAll( jobParams );
      }
    }
  }

  /**
   * Sets the last time the job executed.
   * 
   * @param lastRun
   *          the last time the job ran. null if the job has not run.
   */
  public void setLastRun( Date lastRun ) {
    this.lastRun = lastRun;
  }

  /**
   * Sets the next time the job will execute
   * 
   * @param nextRun
   *          the next time the job will run. null if the job will not run again.
   */
  public void setNextRun( Date nextRun ) {
    this.nextRun = nextRun;
  }

  /**
   * Sets the name of the IAction class that will run when the job is executed.
   * 
   * @param schedulableClass
   *          the name of the IAction to run.
   */
  public void setSchedulableClass( String schedulableClass ) {
    this.schedulableClass = schedulableClass;
  }

  /**
   * Sets the id that uniquely defines this job.
   * 
   * @param jobId
   *          the job id
   */
  public void setJobId( String jobId ) {
    this.jobId = jobId;
  }

  /**
   * Sets the name of the user that has scheduled this job
   * 
   * @param userName
   *          the user name
   */
  public void setUserName( String userName ) {
    this.userName = userName;
  }

  /**
   * Sets the user defined name of this job.
   * 
   * @param jobName
   *          the job name
   */
  public void setJobName( String jobName ) {
    this.jobName = jobName;
  }

  /**
   * @return the current job state
   */
  public JobState getState() {
    return state;
  }

  /**
   * Sets the current state of this job
   * 
   * @param state
   *          the job state
   */
  public void setState( JobState state ) {
    this.state = state;
  }

  /**
   * Sets the group name of this job
   * 
   * @param groupName
   *          the group name
   */
  public void setGroupName( final String groupName ) {
    this.groupName = groupName;
  }
}
