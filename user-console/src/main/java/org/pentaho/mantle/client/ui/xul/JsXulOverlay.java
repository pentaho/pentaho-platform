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

public class JsXulOverlay extends JavaScriptObject {

  // Overlay types always have protected, zero argument constructors.
  protected JsXulOverlay() {
  }

  public final native String getId() /*-{ return this.id; }-*/; //

  public final native String getOverlayUri() /*-{ return this.source; }-*/; //

  public final native String getOverlayXml() /*-{ return this.source; }-*/; //

  public final native String getResourceBundleUri() /*-{ return this.resourceBundleUri; }-*/; //

  public final native String getSource() /*-{ return this.source; }-*/; //

  public final native String getPriority() /*-{ return this.priority; }-*/; //

  public static final native JsArray<JsXulOverlay> parseJson( String json )
  /*-{
    var obj = JSON.parse(json);
    return obj.overlay;
  }-*/;

}
