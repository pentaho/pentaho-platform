/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.MultivaluedMap;

public class JerseyUtil {

  public static HttpServletRequest correctPostRequest( final MultivaluedMap<String, String> formParams,
      HttpServletRequest httpServletRequest ) {
    final HashMap<String, String[]> formParamsHashMap = new HashMap<String, String[]>();
    formParamsHashMap.putAll( httpServletRequest.getParameterMap() );

    for ( Entry<String, List<String>> entry : formParams.entrySet() ) {
      List<String> value = entry.getValue();
      String[] valueArray = value.toArray( new String[value.size()] );
      formParamsHashMap.put( entry.getKey(), valueArray );
    }

    HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper( httpServletRequest ) {
      @Override public Map<String, String[]> getParameterMap() {
        return formParamsHashMap;
      }

      @Override
      public String[] getParameterValues( String name ) {
        return (String[]) formParamsHashMap.get( name );
      }

      @Override
      public String getParameter( String name ) {
        Object value = formParamsHashMap.get( name );
        if ( value != null && value instanceof String[] && ( (String[]) value )[0] != null ) {
          return ( (String[]) value )[0];
        } else {
          return null;
        }
      }
    };

    return requestWrapper;
  }
}
