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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.api.scheduler2.recur.ITimeRecurrence;
import org.pentaho.platform.api.scheduler2.wrappers.DayOfMonthWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.DayOfWeekWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.HourlyWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.ITimeWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.MinuteWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.MonthlyWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.SecondWrapper;
import org.pentaho.platform.api.scheduler2.wrappers.YearlyWrapper;
import org.pentaho.platform.scheduler2.quartz.QuartzCronStringFactory;
import org.pentaho.platform.scheduler2.recur.RecurrenceList;
import org.pentaho.platform.scheduler2.recur.SequentialRecurrence;

/**
 * Used to specify a recurrence of scheduled job execution or a recurrence of scheduler availability.
 * 
 * @author arodriguez
 */
@XmlRootElement
public class ComplexJobTrigger extends JobTrigger {

  private static final long serialVersionUID = -2742874361158319735L;

  public static final int SUNDAY = 1;
  public static final int MONDAY = 2;
  public static final int TUESDAY = 3;
  public static final int WEDNESDAY = 4;
  public static final int THURSDAY = 5;
  public static final int FRIDAY = 6;
  public static final int SATURDAY = 7;

  public static final int JANUARY = 1;
  public static final int FEBRUARY = 2;
  public static final int MARCH = 3;
  public static final int APRIL = 4;
  public static final int MAY = 5;
  public static final int JUNE = 6;
  public static final int JULY = 7;
  public static final int AUGUST = 8;
  public static final int SEPTEMBER = 9;
  public static final int OCTOBER = 10;
  public static final int NOVEMBER = 11;
  public static final int DECEMBER = 12;

  private YearlyWrapper yearlyRecurrences = new YearlyWrapper();
  private MonthlyWrapper monthlyRecurrences = new MonthlyWrapper();
  private DayOfMonthWrapper dayOfMonthRecurrences = new DayOfMonthWrapper();
  private DayOfWeekWrapper dayOfWeekRecurrences = new DayOfWeekWrapper();
  private HourlyWrapper hourlyRecurrences = new HourlyWrapper();
  private MinuteWrapper minuteRecurrences = new MinuteWrapper();
  private SecondWrapper secondRecurrences = new SecondWrapper();

  /**
   * Creates a recurrence for the specified date/time. Specifying both a day of month and day of week is not supported.
   * At least one of them should be null. If both are specified only the day of month will be used.
   * 
   * @param year
   *          the year to occur. If null recurrence will be every year.
   * @param month
   *          the month to occur. If null recurrence will be every month.
   * @param dayOfMonth
   *          the day of month to occur. If null recurrence will be every day of month. If specified day of week must be
   *          null.
   * @param dayOfWeek
   *          the day of week to occur. If null recurrence will be every day of week. If specified day of month must be
   *          null.
   * @param hourOfDay
   *          the hour of day to occur. If null recurrence will be every hour of day.
   */
  public ComplexJobTrigger( Integer year, Integer month, Integer dayOfMonth, Integer dayOfWeek, Integer hourOfDay ) {
    this();
    setYearlyRecurrence( year );
    setMonthlyRecurrence( month );
    if ( ( dayOfMonth != null ) && ( dayOfWeek == null ) ) {
      setDayOfMonthRecurrence( dayOfMonth );
    }
    if ( ( dayOfMonth == null ) && ( dayOfWeek != null ) ) {
      setDayOfWeekRecurrence( dayOfWeek );
    }
    setHourlyRecurrence( hourOfDay );
  }

  /**
   * Creates a default recurrence of every day of every year at midnight.
   */
  public ComplexJobTrigger() {
    setHourlyRecurrence( 0 );
    setMinuteRecurrence( 0 );
    setSecondRecurrence( 0 );
  }

  private void setRecurrences( ITimeWrapper theList, Integer... recurrences ) {
    theList.clear();
    addRecurrences( theList, recurrences );
  }

