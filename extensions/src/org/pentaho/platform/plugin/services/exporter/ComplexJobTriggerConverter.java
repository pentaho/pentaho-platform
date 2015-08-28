package org.pentaho.platform.plugin.services.exporter;

import org.apache.commons.lang.ArrayUtils;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.recur.ITimeRecurrence;
import org.pentaho.platform.api.scheduler2.wrappers.DayOfMonthWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.DayOfWeekWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.ITimeWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.MonthlyWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.YearlyWrapper;
import org.pentaho.platform.scheduler2.recur.IncrementalRecurrence;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfMonth;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek;
import org.pentaho.platform.scheduler2.recur.RecurrenceList;
import org.pentaho.platform.scheduler2.recur.SequentialRecurrence;
import org.pentaho.platform.web.http.api.resources.ComplexJobTriggerProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a ComplexJobTrigger into a ComplexJobTriggerProxy
 */
public class ComplexJobTriggerConverter {

  private ComplexJobTrigger jobTrigger;

  public ComplexJobTriggerConverter() {
  }

  public ComplexJobTrigger getJobTrigger() {
    return jobTrigger;
  }

  public void setJobTrigger( ComplexJobTrigger jobTrigger ) {
    this.jobTrigger = jobTrigger;
  }

  public ComplexJobTriggerProxy convertToComplexJobTriggerProxy() {
    ComplexJobTriggerProxy proxy = new ComplexJobTriggerProxy();
    proxy.setCronString( jobTrigger.getCronString() );

    DayOfMonthWrapper dayOfMonthRecurrences = jobTrigger.getDayOfMonthRecurrences();
    DayOfWeekWrapper dayOfWeekRecurrences = jobTrigger.getDayOfWeekRecurrences();
    MonthlyWrapper monthlyRecurrences = jobTrigger.getMonthlyRecurrences();
    YearlyWrapper yearlyRecurrences = jobTrigger.getYearlyRecurrences();

    if ( dayOfMonthRecurrences != null ) {
      proxy.setDaysOfMonth( convertITimeWrapper( dayOfMonthRecurrences ) );
      unwrap( proxy, dayOfMonthRecurrences );
    } else if ( dayOfWeekRecurrences != null ) {
      proxy.setDaysOfWeek( convertITimeWrapper( dayOfWeekRecurrences ) );
    } else if ( monthlyRecurrences != null ) {
      proxy.setMonthsOfYear( convertITimeWrapper( monthlyRecurrences ) );
    } else if ( yearlyRecurrences != null ) {
      proxy.setYears( convertITimeWrapper( yearlyRecurrences ) );
    }

    return proxy;
  }

  private void unwrap( ComplexJobTriggerProxy proxy, DayOfMonthWrapper wrapper ) {

    for ( ITimeRecurrence recurrence : wrapper.getRecurrences() ) {
      if ( recurrence instanceof QualifiedDayOfMonth ) {
        QualifiedDayOfMonth dayOfMonth = (QualifiedDayOfMonth) recurrence;
        // TODO
      }
    }
  }


  public static int[] convertITimeWrapper( ITimeWrapper recurrences ) {
    List<Integer> vals = new ArrayList<>();
    for ( ITimeRecurrence recurrence : recurrences.getRecurrences() ) {
      if ( recurrence instanceof RecurrenceList ) {
        RecurrenceList recList = (RecurrenceList) recurrence;
        for ( Integer integer : recList.getValues() ) {
          vals.add( integer );
        }
      } else if ( recurrence instanceof IncrementalRecurrence ) {
        IncrementalRecurrence incr = (IncrementalRecurrence) recurrence;
        // TODO
      } else if ( recurrence instanceof QualifiedDayOfWeek ) {
        QualifiedDayOfWeek qual = (QualifiedDayOfWeek) recurrence;
        // TODO
      } else if ( recurrence instanceof SequentialRecurrence ) {
        SequentialRecurrence seq = (SequentialRecurrence) recurrence;
        // TODO
      }
    }

    return ArrayUtils.toPrimitive( vals.toArray( new Integer[ vals.size() ] ) );

  }

}
