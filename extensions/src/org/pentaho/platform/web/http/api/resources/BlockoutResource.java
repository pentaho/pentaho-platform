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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IBlockoutTrigger;
import org.quartz.SchedulerException;
import org.quartz.Trigger;


/**
 * @author wseyler
 *
 */
@Path("/scheduler/blockout")
public class BlockoutResource extends AbstractJaxRSResource {
  IBlockoutManager manager;
  
  @GET
  @Path("/list")
  @Produces({ APPLICATION_JSON, APPLICATION_XML })
  public List<IBlockoutTrigger> getBlockouts() {
    List<IBlockoutTrigger> blockouts = new ArrayList<IBlockoutTrigger>();
    try {
      IBlockoutTrigger[] blockoutsArray = manager.getBlockouts();
      for (IBlockoutTrigger blockout : blockoutsArray) {
        blockouts.add(blockout);
      }
      
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
    return blockouts;
  }
  
  @GET
  @Path("/get")
  @Produces({ APPLICATION_JSON, APPLICATION_XML })
  public IBlockoutTrigger getBlockout(@QueryParam("blockoutName") String blockoutName) {
    try {
      return manager.getBlockout(blockoutName);
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }
  
  @PUT
  @Path("/add")
  @Consumes({ APPLICATION_JSON, APPLICATION_XML })
  @Produces({TEXT_PLAIN})
  public Response addBlockout(IBlockoutTrigger trigger) {
    try {
      manager.addBlockout(trigger);
      return Response.ok().build();
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }
  
  @DELETE
  @Path("/delete")
  public Response deleteBlockout(@QueryParam("blockoutName") String blockoutName) {
    boolean success = true;
    try {
      success = manager.deleteBlockout(blockoutName);
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
    if (success) {
      return Response.status(Status.OK).build();
    } else {
      return Response.status(Status.NOT_MODIFIED).build();
    }
  }
  
  @POST
  @Path("/update")
  @Consumes({ APPLICATION_JSON, APPLICATION_XML })
  @Produces({TEXT_PLAIN})
  public Response updateBlockout(@QueryParam("blockoutName") String blockoutName, IBlockoutTrigger updateBlockout) {
    try {
      manager.updateBlockout(blockoutName, updateBlockout);
      return Response.ok().build();
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }
  
  @GET
  @Path("/willFire")
  @Consumes({ APPLICATION_JSON, APPLICATION_XML })
  @Produces({TEXT_PLAIN})
  public Response willFire(Trigger trigger) {
    Integer fireTimes = manager.willFire(trigger);
    return Response.ok(fireTimes).build();
  }

  @GET
  @Path("/shouldFireNow")
  @Produces({TEXT_PLAIN})
  public Response shouldFireNow() {
    Boolean result = manager.shouldFireNow();
    return Response.ok(result.toString()).build();
  }
  
  @GET
  @Path("/willBlock")
  @Consumes({ APPLICATION_JSON, APPLICATION_XML })
  @Produces({TEXT_PLAIN})
  public Response willBlock(IBlockoutTrigger blockoutTrigger) {
    Boolean result = manager.willBlockSchedules(blockoutTrigger);
    return Response.ok(result.toString()).build();
  }
}
