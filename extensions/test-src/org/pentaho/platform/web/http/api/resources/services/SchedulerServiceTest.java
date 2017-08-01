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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.web.http.api.resources.services;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.CronJobTrigger;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.SchedulerAction;
import org.pentaho.platform.web.http.api.resources.ComplexJobTriggerProxy;
import org.pentaho.platform.web.http.api.resources.JobRequest;
import org.pentaho.platform.web.http.api.resources.JobScheduleParam;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.pentaho.platform.web.http.api.resources.SchedulerOutputPathResolver;
import org.pentaho.platform.web.http.api.resources.SessionResource;
import org.pentaho.platform.web.http.api.resources.proxies.BlockStatusProxy;

public class SchedulerServiceTest {

  private static SchedulerService schedulerService;

  @Before
  public void setUp() {
    schedulerService = Mockito.spy( new SchedulerService() );
    schedulerService.policy = Mockito.mock( IAuthorizationPolicy.class );
    schedulerService.scheduler = Mockito.mock( IScheduler.class );
    schedulerService.repository = Mockito.mock( IUnifiedRepository.class );
    schedulerService.blockoutManager = Mockito.mock( IBlockoutManager.class );
  }

  @After
  public void cleanup() {
    schedulerService = null;
  }

  @Test
  public void testCreateJob() throws Exception {

    List<JobScheduleParam> jobParameters = new ArrayList<>();
    JobScheduleParam jobScheduleParam1 = Mockito.mock( JobScheduleParam.class );
    Mockito.doReturn( "name1" ).when( jobScheduleParam1 ).getName();
    Mockito.doReturn( "value1" ).when( jobScheduleParam1 ).getValue();
    jobParameters.add( jobScheduleParam1 );

    Job job = Mockito.mock( Job.class );

    JobScheduleRequest scheduleRequest = Mockito.mock( JobScheduleRequest.class );
    Mockito.doReturn( "className" ).when( scheduleRequest ).getActionClass();
    Mockito.doReturn( "jobName" ).when( scheduleRequest ).getJobName();
    Mockito.doReturn( jobParameters ).when( scheduleRequest ).getJobParameters();
    Mockito.doNothing().when( scheduleRequest ).setJobName( Mockito.anyString() );

    Mockito.doReturn( true ).when( schedulerService ).isPdiFile( Mockito.any( RepositoryFile.class ) );

    SchedulerOutputPathResolver schedulerOutputPathResolver = Mockito.mock( SchedulerOutputPathResolver.class );
    Mockito.doReturn( "outputFile" ).when( schedulerOutputPathResolver ).resolveOutputFilePath();

    SimpleJobTrigger simpleJobTrigger = Mockito.mock( SimpleJobTrigger.class );
    ComplexJobTriggerProxy complexJobTriggerProxy = Mockito.mock( ComplexJobTriggerProxy.class );
    CronJobTrigger cronJobTrigger = Mockito.mock( CronJobTrigger.class );

    RepositoryFile repositoryFile = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( "file.ext" ).when( repositoryFile ).getName();

    Map<String, Serializable> metadata = Mockito.mock( Map.class );
    Mockito.doReturn( metadata ).when( schedulerService.repository ).getFileMetadata( Mockito.anyString() );
    Mockito.doReturn( true ).when( metadata ).containsKey( RepositoryFile.SCHEDULABLE_KEY );
    Mockito.doReturn( "true" ).when( metadata ).get( RepositoryFile.SCHEDULABLE_KEY );

    Mockito.doReturn( simpleJobTrigger ).when( scheduleRequest ).getSimpleJobTrigger();
    Mockito.doReturn( complexJobTriggerProxy ).when( scheduleRequest ).getComplexJobTrigger();
    Mockito.doReturn( cronJobTrigger ).when( scheduleRequest ).getCronJobTrigger();
    Mockito.doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    Mockito.doReturn( "file.ext" ).when( scheduleRequest ).getInputFile();
    Mockito.doReturn( repositoryFile ).when( schedulerService.repository ).getFile( Mockito.anyString() );

    Mockito.doReturn( "ext" ).when( schedulerService ).getExtension( Mockito.anyString() );

    Mockito.doReturn( true ).when( schedulerService ).getAutoCreateUniqueFilename( Mockito.any( JobScheduleRequest.class ) );

    Mockito.doReturn( job ).when( schedulerService.scheduler )
        .createJob( Mockito.anyString(), Mockito.anyString(), Mockito.any( Map.class ), Mockito.any( IJobTrigger.class ),
            Mockito.any( IBackgroundExecutionStreamProvider.class ) );

    Mockito.doReturn( Class.class ).when( schedulerService ).getAction( Mockito.anyString() );

    Mockito.doReturn( job ).when( schedulerService.scheduler )
        .createJob( Mockito.anyString(), Mockito.any( Class.class ), Mockito.any( Map.class ), Mockito.any( IJobTrigger.class ) );

    //Test 1
    schedulerService.createJob( scheduleRequest );

    //Test 2
    Mockito.doReturn( "" ).when( scheduleRequest ).getJobName();

    schedulerService.createJob( scheduleRequest );

    //Test 3
    Mockito.doReturn( "" ).when( scheduleRequest ).getInputFile();
    Mockito.doReturn( "" ).when( scheduleRequest ).getActionClass();

    schedulerService.createJob( scheduleRequest );

    Mockito.verify( scheduleRequest, Mockito.times( 15 ) ).getSimpleJobTrigger();
    Mockito.verify( scheduleRequest, Mockito.times( 11 ) ).getInputFile();
    Mockito.verify( schedulerService.policy, Mockito.times( 3 ) ).isAllowed( SchedulerAction.NAME );
    Mockito.verify( schedulerService.repository, Mockito.times( 2 ) ).getFile( Mockito.anyString() );
    Mockito.verify( scheduleRequest, Mockito.times( 9 ) ).getJobName();
    Mockito.verify( scheduleRequest, Mockito.times( 3 ) ).setJobName( Mockito.anyString() );
    Mockito.verify( scheduleRequest, Mockito.times( 5 ) ).getActionClass();
    Mockito.verify( schedulerService.repository, Mockito.times( 2 ) ).getFileMetadata( Mockito.anyString() );
    Mockito.verify( schedulerService, Mockito.times( 3 ) ).isPdiFile( Mockito.any( RepositoryFile.class ) );
    Mockito.verify( schedulerService, Mockito.times( 3 ) ).handlePDIScheduling( Mockito.any( RepositoryFile.class ),
        Mockito.any( HashMap.class ), Mockito.any( HashMap.class ) );
    Mockito.verify( schedulerService, Mockito.times( 2 ) ).getSchedulerOutputPathResolver( Mockito.any( JobScheduleRequest.class ) );
    Mockito.verify( schedulerService, Mockito.times( 2 ) ).getExtension( Mockito.anyString() );
    Mockito.verify( scheduleRequest, Mockito.times( 5 ) ).getActionClass();
    Mockito.verify( schedulerService ).getAction( Mockito.anyString() );
    Mockito.verify( schedulerService.scheduler )
        .createJob( Mockito.anyString(), Mockito.any( Class.class ), Mockito.any( Map.class ), Mockito.any( IJobTrigger.class ) );
  }

