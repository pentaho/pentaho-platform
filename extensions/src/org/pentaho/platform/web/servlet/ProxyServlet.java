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

package org.pentaho.platform.web.servlet;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * This servlet is used to Proxy a Servlet request to another server for processing and returns that result to the
 * caller as if this Servlet actiually serviced it. Setup the proxy by editing the <b>web.xml</b> to map the servlet
 * name you want to proxy to the Proxy Servlet class.
 * <p>
 * 
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
 * 
 * In the above example, all requests to /ViewAction will be forwarded to the ViewAction Servlet running on the Pentaho
 * server atmy.remoteserver.com:8080
 * 
 * <p>
 * NOTES:
 * <p>
 * 
 * For this to be useful, both Pentaho servers should be using the same database repository.
 * <p>
 * The recieving server should have the ProxyTrustingFilter enabled to handle authicentation.
 * <p>
 * This Servlet only works with GET requests. All requests in the Pentaho BI Platform are currently gets.
 * 
 * @see com.pentaho.test.servlet.ProxyTrustingFilter
 * @author Doug Moran
 * 
 */
public class ProxyServlet extends ServletBase {

  /**
   * 
   */
  private static final long serialVersionUID = 4680027723733552639L;

  private static final Log logger = LogFactory.getLog( ProxyServlet.class );

  @Override
  public Log getLogger() {
    return ProxyServlet.logger;
  }

  String proxyURL = null; // "http://localhost:8080/pentaho";

  String errorURL = null; // The URL to redirect to if the user is invalid

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
      proxyURL.trim();
      try {
        URL url = new URL( proxyURL ); // Just doing this to verify
        // it's good
        info( Messages.getInstance().getString( "ProxyServlet.INFO_0001_URL_SELECTED", url.toExternalForm() ) ); // using 'url' to get rid of unused var compiler warning //$NON-NLS-1$
      } catch ( Throwable t ) {
        error( Messages.getInstance().getErrorString( "ProxyServlet.ERROR_0002_INVALID_URL", proxyURL ) ); //$NON-NLS-1$
        proxyURL = null;
      }
    }

    errorURL = servletConfig.getInitParameter( "ErrorURL" );
    super.init( servletConfig );
  }

  protected void doProxy( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
    if ( proxyURL == null ) { // Got nothing from web.xml
      return;
    }

    String servletPath = request.getServletPath();
    // System .out.println( ">>>>>>>> REQ: " + request.getRequestURL().toString() ); //$NON-NLS-1$//$NON-NLS-2$
    PentahoSystem.systemEntryPoint();
    try {
      String theUrl = proxyURL + servletPath;
      PostMethod method = new PostMethod( theUrl );

      // Copy the parameters from the request to the proxy
      // System .out.print( ">>>>>>>> PARAMS: " ); //$NON-NLS-1$
      Map paramMap = request.getParameterMap();
      Map.Entry entry;
      String[] array;
      for ( Iterator it = paramMap.entrySet().iterator(); it.hasNext(); ) {
        entry = (Map.Entry) it.next();
        array = (String[]) entry.getValue();
        for ( String element : array ) {
          method.addParameter( (String) entry.getKey(), element );
          // System.out.print( (String)entry.getKey() + "=" + array[i]
          // + "&" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
      // System.out.println( "" ); //$NON-NLS-1$

      // Just in case someone is trying to spoof the proxy
      method.removeParameter( "_TRUST_USER_" ); //$NON-NLS-1$

      // Get the user from the session
      IPentahoSession userSession = getPentahoSession( request );
      String name = userSession.getName();

      // Add the trusted user from the session
      if ( ( name != null ) && ( name.length() > 0 ) ) {
        method.addParameter( "_TRUST_USER_", name ); //$NON-NLS-1$
        // System.out.println( ">>>>>>>> USR: " + name ); //$NON-NLS-1$
      } else if ( ( errorURL != null ) && ( errorURL.trim().length() > 0 ) ) {
        response.sendRedirect( errorURL );
        // System.out.println( ">>>>>>>> REDIR: " + errorURL );
        // //$NON-NLS-1$
        return;
      }

      // System.out.println( ">>>>>>>> PROXY: " + theUrl ); //$NON-NLS-1$
      debug( Messages.getInstance().getString( "ProxyServlet.DEBUG_0001_OUTPUT_URL", theUrl ) ); //$NON-NLS-1$

      // Now do the request
      HttpClient client = new HttpClient();

      try {
        // Execute the method.
        int statusCode = client.executeMethod( method );

        if ( statusCode != HttpStatus.SC_OK ) {
          error( Messages.getInstance().getErrorString(
              "ProxyServlet.ERROR_0003_REMOTE_HTTP_CALL_FAILED", method.getStatusLine().toString() ) ); //$NON-NLS-1$
          return;
        }
        setHeader( "Content-Type", method, response ); //$NON-NLS-1$
        setHeader( "Content-Length", method, response ); //$NON-NLS-1$

        InputStream inStr = method.getResponseBodyAsStream();
        ServletOutputStream outStr = response.getOutputStream();

        int inCnt = 0;
        byte[] buf = new byte[2048];
        while ( -1 != ( inCnt = inStr.read( buf ) ) ) {
          outStr.write( buf, 0, inCnt );
        }
      } catch ( HttpException e ) {
        error( Messages.getInstance().getErrorString( "ProxyServlet.ERROR_0004_PROTOCOL_FAILURE" ), e ); //$NON-NLS-1$
        e.printStackTrace();
      } catch ( IOException e ) {
        error( Messages.getInstance().getErrorString( "ProxyServlet.ERROR_0005_TRANSPORT_FAILURE" ), e ); //$NON-NLS-1$
        e.printStackTrace();
      } finally {
        method.releaseConnection();
      }
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  void setHeader( final String headerStr, final HttpMethod method, final HttpServletResponse response ) {
    Header header = method.getResponseHeader( headerStr );
    if ( header != null ) {
      response.setHeader( headerStr, header.getValue() );
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
