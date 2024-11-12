/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
