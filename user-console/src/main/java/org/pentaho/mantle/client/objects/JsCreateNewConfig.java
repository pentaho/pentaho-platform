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


package org.pentaho.mantle.client.objects;

import com.google.gwt.core.client.JavaScriptObject;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;

public class JsCreateNewConfig extends JavaScriptObject {

  protected JsCreateNewConfig() {
  }

  public final native String getName() /*-{ return this.name; }-*/; //

  public final native String getValue() /*-{ return this.value; }-*/; //

  public final int getPriority() {
    String value = getValue();
    StringTokenizer st = new StringTokenizer( value, ',' );
    return Integer.parseInt( st.tokenAt( 0 ).trim() );
  }

  public final String getLabel() {
    String value = getValue();
    StringTokenizer st = new StringTokenizer( value, ',' );
    return st.tokenAt( 1 ).trim();
  }

  public final String getTabName() {
    String value = getValue();
    StringTokenizer st = new StringTokenizer( value, ',' );
    return st.tokenAt( 2 ).trim();
  }

  public final String getActionUrl() {
    String value = getValue();
    StringTokenizer st = new StringTokenizer( value, ',' );
    return st.tokenAt( 3 ).trim();
  }

  public final String getEnabledUrl() {
    String value = getValue();
    StringTokenizer st = new StringTokenizer( value, ',' );
    if ( st.countTokens() < 5 ) {
      return null;
    } else {
      return st.tokenAt( 4 ).trim();
    }
  }

}
