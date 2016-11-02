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

import org.junit.Before;
import org.junit.Test;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    Job job = mock( Job.class );
    JobTrigger trigger = mock( JobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );

  }

  @Test
  public void testCreateJobScheduleRequest_SimpleJobTrigger() throws Exception {
    String jobName = "JOB";

    Job job = mock( Job.class );
    SimpleJobTrigger trigger = mock( SimpleJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );

    assertNotNull( jobScheduleRequest );
    assertEquals( jobName, jobScheduleRequest.getJobName() );
    assertEquals( trigger, jobScheduleRequest.getSimpleJobTrigger() );
  }

  @Test
  public void testCreateJobScheduleRequest_NoStreamProvider() throws Exception {
    String jobName = "JOB";

    Job job = mock( Job.class );
    SimpleJobTrigger trigger = mock( SimpleJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );
    Map<String, Serializable> params = new HashMap<>();
    params.put( "directory", "/home/admin" );
    params.put( "transformation", "myTransform" );

    HashMap<String, String> pdiParams = new HashMap<>();
    pdiParams.put( "pdiParam", "pdiParamValue" );
    params.put( ScheduleExportUtil.RUN_PARAMETERS_KEY, pdiParams );

    when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );

    assertNotNull( jobScheduleRequest );
    assertEquals( jobName, jobScheduleRequest.getJobName() );
    assertEquals( trigger, jobScheduleRequest.getSimpleJobTrigger() );
    assertEquals( "/home/admin/myTransform.ktr", jobScheduleRequest.getInputFile() );
    assertEquals( "/home/admin/myTransform*", jobScheduleRequest.getOutputFile() );
    assertEquals( "pdiParamValue", jobScheduleRequest.getPdiParameters().get( "pdiParam" ) );
  }

  @Test
  public void testCreateJobScheduleRequest_StringStreamProvider() throws Exception {
    String jobName = "JOB";

    Job job = mock( Job.class );
    SimpleJobTrigger trigger = mock( SimpleJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );
    Map<String, Serializable> params = new HashMap<>();
    params.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, "import file = /home/admin/myJob.kjb:output file=/home/admin/myJob*" );
    when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );

    assertNotNull( jobScheduleRequest );
    assertEquals( jobName, jobScheduleRequest.getJobName() );
    assertEquals( trigger, jobScheduleRequest.getSimpleJobTrigger() );
    assertEquals( "/home/admin/myJob.kjb", jobScheduleRequest.getInputFile() );
    assertEquals( "/home/admin/myJob*", jobScheduleRequest.getOutputFile() );
  }

  @Test
  public void testCreateJobScheduleRequest_ComplexJobTrigger() throws Exception {
    String jobName = "JOB";
    Date now = new Date();

    Job job = mock( Job.class );
    ComplexJobTrigger trigger = mock( ComplexJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );

    when( trigger.getCronString() ).thenReturn( "0 30 13 ? * 2,3,4,5,6 *" );
    when( trigger.getDuration() ).thenReturn( -1L );
    when( trigger.getStartTime() ).thenReturn( now );
    when( trigger.getEndTime() ).thenReturn( now );
    when( trigger.getUiPassParam() ).thenReturn( "uiPassParm" );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );

    assertNotNull( jobScheduleRequest );
    assertEquals( jobName, jobScheduleRequest.getJobName() );

    // we should be getting back a cron trigger, not a complex trigger.
    assertNull( jobScheduleRequest.getSimpleJobTrigger() );
    assertNull( jobScheduleRequest.getComplexJobTrigger() );
    assertNotNull( jobScheduleRequest.getCronJobTrigger() );

    assertEquals( trigger.getCronString(), jobScheduleRequest.getCronJobTrigger().getCronString() );
    assertEquals( trigger.getDuration(), jobScheduleRequest.getCronJobTrigger().getDuration() );
    assertEquals( trigger.getEndTime(), jobScheduleRequest.getCronJobTrigger().getEndTime() );
    assertEquals( trigger.getStartTime(), jobScheduleRequest.getCronJobTrigger().getStartTime() );
    assertEquals( trigger.getUiPassParam(), jobScheduleRequest.getCronJobTrigger().getUiPassParam() );
  }

  @Test
  public void testCreateJobScheduleRequest_CronJobTrigger() throws Exception {
    String jobName = "JOB";

    Job job = mock( Job.class );
    CronJobTrigger trigger = mock( CronJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );

    assertNotNull( jobScheduleRequest );
    assertEquals( jobName, jobScheduleRequest.getJobName() );
    assertEquals( trigger, jobScheduleRequest.getCronJobTrigger() );
  }

  @Test
  public void testCreateJobScheduleRequest_StreamProviderJobParam() throws Exception {
    String jobName = "JOB";
    String inputPath = "/input/path/to/file.ext";
    String outputPath = "/output/path/location.*";

    Map<String, Serializable> params = new HashMap<>();

    RepositoryFileStreamProvider streamProvider = mock( RepositoryFileStreamProvider.class );
    params.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, streamProvider );

    Job job = mock( Job.class );
    CronJobTrigger trigger = mock( CronJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );
    when( job.getJobParams() ).thenReturn( params );
    when( streamProvider.getInputFilePath() ).thenReturn( inputPath );
    when( streamProvider.getOutputFilePath() ).thenReturn( outputPath );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );
    assertEquals( inputPath, jobScheduleRequest.getInputFile() );
    assertEquals( outputPath, jobScheduleRequest.getOutputFile() );
    assertEquals( 0, jobScheduleRequest.getJobParameters().size() );
  }

  @Test
  public void testCreateJobScheduleRequest_ActionClassJobParam() throws Exception {
    String jobName = "JOB";
    String actionClass = "com.pentaho.Action";
    Map<String, Serializable> params = new HashMap<>();

    params.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, actionClass );

    Job job = mock( Job.class );
    CronJobTrigger trigger = mock( CronJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );
    when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );
    assertEquals( actionClass, jobScheduleRequest.getActionClass() );
    assertEquals( actionClass, jobScheduleRequest.getJobParameters().get( 0 ).getValue() );
  }

  @Test
  public void testCreateJobScheduleRequest_TimeZoneJobParam() throws Exception {
    String jobName = "JOB";
    String timeZone = "America/New_York";
    Map<String, Serializable> params = new HashMap<>();

    params.put( IBlockoutManager.TIME_ZONE_PARAM, timeZone );

    Job job = mock( Job.class );
    CronJobTrigger trigger = mock( CronJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );
    when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );
    assertEquals( timeZone, jobScheduleRequest.getTimeZone() );
    assertEquals( timeZone, jobScheduleRequest.getJobParameters().get( 0 ).getValue() );
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

    Job job = mock( Job.class );
    CronJobTrigger trigger = mock( CronJobTrigger.class );

    when( job.getJobTrigger() ).thenReturn( trigger );
    when( job.getJobName() ).thenReturn( jobName );
    when( job.getJobParams() ).thenReturn( params );

    JobScheduleRequest jobScheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );
    for ( JobScheduleParam jobScheduleParam : jobScheduleRequest.getJobParameters() ) {
      assertTrue( jobScheduleParam.getValue().equals( l )
              || jobScheduleParam.getValue().equals( d )
              || jobScheduleParam.getValue().equals( b ) );
    }
  }

  @Test
  public void testConstructor() throws Exception {
    // only needed to get 100% code coverage
    assertNotNull( new ScheduleExportUtil() );
  }
}
