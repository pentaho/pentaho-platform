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
