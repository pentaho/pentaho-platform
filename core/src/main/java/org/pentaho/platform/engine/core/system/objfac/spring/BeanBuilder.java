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

  private static final String ID_ATTRIBUTE = "id";
  private String type;
  private Map<String, String> attributes;
  private static final ThreadLocal<BeanBuilder> resolvingBean = new ThreadLocal<>();
  private static final Logger log = LoggerFactory.getLogger( BeanBuilder.class );
  private Integer dampeningTimeout = null;

  /*
   * (non-Javadoc)
   *
   * @see org.springframework.beans.factory.FactoryBean#getObject()
   */
  public Object getObject() {

    try {
      if ( resolvingBean.get() == this ) {
        log.warn(
          "Circular Reference detected in bean creation ( {} : {}). "
            + "Very likely a published pentaho bean is resolving itself. "
            + "Ensure that the published attributes do not match that of the Pentaho bean query. "
            + "The system will attempt to find the next highest available bean, but at a performance penalty",
          type, attributes );

        // attempt to find a lower priority bean for them
        Class<?> cls = getClass().getClassLoader().loadClass( type.trim() );

        resolvingBean.set( this );

        List<? extends IPentahoObjectReference<?>> objectReferences =
            PentahoSystem.getObjectFactory().getObjectReferences( cls, PentahoSessionHolder.getSession(), attributes );

        resolvingBean.remove();

        if ( objectReferences.size() > 1 ) {
          // we have more than one, return the second highest
          return objectReferences.get( 1 ).getObject();
        }

        // there's only one bean, this is a fatal situation
        throw new IllegalStateException(
          "Fatal Circular reference in Pentaho Bean ( " + type + " : " + attributes + ")" );
      }

      final Class<?> cls = getClass().getClassLoader().loadClass( type.trim() );

      resolvingBean.set( this );

      Object val = null;
      IPentahoObjectReference<?> objectReference =
        PentahoSystem.getObjectFactory().getObjectReference( cls, PentahoSessionHolder.getSession(), attributes );
      if ( objectReference != null ) {
        val = objectReference.getObject();
      }

      resolvingBean.remove();

      if ( val == null ) {
        log.debug( "No object was found to satisfy pen:bean request [{} : {}]", type, attributes );

        final int f_dampeningTimeout = getDampeningTimeout();
        // send back a proxy
        if ( cls.isInterface() && dampeningTimeout > -1 ) {
          log.debug( "Request bean which wasn't found is interface-based. Instantiating a Proxy dampener" );

          val = Proxy.newProxyInstance( cls.getClassLoader(), new Class[] { cls }, new InvocationHandler() {
            // class name to prevent locking somewhere else by the same string
            final String lock = "lock_" + getClass().getName();
            Object target;
            Thread watcher;
            boolean dead = false;

            private void startWatcherThread( final int millis ) {

              watcher = new Thread( () -> {
                int countdown = millis;
                while ( countdown > 0 ) {
                  IPentahoObjectReference<?> objectReference1;
                  try {
                    objectReference1 = PentahoSystem.getObjectFactory()
                      .getObjectReference( cls, PentahoSessionHolder.getSession(), attributes );
                    if ( objectReference1 != null ) {
                      target = objectReference1.getObject();
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
              } );
              watcher.start();
            }

            @Override
            public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
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
                if ( hasNoAttributesOtherThanId() ) {
                  target = getFallbackBySimpleName( cls );
                }

                if ( target == null ) {
                  throw new IllegalStateException( "Target of Bean was never resolved: " + cls.getName() );
                }
              }

              return method.invoke( target, args );
            }
          } );
        } else if ( !cls.isInterface() && hasNoAttributesOtherThanId() ) {
          val = getFallbackBySimpleName( cls );
        }
      }

      return val;

    } catch ( ClassNotFoundException | ObjectFactoryException e ) {
      throw new RuntimeException( e );
    }
  }

  private boolean hasNoAttributesOtherThanId() {
    return attributes == null
      || attributes.isEmpty()
      || ( attributes.size() == 1 && attributes.containsKey( ID_ATTRIBUTE ) );
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

  private Object getFallbackBySimpleName( Class<?> clazz ) {
    Object lastChanceObject;
    if ( attributes != null && attributes.containsKey( ID_ATTRIBUTE ) ) {
      lastChanceObject = PentahoSystem.get( clazz, attributes.get( ID_ATTRIBUTE ), PentahoSessionHolder.getSession() );
    } else {
      lastChanceObject = PentahoSystem.get( clazz, PentahoSessionHolder.getSession() );
    }

    if ( lastChanceObject != null ) {
      log.warn(
        "Target of <pen:bean class=\"{}\"> was found using deprecated bean ID == class.getSimpleName() fallback. "
          + "The target bean with the id \"{}\" should be published directly with <pen:publish>",
        clazz.getName(),
        clazz.getSimpleName() );
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
