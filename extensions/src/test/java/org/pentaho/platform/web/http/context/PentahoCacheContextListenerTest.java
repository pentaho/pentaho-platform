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

package org.pentaho.platform.web.http.context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.ServletContextEvent;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by rfellows on 10/28/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoCacheContextListenerTest {

  PentahoCacheContextListener pentahoCacheContextListener;

  @Mock ServletContextEvent contextEvent;
  @Mock ICacheManager cacheManager;

  @Before
  public void setUp() throws Exception {
    pentahoCacheContextListener = new PentahoCacheContextListener();
  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testContextInitialized() throws Exception {
    // this method does nothing, call it for code coverage
    pentahoCacheContextListener.contextInitialized( contextEvent );
  }

  @Test
  public void testContextDestroyed() throws Exception {
    PentahoSystem.registerObject( cacheManager );
    pentahoCacheContextListener.contextDestroyed( contextEvent );
    verify( cacheManager ).cacheStop();
  }

  @Test
  public void testContextDestroyed_noCachManager() throws Exception {
    pentahoCacheContextListener.contextDestroyed( contextEvent );
    verify( cacheManager, never() ).cacheStop();
  }

}
