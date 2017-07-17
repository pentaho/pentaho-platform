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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.AbstractApplicationEventMulticaster;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ApplicationEventDispatcher extends AbstractApplicationEventMulticaster {
  public final static String ASYNC_ANNOTATION_QUALIFIER = "dispatcher";

  private ApplicationEventMulticaster asyncMulticaster;
  private ApplicationEventMulticaster defaultMulticaster;

  private BeanFactory beanFactory;

  @Override
  public void addApplicationListener( final ApplicationListener listener ) {
    if ( isAsync( listener ) ) {
      asyncMulticaster.addApplicationListener( listener );
    } else {
      defaultMulticaster.addApplicationListener( listener );
    }
  }

  @Override
  public void removeApplicationListener( final ApplicationListener listener ) {
    asyncMulticaster.removeApplicationListener( listener );
    defaultMulticaster.removeApplicationListener( listener );
  }

  @Override
  public void removeApplicationListenerBean( final String listenerBeanName ) {
    asyncMulticaster.removeApplicationListenerBean( listenerBeanName );
    defaultMulticaster.removeApplicationListenerBean( listenerBeanName );
  }

  @Override
  public void removeAllListeners() {
    defaultMulticaster.removeAllListeners();
    asyncMulticaster.removeAllListeners();
  }

  public void multicastEvent( final ApplicationEvent event ) {
    defaultMulticaster.multicastEvent( event );
    asyncMulticaster.multicastEvent( event );
  }

  public void multicastEvent(final ApplicationEvent event, final ResolvableType type ) {
    defaultMulticaster.multicastEvent( event, type );
    asyncMulticaster.multicastEvent( event, type );
  }

  public void setAsyncMulticaster( final ApplicationEventMulticaster asyncMulticaster ) {
    this.asyncMulticaster = asyncMulticaster;
  }

  public void setDefaultMulticaster( final ApplicationEventMulticaster defaultMulticaster ) {
    this.defaultMulticaster = defaultMulticaster;
  }

  @Override
  public void setBeanFactory( final BeanFactory beanFactory ) {
    super.setBeanFactory( beanFactory );

    if ( defaultMulticaster instanceof AbstractApplicationEventMulticaster ) {
      ( (AbstractApplicationEventMulticaster) defaultMulticaster ).setBeanFactory( beanFactory );
    }

    if ( asyncMulticaster instanceof AbstractApplicationEventMulticaster ) {
      ( (AbstractApplicationEventMulticaster) asyncMulticaster ).setBeanFactory( beanFactory );
    }

    this.beanFactory = beanFactory;
  }

  @Override
  public void addApplicationListenerBean( final String listenerBeanName ) {
    final ApplicationListener listener = beanFactory.getBean( listenerBeanName, ApplicationListener.class );
    if ( isAsync( listener ) ) {
      asyncMulticaster.addApplicationListenerBean( listenerBeanName );
    } else {
      defaultMulticaster.addApplicationListenerBean( listenerBeanName );
    }
  }

  private boolean isAsync( final ApplicationListener listener ) {
    Async annotation = getAsyncAnnotationDirect( listener );
    if ( annotation == null ) {  // could not get directly, try assuming proxy
      annotation = getAsyncAnnotationThroughProxy( listener );
    }

    // http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/Async.html
    // Only consider async annotation having the supported qualifier.
    return ( annotation != null && annotation.value().equals( ASYNC_ANNOTATION_QUALIFIER ) );
  }

  private Async getAsyncAnnotationDirect(final ApplicationListener listener ) {
    return listener.getClass().getDeclaredAnnotation( Async.class );
  }

  private Async getAsyncAnnotationThroughProxy(final ApplicationListener listener ) {
    // The listener could actually be an proxy/wrapper implementing the ApplicationListener.
    // Such a listener (spring implementation details here) has a "method" field representing the
    // target method which the proxy delegates events to.
    //
    try {
      final Field method = listener.getClass().getDeclaredField("method" );
      method.setAccessible( true );
      final Method listeningMethod = (Method) method.get( listener );
      return listeningMethod.getDeclaredAnnotation( Async.class );

    } catch ( final NoSuchFieldException e ) {
    } catch ( final IllegalAccessException e ) {
    }

    return null;
  }
}
