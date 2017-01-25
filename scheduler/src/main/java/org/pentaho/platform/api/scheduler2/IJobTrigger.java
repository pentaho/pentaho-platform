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

import java.util.Date;

public interface IJobTrigger {
  /**
   * Returns the trigger start time.
   * 
   * @return the trigger start time.
   */
  public Date getStartTime();

  /**
   * Sets the trigger start time.
   * 
   * @param startTime
   *          when to start the trigger. If null the trigger starts immediately.
   */
  public void setStartTime( Date startTime );

  /**
   * Returns the trigger end time.
   * 
   * @return the trigger end time.
   */
  public Date getEndTime();

  /**
   * Sets the trigger end time.
   * 
   * @param endTime
   *          when to end the trigger. If null the trigger runs indefinitely
   */
  public void setEndTime( Date endTime );

  /**
   * @return the uiPassParam
   */
  public String getUiPassParam();

  /**
   * The value of this field comes from the UI and is persisted in quartz but not used by quartz or the server. It is
   * strictly a way for the UI to persist something. In the present implementation, this field holds the scheduleType.
   * @See JsJobTrigger
   * 
   * @param uiPassParam
   *          A User Interface provided string
   */
  public void setUiPassParam( String uiPassParam );

  /**
   * Returns the Cron String used by quartz Scheduler
   * 
   * @return the cronString
   */
  public String getCronString();

  /**
   * Sets the cron String used by the quartz scheduler
   * 
   * @param cronString
   *          the cronString to set
   */
  public void setCronString( String cronString );

  /**
   * @return a long that represents in milliseconds how long this trigger should be in effect once triggered
   */
  public long getDuration();

  /**
   * @param duration
   * 
   *          Sets the length of time in milliseconds that this trigger should be in effect.
   */
  public void setDuration( long duration );
}
