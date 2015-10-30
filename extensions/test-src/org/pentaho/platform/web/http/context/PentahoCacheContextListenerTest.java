/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.web.http.context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.ServletContextEvent;

import static org.junit.Assert.*;
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