  @Test
  public void testCreateJobException() throws Exception {

    List<JobScheduleParam> jobParameters = new ArrayList<>();
    JobScheduleParam jobScheduleParam1 = Mockito.mock( JobScheduleParam.class );
    Mockito.doReturn( "name1" ).when( jobScheduleParam1 ).getName();
    Mockito.doReturn( "value1" ).when( jobScheduleParam1 ).getValue();
    jobParameters.add( jobScheduleParam1 );

    Job job = Mockito.mock( Job.class );

    JobScheduleRequest scheduleRequest = Mockito.mock( JobScheduleRequest.class );
    Mockito.doReturn( "className" ).when( scheduleRequest ).getActionClass();
    Mockito.doReturn( "jobName" ).when( scheduleRequest ).getJobName();
    Mockito.doReturn( jobParameters ).when( scheduleRequest ).getJobParameters();
    Mockito.doNothing().when( scheduleRequest ).setJobName( Mockito.anyString() );

    Mockito.doReturn( true ).when( schedulerService ).isPdiFile( Mockito.any( RepositoryFile.class ) );

    SchedulerOutputPathResolver schedulerOutputPathResolver = Mockito.mock( SchedulerOutputPathResolver.class );
    Mockito.doReturn( "outputFile" ).when( schedulerOutputPathResolver ).resolveOutputFilePath();

    SimpleJobTrigger simpleJobTrigger = Mockito.mock( SimpleJobTrigger.class );
    ComplexJobTriggerProxy complexJobTriggerProxy = Mockito.mock( ComplexJobTriggerProxy.class );
    CronJobTrigger cronJobTrigger = Mockito.mock( CronJobTrigger.class );

    RepositoryFile repositoryFile = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( "file.ext" ).when( repositoryFile ).getName();

    Map<String, Serializable> metadata = Mockito.mock( Map.class );
    Mockito.doReturn( metadata ).when( schedulerService.repository ).getFileMetadata( Mockito.anyString() );
    Mockito.doReturn( true ).when( metadata ).containsKey( RepositoryFile.SCHEDULABLE_KEY );
    Mockito.doReturn( "True" ).when( metadata ).get( RepositoryFile.SCHEDULABLE_KEY );

    Mockito.doReturn( simpleJobTrigger ).when( scheduleRequest ).getSimpleJobTrigger();
    Mockito.doReturn( complexJobTriggerProxy ).when( scheduleRequest ).getComplexJobTrigger();
    Mockito.doReturn( cronJobTrigger ).when( scheduleRequest ).getCronJobTrigger();
    Mockito.doReturn( false ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    Mockito.doReturn( "file.ext" ).when( scheduleRequest ).getInputFile();
    Mockito.doReturn( repositoryFile ).when( schedulerService.repository ).getFile( Mockito.anyString() );

    Mockito.doReturn( "ext" ).when( schedulerService ).getExtension( Mockito.anyString() );

    Mockito.doReturn( true ).when( schedulerService ).getAutoCreateUniqueFilename( Mockito.any( JobScheduleRequest.class ) );

    Mockito.doReturn( job ).when( schedulerService.scheduler )
        .createJob( Mockito.anyString(), Mockito.anyString(), Mockito.any( Map.class ), Mockito.any( IJobTrigger.class ),
            Mockito.any( IBackgroundExecutionStreamProvider.class ) );

    Mockito.doReturn( Class.class ).when( schedulerService ).getAction( Mockito.anyString() );

    Mockito.doReturn( job ).when( schedulerService.scheduler )
        .createJob( Mockito.anyString(), Mockito.any( Class.class ), Mockito.any( Map.class ), Mockito.any( IJobTrigger.class ) );


    //Test 1
    try {
      schedulerService.createJob( scheduleRequest );
      Assert.fail();
    } catch ( SecurityException e ) {
      //Should catch it
    }

    //Test 2
    Mockito.doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );
    Mockito.doReturn( "false" ).when( metadata ).get( RepositoryFile.SCHEDULABLE_KEY );

    try {
      schedulerService.createJob( scheduleRequest );
      Assert.fail();
    } catch ( IllegalAccessException e ) {
      //Should catch it
    }

    //Test 3
    Mockito.doReturn( "" ).when( scheduleRequest ).getInputFile();
    Mockito.doThrow( new ClassNotFoundException() ).when( schedulerService ).getAction( Mockito.anyString() );

    try {
      schedulerService.createJob( scheduleRequest );
      Assert.fail();
    } catch ( RuntimeException e ) {
      //Should catch it
    }

    Mockito.verify( scheduleRequest, Mockito.times( 7 ) ).getSimpleJobTrigger();
    Mockito.verify( scheduleRequest, Mockito.times( 3 ) ).getInputFile();
    Mockito.verify( schedulerService.policy, Mockito.times( 3 ) ).isAllowed( SchedulerAction.NAME );
    Mockito.verify( schedulerService.repository, Mockito.times( 1 ) ).getFile( Mockito.anyString() );
    Mockito.verify( scheduleRequest, Mockito.times( 1 ) ).getJobName();
    Mockito.verify( scheduleRequest, Mockito.times( 2 ) ).setJobName( Mockito.anyString() );
    Mockito.verify( scheduleRequest, Mockito.times( 7 ) ).getActionClass();
    Mockito.verify( schedulerService.repository, Mockito.times( 1 ) ).getFileMetadata( Mockito.anyString() );
    Mockito.verify( schedulerService, Mockito.times( 1 ) ).isPdiFile( Mockito.any( RepositoryFile.class ) );
    Mockito.verify( schedulerService, Mockito.times( 1 ) ).handlePDIScheduling( Mockito.any( RepositoryFile.class ),
        Mockito.any( HashMap.class ), Mockito.any( HashMap.class ) );
    Mockito.verify( scheduleRequest, Mockito.times( 7 ) ).getActionClass();
    Mockito.verify( schedulerService ).getAction( Mockito.anyString() );
  }

