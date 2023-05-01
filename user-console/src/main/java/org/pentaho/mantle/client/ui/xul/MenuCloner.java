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

package org.pentaho.mantle.client.ui.xul;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.UIObject;
import org.pentaho.gwt.widgets.client.menuitem.CheckBoxMenuItem;
import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuItem;
import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuSeparator;
import org.pentaho.gwt.widgets.client.utils.MenuBarUtils;

import java.util.function.Function;

public class MenuCloner {

  public static <T extends MenuBar> T cloneMenuBar( MenuBar menuBar, Function<MenuBar, T> menuBarCreator ) {
    T menuBarClone = menuBarCreator.apply( menuBar );

    menuBarClone.setAnimationEnabled( menuBar.isAnimationEnabled() );
    menuBarClone.setAutoOpen( menuBar.getAutoOpen() );
    menuBarClone.setFocusOnHoverEnabled( menuBar.isFocusOnHoverEnabled() );

    menuBarClone.getElement().setId( menuBar.getElement().getId() );

    MenuBarUtils.getAllItems( menuBar ).forEach( ( UIObject uio ) -> {
      if ( uio instanceof MenuItemSeparator ) {
        menuBarClone.addSeparator( cloneMenuItemSeparator( (MenuItemSeparator) uio ) );
      } else {
        MenuItem menuItemClone;
        if ( uio instanceof PentahoMenuItem ) {
          menuItemClone = clonePentahoMenuItem( (PentahoMenuItem) uio, menuBarCreator );
        } else if ( uio instanceof CheckBoxMenuItem ) {
          menuItemClone = cloneCheckBoxMenuItem( (CheckBoxMenuItem) uio, menuBarCreator );
        } else if ( uio instanceof MenuItem ) {
          menuItemClone = cloneMenuItem( (MenuItem) uio, menuBarCreator );
        } else {
          throw new RuntimeException( "Logic not implemented to copy menu related object: " + uio.getClass() );
        }

        menuBarClone.addItem( menuItemClone );
      }
    } );

    return menuBarClone;
  }

  public static <T extends MenuBar> MenuItem cloneMenuItem( MenuItem menuItem, Function<MenuBar, T> menuBarCreator ) {
    MenuItem menuItemClone = new MenuItem( menuItem.getHTML(), true, (Scheduler.ScheduledCommand) null );
    return cloneMenuItem( menuItem, menuItemClone, menuBarCreator );
  }

  public static <T extends MenuBar> MenuItem cloneMenuItem( MenuItem menuItem, MenuItem menuItemClone,
                                                            Function<MenuBar, T> menuBarCreator ) {
    menuItemClone.setEnabled( menuItem.isEnabled() );
    menuItemClone.setVisible( menuItem.isVisible() );

    menuItemClone.getElement().setId( menuItem.getElement().getId() );

    MenuBar subMenuBar = menuItem.getSubMenu();
    if ( subMenuBar != null ) {
      menuItemClone.setSubMenu( cloneMenuBar( subMenuBar, menuBarCreator ) );
    }

    // Strangely, it seems menu items with submenus can also have a command.
    if ( menuItem.getScheduledCommand() != null ) {
      menuItemClone.setScheduledCommand( menuItem.getScheduledCommand() );
    }

    return menuItemClone;
  }

  public static MenuItemSeparator cloneMenuItemSeparator( MenuItemSeparator menuItemSeparator ) {
    MenuItemSeparator menuItemSeparatorClone;
    if ( menuItemSeparator instanceof PentahoMenuSeparator ) {
      menuItemSeparatorClone = new PentahoMenuSeparator();
    } else {
      menuItemSeparatorClone = new MenuItemSeparator();
    }

    menuItemSeparatorClone.setVisible( menuItemSeparator.isVisible() );

    return menuItemSeparatorClone;

  }

  public static <T extends MenuBar> PentahoMenuItem clonePentahoMenuItem( PentahoMenuItem menuItem,
                                                                          Function<MenuBar, T> menuBarCreator ) {
    PentahoMenuItem menuItemClone = new PentahoMenuItem( menuItem.getText(), null );
    menuItemClone.setUseCheckUI( menuItem.isUseCheckUI() );
    menuItemClone.setChecked( menuItem.isChecked() );

    cloneMenuItem( menuItem, menuItemClone, menuBarCreator );

    return menuItemClone;
  }

  public static <T extends MenuBar> CheckBoxMenuItem cloneCheckBoxMenuItem( CheckBoxMenuItem menuItem,
                                                                            Function<MenuBar, T> menuBarCreator ) {
    CheckBoxMenuItem menuItemClone = new CheckBoxMenuItem( menuItem.getText(), null );
    menuItemClone.setChecked( menuItem.isChecked() );

    cloneMenuItem( menuItem, menuItemClone, menuBarCreator );

    return menuItemClone;
  }
}
