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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.filters;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.owasp.encoder.Encode;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.ConfigurationAdminNonOsgiProxy;
import org.pentaho.platform.web.http.api.resources.services.FileService;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * If the request is searching for a webcontext.js, it writes out the content of the webcontext.js
 */
public class PentahoWebContextFilter implements Filter {

  public static final String WEB_CONTEXT_JS = "webcontext.js"; //$NON-NLS-1$

  public static final String PARAM_SSO_ENABLED = "ssoEnabled";

  private static final String REQUIREJS_LOCATION = "content/common-ui/resources/web/require.js";
  private static final String REQUIREJS_CONFIG_LOCATION = "content/common-ui/resources/web/require-cfg.js";
  private static final String REQUIREJS_INIT_LOCATION = "osgi/requirejs-manager/js/require-init.js";

  static final String DEFAULT_OSGI_BRIDGE = "osgi/";
  static final String DEFAULT_SERVICES_ROOT = "cxf/";

  static final String PLATFORM_OSGI_BRIDGE_ID = "proxy";
  private static final String SERVICES_PERSISTENCE_ID = "org.apache.cxf.osgi";
  private static final String SERVICES_CONTEXT_PROPERTY = "org.apache.cxf.servlet.context";

  static final String USE_FULL_URL_PARAM = "useFullyQualifiedUrl";

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
  private static final String APPLICATION = "application"; //$NON-NLS-1$
  private static final String GLOBAL = "global"; //$NON-NLS-1$
  private static final String REQUIRE_JS = "requirejs"; //$NON-NLS-1$
  private FileService fileService;

  private String ssoEnabled = null;

  // Changed to not do so much work for every request
  private static final ThreadLocal<byte[]> THREAD_LOCAL_REQUIRE_SCRIPT = new ThreadLocal<>();
  protected static ICacheManager cache = PentahoSystem.getCacheManager( null );

