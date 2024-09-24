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
import org.pentaho.platform.api.engine.ISystemConfig;

import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 10/22/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoPropertyPlaceholderConfigurerTest {
  PentahoPropertyPlaceholderConfigurer configurer;
  @Mock ISystemConfig systemConfig;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testConstructor() throws Exception {
    configurer = new PentahoPropertyPlaceholderConfigurer( systemConfig );
    assertNotNull( configurer );
    assertEquals( systemConfig, configurer.getSystemConfig() );
  }

  @Test ( expected = IllegalArgumentException.class )
  public void testConstructor_nullConfig() throws Exception {
    configurer = new PentahoPropertyPlaceholderConfigurer( null );
  }

  @Test
  public void testResolvePlaceholder() throws Exception {
    when( systemConfig.getProperty( "prop" ) ).thenReturn( "val" );

    configurer = new PentahoPropertyPlaceholderConfigurer( systemConfig );

    String prop = configurer.resolvePlaceholder( "prop", null );
    assertEquals( prop, "val" );
  }

  @Test
  public void testResolvePlaceholder_fallback() throws Exception {
    when( systemConfig.getProperty( "prop" ) ).thenReturn( null );

    configurer = spy( new PentahoPropertyPlaceholderConfigurer( systemConfig ) );

    Properties properties = mock( Properties.class );
    when( properties.getProperty( "prop" ) ).thenReturn( "fallbackValue" );

    String prop = configurer.resolvePlaceholder( "prop", properties );
    assertEquals( prop, "fallbackValue" );
  }
}
