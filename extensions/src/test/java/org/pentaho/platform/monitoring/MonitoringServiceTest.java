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
