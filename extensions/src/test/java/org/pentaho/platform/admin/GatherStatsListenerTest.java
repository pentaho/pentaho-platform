package org.pentaho.platform.admin;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/19/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class GatherStatsListenerTest {

  GatherStatsListener gatherStatsListener;

  @Mock IPentahoSession session;
  @Mock IScheduler scheduler;

  @Before
  public void setUp() throws Exception {
    gatherStatsListener = new GatherStatsListener();
    gatherStatsListener.setIntervalInSeconds( 101 );
    gatherStatsListener.setTransFileName( "MyTransform.ktr" );
  }

  @Test
  public void testStartUp() throws Exception {
    PentahoSystem.registerObject( scheduler );

    Job job = mock( Job.class );
    when( scheduler.createJob(
      eq( "Gather Stats" ),
      eq( GatherStatsAction.class ),
      eq( gatherStatsListener.jobMap ),
      any( JobTrigger.class ) ) ).thenReturn( job );

    boolean startup = gatherStatsListener.startup( session );
    assertTrue( startup );
    assertEquals( 1, gatherStatsListener.jobMap.size() );
    assertEquals( gatherStatsListener.getTransFileName(), gatherStatsListener.jobMap.get( "transFileName" ) );
    assertEquals( 101, gatherStatsListener.getIntervalInSeconds() );
    verify( scheduler ).createJob(
      eq( "Gather Stats" ),
      eq( GatherStatsAction.class ),
      eq( gatherStatsListener.jobMap ),
      any( JobTrigger.class ) );
  }

  @Test
  public void testShutdown() throws Exception {
    // for the code coverage game, this method does nothing
    gatherStatsListener.shutdown();
  }

  @Test
  public void testStartup_withExceptionFromScheduleJobCall() throws Exception {
    PentahoSystem.registerObject( scheduler );

    when( scheduler.createJob(
      eq( "Gather Stats" ),
      eq( GatherStatsAction.class ),
      eq( gatherStatsListener.jobMap ),
      any( JobTrigger.class ) ) ).thenThrow( new SchedulerException( "error" ) );

    boolean startup = gatherStatsListener.startup( session );
    assertTrue( startup );
    assertEquals( 1, gatherStatsListener.jobMap.size() );
    assertEquals( gatherStatsListener.getTransFileName(), gatherStatsListener.jobMap.get( "transFileName" ) );
    assertEquals( 101, gatherStatsListener.getIntervalInSeconds() );
    verify( scheduler ).createJob(
      eq( "Gather Stats" ),
      eq( GatherStatsAction.class ),
      eq( gatherStatsListener.jobMap ),
      any( JobTrigger.class ) );
  }

  @After
  public void cleanup() {
    PentahoSystem.clearObjectFactory();
  }
}