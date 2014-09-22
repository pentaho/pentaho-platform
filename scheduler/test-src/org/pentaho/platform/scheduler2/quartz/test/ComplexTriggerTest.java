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

package org.pentaho.platform.scheduler2.quartz.test;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.scheduler2.recur.IncrementalRecurrence;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek.DayOfWeek;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek.DayOfWeekQualifier;
import org.pentaho.platform.scheduler2.recur.RecurrenceList;
import org.pentaho.platform.scheduler2.recur.SequentialRecurrence;

@SuppressWarnings( "nls" )
public class ComplexTriggerTest {

  @Test
  public void timeSliceTest() {
    ComplexJobTrigger trigger = new ComplexJobTrigger();
    trigger.addYearlyRecurrence( 2010 );
    trigger.addMonthlyRecurrence( ComplexJobTrigger.MARCH );
    trigger.addDayOfWeekRecurrence( ComplexJobTrigger.SATURDAY );
    trigger.addHourlyRecurrence( 12 );
    trigger.addMinuteRecurrence( 15 );
    trigger.addSecondRecurrence( 1 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0,1 0,15 0,12 ? 3 7 2010" );

    trigger = new ComplexJobTrigger();
    trigger.setYearlyRecurrence( 2010 );
    trigger.setMonthlyRecurrence( ComplexJobTrigger.MARCH );
    trigger.setDayOfWeekRecurrence( ComplexJobTrigger.SATURDAY );
    trigger.setHourlyRecurrence( 12 );
    trigger.setMinuteRecurrence( 15 );
    trigger.setSecondRecurrence( 1 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "1 15 12 ? 3 7 2010" );

    trigger = new ComplexJobTrigger();
    trigger.addYearlyRecurrence( 2010 );
    trigger.addMonthlyRecurrence( ComplexJobTrigger.MARCH );
    trigger.addDayOfMonthRecurrence( 10 );
    trigger.addHourlyRecurrence( 12 );
    trigger.addMinuteRecurrence( 15 );
    trigger.addSecondRecurrence( 1 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0,1 0,15 0,12 10 3 ? 2010" );

    trigger = new ComplexJobTrigger();
    trigger.setYearlyRecurrence( 2010 );
    trigger.setMonthlyRecurrence( ComplexJobTrigger.MARCH );
    trigger.setDayOfMonthRecurrence( 10 );
    trigger.setHourlyRecurrence( 12 );
    trigger.setMinuteRecurrence( 15 );
    trigger.setSecondRecurrence( 1 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "1 15 12 10 3 ? 2010" );

    trigger = new ComplexJobTrigger( 2010, ComplexJobTrigger.MARCH, null, ComplexJobTrigger.SATURDAY, 12 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 0 12 ? 3 7 2010" );

    trigger = new ComplexJobTrigger( 2010, ComplexJobTrigger.MARCH, 10, null, 12 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 0 12 10 3 ? 2010" );

    trigger = QuartzScheduler.createComplexTrigger( "5 15 12 ? 3 7 2010" );
    Assert.assertEquals( trigger.getYearlyRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getYearlyRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getYearlyRecurrences().get( 0 ) ).getValues().size(), 1 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getYearlyRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 2010 ) );

    Assert.assertEquals( trigger.getMonthlyRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getMonthlyRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMonthlyRecurrences().get( 0 ) ).getValues().size(), 1 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMonthlyRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( ComplexJobTrigger.MARCH ) );

    Assert.assertEquals( trigger.getDayOfMonthRecurrences().size(), 0 );

    Assert.assertEquals( trigger.getDayOfWeekRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getDayOfWeekRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getDayOfWeekRecurrences().get( 0 ) ).getValues().size(), 1 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getDayOfWeekRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 7 ) );

