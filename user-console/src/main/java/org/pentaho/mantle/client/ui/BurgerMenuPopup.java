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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Widget;

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

    menuButton.getElement().setAttribute( "aria-controls", menuBar.getElement().getId() );
    menuButton.getElement().setAttribute( "aria-expanded", "true" );

    // Mouse-down'ing on the button should not trigger the default autoHide behavior, of hiding the PopupPanel.
    // This would hide the popup, however, the click event on the button would then immediately re-open the menu popup.
    // Instead, the dialog is not closed when clicking on the button and then it is hidden instead in the
    // click preview event, in which the event is also canceled, to prevent the default action of opening the popup...
    addAutoHidePartner( menuButton.getElement() );

    addCloseHandler( ev -> {
      menuButton.getElement().removeAttribute( "aria-controls" );
      menuButton.getElement().removeAttribute( "aria-expanded" );

      if ( menuButton instanceof Focusable ) {
        ( (Focusable) menuButton ).setFocus( true );
      }
    } );
  }

  @Override
  protected void onPreviewNativeEvent( Event.NativePreviewEvent event ) {
    switch ( event.getTypeInt() ) {
      case Event.ONKEYDOWN: {
        switch ( event.getNativeEvent().getKeyCode() ) {
          // Must handle ESCAPE for the top-level menu.
          case KeyCodes.KEY_ESCAPE:
          case KeyCodes.KEY_TAB: {
            hide();
            event.cancel();
            return;
          }
          default:
        }
        break;
      }

      case Event.ONCLICK: {
        // See comment above on constructor, near addAutoHidePartner.
        if ( !eventTargetsPopupLocal( event.getNativeEvent() ) ) {
          hide();
          event.cancel();
        }
        break;
      }
      default:
    }

    super.onPreviewNativeEvent( event );
  }

  public void showMenu() {
    setPopupPositionAndShow( ( offsetWidth, offsetHeight ) ->
      // Place below the button.
      setPopupPosition(
        menuButton.getAbsoluteLeft(),
        menuButton.getAbsoluteTop() + menuButton.getOffsetHeight() )
    );

    ( (BurgerMenuBar) this.getWidget() ).focus();
  }

  // Local version of private super.eventTargetsPopup( NativeEvent ).
  protected boolean eventTargetsPopupLocal( NativeEvent event ) {
    EventTarget target = event.getEventTarget();
    if ( Element.is( target ) ) {
      return getElement().isOrHasChild( Element.as( target ) );
    }

    return false;
  }
}
