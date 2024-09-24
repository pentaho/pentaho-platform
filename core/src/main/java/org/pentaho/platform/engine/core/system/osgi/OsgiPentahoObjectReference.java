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

package org.pentaho.platform.engine.core.system.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nbaker on 4/27/15.
 */
public class OsgiPentahoObjectReference<T> implements IPentahoObjectReference<T> {
  private Class<T> type;
  private ServiceReference osgiRef;
  private Map<String, Object> attributes;
  private BundleContext bundleContext;

  public OsgiPentahoObjectReference( BundleContext bundleContext, Class<T> type, ServiceReference ref ) {
    this.bundleContext = bundleContext;
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
    Object service = bundleContext.getService( osgiRef );

    if ( service instanceof IPentahoInitializer ) {
      ( (IPentahoInitializer) service ).init( PentahoSessionHolder.getSession() );
    }
    return (T) service;
  }

  @Override
  public Integer getRanking() {
    Object property = osgiRef.getProperty( "service.ranking" );
    if( property == null ){
      return 0;
    }
    try{
      return Integer.parseInt( property.toString() );
    } catch ( NumberFormatException e ){
      return 0;
    }
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
