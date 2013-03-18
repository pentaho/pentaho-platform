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
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.solutionbrowser.fileproperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.tabs.PentahoTab;
import org.pentaho.gwt.widgets.client.tabs.PentahoTabPanel;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * File properties parent panel displayed when right clicking
 * a file in PUC repo browser. Subpanels include: General,
 * Share, History
 */
public class FilePropertiesDialog extends PromptDialogBox {
  public enum Tabs {
    GENERAL, PERMISSION, HISTORY
  }

  private PentahoTabPanel propertyTabs;
  private GeneralPanel generalTab;
  private PermissionsPanel permissionsTab;
  private GeneratedContentPanel generatedContentTab;

  private String moduleBaseURL = GWT.getModuleBaseURL();
  private String moduleName = GWT.getModuleName();
  private String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));

  boolean dirty = false;

  /**
   *
   * @param fileSummary
   * @param propertyTabs
   * @param callback
   * @param defaultTab
   */
  public FilePropertiesDialog(RepositoryFile fileSummary, final PentahoTabPanel propertyTabs, final IDialogCallback callback, Tabs defaultTab) {
    super(fileSummary.getName() + " " + Messages.getString("properties"), Messages.getString("ok"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    boolean isInTrash = fileSummary.getPath().contains("/.trash/pho:");
    setContent(propertyTabs);

    generalTab = new GeneralPanel(this, fileSummary);

    if (!fileSummary.isFolder()) {
      generatedContentTab = new GeneratedContentPanel(SolutionBrowserPanel.pathToId(fileSummary.getPath()), null, null);
    }
    if (!isInTrash) {
      permissionsTab = new PermissionsPanel(fileSummary);
    }

    generalTab.getElement().setId("filePropertiesGeneralTab");
    if (!isInTrash) {
      permissionsTab.getElement().setId("filePropertiesPermissionsTab");
    }

    // get metadata via REST
    getMetadata(fileSummary);
    getAcls(fileSummary);

    okButton.getElement().setId("filePropertiesOKButton");
    cancelButton.getElement().setId("filePropertiesCancelButton");

    super.setCallback(new IDialogCallback() {

      public void cancelPressed() {
        if (callback != null) {
          callback.cancelPressed();
        }
      }

      public void okPressed() {
        applyPanel();
        if (callback != null) {
          callback.okPressed();
        }
      }
    });
    this.propertyTabs = propertyTabs;
    this.propertyTabs.addTab(Messages.getString("general"), Messages.getString("general"), false, generalTab);
    if (permissionsTab != null) {
      this.propertyTabs.addTab(Messages.getString("share"), Messages.getString("share"), false, permissionsTab);
    }
    if (generatedContentTab != null) {
      this.propertyTabs.addTab(Messages.getString("history"), Messages.getString("history"), false, generatedContentTab);
    }
    getWidget().setHeight("100%"); //$NON-NLS-1$
    getWidget().setWidth("100%"); //$NON-NLS-1$
    setPixelSize(490, 420);
    showTab(defaultTab);
  }

  /**
   *
   */
  private void applyPanel() {
    List<RequestBuilder> requestBuilders = new ArrayList();
    for (int i=0;i<propertyTabs.getTabCount();i++) {
      Widget w = propertyTabs.getTab(i).getContent();
      if (w instanceof IFileModifier) {
        // get requests from sub panels
        if(((IFileModifier) w).prepareRequests() != null){
          requestBuilders.addAll(((IFileModifier) w).prepareRequests());
        }
      }
    }

    RequestCallback requestCallback;

    // chain requests from subpanels using callbacks to try and avoid any StaleItemStateExceptions
    for(int i = 0; i <= requestBuilders.size() - 1; i++ ){
      RequestBuilder requestBuilder = requestBuilders.get(i);
      if(i < requestBuilders.size() - 1){
        final RequestBuilder nextRequest = requestBuilders.get(i + 1);
        requestCallback = new ChainedRequestCallback(nextRequest);
      }
      else{
        requestCallback = new RequestCallback() {
          @Override
          public void onError(Request arg0, Throwable arg1) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), arg1.toString(), false, false, true); //$NON-NLS-1$
            dialogBox.center();
          }

          @Override
          public void onResponseReceived(Request arg0, Response arg1) {
            if (arg1.getStatusCode() == Response.SC_OK) {
              dirty = false;
            } else {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), arg1.toString(), false, false, true); //$NON-NLS-1$
              dialogBox.center();
            }
          }
        };
      }
      requestBuilder.setCallback(requestCallback);
    }

    // start the chain
    try{
      requestBuilders.get(0).send();
    }
    catch (RequestException e){

    }
  }

  /**
   *
   * @param tab
   */
  public void showTab(Tabs tab) {
    for (int i=0; i<propertyTabs.getTabCount(); i++) {
      PentahoTab pTab = propertyTabs.getTab(i);
      switch(tab) {
        case GENERAL:
          if (pTab.getContent() == generalTab) {
            propertyTabs.selectTab(pTab);
          }
          break;
        case PERMISSION:
          if (pTab.getContent() == permissionsTab) {
            propertyTabs.selectTab(pTab);
          }
          break;
        case HISTORY:
          if (pTab.getContent() == generatedContentTab) {
            propertyTabs.selectTab(pTab);
          }
          break;
        default:
          break;
      }
    }
  }

  /**
   *
   * @param fileSummary
   */
  protected void getAcls(RepositoryFile fileSummary) {
    String url = contextURL + "api/repo/files/" + SolutionBrowserPanel.pathToId(fileSummary.getPath()) + "/acl"; //$NON-NLS-1$ //$NON-NLS-2$
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    try {
      builder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), exception.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
          dialogBox.center();
        }

        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == Response.SC_OK) {
            generalTab.setAclResponse(response);
            permissionsTab.setAclResponse(response);
          } else {
            MessageDialogBox dialogBox = new MessageDialogBox(
                Messages.getString("error"), Messages.getString("serverErrorColon") + " " + response.getStatusCode(), false, false, true); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            dialogBox.center();
          }
        }
      });
    } catch (RequestException e) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
      dialogBox.center();
    }
  }

  /**
   *
   * @param fileSummary
   */
  protected void getMetadata(RepositoryFile fileSummary){
    String metadataUrl = contextURL + "api/repo/files/" + SolutionBrowserPanel.pathToId(fileSummary.getPath()) + "/metadata"; //$NON-NLS-1$ //$NON-NLS-2$
    RequestBuilder metadataBuilder = new RequestBuilder(RequestBuilder.GET, metadataUrl);
    metadataBuilder.setHeader("accept", "application/json");
    try {
      metadataBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), exception.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
          dialogBox.center();
        }

        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == Response.SC_OK) {
            if (response.getText() != null && !"".equals(response.getText()) && !response.getText().equals("null")) {
              generalTab.setMetadataResponse(response);
            }
          } else {
            MessageDialogBox dialogBox = new MessageDialogBox(
                Messages.getString("error"), Messages.getString("serverErrorColon") + " " + response.getStatusCode(), false, false, true); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            dialogBox.center();
          }
        }
      });
    } catch (RequestException e) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
      dialogBox.center();
    }
  }

  /**
   *
   */
  protected class ChainedRequestCallback implements RequestCallback{
    RequestBuilder nextRequest;

    /**
     *
     * @param arg0
     * @param arg1
     */
    @Override
    public void onError(Request arg0, Throwable arg1) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), arg1.toString(), false, false, true); //$NON-NLS-1$
      dialogBox.center();
    }

    /**
     *
     * @param arg0
     * @param arg1
     */
    @Override
    public void onResponseReceived(Request arg0, Response arg1) {
      if (arg1.getStatusCode() == Response.SC_OK) {
        try{
          nextRequest.send();
        }
        catch (RequestException e){

        }
        dirty = false;
      } else {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), arg1.toString(), false, false, true); //$NON-NLS-1$
        dialogBox.center();
      }
    }

    /**
     *
     * @param nextRequest
     */
    public void setNextRequest(RequestBuilder nextRequest){
      this.nextRequest = nextRequest;
    }

    /**
     *
     * @param nextRequest
     */
    public ChainedRequestCallback(RequestBuilder nextRequest){
      this.nextRequest = nextRequest;
    }

    /**
     *
     */
    public ChainedRequestCallback(){

    }
  }
}
