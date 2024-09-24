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