  private void addRecurrences( ITimeWrapper theList, Integer... recurrences ) {
    List<Integer> nonNullRecurrences = ( recurrences == null ? new ArrayList<Integer>() : filterNulls( recurrences ) );
    if ( nonNullRecurrences.size() == 0 ) {
      return;
    }
    if ( nonNullRecurrences.size() == 1 ) {
      theList.add( new RecurrenceList( nonNullRecurrences.get( 0 ) ) );
    } else if ( nonNullRecurrences.size() == 2 ) {
      TreeSet<Integer> sortedRecurrences = new TreeSet<Integer>( nonNullRecurrences );
      theList.add( new RecurrenceList( sortedRecurrences.toArray( new Integer[0] ) ) );
    } else {
      TreeSet<Integer> sortedRecurrences = new TreeSet<Integer>( nonNullRecurrences );
      Integer previourOcurrence = null;
      boolean isSequential = true;
      for ( Integer value : sortedRecurrences ) {
        if ( ( previourOcurrence != null ) && ( value.intValue() != previourOcurrence.intValue() + 1 ) ) {
          isSequential = false;
          break;
        }
        previourOcurrence = value;
      }
      if ( isSequential ) {
        theList.add( new SequentialRecurrence( sortedRecurrences.first(), sortedRecurrences.last() ) );
      } else {
        theList.add( new RecurrenceList( sortedRecurrences.toArray( new Integer[0] ) ) );
      }
    }
  }

  /**
   * Add a recurrence to the yearly recurrences.
   * 
   * @param recurrence
   *          the yearly recurrence. If null no addition occurs.
   */
  public void addYearlyRecurrence( ITimeRecurrence recurrence ) {
    if ( recurrence != null ) {
      yearlyRecurrences.add( recurrence );
    }
  }

  /**
   * Add a recurrence to the yearly recurrences.
   * 
   * @param recurrence
   *          the yearly recurrence. If null no addition occurs.
   */
  public void addYearlyRecurrence( Integer... recurrence ) {
    addRecurrences( yearlyRecurrences, recurrence );
  }

  /**
   * Overrides any previously applied yearly recurrences with the provided recurrence.
   * 
   * @param recurrence
   *          the yearly recurrence. If null it will recur every year.
   */
  public void setYearlyRecurrence( ITimeRecurrence recurrence ) {
    yearlyRecurrences.clear();
    if ( recurrence != null ) {
      yearlyRecurrences.add( recurrence );
    }
  }

  /**
   * Overrides any previously applied yearly recurrences with the provided recurrence.
   * 
   * @param recurrence
   *          the yearly recurrence. If not recurrences are provided it will recur every year.
   */
  public void setYearlyRecurrence( Integer... recurrence ) {
    setRecurrences( yearlyRecurrences, recurrence );
  }

  /**
   * Add a recurrence to the monthly recurrence.
   * 
   * @param recurrence
   *          the monthly recurrence. If null no addition occurs.
   */
  public void addMonthlyRecurrence( ITimeRecurrence recurrence ) {
    if ( recurrence != null ) {
      monthlyRecurrences.add( recurrence );
    }
  }

  /**
   * Add a recurrence to the monthly recurrence (1=January, 12=December).
   * 
   * @param recurrence
   *          the monthly recurrence. If null no addition occurs.
   */
  public void addMonthlyRecurrence( Integer... recurrence ) {
    addRecurrences( monthlyRecurrences, recurrence );
  }

  /**
   * Overrides any previously applied monthly recurrences with the provided recurrence.
   * 
   * @param recurrence
   *          the monthly recurrence. If null it will recur every month.
   */
  public void setMonthlyRecurrence( ITimeRecurrence recurrence ) {
    monthlyRecurrences.clear();
    if ( recurrence != null ) {
      monthlyRecurrences.add( recurrence );
    }
  }

