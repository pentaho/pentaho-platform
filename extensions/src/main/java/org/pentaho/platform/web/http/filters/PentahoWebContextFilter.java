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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * If the request is searching for a webcontext.js, it writes out the content of the webcontext.js
 */
public class PentahoWebContextFilter implements Filter {

  public static final String WEB_CONTEXT_JS = "webcontext.js"; //$NON-NLS-1$

  private static final String REQUIREJS_LOCATION = "content/common-ui/resources/web/require.js";
  private static final String REQUIREJS_CONFIG_LOCATION = "content/common-ui/resources/web/require-cfg.js";
  private static final String REQUIREJS_INIT_LOCATION = "osgi/requirejs-manager/js/require-init.js";

  static final String FILTER_APPLIED = "__pentaho_web_context_filter_applied"; //$NON-NLS-1$
  static final String initialComment =
      "/** webcontext.js is created by a PentahoWebContextFilter. This filter searches for an " + //$NON-NLS-1$
          "incoming URI having \"webcontext.js\" in it. If it finds that, "
          + "it write CONTEXT_PATH and FULLY_QUALIFIED_SERVER_URL"
          + //$NON-NLS-1$
          " and it values from the servlet request to this js **/ \n\n\n"; //$NON-NLS-1$
  static final byte[] initialCommentBytes = initialComment.getBytes();

  private static final String JS = ".js"; //$NON-NLS-1$
  private static final String CSS = ".css"; //$NON-NLS-1$
  private static final String CONTEXT = "context"; //$NON-NLS-1$
  private static final String GLOBAL = "global"; //$NON-NLS-1$
  private static final String REQUIRE_JS = "requirejs"; //$NON-NLS-1$

  // Changed to not do so much work for every request
  private static final ThreadLocal<byte[]> THREAD_LOCAL_REQUIRE_SCRIPT = new ThreadLocal<>();
  protected static ICacheManager cache = PentahoSystem.getCacheManager( null );

  private Map<String, String> environmentVariables;

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {
    this.environmentVariables = new HashMap<>();
  }

  @Override
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

  @Override
  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,
      ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String requestStr = httpRequest.getRequestURI();
    boolean isWebContextJSRequest = requestStr != null && requestStr.endsWith( WEB_CONTEXT_JS );

