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
