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

package org.pentaho.platform.web.http.request;

import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.util.web.HttpUtil;

import javax.servlet.http.HttpServletRequest;
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
