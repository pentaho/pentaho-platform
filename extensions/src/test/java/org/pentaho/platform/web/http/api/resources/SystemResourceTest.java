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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.util.Assert;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;

public class SystemResourceTest {
  SystemResource systemResource;
  private static MicroPlatform platform;

  @BeforeClass
  public static void initPlatform() throws Exception {
    platform = new MicroPlatform();
    platform.defineInstance( IUserSettingService.class, mock( IUserSettingService.class ) );
    platform.start();
  }

  @Before
  public void setup() {
    PentahoSessionHolder.setSession( null );
    systemResource = new SystemResource();
  }

  @After
  public void teardown() {
    systemResource = null;
  }

  @AfterClass
  public static void shutdownPlatform() {
    platform.stop();
  }

  @Test
  public void testSetLocaleOverride() {
    Response resp = null;
    try {
      resp = systemResource.setLocaleOverride( "en_US" );
    } catch ( Exception e ) {

    }
    Assert.notNull(resp);
  }
}
