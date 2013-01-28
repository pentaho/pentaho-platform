package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class JsUserRoleList extends JavaScriptObject {

  protected JsUserRoleList() {
    
  }
  
  public final native JsArrayString getRoles() /*-{ return this.roles; }-*/; //
}
