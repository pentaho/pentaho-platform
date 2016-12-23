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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.filters;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.google.common.collect.Iterators;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * This filter is designed to work around a limitation of some web servers which prohibit the encoded form of / and \
 * (%2F and %5C) being in the URL. Clients can pass double-encoded forms of these encodings instead (%252F and %255C).
 * This filter will detect any occurance of these double encodings and correct them before passing on to the rest of the
 * servlet chain.
 * <p/>
 * Created by nbaker on 4/8/14.
 */
public class PentahoPathDecodingFilter implements Filter {

  private static final Log LOGGER = LogFactory.getLog( PentahoPathDecodingFilter.class );

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {

  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
    throws IOException, ServletException {
    EncodingAwareHttpServletRequestWrapper wrapper = new EncodingAwareHttpServletRequestWrapper(
        (HttpServletRequest) servletRequest );
    filterChain.doFilter( wrapper, servletResponse );
  }

  @Override
  public void destroy() {

  }

  private static final class EncodingAwareHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private MultivaluedMap<String, String> parameterMap = null;
    private Map<String, String[]> parameters = null;

    private EncodingAwareHttpServletRequestWrapper( HttpServletRequest request ) {
      super( request );

      if ( HttpMethod.GET.equalsIgnoreCase( request.getMethod() ) ) {
        String query = getQueryString();
        if ( StringUtils.isNotBlank( query ) ) {
          try {
            List<NameValuePair> pairs =
                URLEncodedUtils.parse( new URI( "/?" + query ), request.getCharacterEncoding() );
            parameterMap = new MultivaluedMapImpl();
            for ( NameValuePair pair : pairs ) {
              parameterMap.add( pair.getName(), pair.getValue() );
            }
          } catch ( URISyntaxException e ) {
            LOGGER.error( e );
          }
        }
      }
    }

    private Map<String, String[]> getNativeMap() {
      if ( parameters == null ) {
        parameters = new HashMap<String, String[]>( parameterMap.size() );
        for ( Map.Entry<String, List<String>> parameter : parameterMap.entrySet() ) {
          List<String> values = parameter.getValue();
          parameters.put( parameter.getKey(), values.toArray( new String[values.size()] ) );
        }
      }
      return parameters;
    }

    @Override
    public String getPathInfo() {
      final String pathInfo = super.getPathInfo();
      if ( StringUtils.isEmpty( pathInfo ) ) {
        return pathInfo;
      }
      return pathInfo.replaceAll( "%5C", "\\\\" ).replaceAll( "%2F", "/" );
    }

    @Override
    public String getRequestURI() {
      final String requestURI = super.getRequestURI();
      return requestURI.replaceAll( "%255C", "%5C" ).replaceAll( "%252F", "%2F" );
    }

    @Override
    @Deprecated // too many conversions
    public Map<String, String[]> getParameterMap() {
      if ( parameterMap == null ) {
        return super.getParameterMap();
      }
      return getNativeMap();
    }

    @Override
    public String getParameter( String name ) {
      if ( parameterMap == null ) {
        return super.getParameter( name );
      }
      return parameterMap.getFirst( name );
    }

    @Override
    public String[] getParameterValues( String name ) {
      if ( parameterMap == null ) {
        return super.getParameterValues( name );
      }
      List<String> values = parameterMap.get( name );
      return values.toArray( new String[values.size()] );
    }

    @Override
    public Enumeration<String> getParameterNames() {
      if ( parameterMap == null ) {
        return super.getParameterNames();
      }
      return Iterators.asEnumeration( parameterMap.keySet().iterator() );
    }

  }
}
