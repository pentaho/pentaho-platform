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

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;
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
