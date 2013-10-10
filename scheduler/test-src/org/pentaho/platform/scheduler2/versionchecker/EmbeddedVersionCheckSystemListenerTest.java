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

package org.pentaho.platform.scheduler2.versionchecker;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.scheduler2.quartz.test.StubUserDetailsService;
import org.pentaho.platform.scheduler2.quartz.test.StubUserRoleListService;
import org.pentaho.platform.scheduler2.ws.ParamValue;
import org.pentaho.platform.scheduler2.ws.test.JaxWsSchedulerServiceTest.TstPluginManager;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.security.userdetails.UserDetailsService;

@SuppressWarnings( "nls" )
public class EmbeddedVersionCheckSystemListenerTest {

  // @Autowired
  // private ApplicationContext applicationContext;

  private Map<String, ParamValue> jobParams;
  private IScheduler scheduler;
  static final String TEST_USER = "TestUser";

  @Before
  public void init() throws SchedulerException, PlatformInitializationException {
    MicroPlatform mp = new MicroPlatform();
    mp.define( IPluginManager.class, TstPluginManager.class );
    mp.define( "IScheduler2", TestQuartzScheduler.class );
    mp.define( IUserRoleListService.class, StubUserRoleListService.class );
    mp.define( UserDetailsService.class, StubUserDetailsService.class );
    mp.start();

    scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null );
    scheduler.start();
  }

  @After
  public void after() throws SchedulerException {
    for ( Job job : scheduler.getJobs( null ) ) {
      scheduler.removeJob( job.getJobId() );
    }
  }

  @Test
  public void testCreateJob() throws SchedulerException {
    IPentahoSession testSession = new StandaloneSession( "TEST_USER" );
    TestEmbeddedVersionCheckSystemListener listener = new TestEmbeddedVersionCheckSystemListener();
    // First setup like defaults
    listener.setDisableVersionCheck( false );
    listener.setRequestedReleases( "Minor, GA" );
    listener.setRepeatIntervalSeconds( 86400 );
    Assert.assertFalse( listener.isDisableVersionCheck() );
    Assert.assertEquals( "Minor, GA", listener.getRequestedReleases() );
    Assert.assertEquals( 86400, listener.getRepeatIntervalSeconds() );
    listener.setRepeatIntervalSeconds( 200 );
    Assert.assertEquals( 43200, listener.calculateRepeatSeconds() ); // makes sure that min isn't ignored
    Assert.assertEquals( 0, listener.calculateRequestFlags() ); // Expect 0 because Minor <> minor and GA <> ga
    listener.setRequestedReleases( "minor, ga" );
    Assert.assertEquals( 40, listener.calculateRequestFlags() ); // should be 8 + 32 = 40

    listener.setFakeAvail(); // Fake availability of version checker.
    listener.startup( testSession );

    IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$
    IJobFilter filter = new IJobFilter() {
      public boolean accept( Job job ) {
        return job.getJobName().contains( "PentahoSystemVersionCheck" );
      }
    };
    List<Job> matchingJobs = scheduler.getJobs( filter );
    Assert.assertEquals( 1, matchingJobs.size() );
    Job aJob = matchingJobs.get( 0 );
    Assert.assertTrue( aJob.getJobName().startsWith( "PentahoSystemVersionCheck" ) );

    Assert.assertEquals( TEST_USER, aJob.getUserName() );

    Map<String, Serializable> vcJobParms = aJob.getJobParams();
    Assert.assertTrue( vcJobParms.size() > 0 );
    Assert.assertTrue( vcJobParms.containsKey( VersionCheckerAction.VERSION_REQUEST_FLAGS ) );
    Object val = vcJobParms.get( VersionCheckerAction.VERSION_REQUEST_FLAGS );
    Assert.assertNotNull( val );
    Assert.assertTrue( val instanceof Integer );
    Integer intVal = (Integer) val;
    Assert.assertEquals( 40, intVal.intValue() );
    listener.deleteJobIfNecessary();

    matchingJobs = scheduler.getJobs( null ); // Should have no jobs now
    Assert.assertEquals( 0, matchingJobs.size() );

  }

  public static class TestQuartzScheduler extends QuartzScheduler {
    @Override
    protected String getCurrentUser() {
      SecurityHelper.getInstance().becomeUser( TEST_USER );
      return super.getCurrentUser();
    }
  }

  public static class TestEmbeddedVersionCheckSystemListener extends EmbeddedVersionCheckSystemListener {
    boolean fakeAvail;

    public void setFakeAvail() {
      this.fakeAvail = true;
    }

    public boolean isVersionCheckAvailable() {
      if ( fakeAvail ) {
        return true;
      } else {
        return super.isVersionCheckAvailable();
      }
    }
  }

}
