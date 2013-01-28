package org.pentaho.mantle.client.dialogs;

import com.google.gwt.core.client.JavaScriptObject;

public class JsCube extends JavaScriptObject {

  protected JsCube() {
  }

  public final native String getId() /*-{ return this.id; }-*/; //

  public final native String getName() /*-{ return this.name; }-*/; //

  public final native String getCatName() /*-{ return this.catName; }-*/; //

}