    Assert.assertEquals( trigger.getHourlyRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getHourlyRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getHourlyRecurrences().get( 0 ) ).getValues().size(), 1 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getHourlyRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 12 ) );

    Assert.assertEquals( trigger.getMinuteRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getMinuteRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMinuteRecurrences().get( 0 ) ).getValues().size(), 1 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMinuteRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 15 ) );

    Assert.assertEquals( trigger.getSecondRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getSecondRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getSecondRecurrences().get( 0 ) ).getValues().size(), 1 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getSecondRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 5 ) );

    trigger = QuartzScheduler.createComplexTrigger( "* * * * * ? *" );
    Assert.assertEquals( trigger.getYearlyRecurrences().size(), 0 );
    Assert.assertEquals( trigger.getMonthlyRecurrences().size(), 0 );
    Assert.assertEquals( trigger.getDayOfMonthRecurrences().size(), 0 );
    Assert.assertEquals( trigger.getDayOfWeekRecurrences().size(), 0 );
    Assert.assertEquals( trigger.getHourlyRecurrences().size(), 0 );
    Assert.assertEquals( trigger.getMinuteRecurrences().size(), 0 );
    Assert.assertEquals( trigger.getSecondRecurrences().size(), 0 );

    trigger = QuartzScheduler.createComplexTrigger( "* * * ? * * *" );
    Assert.assertEquals( trigger.getYearlyRecurrences().size(), 0 );
    Assert.assertEquals( trigger.getMonthlyRecurrences().size(), 0 );
    Assert.assertEquals( trigger.getDayOfMonthRecurrences().size(), 0 );
    Assert.assertEquals( trigger.getDayOfWeekRecurrences().size(), 0 );
    Assert.assertEquals( trigger.getHourlyRecurrences().size(), 0 );
    Assert.assertEquals( trigger.getMinuteRecurrences().size(), 0 );
    Assert.assertEquals( trigger.getSecondRecurrences().size(), 0 );

    trigger = new ComplexJobTrigger();
    trigger.addYearlyRecurrence( 2010, 2013 );
    trigger.addMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.MAY );
    trigger.addDayOfMonthRecurrence( 3, 10 );
    trigger.addHourlyRecurrence( 12, 15 );
    trigger.addMinuteRecurrence( 30, 45 );
    trigger.addSecondRecurrence( 1, 2 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0,1,2 0,30,45 0,12,15 3,10 3,5 ? 2010,2013" );

    trigger = new ComplexJobTrigger();
    trigger.setYearlyRecurrence( 2010, 2013 );
    trigger.setMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.MAY );
    trigger.setDayOfMonthRecurrence( 3, 10 );
    trigger.setHourlyRecurrence( 12, 15 );
    trigger.setMinuteRecurrence( 30, 45 );
    trigger.setSecondRecurrence( 1, 2 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "1,2 30,45 12,15 3,10 3,5 ? 2010,2013" );

    trigger = new ComplexJobTrigger();
    trigger.addYearlyRecurrence( 2010, 2013 );
    trigger.addMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.MAY );
    trigger.addDayOfMonthRecurrence( 3, 10 );
    trigger.addHourlyRecurrence( 12, 15 );
    trigger.addMinuteRecurrence( 30, 45 );
    trigger.addSecondRecurrence( 1, 2 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0,1,2 0,30,45 0,12,15 3,10 3,5 ? 2010,2013" );

    trigger = new ComplexJobTrigger();
    trigger.setYearlyRecurrence( 2010, 2013 );
    trigger.setMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.MAY );
    trigger.setDayOfMonthRecurrence( 3, 10 );
    trigger.setHourlyRecurrence( 12, 15 );
    trigger.setMinuteRecurrence( 30, 45 );
    trigger.setSecondRecurrence( 1, 2 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "1,2 30,45 12,15 3,10 3,5 ? 2010,2013" );

    trigger = new ComplexJobTrigger();
    trigger.addYearlyRecurrence( 2010, 2013 );
    trigger.addMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.MAY );
    trigger.addDayOfWeekRecurrence( ComplexJobTrigger.SATURDAY, ComplexJobTrigger.SUNDAY );
    trigger.addHourlyRecurrence( 12, 15 );
    trigger.addMinuteRecurrence( 30, 45 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 0,30,45 0,12,15 ? 3,5 1,7 2010,2013" );

    trigger = new ComplexJobTrigger();
    trigger.setYearlyRecurrence( 2010, 2013 );
    trigger.setMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.MAY );
    trigger.setDayOfWeekRecurrence( ComplexJobTrigger.SATURDAY, ComplexJobTrigger.SUNDAY );
    trigger.setHourlyRecurrence( 12, 15 );
    trigger.setMinuteRecurrence( 30, 45 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 30,45 12,15 ? 3,5 1,7 2010,2013" );

    trigger = new ComplexJobTrigger();
    trigger.addYearlyRecurrence( 2010, 2013 );
    trigger.addMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.MAY );
    trigger.addDayOfWeekRecurrence( ComplexJobTrigger.SATURDAY, ComplexJobTrigger.SUNDAY );
    trigger.addHourlyRecurrence( 12, 15 );
    trigger.addMinuteRecurrence( 30, 45 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 0,30,45 0,12,15 ? 3,5 1,7 2010,2013" );

    trigger = new ComplexJobTrigger();
    trigger.setYearlyRecurrence( 2010, 2013 );
    trigger.setMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.MAY );
    trigger.setDayOfWeekRecurrence( ComplexJobTrigger.SATURDAY, ComplexJobTrigger.SUNDAY );
    trigger.setHourlyRecurrence( 12, 15 );
    trigger.setMinuteRecurrence( 30, 45 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 30,45 12,15 ? 3,5 1,7 2010,2013" );

    trigger = QuartzScheduler.createComplexTrigger( "0 30,45 12,15 3,10 3,5 ? 2010,2013" );
    Assert.assertEquals( trigger.getYearlyRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getYearlyRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getYearlyRecurrences().get( 0 ) ).getValues().size(), 2 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getYearlyRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 2010 ) );
    Assert.assertEquals( ( (RecurrenceList) trigger.getYearlyRecurrences().get( 0 ) ).getValues().get( 1 ),
        new Integer( 2013 ) );

    Assert.assertEquals( trigger.getMonthlyRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getMonthlyRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMonthlyRecurrences().get( 0 ) ).getValues().size(), 2 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMonthlyRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( ComplexJobTrigger.MARCH ) );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMonthlyRecurrences().get( 0 ) ).getValues().get( 1 ),
        new Integer( ComplexJobTrigger.MAY ) );

    Assert.assertEquals( trigger.getDayOfMonthRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getDayOfMonthRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getDayOfMonthRecurrences().get( 0 ) ).getValues().size(), 2 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getDayOfMonthRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 3 ) );
    Assert.assertEquals( ( (RecurrenceList) trigger.getDayOfMonthRecurrences().get( 0 ) ).getValues().get( 1 ),
        new Integer( 10 ) );

    Assert.assertEquals( trigger.getDayOfWeekRecurrences().size(), 0 );

    Assert.assertEquals( trigger.getHourlyRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getHourlyRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getHourlyRecurrences().get( 0 ) ).getValues().size(), 2 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getHourlyRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 12 ) );
    Assert.assertEquals( ( (RecurrenceList) trigger.getHourlyRecurrences().get( 0 ) ).getValues().get( 1 ),
        new Integer( 15 ) );

    Assert.assertEquals( trigger.getMinuteRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getMinuteRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMinuteRecurrences().get( 0 ) ).getValues().size(), 2 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMinuteRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 30 ) );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMinuteRecurrences().get( 0 ) ).getValues().get( 1 ),
        new Integer( 45 ) );

    trigger = new ComplexJobTrigger();
    trigger.addYearlyRecurrence( new SequentialRecurrence( 2010, 2013 ) );
    trigger.addMonthlyRecurrence( new SequentialRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.MAY ) );
    trigger.addDayOfMonthRecurrence( new SequentialRecurrence( 15, 20 ) );
    trigger.addHourlyRecurrence( new SequentialRecurrence( 12, 15 ) );
    trigger.addMinuteRecurrence( new SequentialRecurrence( 30, 45 ) );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 0,30-45 0,12-15 15-20 3-5 ? 2010-2013" );

    trigger = new ComplexJobTrigger();
    trigger.setYearlyRecurrence( new SequentialRecurrence( 2010, 2013 ) );
    trigger.setMonthlyRecurrence( new SequentialRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.MAY ) );
    trigger.setDayOfMonthRecurrence( new SequentialRecurrence( 15, 20 ) );
    trigger.setHourlyRecurrence( new SequentialRecurrence( 12, 15 ) );
    trigger.setMinuteRecurrence( new SequentialRecurrence( 30, 45 ) );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 30-45 12-15 15-20 3-5 ? 2010-2013" );

    trigger = new ComplexJobTrigger();
    trigger.addYearlyRecurrence( 2010, 2011, 2012, 2013 );
    trigger.addMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.APRIL, ComplexJobTrigger.MAY );
    trigger.addDayOfMonthRecurrence( 15, 16, 17, 18, 19, 20 );
    trigger.addHourlyRecurrence( 12, 13, 14, 15 );
    trigger.addMinuteRecurrence( 30, 31, 32, 33, 34, 35, 36 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 0,30-36 0,12-15 15-20 3-5 ? 2010-2013" );

    trigger = new ComplexJobTrigger();
    trigger.setYearlyRecurrence( 2010, 2011, 2012, 2013 );
    trigger.setMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.APRIL, ComplexJobTrigger.MAY );
    trigger.setDayOfMonthRecurrence( 15, 16, 17, 18, 19, 20 );
    trigger.setHourlyRecurrence( 12, 13, 14, 15 );
    trigger.setMinuteRecurrence( 30, 31, 32, 33, 34, 35, 36 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 30-36 12-15 15-20 3-5 ? 2010-2013" );

    trigger = new ComplexJobTrigger();
    trigger.addYearlyRecurrence( 2010, 2011, 2012, 2013 );
    trigger.addMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.APRIL, ComplexJobTrigger.MAY );
    trigger.addDayOfWeekRecurrence( ComplexJobTrigger.SUNDAY, ComplexJobTrigger.MONDAY, ComplexJobTrigger.TUESDAY,
        ComplexJobTrigger.WEDNESDAY, ComplexJobTrigger.THURSDAY, ComplexJobTrigger.FRIDAY, ComplexJobTrigger.SATURDAY );
    trigger.addHourlyRecurrence( 12, 13, 14, 15 );
    trigger.addMinuteRecurrence( 30, 31, 32, 33, 34, 35, 36 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 0,30-36 0,12-15 ? 3-5 1-7 2010-2013" );

    trigger = new ComplexJobTrigger();
    trigger.setYearlyRecurrence( 2010, 2011, 2012, 2013 );
    trigger.setMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.APRIL, ComplexJobTrigger.MAY );
    trigger.setDayOfWeekRecurrence( ComplexJobTrigger.SUNDAY, ComplexJobTrigger.MONDAY, ComplexJobTrigger.TUESDAY,
        ComplexJobTrigger.WEDNESDAY, ComplexJobTrigger.THURSDAY, ComplexJobTrigger.FRIDAY, ComplexJobTrigger.SATURDAY );
    trigger.setHourlyRecurrence( 12, 13, 14, 15 );
    trigger.setMinuteRecurrence( 30, 31, 32, 33, 34, 35, 36 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 30-36 12-15 ? 3-5 1-7 2010-2013" );

    trigger = QuartzScheduler.createComplexTrigger( "0 30-45 12-15 ? 3-5 1-7 2010-2013" );
    Assert.assertEquals( trigger.getYearlyRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getYearlyRecurrences().get( 0 ) instanceof SequentialRecurrence );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getYearlyRecurrences().get( 0 ) ).getFirstValue(),
        new Integer( 2010 ) );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getYearlyRecurrences().get( 0 ) ).getLastValue(),
        new Integer( 2013 ) );

    Assert.assertEquals( trigger.getMonthlyRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getMonthlyRecurrences().get( 0 ) instanceof SequentialRecurrence );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getMonthlyRecurrences().get( 0 ) ).getFirstValue(),
        new Integer( ComplexJobTrigger.MARCH ) );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getMonthlyRecurrences().get( 0 ) ).getLastValue(),
        new Integer( ComplexJobTrigger.MAY ) );

    Assert.assertEquals( trigger.getDayOfMonthRecurrences().size(), 0 );

    Assert.assertEquals( trigger.getDayOfWeekRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getDayOfWeekRecurrences().get( 0 ) instanceof SequentialRecurrence );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getDayOfWeekRecurrences().get( 0 ) ).getFirstValue(),
        new Integer( ComplexJobTrigger.SUNDAY ) );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getDayOfWeekRecurrences().get( 0 ) ).getLastValue(),
        new Integer( ComplexJobTrigger.SATURDAY ) );

    Assert.assertEquals( trigger.getHourlyRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getHourlyRecurrences().get( 0 ) instanceof SequentialRecurrence );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getHourlyRecurrences().get( 0 ) ).getFirstValue(),
        new Integer( 12 ) );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getHourlyRecurrences().get( 0 ) ).getLastValue(),
        new Integer( 15 ) );

    Assert.assertEquals( trigger.getMinuteRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getMinuteRecurrences().get( 0 ) instanceof SequentialRecurrence );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getMinuteRecurrences().get( 0 ) ).getFirstValue(),
        new Integer( 30 ) );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getMinuteRecurrences().get( 0 ) ).getLastValue(),
        new Integer( 45 ) );

    trigger = new ComplexJobTrigger();
    trigger.addDayOfWeekRecurrence( new QualifiedDayOfWeek( DayOfWeekQualifier.LAST, DayOfWeek.FRI ) );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 0 0 ? * 6L *" );

    trigger = new ComplexJobTrigger();
    trigger.addDayOfWeekRecurrence( new QualifiedDayOfWeek( DayOfWeekQualifier.LAST, DayOfWeek.FRI ) );
    trigger.addDayOfWeekRecurrence( new QualifiedDayOfWeek( DayOfWeekQualifier.THIRD, DayOfWeek.SUN ) );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "0 0 0 ? * 6L,1#3 *" );

    trigger = QuartzScheduler.createComplexTrigger( "* 0 0 ? * 6L,1#3 *" );
    Assert.assertEquals( trigger.getDayOfWeekRecurrences().size(), 2 );
    Assert.assertTrue( trigger.getDayOfWeekRecurrences().get( 0 ) instanceof QualifiedDayOfWeek );
    Assert.assertEquals( ( (QualifiedDayOfWeek) trigger.getDayOfWeekRecurrences().get( 0 ) ).getQualifier(),
        DayOfWeekQualifier.LAST );
    Assert.assertEquals( ( (QualifiedDayOfWeek) trigger.getDayOfWeekRecurrences().get( 0 ) ).getDayOfWeek(),
        DayOfWeek.FRI );
    Assert.assertTrue( trigger.getDayOfWeekRecurrences().get( 1 ) instanceof QualifiedDayOfWeek );
    Assert.assertEquals( ( (QualifiedDayOfWeek) trigger.getDayOfWeekRecurrences().get( 1 ) ).getQualifier(),
        DayOfWeekQualifier.THIRD );
    Assert.assertEquals( ( (QualifiedDayOfWeek) trigger.getDayOfWeekRecurrences().get( 1 ) ).getDayOfWeek(),
        DayOfWeek.SUN );

    trigger = new ComplexJobTrigger();
    trigger.addYearlyRecurrence( 2010, 2013 );
    trigger.addYearlyRecurrence( new SequentialRecurrence( 2015, 2020 ) );
    trigger.addYearlyRecurrence( new IncrementalRecurrence( 2025, 5 ) );
    trigger.addMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.MAY );
    trigger.addMonthlyRecurrence( new SequentialRecurrence( ComplexJobTrigger.JULY, ComplexJobTrigger.SEPTEMBER ) );
    trigger.addMonthlyRecurrence( new IncrementalRecurrence( ComplexJobTrigger.JANUARY, 3 ) );
    trigger.addDayOfMonthRecurrence( 3, 10 );
    trigger.addDayOfMonthRecurrence( new SequentialRecurrence( 15, 20 ) );
    trigger.addDayOfMonthRecurrence( new IncrementalRecurrence( 21, 3 ) );
    trigger.addHourlyRecurrence( 12, 15 );
    trigger.addHourlyRecurrence( new SequentialRecurrence( 25, 30 ) );
    trigger.addHourlyRecurrence( new IncrementalRecurrence( 10, 5 ) );
    trigger.addMinuteRecurrence( 30, 45 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(),
        "0 0,30,45 0,12,15,25-30,10/5 3,10,15-20,21/3 3,5,7-9,1/3 ? 2010,2013,2015-2020,2025/5" );

    trigger = new ComplexJobTrigger();
    trigger.setYearlyRecurrence( 2010, 2013 );
    trigger.addYearlyRecurrence( new SequentialRecurrence( 2015, 2020 ) );
    trigger.addYearlyRecurrence( new IncrementalRecurrence( 2025, 5 ) );
    trigger.setMonthlyRecurrence( ComplexJobTrigger.MARCH, ComplexJobTrigger.MAY );
    trigger.addMonthlyRecurrence( new SequentialRecurrence( ComplexJobTrigger.JULY, ComplexJobTrigger.SEPTEMBER ) );
    trigger.addMonthlyRecurrence( new IncrementalRecurrence( ComplexJobTrigger.JANUARY, 3 ) );
    trigger.setDayOfWeekRecurrence( ComplexJobTrigger.THURSDAY, ComplexJobTrigger.FRIDAY );
    trigger.addDayOfWeekRecurrence( new SequentialRecurrence( ComplexJobTrigger.SUNDAY, ComplexJobTrigger.MONDAY ) );
    trigger.setHourlyRecurrence( 12, 15 );
    trigger.addHourlyRecurrence( new SequentialRecurrence( 25, 30 ) );
    trigger.addHourlyRecurrence( new IncrementalRecurrence( 10, 5 ) );
    trigger.setMinuteRecurrence( 30, 45 );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(),
        "0 30,45 12,15,25-30,10/5 ? 3,5,7-9,1/3 5,6,1-2 2010,2013,2015-2020,2025/5" );

    trigger = new ComplexJobTrigger();
    trigger.setYearlyRecurrence( new SequentialRecurrence( 2015, 2020 ) );
    trigger.setMonthlyRecurrence( new SequentialRecurrence( ComplexJobTrigger.JULY, ComplexJobTrigger.SEPTEMBER ) );
    trigger.setDayOfWeekRecurrence( new SequentialRecurrence( ComplexJobTrigger.SUNDAY, ComplexJobTrigger.MONDAY ) );
    trigger.setHourlyRecurrence( new SequentialRecurrence( 25, 30 ) );
    trigger.setMinuteRecurrence( new SequentialRecurrence( 5, 10 ) );
    trigger.setSecondRecurrence( new SequentialRecurrence( 30, 35 ) );
    System.out.println( trigger.toString() );
    Assert.assertEquals( trigger.toString(), "30-35 5-10 25-30 ? 7-9 1-2 2015-2020" );

    trigger =
        QuartzScheduler
            .createComplexTrigger( "0 30,45 12,15,25-30,10/5 3,10,15-20,21/3 3,5,7-9,"
                    + "1/3 ? 2010,2013,2015-2020,2025/5" );
    Assert.assertEquals( trigger.getYearlyRecurrences().size(), 3 );
    Assert.assertTrue( trigger.getYearlyRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getYearlyRecurrences().get( 0 ) ).getValues().size(), 2 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getYearlyRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 2010 ) );
    Assert.assertEquals( ( (RecurrenceList) trigger.getYearlyRecurrences().get( 0 ) ).getValues().get( 1 ),
        new Integer( 2013 ) );

    Assert.assertTrue( trigger.getYearlyRecurrences().get( 1 ) instanceof SequentialRecurrence );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getYearlyRecurrences().get( 1 ) ).getFirstValue(),
        new Integer( 2015 ) );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getYearlyRecurrences().get( 1 ) ).getLastValue(),
        new Integer( 2020 ) );

    Assert.assertTrue( trigger.getYearlyRecurrences().get( 2 ) instanceof IncrementalRecurrence );
    Assert.assertEquals( ( (IncrementalRecurrence) trigger.getYearlyRecurrences().get( 2 ) ).getStartingValue(),
        new Integer( 2025 ) );
    Assert.assertEquals( ( (IncrementalRecurrence) trigger.getYearlyRecurrences().get( 2 ) ).getIncrement(),
        new Integer( 5 ) );

    Assert.assertEquals( trigger.getMonthlyRecurrences().size(), 3 );
    Assert.assertTrue( trigger.getMonthlyRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMonthlyRecurrences().get( 0 ) ).getValues().size(), 2 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMonthlyRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( ComplexJobTrigger.MARCH ) );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMonthlyRecurrences().get( 0 ) ).getValues().get( 1 ),
        new Integer( ComplexJobTrigger.MAY ) );

    Assert.assertTrue( trigger.getMonthlyRecurrences().get( 1 ) instanceof SequentialRecurrence );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getMonthlyRecurrences().get( 1 ) ).getFirstValue(),
        new Integer( ComplexJobTrigger.JULY ) );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getMonthlyRecurrences().get( 1 ) ).getLastValue(),
        new Integer( ComplexJobTrigger.SEPTEMBER ) );

    Assert.assertTrue( trigger.getMonthlyRecurrences().get( 2 ) instanceof IncrementalRecurrence );
    Assert.assertEquals( ( (IncrementalRecurrence) trigger.getMonthlyRecurrences().get( 2 ) ).getStartingValue(),
        new Integer( ComplexJobTrigger.JANUARY ) );
    Assert.assertEquals( ( (IncrementalRecurrence) trigger.getMonthlyRecurrences().get( 2 ) ).getIncrement(),
        new Integer( 3 ) );

    Assert.assertEquals( trigger.getDayOfMonthRecurrences().size(), 3 );
    Assert.assertTrue( trigger.getDayOfMonthRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getDayOfMonthRecurrences().get( 0 ) ).getValues().size(), 2 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getDayOfMonthRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 3 ) );
    Assert.assertEquals( ( (RecurrenceList) trigger.getDayOfMonthRecurrences().get( 0 ) ).getValues().get( 1 ),
        new Integer( 10 ) );

    Assert.assertTrue( trigger.getDayOfMonthRecurrences().get( 1 ) instanceof SequentialRecurrence );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getDayOfMonthRecurrences().get( 1 ) ).getFirstValue(),
        new Integer( 15 ) );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getDayOfMonthRecurrences().get( 1 ) ).getLastValue(),
        new Integer( 20 ) );

    Assert.assertTrue( trigger.getDayOfMonthRecurrences().get( 2 ) instanceof IncrementalRecurrence );
    Assert.assertEquals( ( (IncrementalRecurrence) trigger.getDayOfMonthRecurrences().get( 2 ) ).getStartingValue(),
        new Integer( 21 ) );
    Assert.assertEquals( ( (IncrementalRecurrence) trigger.getDayOfMonthRecurrences().get( 2 ) ).getIncrement(),
        new Integer( 3 ) );
    Assert.assertEquals( trigger.getDayOfMonthRecurrences().size(), 3 );

    Assert.assertEquals( trigger.getDayOfWeekRecurrences().size(), 0 );

    Assert.assertEquals( trigger.getHourlyRecurrences().size(), 3 );
    Assert.assertTrue( trigger.getHourlyRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getHourlyRecurrences().get( 0 ) ).getValues().size(), 2 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getHourlyRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 12 ) );
    Assert.assertEquals( ( (RecurrenceList) trigger.getHourlyRecurrences().get( 0 ) ).getValues().get( 1 ),
        new Integer( 15 ) );

    Assert.assertTrue( trigger.getHourlyRecurrences().get( 1 ) instanceof SequentialRecurrence );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getHourlyRecurrences().get( 1 ) ).getFirstValue(),
        new Integer( 25 ) );
    Assert.assertEquals( ( (SequentialRecurrence) trigger.getHourlyRecurrences().get( 1 ) ).getLastValue(),
        new Integer( 30 ) );

    Assert.assertTrue( trigger.getHourlyRecurrences().get( 2 ) instanceof IncrementalRecurrence );
    Assert.assertEquals( ( (IncrementalRecurrence) trigger.getHourlyRecurrences().get( 2 ) ).getStartingValue(),
        new Integer( 10 ) );
    Assert.assertEquals( ( (IncrementalRecurrence) trigger.getHourlyRecurrences().get( 2 ) ).getIncrement(),
        new Integer( 5 ) );

    Assert.assertEquals( trigger.getMinuteRecurrences().size(), 1 );
    Assert.assertTrue( trigger.getMinuteRecurrences().get( 0 ) instanceof RecurrenceList );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMinuteRecurrences().get( 0 ) ).getValues().size(), 2 );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMinuteRecurrences().get( 0 ) ).getValues().get( 0 ),
        new Integer( 30 ) );
    Assert.assertEquals( ( (RecurrenceList) trigger.getMinuteRecurrences().get( 0 ) ).getValues().get( 1 ),
        new Integer( 45 ) );

  }
}
