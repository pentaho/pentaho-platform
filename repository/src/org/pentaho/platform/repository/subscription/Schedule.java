/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * 
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Oct 17, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.repository.subscription;

import java.util.Date;

import org.pentaho.platform.api.repository.ISchedule;

public class Schedule implements ISchedule {
  private String id;

  private String title;

  private String scheduleReference;

  private String description;

  private String cronString = null;

  private String group;

  private Date lastTrigger;
  
  private Integer repeatCount = null;
  
  private Integer repeatInterval = null;
  
  private Date startDate = null;
  
  private Date endDate = null;

  private int revision = -1; // Hibernate Revision

  protected Schedule() {
    // Needed for Hibernate.
  }

  public Schedule(final String id, final String title, final String scheduleRef, final String description,
      final String cronString, final String group, final Date startDate, final Date endDate) {
    this.id = id;
    this.title = title;
    this.scheduleReference = scheduleRef;
    this.description = description;
    this.cronString = cronString;
    this.group = group;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public Schedule(final String id, final String title, final String scheduleRef, final String description,
      final Integer repeatCount, final Integer repeatInterval, final String group, final Date startDate, final Date endDate) {
    this.id = id;
    this.title = title;
    this.scheduleReference = scheduleRef;
    this.description = description;
    this.repeatCount = repeatCount;
    this.repeatInterval = repeatInterval;
    this.group = group;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Schedule)) {
      return false;
    }
    final Schedule that = (Schedule) other;
    return this.getId().equals(that.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  /**
   * @return Returns the revision.
   */
  public int getRevision() {
    return revision;
  }

  /**
   * @param revision
   *            The revision to set. This is set by Hibernate
   */
  protected void setRevision(final int revision) {
    this.revision = revision;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getScheduleReference() {
    return scheduleReference;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public void setScheduleReference(final String scheduleRef) {
    this.scheduleReference = scheduleRef;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public String getCronString() {
    return cronString;
  }

  /**
   * NOTE: see NOTE in setRepeatInterval
   */
  public void setCronString(final String cronString) {
    if ( null != cronString ) {
      setRepeatInterval(null );
      setRepeatCount(null );
    }
    this.cronString = cronString;
  }

  public String getGroup() {
    return ((group == null) ? "" : group); //$NON-NLS-1$
  }

  public void setGroup(final String group) {
    this.group = group;
  }

  public Date getLastTrigger() {
    return lastTrigger;
  }

  public void setLastTrigger(final Date lastTrigger) {
    this.lastTrigger = lastTrigger;
  }

  public Integer getRepeatCount() {
    return repeatCount;
  }

  public Integer getRepeatInterval() {
    return repeatInterval;
  }

  public void setRepeatCount(Integer repeatCount) {
    this.repeatCount = repeatCount;
  }

  /**
   * NOTE: repeat schedules and cron schedules are mutually exclusive. When setting
   * the repeat interval, the cron string will be nulled, and visa versa.
   */
  public void setRepeatInterval(Integer repeatInterval ) {
    if ( null != repeatInterval ) {
      setCronString( null );
    }
    this.repeatInterval = repeatInterval;
  }
  
  /**
   * NOTE: in this implementation, it is possible for isCronSchedule to return false
   * and isRepeatSchedule to return false on the same instance. This can happen
   * if the instance has had neither its cronString nor its repeatCount and repeatInterval
   * initialized. While the instance is in a state where both of these methods
   * return false, the instance should be considered insufficiently initialized, and invalid. 
   */
  public boolean isCronSchedule() {
    return cronString != null && repeatInterval == null;
  }

  /**
   * NOTE: see NOTE in isCronSchedule
   */
  public boolean isRepeatSchedule() {
    return cronString == null && repeatInterval != null;
  }

  public Date getEndDate() {
    return endDate;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public void setStartDate(Date startDate) { 
    this.startDate = startDate;
  }

}
