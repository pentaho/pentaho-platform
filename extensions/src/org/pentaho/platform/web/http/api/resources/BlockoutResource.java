/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 * 
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.platform.web.http.api.resources;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.proxies.BlockStatusProxy;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

/**
 * This resource manages  blockout shedules in the platform
 *
 * @author wseyler
 */

@org.codehaus.enunciate.XmlTransient
@Path( "/scheduler/blockout" )
public class BlockoutResource extends AbstractJaxRSResource {
  private SchedulerResource schedulerResource = null;

  protected IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );

  public BlockoutResource() {
    super();
    schedulerResource = new SchedulerResource();
  }

  /**
   * Retrieves all blockout jobs in the system
   *
   * @return list of <code> Job </code>
   */
  @GET
  @Path( "/blockoutjobs" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public List<Job> getJobs() {
    return (List<Job>) schedulerResource.getJobs().getEntity();
  }

  /**
   * Determines whether there are any blockouts in the system
   *
   * @return true if the system has any blockouts
   */
  @GET
  @Path( "/hasblockouts" )
  @Produces( { TEXT_PLAIN } )
  public Response hasBlockouts() {
    return schedulerResource.hasBlockouts();
  }

  /**
   * Creates a new blockout schedule
   *
   * @param request <code> JobScheduleRequest </code>
   * @return
   * @throws IOException
   */
  @POST
  @Path( "/add" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  public Response addBlockout( JobScheduleRequest request ) throws IOException {
    return schedulerResource.addBlockout( request );
  }

  /**
   * Updates a selected blockout schedule
   *
   * @param jobId   (ID of the blockout schedule to be updated)
   * @param request <code> JobScheduleRequest </code>
   * @return
   * @throws IOException
   */
  @POST
  @Path( "/update" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  public Response updateBlockout( @QueryParam( "jobid" ) String jobId, JobScheduleRequest request ) throws IOException {
    return schedulerResource.updateBlockout( jobId, request );
  }

  /**
   * Checks if the selected blockout schedule will be fired
   *
   * @param request <code> JobScheduleRequest </code>
   * @return
   */
  @GET
  @Path( "/willFire" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  @Produces( { TEXT_PLAIN } )
  public Response willFire( JobScheduleRequest request ) {
    return schedulerResource.blockoutWillFire( request );
  }

  /**
   * Checks if the selected blockout schedule should be fired now
   *
   * @return
   */
  @GET
  @Path( "/shouldFireNow" )
  @Produces( { TEXT_PLAIN } )
  public Response shouldFireNow() {
    return schedulerResource.shouldFireNow();
  }

  /**
   * Check the status of the selected blockout schedule. The status will display whether it is completely blocked or
   * partially blocked
   *
   * @param request
   * @return <code> BlockStatusProxy </code>
   * @throws UnifiedRepositoryException
   * @throws SchedulerException
   */
  @POST
  @Path( "/blockstatus" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public BlockStatusProxy getBlockStatus( JobScheduleRequest request ) throws UnifiedRepositoryException,
    SchedulerException {
    return (BlockStatusProxy) schedulerResource.getBlockStatus( request ).getEntity();
  }

}
