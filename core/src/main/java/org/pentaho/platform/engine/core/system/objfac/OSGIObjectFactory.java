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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.osgi.OSGIUtils;
import org.pentaho.platform.engine.core.system.osgi.OsgiPentahoObjectReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This IPentahoObjectFactory implementation looks up objects in a configured OSGI BundleContext.
 * <p/>
 * Parameterized get() calls will have their values converted to an OSGI service {@link org.osgi.framework.Filter} with
 * all parameters combined with boolean "&amp;".
 * <p/>
 * User: nbaker Date: 10/31/13 Time: 11:43 AM
 */
@SuppressWarnings( "unchecked" )
public class OSGIObjectFactory implements IPentahoObjectFactory {

  private BundleContext context;
  Logger log = LoggerFactory.getLogger( OSGIObjectFactory.class );
  public static final String REFERENCE_CLASS = "reference_class";

  public OSGIObjectFactory( final BundleContext context ) {
    this.context = context;

  }

  public <T> T get( Class<T> tClass, IPentahoSession session ) throws ObjectFactoryException {
    return get( tClass, null, session );
  }

  @Override
  public <T> T get( Class<T> interfaceClass, String key, IPentahoSession session ) throws ObjectFactoryException {
    Map map = ( key != null ) ? Collections.singletonMap( "id", key ) : Collections.emptyMap();
    return (T) get( interfaceClass, session, map );
  }

  @Override
  public <T> T get( Class<T> interfaceClass, IPentahoSession session, Map<String, String> properties )
    throws ObjectFactoryException {
    if ( isBundleContextValid() == false ) {
      return null;
    }

    try {
      Map<String, String> props = new HashMap<String, String>();
      if ( properties != null ) {
        props.putAll( properties );
      }
      props.put( REFERENCE_CLASS, interfaceClass.getName() );
      Collection<ServiceReference<IPentahoObjectReference>> serviceReferences = this.context
        .getServiceReferences( IPentahoObjectReference.class, OSGIUtils.createFilter( props ) );

      if ( serviceReferences != null && serviceReferences.size() > 0 ) {
        IPentahoObjectReference<T> obj = context.getService( serviceReferences.iterator().next() );
        return obj.getObject();
      }

    } catch ( InvalidSyntaxException e ) {
      log.debug( "Error retrieving from OSGI as ServiceReference, will try as bare type", e );
    }

    String filter = OSGIUtils.createFilter( properties );
    try {
      Collection<ServiceReference<T>> refs = context.getServiceReferences( interfaceClass, filter );
      ServiceReference ref;
      if ( refs != null && refs.size() > 0 ) {
        ref = refs.toArray( new ServiceReference[ refs.size() ] )[ 0 ];
      } else {
        ref = context.getServiceReference( "" + interfaceClass.getName() );
      }
      if ( ref == null ) {
        log.info( "\n\nOSGI: did not find object: " + interfaceClass.getName() );
        return null;
      }
      Object obj = context.getService( ref );
      if ( obj instanceof IPentahoInitializer ) {
        ( (IPentahoInitializer) obj ).init( session );
      }
      return (T) obj;
    } catch ( InvalidSyntaxException e ) {
      log.error( "Error retrieving from OSGI ObjectFactory", e );
    }
    return null;
  }

  @Override
  public boolean objectDefined( String clazz ) {
    if ( clazz == null ) {
      throw new IllegalStateException( "Class is null" );
    }

    if ( isBundleContextValid() == false ) {
      return false;
    }

    ServiceReference ref = context.getServiceReference( clazz );
    return ref != null;
  }

  @Override
  public boolean objectDefined( Class<?> clazz ) {
    if ( clazz == null ) {
      throw new IllegalStateException( "Class is null" );
    }

    try {
      return getObjectReference( clazz, null ) != null;
    } catch ( ObjectFactoryException e ) {
      return false;
    }
  }

  @Override
  public Class<?> getImplementingClass( String key ) {
    throw new UnsupportedOperationException( "OSGI Object Factory does not support this method" );
  }

