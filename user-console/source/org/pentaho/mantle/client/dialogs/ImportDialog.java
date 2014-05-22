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

package org.pentaho.mantle.client.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
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
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.IDialogValidatorCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.listbox.CustomListBox;
import org.pentaho.gwt.widgets.client.listbox.DefaultListItem;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.GenericEvent;
import org.pentaho.mantle.client.messages.Messages;

/**
 * @author wseyler/modifed for Import parameters by tband
 */
@SuppressWarnings( "deprecation" )
public class ImportDialog extends PromptDialogBox {

  private FormPanel form;

  /**
   * @param repositoryFile
   */
  public ImportDialog( RepositoryFile repositoryFile ) {
    super( Messages.getString( "import" ), Messages.getString( "ok" ), Messages.getString( "cancel" ), false, true ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    form = new FormPanel();
    form.addSubmitHandler( new SubmitHandler() {
      @Override
      public void onSubmit( SubmitEvent se ) {
        // if no file is selected then do not proceed
        okButton.setEnabled( false );
        cancelButton.setEnabled( false );
        MantleApplication.showBusyIndicator( Messages.getString( "pleaseWait" ), Messages
            .getString( "importInProgress" ) );
      }
    } );
    form.addSubmitCompleteHandler( new SubmitCompleteHandler() {
      @Override
      public void onSubmitComplete( SubmitCompleteEvent sce ) {
        MantleApplication.hideBusyIndicator();
        okButton.setEnabled( false );
        cancelButton.setEnabled( true );
        ImportDialog.this.hide();
        String result = sce.getResults();
        if ( result.length() > 5 ) {
          HTML messageTextBox = null;
          if ( result.contains( "INVALID_MIME_TYPE" ) == true ) {
            messageTextBox = new HTML( Messages.getString( "uploadInvalidFileTypeQuestion", result ) );
          } else {
            logWindow( result, Messages.getString( "importLogWindowTitle" ) );
          }

          if ( messageTextBox != null ) {
            PromptDialogBox dialogBox =
                new PromptDialogBox( Messages.getString( "uploadUnsuccessful" ), Messages.getString( "close" ), null,
                    true, true );
            dialogBox.setContent( messageTextBox );
            dialogBox.center();
          }
        }

        // if mantle_isBrowseRepoDirty=true: do getChildren call 
        // if mantle_isBrowseRepoDirty=false: use stored fileBrowserModel in myself.get("cachedData")
        setBrowseRepoDirty( Boolean.TRUE );

        // BISERVER-9319 Refresh browse perspective after import
        final GenericEvent event = new GenericEvent();
        event.setEventSubType( "ImportDialogEvent" );
        EventBusUtil.EVENT_BUS.fireEvent( event );
      }
    } );

    VerticalPanel rootPanel = new VerticalPanel();

    VerticalPanel spacer = new VerticalPanel();
    spacer.setHeight( "10px" );
    rootPanel.add( spacer );

    Label fileLabel = new Label( Messages.getString( "file" ) + ":" );
    final TextBox importDir = new TextBox();
    rootPanel.add( fileLabel );

    okButton.setEnabled( false );

    final TextBox fileTextBox = new TextBox();
    fileTextBox.setHeight( "26px" );
    fileTextBox.setEnabled( false );

    final FileUpload upload = new FileUpload();
    upload.setName( "fileUpload" );
    ChangeHandler fileUploadHandler = new ChangeHandler() {
      @Override
      public void onChange( ChangeEvent event ) {
        fileTextBox.setText( upload.getFilename() );
        if ( !"".equals( importDir.getValue() ) ) {
          okButton.setEnabled( true );
        } else {
          okButton.setEnabled( false );
        }
      }
    };
    upload.addChangeHandler( fileUploadHandler );
    upload.setVisible( false );

    HorizontalPanel fileUploadPanel = new HorizontalPanel();
    fileUploadPanel.add( fileTextBox );
    fileUploadPanel.add( new HTML( "&nbsp;" ) );

    Button browseButton = new Button( Messages.getString( "browse" ) + "..." );
    browseButton.setStyleName( "pentaho-button" );
    fileUploadPanel.add( browseButton );
    browseButton.addClickHandler( new ClickHandler() {
      @Override
      public void onClick( ClickEvent event ) {
        jsClickUpload( upload.getElement() );
      }
    } );

    rootPanel.add( fileUploadPanel );
    rootPanel.add( upload );

    final CheckBox applyAclPermissions = new CheckBox( Messages.getString( "applyAclPermissions" ), true );
    applyAclPermissions.setName( "applyAclPermissions" );
    applyAclPermissions.setValue( Boolean.TRUE );
    applyAclPermissions.setFormValue( "true" );
    applyAclPermissions.setEnabled( true );
    applyAclPermissions.setVisible( false );

    final CheckBox overwriteAclPermissions = new CheckBox( Messages.getString( "overwriteAclPermissions" ), true );
    overwriteAclPermissions.setName( "overwriteAclPermissions" );
    overwriteAclPermissions.setValue( Boolean.FALSE );
    overwriteAclPermissions.setFormValue( "false" );
    overwriteAclPermissions.setEnabled( true );
    overwriteAclPermissions.setVisible( false );

    final Hidden overwriteFile = new Hidden( "overwriteFile" );
    overwriteFile.setValue( "true" );

    final Hidden logLevel = new Hidden( "logLevel" );
    logLevel.setValue( "WARN" );

    final Hidden retainOwnership = new Hidden( "retainOwnership" );
    retainOwnership.setValue( "true" );

    rootPanel.add( applyAclPermissions );
    rootPanel.add( overwriteAclPermissions );
    rootPanel.add( overwriteFile );
    rootPanel.add( logLevel );
    rootPanel.add( retainOwnership );

    spacer = new VerticalPanel();
    spacer.setHeight( "4px" );
    rootPanel.add( spacer );

    DisclosurePanel disclosurePanel = new DisclosurePanel( Messages.getString( "advancedOptions" ) );
    disclosurePanel.getHeader().setStyleName( "gwt-Label" );
    HorizontalPanel mainPanel = new HorizontalPanel();
    mainPanel.add( new HTML( "&nbsp;" ) );
    VerticalPanel disclosureContent = new VerticalPanel();

    HTML replaceLabel = new HTML( Messages.getString( "fileExists" ) );
    replaceLabel.setStyleName( "gwt-Label" );
    disclosureContent.add( replaceLabel );

    final CustomListBox overwriteFileDropDown = new CustomListBox();
    DefaultListItem replaceListItem = new DefaultListItem( Messages.getString( "replaceFile" ) );
    replaceListItem.setValue( "true" );
    overwriteFileDropDown.addItem( replaceListItem );
    DefaultListItem doNotImportListItem = new DefaultListItem( Messages.getString( "doNotImport" ) );
    doNotImportListItem.setValue( "false" );
    overwriteFileDropDown.addItem( doNotImportListItem );
    overwriteFileDropDown.setVisibleRowCount( 1 );
    disclosureContent.add( overwriteFileDropDown );

    spacer = new VerticalPanel();
    spacer.setHeight( "4px" );
    disclosureContent.add( spacer );

    HTML filePermissionsLabel = new HTML( Messages.getString( "filePermissions" ) );
    filePermissionsLabel.setStyleName( "gwt-Label" );
    disclosureContent.add( filePermissionsLabel );

    final CustomListBox filePermissionsDropDown = new CustomListBox();
    DefaultListItem usePermissionsListItem = new DefaultListItem( Messages.getString( "usePermissions" ) );
    usePermissionsListItem.setValue( "false" );
    filePermissionsDropDown.addItem( usePermissionsListItem ); // If selected set "overwriteAclPermissions" to
    // false.
    DefaultListItem retainPermissionsListItem = new DefaultListItem( Messages.getString( "retainPermissions" ) );
    retainPermissionsListItem.setValue( "true" );
    filePermissionsDropDown.addItem( retainPermissionsListItem ); // If selected set "overwriteAclPermissions" to
    // true.
    DefaultListItem removePermissionsListItem = new DefaultListItem( Messages.getString( "removePermissions" ) );
    removePermissionsListItem.setValue( "none" );
    filePermissionsDropDown.addItem( removePermissionsListItem ); // If selected then set "applyAclPermissions" to
    // false else true.

    final ChangeListener filePermissionsHandler = new ChangeListener() {
      @Override
      public void onChange( Widget sender ) {
        String value = filePermissionsDropDown.getSelectedItem().getValue().toString();

        applyAclPermissions.setValue( Boolean.TRUE );
        applyAclPermissions.setFormValue( "true" );

        overwriteAclPermissions.setFormValue( value );
        overwriteAclPermissions.setValue( Boolean.valueOf( value ) );

        if ( value.equals( "none" ) ) {
          applyAclPermissions.setValue( Boolean.FALSE );
          applyAclPermissions.setFormValue( "false" );
          overwriteAclPermissions.setValue( Boolean.FALSE );
          overwriteAclPermissions.setFormValue( "false" );
        }
      }
    };

    filePermissionsDropDown.addChangeListener( filePermissionsHandler );
    filePermissionsDropDown.setVisibleRowCount( 1 );
    disclosureContent.add( filePermissionsDropDown );

    spacer = new VerticalPanel();
    spacer.setHeight( "4px" );
    disclosureContent.add( spacer );

    HTML fileOwnershipLabel = new HTML( Messages.getString( "fileOwnership" ) );
    fileOwnershipLabel.setStyleName( "gwt-Label" );
    disclosureContent.add( fileOwnershipLabel );

    final CustomListBox retainOwnershipDropDown = new CustomListBox();
    retainOwnershipDropDown.addChangeListener( new ChangeListener() {
      @Override
      public void onChange( Widget sender ) {
        String value = retainOwnershipDropDown.getSelectedItem().getValue().toString();
        retainOwnership.setValue( value );
      }
    } );
    DefaultListItem keepOwnershipListItem = new DefaultListItem( Messages.getString( "keepOwnership" ) );
    keepOwnershipListItem.setValue( "true" );
    retainOwnershipDropDown.addItem( keepOwnershipListItem );
    DefaultListItem assignOwnershipListItem = new DefaultListItem( Messages.getString( "assignOwnership" ) );
    assignOwnershipListItem.setValue( "false" );
    retainOwnershipDropDown.addItem( assignOwnershipListItem );

    retainOwnershipDropDown.setVisibleRowCount( 1 );
    disclosureContent.add( retainOwnershipDropDown );

    spacer = new VerticalPanel();
    spacer.setHeight( "4px" );
    disclosureContent.add( spacer );

    ChangeListener overwriteFileHandler = new ChangeListener() {
      @Override
      public void onChange( Widget sender ) {
        String value = overwriteFileDropDown.getSelectedItem().getValue().toString();
        overwriteFile.setValue( value );
        if ( value.equals( "false" ) ) {
          filePermissionsDropDown.setSelectedIndex( 1 );
          filePermissionsDropDown.setEnabled( false );
          retainOwnershipDropDown.setSelectedIndex( 0 );
          retainOwnershipDropDown.setEnabled( false );
        } else if ( value.equals( "true" ) ) {
          filePermissionsDropDown.setEnabled( true );
          retainOwnershipDropDown.setEnabled( true );
        }
      }
    };
    overwriteFileDropDown.addChangeListener( overwriteFileHandler );

    HTML loggingLabel = new HTML( Messages.getString( "logging" ) );
    loggingLabel.setStyleName( "gwt-Label" );
    disclosureContent.add( loggingLabel );

    final CustomListBox loggingDropDown = new CustomListBox();
    loggingDropDown.addChangeListener( new ChangeListener() {
      @Override
      public void onChange( Widget sender ) {
        String value = loggingDropDown.getSelectedItem().getValue().toString();
        logLevel.setValue( value );
      }
    } );
    DefaultListItem noneListItem = new DefaultListItem( Messages.getString( "none" ) );
    noneListItem.setValue( "WARN" );
    loggingDropDown.addItem( noneListItem );
    DefaultListItem shortListItem = new DefaultListItem( Messages.getString( "short" ) );
    shortListItem.setValue( "INFO" );
    loggingDropDown.addItem( shortListItem );
    DefaultListItem debugListItem = new DefaultListItem( Messages.getString( "verbose" ) );
    debugListItem.setValue( "TRACE" );
    loggingDropDown.addItem( debugListItem );
    loggingDropDown.setVisibleRowCount( 1 );
    disclosureContent.add( loggingDropDown );

    mainPanel.add( disclosureContent );
    disclosurePanel.setContent( mainPanel );
    rootPanel.add( disclosurePanel );

    importDir.setName( "importDir" );
    importDir.setText( repositoryFile.getPath() );
    importDir.setVisible( false );

    rootPanel.add( importDir );

    form.setEncoding( FormPanel.ENCODING_MULTIPART );
    form.setMethod( FormPanel.METHOD_POST );

    setFormAction();

    form.add( rootPanel );

    setContent( form );
  }

  native void jsClickUpload( Element uploadElement ) /*-{
      uploadElement.click();
  }-*/;

  private static native void logWindow( String innerText, String windowTitle ) /*-{
      var logWindow = window.open('', '', 'width=640, height=480, location=no, menubar=yes, toolbar=yes', false);
      var htmlText = '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">\
  	  <html><head><title>' + windowTitle + '</title></head><body bgcolor="#FFFFFF" topmargin="6" leftmargin="6">'
          + innerText + "</body></html>";
      logWindow.document.write(htmlText);
  }-*/;

  private void setFormAction() {
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );
    String importURL = contextURL + "api/repo/files/import";
    form.setAction( importURL );
  }

  public FormPanel getForm() {
    return form;
  }

  protected void onOk() {
    IDialogCallback callback = this.getCallback();
    IDialogValidatorCallback validatorCallback = this.getValidatorCallback();
    if ( validatorCallback == null || ( validatorCallback != null && validatorCallback.validate() ) ) {
      try {
        if ( callback != null ) {
          setFormAction();
          callback.okPressed();
        }
      } catch ( Throwable dontCare ) {
        //ignored
      }
    }
  }

  private static native void setBrowseRepoDirty( boolean isDirty )
  /*-{
      $wnd.mantle_isBrowseRepoDirty = isDirty;
  }-*/;
}
