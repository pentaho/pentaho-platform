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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.security;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.List;
import java.util.stream.Collectors;

public class CsrfUtil {

  public static final String API_SYSTEM_CSRF = "/api/system/csrf";
  public static final String API_SYSTEM_CSRF_PARAM_URL = "url";
  public static final String API_SYSTEM_CSRF_RESPONSE_HEADER_TOKEN = "X-CSRF-TOKEN";
  public static final String API_SYSTEM_CSRF_RESPONSE_HEADER_HEADER = "X-CSRF-HEADER";
  public static final String API_SYSTEM_CSRF_RESPONSE_HEADER_PARAM = "X-CSRF-PARAM";

  public static CsrfToken getCsrfToken( Client client, String contextURL, String serviceURL ) {

    String csrfServiceURL = contextURL + API_SYSTEM_CSRF;

    WebResource csrfTokenResource = client
        .resource( csrfServiceURL )
        .queryParam( API_SYSTEM_CSRF_PARAM_URL, serviceURL );

    WebResource.Builder requestBuilder = csrfTokenResource.getRequestBuilder();
    ClientResponse response = requestBuilder.get( ClientResponse.class );

    // Response body is empty. Thus, should return 204 response.
    if ( response == null || !(response.getStatus() == 204 || response.getStatus() == 200)) {
      return null;
    }

    // When CSRF protection is disabled, the token is not returned.
    String token = response.getHeaders().getFirst( API_SYSTEM_CSRF_RESPONSE_HEADER_TOKEN );
    if ( token == null ) {
      return null;
    }

    String header = response.getHeaders().getFirst( API_SYSTEM_CSRF_RESPONSE_HEADER_HEADER );
    String parameter = response.getHeaders().getFirst( API_SYSTEM_CSRF_RESPONSE_HEADER_PARAM );

    List<String> cookieList = response.getCookies().stream().map( cookie -> cookie.toCookie().toString() )
      .collect( Collectors.toList() );

    return new CsrfToken( header, parameter, token, cookieList );
  }
}