    if ( isWebContextJSRequest && httpRequest.getAttribute( FILTER_APPLIED ) == null ) {
      httpRequest.setAttribute( FILTER_APPLIED, Boolean.TRUE );

      try {
        response.setContentType( "text/javascript" );
        OutputStream out = response.getOutputStream();

        out.write( initialCommentBytes );

        printWebContextVar( out, "CONTEXT_PATH", getContextPathVar( httpRequest ) );

        printWebContextVar( out, "FULL_QUALIFIED_URL", getFullyQualifiedServerUrlVar() );

        printWebContextVar( out, "SERVER_PROTOCOL", getServerProtocolVar() );

        printWebContextVar( out, "PENTAHO_CONTEXT_NAME", getContextNameVar( httpRequest ) );

        printWebContextVar( out, "active_theme", getActiveThemeVar( httpRequest ) );

        printWebContextVar( out, "requireCfg", getRequireCfgVar(), false, false );

        // config for 'pentaho/context' amd module
        printPentahoEnvironmentConfig( out, httpRequest );

        // Let all plugins contribute to the RequireJS config
        printResourcesForContext( REQUIRE_JS, out, httpRequest, false );

        byte[] requireScriptBytes = THREAD_LOCAL_REQUIRE_SCRIPT.get();
        if ( requireScriptBytes == null ) {
          printDocumentWrite( out, REQUIREJS_LOCATION );
          printDocumentWrite( out, REQUIREJS_CONFIG_LOCATION );
        } else {
          out.write( requireScriptBytes );
        }

        printWebContextVar( out, "SESSION_NAME", getSessionNameVar() );
        // Compute the effective locale and set it in the global scope. Also provide it as a module if the RequireJs
        // system is available.
        printWebContextVar( out, "SESSION_LOCALE", getLocaleVar( httpRequest ) );

        printWebContextVar( out, "HOME_FOLDER", getHomeFolderVar() );

        printWebContextVar( out, "RESERVED_CHARS", getReservedCharsVar() );

        printWebContextVar( out, "RESERVED_CHARS_DISPLAY", getReservedCharsDisplayVar() );

        printWebContextVar( out, "RESERVED_CHARS_REGEX_PATTERN", getReservedRegexPatternVar(), true, false );

        boolean noOsgiRequireConfig = "true".equals( request.getParameter( "noOsgiRequireConfig" ) );
        if ( !noOsgiRequireConfig && !"anonymousUser".equals( getSession().getName() ) ) {
          final String useFullyQualifiedUrlParameter = httpRequest.getParameter( "fullyQualifiedUrl" );

          String requireInitSrc = REQUIREJS_INIT_LOCATION + "?requirejs=false" + ( useFullyQualifiedUrlParameter != null
                  ? "&fullyQualifiedUrl=" + useFullyQualifiedUrlParameter
                  : "" );

          printDocumentWrite( out, requireInitSrc );
        }

        boolean requireJsOnly = "true".equals( request.getParameter( "requireJsOnly" ) );

        if ( !requireJsOnly ) {
          // print global resources defined in plugins
          printResourcesForContext( GLOBAL, out, httpRequest, false );

          // print out external-resources defined in plugins if a context has been passed in
          boolean cssOnly = "true".equals( request.getParameter( "cssOnly" ) );

          String contextName = getContextNameVar( httpRequest );
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

  Integer getRequireWaitTime() {
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

    return waitTime;
  }

  // region get Environment Variables
  private String getActiveThemeVar( HttpServletRequest request ) {
    return this.environmentVariables.computeIfAbsent( "active_theme", s -> {
      IPentahoSession session = getSession();

      String activeTheme = (String) session.getAttribute( "pentaho-user-theme" );

      String ua = request.getHeader( "User-Agent" );
      // check if we're coming from a mobile device, if so, lock to system default (crystal)
      if ( StringUtils.isNotEmpty( ua ) && ua.matches( ".*(?i)(iPad|iPod|iPhone|Android).*" ) ) {
        activeTheme = PentahoSystem.getSystemSetting( "default-theme", "crystal" );
      }

      if ( activeTheme == null ) {
        IUserSettingService settingsService = getUserSettingsService();

        try {
          activeTheme = settingsService.getUserSetting( "pentaho-user-theme", null ).getSettingValue();
        } catch ( Exception ignored ) {
          // the user settings service is not valid in the agile-bi deployment of the server
        }

        if ( activeTheme == null ) {
          activeTheme = PentahoSystem.getSystemSetting( "default-theme", "crystal" );
        }
      }

      return activeTheme;

    } );
  }

  private String getLocaleVar( HttpServletRequest request ) {
    return this.environmentVariables.computeIfAbsent( "SESSION_LOCALE", s -> {
      String requestLocale = request.getParameter( "locale" );

      boolean hasLocaleParam = StringUtils.isNotEmpty( requestLocale );

      return hasLocaleParam ? requestLocale : LocaleHelper.getLocale().toString();
    } );
  }

  private String getSessionNameVar() {
    return this.environmentVariables.computeIfAbsent( "SESSION_NAME", s -> {
      if ( getSession() == null ) {
        return null;
      }

      return getSession().getName();
    } );
  }

  private String getHomeFolderVar() {
    return this.environmentVariables.computeIfAbsent( "HOME_FOLDER", s -> {
      if ( getSession() == null ) {
        return null;
      }

      String sessionName = getSession().getName();
      return ClientRepositoryPaths.getUserHomeFolderPath( sessionName );
    } );
  }

  private String getReservedCharsVar() {
    return this.environmentVariables.computeIfAbsent( "RESERVED_CHARS", s -> {
      StringBuilder sb = new StringBuilder();
      for ( char c : getRepositoryReservedChars() ) {
        sb.append( c );
      }

      return sb.toString();
    } );
  }

  private String getReservedCharsDisplayVar() {
    return this.environmentVariables.computeIfAbsent( "RESERVED_CHARS_DISPLAY", s -> {
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

      return sb.toString();
    } );
  }

  private String getReservedRegexPatternVar() {
    return this.environmentVariables.computeIfAbsent( "RESERVED_CHARS_REGEX_PATTERN", s ->
            "/" + makeReservedCharPattern() + "/" );
  }

  private String getContextNameVar( HttpServletRequest request ) {
    return this.environmentVariables.computeIfAbsent( "PENTAHO_CONTEXT_NAME", s -> request.getParameter( CONTEXT ) );
  }

  protected String getFullyQualifiedServerUrlVar() {
    return this.environmentVariables.computeIfAbsent( "FULL_QUALIFIED_URL", s -> {
      // split out a fully qualified url, guaranteed to have a trailing slash
      String fullyQualifiedServerURL = getApplicationContext().getFullyQualifiedServerURL();
      if ( !fullyQualifiedServerURL.endsWith( "/" ) ) {
        fullyQualifiedServerURL += "/";
      }
      return fullyQualifiedServerURL;
    } );
  }

  private String getContextPathVar( HttpServletRequest request ) {
    return this.environmentVariables.computeIfAbsent( "CONTEXT_PATH", s -> {
      // split out a fully qualified url, guaranteed to have a trailing slash
      IPentahoRequestContext requestContext = getRequestContext();
      String contextPath = requestContext.getContextPath();

      final boolean shouldUseFullyQualifiedUrl = shouldUseFullyQualifiedUrl( request );
      if ( shouldUseFullyQualifiedUrl ) {
        contextPath = getFullyQualifiedServerUrlVar();
      }

      return contextPath;
    } );
  }

  private String getServerProtocolVar() {
    return this.environmentVariables.computeIfAbsent( "SERVER_PROTOCOL", s -> {
      String fullyQualifiedServerURL = getFullyQualifiedServerUrlVar();
      if ( fullyQualifiedServerURL.startsWith( "http" ) ) {
        return fullyQualifiedServerURL.substring( 0, fullyQualifiedServerURL.indexOf( ":" ) );
      }

      return "http";
    } );
  }

  private String getRequireCfgVar() {
    // setup a RequireJS config object for plugins to extend
    StringBuilder requireCfg = new StringBuilder();

    requireCfg
            .append( "{" )
            .append( "\n  waitSeconds: " ).append( getRequireWaitTime() ).append( "," )
            .append( "\n  paths: {}," )
            .append( "\n  shim: {}," )
            .append( "\n  map: { \"*\": {} }," )
            .append( "\n  bundles: {}," )
            .append( "\n  config: { \"pentaho/service\": {} }," )
            .append( "\n  packages: []" )
            .append( "}" );

    return requireCfg.toString();
  }
  // endregion

  // region Print Methods
  private void printResourcesForContext( String contextName, OutputStream out, HttpServletRequest request,
                                         boolean printCssOnly ) throws IOException {

    IPluginManager pluginManager = getPluginManager();

    String reqStr = "";
    Map paramMap = request.getParameterMap();

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
          out.write( ( "document.write(\"<link rel='stylesheet' type='text/css' href='\" + CONTEXT_PATH + \"" + res.trim() + reqStr + "'/>\");\n\n" //$NON-NLS-1$ //$NON-NLS-2$
          ).getBytes() );
        }
      }
    }

  }

