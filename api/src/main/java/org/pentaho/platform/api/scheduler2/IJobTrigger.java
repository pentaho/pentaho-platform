/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.api.scheduler2;

import java.util.Date;

public interface IJobTrigger {
  /**
   * Returns the trigger start time.
   * 
   * @return the trigger start time.
   */
  Date getStartTime();

  /**
   * Sets the trigger start time.
   * 
   * @param setStartTime
   *          when to start the trigger. If null the trigger starts immediately.
   */
  void setStartTime( Date startTime );

  /**
   * Returns the trigger end time.
   * 
   * @return the trigger end time.
   */
  Date getEndTime();

  /**
   * Sets the trigger end time.
   * 
   * @param endTime
   *          when to end the trigger. If null the trigger runs indefinitely
   */
  void setEndTime( Date endTime );

  /**
   * @return the uiPassParam
   */
  String getUiPassParam();

  /**
   * The value of this field comes from the UI and is persisted in quartz but not used by quartz or the server. It is
   * strictly a way for the UI to persist something. In the present implementation, this field holds the scheduleType.
   * @See JsJobTrigger
   * 
   * @param uiPassParam
   *          A User Interface provided string
   */
  void setUiPassParam( String uiPassParam );

  /**
   * Returns the Cron String used by quartz Scheduler
   * 
   * @return the cronString
   */
  String getCronString();

  /**
   * Sets the cron String used by the quartz scheduler
   * 
   * @param cronString
   *          the cronString to set
   */
  void setCronString( String cronString );

  /**
   * @return a long that represents in milliseconds how long this trigger should be in effect once triggered
   */
  long getDuration();

  /**
   * @param duration
   * 
   *          Sets the length of time in milliseconds that this trigger should be in effect.
   */
  void setDuration( long duration );

  /**
   * @return the start hour (0-23)
   */
  int getStartHour();

  /**
   * Set the start hour (0-23)
   *
   * @param startHour
   */
  void setStartHour( int startHour );

  /**
   * @return the start minute (0-59)
   */
  int getStartMin();

  /**
   * Set the start minute (0-59)
   *
   * @param startMin
   */
  void setStartMin( int startMin );

  /**
   * @return the start year (indexed from 1900 per java.util.Date)
   */
  int getStartYear();

  /**
   * Set the start year (indexed from 1900 per java.util.Date)
   *
   * @param startYear
   */
  void setStartYear( int startYear );

  /**
   * @return the start month (0-11)
   */
  int getStartMonth();

  /**
   * Set the start month (0-11)
   *
   * @param startMonth
   */
  void setStartMonth( int startMonth );

  /**
   * @return the start day (1-31)
   */
  int getStartDay();

  /**
   * Set the start day (1-31)
   *
   * @param startDay
   */
  void setStartDay( int startDay );

  /**
   * @return 0 - AM;  1 - PM
   */
  int getStartAmPm();

  /**
   * Set the AM/PM value; 0 - AM; 1 - PM
   *
   * @param startAmPm
   */
  void setStartAmPm( int startAmPm );

  /**
   * Set the time zone (see java.util.TimeZone for valid IDs)
   * Expects a TimeZone.
   *
   * @param timeZone
   */
  void setTimeZone( String timeZone );

  /**
   * @return the time zone
   */
  String getTimeZone();
}
