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

import org.codehaus.enunciate.Facet;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Path( "" )
@Facet ( name = "Unsupported" )
public class RootResource extends AbstractJaxRSResource {

  @GET
  public Response doGetRoot() throws URISyntaxException {
    return doGetDocs();
  }

  @GET
  @Path( "docs" )
  @Facet( name = "Unsupported" )
  public Response doGetDocs() throws URISyntaxException {
    String fqurl = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
    URI uri = new URI( fqurl + "docs/InformationMap.jsp" ); //$NON-NLS-1$
    return Response.temporaryRedirect( uri ).build();
  }

}
