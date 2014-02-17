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

package org.pentaho.platform.scheduler2.ws.test;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.IScheduler.SchedulerStatus;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.api.scheduler2.recur.ITimeRecurrence;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.scheduler2.quartz.test.StubUserDetailsService;
import org.pentaho.platform.scheduler2.quartz.test.StubUserRoleListService;
import org.pentaho.platform.scheduler2.recur.IncrementalRecurrence;
import org.pentaho.platform.scheduler2.ws.ISchedulerService;
import org.pentaho.platform.scheduler2.ws.ListParamValue;
import org.pentaho.platform.scheduler2.ws.MapParamValue;
import org.pentaho.platform.scheduler2.ws.ParamValue;
import org.pentaho.platform.scheduler2.ws.StringParamValue;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.engine.core.PluginManagerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SuppressWarnings( "nls" )
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration
public class JaxWsSchedulerServiceTest {

  @Autowired
  private ApplicationContext applicationContext;

  private ISchedulerService schedulerSvc;

  private Map<String, ParamValue> jobParams;

  private IScheduler scheduler;

  static final String TEST_USER = "TestUser";

  private SimpleJobTrigger RUN_ONCE_IN_2_SECS;
  private SimpleJobTrigger RUN_ONCE_IN_3_SECS;

  @Before
  public void init() throws SchedulerException, PlatformInitializationException {
    schedulerSvc = (ISchedulerService) applicationContext.getBean( "schedulerFromWs" );
    MicroPlatform mp = new MicroPlatform();
    mp.define( IPluginManager.class, TstPluginManager.class );
    mp.define( "IScheduler2", TestQuartzScheduler.class );
    mp.define( IUserRoleListService.class, StubUserRoleListService.class );
    mp.define( UserDetailsService.class, StubUserDetailsService.class );
    mp.start();

    scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null );
    scheduler.start();

    MyAction.executed = false;
    MyAction.staticStringParam = null;

    jobParams = new HashMap<String, ParamValue>();
    jobParams.put( "stringParam", new StringParamValue( "testStringValue" ) );

    RUN_ONCE_IN_2_SECS = JobTrigger.ONCE_NOW;
    RUN_ONCE_IN_2_SECS.setStartTime( new Date( System.currentTimeMillis() + 2000L ) );

