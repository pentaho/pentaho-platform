/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.workspace;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

/**
 * Wraps the JSON response for a blockoutStatus REST response. We get back a string that represents something like:
 * {"partiallyBlocked":"false", "totallyBlocked":"false"}
 */
public class JsBlockStatus extends JavaScriptObject {

  // Overlay types always have protected, zero argument constructors.
  protected JsBlockStatus() {
  }

  // JSNI methods to get job data.
  public final native String getPartiallyBlocked() /*-{ return this.partiallyBlocked; }-*/; //

  public final native String getTotallyBlocked() /*-{ return this.totallyBlocked; }-*/; //

  public final String getJSONString() {
    return new JSONObject( this ).toString();
  }
}