  /**
   * Overrides any previously applied monthly recurrences with the provided recurrence (1=January, 12=December).
   * 
   * @param recurrence
   *          the monthly recurrence. If no recurrences are provided it will recur every month.
   */
  public void setMonthlyRecurrence( Integer... recurrence ) {
    setRecurrences( monthlyRecurrences, recurrence );
  }

  /**
   * Add a recurrence to the day of month recurrence. Calling this method with a non-null parameter causes the day of
   * week recurrence to be set to all days of the week.
   * 
   * @param recurrence
   *          the day of month recurrences. If null no modification is made to this object.
   */
  public void addDayOfMonthRecurrence( ITimeRecurrence recurrence ) {
    if ( recurrence != null ) {
      dayOfMonthRecurrences.add( recurrence );
      dayOfWeekRecurrences.clear();
    }
  }

  /**
   * Add a recurrence to the day of week recurrence. Calling this method with a non-null parameter causes the day of
   * month recurrence to be set to all days of the month.
   * 
   * @param recurrence
   *          the day of week recurrences. If null no modification is made to this object.
   */
  public void addDayOfMonthRecurrence( Integer... recurrence ) {
    addRecurrences( dayOfMonthRecurrences, recurrence );
    dayOfWeekRecurrences.clear();
  }

  /**
   * Overrides any previously applied day of month recurrences with the provided recurrence. Calling this method with a
   * non-null parameter causes the day of week recurrence to be set to all days of the week.
   * 
   * @param recurrence
   *          the day of month recurrences. If null it will recur every day of month.
   */
  public void setDayOfMonthRecurrence( ITimeRecurrence recurrence ) {
    dayOfMonthRecurrences.clear();
    if ( recurrence != null ) {
      dayOfMonthRecurrences.add( recurrence );
      dayOfWeekRecurrences.clear();
    }
  }

  /**
   * Overrides any previously applied day of month recurrences with the provided recurrence. Calling this method with
   * one or more days of month causes the day of week recurrence to be set to all days of the week.
   * 
   * @param recurrence
   *          the day of month recurrences. If no days of month are provided it will recur every day of month.
   */
  public void setDayOfMonthRecurrence( Integer... recurrence ) {
    setRecurrences( dayOfMonthRecurrences, recurrence );
    if ( dayOfMonthRecurrences.size() > 0 ) {
      dayOfWeekRecurrences.clear();
    }
  }

  /**
   * Add a recurrence to the day of week recurrence. Calling this method with a non-null parameter causes the day of
   * month recurrence to be set to all days of the month.
   * 
   * @param recurrence
   *          the day of week recurrences. If null no modification is made to this object.
   */
  public void addDayOfWeekRecurrence( ITimeRecurrence recurrence ) {
    if ( recurrence != null ) {
      dayOfWeekRecurrences.add( recurrence );
      dayOfMonthRecurrences.clear();
    }
  }

  /**
   * Add a recurrence to the day of week recurrence (1=Sunday, 7=Saturday). Calling this method with a non-null
   * parameter causes the day of month recurrence to be set to all days of the month.
   * 
   * @param recurrence
   *          the day of week recurrences. If null no modification is made to this object.
   */
  public void addDayOfWeekRecurrence( Integer... recurrence ) {
    addRecurrences( dayOfWeekRecurrences, recurrence );
    dayOfMonthRecurrences.clear();
  }

  /**
   * Overrides any previously applied day of week recurrences with the provided recurrence. Calling this method with a
   * non-null parameter causes the day of month recurrence to be set to all days of the month.
   * 
   * @param recurrence
   *          the day of week recurrences. If null it will recur every day of week.
   */
  public void setDayOfWeekRecurrence( ITimeRecurrence recurrence ) {
    dayOfWeekRecurrences.clear();
    if ( recurrence != null ) {
      dayOfWeekRecurrences.add( recurrence );
      dayOfMonthRecurrences.clear();
    }
  }

