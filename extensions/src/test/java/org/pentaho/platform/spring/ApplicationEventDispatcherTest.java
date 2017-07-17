/*!
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
 * Copyright (c) 2017 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.spring;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

class AppEvent extends ApplicationEvent {
  final private String id;

  AppEvent( final String id ) {
    super( "source" );
    this.id = id;
  }

  String getId() {
    return id;
  }
}

class AsyncAppEvent {
  final private String id;

  AsyncAppEvent( final String id ) {
    this.id = id;
  }

  String getId() {
    return id;
  }
}

@Component
class SyncAppEventListener implements ApplicationListener {
  final ArrayList<AppEvent> receivedEvents = new ArrayList<>(  );
  final ArrayList<Long> threadIds = new ArrayList<> ( );

  public AppEvent getLastReceivedEvent() {
    return receivedEvents.isEmpty() ? null : receivedEvents.get( receivedEvents.size() - 1 );
  }

  public ArrayList<AppEvent> getReceivedEvents() {
    return receivedEvents;
  }

  public int numThreadIds() {
    return threadIds.size();
  }

  public long getLastThreadId() {
    return threadIds.get( threadIds.size() -1 );
  }

  @Override
  public void onApplicationEvent( final ApplicationEvent event ) {
    receivedEvents.add( (AppEvent) event );
    if ( ! threadIds.contains( Thread.currentThread().getId() ) ) {
      threadIds.add( Thread.currentThread().getId() );
    }
  }
}

@Component
class AsyncAppEventListener {
  final ArrayList<AsyncAppEvent> receivedEvents = new ArrayList<>( );
  final ArrayList<Long> threadIds = new ArrayList<> ( );

  @EventListener
  @Async( ApplicationEventDispatcher.ASYNC_ANNOTATION_QUALIFIER )
  public void handleEvent( final AsyncAppEvent event ) {
    receivedEvents.add( event );
    if ( ! threadIds.contains( Thread.currentThread().getId() ) ) {
      threadIds.add( Thread.currentThread().getId() );
    }
  }

  public AsyncAppEvent getLastReceivedEvent() {
    return receivedEvents.isEmpty() ? null : receivedEvents.get( receivedEvents.size() - 1 );
  }

  public ArrayList<AsyncAppEvent> getReceivedEvents() {
    return receivedEvents;
  }

  public int numThreadIds() {
    return threadIds.size();
  }

  public long getLastThreadId() {
    return threadIds.get( threadIds.size() -1 );
  }
}

@Component
class PseudoAsyncAppEventListener {
  final ArrayList<AsyncAppEvent> receivedEvents = new ArrayList<>( );
  final ArrayList<Long> threadIds = new ArrayList<> ( );

  @EventListener
  @Async
  public void handleEvent( final AsyncAppEvent event ) {
    receivedEvents.add( event );
    if ( ! threadIds.contains( Thread.currentThread().getId() ) ) {
      threadIds.add( Thread.currentThread().getId() );
    }
  }

  public AsyncAppEvent getLastReceivedEvent() {
    return receivedEvents.isEmpty() ? null : receivedEvents.get( receivedEvents.size() - 1 );
  }

  public ArrayList<AsyncAppEvent> getReceivedEvents() {
    return receivedEvents;
  }

  public int numThreadIds() {
    return threadIds.size();
  }

  public long getLastThreadId() {
    return threadIds.get( threadIds.size() -1 );
  }
}

@Configuration
class AppConfig {
  @Bean( name = "applicationEventMulticaster" )
  public ApplicationEventMulticaster getApplicationEventMulticaster() {
    final ApplicationEventDispatcher dispatcher = new ApplicationEventDispatcher();
    final SimpleApplicationEventMulticaster syncMulticaster = new SimpleApplicationEventMulticaster();
    final SimpleApplicationEventMulticaster asyncMulticaster = new SimpleApplicationEventMulticaster();
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setMaxPoolSize( 5 );
    executor.setCorePoolSize( 5 );
    executor.setKeepAliveSeconds( 60 );
    executor.setQueueCapacity( 10000 );
    executor.initialize();
    asyncMulticaster.setTaskExecutor( executor );

    dispatcher.setAsyncMulticaster( asyncMulticaster );
    dispatcher.setDefaultMulticaster( syncMulticaster );

    return dispatcher;
  }

  @Autowired
  private ApplicationEventPublisher publisher;

  void publish( final AppEvent event ) {
    publisher.publishEvent( event );
  }

  void publish( final AsyncAppEvent event ) {
    publisher.publishEvent( event );
  }

}

public class ApplicationEventDispatcherTest {
  private AnnotationConfigApplicationContext ctx;
  private AppConfig appConfig;
  private AsyncAppEventListener asyncListener;
  private PseudoAsyncAppEventListener pseudoAsyncListener;
  private SyncAppEventListener syncListener;
  private ApplicationEventMulticaster dispatcher;

  @Before
  public void setup() throws Exception {
    ctx = new AnnotationConfigApplicationContext( "org.pentaho.platform.spring" );
    ctx.start();
    appConfig = ctx.getBean( AppConfig.class );
    asyncListener = ctx.getBean( AsyncAppEventListener.class );
    syncListener = ctx.getBean( SyncAppEventListener.class );
    pseudoAsyncListener = ctx.getBean( PseudoAsyncAppEventListener.class );
    dispatcher = (ApplicationEventMulticaster) ctx.getBean( "applicationEventMulticaster" );
  }

  /**
   * Tests listeners are added and removed properly in the dispatcher.
   * Tests the dispatcher works as expected in terms of publishing async.
   * Tests "other" listeners are not affected by the "async behaviour" of the dispatcher.
   *
   * @throws Exception
   */
  @Test
  public void test() throws Exception {
    // verify both (sync and async) listeners are "on" and working properly
    appConfig.publish( new AppEvent( "1" ) );
    Assert.assertEquals( "1", syncListener.getLastReceivedEvent().getId() );
    Assert.assertEquals( 1, syncListener.numThreadIds() );
    Assert.assertEquals( Thread.currentThread().getId(), syncListener.getLastThreadId() );

    appConfig.publish( new AsyncAppEvent( "2" ) );
    Thread.sleep( 3000 ); // drain time for async
    Assert.assertEquals( "2", asyncListener.getLastReceivedEvent().getId() );
    Assert.assertEquals( 1, asyncListener.numThreadIds() );
    Assert.assertNotEquals( Thread.currentThread().getId(), asyncListener.getLastThreadId() );
    Assert.assertEquals( "2", pseudoAsyncListener.getLastReceivedEvent().getId() );
    Assert.assertEquals( 1, pseudoAsyncListener.numThreadIds() );
    Assert.assertEquals( Thread.currentThread().getId(), pseudoAsyncListener.getLastThreadId() );

    // verify async listeners receive multiple events on separate threads
    // engage all 5 threads
    appConfig.publish( new AsyncAppEvent( "3" ) );
    appConfig.publish( new AsyncAppEvent( "4" ) );
    appConfig.publish( new AsyncAppEvent( "5" ) );
    appConfig.publish( new AsyncAppEvent( "6" ) );
    appConfig.publish( new AsyncAppEvent( "7" ) );

    Thread.sleep( 3000 ); // allow events to drain

    Assert.assertEquals( 1, syncListener.getReceivedEvents().size() ); // not picked up by sync listener
    Assert.assertEquals( 6, asyncListener.getReceivedEvents().size() ); // 5 plus the initial one
    Assert.assertEquals( 5, asyncListener.numThreadIds() );
    // pseudo async (listeners with Async annotation but without the required qualifier
    // are subject to the default dispatcher. So in our case expose "sync" mentality
    //
    Assert.assertEquals( 6, pseudoAsyncListener.getReceivedEvents().size() ); // 5 plus the initial one
    Assert.assertEquals( "7", pseudoAsyncListener.getLastReceivedEvent().getId() );
    Assert.assertEquals( 1, pseudoAsyncListener.numThreadIds() );
    Assert.assertEquals( Thread.currentThread().getId(), pseudoAsyncListener.getLastThreadId() );

    // verify listener removal works properly
    //
    dispatcher.removeApplicationListener( syncListener );   // remove by reference
    dispatcher.removeApplicationListenerBean( "syncAppEventListener" ); // remove by name
    appConfig.publish( new AppEvent( "8" ) ); // no further events should be received by sync listener
    Assert.assertEquals( 1, syncListener.getReceivedEvents().size() );
    Assert.assertEquals( "1", syncListener.getLastReceivedEvent().getId() );

    // but should still be received by the other listeners
    appConfig.publish( new AsyncAppEvent( "9" ) );
    appConfig.publish( new AsyncAppEvent( "10" ) );
    appConfig.publish( new AsyncAppEvent( "11" ) );
    appConfig.publish( new AsyncAppEvent( "12" ) );
    appConfig.publish( new AsyncAppEvent( "13" ) );

    Thread.sleep( 3000 ); // allow events to drain

    Assert.assertEquals( 11, asyncListener.getReceivedEvents().size() ); // 5+5 plus the initial one
    Assert.assertEquals( 11, pseudoAsyncListener.getReceivedEvents().size() ); // 5+5 plus the initial one

    dispatcher.removeAllListeners();
    appConfig.publish( new AsyncAppEvent( "14" ) );

    Thread.sleep( 3000 ); // allow events to drain

    // no further events should have been received now
    Assert.assertEquals( "13", pseudoAsyncListener.getLastReceivedEvent().getId() );
    Assert.assertEquals( 11, pseudoAsyncListener.getReceivedEvents().size() );
    Assert.assertEquals( 11, asyncListener.getReceivedEvents().size() );

  }

  @After
  public void teardown() throws Exception {
    ctx.stop();
  }
}
