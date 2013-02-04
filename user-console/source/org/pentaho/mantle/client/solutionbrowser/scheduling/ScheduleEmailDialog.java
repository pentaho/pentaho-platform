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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.mantle.client.solutionbrowser.scheduling;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog;
import org.pentaho.gwt.widgets.client.wizards.IWizardPanel;
import org.pentaho.gwt.widgets.client.wizards.panels.JsSchedulingParameter;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.workspace.JsJob;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class ScheduleEmailDialog extends AbstractWizardDialog {
  FileItem fileItem = null;
  String moduleBaseURL = GWT.getModuleBaseURL();
  String moduleName = GWT.getModuleName();
  String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));

  IDialogCallback callback;

  ScheduleEmailWizardPanel scheduleEmailWizardPanel;
  AbstractWizardDialog parentDialog;
  String filePath;
  JSONObject jobSchedule;
  JSONArray scheduleParams;
  JsJob editJob;
  
  Boolean done = false;

  public ScheduleEmailDialog(AbstractWizardDialog parentDialog, String filePath, JSONObject jobSchedule, JSONArray scheduleParams, JsJob editJob) {
    super(Messages.getString("newSchedule"), null, false, true); //$NON-NLS-1$
    this.parentDialog = parentDialog;
    this.filePath = filePath;
    this.jobSchedule = jobSchedule;
    this.scheduleParams = scheduleParams;
    this.editJob = editJob;
    initDialog();
  }

  private void initDialog() {
    scheduleEmailWizardPanel = new ScheduleEmailWizardPanel(filePath, editJob);
    IWizardPanel[] wizardPanels = { scheduleEmailWizardPanel };
    this.setWizardPanels(wizardPanels);
    setPixelSize(650, 360);
    wizardDeckPanel.setHeight("100%"); //$NON-NLS-1$
  }

  public boolean onKeyDownPreview(char key, int modifiers) {
    if (key == KeyCodes.KEY_ESCAPE) {
      hide();
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#finish()
   */
  @Override
  protected boolean onFinish() {
    JSONObject scheduleRequest = (JSONObject) JSONParser.parseStrict(jobSchedule.toString());
    JsArray<JsSchedulingParameter> emailParams = scheduleEmailWizardPanel.getEmailParams();

    if (scheduleParams == null) {
      scheduleParams = new JSONArray();
    }
    if (emailParams != null) {
      int index = scheduleParams.size();
      for (int i = 0; i < emailParams.length(); i++) {
        scheduleParams.set(index++, new JSONObject(emailParams.get(i)));
      }
    }

    scheduleRequest.put("jobParameters", scheduleParams); //$NON-NLS-1$    

    RequestBuilder scheduleFileRequestBuilder = new RequestBuilder(RequestBuilder.POST, contextURL + "api/scheduler/job");
    scheduleFileRequestBuilder.setHeader("Content-Type", "application/json"); //$NON-NLS-1$//$NON-NLS-2$

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
            ScheduleEmailDialog.this.hide();
            if (callback != null) {
              callback.okPressed();
            }
          } else {
            MessageDialogBox dialogBox = new MessageDialogBox(
                Messages.getString("error"), Messages.getString("serverErrorColon") + " " + response.getStatusCode(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-2$
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

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onNext(org.pentaho.gwt.widgets.client.wizards.IWizardPanel,
   * org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
   */
  @Override
  protected boolean onNext(IWizardPanel nextPanel, IWizardPanel previousPanel) {
    // TODO Auto-generated method stub
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog#onPrevious(org.pentaho.gwt.widgets.client.wizards.IWizardPanel,
   * org.pentaho.gwt.widgets.client.wizards.IWizardPanel)
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

  public void setCallback(IDialogCallback callback) {
    this.callback = callback;
  }

  public IDialogCallback getCallback() {
    return callback;
  }
}
