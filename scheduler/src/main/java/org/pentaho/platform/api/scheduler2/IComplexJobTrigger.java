package org.pentaho.platform.api.scheduler2;

import org.pentaho.platform.api.scheduler2.wrappers.ITimeWrapper;
import org.pentaho.platform.scheduler2.recur.ITimeRecurrence;

import java.util.List;

public interface IComplexJobTrigger extends IJobTrigger {
  static final int SUNDAY = 1;

  long getRepeatInterval();
  void setRepeatInterval( long repeatIntervalSeconds );
  void addYearlyRecurrence( Integer... recurrence );
  void addMonthlyRecurrence( Integer... recurrence );
  void addDayOfMonthRecurrence( Integer... recurrence );
  void addDayOfWeekRecurrence( ITimeRecurrence recurrence );
  void addDayOfWeekRecurrence( Integer... recurrence );
  void setHourlyRecurrence( Integer... recurrence );
  void addMinuteRecurrence( Integer... recurrence );
  void setCronString(String cronString);

  List<ITimeWrapper> getDayOfMonthRecurrences();
  List<ITimeWrapper> getMonthlyRecurrences();
  List<ITimeWrapper> getYearlyRecurrences();
  List<ITimeWrapper> getDayOfWeekRecurrences();
}
