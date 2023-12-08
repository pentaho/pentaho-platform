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

package org.pentaho.platform.engine.core.system.objfac;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.spring.SpringScopeSessionHolder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Framework for Spring-based object factories. Subclasses are required only to implement the init method, which is
 * responsible for setting the {@link ApplicationContext}.
 * <p/>
 * A note on creation and management of objects: Object creation and scoping is handled by Spring with one exception: in
 * the case of a {@link StandaloneSession}. Spring's session scope relates a bean to an javax.servlet.http.HttpSession,
 * and as such it does not know about custom sessions. The correct approach to solve this problem is to write a custom
 * Spring scope (called something like "pentahosession"). Unfortunately, we cannot implement a custom scope to handle
 * the {@link StandaloneSession} because the custom scope would not be able to access it. There is currently no way to
 * statically obtain a reference to a pentaho session. So we are left with using custom logic in this factory to execute
 * a different non-Spring logic path when the IPentahoSession is of type StandaloneSession.
 * <p/>
 *
 * @author Aaron Phillips
 * @see IPentahoObjectFactory
 */
public abstract class AbstractSpringPentahoObjectFactory implements IPentahoObjectFactory {

  protected ConfigurableApplicationContext beanFactory;
  protected static final Log logger = LogFactory.getLog( AbstractSpringPentahoObjectFactory.class );
  protected static final String PRIORITY = "priority";
  private BeanDefinitionPriorityComparitor priorityComparitor = new BeanDefinitionPriorityComparitor();
  private String name;

  protected AbstractSpringPentahoObjectFactory() {
  }

  protected AbstractSpringPentahoObjectFactory( final String name ) {
    this.name = name;
  }

  /**
   * @see IPentahoObjectFactory#get(Class, IPentahoSession)
   */
  public <T> T get( Class<T> interfaceClass, final IPentahoSession session ) throws ObjectFactoryException {
    return get( interfaceClass, null, session );
  }

  /**
   * @see IPentahoObjectFactory#get(Class, String, IPentahoSession)
   */
  public <T> T get( Class<T> interfaceClass, String key, final IPentahoSession session ) throws ObjectFactoryException {
    return retreiveObject( interfaceClass, key, session, null );
  }

  public <T> T get( Class<T> interfaceClass, IPentahoSession session, Map<String, String> props )
      throws ObjectFactoryException {
    return retreiveObject( interfaceClass, null, session, props );
  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession curSession, Map<String, String> properties )
      throws ObjectFactoryException {
    return retreiveObjects( interfaceClass, curSession, properties );
  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession curSession ) throws ObjectFactoryException {
    return retreiveObjects( interfaceClass, curSession, null );
  }

  protected Object instanceClass( String simpleName ) throws ObjectFactoryException {
    return instanceClass( simpleName, null );
  }

  protected Object instanceClass( String simpleName, String key ) throws ObjectFactoryException {
    Object object = null;
    try {
      if ( beanFactory.containsBean( simpleName ) ) {
        object = beanFactory.getType( simpleName ).newInstance();
      } else if ( key != null ) {
        object = beanFactory.getType( key ).newInstance();
      }
    } catch ( Exception e ) {
      String msg =
          Messages.getInstance()
              .getString( "AbstractSpringPentahoObjectFactory.WARN_FAILED_TO_CREATE_OBJECT", key ); //$NON-NLS-1$
      throw new ObjectFactoryException( msg, e );
    }
    return object;
  }

  protected Object instanceClass( Class<?> interfaceClass, String key ) throws ObjectFactoryException {
    Object object = null;
    try {
      if ( key != null ) {
        object = beanFactory.getType( key ).newInstance();
      } else {
        // No published beans by this type, try the interface simplename itself as the key (legacy behavior)
        object = beanFactory.getType( interfaceClass.getSimpleName() ).newInstance();
      }
    } catch ( Exception e ) {
      String msg =
          Messages.getInstance()
              .getString( "AbstractSpringPentahoObjectFactory.WARN_FAILED_TO_CREATE_OBJECT", key ); //$NON-NLS-1$
      throw new ObjectFactoryException( msg, e );
    }
    return object;
  }

  private <T> T retrieveViaSpring( Class<T> interfaceClass ) throws ObjectFactoryException {
    return retrieveViaSpring( interfaceClass, null );
  }

