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