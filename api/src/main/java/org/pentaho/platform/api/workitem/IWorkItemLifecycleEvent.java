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
