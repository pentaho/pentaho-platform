/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.JavaScriptObject;

public class JsLocalizedRoleName extends JavaScriptObject {

  protected JsLocalizedRoleName() {

  }

  public final native String getLocalizedName() /*-{ return this.localizedName; }-*/; //

  public final native String getRoleName() /*-{ return this.roleName; }-*/; //
}
