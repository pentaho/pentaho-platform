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
 */
package org.pentaho.mantle.client.dialogs;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.IDialogValidatorCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author wseyler/modifed for Import parameters by tband
 *
 */
public class ImportDialog extends PromptDialogBox {
  
  private FormPanel form;

  /**
   * @param repositoryFile
   */
  public ImportDialog(RepositoryFile repositoryFile) {
    super(Messages.getString("import"), Messages.getString("ok"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    final PopupPanel indefiniteProgress = new PopupPanel(false, true);
    DOM.setStyleAttribute(indefiniteProgress.getElement(), "zIndex", "2000"); // Gets it to the front
    Image waitImage = new Image("mantle/large-loading.gif"); //$NON-NLS-1$
    indefiniteProgress.add(waitImage);

    form = new FormPanel();
    form.addSubmitHandler(new SubmitHandler() {
      @Override
      public void onSubmit(SubmitEvent se) {
        //if no file is selected then do not proceed  
        okButton.setEnabled(false);
        cancelButton.setEnabled(false);
        indefiniteProgress.center();
      }
    });
    form.addSubmitCompleteHandler(new SubmitCompleteHandler() {
      @Override
      public void onSubmitComplete(SubmitCompleteEvent sce) {
        new RefreshRepositoryCommand().execute(false);
        indefiniteProgress.hide();
        okButton.setEnabled(false);
        cancelButton.setEnabled(true);
        ImportDialog.this.hide();
        logWindow(sce.getResults());
      }
    });

    VerticalPanel rootPanel = new VerticalPanel();
    rootPanel.add(new HTML("&nbsp;"));
    
    Label fileLabel = new Label(Messages.getString("file") + ":");
    final TextBox importDir = new TextBox();
    rootPanel.add(fileLabel);

    final CheckBox applyAclPermissions = new CheckBox(Messages.getString("applyAclPermissions"), true);
    applyAclPermissions.setName("applyAclPermissions");
    applyAclPermissions.setValue(Boolean.TRUE);
    applyAclPermissions.setFormValue("true");
    applyAclPermissions.setEnabled(true);
    applyAclPermissions.setVisible(false);
    
    final CheckBox overwriteAclPermissions = new CheckBox(Messages.getString("overwriteAclPermissions"), true);
    overwriteAclPermissions.setName("overwriteAclPermissions");
    overwriteAclPermissions.setValue(Boolean.TRUE);
    overwriteAclPermissions.setFormValue("true");
    overwriteAclPermissions.setEnabled(true);
    overwriteAclPermissions.setVisible(false);

    okButton.setEnabled(false);
    FileUpload upload = new FileUpload();
    upload.setName("fileUpload");
    ChangeHandler fileUploadHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        if (!"".equals(importDir.getValue())) {
          okButton.setEnabled(true);
        } else {
          okButton.setEnabled(false);
        }
      }
    };
    upload.addChangeHandler(fileUploadHandler);
    rootPanel.add(upload);

    rootPanel.add(applyAclPermissions);
    rootPanel.add(overwriteAclPermissions);
    
    DisclosurePanel disclosurePanel = new DisclosurePanel(Messages.getString("advancedOptions"));
    disclosurePanel.getHeader().setStyleName("gwt-Label");
    HorizontalPanel mainPanel = new HorizontalPanel();
    mainPanel.add(new HTML("&nbsp;"));
    VerticalPanel disclosureContent = new VerticalPanel();
    
    HTML replaceLabel = new HTML("&nbsp;" + Messages.getString("fileExists"));
    replaceLabel.setStyleName("gwt-Label");
    disclosureContent.add(replaceLabel);
    
    final ListBox overwriteFile = new ListBox();    

    overwriteFile.setName("overwriteFile");
    overwriteFile.addItem(Messages.getString("replaceFile"), "true");
    overwriteFile.addItem(Messages.getString("doNotImport"), "false");
    overwriteFile.setVisibleItemCount(1);
    disclosureContent.add(overwriteFile);
    
    HTML filePermissionsLabel = new HTML("&nbsp;" + Messages.getString("filePermissions"));
    filePermissionsLabel.setStyleName("gwt-Label");
    disclosureContent.add(filePermissionsLabel);
    
    final ListBox filePermissionsDropDown = new ListBox();
    filePermissionsDropDown.addItem(Messages.getString("retainPermissions"), "true"); //If selected set "overwriteAclPermissions" to true.
    filePermissionsDropDown.addItem(Messages.getString("usePermissions"), "false"); //If selected set "overwriteAclPermissions" to false.
    filePermissionsDropDown.addItem(Messages.getString("removePermissions"), "none"); //If selected then set "applyAclPermissions" to false else true.
    ChangeHandler filePermissionsHandler = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
          String value = filePermissionsDropDown.getValue(filePermissionsDropDown.getSelectedIndex());
          
          applyAclPermissions.setValue(Boolean.TRUE);
          applyAclPermissions.setFormValue("true");
          
          overwriteAclPermissions.setFormValue(value);
          overwriteAclPermissions.setValue(Boolean.valueOf(value));
          
          if(value.equals("none")) {
        	  applyAclPermissions.setValue(Boolean.FALSE);
        	  applyAclPermissions.setFormValue("false");
          }
        }
    };
    filePermissionsDropDown.addChangeHandler(filePermissionsHandler);
    filePermissionsDropDown.setVisibleItemCount(1);
    disclosureContent.add(filePermissionsDropDown);
    
    HTML fileOwnershipLabel = new HTML("&nbsp;" + Messages.getString("fileOwnership"));
    fileOwnershipLabel.setStyleName("gwt-Label");
    disclosureContent.add(fileOwnershipLabel);
    
    final ListBox retainOwnership = new ListBox();
    retainOwnership.setName("retainOwnership");
    retainOwnership.addItem(Messages.getString("keepOwnership"), "true");
    retainOwnership.addItem(Messages.getString("assignOwnership"), "false");
    retainOwnership.setVisibleItemCount(1);
    disclosureContent.add(retainOwnership);
    
    ChangeHandler overwriteFileHandler = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
        	String value = overwriteFile.getValue(overwriteFile.getSelectedIndex());
        	if(value.equals("false")) {
        		filePermissionsDropDown.setSelectedIndex(1);
        		filePermissionsDropDown.setEnabled(false);
        		retainOwnership.setSelectedIndex(0);
        		retainOwnership.setEnabled(false);
        	} 
        	if(value.equals("true")) {
        		filePermissionsDropDown.setEnabled(true);
        		retainOwnership.setEnabled(true);
        	}
        	
        }
    };
    overwriteFile.addChangeHandler(overwriteFileHandler);
    
    HTML loggingLabel = new HTML("&nbsp;" + Messages.getString("logging"));
    loggingLabel.setStyleName("gwt-Label");
    disclosureContent.add(loggingLabel);
    
    ListBox loggingDropDown = new ListBox();
    loggingDropDown.setName("logLevel");
    loggingDropDown.addItem(Messages.getString("none"), "WARN");
    loggingDropDown.addItem(Messages.getString("short"), "INFO");
    loggingDropDown.addItem(Messages.getString("verbose", "DEBUG"));
    loggingDropDown.setVisibleItemCount(1);
    disclosureContent.add(loggingDropDown);   
    
    mainPanel.add(disclosureContent);
    disclosurePanel.setContent(mainPanel);
    rootPanel.add(disclosurePanel);

    importDir.setName("importDir");
    importDir.setText(repositoryFile.getPath());
    importDir.setVisible(false);

    rootPanel.add(importDir);

    form.setEncoding(FormPanel.ENCODING_MULTIPART);
    form.setMethod(FormPanel.METHOD_POST);

    setFormAction();

    form.add(rootPanel);

    setContent(form);
  }
  
  private static native void logWindow(String innerText) /*-{
  	var logWindow = window.open('', 'Import Results', 'width=640, height=480');
    logWindow.document.write(innerText);
  }-*/;

  private void setFormAction() {
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
    String importURL = contextURL + "api/repo/files/import";
    form.setAction(importURL);
  }

  public FormPanel getForm() {
    return form;
  }

  protected void onOk() {
    IDialogCallback callback = this.getCallback();
    IDialogValidatorCallback validatorCallback = this.getValidatorCallback();
    if (validatorCallback == null || (validatorCallback != null && validatorCallback.validate())) {
      try {
        if (callback != null) {
          setFormAction();
          callback.okPressed();
        }
      } catch (Throwable dontCare) {
      }
    }
  }
}
