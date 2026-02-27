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


package org.pentaho.platform.api.monitoring;


public interface IMonitoringService {

  /**
   * register a subscriber ( a.k.a. event handler ) in event bus
   *
   * @param subscriber IMonitoringSubscriber event handler
   */
  <T extends IMonitoringSubscriber> void register( T subscriber );


  /**
   * unregister a subscriber ( a.k.a. event handler ) from event bus
   *
   * @param subscriber IMonitoringSubscriber event handler
   */
  <T extends IMonitoringSubscriber> void unregister( T subscriber );


  /**
   * all subscribers of this event type will be triggered
   *
   * @param event IMonitoringEvent event object
   */
  <T extends IMonitoringEvent> void post( T event );

}
