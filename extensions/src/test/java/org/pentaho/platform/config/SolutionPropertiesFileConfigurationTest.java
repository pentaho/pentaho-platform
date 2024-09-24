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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/22/15.
 */
public class SolutionPropertiesFileConfigurationTest {
  File f;

  @Before
  public void setUp() throws Exception {
    f = File.createTempFile( "SolutionPropertiesFileConfigurationTest", ".tmp" );
  }

  @Test
  public void testGetSolutionPath_fromSystemProperties() throws Exception {
    System.setProperty( "PentahoSystemPath", f.getParent() );
    PentahoSystem.setApplicationContext( null );
    String solutionPath = SolutionPropertiesFileConfiguration.getSolutionPath();
    assertEquals( f.getParent(), solutionPath );
  }

  @Test
  public void testGetSolutionPath_fromAppContext() throws Exception {
    IApplicationContext appContext = mock( IApplicationContext.class );
    when( appContext.getSolutionPath( "system" ) ).thenReturn( f.getParent() );

    PentahoSystem.setApplicationContext( appContext );

    String solutionPath = SolutionPropertiesFileConfiguration.getSolutionPath();
    assertEquals( f.getParent(), solutionPath );
  }

  @After
  public void tearDown() throws Exception {
    f.delete();
    PentahoSystem.clearObjectFactory();
  }

  @Test ( expected = IllegalArgumentException.class )
  public void testConstructor() throws Exception {
    new SolutionPropertiesFileConfiguration( "id", "props.properties" );
  }
}
