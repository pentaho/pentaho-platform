/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.action.jfreereport.helper;

import org.pentaho.reporting.libraries.resourceloader.ResourceData;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoader;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.loader.LoaderUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is implemented to support loading solution files from the pentaho repository into JFreeReport
 * 
 * @author Will Gorman
 */
public class PentahoResourceLoader implements ResourceLoader {

  public static final String SOLUTION_SCHEMA_NAME = "solution"; //$NON-NLS-1$

  public static final String HTTP_URL_PREFIX = "http://"; //$NON-NLS-1$

  public static final String SCHEMA_SEPARATOR = ":/"; //$NON-NLS-1$

  public static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

  public static final String WIN_PATH_SEPARATOR = "\\"; //$NON-NLS-1$

  /** keep track of the resource manager */
  private ResourceManager manager;

  /**
   * default constructor
   */
  public PentahoResourceLoader() {
  }

  /**
   * set the resource manager
   * 
   * @param manager
   *          resource manager
   */
  public void setResourceManager( final ResourceManager manager ) {
    this.manager = manager;
  }

  /**
   * get the resource manager
   * 
   * @return resource manager
   */
  public ResourceManager getManager() {
    return manager;
  }

  /**
   * get the schema name, in this case it's always "solution"
   * 
   * @return the schema name
   */
  public String getSchema() {
    return PentahoResourceLoader.SOLUTION_SCHEMA_NAME;
  }

  /**
   * create a resource data object
   * 
   * @param key
   *          resource key
   * @return resource data
   * @throws ResourceLoadingException
   */
  public ResourceData load( final ResourceKey key ) throws ResourceLoadingException {
    return new PentahoResourceData( key );
  }

  /**
   * see if the pentaho resource loader can support the content key path
   * 
   * @param values
   *          map of values to look in
   * @return true if class supports the content key.
   */
  public boolean isSupportedKey( final ResourceKey key ) {
    if ( key.getSchema().equals( getSchema() ) ) {
      return true;
    }
    return false;
  }

  /**
   * create a new key based on the values provided
   * 
   * @param values
   *          map of values
   * @return new resource key
   * @throws ResourceKeyCreationException
   */
  public ResourceKey createKey( final Object value, final Map factoryKeys ) throws ResourceKeyCreationException {
    if ( value instanceof String ) {
      String valueString = (String) value;
      if ( valueString.startsWith( getSchema() + PentahoResourceLoader.SCHEMA_SEPARATOR ) ) {
        valueString = valueString.replace( '\\', '/' );
        String path = valueString.substring( getSchema().length() + PentahoResourceLoader.SCHEMA_SEPARATOR.length() );
        return new ResourceKey( getSchema(), path, factoryKeys );
      }
    }
    return null;
  }

  /**
   * derive a key from an existing key, used when a relative path is given.
   * 
   * @param parent
   *          the parent key
   * @param data
   *          the new data to be keyed
   * @return derived key
   * @throws ResourceKeyCreationException
   */
  public ResourceKey deriveKey( final ResourceKey parent, final String path, final Map data )
    throws ResourceKeyCreationException {
    if ( isSupportedKey( parent ) == false ) {
      throw new ResourceKeyCreationException( "Assertation: Unsupported parent key type" ); //$NON-NLS-1$
    }

    String resource;
    if ( path.startsWith( "solution://" ) ) {
      resource = path;
    } else if ( path.startsWith( "/" ) ) {
      resource = "solution:/" + path; //$NON-NLS-1$
    } else {
      resource = LoaderUtils.mergePaths( (String) parent.getIdentifier(), path );
      // Currently LoaderUtils is stripping off the leading slash from the file path
      // Previously with the DB based repository we never had a slash before
      // pentaho-solutions. But with the JCR repository all the path starts with a slash
      // if it is being referenced from the root tennant.
      // For this reason we will check if there is no slash in the beginning then we will
      // add the slash. We do need to make sure that the resource is not a url
      if ( !resource.startsWith( HTTP_URL_PREFIX ) && !resource.startsWith( PATH_SEPARATOR ) ) {
        resource = PATH_SEPARATOR + resource;
      }
    }

    final Map map;
    if ( data != null ) {
      map = new HashMap();
      map.putAll( parent.getFactoryParameters() );
      map.putAll( data );
    } else {
      map = parent.getFactoryParameters();
    }

    return new ResourceKey( parent.getSchema(), resource, map );
  }

  public URL toURL( final ResourceKey key ) {
    // not supported ..
    return null;
  }

  public ResourceKey deserialize( ResourceKey bundleKey, String stringKey ) throws ResourceKeyCreationException {
    return null;
  }

  public boolean isSupportedDeserializer( String data ) {
    return false;
  }

  public String serialize( ResourceKey resourceKey, ResourceKey key ) throws ResourceException {
    return null;
  }

}
