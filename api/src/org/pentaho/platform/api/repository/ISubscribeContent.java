/*
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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Oct 7, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.api.repository;

import java.util.List;
import java.util.Map;

/**
 * This class defines an action sequence (e.g. a report) that users can create subscriptions to. This class contains a reference
 * to the action sequence that will be executed when the subscription is viewed online or scheduled. Parameters to the action
 * sequence can be defined here. These parameters will be applied to every subscription to the action sequence. These parameter
 * values can be overriden by the individual subscriptions if the user interface allows.
 * 
 * @author James Dixon
 * 
 */
public interface ISubscribeContent {

  public boolean equals(Object other);

  public int hashCode();

  /**
   * @return Returns the revision.
   */
  public int getRevision();

  /**
   * Gets the reference to the action sequence used by subscriptions to this content
   * 
   * @return Action sequence reference
   */
  public String getActionReference();

  public void addSchedule(ISchedule schedule);

  public List<ISchedule> getSchedules();

  public boolean hasSchedule(ISchedule schedule);

  public boolean removeSchedule(ISchedule schedule);
  
  /**
   * removes all schedules.
   */
  public void clearsSchedules();

  /**
   * Sets the parameters that will be provided to the action sequence when it is executed as part of a subscription
   * 
   * @param parameters
   *            The parameters to pass to the action sequence
   */
  public void setParameters(Map<String,Object> parameters);

  /**
   * Gets the parameters for the content
   * 
   * @return Parameter HashMap
   */
  public Map<String,Object> getParameters();

  /**
   * Gets the id of this content
   * 
   * @return
   */
  public String getId();

  public String getType();

  public void setType(String contentType);

  public void setActionReference(String actionReference);

  public void setSchedules(List<ISchedule> schedules);

}
