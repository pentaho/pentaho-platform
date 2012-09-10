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
 * Copyright 2010 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.Date;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * The marker superclass for the various types of job triggers.
 * 
 * @author aphillips
 * 
 * @see SimpleJobTrigger
 * @see ComplexJobTrigger
 */
@XmlSeeAlso({SimpleJobTrigger.class, ComplexJobTrigger.class, CronJobTrigger.class})
public abstract class JobTrigger implements Serializable{
  public static final SimpleJobTrigger ONCE_NOW = new SimpleJobTrigger(new Date(), null, 0, 0L);
  
  private Date startTime;
  private Date endTime;
  private String uiPassParam; 
  private String cronString;
    
  public JobTrigger() {
  }
  
  public JobTrigger(Date startTime, Date endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
  }
  
  /**
   * Returns the trigger start time.
   * @return the trigger start time.
   */
  public Date getStartTime() {
    return startTime;
  }
  
  /**
   * Sets the trigger start time.
   * @param startTime when to start the trigger. If null the trigger starts immediately.
   */
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }
  
  /**
   * Returns the trigger end time.
   * @return the trigger end time.
   */
  public Date getEndTime() {
    return endTime;
  }
  
  /**
   * Sets the trigger end time.
   * @param startTime when to end the trigger. If null the trigger runs indefinitely
   */
  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

	/**
	 * @return the uiPassParam
	 */
	public String getUiPassParam() {
		return uiPassParam;
	}

	/**
	 * The value of this field comes from the UI and is persisted in quartz but not used
   * by quartz or the server.  It is strictly a way for the UI to persist something.  In the
   * present implementation, this field holds the scheduleType.  @See JsJobTrigger
	 * @param uiPassParam A User Interface provided string
	 */
	public void setUiPassParam(String uiPassParam) {
		this.uiPassParam = uiPassParam;
	}

	/**
	 * Returns the Cron String used by quartz Scheduler
	 * @return the cronString
	 */
	public String getCronString() {
		return cronString;
	}

	/**
	 * Sets the cron String used by the quartz scheduler
	 * @param cronString the cronString to set
	 */
	public void setCronString(String cronString) {
		this.cronString = cronString;
	}
	
	
  
  
}
