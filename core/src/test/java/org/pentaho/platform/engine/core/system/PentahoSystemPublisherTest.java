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

package org.pentaho.platform.engine.core.system;

import org.junit.Assert;
import org.junit.Test;

public class PentahoSystemPublisherTest {
  @Test
  public void subscribeTest() {
    PentahoSystemPublisher.getInstance().subscribe( PentahoSystemPublisher.START_UP_TOPIC, this::startupSubscriber );
    PentahoSystemPublisher.getInstance().subscribe( PentahoSystemPublisher.SHUT_DOWN_TOPIC, this::shutdownSubscriber );
    Assert.assertEquals( 2,  PentahoSystemPublisher.getInstance().topicCount() );
    PentahoSystemPublisher.getInstance().publish( PentahoSystemPublisher.START_UP_TOPIC, true );
    PentahoSystemPublisher.getInstance().publish( PentahoSystemPublisher.SHUT_DOWN_TOPIC, true );
  }

  private void shutdownSubscriber( boolean isStopping ) {
    Assert.assertTrue( isStopping );
  }

  public void startupSubscriber( boolean isStarting ) {
    Assert.assertTrue( isStarting );
  }
}
