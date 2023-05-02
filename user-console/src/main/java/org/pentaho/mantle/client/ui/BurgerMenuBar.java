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
import com.google.gwt.safehtml.shared.annotations.IsSafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.FocusImpl;
import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuItem;
import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuSeparator;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.MenuBarUtils;

import java.util.List;

public class BurgerMenuBar extends MenuBar {

  public static class BurgerBackMenuItem extends PentahoMenuItem {
    public BurgerBackMenuItem( @IsSafeHtml String text, boolean asHTML ) {
      super( text, asHTML, null );

      addStyleName( "pen-BurgerBackMenuItem" );

      setScheduledCommand( this::goBack );
    }

    private void goBack() {
      ( (BurgerMenuBar) getParentMenu() ).closeLocal( true );
    }
  }

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
    MenuItem selectedItemBefore = null;
    int eventType = DOM.eventGetType( event );
    switch ( eventType ) {
      case Event.ONCLICK: {
        onItemClick( findItemLocal( DOM.eventGetTarget( event ) ) );
        return;
      }

      // Overriding mouse over and out, and prevent calling the base, private itemOver method,
      // is the only way to prevent hovering-over from automatically open a sub-menu on non-top-level menus...
      case Event.ONMOUSEOVER:
        onItemOver( findItemLocal( DOM.eventGetTarget( event ) ), true );
        return;

      case Event.ONMOUSEOUT:
        onItemOver( findItemLocal( DOM.eventGetTarget( event ) ), false );
        return;

      case Event.ONKEYDOWN:
        switch ( event.getKeyCode() ) {
          case KeyCodes.KEY_ESCAPE:
            // Override so that only this menubar's popup menu is closed,
            // and not all popups until (but except) the top-level one.
            onEscape();
            eatEventLocal( event );
            return;

          case KeyCodes.KEY_ENTER:
            // Override to also place focus on submenu, when there is one.
            onEnter();
            eatEventLocal( event );
            return;

          default:
            selectedItemBefore = getSelectedItem();
            break;
        }
        break;
    }

    super.onBrowserEvent( event );

