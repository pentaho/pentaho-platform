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
 * Copyright (c) 2002-2023 Hitachi Vantara. All rights reserved.
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
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.listbox.CustomListBox;
import org.pentaho.gwt.widgets.client.listbox.DefaultListItem;
import org.pentaho.gwt.widgets.client.panel.HorizontalFlexPanel;
import org.pentaho.gwt.widgets.client.panel.VerticalFlexPanel;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.csrf.CsrfUtil;
import org.pentaho.mantle.client.csrf.JsCsrfToken;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.GenericEvent;
import org.pentaho.mantle.client.messages.Messages;

/**
 * @author wseyler/modifed for Import parameters by tband
 */
@SuppressWarnings( "deprecation" )
public class ImportDialog extends PromptDialogBox {

  private static final String UPLOAD_ACCESS_DENIED_SNIPPET = "UnifiedRepositoryAccessDeniedException";
  private static final String IMPORT_API_PATH = "api/repo/files/import";

  /**
   * The name of the CSRF token field to use when CSRF protection is disabled.
   *
   * An arbitrary name, yet different from the name it can have when CSRF protection enabled.
   * This avoids not having to dynamically adding and removing the field from the form depending
   * on whether CSRF protection is enabled or not.
   *
   * When CSRF protection is enabled,
   * the actual name of the field is set before each submit.
   */
  private static final String DISABLED_CSRF_TOKEN_PARAMETER = "csrf_token_disabled";

  private final FormPanel form;

  /**
   * The CSRF token field/parameter.
   * Its name and value are set to the expected values before each submit,
   * to match the obtained {@link JsCsrfToken}.
   *
   * The Tomcat's context must have the `allowCasualMultipartParsing` attribute set
   * so that the `CsrfGateFilter` is able to transparently read this parameter
   * in a multi-part encoding form, as is the case of `form`.
   */
  private final Hidden csrfTokenParameter;

  private final CustomListBox retainOwnershipDropDown = new CustomListBox();
  final CheckBox applyAclPermissions = new CheckBox( Messages.getString( "applyAclPermissions" ), true );

