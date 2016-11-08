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

package org.pentaho.platform.scheduler2.ws.test;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.recur.ITimeRecurrence;
import org.pentaho.platform.api.scheduler2.wrappers.ITimeWrapper;
import org.pentaho.platform.scheduler2.recur.IncrementalRecurrence;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek;
import org.pentaho.platform.scheduler2.recur.RecurrenceList;
import org.pentaho.platform.scheduler2.recur.SequentialRecurrence;

import junit.framework.Assert;

@SuppressWarnings( "nls" )
public class ComplexTriggerJAXBTest {

  ComplexJobTrigger trigger;

  IncrementalRecurrence inc = new IncrementalRecurrence( 2010, 5 );

  SequentialRecurrence seq = new SequentialRecurrence( 5, 12 );

  QualifiedDayOfWeek qday = new QualifiedDayOfWeek( QualifiedDayOfWeek.DayOfWeekQualifier.FIFTH,
      QualifiedDayOfWeek.DayOfWeek.MON );

  Integer[] list = { 7, 11 };

  @Before
  public void init() {
    trigger = new ComplexJobTrigger();
  }

  private ComplexJobTrigger process( ComplexJobTrigger t ) throws JAXBException {
    return JaxBUtil.outin( t, ComplexJobTrigger.class, SequentialRecurrence.class, IncrementalRecurrence.class,
        RecurrenceList.class, QualifiedDayOfWeek.class );
  }

  @Test
  public void testYearlDimension() throws JAXBException {
    trigger.addYearlyRecurrence( inc );
    trigger.addYearlyRecurrence( seq );
    trigger.addYearlyRecurrence( list );

    assertRecurrencesCorrect( "YEAR", 3, process( trigger ).getYearlyRecurrences() );
  }

  @Test
  public void testMonthDimension() throws JAXBException {
    trigger.addMonthlyRecurrence( inc );
    trigger.addMonthlyRecurrence( seq );
    trigger.addMonthlyRecurrence( list );
    assertRecurrencesCorrect( "MONTH", 3, process( trigger ).getMonthlyRecurrences() );
  }

  @Test
  public void testHourDimension() throws JAXBException {
    trigger.setHourlyRecurrence( inc );
    trigger.addHourlyRecurrence( seq );
    trigger.addHourlyRecurrence( list );
    assertRecurrencesCorrect( "MINUTE", 3, process( trigger ).getHourlyRecurrences() );
  }

  @Test
  public void testMinuteDimension() throws JAXBException {
    trigger.setMinuteRecurrence( inc );
    trigger.addMinuteRecurrence( seq );
    trigger.addMinuteRecurrence( list );
    assertRecurrencesCorrect( "MINUTE", 3, process( trigger ).getMinuteRecurrences() );
  }

  @Test
  public void testDayOfMonthDimension() throws JAXBException {
    trigger.addDayOfMonthRecurrence( inc );
    trigger.addDayOfMonthRecurrence( seq );
    trigger.addDayOfMonthRecurrence( list );
    assertRecurrencesCorrect( "DAYOFMONTH", 3, process( trigger ).getDayOfMonthRecurrences() );
  }

  @Test
  public void testDayOfWeekDimension() throws JAXBException {
    trigger.addDayOfWeekRecurrence( qday );
    trigger.addDayOfWeekRecurrence( inc );
    trigger.addDayOfWeekRecurrence( seq );
    trigger.addDayOfWeekRecurrence( list );
    assertRecurrencesCorrect( "DAYOFWEEK", 4, process( trigger ).getDayOfWeekRecurrences() );
  }

  private void assertRecurrencesCorrect( String dimension, int expectedCount, ITimeWrapper recurrences ) {
    int count = 0;
    for ( ITimeRecurrence rec : recurrences.getRecurrences() ) {
      if ( rec instanceof IncrementalRecurrence ) {
        count++;
        IncrementalRecurrence i = (IncrementalRecurrence) rec;
        Assert.assertEquals( "Wrong starting value for dimension " + dimension, inc.getStartingValue(), i
            .getStartingValue() );
        Assert.assertEquals( "Wrong increment for dimension " + dimension, inc.getIncrement(), i.getIncrement() );
      }
      if ( rec instanceof SequentialRecurrence ) {
        count++;
        SequentialRecurrence s = (SequentialRecurrence) rec;
        Assert.assertEquals( "Wrong first value for dimension " + dimension, seq.getFirstValue(), s.getFirstValue() );
        Assert.assertEquals( "Wrong last value for dimension " + dimension, seq.getLastValue(), s.getLastValue() );
      }
      if ( rec instanceof RecurrenceList ) {
        count++;
        RecurrenceList l = (RecurrenceList) rec;
        Assert.assertEquals( "Wrong first value for dimension " + dimension, list[0], l.getValues().get( 0 ) );
        Assert.assertEquals( "Wrong second value for dimension " + dimension, list[1], l.getValues().get( 1 ) );
      }
      if ( rec instanceof QualifiedDayOfWeek ) {
        count++;
        QualifiedDayOfWeek q = (QualifiedDayOfWeek) rec;
        Assert.assertEquals( "Wrong day of week for dimension " + dimension, qday.getDayOfWeek(), q.getDayOfWeek() );
        Assert.assertEquals( "Wrong qualifier for dimension " + dimension, qday.getQualifier(), q.getQualifier() );
      }
    }
    Assert.assertEquals( "A recurrence type was expected but not found", expectedCount, count );
  }

}
