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
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.Job.JobState;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.web.http.api.resources.proxies.BlockStatusProxy;
import org.pentaho.platform.web.http.api.resources.services.SchedulerService;
import org.pentaho.platform.web.http.messages.Messages;

/**
 * Represents a file node in the repository. This api provides methods for discovering information about repository
 * files as well as CRUD operations
 *
 * @author aaron
 */
@Path ( "/scheduler" )
public class SchedulerResource extends AbstractJaxRSResource {

  protected SchedulerService schedulerService;

  protected static final Log logger = LogFactory.getLog( SchedulerResource.class );

  public SchedulerResource() {
    schedulerService = new SchedulerService();
  }


  /**
   * Creates a new Job/Schedule
   * <p/>
   * <p><b>Example Request:</b><br />
   * PUT api/scheduler/job
   * </p>
   *
   * @param scheduleRequest A JobScheduleRequest object to define the parameters of the job being created
   *                        <pre function="syntax.xml">
   *                        &lt;jobScheduleRequest&gt;
   *                        &lt;jobName&gt;JobName&lt;/jobName&gt;
   *                        &lt;simpleJobTrigger&gt;
   *                        &lt;uiPassParam&gt;MINUTES&lt;/uiPassParam&gt;
   *                        &lt;repeatInterval&gt;1800&lt;/repeatInterval&gt;
   *                        &lt;repeatCount&gt;-1&lt;/repeatCount&gt;
   *                        &lt;startTime&gt;2014-08-14T11:46:00.000-04:00&lt;/startTime&gt;
   *                        &lt;endTime /&gt;
   *                        &lt;/simpleJobTrigger&gt;
   *                        &lt;inputFile&gt;/path/to/input/file&lt;/inputFile&gt;
   *                        &lt;outputFile&gt;/path/to/output/file&lt;/outputFile&gt;
   *                        &lt;jobParameters&gt;
   *                        &lt;name&gt;ParameterName&lt;/name&gt;
   *                        &lt;type&gt;string&lt;/type&gt;
   *                        &lt;stringValue&gt;false&lt;/stringValue&gt;
   *                        &lt;/jobParameters&gt;
   *                        &lt;/jobScheduleRequest&gt;
   *                        </pre>
   * @return A jax-rs Response object with the created jobId
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * username	JobName	1405356465422
   * </pre>
   */
  @POST
  @Path ( "/job" )
  @Consumes ( { APPLICATION_JSON, APPLICATION_XML } )
  @Produces ( "text/plain" )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Schedule created successfully." ),
      @ResponseCode ( code = 401, condition = "User is not allowed to create schedules." ),
      @ResponseCode ( code = 403, condition = "Cannot create schedules for the specified file." ),
      @ResponseCode ( code = 500, condition = "An error occurred while creating a schedule." )
  } )
  public Response createJob( JobScheduleRequest scheduleRequest ) {
    try {
      Job job = schedulerService.createJob( scheduleRequest );
      return buildPlainTextOkResponse( job.getJobId() );
    } catch ( SchedulerException e ) {
      return buildServerErrorResponse( e.getCause().getMessage() );
    } catch ( IOException e ) {
      return buildServerErrorResponse( e.getCause().getMessage() );
    } catch ( SecurityException e ) {
      return buildStatusResponse( UNAUTHORIZED );
    } catch ( IllegalAccessException e ) {
      return buildStatusResponse( FORBIDDEN );
    }
  }

  /**
   * Execute a previously created job/schedule
   * <p/>
   * <p><b>Example Request:</b><br />
   * POST api/scheduler/triggerNow
   * </p>
   *
   * @param jobRequest A JobRequest object containing the jobId
   *                   <pre function="syntax.xml">
   *                   &lt;jobRequest&gt;
   *                   &lt;jobId&gt;username	JobName	1408369303507&lt;/jobId&gt;
   *                   &lt;/jobRequest&gt;
   *                   </pre>
   * @return A Response object indicating the status of the scheduler
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * NORMAL
   * </pre>
   */
  @POST
  @Path ( "/triggerNow" )
  @Produces ( "text/plain" )
  @Consumes ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Job triggered successfully." ),
      @ResponseCode ( code = 400, condition = "Invalid input." ),
      @ResponseCode ( code = 500, condition = "Invalid jobId." )
  } )
  public Response triggerNow( JobRequest jobRequest ) {
    try {
      Job job = schedulerService.triggerNow( jobRequest.getJobId() );
      return buildPlainTextOkResponse( job.getState().name() );
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Get the job/schedule created by the system for deleting generated files
   * <p/>
   * <p><b>Example Request:</b><br />
   * GET api/scheduler/getContentCleanerJob
   * </p>
   *
   * @return A Job object containing the definition of the content cleaner job
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * &lt;job&gt;
   * &lt;groupName&gt;admin&lt;/groupName&gt;
   * &lt;jobId&gt;admin	GeneratedContentCleaner	1408377444383&lt;/jobId&gt;
   * &lt;jobName&gt;GeneratedContentCleaner&lt;/jobName&gt;
   * &lt;jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;uiPassParam&lt;/name&gt;
   * &lt;value&gt;DAILY&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;age&lt;/name&gt;
   * &lt;value&gt;15552000&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;user_locale&lt;/name&gt;
   * &lt;value&gt;en_US&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionUser&lt;/name&gt;
   * &lt;value&gt;admin&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionClass&lt;/name&gt;
   * &lt;value&gt;org.pentaho.platform.admin.GeneratedContentCleaner&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;lineage-id&lt;/name&gt;
   * &lt;value&gt;c3cfbad4-2e34-4dbd-8071-a2f3c7e8fab9&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;/jobParams&gt;
   * &lt;jobTrigger xsi:type="simpleJobTrigger"&gt;
   * &lt;duration&gt;-1&lt;/duration&gt;
   * &lt;startTime&gt;2014-08-18T11:57:00-04:00&lt;/startTime&gt;
   * &lt;uiPassParam&gt;DAILY&lt;/uiPassParam&gt;
   * &lt;repeatCount&gt;-1&lt;/repeatCount&gt;
   * &lt;repeatInterval&gt;86400&lt;/repeatInterval&gt;
   * &lt;/jobTrigger&gt;
   * &lt;lastRun&gt;2014-08-18T11:57:00-04:00&lt;/lastRun&gt;
   * &lt;nextRun&gt;2014-08-19T11:57:00-04:00&lt;/nextRun&gt;
   * &lt;state&gt;NORMAL&lt;/state&gt;
   * &lt;userName&gt;admin&lt;/userName&gt;
   * &lt;/job&gt;
   * </pre>
   */
  @GET
  @Path ( "/getContentCleanerJob" )
  @Produces ( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Content cleaner job successfully retrieved." ),
      @ResponseCode ( code = 204, condition = "No content cleaner job exists." ),
  } )
  public Job getContentCleanerJob() {
    try {
      return schedulerService.getContentCleanerJob();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Retrieve the all the job(s) visible to the current users
   * <p/>
   * <p><b>Example Request:</b><br />
   * GET api/scheduler/jobs
   * </p>
   *
   * @param asCronString Cron string (Unused)
   * @return A list of jobs that are visible to the current users
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * &lt;jobs&gt;
   * &lt;job&gt;
   * &lt;groupName&gt;admin&lt;/groupName&gt;
   * &lt;jobId&gt;admin	PentahoSystemVersionCheck	1408369303507&lt;/jobId&gt;
   * &lt;jobName&gt;PentahoSystemVersionCheck&lt;/jobName&gt;
   * &lt;jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionUser&lt;/name&gt;
   * &lt;value&gt;admin&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionClass&lt;/name&gt;
   * &lt;value&gt;org.pentaho.platform.scheduler2.versionchecker.VersionCheckerAction&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;lineage-id&lt;/name&gt;
   * &lt;value&gt;1986cc90-cf87-43f6-8924-9d6e443e7d5d&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;versionRequestFlags&lt;/name&gt;
   * &lt;value&gt;0&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;/jobParams&gt;
   * &lt;jobTrigger xsi:type="simpleJobTrigger"&gt;
   * &lt;duration&gt;-1&lt;/duration&gt;
   * &lt;startTime&gt;2014-08-18T09:41:43.506-04:00&lt;/startTime&gt;
   * &lt;repeatCount&gt;-1&lt;/repeatCount&gt;
   * &lt;repeatInterval&gt;86400&lt;/repeatInterval&gt;
   * &lt;/jobTrigger&gt;
   * &lt;lastRun&gt;2014-08-18T11:37:31.412-04:00&lt;/lastRun&gt;
   * &lt;nextRun&gt;2014-08-19T09:41:43.506-04:00&lt;/nextRun&gt;
   * &lt;state&gt;NORMAL&lt;/state&gt;
   * &lt;userName&gt;admin&lt;/userName&gt;
   * &lt;/job&gt;
   * &lt;job&gt;
   * &lt;groupName&gt;admin&lt;/groupName&gt;
   * &lt;jobId&gt;admin	UpdateAuditData	1408373019115&lt;/jobId&gt;
   * &lt;jobName&gt;UpdateAuditData&lt;/jobName&gt;
   * &lt;jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;autoCreateUniqueFilename&lt;/name&gt;
   * &lt;value&gt;false&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;uiPassParam&lt;/name&gt;
   * &lt;value&gt;MINUTES&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-StreamProvider&lt;/name&gt;
   * &lt;value&gt;input file = /public/pentaho-operations-mart/update_audit_mart_data/UpdateAuditData.xaction:outputFile = /public/pentaho-operations-mart/generated_logs/UpdateAuditData.*&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;user_locale&lt;/name&gt;
   * &lt;value&gt;en_US&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionUser&lt;/name&gt;
   * &lt;value&gt;admin&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionId&lt;/name&gt;
   * &lt;value&gt;xaction.backgroundExecution&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;lineage-id&lt;/name&gt;
   * &lt;value&gt;1f2402c4-0a70-40e4-b428-0d328f504cb3&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;/jobParams&gt;
   * &lt;jobTrigger xsi:type="simpleJobTrigger"&gt;
   * &lt;duration&gt;-1&lt;/duration&gt;
   * &lt;startTime&gt;2014-07-14T12:47:00-04:00&lt;/startTime&gt;
   * &lt;uiPassParam&gt;MINUTES&lt;/uiPassParam&gt;
   * &lt;repeatCount&gt;-1&lt;/repeatCount&gt;
   * &lt;repeatInterval&gt;1800&lt;/repeatInterval&gt;
   * &lt;/jobTrigger&gt;
   * &lt;lastRun&gt;2014-08-18T12:47:00-04:00&lt;/lastRun&gt;
   * &lt;nextRun&gt;2014-08-18T13:17:00-04:00&lt;/nextRun&gt;
   * &lt;state&gt;NORMAL&lt;/state&gt;
   * &lt;userName&gt;admin&lt;/userName&gt;
   * &lt;/job&gt;
   * &lt;/jobs&gt;
   * </pre>
   */
  @Deprecated
  @GET
  @Path ( "/jobs" )
  @Produces ( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Jobs retrieved successfully." ),
      @ResponseCode ( code = 500, condition = "Error while retrieving jobs." ),
  } )
  public List<Job> getJobs( @DefaultValue ( "false" ) @QueryParam ( "asCronString" ) Boolean asCronString ) {
    try {
      return schedulerService.getJobs();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Retrieve the all the job(s) visible to the current users
   * <p/>
   * <p><b>Example Request:</b><br />
   * GET api/scheduler/getJobs
   * </p>
   *
   * @return A list of jobs that are visible to the current users
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * &lt;jobs&gt;
   * &lt;job&gt;
   * &lt;groupName&gt;admin&lt;/groupName&gt;
   * &lt;jobId&gt;admin	PentahoSystemVersionCheck	1408369303507&lt;/jobId&gt;
   * &lt;jobName&gt;PentahoSystemVersionCheck&lt;/jobName&gt;
   * &lt;jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionUser&lt;/name&gt;
   * &lt;value&gt;admin&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionClass&lt;/name&gt;
   * &lt;value&gt;org.pentaho.platform.scheduler2.versionchecker.VersionCheckerAction&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;lineage-id&lt;/name&gt;
   * &lt;value&gt;1986cc90-cf87-43f6-8924-9d6e443e7d5d&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;versionRequestFlags&lt;/name&gt;
   * &lt;value&gt;0&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;/jobParams&gt;
   * &lt;jobTrigger xsi:type="simpleJobTrigger"&gt;
   * &lt;duration&gt;-1&lt;/duration&gt;
   * &lt;startTime&gt;2014-08-18T09:41:43.506-04:00&lt;/startTime&gt;
   * &lt;repeatCount&gt;-1&lt;/repeatCount&gt;
   * &lt;repeatInterval&gt;86400&lt;/repeatInterval&gt;
   * &lt;/jobTrigger&gt;
   * &lt;lastRun&gt;2014-08-18T11:37:31.412-04:00&lt;/lastRun&gt;
   * &lt;nextRun&gt;2014-08-19T09:41:43.506-04:00&lt;/nextRun&gt;
   * &lt;state&gt;NORMAL&lt;/state&gt;
   * &lt;userName&gt;admin&lt;/userName&gt;
   * &lt;/job&gt;
   * &lt;job&gt;
   * &lt;groupName&gt;admin&lt;/groupName&gt;
   * &lt;jobId&gt;admin	UpdateAuditData	1408373019115&lt;/jobId&gt;
   * &lt;jobName&gt;UpdateAuditData&lt;/jobName&gt;
   * &lt;jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;autoCreateUniqueFilename&lt;/name&gt;
   * &lt;value&gt;false&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;uiPassParam&lt;/name&gt;
   * &lt;value&gt;MINUTES&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-StreamProvider&lt;/name&gt;
   * &lt;value&gt;input file = /public/pentaho-operations-mart/update_audit_mart_data/UpdateAuditData.xaction:outputFile = /public/pentaho-operations-mart/generated_logs/UpdateAuditData.*&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;user_locale&lt;/name&gt;
   * &lt;value&gt;en_US&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionUser&lt;/name&gt;
   * &lt;value&gt;admin&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionId&lt;/name&gt;
   * &lt;value&gt;xaction.backgroundExecution&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;lineage-id&lt;/name&gt;
   * &lt;value&gt;1f2402c4-0a70-40e4-b428-0d328f504cb3&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;/jobParams&gt;
   * &lt;jobTrigger xsi:type="simpleJobTrigger"&gt;
   * &lt;duration&gt;-1&lt;/duration&gt;
   * &lt;startTime&gt;2014-07-14T12:47:00-04:00&lt;/startTime&gt;
   * &lt;uiPassParam&gt;MINUTES&lt;/uiPassParam&gt;
   * &lt;repeatCount&gt;-1&lt;/repeatCount&gt;
   * &lt;repeatInterval&gt;1800&lt;/repeatInterval&gt;
   * &lt;/jobTrigger&gt;
   * &lt;lastRun&gt;2014-08-18T12:47:00-04:00&lt;/lastRun&gt;
   * &lt;nextRun&gt;2014-08-18T13:17:00-04:00&lt;/nextRun&gt;
   * &lt;state&gt;NORMAL&lt;/state&gt;
   * &lt;userName&gt;admin&lt;/userName&gt;
   * &lt;/job&gt;
   * &lt;/jobs&gt;
   * </pre>
   */
  @GET
  @Path ( "/getJobs" )
  @Produces ( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Jobs retrieved successfully." ),
      @ResponseCode ( code = 500, condition = "Error while retrieving jobs." ),
  } )
  public List<Job> getAllJobs() {
    try {
      return schedulerService.getJobs();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Checks whether the current user may schedule a repository file in the platform
   * <p/>
   * <p><b>Example Request:</b><br />
   * GET api/scheduler/isScheduleAllowed?id=fileId
   * </p>
   *
   * @param id The repository file ID of the content to checked
   *           <pre function="syntax.xml">
   *           fileId
   *           </pre>
   * @return true or false. true indicates scheduling is allowed and false indicates scheduling is not allowed for the file.
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * true
   * </pre>
   */
  @GET
  @Path ( "/isScheduleAllowed" )
  @Produces ( TEXT_PLAIN )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully retrieved scheduling ability of repository file." ),
      @ResponseCode ( code = 500, condition = "Invalid repository file id." ),
  } )
  public String isScheduleAllowed( @QueryParam ( "id" ) String id ) {
    return "" + schedulerService.isScheduleAllowed( id );
  }

  /**
   * Checks whether the current user has authority to schedule any content in the platform
   * <p/>
   * <p><b>Example Request:</b><br />
   * GET api/scheduler/canSchedule
   * </p>
   *
   * @return true or false. true indicates scheduling is allowed and false indicates scheduling is not allowed for the user.
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * true
   * </pre>
   */
  @GET
  @Path ( "/canSchedule" )
  @Produces ( TEXT_PLAIN )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successful retrieved the scheduling permission." ),
      @ResponseCode ( code = 500, condition = "Unable to retrieve the scheduling permission." )
  } )
  public String doGetCanSchedule() {
    return schedulerService.doGetCanSchedule();
  }

  /**
   * Returns the state of the scheduler with the value of RUNNING or PAUSED
   * <p/>
   * <p><b>Example Request:</b><br />
   * GET api/scheduler/state
   * </p>
   *
   * @return status of the scheduler as RUNNING or PAUSED
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * RUNNING
   * </pre>
   */
  @GET
  @Path ( "/state" )
  @Produces ( "text/plain" )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully retrieved the state of the scheduler." ),
      @ResponseCode ( code = 500, condition = "An error occurred when getting the state of the scheduler." )
  } )
  public Response getState() {
    try {
      String state = schedulerService.getState();
      return buildPlainTextOkResponse( state );
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Resume the scheduler from a paused state
   * <p/>
   * <p><b>Example Request:</b><br />
   * POST api/scheduler/start
   * </p>
   *
   * @return A jax-rs Response object containing the status of the scheduler
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * RUNNING
   * </pre>
   */
  @POST
  @Path ( "/start" )
  @Produces ( "text/plain" )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully started the server." ),
      @ResponseCode ( code = 500, condition = "An error occurred when resuming the scheduler." )
  } )
  public Response start() {
    try {
      String status = schedulerService.start();
      return buildPlainTextOkResponse( status );
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Pause the scheduler from a running state
   * <p/>
   * <p><b>Example Request:</b><br />
   * POST api/scheduler/pause
   * </p>
   *
   * @return A jax-rs Response object containing the status of the scheduler
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * PAUSED
   * </pre>
   */
  @POST
  @Path ( "/pause" )
  @Produces ( "text/plain" )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully paused the server." ),
      @ResponseCode ( code = 500, condition = "An error occurred when pausing the scheduler." )
  } )
  public Response pause() {
    try {
      String status = schedulerService.pause();
      return buildPlainTextOkResponse( status );
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Shuts down the scheduler
   * <p/>
   * <p><b>Example Request:</b><br />
   * POST api/scheduler/shutdown
   * </p>
   *
   * @return A jax-rs Response object containing the status of the scheduler
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * PAUSED
   * </pre>
   */
  @POST
  @Path ( "/shutdown" )
  @Produces ( "text/plain" )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully shut down the server." ),
      @ResponseCode ( code = 500, condition = "An error occurred when shutting down the scheduler." )
  } )
  public Response shutdown() {
    try {
      String status = schedulerService.shutdown();
      return buildPlainTextOkResponse( status );
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Checks the state of the selected job/schedule
   * <p/>
   * <p><b>Example Request:</b><br />
   * POST api/scheduler/jobState
   * </p>
   *
   * @param jobRequest A JobRequest object containing the jobId
   *                   <pre function="syntax.xml">
   *                   &lt;jobRequest&gt;
   *                   &lt;jobId&gt;username	JobName	1408369303507&lt;/jobId&gt;
   *                   &lt;/jobRequest&gt;
   *                   </pre>
   * @return A jax-rs Response object containing the status of the scheduled job
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * NORMAL
   * </pre>
   */
  @POST
  @Path ( "/jobState" )
  @Produces ( "text/plain" )
  @Consumes ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully retrieved the state of the requested job." ),
      @ResponseCode ( code = 500, condition = "Invalid jobId." )
  } )
  public Response getJobState( JobRequest jobRequest ) {
    try {
      return buildPlainTextOkResponse( schedulerService.getJobState( jobRequest ).name() );
    } catch ( UnsupportedOperationException e ) {
      return buildPlainTextStatusResponse( UNAUTHORIZED );
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Pause the specified job/schedule
   * <p/>
   * <p><b>Example Request:</b><br />
   * POST api/scheduler/pauseJob
   * </p>
   *
   * @param jobRequest A JobRequest object containing the jobId
   *                   <pre function="syntax.xml">
   *                   &lt;jobRequest&gt;
   *                   &lt;jobId&gt;username	JobName	1408369303507&lt;/jobId&gt;
   *                   &lt;/jobRequest&gt;
   *                   </pre>
   * @return A jax-rs Response object containing the status of the scheduled job
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * PAUSED
   * </pre>
   */
  @POST
  @Path ( "/pauseJob" )
  @Produces ( "text/plain" )
  @Consumes ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully paused the job." ),
      @ResponseCode ( code = 500, condition = "Invalid jobId." )
  } )
  public Response pauseJob( JobRequest jobRequest ) {
    try {
      JobState state = schedulerService.pauseJob( jobRequest.getJobId() );
      return buildPlainTextOkResponse( state.name() );
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Resume the specified job/schedule
   * <p/>
   * <p><b>Example Request:</b><br />
   * POST api/scheduler/resumeJob
   * </p>
   *
   * @param jobRequest A JobRequest object containing the jobId
   *                   <pre function="syntax.xml">
   *                   &lt;jobRequest&gt;
   *                   &lt;jobId&gt;username	JobName	1408369303507&lt;/jobId&gt;
   *                   &lt;/jobRequest&gt;
   *                   </pre>
   * @return A jax-rs Response object containing the status of the scheduled job
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * NORMAL
   * </pre>
   */
  @POST
  @Path ( "/resumeJob" )
  @Produces ( "text/plain" )
  @Consumes ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully resumed the job." ),
      @ResponseCode ( code = 500, condition = "Invalid jobId." )
  } )
  public Response resumeJob( JobRequest jobRequest ) {
    try {
      JobState state = schedulerService.resumeJob( jobRequest.getJobId() );
      return buildPlainTextOkResponse( state.name() );
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Delete the specified job/schedule from the platform
   * <p/>
   * <p><b>Example Request:</b><br />
   * DELETE api/scheduler/removeJob
   * </p>
   *
   * @param jobRequest A JobRequest object containing the jobId
   *                   <pre function="syntax.xml">
   *                   &lt;jobRequest&gt;
   *                   &lt;jobId&gt;username	JobName	1408369303507&lt;/jobId&gt;
   *                   &lt;/jobRequest&gt;
   *                   </pre>
   * @return A jax-rs Response object containing the status of the scheduled job
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * REMOVED
   * </pre>
   */
  @DELETE
  @Path ( "/removeJob" )
  @Produces ( "text/plain" )
  @Consumes ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully removed the job." ),
      @ResponseCode ( code = 500, condition = "Invalid jobId." )
  } )
  public Response removeJob( JobRequest jobRequest ) {
    try {
      if ( schedulerService.removeJob( jobRequest.getJobId() ) ) {
        return buildPlainTextOkResponse( "REMOVED" );
      }
      Job job = schedulerService.getJob( jobRequest.getJobId() );
      return buildPlainTextOkResponse( job.getState().name() );
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Return the information for a specified job
   * <p/>
   * <p><b>Example Request:</b><br />
   * GET api/scheduler/jobInfo?jobId=admin%09PentahoSystemVersionCheck%091408387651641
   * </p>
   *
   * @param jobId        The jobId of the job for which we are requesting information
   * @param asCronString Cron string (Unused)
   * @return A Job object containing the info for the specified job
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * &lt;job&gt;
   * &lt;jobId&gt;admin	PentahoSystemVersionCheck	1408387651641&lt;/jobId&gt;
   * &lt;jobName&gt;PentahoSystemVersionCheck&lt;/jobName&gt;
   * &lt;jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionUser&lt;/name&gt;
   * &lt;value&gt;admin&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionClass&lt;/name&gt;
   * &lt;value&gt;org.pentaho.platform.scheduler2.versionchecker.VersionCheckerAction&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;lineage-id&lt;/name&gt;
   * &lt;value&gt;da116fa7-539f-43ca-b6d7-8ce5408c97ce&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;versionRequestFlags&lt;/name&gt;
   * &lt;value&gt;0&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;/jobParams&gt;
   * &lt;jobTrigger xsi:type="simpleJobTrigger"&gt;
   * &lt;duration&gt;-1&lt;/duration&gt;
   * &lt;startTime&gt;2014-08-18T14:47:31.639-04:00&lt;/startTime&gt;
   * &lt;repeatCount&gt;-1&lt;/repeatCount&gt;
   * &lt;repeatInterval&gt;86400&lt;/repeatInterval&gt;
   * &lt;/jobTrigger&gt;
   * &lt;lastRun&gt;2014-08-18T14:47:31.639-04:00&lt;/lastRun&gt;
   * &lt;nextRun&gt;2014-08-19T14:47:31.639-04:00&lt;/nextRun&gt;
   * &lt;state&gt;NORMAL&lt;/state&gt;
   * &lt;userName&gt;admin&lt;/userName&gt;
   * &lt;/job&gt;
   * </pre>
   */
  @GET
  @Path ( "/jobinfo" )
  @Produces ( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully retrieved the information for the requested job." ),
      @ResponseCode ( code = 500, condition = "Invalid jobId." )
  } )
  public Job getJob( @QueryParam ( "jobId" ) String jobId,
                     @DefaultValue ( "false" ) @QueryParam ( "asCronString" ) String asCronString ) {
    try {
      return schedulerService.getJobInfo( jobId );
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  @Deprecated
  @GET
  @Path ( "/jobinfotest" )
  @Produces ( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public JobScheduleRequest getJobInfo() {
    return schedulerService.getJobInfo();
  }

  /**
   * @return list of Job
   * @deprecated Method is deprecated as the name getBlockoutJobs is preferred over getJobs
   * <p/>
   * Retrieves all blockout jobs in the system
   */
  @Deprecated
  @Facet ( name = "Unsupported" )
  public List<Job> getJobs() {
    return getBlockoutJobs();
  }


  /**
   * Get all the blockout jobs in the system
   * <p/>
   * <p><b>Example Request:</b><br />
   * GET /scheduler/blockout/blockoutJobs
   * </p>
   *
   * @return A Response object that contains a list of blockout jobs
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * &lt;jobs&gt;
   * &lt;job&gt;
   * &lt;groupName&gt;admin&lt;/groupName&gt;
   * &lt;jobId&gt;admin	BlockoutAction	1408457558636&lt;/jobId&gt;
   * &lt;jobName&gt;BlockoutAction&lt;/jobName&gt;
   * &lt;jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;TIME_ZONE_PARAM&lt;/name&gt;
   * &lt;value&gt;America/New_York&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;DURATION_PARAM&lt;/name&gt;
   * &lt;value&gt;10080000&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;uiPassParam&lt;/name&gt;
   * &lt;value&gt;DAILY&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;user_locale&lt;/name&gt;
   * &lt;value&gt;en_US&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionUser&lt;/name&gt;
   * &lt;value&gt;admin&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;ActionAdapterQuartzJob-ActionClass&lt;/name&gt;
   * &lt;value&gt;org.pentaho.platform.scheduler2.blockout.BlockoutAction&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;jobParams&gt;
   * &lt;name&gt;lineage-id&lt;/name&gt;
   * &lt;value&gt;0989726c-3247-4864-bc79-8e2a1dc60c58&lt;/value&gt;
   * &lt;/jobParams&gt;
   * &lt;/jobParams&gt;
   * &lt;jobTrigger xsi:type="complexJobTrigger"&gt;
   * &lt;cronString&gt;0 12 10 ? * 2,3,4,5,6 *&lt;/cronString&gt;
   * &lt;duration&gt;10080000&lt;/duration&gt;
   * &lt;startTime&gt;2014-08-19T10:12:00-04:00&lt;/startTime&gt;
   * &lt;uiPassParam&gt;DAILY&lt;/uiPassParam&gt;
   * &lt;dayOfMonthRecurrences /&gt;
   * &lt;dayOfWeekRecurrences&gt;
   * &lt;recurrenceList&gt;
   * &lt;values&gt;2&lt;/values&gt;
   * &lt;values&gt;3&lt;/values&gt;
   * &lt;values&gt;4&lt;/values&gt;
   * &lt;values&gt;5&lt;/values&gt;
   * &lt;values&gt;6&lt;/values&gt;
   * &lt;/recurrenceList&gt;
   * &lt;/dayOfWeekRecurrences&gt;
   * &lt;hourlyRecurrences&gt;
   * &lt;recurrenceList&gt;
   * &lt;values&gt;10&lt;/values&gt;
   * &lt;/recurrenceList&gt;
   * &lt;/hourlyRecurrences&gt;
   * &lt;minuteRecurrences&gt;
   * &lt;recurrenceList&gt;
   * &lt;values&gt;12&lt;/values&gt;
   * &lt;/recurrenceList&gt;
   * &lt;/minuteRecurrences&gt;
   * &lt;monthlyRecurrences /&gt;
   * &lt;secondRecurrences&gt;
   * &lt;recurrenceList&gt;
   * &lt;values&gt;0&lt;/values&gt;
   * &lt;/recurrenceList&gt;
   * &lt;/secondRecurrences&gt;
   * &lt;yearlyRecurrences /&gt;
   * &lt;/jobTrigger&gt;
   * &lt;nextRun&gt;2014-08-20T10:12:00-04:00&lt;/nextRun&gt;
   * &lt;state&gt;NORMAL&lt;/state&gt;
   * &lt;userName&gt;admin&lt;/userName&gt;
   * &lt;/job&gt;
   * &lt;/jobs&gt;
   * </pre>
   */
  @GET
  @Path ( "/blockout/blockoutjobs" )
  @Produces ( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully retrieved blockout jobs." ),
  } )
  public List<Job> getBlockoutJobs() {
    return schedulerService.getBlockOutJobs();
  }


  /**
   * Checks if there are blockouts in the system
   * <p/>
   * <p><b>Example Request:</b><br />
   * GET api/scheduler/blockout/hasblockouts
   * </p>
   *
   * @return true or false whether there are blackouts or not
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * true
   * </pre>
   */
  @GET
  @Path ( "/blockout/hasblockouts" )
  @Produces ( { TEXT_PLAIN } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully determined whether or not the system contains blockouts." ),
  } )
  public Response hasBlockouts() {
    Boolean hasBlockouts = schedulerService.hasBlockouts();
    return buildOkResponse( hasBlockouts.toString() );
  }

  /**
   * Creates a new blockout for scheduled jobs
   * <p/>
   * <p><b>Example Request:</b><br />
   * POST api/scheduler/blockout/add
   * </p>
   *
   * @param jobScheduleRequest A JobScheduleRequest object defining the blockout job
   *                           <pre function="syntax.xml">
   *                           &lt;jobScheduleRequest&gt;
   *                           &lt;jobName&gt;DAILY-1820438815:admin:7740000&lt;/jobName&gt;
   *                           &lt;complexJobTrigger&gt;
   *                           &lt;uiPassParam&gt;DAILY&lt;/uiPassParam&gt;
   *                           &lt;daysOfWeek&gt;1&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;2&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;3&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;4&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;5&lt;/daysOfWeek&gt;
   *                           &lt;startTime&gt;2014-08-19T10:51:00.000-04:00&lt;/startTime&gt;
   *                           &lt;endTime /&gt;
   *                           &lt;/complexJobTrigger&gt;
   *                           &lt;inputFile&gt;&lt;/inputFile&gt;
   *                           &lt;outputFile&gt;&lt;/outputFile&gt;
   *                           &lt;duration&gt;7740000&lt;/duration&gt;
   *                           &lt;timeZone&gt;America/New_York&lt;/timeZone&gt;
   *                           &lt;/jobScheduleRequest&gt;
   *                           </pre>
   * @return a Response object which contains the ID of the blockout which was created
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * admin	BlockoutAction	1408459814192
   * </pre>
   */
  @POST
  @Path ( "/blockout/add" )
  @Consumes ( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successful operation." ),
      @ResponseCode ( code = 401, condition = "User is not authorized to create blockout." )
  } )
  public Response addBlockout( JobScheduleRequest jobScheduleRequest ) {
    try {
      Job job = schedulerService.addBlockout( jobScheduleRequest );
      return buildPlainTextOkResponse( job.getJobId() );
    } catch ( IOException e ) {
      return buildStatusResponse( UNAUTHORIZED );
    } catch ( SchedulerException e ) {
      return buildStatusResponse( UNAUTHORIZED );
    } catch ( IllegalAccessException e ) {
      return buildStatusResponse( UNAUTHORIZED );
    }
  }

  /**
   * Update an existing blockout
   * <p/>
   * <p><b>Example Request:</b><br />
   * POST api/scheduler/blockout/update
   * </p>
   *
   * @param jobId              The jobId of the blockout we are editing
   *                           <pre function="syntax.xml">
   *                           admin%09BlockoutAction%091408459814192
   *                           </pre>
   * @param jobScheduleRequest The payload containing the definition of the blockout
   *                           <pre function="syntax.xml">
   *                           &lt;jobScheduleRequest&gt;
   *                           &lt;jobName&gt;DAILY-1820438815:admin:7740000&lt;/jobName&gt;
   *                           &lt;complexJobTrigger&gt;
   *                           &lt;uiPassParam&gt;DAILY&lt;/uiPassParam&gt;
   *                           &lt;daysOfWeek&gt;1&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;2&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;3&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;4&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;5&lt;/daysOfWeek&gt;
   *                           &lt;startTime&gt;2012-01-12T10:51:00.000-04:00&lt;/startTime&gt;
   *                           &lt;endTime /&gt;
   *                           &lt;/complexJobTrigger&gt;
   *                           &lt;inputFile&gt;&lt;/inputFile&gt;
   *                           &lt;outputFile&gt;&lt;/outputFile&gt;
   *                           &lt;duration&gt;7740000&lt;/duration&gt;
   *                           &lt;timeZone&gt;America/New_York&lt;/timeZone&gt;
   *                           &lt;/jobScheduleRequest&gt;
   *                           </pre>
   * @return a Response object which contains the ID of the blockout which was created
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * admin	BlockoutAction	1408473190419
   * </pre>
   */
  @POST
  @Path ( "/blockout/update" )
  @Consumes ( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successful operation." ),
      @ResponseCode ( code = 401, condition = "User is not authorized to update blockout." )
  } )
  public Response updateBlockout( @QueryParam ( "jobid" ) String jobId, JobScheduleRequest jobScheduleRequest ) {
    try {
      Job job = schedulerService.updateBlockout( jobId, jobScheduleRequest );
      return buildPlainTextOkResponse( job.getJobId() );
    } catch ( IOException e ) {
      return buildStatusResponse( Status.UNAUTHORIZED );
    } catch ( SchedulerException e ) {
      return buildStatusResponse( Status.UNAUTHORIZED );
    } catch ( IllegalAccessException e ) {
      return buildStatusResponse( Status.UNAUTHORIZED );
    }
  }

  /**
   * Checks if the selected blockout schedule will be fired
   * <p/>
   * <p><b>Example Request:</b><br />
   * POST api/scheduler/blockout/willFire
   * </p>
   *
   * @param jobScheduleRequest The payload containing the definition of the blockout
   *                           <pre function="syntax.xml">
   *                           &lt;jobScheduleRequest&gt;
   *                           &lt;jobName&gt;DAILY-1820438815:admin:7740000&lt;/jobName&gt;
   *                           &lt;complexJobTrigger&gt;
   *                           &lt;uiPassParam&gt;DAILY&lt;/uiPassParam&gt;
   *                           &lt;daysOfWeek&gt;1&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;2&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;3&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;4&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;5&lt;/daysOfWeek&gt;
   *                           &lt;startTime&gt;2012-01-12T10:51:00.000-04:00&lt;/startTime&gt;
   *                           &lt;endTime /&gt;
   *                           &lt;/complexJobTrigger&gt;
   *                           &lt;inputFile&gt;&lt;/inputFile&gt;
   *                           &lt;outputFile&gt;&lt;/outputFile&gt;
   *                           &lt;duration&gt;7740000&lt;/duration&gt;
   *                           &lt;timeZone&gt;America/New_York&lt;/timeZone&gt;
   *                           &lt;/jobScheduleRequest&gt;
   *                           </pre>
   * @return true or false indicating whether or not the blockout will fire
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * true
   * </pre>
   */
  @POST
  @Path ( "/blockout/willFire" )
  @Consumes ( { APPLICATION_JSON, APPLICATION_XML } )
  @Produces ( { TEXT_PLAIN } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successful operation." ),
      @ResponseCode ( code = 500, condition = "An error occurred while determining blockouts being fired." )
  } )
  public Response blockoutWillFire( JobScheduleRequest jobScheduleRequest ) {
    Boolean willFire;
    try {
      willFire = schedulerService.willFire( convertScheduleRequestToJobTrigger( jobScheduleRequest ) );
    } catch ( UnifiedRepositoryException e ) {
      return buildServerErrorResponse( e );
    } catch ( SchedulerException e ) {
      return buildServerErrorResponse( e );
    }
    return buildOkResponse( willFire.toString() );
  }

  /**
   * Checks if the selected blockout schedule should be fired now
   * <p/>
   * <p><b>Example Request:</b><br />
   * GET api/scheduler/blockout/shouldFireNow
   * </p>
   *
   * @return true or false whether or not the blockout should fire now
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * true
   * </pre>
   */
  @GET
  @Path ( "/blockout/shouldFireNow" )
  @Produces ( { TEXT_PLAIN } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successful operation." )
  } )
  public Response shouldFireNow() {
    Boolean result = schedulerService.shouldFireNow();
    return buildOkResponse( result.toString() );
  }


  /**
   * Check the status of the selected blockout schedule.
   * <p/>
   * <p><b>Example Request:</b><br />
   * POST api/scheduler/blockout/blockstatus
   * </p>
   *
   * @param jobScheduleRequest The payload containing the definition of the blockout
   *                           <pre function="syntax.xml">
   *                           &lt;jobScheduleRequest&gt;
   *                           &lt;jobName&gt;DAILY-1820438815:admin:7740000&lt;/jobName&gt;
   *                           &lt;complexJobTrigger&gt;
   *                           &lt;uiPassParam&gt;DAILY&lt;/uiPassParam&gt;
   *                           &lt;daysOfWeek&gt;1&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;2&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;3&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;4&lt;/daysOfWeek&gt;
   *                           &lt;daysOfWeek&gt;5&lt;/daysOfWeek&gt;
   *                           &lt;startTime&gt;2012-01-12T10:51:00.000-04:00&lt;/startTime&gt;
   *                           &lt;endTime /&gt;
   *                           &lt;/complexJobTrigger&gt;
   *                           &lt;inputFile&gt;&lt;/inputFile&gt;
   *                           &lt;outputFile&gt;&lt;/outputFile&gt;
   *                           &lt;duration&gt;7740000&lt;/duration&gt;
   *                           &lt;timeZone&gt;America/New_York&lt;/timeZone&gt;
   *                           &lt;/jobScheduleRequest&gt;
   *                           </pre>
   * @return a Response object which contains a BlockStatusProxy which contains totallyBlocked and partiallyBlocked flags
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * &lt;blockStatusProxy&gt;
   * &lt;partiallyBlocked&gt;true&lt;/partiallyBlocked&gt;
   * &lt;totallyBlocked&gt;true&lt;/totallyBlocked&gt;
   * &lt;/blockStatusProxy&gt;
   * </pre>
   */
  @POST
  @Path ( "/blockout/blockstatus" )
  @Consumes ( { APPLICATION_JSON, APPLICATION_XML } )
  @Produces ( { APPLICATION_JSON, APPLICATION_XML } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully got the blockout status." ),
      @ResponseCode ( code = 401, condition = "User is not authorized to get the blockout status." )
  } )
  public Response getBlockStatus( JobScheduleRequest jobScheduleRequest ) {
    try {
      BlockStatusProxy blockStatusProxy = schedulerService.getBlockStatus( jobScheduleRequest );
      return buildOkResponse( blockStatusProxy );
    } catch ( SchedulerException e ) {
      return buildStatusResponse( Status.UNAUTHORIZED );
    }
  }

  /**
   * Retrieve the list of execute content by lineage id.
   * <p/>
   * <p><b>Example Request:</b><br />
   * GET api/scheduler/generatedContentForSchedule
   * </p>
   *
   * @param lineageId the path for the file
   *                  <pre function="syntax.xml">
   *                  :path:to:file:id
   *                  </pre>
   * @return list of RepositoryFileDto objects
   * <p/>
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   * &lt;List&gt;
   * &lt;repositoryFileDto&gt;
   * &lt;createdDate&gt;1402911997019&lt;/createdDate&gt;
   * &lt;fileSize&gt;3461&lt;/fileSize&gt;
   * &lt;folder&gt;false&lt;/folder&gt;
   * &lt;hidden&gt;false&lt;/hidden&gt;
   * &lt;id&gt;ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/id&gt;
   * &lt;lastModifiedDate&gt;1406647160536&lt;/lastModifiedDate&gt;
   * &lt;locale&gt;en&lt;/locale&gt;
   * &lt;localePropertiesMapEntries&gt;
   * &lt;localeMapDto&gt;
   * &lt;locale&gt;default&lt;/locale&gt;
   * &lt;properties&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;file.title&lt;/key&gt;
   * &lt;value&gt;myFile&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;jcr:primaryType&lt;/key&gt;
   * &lt;value&gt;nt:unstructured&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;title&lt;/key&gt;
   * &lt;value&gt;myFile&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;file.description&lt;/key&gt;
   * &lt;value&gt;myFile Description&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;/properties&gt;
   * &lt;/localeMapDto&gt;
   * &lt;/localePropertiesMapEntries&gt;
   * &lt;locked&gt;false&lt;/locked&gt;
   * &lt;name&gt;myFile.prpt&lt;/name&gt;&lt;/name&gt;
   * &lt;originalParentFolderPath&gt;/public/admin&lt;/originalParentFolderPath&gt;
   * &lt;ownerType&gt;-1&lt;/ownerType&gt;
   * &lt;path&gt;/public/admin/ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/path&gt;
   * &lt;title&gt;myFile&lt;/title&gt;
   * &lt;versionId&gt;1.9&lt;/versionId&gt;
   * &lt;versioned&gt;true&lt;/versioned&gt;
   * &lt;/repositoryFileAclDto&gt;
   * &lt;/List&gt;
   * </pre>
   */
  @GET
  @Path ( "/generatedContentForSchedule" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully got the generated content for schedule" )
  } )
  public List<RepositoryFileDto> doGetGeneratedContentForSchedule( @QueryParam ( "lineageId" ) String lineageId ) {
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

  protected Response buildOkResponse( Object entity ) {
    return Response.ok( entity ).build();
  }

  protected Response buildPlainTextOkResponse( String msg ) {
    return Response.ok( msg ).type( MediaType.TEXT_PLAIN ).build();
  }

  protected Response buildServerErrorResponse( Object entity ) {
    return Response.serverError().entity( entity ).build();
  }

  protected Response buildStatusResponse( Status status ) {
    return Response.status( status ).build();
  }

  protected Response buildPlainTextStatusResponse( Status status ) {
    return Response.status( status ).type( MediaType.TEXT_PLAIN ).build();
  }

  protected JobRequest getJobRequest() {
    return new JobRequest();
  }

  protected IJobTrigger convertScheduleRequestToJobTrigger( JobScheduleRequest request ) throws SchedulerException {
    return SchedulerResourceUtil.convertScheduleRequestToJobTrigger( request, schedulerService.getScheduler() );
  }
}
