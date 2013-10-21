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

package org.pentaho.mantle.client.dialogs.scheduling;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.commands.AbstractCommand;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileActionEvent;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.login.client.MantleLoginDialog;

public class ScheduleHelper {

  static {
    setupNativeHooks( new ScheduleHelper() );
  }

  private static native void setupNativeHooks( ScheduleHelper scheduleHelper )
  /*-{
    $wnd.mantle_confirmBackgroundExecutionDialog = function(url) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      @org.pentaho.mantle.client.dialogs.scheduling.ScheduleHelper::confirmBackgroundExecutionDialog(Ljava/lang/String;)(url);      
    }
  }-*/;

  private static void showScheduleDialog( final String fileNameWithPath ) {

    final SolutionFileActionEvent event = new SolutionFileActionEvent();
    event.setAction( ScheduleHelper.class.getName() );
    try {

      final String url = GWT.getHostPageBaseURL() + "api/mantle/isAuthenticated"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
      requestBuilder.setHeader( "accept", "text/plain" );
      requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      requestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable caught ) {
          MantleLoginDialog.performLogin( new AsyncCallback<Boolean>() {

            public void onFailure( Throwable caught ) {
            }

            public void onSuccess( Boolean result ) {
              showScheduleDialog( fileNameWithPath );
            }

          } );
        }

        public void onResponseReceived( Request request, Response response ) {
          String moduleBaseURL = GWT.getModuleBaseURL();
          String moduleName = GWT.getModuleName();
          final String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

          RequestBuilder emailValidRequest =
              new RequestBuilder( RequestBuilder.GET, contextURL + "api/emailconfig/isValid" );
          emailValidRequest.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
          emailValidRequest.setHeader( "accept", "text/plain" );
          try {
            emailValidRequest.sendRequest( null, new RequestCallback() {

              public void onError( Request request, Throwable exception ) {
                MessageDialogBox dialogBox =
                    new MessageDialogBox( Messages.getString( "error" ), exception.toString(), false, false, true ); //$NON-NLS-1$
                dialogBox.center();
                event.setMessage( exception.getLocalizedMessage() );
                EventBusUtil.EVENT_BUS.fireEvent( event );
              }

              public void onResponseReceived( Request request, Response response ) {
                if ( response.getStatusCode() == Response.SC_OK ) {
                  final boolean isEmailConfValid = Boolean.parseBoolean( response.getText() );

                  NewScheduleDialog dialog = new NewScheduleDialog( fileNameWithPath, null, isEmailConfValid );
                  dialog.center();

                  event.setMessage( "Open" );
                  EventBusUtil.EVENT_BUS.fireEvent( event );
                }
              }
            } );
          } catch ( RequestException e ) {
            MessageDialogBox dialogBox =
                new MessageDialogBox( Messages.getString( "error" ), e.toString(), false, false, true ); //$NON-NLS-1$
            dialogBox.center();
            event.setMessage( e.getLocalizedMessage() );
            EventBusUtil.EVENT_BUS.fireEvent( event );
          }
        }

      } );
    } catch ( RequestException e ) {
      Window.alert( e.getMessage() );
      event.setMessage( e.getLocalizedMessage() );
      EventBusUtil.EVENT_BUS.fireEvent( event );
    }
  }

  public static void createSchedule( final RepositoryFile repositoryFile ) {
    AbstractCommand scheduleCommand = new AbstractCommand() {

      private void schedule() {
        String extension = ""; //$NON-NLS-1$
        if ( repositoryFile.getPath().lastIndexOf( "." ) > 0 ) { //$NON-NLS-1$
          extension = repositoryFile.getPath().substring( repositoryFile.getPath().lastIndexOf( "." ) + 1 ); //$NON-NLS-1$
        }

        if ( SolutionBrowserPanel.getInstance().getExecutableFileExtensions().contains( extension ) ) {
          showScheduleDialog( repositoryFile.getPath() );
        } else {
          final MessageDialogBox dialogBox =
              new MessageDialogBox(
                  Messages.getString( "open" ), Messages.getString( "scheduleInvalidFileType", repositoryFile.getPath() ), false, false, true ); //$NON-NLS-1$ //$NON-NLS-2$

          dialogBox.setCallback( new IDialogCallback() {
            public void cancelPressed() {
            }

            public void okPressed() {
              dialogBox.hide();
            }
          } );

          dialogBox.center();
          return;
        }

      }

      protected void performOperation() {

        // hit the server and check: isScheduleAllowed
        final String url = GWT.getHostPageBaseURL() + "api/scheduler/isScheduleAllowed?id=" + repositoryFile.getId(); //$NON-NLS-1$
        RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
        requestBuilder.setHeader( "accept", "text/plain" );
        requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
        final MessageDialogBox errorDialog =
            new MessageDialogBox(
                Messages.getString( "error" ), Messages.getString( "noSchedulePermission" ), false, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
        try {
          requestBuilder.sendRequest( null, new RequestCallback() {

            public void onError( Request request, Throwable caught ) {
              errorDialog.center();
            }

            public void onResponseReceived( Request request, Response response ) {
              if ( "true".equalsIgnoreCase( response.getText() ) ) {
                schedule();
              } else {
                errorDialog.center();
              }
            }
          } );
        } catch ( RequestException re ) {
          errorDialog.center();
        }

      }

      protected void performOperation( boolean feedback ) {
        performOperation();
      }

    };
    scheduleCommand.execute();

  }

  /**
   * The passed in URL has all the parameters set for background execution. We simply call GET on the URL and
   * handle the response object. If the response object contains a particular string then we display success
   * message box.
   * 
   * @param url
   *          Complete url with all the parameters set for scheduling a job in the background.
   */
  private static void runInBackground( final String url ) {

    RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
    try {
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      builder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          MessageDialogBox dialogBox =
              new MessageDialogBox(
                  Messages.getString( "error" ), Messages.getString( "couldNotBackgroundExecute" ), false, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
          dialogBox.center();
        }

        public void onResponseReceived( Request request, Response response ) {
          /*
           * We are checking for this specific string because if the job was scheduled successfully by
           * QuartzBackgroundExecutionHelper then the response is an html that contains the specific string. We
           * have coded this way because we did not want to touch the old way.
           */
          if ( "true".equals( response.getHeader( "background_execution" ) ) ) {
            MessageDialogBox dialogBox =
                new MessageDialogBox(
                    Messages.getString( "info" ), Messages.getString( "backgroundJobScheduled" ), false, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
            dialogBox.center();
          }
        }
      } );
    } catch ( RequestException e ) {
      MessageDialogBox dialogBox = new MessageDialogBox( Messages.getString( "error" ), //$NON-NLS-1$
          Messages.getString( "couldNotBackgroundExecute" ), false, false, true ); //$NON-NLS-1$
      dialogBox.center();
    }
  }

  public static void confirmBackgroundExecutionDialog( final String url ) {
    final String title = Messages.getString( "confirm" ); //$NON-NLS-1$
    final String message = Messages.getString( "userParamBackgroundWarning" ); //$NON-NLS-1$
    VerticalPanel vp = new VerticalPanel();
    vp.add( new Label( Messages.getString( message ) ) );

    final PromptDialogBox scheduleInBackground =
        new PromptDialogBox( title, Messages.getString( "yes" ), Messages.getString( "no" ), false, true, vp ); //$NON-NLS-1$ //$NON-NLS-2$

    final IDialogCallback callback = new IDialogCallback() {
      public void cancelPressed() {
        scheduleInBackground.hide();
      }

      public void okPressed() {
        runInBackground( url );
      }
    };
    scheduleInBackground.setCallback( callback );
    scheduleInBackground.center();
  }

}