  /**
   * @param repositoryFile
   * @param allowAdvancedDialog
   */
  public ImportDialog( RepositoryFile repositoryFile, boolean allowAdvancedDialog ) {
    super( Messages.getString( "import" ), Messages.getString( "ok" ), Messages.getString( "cancel" ), false, true ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    addStyleName( "import-dialog" );
    setResponsive( true );
    setSizingMode( DialogSizingMode.FILL_VIEWPORT_WIDTH );
    setWidthCategory( DialogWidthCategory.EXTRA_SMALL );

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
          if ( result.contains( "INVALID_MIME_TYPE" )  ) {
            MessageDialogBox dialogBox = new MessageDialogBox(
                    Messages.getString( "uploadUnsuccessful" ),
                    Messages.getString( "uploadInvalidFileTypeQuestion", result ),
                    true,
                    Messages.getString( "close" ) );
            dialogBox.center();
          } else if ( result.contains( UPLOAD_ACCESS_DENIED_SNIPPET ) ) {
            MessageDialogBox messageDialogBox = new MessageDialogBox(
                    Messages.getString( "uploadUnsuccessful" ),
                    Messages.getString( "uploadFolderAccessDenied" ),
                    true,
                    Messages.getString( "ok" ) );
            messageDialogBox.center();
          } else {
            logWindow( result, Messages.getString( "importLogWindowTitle" ) );
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

    VerticalPanel rootPanel = new VerticalFlexPanel();
    rootPanel.addStyleName( "import-dialog-root-panel" );

    VerticalPanel spacer = new VerticalPanel();
    spacer.setHeight( "10px" );
    rootPanel.add( spacer );

    Label fileLabel = new Label( Messages.getString( "file" ) + ":" );
    final TextBox importDir = new TextBox();
    rootPanel.add( fileLabel );

    okButton.setEnabled( false );

    final TextBox fileTextBox = new TextBox();
    fileTextBox.setEnabled( false );

    //We use an fileNameOverride because FileUpload can only handle US character set reliably.
    final Hidden fileNameOverride = new Hidden( "fileNameOverride" );

    final FileUpload upload = new FileUpload();
    upload.setName( "fileUpload" );
    ChangeHandler fileUploadHandler = new ChangeHandler() {
      @Override
      public void onChange( ChangeEvent event ) {
        fileTextBox.setText( upload.getFilename() );
        if ( !"".equals( importDir.getValue() ) ) {
          //Set the fileNameOverride because the fileUpload object can only reliably transmit US-ASCII
          //character set.  See RFC283 section 2.3 for details
          String fileNameValue = upload.getFilename().replaceAll( "\\\\", "/" );
          fileNameValue = fileNameValue.substring( fileNameValue.lastIndexOf( "/" ) + 1 );
          fileNameOverride.setValue( fileNameValue );
          okButton.setEnabled( true );
        } else {
          okButton.setEnabled( false );
        }
      }
    };
    upload.addChangeHandler( fileUploadHandler );
    upload.setVisible( false );

    HorizontalPanel fileUploadPanel = new HorizontalFlexPanel();
    fileUploadPanel.addStyleName( "file-upload-panel" );
    fileUploadPanel.add( fileTextBox );
    fileUploadPanel.add( new HTML( "&nbsp;" ) );

    Button browseButton = new Button( Messages.getString( "browse" ) + "..." );
    browseButton.setStyleName( "pentaho-button" );
    fileUploadPanel.add( browseButton );
    browseButton.addClickHandler( new ClickHandler() {
      @Override
      public void onClick( ClickEvent event ) {
        setRetainOwnershipState();
        jsClickUpload( upload.getElement() );
      }
    } );

    rootPanel.add( fileUploadPanel );
    rootPanel.add( upload );

    applyAclPermissions.setName( "applyAclPermissions" );
    applyAclPermissions.setValue( Boolean.FALSE );
    applyAclPermissions.setFormValue( "false" );
    applyAclPermissions.setEnabled( true );
    applyAclPermissions.setVisible( false );

    final CheckBox overwriteAclPermissions = new CheckBox( Messages.getString( "overwriteAclPermissions" ), true );
    overwriteAclPermissions.setName( "overwriteAclPermissions" );
    applyAclPermissions.setValue( Boolean.FALSE );
    applyAclPermissions.setFormValue( "false" );
    overwriteAclPermissions.setEnabled( true );
    overwriteAclPermissions.setVisible( false );

    final Hidden overwriteFile = new Hidden( "overwriteFile" );
    overwriteFile.setValue( "true" );

    final Hidden logLevel = new Hidden( "logLevel" );
    logLevel.setValue( "ERROR" );

    final Hidden retainOwnership = new Hidden( "retainOwnership" );
    retainOwnership.setValue( "true" );

    csrfTokenParameter = new Hidden( DISABLED_CSRF_TOKEN_PARAMETER );

    rootPanel.add( applyAclPermissions );
    rootPanel.add( overwriteAclPermissions );
    rootPanel.add( overwriteFile );
    rootPanel.add( logLevel );
    rootPanel.add( retainOwnership );
    rootPanel.add( fileNameOverride );
    rootPanel.add( csrfTokenParameter );

    spacer = new VerticalPanel();
    spacer.setHeight( "4px" );
    rootPanel.add( spacer );

    DisclosurePanel disclosurePanel = new DisclosurePanel( Messages.getString( "advancedOptions" ) );
    disclosurePanel.getHeader().setStyleName( "gwt-Label" );
    disclosurePanel.addStyleName( "import-dialog-disclosure-panel" );
    disclosurePanel.addStyleName( VerticalFlexPanel.STYLE_NAME );
    disclosurePanel.setVisible( allowAdvancedDialog );
    HorizontalPanel mainPanel = new HorizontalFlexPanel();
    mainPanel.add( new HTML( "&nbsp;" ) );
    VerticalPanel disclosureContent = new VerticalFlexPanel();
    disclosureContent.addStyleName( "import-dialog-disclosure-content-panel" );

    HTML replaceLabel = new HTML( Messages.getString( "fileExists" ) );
    replaceLabel.setStyleName( "gwt-Label" );
    disclosureContent.add( replaceLabel );

    final CustomListBox overwriteFileDropDown = new CustomListBox();
    final CustomListBox filePermissionsDropDown = new CustomListBox();

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

    DefaultListItem usePermissionsListItem = new DefaultListItem( Messages.getString( "usePermissions" ) );
    usePermissionsListItem.setValue( "false" );
    filePermissionsDropDown.addItem( usePermissionsListItem ); // If selected set "overwriteAclPermissions" to
    // false.
    DefaultListItem retainPermissionsListItem = new DefaultListItem( Messages.getString( "retainPermissions" ) );
    retainPermissionsListItem.setValue( "true" );
    filePermissionsDropDown.addItem( retainPermissionsListItem ); // If selected set "overwriteAclPermissions" to
    // true.

    final ChangeListener filePermissionsHandler = new ChangeListener() {
      @Override
      public void onChange( Widget sender ) {
        String value = filePermissionsDropDown.getSelectedItem().getValue().toString();

        applyAclPermissions.setValue( Boolean.valueOf( value ) );
        applyAclPermissions.setFormValue( value );
        overwriteAclPermissions.setFormValue( value );
        overwriteAclPermissions.setValue( Boolean.valueOf( value ) );
        setRetainOwnershipState();
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
      }
    };
    overwriteFileDropDown.addChangeListener( overwriteFileHandler );

    HTML loggingLabel = new HTML( Messages.getString( "logging" ) );
    loggingLabel.setStyleName( "gwt-Label" );
    disclosureContent.add( loggingLabel );

    final CustomListBox loggingDropDown = new CustomListBox();
    loggingDropDown.addChangeListener( new ChangeListener() {
      public void onChange( Widget sender ) {
        String value = loggingDropDown.getSelectedItem().getValue().toString();
        logLevel.setValue( value );
      }
    } );
    DefaultListItem noneListItem = new DefaultListItem( Messages.getString( "none" ) );
    noneListItem.setValue( "ERROR" );
    loggingDropDown.addItem( noneListItem );
    DefaultListItem shortListItem = new DefaultListItem( Messages.getString( "short" ) );
    shortListItem.setValue( "WARN" );
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

    setupFormAction();

    form.add( rootPanel );

    setContent( form );
  }

  native void jsClickUpload( Element uploadElement )
  /*-{
     uploadElement.click();
  }-*/;

  private static native void logWindow( String innerText, String windowTitle )
  /*-{
     var logWindow = window.open('', '', 'width=640, height=480, location=no, menubar=yes, toolbar=yes', false);
     var htmlText = '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">\
     <html><head><title>' + windowTitle + '</title></head><body bgcolor="#FFFFFF" topmargin="6" leftmargin="6">'
     + innerText + "</body></html>";
     logWindow.document.write(htmlText);
  }-*/;

  /**
   * Setups the form panel action URL, having in account the GWT context URL.
   */
  private void setupFormAction() {
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );
    String fullURL = contextURL + IMPORT_API_PATH;
    form.setAction( fullURL );
  }

  /**
   * Obtains a CSRF token for the form's current URL and
   * fills it in the form's token parameter hidden field.
   */
  private void setupCsrfToken() {
    JsCsrfToken token = CsrfUtil.getCsrfTokenSync( form.getAction() );
    if ( token != null ) {
      csrfTokenParameter.setName( token.getParameter() );
      csrfTokenParameter.setValue( token.getToken() );
    } else {
      // Reset the field.
      csrfTokenParameter.setName( DISABLED_CSRF_TOKEN_PARAMETER );
      csrfTokenParameter.setValue( "" );
    }
  }

  public FormPanel getForm() {
    return form;
  }

  protected void onOk() {
    IDialogCallback callback = this.getCallback();
    IDialogValidatorCallback validatorCallback = this.getValidatorCallback();
    if ( validatorCallback == null || validatorCallback.validate() ) {
      try {
        if ( callback != null ) {
          setupFormAction();
          setupCsrfToken();

          callback.okPressed();
        }
      } catch ( Throwable dontCare ) {
        // ignored
      }
    }
  }

  private static native void setBrowseRepoDirty( boolean isDirty )
  /*-{
      $wnd.mantle_isBrowseRepoDirty = isDirty;
  }-*/;

  // This is a work around of a GWT Widget bug. If you disable a CustomerListBox before
  // it is rendered it will stay two lines even when setVisibleRowCount( 1 ) is called.
  // Furthermore, the listbox will not be resized to fit the text. By forcing the list
  // box to disable during the browse button hander, instead of prior to displaying, we
  // circumvent this problem while still forcing the disabled state, as needed.
  private void setRetainOwnershipState() {
    if ( !applyAclPermissions.getValue() ) {
      retainOwnershipDropDown.setSelectedIndex( 0 );
      retainOwnershipDropDown.setEnabled( false );
    } else {
      retainOwnershipDropDown.setEnabled( true );
    }
  }
}
