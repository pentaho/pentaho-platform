package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.JavaScriptObject;

public class JsLocalizedRoleName extends JavaScriptObject {

  protected JsLocalizedRoleName() {
    
  }
  public final native String getLocalizedName() /*-{ return this.localizedName; }-*/; //
  public final native String getRoleName() /*-{ return this.roleName; }-*/; //
}
