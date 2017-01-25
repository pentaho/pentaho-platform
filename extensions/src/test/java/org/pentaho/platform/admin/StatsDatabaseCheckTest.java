package org.pentaho.platform.admin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.test.platform.utils.TestResourceLocation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 10/20/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class StatsDatabaseCheckTest {
  StatsDatabaseCheck statsDatabaseCheck;

  @Mock IPentahoSession session;
  @Mock IApplicationContext appContext;

  @Before
  public void setUp() throws Exception {
    statsDatabaseCheck = new StatsDatabaseCheck();
    PentahoSystem.setApplicationContext( appContext );
  }

  @Test
  public void testGetJobFileFullPath() throws Exception {
    when( appContext.getSolutionPath( "system" ) ).thenReturn( TestResourceLocation.TEST_RESOURCES + "/FileOutputResourceTest/system" );
    statsDatabaseCheck.setJobFileName( "HelloWorld.kjb" );

    statsDatabaseCheck.getJobFileFullPath();
    assertEquals( TestResourceLocation.TEST_RESOURCES + "/FileOutputResourceTest/system/HelloWorld.kjb", statsDatabaseCheck.getJobFileFullPath() );
  }

  @Test
  public void testShutdown() throws Exception {
    // code coverage test
    statsDatabaseCheck.shutdown();
  }

  @Test
  public void testStartup() throws Exception {
    StatsDatabaseCheck spyCheck = spy( statsDatabaseCheck );
    doReturn( null ).when( spyCheck ).getJobFileFullPath();

    boolean startup = spyCheck.startup( session );
    assertFalse( startup );
  }

  @Test
  public void testExecuteJob() throws Exception {
    JobMeta jobMeta = mock( JobMeta.class );
    String filePath = TestResourceLocation.TEST_RESOURCES + "/FileOutputResourceTest/system/HelloWorld.kjb";
    when( jobMeta.getFilename() ).thenReturn( filePath );

    boolean executeJob = statsDatabaseCheck.executeJob( jobMeta, filePath );
    assertFalse( executeJob );
  }

}