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


import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.scheduler2.blockout.BlockoutAction;
import org.pentaho.platform.web.http.api.resources.proxies.BlockStatusProxy;

/**
 * @author wseyler
 *
 */

@org.codehaus.enunciate.XmlTransient
@Path("/scheduler/blockout")
public class BlockoutResource extends AbstractJaxRSResource {
  private static final Log logger = LogFactory.getLog(BlockoutResource.class);
  
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
    return manager.getBlockOutJobs();
  }

  @GET
  @Path("/hasblockouts")
  @Produces({ TEXT_PLAIN })
  public Response hasBlockouts() {
    List<Job> jobs = manager.getBlockOutJobs();
    return Response.ok((jobs != null && jobs.size() > 0 ) ? Boolean.TRUE.toString() : Boolean.FALSE.toString()).build();
  }
  
  @POST
  @Path("/add")
  @Consumes({ APPLICATION_JSON, APPLICATION_XML })
  public Response addBlockout(JobScheduleRequest request) throws IOException {
    request.setActionClass(BlockoutAction.class.getCanonicalName());
    request.getJobParameters().add(new JobScheduleParam(IBlockoutManager.DURATION_PARAM, request.getDuration()));
    request.getJobParameters().add(new JobScheduleParam(IBlockoutManager.TIME_ZONE_PARAM, request.getTimeZone()));
    updateStartDateForTimeZone(request);
    return schedulerResource.createJob(request);
  }

  @POST
  @Path("/update")
  @Consumes({ APPLICATION_JSON, APPLICATION_XML })
  public Response updateBlockout(@QueryParam("jobid")
  String jobId, JobScheduleRequest request) throws IOException {
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
  public BlockStatusProxy getBlockStatus(JobScheduleRequest request) throws UnifiedRepositoryException,
      SchedulerException {
    // Get blockout status
    Boolean totallyBlocked = false;
    Boolean partiallyBlocked = manager.isPartiallyBlocked(SchedulerResourceUtil.convertScheduleRequestToJobTrigger(
        request, scheduler));
    if (partiallyBlocked) {
      totallyBlocked = !manager.willFire(SchedulerResourceUtil.convertScheduleRequestToJobTrigger(request, scheduler));
    }
    return new BlockStatusProxy(totallyBlocked, partiallyBlocked);
  }

  private void updateStartDateForTimeZone(JobScheduleRequest request) {
    if (request.getSimpleJobTrigger() != null) {
      if (request.getSimpleJobTrigger().getStartTime() != null) {
        Date origStartDate = request.getSimpleJobTrigger().getStartTime();
        Date serverTimeZoneStartDate = convertDateToServerTimeZone(origStartDate, request.getTimeZone());
        request.getSimpleJobTrigger().setStartTime(serverTimeZoneStartDate);
      }
    } else if (request.getComplexJobTrigger() != null) {
      if (request.getComplexJobTrigger().getStartTime() != null) {
        Date origStartDate = request.getComplexJobTrigger().getStartTime();
        Date serverTimeZoneStartDate = convertDateToServerTimeZone(origStartDate, request.getTimeZone());
        request.getComplexJobTrigger().setStartTime(serverTimeZoneStartDate);
      }
    } else if (request.getCronJobTrigger() != null) {
      if (request.getCronJobTrigger().getStartTime() != null) {
        Date origStartDate = request.getCronJobTrigger().getStartTime();
        Date serverTimeZoneStartDate = convertDateToServerTimeZone(origStartDate, request.getTimeZone());
        request.getCronJobTrigger().setStartTime(serverTimeZoneStartDate);
      }
    }
  }
  
  public Date convertDateToServerTimeZone(Date dateTime, String timeZone) {
    Calendar userDefinedTime = Calendar.getInstance();
    userDefinedTime.setTime(dateTime);
    if(!TimeZone.getDefault().getID().equalsIgnoreCase(timeZone)) {
      logger.warn("original defined time: " + userDefinedTime.getTime().toString() + " on tz:" + timeZone);
      Calendar quartzStartDate = new GregorianCalendar(TimeZone.getTimeZone(timeZone));
      quartzStartDate.set(Calendar.YEAR, userDefinedTime.get(Calendar.YEAR));
      quartzStartDate.set(Calendar.MONTH, userDefinedTime.get(Calendar.MONTH));
      quartzStartDate.set(Calendar.DAY_OF_MONTH, userDefinedTime.get(Calendar.DAY_OF_MONTH));
      quartzStartDate.set(Calendar.HOUR_OF_DAY, userDefinedTime.get(Calendar.HOUR_OF_DAY));
      quartzStartDate.set(Calendar.MINUTE, userDefinedTime.get(Calendar.MINUTE));
      quartzStartDate.set(Calendar.SECOND, userDefinedTime.get(Calendar.SECOND));
      quartzStartDate.set(Calendar.MILLISECOND, userDefinedTime.get(Calendar.MILLISECOND));
      logger.warn("adapted time for " + TimeZone.getDefault().getID() + ": " + quartzStartDate.getTime().toString());
      return quartzStartDate.getTime();
    } else {
      return dateTime;
    }
  }
}
