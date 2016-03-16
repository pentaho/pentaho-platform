/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.CronJobTrigger;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.api.scheduler2.recur.ITimeRecurrence;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek;
import org.pentaho.platform.scheduler2.recur.RecurrenceList;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 11/9/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class SchedulerResourceUtilTest {

  @Mock JobScheduleRequest scheduleRequest;
  @Mock QuartzScheduler scheduler;
  @Mock SimpleJobTrigger simple;
  @Mock RepositoryFile repo;
  CronJobTrigger cron;
  ComplexJobTriggerProxy complex;
  Date now;

  private TimeZone system;

  @Before
  public void setUp() throws Exception {
    // this makes the test non-deterministic!
    now = new Date();

    complex = new ComplexJobTriggerProxy();
    complex.setStartTime( now );

    system = TimeZone.getDefault();
    TimeZone.setDefault( TimeZone.getTimeZone( "EST" ) );
  }

  @After
  public void tearDown() {
    TimeZone.setDefault( system );
    system = null;
  }

  @Test
  public void testConvertScheduleRequestToJobTrigger_SimpleJobTrigger() throws Exception {
    IJobTrigger trigger = SchedulerResourceUtil.convertScheduleRequestToJobTrigger( scheduleRequest, scheduler );
    assertNotNull( trigger );
    assertTrue( trigger instanceof SimpleJobTrigger );
    assertTrue( trigger.getStartTime().getTime() > System.currentTimeMillis() );
  }

  @Test
  public void testConvertScheduleRequestToJobTrigger_SimpleJobTrigger_basedOnExisting() throws Exception {
    when( scheduleRequest.getSimpleJobTrigger() ).thenReturn( simple );
    IJobTrigger trigger = SchedulerResourceUtil.convertScheduleRequestToJobTrigger( scheduleRequest, scheduler );
    assertNotNull( trigger );
    assertEquals( simple, trigger );
    verify( simple ).setStartTime( any( Date.class ) );
  }

  @Test
  public void testConvertScheduleRequestToJobTrigger_ComplexJobTrigger_daysOfMonth() throws Exception {
    complex.setDaysOfMonth( new int[] { 1, 25 } );

    when( scheduleRequest.getComplexJobTrigger() ).thenReturn( complex );
    IJobTrigger trigger = SchedulerResourceUtil.convertScheduleRequestToJobTrigger( scheduleRequest, scheduler );
    assertNotNull( trigger );
    assertTrue( trigger instanceof ComplexJobTrigger );

    ComplexJobTrigger trig = (ComplexJobTrigger) trigger;
    List<ITimeRecurrence> recurrences = trig.getDayOfMonthRecurrences().getRecurrences();
    assertEquals( 2, recurrences.size() );
    RecurrenceList rec = (RecurrenceList) recurrences.get( 0 );
    assertEquals( 1, rec.getValues().get( 0 ).intValue() );
    rec = (RecurrenceList) recurrences.get( 1 );
    assertEquals( 25, rec.getValues().get( 0 ).intValue() );
  }

  @Test
  public void testConvertScheduleRequestToJobTrigger_ComplexJobTrigger_monthsOfYear() throws Exception {
    complex.setMonthsOfYear( new int[] { 1, 8 } );

    when( scheduleRequest.getComplexJobTrigger() ).thenReturn( complex );
    IJobTrigger trigger = SchedulerResourceUtil.convertScheduleRequestToJobTrigger( scheduleRequest, scheduler );
    assertNotNull( trigger );
    assertTrue( trigger instanceof ComplexJobTrigger );

    ComplexJobTrigger trig = (ComplexJobTrigger) trigger;
    List<ITimeRecurrence> recurrences = trig.getMonthlyRecurrences().getRecurrences();
    assertEquals( 2, recurrences.size() );
    RecurrenceList rec = (RecurrenceList) recurrences.get( 0 );
    assertEquals( 2, rec.getValues().get( 0 ).intValue() );
    rec = (RecurrenceList) recurrences.get( 1 );
    assertEquals( 9, rec.getValues().get( 0 ).intValue() );
  }

  @Test
  public void testConvertScheduleRequestToJobTrigger_ComplexJobTrigger_years() throws Exception {
    complex.setYears( new int[] { 2016, 2020 } );

    when( scheduleRequest.getComplexJobTrigger() ).thenReturn( complex );
    IJobTrigger trigger = SchedulerResourceUtil.convertScheduleRequestToJobTrigger( scheduleRequest, scheduler );
    assertNotNull( trigger );
    assertTrue( trigger instanceof ComplexJobTrigger );

    ComplexJobTrigger trig = (ComplexJobTrigger) trigger;
    List<ITimeRecurrence> recurrences = trig.getYearlyRecurrences().getRecurrences();
    assertEquals( 2, recurrences.size() );
    RecurrenceList rec = (RecurrenceList) recurrences.get( 0 );
    assertEquals( 2016, rec.getValues().get( 0 ).intValue() );
    rec = (RecurrenceList) recurrences.get( 1 );
    assertEquals( 2020, rec.getValues().get( 0 ).intValue() );
  }

  @Test
  public void testConvertScheduleRequestToJobTrigger_ComplexJobTrigger_daysOfWeek() throws Exception {
    complex.setDaysOfWeek( new int[] { 1, 5 } );

    when( scheduleRequest.getComplexJobTrigger() ).thenReturn( complex );
    IJobTrigger trigger = SchedulerResourceUtil.convertScheduleRequestToJobTrigger( scheduleRequest, scheduler );
    assertNotNull( trigger );
    assertTrue( trigger instanceof ComplexJobTrigger );

    ComplexJobTrigger trig = (ComplexJobTrigger) trigger;
    List<ITimeRecurrence> recurrences = trig.getDayOfWeekRecurrences().getRecurrences();
    assertEquals( 2, recurrences.size() );
    RecurrenceList rec = (RecurrenceList) recurrences.get( 0 );
    assertEquals( 2, rec.getValues().get( 0 ).intValue() );
    rec = (RecurrenceList) recurrences.get( 1 );
    assertEquals( 6, rec.getValues().get( 0 ).intValue() );
  }

  @Test
  public void testConvertScheduleRequestToJobTrigger_ComplexJobTrigger_weeksOfMonth() throws Exception {
    complex.setDaysOfWeek( new int[] { 1, 5 } );
    complex.setWeeksOfMonth( new int[] { 3, 4 } );

    when( scheduleRequest.getComplexJobTrigger() ).thenReturn( complex );
    IJobTrigger trigger = SchedulerResourceUtil.convertScheduleRequestToJobTrigger( scheduleRequest, scheduler );
    assertNotNull( trigger );
    assertTrue( trigger instanceof ComplexJobTrigger );

    ComplexJobTrigger trig = (ComplexJobTrigger) trigger;
    List<ITimeRecurrence> recurrences = trig.getDayOfWeekRecurrences().getRecurrences();
    assertEquals( 4, recurrences.size() );

    QualifiedDayOfWeek rec = (QualifiedDayOfWeek) recurrences.get( 0 );
    assertEquals( "MON", rec.getDayOfWeek().toString() );
    assertEquals( "FOURTH", rec.getQualifier().toString() );

    rec = (QualifiedDayOfWeek) recurrences.get( 1 );
    assertEquals( "MON", rec.getDayOfWeek().toString() );
    assertEquals( "LAST", rec.getQualifier().toString() );

    rec = (QualifiedDayOfWeek) recurrences.get( 2 );
    assertEquals( "FRI", rec.getDayOfWeek().toString() );
    assertEquals( "FOURTH", rec.getQualifier().toString() );

    rec = (QualifiedDayOfWeek) recurrences.get( 3 );
    assertEquals( "FRI", rec.getDayOfWeek().toString() );
    assertEquals( "LAST", rec.getQualifier().toString() );
  }

  @Test
  public void testConvertScheduleRequestToJobTrigger_CronString() throws Exception {
    cron = new CronJobTrigger();
    cron.setCronString( "0 45 16 ? * 2#4,2L,6#4,6L *" );
    cron.setDuration( 200000 );
    cron.setStartTime( now );
    cron.setUiPassParam( "param" );
    cron.setEndTime( now );

    when( scheduleRequest.getCronJobTrigger() ).thenReturn( cron );

    IJobTrigger trigger = SchedulerResourceUtil.convertScheduleRequestToJobTrigger( scheduleRequest, scheduler );
    assertTrue( trigger instanceof ComplexJobTrigger );

    ComplexJobTrigger trig = (ComplexJobTrigger) trigger;
    assertEquals( now, trig.getStartTime() );
    assertEquals( now, trig.getEndTime() );
    assertEquals( 200000, trig.getDuration() );
    assertEquals( "param", trig.getUiPassParam() );

    List<ITimeRecurrence> recurrences = trig.getDayOfWeekRecurrences().getRecurrences();
    assertEquals( 4, recurrences.size() );

    QualifiedDayOfWeek rec = (QualifiedDayOfWeek) recurrences.get( 0 );
    assertEquals( "MON", rec.getDayOfWeek().toString() );
    assertEquals( "FOURTH", rec.getQualifier().toString() );

    rec = (QualifiedDayOfWeek) recurrences.get( 1 );
    assertEquals( "MON", rec.getDayOfWeek().toString() );
    assertEquals( "LAST", rec.getQualifier().toString() );

    rec = (QualifiedDayOfWeek) recurrences.get( 2 );
    assertEquals( "FRI", rec.getDayOfWeek().toString() );
    assertEquals( "FOURTH", rec.getQualifier().toString() );

    rec = (QualifiedDayOfWeek) recurrences.get( 3 );
    assertEquals( "FRI", rec.getDayOfWeek().toString() );
    assertEquals( "LAST", rec.getQualifier().toString() );

  }

  @Test
  public void testUpdateStartDateForTimeZone_simple() throws Exception {
    SimpleJobTrigger sjt = new SimpleJobTrigger();
    sjt.setStartTime( now );
    when( scheduleRequest.getSimpleJobTrigger() ).thenReturn( sjt );
    when( scheduleRequest.getTimeZone() ).thenReturn( "GMT" );

    long gmtTime = now.getTime() + TimeZone.getTimeZone( "EST" ).getRawOffset();

    SchedulerResourceUtil.updateStartDateForTimeZone( scheduleRequest );
    assertEquals( gmtTime, scheduleRequest.getSimpleJobTrigger().getStartTime().getTime() );
  }

  @Test
  public void testUpdateStartDateForTimeZone_complex() throws Exception {
    ComplexJobTriggerProxy t = new ComplexJobTriggerProxy();
    t.setStartTime( now );
    when( scheduleRequest.getComplexJobTrigger() ).thenReturn( t );
    when( scheduleRequest.getTimeZone() ).thenReturn( "GMT" );

    long gmtTime = now.getTime() + TimeZone.getTimeZone( "EST" ).getRawOffset();

    SchedulerResourceUtil.updateStartDateForTimeZone( scheduleRequest );
    assertEquals( gmtTime, scheduleRequest.getComplexJobTrigger().getStartTime().getTime() );
  }

  @Test
  public void testUpdateStartDateForTimeZone_cron() throws Exception {
    CronJobTrigger t = new CronJobTrigger();
    t.setStartTime( now );
    when( scheduleRequest.getCronJobTrigger() ).thenReturn( t );
    when( scheduleRequest.getTimeZone() ).thenReturn( "GMT" );

    long gmtTime = now.getTime() + TimeZone.getTimeZone( "EST" ).getRawOffset();

    SchedulerResourceUtil.updateStartDateForTimeZone( scheduleRequest );
    assertEquals( gmtTime, scheduleRequest.getCronJobTrigger().getStartTime().getTime() );
  }

  @Test
  public void testIsPdiFile_ktr() throws Exception {
    when( repo.getName() ).thenReturn( "transform.ktr" );
    assertTrue( SchedulerResourceUtil.isPdiFile( repo ) );
  }

  @Test
  public void testIsPdiFile_kjb() throws Exception {
    when( repo.getName() ).thenReturn( "job.kjb" );
    assertTrue( SchedulerResourceUtil.isPdiFile( repo ) );
  }

  @Test
  public void testIsPdiFile_txt() throws Exception {
    when( repo.getName() ).thenReturn( "readme.txt" );
    assertFalse( SchedulerResourceUtil.isPdiFile( repo ) );
  }

  @Test
  public void testIsPdiFile_null() throws Exception {
    assertFalse( SchedulerResourceUtil.isPdiFile( null ) );
  }

  @Test
  public void testHandlePdiScheduling_ktr() throws Exception {
    HashMap<String, Serializable> params = new HashMap<>();
    params.put( "test", "value" );
    when( repo.getName() ).thenReturn( "transform.ktr" );
    when( repo.getPath() ).thenReturn( "/home/me/transform.ktr" );

    HashMap<String, Serializable> result = SchedulerResourceUtil.handlePDIScheduling( repo, params );
    assertEquals( params.size() + 2, result.size() );
    assertEquals( "transform", result.get( "transformation" ) );
    assertEquals( "home/me", result.get( "directory" ) );
    assertEquals( "value", ( (HashMap) result.get( "parameters" ) ).get( "test" ) );
  }

  @Test
  public void testHandlePdiScheduling_job() throws Exception {
    HashMap<String, Serializable> params = new HashMap<>();
    params.put( "test", "value" );
    when( repo.getName() ).thenReturn( "job.kjb" );
    when( repo.getPath() ).thenReturn( "/home/me/job.kjb" );

    HashMap<String, Serializable> result = SchedulerResourceUtil.handlePDIScheduling( repo, params );
    assertEquals( params.size() + 2, result.size() );
    assertEquals( "job", result.get( "job" ) );
    assertEquals( "home/me", result.get( "directory" ) );
    assertEquals( "value", ( (HashMap) result.get( "parameters" ) ).get( "test" ) );
  }

  @Test
  public void testHandlePdiScheduling_notPdiFile() throws Exception {
    HashMap<String, Serializable> params = new HashMap<>();
    params.put( "test", "value" );
    when( repo.getName() ).thenReturn( "readme.txt" );

    HashMap<String, Serializable> result = SchedulerResourceUtil.handlePDIScheduling( repo, params );
    assertEquals( params, result );
  }
}