    RUN_ONCE_IN_3_SECS = JobTrigger.ONCE_NOW;
    RUN_ONCE_IN_3_SECS.setStartTime( new Date( System.currentTimeMillis() + 2000L ) );
  }

  @After
  public void after() throws SchedulerException {
    for ( Job job : scheduler.getJobs( null ) ) {
      scheduler.removeJob( job.getJobId() );
    }
  }

  private static Map<String, ParamValue> generateFullJobParams() {
    HashMap<String, ParamValue> privateParams = new HashMap<String, ParamValue>();

    ListParamValue listValue = new ListParamValue();
    listValue.add( "testListVal0" );
    listValue.add( "testListVal1" );

    MapParamValue mapValue = new MapParamValue();
    mapValue.put( "testMapKey0", "testMapVal0" );
    mapValue.put( "testMapKey1", "testMapVal1" );

    privateParams.put( "stringParam", new StringParamValue( "testStringValue" ) );
    privateParams.put( "listParam", listValue );
    privateParams.put( "mapParam", mapValue );

    return privateParams;
  }

  @Test
  public void testCreateSimpleJob() throws SchedulerException {

    schedulerSvc.createSimpleJob( "test job", generateFullJobParams(), JobTrigger.ONCE_NOW );
    int state = schedulerSvc.getSchedulerStatus();
    Assert.assertEquals( SchedulerStatus.RUNNING.ordinal(), state );

    int tries = 0;
    do {
      sleep( 1 );
    } while ( !MyAction.executed && tries++ < 10 );

    Assert.assertTrue( "the action was not executed in the expected window", MyAction.executed );

    //
    // Along with this test, make sure all the different kinds of job parameters can be set
    //
    Assert.assertEquals( "job params not properly set", "testStringValue", MyAction.staticStringParam );
    Assert.assertEquals( "job params not properly set", "testListVal0", MyAction.staticListParam.get( 0 ) );
    Assert.assertEquals( "job params not properly set", "testListVal1", MyAction.staticListParam.get( 1 ) );
    Assert.assertEquals( "job params not properly set", "testMapVal0", MyAction.staticMapParam.get( "testMapKey0" ) );
    Assert.assertEquals( "job params not properly set", "testMapVal1", MyAction.staticMapParam.get( "testMapKey1" ) );
  }

  @Test
  public void testCreateComplexJob() throws SchedulerException {
    int startingMinute = ( Calendar.getInstance().get( Calendar.MINUTE ) ) % 60;
    ComplexJobTrigger jobTrigger = new ComplexJobTrigger();
    jobTrigger.setMinuteRecurrence( new IncrementalRecurrence( startingMinute, 1 ) );
    jobTrigger.setHourlyRecurrence( (ITimeRecurrence) null );
    System.out.println( jobTrigger.toString() );

    schedulerSvc.createComplexJob( "test job", jobParams, jobTrigger );

    int tries = 0;
    do {
      sleep( 10 );
    } while ( !MyAction.executed && tries++ < 13 );

    Assert.assertTrue( "the action was not executed in the expected window", MyAction.executed );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testGetJobs() throws SchedulerException {
    scheduler.pause();
    schedulerSvc.createSimpleJob( "testGetJobsJob", generateFullJobParams(), JobTrigger.ONCE_NOW );
    Job[] serviceJobs = schedulerSvc.getJobs();

    //
    // First make sure the actual scheduler engine can find the newly created job
    //
    List<Job> engineJobs = scheduler.getJobs( new IJobFilter() {
      public boolean accept( Job job ) {
        return job.getJobName().contains( "testGetJobsJob" );
      }
    } );
    Assert.assertEquals( "The scheduler engine does not know about the job.", 1, engineJobs.size() );
    Job engineJob = engineJobs.get( 0 );

    //
    // Now make sure we have the same job available on the webservice client side
    //
    Assert.assertEquals( "The scheduler service does not know about the job.", 1, serviceJobs.length );
    Job serviceJob = schedulerSvc.getJobs()[0];
    Assert.assertEquals( "jobName is wrong", engineJob.getJobName(), serviceJob.getJobName() );

    Map<String, Serializable> params = serviceJob.getJobParams();
    Assert.assertTrue( "string job parameter is wrong", "testStringValue".equals( params.get( "stringParam" ) ) );

    Assert.assertTrue( "list job parameter is missing", params.containsKey( "listParam" ) );
    Assert.assertTrue( "map job parameter is missing", params.containsKey( "mapParam" ) );

    Assert.assertTrue( "list job parameter is wrong type. Expected List but is "
        + params.get( "listParam" ).getClass().getName(), params.get( "listParam" ) instanceof List );
    Assert.assertTrue( "map job parameter is wrong type. Expected Map but is "
        + params.get( "mapParam" ).getClass().getName(), params.get( "mapParam" ) instanceof Map );

    List<String> listParam = (List<String>) params.get( "listParam" );
    Assert.assertTrue( "list job parameter has wrong value", "testListVal0".equals( listParam.get( 0 ) ) );
    Assert.assertTrue( "list job parameter has wrong value", "testListVal1".equals( listParam.get( 1 ) ) );

    Map<String, String> mapParam = (Map<String, String>) params.get( "mapParam" );
    Assert.assertTrue( "map job parameter has wrong value", "testMapVal0".equals( mapParam.get( "testMapKey0" ) ) );
    Assert.assertTrue( "map job parameter has wrong value", "testMapVal1".equals( mapParam.get( "testMapKey1" ) ) );
  }

  @Test
  public void testPause() throws SchedulerException {
    schedulerSvc.pause();
    schedulerSvc.createSimpleJob( "test job", jobParams, JobTrigger.ONCE_NOW );

    sleep( 5 );

    Assert.assertFalse( "the action should not have been executed", MyAction.executed );
  }

  @Test
  public void testResume() throws SchedulerException {
    schedulerSvc.pause();
    schedulerSvc.createSimpleJob( "test job", jobParams, RUN_ONCE_IN_3_SECS );
    schedulerSvc.start();

    int tries = 0;
    do {
      sleep( 1 );
    } while ( !MyAction.executed && tries++ < 10 );

    Assert.assertTrue( "the action was not executed in the expected window", MyAction.executed );
  }

  @Test
  public void testPauseJob() throws SchedulerException {
    String jobId = schedulerSvc.createSimpleJob( "test job", jobParams, RUN_ONCE_IN_2_SECS );
    schedulerSvc.pauseJob( jobId );

    sleep( 7 );

    Assert.assertFalse( "the action should not have been executed", MyAction.executed );
  }

  @Test
  public void testResumeJob() throws SchedulerException {
    String jobId = schedulerSvc.createSimpleJob( "test job", jobParams, RUN_ONCE_IN_3_SECS );
    schedulerSvc.pauseJob( jobId );
    schedulerSvc.resumeJob( jobId );

    int tries = 0;
    do {
      sleep( 1 );
    } while ( !MyAction.executed && tries++ < 10 );

    Assert.assertTrue( "the action was not executed in the expected window", MyAction.executed );
  }

  private void sleep( int seconds ) {
    try {
      Thread.sleep( seconds * 1000 );
    } catch ( InterruptedException e ) {
      boolean ignored = true;
    }
  }

  public static class TstPluginManager extends PluginManagerAdapter {

    @Override
    public Class<?> loadClass( String beanId ) throws PluginBeanException {
      return MyAction.class;
    }
  }

  public static class MyAction implements IAction {

    public static boolean executed = false;

    public static String staticStringParam;
    public static List<String> staticListParam;
    public static Map<String, String> staticMapParam;

    public String stringParam;
    public List<String> listParam;
    public Map<String, String> mapParam;

    public void execute() throws Exception {
      System.out.println( "I RANNNNNN!! at " + new Date() );
      executed = true;
    }

    //
    // Bean property getters/setters
    //

    public List<String> getListParam() {
      return listParam;
    }

    public void setListParam( List<String> listParam ) {
      this.listParam = listParam;
      staticListParam = listParam;
    }

    public Map<String, String> getMapParam() {
      return mapParam;
    }

    public void setMapParam( Map<String, String> mapParam ) {
      this.mapParam = mapParam;
      staticMapParam = mapParam;
    }

    public void setStringParam( String value ) {
      stringParam = value;
      staticStringParam = value;
    }

    public String getStringParam() {
      return stringParam;
    }
  }

  public static class TestQuartzScheduler extends QuartzScheduler {
    @Override
    protected String getCurrentUser() {
      SecurityHelper.getInstance().becomeUser( TEST_USER );
      return super.getCurrentUser();
    }
  }

  @Test( timeout = 1000 * 5 * 60 )
  public void testUpdateComplexJob() throws SchedulerException {
	long start  = System.currentTimeMillis() + 1000;
	long end = System.currentTimeMillis() + 1000 + 5*60*60*100;
    int startingMinute = ( Calendar.getInstance().get( Calendar.MINUTE ) + 10 ) % 60;
    ComplexJobTrigger jobTrigger = new ComplexJobTrigger();
    jobTrigger.setStartTime( new Date(start) );
    jobTrigger.setEndTime( new Date(end) );
    jobTrigger.setMinuteRecurrence( new IncrementalRecurrence( startingMinute, 1 ) );
    jobTrigger.setHourlyRecurrence( (ITimeRecurrence) null );
    System.out.println( jobTrigger.toString() );

    String jobId = schedulerSvc.createComplexJob( "test job", jobParams, jobTrigger );

    Assert.assertEquals( 1, schedulerSvc.getJobs().length );

    jobTrigger = new ComplexJobTrigger();

	start  = System.currentTimeMillis() + 2*1000;
	end = System.currentTimeMillis() + 1000 + 7*60*60*100;

    jobTrigger.setStartTime( new Date(start) );
    jobTrigger.setEndTime( new Date(end) );

    startingMinute = ( Calendar.getInstance().get( Calendar.MINUTE ) + 20 ) % 60;
    jobTrigger.setMinuteRecurrence( new IncrementalRecurrence( startingMinute, 5 ) );
    jobTrigger.setHourlyRecurrence( (ITimeRecurrence) null );
    System.out.println( jobTrigger.toString() );

    HashMap<String, ParamValue> newJobParams = new HashMap<String, ParamValue>( jobParams );
    newJobParams.put( "newKey", new StringParamValue( "" ) );
    schedulerSvc.updateJobToUseComplexTrigger( jobId, newJobParams, jobTrigger );

    Assert.assertEquals( 1, schedulerSvc.getJobs().length );
    Job job = schedulerSvc.getJobs()[0];
    jobTrigger = (ComplexJobTrigger) job.getJobTrigger();
    Assert.assertEquals( (Integer) startingMinute,
        ( (IncrementalRecurrence) jobTrigger.getMinuteRecurrences().get( 0 ) ).getStartingValue() );
    Assert.assertEquals( (Integer) 5, ( (IncrementalRecurrence) jobTrigger.getMinuteRecurrences().get( 0 ) )
        .getIncrement() );
    Assert.assertTrue( job.getJobParams().containsKey( "newKey" ) );

    Assert.assertEquals( new Date(start), jobTrigger.getStartTime() );

    Assert.assertEquals( new Date(end), jobTrigger.getEndTime() );
  }

  @Test
  public void testUpdateSimpleJob() throws SchedulerException {
	long start  = System.currentTimeMillis() + 1000;
	long end = System.currentTimeMillis() + 1000 + 5*60*60*100;
    SimpleJobTrigger jobTrigger = new SimpleJobTrigger();
    jobTrigger.setStartTime( new Date(start) );
    jobTrigger.setEndTime( new Date(end) );
    jobTrigger.setRepeatInterval( 10 );
    jobTrigger.setRepeatCount( 20 );
    System.out.println( jobTrigger.toString() );

    String jobId = schedulerSvc.createSimpleJob( "test job", jobParams, jobTrigger );

    Assert.assertEquals( 1, schedulerSvc.getJobs().length );

    jobTrigger = new SimpleJobTrigger();

	start  = System.currentTimeMillis() + 1000;
	end = System.currentTimeMillis() + 1000 + 5*60*60*100;

    jobTrigger.setStartTime( new Date(start));
    jobTrigger.setEndTime( new Date(end) );

    jobTrigger.setRepeatInterval( 40 );
    jobTrigger.setRepeatCount( 50 );
    System.out.println( jobTrigger.toString() );

    HashMap<String, ParamValue> newJobParams = new HashMap<String, ParamValue>( jobParams );
    newJobParams.put( "newKey", new StringParamValue( "" ) );
    schedulerSvc.updateJobToUseSimpleTrigger( jobId, newJobParams, jobTrigger );

    Assert.assertEquals( 1, schedulerSvc.getJobs().length );
    Job job = schedulerSvc.getJobs()[0];
    jobTrigger = (SimpleJobTrigger) job.getJobTrigger();
    Assert.assertEquals( 40, jobTrigger.getRepeatInterval() );
    Assert.assertEquals( 50, jobTrigger.getRepeatCount() );
    Assert.assertTrue( job.getJobParams().containsKey( "newKey" ) );

    jobTrigger.getStartTime() ;
    Assert.assertEquals( new Date(start), jobTrigger.getStartTime());

    Assert.assertEquals( new Date(end), jobTrigger.getEndTime() );
  }
}
