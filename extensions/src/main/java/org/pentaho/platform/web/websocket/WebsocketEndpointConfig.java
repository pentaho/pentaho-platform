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

import org.pentaho.platform.api.websocket.IWebsocketEndpointConfig;

import java.util.List;
import java.util.function.Predicate;

/**
 * This class should be used to configure a websocket endpoint in platform plugins.
 * Declare a bean in the plugin.spring.xml on your platform plugin.
 *
 * <pre>
 * <bean id="corsUtil" class="<local plugin CORS util class>" factory-method="getInstance" />
 * <bean id="cda.plataform.websocket.endpoint.list" class="org.pentaho.platform.web.websocket.EndpointConfig">
 *   <constructor-arg value="query"/>
 *   <constructor-arg type="java.lang.Class" value="<IWebsocketEndpoint implementation class>"/>
 *   <constructor-arg>
 *     <list>
 *       <value>sub_protocol_value</value>
 *     </list>
 *   </constructor-arg>
 *   <constructor-arg value="#{corsUtil.isCorsRequestOriginAllowedPredicate()}" />
 *   <constructor-arg value="8192" />
 * </bean>
 * </pre>
 *
 * The CORS utility class usage is used as an example on how to pass a predicate as a parameter (use
 * any other mean that you found useful). The predicate can implement just the following code:
 * {@code * return domain -> this.getDomainWhitelist().contains( domain ); }
 *
 * For the websocket endpoint implementation you should go with an implementation of the
 * {@link javax.websocket.Endpoint} class, in order to have the endpoint registered in the platform.
 *
 *
 * The websocket is created in the {@link org.pentaho.platform.web.servlet.PluginDispatchServlet} initialization.
 * The endpoint will be ws://<server:port>/<plugin code>/websocket/<config map key>
 *
 */
public class WebsocketEndpointConfig implements IWebsocketEndpointConfig {
  private String urlSufix;
  private Class<?> endpointImpl;
  private List<String> subProtocolAccepted;
  private Predicate<String> isOriginAllowedPredicate;
  private int maxMessageBytesLength = 8192; //8192 is Tomcat default

  private static WebsocketEndpointConfig instance;
  private String SERVLET_CONTEXT_PATH_PROPERTY = this.getClass().getName() + ":servletContextPath";
  private String MAX_MESSAGE_LENGTH = this.getClass().getName() + ":maximumMessageLength";

  public WebsocketEndpointConfig( String urlSufix,
                                  Class<?> endpointImpl,
                                  List<String> subProtocolAccepted,
                                  Predicate<String> isOriginAllowedPredicate,
                                  int maxMessageBytesLength ) {
    this.urlSufix = urlSufix;
    this.endpointImpl = endpointImpl;
    this.subProtocolAccepted = subProtocolAccepted;
    this.isOriginAllowedPredicate = isOriginAllowedPredicate;
    this.maxMessageBytesLength = maxMessageBytesLength;
  }

  /**
   * Get the URL sufix where the websocket will be available.
   * @return A String with the URL sufix.
   */
  public String getUrlSufix() {
    return urlSufix;
  }

  /**
   * The class that implements this websocket endpoint.
   * @return A {@link Class} instance with a websocket implementation.
   */
  public Class<?> getEndpointImpl() {
    return endpointImpl;
  }

  /**
   * Gets the list of sub protocols that this web socket will accept. Empty if it accepts all sub protocols.
   * @return A list of {@link String} with the subprotocols.
   */
  public List<String> getSubProtocolAccepted() {
    return subProtocolAccepted;
  }

  /**
   * Gets the predicate which evaluates if a origin is allowed for the websocket.
   *
   * @return a predicate that accepts a String and checks if the origin received as parameter is accepted.
   */
  public Predicate<String> getIsOriginAllowedPredicate() {
    return isOriginAllowedPredicate;
  }

  /**
   * Gets the maximum message length in bytes.
   * @return the maximum message length in bytes.
   */
  public int getMaxMessageBytesLength() {
    return maxMessageBytesLength;
  }

  /**
   * Get the servlet context path property.
   * @return the string with the property name.
   */
  @Override
  public String getServletContextPathPropertyName() {
    return this.SERVLET_CONTEXT_PATH_PROPERTY;
  }

  /**
   * Get the maximum message property.
   * @return the string with the property name.
   */
  @Override
  public String getMaxMessagePropertyName() {
    return this.MAX_MESSAGE_LENGTH;
  }

  /**
   * This should be used only to get the property names values, since it gives a raw empty instance.
   * @return
   */
  public static WebsocketEndpointConfig getInstanceToReadProperties() {
    if ( WebsocketEndpointConfig.instance != null ) {
      return WebsocketEndpointConfig.instance;
    } else {
      WebsocketEndpointConfig.instance = new WebsocketEndpointConfig( null, null, null, null, 0 );
      return WebsocketEndpointConfig.instance;
    }
  }

}
