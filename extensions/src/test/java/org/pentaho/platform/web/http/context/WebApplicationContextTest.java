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
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/28/15.
 */
public class WebApplicationContextTest {

  WebApplicationContext webAppContext;

  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void testGetPentahoServerName() throws Exception {
    webAppContext = new WebApplicationContext( "rootPath", null, null );
    ISystemSettings settingsService = mock( ISystemSettings.class );
    when( settingsService.getSystemSetting( eq( "name"), nullable( String.class ) ) ).thenReturn( "PENTAHO" );
    PentahoSystem.setSystemSettingsService( settingsService );
    assertEquals( "PENTAHO", webAppContext.getPentahoServerName() );
  }

  @Test
  public void testGetSetFullyQualifiedServerURL() throws Exception {
    webAppContext = new WebApplicationContext( "rootPath", "", null, null );
    assertEquals( "/", webAppContext.getFullyQualifiedServerURL() );
    webAppContext.setFullyQualifiedServerURL( "http://localhost:8080/pentaho" );
    assertEquals( "http://localhost:8080/pentaho/", webAppContext.getFullyQualifiedServerURL() );
  }
}
