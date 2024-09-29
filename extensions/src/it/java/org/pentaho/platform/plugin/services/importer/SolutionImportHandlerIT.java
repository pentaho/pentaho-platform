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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importer;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.Job.JobState;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.scheduler2.quartz.test.StubUserRoleListService;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.pentaho.test.platform.engine.core.MicroPlatform;

public class SolutionImportHandlerIT extends Assert {

  private IScheduler scheduler;
  static final String TEST_USER = "TestUser";
  @Before
  public void init() throws PlatformInitializationException, SchedulerException {

    MicroPlatform mp = new MicroPlatform();

    mp.define( "IScheduler", TestQuartzScheduler.class );
    mp.define( IUserRoleListService.class, StubUserRoleListService.class );

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( nullable( String.class ) ) ).thenReturn( true );
    mp.defineInstance( IAuthorizationPolicy.class, policy );

    mp.start();

    scheduler = PentahoSystem.get( IScheduler.class );
    scheduler.start();
  }

  @Test
  public void testImportSchedules() throws PlatformImportException, SchedulerException {
    SolutionImportHandler importHandler = new SolutionImportHandler( Collections.emptyList() );
    importHandler = spy( importHandler );

    List<JobScheduleRequest> requests = new ArrayList<>( 4 );
    requests.add( createJobScheduleRequest( "NORMAL", JobState.NORMAL ) );
    requests.add( createJobScheduleRequest( "PAUSED", JobState.PAUSED ) );
    requests.add( createJobScheduleRequest( "PAUSED", JobState.COMPLETE ) );
    requests.add( createJobScheduleRequest( "PAUSED", JobState.ERROR ) );

    doReturn( new ArrayList<Job>(  ) ).when( importHandler ).getAllJobs( any() );
    importHandler.importSchedules( requests );

    List<Job> jobs = scheduler.getJobs( job -> true );

    assertEquals( 4, jobs.size() );

    for ( Job job : jobs ) {
      assertEquals( job.getJobName(), job.getState().toString() );
    }
  }

  private JobScheduleRequest createJobScheduleRequest( String name, JobState jobState ) {
    JobScheduleRequest scheduleRequest = mock( JobScheduleRequest.class );
    doReturn( TestAction.class.getName() ).when( scheduleRequest ).getActionClass();
    doReturn( name ).when( scheduleRequest ).getJobName();
    doReturn( jobState ).when( scheduleRequest ).getJobState();
    return scheduleRequest;
  }

  public static class TestAction implements IAction {
    @Override
    public void execute() throws Exception {
    }
  }

  public static class TestQuartzScheduler extends QuartzScheduler {
    @Override
    protected String getCurrentUser() {
      SecurityHelper.getInstance().becomeUser( TEST_USER );
      return super.getCurrentUser();
    }
  }
}
