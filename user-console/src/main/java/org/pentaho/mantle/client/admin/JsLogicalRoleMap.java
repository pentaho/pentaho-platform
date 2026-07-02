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
import com.google.gwt.core.client.JsArray;

public class JsLogicalRoleMap extends JavaScriptObject {

  protected JsLogicalRoleMap() {

  }

  public final native JsArray<JsLocalizedRoleName> getLogicalRoles() /*-{ return this.localizedRoleNames; }-*/; //

  public final native JsArray<JsLogicalRoleAssigment> getRoleAssignments() /*-{ return this.assignments; }-*/; //
}
