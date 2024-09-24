/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.security.event;

import org.junit.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * @author Andrey Khayrutdinov
 */
public class OrderedApplicationEventMulticasterTest {

  @Test
  public void multicastEvent_DirectOrderOfListeners() throws Exception {
    List<ApplicationListener> collector = collector();
    List<ApplicationListener<?>> listeners = new ArrayList<>();
    for ( int i = 0; i < 5; i++ ) {
      listeners.add( new OrderedStubListener( i, collector ) );
    }

    runTest( listeners, collector );
  }

  @Test
  public void multicastEvent_ReverseOrderOfListeners() throws Exception {
    List<ApplicationListener> collector = collector();
    List<ApplicationListener<?>> listeners = new ArrayList<>();
    for ( int i = 0; i < 5; i++ ) {
      listeners.add( new OrderedStubListener( -i, collector ) );
    }

    runTest( listeners, collector );
  }

  @Test
  public void multicastEvent_RandomOrderOfListeners() throws Exception {
    List<ApplicationListener> collector = collector();
    List<ApplicationListener<?>> listeners = new ArrayList<>();
    for ( int i = 0; i < 5; i++ ) {
      listeners.add( new OrderedStubListener( i, collector ) );
    }
    Collections.shuffle( listeners );

    runTest( listeners, collector );
  }


  @Test
  public void multicastEvent_RandomOrderOfDifferentListeners() throws Exception {
    List<ApplicationListener> collector = collector();
    List<ApplicationListener<?>> listeners = new ArrayList<>();
    for ( int i = 0; i < 5; i++ ) {
      listeners.add( new OrderedStubListener( i, collector ) );
    }
    for ( int i = 0; i < 5; i++ ) {
      listeners.add( new StubListener( collector ) );
    }
    Collections.shuffle( listeners );

    runTest( listeners, collector );
  }


  private void runTest( Collection<ApplicationListener<?>> listeners, List<ApplicationListener> collector ) {
    TestMulticaster multicaster = new TestMulticaster();
    multicaster.setApplicationListeners( listeners );

    multicaster.multicastEvent( mock( ApplicationEvent.class ) );
    assertCalledInOrder( collector );
  }

  private List<ApplicationListener> collector() {
    return Collections.synchronizedList( new ArrayList<ApplicationListener>() );
  }

  private void assertCalledInOrder( List<ApplicationListener> list ) {
    if ( list.size() > 1 ) {
      ApplicationListener previous = list.get( 0 );
      for ( int i = 1; i < list.size(); i++ ) {
        ApplicationListener current = list.get( i );
        if ( previous instanceof Ordered ) {
          if ( current instanceof Ordered ) {
            int prevOrder = ( (Ordered) previous ).getOrder();
            int curOrder = ( (Ordered) current ).getOrder();
            assertTrue( "Previous=" + prevOrder + "\tCurrent=" + curOrder, prevOrder <= curOrder );
          }
        } else {
          if ( current instanceof Ordered ) {
            fail( "Ordered listeners should always precede non-ordered" );
          }
        }
        previous = current;
      }
    }
  }

  private static class TestMulticaster extends OrderedApplicationEventMulticaster {
    private Collection<ApplicationListener<?>> applicationListeners;

    public void setApplicationListeners( Collection<ApplicationListener<?>> applicationListeners ) {
      this.applicationListeners = applicationListeners;
    }

    @Override
    public Collection<ApplicationListener<?>> getApplicationListeners() {
      return applicationListeners;
    }
  }

  private static class OrderedStubListener implements ApplicationListener, Ordered {
    private final int order;
    private final List<ApplicationListener> invoked;

    public OrderedStubListener( int order, List<ApplicationListener> invoked ) {
      this.order = order;
      this.invoked = invoked;
    }

    @Override
    public void onApplicationEvent( ApplicationEvent applicationEvent ) {
      invoked.add( this );
    }

    @Override
    public int getOrder() {
      return order;
    }
  }

  private static class StubListener implements ApplicationListener {
    private final List<ApplicationListener> invoked;

    public StubListener( List<ApplicationListener> invoked ) {
      this.invoked = invoked;
    }

    @Override
    public void onApplicationEvent( ApplicationEvent applicationEvent ) {
      invoked.add( this );
    }
  }
}
