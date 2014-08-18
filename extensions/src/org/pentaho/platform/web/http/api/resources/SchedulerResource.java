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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.Job.JobState;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.scheduler2.blockout.BlockoutAction;
import org.pentaho.platform.web.http.api.resources.services.SchedulerService;
import org.pentaho.platform.web.http.messages.Messages;

/**
 * Represents a file node in the repository. This api provides methods for discovering information about repository
 * files as well as CRUD operations
 *
 * @author aaron
 */
@Path( "/scheduler" )
public class SchedulerResource extends AbstractJaxRSResource {

  private SchedulerService schedulerService;

  protected static final Log logger = LogFactory.getLog( SchedulerResource.class );

  public SchedulerResource() {
    schedulerService = new SchedulerService();
  }


  /**
   * Creates a new Job/Schedule
   *
   * <p>Example Request:<br />
   *  PUT api/scheduler/job
   * </p>
   *
   * @param scheduleRequest A JobScheduleRequest object to define the parameters of the job being created
   * <pre function="syntax.xml">
   *  &lt;jobScheduleRequest&gt;
   *    &lt;jobName&gt;JobName&lt;/jobName&gt;
   *    &lt;simpleJobTrigger&gt;
   *    &lt;uiPassParam&gt;MINUTES&lt;/uiPassParam&gt;
   *    &lt;repeatInterval&gt;1800&lt;/repeatInterval&gt;
   *    &lt;repeatCount&gt;-1&lt;/repeatCount&gt;
   *    &lt;startTime&gt;2014-08-14T11:46:00.000-04:00&lt;/startTime&gt;
   *    &lt;endTime /&gt;
   *    &lt;/simpleJobTrigger&gt;
   *    &lt;inputFile&gt;/path/to/input/file&lt;/inputFile&gt;
   *    &lt;outputFile&gt;/path/to/output/file&lt;/outputFile&gt;
   *    &lt;jobParameters&gt;
   *    &lt;name&gt;ParameterName&lt;/name&gt;
   *    &lt;type&gt;string&lt;/type&gt;
   *    &lt;stringValue&gt;false&lt;/stringValue&gt;
   *    &lt;/jobParameters&gt;
   *  &lt;/jobScheduleRequest&gt;
   * </pre>
   *
   * @return A jax-rs Response object with the created jobId
   * <pre function="syntax.xml">
   *  username	JobName	1405356465422
   * </pre>
   */
  @POST
  @Path( "/job" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  @Produces( "text/plain" )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Schedule created successfully." ),
    @ResponseCode( code = 401, condition = "User is not allowed to create schedules." ),
    @ResponseCode( code = 403, condition = "Cannot create schedules for the specified file." ),
    @ResponseCode( code = 500, condition = "An error occurred while creating a schedule." )
  })
  public Response createJob( JobScheduleRequest scheduleRequest ) {
    try {
      Job job = schedulerService.createJob( scheduleRequest );
      return Response.ok( job.getJobId() ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      return Response.serverError().entity( e.getCause().getMessage() ).build();
    } catch ( IOException e ) {
      return Response.serverError().entity( e.getCause().getMessage() ).build();
    } catch ( SecurityException e ) {
      return Response.status( UNAUTHORIZED ).build();
    } catch ( IllegalAccessException e ) {
      return Response.status( FORBIDDEN ).build();
    }
  }

  /**
   * Execute a previously created job/schedule
   *
   * <pre function="syntax.xml">
   *  POST api/scheduler/triggerNow
   * </pre>
   *
   * @param jobRequest A JobRequest object containing the jobId
   * <pre function="syntax.xml">
   *  &lt;jobRequest&gt;
   *    &lt;jobId&gt;username	JobName	1408369303507&lt;/jobId&gt;
   *  &lt;/jobRequest&gt;
   * </pre>
   *
   * @return A Response object indicating the status of the scheduler
   * <pre function="syntax.xml">
   *  NORMAL
   * </pre>
   */
  @POST
  @Path( "/triggerNow" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Job triggered successfully." ),
    @ResponseCode( code = 400, condition = "Invalid input." ),
    @ResponseCode( code = 500, condition = "Invalid jobId." )
  })
  public Response triggerNow( JobRequest jobRequest ) {
    try {
      Job job = schedulerService.triggerNow( jobRequest.getJobId() );
      return Response.ok( job.getState().name() ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Get the job/schedule created by the system for deleting generated files
   *
   * <p>Example Request:<br />
   *  GET api/scheduler/getContentCleanerJob
   * </p>
   *
   * @return A Job object containing the definition of the content cleaner job
   * <pre function="syntax.xml">
   *  &lt;job&gt;
   *    &lt;groupName&gt;admin&lt;/groupName&gt;
   *    &lt;jobId&gt;admin	GeneratedContentCleaner	1408377444383&lt;/jobId&gt;
   *    &lt;jobName&gt;GeneratedContentCleaner&lt;/jobName&gt;
   *    &lt;jobParams&gt;
   *    &lt;jobParams&gt;
   *    &lt;name&gt;uiPassParam&lt;/name&gt;
   *    &lt;value&gt;DAILY&lt;/value&gt;
   *    &lt;/jobParams&gt;
   *    &lt;jobParams&gt;
   *    &lt;name&gt;age&lt;/name&gt;
   *    &lt;value&gt;15552000&lt;/value&gt;
   *    &lt;/jobParams&gt;
   *    &lt;jobParams&gt;
   *    &lt;name&gt;user_locale&lt;/name&gt;
   *    &lt;value&gt;en_US&lt;/value&gt;
   *    &lt;/jobParams&gt;
   *    &lt;jobParams&gt;
   *    &lt;name&gt;ActionAdapterQuartzJob-ActionUser&lt;/name&gt;
   *    &lt;value&gt;admin&lt;/value&gt;
   *    &lt;/jobParams&gt;
   *    &lt;jobParams&gt;
   *    &lt;name&gt;ActionAdapterQuartzJob-ActionClass&lt;/name&gt;
   *    &lt;value&gt;org.pentaho.platform.admin.GeneratedContentCleaner&lt;/value&gt;
   *    &lt;/jobParams&gt;
   *    &lt;jobParams&gt;
   *    &lt;name&gt;lineage-id&lt;/name&gt;
   *    &lt;value&gt;c3cfbad4-2e34-4dbd-8071-a2f3c7e8fab9&lt;/value&gt;
   *    &lt;/jobParams&gt;
   *    &lt;/jobParams&gt;
   *    &lt;jobTrigger xsi:type="simpleJobTrigger"&gt;
   *    &lt;duration&gt;-1&lt;/duration&gt;
   *    &lt;startTime&gt;2014-08-18T11:57:00-04:00&lt;/startTime&gt;
   *    &lt;uiPassParam&gt;DAILY&lt;/uiPassParam&gt;
   *    &lt;repeatCount&gt;-1&lt;/repeatCount&gt;
   *    &lt;repeatInterval&gt;86400&lt;/repeatInterval&gt;
   *    &lt;/jobTrigger&gt;
   *    &lt;lastRun&gt;2014-08-18T11:57:00-04:00&lt;/lastRun&gt;
   *    &lt;nextRun&gt;2014-08-19T11:57:00-04:00&lt;/nextRun&gt;
   *    &lt;state&gt;NORMAL&lt;/state&gt;
   *    &lt;userName&gt;admin&lt;/userName&gt;
   *  &lt;/job&gt;
   * </pre>
   */
  @GET
  @Path( "/getContentCleanerJob" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Content cleaner job successfully retrieved." ),
    @ResponseCode( code = 204, condition = "No content cleaner job exists." ),
  })
  public Job getContentCleanerJob() {
    try {
      return schedulerService.getContentCleanerJob();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Retrieve the all the job(s) visible to the current users
   *
   * <pre function="syntax.xml">
   *  GET api/scheduler/jobs
   * </pre>
   *
   * @param asCronString Cron string (Unused)
   *
   * @return A list of jobs that are visible to the current users
   *
   * <pre function="syntax.xml">
   *  &lt;jobs&gt;
   *   &lt;job&gt;
   *   &lt;groupName&gt;admin&lt;/groupName&gt;
   *   &lt;jobId&gt;admin	PentahoSystemVersionCheck	1408369303507&lt;/jobId&gt;
   *   &lt;jobName&gt;PentahoSystemVersionCheck&lt;/jobName&gt;
   *   &lt;jobParams&gt;
   *   &lt;jobParams&gt;
   *   &lt;name&gt;ActionAdapterQuartzJob-ActionUser&lt;/name&gt;
   *   &lt;value&gt;admin&lt;/value&gt;
   *   &lt;/jobParams&gt;
   *   &lt;jobParams&gt;
   *   &lt;name&gt;ActionAdapterQuartzJob-ActionClass&lt;/name&gt;
   *   &lt;value&gt;org.pentaho.platform.scheduler2.versionchecker.VersionCheckerAction&lt;/value&gt;
   *   &lt;/jobParams&gt;
   *   &lt;jobParams&gt;
   *   &lt;name&gt;lineage-id&lt;/name&gt;
   *   &lt;value&gt;1986cc90-cf87-43f6-8924-9d6e443e7d5d&lt;/value&gt;
   *   &lt;/jobParams&gt;
   *   &lt;jobParams&gt;
   *   &lt;name&gt;versionRequestFlags&lt;/name&gt;
   *   &lt;value&gt;0&lt;/value&gt;
   *   &lt;/jobParams&gt;
   *   &lt;/jobParams&gt;
   *   &lt;jobTrigger xsi:type="simpleJobTrigger"&gt;
   *   &lt;duration&gt;-1&lt;/duration&gt;
   *   &lt;startTime&gt;2014-08-18T09:41:43.506-04:00&lt;/startTime&gt;
   *   &lt;repeatCount&gt;-1&lt;/repeatCount&gt;
   *   &lt;repeatInterval&gt;86400&lt;/repeatInterval&gt;
   *   &lt;/jobTrigger&gt;
   *   &lt;lastRun&gt;2014-08-18T11:37:31.412-04:00&lt;/lastRun&gt;
   *   &lt;nextRun&gt;2014-08-19T09:41:43.506-04:00&lt;/nextRun&gt;
   *   &lt;state&gt;NORMAL&lt;/state&gt;
   *   &lt;userName&gt;admin&lt;/userName&gt;
   *   &lt;/job&gt;
   *   &lt;job&gt;
   *   &lt;groupName&gt;admin&lt;/groupName&gt;
   *   &lt;jobId&gt;admin	UpdateAuditData	1408373019115&lt;/jobId&gt;
   *   &lt;jobName&gt;UpdateAuditData&lt;/jobName&gt;
   *   &lt;jobParams&gt;
   *   &lt;jobParams&gt;
   *   &lt;name&gt;autoCreateUniqueFilename&lt;/name&gt;
   *   &lt;value&gt;false&lt;/value&gt;
   *   &lt;/jobParams&gt;
   *   &lt;jobParams&gt;
   *   &lt;name&gt;uiPassParam&lt;/name&gt;
   *   &lt;value&gt;MINUTES&lt;/value&gt;
   *   &lt;/jobParams&gt;
   *   &lt;jobParams&gt;
   *   &lt;name&gt;ActionAdapterQuartzJob-StreamProvider&lt;/name&gt;
   *   &lt;value&gt;input file = /public/pentaho-operations-mart/update_audit_mart_data/UpdateAuditData.xaction:outputFile = /public/pentaho-operations-mart/generated_logs/UpdateAuditData.*&lt;/value&gt;
   *   &lt;/jobParams&gt;
   *   &lt;jobParams&gt;
   *   &lt;name&gt;user_locale&lt;/name&gt;
   *   &lt;value&gt;en_US&lt;/value&gt;
   *   &lt;/jobParams&gt;
   *   &lt;jobParams&gt;
   *   &lt;name&gt;ActionAdapterQuartzJob-ActionUser&lt;/name&gt;
   *   &lt;value&gt;admin&lt;/value&gt;
   *   &lt;/jobParams&gt;
   *   &lt;jobParams&gt;
   *   &lt;name&gt;ActionAdapterQuartzJob-ActionId&lt;/name&gt;
   *   &lt;value&gt;xaction.backgroundExecution&lt;/value&gt;
   *   &lt;/jobParams&gt;
   *   &lt;jobParams&gt;
   *   &lt;name&gt;lineage-id&lt;/name&gt;
   *   &lt;value&gt;1f2402c4-0a70-40e4-b428-0d328f504cb3&lt;/value&gt;
   *   &lt;/jobParams&gt;
   *   &lt;/jobParams&gt;
   *   &lt;jobTrigger xsi:type="simpleJobTrigger"&gt;
   *   &lt;duration&gt;-1&lt;/duration&gt;
   *   &lt;startTime&gt;2014-07-14T12:47:00-04:00&lt;/startTime&gt;
   *   &lt;uiPassParam&gt;MINUTES&lt;/uiPassParam&gt;
   *   &lt;repeatCount&gt;-1&lt;/repeatCount&gt;
   *   &lt;repeatInterval&gt;1800&lt;/repeatInterval&gt;
   *   &lt;/jobTrigger&gt;
   *   &lt;lastRun&gt;2014-08-18T12:47:00-04:00&lt;/lastRun&gt;
   *   &lt;nextRun&gt;2014-08-18T13:17:00-04:00&lt;/nextRun&gt;
   *   &lt;state&gt;NORMAL&lt;/state&gt;
   *   &lt;userName&gt;admin&lt;/userName&gt;
   *   &lt;/job&gt;
   *  &lt;/jobs&gt;
   * </pre>
   */
  @Deprecated
  @GET
  @Path( "/jobs" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Jobs retrieved successfully." ),
    @ResponseCode( code = 500, condition = "Error while retrieving jobs." ),
  })
  public List<Job> getJobs( @DefaultValue( "false" ) @QueryParam( "asCronString" ) Boolean asCronString ) {
    try {
      return schedulerService.getJobs();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Retrieve the all the job(s) visible to the current users
   *
   * @param asCronString (Cron string) - UNUSED
   * @return list of <code> Job </code>
   */
  @GET
  @Path( "/getJobs" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public List<Job> getAllJobs() {
    try {
      return schedulerService.getJobs();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }
 
  /**
   * Checks whether the current user may schedule a repository file in the platform
   *
   * <pre function="syntax.xml">
   *  GET api/scheduler/isScheduleAllowed?id=fileId
   * </pre>
   *
   * @param id The repository file ID of the content to checked
   * <pre function="syntax.xml">
   *  fileId
   * </pre>
   *
   * @return true or false. true indicates scheduling is allowed and false indicates scheduling is not allowed.
   *
   * <pre function="syntax.xml">
   *  true
   * </pre>
   */
  @GET
  @Path( "/isScheduleAllowed" )
  @Produces( TEXT_PLAIN )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Successfully retrieved scheduling ability of repository file." ),
    @ResponseCode( code = 500, condition = "Invalid repository file id." ),
  })
  public String isScheduleAllowed( @QueryParam( "id" ) String id ) {
    return "" + schedulerService.isScheduleAllowed( id );
  }

  /**
   * <p>Checks whether the current user has authority to schedule any content in the platform</p>
   *
   * <p>Example Request:<br />
   *  api/scheduler/canSchedule
   * </p>
   *
   * <p>Checks whether the current user has authority to schedule any content in the platform and returns the response as a true/false value.</p>
   *
   * @return "true" or "false"
   */
  @GET
  @Path( "/canSchedule" )
  @Produces( TEXT_PLAIN )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Successful operation." ),
    @ResponseCode( code = 500, condition = "An error occurred while completing the operation." )
  })
  public String doGetCanSchedule() {
    return schedulerService.doGetCanSchedule();
  }

  /**
   * <p>Returns the state of the scheduler</p>
   *
   * <p>Example Request:<br />
   *  api/scheduler/state
   * </p>
   *
   * <p>Returns the state of the scheduler with the value of RUNNING, NORMAL or PAUSED</p>
   *
   * @return status of the scheduler as RUNNING, NORMAL or PAUSED
   */
  @GET
  @Path( "/state" )
  @Produces( "text/plain" )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Successfully retrieved the state of the scheduler." ),
    @ResponseCode( code = 500, condition = "An error occurred when getting the state of the scheduler." )
  })
  public Response getState() {
    try {
      String state = schedulerService.getState();
      return Response.ok( state ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * <p>Resume the scheduler</p>
   *
   * <p>Example Request:<br />
   *  api/scheduler/start
   * </p>
   *
   * <p>If the scheduler is in the PAUSE state, this will resume the scheduler</p>
   *
   * @return A jax-rs Response object with the status of the scheduler
   */
  @POST
  @Path( "/start" )
  @Produces( "text/plain" )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Successfully started the server." ),
    @ResponseCode( code = 500, condition = "An error occurred when resuming the scheduler." )
  })
  public Response start() {
    try {
      String status = schedulerService.start();
      return Response.ok( status ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * <p>Pause the scheduler</p>
   *
   * <p>Example Request:<br />
   *  api/scheduler/pause
   * </p>
   *
   * <p>If the scheduler is in the RUNNING state, this will pause the scheduler</p>
   *
   * @return A jax-rs Response object with the status of the scheduler
   */
  @POST
  @Path( "/pause" )
  @Produces( "text/plain" )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Successfully paused the server." ),
    @ResponseCode( code = 500, condition = "An error occurred when pausing the scheduler." )
  })
  public Response pause() {
    try {
      String status = schedulerService.pause();
      return Response.ok( status ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * <p>Shuts down the scheduler</p>
   *
   * <p>Example Request:<br />
   *  api/scheduler/shutdown
   * </p>
   *
   * <p>Regardless of the scheduler state, this will shut down the scheduler</p>
   *
   * @return A jax-rs Response object with the status of the scheduler
   */
  @POST
  @Path( "/shutdown" )
  @Produces( "text/plain" )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Successfully shut down the server." ),
    @ResponseCode( code = 500, condition = "An error occurred when shutting down the scheduler." )
  })
  public Response shutdown() {
    try {
      String status = schedulerService.shutdown();
      return Response.ok( status ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Checks the state of the selected job/schedule.
   *
   * @param jobRequest <code> JobRequest </code>
   * @return state of the job/schedule
   */
  @POST
  @Path( "/jobState" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response getJobState( JobRequest jobRequest ) {
    try {
      return Response.ok( schedulerService.getJobState( jobRequest ).name() ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( UnsupportedOperationException e ) {
      return Response.status( Status.UNAUTHORIZED ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Pause the selected job/schedule
   *
   * @param jobRequest <code> JobRequest </code>
   * @return
   */
  @POST
  @Path( "/pauseJob" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response pauseJob( JobRequest jobRequest ) {
    try {
      JobState state = schedulerService.pauseJob( jobRequest.getJobId() );
      return Response.ok( state.name() ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Resume the selected job/schedule
   *
   * @param jobRequest <code> JobRequest </code>
   * @return
   */
  @POST
  @Path( "/resumeJob" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response resumeJob( JobRequest jobRequest ) {
    try {
      JobState state = schedulerService.resumeJob( jobRequest.getJobId() );
      return Response.ok( state.name() ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Delete the selected job/schedule from the platform
   *
   * @param jobRequest <code> JobRequest </code>
   * @return
   */
  @DELETE
  @Path( "/removeJob" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response removeJob( JobRequest jobRequest ) {
    try {
      if (schedulerService.removeJob( jobRequest.getJobId() ) ) {
        return Response.ok( "REMOVED" ).type( MediaType.TEXT_PLAIN ).build();
      }
      Job job = schedulerService.getJob( jobRequest.getJobId() );
      return Response.ok( job.getState().name() ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Return the information regarding a specific job, identified by ID
   *
   * @param jobId
   * @param asCronString
   * @return
   */
  @GET
  @Path( "/jobinfo" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public Job getJob( @QueryParam( "jobId" ) String jobId,
                     @DefaultValue( "false" ) @QueryParam( "asCronString" ) String asCronString ) {
    try {
      return schedulerService.getJobInfo( jobId );
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  @Deprecated
  @GET
  @Path( "/jobinfotest" )
  @Produces( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public JobScheduleRequest getJobInfo() {
    return schedulerService.getJobInfo();
  }

  /**
   * @deprecated
   * Method is deprecated as the name getBlockoutJobs is preferred over getJobs
   * 
   * Retrieves all blockout jobs in the system
   *
   * @return list of <code> Job </code>
   */
  @Deprecated
  @Facet( name = "Unsupported" )
  public Response getJobs() {
    return getBlockoutJobs();
  }

  
  /**
   * Retrieves all blockout jobs
   * <p/>
   * This endpoint will return a list of all blockout jobs in the system.  
   * 
   * @return a Response object that contains a list of blockout jobs
   */
  @GET
  @Path( "/blockout/blockoutJobs" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public Response getBlockoutJobs() {
    return Response.ok( schedulerService.getBlockOutJobs() ).build();
  }
  
  
  /**
   * Checks if there are blockouts
   * <p/>
   * This endpoint determines whether there are any blockouts in the system
   *
   * @return true if the system has any blockouts, false otherwise
   */
  @GET
  @Path( "/blockout/hasBlockouts" )
  @Produces( { TEXT_PLAIN } )
  public Response hasBlockouts() {
    Boolean hasBlockouts = schedulerService.hasBlockouts();
    return Response.ok( hasBlockouts.toString() ).build();
  }

  /**
   * Creates a new blockout
   * <p>
   * Creates a new blockout with the values from the supplied XML/JSON payload
   * 
   * @param scheduleRequest <pre function="syntax.xml">
   *
   *                        </pre>
   * @return a Response object which contains the ID of the blockout which was created
   * 
   * @throws IOException
   */
  @POST
  @Path( "/blockout/createBlockout" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Successful operation." ),
    @ResponseCode( code = 401, condition = "User is not authorized to create blockout." )
  })
  public Response addBlockout( JobScheduleRequest request ) throws IOException {
    if ( schedulerService.isScheduleAllowed() ) {
      request.setActionClass( BlockoutAction.class.getCanonicalName() );
      request.getJobParameters().add( new JobScheduleParam( IBlockoutManager.DURATION_PARAM, request.getDuration() ) );
      request.getJobParameters().add( new JobScheduleParam( IBlockoutManager.TIME_ZONE_PARAM, request.getTimeZone() ) );
      SchedulerResourceUtil.updateStartDateForTimeZone( request );
      return createJob( request );
    }
    return Response.status( Status.UNAUTHORIZED ).build();
  }

  /**
   * Update existing blockout
   * <p>
   * This endpoint will edit an existing blockout with the values from the supplied XML/JSON payload
   * 
   * @param jobId The jobId we are editing <pre function="syntax.xml">
   *
   *                        </pre>
   * @param scheduleRequest The payload containing the definition of the blockout <pre function="syntax.xml">
   *
   *                        </pre>
   * @return a Response object which contains the ID of the blockout which was created
   * 
   * @throws IOException
   */
  @POST
  @Path( "/blockout/updateBlockout" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Successful operation." ),
    @ResponseCode( code = 401, condition = "User is not authorized to update blockout." )
  })
  public Response updateBlockout( @QueryParam( "jobid" ) String jobId, JobScheduleRequest request ) throws IOException {
    if ( schedulerService.isScheduleAllowed() ) {
      JobRequest jobRequest = new JobRequest();
      jobRequest.setJobId( jobId );
      Response response = removeJob( jobRequest );
      if ( response.getStatus() == 200 ) {
        response = addBlockout( request );
      }
      return response;
    }
    return Response.status( Status.UNAUTHORIZED ).build();
  }

  /**
   * Checks if the selected blockout schedule will be fired
   * <p>
   * Checks if the selected blockout schedule will be fired
   * 
   * @param scheduleRequest <pre function="syntax.xml">
   *
   *                        </pre>
   * @return a Response object which contains true/false if the provided schedule (blockout) will fire
   */
  @GET
  @Path( "/blockout/blockoutWillFire" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  @Produces( { TEXT_PLAIN } )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Successful operation." ),
    @ResponseCode( code = 500, condition = "An error occurred while completing the operation, UnifiedRepositoryException or SchedulerException." )
  })
  public Response blockoutWillFire( JobScheduleRequest request ) {
    Boolean willFire;
    try {
      willFire =
        schedulerService.willFire( SchedulerResourceUtil.convertScheduleRequestToJobTrigger( request ) );
    } catch ( UnifiedRepositoryException e ) {
      return Response.serverError().entity( e ).build();
    } catch ( SchedulerException e ) {
      return Response.serverError().entity( e ).build();
    }
    return Response.ok( willFire.toString() ).build();
  }

  /**
   * Checks if the selected blockout schedule should be fired now
   * <p>
   * Checks if the selected blockout schedule should be fired now
   * 
   * @return true if the selected blockout should be fired now, false otherwise
   */
  @GET
  @Path( "/blockout/blockoutShouldFireNow" )
  @Produces( { TEXT_PLAIN } )
  public Response shouldFireNow() {
    Boolean result = schedulerService.shouldFireNow();
    return Response.ok( result.toString() ).build();
  }

  
  /**
   * Check the status of the selected blockout schedule. 
   * <p>
   * Check the status of the selected blockout schedule. The status will display whether it is completely blocked or
   * partially blocked
   * 
   * @param scheduleRequest <pre function="syntax.xml">
   *
   *                        </pre>
   * @return a Response object which contains a BlockStatusProxy which contains totallyBlocked and partiallyBlocked flags
   * 
   * @throws UnifiedRepositoryException
   * @throws SchedulerException
   */  
  @POST
  @Path( "/blockout/blockStatus" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public Response getBlockStatus( JobScheduleRequest request ) throws UnifiedRepositoryException,
    SchedulerException {
    IJobTrigger trigger = SchedulerResourceUtil.convertScheduleRequestToJobTrigger( request );
    return Response.ok( schedulerService.getBlockStatus( trigger ) ).build();
  }

  /**
   * Retrieve the list of execute content by lineage id.
   *
   * @param lineageId the path for the file
   * <pre function="syntax.xml">
   *  :path:to:file:id
   * </pre>
   * @return list of <code> repositoryFileDto </code>
   * <pre function="syntax.xml">
   *   <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   *    &lt;List&gt;
   *     &lt;repositoryFileDto&gt;
   *     &lt;createdDate&gt;1402911997019&lt;/createdDate&gt;
   *     &lt;fileSize&gt;3461&lt;/fileSize&gt;
   *     &lt;folder&gt;false&lt;/folder&gt;
   *     &lt;hidden&gt;false&lt;/hidden&gt;
   *     &lt;id&gt;ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/id&gt;
   *     &lt;lastModifiedDate&gt;1406647160536&lt;/lastModifiedDate&gt;
   *     &lt;locale&gt;en&lt;/locale&gt;
   *     &lt;localePropertiesMapEntries&gt;
   *       &lt;localeMapDto&gt;
   *         &lt;locale&gt;default&lt;/locale&gt;
   *         &lt;properties&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;file.title&lt;/key&gt;
   *             &lt;value&gt;myFile&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;jcr:primaryType&lt;/key&gt;
   *             &lt;value&gt;nt:unstructured&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;title&lt;/key&gt;
   *             &lt;value&gt;myFile&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;file.description&lt;/key&gt;
   *             &lt;value&gt;myFile Description&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *         &lt;/properties&gt;
   *       &lt;/localeMapDto&gt;
   *     &lt;/localePropertiesMapEntries&gt;
   *     &lt;locked&gt;false&lt;/locked&gt;
   *     &lt;name&gt;myFile.prpt&lt;/name&gt;&lt;/name&gt;
   *     &lt;originalParentFolderPath&gt;/public/admin&lt;/originalParentFolderPath&gt;
   *     &lt;ownerType&gt;-1&lt;/ownerType&gt;
   *     &lt;path&gt;/public/admin/ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/path&gt;
   *     &lt;title&gt;myFile&lt;/title&gt;
   *     &lt;versionId&gt;1.9&lt;/versionId&gt;
   *     &lt;versioned&gt;true&lt;/versioned&gt;
   *   &lt;/repositoryFileAclDto&gt;
   *  &lt;/List&gt;
   * </pre>
   */
  @GET
  @Path( "/generatedContentForSchedule" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetGeneratedContentForSchedule( @QueryParam( "lineageId" ) String lineageId ) {
    List<RepositoryFileDto> repositoryFileDtoList = new ArrayList<RepositoryFileDto>();
    try {
      repositoryFileDtoList = schedulerService.doGetGeneratedContentForSchedule( lineageId );
    } catch ( FileNotFoundException e ) {
      //return the empty list
    } catch ( Throwable t ) {
      logger
        .error( Messages.getInstance().getString( "FileResource.GENERATED_CONTENT_FOR_USER_FAILED", lineageId ), t );
    }
    return repositoryFileDtoList;
  }
}
