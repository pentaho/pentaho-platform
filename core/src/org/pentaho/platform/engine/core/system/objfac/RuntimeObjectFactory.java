package org.pentaho.platform.engine.core.system.objfac;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang.ClassUtils;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoObjectRegistration;
import org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * This class supports the registration of Object implementations as well as {@link IPentahoObjectReference }s at
 * runtime.
 * <p/>
 * PentahoSystem adds an instance of this class to its AggregateObjectFactory and delegates to it for its static
 * registration methods which shadow those defined in {@link IPentahoRegistrableObjectFactory}
 * <p/>
 * Created by nbaker on 2/19/14.
 */
public class RuntimeObjectFactory implements IPentahoRegistrableObjectFactory {


  private final Multimap<Class, IPentahoObjectReference<?>> registry =
    Multimaps.synchronizedSetMultimap( HashMultimap
      .<Class, IPentahoObjectReference<?>>create() );


  public RuntimeObjectFactory() {

  }


  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public <T> IPentahoObjectRegistration registerObject( T obj ) {
    if ( obj instanceof IPentahoObjectReference ) {
      throw new IllegalArgumentException(
        "Object cannot be of type: IPentahoObjectRegistration. Call the appropriate registerReference instead" );
    }
    return registerReference( new SingletonPentahoObjectReference<T>( (Class<T>) obj.getClass(), obj ), Types.ALL );

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference ) {
    return registerReference( reference, Types.ALL );

  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public <T> IPentahoObjectRegistration registerObject( T obj, Types types ) {
    if ( obj instanceof IPentahoObjectReference ) {
      throw new IllegalArgumentException(
        "Object cannot be of type: IPentahoObjectRegistration. Call the appropriate registerReference instead" );
    }
    return registerReference( new SingletonPentahoObjectReference<T>( (Class<T>) obj.getClass(), obj ), types );

  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings( { "unchecked" } )
  @Override
  public <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference, Types types ) {
    List<Class<?>> classesToPublishAs = new ArrayList<Class<?>>();
    final Class<?> clazz = reference.getObjectClass();
    switch( types ) {
      case INTERFACES:
        classesToPublishAs.addAll( ClassUtils.getAllInterfaces( clazz ) );
        break;
      case CLASSES:
        classesToPublishAs.addAll( ClassUtils.getAllSuperclasses( clazz ) );
        classesToPublishAs.add( clazz );
        break;
      case ALL:
        classesToPublishAs.addAll( ClassUtils.getAllInterfaces( clazz ) );
        classesToPublishAs.addAll( ClassUtils.getAllSuperclasses( clazz ) );
        classesToPublishAs.add( clazz );
        break;
    }
    return registerReference( reference, classesToPublishAs.toArray( new Class[ classesToPublishAs.size() ] ) );
  }

  @SuppressWarnings( "unchecked" )
  @Override public <T> IPentahoObjectRegistration registerObject( T obj, Class<?>... classes ) {
    if ( obj instanceof IPentahoObjectReference ) {
      throw new IllegalArgumentException(
        "Object cannot be of type: IPentahoObjectRegistration. Call the appropriate registerReference instead" );
    }
    return registerReference( new SingletonPentahoObjectReference<T>( (Class<T>) obj.getClass(), obj ), classes );
  }

  @Override
  public final <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference,
                                                                 Class<?>... classes ) {

    for ( Class<?> aClass : classes ) {
      registry.get( aClass ).add( reference );
    }
    return new ObjectRegistration( reference, Arrays.asList( classes ) );

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T get( final Class<T> interfaceClass, final IPentahoSession session ) throws ObjectFactoryException {
    final IPentahoObjectReference<T> objectReference = getObjectReference( interfaceClass, session );
    if ( objectReference == null ) {
      return null;
    }
    return objectReference.getObject();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T get( final Class<T> interfaceClass, final String key, final IPentahoSession session )
    throws ObjectFactoryException {

    IPentahoObjectReference<T> reference = getObjectReference( interfaceClass, session,
      Collections.singletonMap( "id", key ) );
    if ( reference == null ) {
      // not found by ID, check by class itself ( special behavior for this deprecated method )
      reference = getObjectReference( interfaceClass, session, Collections.<String, String>emptyMap() );
      if ( reference == null ) {
        return null;
      }
    }
    return reference.getObject();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T get( final Class<T> interfaceClass, final IPentahoSession session, final Map<String, String> properties )
    throws ObjectFactoryException {

    List<IPentahoObjectReference<T>> references = getObjectReferences( interfaceClass, session, properties );
    if ( references.isEmpty() ) {
      return null;
    }
    return references.get( 0 ).getObject();

  }

  /**
   * This class cannot respond to a simple key request. False will always be returned.
   * <p/>
   * {@inheritDoc}
   */
  @Override
  public boolean objectDefined( String key ) {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean objectDefined( Class<?> clazz ) {
    return !registry.get( clazz ).isEmpty();
  }

  /**
   * This class cannot respond to a simple key request. Null will always be returned.
   * <p/>
   * {@inheritDoc}
   */
  @Override
  public Class<?> getImplementingClass( String key ) {
    return null;
  }

  /**
   * No meaning for this class. No-Op Implementation to satisfy interface.
   * <p/>
   * {@inheritDoc}
   */
  @Override
  public void init( String configFile, Object context ) {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> List<T> getAll( final Class<T> interfaceClass, final IPentahoSession session )
    throws ObjectFactoryException {
    return getAll( interfaceClass, session, Collections.<String, String>emptyMap() );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public <T> List<T> getAll( final Class<T> interfaceClass, final IPentahoSession session,
                             final Map<String, String> properties )
    throws ObjectFactoryException {
    List<IPentahoObjectReference<T>> retValReferences = getObjectReferences( interfaceClass, session, properties );
    List<T> retVals = new ArrayList<T>();
    for ( IPentahoObjectReference ref : retValReferences ) {
      retVals.add( (T) ref.getObject() );
    }
    return retVals;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> interfaceClass, IPentahoSession curSession )
    throws ObjectFactoryException {
    return getObjectReference( interfaceClass, curSession, Collections.<String, String>emptyMap() );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> interfaceClass, IPentahoSession curSession,
                                                            Map<String, String> properties )
    throws ObjectFactoryException {
    final List<IPentahoObjectReference<T>> objectReferences =
      getObjectReferences( interfaceClass, curSession, properties );
    if ( objectReferences.isEmpty() ) {
      return null;
    }
    return objectReferences.get( 0 );

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> List<IPentahoObjectReference<T>> getObjectReferences( Class<T> interfaceClass, IPentahoSession curSession )
    throws ObjectFactoryException {
    return getObjectReferences( interfaceClass, curSession, Collections.<String, String>emptyMap() );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> List<IPentahoObjectReference<T>> getObjectReferences( final Class<T> interfaceClass,
                                                                   final IPentahoSession curSession,
                                                                   final Map<String, String> properties )
    throws ObjectFactoryException {
    List<IPentahoObjectReference<T>> retValReferences = new ArrayList<IPentahoObjectReference<T>>();

    try {
      retValReferences =
        SessionCapturedOperation.execute( curSession, new Callable<List<IPentahoObjectReference<T>>>() {
          @SuppressWarnings( "unchecked" )
          @Override public List<IPentahoObjectReference<T>> call() throws Exception {

            List<IPentahoObjectReference<?>> iPentahoObjectReferences =
              getReferencesByQuery( interfaceClass, properties );

            final ArrayList<IPentahoObjectReference<T>> retVals = new ArrayList<IPentahoObjectReference<T>>();
            if ( !iPentahoObjectReferences.isEmpty() ) {

              for ( IPentahoObjectReference<?> ref : iPentahoObjectReferences ) {
                retVals.add( (IPentahoObjectReference<T>) ref );
              }
            }
            return retVals;
          }
        } );
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    Collections.sort( retValReferences );
    Collections.reverse( retValReferences );
    return retValReferences;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return "Runtime Object Factory";
  }


  private <T> List<IPentahoObjectReference<?>> getReferencesByQuery( Class<T> type,
                                                                     Map<String, String> query ) {
    Collection<IPentahoObjectReference<?>> iPentahoObjectReferences = registry.get( type );

    if ( iPentahoObjectReferences.isEmpty() ) {
      return Collections.emptyList();
    }

    final ArrayList<IPentahoObjectReference<?>> returnCollection = new ArrayList<IPentahoObjectReference<?>>();

    for ( IPentahoObjectReference<?> next : iPentahoObjectReferences ) {
      if ( query == null || query.isEmpty() ) {
        returnCollection.add( next );
        continue;
      }

      final Map<String, Object> attributes = next.getAttributes();
      for ( Map.Entry<String, String> queryEntry : query.entrySet() ) {
        if ( attributes.containsKey( queryEntry.getKey() ) && attributes.get( queryEntry.getKey() )
          .equals( queryEntry.getValue() ) ) {
          returnCollection.add( next );
        }
      }
    }

    return returnCollection;

  }

  /**
   * Light wrapper around a {@link Callable} which ensures that the Thread-Bound session in {@link PentahoSessionHolder}
   * is set to that of the specified Session from the ObjectFactory request.
   */
  private static class SessionCapturedOperation {
    private static <T> T execute( IPentahoSession session, Callable<T> callee ) throws Exception {
      SessionSwapper.swap( session );
      try {
        return callee.call();
      } finally {
        SessionSwapper.restore();
      }
    }

  }

  /**
   * Captures the Thread-Bound session replacing it with the one provided. Supports restoring the original later.
   */
  private static class SessionSwapper {
    private static final ThreadLocal<IPentahoSession> originalSessions = new ThreadLocal<IPentahoSession>();

    public static void swap( IPentahoSession tempSession ) {
      IPentahoSession originalSession = PentahoSessionHolder.getSession();

      // Capture even if the same to simplify restore
      originalSessions.set( originalSession );
      if ( originalSession != tempSession ) {
        PentahoSessionHolder.setSession( tempSession );
      }
    }

    public static void restore() {
      IPentahoSession orig = originalSessions.get();
      PentahoSessionHolder.setSession( orig );
    }
  }

  /**
   * Handle returned when an object or reference is registered. Supports de-registration.
   */
  protected class ObjectRegistration implements IPentahoObjectRegistration {

    private IPentahoObjectReference<?> reference;
    private List<Class<?>> publishedClasses;

    public ObjectRegistration( IPentahoObjectReference<?> reference, List<Class<?>> publishedClasses ) {

      this.reference = reference;
      this.publishedClasses = publishedClasses;
    }

    @Override public void remove() {

      for ( Class<?> aClass : publishedClasses ) {
        registry.get( aClass ).remove( reference );
      }

    }
  }

}
