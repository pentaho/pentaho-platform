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
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.authorization.caching.IAuthorizationDecisionCache;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

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

  @GET
  @Path( "/authorizationDecisionCache" )
  @Produces( { MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public Response flushAuthorizationDecisionCache() {
    if ( canAdminister() ) {
      IAuthorizationDecisionCache decisionCache = PentahoSystem.get( IAuthorizationDecisionCache.class );
      if ( decisionCache != null ) {
        decisionCache.invalidateAll();
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
