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

import com.google.gwt.user.client.ui.Frame;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

public class UrlCommand extends AbstractCommand {

  String url;
  String title;
  boolean showInDialog = false;
  int dialogWidth = 600;
  int dialogHeight = 400;

  public UrlCommand() {
  }

  public UrlCommand( String url, String title ) {
    this.url = url;
    this.title = title;
  }

  public UrlCommand( String url, String title, boolean showInDialog, int dialogWidth, int dialogHeight ) {
    this( url, title );
    this.showInDialog = showInDialog;
    this.dialogWidth = dialogWidth;
    this.dialogHeight = dialogHeight;
  }

  public static void _execute( String url, String title, boolean showInDialog, int dialogWidth, int dialogHeight ) {
    UrlCommand cmd = new UrlCommand( url, title, showInDialog, dialogWidth, dialogHeight );
    cmd.execute();
  }

  protected void performOperation() {
    performOperation( false );
  }

  protected void performOperation( boolean feedback ) {
    if ( showInDialog ) {
      final Frame frame = new Frame( url );
      frame.setSize( dialogHeight + "px", dialogWidth + "px" );

      final PromptDialogBox dialogBox =
              new PromptDialogBox( title, Messages.getString( "ok" ), null, false, false, frame );
      dialogBox.center();
    } else {
      SolutionBrowserPanel navigatorPerspective = SolutionBrowserPanel.getInstance();
      navigatorPerspective.getContentTabPanel().showNewURLTab( title, "", url, false ); //$NON-NLS-1$
    }
  }
}
