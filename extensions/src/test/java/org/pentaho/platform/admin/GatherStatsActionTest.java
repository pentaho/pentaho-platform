/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.admin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.test.platform.utils.TestResourceLocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
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

  @Test
  public void testExecute_nullJobFilePath() throws Exception {
    try {
      gatherStatsAction.execute();
      fail( "Expected execute() to throw when trans file path is not configured" );
    } catch ( Exception ignored ) {
      // TransMeta creation may fail before XML parsing when Kettle plugins are not initialized in unit test context.
    }
  }

  @Test
  public void testExecuteClearsBowlCacheBeforeLoadingMetadata() throws Exception {
    GatherStatsAction spyAction = spy( gatherStatsAction );
    doNothing().when( spyAction ).clearBowlCache();

    try {
      spyAction.execute();
    } catch ( Exception ignored ) {
      // expected - TransMeta creation may throw if Kettle plugins are not initialized
    }

    verify( spyAction ).clearBowlCache();
  }

  @Test
  public void testGetJobFileFullPath() throws Exception {
    when( appContext.getSolutionPath( "system" ) ).thenReturn( TestResourceLocation.TEST_RESOURCES + "/FileOutputResourceTest/system" );
    gatherStatsAction.setTransFileName( "HelloWorld.ktr" );
    String jobFileFullPath = gatherStatsAction.getJobFileFullPath();
    assertEquals( TestResourceLocation.TEST_RESOURCES + "/FileOutputResourceTest/system/HelloWorld.ktr", jobFileFullPath );
  }
}