  private void printWebContextVar( OutputStream out, String variable, String value ) throws IOException {
    printWebContextVar( out, variable, value, true, true );
  }

  private void printWebContextVar( OutputStream out, String variable, String value,
                                   boolean deprecated, boolean escapeValue ) throws IOException {
    if ( escapeValue ) {
      value = escapeEnvironmentVar( value );
    }

    String deprecatedComment = deprecated
            ? "\n/** @deprecated - use 'pentaho/context' module's variable instead */"
            : "";

    StringBuilder environmentVariable =
            new StringBuilder( deprecatedComment );
    environmentVariable.append( "\nvar " ).append( variable ).append( " = " ).append( value ).append( ";\n" );

    if ( variable.equals( "SESSION_LOCALE" ) ) {
      // If RequireJs is available, supply a module
      environmentVariable
              .append( "if (typeof(pen) !== 'undefined' && pen.define) {\n" )
              .append( "  pen.define('Locale', {locale: " ).append( value ).append( " })\n};\n\n" );
    }

    out.write( environmentVariable.toString().getBytes( "UTF-8" ) );
  }

  private void printPentahoEnvironmentConfig( OutputStream out, HttpServletRequest request ) throws IOException {
    String theme = escapeEnvironmentVar( getActiveThemeVar( request ) );
    String locale = escapeEnvironmentVar( getLocaleVar( request ) );
    String userID = escapeEnvironmentVar( getSessionNameVar() );
    String userHome = escapeEnvironmentVar( getHomeFolderVar() );

    String reservedChars = escapeEnvironmentVar( getReservedCharsVar() );
    String serverURL = escapeEnvironmentVar( getServerUrl( request ) );

    StringBuilder environmentModule = new StringBuilder( "// configuration for 'pentaho/context' amd module\n" );
    environmentModule
            //TODO: when we rename the module 'pentaho/context' to 'pentaho/environment', also change it here
            .append( "\nrequireCfg.config[\"pentaho/context\"] = {" )

            .append( "\n  theme: " ).append( theme ).append( "," )
            .append( "\n  locale: " ).append( locale ).append( "," )

            .append( "\n  user: {" )
            .append( "\n    id: " ).append( userID ).append( "," )
            .append( "\n    home: " ).append( userHome )
            .append( "\n  }," )

            .append( "\n  reservedChars: " ).append( reservedChars ).append( "," )

            .append( "\n  server: {" )
            .append( "\n    url: " ).append( serverURL )
            .append( "\n  }" )

            .append( "\n};\n" );

    out.write( environmentModule.toString().getBytes( "UTF-8" ) );
  }

