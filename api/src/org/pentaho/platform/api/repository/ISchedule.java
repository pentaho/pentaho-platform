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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Oct 17, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.api.repository;

import java.util.Date;

public interface ISchedule {

  /**
   * @return Returns the revision.
   */
  public int getRevision();

  /**
   * A unique id, often a GUID uniquely identifying the schedule, used to locate the 
   * schedule in the Solution Repository (ISolutionRepository implementors).
   * @return
   */
  public String getId();

  public String getTitle();

  /**
   * Typically the "name" of the schedule
   * @return
   */
  public String getScheduleReference();

  public void setId(String id);

  public void setTitle(String title);

  public void setScheduleReference(String scheduleRef);

  public String getDescription();

  public void setDescription(String description);

  /**
   * Get the cron string for this schedule. You
   * should only call this method if isCronSchedule() returns true.
   * @return
   */
  public String getCronString();

  public void setCronString(String cronString);

  public String getGroup();

  public void setGroup(String group);

  public Date getLastTrigger();

  public void setLastTrigger(Date lastTrigger);

  /**
   * Set repeat time in milliseconds (the repeat time is the period of
   * time the scheduler will wait before running the schedule's work again.
   * @param repeatInterval Integer repeat time in milliseconds
   */
  public void setRepeatInterval( Integer repeatInterval );
  
  /**
   * Get repeat time in milliseconds. You
   * should only call this method if isRepeatSchedule() returns true.
   * @return Integer the repeat time in milliseconds
   */
  public Integer getRepeatInterval();

  /**
   * Set the repeat count (number of times the schedule should run).
   * @param repeatCount
   */
  public void setRepeatCount( Integer repeatCount );
  
  /**
   * Get the repeat count (number of times the schedule should run). You
   * should only call this method if isRepeatSchedule() returns true.
   * @return repeat count
   */
  public Integer getRepeatCount();

  public Date getStartDate();
  public void setStartDate( Date startDate );
  
  public Date getEndDate();
  public void setEndDate( Date startDate );
  /**
   * Is this schedule defined by a cron string, or by a repeat time and repeat count?
   * This schedule can be one or the other but not both.
   * 
   * @return true if this is a cron schedule, else false
   */
  public boolean isCronSchedule();
  
  /**
   * Is this schedule defined by a cron string, or by a repeat time and repeat count?
   * This schedule can be one or the other but not both.
   * 
   * @return true if this is a repeating schedule, else false
   */
  public boolean isRepeatSchedule();
}
