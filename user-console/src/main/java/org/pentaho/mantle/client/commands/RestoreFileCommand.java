/*!
 *
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
 *
 * Copyright (c) 2002-2023 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.mantle.client.commands;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.dialogs.OverwritePromptDialog;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileActionEvent;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import java.util.List;

/**
 * @author wseyler
 * 
 */
public class RestoreFileCommand implements Command {
  String moduleBaseURL = GWT.getModuleBaseURL();
  String moduleName = GWT.getModuleName();
  String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

  List<RepositoryFile> repositoryFiles;

  String fileList;

  String type;

  private static final String CANNOTRESTORE = "cannotRestore";

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getFileList() {
    return fileList;
  }

  public void setFileList( String fileList ) {
    this.fileList = fileList;
  }

  public RestoreFileCommand() {
  }

  /**
   * @param selectedItemsClone
   */
  public RestoreFileCommand( List<RepositoryFile> selectedItemsClone ) {
    repositoryFiles = selectedItemsClone;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.user.client.Command#execute()
   */
  @Override
  public void execute() {
    final SolutionFileActionEvent event = new SolutionFileActionEvent();
    event.setAction( this.getClass().getName() );
    String temp = "";

    if ( repositoryFiles != null ) {
      for ( RepositoryFile repoFile : repositoryFiles ) {
        temp += repoFile.getId() + ","; //$NON-NLS-1$
      }
    }

    // Add file names from js
    temp = temp + fileList;

    // remove trailing ","
    temp = temp.substring( 0, temp.length() - 1 );

    final String filesList = temp;

    String deleteFilesURL = contextURL + "api/repo/files/restore"; //$NON-NLS-1$
    RequestBuilder deleteFilesRequestBuilder = new RequestBuilder( RequestBuilder.PUT, deleteFilesURL );
    deleteFilesRequestBuilder.setHeader( "Content-Type", "text/plain" ); //$NON-NLS-1$//$NON-NLS-2$
    deleteFilesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      deleteFilesRequestBuilder.sendRequest( filesList, new RequestCallback() {

        @Override
        public void onError( Request request, Throwable exception ) {
          MessageDialogBox dialogBox = new MessageDialogBox(
            Messages.getString( CANNOTRESTORE ),
            Messages.getString( "couldNotRestoreItem", type ),
            false );
          dialogBox.center();
          event.setMessage( CANNOTRESTORE );
          EventBusUtil.EVENT_BUS.fireEvent( event );
        }


        @Override
        public void onResponseReceived( final Request request, final Response response ) {
          if ( response.getStatusCode() == Response.SC_OK ) {
            new RefreshRepositoryCommand().execute( false );
            event.setMessage( "Success" );
            EventBusUtil.EVENT_BUS.fireEvent( event );
          } else if ( response.getStatusCode() == Response.SC_CONFLICT
            || response.getStatusCode() == Response.SC_NOT_ACCEPTABLE ) {
            final int restoreResponseStatusCode = response.getStatusCode();

            final String userHomeDirUrl = GWT.getHostPageBaseURL() + "api/session/userWorkspaceDir";

            final RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, userHomeDirUrl );
            try {
              // Get user home folder string
              builder.sendRequest( "", new RequestCallback() {
                @Override
                public void onResponseReceived( final Request request, final Response response ) {
                  if ( response.getStatusCode() == 200 ) {
                    // API returns /user/home_folder/workspace
                    String userHomeFolderPath = response.getText().replaceAll( "/workspace", "" );
                    performRestoreToHomeFolder( filesList, restoreResponseStatusCode, userHomeFolderPath, event );
                  }
                }

                @Override
                public void onError( Request request, Throwable exception ) {
                  showErrorDialogBox( event );
                }
              } );
            } catch ( RequestException e ) {
              showErrorDialogBox( event );
            }
          } else {
            MessageDialogBox dialogBox = new MessageDialogBox(
              Messages.getString( CANNOTRESTORE ),
              Messages.getString( "couldNotRestoreItem", type ),
              false,
              Messages.getString( "close" ) );
            dialogBox.center();
            event.setMessage( "Success" );
            FileChooserDialog.setIsDirty( Boolean.TRUE );
            setBrowseRepoDirty( Boolean.TRUE );
            EventBusUtil.EVENT_BUS.fireEvent( event );
          }
        }
      } );
    } catch ( RequestException e ) {
      showErrorDialogBox( event );
    }
  }

  private void performRestoreToHomeFolder( final String filesList, final int restoreResponseStatusCode,
                                           final String userHomeFolderPath, final SolutionFileActionEvent event ) {

    final String encodedUserHomeFolderPath = SolutionBrowserPanel.pathToId( userHomeFolderPath );
    String fileListDescription = "files";
    // if there is one file
    if ( filesList.split( "," ).length == 1 ) {
      fileListDescription = "file";
    }

    String messageHtml = Messages.getString( "cannotRestoreToOriginFolder", fileListDescription )
      + "<br> <br>"
      + Messages.getString( "restoreToHomeFolder", userHomeFolderPath )
      + "<br>";

    final MessageDialogBox restoreFileWarningDialogBox = new MessageDialogBox(
      Messages.getString( "couldNotWriteToFolder" ),
      messageHtml,
      true,
      Messages.getString( "restoreFile" ),
      Messages.getString( "cancel" ) );

    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
        restoreFileWarningDialogBox.hide();
      }

      public void okPressed() {
        // We can't write to origin file folder, and there are
        // files in homeFolder with same names
        if ( restoreResponseStatusCode == Response.SC_CONFLICT ) {
          final OverwritePromptDialog overwriteDialog = new OverwritePromptDialog();
          final IDialogCallback callback = new IDialogCallback() {
            public void cancelPressed() {
              event.setMessage( "Cancel" );
              overwriteDialog.hide();
            }

            public void okPressed() {
              String restoreFilesUrl = contextURL + "api/repo/files/restore?overwriteMode=" + overwriteDialog.getOverwriteMode(); //$NON-NLS-1$
              RequestBuilder builder = new RequestBuilder( RequestBuilder.PUT, restoreFilesUrl );
              try {
                builder.sendRequest( filesList, new RequestCallback() {
                  @Override
                  public void onResponseReceived( Request request, Response response ) {
                    if ( response.getStatusCode() == Response.SC_OK ) {
                      new RefreshRepositoryCommand().execute( false );
                      event.setMessage( "Success" );
                      EventBusUtil.EVENT_BUS.fireEvent( event );
                    } else {
                      showErrorDialogBox( event );
                    }
                  }

                  @Override
                  public void onError( Request request, Throwable exception ) {
                    showErrorDialogBox( event );
                  }
                } );
              } catch ( RequestException e ) {
                showErrorDialogBox( event );
              }
            }
          };
          overwriteDialog.setCallback( callback );
          overwriteDialog.center();
        } else if ( restoreResponseStatusCode == Response.SC_NOT_ACCEPTABLE ) {
          String moveFilesURL = contextURL + "api/repo/files/" + encodedUserHomeFolderPath + "/move";
          RequestBuilder builder = new RequestBuilder( RequestBuilder.PUT, moveFilesURL );
          try {
            builder.sendRequest( filesList, new RequestCallback() {
              @Override
              public void onResponseReceived( Request request, Response response ) {
                new RefreshRepositoryCommand().execute( false );
                event.setMessage( "Success" );
                EventBusUtil.EVENT_BUS.fireEvent( event );
              }

              @Override
              public void onError( Request request, Throwable exception ) {
                showErrorDialogBox( event );
              }
            } );
          } catch ( RequestException e ) {
            showErrorDialogBox( event );
          }
        }
      }
    };

    restoreFileWarningDialogBox.setCallback( callback );
    restoreFileWarningDialogBox.center();
  }

  public void showErrorDialogBox( SolutionFileActionEvent event ) {
    MessageDialogBox dialogBox = new MessageDialogBox(
      Messages.getString( "error" ),
      Messages.getString( "restoreError" ),
      false );
    dialogBox.center();
    event.setMessage( Messages.getString( "restoreError" ) );
    EventBusUtil.EVENT_BUS.fireEvent( event );
  }

  private static native void setBrowseRepoDirty( boolean isDirty )
  /*-{
    $wnd.mantle_isBrowseRepoDirty=isDirty;
  }-*/;
}
