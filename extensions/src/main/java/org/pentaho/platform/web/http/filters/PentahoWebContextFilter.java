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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.filters;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.owasp.encoder.Encode;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.util.messages.LocaleHelper;

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
  String serverProtocol = null;
  private static final String JS = ".js"; //$NON-NLS-1$
  private static final String CSS = ".css"; //$NON-NLS-1$
  private static final String CONTEXT = "context"; //$NON-NLS-1$
  private static final String GLOBAL = "global"; //$NON-NLS-1$
  private static final String REQUIRE_JS = "requirejs"; //$NON-NLS-1$
  // Changed to not do so much work for every request
  private static final ThreadLocal<byte[]> THREAD_LOCAL_REQUIRE_SCRIPT = new ThreadLocal<byte[]>();
  protected static ICacheManager cache = PentahoSystem.getCacheManager( null );

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
      IPentahoRequestContext requestContext = getRequestContext();
      String contextPath = requestContext.getContextPath();

      final boolean shouldUseFullyQualifiedUrl = shouldUseFullyQualifiedUrl( httpRequest );
      if ( shouldUseFullyQualifiedUrl ) {
        contextPath = getFullyQualifiedServerURL();
      }

      try {
        response.setContentType( "text/javascript" );
        OutputStream out = response.getOutputStream();
        out.write( initialCommentBytes );

        String webContext = "var CONTEXT_PATH = '" + contextPath + "';\n\n";

        out.write( webContext.getBytes() );
        out.write( fullyQualifiedUrl.getBytes() );
        out.write( serverProtocol.getBytes() );
        // Compute the effective locale and set it in the global scope. Also provide it as a module if the RequireJs
        // system is available.
        Locale effectiveLocale = LocaleHelper.getLocale();
        if ( !StringUtils.isEmpty( request.getParameter( "locale" ) ) ) {
          effectiveLocale = new Locale( request.getParameter( "locale" ) );
        }

        // context name variable
        String contextName = request.getParameter( CONTEXT );
        printContextName( contextName, out );

        // active_theme variable
        printActiveTheme( httpRequest, out );
        // setup a RequireJS config object for plugins to extend
        printRequireJsCfgStart( out );

        // Let all plugins contribute to the RequireJS config
        printResourcesForContext( REQUIRE_JS, out, httpRequest, false );

        byte[] requireScriptBytes = THREAD_LOCAL_REQUIRE_SCRIPT.get();
        if ( requireScriptBytes == null ) {
          String requireJsLocation = "content/common-ui/resources/web/require.js";
          String requireJsConfigLocation = "content/common-ui/resources/web/require-cfg.js";
          String requireScript =
              "document.write(\"<script type='text/javascript' src='\" + CONTEXT_PATH + \""
                  + requireJsLocation + "'></scr\"+\"ipt>\");\n"
                  + "document.write(\"<script type=\'text/javascript\' src='\" + CONTEXT_PATH + \""
                  + requireJsConfigLocation + "'></scr\"+\"ipt>\");\n";
          requireScriptBytes = requireScript.getBytes();
          THREAD_LOCAL_REQUIRE_SCRIPT.set( requireScriptBytes );
        }

        out.write( requireScriptBytes );

        printSessionName( out );
        printLocale( effectiveLocale, out );
        printHomeFolder( out );
        printReservedChars( out );
        printReservedCharsDisplay( out );
        printReservedRegexPattern( out );

        boolean noOsgiRequireConfig = "true".equals( request.getParameter( "noOsgiRequireConfig" ) );
        if ( !noOsgiRequireConfig && !"anonymousUser".equals( getSession().getName() ) ) {
          final String useFullyQualifiedUrlParameter = httpRequest.getParameter( "fullyQualifiedUrl" );

          out.write( ( "document.write(\"<script type='text/javascript' src='\" + CONTEXT_PATH + \""
              + "osgi/requirejs-manager/js/require-init.js?requirejs=false"
              + ( useFullyQualifiedUrlParameter != null ? "&fullyQualifiedUrl=" + useFullyQualifiedUrlParameter : "" )
              + "'></scr\"+\"ipt>\");\n" ).getBytes( "UTF-8" ) );
        }

        boolean requireJsOnly = "true".equals( request.getParameter( "requireJsOnly" ) );

        if ( !requireJsOnly ) {
          // print global resources defined in plugins
          printResourcesForContext( GLOBAL, out, httpRequest, false );

          // print out external-resources defined in plugins if a context has been passed in
          boolean cssOnly = "true".equals( request.getParameter( "cssOnly" ) );
          if ( StringUtils.isNotEmpty( contextName ) ) {
            printResourcesForContext( contextName, out, httpRequest, cssOnly );
          }
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


  private boolean shouldUseFullyQualifiedUrl( HttpServletRequest httpRequest ) {
    final String useFullyQualifiedUrlParameter = httpRequest.getParameter( "fullyQualifiedUrl" );
    if ( useFullyQualifiedUrlParameter != null ) {
      return "true".equals( useFullyQualifiedUrlParameter );
    } else {
      // Returning false for now. The smart way of determining whether we should use the fully
      // qualified url did not work behind the proxy server and this case
      // http://jira.pentaho.com/browse/BACKLOG-16728 was created.
      return false;
    }
  }

  private void printHomeFolder( OutputStream out ) throws IOException {
    StringBuilder sb = new StringBuilder( "// Providing home folder location for UI defaults\n" );
    if ( getSession() != null ) {
      String homePath = ClientRepositoryPaths.getUserHomeFolderPath( StringEscapeUtils
          .escapeJavaScript( getSession().getName() ) );
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

  private void printReservedCharsDisplay( OutputStream out ) throws IOException {
    List<Character> reservedCharacters = JcrRepositoryFileUtils.getReservedChars();
    StringBuffer sb = new StringBuffer();
    for ( int i = 0; i < reservedCharacters.size(); i++ ) {
      if ( reservedCharacters.get( i ) >= 0x07 && reservedCharacters.get( i ) <= 0x0d ) {
        sb.append( StringEscapeUtils.escapeJava( "" + reservedCharacters.get( i ) ) );
      } else {
        sb.append( reservedCharacters.get( i ) );
      }
      if ( i + 1 < reservedCharacters.size() ) {
        sb.append( ", " );
      }
    }
    String scriptLine =
        "var RESERVED_CHARS_DISPLAY = \""
            + StringEscapeUtils.escapeJavaScript( sb.toString() )
            + "\";\n";
    out.write( scriptLine.getBytes() );
  }

  private void printReservedRegexPattern( OutputStream out ) throws IOException {
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
    StringBuilder sb = new StringBuilder( "// Providing name for session\n" );
    if ( getSession() == null ) {
      sb.append( "var SESSION_NAME = null;\n" ); // Global variable
    } else {
      sb.append( "var SESSION_NAME = '"
          + StringEscapeUtils.escapeJavaScript( getSession().getName() ) + "';\n" ); // Global variable
    }
    out.write( sb.toString().getBytes() );
  }

  private void printLocale( Locale effectiveLocale, OutputStream out ) throws IOException {
    StringBuilder sb =
        new StringBuilder( "// Providing computed Locale for session\n" ).append(
            "var SESSION_LOCALE = '" + effectiveLocale.toString() + "';\n" ) // Global variable
            // If RequireJs is available, supply a module
            .append(
                "if(typeof(pen) != 'undefined' && pen.define){pen.define('Locale', {locale:'"
                    + effectiveLocale.toString() + "'})};" );
    out.write( sb.toString().getBytes() );
  }

  private void printContextName( String contextName, OutputStream out ) throws IOException {
    StringBuilder sb = new StringBuilder( "// Providing name for context\n" );

    sb.append( "var PENTAHO_CONTEXT_NAME = '"
        + StringEscapeUtils.escapeJavaScript( contextName ) + "';\n\n" ); // Global variable

    out.write( sb.toString().getBytes() );
  }

  private void printActiveTheme( HttpServletRequest request, OutputStream out ) throws IOException {

    // NOTE: this code should be kept in sync with that of ThemeServlet.java

    StringBuilder sb = new StringBuilder( "// Providing active theme\n" );

    IPentahoSession session = getSession();

    String activeTheme = (String) session.getAttribute( "pentaho-user-theme" );

    String ua = request.getHeader( "User-Agent" );
    // check if we're coming from a mobile device, if so, lock to system default (crystal)
    if ( !StringUtils.isEmpty( ua ) && ua.matches( ".*(?i)(iPad|iPod|iPhone|Android).*" ) ) {
      activeTheme = PentahoSystem.getSystemSetting( "default-theme", "crystal" );
    }

    if ( activeTheme == null ) {
      IUserSettingService settingsService = PentahoSystem.get( IUserSettingService.class, session );

      try {
        activeTheme = settingsService.getUserSetting( "pentaho-user-theme", null ).getSettingValue();
      } catch ( Exception ignored ) {
        // the user settings service is not valid in the agile-bi deployment of the server
      }

      if ( activeTheme == null ) {
        activeTheme = PentahoSystem.getSystemSetting( "default-theme", "crystal" );
      }
    }

    sb.append( "var active_theme = '"
        + StringEscapeUtils.escapeJavaScript( activeTheme )
        + "';\n\n" ); // Global variable

    out.write( sb.toString().getBytes() );
  }

  private void printResourcesForContext( String contextName, OutputStream out, HttpServletRequest request,
                                         boolean printCssOnly ) throws IOException {

    IPluginManager pluginManager = getPluginManager();

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
          sb.append( sep ).append( Encode.forJavaScript( me.getKey().toString() ) ).append( "=" ).append(
              Encode.forJavaScript( me.getValue()[i] ) );
        }
        if ( sep == '?' ) {
          sep = '&'; // change the separator
        }
      }
      reqStr = sb.toString(); // get the request string.
    }

    List<String> externalResources = pluginManager.getExternalResourcesForContext( contextName );
    out.write( ( "<!-- Injecting web resources defined in by plugins as external-resources for: "
        + Encode.forHtml(
        contextName ) + "-->\n" ).getBytes() ); //$NON-NLS-1$ //$NON-NLS-2$
    if ( externalResources != null ) {

      for ( String res : externalResources ) {
        if ( res == null ) {
          continue;
        }
        if ( res.endsWith( JS ) && !printCssOnly ) {
          out.write( ( "document.write(\"<script language='javascript' type='text/javascript' src='\" + CONTEXT_PATH + \"" + res.trim() + reqStr + "'></scr\"+\"ipt>\");\n" //$NON-NLS-1$ //$NON-NLS-2$
          ).getBytes() );
        } else if ( res.endsWith( CSS ) ) {
          out.write( ( "document.write(\"<link rel='stylesheet' type='text/css' href='\" + CONTEXT_PATH + \"" + res.trim() + reqStr + "'/>\");\n" //$NON-NLS-1$ //$NON-NLS-2$
          ).getBytes() );
        }
      }
    }

  }

  protected void printRequireJsCfgStart( OutputStream out ) throws IOException {

    Integer waitTime = null;

    if ( cache != null ) {

      waitTime = (Integer) cache.getFromGlobalCache( PentahoSystem.WAIT_SECONDS );

    }

    if ( waitTime == null ) {
      try {
        waitTime = Integer.valueOf( PentahoSystem.getSystemSetting( PentahoSystem.WAIT_SECONDS, "30" ) );
      } catch ( NumberFormatException e ) {
        waitTime = 30;
      }
      if ( cache != null ) {
        cache.putInGlobalCache( PentahoSystem.WAIT_SECONDS, waitTime );
      }
    }

    String requireJsCfgStart = "var requireCfg = {waitSeconds: " + waitTime
        + ", paths: {}, shim: {}, map: {\"*\": {}}, bundles: {}, config: {\"pentaho/service\": {}}, packages: []};\n";
    out.write( requireJsCfgStart.getBytes() );

  }

  protected void addCustomInfo( OutputStream out ) throws IOException {

  }

  public void init( FilterConfig filterConfig ) throws ServletException {
    String fullyQualifiedServerURL = getFullyQualifiedServerURL();

    String serverProtocolValue;
    if ( fullyQualifiedServerURL.startsWith( "http" ) ) {
      serverProtocolValue = fullyQualifiedServerURL.substring( 0, fullyQualifiedServerURL.indexOf( ":" ) );
    } else {
      serverProtocolValue = "http";
    }
    fullyQualifiedUrl = "var FULL_QUALIFIED_URL = '" + fullyQualifiedServerURL + "';\n\n"; //$NON-NLS-1$ //$NON-NLS-2$
    serverProtocol = "var SERVER_PROTOCOL = '" + serverProtocolValue + "';\n\n"; //$NON-NLS-1$ //$NON-NLS-2$
  }

  private String getFullyQualifiedServerURL() {
    // split out a fully qualified url, guaranteed to have a trailing slash
    String fullyQualifiedServerURL = getApplicationContext().getFullyQualifiedServerURL();
    if ( !fullyQualifiedServerURL.endsWith( "/" ) ) {
      fullyQualifiedServerURL += "/";
    }
    return fullyQualifiedServerURL;
  }

  IApplicationContext getApplicationContext() {
    return PentahoSystem.getApplicationContext();
  }

  IPentahoRequestContext getRequestContext() {
    return PentahoRequestContextHolder.getRequestContext();
  }

  IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  IPluginManager getPluginManager() {
    return PentahoSystem.get( IPluginManager.class );
  }

}
