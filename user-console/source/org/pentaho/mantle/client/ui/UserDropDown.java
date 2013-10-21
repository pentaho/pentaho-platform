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

package org.pentaho.mantle.client.ui;

import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import org.pentaho.mantle.client.commands.LogoutCommand;
import org.pentaho.mantle.client.messages.Messages;

public class UserDropDown extends CustomDropDown {

  public UserDropDown() {
    super( UserDropDown.getUsername(), null, MODE.MINOR );
    MenuBar menuBar = new MenuBar( true );
    menuBar.addItem( new MenuItem( Messages.getString( "logout" ), new LogoutCommand() ) );
    setMenuBar( menuBar );
  }

  private static native String getUsername()
  /*-{  
    return window.top.SESSION_NAME;
  }-*/;

}
