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

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.pentaho.platform.api.monitoring.IMonitoringEvent;
import org.pentaho.platform.api.monitoring.IMonitoringService;
import org.pentaho.platform.api.monitoring.IMonitoringSubscriber;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;
import org.pentaho.platform.monitoring.subscribers.MonitoringDeadEventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.Executors;

/**
 * Wrapper for the AsyncEventBus class.
 * <p/>
 * The methods of AsyncEventBus use internal synchronization, so this singleton is thread-safe
 */
public class MonitoringService implements IMonitoringService {

  private Logger logger = LoggerFactory.getLogger( MonitoringService.class );

  private AsyncEventBus asyncEventBus; // asynchronous dispatching of events


  public MonitoringService() {

    asyncEventBus = new AsyncEventBus( Executors.newCachedThreadPool() );

    // register the bus with PentahoSystem
    PentahoSystem.registerReference(
      new SingletonPentahoObjectReference.Builder<EventBus>( EventBus.class ).object( asyncEventBus ).attributes(
        Collections.<String, Object>singletonMap( "id", "monitoring" ) ).build(), EventBus.class );

    // guava's elegant Catch-All-That-Fell-Thru-Cracks ( a.k.a 'DeadEvents' )
    MonitoringDeadEventSubscriber deadEventSubscriber = new MonitoringDeadEventSubscriber();
    getAsyncEventBus().register( deadEventSubscriber );

    //register this service in PentahoSystem
    registerMyself();
  }

  /**
   * register a subscriber ( a.k.a. event handler ) in event bus
   *
   * @param subscriber IMonitoringSubscriber event handler
   */
  @Override
  public synchronized <T extends IMonitoringSubscriber> void register( T subscriber ) {
    logger.debug( "registering subscriber " + ( subscriber != null ? subscriber.getSubscriberId() : "null" ) );
    getAsyncEventBus().register( subscriber );
  }

  /**
   * unregister a subscriber ( a.k.a. event handler ) from event bus
   *
   * @param subscriber IMonitoringSubscriber event handler
   */
  @Override
  public synchronized <T extends IMonitoringSubscriber> void unregister( T subscriber ) {
    logger.debug( "unregistering subscriber " + ( subscriber != null ? subscriber.getSubscriberId() : "null" ) );
    getAsyncEventBus().unregister( subscriber );
  }

  /**
   * all subscribers of this event type will be triggered
   *
   * @param event IMonitoringEvent event object
   */
  @Override
  public synchronized <T extends IMonitoringEvent> void post( T event ) {
    logger.debug( "posting event " + ( event != null ? event.getId() : "null" ) );
    getAsyncEventBus().post( event );
  }

  private AsyncEventBus getAsyncEventBus() {
    return asyncEventBus;
  }

  private void registerMyself() {

    if ( PentahoSystem.getInitializedOK() ) {
      PentahoSystem.registerObject( this );
    }

    if ( !PentahoSystem.getInitializedOK() || PentahoSystem.get( IMonitoringService.class ) == null ) {
      logger.error( "Unable to register Monitoring Event Bus Service in PentahoSystem" );
    }
  }

}