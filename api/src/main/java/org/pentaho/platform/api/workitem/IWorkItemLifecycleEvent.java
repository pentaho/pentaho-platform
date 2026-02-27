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

import java.util.Date;

/**
 * Encapsulates all information pertaining to a "work item" at a specific point in its lifecycle.
 */
public interface IWorkItemLifecycleEvent {

  /**
   * Returns the unique id of a work item.
   *
   * @return the unique id of a work item
   */
  String getWorkItemUid();

  /**
   * Returns the work item details.
   *
   * @return the work item details
   */
  String getWorkItemDetails();

  /**
   * Returns the current {@link IWorkItemLifecyclePhase} for the work item.
   *
   * @return the current {@link IWorkItemLifecyclePhase} for the work item.
   */
  IWorkItemLifecyclePhase getWorkItemLifecyclePhase();

  /**
   * Returns details related to the current {@link IWorkItemLifecyclePhase}
   *
   * @return details related to the current {@link IWorkItemLifecyclePhase}
   */
  String getLifecycleDetails();

  /**
   * Returns the {@link Date} when the event occured.
   *
   * @return the {@link Date} when the event occured.
   */
  Date getSourceTimestamp();

  /**
   * Returns the hostname of the host where the event originated.
   *
   * @return the hostname of the host where the event originated.
   */
  String getSourceHostName();

  /**
   * Returns the ip of the host where the event originated.
   *
   * @return the ip of the host where the event originated.
   */
  String getSourceHostIp();

}