  private void printDocumentWrite( OutputStream out, String location ) throws IOException {
    String script = "\ndocument.write(\"<script type='text/javascript' " +
            "src='\" + CONTEXT_PATH + \"" + location + "'></scr\"+\"ipt>\");\n";

    out.write( script.getBytes( "UTF-8" ) );
  }
  // endregion

  private String escapeEnvironmentVar( String value ) {
    if ( value != null ) {
      value = "\"" + StringEscapeUtils.escapeJavaScript( value ) + "\"";
    }

    return value;
  }

  String getServerUrl( HttpServletRequest request ) {
    String url = getFullyQualifiedServerUrlVar();
    if ( url != null && !url.isEmpty() ) {
      return url;
    }

    url = getContextPathVar( request );
    if ( url != null && !url.isEmpty() ) {
      return url;
    }

    url = getServerProtocolVar();
    if ( url != null && !url.isEmpty() ) {
      return url;
    }

    return null;
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

  protected void addCustomInfo( OutputStream out ) throws IOException {
  }

  // region package-private methods for unit testing mock/spying
  List<Character> getRepositoryReservedChars() {
    return JcrRepositoryFileUtils.getReservedChars();
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

  IUserSettingService getUserSettingsService() {
    return PentahoSystem.get( IUserSettingService.class, getSession() );
  }
  // endregion
}
