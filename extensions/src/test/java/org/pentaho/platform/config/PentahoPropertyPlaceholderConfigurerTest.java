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
