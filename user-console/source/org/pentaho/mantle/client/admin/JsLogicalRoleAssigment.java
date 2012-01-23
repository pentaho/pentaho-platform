package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;


public class JsLogicalRoleAssigment extends JavaScriptObject {

  protected JsLogicalRoleAssigment() {
    
  }
  public final native JsArrayString getAssignedLogicalRoles() /*-{ return this.logicalRoles; }-*/; //
  public final native String getRoleName() /*-{ return this.roleName; }-*/; //
}
