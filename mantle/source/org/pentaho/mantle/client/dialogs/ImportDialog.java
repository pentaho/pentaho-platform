/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Jan 19, 2011 
 * @author wseyler
 */


package org.pentaho.mantle.client.dialogs;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.IDialogValidatorCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

/**
 * @author wseyler
 *
 */
public class ImportDialog extends PromptDialogBox {
  private FormPanel form;
  protected PopupPanel indefiniteProgress;
  protected FileUpload upload;
  
  /**
   * @param repositoryFile
   */
  public ImportDialog(RepositoryFile repositoryFile) {
    super(Messages.getString("import"), Messages.getString("ok"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    indefiniteProgress = new PopupPanel(false, true);
    DOM.setStyleAttribute(indefiniteProgress.getElement(), "zIndex", "2000"); // Gets it to the front
    Image waitImage = new Image("mantle/large-loading.gif"); //$NON-NLS-1$
    indefiniteProgress.add(waitImage);
    
    form = new FormPanel();
    form.addSubmitHandler(new SubmitHandler() {
      @Override
      public void onSubmit(SubmitEvent se) {
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
        okButton.setEnabled(true);
        cancelButton.setEnabled(true);
        ImportDialog.this.hide();
        Window.alert(sce.getResults());
      }
    });
    
    VerticalPanel rootPanel = new VerticalPanel(); 
    Label importLocationLabel = new Label(Messages.getString("importLocation") + " " + repositoryFile.getPath());
    TextBox importDir = new TextBox();
    rootPanel.add(importLocationLabel);
    
    upload = new FileUpload();
    upload.setName("fileUpload");
    rootPanel.add(upload);
    
    importDir.setName("importDir");
    importDir.setText(repositoryFile.getPath());
    importDir.setVisible(false);
    rootPanel.add(importDir);

    form.setEncoding(FormPanel.ENCODING_MULTIPART);
    form.setMethod(FormPanel.METHOD_POST);
    
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
    String importURL = contextURL + "api/repo/files/import";
    form.setAction(importURL);
    
    form.add(rootPanel);
    
    setContent(form);
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
          callback.okPressed();
        }
      } catch (Throwable dontCare) {
      }
    }
  }
}
