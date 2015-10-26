/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
