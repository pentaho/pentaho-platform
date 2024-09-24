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