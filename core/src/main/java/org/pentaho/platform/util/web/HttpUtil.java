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


package org.pentaho.platform.util.web;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class HttpUtil {
  //7 seconds
  public static final int PAGE_TIMEOUT = 7_000;
  //3 seconds
  public static final int CONNECTION_TIMEOUT = 3_000;

  private static final String INVALID_URL = "Invalid URL";

  private static HttpClientManager httpClientManager = HttpClientManager.getInstance();
  private static HttpClientManager.HttpClientBuilderFacade clientBuilder = httpClientManager.createBuilder();

  public static HttpClient getClient() {
    clientBuilder.setSocketTimeout( PAGE_TIMEOUT ).setConnectionTimeout( CONNECTION_TIMEOUT );

    return clientBuilder.build();
  }

  public static boolean getURLContent( final String url, final StringBuffer content ) throws IOException {
    try {
      HttpClient httpClient = null;

      String host = System.getProperty( "http.proxyHost" );
      String port = System.getProperty( "http.proxyPort" );

      if ( StringUtils.isNotEmpty( host ) && StringUtils.isNotEmpty( port ) ) {
        clientBuilder.setProxy( host, Integer.parseInt( port ) ).
          setSocketTimeout( PAGE_TIMEOUT ).setConnectionTimeout( CONNECTION_TIMEOUT );
        String user = System.getProperty( "http.proxyUser" );
        if ( StringUtils.isNotBlank( user ) ) {
          String password = System.getProperty( "http.proxyPassword" );
          clientBuilder.setCredentials( user, password );
          httpClient = clientBuilder.build();
        }

      }

      if ( httpClient == null ) {
        httpClient = getClient();
      }

      HttpGet call = new HttpGet( url );
      // execute the HTTP call
      HttpResponse httpResponse = httpClient.execute( call );
      final int status = httpResponse.getStatusLine().getStatusCode();
      if ( status == 200 ) {
        InputStream response = httpResponse.getEntity().getContent();
        try {
          byte[] buffer = new byte[2048];
          int size = response.read( buffer );
          while ( size > 0 ) {
            for ( int idx = 0; idx < size; idx++ ) {
              content.append( (char) buffer[idx] );
            }
            size = response.read( buffer );
          }
        } catch ( Exception e ) {
          // we can ignore this because the content comparison will fail
        }
      }
    } catch ( Throwable e ) {
      StringWriter writer = new StringWriter();
      PrintWriter writer2 = new PrintWriter( writer );
      e.printStackTrace( writer2 );
      content.append( writer.getBuffer() );
      return false;
    }
    return true;

  }

  public static void getURLContent_old( final String uri, final StringBuffer content ) throws MalformedURLException,
    IOException {
    URL url = new URL( uri );
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.connect();
    InputStream in = connection.getInputStream();
    byte[] buffer = new byte[300];
    int n = buffer.length;
    while ( n > 0 ) {
      n = in.read( buffer );
      for ( int i = 0; i < n; i++ ) {
        content.append( (char) buffer[i] );
      }
    }
    n = in.read( buffer );
  }

  public static String getURLContent( final String uri ) {
    if ( !isValidURL( uri ) ) {
      throw new IllegalArgumentException( INVALID_URL );
    }
    try {
      StringBuffer content = new StringBuffer();
      HttpUtil.getURLContent( uri, content );
      return content.toString();
    } catch ( Exception e ) {
      // TODO: handle this error
      Logger
        .error(
          "org.pentaho.platform.util.web.HttpUtil",
          Messages.getInstance().getErrorString( "HttpUtil.ERROR_0001_URL_ERROR", e.getMessage() ),
          e ); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }
  }

  public static InputStream getURLInputStream( final String uri ) {
    if ( !isValidURL( uri ) ) {
      throw new IllegalArgumentException( INVALID_URL );
    }
    try {
      URL url = new URL( uri );
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.connect();
      InputStream in = connection.getInputStream();
      return in;
    } catch ( Exception e ) {
      // TODO: handle this error
      Logger
        .error(
          "org.pentaho.platform.util.web.HttpUtil",
          Messages.getInstance().getErrorString( "HttpUtil.ERROR_0001_URL_ERROR", e.getMessage() ),
          e ); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }

  }

  public static Reader getURLReader( final String uri ) {
    if ( !isValidURL( uri ) ) {
      throw new IllegalArgumentException( INVALID_URL );
    }
    try {
      URL url = new URL( uri );
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.connect();
      InputStream in = connection.getInputStream();
      return new InputStreamReader( in );
    } catch ( Exception e ) {
      // TODO: handle this error
      Logger.error( HttpUtil.class.getName(), Messages.getInstance().getErrorString(
        "HttpUtil.ERROR_0001_URL_ERROR", e.getMessage() ), e ); //$NON-NLS-1$
      return null;
    }

  }

  //
  // The code in the next two methods is based on the code in HttpUtils.java
  // from
  // jakarta.servlet.http. HttpUtils is deprecated - so I updated the methods to
  // be a bit smarter
  // and use Map instead of Hashtable
  //
  public static Map parseQueryString( final String s ) {
    String[] valArray = null;
    if ( s == null ) {
      throw new IllegalArgumentException();
    }
    Map<String, String[]> rtn = new HashMap<String, String[]>();
    StringBuffer sb = new StringBuffer();
    String key;
    for ( StringTokenizer st = new StringTokenizer( s, "&" ); st.hasMoreTokens();
         rtn.put( key, valArray ) ) { //$NON-NLS-1$
      String pair = st.nextToken();
      int pos = pair.indexOf( '=' );
      if ( pos == -1 ) {
        throw new IllegalArgumentException();
      }
      key = HttpUtil.parseName( pair.substring( 0, pos ), sb );
      String val = HttpUtil.parseName( pair.substring( pos + 1, pair.length() ), sb );
      if ( rtn.containsKey( key ) ) {
        String[] oldVals = rtn.get( key );
        valArray = new String[oldVals.length + 1];
        System.arraycopy( oldVals, 0, valArray, 0, oldVals.length );
        valArray[oldVals.length] = val;
      } else {
        valArray = new String[1];
        valArray[0] = val;
      }
    }
    return rtn;
  }

  private static String parseName( final String s, final StringBuffer sb ) {
    sb.setLength( 0 );
    char c;
    for ( int i = 0; i < s.length(); i++ ) {
      c = s.charAt( i );
      switch ( c ) {
        case 43: { // '+'
          sb.append( ' ' );
          break;
        }
        case 37: { // '%'
          try {
            sb.append( (char) Integer.parseInt( s.substring( i + 1, i + 3 ), 16 ) );
            i += 2;
            break;
          } catch ( NumberFormatException numberformatexception ) {
            throw new IllegalArgumentException();
          } catch ( StringIndexOutOfBoundsException oob ) {
            String rest = s.substring( i );
            sb.append( rest );
            if ( rest.length() == 2 ) {
              i++;
            }
          }
          break;
        }
        default: {
          sb.append( c );
          break;
        }
      }
    }
    return sb.toString();
  }

  /**
   * This method first checks system.property file ssrf-protection-enabled property. If it is set to false (default),
   * the system will allow all URLs. If the system.property ssrf-protection-enabled is set to true, the system will
   * only allow URLs that match the allowed URLs list. The allowed URLs is a comma-separated list defined in the
   * alternative-fully-qualified-server-urls property in the server.properties file.
   *
   * @param urlString the URL to be checked
   * @return true if the URL is allowed to be accessed, false otherwise
   * @throws SecurityException
   */

  @VisibleForTesting
  protected static boolean isValidURL( String urlString ) throws SecurityException {
    boolean allowedUrl = false;
    boolean checkSSRFProtectionEnable = "true".equals( PentahoSystem.get( ISystemConfig.class )
      .getProperty( "system.ssrf-protection-enabled", "false" ) );
    if ( !checkSSRFProtectionEnable ) {
      return true;
    }
    String allowedHosts = PentahoSystem.get( ISystemConfig.class )
      .getProperty( "server.alternative-fully-qualified-server-urls" );
    if ( allowedHosts != null ) {
      String[] hosts = StringUtils.stripAll( allowedHosts.split( "," ) );
      try {
        URL url = new URL( urlString );
        for ( String host : hosts ) {
          if ( url.getHost().equals( new URL( host ).getHost() ) ) {
            allowedUrl = true;
            break;
          }
        }
      } catch ( MalformedURLException e ) {
        //Either no legal protocol could be found in the provided string or the string could not be parsed.
        Logger.error( HttpUtil.class.getName(), Messages.getInstance().getErrorString(
          "Malformed URL:", e.getMessage() ), e ); //$NON-NLS-1$
      }
    }
    return allowedUrl;
  }

}
