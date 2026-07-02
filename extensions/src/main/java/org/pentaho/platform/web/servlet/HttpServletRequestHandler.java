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

import jakarta.servlet.http.HttpServletRequest;
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
