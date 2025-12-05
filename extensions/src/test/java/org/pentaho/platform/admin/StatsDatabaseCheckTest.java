/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.admin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.job.JobMeta;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.test.platform.utils.TestResourceLocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
  public void testGetJobFileFullPath() {
    when( appContext.getSolutionPath( "system" ) ).thenReturn( TestResourceLocation.TEST_RESOURCES + "/FileOutputResourceTest/system" );
    statsDatabaseCheck.setJobFileName( "HelloWorld.kjb" );

    statsDatabaseCheck.getJobFileFullPath();
    assertEquals( TestResourceLocation.TEST_RESOURCES + "/FileOutputResourceTest/system/HelloWorld.kjb", statsDatabaseCheck.getJobFileFullPath() );
  }

  @Test
  public void testShutdown() {
    // code coverage test
    statsDatabaseCheck.shutdown();
  }

  @Test
  public void testStartup() {
    StatsDatabaseCheck spyCheck = spy( statsDatabaseCheck );
    doReturn( null ).when( spyCheck ).getJobFileFullPath();

    boolean startup = spyCheck.startup( session );
    assertFalse( startup );
  }

  @Test
  public void testExecuteJob() {
    JobMeta jobMeta = mock( JobMeta.class );
    String filePath = TestResourceLocation.TEST_RESOURCES + "/FileOutputResourceTest/system/HelloWorld.kjb";
    when( jobMeta.getFilename() ).thenReturn( filePath );

    boolean executeJob = statsDatabaseCheck.executeJob( jobMeta, filePath );
    assertFalse( executeJob );
  }

}