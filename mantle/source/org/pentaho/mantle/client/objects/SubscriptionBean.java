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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.objects;

import java.io.Serializable;
import java.util.ArrayList;

public class SubscriptionBean implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 3279051936233043771L;
  private String id;
  private String name;
  private String xactionName;
  private String scheduleDate;
  private String size;
  private String type;
  private String pluginUrl;
  private ArrayList<String[]> content;

  public SubscriptionBean() {

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getXactionName() {
    return xactionName;
  }

  public void setXactionName(String xactionName) {
    this.xactionName = xactionName;
  }

  public String getScheduleDate() {
    return scheduleDate;
  }

  public void setScheduleDate(String scheduleDate) {
    this.scheduleDate = scheduleDate;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public ArrayList<String[]> getContent() {
    return content;
  }

  public void setContent(ArrayList<String[]> content) {
    this.content = content;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPluginUrl() {
    return pluginUrl;
  }

  public void setPluginUrl(String pluginUrl) {
    this.pluginUrl = pluginUrl;
  }
}
