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

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Iterator;

public class ActionSequenceParameterContentGenerator extends SimpleContentGenerator {

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

    if ( path != null && !path.isEmpty() ) {
      IUnifiedRepository unifiedRepository = PentahoSystem.get( IUnifiedRepository.class, null );
      RepositoryFile file = unifiedRepository.getFile( path );

      if ( file == null ) {
        // No repository file - check if this is a system job using actionClass
        if ( SchedulerJobUtil.isSystemJob( path ) ) {
          // System job with actionClass - return empty parameters (valid request)
          String encoding = LocaleHelper.getSystemEncoding();
          String emptyParams = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?><parameters></parameters>";
          outputStream.write( emptyParams.getBytes( encoding ) );
          return;
        }
        // Not a system job and file doesn't exist - this is invalid input, throw exception for 404
        throw new FileNotFoundException( "Repository file not found: " + path );
      }

      String buffer = XactionUtil.doParameter( file, requestParams, PentahoSessionHolder.getSession() );
      outputStream.write( buffer.getBytes( LocaleHelper.getSystemEncoding() ) );
    }
  }

  @Override
  public String getMimeType() {
    return "text/xml";
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( ActionSequenceContentGenerator.class );
  }

  @SuppressWarnings ( "unchecked" )
  private IParameterProvider getRequestParameters() {
    if ( this.requestParameters != null ) {
      return this.requestParameters;
    }

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
    if ( this.pathParameters != null ) {
      return this.pathParameters;
    }

    IParameterProvider pathParams = this.parameterProviders.get( "path" ); //$NON-NLS-1$
    SimpleParameterSetter parameters = new SimpleParameterSetter();
    if ( pathParams != null ) {
      Iterator pathParamIterator = pathParams.getParameterNames();
      while ( pathParamIterator.hasNext() ) {
        String param = (String) pathParamIterator.next();
        parameters.setParameter( param, pathParams.getParameter( param ) );
      }
    }
    this.pathParameters = parameters;
    return parameters;
  }
}
