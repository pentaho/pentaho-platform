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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

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
