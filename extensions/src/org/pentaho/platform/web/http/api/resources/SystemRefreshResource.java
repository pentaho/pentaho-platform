/*
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
 * Copyright 2012 Pentaho Corporation.  All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;

@Path("/system/refresh")
public class SystemRefreshResource extends AbstractJaxRSResource {

  @GET
  @Path("/globalActions")
  @Produces(TEXT_PLAIN)
  public Response executeGlobalActions() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    if (SecurityHelper.getInstance().isPentahoAdministrator(pentahoSession)) {
      PentahoSystem.publish(pentahoSession, org.pentaho.platform.engine.core.system.GlobalListsPublisher.class.getName());
    }
    return Response.ok().type(MediaType.TEXT_PLAIN).build();
  }

  @GET
  @Path("/metadata")
  @Produces(TEXT_PLAIN)
  public String refreshMetadata() {
    String result = null;
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    if (SecurityHelper.getInstance().isPentahoAdministrator(pentahoSession)) {
      result = PentahoSystem.publish(pentahoSession, org.pentaho.platform.engine.services.metadata.MetadataPublisher.class.getName());
    }
    return result;
  }

  @GET
  @Path("/systemSettings")
  @Produces(TEXT_PLAIN)
  public Response refreshSystemSettings() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    if (SecurityHelper.getInstance().isPentahoAdministrator(pentahoSession)) {
      PentahoSystem.publish(pentahoSession, org.pentaho.platform.engine.core.system.SettingsPublisher.class.getName());
    }
    return Response.ok().type(MediaType.TEXT_PLAIN).build();
  }

  @GET
  @Path("/repository")
  @Produces(TEXT_PLAIN)
  public Response refreshRepository() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    if (SecurityHelper.getInstance().isPentahoAdministrator(pentahoSession)) {
      PentahoSystem.get(ISolutionRepository.class, pentahoSession).reloadSolutionRepository(pentahoSession, pentahoSession.getLoggingLevel());
    }
    return Response.ok().type(MediaType.TEXT_PLAIN).build();
  }

  @GET
  @Path("/mondrianSchemaCache")
  @Produces(TEXT_PLAIN)
  public Response flushMondrianSchemaCache() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    if (SecurityHelper.getInstance().isPentahoAdministrator(pentahoSession)) {
      IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService", pentahoSession); //$NON-NLS-1$
      mondrianCatalogService.reInit(pentahoSession);
    }
    return Response.ok().type(MediaType.TEXT_PLAIN).build();
  }

  @GET
  @Path("/reportingDataCache")
  @Produces(TEXT_PLAIN)
  public Response purgeReportingDataCache() {
    ICacheManager cacheManager = PentahoSystem.get(ICacheManager.class);
    cacheManager.clearRegionCache("report-dataset-cache");
    cacheManager.clearRegionCache("report-output-handlers");
    return Response.ok().type(MediaType.TEXT_PLAIN).build();
  }

}
