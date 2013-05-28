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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.mantle.client.dialogs.scheduling;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class JsSchedulingParameter extends JavaScriptObject implements ISchedulingParameter {

  protected JsSchedulingParameter() {
  }
  
  public final native String getName() /*-{
    return this.name;
  }-*/;

  public final native JsArrayString getStringValue() /*-{
    return this.stringValue;
  }-*/;

  public final native void setName(String name) /*-{
    this.name = name;
  }-*/;

  public final native void setStringValue(JsArrayString value) /*-{
    return this.stringValue = value;
  }-*/;

  public final native String getType() /*-{
    return this.type;
  }-*/;

  public final native void setType(String type) /*-{
    this.type = type;
  }-*/;
}
