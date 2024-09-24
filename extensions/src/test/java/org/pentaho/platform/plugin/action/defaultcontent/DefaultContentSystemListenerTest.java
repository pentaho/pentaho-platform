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

package org.pentaho.platform.plugin.action.defaultcontent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;

import static org.junit.Assert.assertTrue;

/**
 * Created by rfellows on 10/20/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class DefaultContentSystemListenerTest {
  @Mock ISystemConfig systemConfig;
  @Mock IApplicationContext appContext;
  @Mock IPlatformImporter platformImporter;
  @Mock IPentahoSession session;

  DefaultContentSystemListener defaultContentSystemListener;

  @Before
  public void setUp() throws Exception {
    PentahoSystem.registerObject( systemConfig );
    PentahoSystem.setApplicationContext( appContext );
    PentahoSystem.registerObject( platformImporter );

    defaultContentSystemListener = new DefaultContentSystemListener();
  }

  @Test
  public void testShutdown() {
    // no code in here, solely for unit test coverage
    defaultContentSystemListener.shutdown();
  }

  @Test
  public void testStartup() {
    boolean startup = defaultContentSystemListener.startup( session );
    assertTrue( startup );
  }
}