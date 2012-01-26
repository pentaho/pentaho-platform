package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.JavaScriptObject;

public class JsSysAdminPanel extends JavaScriptObject implements ISysAdminPanel {

  protected JsSysAdminPanel() {
    
  }
    
  public final native String getId() /*-{
    return this.id;
  }-*/;
  
  public final native boolean passivate() /*-{
    return this.passivate();
  }-*/;
  
  public final native void activate() /*-{
    this.activate();
  }-*/;
}