  private <T> T retrieveViaSpring( Class<T> interfaceClass, Map<String, String> props ) throws ObjectFactoryException {
    Object object = null;
    try {

      String beanName = interfaceClass.getSimpleName();


      if ( !beanFactory.getBeanFactory().containsBean( beanName ) ) {
        throw new IllegalStateException( "No bean found for given type" );
      }

      object = beanFactory.getBean( beanName );

    } catch ( Throwable t ) {
      String msg =
          Messages.getInstance().getString(
              "AbstractSpringPentahoObjectFactory.WARN_FAILED_TO_RETRIEVE_OBJECT",
              interfaceClass.getSimpleName() ); //$NON-NLS-1$
      throw new ObjectFactoryException( msg, t );
    }

    // Sanity check
    if ( interfaceClass.isAssignableFrom( object.getClass() ) == false ) {
      throw new IllegalStateException( "Object retrived from Spring not expected type: "
          + interfaceClass.getSimpleName() );
    }

    return (T) object;
  }

  private BeanDefinition getBeanDefinitionFromFactory( final String name ) {
    return beanFactory.getBeanFactory().getBeanDefinition( name );
  }

  protected Object retrieveViaSpring( String beanId ) throws ObjectFactoryException {
    Object object;
    try {
      object = beanFactory.getBean( beanId );
    } catch ( Throwable t ) {
      String msg =
          Messages.getInstance()
              .getString( "AbstractSpringPentahoObjectFactory.WARN_FAILED_TO_RETRIEVE_OBJECT", beanId ); //$NON-NLS-1$
      throw new ObjectFactoryException( msg, t );
    }
    return object;
  }

