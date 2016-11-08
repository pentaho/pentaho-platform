package org.pentaho.platform.admin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.test.platform.utils.TestResourceLocation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/19/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class GatherStatsActionTest {
  GatherStatsAction gatherStatsAction;
  @Mock IApplicationContext appContext;

  @Before
  public void setUp() throws Exception {
    gatherStatsAction = new GatherStatsAction();
    PentahoSystem.setApplicationContext( appContext );
  }

  @Test( expected = KettleXMLException.class )
  public void testExecute_nullJobFilePath() throws Exception {

    GatherStatsAction spyStatsAction = spy( gatherStatsAction );
    doReturn( null ).when( spyStatsAction ).getJobFileFullPath();

    gatherStatsAction.execute();
  }

  @Test
  public void testGetJobFileFullPath() throws Exception {
    when( appContext.getSolutionPath( "system" ) ).thenReturn( TestResourceLocation.TEST_RESOURCES + "/FileOutputResourceTest/system" );
    gatherStatsAction.setTransFileName( "HelloWorld.ktr" );
    String jobFileFullPath = gatherStatsAction.getJobFileFullPath();
    assertEquals( TestResourceLocation.TEST_RESOURCES + "/FileOutputResourceTest/system/HelloWorld.ktr", jobFileFullPath );
  }
}