/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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

}
