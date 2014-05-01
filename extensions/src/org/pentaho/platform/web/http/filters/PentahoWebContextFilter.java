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

package org.pentaho.platform.web.http.filters;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.util.messages.LocaleHelper;

import com.google.gwt.regexp.shared.RegExp;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * If the request is searching for a webcontext.js, it writes out the content of the webcontext.js
 * 
 * @author Ramaiz Mansoor
 * 
 */
public class PentahoWebContextFilter implements Filter {

  public static final String WEB_CONTEXT_JS = "webcontext.js"; //$NON-NLS-1$
  static final String FILTER_APPLIED = "__pentaho_web_context_filter_applied"; //$NON-NLS-1$
  static final String initialComment =
      "/** webcontext.js is created by a PentahoWebContextFilter. This filter searches for an " + //$NON-NLS-1$
          "incoming URI having \"webcontext.js\" in it. If it finds that, "
        + "it write CONTEXT_PATH and FULLY_QUALIFIED_SERVER_URL"
          + //$NON-NLS-1$
          " and it values from the servlet request to this js **/ \n\n\n"; //$NON-NLS-1$
  static final byte[] initialCommentBytes = initialComment.getBytes();
  String fullyQualifiedUrl = null;
  private static final String JS = ".js"; //$NON-NLS-1$
  private static final String CSS = ".css"; //$NON-NLS-1$
  private static final String CONTEXT = "context"; //$NON-NLS-1$
  private static final String GLOBAL = "global"; //$NON-NLS-1$
  private static final byte[] REQUIRE_JS_CFG_START =
      "var requireCfg = {waitSeconds: 30, paths: {}, shim: {}};\n".getBytes(); //$NON-NLS-1$
  private static final String REQUIRE_JS = "requirejs"; //$NON-NLS-1$
  // Changed to not do so much work for every request
  private static final ThreadLocal<byte[]> THREAD_LOCAL_CONTEXT_PATH = new ThreadLocal<byte[]>();
  private static final ThreadLocal<byte[]> THREAD_LOCAL_REQUIRE_SCRIPT = new ThreadLocal<byte[]>();

  public void destroy() {
    // TODO Auto-generated method stub

  }

