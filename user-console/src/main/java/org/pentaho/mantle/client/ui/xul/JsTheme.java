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

public class JsTheme extends JavaScriptObject {

  // Overlay types always have protected, zero argument constructors.
  protected JsTheme() {
  }

  public final native String getId() /*-{ return this.id; }-*/; //

  public final native String getName() /*-{ return this.name; }-*/; //

  public static final native JsArray<JsTheme> getThemes( String json )
  /*-{
    var obj = JSON.parse(json);

    // Sort themes alphabetically
    obj.theme = obj.theme.sort(function(a, b) {
      return (a.name > b.name) ? 1 : (a.name == b.name)? 0 : -1;
    });

    return obj.theme;
  }-*/;

}