    if ( eventType == Event.ONKEYDOWN ) {
      MenuItem selectedItemAfter = getSelectedItem();
      if ( selectedItemAfter != null && selectedItemAfter != selectedItemBefore ) {
        // Selected menu item changed when using the keyboard.
        ensureVisible( selectedItemAfter );
      }
    }
  }

  private void onItemClick( MenuItem item ) {
    focus();

    if ( item != null ) {
      doItemActionLocal( item, true, true );
    }
  }

  private void onItemOver( MenuItem item, boolean isOver ) {
    // Still need to select and unselect items.
    if ( item != null && item.isEnabled() ) {
      selectItem( isOver ? item : null );
      focus();
    }
  }

  protected void onEscape() {
    closeLocal( true );
  }

  protected void onEnter() {
    if ( !selectFirstItemIfNoneSelectedLocal() ) {
      doItemActionLocal( getSelectedItem(), true, true );
    }
  }

  protected void ensureVisible( MenuItem menuItem ) {
    ElementUtils.scrollVerticallyIntoView( menuItem.getElement() );
  }

  public List<UIObject> getAllItems() {
    return MenuBarUtils.getAllItems( this );
  }

  @Override
  public List<MenuItem> getItems() {
    return super.getItems();
  }

  // region Back Menu Items
  /**
   * Adds a "back" menu item to each descendant menu bar.
   * <p>
   *   Should be called on the root of the subtree, once it's fully built,
   *   or when there have been modifications to it.
   * </p>
   *
   * @see BurgerBackMenuItem
   */
  public void addBackItemToDescendantMenus() {
    addBackItems( null );
  }

  private void addBackItems( MenuItem parentMenuItem ) {
    if ( parentMenuItem != null && hasItemsButNoBackItem() ) {
      // Add a back menu item, followed by a menu item separator.
      insertSeparator( new PentahoMenuSeparator(), 0 );
      insertItem( new BurgerBackMenuItem( parentMenuItem.getHTML(), true ), 0 );
    }

    for ( MenuItem menuItem : getItems() ) {
      BurgerMenuBar subMenu = (BurgerMenuBar) menuItem.getSubMenu();
      if ( subMenu != null ) {
        subMenu.addBackItems( menuItem );
      }
    }
  }

  private boolean hasItemsButNoBackItem() {
    for ( MenuItem menuItem : getItems() ) {
      return !( menuItem instanceof BurgerBackMenuItem );
    }

    return false;
  }
  // endregion Back Menu Items

  protected void closeRoot() {
    PopupPanel menuPopup = getRootPopup();
    if ( menuPopup != null ) {
      menuPopup.hide();
    }
  }

  protected BurgerMenuBar getRootMenu() {
    BurgerMenuBar parentMenu = getParentMenu();
    if ( parentMenu != null ) {
      return parentMenu.getRootMenu();
    }

    return this;
  }

  protected PopupPanel getRootPopup() {
    Widget parent = getRootMenu().getParent();
    while ( parent != null ) {
      if ( parent instanceof PopupPanel ) {
        return (BurgerMenuPopup) parent;
      }

      parent = parent.getParent();
    }

    return null;
  }

  // Local version of private super.findItem( Element ).
  protected MenuItem findItemLocal( com.google.gwt.dom.client.Element hItem ) {
    for ( MenuItem item : getItems() ) {
      if ( item.getElement().isOrHasChild( hItem ) ) {
        return item;
      }
    }

    return null;
  }

  // Local version of private super.eatEvent( Event ).
  protected void eatEventLocal( Event event ) {
    event.stopPropagation();
    event.preventDefault();
  }

  // Local version of private super.close( boolean ).
  protected void closeLocal( boolean focus ) {
    BurgerMenuBar parentMenuBar = getParentMenu();
    if ( parentMenuBar != null ) {
      PopupPanel popupPanel = parentMenuBar.getPopup();
      if ( popupPanel != null ) {
        popupPanel.hide( !focus );
      }

      if ( focus ) {
        parentMenuBar.focus();
      }
    }
  }

  // Local version of private super.selectFirstItemIfNoneSelected().
  private boolean selectFirstItemIfNoneSelectedLocal() {
    if ( getSelectedItem() == null ) {
      for ( MenuItem nextItem : getItems() ) {
        if ( nextItem.isEnabled() ) {
          selectItem( nextItem );
          break;
        }
      }

      return true;
    }

    return false;
  }

  // Local, adapted version of private super.doItemAction(MenuItem, boolean, boolean).
  // Overrides the command execution of a menu item closes the whole menu (unlike closeAllParents()).
  // Additionally, the special back menu items do not cause the menu to close.
  protected void doItemActionLocal( MenuItem item, boolean fireCommand, boolean focus ) {
    if ( !item.isEnabled() ) {
      return;
    }

    if ( fireCommand && item.getScheduledCommand() != null ) {
      FocusImpl.getFocusImplForPanel().blur( getElement() );

      if ( !( item instanceof BurgerBackMenuItem ) ) {
        closeRoot();
      }

      Scheduler.ScheduledCommand cmd = item.getScheduledCommand();
      Scheduler.get().scheduleFinally( () -> cmd.execute() );
    } else {
      doItemActionLocalSuper( item, fireCommand, focus );
    }

    if ( item.getScheduledCommand() == null && item.getSubMenu() != null ) {
      item.getSubMenu().focus();
    }
  }

  // Local version of private super.doItemAction(MenuItem, boolean, boolean).
  private native void doItemActionLocalSuper( MenuItem item, boolean fireCommand, boolean focus ) /*-{
    this.@com.google.gwt.user.client.ui.MenuBar::doItemAction(Lcom/google/gwt/user/client/ui/MenuItem;ZZ)(item,
        fireCommand, focus);
  }-*/;

  // Access to private field super.popup
  protected DecoratedPopupPanel getPopup() {
    return MenuBarUtils.getPopup( this );
  }

  // Access to private field super.parentMenu
  protected BurgerMenuBar getParentMenu() {
    return (BurgerMenuBar) MenuBarUtils.getParentMenu( this );
  }
}
