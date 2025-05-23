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

package org.pentaho.platform.web.servlet.jersey;

import jakarta.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;

@Provider
@Singleton
public class JerseyAPIExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse( Exception exception ) {
        if ( exception instanceof WebApplicationException ) {
            final Response exResponse = ( ( WebApplicationException ) exception ).getResponse();
            final String entity = exResponse.getEntity() != null ? exResponse.getEntity().toString() : null;

            return Response.status( exResponse.getStatus() ).entity( entity ).type( exResponse.getMediaType() ).build();
        } else if ( exception instanceof AccessDeniedException ) {
            return Response.status( Response.Status.FORBIDDEN ).entity( exception.getMessage() ).build();
        } else if ( exception instanceof AuthenticationException ) {
            return Response.status( Response.Status.FORBIDDEN ).entity( exception.getMessage() ).build();
        } else if ( exception instanceof MappableException ) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail( HttpStatusCode.valueOf( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() ), exception.getMessage() );
            return Response.serverError().entity( ErrorResponse.builder( new ServletException(),  problemDetail ).build() ).build();
        } else if ( exception instanceof ContainerException ) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail( HttpStatusCode.valueOf( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() ), exception.getMessage() );
            return Response.serverError().entity( ErrorResponse.builder( new ServletException( exception ),  problemDetail ).build() ).build();
        } else {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail( HttpStatusCode.valueOf( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() ), exception.getMessage() );
            return Response.serverError().entity( ErrorResponse.builder( exception,  problemDetail ).build() ).build();
        }
    }
}
