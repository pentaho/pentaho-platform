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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.MantleUtils;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.messages.Messages;

import java.util.Date;

public class ContentCleanerPanel extends DockPanel implements ISysAdminPanel {

  private static ContentCleanerPanel instance = new ContentCleanerPanel();
  private static final long DAY_IN_MILLIS = 24L * 60L * 60L * 1000L;
  private String scheduleTextBoxValue = null;

  /**
   * Use get instance for use in Admin, otherwise use constructor
   * 
   * @return singleton ContentCleanerPanel
   */
  public static ContentCleanerPanel getInstance() {
    return instance;
  }

  public ContentCleanerPanel() {
    setupNativeHooks( this );
    setStyleName( "pentaho-admin-panel" );
    activate();
  }

  public void activate() {
    clear();

    RequestBuilder scheduleFileRequestBuilder =
        new RequestBuilder( RequestBuilder.GET, MantleUtils.getSchedulerPluginContextURL() + "api/scheduler/getContentCleanerJob?cb="
            + System.currentTimeMillis() );
    scheduleFileRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    scheduleFileRequestBuilder.setHeader( "Content-Type", "application/json" ); //$NON-NLS-1$//$NON-NLS-2$
    scheduleFileRequestBuilder.setHeader( "accept", "application/json" ); //$NON-NLS-1$//$NON-NLS-2$

    try {
      scheduleFileRequestBuilder.sendRequest( "", new RequestCallback() {
        public void onError( Request request, Throwable exception ) {
        }

        public void onResponseReceived( Request request, Response response ) {
          final TextBox nowTextBox = new TextBox();
          nowTextBox.setWidth( "24px" );
          nowTextBox.getElement().getStyle().setPadding( 5, Unit.PX );
          nowTextBox.getElement().getStyle().setMarginLeft( 5, Unit.PX );
          nowTextBox.getElement().getStyle().setMarginRight( 5, Unit.PX );
          final TextBox scheduleTextBox = new TextBox();
          scheduleTextBox.setVisibleLength( 4 );

          scheduleTextBoxValue = processScheduleTextBoxValue( JsonUtils.escapeJsonForEval( response.getText() ) );
          if ( scheduleTextBoxValue != null ) {
            scheduleTextBox.setValue( scheduleTextBoxValue );
          } else {
            scheduleTextBox.setText( "180" );
          }

          scheduleTextBox.addChangeHandler( new ChangeHandler() {
            public void onChange( ChangeEvent event ) {
              processScheduleTextBoxChangeHandler( scheduleTextBoxValue );
            }
          } );

          Label settingsLabel = new Label( Messages.getString( "settings" ) );
          settingsLabel.setStyleName( "pentaho-fieldgroup-major" );
          add( settingsLabel, DockPanel.NORTH );

          VerticalPanel nowPanelWrapper = new VerticalPanel();
          Label deleteNowLabel = new Label( Messages.getString( "deleteGeneratedFilesNow" ) );
          deleteNowLabel.getElement().getStyle().setPaddingTop( 15, Unit.PX );
          deleteNowLabel.setStyleName( "pentaho-fieldgroup-minor" );
          nowPanelWrapper.add( deleteNowLabel );

          HorizontalPanel nowLabelPanel = new HorizontalPanel();
          nowLabelPanel.getElement().getStyle().setPaddingTop( 10, Unit.PX );
          nowLabelPanel.getElement().getStyle().setPaddingBottom( 10, Unit.PX );

          Label deleteGeneratedFilesOlderThan = new Label( Messages.getString( "deleteGeneratedFilesOlderThan" ) );
          deleteGeneratedFilesOlderThan.getElement().getStyle().setPaddingTop( 7, Unit.PX );
          nowLabelPanel.add( deleteGeneratedFilesOlderThan );

          nowLabelPanel.add( nowTextBox );
          nowTextBox.setText( "180" );
          Label days = new Label( Messages.getString( "daysDot" ) );
          days.getElement().getStyle().setPaddingTop( 7, Unit.PX );
          nowLabelPanel.add( days );
          Button deleteNowButton = new Button( Messages.getString( "deleteNow" ) );
          deleteNowButton.setStylePrimaryName( "pentaho-button" );
          deleteNowButton.addStyleName( "first" );
          deleteNowButton.addClickHandler( new ClickHandler() {
            public void onClick( ClickEvent event ) {
              MessageDialogBox warning =
                new MessageDialogBox( Messages.getString( "deleteNow" ),
                  Messages.getString( "confirmDeleteGeneratedContentNow" ),
                  false, false, true, Messages.getString( "yes" ), null, Messages.getString( "no" ) );
              warning.setCallback( new IDialogCallback() {
                @Override
                public void okPressed() {
                  deleteContentNow( Long.parseLong( nowTextBox.getValue() ) * DAY_IN_MILLIS );
                }

                @Override
                public void cancelPressed() {
                }
              } );
              warning.center();
            }
          } );
          nowPanelWrapper.add( nowLabelPanel );
          nowPanelWrapper.add( deleteNowButton );
          add( nowPanelWrapper, DockPanel.NORTH );

          // scheduled
          VerticalPanel scheduledPanel = new VerticalPanel();
          Label deleteScheduleLabel = new Label( Messages.getString( "scheduleDeletionOfGeneratedFiles" ) );
          deleteScheduleLabel.setStyleName( "pentaho-fieldgroup-minor" );
          deleteScheduleLabel.getElement().getStyle().setPaddingTop( 15, Unit.PX );
          scheduledPanel.add( deleteScheduleLabel );

          Label descLabel;
          boolean fakeJob = isFakeJob();
          if ( !fakeJob ) {
            String desc = getJobDescription();
            descLabel = new Label( desc );
            scheduledPanel.add( descLabel );
          } else {
            descLabel = new Label( Messages.getString( "generatedFilesAreNotScheduledToBeDeleted" ) );
            scheduledPanel.add( descLabel );
          }
          descLabel.getElement().getStyle().setPaddingTop( 10, Unit.PX );
          descLabel.getElement().getStyle().setPaddingBottom( 10, Unit.PX );

          Button editScheduleButton = new Button( Messages.getString( "edit" ) );
          if ( fakeJob ) {
            editScheduleButton.setText( Messages.getString( "scheduleDeletion" ) );
          }
          Button deleteScheduleButton = new Button( Messages.getString( "cancelSchedule" ) );
          deleteScheduleButton.setStylePrimaryName( "pentaho-button" );
          deleteScheduleButton.addStyleName( "last" );
          deleteScheduleButton.addClickHandler( new ClickHandler() {
            public void onClick( ClickEvent event ) {
              deleteContentCleaner();
            }
          } );
          editScheduleButton.setStylePrimaryName( "pentaho-button" );
          editScheduleButton.addStyleName( "first" );
          editScheduleButton.addClickHandler( new ClickHandler() {
            public void onClick( ClickEvent event ) {
              createScheduleRecurrenceDialog( scheduleTextBoxValue, Messages.getString( "deleteGeneratedFilesOlderThan" ), Messages.getString( "daysUsingTheFollowingRules" ) );
            }
          } );
          HorizontalPanel scheduleButtonPanel = new HorizontalPanel();
          scheduleButtonPanel.add( editScheduleButton );
          if ( !fakeJob ) {
            scheduleButtonPanel.add( deleteScheduleButton );
          }
          scheduledPanel.add( scheduleButtonPanel );
          add( scheduledPanel, DockPanel.NORTH );

          VerticalPanel fillPanel = new VerticalPanel();
          add( fillPanel, DockPanel.NORTH );
          fillPanel.getElement().getParentElement().getStyle().setHeight( 100, Unit.PCT );
        }
      } );
    } catch ( RequestException re ) {
      //ignored
    }

  }

