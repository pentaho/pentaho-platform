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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This servlet is used to Proxy a Servlet request to another server for processing and returns that result to the
 * caller as if this Servlet actually serviced it. Setup the proxy by editing the <b>web.xml</b> to map the servlet
 * name you want to proxy to the Proxy Servlet class.
 * <p>
 * <p>
 * <pre>
 *  &lt;servlet&gt;
 *    &lt;servlet-name&gt;ViewAction&lt;/servlet-name&gt;
 *    &lt;servlet-class&gt;org.pentaho.platform.web.servlet.ProxyServlet&lt;/servlet-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;ProxyURL&lt;/param-name&gt;
 *       &lt;param-value&gt;http://my.remoteserver.com:8080/pentaho&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *   &lt;/servlet&gt;
 * </pre>
 * <p>
 * In the above example, all requests to /ViewAction will be forwarded to the ViewAction Servlet running on the Hitachi
 * Vantara server at my.remoteserver.com:8080
 * <p>
 * <p>
 * NOTES:
 * <p>
 * <p>
 * For this to be useful, both Pentaho servers should be using the same database repository.
 * <p>
 * The receiving server should have the ProxyTrustingFilter enabled to handle authentication.
 * <p>
 * This Servlet works with GET and POST requests. All requests in the Pentaho BI Platform are currently GET requests.
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

  /**
   * The URL the proxy servlet will redirect the request to. E.g. "http://localhost:8080/pentaho".
   */
  private String proxyURL = null;

  private boolean isLocaleOverrideEnabled = true;

  /**
   * The URL to redirect to if the an error occurs
   */
  private String errorURL = null;

  /**
   * Base Constructor
   */
  public ProxyServlet() {
    super();
  }

  @Override
  public void init( final ServletConfig servletConfig ) throws ServletException {
    proxyURL = servletConfig.getInitParameter( "ProxyURL" );
    if ( ( proxyURL == null ) ) {
      error( Messages.getInstance().getString( "ProxyServlet.ERROR_0001_NO_PROXY_URL_SPECIFIED" ) );
    } else {
      try {
        URL url = new URL( proxyURL.trim() ); // Just doing this to verify
        // it's good
        info( Messages.getInstance().getString( "ProxyServlet.INFO_0001_URL_SELECTED",
          url.toExternalForm() ) ); // using 'url' to get rid of unused var compiler warning
      } catch ( MalformedURLException mue ) {
        error( Messages.getInstance().getErrorString( "ProxyServlet.ERROR_0002_INVALID_URL", proxyURL ) );
        proxyURL = null;
      }
    }

    // To have a totally backward compatible behavior, specify the `LocaleOverrideEnabled` parameter with "false"
    String localeOverrideEnabledStr = servletConfig.getInitParameter( "LocaleOverrideEnabled" );
    if ( StringUtils.isNotEmpty( localeOverrideEnabledStr ) ) {
      isLocaleOverrideEnabled = localeOverrideEnabledStr.equalsIgnoreCase( "true" );
    }

    errorURL = servletConfig.getInitParameter( "ErrorURL" );
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

  protected void doProxy( final HttpServletRequest request, final HttpServletResponse response ) {
    // Got nothing from web.xml.
    if ( getProxyURL() == null ) {
      return;
    }

    PentahoSystem.systemEntryPoint();
    try {
      // Get the user from the session
      final IPentahoSession userSession = getPentahoSession( request );
      final String userName = userSession != null ? userSession.getName() : null;
      if ( StringUtils.isEmpty( userName ) && StringUtils.isNotBlank( getErrorURL() ) ) {
        response.sendRedirect( getErrorURL() );
        return;
      }

      final URI requestUri = buildProxiedUri( request, userName );

      doProxyCore( requestUri, request, response );

    } catch ( URISyntaxException e ) {
      error( Messages.getInstance().getErrorString(
        "ProxyServlet.ERROR_0006_URI_SYNTAX_EXCEPTION", e.getMessage() ) );
    } catch ( IOException e ) {
      error( Messages.getInstance().getErrorString(
        "ProxyServlet.ERROR_0005_TRANSPORT_FAILURE" ), e );
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  protected URI buildProxiedUri( final HttpServletRequest request, final String userName ) throws URISyntaxException, MalformedURLException {
    final URI proxyURI = new URL( getProxyURL().trim() ).toURI();

    // ignores the request query string
    final URI baseURI = new URI(
        proxyURI.getScheme(),
        proxyURI.getAuthority(),
        proxyURI.getPath() + request.getServletPath(),
        proxyURI.getQuery(),
        proxyURI.getFragment() );

    URIBuilder uriBuilder = new URIBuilder( baseURI );

    List<NameValuePair> queryParams = uriBuilder.isQueryEmpty() ? new ArrayList<>() : uriBuilder.getQueryParams();

    // Copy the parameters from the request to the proxy.
    Map<String, String[]> paramMap = request.getParameterMap();
    for ( Map.Entry<String, String[]> entry : paramMap.entrySet() ) {
      // Just in case someone is trying to spoof the proxy.
      if ( entry.getKey().equals( TRUST_USER_PARAM ) ) {
        continue;
      }

      for ( String element : entry.getValue() ) {
        queryParams.add( new BasicNameValuePair( entry.getKey(), element ) );
      }
    }

    // Add the trusted user from the session if it is not set via the proxy URL and override Locale if enabled
    if ( StringUtils.isNotEmpty( userName ) ) {
      if ( queryParams.stream().noneMatch( param -> param.getName().equals( TRUST_USER_PARAM ) ) ) {

        queryParams.add( new BasicNameValuePair( TRUST_USER_PARAM, userName ) );
      }

      if ( isLocaleOverrideEnabled() ) {
        // Remove TRUST_LOCALE_OVERRIDE_PARAM value if it was added from the proxyURL query parameters
        if ( queryParams.stream().anyMatch( param -> param.getName().equals( TRUST_LOCALE_OVERRIDE_PARAM ) ) ) {
          queryParams = queryParams.stream()
            .filter( param -> !param.getName().equals( TRUST_LOCALE_OVERRIDE_PARAM ) )
            .collect( Collectors.toList() );
        }

        queryParams.add( new BasicNameValuePair( TRUST_LOCALE_OVERRIDE_PARAM, LocaleHelper.getLocale().toString() ) );
      }
    }

    uriBuilder.setParameters( queryParams );

    debug( Messages.getInstance().getString( "ProxyServlet.DEBUG_0001_OUTPUT_URL", uriBuilder.toString() ) );

    return uriBuilder.build();
  }

  protected void doProxyCore( final URI requestUri, final HttpServletRequest request, final HttpServletResponse response ) {
    final HttpPost method = new HttpPost( requestUri );

    // Now do the request
    try ( CloseableHttpClient client = HttpClientManager.getInstance().createDefaultClient() ) {
      // Copy the POST request body to the proxy request
      if ( request.getMethod().equals( HttpPost.METHOD_NAME ) ) {
        final ByteArrayOutputStream content = new ByteArrayOutputStream();

        copyContent( request.getInputStream(), content );

        final HttpEntity entity = new ByteArrayEntity(
          content.toByteArray(),
          ContentType.getByMimeType( request.getContentType() ) );

        method.setEntity( entity );
      }

      // Execute the method.
      final HttpResponse httpResponse = client.execute( method );

      // Validate the proxy response code
      final StatusLine statusLine = httpResponse.getStatusLine();
      if ( statusLine.getStatusCode() != HttpStatus.SC_OK ) {
        error( Messages.getInstance().getErrorString(
          "ProxyServlet.ERROR_0003_REMOTE_HTTP_CALL_FAILED", statusLine.toString() ) );
        return;
      }

      // Set Content-Type response header if it is missing from the response
      if ( StringUtils.isEmpty( response.getContentType() )
        && httpResponse.getEntity().getContentType() != null
        && StringUtils.isNotEmpty( httpResponse.getEntity().getContentType().getValue() ) ) {

        response.setContentType( httpResponse.getEntity().getContentType().getValue() );
      }

      // Copy the proxy response content to the response
      copyContent( httpResponse.getEntity().getContent(), response.getOutputStream() );

    } catch ( UnsupportedOperationException | IOException e ) {
      error( Messages.getInstance().getErrorString(
        "ProxyServlet.ERROR_0005_TRANSPORT_FAILURE" ), e );
    } finally {
      method.releaseConnection();
    }
  }

  @VisibleForTesting
  protected void copyContent( InputStream in, OutputStream out ) throws IOException {
    if ( in == null || out == null ) {
      return;
    }

    int inCnt;
    byte[] buf = new byte[ 2048 ];

    while ( -1 != ( inCnt = in.read( buf ) ) ) {
      out.write( buf, 0, inCnt );
    }
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
