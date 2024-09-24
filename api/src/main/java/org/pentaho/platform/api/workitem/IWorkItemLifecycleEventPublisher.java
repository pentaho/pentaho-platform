/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
