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

package org.pentaho.platform.util.client;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import java.io.IOException;
import java.io.InputStream;

public class ClientUtil {

  /**
   * Returns an instance of an HttpClient. Only one is created per ConnectionServiceClient so all calls should be
   * made synchronously.
   * 
   * @return The HTTP client to be used for web service calls
   */
  public static HttpClient getClient( String userId, String password ) {

    HttpClient httpClientInstance = new HttpClient();
    if ( ( userId != null ) && ( userId.length() > 0 ) && ( password != null ) && ( password.length() > 0 ) ) {
      Credentials creds = new UsernamePasswordCredentials( userId, password );
      httpClientInstance.getState().setCredentials( AuthScope.ANY, creds );
      httpClientInstance.getParams().setAuthenticationPreemptive( true );
    }
    return httpClientInstance;
  }

  /**
   * Submits an HTTP result with the provided HTTPMethod and returns a dom4j document of the response
   * 
   * @param callMethod
   * @return
   * @throws ConnectionServiceException
   */
  public static org.dom4j.Document getResultDom4jDocument( HttpClient client, HttpMethod callMethod )
    throws ServiceException {

    try {
      // execute the HTTP call
      int status = client.executeMethod( callMethod );
      if ( status != HttpStatus.SC_OK ) {
        throw new ServiceException( "Web service call failed with code " + status ); //$NON-NLS-1$
      }
      // get the result as a string
      InputStream in = callMethod.getResponseBodyAsStream();
      byte[] buffer = new byte[2048];
      int n = in.read( buffer );
      StringBuilder sb = new StringBuilder();
      while ( n != -1 ) {
        sb.append( new String( buffer, 0, n ) );
        n = in.read( buffer );
      }
      String result = sb.toString();
      // convert to XML
      return DocumentHelper.parseText( result );
    } catch ( IOException e ) {
      throw new ServiceException( e );
    } catch ( DocumentException e ) {
      throw new ServiceException( e );
    }

  }
}
