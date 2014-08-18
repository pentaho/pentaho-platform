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
* Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
*/
package org.pentaho.platform.monitoring;

import com.google.common.eventbus.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.monitoring.IMonitoringEvent;
import org.pentaho.platform.api.monitoring.IMonitoringService;
import org.pentaho.platform.api.monitoring.IMonitoringSubscriber;

import java.io.Serializable;

public class MonitoringServiceTest {

  IMonitoringService service;

  @Before
  public void setUp() {

    // we will not be registering MonitoringService in the pentaho system,
    // but that's not what we are aiming to test here
    service = new MonitoringService();
  }

  @Test
  public void testMonitoringServiceRegistration() {
    service.register( new MockMonitoringSubscriber() );
  }

  @Test
  public void testMonitoringServiceEventPosting() {
    service.post( new MockMonitoringEvent() );
  }

  @After
  public void teardown() {
    service = null;
  }

  private class MockMonitoringSubscriber implements IMonitoringSubscriber {

    @Override public String getSubscriberId() {
      return getClass().getName();
    }

    @Subscribe public void handleEvent( MockMonitoringEvent o ) {
      /* do nothing */
    }
  }

  private class MockMonitoringEvent implements IMonitoringEvent {

    @Override public Serializable getId() {
      return getClass().getName();
    }
  }
}
