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

package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleRecurrenceDialog;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.workspace.JsJob;
import org.pentaho.mantle.client.workspace.JsJobParam;

import java.util.Date;

public class ContentCleanerPanel extends DockPanel implements ISysAdminPanel {

  private static ContentCleanerPanel instance = new ContentCleanerPanel();

  /**
   * Use get instance for use in Admin, otherwise use constructor
   * 
   * @return singleton ContentCleanerPanel
   */
  public static ContentCleanerPanel getInstance() {
    return instance;
  }

  public ContentCleanerPanel() {
    setStyleName( "pentaho-admin-panel" );
    activate();
  }

  public void activate() {
    clear();
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

    RequestBuilder scheduleFileRequestBuilder =
        new RequestBuilder( RequestBuilder.GET, contextURL + "api/scheduler/getContentCleanerJob?cb="
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

          JsJob tmpJsJob = parseJsonJob( JsonUtils.escapeJsonForEval( response.getText() ) );

          boolean fakeJob = false;
          if ( tmpJsJob == null ) {
            tmpJsJob = createJsJob();
            fakeJob = true;
          }
          final JsJob jsJob = tmpJsJob;

          if ( jsJob != null ) {
            scheduleTextBox.setValue( "" + ( Long.parseLong( jsJob.getJobParamValue( "age" ) ) / 86400L ) );
          } else {
            scheduleTextBox.setText( "180" );
          }
          scheduleTextBox.addChangeHandler( new ChangeHandler() {
            public void onChange( ChangeEvent event ) {
              if ( jsJob != null ) {
                JsArray<JsJobParam> params = jsJob.getJobParams();
                for ( int i = 0; i < params.length(); i++ ) {
                  if ( params.get( i ).getName().equals( "age" ) ) {
                    params.get( i ).setValue( "" + ( Long.parseLong( scheduleTextBox.getText() ) * 86400L ) );
                    break;
                  }
                }
              }
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
              deleteContentNow( Long.parseLong( nowTextBox.getValue() ) * 86400L );
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
          if ( !fakeJob ) {
            String desc = jsJob.getJobTrigger().getDescription();
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
              deleteContentCleaner( jsJob );
            }
          } );
          editScheduleButton.setStylePrimaryName( "pentaho-button" );
          editScheduleButton.addStyleName( "first" );
          editScheduleButton.addClickHandler( new ClickHandler() {
            public void onClick( ClickEvent event ) {
              IDialogCallback callback = new IDialogCallback() {
                public void okPressed() {
                  deleteContentCleaner( jsJob );
                }

                public void cancelPressed() {
                }
              };

              HorizontalPanel scheduleLabelPanel = new HorizontalPanel();
              scheduleLabelPanel.add( new Label( Messages.getString( "deleteGeneratedFilesOlderThan" ), false ) );
              scheduleLabelPanel.add( scheduleTextBox );
              scheduleLabelPanel.add( new Label( Messages.getString( "daysUsingTheFollowingRules" ), false ) );
              ScheduleRecurrenceDialog editSchedule =
                  new ScheduleRecurrenceDialog( null, jsJob, callback, false, false,
                      AbstractWizardDialog.ScheduleDialogType.SCHEDULER );
              editSchedule.setShowSuccessDialog( false );
              editSchedule.addCustomPanel( scheduleLabelPanel, DockPanel.NORTH );
              editSchedule.center();
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
    String date = DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 ).format( new Date() );
    String json =
        "{\"jobName\": \"Content Cleaner\", \"actionClass\": \"org.pentaho.platform.admin.GeneratedContentCleaner\","
          + " \"jobParameters\":[ { \"name\": \"age\", \"stringValue\": \""
            + age
            + "\", \"type\": \"string\" }], \"simpleJobTrigger\": { \"endTime\": null, \"repeatCount\": \"0\", "
          + "\"repeatInterval\": \"0\", \"startTime\": \"" + date + "\", \"uiPassParam\": \"RUN_ONCE\"} }";

    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

    RequestBuilder scheduleFileRequestBuilder =
        new RequestBuilder( RequestBuilder.POST, contextURL + "api/scheduler/job" );
    scheduleFileRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    scheduleFileRequestBuilder.setHeader( "Content-Type", "application/json" ); //$NON-NLS-1$//$NON-NLS-2$
    try {
      scheduleFileRequestBuilder.sendRequest( json, new RequestCallback() {
        public void onError( Request request, Throwable exception ) {
        }

        public void onResponseReceived( Request request, Response response ) {
        }
      } );
    } catch ( RequestException re ) {
      //ignored
    }
  }

  private final native JsJob parseJsonJob( String json )
  /*-{
    window.top.jobjson = json;
    if (null == json || "" == json) {
      return null;
    }
    var obj = eval('(' + json + ')');
    return obj;
  }-*/;

  private final native JsJob createJsJob()
  /*-{
    var jsJob = new Object();
    jsJob.jobParams = new Object();
    jsJob.jobParams.jobParams = [];
    jsJob.jobParams.jobParams[0] = new Object();
    jsJob.jobParams.jobParams[0].name = "ActionAdapterQuartzJob-ActionClass";
    jsJob.jobParams.jobParams[0].value = "org.pentaho.platform.admin.GeneratedContentCleaner";
    jsJob.jobParams.jobParams[1] = new Object();
    jsJob.jobParams.jobParams[1].name = "age";
    jsJob.jobParams.jobParams[1].value = "15552000";
    jsJob.jobTrigger = new Object();
    jsJob.jobTrigger['@type'] = "simpleJobTrigger";
    jsJob.jobTrigger.repeatCount = -1;
    jsJob.jobTrigger.repeatInterval = 86400;
    jsJob.jobTrigger.scheduleType = "DAILY";
    //jsJob.jobTrigger.startTime = "2013-03-22T09:35:52.276-04:00";
    jsJob.jobName = "GeneratedContentCleaner";
    return jsJob;
  }-*/;

  private void deleteContentCleaner( JsJob jsJob ) {
    if ( jsJob == null || StringUtils.isEmpty( jsJob.getJobId() ) ) {
      activate();
      return;
    }
    final String url = GWT.getHostPageBaseURL() + "api/scheduler/removeJob"; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder( RequestBuilder.DELETE, url );
    builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    builder.setHeader( "Content-Type", "application/json" ); //$NON-NLS-1$//$NON-NLS-2$

    JSONObject startJobRequest = new JSONObject();
    startJobRequest.put( "jobId", new JSONString( jsJob.getJobId() ) ); //$NON-NLS-1$

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

}
