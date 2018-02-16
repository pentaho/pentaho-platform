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

package org.pentaho.platform.engine.core.system.objfac.spring;

import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

/**
 * Obtains a reference to the requested bean from the PentahoSystem
 * <p/>
 * {@code} <pen:bean class="com.foo.Clazz"/> {@code}
 * <p/>
 * User: nbaker Date: 3/2/13
 */
public class BeanBuilder implements FactoryBean {

  private String type;
  private Map<String, String> attributes;
  private static ThreadLocal<BeanBuilder> resolvingBean = new ThreadLocal<BeanBuilder>();
  private static Logger log = LoggerFactory.getLogger( BeanBuilder.class );
  private Integer dampeningTimeout = null;

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.beans.factory.FactoryBean#getObject()
   */
  public Object getObject() {

    try {
      if ( resolvingBean.get() == this ) {
        log.warn( "Circular Reference detected in bean creation ( "
            + type
            + " : "
            + attributes
            + "). Very likely a published "
            + "pentaho bean is resolving itself. Ensure that the published attributes do not match that of the Pentaho "
            + "bean query. The system will attempt to find the next highest available bean, but at a performance "
            + "penilty" );
        // attempt to find a lower priority bean for them
        Class cls = getClass().getClassLoader().loadClass( type.trim() );
        resolvingBean.set( this );
        List<IPentahoObjectReference<?>> objectReferences =
            PentahoSystem.getObjectFactory().getObjectReferences( cls, PentahoSessionHolder.getSession(), attributes );
        resolvingBean.set( null );
        if ( objectReferences.size() > 1 ) {
          // we have more than one, return the second highest
          return objectReferences.get( 1 ).getObject();
        } else {
          // there's only one bean, this is a fatal situation
          throw new IllegalStateException( "Fatal Circular reference in Pentaho Bean ( " + type + " : " + attributes
              + ")" );
        }

      } else {
        final Class cls = getClass().getClassLoader().loadClass( type.trim() );
        resolvingBean.set( this );
        Object val = null;
        IPentahoObjectReference objectReference =
            PentahoSystem.getObjectFactory().getObjectReference( cls, PentahoSessionHolder.getSession(),
                attributes );
        if ( objectReference != null ) {
          val = objectReference.getObject();
        }
        resolvingBean.set( null );
        if ( val == null ) {
          log.debug( "No object was found to satisfy pen:bean request [" + type + " : " + attributes + "]" );

          final int f_dampeningTimeout = getDampeningTimeout();
          // send back a proxy
          if ( cls.isInterface() && dampeningTimeout > -1 ) {
            log.debug( "Request bean which wasn't found is interface-based. Instantiating a Proxy dampener" );

            val = Proxy.newProxyInstance( cls.getClassLoader(), new Class[] { cls }, new InvocationHandler() {
              String lock = "lock_" + getClass().getName(); // class name to prevent locking somewhere else by the same string
              Object target;
              Thread watcher;
              boolean dead = false;

              private void startWatcherThread( final int millis ) {

                watcher = new Thread( new Runnable() {
                  @Override public void run() {
                    int countdown = millis;
                    while ( countdown > 0 ) {
                      IPentahoObjectReference objectReference;
                      try {
                        objectReference =
                            PentahoSystem.getObjectFactory().getObjectReference( cls, PentahoSessionHolder.getSession(),
                                attributes );
                        if ( objectReference != null ) {
                          target = objectReference.getObject();
                        }
                      } catch ( ObjectFactoryException e ) {
                        log.debug( "Error fetching from PentahoSystem", e );
                      }

                      if ( target != null ) {
                        synchronized ( lock ) {
                          lock.notifyAll();
                        }
                        return;
                      }
                      countdown -= 100;
                    }
                    dead = true;
                  }
                } );
                watcher.start();
              }

              @Override public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
                if ( target == null && f_dampeningTimeout > 0 ) {
                  synchronized ( lock ) {
                    if ( watcher == null && !dead ) {
                      startWatcherThread( f_dampeningTimeout );
                      lock.wait( f_dampeningTimeout );
                    }
                  }
                }
                if ( target == null ) {

                  // Last chance. If the attributes are empty, try a plain PentahoSystem.get() which will find bean's
                  // with the ID equal to the simple name of the class
                  if ( attributes.isEmpty() || ( attributes.size() == 1 && attributes.containsKey( "id" ) ) ) {
                    target = getFallbackBySimpleName( cls, attributes );
                  }
                  if ( target == null ) {
                    throw new IllegalStateException( "Target of Bean was never resolved: " + cls.getName() );
                  }
                }
                return method.invoke( target, args );
              }
            } );
          } else if ( !cls.isInterface() && ( attributes.isEmpty() || ( attributes.size() == 1 && attributes.containsKey( "id" ) ) ) ) {
            val = getFallbackBySimpleName( cls, attributes );
          }
        }
        return val;
      }
    } catch ( ClassNotFoundException e ) {
      throw new RuntimeException( e );
    } catch ( ObjectFactoryException e ) {
      throw new RuntimeException( e );
    }


  }

  private int getDampeningTimeout() {
    if ( dampeningTimeout == null ) {
      dampeningTimeout = 0;
      ISystemConfig iSystemConfig = PentahoSystem.get( ISystemConfig.class );
      if ( iSystemConfig != null ) {
        String property = iSystemConfig.getProperty( "system.dampening-timeout" );
        if ( property != null ) {
          dampeningTimeout = Integer.valueOf( property );
        }
      }
    }
    return dampeningTimeout;
  }

  private Object getFallbackBySimpleName( Class clazz, Map<String, String> attributes ) {
    Object lastChanceObject;
    if ( attributes != null && attributes.containsKey( "id" ) ) {
      lastChanceObject = PentahoSystem.get( clazz, attributes.get( "id" ), PentahoSessionHolder.getSession() );
    } else {
      lastChanceObject = PentahoSystem.get( clazz, PentahoSessionHolder.getSession() );
    }
    if ( lastChanceObject != null ) {
      log.warn( "Target of <pen:bean class=\"" + clazz.getName()
          + "\"> was found using deprecated bean ID == class.getSimpleName() "
          + "fallback. The target bean with the id \"" + clazz.getSimpleName()
          + "\" should be published directly with <pen:publish>" );
    }
    return lastChanceObject;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.beans.factory.FactoryBean#getObjectType()
   */
  public Class<?> getObjectType() {
    return Object.class;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.beans.factory.FactoryBean#isSingleton()
   */
  public boolean isSingleton() {
    return true;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes( Map<String, String> attributes ) {
    this.attributes = attributes;
  }
}
