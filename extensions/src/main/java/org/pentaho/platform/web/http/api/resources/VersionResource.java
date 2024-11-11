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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.Facet;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.VersionHelper;
import org.pentaho.platform.util.VersionInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * This resource manages version checking capability of the platform
 * 
 *
 */
@Path( "/version" )
@Facet( name = "Unsupported" )
public class VersionResource extends AbstractJaxRSResource {

  protected static final Log logger = LogFactory.getLog( VersionResource.class );

  /**
   * Returns the current version of the platform
   * 
   * @return platform's version
   */
  @GET
  @Path( "/show" )
  @Produces( TEXT_PLAIN )
  @Facet ( name = "Unsupported" )
  public Response getVersion() {
    VersionInfo versionInfo = VersionHelper.getVersionInfo( PentahoSystem.class );
    return Response.ok( versionInfo.getVersionNumber() ).type( MediaType.TEXT_PLAIN ).build();
  }
}
