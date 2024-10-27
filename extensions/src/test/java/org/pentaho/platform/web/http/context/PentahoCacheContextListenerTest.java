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


package org.pentaho.platform.web.http.context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import jakarta.servlet.ServletContextEvent;

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
