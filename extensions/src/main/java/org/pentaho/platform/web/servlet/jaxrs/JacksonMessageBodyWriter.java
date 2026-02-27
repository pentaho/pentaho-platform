/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.web.servlet.jaxrs;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces( MediaType.APPLICATION_JSON )
public class JacksonMessageBodyWriter extends JacksonMessageBodyBase implements MessageBodyWriter<Object> {

  @Override
  public boolean isWriteable( final Class<?> cls, Type type, Annotation[] annotations, MediaType mediaType ) {
    return isSupported( cls, type, annotations, mediaType );
  }

  @Override
  public void writeTo( Object obj, Class<?> cls, Type type, Annotation[] annotations, MediaType mediaType,
                                 MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream )
    throws IOException, WebApplicationException {
    getMapper( cls ).writeValue( outputStream, obj );
  }

  @Override
  public long getSize( Object o, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType ) {
    // This method is deprecated an ignored by JAX-RS, return a dummy value
    //
    return 0;
  }
}
