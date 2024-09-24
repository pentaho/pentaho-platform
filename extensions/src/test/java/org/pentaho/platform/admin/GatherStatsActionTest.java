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

package org.pentaho.platform.admin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.test.platform.utils.TestResourceLocation;

import static org.junit.Assert.assertEquals;
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