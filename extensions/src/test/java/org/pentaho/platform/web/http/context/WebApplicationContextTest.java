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
