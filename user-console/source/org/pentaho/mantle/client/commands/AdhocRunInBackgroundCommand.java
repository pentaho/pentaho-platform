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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.commands;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.utils.NameUtils;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleOutputLocationDialog;
import org.pentaho.mantle.client.events.SolutionFileHandler;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

public class AdhocRunInBackgroundCommand extends RunInBackgroundCommand {


  public AdhocRunInBackgroundCommand() {
  }

  private String jobId = null;

  private String recalculateFinished = null;

  private String solutionPath = null;

  @Override
  public String getSolutionPath() {
    return solutionPath;
  }

  @Override
  public void setSolutionPath( String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  public void setJobId( String jobId ) {
    this.jobId = jobId;
  }

  public String getJobId() {
    return jobId;
  }

  public String getRecalculateFinished() {
    return recalculateFinished;
  }

  public void setRecalculateFinished( String recalculateFinished ) {
    this.recalculateFinished = recalculateFinished;
  }

  @Override
  protected void performOperation() {
    final SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
    if ( this.getSolutionPath() != null ) {
      sbp.getFile( this.getSolutionPath(), new SolutionFileHandler() {
        @Override
        public void handle( RepositoryFile repositoryFile ) {
          showDialog( true );
        }
      } );
    } else {
      performOperation( true );
    }
  }

  @Override
  protected void showDialog( final boolean feedback ) {
    final ScheduleOutputLocationDialog outputLocationDialog = new ScheduleOutputLocationDialog( getSolutionPath() ) {
      @Override
      protected void onSelect( final String name, final String outputPath ) {
        setOutputName( name );
        setOutputLocationPath( outputPath );
        performOperation( feedback );
      }

      @Override protected void onCancel() {
        super.onCancel();
        AdhocRunInBackgroundCommand.onCancel();
      }

      @Override protected void onOk() {
        super.onOk();
        AdhocRunInBackgroundCommand.onOk( getOutputLocationPath() );
      }

      @Override protected void onAttach() {
        super.onAttach();
        AdhocRunInBackgroundCommand.onAttach();
      }
    };

    outputLocationDialog.setOkButtonText( Messages.getString( "ok" ) );
    outputLocationDialog.setScheduleNameText( Messages.getString( "scheduleNameColonReportviewer" ) );
    outputLocationDialog.center();
  }

  public static native void onCancel()
  /*-{
    $wnd.mantle_fireEvent('GenericEvent', {eventSubType: 'locationPromptCanceled'});
  }-*/;

  public static native void onOk( final String outputPath )
  /*-{
    $wnd.mantle_fireEvent('GenericEvent', {eventSubType: 'locationPromptOk', stringParam: outputPath});
  }-*/;

  public static native void onAttach()
  /*-{
git  }-*/;

  private static native void onFinished( String uuid )/*-{
    $wnd.mantle_fireEvent('GenericEvent', {eventSubType: 'locationPromptFinish', stringParam: uuid});
  }-*/;

  private RequestBuilder createTreeRequest() {
    RequestBuilder scheduleFileRequestBuilder = new RequestBuilder( RequestBuilder.GET, contextURL + "api/repo/files/"
      + NameUtils.encodeRepositoryPath( getOutputLocationPath() ) + "/tree?depth=1" );
    scheduleFileRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    return scheduleFileRequestBuilder;
  }

  @Override
  protected void performOperation( boolean feedback ) {

    RequestBuilder treeRequestBuilder = createTreeRequest();

    try {
      treeRequestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          AdhocRunInBackgroundCommand.this.onError( exception );
        }

        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == Response.SC_OK ) {
            RequestBuilder scheduleFileRequestBuilder = parseFolderId( response );

            try {
              scheduleFileRequestBuilder.sendRequest( "", new RequestCallback() {

                @Override
                public void onError( Request request, Throwable exception ) {
                  new MessageDialogBox(
                    Messages.getString( "error" ), exception.toString(), false, false, true );
                }

                @Override
                public void onResponseReceived( Request request, Response response ) {
                  if ( response.getStatusCode() != Response.SC_OK ) {
                    AdhocRunInBackgroundCommand.this.onError( response );
                  } else {
                    try {
                      onFinished( JSONParser.
                        parseStrict( response.getText() ).isObject()
                        .get( "uuid" ).isString().stringValue() );
                    } catch ( Exception e ) {
                      AdhocRunInBackgroundCommand.this.onError( e );
                    }
                  }
                }

              } );
            } catch ( RequestException e ) {
              AdhocRunInBackgroundCommand.this.onError( e );
            }
          } else {
            AdhocRunInBackgroundCommand.this.onError( response );
          }
        }

      } );
    } catch ( RequestException e ) {
      AdhocRunInBackgroundCommand.this.onError( e );
    }
  }

  private RequestBuilder parseFolderId( Response response ) {
    String folderId = null;
    Document repository = XMLParser.parse( response.getText() );
    NodeList fileNodeList = repository.getElementsByTagName( "file" );
    for ( int i = 0; i < fileNodeList.getLength(); i++ ) {
      Element element = (Element) fileNodeList.item( i );
      Node pathNode = element.getElementsByTagName( "path" ).item( 0 );

      if ( getOutputLocationPath().equals( pathNode.getFirstChild().getNodeValue() ) ) {
        folderId = element.getElementsByTagName( "id" ).item( 0 ).getFirstChild().getNodeValue();
      }
    }

    RequestBuilder scheduleFileRequestBuilder =
      new RequestBuilder( RequestBuilder.POST, contextURL + "plugin/reporting/api/jobs/"
        + jobId + "/schedule?confirm=true&folderId=" + folderId + "&newName=" + getOutputName() + "&recalculateFinished=" + getRecalculateFinished() );
    scheduleFileRequestBuilder.setHeader( "Content-Type", "application/json" );
    scheduleFileRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    return scheduleFileRequestBuilder;
  }

  private void onError( Response response ) {
    MessageDialogBox dialogBox =
      new MessageDialogBox(
        Messages.getString( "error" ),
        Messages.getString( "serverErrorColon" ) + " " + response.getStatusCode(), false, false,
        true );
    dialogBox.center();
  }

  private void onError( Throwable exception ) {
    MessageDialogBox dialogBox =
      new MessageDialogBox( Messages.getString( "error" ), exception.toString(), false, false,
        true );
    dialogBox.center();
  }
}
