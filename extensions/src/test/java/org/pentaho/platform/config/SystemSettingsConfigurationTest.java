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

package org.pentaho.platform.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/22/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class SystemSettingsConfigurationTest {

  SystemSettingsConfiguration config;
  @Mock ISystemSettings settings;

  @Before
  public void setUp() throws Exception {
    config = new SystemSettingsConfiguration( "id", settings );

    // id is hardcoded, the id passed is ignored
    assertEquals( "system", config.getId() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_IllegalId() throws Exception {
    config = new SystemSettingsConfiguration( null, settings );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_IllegalSettings() throws Exception {
    config = new SystemSettingsConfiguration( "id", null );
  }

  @Test
  public void testGetProperties() {
    SystemSettings settings = new SystemSettings();

    IApplicationContext appContext = mock( IApplicationContext.class );
    when( appContext.getSolutionPath( nullable( String.class ) ) )
      .thenReturn( TestResourceLocation.TEST_RESOURCES + "/solution/system" );

    PentahoSystem.setApplicationContext( appContext );

    config = new SystemSettingsConfiguration( "system", settings );

    Properties properties = config.getProperties();
    assertNotNull( properties );
    assertEquals( "DEBUG", properties.getProperty( "log-level" ) );
  }

  @Test( expected = UnsupportedOperationException.class )
  public void testUpdate() throws Exception {
    config.update( new Properties() );
  }
}
