package org.pentaho.platform.plugin.services.exporter.strategies;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.RoleExport;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

/**
 * @author Andrei Abramov
 */
public class LdapExportStrategyTest {

  IPentahoSession session;
  ExportManifest exportManifest;
  LdapExportStrategy ldapExportStrategy;


  @Before
  public void setUp() throws Exception {
    session = mock( IPentahoSession.class );
    exportManifest = spy( new ExportManifest() );
    ldapExportStrategy = new LdapExportStrategy();
    PentahoSessionHolder.setSession( session );
  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void exportUsersAndRoles() throws Exception {

    IUserRoleListService mockService = mock( IUserRoleListService.class );
    PentahoSystem.registerObject( mockService );

    IAnyUserSettingService userSettingService = mock( IAnyUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );

    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
    PentahoSystem.registerObject( roleBindingDao );

    String tenantPath = "path";
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( tenantPath );

    String role = "testRole";
    List<String> roleList = new ArrayList<String>();
    roleList.add( role );
    when( mockService.getAllRoles() ).thenReturn( roleList );

    String user = "testUser";
    List<String> userList = new ArrayList<String>();
    userList.add( user );
    when( mockService.getAllUsers( any( ITenant.class ) ) ).thenReturn( userList );

    Map<String, List<String>> map = new HashMap<String, List<String>>();
    List<String> permissions = new ArrayList<String>();
    permissions.add( "read" );
    map.put( "testRole", permissions );
    RoleBindingStruct struct = mock( RoleBindingStruct.class );
    struct.bindingMap = map;
    when( roleBindingDao.getRoleBindingStruct( anyString() ) ).thenReturn( struct );

    ArgumentCaptor<RoleExport> roleCaptor = ArgumentCaptor.forClass( RoleExport.class );
    ArgumentCaptor<UserExport> userCaptor = ArgumentCaptor.forClass( UserExport.class );
    ExportManifest manifest = mock( ExportManifest.class );

    List<IUserSetting> settings = new ArrayList<>();
    IUserSetting setting = mock( IUserSetting.class );
    settings.add( setting );
    when( userSettingService.getUserSettings( user ) ).thenReturn( settings );
    when( userSettingService.getGlobalUserSettings() ).thenReturn( settings );

    ldapExportStrategy.exportUsersAndRoles( manifest );

    verify( manifest ).addUserExport( userCaptor.capture() );
    verify( manifest ).addRoleExport( roleCaptor.capture() );
    verify( userSettingService ).getGlobalUserSettings();
    verify( manifest ).addGlobalUserSetting( any( ExportManifestUserSetting.class ) );
    assertEquals( settings.size(), userCaptor.getValue().getUserSettings().size() );

    UserExport userExport = userCaptor.getValue();
    assertEquals( "testUser", userExport.getUsername() );
    RoleExport roleExport = roleCaptor.getValue();
    assertEquals( "testRole", roleExport.getRolename() );
  }

}
