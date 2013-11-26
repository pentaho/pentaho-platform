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

package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
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
