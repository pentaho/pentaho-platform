/*
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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Jul 30, 2008 
 * @author wseyler
 */
package org.pentaho.mantle.client.solutionbrowser.scheduling;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog;
import org.pentaho.gwt.widgets.client.wizards.IWizardPanel;
import org.pentaho.gwt.widgets.client.wizards.panels.JsSchedulingParameter;
import org.pentaho.gwt.widgets.client.wizards.panels.ScheduleParamsWizardPanel;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * @author wseyler
 *
 */
public class ScheduleParamsDialog extends AbstractWizardDialog {
  FileItem fileItem = null;
  String moduleBaseURL = GWT.getModuleBaseURL();
  String moduleName = GWT.getModuleName();
  String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
  
  ScheduleParamsWizardPanel scheduleParamsWizardPanel;
  NewScheduleDialog parentDialog;
  String filePath;
  JSONObject jobSchedule;

  Boolean done = false;
 
  
  public ScheduleParamsDialog(NewScheduleDialog parentDialog) {
    super(Messages.getString("newSchedule"), null, false, true); //$NON-NLS-1$
    this.parentDialog = parentDialog;
    this.filePath = parentDialog.filePath;
    this.jobSchedule = parentDialog.getSchedule();
    initDialog();
  }

  public ScheduleParamsDialog(String filePath, JSONObject schedule) {
    super(Messages.getString("runInBackground"), null, false, true); //$NON-NLS-1$
    this.filePath = filePath;
    this.jobSchedule = schedule;
    initDialog();
 }
  
  private void initDialog() {
    scheduleParamsWizardPanel = new ScheduleParamsWizardPanel(filePath);
    IWizardPanel[] wizardPanels = {scheduleParamsWizardPanel};
    this.setWizardPanels(wizardPanels);
    setPixelSize(800, 465);
    String urlPath = filePath.replaceAll("/", ":"); //$NON-NLS-1$  //$NON-NLS-2$
    setParametersUrl("api/repos/" + urlPath + "/parameterUi"); //$NON-NLS-1$ //$NON-NLS-2$
    wizardDeckPanel.setHeight("100%"); //$NON-NLS-1$
   }
  
  void setScheduleDescription(String description) {
    scheduleParamsWizardPanel.setScheduleDescription(description);
  }
  
  JSONArray getScheduleParams() {
    JsArray<JsSchedulingParameter> schedulingParams = scheduleParamsWizardPanel.getParams();
    JSONArray params = new JSONArray();
    for (int i = 0; i < schedulingParams.length(); i++) {
      params.set(i, new JSONObject(schedulingParams.get(i)));
    }
    return params;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#finish()
   */
  @Override
  protected boolean onFinish() {
    JSONObject scheduleRequest = (JSONObject)JSONParser.parseStrict(jobSchedule.toString());
    scheduleRequest.put("jobParameters", getScheduleParams()); //$NON-NLS-1$    

    RequestBuilder scheduleFileRequestBuilder = new RequestBuilder(RequestBuilder.POST, contextURL + "api/scheduler/job");
    scheduleFileRequestBuilder.setHeader("Content-Type", "application/json");  //$NON-NLS-1$//$NON-NLS-2$

    try {
      scheduleFileRequestBuilder.sendRequest(scheduleRequest.toString(), new RequestCallback() {

        @Override
        public void onError(Request request, Throwable exception) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), exception.toString(), false, false, true); //$NON-NLS-1$
          dialogBox.center();
          setDone(false);
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == 200) {
            setDone(true);
            ScheduleParamsDialog.this.hide();
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("schedule"), Messages.getString("fileScheduled", filePath.substring(filePath.lastIndexOf("/") + 1)), //$NON-NLS-1$ //$NON-NLS-2$
                false, false, true);
            dialogBox.center();

          } else {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("serverErrorColon") + " " + response.getStatusCode(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-2$
                false, false, true);
            dialogBox.center();
            setDone(false);
          }                
        }
        
      });
    } catch (RequestException e) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.toString(), //$NON-NLS-1$
          false, false, true);
      dialogBox.center();
      setDone(false);
    }
    setDone(true);
    return true;
  }

  public Boolean getDone() {
    return done;
  }

  public void setDone(Boolean done) {
    this.done = done;
  }

  /* (non-Javadoc)
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onNext(org.pentaho.gwt.widgets.client.wizards.IWizardPanel, org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected boolean onNext(IWizardPanel nextPanel, IWizardPanel previousPanel) {
    // TODO Auto-generated method stub
    return true;
  }

  /* (non-Javadoc)
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onPrevious(org.pentaho.gwt.widgets.client.wizards.IWizardPanel, org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected void backClicked() {
    hide();
    parentDialog.center();  
    
  }

  @Override
  public void center() {
    // TODO Auto-generated method stub
    super.center();
  }

  public void setParametersUrl(String url) {
    scheduleParamsWizardPanel.setParametersUrl(url);
  }

  @Override
  protected boolean onPrevious(IWizardPanel previousPanel, IWizardPanel currentPanel) {
    return true;

  }

  @Override
  protected boolean showBack(int index) {
    return parentDialog != null;
  }

  @Override
  protected boolean showFinish(int index) {
    return true;
  }

  @Override
  protected boolean showNext(int index) {
    return false;
  }

  @Override
  protected boolean enableBack(int index) {
    return true;
  }
}
