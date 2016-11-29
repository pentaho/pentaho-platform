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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.exporter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.CronJobTrigger;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.web.http.api.resources.JobScheduleParam;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScheduleExportUtilTest {

  @Before
  public void setUp() throws Exception {

  }

  @Test( expected = IllegalArgumentException.class )
  public void testCreateJobScheduleRequest_null() throws Exception {
    ScheduleExportUtil.createJobScheduleRequest( null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testCreateJobScheduleRequest_unknownTrigger() throws Exception {
    String jobName = "JOB";

    Job job = Mockito.mock( Job.class );
    JobTrigger trigger = Mockito.mock( JobTrigger.class );

    Mockito.when( job.getJobTrigger() ).thenReturn( trigger );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );

  }

  @Test
  public void testCreateJobScheduleRequest_SimpleJobTrigger() throws Exception {
    String jobName = "JOB";

    Job job = Mockito.mock( Job.class );
    SimpleJobTrigger trigger = Mockito.mock( SimpleJobTrigger.class );

    Mockito.when( job.getJobTrigger() ).thenReturn( trigger );
    Mockito.when( job.getJobName() ).thenReturn( jobName );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );

    Assert.assertNotNull( jobScheduleRequest );
    Assert.assertEquals( jobName, jobScheduleRequest.getJobName() );
    Assert.assertEquals( trigger, jobScheduleRequest.getSimpleJobTrigger() );
  }

  @Test
  public void testCreateJobScheduleRequest_NoStreamProvider() throws Exception {
    String jobName = "JOB";

    Job job = Mockito.mock( Job.class );
    SimpleJobTrigger trigger = Mockito.mock( SimpleJobTrigger.class );

    Mockito.when( job.getJobTrigger() ).thenReturn( trigger );
    Mockito.when( job.getJobName() ).thenReturn( jobName );
    Map<String, Serializable> params = new HashMap<>();
    params.put( "directory", "/home/admin" );
    params.put( "transformation", "myTransform" );

    HashMap<String, String> pdiParams = new HashMap<>();
    pdiParams.put( "pdiParam", "pdiParamValue" );
    params.put( ScheduleExportUtil.RUN_PARAMETERS_KEY, pdiParams );

    Mockito.when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );

    Assert.assertNotNull( jobScheduleRequest );
    Assert.assertEquals( jobName, jobScheduleRequest.getJobName() );
    Assert.assertEquals( trigger, jobScheduleRequest.getSimpleJobTrigger() );
    Assert.assertEquals( "/home/admin/myTransform.ktr", jobScheduleRequest.getInputFile() );
    Assert.assertEquals( "/home/admin/myTransform*", jobScheduleRequest.getOutputFile() );
    Assert.assertEquals( "pdiParamValue", jobScheduleRequest.getPdiParameters().get( "pdiParam" ) );
  }

  @Test
  public void testCreateJobScheduleRequest_StringStreamProvider() throws Exception {
    String jobName = "JOB";

    Job job = Mockito.mock( Job.class );
    SimpleJobTrigger trigger = Mockito.mock( SimpleJobTrigger.class );

    Mockito.when( job.getJobTrigger() ).thenReturn( trigger );
    Mockito.when( job.getJobName() ).thenReturn( jobName );
    Map<String, Serializable> params = new HashMap<>();
    params.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, "import file = /home/admin/myJob.kjb:output file=/home/admin/myJob*" );
    Mockito.when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );

    Assert.assertNotNull( jobScheduleRequest );
    Assert.assertEquals( jobName, jobScheduleRequest.getJobName() );
    Assert.assertEquals( trigger, jobScheduleRequest.getSimpleJobTrigger() );
    Assert.assertEquals( "/home/admin/myJob.kjb", jobScheduleRequest.getInputFile() );
    Assert.assertEquals( "/home/admin/myJob*", jobScheduleRequest.getOutputFile() );
  }

  @Test
  public void testCreateJobScheduleRequest_ComplexJobTrigger() throws Exception {
    String jobName = "JOB";
    Date now = new Date();

    Job job = Mockito.mock( Job.class );
    ComplexJobTrigger trigger = Mockito.mock( ComplexJobTrigger.class );

    Mockito.when( job.getJobTrigger() ).thenReturn( trigger );
    Mockito.when( job.getJobName() ).thenReturn( jobName );

    Mockito.when( trigger.getCronString() ).thenReturn( "0 30 13 ? * 2,3,4,5,6 *" );
    Mockito.when( trigger.getDuration() ).thenReturn( -1L );
    Mockito.when( trigger.getStartTime() ).thenReturn( now );
    Mockito.when( trigger.getEndTime() ).thenReturn( now );
    Mockito.when( trigger.getUiPassParam() ).thenReturn( "uiPassParm" );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );

    Assert.assertNotNull( jobScheduleRequest );
    Assert.assertEquals( jobName, jobScheduleRequest.getJobName() );

    // we should be getting back a cron trigger, not a complex trigger.
    Assert.assertNull( jobScheduleRequest.getSimpleJobTrigger() );
    Assert.assertNull( jobScheduleRequest.getComplexJobTrigger() );
    Assert.assertNotNull( jobScheduleRequest.getCronJobTrigger() );

    Assert.assertEquals( trigger.getCronString(), jobScheduleRequest.getCronJobTrigger().getCronString() );
    Assert.assertEquals( trigger.getDuration(), jobScheduleRequest.getCronJobTrigger().getDuration() );
    Assert.assertEquals( trigger.getEndTime(), jobScheduleRequest.getCronJobTrigger().getEndTime() );
    Assert.assertEquals( trigger.getStartTime(), jobScheduleRequest.getCronJobTrigger().getStartTime() );
    Assert.assertEquals( trigger.getUiPassParam(), jobScheduleRequest.getCronJobTrigger().getUiPassParam() );
  }

  @Test
  public void testCreateJobScheduleRequest_CronJobTrigger() throws Exception {
    String jobName = "JOB";

    Job job = Mockito.mock( Job.class );
    CronJobTrigger trigger = Mockito.mock( CronJobTrigger.class );

    Mockito.when( job.getJobTrigger() ).thenReturn( trigger );
    Mockito.when( job.getJobName() ).thenReturn( jobName );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );

    Assert.assertNotNull( jobScheduleRequest );
    Assert.assertEquals( jobName, jobScheduleRequest.getJobName() );
    Assert.assertEquals( trigger, jobScheduleRequest.getCronJobTrigger() );
  }

  @Test
  public void testCreateJobScheduleRequest_StreamProviderJobParam() throws Exception {
    String jobName = "JOB";
    String inputPath = "/input/path/to/file.ext";
    String outputPath = "/output/path/location.*";

    Map<String, Serializable> params = new HashMap<>();

    RepositoryFileStreamProvider streamProvider = Mockito.mock( RepositoryFileStreamProvider.class );
    params.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, streamProvider );

    Job job = Mockito.mock( Job.class );
    CronJobTrigger trigger = Mockito.mock( CronJobTrigger.class );

    Mockito.when( job.getJobTrigger() ).thenReturn( trigger );
    Mockito.when( job.getJobName() ).thenReturn( jobName );
    Mockito.when( job.getJobParams() ).thenReturn( params );
    Mockito.when( streamProvider.getInputFilePath() ).thenReturn( inputPath );
    Mockito.when( streamProvider.getOutputFilePath() ).thenReturn( outputPath );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );
    Assert.assertEquals( inputPath, jobScheduleRequest.getInputFile() );
    Assert.assertEquals( outputPath, jobScheduleRequest.getOutputFile() );
    Assert.assertEquals( 0, jobScheduleRequest.getJobParameters().size() );
  }

  @Test
  public void testCreateJobScheduleRequest_ActionClassJobParam() throws Exception {
    String jobName = "JOB";
    String actionClass = "com.pentaho.Action";
    Map<String, Serializable> params = new HashMap<>();

    params.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, actionClass );

    Job job = Mockito.mock( Job.class );
    CronJobTrigger trigger = Mockito.mock( CronJobTrigger.class );

    Mockito.when( job.getJobTrigger() ).thenReturn( trigger );
    Mockito.when( job.getJobName() ).thenReturn( jobName );
    Mockito.when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );
    Assert.assertEquals( actionClass, jobScheduleRequest.getActionClass() );
    Assert.assertEquals( actionClass, jobScheduleRequest.getJobParameters().get( 0 ).getValue() );
  }

  @Test
  public void testCreateJobScheduleRequest_TimeZoneJobParam() throws Exception {
    String jobName = "JOB";
    String timeZone = "America/New_York";
    Map<String, Serializable> params = new HashMap<>();

    params.put( IBlockoutManager.TIME_ZONE_PARAM, timeZone );

    Job job = Mockito.mock( Job.class );
    CronJobTrigger trigger = Mockito.mock( CronJobTrigger.class );

    Mockito.when( job.getJobTrigger() ).thenReturn( trigger );
    Mockito.when( job.getJobName() ).thenReturn( jobName );
    Mockito.when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );
    Assert.assertEquals( timeZone, jobScheduleRequest.getTimeZone() );
    Assert.assertEquals( timeZone, jobScheduleRequest.getJobParameters().get( 0 ).getValue() );
  }

  @Test
  public void testCreateJobScheduleRequest_MultipleTypesJobParam() throws Exception {
    String jobName = "JOB";
    Long l = Long.MAX_VALUE;
    Date d = new Date();
    Boolean b = true;

    Map<String, Serializable> params = new HashMap<>();

    params.put( "NumberValue", l );
    params.put( "DateValue", d );
    params.put( "BooleanValue", b );

    Job job = Mockito.mock( Job.class );
    CronJobTrigger trigger = Mockito.mock( CronJobTrigger.class );

    Mockito.when( job.getJobTrigger() ).thenReturn( trigger );
    Mockito.when( job.getJobName() ).thenReturn( jobName );
    Mockito.when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );
    for ( JobScheduleParam jobScheduleParam : jobScheduleRequest.getJobParameters() ) {
      Assert.assertTrue( jobScheduleParam.getValue().equals( l )
              || jobScheduleParam.getValue().equals( d )
              || jobScheduleParam.getValue().equals( b ) );
    }
  }

  @Test
  public void testConstructor() throws Exception {
    // only needed to get 100% code coverage
    Assert.assertNotNull( new ScheduleExportUtil() );
  }
}
