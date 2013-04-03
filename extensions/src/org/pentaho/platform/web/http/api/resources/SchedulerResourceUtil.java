package org.pentaho.platform.web.http.api.resources;

import java.util.Calendar;
import java.util.Date;

import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek.DayOfWeek;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek.DayOfWeekQualifier;

public class SchedulerResourceUtil {

  public static IJobTrigger convertScheduleRequestToJobTrigger(JobScheduleRequest scheduleRequest, IScheduler scheduler)
      throws SchedulerException, UnifiedRepositoryException {

    // Used to determine if created by a RunInBackgroundCommand
    boolean runInBackground = scheduleRequest.getSimpleJobTrigger() == null
        && scheduleRequest.getComplexJobTrigger() == null && scheduleRequest.getCronJobTrigger() == null;

    IJobTrigger jobTrigger = runInBackground ? new SimpleJobTrigger(null, null, 0, 0) : scheduleRequest
        .getSimpleJobTrigger();

    if (scheduleRequest.getSimpleJobTrigger() != null) {
      SimpleJobTrigger simpleJobTrigger = scheduleRequest.getSimpleJobTrigger();

      if (simpleJobTrigger.getStartTime() == null) {
        simpleJobTrigger.setStartTime(new Date());
      }

      jobTrigger = simpleJobTrigger;

    } else if (scheduleRequest.getComplexJobTrigger() != null) {

      ComplexJobTriggerProxy proxyTrigger = scheduleRequest.getComplexJobTrigger();
      ComplexJobTrigger complexJobTrigger = new ComplexJobTrigger();
      complexJobTrigger.setStartTime(proxyTrigger.getStartTime());
      complexJobTrigger.setEndTime(proxyTrigger.getEndTime());

      if (proxyTrigger.getDaysOfWeek().length > 0) {
        if (proxyTrigger.getWeeksOfMonth().length > 0) {
          for (int dayOfWeek : proxyTrigger.getDaysOfWeek()) {
            for (int weekOfMonth : proxyTrigger.getWeeksOfMonth()) {

              QualifiedDayOfWeek qualifiedDayOfWeek = new QualifiedDayOfWeek();
              qualifiedDayOfWeek.setDayOfWeek(DayOfWeek.values()[dayOfWeek]);

              if (weekOfMonth == JobScheduleRequest.LAST_WEEK_OF_MONTH) {
                qualifiedDayOfWeek.setQualifier(DayOfWeekQualifier.LAST);
              } else {
                qualifiedDayOfWeek.setQualifier(DayOfWeekQualifier.values()[weekOfMonth]);
              }
              complexJobTrigger.addDayOfWeekRecurrence(qualifiedDayOfWeek);
            }
          }
        } else {
          for (int dayOfWeek : proxyTrigger.getDaysOfWeek()) {
            complexJobTrigger.addDayOfWeekRecurrence(dayOfWeek + 1);
          }
        }
      } else if (proxyTrigger.getDaysOfMonth().length > 0) {

        for (int dayOfMonth : proxyTrigger.getDaysOfMonth()) {
          complexJobTrigger.addDayOfMonthRecurrence(dayOfMonth);
        }
      }

      for (int month : proxyTrigger.getMonthsOfYear()) {
        complexJobTrigger.addMonthlyRecurrence(month + 1);
      }

      for (int year : proxyTrigger.getYears()) {
        complexJobTrigger.addYearlyRecurrence(year);
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(complexJobTrigger.getStartTime());
      complexJobTrigger.setHourlyRecurrence(calendar.get(Calendar.HOUR_OF_DAY));
      complexJobTrigger.setMinuteRecurrence(calendar.get(Calendar.MINUTE));
      complexJobTrigger.setUiPassParam(scheduleRequest.getComplexJobTrigger().getUiPassParam());
      jobTrigger = complexJobTrigger;

    } else if (scheduleRequest.getCronJobTrigger() != null) {

      if (scheduler instanceof QuartzScheduler) {
        ComplexJobTrigger complexJobTrigger = QuartzScheduler.createComplexTrigger(scheduleRequest.getCronJobTrigger()
            .getCronString());
        complexJobTrigger.setStartTime(scheduleRequest.getCronJobTrigger().getStartTime());
        complexJobTrigger.setEndTime(scheduleRequest.getCronJobTrigger().getEndTime());
        complexJobTrigger.setUiPassParam(scheduleRequest.getCronJobTrigger().getUiPassParam());
        jobTrigger = complexJobTrigger;
      } else {
        throw new IllegalArgumentException();
      }
    }

    return jobTrigger;
  }
}
