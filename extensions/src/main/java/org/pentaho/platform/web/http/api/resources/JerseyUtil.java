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


package org.pentaho.platform.web.http.api.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.ws.rs.core.MultivaluedMap;

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
