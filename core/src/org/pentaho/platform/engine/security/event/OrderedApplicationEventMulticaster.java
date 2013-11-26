/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.security.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * {@link ApplicationEventMulticaster} that will respect values returned by {@link ApplicationListener}s that are
 * also {@link Ordered}. If there is a mixture of ordered and non-ordered {@code ApplicationListener}s, all ordered
 * {@code ApplicationListener}s will precede all non-ordered {@code ApplicationListener}s. This class must be
 * registered as a bean with the name {@code applicationEventMulticaster}.
 * 
 * <p>
 * See <a href="https://jira.springsource.org/browse/SPR-5240">SPR-5240</a>.
 * </p>
 * 
 * @see org.springframework.context.support.AbstractApplicationContext#APPLICATION_EVENT_MULTICASTER_BEAN_NAME
 * @author mlowery
 */
public class OrderedApplicationEventMulticaster extends SimpleApplicationEventMulticaster {

  @SuppressWarnings( "unchecked" )
  @Override
  public void multicastEvent( final ApplicationEvent event ) {
    List<ApplicationListener> listeners = new ArrayList<ApplicationListener>( getApplicationListeners() );

    // sort listeners
    Collections.sort( listeners, new Comparator<ApplicationListener>() {
      public int compare( final ApplicationListener o1, final ApplicationListener o2 ) {
        if ( o1 instanceof Ordered && o2 instanceof Ordered ) {
          return new Integer( ( (Ordered) o1 ).getOrder() ).compareTo( new Integer( ( (Ordered) o2 ).getOrder() ) );
        } else if ( o1 instanceof Ordered ) {
          return -1;
        } else if ( o2 instanceof Ordered ) {
          return 1;
        } else {
          return 0;
        }
      }
    } );

    // iterate over sorted listeners
    for ( final ApplicationListener listener : listeners ) {
      getTaskExecutor().execute( new Runnable() {
        public void run() {
          listener.onApplicationEvent( event );
        }
      } );
    }
  }

}