  /**
   * Overrides any previously applied day of week recurrences with the provided recurrence (1=Sunday, 7=Saturday).
   * Calling this method with one or more days of week causes the day of month recurrence to be set to all days of the
   * month.
   * 
   * @param recurrence
   *          the day of week recurrences. If no days of week are provided it will recur every day of the week.
   */
  public void setDayOfWeekRecurrence( Integer... recurrence ) {
    setRecurrences( dayOfWeekRecurrences, recurrence );
    if ( dayOfWeekRecurrences.size() > 0 ) {
      dayOfMonthRecurrences.clear();
    }
  }

  /**
   * Add a recurrence to the hourly recurrence.
   * 
   * @param recurrence
   *          the hourly recurrence. If null no modification is made to this object.
   */
  public void addHourlyRecurrence( ITimeRecurrence recurrence ) {
    if ( recurrence != null ) {
      hourlyRecurrences.add( recurrence );
    }
  }

  /**
   * Add a recurrence to the minute recurrence.
   * 
   * @param recurrence
   *          the minute recurrence. If null no modification is made to this object.
   */
  public void addHourlyRecurrence( Integer... recurrence ) {
    addRecurrences( hourlyRecurrences, recurrence );
  }

  /**
   * Overrides any previously applied hourly recurrences with the provided recurrence.
   * 
   * @param recurrence
   *          the hourly recurrence. If null it will recur every hour
   */
  public void setHourlyRecurrence( ITimeRecurrence recurrence ) {
    hourlyRecurrences.clear();
    if ( recurrence != null ) {
      hourlyRecurrences.add( recurrence );
    }
  }

  /**
   * Overrides any previously applied hourly recurrences with the provided recurrence.
   * 
   * @param recurrence
   *          the hourly recurrence. If no recurrence is provided it will recur every hour.
   */
  public void setHourlyRecurrence( Integer... recurrence ) {
    setRecurrences( hourlyRecurrences, recurrence );
  }

  /**
   * Add a recurrence to the minute recurrence.
   * 
   * @param recurrence
   *          the minute recurrence. If null no modification is made to this object.
   */
  public void addMinuteRecurrence( ITimeRecurrence recurrence ) {
    if ( recurrence != null ) {
      minuteRecurrences.add( recurrence );
    }
  }

  /**
   * Add a recurrence to the minute recurrence.
   * 
   * @param recurrence
   *          the minute recurrence. If null no modification is made to this object.
   */
  public void addMinuteRecurrence( Integer... recurrence ) {
    addRecurrences( minuteRecurrences, recurrence );
  }

  /**
   * Overrides any previously applied minute recurrences with the provided recurrence.
   * 
   * @param recurrence
   *          the minute recurrence. If null it will recur every minute.
   */
  public void setMinuteRecurrence( ITimeRecurrence recurrence ) {
    minuteRecurrences.clear();
    if ( recurrence != null ) {
      minuteRecurrences.add( recurrence );
    }
  }

  /**
   * Overrides any previously applied minute recurrences with the provided recurrence.
   * 
   * @param recurrence
   *          the minute recurrence. If no recurrence is provided it will recur every minute.
   */
  public void setMinuteRecurrence( Integer... recurrence ) {
    setRecurrences( minuteRecurrences, recurrence );
  }

  /**
   * Add a recurrence to the second recurrence.
   * 
   * @param recurrence
   *          the second recurrence. If null no modification is made to this object.
   */
  public void addSecondRecurrence( ITimeRecurrence recurrence ) {
    if ( recurrence != null ) {
      secondRecurrences.add( recurrence );
    }
  }

  /**
   * Add a recurrence to the second recurrence.
   * 
   * @param recurrence
   *          the second recurrence. If null no modification is made to this object.
   */
  public void addSecondRecurrence( Integer... recurrence ) {
    addRecurrences( secondRecurrences, recurrence );
  }

