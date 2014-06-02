/*
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
 * Copyright 2013 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    String filter = createFilter( properties );
    try {
      Collection<ServiceReference<T>> refs = context.getServiceReferences( interfaceClass, filter );
      ServiceReference ref;
      if ( refs != null && refs.size() > 0 ) {
        ref = refs.toArray( new ServiceReference[ refs.size() ] )[ 0 ];
      } else {
        ref = context.getServiceReference( "" + interfaceClass.getName() );
      }
      if ( ref == null ) {
        log.error( "\n\nOSGI: did not find object: " + interfaceClass.getName() );
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
    ServiceReference ref = context.getServiceReference( clazz );
    return ref != null;
  }

  @Override
  public boolean objectDefined( Class<?> clazz ) {
    if ( clazz == null ) {
      throw new IllegalStateException( "Class is null" );
    }
    ServiceReference ref = context.getServiceReference( clazz );
    return ref != null;
  }

  @Override
  public Class<?> getImplementingClass( String key ) {
    throw new UnsupportedOperationException( "OSGI Object Factory does not support this method" );
  }

  @Override
  public void init( String configFile, Object context ) {

  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession curSession ) throws ObjectFactoryException {
    return getAll( interfaceClass, curSession, Collections.<String, String>emptyMap() );
  }

  private String createFilter( Map<String, String> props ) {
    if ( props == null || props.size() == 0 ) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    sb.append( "(" );
    for ( Map.Entry<String, String> entry : props.entrySet() ) {
      sb.append( "&(" ).append( entry.getKey() ).append( "=" ).append( entry.getValue() ).append( ")" );
    }
    sb.append( ")" );
    return sb.toString();
  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession session, Map<String, String> properties )
    throws ObjectFactoryException {

    String filter = createFilter( properties );

    List<T> returnList = new ArrayList<T>();
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

    String filter = createFilter( properties );
    try {
      Collection<ServiceReference<T>> refs = context.getServiceReferences( interfaceClass, filter );
      if ( refs == null || refs.size() == 0 ) {
        log.error( "\n\nOSGI: did not find object: " + interfaceClass.getName() );
        return null;
      }
      ServiceReference<T> serviceReference = refs.toArray( new ServiceReference[ refs.size() ] )[ 0 ];

      return new OsgiPentahoObjectReference( interfaceClass, serviceReference );
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

    String filter = createFilter( properties );
    try {
      Collection<ServiceReference<T>> refs = context.getServiceReferences( interfaceClass, filter );
      if ( refs == null || refs.size() == 0 ) {
        log.error( "OSGI: did not find object: " + interfaceClass.getName() );
        return Collections.emptyList();
      }

      List<IPentahoObjectReference<T>> returnRefs = new ArrayList<IPentahoObjectReference<T>>();
      for ( ServiceReference ref : refs ) {
        returnRefs.add( new OsgiPentahoObjectReference<T>( interfaceClass, ref ) );
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

  private class OsgiPentahoObjectReference<T> implements IPentahoObjectReference<T> {
    private Class<T> type;
    private ServiceReference osgiRef;
    private Map<String, Object> attributes;

    public OsgiPentahoObjectReference( Class<T> type, ServiceReference ref ) {
      this.type = type;
      osgiRef = ref;
    }

    @Override public Class<?> getObjectClass() {
      return type;
    }

    @Override
    public Map<String, Object> getAttributes() {
      if ( attributes != null ) {
        return attributes;
      }
      attributes = new HashMap<String, Object>();

      for ( String key : osgiRef.getPropertyKeys() ) {
        attributes.put( key, osgiRef.getProperty( key ) );
      }
      return attributes;
    }

    @Override
    public T getObject() {
      Object service = context.getService( osgiRef );

      if ( service instanceof IPentahoInitializer ) {
        ( (IPentahoInitializer) service ).init( PentahoSessionHolder.getSession() );
      }
      return (T) service;
    }

    @Override
    public Integer getRanking() {
      Object property = osgiRef.getProperty( "service.ranking" );
      if ( !( property instanceof Integer ) ) {
        return 0;
      }
      return (Integer) property;
    }

    @Override
    public int compareTo( IPentahoObjectReference<T> o ) {
      if ( o == null ) {
        return 1;
      }
      if ( o instanceof OsgiPentahoObjectReference ) {
        OsgiPentahoObjectReference ref = (OsgiPentahoObjectReference) o;
        return osgiRef.compareTo( ref.osgiRef );
      }
      Integer ourRank = getRanking();
      Integer theirRanking = o.getRanking();
      return ourRank.compareTo( theirRanking );

    }
  }
}
