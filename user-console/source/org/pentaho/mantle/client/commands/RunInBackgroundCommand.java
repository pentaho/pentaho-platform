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

import com.google.gwt.json.client.*;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleEmailDialog;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleOutputLocationDialog;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleParamsDialog;
import org.pentaho.mantle.client.events.SolutionFileHandler;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

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

  private String solutionPath = null;
  private String solutionTitle = null;
  private String outputLocationPath = null;
  private String outputName = null;

  public String getSolutionTitle() {
    return solutionTitle;
  }

  public void setSolutionTitle(String solutionTitle) {
    this.solutionTitle = solutionTitle;
  }

  public String getSolutionPath() {
    return solutionPath;
  }

  public void setSolutionPath(String solutionPath) {
    this.solutionPath = solutionPath;
  }

  public String getOutputLocationPath() {
    return outputLocationPath;
  }

  public void setOutputLocationPath(String outputLocationPath) {
    this.outputLocationPath = outputLocationPath;
  }

  public String getModuleBaseURL() {
    return moduleBaseURL;
  }

  public void setModuleBaseURL(String moduleBaseURL) {
    this.moduleBaseURL = moduleBaseURL;
  }

  public String getOutputName() {
    return outputName;
  }

  public void setOutputName(String outputName) {
    this.outputName = outputName;
  }

  protected void performOperation() {
    final SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
    if(this.getSolutionPath() != null){
      sbp.getFile(this.getSolutionPath(), new SolutionFileHandler() {
        @Override
        public void handle(RepositoryFile repositoryFile) {
          RunInBackgroundCommand.this.repositoryFile = new FileItem(repositoryFile, null, null, false, null);
          showDialog(true);
        }
      });
    }
    else{
      performOperation(true);
    }
  }

  @SuppressWarnings("deprecation")
  protected JSONObject getJsonSimpleTrigger(int repeatCount, int interval, Date startDate, Date endDate) {
    JSONObject trigger = new JSONObject();
    trigger.put("repeatInterval", new JSONNumber(interval)); //$NON-NLS-1$
    trigger.put("repeatCount", new JSONNumber(repeatCount)); //$NON-NLS-1$
    trigger
        .put(
            "startTime", startDate != null ? new JSONString(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(startDate)) : JSONNull.getInstance()); //$NON-NLS-1$
    if (endDate != null) {
      endDate.setHours(23);
      endDate.setMinutes(59);
      endDate.setSeconds(59);
    }
    trigger
        .put(
            "endTime", endDate == null ? JSONNull.getInstance() : new JSONString(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(endDate))); //$NON-NLS-1$
    return trigger;
  }

  protected void showDialog(final boolean feedback){
    final ScheduleOutputLocationDialog outputLocationDialog = new ScheduleOutputLocationDialog(solutionPath){
      @Override
      protected void onSelect(final String name, final String outputLocationPath) {
        setOutputName(name);
        setOutputLocationPath(outputLocationPath);
        performOperation(feedback);
      }
    };
    outputLocationDialog.center();
  }

  protected void performOperation(boolean feedback) {

    final String filePath = (this.getSolutionPath() != null) ? this.getSolutionPath() : repositoryFile.getPath();
    final String fileName = (this.getSolutionTitle() != null) ? this.getSolutionTitle() : repositoryFile.getName();
    String urlPath = URL.encodePathSegment(filePath.replaceAll("/", ":")); //$NON-NLS-1$ //$NON-NLS-2$
    RequestBuilder scheduleFileRequestBuilder = new RequestBuilder(RequestBuilder.GET, contextURL + "api/repo/files/" //$NON-NLS-1$
        + urlPath + "/parameterizable"); //$NON-NLS-1$
    scheduleFileRequestBuilder.setHeader("accept", "text/plain"); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      scheduleFileRequestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          MessageDialogBox dialogBox = new MessageDialogBox(
              Messages.getString("error"), exception.toString(), false, false, true); //$NON-NLS-1$
          dialogBox.center();
        }

        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == Response.SC_OK) {
            final JSONObject scheduleRequest = new JSONObject();
            scheduleRequest.put("inputFile", new JSONString(filePath.replaceAll(":", "/"))); //$NON-NLS-1$

            // Set job name
            if(StringUtils.isEmpty(getOutputName())){
              scheduleRequest.put("jobName", JSONNull.getInstance()); //$NON-NLS-1$
            }
            else{
              scheduleRequest.put("jobName", new JSONString(getOutputName().replaceAll(":", "/"))); //$NON-NLS-1$
            }

            // Set output path location
            if(StringUtils.isEmpty(getOutputLocationPath())){
              scheduleRequest.put("outputFile", JSONNull.getInstance()); //$NON-NLS-1$
            }
            else{
              scheduleRequest.put("outputFile", new JSONString(getOutputLocationPath().replaceAll(":", "/"))); //$NON-NLS-1$
            }

            //BISERVER-9321
            scheduleRequest.put("runInBackground", JSONBoolean.getInstance(true));

            final boolean hasParams = Boolean.parseBoolean(response.getText());

            RequestBuilder emailValidRequest = new RequestBuilder(RequestBuilder.GET, contextURL
                + "api/emailconfig/isValid"); //$NON-NLS-1$
            emailValidRequest.setHeader("accept", "text/plain"); //$NON-NLS-1$ //$NON-NLS-2$
            try {
              emailValidRequest.sendRequest(null, new RequestCallback() {

                public void onError(Request request, Throwable exception) {
                  MessageDialogBox dialogBox = new MessageDialogBox(
                      Messages.getString("error"), exception.toString(), false, false, true); //$NON-NLS-1$
                  dialogBox.center();
                }

                public void onResponseReceived(Request request, Response response) {
                  if (response.getStatusCode() == Response.SC_OK) {
                    //final boolean isEmailConfValid = Boolean.parseBoolean(response.getText());
                    // force false for now, I have a feeling PM is going to want this, making it easy to turn back on
                    final boolean isEmailConfValid = false;
                    if (hasParams) {
                      ScheduleParamsDialog dialog = new ScheduleParamsDialog(filePath, scheduleRequest,
                          isEmailConfValid);
                      dialog.center();
                    } else if (isEmailConfValid) {
                      ScheduleEmailDialog scheduleEmailDialog = new ScheduleEmailDialog(null, filePath,
                          scheduleRequest, null, null);
                      scheduleEmailDialog.center();
                    } else {
                      // just run it
                      RequestBuilder scheduleFileRequestBuilder = new RequestBuilder(RequestBuilder.POST, contextURL
                          + "api/scheduler/job"); //$NON-NLS-1$
                      scheduleFileRequestBuilder.setHeader("Content-Type", "application/json"); //$NON-NLS-1$//$NON-NLS-2$

                      try {
                        scheduleFileRequestBuilder.sendRequest(scheduleRequest.toString(), new RequestCallback() {

                          @Override
                          public void onError(Request request, Throwable exception) {
                            MessageDialogBox dialogBox = new MessageDialogBox(
                                Messages.getString("error"), exception.toString(), false, false, true); //$NON-NLS-1$
                            dialogBox.center();
                          }

                          @Override
                          public void onResponseReceived(Request request, Response response) {
                            if (response.getStatusCode() == 200) {
                              MessageDialogBox dialogBox = new MessageDialogBox(
                                  Messages.getString("runInBackground"), Messages.getString("backgroundExecutionStarted"), //$NON-NLS-1$ //$NON-NLS-2$
                                  false, false, true);
                              dialogBox.center();
                            } else {
                              MessageDialogBox dialogBox = new MessageDialogBox(
                                  Messages.getString("error"), Messages.getString("serverErrorColon") + " " + response.getStatusCode(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-2$ //$NON-NLS-3$
                                  false, false, true);
                              dialogBox.center();
                            }
                          }

                        });
                      } catch (RequestException e) {
                        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.toString(), //$NON-NLS-1$
                            false, false, true);
                        dialogBox.center();
                      }
                    }

                  }
                }
              });
            } catch (RequestException e) {
              MessageDialogBox dialogBox = new MessageDialogBox(
                  Messages.getString("error"), e.toString(), false, false, true); //$NON-NLS-1$
              dialogBox.center();
            }

          } else {
            MessageDialogBox dialogBox = new MessageDialogBox(
                Messages.getString("error"), Messages.getString("serverErrorColon") + " " + response.getStatusCode(), false, false, true); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            dialogBox.center();
          }
        }

      });
    } catch (RequestException e) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.toString(), false, false, true); //$NON-NLS-1$
      dialogBox.center();
    }
  }

}
