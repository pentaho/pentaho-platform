/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository.usersettings;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserSettingServiceTest {
  private static final String USER_FOLDER_ID = "userFolderID";
  private static final String TENANT_FOLDER_ID = "tenantFolderID";
  private static final String COMMON_SETTING_NAME = "commonSettingName";
  private static final String COMMON_USER_SETTING_VALUE = "userSettingValue";
  private static final String COMMON_GLOBAL_SETTING_VALUE = "globalSettingValue";

  private static final String USER_SETTING_NAME_1 = "setting1";
  private static final String USER_SETTING_NAME_2 = "setting2";
  private static final String USER_SETTING_NAME_3 = "setting3";
  private static final String USER_SETTING_VALUE_1 = "1";
  private static final String USER_SETTING_VALUE_2 = "2";
  private static final String USER_SETTING_VALUE_3 = "3";

  private static final String GLOBAL_SETTING_NAME_1 = "setting11";
  private static final String GLOBAL_SETTING_NAME_2 = "setting22";
  private static final String GLOBAL_SETTING_NAME_3 = "setting33";
  private static final String GLOBAL_SETTING_VALUE_1 = "11";
  private static final String GLOBAL_SETTING_VALUE_2 = "22";
  private static final String GLOBAL_SETTING_VALUE_3 = "33";

  private UserSettingService userSettingService;
  private IUnifiedRepository repository;
  private IPentahoSession session;
  private HashMap<String, Serializable> userSettings;
  private HashMap<String, Serializable> globalSettings;
  private ISecurityHelper securityHelper;

  @Before
  public void setUp() throws Exception {
    repository = mock( IUnifiedRepository.class );

    session = mock( IPentahoSession.class );
    when( session.getName() ).thenReturn( "test" );
    PentahoSessionHolder.setSession( session );

    userSettings = new HashMap<String, Serializable>() { {
        put( USER_SETTING_NAME_1, USER_SETTING_VALUE_1 );
        put( UserSettingService.SETTING_PREFIX + COMMON_SETTING_NAME, COMMON_USER_SETTING_VALUE );
        put( USER_SETTING_NAME_2, USER_SETTING_VALUE_2 );
        put( UserSettingService.SETTING_PREFIX + USER_SETTING_NAME_3, USER_SETTING_VALUE_3 );
      } };
    globalSettings = new HashMap<String, Serializable>() { {
        put( GLOBAL_SETTING_NAME_1, GLOBAL_SETTING_VALUE_1 );
        put( UserSettingService.SETTING_PREFIX + COMMON_SETTING_NAME, COMMON_GLOBAL_SETTING_VALUE );
        put( GLOBAL_SETTING_NAME_2, GLOBAL_SETTING_VALUE_2 );
        put( UserSettingService.SETTING_PREFIX + GLOBAL_SETTING_NAME_3, GLOBAL_SETTING_VALUE_3 );
      } };

    when( repository.getFileMetadata( eq( USER_FOLDER_ID ) ) ).thenReturn( userSettings );
    when( repository.getFileMetadata( eq( TENANT_FOLDER_ID ) ) ).thenReturn( globalSettings );

    final RepositoryFile tenantRepositoryFile = mock( RepositoryFile.class );
    when( tenantRepositoryFile.getId() ).thenReturn( TENANT_FOLDER_ID );
    when( repository.getFile( eq( ClientRepositoryPaths.getEtcFolderPath() ) ) ).thenReturn( tenantRepositoryFile );

    final RepositoryFile userRepositoryFile = mock( RepositoryFile.class );
    when( userRepositoryFile.getId() ).thenReturn( USER_FOLDER_ID );
    when( repository.getFile( eq( ClientRepositoryPaths.getUserHomeFolderPath( session.getName() ) ) ) ).thenReturn( userRepositoryFile );

    securityHelper = mock( ISecurityHelper.class );
    when( securityHelper.runAsSystem( any( Callable.class ) ) ).thenAnswer( new Answer<Object>() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        final Callable callable = (Callable) invocation.getArguments()[0];
        if ( callable != null ) {
          return callable.call();
        }
        return null;
      }
    } );
    SecurityHelper.setMockInstance( securityHelper );

    userSettingService = new UserSettingServiceForTesting( repository );
    userSettingService.init( session );
  }

  @Test
  public void testDeleteUserSettings() throws Exception {
    final RepositoryFile repositoryFile = mock( RepositoryFile.class );
    when( repositoryFile.getId() ).thenReturn( USER_FOLDER_ID );
    when( repository.getFile( anyString() ) ).thenReturn( repositoryFile );

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    doAnswer( new Answer() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {

        final Map<String, Serializable> settings = (Map<String, Serializable>) invocation.getArguments()[1];

        assertNotNull( settings );
        assertEquals( 2, settings.size() );

        final Iterator<String> iterator = settings.keySet().iterator();
        assertFalse( iterator.next().startsWith( UserSettingService.SETTING_PREFIX ) );
        assertFalse( iterator.next().startsWith( UserSettingService.SETTING_PREFIX ) );

        return null;
      }
    } ).when( repository ).setFileMetadata( eq( USER_FOLDER_ID ), anyMap() );

    userSettingService.deleteUserSettings();
  }

  @Test
  public void testGetUserSettingsByName() throws Exception {

    try {

      ( (UserSettingServiceForTesting) userSettingService ).setCanAdministerOverrideValue( true ); // autoboxing

      final List<IUserSetting> settings = userSettingService.getUserSettings( "test" );

      assertNotNull( settings );
      assertEquals( 2, settings.size() );
      for ( IUserSetting setting : settings ) {
        if ( COMMON_SETTING_NAME.equals( setting.getSettingName() ) ) {
          assertEquals( COMMON_USER_SETTING_VALUE, setting.getSettingValue() );
        } else if ( USER_SETTING_NAME_3.equals( setting.getSettingName() ) ) {
          assertEquals( USER_SETTING_VALUE_3, setting.getSettingValue() );
        } else if ( GLOBAL_SETTING_NAME_3.equals( setting.getSettingName() ) ) {
          assertEquals( GLOBAL_SETTING_VALUE_3, setting.getSettingValue() );
        }
      }

    } finally {
      ( (UserSettingServiceForTesting) userSettingService ).setCanAdministerOverrideValue( null );
    }
  }

  @Test
  public void testGetUserSettingByName() throws Exception {
    final String settingName = USER_SETTING_NAME_3;
    final String defaultValue = "defaultValue";

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    //try to get existing setting
    final IUserSetting userSetting = userSettingService.getUserSetting( "test", settingName, defaultValue );
    assertEquals( settingName, userSetting.getSettingName() );
    assertEquals( USER_SETTING_VALUE_3, userSetting.getSettingValue() );
  }

  @Test
  public void testDeleteUserSettingsByName() throws Exception {
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    final RepositoryFile repositoryFile = mock( RepositoryFile.class );
    when( repositoryFile.getId() ).thenReturn( USER_FOLDER_ID );
    when( repository.getFile( anyString() ) ).thenReturn( repositoryFile );

    doAnswer( new Answer() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {

        final Map<String, Serializable> settings = (Map<String, Serializable>) invocation.getArguments()[1];

        assertNotNull( settings );
        assertEquals( 2, settings.size() );

        final Iterator<String> iterator = settings.keySet().iterator();
        assertFalse( iterator.next().startsWith( UserSettingService.SETTING_PREFIX ) );
        assertFalse( iterator.next().startsWith( UserSettingService.SETTING_PREFIX ) );

        return null;
      }
    } ).when( repository ).setFileMetadata( eq( USER_FOLDER_ID ), anyMap() );

    userSettingService.deleteUserSettings( "test" );
  }

  @Test
  public void testSetUserSettingByName() throws Exception {
    final String settingName = "settingName";
    final String settingValue = "settingValue";

    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    PentahoSystem.registerObject( policy );

    assertEquals( 4, userSettings.size() );
    userSettingService.setUserSetting( "test", settingName, settingValue );
    assertEquals( 5, userSettings.size() );

    final Serializable value = userSettings.get( UserSettingService.SETTING_PREFIX + settingName );
    assertEquals( settingValue, value );

    verify( repository ).setFileMetadata( eq( USER_FOLDER_ID ), anyMap() );
  }

  @Test
  public void testGetUserSettings() throws Exception {
    final List<IUserSetting> settings = userSettingService.getUserSettings();

    assertNotNull( settings );
    assertEquals( 3, settings.size() );
    for ( IUserSetting setting : settings ) {
      if ( COMMON_SETTING_NAME.equals( setting.getSettingName() ) ) {
        assertEquals( COMMON_USER_SETTING_VALUE, setting.getSettingValue() );
      } else if ( USER_SETTING_NAME_3.equals( setting.getSettingName() ) ) {
        assertEquals( USER_SETTING_VALUE_3, setting.getSettingValue() );
      } else if ( GLOBAL_SETTING_NAME_3.equals( setting.getSettingName() ) ) {
        assertEquals( GLOBAL_SETTING_VALUE_3, setting.getSettingValue() );
      }
    }
  }

  @Test
  public void testGetUserSettingNoAuth() throws Exception {
    final String settingName = "settingName";
    final String defaultValue = "defaultValue";

    when( session.getAttribute( eq( "SPRING_SECURITY_CONTEXT" ) ) ).thenReturn( null );

    final IUserSetting userSetting = userSettingService.getUserSetting( settingName, defaultValue );

    assertEquals( settingName, userSetting.getSettingName() );
    assertEquals( defaultValue, userSetting.getSettingValue() );
  }

  @Test
  public void testGetUserSettingUnset() throws Exception {
    final String settingName = "settingName";
    final String defaultValue = "defaultValue";

    when( session.getAttribute( eq( "SPRING_SECURITY_CONTEXT" ) ) ).thenReturn( 1 );

    final IUserSetting userSetting = userSettingService.getUserSetting( settingName, defaultValue );
    assertEquals( settingName, userSetting.getSettingName() );
    assertEquals( defaultValue, userSetting.getSettingValue() );
  }

  @Test
  public void testGetUserSetting() throws Exception {
    final String settingName = USER_SETTING_NAME_3;
    final String defaultValue = "defaultValue";

    when( session.getAttribute( eq( "SPRING_SECURITY_CONTEXT" ) ) ).thenReturn( 1 );

    //try to get existing setting
    final IUserSetting userSetting = userSettingService.getUserSetting( settingName, defaultValue );
    assertEquals( settingName, userSetting.getSettingName() );
    assertEquals( USER_SETTING_VALUE_3, userSetting.getSettingValue() );
  }

  @Test
  public void testGetUserSettingOnlyGlobalExist() throws Exception {
    final String settingName = GLOBAL_SETTING_NAME_3;
    final String defaultValue = "defaultValue";

    when( session.getAttribute( eq( "SPRING_SECURITY_CONTEXT" ) ) ).thenReturn( 1 );

    final IUserSetting userSetting = userSettingService.getUserSetting( settingName, defaultValue );
    assertEquals( settingName, userSetting.getSettingName() );
    assertEquals( GLOBAL_SETTING_VALUE_3, userSetting.getSettingValue() );
  }

  @Test
  public void testSetUserSetting() throws Exception {
    final String settingName = "settingName";
    final String settingValue = "settingValue";

    assertEquals( 4, userSettings.size() );
    userSettingService.setUserSetting( settingName, settingValue );
    assertEquals( 5, userSettings.size() );

    final Serializable value = userSettings.get( UserSettingService.SETTING_PREFIX + settingName );
    assertEquals( settingValue, value );

    verify( repository ).setFileMetadata( eq( USER_FOLDER_ID ), anyMap() );
  }

  @Test
  public void testGetGlobalUserSettingUnset() throws Exception {
    final String settingName = "settings";
    final String defaultValue = "defaultValue";

    final IUserSetting globalUserSetting = userSettingService.getGlobalUserSetting( settingName, defaultValue );

    assertEquals( settingName, globalUserSetting.getSettingName() );
    assertEquals( defaultValue, globalUserSetting.getSettingValue() );
  }

  @Test
  public void testGetGlobalUserSetting() throws Exception {
    final String settingName = GLOBAL_SETTING_NAME_3;
    final String defaultValue = "defaultValue";

    final IUserSetting globalUserSetting = userSettingService.getGlobalUserSetting( settingName, defaultValue );

    assertEquals( settingName, globalUserSetting.getSettingName() );
    assertEquals( GLOBAL_SETTING_VALUE_3, globalUserSetting.getSettingValue() );
  }

  @Test
  public void testGetGlobalUserSettings() throws Exception {
    final List<IUserSetting> globalUserSettings = userSettingService.getGlobalUserSettings();

    assertNotNull( globalUserSettings );
    assertEquals( 2, globalUserSettings.size() );
    label : for ( IUserSetting setting : globalUserSettings ) {
      for ( String key : globalSettings.keySet() ) {
        if ( key.startsWith( UserSettingService.SETTING_PREFIX ) && key.substring( UserSettingService.SETTING_PREFIX.length() ).equals( setting.getSettingName() ) ) {
          assertEquals( globalSettings.get( key ), setting.getSettingValue() );
          continue label;
        }
      }
      fail( "wrong setting" );
    }
  }

  @Test
  public void testSetGlobalUserSettingNonAdmin() throws Exception {
    final String settingName = "settingName";
    final String settingValue = "settingValue";

    when( userSettingService.canAdminister() ).thenReturn( false );

    assertEquals( 4, globalSettings.size() );
    userSettingService.setGlobalUserSetting( settingName, settingValue );
    assertEquals( 4, globalSettings.size() );
  }

  @Test
  public void testSetGlobalUserSettingAdmin() throws Exception {
    final String settingName = "settingName";
    final String settingValue = "settingValue";

    when( userSettingService.canAdminister() ).thenReturn( true );

    assertEquals( 4, globalSettings.size() );
    userSettingService.setGlobalUserSetting( settingName, settingValue );
    assertEquals( 5, globalSettings.size() );

    verify( repository ).setFileMetadata( eq( TENANT_FOLDER_ID ), anyMap() );
  }

  private class UserSettingServiceForTesting extends UserSettingService {

    private Boolean canAdministerOverrideValue = null;

    public UserSettingServiceForTesting( IUnifiedRepository repository ) {
      super( repository );
    }

    public void setCanAdministerOverrideValue( Boolean canAdministerOverrideValue ) {
      this.canAdministerOverrideValue = canAdministerOverrideValue;
    }

    @Override
    public boolean canAdminister() {
      if ( canAdministerOverrideValue != null ) {
        return canAdministerOverrideValue; // autoboxing
      } else {
        return super.canAdminister();
      }
    }
  }
}
