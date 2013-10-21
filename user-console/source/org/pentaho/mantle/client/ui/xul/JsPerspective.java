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

package org.pentaho.mantle.client.ui.xul;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public class JsPerspective extends JavaScriptObject {

  // Overlay types always have protected, zero argument constructors.
  protected JsPerspective() {
  }

  public final native String getId() /*-{ return this.id; }-*/; //

  public final native String getTitle() /*-{ return this.title; }-*/; //

  public final native String getResourceBundleUri() /*-{ return this.resourceBundleUri; }-*/; //

  public final native String getContentUrl() /*-{ return this.contentUrl; }-*/; //

  public final native String getLayoutPriority() /*-{ return this.layoutPriority; }-*/; //

  public final native JsArray<JsXulOverlay> getOverlays() /*-{ return this.overlays; }-*/; //

  public final native JsArrayString getRequiredSecurityActions() /*-{ return this.requiredSecurityActions; }-*/; //

  public static final native JsArray<JsPerspective> parseJson( String json )
  /*-{
    var obj = eval('(' + json + ')');
    return obj.pluginPerspective;
  }-*/;

}
