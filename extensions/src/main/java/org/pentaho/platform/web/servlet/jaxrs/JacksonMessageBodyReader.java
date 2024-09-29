/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