  @Test
  public void testTriggerNow() throws Exception {

    JobRequest jobRequest = Mockito.mock( JobRequest.class );
    Job job = Mockito.mock( Job.class );

    Mockito.doReturn( job ).when( schedulerService.scheduler ).getJob( Mockito.anyString() );
    Mockito.doReturn( true ).when( schedulerService.policy ).isAllowed( Mockito.anyString() );
    Mockito.doNothing().when( schedulerService.scheduler ).triggerNow( Mockito.anyString() );

    //Test 1
    Job resultJob1 = schedulerService.triggerNow( jobRequest.getJobId() );

    Assert.assertEquals( job, resultJob1 );

    //Test 2
    Mockito.doReturn( "test" ).when( job ).getUserName();
    Mockito.doReturn( false ).when( schedulerService.policy ).isAllowed( Mockito.anyString() );

    IPentahoSession pentahoSession = Mockito.mock( IPentahoSession.class );
    Mockito.doReturn( "test" ).when( pentahoSession ).getName();
    Mockito.doReturn( pentahoSession ).when( schedulerService ).getSession();

    Job resultJob2 = schedulerService.triggerNow( jobRequest.getJobId() );

    Assert.assertEquals( job, resultJob2 );

    Mockito.verify( schedulerService.scheduler, Mockito.times( 4 ) ).getJob( Mockito.anyString() );
    Mockito.verify( schedulerService.scheduler, Mockito.times( 2 ) ).triggerNow( Mockito.anyString() );
    Mockito.verify( schedulerService.policy, Mockito.times( 2 ) ).isAllowed( Mockito.anyString() );
  }

  @Test
  public void testGetContentCleanerJob() throws Exception {

    IJobFilter jobFilter = Mockito.mock( IJobFilter.class );

    List<Job> jobs = new ArrayList<>();

    IPentahoSession session = Mockito.mock( IPentahoSession.class );
    Mockito.doReturn( session ).when( schedulerService ).getSession();
    Mockito.doReturn( "sessionName" ).when( session ).getName();

    Mockito.doReturn( true ).when( schedulerService.policy ).isAllowed( AdministerSecurityAction.NAME );
    Mockito.doReturn( jobFilter ).when( schedulerService ).getJobFilter( Mockito.anyBoolean(), Mockito.anyString() );
    Mockito.doReturn( jobs ).when( schedulerService.scheduler ).getJobs( Mockito.any( IJobFilter.class ) );

    //Test 1
    Job job = schedulerService.getContentCleanerJob();

    Assert.assertNull( job );

    //Test 2
    Job job1 = Mockito.mock( Job.class );
    jobs.add( job1 );

    job = schedulerService.getContentCleanerJob();

    Assert.assertNotNull( job );

    Mockito.verify( schedulerService, Mockito.times( 2 ) ).getSession();
    Mockito.verify( session, Mockito.times( 2 ) ).getName();
    Mockito.verify( schedulerService.policy, Mockito.times( 2 ) ).isAllowed( AdministerSecurityAction.NAME );
    Mockito.verify( schedulerService.scheduler, Mockito.times( 2 ) ).getJobs( Mockito.any( IJobFilter.class ) );
  }

  @Test
  public void testGetContentCleanerJobException() throws Exception {

    IJobFilter jobFilter = Mockito.mock( IJobFilter.class );

    IPentahoSession session = Mockito.mock( IPentahoSession.class );
    Mockito.doReturn( session ).when( schedulerService ).getSession();
    Mockito.doReturn( "sessionName" ).when( session ).getName();

    Mockito.doReturn( true ).when( schedulerService.policy ).isAllowed( AdministerSecurityAction.NAME );
    Mockito.doReturn( jobFilter ).when( schedulerService ).getJobFilter( Mockito.anyBoolean(), Mockito.anyString() );
    Mockito.doThrow( new SchedulerException( "" ) ).when( schedulerService.scheduler ).getJobs( Mockito.any( IJobFilter.class ) );

    try {
      schedulerService.getContentCleanerJob();
      Assert.fail();
    } catch ( SchedulerException e ) {
      //Should catch the exception
    }

    Mockito.verify( schedulerService ).getSession();
    Mockito.verify( session ).getName();
    Mockito.verify( schedulerService.policy ).isAllowed( AdministerSecurityAction.NAME );
    Mockito.verify( schedulerService.scheduler ).getJobs( Mockito.any( IJobFilter.class ) );
  }

