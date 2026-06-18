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


package org.pentaho.platform.util.client;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.pentaho.di.core.util.HttpClientManager;

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
    HttpClientManager.HttpClientBuilderFacade clientBuilder = HttpClientManager.getInstance().createBuilder();

    if ( StringUtils.isNotEmpty( userId ) && StringUtils.isNotEmpty( password ) ) {
      clientBuilder.setCredentials( userId, password );
    }

    return clientBuilder.build();
  }

  /**
   * Submits an HTTP result with the provided HTTPMethod and returns a dom4j document of the response
   *
   * @param callMethod
   * @return
   * @throws ServiceException
   */
  public static org.dom4j.Document getResultDom4jDocument( HttpClient client, HttpUriRequest callMethod )
    throws ServiceException {

    try {
      // execute the HTTP call
      HttpResponse httpResponse = client.execute( callMethod );
      final int status = httpResponse.getStatusLine().getStatusCode();
      if ( status != HttpStatus.SC_OK ) {
        throw new ServiceException( "Web service call failed with code " + status ); //$NON-NLS-1$
      }
      // get the result as a string
      InputStream in = httpResponse.getEntity().getContent();
      byte[] buffer = new byte[ 2048 ];
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
