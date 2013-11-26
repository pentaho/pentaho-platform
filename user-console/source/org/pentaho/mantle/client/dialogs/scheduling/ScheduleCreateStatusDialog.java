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

package org.pentaho.mantle.client.dialogs.scheduling;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.ui.PerspectiveManager;

public class ScheduleCreateStatusDialog extends PromptDialogBox {

  public ScheduleCreateStatusDialog() {
    super( Messages.getString( "scheduleCreated" ), Messages.getString( "yes" ), Messages.getString( "no" ), false,
        true );
    Label label = new Label();
    label.setText( Messages.getString( "scheduleCreateSuccess" ) );
    label.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_LEFT );
    setContent( label );
    setWidth( "400px" );
  }

  protected void onOk() {
    super.onOk();
    PerspectiveManager.getInstance().setPerspective( PerspectiveManager.SCHEDULES_PERSPECTIVE );
  }

}
