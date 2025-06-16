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


package org.pentaho.platform.web.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;
import org.pentaho.platform.engine.services.solution.SimpleParameterSetter;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.api.resources.XactionUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Iterator;

/**
 * The purpose of this content generator is to render prompts for a specific xaction, used during background and
 * scheduling operations.
 *
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class ActionSequenceParameterUiContentGenerator extends SimpleContentGenerator {

  private static final long serialVersionUID = 458870144807597675L;

  private IParameterProvider requestParameters;

  private IParameterProvider pathParameters;

  private String path = null;

  @Override
  public void createContent( OutputStream outputStream ) throws Exception {
    IParameterProvider requestParams = getRequestParameters();
    IParameterProvider pathParams = getPathParameters();

    if ( ( requestParams != null ) && ( requestParams.getStringParameter( "path", null ) != null ) ) {
      path = URLDecoder
        .decode( requestParams.getStringParameter( "path", "" ), "UTF-8" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } else if ( ( pathParams != null ) && ( pathParams.getStringParameter( "path", null ) != null ) ) { //$NON-NLS-1$
      path = URLDecoder
        .decode( pathParams.getStringParameter( "path", "" ), "UTF-8" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    if ( path != null && path.length() > 0 ) {
      IUnifiedRepository unifiedRepository = PentahoSystem.get( IUnifiedRepository.class, null );
      RepositoryFile file = unifiedRepository.getFile( path );
      HttpServletRequest httpRequest = (HttpServletRequest) pathParams.getParameter( "httprequest" ); //$NON-NLS-1$
      HttpServletResponse httpResponse = (HttpServletResponse) pathParams.getParameter( "httpresponse" ); //$NON-NLS-1$
      String buffer =
        XactionUtil.executeScheduleUi( file, httpRequest, httpResponse, PentahoSessionHolder.getSession(),
          outputHandler.getMimeTypeListener() );
      outputStream.write( buffer.toString().getBytes( LocaleHelper.getSystemEncoding() ) );
    }
  }

  @Override
  public String getMimeType() {
    return "text/html";
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( ActionSequenceContentGenerator.class );
  }

  @SuppressWarnings ( "unchecked" )
  private IParameterProvider getRequestParameters() {
    if ( this.parameterProviders == null ) {
      return new SimpleParameterProvider();
    }

    IParameterProvider requestParams = this.parameterProviders.get( "request" ); //$NON-NLS-1$
    SimpleParameterSetter parameters = new SimpleParameterSetter();
    Iterator requestParamIterator = requestParams.getParameterNames();
    while ( requestParamIterator.hasNext() ) {
      String param = (String) requestParamIterator.next();
      parameters.setParameter( param, requestParams.getParameter( param ) );
    }
    this.requestParameters = parameters;
    return parameters;
  }

  @SuppressWarnings ( "unchecked" )
  public IParameterProvider getPathParameters() {

    IParameterProvider pathParams = this.parameterProviders.get( "path" ); //$NON-NLS-1$
    SimpleParameterSetter parameters = new SimpleParameterSetter();
    Iterator pathParamIterator = pathParams.getParameterNames();
    while ( pathParamIterator.hasNext() ) {
      String param = (String) pathParamIterator.next();
      parameters.setParameter( param, pathParams.getParameter( param ) );
    }

    this.pathParameters = parameters;
    return parameters;
  }
}
