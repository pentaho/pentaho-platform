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

package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
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
import com.google.gwt.user.client.ui.HTML;

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

    final HTML messageTextBox = new HTML( Messages.getString( "confirmSwitchTheme.message" ) );
    final PromptDialogBox fileMoveToTrashWarningDialogBox =
        new PromptDialogBox( Messages.getString( "confirmSwitchTheme.title" ), Messages.getString( "confirmSwitchTheme.ok" ), Messages
            .getString( "confirmSwitchTheme.cancel" ), true, true );
    fileMoveToTrashWarningDialogBox.setContent( messageTextBox );

    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
      }

      public void okPressed() {
        final String url = GWT.getHostPageBaseURL() + "api/theme/set"; //$NON-NLS-1$
        RequestBuilder setThemeRequestBuilder = new RequestBuilder( RequestBuilder.POST, url );
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
