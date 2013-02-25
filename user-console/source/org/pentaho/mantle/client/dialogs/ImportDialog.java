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
import org.pentaho.gwt.widgets.client.listbox.CustomListBox;
import org.pentaho.gwt.widgets.client.listbox.DefaultListItem;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
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
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

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
        String result = sce.getResults();
        if (result.length() > 5) {
          logWindow(result,Messages.getString("importLogWindowTitle"));
        } else {
          Window.alert (Messages.getString("importSuccessMessage"));
        }
        
      }
    });

    VerticalPanel rootPanel = new VerticalPanel();
    
    VerticalPanel spacer = new VerticalPanel();
    spacer.setHeight("10px");
    rootPanel.add(spacer);
    
    Label fileLabel = new Label(Messages.getString("file") + ":");
    final TextBox importDir = new TextBox();
    rootPanel.add(fileLabel);
    
    okButton.setEnabled(false);
    
    final TextBox fileTextBox = new TextBox();
    fileTextBox.setHeight("26px");
    fileTextBox.setEnabled(false);
    
    final FileUpload upload = new FileUpload();
    upload.setName("fileUpload");
    ChangeHandler fileUploadHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
    	fileTextBox.setText(upload.getFilename());
        if (!"".equals(importDir.getValue())) {
          okButton.setEnabled(true);
        } else {
          okButton.setEnabled(false);
        }
      }
    };
    upload.addChangeHandler(fileUploadHandler);
    upload.setVisible(false);
    
    HorizontalPanel fileUploadPanel = new HorizontalPanel();
    fileUploadPanel.add(fileTextBox);
    fileUploadPanel.add(new HTML("&nbsp;")); 
    
    Button browseButton = new Button(Messages.getString("browse") +  "...");
    browseButton.setStyleName("pentaho-button");
    fileUploadPanel.add(browseButton);
    browseButton.addClickHandler(
      new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
		  jsClickUpload(upload.getElement());
		}
	  }
	);

    rootPanel.add(fileUploadPanel);
    rootPanel.add(upload);
    
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

    rootPanel.add(applyAclPermissions);
    rootPanel.add(overwriteAclPermissions);
    
    spacer = new VerticalPanel();
    spacer.setHeight("4px");
    rootPanel.add(spacer);
    
    DisclosurePanel disclosurePanel = new DisclosurePanel(Messages.getString("advancedOptions"));
    disclosurePanel.getHeader().setStyleName("gwt-Label");
    HorizontalPanel mainPanel = new HorizontalPanel();
    mainPanel.add(new HTML("&nbsp;"));
    VerticalPanel disclosureContent = new VerticalPanel();
    
    HTML replaceLabel = new HTML(Messages.getString("fileExists"));
    replaceLabel.setStyleName("gwt-Label");
    disclosureContent.add(replaceLabel);
    
    final CustomListBox overwriteFile = new CustomListBox();    
    overwriteFile.getElement().getElementsByTagName("input").getItem(0).setPropertyString("name", "overwriteFile");
    DefaultListItem replaceListItem = new DefaultListItem(Messages.getString("replaceFile"));
    replaceListItem.setValue("true");
    overwriteFile.addItem(replaceListItem);
    DefaultListItem doNotImportListItem = new DefaultListItem(Messages.getString("doNotImport"));
    doNotImportListItem.setValue("false");
    overwriteFile.addItem(doNotImportListItem);
    overwriteFile.setVisibleRowCount(1);
    disclosureContent.add(overwriteFile);
    
    spacer = new VerticalPanel();
    spacer.setHeight("4px");
    disclosureContent.add(spacer);
    
    HTML filePermissionsLabel = new HTML(Messages.getString("filePermissions"));
    filePermissionsLabel.setStyleName("gwt-Label");
    disclosureContent.add(filePermissionsLabel);
    
    final CustomListBox filePermissionsDropDown = new CustomListBox();
    DefaultListItem retainPermissionsListItem = new DefaultListItem(Messages.getString("retainPermissions"));
    retainPermissionsListItem.setValue("true");
    filePermissionsDropDown.addItem(retainPermissionsListItem); //If selected set "overwriteAclPermissions" to true.
    DefaultListItem usePermissionsListItem = new DefaultListItem(Messages.getString("usePermissions"));
    usePermissionsListItem.setValue("false");
    filePermissionsDropDown.addItem(usePermissionsListItem); //If selected set "overwriteAclPermissions" to false.
    DefaultListItem removePermissionsListItem = new DefaultListItem(Messages.getString("removePermissions"));
    removePermissionsListItem.setValue("none");
    filePermissionsDropDown.addItem(removePermissionsListItem); //If selected then set "applyAclPermissions" to false else true.    
    
    ChangeListener filePermissionsHandler = new ChangeListener() {
        @Override
        public void onChange(Widget sender) {
          String value = filePermissionsDropDown.getSelectedItem().getValue().toString();
          filePermissionsDropDown.getElement().getElementsByTagName("input").getItem(0).setPropertyString("value", value);
          
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
    filePermissionsDropDown.addChangeListener(filePermissionsHandler);
    filePermissionsDropDown.setVisibleRowCount(1);
    disclosureContent.add(filePermissionsDropDown);
    
    spacer = new VerticalPanel();
    spacer.setHeight("4px");
    disclosureContent.add(spacer);
    
    HTML fileOwnershipLabel = new HTML(Messages.getString("fileOwnership"));
    fileOwnershipLabel.setStyleName("gwt-Label");
    disclosureContent.add(fileOwnershipLabel);
    
    final CustomListBox retainOwnership = new CustomListBox();
    retainOwnership.getElement().getElementsByTagName("input").getItem(0).setPropertyString("name", "retainOwnership");
    retainOwnership.addChangeListener(new ChangeListener() {
		public void onChange(Widget sender) {
			retainOwnership.getElement().getElementsByTagName("input").getItem(0).setPropertyString("value", retainOwnership.getSelectedItem().getValue().toString());
		}
	});
    DefaultListItem keepOwnershipListItem = new DefaultListItem(Messages.getString("keepOwnership"));
    keepOwnershipListItem.setValue("true");
    retainOwnership.addItem(keepOwnershipListItem);    
    DefaultListItem assignOwnershipListItem = new DefaultListItem(Messages.getString("assignOwnership"));
    assignOwnershipListItem.setValue("false");
    retainOwnership.addItem(assignOwnershipListItem); 
    
    retainOwnership.setVisibleRowCount(1);
    disclosureContent.add(retainOwnership);
    
    spacer = new VerticalPanel();
    spacer.setHeight("4px");
    disclosureContent.add(spacer);
    
    ChangeListener overwriteFileHandler = new ChangeListener() {
        @Override
        public void onChange(Widget sender) {
        	String value = overwriteFile.getSelectedItem().getValue().toString();
        	overwriteFile.getElement().getElementsByTagName("input").getItem(0).setPropertyString("value", value);
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
    overwriteFile.addChangeListener(overwriteFileHandler);
    
    HTML loggingLabel = new HTML(Messages.getString("logging"));
    loggingLabel.setStyleName("gwt-Label");
    disclosureContent.add(loggingLabel);
    
    final CustomListBox loggingDropDown = new CustomListBox();
    loggingDropDown.getElement().getElementsByTagName("input").getItem(0).setPropertyString("name", "logLevel");
    loggingDropDown.addChangeListener(new ChangeListener() {
		public void onChange(Widget sender) {
			loggingDropDown.getElement().getElementsByTagName("input").getItem(0).setPropertyString("value", loggingDropDown.getSelectedItem().getValue().toString());
		}
	});
    DefaultListItem noneListItem = new DefaultListItem(Messages.getString("none"));
    noneListItem.setValue("WARN");
    loggingDropDown.addItem(noneListItem);
    DefaultListItem shortListItem = new DefaultListItem(Messages.getString("short"));
    shortListItem.setValue("INFO");
    loggingDropDown.addItem(shortListItem);
    DefaultListItem debugListItem = new DefaultListItem(Messages.getString("verbose"));
    debugListItem.setValue("TRACE");
    loggingDropDown.addItem(debugListItem);
    loggingDropDown.setVisibleRowCount(1);
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
  
  native void jsClickUpload(Element uploadElement) /*-{
    uploadElement.click();
  }-*/;
  
  private static native void logWindow(String innerText, String windowTitle) /*-{
  	var logWindow = window.open('', '', 'width=640, height=480, location=no, menubar=yes, toolbar=yes', false);
  	var htmlText = '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">\
  	  <html><head><title>' + windowTitle + '</title></head><body bgcolor="#FFFFFF" topmargin="6" leftmargin="6">'
  	  + innerText + "</body></html>";
    logWindow.document.write(htmlText);
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
