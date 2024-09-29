/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.web.http.api.resources;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import java.lang.reflect.Field;
import java.util.ArrayList;

@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {

  private JAXBContext context;
  @SuppressWarnings( "rawtypes" )
  private final ArrayList<Class> types = new ArrayList<Class>();
  private final ArrayList<String> arrays = new ArrayList<String>();
  private final Logger logger = LoggerFactory.getLogger( getClass() );

  public JAXBContextResolver() throws Exception {
    types.add( ArrayList.class );
    types.add( JaxbList.class );
    types.add( Setting.class );
    arrays.add( "list" );
    arrays.add( "values" );
    arrays.add( "setting" );
    JSONConfiguration config =
        JSONConfiguration.mapped().rootUnwrapping( true ).arrays( arrays.toArray( new String[] {} ) ).build();
    context = new JSONJAXBContext( config, types.toArray( new Class[] {} ) );
  }

  public JAXBContext getContext( Class<?> objectType ) {
    synchronized ( types ) {
      if ( types.contains( objectType ) ) {
        return context;
      }
    }

    // need to see if class has any ArrayList types, if so, add those to arrays
    Field[] fields = objectType.getDeclaredFields();
    for ( int i = 0; i < fields.length; i++ ) {
      if ( fields[i].getType().isAssignableFrom( ArrayList.class ) ) {
        String simpleName = fields[i].getName();
        simpleName = simpleName.substring( 0, 1 ).toLowerCase() + simpleName.substring( 1 );
        arrays.add( simpleName );
      }
    }

    String simpleName = objectType.getSimpleName();
    simpleName = simpleName.substring( 0, 1 ).toLowerCase() + simpleName.substring( 1 );
    arrays.add( simpleName );
    try {
      JSONConfiguration config =
          JSONConfiguration.mapped().rootUnwrapping( true ).arrays( arrays.toArray( new String[arrays.size()] ) )
              .build();

      synchronized ( types ) {
        types.add( objectType );
        context = new JSONJAXBContext( config, types.toArray( new Class[types.size()] ) );
      }
      return context;
    } catch ( JAXBException e ) {
      logger.error( "Error creating JAXBContext for class " + objectType, e );
    }
    return null;
  }

}
