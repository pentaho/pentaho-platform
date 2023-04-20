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
 * Copyright (c) 2023 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.mantle.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.UIObject;

import java.util.List;

public class BurgerMenuBar extends MenuBar {
  public BurgerMenuBar() {
    super( true );

    // Changing the primary style name will result in
    // Menu Popup Panels with an additional class of pen-BurgerMenuBarPopup (beyond the gwt-MenuBarPopup)
    setStylePrimaryName( "pen-BurgerMenuBar" );

    // Recover base style names to allow sharing the CSS.
    addStyleName( "gwt-MenuBar" );
    addStyleName( "gwt-MenuBar-vertical" );
  }

  @Override
  public void onBrowserEvent( Event event ) {
    // Overriding mouse over and out, and prevent calling the base, private itemOver method,
    // is the only way to prevent hovering-over from automatically open a sub-menu on non-top-level menus...
    switch ( DOM.eventGetType( event ) ) {
      case Event.ONMOUSEOVER:
        onItemOver( findItem( DOM.eventGetTarget( event ) ), true );
        return;

      case Event.ONMOUSEOUT:
        onItemOver( findItem( DOM.eventGetTarget( event ) ), false );
        return;
    }

    super.onBrowserEvent( event );
  }

  private void onItemOver( MenuItem item, boolean isOver ) {
    // Still need to select and unselect items
    if ( item != null && item.isEnabled() ) {
      selectItem( isOver ? item : null );
    }
  }

  private MenuItem findItem( com.google.gwt.dom.client.Element hItem ) {
    for ( MenuItem item : getItems() ) {
      if ( item.getElement().isOrHasChild( hItem ) ) {
        return item;
      }
    }

    return null;
  }

  public List<UIObject> getAllItems() {
    return getMenuBarAllItems( this );
  }

  @Override
  public List<MenuItem> getItems() {
    return super.getItems();
  }

  public static native List<UIObject> getMenuBarAllItems( MenuBar menuBar ) /*-{
    return menuBar.@com.google.gwt.user.client.ui.MenuBar::allItems;
  }-*/;

  public static native List<MenuItem> getMenuBarItems( MenuBar menuBar ) /*-{
    return menuBar.@com.google.gwt.user.client.ui.MenuBar::items;
  }-*/;
}
