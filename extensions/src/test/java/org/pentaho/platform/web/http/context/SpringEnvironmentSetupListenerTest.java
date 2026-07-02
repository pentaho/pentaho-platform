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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/28/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class SpringEnvironmentSetupListenerTest {

  SpringEnvironmentSetupListener setupListener;

  @Mock ServletContextEvent contextEvent;
  @Mock ServletContext context;

  @Before
  public void setUp() throws Exception {
    setupListener = new SpringEnvironmentSetupListener();
    when( contextEvent.getServletContext() ).thenReturn( context );
  }

  @Test
  public void testContextInitialized() throws Exception {
    File tmp = File.createTempFile( "SpringEnvironmentSetupListenerTest", ".tmp" );
    tmp.deleteOnExit();
    when( context.getInitParameter( "solution-path" ) ).thenReturn( tmp.getParent() );

    setupListener.contextInitialized( contextEvent );

    assertEquals( tmp.getParent() + File.separator + "system", System.getProperty( "PentahoSystemPath" ) );
  }

  @Test
  public void testContextDestroyed() throws Exception {
    setupListener.contextDestroyed( contextEvent );
  }
}
