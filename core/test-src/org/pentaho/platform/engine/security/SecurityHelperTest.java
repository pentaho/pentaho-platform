/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.Matchers;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.boot.PentahoSystemBoot;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;

public class SecurityHelperTest {

  private static final String PENTAHO_OBJECT_FACTORY_MOCK_NAME = "Mock of IPentahoObjectFactory";
  private static final String SINGLE_TENANT_ADMIN_USER_NAME = "singleTenantAdminUserName";
  private static final String ADMIN_USER_NAME = "super_admin";
  private static final String CALLABLE_RETURNED_VALUE_OK = "ok";
  private static final String DEF_USERNAME = "myuser";
  private static final String[] ALL_ROLES_ARRAY = { "role1", "role2" };
  private static final String[] ADMIN_ROLES_ARRAY = { "adm_role1", "adm_role2" };
  private static final String ANONIMOUS_USER;
  private static final String ANONIMOUS_ROLE;
  private static ISystemSettings oldSystemSettingsService;

  private SecurityHelper emptySecurityHelper;

  static {
    setSystemSettingsService( null );
    ANONIMOUS_USER = PentahoSystem.getSystemSetting( "anonymous-authentication/anonymous-user", "anonymousUser" );
    ANONIMOUS_ROLE = PentahoSystem.getSystemSetting( "anonymous-authentication/anonymous-role", "Anonymous" );
    rollbackSystemSettingsService();
  }

  @Before
  public void init() {
    setSystemSettingsService( null );

    PentahoSystemBoot boot = new PentahoSystemBoot();

    boot.setFilePath( "test-src/solution" );
    emptySecurityHelper = spy( new SecurityHelper() );
  }

  @After
  public void destroy() {
    rollbackSystemSettingsService();
  }


  @Test
  public void createAuthentificationTest() {
    Authentication authentication = getAuthorizedSecurityHelper().createAuthentication( DEF_USERNAME );
    GrantedAuthority[] autorities = authentication.getAuthorities();

    // check for the all inner roles from ALL_ROLES_ARRAY that they are present in authentication authorities
    for ( String sourceRole : ALL_ROLES_ARRAY ) {

      boolean roleWasFound = false;
      for ( GrantedAuthority authRole : autorities ) {
        if ( sourceRole.equals( authRole.getAuthority() ) ) {
          roleWasFound = true;
          break;
        }
      }

      if ( !roleWasFound ) {
        fail( "not whole of required roles are present in created authentication authorities" );
        return;
      }
    }
  }

  @Test
  public void createAnonimousAuthentificationTest() {
    Authentication auth = getAuthorizedSecurityHelper().createAuthentication( ANONIMOUS_USER );

    boolean roleWasFound = false;
    for ( GrantedAuthority authElem : auth.getAuthorities() ) {
      if ( authElem != null && authElem.equals( ANONIMOUS_ROLE ) ) {
        roleWasFound = true;
        break;
      }
    }

    assertTrue( "not granted access for anonimous user", roleWasFound );
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void runAsSystemTest() throws Exception {
    // creating environment
    PentahoSystemBoot boot = new PentahoSystemBoot();
    boot.setFilePath( "test-src/solution" );
    IPentahoObjectFactory pentahoObjectFactory = mock( IPentahoObjectFactory.class, PENTAHO_OBJECT_FACTORY_MOCK_NAME );
    when( pentahoObjectFactory.objectDefined( eq( SINGLE_TENANT_ADMIN_USER_NAME ) ) ).thenReturn( true );
    when( pentahoObjectFactory.get( eq( String.class ), eq( SINGLE_TENANT_ADMIN_USER_NAME ),
        Matchers.<IPentahoSession>any() ) ).thenReturn( ADMIN_USER_NAME );
    when( pentahoObjectFactory.getName() ).thenReturn( PENTAHO_OBJECT_FACTORY_MOCK_NAME );
    boot.setFactory( pentahoObjectFactory );

    IUserRoleListService mockUserRoleListService = getUserRoleListServiceMock( ADMIN_USER_NAME, ADMIN_ROLES_ARRAY );
    doReturn( mockUserRoleListService ).when( emptySecurityHelper ).getUserRoleListService();

    // test for call
    Callable<String> callable = (Callable<String>) mock( Callable.class );
    when( callable.call() ).thenReturn( CALLABLE_RETURNED_VALUE_OK );
    String runningResult = emptySecurityHelper.runAsSystem( callable );

    assertEquals( CALLABLE_RETURNED_VALUE_OK, runningResult );
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void runAsAnonymousTest() throws Exception {
    Callable<String> callable = (Callable<String>) mock( Callable.class );
    when( callable.call() ).thenReturn( CALLABLE_RETURNED_VALUE_OK );
    String runningResult = emptySecurityHelper.runAsAnonymous( callable );

    assertEquals( CALLABLE_RETURNED_VALUE_OK, runningResult );
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void runAsUserTest() throws Exception {
    Callable<String> callable = (Callable<String>) mock( Callable.class );
    when( callable.call() ).thenReturn( CALLABLE_RETURNED_VALUE_OK );
    String runningResult = getAuthorizedSecurityHelper().runAsUser( DEF_USERNAME, callable );

    assertEquals( CALLABLE_RETURNED_VALUE_OK, runningResult );
  }

  private static void setSystemSettingsService( ISystemSettings service ) {
    oldSystemSettingsService = PentahoSystem.getSystemSettings();
    PentahoSystem.setSystemSettingsService( service );
  }

  private static void rollbackSystemSettingsService() {
    PentahoSystem.setSystemSettingsService( oldSystemSettingsService );
  }

  private SecurityHelper getAuthorizedSecurityHelper() {
    SecurityHelper authorizedSecurityHelper = spy( new SecurityHelper() );
    IUserRoleListService userRoleListServiceMock = getUserRoleListServiceMock( DEF_USERNAME, ALL_ROLES_ARRAY );
    doReturn( userRoleListServiceMock ).when( authorizedSecurityHelper ).getUserRoleListService();
    return authorizedSecurityHelper;
  }

  private IUserRoleListService getUserRoleListServiceMock( String userName, String[] roles ) {
    IUserRoleListService mockUserRoleListService = mock( IUserRoleListService.class );
    List<String> noRoles = new ArrayList<String>();
    List<String> allRoles = new ArrayList<String>( Arrays.asList( roles ) );

    when( mockUserRoleListService.getRolesForUser( Matchers.<ITenant>any(), eq( userName ) ) ).thenReturn( allRoles );
    when( mockUserRoleListService.getRolesForUser( Matchers.<ITenant>any(), AdditionalMatchers.not( eq( userName ) ) ) )
        .thenReturn( noRoles );

    return mockUserRoleListService;
  }
}
