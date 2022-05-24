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
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This servlet is used to Proxy a Servlet request to another server for processing and returns that result to the
 * caller as if this Servlet actiually serviced it. Setup the proxy by editing the <b>web.xml</b> to map the servlet
 * name you want to proxy to the Proxy Servlet class.
 * <p>
 * <p>
 * <pre>
 *  &lt;servlet&gt;
 *    &lt;servlet-name&gt;ViewAction&lt;/servlet-name&gt;
 *    &lt;servlet-class&gt;com.pentaho.ui.servlet.ProxyServlet&lt;/servlet-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;ProxyURL&lt;/param-name&gt;
 *       &lt;param-value&gt;http://my.remoteserver.com:8080/pentaho&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *   &lt;/servlet&gt;
 * </pre>
 * <p>
 * In the above example, all requests to /ViewAction will be forwarded to the ViewAction Servlet running on the Hitachi
 * Vantara server atmy.remoteserver.com:8080
 * <p>
 * <p>
 * NOTES:
 * <p>
 * <p>
 * For this to be useful, both Pentaho servers should be using the same database repository.
 * <p>
 * The recieving server should have the ProxyTrustingFilter enabled to handle authicentation.
 * <p>
 * This Servlet only works with GET requests. All requests in the Pentaho BI Platform are currently gets.
 *
 * @author Doug Moran
 * @see org.pentaho.platform.web.http.filters.ProxyTrustingFilter
 */
public class ProxyServlet extends ServletBase {

  private static final long serialVersionUID = 4680027723733552639L;

  private static final String TRUST_USER_PARAM = "_TRUST_USER_";
  private static final String TRUST_LOCALE_OVERRIDE_PARAM = "_TRUST_LOCALE_OVERRIDE_";

  private static final Log logger = LogFactory.getLog( ProxyServlet.class );

  @Override
  public Log getLogger() {
    return ProxyServlet.logger;
  }

  private String proxyURL = null; // "http://localhost:8080/pentaho";

  private boolean isLocaleOverrideEnabled = true;

  private String errorURL = null; // The URL to redirect to if the user is invalid

  /**
   * Base Constructor
   */
  public ProxyServlet() {
    super();
  }

  @Override
  public void init( final ServletConfig servletConfig ) throws ServletException {
    proxyURL = servletConfig.getInitParameter( "ProxyURL" ); //$NON-NLS-1$
    if ( ( proxyURL == null ) ) {
      error( Messages.getInstance().getString( "ProxyServlet.ERROR_0001_NO_PROXY_URL_SPECIFIED" ) ); //$NON-NLS-1$
    } else {
      try {
        URL url = new URL( proxyURL.trim() ); // Just doing this to verify
        // it's good
        info( Messages.getInstance().getString( "ProxyServlet.INFO_0001_URL_SELECTED",
          url.toExternalForm() ) ); // using 'url' to get rid of unused var compiler warning //$NON-NLS-1$
      } catch ( Throwable t ) {
        error( Messages.getInstance().getErrorString( "ProxyServlet.ERROR_0002_INVALID_URL", proxyURL ) ); //$NON-NLS-1$
        proxyURL = null;
      }
    }

    // To have a totally backward compatible behavior, specify the `LocaleOverrideEnabled` parameter with "false"
    String localeOverrideEnabledStr = servletConfig.getInitParameter( "LocaleOverrideEnabled" );
    if ( StringUtils.isNotEmpty( localeOverrideEnabledStr ) ) {
      isLocaleOverrideEnabled = localeOverrideEnabledStr.equalsIgnoreCase( "true" );
    }

    errorURL = servletConfig.getInitParameter( "ErrorURL" ); //$NON-NLS-1$
    super.init( servletConfig );
  }

  public String getProxyURL() {
    return proxyURL;
  }

  public String getErrorURL() {
    return errorURL;
  }

  public boolean isLocaleOverrideEnabled() {
    return isLocaleOverrideEnabled;
  }

