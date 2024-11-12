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

import com.google.gwt.user.client.ui.TextBox;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.IDialogValidatorCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

public class OpenURLCommand extends AbstractCommand {

  public OpenURLCommand() {
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    final TextBox textBox = new TextBox();
    textBox.setText( "http://" ); //$NON-NLS-1$
    textBox.setWidth( "500px" ); //$NON-NLS-1$
    textBox.setVisibleLength( 72 );
    IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
      }

      public void okPressed() {
        SolutionBrowserPanel.getInstance().getContentTabPanel().showNewURLTab( textBox.getText(), textBox.getText(),
            textBox.getText(), false );
      }

    };
    IDialogValidatorCallback validatorCallback = new IDialogValidatorCallback() {
      public boolean validate() {
        boolean isValid = !"".equals( textBox.getText() ) && textBox.getText() != null; //$NON-NLS-1$
        if ( !isValid ) {
          MessageDialogBox dialogBox =
              new MessageDialogBox(
                  Messages.getString( "error" ), Messages.getString( "urlNotSpecified" ), false );
          dialogBox.center();
        }
        return isValid;
      }
    };
    PromptDialogBox promptDialog =
        new PromptDialogBox(
            Messages.getString( "enterURL" ), Messages.getString( "ok" ), Messages.getString( "cancel" ), false, true, textBox ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    promptDialog.setValidatorCallback( validatorCallback );
    promptDialog.setCallback( callback );
    promptDialog.setWidth( "500px" ); //$NON-NLS-1$
    promptDialog.center();
  }

}
