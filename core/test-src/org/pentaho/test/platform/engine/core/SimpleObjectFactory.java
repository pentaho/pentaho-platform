/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 3 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2005 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.test.platform.engine.core;

import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings( "nls" )
/**
 * A very simplistic implementation of IPentahoObjectFactory which allows
 * the user to programmatically define platform objects for later instancing.
 * @author aphillips
 */
public class SimpleObjectFactory implements IPentahoObjectFactory {

  private HashMap<String, String> classnamesMap = new HashMap<String, String>();

  public <T> T get( Class<T> interfaceClass, IPentahoSession session ) throws ObjectFactoryException {
    return get( interfaceClass, interfaceClass.getSimpleName(), session );
  }

  @SuppressWarnings( "unchecked" )
  public <T> T get( Class<T> interfaceClass, String key, IPentahoSession session ) throws ObjectFactoryException {
    String classname = classnamesMap.get( key );
    try {
      Class implClass = Class.forName( classname );
      T t = (T) implClass.newInstance();
      if ( t instanceof IPentahoInitializer ) {
        ( (IPentahoInitializer) t ).init( session );
      }
      return t;
    } catch ( Throwable th ) {
      throw new ObjectFactoryException( "Could not create instance for class " + classname, th );
    }
  }

  public Class<?> getImplementingClass( String key ) {
    try {
      return Class.forName( classnamesMap.get( key ) );
    } catch ( ClassNotFoundException e ) {
      e.printStackTrace();
      return null;
    }
  }

  public void init( String configFile, Object context ) {
  }

  public boolean objectDefined( String key ) {
    return classnamesMap.containsKey( key );
  }

  /**
   * Register an object for creation.
   * 
   * @param key
   *          identifying string
   * @param classname
   *          must be a fully qualified class name for a top-level or nested top-level (static inner) class
   */
  public void defineObject( String key, String classname ) {
    classnamesMap.put( key, classname );
  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession curSession ) throws ObjectFactoryException {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> clazz, IPentahoSession curSession ) {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public <T> T get( Class<T> interfaceClass, IPentahoSession session, Map<String, String> properties )
    throws ObjectFactoryException {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean objectDefined( Class<?> clazz ) {
    return false; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> interfaceClass, IPentahoSession curSession,
      Map<String, String> properties ) {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public <T> List<IPentahoObjectReference<T>> getObjectReferences( Class<T> interfaceClass,
                                                                   IPentahoSession curSession ) {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public <T> List<IPentahoObjectReference<T>> getObjectReferences( Class<T> interfaceClass, IPentahoSession curSession,
      Map<String, String> properties ) {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession curSession, Map<String, String> properties )
    throws ObjectFactoryException {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getName() {
    return "Simple Object Factory";
  }
}
