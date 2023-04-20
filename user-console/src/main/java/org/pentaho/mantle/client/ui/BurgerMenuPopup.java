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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;

public class BurgerMenuPopup extends DecoratedPopupPanel {
  private Widget menuButton;

  public BurgerMenuPopup( Widget menuButton, BurgerMenuBar menuBar ) {
    super( true );

    // Imitate BurgerMenuBar's Popup Panel classes
    setStyleName( "gwt-MenuBarPopup" );
    addStyleName( "pen-BurgerMenuBarPopup" );

    this.menuButton = menuButton;

    setWidget( menuBar );

    // Avoid inheriting CSS styling for .mainMenubar.
    menuBar.getElement().setId( "burgerMenubar" );

    overrideLeafMenuItemCommands( menuBar );

    menuButton.getElement().setAttribute( "aria-controls", menuBar.getElement().getId() );
    menuButton.getElement().setAttribute( "aria-expanded", "true" );

    addCloseHandler( ev -> {
      menuButton.getElement().removeAttribute( "aria-controls" );
      menuButton.getElement().removeAttribute( "aria-expanded" );

      if ( menuButton instanceof Focusable ) {
        ( (Focusable) menuButton ).setFocus( true );
      }
    } );
  }

  @Override
  public boolean onKeyDownPreview( char key, int modifiers ) {
    switch ( key ) {
      case KeyCodes.KEY_ESCAPE:
      case KeyCodes.KEY_TAB: {
        this.hide();
        // Suppress the event.
        return false;
      }
    }

    return super.onKeyDownPreview( key, modifiers );
  }

  public void showMenu() {
    setPopupPositionAndShow( ( offsetWidth, offsetHeight ) -> {
      // Place below the button.
      setPopupPosition(
        menuButton.getAbsoluteLeft(),
        menuButton.getAbsoluteTop() + menuButton.getOffsetHeight() );
    } );

    ( (BurgerMenuBar) this.getWidget() ).focus();
  }

  private void overrideLeafMenuItemCommands( BurgerMenuBar menuBar ) {

    // TODO: Must not affect the special back buttons!

    for ( MenuItem menuItem : menuBar.getItems() ) {
      BurgerMenuBar subMenu = (BurgerMenuBar) menuItem.getSubMenu();
      if ( subMenu != null ) {
        overrideLeafMenuItemCommands( subMenu );
      } else {
        Scheduler.ScheduledCommand command = menuItem.getScheduledCommand();
        if ( command != null ) {
          menuItem.setScheduledCommand( () -> {

            BurgerMenuPopup.this.hide();

            command.execute();
          } );
        }
      }
    }
  }
}
