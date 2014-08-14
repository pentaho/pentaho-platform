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
package org.pentaho.platform.web.http.api.resources.services;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.CronJobTrigger;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.SchedulerAction;
import org.pentaho.platform.web.http.api.resources.ComplexJobTriggerProxy;
import org.pentaho.platform.web.http.api.resources.JobRequest;
import org.pentaho.platform.web.http.api.resources.JobScheduleParam;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.pentaho.platform.web.http.api.resources.SchedulerOutputPathResolver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulerServiceTest {

  private static SchedulerService schedulerService;

  @Before
  public void setUp() {
    schedulerService = spy( new SchedulerService() );
    schedulerService.policy = mock( IAuthorizationPolicy.class );
    schedulerService.scheduler = mock( IScheduler.class );
    schedulerService.repository = mock( IUnifiedRepository.class );
  }

  @After
  public void cleanup() {
    schedulerService = null;
  }

  @Test
  public void testCreateJob() throws Exception {

    List<JobScheduleParam> jobParameters = new ArrayList<JobScheduleParam>();
    JobScheduleParam jobScheduleParam1 = mock( JobScheduleParam.class );
    doReturn( "name1" ).when( jobScheduleParam1 ).getName();
    doReturn( "value1" ).when( jobScheduleParam1 ).getValue();
    jobParameters.add( jobScheduleParam1 );

    Job job = mock( Job.class );

    JobScheduleRequest scheduleRequest = mock( JobScheduleRequest.class );
    doReturn( "className" ).when( scheduleRequest ).getActionClass();
    doReturn( "jobName" ).when( scheduleRequest ).getJobName();
    doReturn( jobParameters ).when( scheduleRequest ).getJobParameters();
    doNothing().when( scheduleRequest ).setJobName( anyString() );

    doReturn( true ).when( schedulerService ).isPdiFile( any( RepositoryFile.class ) );

    SchedulerOutputPathResolver schedulerOutputPathResolver = mock( SchedulerOutputPathResolver.class );
    doReturn( "outputFile" ).when( schedulerOutputPathResolver ).resolveOutputFilePath();

    SimpleJobTrigger simpleJobTrigger = mock( SimpleJobTrigger.class );
    ComplexJobTriggerProxy complexJobTriggerProxy = mock( ComplexJobTriggerProxy.class );
    CronJobTrigger cronJobTrigger = mock( CronJobTrigger.class );

    RepositoryFile repositoryFile = mock( RepositoryFile.class );
    doReturn( "file.ext" ).when( repositoryFile ).getName();

    Map<String, Serializable> metadata = mock( Map.class );
    doReturn( metadata ).when( schedulerService.repository ).getFileMetadata( anyString() );
    doReturn( true ).when( metadata ).containsKey( "_PERM_SCHEDULABLE" );
    doReturn( "true" ).when( metadata ).get( "_PERM_SCHEDULABLE" );

    doReturn( simpleJobTrigger ).when( scheduleRequest ).getSimpleJobTrigger();
    doReturn( complexJobTriggerProxy ).when( scheduleRequest ).getComplexJobTrigger();
    doReturn( cronJobTrigger ).when( scheduleRequest ).getCronJobTrigger();
    doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    doReturn( "file.ext" ).when( scheduleRequest ).getInputFile();
    doReturn( repositoryFile ).when( schedulerService.repository ).getFile( anyString() );

    doReturn( "ext" ).when( schedulerService ).getExtension( anyString() );

    doReturn( true ).when( schedulerService ).getAutoCreateUniqueFilename( any( JobScheduleRequest.class ) );

    doReturn( job ).when( schedulerService.scheduler )
      .createJob( anyString(), anyString(), any( Map.class ), any( IJobTrigger.class ),
        any( IBackgroundExecutionStreamProvider.class ) );

    doReturn( Class.class ).when( schedulerService ).getAction( anyString() );

    doReturn( job ).when( schedulerService.scheduler )
      .createJob( anyString(), any( Class.class ), any( Map.class ), any( IJobTrigger.class ) );

    //Test 1
    schedulerService.createJob( scheduleRequest );

    //Test 2
    doReturn( "" ).when( scheduleRequest ).getJobName();

    schedulerService.createJob( scheduleRequest );

    //Test 3
    doReturn( "" ).when( scheduleRequest ).getInputFile();
    doReturn( "" ).when( scheduleRequest ).getActionClass();

    schedulerService.createJob( scheduleRequest );

    verify( scheduleRequest, times( 15 ) ).getSimpleJobTrigger();
    verify( scheduleRequest, times( 11 ) ).getInputFile();
    verify( schedulerService.policy, times( 3 ) ).isAllowed( SchedulerAction.NAME );
    verify( schedulerService.repository, times( 2 ) ).getFile( anyString() );
    verify( scheduleRequest, times( 9 ) ).getJobName();
    verify( scheduleRequest, times( 3 ) ).setJobName( anyString() );
    verify( scheduleRequest, times( 5 ) ).getActionClass();
    verify( schedulerService.repository, times( 2 ) ).getFileMetadata( anyString() );
    verify( schedulerService, times( 3 ) ).isPdiFile( any( RepositoryFile.class ) );
    verify( schedulerService, times( 3 ) ).handlePDIScheduling( any( RepositoryFile.class ), any( HashMap.class ) );
    verify( schedulerService, times( 2 ) ).getSchedulerOutputPathResolver( any( JobScheduleRequest.class ) );
    verify( schedulerService, times( 2 ) ).getExtension( anyString() );
    verify( scheduleRequest, times( 5 ) ).getActionClass();
    verify( schedulerService ).getAction( anyString() );
    verify( schedulerService.scheduler )
      .createJob( anyString(), any( Class.class ), any( Map.class ), any( IJobTrigger.class ) );
  }

  @Test
  public void testCreateJobException() throws Exception {


    List<JobScheduleParam> jobParameters = new ArrayList<JobScheduleParam>();
    JobScheduleParam jobScheduleParam1 = mock( JobScheduleParam.class );
    doReturn( "name1" ).when( jobScheduleParam1 ).getName();
    doReturn( "value1" ).when( jobScheduleParam1 ).getValue();
    jobParameters.add( jobScheduleParam1 );

    Job job = mock( Job.class );

    JobScheduleRequest scheduleRequest = mock( JobScheduleRequest.class );
    doReturn( "className" ).when( scheduleRequest ).getActionClass();
    doReturn( "jobName" ).when( scheduleRequest ).getJobName();
    doReturn( jobParameters ).when( scheduleRequest ).getJobParameters();
    doNothing().when( scheduleRequest ).setJobName( anyString() );

    doReturn( true ).when( schedulerService ).isPdiFile( any( RepositoryFile.class ) );

    SchedulerOutputPathResolver schedulerOutputPathResolver = mock( SchedulerOutputPathResolver.class );
    doReturn( "outputFile" ).when( schedulerOutputPathResolver ).resolveOutputFilePath();

    SimpleJobTrigger simpleJobTrigger = mock( SimpleJobTrigger.class );
    ComplexJobTriggerProxy complexJobTriggerProxy = mock( ComplexJobTriggerProxy.class );
    CronJobTrigger cronJobTrigger = mock( CronJobTrigger.class );

    RepositoryFile repositoryFile = mock( RepositoryFile.class );
    doReturn( "file.ext" ).when( repositoryFile ).getName();

    Map<String, Serializable> metadata = mock( Map.class );
    doReturn( metadata ).when( schedulerService.repository ).getFileMetadata( anyString() );
    doReturn( true ).when( metadata ).containsKey( "_PERM_SCHEDULABLE" );
    doReturn( "True" ).when( metadata ).get( "_PERM_SCHEDULABLE" );

    doReturn( simpleJobTrigger ).when( scheduleRequest ).getSimpleJobTrigger();
    doReturn( complexJobTriggerProxy ).when( scheduleRequest ).getComplexJobTrigger();
    doReturn( cronJobTrigger ).when( scheduleRequest ).getCronJobTrigger();
    doReturn( false ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    doReturn( "file.ext" ).when( scheduleRequest ).getInputFile();
    doReturn( repositoryFile ).when( schedulerService.repository ).getFile( anyString() );

    doReturn( "ext" ).when( schedulerService ).getExtension( anyString() );

    doReturn( true ).when( schedulerService ).getAutoCreateUniqueFilename( any( JobScheduleRequest.class ) );

    doReturn( job ).when( schedulerService.scheduler )
      .createJob( anyString(), anyString(), any( Map.class ), any( IJobTrigger.class ),
        any( IBackgroundExecutionStreamProvider.class ) );

    doReturn( Class.class ).when( schedulerService ).getAction( anyString() );

    doReturn( job ).when( schedulerService.scheduler )
      .createJob( anyString(), any( Class.class ), any( Map.class ), any( IJobTrigger.class ) );


    //Test 1
    try {
      schedulerService.createJob( scheduleRequest );
      fail();
    } catch ( SecurityException e ) {
      //Should catch it
    }

    //Test 2
    doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );
    doReturn( "false" ).when( metadata ).get( "_PERM_SCHEDULABLE" );

    try {
      schedulerService.createJob( scheduleRequest );
      fail();
    } catch ( IllegalAccessException e ) {
      //Should catch it
    }

    //Test 3
    doReturn( "" ).when( scheduleRequest ).getInputFile();
    doThrow( new ClassNotFoundException() ).when( schedulerService ).getAction( anyString() );

    try {
      schedulerService.createJob( scheduleRequest );
      fail();
    } catch ( RuntimeException e ) {
      //Should catch it
    }

    verify( scheduleRequest, times( 7 ) ).getSimpleJobTrigger();
    verify( scheduleRequest, times( 3 ) ).getInputFile();
    verify( schedulerService.policy, times( 3 ) ).isAllowed( SchedulerAction.NAME );
    verify( schedulerService.repository, times( 1 ) ).getFile( anyString() );
    verify( scheduleRequest, times( 1 ) ).getJobName();
    verify( scheduleRequest, times( 2 ) ).setJobName( anyString() );
    verify( scheduleRequest, times( 7 ) ).getActionClass();
    verify( schedulerService.repository, times( 1 ) ).getFileMetadata( anyString() );
    verify( schedulerService, times( 1 ) ).isPdiFile( any( RepositoryFile.class ) );
    verify( schedulerService, times( 1 ) ).handlePDIScheduling( any( RepositoryFile.class ), any( HashMap.class ) );
    verify( scheduleRequest, times( 7 ) ).getActionClass();
    verify( schedulerService ).getAction( anyString() );
  }

  @Test
  public void testTriggerNow() throws Exception {

    JobRequest jobRequest = mock( JobRequest.class );
    Job job = mock( Job.class );

    doReturn( job ).when( schedulerService.scheduler ).getJob( anyString() );
    doReturn( true ).when( schedulerService.policy ).isAllowed( anyString() );
    doNothing().when( schedulerService.scheduler ).triggerNow( anyString() );

    //Test 1
    Job resultJob1 = schedulerService.triggerNow( jobRequest );

    assertEquals( job, resultJob1 );

    //Test 2
    doReturn( "test" ).when( job ).getUserName();
    doReturn( false ).when( schedulerService.policy ).isAllowed( anyString() );

    IPentahoSession pentahoSession = mock( IPentahoSession.class );
    doReturn( "test" ).when( pentahoSession ).getName();
    doReturn( pentahoSession ).when( schedulerService ).getSession();

    Job resultJob2 = schedulerService.triggerNow( jobRequest );

    assertEquals( job, resultJob2 );

    verify( schedulerService.scheduler, times( 4 ) ).getJob( anyString() );
    verify( schedulerService.scheduler, times( 2 ) ).triggerNow( anyString() );
    verify( schedulerService.policy, times( 2 ) ).isAllowed( anyString() );
  }

  @Test
  public void testGetContentCleanerJob() throws Exception {

    IJobFilter jobFilter = mock( IJobFilter.class );

    List<Job> jobs = new ArrayList<Job>();

    IPentahoSession session = mock( IPentahoSession.class );
    doReturn( session ).when( schedulerService ).getSession();
    doReturn( "sessionName" ).when( session ).getName();

    doReturn( true ).when( schedulerService.policy ).isAllowed( AdministerSecurityAction.NAME );
    doReturn( jobFilter ).when( schedulerService ).getJobFilter( anyBoolean(), anyString() );
    doReturn( jobs ).when( schedulerService.scheduler ).getJobs( any( IJobFilter.class ) );

    //Test 1
    Job job = schedulerService.getContentCleanerJob();

    assertNull( job );

    //Test 2
    Job job1 = mock( Job.class );
    jobs.add( job1 );

    job = schedulerService.getContentCleanerJob();

    assertNotNull( job );

    verify( schedulerService, times( 2 ) ).getSession();
    verify( session, times( 2 ) ).getName();
    verify( schedulerService.policy, times( 2 ) ).isAllowed( AdministerSecurityAction.NAME );
    verify( schedulerService.scheduler, times( 2 ) ).getJobs( any( IJobFilter.class ) );
  }

  @Test
  public void testGetContentCleanerJobException() throws Exception {

    IJobFilter jobFilter = mock( IJobFilter.class );

    List<Job> jobs = new ArrayList<Job>();

    IPentahoSession session = mock( IPentahoSession.class );
    doReturn( session ).when( schedulerService ).getSession();
    doReturn( "sessionName" ).when( session ).getName();

    doReturn( true ).when( schedulerService.policy ).isAllowed( AdministerSecurityAction.NAME );
    doReturn( jobFilter ).when( schedulerService ).getJobFilter( anyBoolean(), anyString() );
    doThrow( new SchedulerException( "" ) ).when( schedulerService.scheduler ).getJobs( any( IJobFilter.class ) );

    try {
      schedulerService.getContentCleanerJob();
      fail();
    } catch ( SchedulerException e ) {
      //Should catch the exception
    }

    verify( schedulerService ).getSession();
    verify( session ).getName();
    verify( schedulerService.policy ).isAllowed( AdministerSecurityAction.NAME );
    verify( schedulerService.scheduler ).getJobs( any( IJobFilter.class ) );
  }

  @Test
  public void testDoCanSchedule() {

    doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    //Test 1
    String isAllowed = schedulerService.doGetCanSchedule();

    assertEquals( "true", isAllowed );

    //Test 2
    doReturn( false ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    isAllowed = schedulerService.doGetCanSchedule();

    assertEquals( "false", isAllowed );

    verify( schedulerService.policy, times( 2 ) ).isAllowed( SchedulerAction.NAME );
  }

  @Test
  public void testGetState() throws SchedulerException {

    doReturn( IScheduler.SchedulerStatus.RUNNING ).when( schedulerService.scheduler ).getStatus();

    String state = schedulerService.getState();

    assertEquals( "RUNNING", state );

    verify( schedulerService.scheduler ).getStatus();
  }

  @Test
  public void testGetStateException() throws SchedulerException {

    doThrow( new SchedulerException( "" ) ).when( schedulerService.scheduler ).getStatus();

    try {
      schedulerService.getState();
      fail();
    } catch ( SchedulerException e ) {
      //Should go here
    }

    verify( schedulerService.scheduler ).getStatus();
  }

  @Test
  public void testStart() throws SchedulerException {
    doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    doNothing().when( schedulerService.scheduler ).start();

    doReturn( IScheduler.SchedulerStatus.RUNNING ).when( schedulerService.scheduler ).getStatus();

    //Test 1
    String state = schedulerService.start();

    assertEquals( "RUNNING", state );

    //Test 2
    doReturn( IScheduler.SchedulerStatus.STOPPED ).when( schedulerService.scheduler ).getStatus();
    doReturn( false ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    state = schedulerService.start();

    assertEquals( "STOPPED", state );

    verify( schedulerService.policy, times( 2 ) ).isAllowed( SchedulerAction.NAME );
    verify( schedulerService.scheduler, times( 1 ) ).start();
    verify( schedulerService.scheduler, times( 2 ) ).getStatus();
  }


  @Test
  public void testStartException() throws SchedulerException {
    doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    doThrow( new SchedulerException( "" ) ).when( schedulerService.scheduler ).start();

    try {
      schedulerService.start();
      fail();
    } catch ( SchedulerException e ) {
      //Should go here
    }

    verify( schedulerService.policy ).isAllowed( SchedulerAction.NAME );
    verify( schedulerService.scheduler ).start();
  }

  @Test
  public void testPause() throws SchedulerException {
    doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    doNothing().when( schedulerService.scheduler ).pause();

    doReturn( IScheduler.SchedulerStatus.PAUSED ).when( schedulerService.scheduler ).getStatus();

    //Test 1
    String state = schedulerService.pause();

    assertEquals( "PAUSED", state );

    //Test 2
    doReturn( IScheduler.SchedulerStatus.RUNNING ).when( schedulerService.scheduler ).getStatus();
    doReturn( false ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    state = schedulerService.pause();

    assertEquals( "RUNNING", state );

    verify( schedulerService.policy, times( 2 ) ).isAllowed( SchedulerAction.NAME );
    verify( schedulerService.scheduler, times( 1 ) ).pause();
    verify( schedulerService.scheduler, times( 2 ) ).getStatus();
  }


  @Test
  public void testPauseException() throws SchedulerException {
    doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    doThrow( new SchedulerException( "" ) ).when( schedulerService.scheduler ).pause();

    try {
      schedulerService.pause();
      fail();
    } catch ( SchedulerException e ) {
      //Should go here
    }

    verify( schedulerService.policy ).isAllowed( SchedulerAction.NAME );
    verify( schedulerService.scheduler ).pause();
  }

  @Test
  public void testShutdown() throws SchedulerException {
    doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    doNothing().when( schedulerService.scheduler ).shutdown();

    doReturn( IScheduler.SchedulerStatus.STOPPED ).when( schedulerService.scheduler ).getStatus();

    //Test 1
    String state = schedulerService.shutdown();

    assertEquals( "STOPPED", state );

    //Test 2
    doReturn( IScheduler.SchedulerStatus.RUNNING ).when( schedulerService.scheduler ).getStatus();
    doReturn( false ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    state = schedulerService.shutdown();

    assertEquals( "RUNNING", state );

    verify( schedulerService.policy, times( 2 ) ).isAllowed( SchedulerAction.NAME );
    verify( schedulerService.scheduler, times( 1 ) ).shutdown();
    verify( schedulerService.scheduler, times( 2 ) ).getStatus();
  }


  @Test
  public void testShutdownException() throws SchedulerException {
    doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    doThrow( new SchedulerException( "" ) ).when( schedulerService.scheduler ).shutdown();

    try {
      schedulerService.shutdown();
      fail();
    } catch ( SchedulerException e ) {
      //Should go here
    }

    verify( schedulerService.policy ).isAllowed( SchedulerAction.NAME );
    verify( schedulerService.scheduler ).shutdown();
  }

}
