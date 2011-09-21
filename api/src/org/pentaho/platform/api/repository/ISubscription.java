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

import org.dom4j.Document;

public interface ISubscription {

  public boolean equals(Object other);

  public int hashCode();

  /**
   * @return Returns the revision.
   */
  public int getRevision();

  public void addSchedule(ISchedule sched);
  
  public boolean deleteSchedule(final ISchedule sched);
  
  public List<ISchedule> getSchedules();

  public String getUser();

  public String getTitle();

  public String getDestination();

  public ISubscribeContent getContent();

  public Map<String,Object> getParameters();

  public String getId();

  public int getType();

  public Document asDocument();

  public String asXml();

  public void setDestination(String destination);

  public void setTitle(String title);

  public Object[] toResultRow(String parameterNames[]);

}
