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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.ui.custom;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Class that wraps list box into div necessary for horizontal scroll effect
 */
public class HorizontalScrollWrapper extends Composite {

  private String id;

  public HorizontalScrollWrapper( ListBox listBox ) {
    id = getListBoxWrapperUIId( listBox );
    String html = ""
      + "<div id='" + id + "' style=\"overflow-x:scroll; width:200px; overflow: -moz-scrollbars-horizontal;\">"
      + "<div id=\"listBox\"></div>"
      + "<div style='font-size: 1px'>&nbsp</div>\n"
      + "</div>";
    HTMLPanel listWrapper = new HTMLPanel( html );
    listWrapper.addAndReplaceElement( listBox, "listBox" );
    initWidget( listWrapper );
  }

  @Override
  public void onLoad() {
    super.onLoad();
    prepareHorizontalScroll( id );
  }

  /**
   * Get html div id
   */
  public static String getListBoxWrapperUIId( ListBox listBox ) {
    return "horizontalScroll-" + listBox.hashCode();
  }

  /**
   * Sets width for select and helper div to enable scrolling
   */
  private static native void prepareHorizontalScroll( String id ) /*-{
      $wnd.prepareHorizontalScroll(id);
  }-*/;
}
