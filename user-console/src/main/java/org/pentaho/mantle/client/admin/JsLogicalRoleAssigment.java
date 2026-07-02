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


package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class JsLogicalRoleAssigment extends JavaScriptObject {

  protected JsLogicalRoleAssigment() {

  }

  public final native JsArrayString getAssignedLogicalRoles() /*-{ return this.logicalRoles; }-*/; //

  public final native String getRoleName() /*-{ return this.roleName; }-*/; //

  public final native boolean isImmutable() /*-{ return this.immutable != undefined
      && this.immutable == "true"; }-*/; //
}
