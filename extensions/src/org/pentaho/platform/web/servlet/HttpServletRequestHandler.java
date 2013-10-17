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

package org.pentaho.platform.web.servlet;

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.ui.IUIComponent;
import org.pentaho.platform.engine.services.BaseRequestHandler;
import org.pentaho.platform.uifoundation.component.BaseUIComponent;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public class HttpServletRequestHandler extends BaseRequestHandler {

  private HttpServletRequest request;

  public HttpServletRequestHandler( final IPentahoSession session, final String instanceId,
      final HttpServletRequest request, final IOutputHandler outputHandler, final IPentahoUrlFactory urlFactory ) {
    super( session, instanceId, outputHandler, null, urlFactory );
    HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider( session );
    setParameterProvider( IParameterProvider.SCOPE_SESSION, sessionParameters );
    setRequest( request );
  }

  public void handleUIRequest( final IUIComponent component, final String contentType ) throws IOException {
    IContentItem contentItem =
        getOutputHandler()
            .getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, getInstanceId(), null );
    OutputStream outputStream = contentItem.getOutputStream( this.getActionPath() );
    ( (BaseUIComponent) component ).setUrlFactory( urlFactory );
    component.handleRequest( outputStream, this, contentType, getParameterProviders() );

  }

  public void setRequest( final HttpServletRequest request ) {
    this.request = request;
    IParameterProvider requestParameters = new HttpRequestParameterProvider( request );
    setParameterProvider( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters );

  }

  public String getStringParameter( final String name ) {
    return request.getParameter( name );
  }

  public Set getParameterNames() {
    return request.getParameterMap().keySet();
  }

}