  public String getId() {
    return "contentCleanerPanel";
  }

  public void passivate( AsyncCallback<Boolean> passivateCallback ) {
    passivateCallback.onSuccess( true );
  }

  /**
   * @param age
   *          in milliseconds
   */
  public void deleteContentNow( long age ) {

    showLoadingIndicator();

    String date = DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( new Date() );
    String json =
        "{\"jobName\": \"Content Cleaner\", \"actionClass\": \"org.pentaho.platform.admin.GeneratedContentCleaner\","
          + " \"jobParameters\":[ { \"name\": \"age\", \"stringValue\": \""
            + age
            + "\", \"type\": \"string\" }], \"simpleJobTrigger\": { \"endTime\": null, \"repeatCount\": \"0\", "
          + "\"repeatInterval\": \"0\", \"startTime\": \"" + date + "\", \"uiPassParam\": \"RUN_ONCE\"} }";

    RequestBuilder scheduleFileRequestBuilder =
        new RequestBuilder( RequestBuilder.POST, MantleUtils.getSchedulerPluginContextURL() + "api/scheduler/job" );
    scheduleFileRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    scheduleFileRequestBuilder.setHeader( "Content-Type", "application/json" ); //$NON-NLS-1$//$NON-NLS-2$
    try {
      scheduleFileRequestBuilder.sendRequest( json, new RequestCallback() {
        public void onError( Request request, Throwable exception ) {
        }

        public void onResponseReceived( Request request, Response response ) {
          String jobId = response.getText();
          final RequestBuilder requestBuilder =
            new RequestBuilder( RequestBuilder.GET, MantleUtils.getSchedulerPluginContextURL()
              + "api/scheduler/jobinfo?jobId=" + URL.encodeQueryString( jobId ) );
          requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
          requestBuilder.setHeader( "Content-Type", "application/json" ); //$NON-NLS-1$//$NON-NLS-2$
          requestBuilder.setHeader( "accept", "application/json" ); //$NON-NLS-1$//$NON-NLS-2$

          // create a timer to check if the job has finished
          Timer t = new Timer() {
            public void run() {
              try {
                requestBuilder.sendRequest( null, new RequestCallback() {

                  @Override
                  public void onResponseReceived( Request request, Response response ) {
                    // if we're receiving a correct job info, then the job is still executing.
                    // once the job is finished, it is removed from scheduler and we should receive a 404 code.
                    if ( response.getStatusCode() != Response.SC_OK ) {
                      hideLoadingIndicator();
                      cancel();
                    }
                  }

                  @Override
                  public void onError( Request request, Throwable throwable ) {
                    hideLoadingIndicator();
                    cancel();
                  }
                } );
              } catch ( RequestException e ) {
                hideLoadingIndicator();
                cancel();
              }
            }
          };
          t.scheduleRepeating( 1000 );
        }
      } );
    } catch ( RequestException re ) {
      hideLoadingIndicator();
    }
  }