  @Test
  public void testDoGetCanSchedule() {

    Mockito.doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    //Test 1
    String isAllowed = schedulerService.doGetCanSchedule();

    Assert.assertEquals( "true", isAllowed );

    //Test 2
    Mockito.doReturn( false ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    isAllowed = schedulerService.doGetCanSchedule();

    Assert.assertEquals( "false", isAllowed );

    Mockito.verify( schedulerService.policy, Mockito.times( 2 ) ).isAllowed( SchedulerAction.NAME );
  }

  @Test
  public void testGetState() throws SchedulerException {

    Mockito.doReturn( IScheduler.SchedulerStatus.RUNNING ).when( schedulerService.scheduler ).getStatus();

    String state = schedulerService.getState();

    Assert.assertEquals( "RUNNING", state );

    Mockito.verify( schedulerService.scheduler ).getStatus();
  }

  @Test
  public void testGetStateException() throws SchedulerException {

    Mockito.doThrow( new SchedulerException( "" ) ).when( schedulerService.scheduler ).getStatus();

    try {
      schedulerService.getState();
      Assert.fail();
    } catch ( SchedulerException e ) {
      //Should go here
    }

    Mockito.verify( schedulerService.scheduler ).getStatus();
  }

  @Test
  public void testStart() throws SchedulerException {
    Mockito.doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    Mockito.doNothing().when( schedulerService.scheduler ).start();

    Mockito.doReturn( IScheduler.SchedulerStatus.RUNNING ).when( schedulerService.scheduler ).getStatus();

    //Test 1
    String state = schedulerService.start();

    Assert.assertEquals( "RUNNING", state );

    //Test 2
    Mockito.doReturn( IScheduler.SchedulerStatus.STOPPED ).when( schedulerService.scheduler ).getStatus();
    Mockito.doReturn( false ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    state = schedulerService.start();

    Assert.assertEquals( "STOPPED", state );

    Mockito.verify( schedulerService.policy, Mockito.times( 2 ) ).isAllowed( SchedulerAction.NAME );
    Mockito.verify( schedulerService.scheduler, Mockito.times( 1 ) ).start();
    Mockito.verify( schedulerService.scheduler, Mockito.times( 2 ) ).getStatus();
  }


  @Test
  public void testStartException() throws SchedulerException {
    Mockito.doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    Mockito.doThrow( new SchedulerException( "" ) ).when( schedulerService.scheduler ).start();

    try {
      schedulerService.start();
      Assert.fail();
    } catch ( SchedulerException e ) {
      //Should go here
    }

    Mockito.verify( schedulerService.policy ).isAllowed( SchedulerAction.NAME );
    Mockito.verify( schedulerService.scheduler ).start();
  }

  @Test
  public void testPause() throws SchedulerException {
    Mockito.doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    Mockito.doNothing().when( schedulerService.scheduler ).pause();

    Mockito.doReturn( IScheduler.SchedulerStatus.PAUSED ).when( schedulerService.scheduler ).getStatus();

    //Test 1
    String state = schedulerService.pause();

    Assert.assertEquals( "PAUSED", state );

    //Test 2
    Mockito.doReturn( IScheduler.SchedulerStatus.RUNNING ).when( schedulerService.scheduler ).getStatus();
    Mockito.doReturn( false ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    state = schedulerService.pause();

    Assert.assertEquals( "RUNNING", state );

    Mockito.verify( schedulerService.policy, Mockito.times( 2 ) ).isAllowed( SchedulerAction.NAME );
    Mockito.verify( schedulerService.scheduler, Mockito.times( 1 ) ).pause();
    Mockito.verify( schedulerService.scheduler, Mockito.times( 2 ) ).getStatus();
  }

  @Test
  public void testPauseJob() throws SchedulerException {
    Job job = Mockito.mock( Job.class );
    Mockito.doReturn( job ).when( schedulerService ).getJob( Mockito.anyString() );
    Mockito.doReturn( true ).when( schedulerService ).isScheduleAllowed();
    Mockito.doNothing().when( schedulerService.scheduler ).pauseJob( Mockito.anyString() );
    Mockito.doReturn( IScheduler.SchedulerStatus.PAUSED ).when( schedulerService.scheduler ).getStatus();
    schedulerService.pauseJob( "job-id" );
  }

  @Test
  public void testPauseJobException() throws SchedulerException {
    Job job = Mockito.mock( Job.class );
    Mockito.doReturn( job ).when( schedulerService ).getJob( Mockito.anyString() );
    Mockito.doReturn( true ).when( schedulerService ).isScheduleAllowed();
    Mockito.doThrow( new SchedulerException( "pause-exception" ) ).when( schedulerService.scheduler ).pauseJob( Mockito.anyString() );
    try {
      schedulerService.pauseJob( "job-id" );
    } catch ( SchedulerException e ) {
      Assert.assertEquals( "pause-exception", e.getMessage() );
    }
  }

  @Test
  public void testResumeJob() throws SchedulerException {
    Job job = Mockito.mock( Job.class );
    Mockito.doReturn( job ).when( schedulerService ).getJob( Mockito.anyString() );
    Mockito.doReturn( true ).when( schedulerService ).isScheduleAllowed();
    Mockito.doNothing().when( schedulerService.scheduler ).resumeJob( Mockito.anyString() );
    Mockito.doReturn( IScheduler.SchedulerStatus.RUNNING ).when( schedulerService.scheduler ).getStatus();
    schedulerService.resumeJob( "job-id" );
  }

  @Test
  public void testResumeJobException() throws SchedulerException {
    Job job = Mockito.mock( Job.class );
    Mockito.doReturn( job ).when( schedulerService ).getJob( Mockito.anyString() );
    Mockito.doReturn( true ).when( schedulerService ).isScheduleAllowed();
    Mockito.doThrow( new SchedulerException( "pause-exception" ) ).when( schedulerService.scheduler ).resumeJob( Mockito.anyString() );
    try {
      schedulerService.resumeJob( "job-id" );
    } catch ( SchedulerException e ) {
      Assert.assertEquals( "pause-exception", e.getMessage() );
    }
  }

  @Test
  public void testRemoveJob() throws SchedulerException {
    Job job = Mockito.mock( Job.class );
    Mockito.doReturn( job ).when( schedulerService ).getJob( Mockito.anyString() );
    Mockito.doReturn( true ).when( schedulerService ).isScheduleAllowed();
    Mockito.doNothing().when( schedulerService.scheduler ).removeJob( Mockito.anyString() );
    Mockito.doReturn( IScheduler.SchedulerStatus.RUNNING ).when( schedulerService.scheduler ).getStatus();
    schedulerService.removeJob( "job-id" );
  }

  @Test
  public void testRemoveJobException() throws SchedulerException {
    Job job = Mockito.mock( Job.class );
    Mockito.doReturn( job ).when( schedulerService ).getJob( Mockito.anyString() );
    Mockito.doReturn( true ).when( schedulerService ).isScheduleAllowed();
    Mockito.doThrow( new SchedulerException( "pause-exception" ) ).when( schedulerService.scheduler ).removeJob( Mockito.anyString() );
    try {
      schedulerService.removeJob( "job-id" );
    } catch ( SchedulerException e ) {
      Assert.assertEquals( "pause-exception", e.getMessage() );
    }
  }

  @Test
  public void testPauseException() throws SchedulerException {
    Mockito.doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    Mockito.doThrow( new SchedulerException( "" ) ).when( schedulerService.scheduler ).pause();

    try {
      schedulerService.pause();
      Assert.fail();
    } catch ( SchedulerException e ) {
      //Should go here
    }

    Mockito.verify( schedulerService.policy ).isAllowed( SchedulerAction.NAME );
    Mockito.verify( schedulerService.scheduler ).pause();
  }

  @Test
  public void testShutdown() throws SchedulerException {
    Mockito.doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    Mockito.doNothing().when( schedulerService.scheduler ).shutdown();

    Mockito.doReturn( IScheduler.SchedulerStatus.STOPPED ).when( schedulerService.scheduler ).getStatus();

    //Test 1
    String state = schedulerService.shutdown();

    Assert.assertEquals( "STOPPED", state );

    //Test 2
    Mockito.doReturn( IScheduler.SchedulerStatus.RUNNING ).when( schedulerService.scheduler ).getStatus();
    Mockito.doReturn( false ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    state = schedulerService.shutdown();

    Assert.assertEquals( "RUNNING", state );

    Mockito.verify( schedulerService.policy, Mockito.times( 2 ) ).isAllowed( SchedulerAction.NAME );
    Mockito.verify( schedulerService.scheduler, Mockito.times( 1 ) ).shutdown();
    Mockito.verify( schedulerService.scheduler, Mockito.times( 2 ) ).getStatus();
  }


  @Test
  public void testShutdownException() throws SchedulerException {
    Mockito.doReturn( true ).when( schedulerService.policy ).isAllowed( SchedulerAction.NAME );

    Mockito.doThrow( new SchedulerException( "" ) ).when( schedulerService.scheduler ).shutdown();

    try {
      schedulerService.shutdown();
      Assert.fail();
    } catch ( SchedulerException e ) {
      //Should go here
    }

    Mockito.verify( schedulerService.policy ).isAllowed( SchedulerAction.NAME );
    Mockito.verify( schedulerService.scheduler ).shutdown();
  }

  @Test
  public void testGetJobs() throws Exception {
    IPentahoSession mockPentahoSession = Mockito.mock( IPentahoSession.class );

    Mockito.doReturn( mockPentahoSession ).when( schedulerService ).getSession();
    Mockito.doReturn( "admin" ).when( mockPentahoSession ).getName();
    Mockito.doReturn( true ).when( schedulerService ).canAdminister( mockPentahoSession );
    List<Job> mockJobs = new ArrayList<Job>();
    mockJobs.add( Mockito.mock( Job.class ) );
    Mockito.doReturn( mockJobs ).when( schedulerService.scheduler ).getJobs( Mockito.any( IJobFilter.class ) );

    List<Job> jobs = schedulerService.getJobs();

    Assert.assertEquals( mockJobs, jobs );

    Mockito.verify( schedulerService, Mockito.times( 1 ) ).getSession();
    Mockito.verify( mockPentahoSession, Mockito.times( 1 ) ).getName();
    Mockito.verify( schedulerService, Mockito.times( 1 ) ).canAdminister( mockPentahoSession );
    Mockito.verify( schedulerService.scheduler, Mockito.times( 1 ) ).getJobs( Mockito.any( IJobFilter.class ) );
  }

  @Test
  public void testDoGetGeneratedContentForSchedule() throws Exception {
    String lineageId = "test.prpt";

    FileService mockFileService = Mockito.mock( FileService.class );
    Mockito.doReturn( mockFileService ).when( schedulerService ).getFileService();

    SessionResource mockSessionResource = Mockito.mock( SessionResource.class );
    Mockito.doReturn( mockSessionResource ).when( schedulerService ).getSessionResource();

    String currentUserDir = "currentUserDir";
    Mockito.doReturn( currentUserDir ).when( mockSessionResource ).doGetCurrentUserDir();

    List<RepositoryFileDto> mockList = Mockito.mock( List.class );
    Mockito.doReturn( mockList ).when( mockFileService )
        .searchGeneratedContent( currentUserDir, lineageId, QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );

    List<RepositoryFileDto> list = schedulerService.doGetGeneratedContentForSchedule( lineageId );
    Assert.assertEquals( mockList, list );
  }

  @Test
  public void testGetJobState() throws Exception {
    JobRequest mockJobRequest = Mockito.mock( JobRequest.class );

    String jobId = "jobId";
    Mockito.doReturn( jobId ).when( mockJobRequest ).getJobId();

    IPentahoSession mockSession = Mockito.mock( IPentahoSession.class );
    Mockito.doReturn( mockSession ).when( schedulerService ).getSession();

    Job mockJob = Mockito.mock( Job.class );
    Mockito.doReturn( mockJob ).when( schedulerService ).getJob( jobId );
    Mockito.doReturn( Job.JobState.BLOCKED ).when( mockJob ).getState();

    String username = "username";
    Mockito.doReturn( username ).when( mockJob ).getUserName();
    Mockito.doReturn( username ).when( mockSession ).getName();

    // Test 1
    Mockito.doReturn( true ).when( schedulerService ).isScheduleAllowed();

    Job.JobState testState = schedulerService.getJobState( mockJobRequest );
    Assert.assertEquals( Job.JobState.BLOCKED, testState );

    // Test 2
    Mockito.doReturn( false ).when( schedulerService ).isScheduleAllowed();

    testState = schedulerService.getJobState( mockJobRequest );
    Assert.assertEquals( Job.JobState.BLOCKED, testState );

    Mockito.verify( mockJobRequest, Mockito.times( 2 ) ).getJobId();
    Mockito.verify( schedulerService, Mockito.times( 1 ) ).getSession();
    Mockito.verify( schedulerService, Mockito.times( 2 ) ).getJob( jobId );
    Mockito.verify( mockJob, Mockito.times( 2 ) ).getState();
    Mockito.verify( mockJob, Mockito.times( 1 ) ).getUserName();
    Mockito.verify( mockSession, Mockito.times( 1 ) ).getName();
  }

  @Test
  public void testGetJobStateError() throws Exception {
    JobRequest mockJobRequest = Mockito.mock( JobRequest.class );

    String jobId = "jobId";
    Mockito.doReturn( jobId ).when( mockJobRequest ).getJobId();

    IPentahoSession mockSession = Mockito.mock( IPentahoSession.class );
    Mockito.doReturn( mockSession ).when( schedulerService ).getSession();

    Job mockJob = Mockito.mock( Job.class );
    Mockito.doReturn( mockJob ).when( schedulerService ).getJob( jobId );
    Mockito.doReturn( Job.JobState.BLOCKED ).when( mockJob ).getState();

    String username = "username";
    Mockito.doReturn( username ).when( mockJob ).getUserName();

    String sessionName = "notUsername";
    Mockito.doReturn( sessionName ).when( mockSession ).getName();

    Mockito.doReturn( false ).when( schedulerService ).isScheduleAllowed();

    try {
      schedulerService.getJobState( mockJobRequest );
      Assert.fail();
    } catch ( UnsupportedOperationException e ) {
      // Expected
    }
  }

  @Test
  public void testGetJobInfo() throws Exception {
    String jobId = "jobId";

    Job mockJob = Mockito.mock( Job.class );
    Mockito.doReturn( mockJob ).when( schedulerService ).getJob( jobId );

    ISecurityHelper mockSecurityHelper = Mockito.mock( ISecurityHelper.class );
    Mockito.doReturn( mockSecurityHelper ).when( schedulerService ).getSecurityHelper();

    IPentahoSession mockPentahoSession = Mockito.mock( IPentahoSession.class );
    Mockito.doReturn( mockPentahoSession ).when( schedulerService ).getSession();

    String sessionName = "sessionName";
    Mockito.doReturn( sessionName ).when( mockPentahoSession ).getName();
    Mockito.doReturn( sessionName ).when( mockJob ).getUserName();

    Map<String, Serializable> mockJobParams = Mockito.mock( Map.class );
    Mockito.doReturn( mockJobParams ).when( mockJob ).getJobParams();

    Set<String> jobParamsKeyset = new HashSet<String>();
    Mockito.doReturn( jobParamsKeyset ).when( mockJobParams ).keySet();

    String jobParamKey = "key";
    jobParamsKeyset.add( jobParamKey );

    String value = "value";
    String[] testArray = new String[]{value};
    Mockito.doReturn( testArray ).when( mockJobParams ).get( jobParamKey );

    // Test 1
    Mockito.doReturn( true ).when( mockSecurityHelper ).isPentahoAdministrator( mockPentahoSession );

    Job testJob = schedulerService.getJobInfo( jobId );
    Assert.assertEquals( mockJob, testJob );

    // Test 2
    Mockito.doReturn( false ).when( mockSecurityHelper ).isPentahoAdministrator( mockPentahoSession );
    testJob = schedulerService.getJobInfo( jobId );
    Assert.assertEquals( mockJob, testJob );

    Mockito.verify( mockJobParams, Mockito.times( 2 ) ).put( Mockito.eq( jobParamKey ), Mockito.any( Serializable.class ) );
    Mockito.verify( schedulerService, Mockito.times( 2 ) ).getJob( jobId );
    Mockito.verify( schedulerService, Mockito.times( 2 ) ).getSecurityHelper();
    Mockito.verify( schedulerService, Mockito.times( 3 ) ).getSession();
    Mockito.verify( mockPentahoSession, Mockito.times( 1 ) ).getName();
    Mockito.verify( mockJob, Mockito.times( 1 ) ).getUserName();
    Mockito.verify( mockJob, Mockito.times( 6 ) ).getJobParams();
    Mockito.verify( mockJobParams, Mockito.times( 2 ) ).keySet();
    Mockito.verify( mockJobParams, Mockito.times( 2 ) ).get( jobParamKey );
    Mockito.verify( mockSecurityHelper, Mockito.times( 2 ) ).isPentahoAdministrator( mockPentahoSession );
  }

  @Test
  public void testGetJobInfoError() throws Exception {
    String jobId = "jobId";

    Job mockJob = Mockito.mock( Job.class );
    Mockito.doReturn( mockJob ).when( schedulerService ).getJob( jobId );

    ISecurityHelper mockSecurityHelper = Mockito.mock( ISecurityHelper.class );
    Mockito.doReturn( mockSecurityHelper ).when( schedulerService ).getSecurityHelper();

    IPentahoSession mockPentahoSession = Mockito.mock( IPentahoSession.class );
    Mockito.doReturn( mockPentahoSession ).when( schedulerService ).getSession();

    Mockito.doReturn( false ).when( mockSecurityHelper ).isPentahoAdministrator( mockPentahoSession );

    String sessionName = "sessionName";
    Mockito.doReturn( sessionName ).when( mockPentahoSession ).getName();

    String username = "username";
    Mockito.doReturn( username ).when( mockJob ).getUserName();

    try {
      schedulerService.getJobInfo( jobId );
      Assert.fail();
    } catch ( RuntimeException e ) {
      // Expected
    }
  }

  @Test
  public void testIsScheduleAllowed() {

    // Test 1
    Mockito.doReturn( true ).when( schedulerService ).isScheduleAllowed();

    Map<String, Serializable> metadata = Mockito.mock( Map.class );

    Mockito.doReturn( metadata ).when( schedulerService.repository ).getFileMetadata( Mockito.anyString() );

    Mockito.doReturn( true ).when( metadata ).containsKey( RepositoryFile.SCHEDULABLE_KEY );
    Mockito.doReturn( "true" ).when( metadata ).get( RepositoryFile.SCHEDULABLE_KEY );

    boolean canSchedule = schedulerService.isScheduleAllowed( Mockito.anyString() );

    Assert.assertTrue( canSchedule );

    // Test 2
    Mockito.doReturn( false ).when( schedulerService ).isScheduleAllowed();

    canSchedule = schedulerService.isScheduleAllowed( Mockito.anyString() );

    Assert.assertFalse( canSchedule );

    // Test 3
    Mockito.doReturn( true ).when( schedulerService ).isScheduleAllowed();
    Mockito.doReturn( false ).when( metadata ).containsKey( RepositoryFile.SCHEDULABLE_KEY );

    canSchedule = schedulerService.isScheduleAllowed( Mockito.anyString() );

    Assert.assertTrue( canSchedule );

    // Test 4
    Mockito.doReturn( true ).when( metadata ).containsKey( RepositoryFile.SCHEDULABLE_KEY );
    Mockito.doReturn( "false" ).when( metadata ).get( RepositoryFile.SCHEDULABLE_KEY );

    canSchedule = schedulerService.isScheduleAllowed( Mockito.anyString() );

    Assert.assertFalse( canSchedule );

    Mockito.verify( schedulerService, Mockito.times( 4 ) ).isScheduleAllowed();
    Mockito.verify( schedulerService.repository, Mockito.times( 3 ) ).getFileMetadata( Mockito.anyString() );
    Mockito.verify( metadata, Mockito.times( 3 ) ).containsKey( RepositoryFile.SCHEDULABLE_KEY );
    Mockito.verify( metadata, Mockito.times( 2 ) ).get( RepositoryFile.SCHEDULABLE_KEY );
  }

  @Test
  public void testGetBlockoutJobs() {

    List<Job> jobs = new ArrayList<Job>();

    Mockito.doReturn( jobs ).when( schedulerService.blockoutManager ).getBlockOutJobs();

    List<Job> returnJobs = schedulerService.getBlockOutJobs();

    Assert.assertEquals( returnJobs, jobs );

    Mockito.verify( schedulerService.blockoutManager ).getBlockOutJobs();
  }

  @Test
  public void testHasBlockouts() {

    List<Job> jobs = new ArrayList<Job>();

    Mockito.doReturn( jobs ).when( schedulerService.blockoutManager ).getBlockOutJobs();

    // Test 1
    boolean hasBlockouts = schedulerService.hasBlockouts();

    Assert.assertFalse( hasBlockouts );

    // Test 2
    jobs.add( Mockito.mock( Job.class ) );
    hasBlockouts = schedulerService.hasBlockouts();

    Assert.assertTrue( hasBlockouts );

    Mockito.verify( schedulerService.blockoutManager, Mockito.times( 2 ) ).getBlockOutJobs();
  }

  @Test
  public void testAddBlockout() throws Exception {

    JobScheduleRequest jobScheduleRequest = Mockito.mock( JobScheduleRequest.class );
    Job jobMock = Mockito.mock( Job.class );

    JobScheduleParam jobScheduleParamMock1 = Mockito.mock( JobScheduleParam.class );
    JobScheduleParam jobScheduleParamMock2 = Mockito.mock( JobScheduleParam.class );

    List<JobScheduleParam> jobScheduleParams = new ArrayList<>();

    Mockito.doReturn( true ).when( schedulerService ).canAdminister();
    Mockito.doNothing().when( jobScheduleRequest ).setActionClass( Mockito.anyString() );
    Mockito.doReturn( jobScheduleParams ).when( jobScheduleRequest ).getJobParameters();
    Mockito.doReturn( jobScheduleParamMock1 ).when( schedulerService ).getJobScheduleParam( Mockito.anyString(), Mockito.anyString() );
    Mockito.doReturn( jobScheduleParamMock2 ).when( schedulerService ).getJobScheduleParam( Mockito.anyString(), Mockito.anyLong() );
    Mockito.doNothing().when( schedulerService ).updateStartDateForTimeZone( jobScheduleRequest );
    Mockito.doReturn( jobMock ).when( schedulerService ).createJob( Mockito.any( JobScheduleRequest.class ) );

    Job job = schedulerService.addBlockout( jobScheduleRequest );

    Assert.assertNotNull( job );
    Assert.assertEquals( 2, jobScheduleParams.size() );

    Mockito.verify( schedulerService ).canAdminister();
    Mockito.verify( jobScheduleRequest ).setActionClass( Mockito.anyString() );
    Mockito.verify( jobScheduleRequest, Mockito.times( 2 ) ).getJobParameters();
    Mockito.verify( schedulerService ).updateStartDateForTimeZone( jobScheduleRequest );
    Mockito.verify( schedulerService ).createJob( Mockito.any( JobScheduleRequest.class ) );
  }

  @Test
  public void testAddBlockoutException() throws Exception {

    // Test 1
    JobScheduleRequest jobScheduleRequest = Mockito.mock( JobScheduleRequest.class );
    Mockito.doReturn( false ).when( schedulerService ).canAdminister();

    try {
      schedulerService.addBlockout( jobScheduleRequest );
      Assert.fail();
    } catch ( IllegalAccessException e ) {
      //Should catch exception
    }

    // Test 2
    Job jobMock = Mockito.mock( Job.class );

    JobScheduleParam jobScheduleParamMock1 = Mockito.mock( JobScheduleParam.class );
    JobScheduleParam jobScheduleParamMock2 = Mockito.mock( JobScheduleParam.class );

    List<JobScheduleParam> jobScheduleParams = new ArrayList<>();

    Mockito.doReturn( true ).when( schedulerService ).canAdminister();
    Mockito.doNothing().when( jobScheduleRequest ).setActionClass( Mockito.anyString() );
    Mockito.doReturn( jobScheduleParams ).when( jobScheduleRequest ).getJobParameters();
    Mockito.doReturn( jobScheduleParamMock1 ).when( schedulerService ).getJobScheduleParam( Mockito.anyString(), Mockito.anyString() );
    Mockito.doReturn( jobScheduleParamMock2 ).when( schedulerService ).getJobScheduleParam( Mockito.anyString(), Mockito.anyLong() );
    Mockito.doNothing().when( schedulerService ).updateStartDateForTimeZone( jobScheduleRequest );
    Mockito.doReturn( jobMock ).when( schedulerService ).createJob( Mockito.any( JobScheduleRequest.class ) );

    Mockito.doThrow( new IOException() ).when( schedulerService ).createJob( jobScheduleRequest );

    try {
      schedulerService.addBlockout( jobScheduleRequest );
      Assert.fail();
    } catch ( IOException e ) {
      //Should catch exception
    }

    // Test 3
    Mockito.doThrow( new SchedulerException( "" ) ).when( schedulerService ).createJob( jobScheduleRequest );

    try {
      schedulerService.addBlockout( jobScheduleRequest );
      Assert.fail();
    } catch ( SchedulerException e ) {
      //Should catch exception
    }

    Mockito.verify( schedulerService, Mockito.times( 3 ) ).canAdminister();
    Mockito.verify( jobScheduleRequest, Mockito.times( 2 ) ).setActionClass( Mockito.anyString() );
    Mockito.verify( jobScheduleRequest, Mockito.times( 4 ) ).getJobParameters();
    Mockito.verify( schedulerService, Mockito.times( 2 ) ).updateStartDateForTimeZone( jobScheduleRequest );
    Mockito.verify( schedulerService, Mockito.times( 2 ) ).createJob( Mockito.any( JobScheduleRequest.class ) );
  }

  @Test
  public void testUpdateBlockout() throws Exception {

    String jobId = "jobId";
    JobScheduleRequest jobScheduleRequest = Mockito.mock( JobScheduleRequest.class );
    Job jobMock = Mockito.mock( Job.class );

    Mockito.doReturn( true ).when( schedulerService ).canAdminister();
    Mockito.doReturn( true ).when( schedulerService ).removeJob( Mockito.anyString() );
    Mockito.doReturn( jobMock ).when( schedulerService ).addBlockout( jobScheduleRequest );

    Job job = schedulerService.updateBlockout( jobId, jobScheduleRequest );

    Assert.assertNotNull( job );

    Mockito.verify( schedulerService ).canAdminister();
    Mockito.verify( schedulerService ).removeJob( Mockito.anyString() );
    Mockito.verify( schedulerService ).addBlockout( jobScheduleRequest );
  }

  @Test
  public void testUpdateBlockoutException() throws Exception {

    String jobId = "jobId";
    JobScheduleRequest jobScheduleRequest = Mockito.mock( JobScheduleRequest.class );

    // Test 1
    Mockito.doReturn( false ).when( schedulerService ).canAdminister();

    try {
      schedulerService.updateBlockout( jobId, jobScheduleRequest );
      Assert.fail();
    } catch ( IllegalAccessException e ) {
      // Should catch the exception
    }

    // Test 2
    Mockito.doReturn( true ).when( schedulerService ).canAdminister();
    Mockito.doThrow( new SchedulerException( "" ) ).when( schedulerService ).removeJob( Mockito.anyString() );

    try {
      schedulerService.updateBlockout( jobId, jobScheduleRequest );
      Assert.fail();
    } catch ( SchedulerException e ) {
      // Should catch the exception
    }

    // Test 3
    Mockito.doReturn( false ).when( schedulerService ).removeJob( Mockito.anyString() );

    try {
      schedulerService.updateBlockout( jobId, jobScheduleRequest );
      Assert.fail();
    } catch ( IllegalAccessException e ) {
      // Should catch the exception
    }

    // Test 4
    Mockito.doReturn( true ).when( schedulerService ).removeJob( Mockito.anyString() );
    Mockito.doThrow( new IOException() ).when( schedulerService ).addBlockout( jobScheduleRequest );

    try {
      schedulerService.updateBlockout( jobId, jobScheduleRequest );
      Assert.fail();
    } catch ( IOException e ) {
      // Should catch the exception
    }

    // Test 5
    Mockito.doThrow( new SchedulerException( "" ) ).when( schedulerService ).addBlockout( jobScheduleRequest );

    try {
      schedulerService.updateBlockout( jobId, jobScheduleRequest );
      Assert.fail();
    } catch ( SchedulerException e ) {
      // Should catch the exception
    }

    Mockito.verify( schedulerService, Mockito.times( 5 ) ).canAdminister();
    Mockito.verify( schedulerService, Mockito.times( 4 ) ).removeJob( Mockito.anyString() );
    Mockito.verify( schedulerService, Mockito.times( 2 ) ).addBlockout( jobScheduleRequest );
  }

  @Test
  public void testWillFire() {

    IJobTrigger jobTrigger = Mockito.mock( IJobTrigger.class );

    // Test 1
    Mockito.doReturn( true ).when( schedulerService.blockoutManager ).willFire( jobTrigger );

    boolean willFire = schedulerService.willFire( jobTrigger );

    Assert.assertTrue( willFire );

    // Test 2
    Mockito.doReturn( false ).when( schedulerService.blockoutManager ).willFire( jobTrigger );

    willFire = schedulerService.willFire( jobTrigger );

    Assert.assertFalse( willFire );

    Mockito.verify( schedulerService.blockoutManager, Mockito.times( 2 ) ).willFire( jobTrigger );
  }

  @Test
  public void testShouldFireNow() {

    // Test 1
    Mockito.doReturn( true ).when( schedulerService.blockoutManager ).shouldFireNow();

    boolean shouldFireNow = schedulerService.shouldFireNow();

    Assert.assertTrue( shouldFireNow );

    // Test 2
    Mockito.doReturn( false ).when( schedulerService.blockoutManager ).shouldFireNow();

    shouldFireNow = schedulerService.shouldFireNow();

    Assert.assertFalse( shouldFireNow );

    Mockito.verify( schedulerService.blockoutManager, Mockito.times( 2 ) ).shouldFireNow();

  }

  @Test
  public void testGetBlockStatus() throws Exception {

    JobScheduleRequest jobScheduleRequestMock = Mockito.mock( JobScheduleRequest.class );
    BlockStatusProxy blockStatusProxyMock = Mockito.mock( BlockStatusProxy.class );
    IJobTrigger jobTrigger = Mockito.mock( IJobTrigger.class );

    Mockito.doReturn( jobTrigger ).when( schedulerService ).convertScheduleRequestToJobTrigger( jobScheduleRequestMock );
    Mockito.doReturn( true ).when( schedulerService.blockoutManager ).isPartiallyBlocked( jobTrigger );
    Mockito.doReturn( true ).when( schedulerService.blockoutManager ).willFire( jobTrigger );
    Mockito.doReturn( blockStatusProxyMock ).when( schedulerService ).getBlockStatusProxy( Mockito.anyBoolean(), Mockito.anyBoolean() );

    // Test 1
    BlockStatusProxy blockStatusProxy = schedulerService.getBlockStatus( jobScheduleRequestMock );

    Assert.assertNotNull( blockStatusProxy );

    // Test 2
    Mockito.doReturn( false ).when( schedulerService.blockoutManager ).isPartiallyBlocked( jobTrigger );

    blockStatusProxy = schedulerService.getBlockStatus( jobScheduleRequestMock );

    Assert.assertNotNull( blockStatusProxy );

    Mockito.verify( schedulerService, Mockito.times( 2 ) ).convertScheduleRequestToJobTrigger( jobScheduleRequestMock );
    Mockito.verify( schedulerService.blockoutManager, Mockito.times( 2 ) ).isPartiallyBlocked( jobTrigger );
    Mockito.verify( schedulerService, Mockito.times( 2 ) ).getBlockStatusProxy( Mockito.anyBoolean(), Mockito.anyBoolean() );
    Mockito.verify( schedulerService.blockoutManager, Mockito.times( 1 ) ).willFire( jobTrigger );
  }

  @Test
  public void testGetBlockStatusException() throws Exception {

    JobScheduleRequest jobScheduleRequestMock = Mockito.mock( JobScheduleRequest.class );

    Mockito.doThrow( new SchedulerException( "" ) ).when( schedulerService ).convertScheduleRequestToJobTrigger( jobScheduleRequestMock );

    try {
      schedulerService.getBlockStatus( jobScheduleRequestMock );
      Assert.fail();
    } catch ( SchedulerException e ) {
      // Should catch the exception
    }

    Mockito.verify( schedulerService ).convertScheduleRequestToJobTrigger( jobScheduleRequestMock );
  }
}
