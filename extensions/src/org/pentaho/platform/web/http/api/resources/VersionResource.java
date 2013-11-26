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
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.VersionHelper;
import org.pentaho.platform.util.VersionInfo;
import org.pentaho.platform.util.versionchecker.PentahoVersionCheckReflectHelper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * This resource manages version checking capability of the platform
 * 
 *
 */
@Path( "/version" )
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
  public Response getVersion() {
    VersionInfo versionInfo = VersionHelper.getVersionInfo( PentahoSystem.class );
    return Response.ok( versionInfo.getVersionNumber() ).type( MediaType.TEXT_PLAIN ).build();
  }

  /**
   * Return software update document to the user
   * 
   * @return software update document
   */
  @GET
  @Path( "/softwareUpdates" )
  @Produces( TEXT_PLAIN )
  public String getSoftwareUpdatesDocument() {
    if ( PentahoVersionCheckReflectHelper.isVersionCheckerAvailable() ) {
      @SuppressWarnings( "rawtypes" )
      List results = PentahoVersionCheckReflectHelper.performVersionCheck( false, -1 );
      return PentahoVersionCheckReflectHelper.logVersionCheck( results, logger );
    }
    return "<vercheck><error><[!CDATA[Version Checker is disabled]]></error></vercheck>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

}