  private void deleteContentCleaner() {
    if ( getJobId() == null ) {
      activate();
      return;
    }
    final String url = MantleUtils.getSchedulerPluginContextURL() + "api/scheduler/removeJob"; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder( RequestBuilder.DELETE, url );
    builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    builder.setHeader( "Content-Type", "application/json" ); //$NON-NLS-1$//$NON-NLS-2$

    JSONObject startJobRequest = new JSONObject();
    startJobRequest.put( "jobId", new JSONString( getJobId() ) ); //$NON-NLS-1$

    try {
      builder.sendRequest( startJobRequest.toString(), new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
          activate();
        }

        public void onResponseReceived( Request request, Response response ) {
          activate();
        }
      } );
    } catch ( RequestException re ) {
      Window.alert( re.getMessage() );
    }
  }

  private static void showLoadingIndicator() {
    WaitPopup.getInstance().setVisible( true );
  }

  private static void hideLoadingIndicator() {
    WaitPopup.getInstance().setVisible( false );
  }

  private native String processScheduleTextBoxValue( String jsonJobString )/*-{
   $wnd.pho.processScheduleTextBoxValue( jsonJobString );
  }-*/;

  private native void processScheduleTextBoxChangeHandler( String scheduleTextBoxValue )/*-{
   $wnd.pho.processScheduleTextBoxChangeHandler( scheduleTextBoxValue );
  }-*/;

  private native boolean isFakeJob()/*-{
   return $wnd.pho.isFakeJob();
  }-*/;

  private native String getJobDescription()/*-{
   return $wnd.pho.getJobDescription();
  }-*/;

  private native String getJobId()/*-{
   return $wnd.pho.getJobId();
  }-*/;

  private native void createScheduleRecurrenceDialog( String scheduleValue, String olderThanLabel, String daysLabel ) /*-{
   $wnd.pho.createScheduleRecurrenceDialog( scheduleValue, olderThanLabel, daysLabel );
  }-*/;

  private static native void setupNativeHooks( ContentCleanerPanel panel )
  /*-{
    $wnd.mantle.deleteContentCleaner = function() {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      panel.@org.pentaho.mantle.client.admin.ContentCleanerPanel::deleteContentCleaner()();
    }
  }-*/;
}