  protected void doProxy( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
    // Got nothing from web.xml.
    if ( proxyURL == null ) {
      return;
    }

    PentahoSystem.systemEntryPoint();
    try {
      // Get the user from the session
      IPentahoSession userSession = getPentahoSession( request );
      String userName = userSession != null ? userSession.getName() : null;
      if ( StringUtils.isEmpty( userName ) && StringUtils.isNotBlank( errorURL ) ) {
        response.sendRedirect( errorURL );
        return;
      }

      URI requestUri = buildProxiedUri( request, userName );

      doProxyCore( requestUri, response );

    } catch ( URISyntaxException e ) {
      error( Messages.getInstance().getErrorString( "ProxyServlet.ERROR_0006_URI_SYNTAX_EXCEPTION", e.getMessage() ) );
      e.printStackTrace();
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  protected URI buildProxiedUri( final HttpServletRequest request, final String userName ) throws URISyntaxException {

    String baseUri = proxyURL + request.getServletPath();
    URIBuilder uriBuilder = new URIBuilder( baseUri );

    List<NameValuePair> queryParams = uriBuilder.isQueryEmpty() ? new ArrayList<>() : uriBuilder.getQueryParams();

    // Just in case someone is trying to spoof the proxy.
    queryParams.removeIf( pair -> pair.getName().equals( TRUST_USER_PARAM ) );

    // Copy the parameters from the request to the proxy.
    Map<String, String[]> paramMap = request.getParameterMap();
    for ( Map.Entry<String, String[]> entry : paramMap.entrySet() ) {
      for ( String element : entry.getValue() ) {
        queryParams.add( new BasicNameValuePair( entry.getKey(), element ) );
      }
    }

    // Add the trusted user from the session
    if ( StringUtils.isNotEmpty( userName ) ) {
      queryParams.add( new BasicNameValuePair( TRUST_USER_PARAM, userName ) );

      if ( isLocaleOverrideEnabled ) {
        Locale localeOverride = getProxyLocaleOverride();
        if ( localeOverride != null ) {
          queryParams.add( new BasicNameValuePair( TRUST_LOCALE_OVERRIDE_PARAM, localeOverride.toString() ) );
        }
      }
    }

    uriBuilder.setParameters( queryParams );

    debug( Messages.getInstance().getString( "ProxyServlet.DEBUG_0001_OUTPUT_URL", uriBuilder.toString() ) );

    return uriBuilder.build();
  }

  protected void doProxyCore( final URI requestUri, final HttpServletResponse response ) {

    HttpPost method = new HttpPost( requestUri );

    // Now do the request
    HttpClient client = HttpClientManager.getInstance().createDefaultClient();

    try {
      // Execute the method.
      HttpResponse httpResponse = client.execute( method );
      StatusLine statusLine = httpResponse.getStatusLine();
      int statusCode = statusLine.getStatusCode();
      if ( statusCode != HttpStatus.SC_OK ) {
        error( Messages.getInstance().getErrorString(
          "ProxyServlet.ERROR_0003_REMOTE_HTTP_CALL_FAILED", statusLine.toString() ) ); //$NON-NLS-1$
        return;
      }

      setHeader( "Content-Type", method, response ); //$NON-NLS-1$
      setHeader( "Content-Length", method, response ); //$NON-NLS-1$

      InputStream inStr = httpResponse.getEntity().getContent();
      ServletOutputStream outStr = response.getOutputStream();

      int inCnt;
      byte[] buf = new byte[ 2048 ];
      while ( -1 != ( inCnt = inStr.read( buf ) ) ) {
        outStr.write( buf, 0, inCnt );
      }

    } catch ( IOException e ) {
      error( Messages.getInstance().getErrorString( "ProxyServlet.ERROR_0005_TRANSPORT_FAILURE" ),
        e ); //$NON-NLS-1$
      e.printStackTrace();
    } finally {
      method.releaseConnection();
    }
  }

  private void setHeader( final String headerStr, final HttpRequestBase method, final HttpServletResponse response ) {
    Header header = method.getHeaders( headerStr )[ 0 ];
    if ( header != null ) {
      response.setHeader( headerStr, header.getValue() );
    }
  }

  /**
   * Gets the locale override to send to the proxy url.
   * <p>
   * Mostly, supports unit testing.
   *
   * @return The locale override, or {@code null}, if none.
   */
  protected Locale getProxyLocaleOverride() {
    return LocaleHelper.getLocaleOverride();
  }

  @Override
  protected void service( final HttpServletRequest arg0, final HttpServletResponse arg1 ) throws ServletException,
    IOException {
    // TODO Auto-generated method stub
    super.service( arg0, arg1 );
  }

  @Override
  protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {
    doProxy( request, response );
  }

  @Override
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException,
    IOException {
    doProxy( request, response );
  }
}
