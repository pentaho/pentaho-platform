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
