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

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;

public class RefreshSystemSettingsCommand extends AbstractCommand {

  public RefreshSystemSettingsCommand() {
  }

  protected void performOperation() {
    final String url = GWT.getHostPageBaseURL() + "api/system/refresh/systemSettings"; //$NON-NLS-1$
    RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
    requestBuilder.setHeader( "accept", "text/plain" );
    requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      requestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          MessageDialogBox dialogBox =
              new MessageDialogBox(
                  Messages.getString( "info" ), Messages.getString( "refreshSystemSettingsSuccess" ), false, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
          dialogBox.center();
        }
      } );
    } catch ( RequestException e ) {
      Window.alert( e.getMessage() );
      // showError(e);
    }
  }

  protected void performOperation( final boolean feedback ) {
    // do nothing
  }
}
