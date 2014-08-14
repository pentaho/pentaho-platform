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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
import org.codehaus.enunciate.doc.ExcludeFromDocumentation;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.Job.JobState;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;
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
   * Creates a new job/schedule with the values from the supplied XML document.
   *
   * @param scheduleRequest <pre function="syntax.xml">
   *
   *                        </pre>
   * @return Returns response based on the success of the operation
   */
  @POST
  @Path( "/job" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  @Produces( "text/plain" )
  @StatusCodes({
    @ResponseCode( code = 200, condition = "Schedule created successfully." ),
    @ResponseCode( code = 401, condition = "User is not allowed to create schedules." ),
    @ResponseCode( code = 403, condition = "Cannot create schedules for the specified file." ),
    @ResponseCode( code = 500, condition = "An error occurred while completing the operation." )
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
   * Execute a selected job/schedule
   *
   * @param jobRequest <code> JobScheduleRequest </code>
   * @return A response object
   */
  @POST
  @Path( "/triggerNow" )
  @Produces( "text/plain" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response triggerNow( JobRequest jobRequest ) {
    try {
      Job job = schedulerService.triggerNow( jobRequest.getJobId() );
      return Response.ok( job.getState().name() ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Return the content cleaner Job/Schedule
   *
   * @return <code> Job </code>
   */
  @GET
  @Path( "/getContentCleanerJob" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
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
   * @param asCronString (Cron string) - UNUSED
   * @return list of <code> Job </code>
   */
  @Deprecated
  @GET
  @Path( "/jobs" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
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
   * Checks if a file can be scheduled by the user
   *<p>
   * Checks whether the current user has authority to schedule any content in the platform and the selected report is
   * schedule able
   *
   * @param id The repository file ID of the content to checked <pre function="syntax.xml">
   *
   *                        </pre>
   * @return ("true" or "false")
   */
  @GET
  @Path( "/isScheduleAllowed" )
  @Produces( TEXT_PLAIN )
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
      Job job = schedulerService.getJob( jobId );
      if ( SecurityHelper.getInstance().isPentahoAdministrator( PentahoSessionHolder.getSession() )
        || PentahoSessionHolder.getSession().getName().equals( job.getUserName() ) ) {
        for ( String key : job.getJobParams().keySet() ) {
          Serializable value = job.getJobParams().get( key );
          if ( value.getClass().isArray() ) {
            String[] sa = ( new String[ 0 ] ).getClass().cast( value );
            ArrayList<String> list = new ArrayList<String>();
            for ( int i = 0; i < sa.length; i++ ) {
              list.add( sa[ i ] );
            }
            job.getJobParams().put( key, list );
          }
        }
        return job;
      } else {
        throw new RuntimeException( "Job not found or improper credentials for access" );
      }
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
    JobScheduleRequest jobRequest = new JobScheduleRequest();
    ComplexJobTriggerProxy proxyTrigger = new ComplexJobTriggerProxy();
    proxyTrigger.setDaysOfMonth( new int[] { 1, 2, 3 } );
    proxyTrigger.setDaysOfWeek( new int[] { 1, 2, 3 } );
    proxyTrigger.setMonthsOfYear( new int[] { 1, 2, 3 } );
    proxyTrigger.setYears( new int[] { 2012, 2013 } );
    proxyTrigger.setStartTime( new Date() );
    jobRequest.setComplexJobTrigger( proxyTrigger );
    jobRequest.setInputFile( "aaaaa" );
    jobRequest.setOutputFile( "bbbbb" );
    ArrayList<JobScheduleParam> jobParams = new ArrayList<JobScheduleParam>();
    jobParams.add( new JobScheduleParam( "param1", "aString" ) );
    jobParams.add( new JobScheduleParam( "param2", 1 ) );
    jobParams.add( new JobScheduleParam( "param3", true ) );
    jobParams.add( new JobScheduleParam( "param4", new Date() ) );
    jobRequest.setJobParameters( jobParams );
    return jobRequest;
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
