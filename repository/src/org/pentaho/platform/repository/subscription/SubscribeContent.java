/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * 
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Oct 7, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.repository.subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.api.repository.ISubscribeContent;

/**
 * This class defines an action sequence (e.g. a report) that users can create subscriptions to. This class contains a reference
 * to the action sequence that will be executed when the subscription is viewed online or scheduled. Parameters to the action
 * sequence can be defined here. These parameters will be applied to every subscription to the action sequence. These parameter
 * values can be overriden by the individual subscriptions if the user interface allows.
 * 
 * @author James Dixon
 * 
 */
public class SubscribeContent implements ISubscribeContent {
  public static final String TYPE_REPORT = "report"; //$NON-NLS-1$

  public static final String TYPE_VIEW = "view"; //$NON-NLS-1$

  public static final String TYPE_DASHBOARD = "dashboard"; //$NON-NLS-1$

  private int revision = -1; // Hibernate Revision

  private String id;

  private String actionReference;

  private Map<String,Object> parameters;

  private String type;

  private List<ISchedule> schedules;

  protected SubscribeContent() {
    // Needed for Hibernate to construct and re-load.
  }
// TODO sbarkdull, one of these ctors should be implemented in terms of the other, or in terms of an init() method
  public SubscribeContent(final String subContId, final String actionReference, final String type) {
    this.actionReference = actionReference;
    this.id = subContId;
    this.type = type;
    parameters = new HashMap<String,Object>();
    schedules = new ArrayList<ISchedule>();
  }

  public SubscribeContent(final String subContId, final String actionReference, final String type, final Map<String,Object> parameters) {
    this.actionReference = actionReference;
    this.id = subContId;
    this.type = type;
    this.parameters = parameters;
    schedules = new ArrayList<ISchedule>();
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof SubscribeContent)) {
      return false;
    }
    final SubscribeContent that = (SubscribeContent) other;
    return this.getId().equals(that.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  /**
   * @return Returns the revision.
   */
  public int getRevision() {
    return revision;
  }

  /**
   * @param revision
   *            The revision to set. This is set by Hibernate.
   */
  protected void setRevision(final int revision) {
    this.revision = revision;
  }

  /**
   * Gets the reference to the action sequence used by subscriptions to this content
   * 
   * @return Action sequence reference
   */
  public String getActionReference() {
    return actionReference;
  }

  public void addSchedule(final ISchedule schedule) {
    schedules.add(schedule);
  }

  public List<ISchedule> getSchedules() {
    return schedules;
  }

  public boolean hasSchedule(final ISchedule schedule) {
    return (schedules.contains(schedule));
  }

  public boolean removeSchedule(final ISchedule schedule) {
    return (schedules.remove(schedule));
  }
  
  public void clearsSchedules() {
    schedules.clear();
  }

  /**
   * Sets the parameters that will be provided to the action sequence when it is executed as part of a subscription
   * 
   * @param parameters
   *            The parameters to pass to the action sequence
   */
  public void setParameters(final Map<String,Object> parameters) {
    this.parameters = parameters;
  }

  /**
   * Gets the parameters for the content
   * 
   * @return Parameter HashMap
   */
  public Map<String,Object> getParameters() {
    return parameters;
  }

  /**
   * Gets the id of this content
   * 
   * @return
   */
  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  protected void setId(final String value) {
    this.id = value;
  }

  /**
   * Sets a reference to the action sequence that will be executed as part of a subscription
   * 
   * @param actionReference
   */
  public void setActionReference(final String actionReference) {
    this.actionReference = actionReference;
  }

  public void setSchedules(final List<ISchedule> schedules) {
    this.schedules = schedules;
  }

  public void setType(final String type) {
    this.type = type;
  }

}
