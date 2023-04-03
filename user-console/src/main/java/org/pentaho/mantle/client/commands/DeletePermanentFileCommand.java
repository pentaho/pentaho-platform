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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileActionEvent;
import org.pentaho.mantle.client.messages.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wseyler
 */
public class DeletePermanentFileCommand extends AbstractCommand {
  String moduleBaseURL = GWT.getModuleBaseURL();

  String moduleName = GWT.getModuleName();
  static final String MULTIPLE_DELETE_TYPE = "multi";

  String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

  List<RepositoryFile> repositoryFiles;
  List<String> deletePermFileIds = new ArrayList();

  private String fileList = "";
  private String type = "";
  private String mode = "";

  private static final String COULDNOTDELETEFILE = "couldNotDeleteFile";


  public String getFileList() {
    return fileList;
  }

  public void setFileList( String fileList ) {
    this.fileList = fileList;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getMode() {
    return mode;
  }

  public void setMode( String mode ) {
    this.mode = mode;
  }

  public DeletePermanentFileCommand() {
  }

  public DeletePermanentFileCommand( String fileList ) {
    setFileList( fileList );
  }


  /*
 * (non-Javadoc)
 *
 * @see com.google.gwt.user.client.Command#execute()
 */
  protected void performOperation( boolean feedback ) {
    final SolutionFileActionEvent event = new SolutionFileActionEvent();

    event.setAction( this.getClass().getName() );

    final MessageDialogBox deleteConfirmDialog;

    if ( mode != null && mode.equals( "purge" ) ) {
      deleteConfirmDialog = new MessageDialogBox(
          Messages.getString( "emptyTrash" ),
          Messages.getString( "deleteAllQuestion" ),
          true,
          Messages.getString( "yesEmptyTrash" ),
          Messages.getString( "no" ) );
    } else {
      String deleteMessage = MULTIPLE_DELETE_TYPE.equals( type )
        ? Messages.getString( "deleteMultiQuestion" )
        : Messages.getString( "deleteQuestion", type );

      deleteConfirmDialog = new MessageDialogBox(
        Messages.getString( "permDelete" ),
        deleteMessage,
        true,
        Messages.getString( "yesPermDelete" ),
        Messages.getString( "no" ) );
    }

    final IDialogCallback callback = new IDialogCallback() {
      public void cancelPressed() {
        deleteConfirmDialog.hide();
      }

      public void okPressed() {
        String temp = "";

        // Add js file list
        temp = temp + fileList;

        // remove trailing ","
        temp = temp.substring( 0, temp.length() - 1 );

        final String filesList = temp;

        String deleteFilesURL = contextURL + "api/repo/files/deletepermanent"; //$NON-NLS-1$
        RequestBuilder deleteFilesRequestBuilder = new RequestBuilder( RequestBuilder.PUT, deleteFilesURL );
        deleteFilesRequestBuilder.setHeader( "Content-Type", "text/plain" ); //$NON-NLS-1$//$NON-NLS-2$
        deleteFilesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
        try {
          deleteFilesRequestBuilder.sendRequest( filesList, new RequestCallback() {

            @Override
            public void onError( Request request, Throwable exception ) {
              MessageDialogBox dialogBox = new MessageDialogBox(
                Messages.getString( "error" ),
                Messages.getString( COULDNOTDELETEFILE ),
                false );
              dialogBox.center();
              event.setMessage( Messages.getString( COULDNOTDELETEFILE ) );
              EventBusUtil.EVENT_BUS.fireEvent( event );
            }

            @Override
            public void onResponseReceived( Request request, Response response ) {
              if ( response.getStatusCode() == 200 ) {
                new RefreshRepositoryCommand().execute( false );
                event.setMessage( "Success" );
                FileChooserDialog.setIsDirty( Boolean.TRUE );
                setBrowseRepoDirty( Boolean.TRUE );
                EventBusUtil.EVENT_BUS.fireEvent( event );
              } else {

                event.setMessage( Messages.getString( COULDNOTDELETEFILE ) );
                EventBusUtil.EVENT_BUS.fireEvent( event );
              }
            }

          } );
        } catch ( RequestException e ) {
          MessageDialogBox dialogBox = new MessageDialogBox(
            Messages.getString( "error" ),
            Messages.getString( COULDNOTDELETEFILE ),
            false );
          dialogBox.center();
          event.setMessage( Messages.getString( COULDNOTDELETEFILE ) );
          EventBusUtil.EVENT_BUS.fireEvent( event );
        }
      }
    };
    if ( !feedback ) {
      callback.okPressed();
    } else {
      deleteConfirmDialog.setCallback( callback );
      deleteConfirmDialog.center();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.commands.AbstractCommand#performOperation()
   */
  @Override
  protected void performOperation() {
    performOperation( true );
  }

  private static native void setBrowseRepoDirty( boolean isDirty )
  /*-{
      $wnd.mantle_isBrowseRepoDirty = isDirty;
  }-*/;
}
