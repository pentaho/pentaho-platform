/*!
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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.web.http.api.resources.UserSettingsResource;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class UserSettingsResourceTest {

  UserSettingsResource userSettingsResource;

  @Before
  public void setup() {
    userSettingsResource = new UserSettingsResource();
  }

  @After
  public void teardown() {
    userSettingsResource = null;
  }

  @Test
  public void testSetUserSetting() throws Exception {
    IUserSettingService userSettingService = mock( IUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );
    Response response = userSettingsResource.setUserSetting( "favorites", "[{\"fullPath\":\"/public/Steel Wheels/Top Customers (report).prpt\", \"title\":\"<script>if(true&&'str'!='str2')alert(1)</script>\", \"lastUse\":0}]" );

    verify( userSettingService ).setUserSetting( "favorites", "[{&quot;fullPath&quot;:&quot;/public/Steel Wheels/Top Customers (report).prpt&quot;, &quot;title&quot;:&quot;&lt;script&gt;if(true&amp;&amp;'str'!='str2')alert(1)&lt;/script&gt;&quot;, &quot;lastUse&quot;:0}]" );
  }
}