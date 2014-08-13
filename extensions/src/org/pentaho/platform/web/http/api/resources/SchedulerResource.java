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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.scheduler2.blockout.BlockoutAction;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.SchedulerAction;
import org.pentaho.platform.web.http.api.resources.services.SchedulerService;

/**
 * Represents a file node in the repository. This api provides methods for discovering information about repository
 * files as well as CRUD operations
 *
 * @author aaron
 */
@Path( "/scheduler" )
public class SchedulerResource extends AbstractJaxRSResource {

  protected IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$

  protected IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class );

  protected IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );

  private SchedulerService schedulerService;


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
      Job job = schedulerService.triggerNow( jobRequest );
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
   * @param asCronString (Cron string)
   * @return list of <code> Job </code>
   */
  @GET
  @Path( "/jobs" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public List<Job> getJobs( @DefaultValue( "false" ) @QueryParam( "asCronString" ) Boolean asCronString ) {
    try {
      IPentahoSession session = PentahoSessionHolder.getSession();
      final String principalName = session.getName(); // this authentication wasn't matching with the job user name,
                                                      // changed to get name via the current session
      final Boolean canAdminister = canAdminister( session );

      List<Job> jobs = scheduler.getJobs( new IJobFilter() {
        public boolean accept( Job job ) {
          if ( canAdminister ) {
            return !IBlockoutManager.BLOCK_OUT_JOB_NAME.equals( job.getJobName() );
          }
          return principalName.equals( job.getUserName() );
        }
      } );
      return jobs;
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  private Boolean canAdminister( IPentahoSession session ) {
    if ( policy.isAllowed( AdministerSecurityAction.NAME ) ) {
      return true;
    }
    return false;
  }

  /**
   * Checks whether the current user has authority to schedule any content in the platform and the selected report is
   * schedule able
   *
   * @param id (Repository file ID of the report that is being scheduled
   * @return ("true" or "false")
   */
  @GET
  @Path( "/isScheduleAllowed" )
  @Produces( TEXT_PLAIN )
  public String isScheduleAllowed( @QueryParam( "id" ) String id ) {
    Boolean canSchedule = false;
    canSchedule = policy.isAllowed( SchedulerAction.NAME );
    if ( canSchedule ) {
      Map<String, Serializable> metadata = repository.getFileMetadata( id );
      if ( metadata.containsKey( "_PERM_SCHEDULABLE" ) ) {
        canSchedule = Boolean.parseBoolean( (String) metadata.get( "_PERM_SCHEDULABLE" ) );
      }
    }
    return "" + canSchedule; //$NON-NLS-1$
  }

  /**
   * Checks whether the current user has authority to schedule any content in the platform
   *
   * @return ("true" or "false")
   */
  @GET
  @Path( "/canSchedule" )
  @Produces( TEXT_PLAIN )
  public String doGetCanSchedule() {
    Boolean isAllowed = policy.isAllowed( SchedulerAction.NAME );
    return isAllowed ? "true" : "false"; //$NON-NLS-1$//$NON-NLS-2$
  }

  /**
   * Returns the state of the scheduler (Schedule could be either paused or normal)
   *
   * @return status of the scheduler
   */
  @GET
  @Path( "/state" )
  @Produces( "text/plain" )
  public Response getState() {
    try {
      return Response.ok( scheduler.getStatus().name() ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * If the scheduler is in the PAUSE state, this will resume the scheduler
   *
   * @return
   */
  @POST
  @Path( "/start" )
  @Produces( "text/plain" )
  public Response start() {
    try {
      if ( policy.isAllowed( SchedulerAction.NAME ) ) {
        scheduler.start();
      }
      return Response.ok( scheduler.getStatus().name() ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * If the schedule in the state of "NORMAL", this will "PAUSE" the scheduler
   *
   * @return
   */
  @POST
  @Path( "/pause" )
  @Produces( "text/plain" )
  public Response pause() {
    try {
      if ( policy.isAllowed( SchedulerAction.NAME ) ) {
        scheduler.pause();
      }
      return Response.ok( scheduler.getStatus().name() ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( SchedulerException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Shuts down the scheduler
   *
   * @return
   */
  @POST
  @Path( "/shutdown" )
  @Produces( "text/plain" )
  public Response shutdown() {
    try {
      if ( policy.isAllowed( SchedulerAction.NAME ) ) {
        scheduler.shutdown();
      }
      return Response.ok( scheduler.getStatus().name() ).type( MediaType.TEXT_PLAIN ).build();
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
      Job job = scheduler.getJob( jobRequest.getJobId() );
      if ( policy.isAllowed( SchedulerAction.NAME ) ) {
        return Response.ok( job.getState().name() ).type( MediaType.TEXT_PLAIN ).build();
      } else {
        if ( PentahoSessionHolder.getSession().getName().equals( job.getUserName() ) ) {
          return Response.ok( job.getState().name() ).type( MediaType.TEXT_PLAIN ).build();
        }
      }
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
      Job job = scheduler.getJob( jobRequest.getJobId() );
      if ( policy.isAllowed( SchedulerAction.NAME ) ) {
        scheduler.pauseJob( jobRequest.getJobId() );
      } else {
        if ( PentahoSessionHolder.getSession().getName().equals( job.getUserName() ) ) {
          scheduler.pauseJob( jobRequest.getJobId() );
        }
      }
      // update job state
      job = scheduler.getJob( jobRequest.getJobId() );
      return Response.ok( job.getState().name() ).type( MediaType.TEXT_PLAIN ).build();
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
      Job job = scheduler.getJob( jobRequest.getJobId() );
      if ( policy.isAllowed( SchedulerAction.NAME ) ) {
        scheduler.resumeJob( jobRequest.getJobId() );
      } else {
        if ( PentahoSessionHolder.getSession().getName().equals( job.getUserName() ) ) {
          scheduler.resumeJob( jobRequest.getJobId() );
        }
      }
      // udpate job state
      job = scheduler.getJob( jobRequest.getJobId() );
      return Response.ok( job.getState().name() ).type( MediaType.TEXT_PLAIN ).build();
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
      Job job = scheduler.getJob( jobRequest.getJobId() );
      if ( policy.isAllowed( SchedulerAction.NAME ) ) {
        scheduler.removeJob( jobRequest.getJobId() );
        return Response.ok( "REMOVED" ).type( MediaType.TEXT_PLAIN ).build();
      } else {
        if ( PentahoSessionHolder.getSession().getName().equals( job.getUserName() ) ) {
          scheduler.removeJob( jobRequest.getJobId() );
          return Response.ok( "REMOVED" ).type( MediaType.TEXT_PLAIN ).build();
        }
      }
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
      Job job = scheduler.getJob( jobId );
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

  @GET
  @Path( "/jobinfotest" )
  @Produces( { APPLICATION_JSON } )
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
   * @return a Response object which contains one of the following:
   * 1) the ID of the blockout which was created
   * 2) a response code of 401 (UNAUTHORIZED) - user not allowed to create blockout
   * 
   * @throws IOException
   */
  @POST
  @Path( "/blockout/createBlockout" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  public Response addBlockout( JobScheduleRequest request ) throws IOException {
    if ( policy.isAllowed( SchedulerAction.NAME ) ) {
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
   * @return a Response object which contains one of the following:
   * 1) the ID of the blockout which was created
   * 2) a response code of 401 (UNAUTHORIZED) - user not allowed to create blockout
   * 
   * @throws IOException
   */
  @POST
  @Path( "/blockout/updateBlockout" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  public Response updateBlockout( @QueryParam( "jobid" ) String jobId, JobScheduleRequest request ) throws IOException {
    if ( policy.isAllowed( SchedulerAction.NAME ) ) {
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
   * @return a Response object which contains one of the following:
   * 1) true/false if the provided schedule (blockout) will fire
   * 2) a response code of 500 (Internal Server Error) with a root cause of:
   * A) UnifiedRepositoryException
   * B) SchedulerException
   */
  @GET
  @Path( "/blockout/blockoutWillFire" )
  @Consumes( { APPLICATION_JSON, APPLICATION_XML } )
  @Produces( { TEXT_PLAIN } )
  public Response blockoutWillFire( JobScheduleRequest request ) {
    Boolean willFire;
    try {
      willFire =
        schedulerService.willFire( SchedulerResourceUtil.convertScheduleRequestToJobTrigger( request, scheduler ) );
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
    IJobTrigger trigger = SchedulerResourceUtil.convertScheduleRequestToJobTrigger( request, scheduler );
    return Response.ok( schedulerService.getBlockStatus( trigger ) ).build();
  }
}
