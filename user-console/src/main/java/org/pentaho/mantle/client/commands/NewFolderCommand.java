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

import org.pentaho.gwt.widgets.client.dialogs.DialogBox;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.panel.VerticalFlexPanel;
import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.gwt.widgets.client.utils.NameUtils;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileHandler;
import org.pentaho.mantle.client.events.SolutionFolderActionEvent;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class NewFolderCommand extends AbstractCommand {

  private String solutionPath = null;
  private String moduleBaseURL = GWT.getModuleBaseURL();
  private String moduleName = GWT.getModuleName();
  private String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

  private RepositoryFile parentFolder;

  private ICallback<String> callback;

  public NewFolderCommand() {
  }

  public NewFolderCommand( RepositoryFile parentFolder ) {
    this.parentFolder = parentFolder;
  }

  public String getSolutionPath() {
    return solutionPath;
  }

  public void setSolutionPath( String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  protected void performOperation() {
    if ( this.getSolutionPath() != null ) {
      final SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();

      if ( callback == null ) {
        setCallback( new ICallback<String>() {
          public void onHandle( String path ) {
            sbp.getSolutionTree().select( path );
          }
        } );
      }

      sbp.getFile( this.getSolutionPath(), new SolutionFileHandler() {
        @Override
        public void handle( RepositoryFile repositoryFile ) {
          NewFolderCommand.this.parentFolder = repositoryFile;
          performOperation( true );
        }
      } );
    } else {
      performOperation( true );
    }
  }

  protected void performOperation( boolean feedback ) {
    final SolutionFolderActionEvent event = new SolutionFolderActionEvent();
    event.setAction( this.getClass().getName() );

    final TextBox folderNameTextBox = new TextBox();
    folderNameTextBox.setVisibleLength( 40 );

    VerticalPanel vp = new VerticalFlexPanel();
    vp.add( new Label( Messages.getString( "newFolderName" ) ) ); //$NON-NLS-1$
    vp.add( folderNameTextBox );
    final PromptDialogBox newFolderDialog =
        new PromptDialogBox(
            Messages.getString( "newFolder" ), Messages.getString( "ok" ), Messages.getString( "cancel" ), false, true, vp ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    newFolderDialog.setResponsive( true );
    newFolderDialog.setMinimumHeightCategory( DialogBox.DialogMinimumHeightCategory.CONTENT );

    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
        newFolderDialog.hide();
      }

      public void okPressed() {

        if ( !NameUtils.isValidFolderName( folderNameTextBox.getText() ) ) {
          event.setMessage( Messages.getString( "containsIllegalCharacters", folderNameTextBox.getText() ) );
          EventBusUtil.EVENT_BUS.fireEvent( event );
          return;
        }

        solutionPath = parentFolder.getPath() + "/" + folderNameTextBox.getText();

        String createDirUrl = contextURL + "api/repo/dirs/" + SolutionBrowserPanel.pathToId( solutionPath ); //$NON-NLS-1$
        RequestBuilder createDirRequestBuilder = new RequestBuilder( RequestBuilder.PUT, createDirUrl );

        try {
          createDirRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
          createDirRequestBuilder.sendRequest( "", new RequestCallback() {

            @Override
            public void onError( Request createFolderRequest, Throwable exception ) {
              MessageDialogBox dialogBox =
                  new MessageDialogBox(
                      Messages.getString( "error" ), Messages.getString( "couldNotCreateFolder", folderNameTextBox.getText() ), //$NON-NLS-1$ //$NON-NLS-2$
                      false, false, true );
              dialogBox.center();
              event.setMessage( Messages.getString( "couldNotCreateFolder", folderNameTextBox.getText() ) );
              EventBusUtil.EVENT_BUS.fireEvent( event );
            }

            @Override
            public void onResponseReceived( Request createFolderRequest, Response createFolderResponse ) {
              if ( createFolderResponse.getStatusCode() == 200 ) {
                NewFolderCommand.this.callback.onHandle( solutionPath );
                new RefreshRepositoryCommand().execute( false );
                event.setMessage( "Success" );
                FileChooserDialog.setIsDirty( Boolean.TRUE );
                setBrowseRepoDirty( Boolean.TRUE );
                EventBusUtil.EVENT_BUS.fireEvent( event );
              } else {
                event.setMessage(
                    StringUtils.isEmpty( createFolderResponse.getText() )
                        || Messages.getString( createFolderResponse.getText() ) == null
                        ? Messages.getString( "couldNotCreateFolder", folderNameTextBox.getText() ) //$NON-NLS-1$
                        : Messages.getString( createFolderResponse.getText(), folderNameTextBox.getText() )
                );
                EventBusUtil.EVENT_BUS.fireEvent( event );
              }
            }
          } );
        } catch ( RequestException e ) {
          Window.alert( e.getLocalizedMessage() );
          event.setMessage( e.getLocalizedMessage() );
          EventBusUtil.EVENT_BUS.fireEvent( event );
        }
      }
    };
    newFolderDialog.setCallback( callback );
    newFolderDialog.center();
  }

  public ICallback<String> getCallback() {
    return callback;
  }

  public void setCallback( ICallback<String> callback ) {
    this.callback = callback;
  }

  private static native void setBrowseRepoDirty( boolean isDirty )
  /*-{
    $wnd.mantle_isBrowseRepoDirty=isDirty;
  }-*/;
}
