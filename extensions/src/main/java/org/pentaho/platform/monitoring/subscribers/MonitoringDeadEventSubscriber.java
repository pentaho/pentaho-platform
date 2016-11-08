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
