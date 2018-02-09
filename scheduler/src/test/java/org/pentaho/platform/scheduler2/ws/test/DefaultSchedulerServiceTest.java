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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.scheduler2.ws.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.scheduler2.ws.DefaultSchedulerService;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { PentahoSystem.class, PentahoSessionHolder.class } )
public class DefaultSchedulerServiceTest {

  @Spy
  DefaultSchedulerService defaultSchedulerService = new DefaultSchedulerService();
  @Mock
  IScheduler iSchedulerMock;
  @Mock
  IPentahoSession iPentahoSessionMock;
  @Mock
  IAuthorizationPolicy policy;
  @Captor
  ArgumentCaptor filterCaptor;

  @Before
  public void setup() {
    mockStatic( PentahoSystem.class );
    mockStatic( PentahoSessionHolder.class );
    when( PentahoSystem.get( IScheduler.class, "IScheduler2", null ) ).thenReturn( iSchedulerMock );
    when( PentahoSessionHolder.getSession() ).thenReturn( iPentahoSessionMock );
    when( PentahoSystem.get( IAuthorizationPolicy.class ) ).thenReturn( policy );
  }

  @Test
  public void testGetJobsNonAdminUser() throws Exception {
    when( policy.isAllowed( anyString() ) ).thenReturn( false );
    when( iPentahoSessionMock.getName() ).thenReturn( "testUser1" );
    defaultSchedulerService.getJobs();
    verify( iSchedulerMock ).getJobs( (IJobFilter) filterCaptor.capture() );
    IJobFilter filter = (IJobFilter) filterCaptor.getValue();
    assertNotNull( filter );
    List<Job> testJobs = getJobs();
    List<Job> filteredJobs = new ArrayList<>();
    for ( Job job : testJobs ) {
      if ( filter.accept( job ) ) {
        filteredJobs.add( job );
      }
    }
    assertEquals( 1, filteredJobs.size() );
    assertEquals( "testJobName1", filteredJobs.get( 0 ).getJobName() );
    assertEquals( "testUser1", filteredJobs.get( 0 ).getUserName() );
  }

  @Test
  public void testGetJobsAdminUser() throws Exception {
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    when( iPentahoSessionMock.getName() ).thenReturn( "admin" );
    defaultSchedulerService.getJobs();
    verify( iSchedulerMock ).getJobs( (IJobFilter) filterCaptor.capture() );
    IJobFilter filter = (IJobFilter) filterCaptor.getValue();
    assertNotNull( filter );
    List<Job> testJobs = getJobs();
    List<Job> filteredJobs = new ArrayList<>();
    for ( Job job : testJobs ) {
      if ( filter.accept( job ) ) {
        filteredJobs.add( job );
        assertNotEquals( "BlockoutAction", job.getJobName() );
      }
    }
    assertEquals( 10, filteredJobs.size() );
  }

  private List<Job> getJobs() {
    List<Job> jobs = new ArrayList<>();
    for ( int i = 0; i < 10; i++ ) {
      jobs.add( mockJob( "testUser" + i, "testJobName" + i ) );
    }
    jobs.add( mockJob( "system", "BlockoutAction" ) );
    return jobs;
  }

  private Job mockJob( String userName, String jobName ) {
    Job job = mock( Job.class );
    when( job.getUserName() ).thenReturn( userName );
    when( job.getJobName() ).thenReturn( jobName );
    return job;
  }
}
