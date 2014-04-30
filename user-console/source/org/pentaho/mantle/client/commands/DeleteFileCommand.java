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

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
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

public class DeleteFileCommand extends AbstractCommand {
  String moduleBaseURL = GWT.getModuleBaseURL();

  String moduleName = GWT.getModuleName();

  String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

  private List<FileItem> repositoryFiles;

  public DeleteFileCommand() {
  }

  public DeleteFileCommand( List<FileItem> selectedItemsClone ) {
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
      SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
      sbp.getFile( this.getSolutionPath(), new SolutionFileHandler() {
        @Override
        public void handle( RepositoryFile repositoryFile ) {
          if ( repositoryFiles == null ) {
            repositoryFiles = new ArrayList<FileItem>();
          }
          repositoryFiles.add( new FileItem( repositoryFile, null, null, false, null ) );
          performOperation( true );
        }
      } );
    } else {
      performOperation( true );
    }
  }

  protected void performOperationMulti( String path ) {

    if ( path != null ) {
      SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
      sbp.getFile( path, new SolutionFileHandler() {
        @Override
        public void handle( RepositoryFile repositoryFile ) {
          if ( repositoryFiles == null ) {
            repositoryFiles = new ArrayList<FileItem>();
          }
          repositoryFiles.add( new FileItem( repositoryFile, null, null, false, null ) );
          performOperation( false );
        }
      } );
    }
  }

  protected void performOperation( boolean feedback ) {
    final SolutionFileActionEvent event = new SolutionFileActionEvent();
    event.setAction( this.getClass().getName() );

    String temp = "";
    String names = "";
    RepositoryFile rf = null;
    for ( FileItem fileItem : repositoryFiles ) {
      rf = fileItem.getRepositoryFile();
      temp += rf.getId() + ","; //$NON-NLS-1$
      if ( rf.getTitle() != null ) {
        names += rf.getTitle() + ","; //$NON-NLS-1$
      } else {
        names += rf.getName() + ","; //$NON-NLS-1$
      }

    }
    // remove trailing ","
    temp = temp.substring( 0, temp.length() - 1 );
    names = names.substring( 0, names.length() - 1 );
    final String filesList = temp;

    if ( feedback ) {
      final HTML messageTextBox = new HTML( Messages.getString( "moveToTrashQuestionFile", names ) );
      final PromptDialogBox fileMoveToTrashWarningDialogBox =
          new PromptDialogBox( Messages.getString( "moveToTrash" ), Messages.getString( "yesMoveToTrash" ), Messages
              .getString( "no" ), true, true );
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

  public void doDelete( String filesList, final SolutionFileActionEvent event ) {
    String deleteFilesURL = contextURL + "api/repo/files/delete"; //$NON-NLS-1$
    RequestBuilder deleteFilesRequestBuilder = new RequestBuilder( RequestBuilder.PUT, deleteFilesURL );
    deleteFilesRequestBuilder.setHeader( "Content-Type", "text/plain" ); //$NON-NLS-1$//$NON-NLS-2$
    deleteFilesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      deleteFilesRequestBuilder.sendRequest( filesList, new RequestCallback() {

        @Override
        public void onError( Request request, Throwable exception ) {
          MessageDialogBox dialogBox =
              new MessageDialogBox( Messages.getString( "error" ), Messages.getString( "couldNotDeleteFile" ), //$NON-NLS-1$ //$NON-NLS-2$
                  false, false, true );
          dialogBox.center();

          event.setMessage( Messages.getString( "couldNotDeleteFile" ) );
          EventBusUtil.EVENT_BUS.fireEvent( event );
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == 200 ) {
            event.setMessage( "Success" );
            EventBusUtil.EVENT_BUS.fireEvent( event );
            new RefreshRepositoryCommand().execute( false );
            FileChooserDialog.setIsDirty( Boolean.TRUE );
            setBrowseRepoDirty( Boolean.TRUE );
            for( FileItem recentItem : repositoryFiles ) {
              if( recentItem != null ) {
                SolutionBrowserPanel.getInstance().removeRecent( recentItem.getPath() );
                SolutionBrowserPanel.getInstance().removeFavorite( recentItem.getPath() );
              }
            }
          } else {
            MessageDialogBox dialogBox =
                new MessageDialogBox( Messages.getString( "error" ), Messages.getString( "couldNotDeleteFile" ), //$NON-NLS-1$ //$NON-NLS-2$
                    false, false, true );
            dialogBox.center();
            event.setMessage( Messages.getString( "couldNotDeleteFile" ) );
            EventBusUtil.EVENT_BUS.fireEvent( event );
          }
        }

      } );
    } catch ( RequestException e ) {
      MessageDialogBox dialogBox =
          new MessageDialogBox( Messages.getString( "error" ), Messages.getString( "couldNotDeleteFile" ), //$NON-NLS-1$ //$NON-NLS-2$
              false, false, true );
      dialogBox.center();
      event.setMessage( Messages.getString( "couldNotDeleteFile" ) );
      EventBusUtil.EVENT_BUS.fireEvent( event );
    }

  }
  
  private static native void setBrowseRepoDirty( boolean isDirty )
  /*-{
    $wnd.mantle_isBrowseRepoDirty=isDirty;
  }-*/;
}
