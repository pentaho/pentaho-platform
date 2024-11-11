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


package org.pentaho.platform.web.servlet.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes( MediaType.APPLICATION_JSON )
public class JacksonMessageBodyReader extends JacksonMessageBodyBase implements MessageBodyReader<Object> {
  @Override public boolean isReadable( Class<?> cls, Type type, Annotation[] annotations, MediaType mediaType ) {
    return isSupported( cls, type, annotations, mediaType );
  }

  @Override public Object readFrom( Class<Object> cls, Type type, Annotation[] annotations, MediaType mediaType,
                                    MultivaluedMap<String, String> multivaluedMap, InputStream inputStream )
    throws IOException, WebApplicationException {
    return getMapper( cls ).readValue( inputStream, cls );
  }
}