  @Override
  public void init( String configFile, Object context ) {
    // No op
  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession curSession ) throws ObjectFactoryException {
    return getAll( interfaceClass, curSession, Collections.<String, String>emptyMap() );
  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession session, Map<String, String> properties )
    throws ObjectFactoryException {

    if ( isBundleContextValid() == false ) {
      return null;
    }

    List<T> returnList = new ArrayList<T>();

    // make sure we check by reference first
    if ( properties == null || !properties.containsKey( REFERENCE_CLASS ) ) {
      Map<String, String> props = new HashMap<String, String>();
      if ( properties != null ) {
        props.putAll( properties );
      }
      props.put( REFERENCE_CLASS, interfaceClass.getName() );
      List<IPentahoObjectReference> all = getAll( IPentahoObjectReference.class, session, props );
      if ( all != null ) {
        for ( IPentahoObjectReference iPentahoObjectReference : all ) {
          returnList.add( (T) iPentahoObjectReference.getObject() );
        }
      }
    }


    String filter = OSGIUtils.createFilter( properties );

    try {
      Collection<ServiceReference<T>> refs = context.getServiceReferences( interfaceClass, filter );
      if ( refs == null || refs.size() == 0 ) {
        log.info( "\n\nOSGI: did not find object: " + interfaceClass.getName() );
        return returnList;
      }

      for ( ServiceReference ref : refs ) {
        T obj = (T) context.getService( ref );
        if ( obj instanceof IPentahoInitializer ) {
          ( (IPentahoInitializer) obj ).init( session );
        }
        returnList.add( obj );
      }

      return returnList;
    } catch ( InvalidSyntaxException e ) {
      e.printStackTrace();
    }
    return returnList;
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> interfaceClass, IPentahoSession curSession )
    throws ObjectFactoryException {
    return getObjectReference( interfaceClass, curSession, Collections.<String, String>emptyMap() );
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> interfaceClass, IPentahoSession curSession,
                                                            Map<String, String> properties )
    throws ObjectFactoryException {

    if ( isBundleContextValid() == false ) {
      return null;
    }


    // make sure we check by reference first
    if ( properties == null || !properties.containsKey( REFERENCE_CLASS ) ) {
      Map<String, String> props = new HashMap<String, String>();
      if ( properties != null ) {
        props.putAll( properties );
      }
      props.put( REFERENCE_CLASS, interfaceClass.getName() );
      IPentahoObjectReference<IPentahoObjectReference> objectReference =
        getObjectReference( IPentahoObjectReference.class, curSession, props );
      if ( objectReference != null ) {
        return objectReference.getObject();
      }
    }

    String filter = OSGIUtils.createFilter( properties );
    try {
      Collection<ServiceReference<T>> refs = context.getServiceReferences( interfaceClass, filter );
      if ( refs == null || refs.size() == 0 ) {
        log.info( "\n\nOSGI: did not find object: " + interfaceClass.getName() );
        return null;
      }

      ServiceReference[] serviceReferences = refs.toArray( new ServiceReference[ refs.size() ] );
      Arrays.sort( serviceReferences, new Comparator<ServiceReference>() {
        @Override public int compare( ServiceReference o1, ServiceReference o2 ) {
          Object oRank1 = o1.getProperty( Constants.SERVICE_RANKING );
          Object oRank2 = o2.getProperty( Constants.SERVICE_RANKING );
          // if the property is not supplied or of incorrect type, use the default
          int rank1 = ( oRank1 != null && oRank1 instanceof Integer ) ? ( (Integer) oRank1 ).intValue() : 0;
          int rank2 = ( oRank2 != null && oRank2 instanceof Integer ) ? ( (Integer) oRank2 ).intValue() : 0;
          return rank1 - rank2;
        }
      } );
      ServiceReference<T> serviceReference = serviceReferences[ 0 ];

      return new OsgiPentahoObjectReference( this.context, interfaceClass, serviceReference );
    } catch ( InvalidSyntaxException e ) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public <T> List<IPentahoObjectReference<T>> getObjectReferences( Class<T> interfaceClass, IPentahoSession curSession )
    throws ObjectFactoryException {

    return getObjectReferences( interfaceClass, curSession, Collections.<String, String>emptyMap() );
  }

  @Override
  public <T> List<IPentahoObjectReference<T>> getObjectReferences( Class<T> interfaceClass, IPentahoSession curSession,
                                                                   Map<String, String> properties )
    throws ObjectFactoryException {

    if ( isBundleContextValid() == false ) {
      return Collections.emptyList();
    }

    List<IPentahoObjectReference<T>> returnRefs = new ArrayList<IPentahoObjectReference<T>>();

    // make sure we check by reference first
    if ( properties == null || !properties.containsKey( REFERENCE_CLASS ) ) {
      Map<String, String> props = new HashMap<>();
      if ( properties != null ) {
        props.putAll( properties );
      }
      props.put( REFERENCE_CLASS, interfaceClass.getName() );
      List<IPentahoObjectReference<IPentahoObjectReference>> objectReferences =
        getObjectReferences( IPentahoObjectReference.class, curSession, props );
      for ( IPentahoObjectReference<IPentahoObjectReference> objectReference : objectReferences ) {
        returnRefs.add( objectReference.getObject() );
      }
    }

    String filter = OSGIUtils.createFilter( properties );
    try {
      Collection<ServiceReference<T>> refs = context.getServiceReferences( interfaceClass, filter );
      if ( refs == null || refs.size() == 0 ) {
        log.info( "OSGI: did not find object: " + interfaceClass.getName() );
        return returnRefs;
      }

      for ( ServiceReference ref : refs ) {
        returnRefs.add( new OsgiPentahoObjectReference<T>( this.context, interfaceClass, ref ) );
      }
      Collections.sort( returnRefs );

      return returnRefs;
    } catch ( InvalidSyntaxException e ) {
      e.printStackTrace();
    }
    return Collections.emptyList();
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  /**
   * Occasionally the Bundle Context will be invalidated before the OSGIObjectFactory wrapping it is de-registered. This
   * method checks for this inconsistency and deregisters the OSGIObjectFactory. Callers should handle a false condition
   * gracefully, returning null or false to the caller.
   */
  private boolean isBundleContextValid() {
    try {
      // This works in Equinox, but Felix throws an IllegalStateException trying to get the Bundle
      int state = context.getBundle().getState();
      switch ( state ) {
        case Bundle.ACTIVE:
          return true;
        default:
          return false;
      }
    } catch ( IllegalStateException e ) {
      return false;
    }
  }

}
