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

import com.google.common.eventbus.DeadEvent;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.monitoring.subscribers.MonitoringDeadEventSubscriber;

public class MonitoringDeadEventSubscriberTest {


  @Test
  public void testGetSubscriberId() {
    Assert.assertNotNull( new MonitoringDeadEventSubscriber().getSubscriberId() );
  }

  @Test
  public void testHandleDeadEvent() {
    new MonitoringDeadEventSubscriber().handleDeadEvent( new DeadEvent( new String("mock"), new String("mock") ) );
  }
}
