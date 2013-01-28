package org.pentaho.mantle.client.usersettings;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsSetting extends JavaScriptObject {

  protected JsSetting() {
  }

  public final native String getName() /*-{ return this.name; }-*/; //

  public final native String getValue() /*-{ return this.value; }-*/; //

  public final static native JsArray<JsSetting> parseSettingsJson(String json)
  /*-{
    var obj = eval('(' + json + ')');
    return obj.setting;
  }-*/;
  
}
