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

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.Repository;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileActionEvent;
import org.pentaho.mantle.client.events.SolutionFileHandler;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HTML;

public class DeleteMultiFilesCommand extends AbstractCommand {
  String moduleBaseURL = GWT.getModuleBaseURL();

  String moduleName = GWT.getModuleName();

  String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

  private List<FileItem> repositoryFiles;

  public DeleteMultiFilesCommand() {
  }

  public DeleteMultiFilesCommand( List<FileItem> selectedItemsClone ) {
    this.repositoryFiles = selectedItemsClone;
  }

  private String solutionPath = null;

  public String getSolutionPath() {
    return solutionPath;
  }

  public void setSolutionPath( String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  protected void performOperation() {

    if ( this.getSolutionPath() != null ) {
      performOperation( true );
    }
  }

  protected void performOperation( boolean feedback ) {
    final SolutionFileActionEvent event = new SolutionFileActionEvent();
    event.setAction( this.getClass().getName() );

    if ( feedback ) {
      final HTML messageTextBox = new HTML( Messages.getString( "moveAllToTrashQuestionFile" ) );
      final PromptDialogBox fileMoveToTrashWarningDialogBox =
        new PromptDialogBox( Messages.getString( "moveToTrash" ), Messages.getString( "yesMoveToTrash" ), Messages
          .getString( "no" ), true, true );
      fileMoveToTrashWarningDialogBox.setContent( messageTextBox );

      final IDialogCallback callback = new IDialogCallback() {

        public void cancelPressed() {
          fileMoveToTrashWarningDialogBox.hide();
        }

        public void okPressed() {

          StringTokenizer filesToDelete = new StringTokenizer( solutionPath, "\t" );
          for ( int i = 0; i < filesToDelete.countTokens(); i++ ) {

            DeleteFileCommand cmd = new DeleteFileCommand();
            cmd.performOperationMulti( filesToDelete.tokenAt( i ) );

          }

        }
      };
      fileMoveToTrashWarningDialogBox.setCallback( callback );
      fileMoveToTrashWarningDialogBox.center();
    } else {
      //do nothing
    }
  }

}
