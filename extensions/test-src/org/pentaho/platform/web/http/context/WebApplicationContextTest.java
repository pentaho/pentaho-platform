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
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
    when( settingsService.getSystemSetting( eq( "name"), anyString() ) ).thenReturn( "PENTAHO" );
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
