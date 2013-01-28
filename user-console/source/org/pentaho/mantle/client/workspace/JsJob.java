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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 * 
 */
package org.pentaho.mantle.client.workspace;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class JsJob extends JavaScriptObject {

  // Overlay types always have protected, zero argument constructors.
  protected JsJob() {
  }

  // JSNI methods to get job data.
  public final native String getJobId() /*-{ return this.jobId; }-*/; //

  public final native String getJobName() /*-{ return this.jobName; }-*/; //

  public final native String getUserName() /*-{ return this.userName; }-*/; //

  private final native String getNativeNextRun() /*-{ return this.nextRun; }-*/; //

  private final native String getNativeLastRun() /*-{ return this.lastRun; }-*/; //
  
  public final native JsArray<JsJobParam> getJobParams() /*-{ return this.jobParams.jobParams; }-*/; //

  public final native JsJobTrigger getJobTrigger() /*-{ return this.jobTrigger; }-*/; //
  
  public final native String getState() /*-{ return this.state; }-*/; //

  public final native void setState(String newState) /*-{ this.state = newState; }-*/; //
  
  public final String getJobParam(String name) {
    JsArray<JsJobParam> params = getJobParams();
    for (int i=0;i<params.length();i++) {
      JsJobParam param = params.get(i);
      if (param.getName().equals(name)) {
        return param.getValue();
      }
    }
    return null;
  }
  
  public final boolean hasResourceName() {
    String resource = getJobParam("ActionAdapterQuartzJob-StreamProvider");
    return (resource != null && !"".equals(resource));
  }  
  
  public final String getFullResourceName() {
    String resource = getJobParam("ActionAdapterQuartzJob-StreamProvider");
    if (resource == null || "".equals(resource)) {
      return getJobName();
    }
    resource = resource.substring(resource.indexOf("/"), resource.indexOf(":"));
    return resource;
  }
  
  public final String getShortResourceName() {
    String resource = getFullResourceName();
    if (resource.indexOf("/") != -1) {
      resource = resource.substring(resource.lastIndexOf("/") + 1);
    }
    return resource;
  }  
  
  public final Date getLastRun() {
    return formatDate(getNativeLastRun());
  }

  public final Date getNextRun() {
    return formatDate(getNativeNextRun());
  }

  public static Date formatDate(String dateStr) {
    try {
      DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
      return format.parse(dateStr);
    } catch (Throwable t) {
    }

    try {
      DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ssZZZ");
      return format.parse(dateStr);
    } catch (Throwable t) {
    }

    return null;
  }

}