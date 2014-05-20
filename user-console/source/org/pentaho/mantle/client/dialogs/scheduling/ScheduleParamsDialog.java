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
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.utils.NameUtils;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog;
import org.pentaho.gwt.widgets.client.wizards.IWizardPanel;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.mantle.client.workspace.JsJob;
import org.pentaho.mantle.client.workspace.JsJobParam;
import org.pentaho.mantle.login.client.MantleLoginDialog;

/**
 * @author wseyler
 * 
 */
public class ScheduleParamsDialog extends AbstractWizardDialog {
  FileItem fileItem = null;
  String moduleBaseURL = GWT.getModuleBaseURL();
  String moduleName = GWT.getModuleName();
  String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );

  IDialogCallback callback;

  ScheduleParamsWizardPanel scheduleParamsWizardPanel;
  ScheduleEmailDialog scheduleEmailDialog;
  ScheduleRecurrenceDialog parentDialog;

  String filePath;

  JSONObject jobSchedule;
  JsJob editJob;
  JSONArray scheduleParams;

  Boolean done = false;
  boolean isEmailConfValid = false;

  public ScheduleParamsDialog( ScheduleRecurrenceDialog parentDialog, boolean isEmailConfValid, JsJob editJob ) {
    super( ScheduleDialogType.SCHEDULER, Messages.getString( "newSchedule" ), null, false, true ); //$NON-NLS-1$
    this.parentDialog = parentDialog;
    filePath = parentDialog.filePath;
    jobSchedule = parentDialog.getSchedule();
    this.isEmailConfValid = isEmailConfValid;
    this.editJob = editJob;
    initDialog();
    if ( isEmailConfValid ) {
      finishButton.setText( Messages.getString( "nextStep" ) );
    }
  }

  public ScheduleParamsDialog( String filePath, JSONObject schedule, boolean isEmailConfValid ) {
    super( ScheduleDialogType.SCHEDULER, Messages.getString( "runInBackground" ), null, false, true ); //$NON-NLS-1$
    this.filePath = filePath;
    jobSchedule = schedule;
    this.isEmailConfValid = isEmailConfValid;
    initDialog();
    if ( isEmailConfValid ) {
      finishButton.setText( Messages.getString( "nextStep" ) );
    }
  }

  @Override
  public boolean onKeyDownPreview( char key, int modifiers ) {
    if ( key == KeyCodes.KEY_ESCAPE ) {
      hide();
    }
    return true;
  }

  public void initDialog() {
    scheduleParamsWizardPanel = new ScheduleParamsWizardPanel( filePath );
    IWizardPanel[] wizardPanels = { scheduleParamsWizardPanel };
    setWizardPanels( wizardPanels );
    setWidth( "800px" );
    String urlPath = URL.encodePathSegment( NameUtils.encodeRepositoryPath( filePath ) );

    String urlParams = "";
    if ( editJob != null ) {
      // add all edit params to URL
      JsArray<JsJobParam> jparams = editJob.getJobParams();
      for ( int i = 0; i < jparams.length(); i++ ) {
        urlParams += i == 0 ? "?" : "&";
        if ( jparams.get( i ).getValue().startsWith( "[" ) && jparams.get( i ).getValue().indexOf( "," ) >= 0 ) {
          // it's an array!
          StringTokenizer st = new StringTokenizer( jparams.get( i ).getValue(), "[]," );
          int tokens = st.countTokens();
          int numParamsAdded = 0;
          for ( int j = 0; j < tokens; j++ ) {
            String token = st.tokenAt( j );
            if ( !StringUtils.isEmpty( token ) ) {
              if ( numParamsAdded > 0 ) {
                urlParams += "&";
              }
              numParamsAdded++;
              urlParams += jparams.get( i ).getName() + "=" + URL.encodeQueryString( token.trim() );
            }
          }
        } else if ( jparams.get( i ).getValue().startsWith( "[" ) && jparams.get( i ).getValue().indexOf( "." ) >= 0 ) {
          // mondrian style param
          urlParams += jparams.get( i ).getName() + "=" + URL.encodeQueryString( jparams.get( i ).getValue().trim() );
        } else if ( jparams.get( i ).getValue().startsWith( "[" ) && jparams.get( i ).getValue().endsWith( "]" ) ) {
          // single value, remove []
          String param = jparams.get( i ).getValue().trim();
          param = param.substring( 1 );
          param = param.substring( 0, param.length() - 1 );
          urlParams += jparams.get( i ).getName() + "=" + URL.encodeQueryString( param );
        } else {
          urlParams += jparams.get( i ).getName() + "=" + URL.encodeQueryString( jparams.get( i ).getValue().trim() );
        }
      }
    }
    setParametersUrl( "api/repos/" + urlPath + "/parameterUi" + urlParams ); //$NON-NLS-1$ //$NON-NLS-2$
    wizardDeckPanel.setHeight( "100%" ); //$NON-NLS-1$

    wizardDeckPanel.getElement().getParentElement().addClassName( "schedule-dialog-content" );
    wizardDeckPanel.getElement().getParentElement().removeClassName( "dialog-content" );

    setSize( "650px", "450px" );
    addStyleName( "schedule-params-dialog" );
  }

  JSONArray getScheduleParams( boolean suppressAlerts ) {
    JsArray<JsSchedulingParameter> schedulingParams = scheduleParamsWizardPanel.getParams( suppressAlerts );
    JSONArray params = new JSONArray();
    for ( int i = 0; i < schedulingParams.length(); i++ ) {
      params.set( i, new JSONObject( schedulingParams.get( i ) ) );
    }
    return params;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#finish()
   */
  @Override
  protected boolean onFinish() {
    scheduleParams = getScheduleParams( false );
    if ( editJob != null ) {
      String lineageId = editJob.getJobParamValue( "lineage-id" );
      JsArrayString lineageIdValue = (JsArrayString) JavaScriptObject.createArray().cast();
      lineageIdValue.push( lineageId );
      JsSchedulingParameter p = (JsSchedulingParameter) JavaScriptObject.createObject().cast();
      p.setName( "lineage-id" );
      p.setType( "string" );
      p.setStringValue( lineageIdValue );
      scheduleParams.set( scheduleParams.size(), new JSONObject( p ) );
    }
    if ( isEmailConfValid ) {
      showScheduleEmailDialog( scheduleParams );
    } else {
      hide();
      JSONObject scheduleRequest = (JSONObject) JSONParser.parseStrict( jobSchedule.toString() );
      scheduleRequest.put( "jobParameters", scheduleParams ); //$NON-NLS-1$    

      RequestBuilder scheduleFileRequestBuilder =
          new RequestBuilder( RequestBuilder.POST, contextURL + "api/scheduler/job" );
      scheduleFileRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      scheduleFileRequestBuilder.setHeader( "Content-Type", "application/json" ); //$NON-NLS-1$//$NON-NLS-2$

      try {
        scheduleFileRequestBuilder.sendRequest( scheduleRequest.toString(), new RequestCallback() {

          @Override
          public void onError( Request request, Throwable exception ) {
            MessageDialogBox dialogBox =
                new MessageDialogBox( Messages.getString( "error" ), exception.toString(), false, false, true ); //$NON-NLS-1$
            dialogBox.center();
            setDone( false );
          }

          @Override
          public void onResponseReceived( Request request, Response response ) {
            if ( response.getStatusCode() == 200 ) {
              setDone( true );
              ScheduleParamsDialog.this.hide();
              if ( callback != null ) {
                callback.okPressed();
              }

              JSONValue rib = jobSchedule.get( "runInBackground" );
              if ( rib != null && rib.isBoolean() != null && rib.isBoolean().booleanValue() ) {
                MessageDialogBox dialogBox =
                    new MessageDialogBox(
                        Messages.getString( "runInBackground" ), Messages.getString( "backgroundExecutionStarted" ), //$NON-NLS-1$ //$NON-NLS-2$
                        false, false, true );
                dialogBox.center();
              } else if ( !PerspectiveManager.getInstance().getActivePerspective().getId().equals(
                  PerspectiveManager.SCHEDULES_PERSPECTIVE ) ) {
                ScheduleCreateStatusDialog successDialog = new ScheduleCreateStatusDialog();
                successDialog.center();
              } else {
                MessageDialogBox dialogBox =
                    new MessageDialogBox(
                        Messages.getString( "scheduleUpdatedTitle" ), Messages.getString( "scheduleUpdatedMessage" ), //$NON-NLS-1$ //$NON-NLS-2$ 
                        false, false, true );
                dialogBox.center();
              }
            } else {
              MessageDialogBox dialogBox = new MessageDialogBox( Messages.getString( "error" ), response.getText(), //$NON-NLS-1$ 
                  false, false, true );
              dialogBox.center();
              setDone( false );
            }
          }

        } );
      } catch ( RequestException e ) {
        MessageDialogBox dialogBox = new MessageDialogBox( Messages.getString( "error" ), e.toString(), //$NON-NLS-1$
            false, false, true );
        dialogBox.center();
        setDone( false );
      }
      setDone( true );
    }
    return false;
  }

  private void showScheduleEmailDialog( final JSONArray scheduleParams ) {

    try {
      final String url = GWT.getHostPageBaseURL() + "api/mantle/isAuthenticated"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
      requestBuilder.setHeader( "accept", "text/plain" );
      requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      requestBuilder.sendRequest( null, new RequestCallback() {

        @Override
        public void onError( Request request, Throwable caught ) {
          MantleLoginDialog.performLogin( new AsyncCallback<Boolean>() {

            @Override
            public void onFailure( Throwable caught ) {
            }

            @Override
            public void onSuccess( Boolean result ) {
              showScheduleEmailDialog( scheduleParams );
            }
          } );
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          if ( scheduleEmailDialog == null ) {
            scheduleEmailDialog =
                new ScheduleEmailDialog( ScheduleParamsDialog.this, filePath, jobSchedule, scheduleParams, editJob );
            scheduleEmailDialog.setCallback( callback );
          } else {
            scheduleEmailDialog.setScheduleParams( scheduleParams );
            scheduleEmailDialog.setJobSchedule( jobSchedule );
          }
          scheduleEmailDialog.center();
          hide();
        }

      } );
    } catch ( RequestException e ) {
      Window.alert( e.getMessage() );
    }

  }

  public Boolean getDone() {
    return done;
  }

  public void setDone( Boolean done ) {
    this.done = done;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onNext(org.pentaho.gwt.widgets.client.wizards.
   * IWizardPanel, org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected boolean onNext( IWizardPanel nextPanel, IWizardPanel previousPanel ) {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onPrevious(org.pentaho.gwt.widgets.client.wizards
   * .IWizardPanel, org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected void backClicked() {
    try {
      scheduleParams = getScheduleParams( true );
    } catch ( Exception e ) {
      // If error generate on trying to assign params while backing out,
      // obviously you want to ignore it.
    }
    parentDialog.center();
    hide();
  }

  @Override
  public void center() {
    if ( scheduleParams != null ) {
      // we have saved params from back/next
      String urlPath = NameUtils.encodeRepositoryPath( filePath );
      String urlParams = "";
      for ( int i = 0; i < scheduleParams.size(); i++ ) {
        JSONObject o = scheduleParams.get( i ).isObject();
        // keys: name, stringValue, type
        JSONString name = o.get( "name" ).isString();
        JSONArray stringValueArr = o.get( "stringValue" ).isArray();

        for ( int j = 0; j < stringValueArr.size(); j++ ) {
          urlParams += ( i == 0 && j == 0 ) ? "?" : "&";
          urlParams +=
              name.stringValue().replace( "\"", "" ) + "=" + stringValueArr.get( j ).toString().replace( "\"", "" );
        }
      }
      setParametersUrl( "api/repos/" + urlPath + "/parameterUi" + urlParams ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    super.center();
  }

  public void setParametersUrl( String url ) {
    scheduleParamsWizardPanel.setParametersUrl( url );
  }

  @Override
  protected boolean onPrevious( IWizardPanel previousPanel, IWizardPanel currentPanel ) {
    return true;
  }

  @Override
  protected boolean showBack( int index ) {
    return parentDialog != null;
  }

  @Override
  protected boolean showFinish( int index ) {
    return true;
  }

  @Override
  protected boolean showNext( int index ) {
    return false;
  }

  @Override
  protected boolean enableBack( int index ) {
    return true;
  }

  public void setCallback( IDialogCallback callback ) {
    this.callback = callback;
  }

  public IDialogCallback getCallback() {
    return callback;
  }

  public ScheduleRecurrenceDialog getParentDialog() {
    return parentDialog;
  }

  public void setParentDialog( ScheduleRecurrenceDialog parentDialog ) {
    this.parentDialog = parentDialog;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath( String filePath ) {
    this.filePath = filePath;
  }

  public JSONObject getJobSchedule() {
    return jobSchedule;
  }

  public void setJobSchedule( JSONObject jobSchedule ) {
    this.jobSchedule = jobSchedule;
  }

  public JsJob getEditJob() {
    return editJob;
  }

  public void setEditJob( JsJob editJob ) {
    this.editJob = editJob;
  }

  public boolean isEmailConfValid() {
    return isEmailConfValid;
  }

  public void setEmailConfValid( boolean isEmailConfValid ) {
    this.isEmailConfValid = isEmailConfValid;
  }

  public void setScheduleParams( JSONArray scheduleParams ) {
    this.scheduleParams = scheduleParams;
  }
}
