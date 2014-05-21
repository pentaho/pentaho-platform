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

import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * AggregateObectFactory holds a collection of IPentahoObjectFactory implementations, delegating calls to each and
 * collecting the results. Results are ordered by "priority" attribute if present, with the highest priority object
 * returned in the calls to retrieve a single object.
 * <p/>
 * {@inheritDoc}
 * <p/>
 * User: nbaker Date: 1/15/13
 */
public class AggregateObjectFactory implements IPentahoObjectFactory {
  protected final Set<IPentahoObjectFactory> factories = Collections.synchronizedSet( new HashSet<IPentahoObjectFactory>() );
  protected IPentahoObjectFactory primaryFactory;
  private Logger logger = LoggerFactory.getLogger( AggregateObjectFactory.class );

  private final ReadWriteLock factoryLock = new ReentrantReadWriteLock( false ); // we're not fair
  private Lock writeLock = factoryLock.writeLock();
  private Lock readLock = factoryLock.readLock();

  public AggregateObjectFactory() {

  }

  public void registerObjectFactory( IPentahoObjectFactory fact, boolean primary ) {
    writeLock.lock();
    try{
      factories.add( fact );
    } finally {
      writeLock.unlock();
    }
    if ( primary ) {
      primaryFactory = fact;
    }
    logger.debug( "New IPentahoObjectFactory registered: " + fact.getName() );
  }

  public void registerObjectFactory( IPentahoObjectFactory fact ) {
    registerObjectFactory( fact, false );
  }

  /**
   * De-Register an ObjectFactory
   *
   * @param factory
   * @return true if the factory was registered and successfully removed.
   */
  public boolean deregisterObjectFactory( IPentahoObjectFactory factory ) {
    writeLock.lock();
    try {
      return factories.remove( factory );
    } finally {
      writeLock.unlock();
    }
  }

  public Set<IPentahoObjectFactory> getFactories() {
    return new HashSet( factories );
  }

  public IPentahoObjectFactory getPrimaryFactory() {
    return primaryFactory;
  }

  @Override
  public <T> T get( Class<T> interfaceClass, String key, IPentahoSession session ) throws ObjectFactoryException {
    // if they want it by id, check for that first
    if ( key != null ) {
      readLock.lock();
      try {
        for ( IPentahoObjectFactory fact : factories ) {
          if ( fact.objectDefined( key ) ) {
            T object = fact.get( interfaceClass, key, session );
            logger.debug( MessageFormat.format( "Found object for key: {0} in factory: {1}", key, fact.getName() ) );
            return object;
          }
        }
      } finally {
        readLock.unlock();
      }
    }

    T fromType = get( interfaceClass, session, null );
    if ( fromType != null ) {
      return fromType;
    }

    String msg =
      Messages.getInstance().getString( "AbstractSpringPentahoObjectFactory.WARN_FAILED_TO_RETRIEVE_OBJECT",
        interfaceClass.getSimpleName() );
    throw new ObjectFactoryException( msg );

  }

  @Override
  public <T> T get( Class<T> interfaceClass, IPentahoSession session ) throws ObjectFactoryException {

    return get( interfaceClass, session, null );
  }

  private int computePriority( IPentahoObjectReference ref ) {
    Map<String, Object> props = ref.getAttributes();
    if ( props == null ) {
      return DEFAULT_PRIORTIY;
    }
    Object sPri = ref.getAttributes().get( "priority" );
    if ( sPri == null ) {
      return DEFAULT_PRIORTIY;
    }
    try {
      return Integer.parseInt( sPri.toString() );
    } catch ( NumberFormatException e ) {
      return DEFAULT_PRIORTIY;
    }

  }

