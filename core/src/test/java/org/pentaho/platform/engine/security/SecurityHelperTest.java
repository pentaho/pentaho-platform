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

package org.pentaho.platform.engine.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.boot.PentahoSystemBoot;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;

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
    Collection<? extends GrantedAuthority> autorities = authentication.getAuthorities();

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
      if ( authElem != null && ANONIMOUS_ROLE.equals( authElem.getAuthority() ) ) {
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
      nullable( IPentahoSession.class ) ) ).thenReturn( ADMIN_USER_NAME );
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

  @Test
  /**
   * Verification for BISERVER-12365 where a Threads are sharing a SecurityContext and making concurrent calls to
   * runAsSytem() and runAsUser()
   */
  public void testWithSharedSecurityContext() throws InterruptedException {


    IUserRoleListService userRoleListService = getUserRoleListServiceMock( "admin", new String[]{"authenticated"} );

    when( userRoleListService.getRolesForUser( any(), eq( "suzy" ) ) )
      .thenReturn( Collections.singletonList( "authenticated" ) );

    PentahoSystem.registerObject( userRoleListService );
    PentahoSystem.registerReference(
      new SingletonPentahoObjectReference.Builder<>( String.class ).object( "admin" )
            .attributes( Collections.singletonMap( "id", "singleTenantAdminUserName" ) ).build() );

    SecurityContextHolder.setStrategyName( PentahoSecurityContextHolderStrategy.class.getName() );
    UsernamePasswordAuthenticationToken token =
      new UsernamePasswordAuthenticationToken( "suzy", "password" );

    final SecurityContext context = new SecurityContextImpl();

    SecurityContextHolder.setContext( context );
    SecurityContextHolder.getContext().setAuthentication( token );

    final CountDownLatch startSignal = new CountDownLatch( 1 );
    final CountDownLatch doneSignal = new CountDownLatch( 1 );

    final Thread t2 = new Thread( () -> {
      try {
        SecurityContextHolder.setContext( context );
        SecurityHelper.getInstance().runAsSystem( (Callable<Void>) () -> {
          System.out.println( "Starting Thread 2" );
          startSignal.await();
          // waiting for t1 to finish
          doneSignal.await();
          System.out.println( "Finishing Thread 2" );
          return null;
        } );
      } catch ( Exception e ) {
        e.printStackTrace();
        fail( e.getMessage() );
      }
    } );

    final Thread t1 = new Thread( () -> {
      try {
        SecurityContextHolder.setContext( context );
        SecurityHelper.getInstance().runAsSystem( (Callable<Void>) () -> {
          System.out.println( "Starting Thread 1" );
          startSignal.await();
          System.out.println( "Finishing Thread 1" );
          return null;
        } );
      } catch ( Exception e ) {
        e.printStackTrace();
        fail( e.getMessage() );
      }
    } );


    t1.start();
    t2.start();
    startSignal.countDown();  // let all threads proceed
    t1.join();                // waits for t1 to die
    doneSignal.countDown();   // t2 is waiting for t1 to finish, let t2 proceed
    t2.join();                // waits for t2 to die

    assertSame( token.getPrincipal(), SecurityContextHolder.getContext().getAuthentication().getPrincipal() );
  }

  @Test
  /**
   * Authenticate as Suzy, make a runAsSystem() call with an embedded runAsUser(), verify that Authentication is
   * restored successfully.
   */
  public void testNestedCalls() throws Exception {

    IUserRoleListService userRoleListService = getUserRoleListServiceMock( "admin", new String[]{"authenticated"} );

    when( userRoleListService.getRolesForUser( any(), eq( "suzy" ) ) ).thenReturn( Collections.singletonList( "authenticated" ) );

    PentahoSystem.registerObject( userRoleListService );
    PentahoSystem.registerReference(
      new SingletonPentahoObjectReference.Builder<>( String.class ).object( "admin" )
            .attributes( Collections.singletonMap( "id", "singleTenantAdminUserName" ) ).build() );

    SecurityContextHolder.setStrategyName( PentahoSecurityContextHolderStrategy.class.getName() );
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken( "suzy", "password" );
    SecurityContextHolder.getContext().setAuthentication( token );
    SecurityHelper.getInstance().runAsSystem( (Callable<Void>) () -> {

      try {
        SecurityHelper.getInstance().runAsUser( "suzy", (Callable<Void>) () -> {

          assertEquals(
              ( (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ).getUsername(),
              "suzy" );
          throw new NullPointerException();
        } );
      } catch ( Exception e ) {
        /* No-op */
      }

      assertEquals( ( (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ).getUsername(),
          "admin" );
      return null;
    } );
    assertSame( SecurityContextHolder.getContext().getAuthentication(), token );
  }

  @Test
  public void isPentahoAdministratorValidPolicyTest() {
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( emptySecurityHelper.getAuthorizationPolicy() ).thenReturn( policy );
    when( policy.isAllowed( anyString() ) ).thenReturn( true );
    assertTrue( emptySecurityHelper.isPentahoAdministrator( any() ) );
  }

  @Test
  public void isPentahoAdministratorInvalidPolicyTest() {
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( emptySecurityHelper.getAuthorizationPolicy() ).thenReturn( policy );
    when( policy.isAllowed( anyString() ) ).thenReturn( false );
    assertFalse( emptySecurityHelper.isPentahoAdministrator( any() ) );
  }

  @Test
  public void isPentahoAdministratorNullPolicyTest() {
    when( emptySecurityHelper.getAuthorizationPolicy() ).thenReturn( null );
    assertFalse( emptySecurityHelper.isPentahoAdministrator( any() ) );
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
    List<String> noRoles = new ArrayList<>();
    List<String> allRoles = new ArrayList<>( Arrays.asList( roles ) );

    when( mockUserRoleListService.getRolesForUser( any(), eq( userName ) ) ).thenReturn( allRoles );
    when( mockUserRoleListService.getRolesForUser( any( ITenant.class ), AdditionalMatchers.not( eq( userName ) ) ) )
        .thenReturn( noRoles );

    return mockUserRoleListService;
  }
}
