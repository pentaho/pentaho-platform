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


package org.pentaho.platform.api.websocket;

import java.util.List;
import java.util.function.Predicate;

public interface IWebsocketEndpointConfig {

  /**
   * Get the URL sufix where the websocket will be available.
   * @return A String with the URL sufix.
   */
  String getUrlSufix();

  /**
   * The class that implements this websocket endpoint.
   * A new instance of this class is going to be created for each new websocket connection.
   * @return A {@link Class} instance with a websocket implementation.
   */
  Class<?> getEndpointImpl();

  /**
   * Gets the list of sub protocols that this web socket will accept. Empty if it accepts all sub protocols.
   * @return A list of {@link String} with the subprotocols.
   */
  List<String> getSubProtocolAccepted();

  /**
   * Gets the predicate which evaluates if a origin is allowed for the websocket.
   *
   * @return a predicate that accepts a String and checks if the origin received as parameter is accepted.
   */
  Predicate<String> getIsOriginAllowedPredicate();

  /**
   * Gets the maximum message length in bytes.
   * @return the maximum message length in bytes.
   */
  int getMaxMessageBytesLength();

  /**
   * Get the servlet context path property.
   * @return the string with the property name.
   */
  String getServletContextPathPropertyName();

  /**
   * Get the maximum message property.
   * @return the string with the property name.
   */
  String getMaxMessagePropertyName();

}
