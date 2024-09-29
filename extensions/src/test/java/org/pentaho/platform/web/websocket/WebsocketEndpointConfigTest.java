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
 * Copyright (c) 2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.websocket;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class WebsocketEndpointConfigTest {

  private WebsocketEndpointConfig websocketEndpointConfig;

  private String urlSufix;
  private Object websocketEndpoint;
  private Class endpointImpl;
  private List<String> subProtocolAccepted;
  private Predicate<String> isOriginAllowedPredicate;
  private int maxMessageBytesLength;

  @Before
  public void setupEndpointConfig() {
    urlSufix = "urlSufixValue";
    websocketEndpoint = new Object();
    endpointImpl = websocketEndpoint.getClass();
    subProtocolAccepted = Arrays.asList( "protocol_1", "protocol_2" );
    isOriginAllowedPredicate = new Predicate<String>() {
      @Override public boolean test( String s ) {
        return "http://localhost:8080".equals( s );
      }
    };
    maxMessageBytesLength = 8196;

    websocketEndpointConfig = new WebsocketEndpointConfig( urlSufix,
      endpointImpl,
      subProtocolAccepted,
      isOriginAllowedPredicate,
      maxMessageBytesLength );
  }

  @Test
  public void testGetUrlSufix() {
    assertEquals( "urlSufixValue", websocketEndpointConfig.getUrlSufix());
  }

  @Test
  public void testGetEndpointImpl() {
    assertEquals( endpointImpl, websocketEndpointConfig.getEndpointImpl());
  }

  @Test
  public void testGetSubProtocolAccepted() {
    assertEquals( subProtocolAccepted, websocketEndpointConfig.getSubProtocolAccepted());
  }

  @Test
  public void testGetIsOriginAllowedPredicate() {
    assertEquals( isOriginAllowedPredicate, websocketEndpointConfig.getIsOriginAllowedPredicate());
  }

  @Test
  public void testGetMaxMessageBytesLength() {
    assertEquals( 8196, websocketEndpointConfig.getMaxMessageBytesLength());
  }

  @Test
  public void testGetServletContextPathPropertyName() {
    assertEquals( websocketEndpointConfig.getClass().getName() + ":servletContextPath", websocketEndpointConfig.getServletContextPathPropertyName());
  }

  @Test
  public void testGetMaxMessagePropertyName() {
    assertEquals( websocketEndpointConfig.getClass().getName() + ":maximumMessageLength", websocketEndpointConfig.getMaxMessagePropertyName());
  }

  @Test
  public void testGetInstance() {
    assertNotNull( WebsocketEndpointConfig.getInstanceToReadProperties() );
    assertEquals( WebsocketEndpointConfig.getInstanceToReadProperties(), WebsocketEndpointConfig.getInstanceToReadProperties() );
    assertNotNull( WebsocketEndpointConfig.getInstanceToReadProperties().getServletContextPathPropertyName() );
    assertNotNull( WebsocketEndpointConfig.getInstanceToReadProperties().getMaxMessagePropertyName() );
    assertNull( WebsocketEndpointConfig.getInstanceToReadProperties().getEndpointImpl() );
    assertNull( WebsocketEndpointConfig.getInstanceToReadProperties().getIsOriginAllowedPredicate() );
    assertNull( WebsocketEndpointConfig.getInstanceToReadProperties().getSubProtocolAccepted() );
    assertNull( WebsocketEndpointConfig.getInstanceToReadProperties().getUrlSufix() );
    assertEquals( 0, WebsocketEndpointConfig.getInstanceToReadProperties().getMaxMessageBytesLength() );

  }
}
