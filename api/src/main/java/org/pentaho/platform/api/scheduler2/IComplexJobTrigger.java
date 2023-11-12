package org.pentaho.platform.api.scheduler2;

import org.pentaho.platform.api.scheduler2.wrappers.DayOfMonthWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.DayOfWeekWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.MonthlyWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.YearlyWrapper;
import org.pentaho.platform.scheduler2.recur.ITimeRecurrence;

public interface IComplexJobTrigger extends IJobTrigger {
  static int SUNDAY = 1;

  long getRepeatInterval();

  void setRepeatInterval( long repeatIntervalSeconds );

  void addYearlyRecurrence( Integer... recurrence );

  void addMonthlyRecurrence( Integer... recurrence );

  void addDayOfMonthRecurrence( Integer... recurrence );

  void addDayOfWeekRecurrence( ITimeRecurrence recurrence );

  void addDayOfWeekRecurrence( Integer... recurrence );

  void setHourlyRecurrence( Integer... recurrence );

  void addMinuteRecurrence( Integer... recurrence );

  void setCronString( String cronString );

  DayOfMonthWrapper getDayOfMonthRecurrences();

  MonthlyWrapper getMonthlyRecurrences();

  YearlyWrapper getYearlyRecurrences();

  DayOfWeekWrapper getDayOfWeekRecurrences();
  void setMinuteRecurrence( Integer... recurrence );

}
