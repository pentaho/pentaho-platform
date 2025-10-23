/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
    var obj = JSON.parse(json);
    return obj.pluginPerspective;
  }-*/;

}
