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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import java.io.File;

import static org.junit.Assert.*;
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
