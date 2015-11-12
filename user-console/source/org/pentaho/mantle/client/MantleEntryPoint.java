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

package org.pentaho.mantle.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.pentaho.gwt.widgets.client.utils.i18n.IResourceBundleLoadCallback;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MantleEntryPoint implements EntryPoint, IResourceBundleLoadCallback {

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    // just some quick sanity setting of the platform effective locale based on the override
    // which comes from the url parameter

    String locale = getLocationParameter( "locale" );
    if ( !StringUtils.isEmpty( locale ) ) {
      final String url = GWT.getHostPageBaseURL() + "api/mantle/locale"; //$NON-NLS-1$
      RequestBuilder builder = getRequestBuilder( RequestBuilder.POST, url );
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      try {
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
    }
    ResourceBundle messages = getResourceBundle();
    Messages.setResourceBundle( messages );
    messages.loadBundle( GWT.getModuleBaseURL() + "messages/", "mantleMessages", true,
        MantleEntryPoint.this ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void bundleLoaded( String bundleName ) {
    Window.setTitle( Messages.getString( "productName" ) ); //$NON-NLS-1$

    MantleApplication mantle = getMantleApplication();
    mantle.loadApplication();

    RootPanel loadingPanel = getRootPanel( "loading" ); //$NON-NLS-1$
    if ( loadingPanel != null ) {
      loadingPanel.removeFromParent();
      loadingPanel.setVisible( false );
      loadingPanel.setHeight( "0px" ); //$NON-NLS-1$
    }
  }

  String getLocationParameter( String param ) {
    return Window.Location.getParameter( param );
  }

  ResourceBundle getResourceBundle() {
    return new ResourceBundle();
  }

  RequestBuilder getRequestBuilder( RequestBuilder.Method method, String url ) {
    return new RequestBuilder( method, url );
  }

  MantleApplication getMantleApplication() {
    return MantleApplication.getInstance();
  }

  RootPanel getRootPanel( String panelName ) {
    return RootPanel.get( panelName );
  }
}
