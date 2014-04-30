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

package org.pentaho.platform.engine.core.system.objfac;

import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandaloneObjectFactory implements IPentahoDefinableObjectFactory {

  private Map<String, ObjectCreator> creators = Collections.synchronizedMap( new HashMap<String, ObjectCreator>() );
  private Map<String, Object> instanceMap = new HashMap<String, Object>();
  private String name;

  public StandaloneObjectFactory() {
  }

  public StandaloneObjectFactory( final String name ) {
    this.name = name;
  }

  public <T> T get( Class<T> interfaceClass, IPentahoSession session ) throws ObjectFactoryException {
    return get( interfaceClass, interfaceClass.getSimpleName(), session );
  }

  @SuppressWarnings( "unchecked" )
  public <T> T get( Class<T> interfaceClass, String key, IPentahoSession session ) throws ObjectFactoryException {
    return (T) retreiveObject( key, session );
  }

  public Class getImplementingClass( String key ) {
    if ( !objectDefined( key ) ) {
      return null;
    }
    ObjectCreator creator = creators.get( key );
    try {
      return creator.createClass();
    } catch ( Exception e ) { // convert to a runtime exception per api
      throw new RuntimeException( "Failed to load implementing class for " + key, e ); //$NON-NLS-1$
    }
  }

  public void init( String arg0, Object arg1 ) {
    creators.clear();
  }

  public boolean objectDefined( String key ) {
    return instanceMap.containsKey( key ) || creators.get( key ) != null;
  }

  public void defineObject( String key, String className, Scope scope ) {

    defineObject( key, className, scope, getClass().getClassLoader() );
  }

  public void defineObject( String key, String className, Scope scope, ClassLoader loader ) {

    ObjectCreator creator = new ObjectCreator( className, scope, loader );
    creators.put( key, creator );
  }

  protected Object retreiveObject( String key, IPentahoSession session ) throws ObjectFactoryException {

    Object o = instanceMap.get( key );
    if ( o != null ) {
      return o;
    }

    ObjectCreator creator = creators.get( key );
    if ( creator == null ) {
      String msg =
        Messages.getInstance()
          .getString( "AbstractSpringPentahoObjectFactory.WARN_FAILED_TO_CREATE_OBJECT", key ); //$NON-NLS-1$
      throw new ObjectFactoryException( msg );
    }

    Object instance = creator.getInstance( key, session );

    return instance;
  }

  private class ObjectCreator {

    private Scope scope = null;
    private String className = null;
    private ThreadLocal<Object> threadLocalInstance = null;
    private Object globalInstance = null;
    private ClassLoader loader;

    public ObjectCreator( String className, Scope scope, ClassLoader loader ) {
      this.className = className.trim();
      this.scope = scope;
      this.loader = loader;
      if ( scope == Scope.THREAD ) {
        threadLocalInstance = new ThreadLocal<Object>();
      }
    }

    public Object getInstance( String key, IPentahoSession session ) throws ObjectFactoryException {
      if ( scope == Scope.GLOBAL ) {
        return getGlobalInstance( key, session );
      } else if ( scope == Scope.SESSION ) {
        return getSessionInstance( key, session );
      } else if ( scope == Scope.LOCAL ) {
        return getLocalInstance( key, session );
      } else if ( scope == Scope.THREAD ) {
        return getThreadInstance( key, session );
      }
      return null;
    }

    protected Object createObject() throws ObjectFactoryException {
      Object instance = null;
      Class<?> classObject = createClass();

      try {
        instance = classObject.newInstance();
      } catch ( Exception e ) {
        if ( e instanceof RuntimeException ) {
          throw (RuntimeException) e;
        }
        throw new ObjectFactoryException( e );
      }

      return instance;
    }

    protected Class createClass() throws ObjectFactoryException {

      try {
        return loader.loadClass( className );
      } catch ( Exception e ) {
        if ( e instanceof RuntimeException ) {
          throw (RuntimeException) e;
        }
        throw new ObjectFactoryException( e );
      }
    }

    public Object getGlobalInstance( String key, IPentahoSession session ) throws ObjectFactoryException {
      if ( null == globalInstance ) {
        globalInstance = createObject();
        if ( globalInstance instanceof IPentahoInitializer ) {
          ( (IPentahoInitializer) globalInstance ).init( session );
        }
      }
      return globalInstance;
    }

    public Object getSessionInstance( String key, IPentahoSession session ) throws ObjectFactoryException {

      if ( null == session ) {
        throw new IllegalArgumentException( Messages.getInstance().getErrorString(
          "SessionObjectCreator.ERROR_0001_INVALID_SESSION" ) ); //$NON-NLS-1$
      }
      Object instance = session.getAttribute( key );

      if ( ( instance == null ) ) {
        instance = createObject();
        if ( instance instanceof IPentahoInitializer ) {
          ( (IPentahoInitializer) instance ).init( session );
        }
        session.setAttribute( key, instance );
      }
      return instance;
    }

    public Object getLocalInstance( String key, IPentahoSession session ) throws ObjectFactoryException {
      Object instance = createObject();

      if ( instance instanceof IPentahoInitializer ) {
        ( (IPentahoInitializer) instance ).init( session );
      }
      return instance;
    }

    public Object getThreadInstance( String key, IPentahoSession session ) throws ObjectFactoryException {
      Object instance = threadLocalInstance.get();

      if ( ( instance == null ) ) {
        instance = createObject();
        if ( instance instanceof IPentahoInitializer ) {
          ( (IPentahoInitializer) instance ).init( session );
        }
        threadLocalInstance.set( instance );
      }
      return instance;
    }
  }

  public void defineInstance( String key, Object instance ) {
    instanceMap.put( key, instance );
  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession curSession ) throws ObjectFactoryException {
    return Collections.singletonList( get( interfaceClass, curSession ) );
  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession curSession, Map<String, String> properties )
    throws ObjectFactoryException {
    return getAll( interfaceClass, curSession );
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> clazz, IPentahoSession curSession )
    throws ObjectFactoryException {

    T obj = get( clazz, curSession );
    return new SingletonPentahoObjectReference<T>( clazz, obj );

  }

  @Override
  public <T> T get( Class<T> interfaceClass, IPentahoSession session, Map<String, String> properties )
    throws ObjectFactoryException {
    return get( interfaceClass, session );
  }

  @Override
  public boolean objectDefined( Class<?> clazz ) {
    return instanceMap.containsKey( clazz.getSimpleName() ) || creators.get( clazz.getSimpleName() ) != null;
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> interfaceClass, IPentahoSession curSession,
                                                            Map<String, String> properties )
    throws ObjectFactoryException {
    T obj = get( interfaceClass, curSession );
    return new SingletonPentahoObjectReference<T>( interfaceClass, obj );
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

    T obj = get( interfaceClass, curSession );
    List<IPentahoObjectReference<T>> refs = new ArrayList<IPentahoObjectReference<T>>();
    refs.add( new SingletonPentahoObjectReference<T>( interfaceClass, obj ) );
    return refs;

  }

  @Override
  public String getName() {
    return name;
  }
}
