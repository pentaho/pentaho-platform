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


package org.pentaho.platform.monitoring.subscribers;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.pentaho.platform.api.monitoring.IMonitoringSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * guava's elegant Catch-All-That-Fell-Thru-Cracks
 * <p/>
 * 'dead-letter' queue in the conventional message queuing systems for intercepting messages that failed to be delivered
 * to any known subscriber
 */
public class MonitoringDeadEventSubscriber implements IMonitoringSubscriber {

  private Logger logger = LoggerFactory.getLogger( MonitoringDeadEventSubscriber.class );

  @Override
  public String getSubscriberId() {
    return this.getClass().getName();
  }

  @Subscribe
  public void handleDeadEvent( DeadEvent e ) {
    logger.error( e != null ? ToStringBuilder.reflectionToString( e.getEvent() ) : "null object" );
  }
}
