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

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class JacksonMessageBodyBase {

  @Context
  private Providers contextProviders;

  private ObjectMapper mapper;

  protected boolean isSupported( Class<?> cls, Type type, Annotation[] annotations, MediaType mediaType ) {
    return mediaType.equals( MediaType.APPLICATION_JSON_TYPE ) && cls.getAnnotation( JsonRootName.class ) != null;
  }

  protected ObjectMapper getMapper( final Class<?> cls ) throws IllegalStateException {
    if ( mapper == null ) {
      final ContextResolver<ObjectMapper>
        contextResolver = contextProviders.getContextResolver( ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE );
      if ( contextResolver == null ) {
        throw new IllegalArgumentException();
      }
      mapper = contextResolver.getContext( cls );
    }

    return mapper;
  }
}
