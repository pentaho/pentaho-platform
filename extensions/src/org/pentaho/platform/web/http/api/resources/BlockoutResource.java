/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Mar 18, 2013 
 * @author wseyler
 */

package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.web.http.api.resources.proxies.BlockStatusProxy;

/**
 * @author wseyler
 *
 */
@Path("/scheduler/blockout")
public class BlockoutResource extends AbstractJaxRSResource {

  private IBlockoutManager manager = null;
  private SchedulerResource schedulerResource = null;
  private IScheduler scheduler = null;
  
  protected IAuthorizationPolicy policy = PentahoSystem.get(IAuthorizationPolicy.class);

  public BlockoutResource() {
    super();
    manager = PentahoSystem.get(IBlockoutManager.class, "IBlockoutManager", null); //$NON-NLS-1$;
    scheduler = PentahoSystem.get(IScheduler.class, "IScheduler2", null); //$NON-NLS-1$
    schedulerResource = new SchedulerResource();
  }

  @GET
  @Path("/blockoutjobs")
  @Produces({ APPLICATION_JSON, APPLICATION_XML })
  public List<Job> getJobs() {
    IPentahoSession session = PentahoSessionHolder.getSession();
    return manager.getBlockOutJobs(canAdminister(session));
  }

  @POST
  @Path("/add")
  @Consumes({ APPLICATION_JSON, APPLICATION_XML })
  public Response addBlockout(JobScheduleRequest request) throws IOException {
    request.setActionClass("org.pentaho.platform.scheduler2.blockout.BlockoutAction");
    return schedulerResource.createJob(request);   
  }

  @POST
  @Path("/update/{jobid}")
  @Consumes({ APPLICATION_JSON, APPLICATION_XML })
  public Response updateBlockout(@QueryParam("jobid")String jobId, JobScheduleRequest request) throws IOException {
    JobRequest jobRequest = new JobRequest();
    jobRequest.setJobId(jobId);
    Response response = schedulerResource.removeJob(jobRequest);
    if (response.getStatus() == 200) {
      response = addBlockout(request);
    } 
    return response;    
  }
  
  @GET
  @Path("/willFire")
  @Consumes({ APPLICATION_JSON, APPLICATION_XML })
  @Produces({ TEXT_PLAIN })
  public Response willFire(JobScheduleRequest request) {
    Boolean willFire;
    try {
      willFire = manager.willFire(SchedulerResourceUtil.convertScheduleRequestToJobTrigger(request, scheduler));
    } catch (UnifiedRepositoryException e) {
      return Response.serverError().entity(e).build();
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      return Response.serverError().entity(e).build();
    }
    return Response.ok(willFire.toString()).build();
  }

  @GET
  @Path("/shouldFireNow")
  @Produces({ TEXT_PLAIN })
  public Response shouldFireNow() {
      Boolean result = manager.shouldFireNow();
      return Response.ok(result.toString()).build();
  }

  @POST
  @Path("/blockstatus")
  @Consumes({ APPLICATION_JSON, APPLICATION_XML })
  @Produces({ APPLICATION_JSON, APPLICATION_XML })
  public BlockStatusProxy getBlockStatus(JobScheduleRequest request) throws UnifiedRepositoryException, SchedulerException {
    // Get blockout status
    Boolean totallyBlocked = false;
    Boolean partiallyBlocked = manager.isPartiallyBlocked(SchedulerResourceUtil.convertScheduleRequestToJobTrigger(request, scheduler));
    if (partiallyBlocked) {
      totallyBlocked = !manager.willFire(SchedulerResourceUtil.convertScheduleRequestToJobTrigger(request, scheduler));
    }
    return new BlockStatusProxy(totallyBlocked, partiallyBlocked);
  }

  private Boolean canAdminister(IPentahoSession session) {
    if (policy.isAllowed(AdministerSecurityAction.NAME)) {
      return true;
    }
    return false;
  }

}
