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


package org.pentaho.platform.web.http.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import jakarta.ws.rs.core.Response;

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
    SettingsWrapper response = userSettingsResource.getUserSettings();
    assertNotNull( response );
    assertTrue( !response.getSettings().isEmpty() );
    assertTrue( response.getSettings().get( 0 ).getName().equals( USER_SETTING_NAME ) );
    assertTrue( response.getSettings().get( 0 ).getValue().equals( USER_SETTING_VALUE ) );
  }
}
