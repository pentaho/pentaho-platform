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


package org.pentaho.mantle.client.usersettings;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsSetting extends JavaScriptObject {

  protected JsSetting() {
  }

  public final native String getName() /*-{ return this.name; }-*/; //

  public final native String getValue() /*-{ return this.value; }-*/; //

  public static final native JsArray<JsSetting> parseSettingsJson( String json )
  /*-{
    if(json == null || json === '') { return null; }
    var obj = JSON.parse(json);
    return obj != null ? obj.setting : obj;
  }-*/;

}
