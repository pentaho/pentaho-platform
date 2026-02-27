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


package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces( { "application/octet-stream", "*/*" } )
public class GeneratorStreamingOutputProvider implements MessageBodyWriter<GeneratorStreamingOutput> {

  private static final Log logger = LogFactory.getLog( GeneratorStreamingOutputProvider.class );

  public boolean isWriteable( Class<?> t, Type gt, Annotation[] as, MediaType mediaType ) {
    return GeneratorStreamingOutput.class.isAssignableFrom( t );
  }

  public long getSize( GeneratorStreamingOutput o, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType ) {
    return -1;
  }

  public abstract class MimeTypeCallback {
    public abstract void setMimeType( String mimeType );
  }

  public void writeTo( GeneratorStreamingOutput o, Class<?> t, Type genericType, Annotation[] as, MediaType mediaType,
      final MultivaluedMap<String, Object> httpHeaders, OutputStream entity ) throws IOException {
    o.write( entity, new MimeTypeCallback() {

      @Override
      public void setMimeType( String mimeType ) {
        // this has to happen *prior* to the content actually being written to the http response output stream
        logger.debug( "Setting Content-Type HTTP response header to " + mimeType ); //$NON-NLS-1$
        httpHeaders.putSingle( "Content-Type", mimeType ); //$NON-NLS-1$
      }

    } );
  }
}