  /**
   * Overrides any previously applied second recurrences with the provided recurrence.
   * 
   * @param recurrence
   *          the second recurrence. If null it will recur every second.
   */
  public void setSecondRecurrence( ITimeRecurrence recurrence ) {
    secondRecurrences.clear();
    if ( recurrence != null ) {
      secondRecurrences.add( recurrence );
    }
  }

  /**
   * Overrides any previously applied second recurrences with the provided recurrence.
   * 
   * @param recurrence
   *          the second recurrence. If no recurrence is provided it will recur every second.
   */
  public void setSecondRecurrence( Integer... recurrence ) {
    setRecurrences( secondRecurrences, recurrence );
  }

  /**
   * Returns the yearly recurrence.
   * 
   * @return the yearly recurrence. An empty list indicates a recurrence of every year.
   */
  @XmlElement
  public YearlyWrapper getYearlyRecurrences() {
    return yearlyRecurrences;
  }

  /**
   * Returns the monthly recurrence.
   * 
   * @return the monthly recurrence. An empty list indicates a recurrence of every month.
   */
  @XmlElement
  public MonthlyWrapper getMonthlyRecurrences() {
    return monthlyRecurrences;
  }

  /**
   * Returns the day of month recurrence.
   * 
   * @return the day of month recurrence. An empty list indicates a recurrence of every day of month.
   */
  @XmlElement
  public DayOfMonthWrapper getDayOfMonthRecurrences() {
    return dayOfMonthRecurrences;
  }

  /**
   * Returns the day of week recurrence.
   * 
   * @return the day of week recurrence. An empty list indicates a recurrence of every day of week.
   */
  @XmlElement
  public DayOfWeekWrapper getDayOfWeekRecurrences() {
    return dayOfWeekRecurrences;
  }

  /**
   * Returns the day of hourly recurrence.
   *
   * @return the day of hourly recurrence. An empty list indicates a recurrence of every hour.
   */
  @XmlElement
  public SecondWrapper getSecondRecurrences() {
    return secondRecurrences;
  }

  /**
   * Returns the day of minute recurrence.
   *
   * @return the day of minute recurrence. An empty list indicates a recurrence of every minute.
   */
  @XmlElement
  public HourlyWrapper getHourlyRecurrences() {
    return hourlyRecurrences;
  }

  @XmlElement
  public MinuteWrapper getMinuteRecurrences() {
    return minuteRecurrences;
  }

  // this setters are for JaxB unmarshalling
  private void setYearlyRecurrences( YearlyWrapper yearlyRecurrences ) {
    this.yearlyRecurrences = yearlyRecurrences;
  }

  private void setMonthlyRecurrences( MonthlyWrapper monthlyRecurrences ) {
    this.monthlyRecurrences = monthlyRecurrences;
  }

  private void setDayOfMonthRecurrences( DayOfMonthWrapper dayOfMonthRecurrences ) {
    this.dayOfMonthRecurrences = dayOfMonthRecurrences;
  }

  private void setDayOfWeekRecurrences( DayOfWeekWrapper dayOfWeekRecurrences ) {
    this.dayOfWeekRecurrences = dayOfWeekRecurrences;
  }

  private void setHourlyRecurrences( HourlyWrapper hourlyRecurrences ) {
    this.hourlyRecurrences = hourlyRecurrences;
  }

  private void setMinuteRecurrences( MinuteWrapper minuteRecurrences ) {
    this.minuteRecurrences = minuteRecurrences;
  }

  private void setSecondRecurrences( SecondWrapper secondRecurrences ) {
    this.secondRecurrences = secondRecurrences;
  }

  public String toString() {
    return QuartzCronStringFactory.createCronString( this );
  }

  private <U> List<U> filterNulls( U[] args ) {
    List<U> nonNullArgs = new ArrayList<U>();
    for ( U recurrence : args ) {
      if ( recurrence != null ) {
        nonNullArgs.add( recurrence );
      }
    }
    return nonNullArgs;
  }
}
