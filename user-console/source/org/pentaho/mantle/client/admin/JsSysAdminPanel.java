package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class JsSysAdminPanel extends JavaScriptObject implements ISysAdminPanel {

  protected JsSysAdminPanel() {
    
  }
    
  public final native String getId() /*-{
    return this.id;
  }-*/;
  
  public final native void passivate(AsyncCallback<Boolean> callback) /*-{
    this.passivate($wnd.mantle_activateWaitingSecurityPanel);
  }-*/;
  
  public final native void activate() /*-{
    this.activate();
  }-*/;
}
