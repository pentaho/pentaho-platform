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
import java.util.function.Consumer;

public interface IWebsocketEndpoint {

  /**
   * The websocket endpoint implementation should call this when the websocket is opened.
   * This gives an opportunity to allocate resources needed for receiving messages, or to reply back to
   * the message consumer received as parameter.
   *
   * The use of the outboundMessageConsumer parameter is optional, so this interface
   * implementations should not rely on it.
   *
   * @param outboundMessageConsumer Parameter which will send a string message over the transport channel.
   */
  void onOpen( Consumer<String> outboundMessageConsumer );

  /**
   * This method should be called when the websocket endpoint implementation
   * receives a new String message.
   *
   * @param message The message received.
   */
  void onMessage( String message, Consumer<String> outboundMessageConsumer );

  /**
   * The websocket endpoint implementation should call this when the websocket is closed.
   * This gives an opportunity to release resources allocated during the onOpen or onMessage.
   */
  void onClose();

  /**
   * Gets a list of subprotocols that this endpoint can handle.
   * @return a {@link List} of subprotocols.
   */
  List<String> getSubProtocols();

}
