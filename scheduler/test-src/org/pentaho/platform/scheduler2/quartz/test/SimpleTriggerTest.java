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

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;

@SuppressWarnings( "nls" )
public class SimpleTriggerTest {

  @Test
  public void defaultValidTest() {
    SimpleJobTrigger trigger = new SimpleJobTrigger();
    Assert.assertNull( trigger.getStartTime() );
    Assert.assertNull( trigger.getEndTime() );
    Assert.assertFalse( trigger.getRepeatCount() != 0 && trigger.getRepeatInterval() < 1 );
  }

  @Test
  public void defaultParamsNoDatesTest() {
    SimpleJobTrigger trigger = new SimpleJobTrigger();
    Assert.assertEquals( trigger.toString(), "repeatCount=0, repeatInterval=0, startTime=null, endTime=null" );
  }

  @Test
  public void defaultParamsDatesTest() {
    Calendar now = Calendar.getInstance();
    Calendar nextMonth = Calendar.getInstance();
    nextMonth.add( Calendar.MONTH, 1 );
    SimpleJobTrigger trigger = new SimpleJobTrigger( now.getTime(), nextMonth.getTime(), 1, 1000 );
    Assert.assertEquals( trigger.toString(), "repeatCount=1, repeatInterval=1000, startTime="
        + now.getTime().toString() + ", endTime=" + nextMonth.getTime().toString() );
  }

}
