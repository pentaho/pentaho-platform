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
import com.google.gwt.user.client.rpc.AsyncCallback;

public class JsSysAdminPanel extends JavaScriptObject implements ISysAdminPanel {

  protected JsSysAdminPanel() {

  }
  public final native String getId() /*-{
    return this.id;
  }-*/;

  public final native void passivate( AsyncCallback<Boolean> callback ) /*-{
    this.passivate($wnd.mantle_activateWaitingSecurityPanel);
  }-*/;

  public final native void activate() /*-{
    this.activate();
  }-*/;
}
