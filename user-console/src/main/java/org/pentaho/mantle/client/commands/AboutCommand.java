/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.mantle.client.commands;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.messages.Messages;

import java.util.Date;

public class AboutCommand extends AbstractCommand {
  private static final String LICENSE_FILE_URL = GWT.getHostPageBaseURL() + "mantle/LICENSE.TXT";

  private static final String LICENSE_READ_ERROR =
    "Error reading license file from server URL: \"" + LICENSE_FILE_URL + "\"";

  private String versionText;
  private String licenseText;

  public AboutCommand() {
  }

  protected void performOperation() {
    performOperation( true );
  }

  /**
   * Begins callback chain for About window values then opens the window:
   * -> retrieve version (if not set)
   * -> retrieve license text (if not set nor error)
   * -> show the window
   */
  protected void performOperation( boolean feedback ) {
    retrieveVersionValue();
  }


  //Setters are here to be accessible in RequestCallbacks
  private void setVersionText( String text ) {
    versionText = text;
  }

  private void setLicenseFileText( String text ) {
    licenseText = text.replace( "\t", "&emsp;" );
    licenseText = "<pre>" + licenseText + "</pre>";
  }

  private void retrieveVersionValue() {
    if ( versionText != null && !versionText.isEmpty() ) {
      retrieveLicenseFileText();
    } else if ( StringUtils.isEmpty( MantleApplication.mantleRevisionOverride ) == false ) {
      setVersionText( MantleApplication.mantleRevisionOverride );
      retrieveLicenseFileText();
    } else {
      final String url = GWT.getHostPageBaseURL() + "api/version/show"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
      requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      requestBuilder.setHeader( "accept", "text/plain" );
      try {
        requestBuilder.sendRequest( null, new RequestCallback() {

          public void onError( Request request, Throwable exception ) {
          }

          public void onResponseReceived( Request request, Response response ) {
            setVersionText( response.getText() );
            retrieveLicenseFileText();
          }
        } );
      } catch ( RequestException e ) {
        Window.alert( e.getMessage() );
      }
    }
  }

  private void retrieveLicenseFileText() {
    //if we already have a license text value that isn't the ERROR value, don't bother getting the value again.
    if ( licenseText != null && !licenseText.isEmpty() && licenseText != LICENSE_READ_ERROR ) {
      showAboutDialog();
    } else {
      RequestBuilder licenseFileRequest = new RequestBuilder( RequestBuilder.GET, LICENSE_FILE_URL );
      licenseFileRequest.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      licenseFileRequest.setHeader( "accept", "text/plain" );

      try {
        licenseFileRequest.sendRequest( null, new RequestCallback() {

          public void onError( Request request, Throwable exception ) {
            setLicenseFileText( LICENSE_READ_ERROR );
            showAboutDialog();
          }

          public void onResponseReceived( Request request, Response response ) {
            setLicenseFileText( response.getText() );
            showAboutDialog();
          }
        } );
      } catch ( RequestException e ) {
        Window.alert( e.getMessage() );
      }
    }
  }

  private void showAboutDialog() {
    String releaseLabel = Messages.getString( "release" );
    PromptDialogBox dialogBox =
      new PromptDialogBox( null, Messages.getString( "ok" ), null, false, true ); //$NON-NLS-1$
    VerticalPanel aboutContent = new VerticalPanel();
    aboutContent.setBorderWidth( 0 );
    aboutContent.setStyleName( "about-splash" );
    aboutContent.add( new Label( releaseLabel + " " + versionText ) );
    aboutContent.add( new HTML( licenseText ) );
    dialogBox.setContent( aboutContent );
    dialogBox.setPixelSize( 700, 400 );
    dialogBox.center();
  }
}