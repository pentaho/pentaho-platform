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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created 1/14/2011
 * @author Aaron Phillips
 *
 */
package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FilenameUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

/**
 * Represents a file node in the repository.  This api provides methods for discovering information
 * about repository files as well as CRUD operations
 * @author aaron
 *
 */
@Path("/scheduler")
public class SchedulerResource extends AbstractJaxRSResource {

  protected IScheduler scheduler = PentahoSystem.get(IScheduler.class, null); //$NON-NLS-1$
  protected IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
  IPluginManager pluginMgr = PentahoSystem.get(IPluginManager.class);
  Random random = new Random(new Date().getTime());

  public SchedulerResource() {
  }

  /////////
  // CREATE

  @POST
  @Path("/job")
  @Consumes( { APPLICATION_XML, APPLICATION_JSON })
  @Produces("text/plain")
  public Response createJob(SimpleJobScheduleRequest scheduleRequest) throws IOException {
    if (scheduleRequest.getJobTrigger().getStartTime() == null) {
      scheduleRequest.getJobTrigger().setStartTime(new Date());
    }
    
    final RepositoryFile file = repository.getFile(scheduleRequest.getInputFile());
    if (file == null) {
      return Response.status(NOT_FOUND).build();
    }
    
    Job job= null;
    try {
      IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
      pentahoSession.getName();
      HashMap<String, Serializable> parameterMap = new HashMap<String, Serializable>();
      String outputFile = ClientRepositoryPaths.getUserHomeFolderPath(pentahoSession.getName()) + "/workspace/" + FilenameUtils.getBaseName(scheduleRequest.getInputFile()) + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
      String actionId = FilenameUtils.getExtension(scheduleRequest.getInputFile()); //$NON-NLS-1$ //$NON-NLS-2$
      job = scheduler.createJob(Integer.toString(Math.abs(random.nextInt())), actionId, parameterMap, scheduleRequest.getJobTrigger(), new RepositoryFileStreamProvider(scheduleRequest.getInputFile(), outputFile));
    } catch (SchedulerException e) {
      return Response.serverError().entity(e.toString()).build();
    }
    return Response.ok(job.getJobId()).type(MediaType.TEXT_PLAIN).build();
  }
  
}
