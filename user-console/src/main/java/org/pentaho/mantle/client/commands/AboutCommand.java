/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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

  public AboutCommand() {
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    if ( StringUtils.isEmpty( MantleApplication.mantleRevisionOverride ) == false ) {
      showAboutDialog( MantleApplication.mantleRevisionOverride );
    } else {
      final String url = GWT.getHostPageBaseURL() + "api/version/show"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
      requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      requestBuilder.setHeader( "accept", "text/plain" );
      try {
        requestBuilder.sendRequest( null, new RequestCallback() {

          public void onError( Request request, Throwable exception ) {
            // showError(exception);
          }

          public void onResponseReceived( Request request, Response response ) {
            showAboutDialog( response.getText() );
          }
        } );
      } catch ( RequestException e ) {
        Window.alert( e.getMessage() );
        // showError(e);
      }
    }
  }

  private void showAboutDialog( String version ) {
    @SuppressWarnings( "deprecation" )
    String licenseInfo = Messages.getString( "licenseInfo", "" + ( ( new Date() ).getYear() + 1900 ) );
    String releaseLabel = Messages.getString( "release" );
    PromptDialogBox dialogBox =
        new PromptDialogBox( Messages.getString( "aboutDialogTitle" ), Messages.getString( "ok" ), null, false, true ); //$NON-NLS-1$

    VerticalPanel aboutContent = new VerticalPanel();
    aboutContent.add( new Label( releaseLabel + " " + version ) );
    aboutContent.add( new HTML( licenseInfo ) );

    dialogBox.setContent( aboutContent );
    dialogBox.center();
  }
}
