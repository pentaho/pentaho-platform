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


package org.pentaho.platform.web.http.request;

import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.util.web.HttpUtil;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;

public class HttpRequestParameterProvider extends SimpleParameterProvider {

  private HttpServletRequest request;

  public HttpRequestParameterProvider( final HttpServletRequest request ) {
    this.request = request;
    setServletRequestParameters( request.getParameterMap() );

    if ( request.getParameter( SimpleParameterProvider.ADDITIONAL_PARAMS ) != null ) {
      String additionalParameters = request.getParameter( SimpleParameterProvider.ADDITIONAL_PARAMS );
      int idx = additionalParameters.indexOf( "?" ); //$NON-NLS-1$
      if ( idx > 0 ) {
        additionalParameters = additionalParameters.substring( idx + 1 );
      }
      Map additionalParms = HttpUtil.parseQueryString( additionalParameters );
      setServletRequestParameters( additionalParms );
    }
  }

  /**
   * Converts single value arrays to String parameters
   * 
   */
  private void setServletRequestParameters( final Map paramMap ) {
    for ( Iterator it = paramMap.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry entry = (Map.Entry) it.next();
      Object value = entry.getValue();
      if ( value != null ) {
        if ( ( value instanceof Object[] ) && ( ( (Object[]) value ).length == 1 ) ) {
          setParameter( (String) entry.getKey(), String.valueOf( ( (Object[]) value )[0] ) );
        } else {
          setParameter( (String) entry.getKey(), value );
        }
      }
    }
  }

  public HttpServletRequest getRequest() {
    return request;
  }

}
