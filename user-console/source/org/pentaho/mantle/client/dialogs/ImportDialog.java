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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author wseyler/modifed for Import parameters by tband
 *
 */
public class ImportDialog extends PromptDialogBox {
  private FormPanel form;

  protected PopupPanel indefiniteProgress;

  protected FileUpload upload;

  protected CheckBox overwrite = null;

  protected CheckBox permission = null;

  protected CheckBox retainOwnership = null;

  protected TextBox importDir = null;

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
        //Window.alert(sce.getResults());
      }
    });

    VerticalPanel rootPanel = new VerticalPanel();
    Label importLocationLabel = new Label(Messages.getString("importLocation") + " " + repositoryFile.getPath());
    importDir = new TextBox();
    rootPanel.add(importLocationLabel);

    //HorizontalPanel hpanel = new HorizontalPanel(); 
    permission = new CheckBox(Messages.getString("applyAclPermissions"), true);
    permission.setName("applyAclPermissions");
    permission.setValue(Boolean.TRUE);
    permission.setFormValue("true");
    ClickHandler phandler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        permission.setFormValue(permission.getValue() ? "true" : "false");
        overwrite.setFormValue(overwrite.getValue() ? "true" : "false");
        overwrite.setEnabled(permission.getValue() ? true : false);
        if(!permission.getValue()){
          overwrite.setFormValue("false");
        }
      }
    };
    permission.addClickHandler(phandler);
    
    overwrite = new CheckBox(Messages.getString("overwriteAclPermissions"), true);
    overwrite.setName("overwriteAclPermissions");
    overwrite.setFormValue("true");
    overwrite.setEnabled(true);
    overwrite.setValue(Boolean.TRUE);
    ClickHandler handler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        overwrite.setFormValue(permission.getValue() ? "true" : "false");
      }
    };
    overwrite.addClickHandler(handler);

    retainOwnership = new CheckBox(Messages.getString("retainOwnership"), true);
    retainOwnership.setName("retainOwnership");
    retainOwnership.setFormValue("true");
    retainOwnership.setValue(Boolean.TRUE);
    ClickHandler rhandler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        retainOwnership.setFormValue(retainOwnership.getValue() ? "true" : "false");
      }
    };
    retainOwnership.addClickHandler(rhandler);
    okButton.setEnabled(false);
    upload = new FileUpload();
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

    rootPanel.add(permission);
    rootPanel.add(overwrite);
    rootPanel.add(retainOwnership);

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
