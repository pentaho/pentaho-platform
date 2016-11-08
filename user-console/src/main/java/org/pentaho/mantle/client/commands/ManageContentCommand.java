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

package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.mantle.client.dialogs.ManageContentDialog;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand;

public class ManageContentCommand extends AbstractCommand {

  public ManageContentCommand() {
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    final ManageContentDialog dialog = new ManageContentDialog();
    dialog.setCallback( new IDialogCallback() {
      public void okPressed() {
        if ( dialog.getState() == ManageContentDialog.STATE.EDIT ) {
          OpenFileCommand cmd = new OpenFileCommand( FileCommand.COMMAND.EDIT );
          cmd.execute();
        } else if ( dialog.getState() == ManageContentDialog.STATE.SHARE ) {
          OpenFileCommand cmd = new OpenFileCommand( FileCommand.COMMAND.SHARE );
          cmd.execute();
        } else if ( dialog.getState() == ManageContentDialog.STATE.SCHEDULE ) {
          OpenFileCommand cmd = new OpenFileCommand( FileCommand.COMMAND.SCHEDULE_NEW );
          cmd.execute();
        }
      }

      public void cancelPressed() {
      }
    } );
    dialog.center();
  }
}
