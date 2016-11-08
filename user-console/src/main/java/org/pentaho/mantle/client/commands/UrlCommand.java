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
      String height = dialogHeight + "px"; //$NON-NLS-1$
      String width = dialogWidth + "px"; //$NON-NLS-1$

      final Frame frame = new Frame( url );

      final PromptDialogBox dialogBox = new PromptDialogBox( title, Messages.getString( "ok" ), null, false, false );
      dialogBox.setStylePrimaryName( "pentaho-dialog" );
      dialogBox.setText( title );
      dialogBox.setContent( frame );

      frame.setSize( width, height );
      dialogBox.center();
      frame.setSize( width, height );
    } else {
      SolutionBrowserPanel navigatorPerspective = SolutionBrowserPanel.getInstance();
      navigatorPerspective.getContentTabPanel().showNewURLTab( title, "", url, false ); //$NON-NLS-1$
    }
  }
}
