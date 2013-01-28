package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsLogicalRoleMap extends JavaScriptObject {
  
  protected JsLogicalRoleMap() {
    
  }
  public final native JsArray<JsLocalizedRoleName> getLogicalRoles() /*-{ return this.localizedRoleNames; }-*/; //
  public final native JsArray<JsLogicalRoleAssigment> getRoleAssignments() /*-{ return this.logicalRoleAssignments; }-*/; //
}
