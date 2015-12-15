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

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.HTML;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileActionEvent;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserFile;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import java.util.ArrayList;
import java.util.List;

public class DeleteFileCommand extends AbstractCommand {
  String moduleBaseURL = GWT.getModuleBaseURL();

  String moduleName = GWT.getModuleName();

  String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

  public DeleteFileCommand() {
  }

  private String solutionPath = null;
  private String fileNames = null;
  private String fileIds = null;

  private List<SolutionBrowserFile> filesToDelete = new ArrayList();

  public String getSolutionPath() {
    return solutionPath;
  }

  public void setSolutionPath( String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  public String getFileNames() {
    return fileNames;
  }

  public void setFileNames( String fileNames ) {
    this.fileNames = fileNames;
  }

  public String getFileIds() {
    return fileIds;
  }

  public void setFileIds( String fileIds ) {
    this.fileIds = fileIds;
  }

  protected void performOperation() {

    if ( this.getSolutionPath() != null && this.getFileNames() != null && this.getFileIds() != null ) {
      StringTokenizer pathTk = new StringTokenizer( this.getSolutionPath(), "\n" );
      StringTokenizer nameTk = new StringTokenizer( this.getFileNames(), "\n" );
      StringTokenizer idTk = new StringTokenizer( this.getFileIds(), "\n" );
      //Build Arrays since we cannot pass complex objects from the js bus
      for ( int i = 0; i < pathTk.countTokens(); i++ ) {
        filesToDelete.add( new SolutionBrowserFile( idTk.tokenAt( i ), nameTk.tokenAt( i ), pathTk.tokenAt( i ) ) );
      }
      performOperation( true );
    } else {
      performOperation( true );
    }
  }

  SolutionFileActionEvent getSolutionFileActionEvent() {
    return new SolutionFileActionEvent();
  }

  PromptDialogBox getPromptDialogBox( String title, String okText, String cancelText, boolean autoHide,
      boolean modal ) {
    return new PromptDialogBox( title, okText, cancelText, autoHide, modal );
  }

  protected void performOperation( boolean feedback ) {
    final SolutionFileActionEvent event = getSolutionFileActionEvent();
    event.setAction( this.getClass().getName() );

    String temp = "";
    String names = "";
    //Convert to a comma delimted list for rest call
    for ( SolutionBrowserFile file : filesToDelete ) {
      temp += file.getId() + ","; //$NON-NLS-1$
      names += file.getName() + ","; //$NON-NLS-1$
    }

    // remove trailing ","
    if ( temp.length() > 0 ) {
      temp = temp.substring( 0, temp.length() - 1 );
    }

    if ( names.length() > 0 ) {
      names = names.substring( 0, names.length() - 1 );
    }

    final String filesList = temp;

    if ( feedback ) {
      final HTML messageTextBox;
      if ( filesToDelete.size() > 1 ) {
        messageTextBox = new HTML( Messages.getString( "moveAllToTrashQuestionFile" ) );
      } else {
        messageTextBox = new HTML( Messages.getString( "moveToTrashQuestionFile", names ) );
      }
      final PromptDialogBox
          fileMoveToTrashWarningDialogBox =
          getPromptDialogBox( Messages.getString( "moveToTrash" ), Messages.getString( "yesMoveToTrash" ),
              Messages.getString( "no" ), true, true );
      fileMoveToTrashWarningDialogBox.setContent( messageTextBox );

      final IDialogCallback callback = new IDialogCallback() {

        public void cancelPressed() {
          fileMoveToTrashWarningDialogBox.hide();
        }

        public void okPressed() {

          doDelete( filesList, event );
        }
      };
      fileMoveToTrashWarningDialogBox.setCallback( callback );
      fileMoveToTrashWarningDialogBox.center();
    } else {
      doDelete( filesList, event );
    }
  }

  RequestBuilder getRequestBuilder( RequestBuilder.Method method, String url ) {
    return new RequestBuilder( method, url );
  }

  public void doDelete( String filesList, final SolutionFileActionEvent event ) {
    String deleteFilesURL = contextURL + "api/repo/files/delete"; //$NON-NLS-1$
    RequestBuilder deleteFilesRequestBuilder = getRequestBuilder( RequestBuilder.PUT, deleteFilesURL );
    deleteFilesRequestBuilder.setHeader( "Content-Type", "text/plain" ); //$NON-NLS-1$//$NON-NLS-2$
    deleteFilesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      deleteFilesRequestBuilder.sendRequest( filesList, new RequestCallback() {

        @Override public void onError( Request request, Throwable exception ) {
          MessageDialogBox
              dialogBox =
              new MessageDialogBox( Messages.getString( "error" ), Messages.getString( "couldNotDeleteFile" ),
                  //$NON-NLS-1$ //$NON-NLS-2$
                  false, false, true );
          dialogBox.center();

          event.setMessage( Messages.getString( "couldNotDeleteFile" ) );
          EventBusUtil.EVENT_BUS.fireEvent( event );
        }

        @Override public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == 200 ) {
            event.setMessage( "Success" );
            EventBusUtil.EVENT_BUS.fireEvent( event );
            new RefreshRepositoryCommand().execute( false );
            FileChooserDialog.setIsDirty( Boolean.TRUE );
            setBrowseRepoDirty( Boolean.TRUE );
            for ( SolutionBrowserFile file : filesToDelete ) {
              if ( file.getPath() != null ) {
                SolutionBrowserPanel.getInstance().removeRecent( file.getPath() );
                SolutionBrowserPanel.getInstance().removeFavorite( file.getPath() );
              }
            }
          } else {
            event.setMessage( Messages.getString( "couldNotDeleteFile" ) );
            EventBusUtil.EVENT_BUS.fireEvent( event );
          }
        }

      } );
    } catch ( RequestException e ) {
      MessageDialogBox
          dialogBox =
          getMessageDialogBox( Messages.getString( "error" ), Messages.getString( "couldNotDeleteFile" ),
              //$NON-NLS-1$ //$NON-NLS-2$
              false, false, true );
      dialogBox.center();
      event.setMessage( Messages.getString( "couldNotDeleteFile" ) );
      EventBusUtil.EVENT_BUS.fireEvent( event );
    }
  }

  MessageDialogBox getMessageDialogBox( String title, String message, boolean isHTML, boolean autoHide,
      boolean modal ) {
    return new MessageDialogBox( title, message, isHTML, autoHide, modal );
  }

  private static native void setBrowseRepoDirty( boolean isDirty )
  /*-{
    $wnd.mantle_isBrowseRepoDirty = isDirty;
  }-*/;
}
