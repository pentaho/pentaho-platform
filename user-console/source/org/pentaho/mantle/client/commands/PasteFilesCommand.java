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
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.mantle.client.dialogs.OverwritePromptDialog;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFolderActionEvent;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserClipboard;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserFile;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.messages.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wseyler
 */
public class PasteFilesCommand extends AbstractCommand {
  /**
   *
   */
  private static final String NAME_NODE_TAG = "name"; //$NON-NLS-1$

  private String solutionPath = null;
  String moduleBaseURL = GWT.getModuleBaseURL();
  String moduleName = GWT.getModuleName();
  String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

  private final SolutionFolderActionEvent event = new SolutionFolderActionEvent( this.getClass().getName() );

  /**
   *
   */
  public PasteFilesCommand() {
  }

  public String getSolutionPath() {
    return solutionPath;
  }

  public void setSolutionPath( String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  /*
               * (non-Javadoc)
               *
               * @see org.pentaho.mantle.client.commands.AbstractCommand#performOperation()
               */
  @Override
  protected void performOperation() {
    performOperation( false );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.mantle.client.commands.AbstractCommand#performOperation(boolean)
   */
  @Override
  protected void performOperation( boolean feedback ) {
    final SolutionBrowserClipboard clipBoard = SolutionBrowserClipboard.getInstance();
    @SuppressWarnings ( "unchecked" )
    final List<SolutionBrowserFile> clipboardFileItems = clipBoard.getClipboardItems();

    if ( clipboardFileItems != null && clipboardFileItems.size() > 0 && getSolutionPath() != null ) {
      String getChildrenUrl =
          contextURL
              + "api/repo/files/" + SolutionBrowserPanel.pathToId( getSolutionPath() ) + "/tree?depth=1"; //$NON-NLS-1$ //$NON-NLS-2$
      RequestBuilder childrenRequestBuilder = new RequestBuilder( RequestBuilder.GET, getChildrenUrl );
      try {
        childrenRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
        childrenRequestBuilder.sendRequest( null, new RequestCallback() {

          @Override
          public void onError( Request getChildrenRequest, Throwable exception ) {
            Window.alert( exception.getLocalizedMessage() );
            event.setMessage( exception.getLocalizedMessage() );
            EventBusUtil.EVENT_BUS.fireEvent( event );
          }

          @Override
          public void onResponseReceived( Request getChildrenRequest, Response getChildrenResponse ) {
            event.setMessage( "Click" );
            EventBusUtil.EVENT_BUS.fireEvent( event );
            boolean cutSameDir = false;
            if ( getChildrenResponse.getStatusCode() >= 200 && getChildrenResponse.getStatusCode() < 300 ) {
              boolean promptForOptions = false;
              Document children = XMLParser.parse( getChildrenResponse.getText() );
              NodeList childrenNameNodes = children.getElementsByTagName( NAME_NODE_TAG );
              List<String> childNames = new ArrayList<String>();
              for ( int i = 0; i < childrenNameNodes.getLength(); i++ ) {
                Node childNameNode = childrenNameNodes.item( i );
                childNames.add( childNameNode.getFirstChild().getNodeValue() );
              }

              for ( SolutionBrowserFile file : clipboardFileItems ) {
                if ( file.getPath() != null ) {
                  String pasteFileParentPath = file.getPath();
                  String fileNameWithExt = pasteFileParentPath.substring( pasteFileParentPath.lastIndexOf( "/" ) + 1, pasteFileParentPath.length() ); //$NON-NLS-1$
                  pasteFileParentPath = pasteFileParentPath.substring( 0, pasteFileParentPath.lastIndexOf( "/" ) ); //$NON-NLS-1$
                  if ( childNames.contains( fileNameWithExt )
                      && !getSolutionPath().equals( pasteFileParentPath ) ) {
                    promptForOptions = true;
                    break;
                  } else if ( childNames.contains( fileNameWithExt )
                      && getSolutionPath().equals( pasteFileParentPath )
                      && SolutionBrowserClipboard.getInstance().getClipboardAction() == SolutionBrowserClipboard.ClipboardAction.CUT ) {
                    cutSameDir = true;
                    break;
                  }
                }
              }
              if ( promptForOptions ) {
                final OverwritePromptDialog overwriteDialog = new OverwritePromptDialog();
                final IDialogCallback callback = new IDialogCallback() {
                  public void cancelPressed() {
                    event.setMessage( "Cancel" );
                    EventBusUtil.EVENT_BUS.fireEvent( event );
                    overwriteDialog.hide();
                  }

                  public void okPressed() {
                    performSave( clipBoard, overwriteDialog.getOverwriteMode() );
                  }
                };
                overwriteDialog.setCallback( callback );
                overwriteDialog.center();
              } else {
                if ( !cutSameDir ) {
                  performSave( clipBoard, 2 );
                } else {
                  event.setMessage( "Cancel" );
                  EventBusUtil.EVENT_BUS.fireEvent( event );
                }
              }
            } else {
              Window.alert( getChildrenResponse.getText() );
            }
          }

        } );
      } catch ( RequestException e ) {
        Window.alert( e.getLocalizedMessage() );
      }
    }
  }

  void performSave( final SolutionBrowserClipboard clipBoard, Integer overwriteMode ) {

    @SuppressWarnings ( "unchecked" )
    final List<SolutionBrowserFile> clipboardFileItems = clipBoard.getClipboardItems();
    String temp = ""; //$NON-NLS-1$
    for ( SolutionBrowserFile file : clipboardFileItems ) {
      temp += file.getId() + ","; //$NON-NLS-1$
    }
    // remove trailing ","
    temp = temp.substring( 0, temp.length() - 1 );
    final String filesList = temp;

    String copyUrl = contextURL
        + "api/repo/files/" + SolutionBrowserPanel.pathToId( getSolutionPath() ) + "/children?mode=" + overwriteMode; //$NON-NLS-1$//$NON-NLS-2$

    String moveUrl = contextURL
        + "api/repo/files/" + SolutionBrowserPanel.pathToId( getSolutionPath() ) + "/move"; //$NON-NLS-1$//$NON-NLS-2$

    RequestBuilder pasteChildrenRequestBuilder = new RequestBuilder( RequestBuilder.PUT, ( SolutionBrowserClipboard.getInstance()
        .getClipboardAction() == SolutionBrowserClipboard.ClipboardAction.CUT ) ? moveUrl : copyUrl );
    pasteChildrenRequestBuilder.setHeader( "Content-Type", "text/plain" ); //$NON-NLS-1$//$NON-NLS-2$
    pasteChildrenRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      pasteChildrenRequestBuilder.sendRequest( filesList, new RequestCallback() {

        @Override
        public void onError( Request pasteChildrenRequest, Throwable exception ) {
          Window.alert( exception.getLocalizedMessage() );
          event.setMessage( exception.getLocalizedMessage() );
          EventBusUtil.EVENT_BUS.fireEvent( event );
        }

        @Override
        public void onResponseReceived( Request pasteChildrenRequest, Response pasteChildrenResponse ) {
          switch ( pasteChildrenResponse.getStatusCode() ) {
            case Response.SC_OK:
              event.setMessage( "Success" );
              EventBusUtil.EVENT_BUS.fireEvent( event );
              FileChooserDialog.setIsDirty( Boolean.TRUE );
              setBrowseRepoDirty( Boolean.TRUE );
              //This will allow for multiple paste presses after a cut/paste.
              SolutionBrowserClipboard.getInstance().setClipboardAction( SolutionBrowserClipboard.ClipboardAction.COPY );
              break;
            case Response.SC_FORBIDDEN:
              event.setMessage( Messages.getString( "pasteFilesCommand.accessDenied" ) );
              EventBusUtil.EVENT_BUS.fireEvent( event );
              break;
            default:
              event.setMessage( pasteChildrenResponse.getText() );
              EventBusUtil.EVENT_BUS.fireEvent( event );
              break;
          }
        }

      } );
    } catch ( RequestException e ) {
      Window.alert( e.getLocalizedMessage() );
      event.setMessage( e.getLocalizedMessage() );
      EventBusUtil.EVENT_BUS.fireEvent( event );
    }

  }

  private static native void setBrowseRepoDirty( boolean isDirty )
  /*-{
    $wnd.mantle_isBrowseRepoDirty = isDirty;
  }-*/;
}
