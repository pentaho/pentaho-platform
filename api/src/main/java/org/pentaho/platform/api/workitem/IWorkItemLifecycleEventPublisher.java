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


package org.pentaho.platform.api.workitem;

import java.util.List;

/**
 * Responsible for publishing {@link IWorkItemLifecycleEvent}s.
 */
public interface IWorkItemLifecycleEventPublisher {

  /**
   * Publishes {@link IWorkItemLifecycleEvent}s to registered {@link IWorkItemLifecycleEventSubscriber}s
   * @param event the {@link IWorkItemLifecycleEvent} being published
   */
  void publish( final IWorkItemLifecycleEvent event );

  /**
   * Sets the {@link List} of {@link IWorkItemLifecycleEventSubscriber}s that wish to listen for
   * {@link IWorkItemLifecycleEvent}s.
   * @param subscribers the {@link List} of {@link IWorkItemLifecycleEventSubscriber}s that wish to listen for
   * {@link IWorkItemLifecycleEvent}s.
   */
  void setSubscribers( final List<IWorkItemLifecycleEventSubscriber> subscribers );

  /**
   * Returns the {@link List} of {@link IWorkItemLifecycleEventSubscriber}s that wish to listen for {@link
   * IWorkItemLifecycleEvent}s.
   *
   * @return the {@link List} of {@link IWorkItemLifecycleEventSubscriber}s that wish to listen for {@link
   * IWorkItemLifecycleEvent}s.
   */
  List<IWorkItemLifecycleEventSubscriber> getSubscribers();
}
