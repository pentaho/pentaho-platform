/*!
 *
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
 *
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 *
 */


package org.pentaho.platform.scheduler2.quartz;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Matchers.any;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.Trigger;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class QuartzSchedulerTest {


  private static IUnifiedRepository repo;
  private static IUnifiedRepository oldRepo;


  @BeforeClass
  public static void setUp() throws Exception {

    oldRepo = PentahoSystem.get( IUnifiedRepository.class );
    repo = Mockito.mock( IUnifiedRepository.class );
    Mockito.when( repo.getFile( Mockito.anyString() ) ).then( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
        final RepositoryFile repositoryFile = Mockito.mock( RepositoryFile.class );
        final String param = (String) invocationOnMock.getArguments()[ 0 ];
        if ( "/home/admin/notexist.ktr".equals( param ) ) {
          return null;
        }
        if ( "/home/admin".equals( param ) ) {
          Mockito.when( repositoryFile.isFolder() ).thenReturn( true );
        }
        if ( "/home/admin/notallowed.ktr".equals( param ) ) {
          Mockito.when( repositoryFile.isFolder() ).thenReturn( false );
          Mockito.when( repositoryFile.isSchedulable() ).thenReturn( false );
        }
        if ( "/home/admin/allowed.ktr".equals( param ) ) {
          Mockito.when( repositoryFile.isFolder() ).thenReturn( false );
          Mockito.when( repositoryFile.isSchedulable() ).thenReturn( true );
        }
        return repositoryFile;
      }
    } );
    PentahoSystem.registerObject( repo, IUnifiedRepository.class );
  }

  @AfterClass
  public static void tearDown() throws Exception {
    repo = null;
    if ( oldRepo != null ) {
      PentahoSystem.registerObject( oldRepo, IUnifiedRepository.class );
    }
  }

  @Test
  public void testValidateParamsNoStreamProviderParam() throws SchedulerException {
    new QuartzScheduler().validateJobParams( Collections.emptyMap() );
  }

  @Test
  public void testValidateParamsNoStringConf() throws SchedulerException {
    new QuartzScheduler()
      .validateJobParams( Collections.singletonMap( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, 1L ) );
  }

  @Test
  public void testValidateParamsNoInputFile() throws SchedulerException {
    new QuartzScheduler()
      .validateJobParams( Collections.singletonMap( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, "someinputfile" ) );
  }

  @Test( expected = SchedulerException.class )
  public void testValidateParamsFileNotFound() throws SchedulerException {
    new QuartzScheduler()
      .validateJobParams( Collections.singletonMap( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER,
        "input = /home/admin/notexist.ktr : output = /home/admin/notexist" ) );
  }

  @Test( expected = SchedulerException.class )
  public void testValidateParamsFileIsFolder() throws SchedulerException {
    new QuartzScheduler()
      .validateJobParams( Collections.singletonMap( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER,
        "input = /home/admin : output = /home/admin/notexist" ) );
  }

  @Test( expected = SchedulerException.class )
  public void testValidateParamsSchedulingNotAllowed() throws SchedulerException {
    new QuartzScheduler()
      .validateJobParams( Collections.singletonMap( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER,
        "input = /home/admin/notallowed.ktr : output = /home/admin/notallowed" ) );
  }

  @Test
  public void testValidateParamsSchedulingAllowed() throws SchedulerException {
    new QuartzScheduler()
      .validateJobParams( Collections.singletonMap( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER,
        "input = /home/admin/allowed.ktr : output = /home/admin/allowed." ) );
  }

  @Test
  public void testSetTimezone() throws Exception {

    CronTrigger cronTrigger = new CronTrigger();
    cronTrigger.setCronExpression( new CronExpression( "0 15 10 ? * 6L 2002-2018" ) );
    String currentTimezoneId = TimeZone.getDefault().getID();

    new QuartzScheduler().setTimezone( cronTrigger, currentTimezoneId );

    assertNotNull( cronTrigger.getTimeZone() );
    assertEquals( currentTimezoneId, cronTrigger.getTimeZone().getID() );
  }

  @Test
  public void testSetJobNextRunToTheFuture() {

    Trigger trigger = Mockito.mock( Trigger.class );
    Job job = new Job();
    QuartzScheduler quartzScheduler = new QuartzScheduler();
    long nowDate = new Date().getTime();
    long futureDate = nowDate+1000000000;

    Mockito.when( trigger.getNextFireTime() ).thenReturn( new Date( futureDate ) );
    Mockito.when( trigger.getFireTimeAfter( any() ) ).thenReturn( new Date( nowDate ) );

    quartzScheduler.setJobNextRun( job, trigger );

    assertEquals( new Date( futureDate ), job.getNextRun() );
  }

  @Test
  public void testSetJobNextRunToThePast() {

    Trigger trigger = Mockito.mock( Trigger.class );
    Job job = new Job();
    QuartzScheduler quartzScheduler = new QuartzScheduler();
    long nowDate = new Date().getTime();
    long pastDate = nowDate-1000000000;

    Mockito.when( trigger.getNextFireTime() ).thenReturn( new Date( pastDate ) );
    Mockito.when( trigger.getFireTimeAfter( any() ) ).thenReturn( new Date( nowDate ) );

    quartzScheduler.setJobNextRun( job, trigger );

    assertEquals( new Date( nowDate ), job.getNextRun() );
  }

  @Test
  public void testSetJobNextRunToNullDate() {

    Trigger trigger = Mockito.mock( Trigger.class );
    Job job = new Job();
    QuartzScheduler quartzScheduler = new QuartzScheduler();
    long nowDate = new Date().getTime();

    Mockito.when( trigger.getNextFireTime() ).thenReturn( null );
    Mockito.when( trigger.getFireTimeAfter( any() ) ).thenReturn( new Date( nowDate ) );

    quartzScheduler.setJobNextRun( job, trigger );

    assertEquals( null,  job.getNextRun() );
  }

}
