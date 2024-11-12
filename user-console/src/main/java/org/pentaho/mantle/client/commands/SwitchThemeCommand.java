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

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.csrf.CsrfRequestBuilder;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

/**
 * User: nbaker Date: 5/13/11
 */
public class SwitchThemeCommand extends AbstractCommand {

  private String theme;

  public SwitchThemeCommand() {
  }

  public SwitchThemeCommand( String theme ) {
    this.theme = theme;
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    final MessageDialogBox fileMoveToTrashWarningDialogBox = new MessageDialogBox(
      Messages.getString( "confirmSwitchTheme.title" ),
      Messages.getString( "confirmSwitchTheme.message" ),
      true,
      Messages.getString( "confirmSwitchTheme.ok" ),
      Messages.getString( "confirmSwitchTheme.cancel" ) );

    final IDialogCallback callback = new IDialogCallback() {
      public void cancelPressed() {
      }

      public void okPressed() {
        final String url = GWT.getHostPageBaseURL() + "api/theme/set"; //$NON-NLS-1$
        RequestBuilder setThemeRequestBuilder = new CsrfRequestBuilder( RequestBuilder.POST, url );
        setThemeRequestBuilder.setHeader( "accept", "text/plain" );
        setThemeRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
        try {
          setThemeRequestBuilder.sendRequest( theme, new RequestCallback() {

            public void onError( Request request, Throwable exception ) {
              // showError(exception);
            }

            public void onResponseReceived( Request request, Response response ) {
              // forcing a setTimeout to fix a problem in IE BISERVER-6385
              Scheduler.get().scheduleDeferred( new Command() {
                public void execute() {
                  Window.Location.reload();
                }
              } );
            }
          } );
        } catch ( RequestException e ) {
          Window.alert( e.getMessage() );
          // showError(e);
        }
      }
    };
    fileMoveToTrashWarningDialogBox.setCallback( callback );
    fileMoveToTrashWarningDialogBox.center();
  }

  public String getTheme() {
    return theme;
  }

  public void setTheme( String theme ) {
    this.theme = theme;
  }
}
