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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.core.system.objfac;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoObjectRegistration;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;
import org.pentaho.platform.engine.core.system.objfac.spring.SpringPentahoObjectReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nbaker on 4/27/15.
 */
public class OSGIRuntimeObjectFactory extends RuntimeObjectFactory {
  public static final String REFERENCE_CLASS = "reference_class";
  private BundleContext bundleContext;
  private AtomicBoolean osgiInitialized = new AtomicBoolean( false );
  private List<OSGIPentahoObjectRegistration> deferredRegistrations = new ArrayList<OSGIPentahoObjectRegistration>();
  private Logger logger = LoggerFactory.getLogger( getClass() );

  public OSGIRuntimeObjectFactory() {
  }

  public void setBundleContext( BundleContext bundleContext ) {

    this.bundleContext = bundleContext;
    // Migrate previously registered entries to OSGI

    Iterator<OSGIPentahoObjectRegistration> iterator = deferredRegistrations.iterator();
    synchronized ( deferredRegistrations ) {
      while ( iterator.hasNext() ) {
        OSGIPentahoObjectRegistration osgiPentahoObjectRegistration = iterator.next();
        ObjectRegistration deferredRegistration = osgiPentahoObjectRegistration.iPentahoObjectRegistration;
        Class<?>[] classes = deferredRegistration.getPublishedClasses()
            .toArray( new Class<?>[ deferredRegistration.getPublishedClasses().size() ] );
        this.registerReference( deferredRegistration.getReference(), osgiPentahoObjectRegistration, classes );
        iterator.remove();
      }
    }
    osgiInitialized.set( true );


  }

  public <T> IPentahoObjectRegistration registerReference( final IPentahoObjectReference<?> reference,
                                                           OSGIPentahoObjectRegistration existingRegistration,
                                                           Class<?>... classes ) {

    if ( this.bundleContext == null ) {
      ObjectRegistration runtimeRegistration = (ObjectRegistration) super.registerReference( reference, classes );
      OSGIPentahoObjectRegistration osgiPentahoObjectRegistration =
          new OSGIPentahoObjectRegistration( runtimeRegistration );
      synchronized ( deferredRegistrations ) {
        deferredRegistrations.add( osgiPentahoObjectRegistration );
      }
      return osgiPentahoObjectRegistration;
    }
    Hashtable<String, Object> hashtable = new Hashtable<String, Object>();
    hashtable.putAll( reference.getAttributes() );

    List<ServiceRegistration<?>> registrations = new ArrayList<ServiceRegistration<?>>();
    for ( Class<?> aClass : classes ) {
      try {

        // When OSGI R6 is released we can use the PrototypeServiceFactory. Until then we can't support factory
        // references unless the IPentahoObjectReference is a Singleton scope
        if ( reference instanceof SingletonPentahoObjectReference
            || reference instanceof SpringPentahoObjectReference && ( hashtable.get( "scope" ).equals( "singleton" ) ) ) {
          ServiceFactory<Object> factory = new ServiceFactory<Object>() {
            @Override
            public Object getService( Bundle bundle, ServiceRegistration<Object> serviceRegistration ) {
              return reference.getObject();
            }

            @Override
            public void ungetService( Bundle bundle, ServiceRegistration<Object> serviceRegistration, Object o ) {

            }
          };
          if ( hashtable.containsKey( "priority" ) ) {
            hashtable.put( Constants.SERVICE_RANKING, hashtable.get( "priority" ) );
          }
          ServiceRegistration<?> serviceRegistration =
              bundleContext.registerService( aClass.getName(), factory, hashtable );
          registrations.add( serviceRegistration );
        } else {

          // Publish it as an IPentahoObjectReference instead
          Hashtable<String, Object> referenceHashTable = new Hashtable<>( hashtable );
          referenceHashTable.put( REFERENCE_CLASS, aClass.getName() );
          ServiceRegistration<?> serviceRegistration =
              bundleContext.registerService( IPentahoObjectReference.class.getName(), reference,
                  referenceHashTable );
          registrations.add( serviceRegistration );
        }
      } catch ( ClassCastException e ) {
        logger.error( "Error Retriving object from OSGI, Class is not as expected", e );
      }
    }
    if ( existingRegistration != null ) {
      existingRegistration.setRegistrations( registrations );
      return existingRegistration;
    } else {
      return new OSGIPentahoObjectRegistration( registrations );
    }

  }

  @Override
  public <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference, Class<?>... classes ) {
    return this.registerReference( reference, null, classes );
  }

  @Override public boolean objectDefined( Class<?> clazz ) {
    if ( this.bundleContext == null || !osgiInitialized.get() ) {
      return super.objectDefined( clazz );
    }
    // Look for IPentahoObjectReference first
    try {
      Collection<ServiceReference<IPentahoObjectReference>> serviceReferences = this.bundleContext
          .getServiceReferences( IPentahoObjectReference.class,
              ( "(" + REFERENCE_CLASS + "=" + clazz.getName() + ")" ) );
      if ( serviceReferences != null && serviceReferences.size() > 0 ) {
        return true;
      }
    } catch ( IllegalStateException ise ) {
      // caused by the bundleContext being invalid
      return false;
    } catch ( InvalidSyntaxException e ) {
      throw new IllegalStateException( "Error finding reference in OSGI" );
    }
    // try by the classname
    return this.bundleContext.getServiceReference( clazz ) != null;
  }

  @Override
  protected <T> List<IPentahoObjectReference<?>> getReferencesByQuery( Class<T> type, Map<String, String> query ) {
    if ( this.bundleContext == null || !osgiInitialized.get() ) {
      return super.getReferencesByQuery( type, query );
    }
    return Collections.emptyList();
  }

  private class OSGIPentahoObjectRegistration implements IPentahoObjectRegistration {
    private List<ServiceRegistration<?>> registrations = new ArrayList<ServiceRegistration<?>>();
    private ObjectRegistration iPentahoObjectRegistration;

    public OSGIPentahoObjectRegistration(
        List<ServiceRegistration<?>> registrations ) {
      this.registrations.addAll( registrations );
    }

    public OSGIPentahoObjectRegistration( ObjectRegistration iPentahoObjectRegistration ) {

      this.iPentahoObjectRegistration = iPentahoObjectRegistration;
    }

    @Override public void remove() {
      if ( iPentahoObjectRegistration != null ) {
        iPentahoObjectRegistration.remove();
      }

      for ( ServiceRegistration<?> registration : registrations ) {
        try {
          registration.unregister();
        } catch ( IllegalStateException e ) {
          // May already have been unregistered during the shutdown sequence.
          logger.debug( "Error on Unregistering the service, it seems already be unregistered", e );
        }
      }

    }

    public void setRegistrations( List<ServiceRegistration<?>> registrations ) {
      this.registrations = registrations;
      this.iPentahoObjectRegistration = null;
    }
  }
}