  protected void close( OutputStream out ) {
    try {
      out.close();
    } catch ( IOException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,
    ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String requestStr = httpRequest.getRequestURI();

    if ( requestStr != null && requestStr.endsWith( WEB_CONTEXT_JS )
        && httpRequest.getAttribute( FILTER_APPLIED ) == null ) {
      httpRequest.setAttribute( FILTER_APPLIED, Boolean.TRUE );
      // split out a fully qualified url, guaranteed to have a trailing slash
      IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
      String contextPath = requestContext.getContextPath();
      try {
        response.setContentType( "text/javascript" ); //$NON-NLS-1$
        OutputStream out = response.getOutputStream();
        out.write( initialCommentBytes );
        
        byte[] contextPathBytes = THREAD_LOCAL_CONTEXT_PATH.get();
        byte[] requireScriptBytes = THREAD_LOCAL_REQUIRE_SCRIPT.get();
        if ( contextPathBytes == null ) {
          String webContext = "var CONTEXT_PATH = '" + contextPath + "';\n\n"; //$NON-NLS-1$ //$NON-NLS-2$
          contextPathBytes = webContext.getBytes();
          THREAD_LOCAL_CONTEXT_PATH.set( contextPathBytes );
          if ( requireScriptBytes == null ) {
            String requireJsLocation = "content/common-ui/resources/web/require.js";
            String requireJsConfigLocation = "content/common-ui/resources/web/require-cfg.js";
            String requireScript =
                "document.write(\"<script type='text/javascript' src='" + contextPath
                + requireJsLocation + "'></scr\"+\"ipt>\");\n"
                + "document.write(\"<script type=\'text/javascript\' src='" + contextPath
                + requireJsConfigLocation + "'></scr\"+\"ipt>\");\n";
            requireScriptBytes = requireScript.getBytes();
            THREAD_LOCAL_REQUIRE_SCRIPT.set( requireScriptBytes );
          }
        }

        String basicAuthFlag = (String) httpRequest.getSession().getAttribute( "BasicAuth" );
        if ( basicAuthFlag != null && basicAuthFlag.equals( "true" ) ) {
          out.write( ( "document.write(\"<script type='text/javascript' src='"
            + contextPath + "js/postAuth.js'></scr\"+\"ipt>\");\n" )
              .getBytes( "UTF-8" ) );
        }

        out.write( contextPathBytes );
        out.write( fullyQualifiedUrl.getBytes() );
        // Compute the effective locale and set it in the global scope. Also provide it as a module if the RequireJs
        // system is available.
        Locale effectiveLocale = LocaleHelper.getLocale();
        if ( !StringUtils.isEmpty( request.getParameter( "locale" ) ) ) {
          effectiveLocale = new Locale( request.getParameter( "locale" ) );
        }

        // setup the RequireJS config object for plugins to extend
        out.write( REQUIRE_JS_CFG_START );

        // Let all plugins contribute to the RequireJS config
        printResourcesForContext( REQUIRE_JS, out, httpRequest, false );

        out.write( requireScriptBytes );

        printSessionName( out );
        printLocale( effectiveLocale, out );
        printHomeFolder( out );
        printReservedChars( out );
        printReservedRegexPattern( out );

        // print global resources defined in plugins
        printResourcesForContext( GLOBAL, out, httpRequest, false );

        // print out external-resources defined in plugins if a context has been passed in
        String contextName = request.getParameter( CONTEXT );
        boolean cssOnly = "true".equals( request.getParameter( "cssOnly" ) );
        if ( StringUtils.isNotEmpty( contextName ) ) {
          printResourcesForContext( contextName, out, httpRequest, cssOnly );
        }

        // Any subclass can add more information to webcontext.js
        addCustomInfo( out );

        out.close();
        return;
      } finally {
        httpRequest.removeAttribute( FILTER_APPLIED );
      }
    } else {
      chain.doFilter( httpRequest, httpResponse );
      return;
    }
  }

  private void printHomeFolder( OutputStream out ) throws IOException {
    StringBuilder sb = new StringBuilder( "<!-- Providing home folder location for UI defaults -->\n" );
    if ( PentahoSessionHolder.getSession() != null ) {
      String homePath = ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
      sb.append( "var HOME_FOLDER = '" + homePath + "';\n" ); // Global variable
    } else {
      sb.append( "var HOME_FOLDER = null;\n" ); // Global variable
    }
    out.write( sb.toString().getBytes() );
  }
  
  private void printReservedChars( OutputStream out ) throws IOException {
    StringBuilder sb = new StringBuilder();
    for ( char c : JcrRepositoryFileUtils.getReservedChars() ) {
      sb.append( c );
    }
    String scriptLine =
        "var RESERVED_CHARS = \""
            + StringEscapeUtils.escapeJavaScript( sb.toString() )
            + "\";\n";
    out.write( scriptLine.getBytes() );
  }
  
  private void printReservedRegexPattern ( OutputStream out ) throws IOException {
    String scriptLine = "var RESERVED_CHARS_REGEX_PATTERN = /" + makeReservedCharPattern() + "/;\n";
    out.write( scriptLine.getBytes() );
  }
  
  private static String makeReservedCharPattern() {
    // escape all reserved characters as they may have special meaning to regex engine
    StringBuilder buf = new StringBuilder();
    buf.append( ".*[" ); //$NON-NLS-1$
    for ( Character ch : JcrRepositoryFileUtils.getReservedChars() ) {
      buf.append( StringEscapeUtils.escapeJavaScript( ch.toString() ) );
    }
    buf.append( "]+.*" ); //$NON-NLS-1$
    return buf.toString();
  }

  private void printSessionName( OutputStream out ) throws IOException {
    StringBuilder sb = new StringBuilder( "<!-- Providing name for session -->\n" );
    if ( PentahoSessionHolder.getSession() == null ) {
      sb.append( "var SESSION_NAME = null;\n" ); // Global variable
    } else {
      sb.append( "var SESSION_NAME = '" + PentahoSessionHolder.getSession().getName() + "';\n" ); // Global variable
    }
    out.write( sb.toString().getBytes() );
  }

  private void printLocale( Locale effectiveLocale, OutputStream out ) throws IOException {
    StringBuilder sb =
        new StringBuilder( "<!-- Providing computed Locale for session -->\n" ).append(
          "var SESSION_LOCALE = '" + effectiveLocale.toString() + "';\n" ) // Global variable
            // If RequireJs is available, supply a module
            .append(
              "if(typeof(pen) != 'undefined' && pen.define){pen.define('Locale', {locale:'"
                + effectiveLocale.toString() + "'})};" );
    out.write( sb.toString().getBytes() );
  }

  private void printResourcesForContext( String contextName, OutputStream out, HttpServletRequest request,
      boolean printCssOnly ) throws IOException {

    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
    Encoder encoder = ESAPI.encoder();

    HttpServletRequest req = ( (HttpServletRequest) request );
    String reqStr = "";
    Map paramMap = req.getParameterMap();

    // Fix for BISERVER-7613, BISERVER-7614, BISERVER-7615
    // Make sure that parameters in the URL are encoded for Javascript safety since they'll be
    // added to Javascript fragments that get executed.
    if ( paramMap.size() > 0 ) {
      StringBuilder sb = new StringBuilder();
      Map.Entry<String, String[]> me = null;
      char sep = '?'; // first separator is '?'
      Iterator<Map.Entry<String, String[]>> it = paramMap.entrySet().iterator();
      int i;
      while ( it.hasNext() ) {
        me = it.next();
        for ( i = 0; i < me.getValue().length; i++ ) {
          sb.append( sep ).append( encoder.encodeForJavaScript( me.getKey().toString() ) ).append( "=" ).append(
            encoder.encodeForJavaScript( me.getValue()[ i ] ) );
        }
        if ( sep == '?' ) {
          sep = '&'; // change the separator
        }
      }
      reqStr = sb.toString(); // get the request string.
    }

    List<String> externalResources = pluginManager.getExternalResourcesForContext( contextName );
    out.write( ( "<!-- Injecting web resources defined in by plugins as external-resources for: "
      + encoder.encodeForHTML(
          contextName ) + "-->\n" ).getBytes() ); //$NON-NLS-1$ //$NON-NLS-2$
    if ( externalResources != null ) {

      for ( String res : externalResources ) {
        if ( res == null ) {
          continue;
        }
        if ( res.endsWith( JS ) && !printCssOnly ) {
          out.write( ( "document.write(\"<script language='javascript' type='text/javascript' src='\"+CONTEXT_PATH + \"" + res.trim() + reqStr + "'></scr\"+\"ipt>\");\n" //$NON-NLS-1$ //$NON-NLS-2$
            ).getBytes() );
        } else if ( res.endsWith( CSS ) ) {
          out.write( ( "document.write(\"<link rel='stylesheet' type='text/css' href='\"+CONTEXT_PATH + \"" + res.trim() + reqStr + "'/>\");\n" //$NON-NLS-1$ //$NON-NLS-2$
            ).getBytes() );
        }
      }
    }

  }

  protected void addCustomInfo( OutputStream out ) throws IOException {

  }

  public void init( FilterConfig filterConfig ) throws ServletException {
    // split out a fully qualified url, guaranteed to have a trailing slash
    String fullyQualifiedServerURL = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
    if ( !fullyQualifiedServerURL.endsWith( "/" ) ) { //$NON-NLS-1$
      fullyQualifiedServerURL += "/"; //$NON-NLS-1$
    }
    fullyQualifiedUrl = "var FULL_QUALIFIED_URL = '" + fullyQualifiedServerURL + "';\n\n"; //$NON-NLS-1$ //$NON-NLS-2$  
  }

}
