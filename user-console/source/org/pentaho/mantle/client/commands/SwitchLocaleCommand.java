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

public class SwitchLocaleCommand extends AbstractCommand {

  private String locale;

  public SwitchLocaleCommand() {
  }

  public SwitchLocaleCommand( String locale ) {
    this.locale = locale;
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    // stuff the locale in the server's session so we can use it
    // to override the browser setting, as needed

    final String url = GWT.getHostPageBaseURL() + "api/mantle/locale"; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder( RequestBuilder.POST, url );
    try {
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      builder.sendRequest( locale, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }

    String newLocalePath = "Home?locale=" + locale;
    String baseUrl = GWT.getModuleBaseURL();
    int index = baseUrl.indexOf( "/mantle/" );
    if ( index >= 0 ) {
      newLocalePath = baseUrl.substring( 0, index ) + "/Home?locale=" + locale;
    }
    Window.Location.replace( newLocalePath );
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale( String locale ) {
    this.locale = locale;
  }
}
