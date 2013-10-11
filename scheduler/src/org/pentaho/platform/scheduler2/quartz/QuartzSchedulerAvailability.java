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

package org.pentaho.platform.scheduler2.quartz;

import java.util.Date;

import org.quartz.impl.calendar.BaseCalendar;

/**
 * Implementation of a Quartz calendar. Note that unlike typical Quartz calendars in which you specify when the trigger
 * is not allowed to fire, when constructing this calendar you specify when the trigger is allowed to fire.
 * 
 * @author arodriguez
 */
public class QuartzSchedulerAvailability extends BaseCalendar {
  private static final long serialVersionUID = 8419843512264409846L;
  Date startTime;
  Date endTime;

  /**
   * Creates a quartz calender which is used to indicate when a trigger is allowed to fire. The trigger will be allowed
   * to fire between the start date and end date.
   * 
   * @param startTime
   *          the earliest time at which the trigger may fire. If null the trigger may fire immediately.
   * @param endTime
   *          the last date at which the trigger may fire. If null the trigger may fire indefinitely.
   */
  public QuartzSchedulerAvailability( Date startTime, Date endTime ) {
    this.startTime = startTime;
    this.endTime = endTime;
  }

  /** {@inheritDoc} */
  public long getNextIncludedTime( long arg0 ) {
    long nextIncludedDate = 0;
    Date date = new Date( arg0 );
    if ( ( startTime != null ) && ( endTime != null ) ) {
      if ( !date.before( startTime ) && date.before( endTime ) ) {
        nextIncludedDate = arg0 + 1;
      } else if ( date.before( startTime ) ) {
        nextIncludedDate = startTime.getTime();
      }
    } else if ( startTime != null ) {
      if ( date.before( startTime ) ) {
        nextIncludedDate = startTime.getTime();
      } else {
        nextIncludedDate = arg0 + 1;
      }
    } else if ( endTime != null ) {
      if ( date.before( endTime ) ) {
        nextIncludedDate = arg0 + 1;
      }
    }
    return nextIncludedDate;
  }

  /** {@inheritDoc} */
  public boolean isTimeIncluded( long arg0 ) {
    boolean isIncluded = false;
    Date date = new Date( arg0 );
    if ( ( startTime != null ) && ( endTime != null ) ) {
      isIncluded = !date.before( startTime ) && !date.after( endTime );
    } else if ( startTime != null ) {
      isIncluded = !date.before( startTime );
    } else if ( endTime != null ) {
      isIncluded = !date.after( endTime );
    }
    return isIncluded;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }
}
