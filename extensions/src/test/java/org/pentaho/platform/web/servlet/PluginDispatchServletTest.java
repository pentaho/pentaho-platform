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


package org.pentaho.platform.web.servlet;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.websocket.IWebsocketEndpointConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.context.WebApplicationContext;
import org.pentaho.platform.web.websocket.WebsocketEndpointConfig;
import org.springframework.beans.factory.ListableBeanFactory;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpointConfig;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class PluginDispatchServletTest {

  PluginDispatchServlet pluginDispatchServlet;

  @Before
  public void setupServlet() {
    PentahoSystem.shutdown();
    PentahoSystem.init( new WebApplicationContext( System.getProperty( "java.io.tmpdir" ), "http://localhost:8080", "pentaho" ) );
    pluginDispatchServlet = spy( new PluginDispatchServlet() );
  }

  @Test
  public void testGetServerUrl() {
    String serverUrl = pluginDispatchServlet.getServerUrl( "/pentaho" );
    assertEquals( "http://localhost:8080/", serverUrl );
  }

  public void testGetDispatchKey( String pathInfo, String expected ) {
    final HttpServletRequest request = mock( HttpServletRequest.class );

    when( request.getPathInfo() ).thenReturn( pathInfo );

    assertEquals( expected, pluginDispatchServlet.getDispatchKey( request ) );
  }

  @Test
  public void testGetDispatchKey() {
    testGetDispatchKey( "/myPlugin/myServlet", "myPlugin/myServlet" );
  }

  @Test
  public void testGetDispatchKeySuffixForwardSlash() {
    testGetDispatchKey( "/myPlugin/myServlet/", "myPlugin/myServlet" );
  }

  @Test
  public void testGetDispatchKeyMatch() {
    testGetDispatchKey( "myPlugin/myServlet", "myPlugin/myServlet" );
  }

  @Test
  public void testGetDispatchEmpty() {
    testGetDispatchKey( "", null );
  }

  @Test
  public void testGetDispatchNull() {
    testGetDispatchKey( null, null );
  }

  @Test
  public void testConfigurePluginWebsockets() throws DeploymentException {
    String pluginId = "pluginId";
    String contextPath = "/pentaho";
    String websocketKey = "websocketEndpoint";
    ListableBeanFactory listableBeanFactory = mock( ListableBeanFactory.class );
    Map.Entry<String, ListableBeanFactory> pluginBeanFactoryEntry = new AbstractMap.SimpleEntry<String, ListableBeanFactory>( pluginId, listableBeanFactory );
    Map<String, IWebsocketEndpointConfig> wsBeans = new TreeMap<>( );
    List<String> subProtocolAccepted = Arrays.asList( new String[]{ "subProtocol1", "subProtocol2" } );
    Predicate<String> isOriginAllowedPredicate = s -> "http://localhost:8080".equals( s ) ? true : false;
    int maxMessageBytesLength = 5000;

    ServletConfig servletConfig = mock( ServletConfig.class );
    ServletContext servletContext = mock( ServletContext.class );
    when( pluginDispatchServlet.getServletConfig() ).thenReturn( servletConfig );
    when( servletConfig.getServletContext() ).thenReturn( servletContext );
    when( servletContext.getContextPath() ).thenReturn( contextPath );
    ServerContainer serverContainer = mock( ServerContainer.class );
    when( servletContext.getAttribute( ServerContainer.class.getName() ) ).thenReturn( serverContainer );

    IWebsocketEndpointConfig websocketEndpointConfig = new WebsocketEndpointConfig( "urlSufix",
      Object.class,
      subProtocolAccepted,
      isOriginAllowedPredicate,
      maxMessageBytesLength );

    wsBeans.put( websocketKey, websocketEndpointConfig);
    when( pluginDispatchServlet.getWebsocketEndpointConfigBeans( listableBeanFactory ) ).thenReturn( wsBeans );

    ServerEndpointConfig.Configurator endpointConfigurator = mock ( ServerEndpointConfig.Configurator.class );
    when( pluginDispatchServlet.getServerWebsocketEndpointConfigurator( contextPath, isOriginAllowedPredicate ) ).thenReturn( endpointConfigurator );

    pluginDispatchServlet.configurePluginWebsockets( pluginBeanFactoryEntry );

    verify( serverContainer, times( 1 ) ).addEndpoint( any( ServerEndpointConfig.class ) );
  }

  @Test
  public void testGetServerWebsocketEndpointConfiguratorPredicate() {
    String contextPath = "/pentaho";
    String serverUrl = "http://localhost:8080";
    Predicate<String> isOriginAllowedPredicate = s -> "http://localhost:8080".equals( s ) ? true : false;

    when( pluginDispatchServlet.getServerUrl( contextPath ) ).thenReturn( serverUrl );
    ServerEndpointConfig.Configurator endpointConfigurator = pluginDispatchServlet.getServerWebsocketEndpointConfigurator( contextPath, isOriginAllowedPredicate );

    assertTrue( endpointConfigurator.checkOrigin( "http://localhost:8080" ) );
    assertFalse( endpointConfigurator.checkOrigin( "http://localhost:8081" ) );
    assertFalse( endpointConfigurator.checkOrigin( "http://127.0.0.1:8080" ) );
  }

  @Test
  public void testGetServerWebsocketEndpointConfiguratorPredicateReturnFalse() {
    String contextPath = "/pentaho";
    String serverUrl = "http://127.0.0.1:8080";

    when( pluginDispatchServlet.getServerUrl( contextPath ) ).thenReturn( serverUrl );
    ServerEndpointConfig.Configurator endpointConfigurator = pluginDispatchServlet.getServerWebsocketEndpointConfigurator( contextPath, null );

    assertFalse( endpointConfigurator.checkOrigin( "http://localhost:8080" ) );
  }

  @Test
  public void testGetServerWebsocketEndpointConfiguratorLocalServer() {
    String contextPath = "/pentaho";
    String serverUrl = "http://localhost:8080";
    Predicate<String> isOriginAllowedPredicate = s -> false;

    when( pluginDispatchServlet.getServerUrl( contextPath ) ).thenReturn( serverUrl );
    ServerEndpointConfig.Configurator endpointConfigurator = pluginDispatchServlet.getServerWebsocketEndpointConfigurator( contextPath, isOriginAllowedPredicate );

    assertTrue( endpointConfigurator.checkOrigin( "http://localhost:8080" ) );
  }
}
