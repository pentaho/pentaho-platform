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

/**
 * A common interface for components wishing to listen for {@link IWorkItemLifecycleEvent}s.
 */
public interface IWorkItemLifecycleEventSubscriber {

  /**
   * Handler for the {@link IWorkItemLifecycleEvent}.
   *
   * @param event the {@link IWorkItemLifecycleEvent} being handled
   */
  void handleEvent( final IWorkItemLifecycleEvent event );
}