  private <T> T
  retreiveObject( Class<T> interfaceClass, String key, IPentahoSession session, Map<String, String> props )
      throws ObjectFactoryException {
    if ( logger.isDebugEnabled() ) {
      // cannot access logger here since this object factory provides the logger
      logger
        .debug( "Attempting to get an instance of [" + interfaceClass.getSimpleName() + "] while in session [" + session
          + "]" ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    // Set the Thread Context Classloader to that of the classloader who loaded this class.
    ClassLoader originalClassLoader = null;
    Object object;
    IPentahoSession previousSpringSession = null;
    try {
      originalClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

      previousSpringSession = SpringScopeSessionHolder.SESSION.get();

      if ( session instanceof StandaloneSession ) {
        // first ask Spring for the object, if it is session scoped it will fail
        // since Spring doesn't know about StandaloneSessions

        // Save the session off to support Session and Request scope.
        SpringScopeSessionHolder.SESSION.set( session );
        try {
          if ( key != null ) { // if they want it by id, look for it that way first
            object = retrieveViaSpring( key );
          } else {
            object = retrieveViaSpring( interfaceClass, props );
          }

        } catch ( Throwable t ) {
          // Spring could not create the object, perhaps due to session scoping, let's try
          // retrieving it from our internal session map
          logger.debug( "Retrieving object from Pentaho session map (not Spring)." ); //$NON-NLS-1$

          try {
            object = session.getAttribute( interfaceClass.getSimpleName() );

            if ( ( object == null ) ) {
              // our internal session map doesn't have it, let's create it
              object = instanceClass( interfaceClass, key );
              session.setAttribute( interfaceClass.getSimpleName(), object );
            }
          } catch ( Throwable tt ) {
            String msg =
                Messages.getInstance().getString( "AbstractSpringPentahoObjectFactory.WARN_FAILED_TO_RETRIEVE_OBJECT",
                    interfaceClass.getSimpleName() ); //$NON-NLS-1$
            throw new ObjectFactoryException( msg, tt );
          }
        }
      } else {
        // be sure to clear out any session held.
        SpringScopeSessionHolder.SESSION.set( null );
        // Spring can handle the object retrieval since we are not dealing with StandaloneSession

        if ( key != null ) { // if they want it by id, look for it that way first
          object = retrieveViaSpring( key );
        } else {
          object = retrieveViaSpring( interfaceClass, props );
        }
      }

    } finally {
      SpringScopeSessionHolder.SESSION.set( previousSpringSession );

      if ( originalClassLoader != null ) {
        Thread.currentThread().setContextClassLoader( originalClassLoader );
      }
    }

    if ( logger.isDebugEnabled() ) {
      logger
        .debug( " Got an instance of [" + interfaceClass.getSimpleName() + "]: " + object ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    if ( object instanceof IPentahoInitializer ) {
      ( (IPentahoInitializer) object ).init( session );
    }
    return (T) object;
  }

  protected <T> List<T> retreiveObjects( Class<T> type, final IPentahoSession session, Map<String, String> properties )
      throws ObjectFactoryException {
    if ( logger.isDebugEnabled() ) {
      // cannot access logger here since this object factory provides the logger
      logger.debug( "Attempting to get an instance of [" + type.getSimpleName() + "] while in session [" + session
        + "]" ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    T object = retrieveViaSpring( type );
    if ( object != null && logger.isDebugEnabled() ) {
      logger.debug( " Got an instance of [" + type.getSimpleName() + "]: " + object ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    return Collections.singletonList( object );
  }

  /**
   * @see IPentahoObjectFactory#objectDefined(String)
   */
  public boolean objectDefined( String key ) {
    return beanFactory.containsBean( key );
  }

  /**
   * @param clazz Interface or class literal to search for
   * @return true if a definition exists
   */
  @Override
  public boolean objectDefined( Class<?> clazz ) {
    return beanFactory.containsBean( clazz.getSimpleName() );
  }

  /**
   * @see IPentahoObjectFactory#getImplementingClass(String)
   */
  @SuppressWarnings( "unchecked" )
  public Class getImplementingClass( String key ) {
    return beanFactory.getType( key );
  }

  protected void setBeanFactory( ConfigurableApplicationContext context ) {
    beanFactory = context;
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> clazz, IPentahoSession curSession )
      throws ObjectFactoryException {
    return getObjectReference( clazz, curSession, null );
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> clazz, IPentahoSession curSession,
                                                            Map<String, String> properties )
      throws ObjectFactoryException {

    return null;
  }

  @Override
  public <T> List<IPentahoObjectReference<T>> getObjectReferences( Class<T> interfaceClass, IPentahoSession curSession )
      throws ObjectFactoryException {
    return getObjectReferences( interfaceClass, curSession, null );
  }

  @Override
  public <T> List<IPentahoObjectReference<T>> getObjectReferences( Class<T> interfaceClass, IPentahoSession curSession,
                                                                   Map<String, String> properties )
      throws ObjectFactoryException {
    return Collections.emptyList();
  }

  @Override
  public String getName() {
    return name;
  }

  /**
   * Used to order the lists of implementations based on their priority
   */
  protected class BeanDefinitionPriorityComparitor implements Comparator<BeanDefinitionNamePair> {
    @Override
    public int compare( BeanDefinitionNamePair beanDefinitionNamePair,
                        BeanDefinitionNamePair beanDefinitionNamePair1 ) {
      int pri1 = computePriority( beanDefinitionNamePair.definition );
      int pri2 = computePriority( beanDefinitionNamePair1.definition );

      if ( pri1 == pri2 ) {
        return 0;
      }
      if ( pri1 < pri2 ) {
        return 1;
      }
      return -1;
    }

    private int computePriority( BeanDefinition ref ) {
      if ( ref == null || ref.getAttribute( PRIORITY ) == null ) {
        // return default
        return DEFAULT_PRIORTIY;
      }

      try {
        int val = Integer.parseInt( ref.getAttribute( PRIORITY ).toString() );
        return val;
      } catch ( NumberFormatException e ) {
        logger
            .error( "bean of type " + ref.getBeanClassName() + " has an invalid priority value, only numeric allowed" );
        // return default
        return DEFAULT_PRIORTIY;
      }
    }
  }

  /**
   * Struct class used internally to maintain a mapping between bean name and definition
   */
  protected static class BeanDefinitionNamePair {
    public String name;
    public BeanDefinition definition;

    public BeanDefinitionNamePair( String name, BeanDefinition definition ) {
      this.definition = definition;
      this.name = name;
    }

  }

  @Override
  public int hashCode() {
    return this.beanFactory.getBeanFactory().hashCode();
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof AbstractSpringPentahoObjectFactory ) ) {
      return false;
    }

    AbstractSpringPentahoObjectFactory that = (AbstractSpringPentahoObjectFactory) o;

    if ( !beanFactory.equals( that.beanFactory ) ) {
      return false;
    }

    return true;
  }
}
