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

    if ( path != null && path.length() > 0 ) {
      IUnifiedRepository unifiedRepository = PentahoSystem.get( IUnifiedRepository.class, null );
      RepositoryFile file = unifiedRepository.getFile( path );
      String buffer = XactionUtil.doParameter( file, requestParams, PentahoSessionHolder.getSession() );
      outputStream.write( buffer.toString().getBytes( LocaleHelper.getSystemEncoding() ) );
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
