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

package org.pentaho.platform.web.http.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class UserSettingsResourceTest {

  public static final String USER_SETTING_VALUE =
    "[{\"fullPath\":\"/public/Steel Wheels/Top Customers (report).prpt\",\"title\":\"\\u003Cscript\\u003Eif"
      + "(true\\u0026\\u0026\\u0027str\\u0027!=\\u0027str2\\u0027)alert(1)\\u003C/script\\u003E\",\"lastUse\":0}]";
  public static final String USER_SETTING_NAME = "favorites";

  UserSettingsResource userSettingsResource;

  IUserSettingService userSettingService;

  @Before
  public void setup() {
    userSettingsResource = mock( UserSettingsResource.class );
    userSettingService = mock( IUserSettingService.class );
    when( userSettingsResource.getUserSettingService() ).thenReturn( userSettingService );

  }

  @After
  public void teardown() {
    userSettingsResource = null;
  }

  @Test
  public void testSetUserSetting() throws Exception {

    when( userSettingsResource.getUserSettingService() ).thenCallRealMethod();
    PentahoSystem.registerObject( userSettingService );

    when( userSettingsResource.setUserSetting( nullable( String.class ), nullable( String.class ) ) ).thenCallRealMethod();
    Response response = userSettingsResource.setUserSetting( USER_SETTING_NAME,
      "[{\"fullPath\":\"/public/Steel Wheels/Top Customers (report).prpt\", \"title\":\"<script>if"
        + "(true&&'str'!='str2')alert(1)</script>\", \"lastUse\":0}]" );

    verify( userSettingService ).setUserSetting( USER_SETTING_NAME, USER_SETTING_VALUE );
  }

  @Test
  public void testGetUserSettings() throws Exception {

    IUserSetting userSetting = mock( IUserSetting.class );
    when( userSetting.getSettingName() ).thenReturn( USER_SETTING_NAME );
    when( userSetting.getSettingValue() ).thenReturn( USER_SETTING_VALUE );

    List<IUserSetting> userSettings = new ArrayList<>();
    userSettings.add( userSetting );
    when( userSettingService.getUserSettings() ).thenReturn( userSettings );

    when( userSettingsResource.getUserSettings() ).thenCallRealMethod();
    ArrayList<Setting> response = userSettingsResource.getUserSettings();
    assertNotNull( response );
    assertTrue( !response.isEmpty() );
    assertTrue( response.get( 0 ).getName().equals( USER_SETTING_NAME ) );
    assertTrue( response.get( 0 ).getValue().equals( USER_SETTING_VALUE ) );
  }
}
