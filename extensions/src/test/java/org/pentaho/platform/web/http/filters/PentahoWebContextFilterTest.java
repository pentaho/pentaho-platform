/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.web.http.filters;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

  @Before
  public void setup() throws IOException, ServletException {
    this.serverScheme = "https";
    String serverName = "di.pentaho.local";
    int port = 9055;


    this.serverAddress = this.serverScheme + "://" + serverName + ":" + port;

    this.contextRoot = "/the/context/root/";
    this.fullyQualifiedServerURL = this.serverAddress + this.contextRoot;

    this.mockRequest = mock( HttpServletRequest.class );

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

    this.reservedChars = new ArrayList<>(2);
    this.reservedChars.add('r');
    this.reservedChars.add('c');
    doReturn( this.reservedChars ).when( this.pentahoWebContextFilter ).getRepositoryReservedChars();


    IPluginManager mockPluginManager = mock( IPluginManager.class );
    doReturn( mockPluginManager ).when( this.pentahoWebContextFilter ).getPluginManager();

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
    when( this.mockRequest.getParameter( "fullyQualifiedUrl" ) ).thenReturn( "true" );

    final String response = executeWebContextFilter();

    String fullyQualifiedServerURL = this.pentahoWebContextFilter.getFullyQualifiedServerUrlVar();
    assertTrue( this.responseSetsContextPathGlobal( response, fullyQualifiedServerURL ) );
    assertTrue( this.requirejsManagerInitIsCalled( response, "true" ) );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlFalse() throws ServletException, IOException {
    when( this.mockRequest.getParameter( "fullyQualifiedUrl" ) ).thenReturn( "false" );

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
  public void testWebContextDefinesHomeFolder() throws ServletException, IOException {
    final String response = executeWebContextFilter();

    String homeFolder = "/home/" + this.sessionName;
    assertTrue( response.contains( getWebContextVarDefinition( "HOME_FOLDER", homeFolder ) ) );
  }

  @Test
  public void testWebContextDefinesReservedChars() throws ServletException, IOException {
    final String response = executeWebContextFilter();

    StringBuilder value = new StringBuilder();
    this.reservedChars.forEach(value::append);
    String reservedChars = value.toString();

    assertTrue( response.contains( getWebContextVarDefinition( "RESERVED_CHARS", reservedChars ) ) );
  }

  @Test
  public void testDoGetDefinesRequireCfg() throws ServletException, IOException {
    Integer waitTime = 1337;
    doReturn( waitTime ).when( this.pentahoWebContextFilter ).getRequireWaitTime();

    String response = executeWebContextFilter();

    String expected = "var requireCfg = {" +
            "\n  waitSeconds: " + waitTime + "," +
            "\n  paths: {}," +
            "\n  shim: {}," +
            "\n  map: { \"*\": {} }," +
            "\n  bundles: {}," +
            "\n  config: { \"pentaho/service\": {} }," +
            "\n  packages: []" +
            "\n};";

    assertTrue( response.contains( expected ) );
  }

  @Test
  public void testWebContextDefinesPentahoEnvironmentModuleConfig() throws ServletException, IOException {
    doReturn( this.fullyQualifiedServerURL ).when( this.pentahoWebContextFilter ).getServerUrl( this.mockRequest );

    String serverUrl = "\"" + StringEscapeUtils.escapeJavaScript( this.fullyQualifiedServerURL ) + "\"";
    String userHome = "\"" + StringEscapeUtils.escapeJavaScript( "/home/" + this.sessionName ) + "\"";
    String sessionLocale = "fo_BA";
    when( this.mockRequest.getParameter( "locale" ) ).thenReturn( sessionLocale );

    StringBuilder value = new StringBuilder();
    this.reservedChars.forEach(value::append);
    String reservedChars = value.toString();

    final String response = executeWebContextFilter();

    String contextModuleConfig = "requireCfg.config[\"pentaho/context\"] = {" +
            "\n  theme: \"" + this.activeTheme + "\"," +
            "\n  locale: \"" + sessionLocale + "\"," +
            "\n  user: {" +
            "\n    id: \"" + this.sessionName + "\"," +
            "\n    home: " + userHome +
            "\n  }," +
            "\n  reservedChars: \"" + reservedChars + "\"," +
            "\n  server: {" +
            "\n    url: " + serverUrl +
            "\n  }" +
            "\n};";

    assertTrue( response.contains( contextModuleConfig ) );
  }

  @Test
  public void testDoGetWithFullyQualifiedUrlDefaultLocalReferer() throws ServletException, IOException {
    when( this.mockRequest.getHeader( "referer" ) ).thenReturn( this.serverAddress + "/other/app" );
    final String response = executeWebContextFilter();

    assertTrue( this.responseSetsContextPathGlobal( response, this.contextRoot ) );
    assertTrue( this.requirejsManagerInitIsCalled( response, null ) );
  }

  private boolean responseSetsContextPathGlobal( String response, String contextRoot ) {
    return response.contains( getWebContextVarDefinition( "CONTEXT_PATH", contextRoot ) );
  }

  private boolean requirejsManagerInitIsCalled( String response, String useFullyQualifiedUrlParameter ) {
    final boolean containsScript = response.contains( "requirejs-manager/js/require-init.js?requirejs=false" );

    final boolean containsFullyQualifiedUrlParameter = response.contains(
      "&fullyQualifiedUrl=" + ( useFullyQualifiedUrlParameter != null ? useFullyQualifiedUrlParameter : "" )
    );

    return containsScript && containsFullyQualifiedUrlParameter == ( useFullyQualifiedUrlParameter != null );
  }

  private String getWebContextVarDefinition( String variable, String value ) {
    String escapedValue = "\"" + StringEscapeUtils.escapeJavaScript( value ) + "\"";

    return "\n/** @deprecated - use 'pentaho/context' module's variable instead */" +
            "\nvar " + variable + " = " + escapedValue + ";";
  }

  private String executeWebContextFilter() throws ServletException, IOException {
    this.pentahoWebContextFilter.doFilter( this.mockRequest, this.mockResponse, null );

    return this.mockResponseOutputStream.toString( "UTF-8" );
  }
}
