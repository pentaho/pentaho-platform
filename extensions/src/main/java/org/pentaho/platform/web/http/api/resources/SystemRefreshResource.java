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

package org.pentaho.platform.web.http.api.resources;

import org.codehaus.enunciate.Facet;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * This resource is responsible for refreshing  different system components (metadata, mondrian etc.)
 *
 * @author rmansoor
 *
 */
@Path( "/system/refresh" )
@Facet( name = "Unsupported" )
public class SystemRefreshResource extends AbstractJaxRSResource {

  /**
   *
   *
   * @return
   */
  @GET
  @Path( "/globalActions" )
  @Facet ( name = "Unsupported" )
  @Produces( TEXT_PLAIN )
  public Response executeGlobalActions() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    if ( canAdminister() ) {
      PentahoSystem.publish( pentahoSession, org.pentaho.platform.engine.core.system.GlobalListsPublisher.class
        .getName() );
    }
    return Response.ok().type( MediaType.TEXT_PLAIN ).build();
  }

  @GET
  @Path( "/metadata" )
  @Facet ( name = "Unsupported" )
  @Produces( TEXT_PLAIN )
  public String refreshMetadata() {
    String result = null;
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    if ( canAdminister() ) {
      result =
          PentahoSystem.publish( pentahoSession, org.pentaho.platform.engine.services.metadata.MetadataPublisher.class
            .getName() );
    }
    return result;
  }

  @GET
  @Path( "/systemSettings" )
  @Facet ( name = "Unsupported" )
  @Produces( TEXT_PLAIN )
  public Response refreshSystemSettings() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    if ( canAdminister() ) {
      PentahoSystem.publish( pentahoSession, org.pentaho.platform.engine.core.system
        .SettingsPublisher.class.getName() );
    }
    return Response.ok().type( MediaType.TEXT_PLAIN ).build();
  }

  @GET
  @Path( "/mondrianSchemaCache" )
  @Produces( { MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public Response flushMondrianSchemaCache() {
    if ( canAdminister() ) {
      IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
      if ( canAdminister() ) {
        // Flush the catalog helper (legacy)
        IMondrianCatalogService mondrianCatalogService =
            PentahoSystem.get( IMondrianCatalogService.class, "IMondrianCatalogService", pentahoSession ); //$NON-NLS-1$
        mondrianCatalogService.reInit( pentahoSession );
        // Flush the IOlapService
        IOlapService olapService =
          PentahoSystem.get( IOlapService.class, "IOlapService", pentahoSession ); //$NON-NLS-1$
        olapService.flushAll( pentahoSession );
      }
      return Response.ok().type( MediaType.TEXT_PLAIN ).build();
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  @GET
  @Path( "/mondrianSingleSchemaCache" )
  @Produces( { MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public Response flushMondrianSchemaCache( @QueryParam( "name" ) String name ) {
    if ( canAdminister() ) {
      IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
      if ( canAdminister() ) {
        IOlapService olapService =
          PentahoSystem.get( IOlapService.class, "IOlapService", pentahoSession ); //$NON-NLS-1$
        olapService.flush( pentahoSession, name );
      }
      return Response.ok().type( MediaType.TEXT_PLAIN ).build();
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * @deprecated use org.pentaho.reporting.platform.plugin.CacheManagerEndpoint instead
   */
  @Deprecated
  @GET
  @Path( "/reportingDataCache" )
  @Produces( { MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public Response purgeReportingDataCache() {
    if ( canAdminister() ) {
      ICacheManager cacheManager = PentahoSystem.get( ICacheManager.class );
      cacheManager.clearRegionCache( "report-dataset-cache" );
      cacheManager.clearRegionCache( "report-output-handlers" );

      Runnable clearCacheAction =
        PentahoSystem.get( Runnable.class, "_ClearCacheAction", PentahoSessionHolder.getSession() );
      if ( clearCacheAction != null ) {
        clearCacheAction.run();
      }

      return Response.ok().type( MediaType.TEXT_PLAIN ).build();
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  private boolean canAdminister() {
    return SystemUtils.canAdminister();
  }
}
