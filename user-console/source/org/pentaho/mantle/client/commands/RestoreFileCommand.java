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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileActionEvent;
import org.pentaho.mantle.client.messages.Messages;

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
          MessageDialogBox dialogBox =
              new MessageDialogBox(
                  Messages.getString( "cannotRestore" ), Messages.getString( "couldNotRestoreItem", type ), //$NON-NLS-1$ //$NON-NLS-2$
                  false, false, true );
          dialogBox.center();
          event.setMessage( "cannotRestore" );
          EventBusUtil.EVENT_BUS.fireEvent( event );
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == 200 ) {
            new RefreshRepositoryCommand().execute( false );
            event.setMessage( "Success" );
            EventBusUtil.EVENT_BUS.fireEvent( event );
          } else {
            MessageDialogBox dialogBox =
                new MessageDialogBox(
                    Messages.getString( "cannotRestore" ), Messages.getString( "couldNotRestoreItem", type ), //$NON-NLS-1$ //$NON-NLS-2$
                    false, false, true, Messages.getString( "close" ) );
            dialogBox.center();
            event.setMessage( "Success" );
            FileChooserDialog.setIsDirty( Boolean.TRUE );
            setBrowseRepoDirty( Boolean.TRUE );
            EventBusUtil.EVENT_BUS.fireEvent( event );
          }
        }
      } );
    } catch ( RequestException e ) {
      MessageDialogBox dialogBox =
          new MessageDialogBox( Messages.getString( "error" ), Messages.getString( "restoreError" ), //$NON-NLS-1$ //$NON-NLS-2$
              false, false, true );
      dialogBox.center();
      event.setMessage( Messages.getString( "restoreError" ) );
      EventBusUtil.EVENT_BUS.fireEvent( event );
    }
  }
  private static native void setBrowseRepoDirty( boolean isDirty )
  /*-{
    $wnd.mantle_isBrowseRepoDirty=isDirty;
  }-*/;
}
