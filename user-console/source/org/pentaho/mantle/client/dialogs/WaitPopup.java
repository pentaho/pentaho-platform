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

package org.pentaho.mantle.client.dialogs;

import com.google.gwt.user.client.ui.SimplePanel;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.messages.Messages;

public class WaitPopup extends SimplePanel {

  private static WaitPopup instance = new WaitPopup();

  private WaitPopup() {
  }

  public static WaitPopup getInstance() {
    return instance;
  }

  @Override
  public void setVisible( boolean visible ) {
    if ( visible ) {
      MantleApplication.showBusyIndicator( Messages.getString( "pleaseWait" ), Messages.getString( "waitMessage" ) );
    } else {
      MantleApplication.hideBusyIndicator();
    }
  }

  public void setVisibleById( boolean visible, String id ) {
    if ( visible ) {
      MantleApplication.showBusyIndicatorById( Messages.getString( "pleaseWait" ), Messages.getString( "waitMessage" ),
          id );
    } else {
      MantleApplication.hideBusyIndicatorById( id );
    }
  }

}