  private LazyInitializer<String> lazyServicesPath;
  private ConfigurationAdminNonOsgiProxy configurationAdminProxy;

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {
    this.configurationAdminProxy = new ConfigurationAdminNonOsgiProxy();
    this.lazyServicesPath = new LazyInitializer<String>() {

      @Override
      protected String initialize() throws ConcurrentException {
        return initializeServicesPath();
      }
    };
    this.setSsoEnabled( filterConfig.getInitParameter( PARAM_SSO_ENABLED ) );
    fileService = new FileService();
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
  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
          throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String requestStr = httpRequest.getRequestURI();
    boolean isWebContextJSRequest = requestStr != null && requestStr.endsWith( WEB_CONTEXT_JS );

    if ( isWebContextJSRequest && httpRequest.getAttribute( FILTER_APPLIED ) == null ) {
      httpRequest.setAttribute( FILTER_APPLIED, Boolean.TRUE );

      try {
        response.setContentType( "text/javascript" );
        OutputStream out = response.getOutputStream();
        HashMap<String, String> webContextVariables = getWebContextVariables( httpRequest );

        out.write( initialCommentBytes );

        printWebContextVar( out, webContextVariables, "CONTEXT_PATH" );

        printWebContextVar( out, webContextVariables, "FULL_QUALIFIED_URL" );

        printWebContextVar( out, webContextVariables, "SERVER_PROTOCOL" );

        printWebContextVar( out, webContextVariables, "PENTAHO_CONTEXT_NAME" );

        printWebContextVar( out, webContextVariables, "active_theme" );

        printWebContextVar( out, webContextVariables, "requireCfg", false, false );

        // This var will enable correct redirect in Session Expire Dialog for a SSO scenario
        if ( getSsoEnabled() != null ) {
          printWebContextVar( out, webContextVariables, "ssoEnabled", false, false );
        }

        // config for 'pentaho/environment' amd module
        printPentahoEnvironmentConfig( out, webContextVariables );

        // Let all plugins contribute to the RequireJS config
        printResourcesForContext( REQUIRE_JS, out, httpRequest, false );

        byte[] requireScriptBytes = THREAD_LOCAL_REQUIRE_SCRIPT.get();
        if ( requireScriptBytes == null ) {
          printDocumentWrite( out, REQUIREJS_LOCATION );
          printDocumentWrite( out, REQUIREJS_CONFIG_LOCATION );
        } else {
          out.write( requireScriptBytes );
        }

        printWebContextVar( out, webContextVariables, "SESSION_NAME" );

        // Compute the effective locale and set it in the global scope. Also provide it as a module if the RequireJs
        // system is available.
        printWebContextVar( out, webContextVariables, "SESSION_LOCALE" );
        printLocaleModule( out, webContextVariables );

        printWebContextVar( out, webContextVariables, "HOME_FOLDER" );

        printWebContextVar( out, webContextVariables, "DEFAULT_FOLDER" );

        printWebContextVar( out, webContextVariables, "RESERVED_CHARS" );

        printWebContextVar( out, webContextVariables, "RESERVED_CHARS_DISPLAY" );

        printWebContextVar( out, webContextVariables, "RESERVED_CHARS_REGEX_PATTERN", true, false );

        boolean noOsgiRequireConfig = "true".equals( request.getParameter( "noOsgiRequireConfig" ) );
        if ( !noOsgiRequireConfig && !"anonymousUser".equals( getSession().getName() ) ) {
          final String useFullyQualifiedUrlParameter = httpRequest.getParameter( USE_FULL_URL_PARAM );

          String requireInitSrc = REQUIREJS_INIT_LOCATION + "?requirejs=false" + ( useFullyQualifiedUrlParameter != null
                  ? "&" + USE_FULL_URL_PARAM + "=" + useFullyQualifiedUrlParameter
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
    IPentahoSession session = getSession();

    String activeTheme = (String) session.getAttribute( "pentaho-user-theme" );

    String ua = request.getHeader( "User-Agent" );
    // check if we're coming from a mobile device, if so, lock to system default (ruby)
    if ( StringUtils.isNotEmpty( ua ) && ua.matches( ".*(?i)(iPad|iPod|iPhone|Android).*" ) ) {
      activeTheme = PentahoSystem.getSystemSetting( "default-theme", "ruby" );
    }

    if ( activeTheme == null ) {
      IUserSettingService settingsService = getUserSettingsService();

      try {
        activeTheme = settingsService.getUserSetting( "pentaho-user-theme", null ).getSettingValue();
      } catch ( Exception ignored ) {
        // the user settings service is not valid in the agile-bi deployment of the server
      }

      if ( activeTheme == null ) {
        activeTheme = PentahoSystem.getSystemSetting( "default-theme", "ruby" );
      }
    }

    return activeTheme;

  }

  HashMap<String, String> getWebContextVariables( HttpServletRequest request ) throws IOException {
    HashMap<String, String> map = new HashMap<>();

    map.put( "requireCfg", getRequireCfgVar() );                             // Global JS variable
    map.put( "ssoEnabled", getSsoEnabled() );                                // Global JS variable

    map.put( "application", getApplicationVar( request ) );                  // Internal variable
    map.put( "PENTAHO_CONTEXT_NAME", getContextNameVar( request ) );         // Global JS environment variable
    map.put( "FULL_QUALIFIED_URL", getFullyQualifiedServerUrlVar() );        // Global JS environment variable
    map.put( "CONTEXT_PATH", getContextPathVar( request ) );                 // Global JS environment variable
    map.put( "SERVER_PROTOCOL", getServerProtocolVar() );                    // Global JS environment variable

    map.put( "active_theme", getActiveThemeVar( request ) );                 // Global JS environment variable
    map.put( "SESSION_LOCALE", getLocaleVar( request ) );                    // Global JS environment variable
    map.put( "SESSION_NAME", getSessionNameVar() );                          // Global JS environment variable
    map.put( "HOME_FOLDER", getHomeFolderVar() );                            // Global JS environment variable
    map.put( "DEFAULT_FOLDER", getDefaultFolderVar() );                      // Global JS environment variable

    map.put( "RESERVED_CHARS", getReservedCharsVar() );                      // Global JS environment variable
    map.put( "RESERVED_CHARS_DISPLAY", getReservedCharsDisplayVar() );       // Global JS environment variable
    map.put( "RESERVED_CHARS_REGEX_PATTERN", getReservedRegexPatternVar() ); // Global JS environment variable

    map.put( PLATFORM_OSGI_BRIDGE_ID, getOsgiBridgePath( request ) );        // Internal variable
    map.put( SERVICES_CONTEXT_PROPERTY, getServicesPath() );                 // Internal variable

    return map;
  }

  private String getLocaleVar( HttpServletRequest request ) {
    String requestLocale = request.getParameter( "locale" );

    boolean hasLocaleParam = StringUtils.isNotEmpty( requestLocale );

    return hasLocaleParam ? requestLocale : LocaleHelper.getLocale().toString();
  }

  private String getSessionNameVar() {
    if ( getSession() == null ) {
      return null;
    }

    return getSession().getName();
  }

  private String getHomeFolderVar() {
    if ( getSession() == null ) {
      return null;
    }

    String sessionName = getSession().getName();
    return ClientRepositoryPaths.getUserHomeFolderPath( sessionName );
  }

  private String getDefaultFolderVar() {
    IPentahoSession session = getSession();
    if ( getSession() == null ) {
      return null;
    }
    String sessionName = session.getName();
    String anonymousUser = PentahoSystem.getSystemSetting( "anonymous-authentication/anonymous-user",
        "anonymousUser" );
    if ( session.isAuthenticated() && !anonymousUser.equals( sessionName ) ) {
      String path = ClientRepositoryPaths.getUserHomeFolderPath( sessionName );
      return fileService.doGetDefaultLocation( path );
    } else {
      String hidePropertyValue = PentahoSystem.get( ISystemConfig.class )
          .getProperty( PentahoSystem.HIDE_USER_HOME_FOLDER_ON_CREATION_PROPERTY );
      Boolean hideUserHomeFolder = hidePropertyValue != null && "true".equals( hidePropertyValue.toLowerCase() );
      if ( hideUserHomeFolder ) {
        return  ClientRepositoryPaths.getPublicFolderPath();
      } else {
        return ClientRepositoryPaths.getUserHomeFolderPath( sessionName );
      }
    }
  }

  private String getReservedCharsVar() {
    StringBuilder sb = new StringBuilder();
    for ( char c : getRepositoryReservedChars() ) {
      sb.append( c );
    }

    return sb.toString();
  }

  private String getReservedCharsDisplayVar() {
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
  }

  private String getReservedRegexPatternVar() {
    return "/" + makeReservedCharPattern() + "/";
  }

  private String getContextNameVar( HttpServletRequest request ) {
    return request.getParameter( CONTEXT );
  }

  private String getApplicationVar( HttpServletRequest request ) {
    return request.getParameter( APPLICATION );
  }

  private String getFullyQualifiedServerUrlVar() {
    // split out a fully qualified url, guaranteed to have a trailing slash
    String fullyQualifiedServerURL = getApplicationContext().getFullyQualifiedServerURL();
    if ( !fullyQualifiedServerURL.endsWith( "/" ) ) {
      fullyQualifiedServerURL += "/";
    }
    return fullyQualifiedServerURL;
  }

  private String getContextPathVar( HttpServletRequest request ) {
    // split out a fully qualified url, guaranteed to have a trailing slash
    IPentahoRequestContext requestContext = getRequestContext();
    String contextPath = requestContext.getContextPath();

    final boolean shouldUseFullyQualifiedUrl = shouldUseFullyQualifiedUrl( request );
    if ( shouldUseFullyQualifiedUrl ) {
      contextPath = getFullyQualifiedServerUrlVar();
    }

    return contextPath;
  }

  private String getServerProtocolVar() {
    String fullyQualifiedServerURL = getFullyQualifiedServerUrlVar();
    if ( fullyQualifiedServerURL.startsWith( "http" ) ) {
      return fullyQualifiedServerURL.substring( 0, fullyQualifiedServerURL.indexOf( ":" ) );
    }

    return "http";
  }

  private String getRequireCfgVar() {
    // setup a RequireJS config object for plugins to extend
    StringBuilder requireCfg = new StringBuilder();

    requireCfg
            .append( "{" )
            .append( "\n  waitSeconds: " ).append( getRequireWaitTime() ).append( "," )
            .append( "\n  paths: {}," )
            .append( "\n  shim: {}," )
            .append( "\n  map: {\"*\": {}}," )
            .append( "\n  bundles: {}," )
            .append( "\n  config: {\"pentaho/modules\": {}}," )
            .append( "\n  packages: []" )
            .append( "\n}" );

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
      Map.Entry<String, String[]> me;
      char sep = '?'; // first separator is '?'

      Iterator<Map.Entry<String, String[]>> it = paramMap.entrySet().iterator();
      int i;
      while ( it.hasNext() ) {
        me = it.next();
        for ( i = 0; i < me.getValue().length; i++ ) {
          sb.append( sep ).append( Encode.forJavaScript( me.getKey() ) ).append( "=" ).append(
              Encode.forJavaScript( me.getValue()[i] ) );
        }
        if ( sep == '?' ) {
          sep = '&'; // change the separator
        }
      }
      reqStr = sb.toString(); // get the request string.
    }

    List<String> externalResources = pluginManager.getExternalResourcesForContext( contextName );
    out.write( ( "\n<!-- Injecting web resources defined in by plugins as external-resources for: "
        + Encode.forHtml( contextName ) + "-->" ).getBytes() ); //$NON-NLS-1$ //$NON-NLS-2$

    if ( externalResources != null ) {
      for ( String res : externalResources ) {
        if ( res == null ) {
          continue;
        }

        if ( res.endsWith( JS ) && !printCssOnly ) {
          out.write( ( "\ndocument.write(\"<script language='javascript' type='text/javascript' src='\" + CONTEXT_PATH + \"" + res.trim() + reqStr + "'></scr\"+\"ipt>\");" //$NON-NLS-1$ //$NON-NLS-2$
          ).getBytes() );
        } else if ( res.endsWith( CSS ) ) {
          out.write( ( "\ndocument.write(\"<link rel='stylesheet' type='text/css' href='\" + CONTEXT_PATH + \"" + res.trim() + reqStr + "'/>\");" //$NON-NLS-1$ //$NON-NLS-2$
          ).getBytes() );
        }
      }
    }

  }

  private void printWebContextVar( OutputStream out, HashMap<String, String> webContextVariables,
                                   String variable ) throws IOException {
    printWebContextVar( out, webContextVariables, variable, true, true );
  }

  private void printWebContextVar( OutputStream out, HashMap<String, String> webContextVariables,
                                   String variable, boolean deprecated, boolean escapeValue ) throws IOException {
    String value = webContextVariables.get( variable );
    if ( escapeValue ) {
      value = escapeEnvironmentVar( value );
    }

    String deprecatedComment = deprecated
            ? "\n/** @deprecated - use 'pentaho/environment' module's variable instead */"
            : "";

    StringBuilder environmentVariable =
            new StringBuilder( deprecatedComment );
    environmentVariable.append( "\nvar " ).append( variable ).append( " = " ).append( value ).append( ";\n" );

    out.write( environmentVariable.toString().getBytes( "UTF-8" ) );
  }

  private void printLocaleModule( OutputStream out, HashMap<String, String> webContextVariables ) throws IOException {
    String value = escapeEnvironmentVar( webContextVariables.get( "SESSION_LOCALE" ) );

    StringBuilder localeModule = new StringBuilder( "// If RequireJs is available, supply a module" );

    localeModule
            .append( "\nif (typeof(pen) !== 'undefined' && pen.define) {" )
            .append( "\n  pen.define('Locale', {locale: " ).append( value ).append( " });" )
            .append( "\n}\n" );


    out.write( localeModule.toString().getBytes( "UTF-8" ) );
  }

  private void printPentahoEnvironmentConfig( OutputStream out, HashMap<String, String> webContextVariables )
          throws IOException {
    String application = escapeEnvironmentVar( webContextVariables.get( "application" ) );
    String theme = escapeEnvironmentVar( webContextVariables.get( "active_theme" ) );
    String locale = escapeEnvironmentVar( webContextVariables.get( "SESSION_LOCALE" ) );
    String userID = escapeEnvironmentVar( webContextVariables.get( "SESSION_NAME" ) );
    String userHome = escapeEnvironmentVar( webContextVariables.get( "HOME_FOLDER" ) );
    String reservedChars = escapeEnvironmentVar( webContextVariables.get( "RESERVED_CHARS" ) );
    String serverRoot = escapeEnvironmentVar( getServerRoot( webContextVariables ) );
    String serverPackages = escapeEnvironmentVar( getServerPackages( webContextVariables ) );

    String serverServices = escapeEnvironmentVar( getServerServices( webContextVariables ) );

    StringBuilder environmentModule = new StringBuilder( "\n// configuration for 'pentaho/environment' amd module" );
    environmentModule
            .append( "\nrequireCfg.config[\"pentaho/environment\"] = {" )
            .append( "\n  application: " ).append( application ).append( "," )
            .append( "\n  theme: " ).append( theme ).append( "," )
            .append( "\n  locale: " ).append( locale ).append( "," )

            .append( "\n  user: {" )
            .append( "\n    id: " ).append( userID ).append( "," )
            .append( "\n    home: " ).append( userHome )
            .append( "\n  }," )

            .append( "\n  reservedChars: " ).append( reservedChars ).append( "," )

            .append( "\n  server: {" )
            .append( "\n    root: " ).append( serverRoot ).append( "," )
            .append( "\n    packages: " ).append( serverPackages ).append( "," )
            .append( "\n    services: " ).append( serverServices )
            .append( "\n  }" )

            .append( "\n};\n" );

    out.write( environmentModule.toString().getBytes( "UTF-8" ) );
  }

  private void printDocumentWrite( OutputStream out, String location ) throws IOException {
    String script = "\ndocument.write(\"<script type='text/javascript' "
            + "src='\" + CONTEXT_PATH + \"" + location + "'></scr\"+\"ipt>\");\n";

    out.write( script.getBytes( "UTF-8" ) );
  }
  // endregion

  private String getServicesPath() {
    try {
      return this.lazyServicesPath.get();
    } catch ( ConcurrentException ce ) {
      return DEFAULT_SERVICES_ROOT;
    }
  }

  private String getOsgiBridgePath( HttpServletRequest request ) {
    String osgiBridgeMapping = "";
    ServletContext servletContext = request.getServletContext();

    ServletRegistration osgiBridgeRegistration = servletContext.getServletRegistration( PLATFORM_OSGI_BRIDGE_ID );
    if ( osgiBridgeRegistration != null ) {
      Collection<String> osgiBridgeMappings = osgiBridgeRegistration.getMappings();

      boolean hasMappings = osgiBridgeMappings != null && osgiBridgeMappings.size() > 0;
      if ( hasMappings ) {
        // Assuming that only one mapping is defined
        osgiBridgeMapping = (String) osgiBridgeMappings.toArray()[0];
      }
    }

    if ( StringUtils.isEmpty( osgiBridgeMapping ) ) {
      osgiBridgeMapping = DEFAULT_OSGI_BRIDGE;
    }

    return normalizeURL( osgiBridgeMapping );
  }

  private String escapeEnvironmentVar( String value ) {
    if ( value != null ) {
      value = "\"" + StringEscapeUtils.escapeJavaScript( value ) + "\"";
    }

    return value;
  }

  private String normalizeURL( String url ) {
    boolean isUrlValid = StringUtils.isNotEmpty( url );

    if ( isUrlValid && url.startsWith( "/" ) ) {
      url = url.substring( 1 );
    }

    // Special case for osgi bridge mapping defined in Platform's web.xml
    if ( isUrlValid && url.endsWith( "*" ) ) {
      int urlLength = url.length();
      url = url.substring( 0, urlLength - 1 );
    }

    if ( isUrlValid && !url.endsWith( "/" ) ) {
      url = url + "/";
    }

    return url;
  }

  String getServerRoot( HashMap<String, String> webContextVariables ) {
    String root = webContextVariables.get( "CONTEXT_PATH" );

    if ( root != null && !root.isEmpty() ) {
      return root;
    }

    root = webContextVariables.get( "SERVER_PROTOCOL" );
    if ( root != null && !root.isEmpty() ) {
      return root;
    }

    return null;
  }

  String getServerPackages( HashMap<String, String> webContextVariables ) {
    String root = getServerRoot( webContextVariables );
    String osgiPath = webContextVariables.get( PLATFORM_OSGI_BRIDGE_ID );

    return root + osgiPath;
  }

  String getServerServices( HashMap<String, String> webContextVariables ) {
    String root = getServerRoot( webContextVariables );
    String osgiPath = webContextVariables.get( PLATFORM_OSGI_BRIDGE_ID );
    String servicesPath = webContextVariables.get( SERVICES_CONTEXT_PROPERTY );

    return root + osgiPath + servicesPath;
  }

  private boolean shouldUseFullyQualifiedUrl( HttpServletRequest httpRequest ) {
    final String useFullyQualifiedUrlParameter = httpRequest.getParameter( USE_FULL_URL_PARAM );

    return "true".equals( useFullyQualifiedUrlParameter );
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

  String initializeServicesPath() {
    Dictionary<String, Object> properties = this.configurationAdminProxy.getProperties( SERVICES_PERSISTENCE_ID );

    String servicesRoot = "";
    if ( properties != null ) {
      servicesRoot = (String) properties.get( SERVICES_CONTEXT_PROPERTY );
    }

    if ( StringUtils.isEmpty( servicesRoot ) ) {
      servicesRoot = DEFAULT_SERVICES_ROOT;
    }

    return normalizeURL( servicesRoot );
  }

  public String getSsoEnabled() {
    return ssoEnabled;
  }

  public void setSsoEnabled( String ssoEnabled ) {
    this.ssoEnabled = ssoEnabled;
  }
  // endregion
}
