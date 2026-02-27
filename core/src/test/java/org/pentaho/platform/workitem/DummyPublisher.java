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


package org.pentaho.platform.workitem;

import org.pentaho.platform.api.workitem.IWorkItemLifecycleEvent;
import org.pentaho.platform.api.workitem.IWorkItemLifecycleEventPublisher;
import org.pentaho.platform.api.workitem.IWorkItemLifecycleEventSubscriber;

import java.util.List;

public class DummyPublisher implements IWorkItemLifecycleEventPublisher {

  public void publish( final IWorkItemLifecycleEvent event ) { }

  public void setSubscribers( final List<IWorkItemLifecycleEventSubscriber> subscribers ) { }

  public List<IWorkItemLifecycleEventSubscriber> getSubscribers() { return null; }
}
