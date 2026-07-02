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

import org.junit.Before;
import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by rfellows on 10/26/15.
 */
public class ComplexJobTriggerProxyTest {

  ComplexJobTriggerProxy jobTrigger;

  @Before
  public void setUp() throws Exception {
    jobTrigger = new ComplexJobTriggerProxy();
  }

  @Test
  public void testConstructor() throws Exception {
    assertThat( ComplexJobTriggerProxy.class, hasValidBeanConstructor() );
  }

  @Test
  public void testGettersAndSetters() throws Exception {
    String[] excludes = new String[] {
      "daysOfMonth",
      "daysOfWeek",
      "monthsOfYear",
      "weeksOfMonth",
      "years"
    };
    assertThat( ComplexJobTriggerProxy.class, hasValidGettersAndSettersExcluding( excludes ) );
  }

  @Test
  public void testDaysOfMonth() throws Exception {
    assertNotNull( jobTrigger.getDaysOfMonth() );
    assertEquals( 0, jobTrigger.getDaysOfMonth().size() );
  }

  @Test
  public void testDaysOfWeek() throws Exception {
    assertNotNull( jobTrigger.getDaysOfWeek() );
    assertEquals( 0, jobTrigger.getDaysOfWeek().size() );
  }

  @Test
  public void testMonthsOfYear() throws Exception {
    assertNotNull( jobTrigger.getMonthsOfYear() );
    assertEquals( 0, jobTrigger.getMonthsOfYear().size() );
  }

  @Test
  public void testWeeksOfMonth() throws Exception {
    assertNotNull( jobTrigger.getWeeksOfMonth() );
    assertEquals( 0, jobTrigger.getWeeksOfMonth().size() );
  }

  @Test
  public void testYears() throws Exception {
    assertNotNull( jobTrigger.getYears() );
    assertEquals( 0, jobTrigger.getYears().size() );
  }

}
