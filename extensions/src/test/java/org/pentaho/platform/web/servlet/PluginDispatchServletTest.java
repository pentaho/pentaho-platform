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
 * Copyright (c) 2018-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.websocket.IWebsocketEndpointConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.context.WebApplicationContext;
import org.pentaho.platform.web.websocket.WebsocketEndpointConfig;
import org.springframework.beans.factory.ListableBeanFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
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
