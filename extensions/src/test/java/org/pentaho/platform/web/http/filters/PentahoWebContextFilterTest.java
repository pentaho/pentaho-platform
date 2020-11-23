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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.services.FileService;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PentahoWebContextFilterTest {
  private String contextRoot;
  private String fullyQualifiedServerURL;
  private String serverAddress;
  private String serverScheme;

  private String activeTheme;
  private String sessionName;
  private List<Character> reservedChars;

  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private java.io.ByteArrayOutputStream mockResponseOutputStream;

  private PentahoWebContextFilter pentahoWebContextFilter;

  @After
  public void cleanup() throws IOException, ServletException {
    PentahoSystem.shutdown();
  }

  @Before
  public void setup() throws IOException, ServletException {
    this.serverScheme = "https";
    String serverName = "di.pentaho.local";
    int port = 9055;


    this.serverAddress = this.serverScheme + "://" + serverName + ":" + port;

    this.contextRoot = "/the/context/root/";
    this.fullyQualifiedServerURL = this.serverAddress + this.contextRoot;

    this.mockRequest = mock( HttpServletRequest.class );

    final ISystemSettings settingsService = mock( ISystemSettings.class );
    when( settingsService.getSystemSetting( eq( "anonymous-authentication/anonymous-user" )  , eq("anonymousUser"))).thenReturn( "anonymousUser");
    PentahoSystem.setSystemSettingsService( settingsService );
    final ISystemConfig systemConfig = mock( ISystemConfig.class );

    final IPentahoObjectFactory objectFactory = mock( IPentahoObjectFactory.class );
    when( objectFactory.objectDefined( eq( ISystemConfig.class ) ) ).thenReturn( true );
    final IPentahoObjectReference pentahoObjectReference = mock( IPentahoObjectReference.class );
    when( pentahoObjectReference.getObject() ).thenReturn( systemConfig );
    try {
      when( objectFactory.getObjectReferences( eq( ISystemConfig.class ), any( IPentahoSession.class ),
          any( Map.class ) ) ).thenReturn( new LinkedList() { {
        add( pentahoObjectReference );
      } } );
    } catch ( ObjectFactoryException e ) {
      e.printStackTrace();
    }
    when( settingsService.getSystemSetting( anyString(), anyString() ) ).thenReturn( "" );
    PentahoSystem.registerObjectFactory( objectFactory );

    PentahoSystem.init();

    ServletContext mockServletContext = mock( ServletContext.class );
    ServletRegistration mockServletRegistration = mock( ServletRegistration.class );
    Collection<String> mappings = new ArrayList<>(1);
    mappings.add( PentahoWebContextFilter.DEFAULT_OSGI_BRIDGE );
    when( mockServletRegistration.getMappings() ).thenReturn( mappings );
    when( mockServletContext.getServletRegistration( PentahoWebContextFilter.PLATFORM_OSGI_BRIDGE_ID ) )
            .thenReturn( mockServletRegistration );
    when( this.mockRequest.getServletContext() ).thenReturn( mockServletContext );
    when( this.mockRequest.getRequestURI() ).thenReturn( "/somewhere/" + PentahoWebContextFilter.WEB_CONTEXT_JS );

    when( this.mockRequest.getScheme() ).thenReturn( this.serverScheme );
    when( this.mockRequest.getServerName() ).thenReturn( serverName );
    when( this.mockRequest.getServerPort() ).thenReturn( port );

    when( this.mockRequest.getHeader( "referer" ) ).thenReturn( this.serverAddress + "/some/app" );

    this.mockResponse = mock( HttpServletResponse.class );
    this.mockResponseOutputStream = new java.io.ByteArrayOutputStream();
    when( this.mockResponse.getOutputStream() ).thenReturn( new ServletOutputStream() {
      @Override
      public void write( int b ) throws IOException {
        PentahoWebContextFilterTest.this.mockResponseOutputStream.write( b );
      }
    } );

    FilterConfig mockFilterConfig = mock( FilterConfig.class );
    this.pentahoWebContextFilter = spy( new PentahoWebContextFilter() );

    IApplicationContext mockApplicationContext = mock( IApplicationContext.class );
    when( mockApplicationContext.getFullyQualifiedServerURL() ).thenReturn( this.fullyQualifiedServerURL );
    doReturn( mockApplicationContext ).when( this.pentahoWebContextFilter ).getApplicationContext();

    IPentahoRequestContext mockRequestContext = mock( IPentahoRequestContext.class );
    when( mockRequestContext.getContextPath() ).thenReturn( this.contextRoot );
    doReturn( mockRequestContext ).when( this.pentahoWebContextFilter ).getRequestContext();

    this.activeTheme = "xptoTheme";
    IUserSetting mockUserSetting = mock( IUserSetting.class );
    when( mockUserSetting.getSettingValue() ).thenReturn( this.activeTheme );

    IUserSettingService mockUserSettingsService = mock( IUserSettingService.class );
    when( mockUserSettingsService.getUserSetting( "pentaho-user-theme", null ) )
            .thenReturn( mockUserSetting );
    doReturn( mockUserSettingsService ).when( this.pentahoWebContextFilter ).getUserSettingsService();

    this.sessionName = "testSession";
    IPentahoSession mockSession = mock( IPentahoSession.class );
    when( mockSession.getName() ).thenReturn( this.sessionName );
    doReturn( mockSession ).when( this.pentahoWebContextFilter ).getSession();

    this.reservedChars = new ArrayList<>( 2 );
    this.reservedChars.add('r');
    this.reservedChars.add('c');
    doReturn( this.reservedChars ).when( this.pentahoWebContextFilter ).getRepositoryReservedChars();


    IPluginManager mockPluginManager = mock( IPluginManager.class );
    doReturn( mockPluginManager ).when( this.pentahoWebContextFilter ).getPluginManager();
    doReturn( PentahoWebContextFilter.DEFAULT_SERVICES_ROOT )
            .when( this.pentahoWebContextFilter ).initializeServicesPath();

    this.pentahoWebContextFilter.init( mockFilterConfig );
  }

  @Test
  public void testWebContextCachedWaitSecondVariable() throws Exception {

    ICacheManager cacheManager = Mockito.mock( ICacheManager.class );
    when( cacheManager.getFromGlobalCache( PentahoSystem.WAIT_SECONDS ) ).thenReturn( null ).thenReturn( 30 );

    PentahoWebContextFilter filter = new PentahoWebContextFilter();
    PentahoWebContextFilter.cache = cacheManager;

    filter.getRequireWaitTime();
    filter.getRequireWaitTime();

    verify( cacheManager, times( 2 ) ).getFromGlobalCache( eq( PentahoSystem.WAIT_SECONDS ) );
    verify( cacheManager, times( 1 ) ).putInGlobalCache( eq( PentahoSystem.WAIT_SECONDS ), anyObject() );
  }

  @Test
  public void testDoGetWithNoOsgiRequireConfigTrue() throws ServletException, IOException {
    when( this.mockRequest.getParameter( "noOsgiRequireConfig" ) ).thenReturn( "true" );

    final String response = executeWebContextFilter();

    assertFalse( this.requirejsManagerInitIsCalled( response, null ) );
  }

  @Test
  public void testDoGetWithNoOsgiRequireConfigFalse() throws ServletException, IOException {
    when( this.mockRequest.getParameter( "noOsgiRequireConfig" ) ).thenReturn( "false" );

    final String response = executeWebContextFilter();

    assertTrue( this.requirejsManagerInitIsCalled( response, null ) );
  }

  @Test
  public void testDoGetWithNoOsgiRequireConfigDefault() throws ServletException, IOException {
    final String response = executeWebContextFilter();

    assertTrue( this.requirejsManagerInitIsCalled( response, null ) );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlTrue() throws ServletException, IOException {
    when( this.mockRequest.getParameter( PentahoWebContextFilter.USE_FULL_URL_PARAM ) ).thenReturn( "true" );

    final String response = executeWebContextFilter();

    String fullyQualifiedServerURL = this.pentahoWebContextFilter
            .getWebContextVariables( this.mockRequest ).get( "FULL_QUALIFIED_URL" );

    assertTrue( this.responseSetsContextPathGlobal( response, fullyQualifiedServerURL ) );
    assertTrue( this.requirejsManagerInitIsCalled( response, "true" ) );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlFalse() throws ServletException, IOException {
    when( this.mockRequest.getParameter( PentahoWebContextFilter.USE_FULL_URL_PARAM ) ).thenReturn( "false" );

    final String response = executeWebContextFilter();

    assertTrue( this.responseSetsContextPathGlobal( response, this.contextRoot ) );
    assertTrue( this.requirejsManagerInitIsCalled( response, "false" ) );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlDefaultOutsideReferer() throws ServletException, IOException {
    when( this.mockRequest.getHeader( "referer" ) ).thenReturn( "http://dashboard.somewhere.com/other/app" );

    final String response = executeWebContextFilter();

    assertTrue( this.responseSetsContextPathGlobal( response, this.contextRoot ) );
    assertTrue( this.requirejsManagerInitIsCalled( response, null ) );
  }

  // region Defines Environment variable
  @Test
  public void testWebContextDefinesContextPath() throws ServletException, IOException {
    final String response = executeWebContextFilter();

    assertTrue( response.contains( getWebContextVarDefinition( "CONTEXT_PATH", this.contextRoot ) ) );
  }

  @Test
  public void testWebContextDefinesServerProtocol() throws ServletException, IOException {
    final String response = executeWebContextFilter();

    assertTrue( response.contains( getWebContextVarDefinition( "SERVER_PROTOCOL", this.serverScheme ) ) );
  }

  @Test
  public void testWebContextDefinesFullQualifiedUrl() throws ServletException, IOException {
    final String response = executeWebContextFilter();

    assertTrue( response.contains(
            getWebContextVarDefinition( "FULL_QUALIFIED_URL", this.fullyQualifiedServerURL ) ) );
  }

  @Test
  public void testWebContextDefinesPentahoContextName() throws ServletException, IOException {
    String contextName = "testContext";
    when( this.mockRequest.getParameter( "context" ) ).thenReturn( contextName );
    final String response = executeWebContextFilter();

    assertTrue( response.contains( getWebContextVarDefinition( "PENTAHO_CONTEXT_NAME", contextName ) ) );
  }

  @Test
  public void testWebContextDefinesActiveTheme() throws ServletException, IOException {
    final String response = executeWebContextFilter();

    assertTrue( response.contains( getWebContextVarDefinition( "active_theme", this.activeTheme ) ) );
  }

  @Test
  public void testWebContextDefinesSessionName() throws ServletException, IOException {
    final String response = executeWebContextFilter();

    assertTrue( response.contains( getWebContextVarDefinition( "SESSION_NAME", this.sessionName ) ) );
  }

  @Test
  public void testWebContextDefinesSessionLocale() throws ServletException, IOException {
    String sessionLocale = "fo_BA";
    when( this.mockRequest.getParameter( "locale" ) ).thenReturn( sessionLocale );
    final String response = executeWebContextFilter();

    assertTrue( response.contains( getWebContextVarDefinition( "SESSION_LOCALE", sessionLocale ) ) );
  }

  @Test
  public void testWebContextDefinesLocaleModule() throws ServletException, IOException {
    String sessionLocale = "fo_BA";
    when( this.mockRequest.getParameter( "locale" ) ).thenReturn( sessionLocale );
    final String response = executeWebContextFilter();

    String expected = "// If RequireJs is available, supply a module" +
            "\nif (typeof(pen) !== 'undefined' && pen.define) {" +
            "\n  pen.define('Locale', {locale: \"" + sessionLocale + "\" });" +
            "\n}\n";

    assertTrue( response.contains( expected ) );
  }

  @Test
  public void testWebContextDefinesHomeFolder() throws ServletException, IOException {
    final String response = executeWebContextFilter();

    String homeFolder = "/home/" + this.sessionName;
    assertTrue( response.contains( getWebContextVarDefinition( "HOME_FOLDER", homeFolder ) ) );
  }

  @Test
  public void testWebContextDefinesDefaultFolderWhenFolderIsHidden() throws ServletException, IOException {
    FileService fileService = mock(FileService.class);
    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.HIDE_USER_HOME_FOLDER_ON_CREATION_PROPERTY ) ) ).thenReturn( "true" );
    String homeFolder = "/home/" + this.sessionName;
    when (fileService.doGetDefaultLocation( homeFolder  )).thenReturn( "/public" );
    final String response = executeWebContextFilter();

    String defaultFolder = "/public";
    assertTrue( response.contains( getWebContextVarDefinition( "DEFAULT_FOLDER", defaultFolder ) ) );
  }

  @Test
  public void testWebContextDefinesDefaultFolderWhenFolderIsVisible() throws ServletException, IOException {
    FileService fileService = mock(FileService.class);
    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.HIDE_USER_HOME_FOLDER_ON_CREATION_PROPERTY ) ) ).thenReturn( "false" );
    String homeFolder = "/home/" + this.sessionName;
    when (fileService.doGetDefaultLocation( homeFolder  )).thenReturn( homeFolder );
    final String response = executeWebContextFilter();

    String defaultFolder = "/home/" + this.sessionName;
    assertTrue( response.contains( getWebContextVarDefinition( "DEFAULT_FOLDER", defaultFolder ) ) );
  }

  @Test
  public void testWebContextDefinesReservedChars() throws ServletException, IOException {
    final String response = executeWebContextFilter();

    StringBuilder value = new StringBuilder();
    this.reservedChars.forEach(value::append);
    String reservedChars = value.toString();

    assertTrue( response.contains( getWebContextVarDefinition( "RESERVED_CHARS", reservedChars ) ) );
  }
  // endregion

  @Test
  public void testDoGetDefinesRequireCfg() throws ServletException, IOException {
    Integer waitTime = 1337;
    doReturn( waitTime ).when( this.pentahoWebContextFilter ).getRequireWaitTime();

    String response = executeWebContextFilter();

    String expected = "var requireCfg = {" +
            "\n  waitSeconds: " + waitTime + "," +
            "\n  paths: {}," +
            "\n  shim: {}," +
            "\n  map: {\"*\": {}}," +
            "\n  bundles: {}," +
            "\n  config: {\"pentaho/modules\": {}}," +
            "\n  packages: []" +
            "\n};";

    assertTrue( response.contains( expected ) );
  }

  @Test
  public void testWebContextDefinesPentahoEnvironmentModuleConfig() throws ServletException, IOException {
    doReturn( this.fullyQualifiedServerURL ).when( this.pentahoWebContextFilter ).getServerRoot( any() );

    String mockServerPackages = this.fullyQualifiedServerURL + "osgi/";
    doReturn( mockServerPackages ).when( this.pentahoWebContextFilter ).getServerPackages( any() );

    String mockServerServices = this.fullyQualifiedServerURL + "services/";
    doReturn( mockServerServices ).when( this.pentahoWebContextFilter ).getServerServices( any() );

    String serverRoot = escapeEnvironmentVariable( this.fullyQualifiedServerURL );
    String serverPackages = escapeEnvironmentVariable( mockServerPackages );
    String serverServices = escapeEnvironmentVariable( mockServerServices );
    String userHome = escapeEnvironmentVariable( "/home/" + this.sessionName );

    String sessionLocale = "fo_BA";
    when( this.mockRequest.getParameter( "locale" ) ).thenReturn( sessionLocale );

    String application = "pentaho-test";
    when( this.mockRequest.getParameter( "application" ) ).thenReturn( application );

    StringBuilder value = new StringBuilder();
    this.reservedChars.forEach(value::append);
    String reservedChars = value.toString();

    final String response = executeWebContextFilter();

    String environmentModuleConfig = "requireCfg.config[\"pentaho/environment\"] = {" +
            "\n  application: \"" + application + "\"," +
            "\n  theme: \"" + this.activeTheme + "\"," +
            "\n  locale: \"" + sessionLocale + "\"," +
            "\n  user: {" +
            "\n    id: \"" + this.sessionName + "\"," +
            "\n    home: " + userHome +
            "\n  }," +
            "\n  reservedChars: \"" + reservedChars + "\"," +
            "\n  server: {" +
            "\n    root: " + serverRoot + "," +
            "\n    packages: " + serverPackages + "," +
            "\n    services: " + serverServices +
            "\n  }" +
            "\n};";

    assertTrue( response.contains( environmentModuleConfig ) );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlDefaultLocalReferer() throws ServletException, IOException {
    when( this.mockRequest.getHeader( "referer" ) ).thenReturn( this.serverAddress + "/other/app" );
    final String response = executeWebContextFilter();

    assertTrue( this.responseSetsContextPathGlobal( response, this.contextRoot ) );
    assertTrue( this.requirejsManagerInitIsCalled( response, null ) );
  }

  // region Auxiliary Methods
  private boolean responseSetsContextPathGlobal( String response, String contextRoot ) {
    return response.contains( getWebContextVarDefinition( "CONTEXT_PATH", contextRoot ) );
  }

  private boolean requirejsManagerInitIsCalled( String response, String useFullyQualifiedUrlParameter ) {
    final boolean containsScript = response.contains( "requirejs-manager/js/require-init.js?requirejs=false" );
    final String parameterValue = useFullyQualifiedUrlParameter != null ? useFullyQualifiedUrlParameter : "";

    final boolean containsFullyQualifiedUrlParameter = response.contains(
      "&" + PentahoWebContextFilter.USE_FULL_URL_PARAM + "=" + parameterValue
    );

    return containsScript && containsFullyQualifiedUrlParameter == ( useFullyQualifiedUrlParameter != null );
  }

  private String getWebContextVarDefinition( String variable, String value ) {
    String escapedValue = escapeEnvironmentVariable( value );

    return "\n/** @deprecated - use 'pentaho/environment' module's variable instead */" +
            "\nvar " + variable + " = " + escapedValue + ";";
  }

  private String executeWebContextFilter() throws ServletException, IOException {
    this.pentahoWebContextFilter.doFilter( this.mockRequest, this.mockResponse, null );

    return this.mockResponseOutputStream.toString( "UTF-8" );
  }

  private String escapeEnvironmentVariable( String value ) {
    return "\"" + StringEscapeUtils.escapeJavaScript( value ) + "\"";
  }
  // endregion

}