  @Override
  public boolean objectDefined( String key ) {
    readLock.lock();
    try{
      for ( IPentahoObjectFactory fact : factories ) {
        if ( fact.objectDefined( key ) ) {
          logger.debug( MessageFormat.format( "Object defined for key: {0} in factory: {1}", key, fact.getName() ) );
          return true;
        }
      }

    } finally {
      readLock.unlock();
    }
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated All usage of key methods are deprecated, use object attributes instead
   */
  @Override
  public Class<?> getImplementingClass( String key ) {
    readLock.lock();
    try{
      for ( IPentahoObjectFactory fact : factories ) {
        if ( fact.objectDefined( key ) ) {
          logger.debug( MessageFormat.format( "Found implementing class for key: {0} in factory: {1}", key, fact
            .getName() ) );
          return fact.getImplementingClass( key );
        }
      }
    } finally {
      readLock.unlock();
    }
    return null;
  }

  @Override
  public void init( String configFile, Object context ) {

  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession curSession ) throws ObjectFactoryException {
    return getAll( interfaceClass, curSession, null );
  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession curSession, Map<String, String> properties )
    throws ObjectFactoryException {

    List<IPentahoObjectReference<T>> referenceList = new ArrayList<IPentahoObjectReference<T>>();

    readLock.lock();
    try{
      for ( IPentahoObjectFactory fact : factories ) {
        if ( fact.objectDefined( interfaceClass ) ) {
          List<IPentahoObjectReference<T>> refs = fact.getObjectReferences( interfaceClass, curSession, properties );
          if ( refs != null ) {
            referenceList.addAll( refs );
          }
        }
      }
    } finally {
      readLock.unlock();
    }

    Collections.sort( referenceList, referencePriorityComparitor );

    // create final list of impls
    List<T> entryList = new ArrayList<T>();
    for ( IPentahoObjectReference<T> ref : referenceList ) {
      entryList.add( ref.getObject() );
    }

    return entryList;
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> clazz, IPentahoSession curSession )
    throws ObjectFactoryException {

    Set<IPentahoObjectReference<T>> references = new HashSet<IPentahoObjectReference<T>>();

    readLock.lock();
    try{
      for ( IPentahoObjectFactory fact : factories ) {
        if ( fact.objectDefined( clazz ) ) {
          IPentahoObjectReference<T> found = fact.getObjectReference( clazz, curSession );
          if ( found != null ) {
            references.add( found );
          }

        }
      }
    } finally {
      readLock.unlock();
    }
    IPentahoObjectReference<T> highestRef = null;
    int highestRefPriority = -1;
    for ( IPentahoObjectReference<T> ref : references ) {
      int pri = computePriority( ref );
      if ( pri > highestRefPriority ) {
        highestRef = ref;
        highestRefPriority = pri;
      }
    }

    return highestRef;
  }

  @Override
  public <T> T get( Class<T> clazz, IPentahoSession session, Map<String, String> properties )
    throws ObjectFactoryException {

    IPentahoObjectReference<T> highestRef = this.getObjectReference( clazz, session, properties );

    if ( highestRef != null ) {
      return highestRef.getObject();
    }
    readLock.lock();
    try{
      for ( IPentahoObjectFactory fact : factories ) {
        if ( fact.objectDefined( clazz.getSimpleName() ) ) {
          T object = fact.get( clazz, clazz.getSimpleName(), session );
          return object;
        }
      }
    } finally {
      readLock.unlock();
    }
    String msg =
      Messages.getInstance().getString( "AbstractSpringPentahoObjectFactory.WARN_FAILED_TO_RETRIEVE_OBJECT",
        clazz.getSimpleName() );
    throw new ObjectFactoryException( msg );

  }

  @Override
  public boolean objectDefined( Class<?> clazz ) {
    readLock.lock();
    try{
      for ( IPentahoObjectFactory fact : factories ) {
        if ( fact.objectDefined( clazz ) ) {

          logger.debug( MessageFormat.format( "Found object for class: {0} in factory: {1}", clazz.getName(), fact
            .getName() ) );
          return true;
        }
      }
    } finally {
      readLock.unlock();
    }
    return false;
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> interfaceClass, IPentahoSession curSession,
                                                            Map<String, String> properties )
    throws ObjectFactoryException {

    Set<IPentahoObjectReference<T>> references = new HashSet<IPentahoObjectReference<T>>();
    readLock.lock();
    try{
      for ( IPentahoObjectFactory fact : factories ) {
        if ( fact.objectDefined( interfaceClass ) ) {
          IPentahoObjectReference<T> found = fact.getObjectReference( interfaceClass, curSession, properties );
          if ( found != null ) {
            references.add( found );
          }
        }
      }
    } finally {
      readLock.unlock();
    }
    IPentahoObjectReference<T> highestRef = null;
    int highestRefPriority = -1;
    for ( IPentahoObjectReference<T> ref : references ) {
      int pri = computePriority( ref );
      if ( pri > highestRefPriority ) {
        highestRef = ref;
        highestRefPriority = pri;
      }
    }

    return highestRef;
  }

  public void clear() {
    writeLock.lock();
    try {
      this.factories.clear();
    } finally {
      writeLock.unlock();
    }
  }

  private static ReferencePriorityComparitor referencePriorityComparitor = new ReferencePriorityComparitor();


  private static class ReferencePriorityComparitor implements Comparator<IPentahoObjectReference> {
    private static final String PRIORITY = "priority";

    @Override
    public int compare( IPentahoObjectReference ref1, IPentahoObjectReference ref2 ) {
      int pri1 = extractPriority( ref1 );
      int pri2 = extractPriority( ref2 );
      if ( pri1 == pri2 ) {
        return 0;
      } else if ( pri1 < pri2 ) {
        return 1;
      } else {
        return -1;
      }

    }

    private int extractPriority( IPentahoObjectReference ref ) {
      if ( ref == null || ref.getAttributes() == null || !ref.getAttributes().containsKey( PRIORITY ) ) {
        // return default
        return DEFAULT_PRIORTIY;
      }

      try {
        return Integer.parseInt( ref.getAttributes().get( PRIORITY ).toString() );
      } catch ( NumberFormatException e ) {
        // return default
        return DEFAULT_PRIORTIY;
      }
    }
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
    // Use a set to avoid duplicates
    Set<IPentahoObjectReference<T>> referenceSet = new HashSet<IPentahoObjectReference<T>>();

    readLock.lock();
    try{
      for ( IPentahoObjectFactory fact : factories ) {
        if ( fact.objectDefined( interfaceClass ) ) {
          List<IPentahoObjectReference<T>> found = fact.getObjectReferences( interfaceClass, curSession, properties );
          if ( found != null ) {
            referenceSet.addAll( found );
          }
        }
      }
    } finally {
      readLock.unlock();
    }

    // transform to a list to sort
    List<IPentahoObjectReference<T>> referenceList = new ArrayList<IPentahoObjectReference<T>>();
    referenceList.addAll( referenceSet );
    Collections.sort( referenceList, referencePriorityComparitor );
    return referenceList;
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }
}
