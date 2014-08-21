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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.web.http.api.resources.proxies.BlockStatusProxy;
import org.pentaho.platform.web.http.api.resources.services.SchedulerService;

import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.List;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class SchedulerResourceTest {

  SchedulerResource schedulerResource;

  @Before
  public void setUp() {
    schedulerResource = spy( new SchedulerResource() );
    schedulerResource.schedulerService = mock( SchedulerService.class );
  }

  @After
  public void tearDown() {
    schedulerResource = null;
  }

  @Test
  public void testCreateJob() throws Exception {
    JobScheduleRequest mockRequest = mock( JobScheduleRequest.class );

    Job mockJob = mock( Job.class );
    doReturn( mockJob ).when( schedulerResource.schedulerService ).createJob( mockRequest );

    String jobId = "jobId";
    doReturn( jobId ).when( mockJob ).getJobId();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildPlainTextOkResponse( jobId );

    Response testResponse = schedulerResource.createJob( mockRequest );
    assertEquals( mockResponse, testResponse );

    verify( schedulerResource.schedulerService, times( 1 ) ).createJob( mockRequest );
    verify( mockJob, times( 1 ) ).getJobId();
    verify( schedulerResource, times( 1 ) ).buildPlainTextOkResponse( jobId );
  }

  @Test
  public void testCreateJobError() throws Exception {
    JobScheduleRequest mockRequest = mock( JobScheduleRequest.class );

    SchedulerException mockSchedulerException = mock( SchedulerException.class );

    Throwable mockSchedulerExceptionCause = mock( Throwable.class );
    doReturn( mockSchedulerExceptionCause ).when( mockSchedulerException ).getCause();

    String schedulerExceptionMessage = "schedulerExceptionMessage";
    doReturn( schedulerExceptionMessage ).when( mockSchedulerExceptionCause ).getMessage();

    Response mockSchedulerExceptionResponse = mock( Response.class );
    doReturn( mockSchedulerExceptionResponse ).when( schedulerResource )
      .buildServerErrorResponse( schedulerExceptionMessage );

    IOException mockIOException = mock( IOException.class );

    Throwable mockIOExceptionCause = mock( Throwable.class );
    doReturn( mockIOExceptionCause ).when( mockIOException ).getCause();

    String ioExceptionMessage = "ioExceptionMessage";
    doReturn( ioExceptionMessage ).when( mockIOExceptionCause ).getMessage();

    Response mockIOExceptionResponse = mock( Response.class );
    doReturn( mockIOExceptionResponse ).when( schedulerResource ).buildServerErrorResponse( ioExceptionMessage );

    Response mockUnauthorizedResponse = mock( Response.class );
    doReturn( mockUnauthorizedResponse ).when( schedulerResource ).buildStatusResponse( UNAUTHORIZED );

    Response mockForbiddenResponse = mock( Response.class );
    doReturn( mockForbiddenResponse ).when( schedulerResource ).buildStatusResponse( FORBIDDEN );

    // Test 1
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).createJob( mockRequest );

    Response testResponse = schedulerResource.createJob( mockRequest );
    assertEquals( mockSchedulerExceptionResponse, testResponse );

    // Test 2
    doThrow( mockIOException ).when( schedulerResource.schedulerService ).createJob( mockRequest );

    testResponse = schedulerResource.createJob( mockRequest );
    assertEquals( mockIOExceptionResponse, testResponse );

    // Test 3
    SecurityException mockSecurityException = mock( SecurityException.class );
    doThrow( mockSecurityException ).when( schedulerResource.schedulerService ).createJob( mockRequest );

    testResponse = schedulerResource.createJob( mockRequest );
    assertEquals( mockUnauthorizedResponse, testResponse );

    // Test 4
    IllegalAccessException mockIllegalAccessException = mock( IllegalAccessException.class );
    doThrow( mockIllegalAccessException ).when( schedulerResource.schedulerService ).createJob( mockRequest );

    testResponse = schedulerResource.createJob( mockRequest );
    assertEquals( mockForbiddenResponse, testResponse );

    verify( mockSchedulerException, times( 1 ) ).getCause();
    verify( mockSchedulerExceptionCause, times( 1 ) ).getMessage();
    verify( schedulerResource, times( 1 ) ).buildServerErrorResponse( schedulerExceptionMessage );
    verify( mockIOException, times( 1 ) ).getCause();
    verify( mockIOExceptionCause, times( 1 ) ).getMessage();
    verify( schedulerResource, times( 1 ) ).buildServerErrorResponse( ioExceptionMessage );
    verify( schedulerResource, times( 1 ) ).buildStatusResponse( UNAUTHORIZED );
    verify( schedulerResource, times( 1 ) ).buildStatusResponse( FORBIDDEN );
    verify( schedulerResource.schedulerService, times( 4 ) ).createJob( mockRequest );
  }

  @Test
  public void testTriggerNow() throws Exception {
    JobRequest mockJobRequest = mock( JobRequest.class );

    String jobId = "jobId";
    doReturn( jobId ).when( mockJobRequest ).getJobId();

    Job mockJob = mock( Job.class );
    doReturn( mockJob ).when( schedulerResource.schedulerService ).triggerNow( jobId );

    Job.JobState mockJobState = Job.JobState.BLOCKED;
    doReturn( mockJobState ).when( mockJob ).getState();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildPlainTextOkResponse( mockJobState.name() );

    Response testResponse = schedulerResource.triggerNow( mockJobRequest );
    assertEquals( mockResponse, testResponse );

    verify( mockJobRequest, times( 1 ) ).getJobId();
    verify( schedulerResource.schedulerService, times( 1 ) ).triggerNow( jobId );
    verify( mockJob, times( 1 ) ).getState();
    verify( schedulerResource, times( 1 ) ).buildPlainTextOkResponse( mockJobState.name() );
  }

  @Test
  public void testTriggerNowError() throws Exception {
    JobRequest mockJobRequest = mock( JobRequest.class );

    String jobId = "jobId";
    doReturn( jobId ).when( mockJobRequest ).getJobId();

    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).triggerNow( jobId );

    try {
      schedulerResource.triggerNow( mockJobRequest );
      fail();
    } catch ( RuntimeException e ) {
      // correct
    }

    verify( mockJobRequest, times( 1 ) ).getJobId();
    verify( schedulerResource.schedulerService, times( 1 ) ).triggerNow( jobId );
  }

  @Test
  public void testGetContentCleanerJob() throws Exception {
    Job mockJob = mock( Job.class );
    doReturn( mockJob ).when( schedulerResource.schedulerService ).getContentCleanerJob();

    Job testJob = schedulerResource.getContentCleanerJob();
    assertEquals( mockJob, testJob );

    verify( schedulerResource.schedulerService, times( 1 ) ).getContentCleanerJob();
  }

  @Test
  public void testGetContentCleanerJobError() throws Exception {
    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).getContentCleanerJob();

    try {
      schedulerResource.getContentCleanerJob();
      fail();
    } catch ( RuntimeException e ) {
      // correct
    }

    verify( schedulerResource.schedulerService, times( 1 ) ).getContentCleanerJob();
  }

  @Test
  public void testGetJobs() throws Exception {
    List<Job> mockJobs = mock( List.class );
    doReturn( mockJobs ).when( schedulerResource.schedulerService ).getJobs();

    Boolean asCronString = Boolean.FALSE;

    List<Job> testJobs = schedulerResource.getJobs( asCronString );
    assertEquals( mockJobs, testJobs );

    verify( schedulerResource.schedulerService, times( 1 ) ).getJobs();
  }

  @Test
  public void testGetJobsError() throws Exception {
    Boolean asCronString = Boolean.FALSE;

    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).getJobs();

    try {
      schedulerResource.getJobs( asCronString );
      fail();
    } catch ( RuntimeException e ) {
      // correct
    }

    verify( schedulerResource.schedulerService, times( 1 ) ).getJobs();
  }

  @Test
  public void testIsScheduleAllowed() {
    String id = "id";

    boolean isScheduleAllowed = true;
    doReturn( isScheduleAllowed ).when( schedulerResource.schedulerService ).isScheduleAllowed( id );

    String testResult = schedulerResource.isScheduleAllowed( id );
    assertEquals( "" + isScheduleAllowed, testResult );

    verify( schedulerResource.schedulerService, times( 1 ) ).isScheduleAllowed( id );
  }

  @Test
  public void testDoGetCanSchedule() {
    String canSchedule = "true";
    doReturn( canSchedule ).when( schedulerResource.schedulerService ).doGetCanSchedule();

    String testResult = schedulerResource.doGetCanSchedule();
    assertEquals( canSchedule, testResult );

    verify( schedulerResource.schedulerService, times( 1 ) ).doGetCanSchedule();
  }

  @Test
  public void testGetState() throws Exception {
    String state = "state";
    doReturn( state ).when( schedulerResource.schedulerService ).getState();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildPlainTextOkResponse( state );

    Response testResult = schedulerResource.getState();
    assertEquals( mockResponse, testResult );

    verify( schedulerResource.schedulerService, times( 1 ) ).getState();
    verify( schedulerResource, times( 1 ) ).buildPlainTextOkResponse( state );
  }

  @Test
  public void testGetStateError() throws Exception {
    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).getState();

    try {
      schedulerResource.getState();
      fail();
    } catch ( RuntimeException e ) {
      // correct
    }

    verify( schedulerResource.schedulerService, times( 1 ) ).getState();
  }

  @Test
  public void testStart() throws Exception {
    String status = "state";
    doReturn( status ).when( schedulerResource.schedulerService ).start();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildPlainTextOkResponse( status );

    Response testResult = schedulerResource.start();
    assertEquals( mockResponse, testResult );

    verify( schedulerResource.schedulerService, times( 1 ) ).start();
    verify( schedulerResource, times( 1 ) ).buildPlainTextOkResponse( status );
  }

  @Test
  public void testStartError() throws Exception {
    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).start();

    try {
      schedulerResource.start();
      fail();
    } catch ( RuntimeException e ) {
      // correct
    }

    verify( schedulerResource.schedulerService, times( 1 ) ).start();
  }

  @Test
  public void testPause() throws Exception {
    String status = "state";
    doReturn( status ).when( schedulerResource.schedulerService ).pause();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildPlainTextOkResponse( status );

    Response testResult = schedulerResource.pause();
    assertEquals( mockResponse, testResult );

    verify( schedulerResource.schedulerService, times( 1 ) ).pause();
    verify( schedulerResource, times( 1 ) ).buildPlainTextOkResponse( status );
  }

  @Test
  public void testPauseError() throws Exception {
    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).pause();

    try {
      schedulerResource.pause();
      fail();
    } catch ( RuntimeException e ) {
      // correct
    }

    verify( schedulerResource.schedulerService, times( 1 ) ).pause();
  }

  @Test
  public void testShutdown() throws Exception {
    String status = "state";
    doReturn( status ).when( schedulerResource.schedulerService ).shutdown();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildPlainTextOkResponse( status );

    Response testResult = schedulerResource.shutdown();
    assertEquals( mockResponse, testResult );

    verify( schedulerResource.schedulerService, times( 1 ) ).shutdown();
    verify( schedulerResource, times( 1 ) ).buildPlainTextOkResponse( status );
  }

  @Test
  public void testShutdownError() throws Exception {
    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).shutdown();

    try {
      schedulerResource.shutdown();
      fail();
    } catch ( RuntimeException e ) {
      // correct
    }

    verify( schedulerResource.schedulerService, times( 1 ) ).shutdown();
  }

  @Test
  public void testGetJobState() throws Exception {
    JobRequest mockJobRequest = mock( JobRequest.class );

    Job.JobState mockJobState = Job.JobState.BLOCKED;
    doReturn( mockJobState ).when( schedulerResource.schedulerService ).getJobState( mockJobRequest );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildPlainTextOkResponse( mockJobState.name() );

    Response testResponse = schedulerResource.getJobState( mockJobRequest );
    assertEquals( mockResponse, testResponse );

    verify( schedulerResource.schedulerService, times( 1 ) ).getJobState( mockJobRequest );
    verify( schedulerResource, times( 1 ) ).buildPlainTextOkResponse( mockJobState.name() );
  }

  @Test
  public void testGetJobStateError() throws Exception {
    JobRequest mockJobRequest = mock( JobRequest.class );

    Response mockUnauthorizedResponse = mock( Response.class );
    doReturn( mockUnauthorizedResponse ).when( schedulerResource ).buildPlainTextStatusResponse( UNAUTHORIZED );

    // Test 1
    UnsupportedOperationException mockUnsupportedOperationException = mock( UnsupportedOperationException.class );
    doThrow( mockUnsupportedOperationException ).when( schedulerResource.schedulerService )
      .getJobState( mockJobRequest );

    Response testResponse = schedulerResource.getJobState( mockJobRequest );
    assertEquals( mockUnauthorizedResponse, testResponse );

    // Test 2
    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).getJobState( mockJobRequest );

    try {
      schedulerResource.getJobState( mockJobRequest );
      fail();
    } catch ( RuntimeException e ) {
      // correct
    }

    verify( schedulerResource, times( 1 ) ).buildPlainTextStatusResponse( UNAUTHORIZED );
    verify( schedulerResource.schedulerService, times( 2 ) ).getJobState( mockJobRequest );
  }

  @Test
  public void testPauseJob() throws Exception {
    String jobId = "jobId";

    JobRequest mockJobRequest = mock( JobRequest.class );
    doReturn( jobId ).when( mockJobRequest ).getJobId();

    Job.JobState state = Job.JobState.BLOCKED;
    doReturn( state ).when( schedulerResource.schedulerService ).pauseJob( jobId );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildPlainTextOkResponse( state.name() );

    Response testResult = schedulerResource.pauseJob( mockJobRequest );
    assertEquals( mockResponse, testResult );

    verify( schedulerResource.schedulerService, times( 1 ) ).pauseJob( jobId );
    verify( schedulerResource, times( 1 ) ).buildPlainTextOkResponse( state.name() );
  }

  @Test
  public void testPauseJobError() throws Exception {
    String jobId = "jobId";

    JobRequest mockJobRequest = mock( JobRequest.class );
    doReturn( jobId ).when( mockJobRequest ).getJobId();

    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).pauseJob( jobId );

    try {
      schedulerResource.pauseJob( mockJobRequest );
      fail();
    } catch ( RuntimeException e ) {
      // correct
    }

    verify( schedulerResource.schedulerService, times( 1 ) ).pauseJob( jobId );
  }

  @Test
  public void testResumeJob() throws Exception {
    String jobId = "jobId";

    JobRequest mockJobRequest = mock( JobRequest.class );
    doReturn( jobId ).when( mockJobRequest ).getJobId();

    Job.JobState state = Job.JobState.BLOCKED;
    doReturn( state ).when( schedulerResource.schedulerService ).resumeJob( jobId );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildPlainTextOkResponse( state.name() );

    Response testResult = schedulerResource.resumeJob( mockJobRequest );
    assertEquals( mockResponse, testResult );

    verify( schedulerResource.schedulerService, times( 1 ) ).resumeJob( jobId );
    verify( schedulerResource, times( 1 ) ).buildPlainTextOkResponse( state.name() );
  }

  @Test
  public void testResumeJobError() throws Exception {
    String jobId = "jobId";

    JobRequest mockJobRequest = mock( JobRequest.class );
    doReturn( jobId ).when( mockJobRequest ).getJobId();

    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).resumeJob( jobId );

    try {
      schedulerResource.resumeJob( mockJobRequest );
      fail();
    } catch ( RuntimeException e ) {
      // correct
    }

    verify( schedulerResource.schedulerService, times( 1 ) ).resumeJob( jobId );
  }

  @Test
  public void testRemoveJob() throws Exception {
    JobRequest mockJobRequest = mock( JobRequest.class );

    String jobId = "jobId";
    doReturn( jobId ).when( mockJobRequest ).getJobId();

    Job mockJob = mock( Job.class );
    doReturn( mockJob ).when( schedulerResource.schedulerService ).getJob( jobId );

    Job.JobState mockJobState = Job.JobState.BLOCKED;
    doReturn( mockJobState ).when( mockJob ).getState();

    Response mockRemovedResponse = mock( Response.class );
    doReturn( mockRemovedResponse ).when( schedulerResource ).buildPlainTextOkResponse( "REMOVED" );

    Response mockJobStateResponse = mock( Response.class );
    doReturn( mockJobStateResponse ).when( schedulerResource ).buildPlainTextOkResponse( mockJobState.name() );

    // Test 1
    doReturn( true ).when( schedulerResource.schedulerService ).removeJob( jobId );

    Response testResponse = schedulerResource.removeJob( mockJobRequest );
    assertEquals( mockRemovedResponse, testResponse );

    // Test 2
    doReturn( false ).when( schedulerResource.schedulerService ).removeJob( jobId );
    testResponse = schedulerResource.removeJob( mockJobRequest );
    assertEquals( mockJobStateResponse, testResponse );

    verify( mockJobRequest, times( 3 ) ).getJobId();
    verify( schedulerResource.schedulerService, times( 1 ) ).getJob( jobId );
    verify( mockJob, times( 1 ) ).getState();
    verify( schedulerResource, times( 1 ) ).buildPlainTextOkResponse( "REMOVED" );
    verify( schedulerResource, times( 1 ) ).buildPlainTextOkResponse( mockJobState.name() );
  }

  @Test
  public void testRemoveJobError() throws Exception {
    String jobId = "jobId";

    JobRequest mockJobRequest = mock( JobRequest.class );
    doReturn( jobId ).when( mockJobRequest ).getJobId();

    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).removeJob( jobId );

    try {
      schedulerResource.removeJob( mockJobRequest );
      fail();
    } catch ( RuntimeException e ) {
      // correct
    }

    verify( schedulerResource.schedulerService, times( 1 ) ).removeJob( jobId );
  }

  @Test
  public void testGetJob() throws Exception {
    String jobId = "jobId";
    String asCronString = "asCronString";

    Job mockJob = mock( Job.class );
    doReturn( mockJob ).when( schedulerResource.schedulerService ).getJobInfo( jobId );

    Job testJob = schedulerResource.getJob( jobId, asCronString );
    assertEquals( mockJob, testJob );

    verify( schedulerResource.schedulerService, times( 1 ) ).getJobInfo( jobId );
  }

  @Test
  public void testGetJobError() throws Exception {
    String jobId = "jobId";
    String asCronString = "asCronString";

    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).getJobInfo( jobId );

    try {
      schedulerResource.getJob( jobId, asCronString );
      fail();
    } catch ( RuntimeException e ) {
      // correct
    }

    verify( schedulerResource.schedulerService, times( 1 ) ).getJobInfo( jobId );
  }

  @Test
  public void testGetJobInfo() {
    JobScheduleRequest mockJobScheduleRequest = mock( JobScheduleRequest.class );
    doReturn( mockJobScheduleRequest ).when( schedulerResource.schedulerService ).getJobInfo();

    JobScheduleRequest testJobScheduleRequest = schedulerResource.getJobInfo();
    assertEquals( mockJobScheduleRequest, testJobScheduleRequest );

    verify( schedulerResource.schedulerService, times( 1 ) ).getJobInfo();
  }

  @Test
  public void testGetBlockoutJobs() {
    List<Job> mockJobs = mock( List.class );
    doReturn( mockJobs ).when( schedulerResource.schedulerService ).getBlockOutJobs();

    List<Job> blockoutJobs = schedulerResource.getBlockoutJobs();
    assertNotNull( blockoutJobs );

    verify( schedulerResource, times( 1 ) ).getBlockoutJobs();
  }

  @Test
  public void testHasBlockouts() {
    Boolean hasBlockouts = Boolean.FALSE;
    doReturn( hasBlockouts ).when( schedulerResource.schedulerService ).hasBlockouts();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildOkResponse( hasBlockouts.toString() );

    Response testResponse = schedulerResource.hasBlockouts();
    assertEquals( mockResponse, testResponse );

    verify( schedulerResource.schedulerService, times( 1 ) ).hasBlockouts();
    verify( schedulerResource, times( 1 ) ).buildOkResponse( hasBlockouts.toString() );
  }

  @Test
  public void testAddBlockout() throws Exception {
    JobScheduleRequest mockJobScheduleRequest = mock( JobScheduleRequest.class );

    Job mockJob = mock( Job.class );
    doReturn( mockJob ).when( schedulerResource.schedulerService ).addBlockout( mockJobScheduleRequest );

    String jobId = "jobId";
    doReturn( jobId ).when( mockJob ).getJobId();

    Response mockJobResponse = mock( Response.class );
    doReturn( mockJobResponse ).when( schedulerResource ).buildPlainTextOkResponse( jobId );

    Response testResponse = schedulerResource.addBlockout( mockJobScheduleRequest );
    assertEquals( mockJobResponse, testResponse );

    verify( schedulerResource.schedulerService, times( 1 ) ).addBlockout( mockJobScheduleRequest );
    verify( mockJob, times( 1 ) ).getJobId();
    verify( schedulerResource, times( 1 ) ).buildPlainTextOkResponse( jobId );
  }

  @Test
  public void testAddBlockoutError() throws Exception {
    JobScheduleRequest mockJobScheduleRequest = mock( JobScheduleRequest.class );

    Response mockUnauthorizedResponse = mock( Response.class );
    doReturn( mockUnauthorizedResponse ).when( schedulerResource ).buildStatusResponse( UNAUTHORIZED );

    // Test 1
    IOException mockIOException = mock( IOException.class );
    doThrow( mockIOException ).when( schedulerResource.schedulerService ).addBlockout( mockJobScheduleRequest );

    Response testResponse = schedulerResource.addBlockout( mockJobScheduleRequest );
    assertEquals( mockUnauthorizedResponse, testResponse );

    // Test 2
    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).addBlockout( mockJobScheduleRequest );

    testResponse = schedulerResource.addBlockout( mockJobScheduleRequest );
    assertEquals( mockUnauthorizedResponse, testResponse );

    // Test 3
    IllegalAccessException mockIllegalAccessException = mock( IllegalAccessException.class );
    doThrow( mockIllegalAccessException ).when( schedulerResource.schedulerService )
      .addBlockout( mockJobScheduleRequest );

    testResponse = schedulerResource.addBlockout( mockJobScheduleRequest );
    assertEquals( mockUnauthorizedResponse, testResponse );

    verify( schedulerResource.schedulerService, times( 3 ) ).addBlockout( mockJobScheduleRequest );
    verify( schedulerResource, times( 3 ) ).buildStatusResponse( UNAUTHORIZED );
  }

  @Test
  public void testUpdateBlockout() throws Exception {
    String jobId = "jobId";
    JobScheduleRequest mockJobScheduleRequest = mock( JobScheduleRequest.class );

    doReturn( true ).when( schedulerResource.schedulerService ).isScheduleAllowed();

    JobRequest mockJobRequest = mock( JobRequest.class );
    doReturn( mockJobRequest ).when( schedulerResource ).getJobRequest();

    Job mockJob = mock( Job.class );
    doReturn( mockJob ).when( schedulerResource.schedulerService ).updateBlockout( jobId, mockJobScheduleRequest );

    doReturn( jobId ).when( mockJob ).getJobId();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildPlainTextOkResponse( jobId );

    Response testResponse = schedulerResource.updateBlockout( jobId, mockJobScheduleRequest );
    assertEquals( mockResponse, testResponse );

    verify( schedulerResource.schedulerService, times( 1 ) ).updateBlockout( jobId, mockJobScheduleRequest );
    verify( mockJob, times( 1 ) ).getJobId();
  }

  @Test
  public void testUpdateBlockoutError() throws Exception {
    String jobId = "jobId";
    JobScheduleRequest mockJobScheduleRequest = mock( JobScheduleRequest.class );

    Response mockUnauthorizedResponse = mock( Response.class );
    doReturn( mockUnauthorizedResponse ).when( schedulerResource ).buildStatusResponse( UNAUTHORIZED );

    // Test 1
    IOException mockIOException = mock( IOException.class );
    doThrow( mockIOException ).when( schedulerResource.schedulerService ).updateBlockout( jobId,
      mockJobScheduleRequest );

    Response testResponse = schedulerResource.updateBlockout( jobId, mockJobScheduleRequest );
    assertEquals( mockUnauthorizedResponse, testResponse );

    // Test 2
    SchedulerException mockSchedulerException = mock( SchedulerException.class );
    doThrow( mockSchedulerException ).when( schedulerResource.schedulerService ).updateBlockout( jobId,
      mockJobScheduleRequest );

    testResponse = schedulerResource.updateBlockout( jobId, mockJobScheduleRequest );
    assertEquals( mockUnauthorizedResponse, testResponse );

    // Test 3
    IllegalAccessException mockIllegalAccessException = mock( IllegalAccessException.class );
    doThrow( mockIllegalAccessException ).when( schedulerResource.schedulerService )
      .updateBlockout( jobId, mockJobScheduleRequest );

    testResponse = schedulerResource.updateBlockout( jobId, mockJobScheduleRequest );
    assertEquals( mockUnauthorizedResponse, testResponse );

    verify( schedulerResource.schedulerService, times( 3 ) ).updateBlockout( jobId, mockJobScheduleRequest );
    verify( schedulerResource, times( 3 ) ).buildStatusResponse( UNAUTHORIZED );
  }

  @Test
  public void testBlockoutWillFire() throws Exception {
    JobScheduleRequest mockJobScheduleRequest = mock( JobScheduleRequest.class );

    IJobTrigger mockJobTrigger = mock( IJobTrigger.class );
    doReturn( mockJobTrigger ).when( schedulerResource ).convertScheduleRequestToJobTrigger( mockJobScheduleRequest );

    Boolean willFire = Boolean.FALSE;
    doReturn( willFire ).when( schedulerResource.schedulerService ).willFire( mockJobTrigger );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildOkResponse( willFire.toString() );

    Response testResponse = schedulerResource.blockoutWillFire( mockJobScheduleRequest );
    assertEquals( mockResponse, testResponse );

    verify( schedulerResource, times( 1 ) ).convertScheduleRequestToJobTrigger( mockJobScheduleRequest );
    verify( schedulerResource.schedulerService, times( 1 ) ).willFire( mockJobTrigger );
    verify( schedulerResource, times( 1 ) ).buildOkResponse( willFire.toString() );
  }

  @Test
  public void testBlockoutWillFireError() throws Exception {
    JobScheduleRequest mockJobScheduleRequest = mock( JobScheduleRequest.class );

    UnifiedRepositoryException mockUnifiedRepositoryException = mock( UnifiedRepositoryException.class );

    SchedulerException mockSchedulerException = mock( SchedulerException.class );

    Response mockUnifiedRepositoryExceptionResponse = mock( Response.class );
    doReturn( mockUnifiedRepositoryExceptionResponse ).when( schedulerResource )
      .buildServerErrorResponse( mockUnifiedRepositoryException );

    Response mockSchedulerExceptionResponse = mock( Response.class );
    doReturn( mockSchedulerExceptionResponse ).when( schedulerResource )
      .buildServerErrorResponse( mockSchedulerException );

    // Test 1
    doThrow( mockUnifiedRepositoryException ).when( schedulerResource )
      .convertScheduleRequestToJobTrigger( mockJobScheduleRequest );

    Response testResponse = schedulerResource.blockoutWillFire( mockJobScheduleRequest );
    assertEquals( mockUnifiedRepositoryExceptionResponse, testResponse );

    // Test 2
    doThrow( mockSchedulerException ).when( schedulerResource )
      .convertScheduleRequestToJobTrigger( mockJobScheduleRequest );

    testResponse = schedulerResource.blockoutWillFire( mockJobScheduleRequest );
    assertEquals( mockSchedulerExceptionResponse, testResponse );

    verify( schedulerResource, times( 1 ) ).buildServerErrorResponse( mockUnifiedRepositoryException );
    verify( schedulerResource, times( 1 ) ).buildServerErrorResponse( mockSchedulerException );
    verify( schedulerResource, times( 2 ) ).convertScheduleRequestToJobTrigger( mockJobScheduleRequest );
  }

  @Test
  public void testShouldFireNow() {
    Boolean shouldFireNow = Boolean.FALSE;
    doReturn( shouldFireNow ).when( schedulerResource.schedulerService ).shouldFireNow();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildOkResponse( shouldFireNow.toString() );

    Response testResponse = schedulerResource.shouldFireNow();
    assertEquals( mockResponse, testResponse );

    verify( schedulerResource.schedulerService, times( 1 ) ).shouldFireNow();
    verify( schedulerResource, times( 1 ) ).buildOkResponse( shouldFireNow.toString() );
  }

  @Test
  public void testGetBlockStatus() throws Exception {
    JobScheduleRequest mockJobScheduleRequest = mock( JobScheduleRequest.class );

    BlockStatusProxy mockBlockStatusProxy = mock( BlockStatusProxy.class );
    doReturn( mockBlockStatusProxy ).when( schedulerResource.schedulerService )
      .getBlockStatus( mockJobScheduleRequest );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( schedulerResource ).buildOkResponse( mockBlockStatusProxy );

    Response testResponse = schedulerResource.getBlockStatus( mockJobScheduleRequest );
    assertEquals( mockResponse, testResponse );

    verify( schedulerResource.schedulerService, times( 1 ) ).getBlockStatus( mockJobScheduleRequest );
    verify( schedulerResource, times( 1 ) ).buildOkResponse( mockBlockStatusProxy );
  }
}
