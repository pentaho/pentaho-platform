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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;

import java.util.Date;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RunInBackgroundCommand extends AbstractCommand {
  String moduleBaseURL = GWT.getModuleBaseURL();
  String moduleName = GWT.getModuleName();
  String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));

  private FileItem repositoryFile;

  public RunInBackgroundCommand() {
  }

  public RunInBackgroundCommand(FileItem fileItem) {
    this.repositoryFile = fileItem;
  }

  protected void performOperation() {
    performOperation(true);
  }
  
  @SuppressWarnings("deprecation")
  protected JSONObject getJsonSimpleTrigger(int repeatCount, int interval, Date startDate, Date endDate) {
    JSONObject trigger = new JSONObject();
    trigger.put("repeatInterval", new JSONNumber(interval)); //$NON-NLS-1$
    trigger.put("repeatCount", new JSONNumber(repeatCount)); //$NON-NLS-1$
    trigger.put("startTime", startDate != null ? new JSONString(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDate)) : JSONNull.getInstance()); //$NON-NLS-1$
    if (endDate != null) {
      endDate.setHours(23);
      endDate.setMinutes(59);
      endDate.setSeconds(59);
    }
    trigger.put("endTime", endDate == null ? JSONNull.getInstance() : new JSONString(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(endDate))); //$NON-NLS-1$
    return trigger;
  }
  
  protected void performOperation(boolean feedback) {
    // delete file
    VerticalPanel vp = new VerticalPanel();
    vp.add(new Label(Messages.getString("confirmRunInBackground"))); //$NON-NLS-1$
    final PromptDialogBox confirmBackgroundSchedule = new PromptDialogBox(
        Messages.getString("runInBackground"), Messages.getString("yes"), Messages.getString("no"), false, true, vp); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
        confirmBackgroundSchedule.hide();
      }

      public void okPressed() {
        String scheduleJobUrl = contextURL + "api/scheduler/job"; //$NON-NLS-1$
        JSONObject trigger = new JSONObject();
        trigger.put("repeatInterval", new JSONNumber(0)); //$NON-NLS-1$
        trigger.put("repeatCount", new JSONNumber(0)); //$NON-NLS-1$
        trigger.put("startTime", JSONNull.getInstance()); //$NON-NLS-1$
        trigger.put("endTime", JSONNull.getInstance()); //$NON-NLS-1$
        
        JSONObject scheduleRequest = new JSONObject();
        scheduleRequest.put("inputFile", new JSONString(repositoryFile.getPath())); //$NON-NLS-1$ //$NON-NLS-2$
        scheduleRequest.put("outputFile", JSONNull.getInstance()); //$NON-NLS-1$
        scheduleRequest.put("simpleJobTrigger", getJsonSimpleTrigger(0, 0, null, null)); //$NON-NLS-1$

        RequestBuilder scheduleFileRequestBuilder = new RequestBuilder(RequestBuilder.POST, scheduleJobUrl);
        scheduleFileRequestBuilder.setHeader("Content-Type", "application/json");  //$NON-NLS-1$//$NON-NLS-2$
        try {
          scheduleFileRequestBuilder.sendRequest(scheduleRequest.toString(), new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotRun"), //$NON-NLS-1$ //$NON-NLS-2$
              false, false, true);
              dialogBox.center();
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == 200) {
                new RefreshRepositoryCommand().execute(false);
              } else {
                MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotRun"), //$NON-NLS-1$ //$NON-NLS-2$
                    false, false, true);
                dialogBox.center();
              }                
            }
            
          });
        } catch (RequestException e) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotRun"), //$NON-NLS-1$ //$NON-NLS-2$
              false, false, true);
          dialogBox.center();
        }
      }
    };
    if (!feedback) {
      callback.okPressed();
    } else {
      confirmBackgroundSchedule.setCallback(callback);
      confirmBackgroundSchedule.center();
    }
  }

}
