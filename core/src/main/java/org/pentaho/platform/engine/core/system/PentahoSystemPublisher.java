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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.core.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PentahoSystemPublisher {
  public static final String START_UP_TOPIC = "system_startup";
  public static final String SHUT_DOWN_TOPIC = "system_shutdown";
  private final Map<String, ArrayList<Consumer<?>>> topicsSubscribers = new HashMap<>();
  private static PentahoSystemPublisher instance = null;

  public static PentahoSystemPublisher getInstance() {
    if ( instance == null ) {
      instance = new PentahoSystemPublisher();
    }
    return instance;
  }

  public int topicCount() {
    return topicsSubscribers.size();
  }

  public <T> void publish( String topic, T value ) {
    ArrayList<Consumer<?>> subscribers = topicsSubscribers.get( topic );
    if ( subscribers == null ) {
      return;
    }

    for ( Consumer subscriberConsumer : subscribers ) {
      subscriberConsumer.accept( value );
    }
  }

  public synchronized <T> void subscribe( String topicName, Consumer<T> subscriberCallback ) {
    ArrayList<Consumer<?>> subscribers = topicsSubscribers.get( topicName );
    if ( subscribers == null ) {
      subscribers = new ArrayList<>();
      subscribers.add( subscriberCallback );
      topicsSubscribers.put( topicName, subscribers );
    } else {
      subscribers.add( subscriberCallback );
    }
  }
}
