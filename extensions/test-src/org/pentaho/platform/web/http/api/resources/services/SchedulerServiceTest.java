/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2014 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.services;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.web.http.api.resources.JobRequest;

public class SchedulerServiceTest {


  private static SchedulerService schedulerService;

  @Before
  public void setUp() {
    schedulerService = spy( new SchedulerService() );
    schedulerService.policy = mock( IAuthorizationPolicy.class );
    schedulerService.scheduler = mock( IScheduler.class );
  }

  @After
  public void cleanup() {
    schedulerService = null;
  }


  @Test
  public void testTriggerNow() throws SchedulerException {

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

}
