/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Created by rfellows on 10/26/15.
 */
public class ComplexJobTriggerTest {

  @Test
  public void testConstructor() throws Exception {
    assertThat( ComplexJobTrigger.class, hasValidBeanConstructor() );
  }

  @Test
  public void testGettersAndSetters() throws Exception {
    assertThat( ComplexJobTrigger.class, hasValidGettersAndSetters() );
  }

  @Test
  public void testInnerClasses() throws Exception {
    ComplexJobTrigger.DayOfMonthRecurrences recurrence = new ComplexJobTrigger.DayOfMonthRecurrences();
    assertNotNull( recurrence.getSequentialRecurrenceOrIncrementalRecurrenceOrQualifiedDayOfMonth() );

    ComplexJobTrigger.DayOfWeekRecurrences recurrence2 = new ComplexJobTrigger.DayOfWeekRecurrences();
    assertNotNull( recurrence2.getSequentialRecurrenceOrIncrementalRecurrenceOrRecurrenceList() );

    ComplexJobTrigger.HourlyRecurrences recurrence3 = new ComplexJobTrigger.HourlyRecurrences();
    assertNotNull( recurrence3.getSequentialRecurrenceOrIncrementalRecurrenceOrRecurrenceList() );

    ComplexJobTrigger.MinuteRecurrences recurrence4 = new ComplexJobTrigger.MinuteRecurrences();
    assertNotNull( recurrence4.getSequentialRecurrenceOrIncrementalRecurrenceOrRecurrenceList() );

    ComplexJobTrigger.MonthlyRecurrences recurrence5 = new ComplexJobTrigger.MonthlyRecurrences();
    assertNotNull( recurrence5.getSequentialRecurrenceOrIncrementalRecurrenceOrRecurrenceList() );

    ComplexJobTrigger.SecondRecurrences recurrence6 = new ComplexJobTrigger.SecondRecurrences();
    assertNotNull( recurrence6.getSequentialRecurrenceOrIncrementalRecurrenceOrRecurrenceList() );

    ComplexJobTrigger.YearlyRecurrences recurrence8 = new ComplexJobTrigger.YearlyRecurrences();
    assertNotNull( recurrence8.getSequentialRecurrenceOrIncrementalRecurrenceOrRecurrenceList() );

  }
}
