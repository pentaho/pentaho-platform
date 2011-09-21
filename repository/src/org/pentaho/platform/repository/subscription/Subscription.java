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

import org.dom4j.Document;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.platform.api.repository.ISchedule;
import org.pentaho.platform.api.repository.ISubscribeContent;
import org.pentaho.platform.api.repository.ISubscription;
import org.pentaho.platform.engine.core.solution.ActionInfo;

public class Subscription implements ISubscription {

  public static final int TYPE_PERSONAL = 1;

  public static final int TYPE_ROLE = 2;

  public static final int TYPE_GROUP = 3;

  public static final int COLUMN_USER = 0;

  public static final int COLUMN_ID = 1;

  public static final int COLUMN_DESTINATION = 2;

  public static final int COLUMN_CONTENT_ID = 3;

  public static final int COLUMN_TITLE = 4;

  public static final int COLUMN_SOLUTION = 5;

  public static final int COLUMN_PATH = 6;

  public static final int COLUMN_ACTION = 7;

  private static final String baseHeaders[] = {
      "user", "id", "destination", "contentid", "title", "solution", "path", "action" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ 

  private String id;

  private int type;

  private String user;

  private String title;

  private ISubscribeContent content;

  private Map<String, Object> parameters;

  private List<ISchedule> schedules;

  private String destination;

  private int revision = -1; // Hibernate Revision

  public static MemoryMetaData getMetadata(final String parameterNames[]) {
    Object columnHeaders[][] = new Object[1][];
    String headerNames[] = new String[Subscription.baseHeaders.length + parameterNames.length];
    for (int i = 0; i < Subscription.baseHeaders.length; i++) {
      headerNames[i] = Subscription.baseHeaders[i];
    }
    int offset = Subscription.baseHeaders.length;
    for (int i = 0; i < parameterNames.length; i++) {
      headerNames[offset + i] = parameterNames[i];
    }
    columnHeaders[0] = headerNames;
    return new MemoryMetaData(columnHeaders, null);
  }

  protected Subscription() {
    // Needed for Hibernate to instantiate and set properties.
  }

  public Subscription(final String subscriptionId, final String user, final String title,
      final ISubscribeContent content, final String destination, final int type) {
    this( subscriptionId, user, title, content, destination, type, new HashMap<String,Object>() );
  }

  public Subscription(final String subscriptionId, final String user, final String title,
      final ISubscribeContent content, final String destination, final int type, final Map<String,Object> parameters) {
    this.user = user;
    this.title = title;
    this.content = content;
    this.type = type;
    this.parameters = parameters;
    this.destination = destination;
    schedules = new ArrayList<ISchedule>();
    id = subscriptionId;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Subscription)) {
      return false;
    }
    final Subscription that = (Subscription) other;
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
   *            The revision to set. This is set by hibernate.
   */
  protected void setRevision(final int revision) {
    this.revision = revision;
  }

  public void addSchedule(final ISchedule sched) {
    schedules.add(sched);
  }

  public boolean deleteSchedule(final ISchedule sched) {
    return schedules.remove( sched );
  }
  
  public List<ISchedule> getSchedules() {
    return schedules;
  }

  public String getUser() {
    return user;
  }

  public String getTitle() {
    return title;
  }

  public String getDestination() {
    return destination;
  }

  public ISubscribeContent getContent() {
    return content;
  }

  public Map<String,Object> getParameters() {
    return parameters;
  }

  public String getId() {
    return id;
  }

  protected void setId(final String value) {
    id = value;
  }

  public int getType() {
    return type;
  }

  public Document asDocument() {
    return null;
  }

  public String asXml() {
    return null;
  }

  protected void setContent(final SubscribeContent content) {
    this.content = content;
  }

  public void setDestination(final String destination) {
    this.destination = destination;
  }

  protected void setParameters(final Map<String,Object> parameters) {
    this.parameters = parameters;
  }

  protected void setSchedules(final List<ISchedule> schedules) {
    this.schedules = schedules;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  protected void setType(final int type) {
    this.type = type;
  }

  protected void setUser(final String user) {
    this.user = user;
  }

  public Object[] toResultRow(final String parameterNames[]) {
    Object[] result = new Object[Subscription.baseHeaders.length + parameterNames.length];

    // expand the subscription into results
    result[Subscription.COLUMN_USER] = this.getUser();
    result[Subscription.COLUMN_ID] = this.getId();
    result[Subscription.COLUMN_DESTINATION] = this.getDestination();
    result[Subscription.COLUMN_CONTENT_ID] = this.getContent().getActionReference();
    ActionInfo contentInfo = ActionInfo.parseActionString(this.getContent().getActionReference());
    result[Subscription.COLUMN_TITLE] = this.getTitle();
    result[Subscription.COLUMN_SOLUTION] = contentInfo.getSolutionName();
    result[Subscription.COLUMN_PATH] = contentInfo.getPath();
    if (parameters != null) {
      result[Subscription.COLUMN_ACTION] = parameters.get("action"); //$NON-NLS-1$
      if (result[Subscription.COLUMN_ACTION] == null) {
        result[Subscription.COLUMN_ACTION] = contentInfo.getActionName();
      }
    } else {
      result[Subscription.COLUMN_ACTION] = contentInfo.getActionName();
    }

    int offset = Subscription.baseHeaders.length;
    for (int i = 0; i < parameterNames.length; i++) {
      result[offset + i] = parameters.get(parameterNames[i]);
    }

    return result;

  }

}
