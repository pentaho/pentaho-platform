package org.pentaho.platform.api.scheduler2;

import java.util.Date;

public interface IJobTrigger {
  /**
   * Returns the trigger start time.
   * @return the trigger start time.
   */
  public Date getStartTime();

  /**
   * Sets the trigger start time.
   * @param startTime when to start the trigger. If null the trigger starts immediately.
   */
  public void setStartTime(Date startTime);

  /**
   * Returns the trigger end time.
   * @return the trigger end time.
   */
  public Date getEndTime();

  /**
   * Sets the trigger end time.
   * @param endTime when to end the trigger. If null the trigger runs indefinitely
   */
  public void setEndTime(Date endTime);

  /**
   * @return the uiPassParam
   */
  public String getUiPassParam();

  /**
   * The value of this field comes from the UI and is persisted in quartz but not used
   * by quartz or the server.  It is strictly a way for the UI to persist something.  In the
   * present implementation, this field holds the scheduleType.  @See JsJobTrigger
   * @param uiPassParam A User Interface provided string
   */
  public void setUiPassParam(String uiPassParam);

  /**
   * Returns the Cron String used by quartz Scheduler
   * @return the cronString
   */
  public String getCronString();

  /**
   * Sets the cron String used by the quartz scheduler
   * @param cronString the cronString to set
   */
  public void setCronString(String cronString);

  /**
   * @return a long that represents in milliseconds how long this trigger should be in effect once triggered
   */
  public int getDuration();

  /**
   * @param duration
   * 
   * Sets the length of time in milliseconds that this trigger should be in effect.
   */
  public void setDuration(int duration);
}
