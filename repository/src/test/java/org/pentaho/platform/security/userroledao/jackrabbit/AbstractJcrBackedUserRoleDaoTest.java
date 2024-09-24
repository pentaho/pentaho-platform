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

package org.pentaho.platform.security.userroledao.jackrabbit;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategySessionFactory;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link org.pentaho.platform.security.userroledao.jackrabbit.AbstractJcrBackedUserRoleDao} Class is created
 * in order to have access to package-private methods.
 *
 * @author Yury_Bakhmutski
 */
@RunWith( MockitoJUnitRunner.class )
public class AbstractJcrBackedUserRoleDaoTest {

  private static final String REPO_CONFIG_FILE = "/jackrabbit/repository.xml";
  private static TransientRepository repository;
  private static Session adminSession;
  private static final String TEST_REPOSITORY_LOCATION = "test-jcr_";
  private static final String[] newRoles = { "Administrator" };
  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_USER_PASS = "admin";
  private static final String PENTAHO_TENANT = "/pentaho/tenant0";
  private static final String TEST_USER_NAME = "testUser";
  private static final String TEST_USER_DEC = "Test User";

  // Mocked objects
  private static ITenant tenantMock;
  private static ITenantedPrincipleNameResolver nameResolverMock;
  private static ITenantedPrincipleNameResolver roleResolverMock;
  private static AbstractJcrBackedUserRoleDao abstractJcrBackedUserRoleDaoMock;


  @BeforeClass
  public static void beforeAll() throws Exception {
    Path repositoryPath = Files.createTempDirectory( TEST_REPOSITORY_LOCATION );
    InputStream configStream = AbstractJcrBackedUserRoleDaoTest.class.getResourceAsStream( REPO_CONFIG_FILE );
    Path repositoryLocation = repositoryPath.toAbsolutePath();
    RepositoryConfig config = RepositoryConfig.create( configStream, repositoryLocation.toString() );
    repository = new TransientRepository( config );
    Credentials creds = new SimpleCredentials( ADMIN_USER,  ADMIN_USER_PASS.toCharArray() );
    adminSession = repository.login( creds );
    initMocks();
  }

  @AfterClass
  public static void destroyRepository() throws Exception {
    repository.shutdown();
    String repositoryLocation = repository.getHomeDir();
    FileUtils.deleteDirectory( new File( repositoryLocation ) );
    repository = null;
  }

  private static void initMocks() throws Exception {

    tenantMock = mock( ITenant.class );
    when( tenantMock.getId() ).thenReturn( PENTAHO_TENANT );

    nameResolverMock = mock( ITenantedPrincipleNameResolver.class );
    when( nameResolverMock.getPrincipleId( nullable( ITenant.class ), nullable( String.class ) ) ).thenReturn( TEST_USER_NAME );
    when( nameResolverMock.getTenant( nullable( String.class ) ) ).thenReturn( tenantMock );
    when( nameResolverMock.getPrincipleName( nullable( String.class ) ) ).thenReturn( TEST_USER_NAME );

    roleResolverMock = mock( ITenantedPrincipleNameResolver.class );
    when( roleResolverMock.getPrincipleId( nullable( ITenant.class ), nullable( String.class ) ) ).thenReturn( "Authenticated_" + PENTAHO_TENANT );

    UserCache userDetailsCache = new NullUserCache();

    abstractJcrBackedUserRoleDaoMock = mock( AbstractJcrBackedUserRoleDao.class );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock )
            .createUser( nullable( Session.class ), nullable( ITenant.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String[].class ) );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).deleteUser( nullable( Session.class ), nullable( IPentahoUser.class ) );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).setTenantedUserNameUtils( nullable( ITenantedPrincipleNameResolver.class ) );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).setTenantedRoleNameUtils( nullable( ITenantedPrincipleNameResolver.class ) );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).setUserDetailsCache( nullable( UserCache.class ) );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).getUser( nullable( Session.class ), nullable( ITenant.class ), nullable( String.class ) );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).convertToPentahoUser( nullable( User.class ) );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).initUserCache();
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).initUserDetailsCache();
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).getUserCache();
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).setUseJackrabbitUserCache( nullable( boolean.class ) );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).isUseJackrabbitUserCache();
    when( abstractJcrBackedUserRoleDaoMock.createUserHomeFolder( nullable( ITenant.class ), nullable( String.class ), nullable( Session.class ) ) ).thenReturn( null );
    when( abstractJcrBackedUserRoleDaoMock.getTenantedUserNameUtils() ).thenReturn( nameResolverMock );

    abstractJcrBackedUserRoleDaoMock.setTenantedUserNameUtils( nameResolverMock );
    abstractJcrBackedUserRoleDaoMock.setTenantedRoleNameUtils( roleResolverMock );
    abstractJcrBackedUserRoleDaoMock.setUserDetailsCache( userDetailsCache );
    abstractJcrBackedUserRoleDaoMock.initUserCache();
    abstractJcrBackedUserRoleDaoMock.initUserDetailsCache();
    abstractJcrBackedUserRoleDaoMock.setUseJackrabbitUserCache( true );
  }

  @Test
  public void testCreateUser() throws Exception {

    String testUser = TEST_USER_NAME + "_create";
    String password = "password";

    try ( MockedStatic<TenantUtils> tenantUtils = mockStatic( TenantUtils.class ) ) {
      tenantUtils.when( () -> TenantUtils.isAccessibleTenant( any() ) ).thenReturn( true );
      when( nameResolverMock.getPrincipleId( nullable( ITenant.class ), nullable( String.class ) ) ).thenReturn( testUser );
      when( nameResolverMock.getTenant( nullable( String.class ) ) ).thenReturn( tenantMock );
      when( nameResolverMock.getPrincipleName( nullable( String.class ) ) ).thenReturn( testUser );
      when( roleResolverMock.getPrincipleId( nullable( ITenant.class ), nullable( String.class ) ) ).thenReturn( "Authenticated_" + PENTAHO_TENANT );

      //test user creation
      IPentahoUser newUser = abstractJcrBackedUserRoleDaoMock
        .createUser( adminSession, tenantMock, TEST_USER_NAME, password, TEST_USER_DEC, newRoles );
      IPentahoUser existingUser = abstractJcrBackedUserRoleDaoMock.getUser( adminSession, tenantMock, TEST_USER_NAME );
      assertThat( existingUser, is( newUser ) );
    }
  }

  @Test
  public void testDeleteUser() throws Exception {

    String testUser = TEST_USER_NAME + "_delete";
    String password1 = "password";
    String password2 = "new_password";
    try ( MockedStatic<TenantUtils> tenantUtils = mockStatic( TenantUtils.class ) ) {
      tenantUtils.when( () -> TenantUtils.isAccessibleTenant( any() ) ).thenReturn( true );
      when( nameResolverMock.getPrincipleId( nullable( ITenant.class ), nullable( String.class ) ) ).thenReturn( testUser );
      when( nameResolverMock.getTenant( nullable( String.class ) ) ).thenReturn( tenantMock );
      when( nameResolverMock.getPrincipleName( nullable( String.class ) ) ).thenReturn( testUser );
      when( roleResolverMock.getPrincipleId( nullable( ITenant.class ), nullable( String.class ) ) ).thenReturn( "Authenticated_" + PENTAHO_TENANT );

      //create user
      IPentahoUser newUser = abstractJcrBackedUserRoleDaoMock
        .createUser( adminSession, tenantMock, testUser, password1, TEST_USER_DEC, newRoles );
      IPentahoUser existingUser = abstractJcrBackedUserRoleDaoMock.getUser( adminSession, tenantMock, testUser );
      assertThat( existingUser, is( newUser ) );

      //test user deletion
      when( abstractJcrBackedUserRoleDaoMock.canDeleteUser( nullable( Session.class ), nullable( IPentahoUser.class ) ) ).thenReturn( true );
      abstractJcrBackedUserRoleDaoMock.deleteUser( adminSession, newUser );
      assertNull( abstractJcrBackedUserRoleDaoMock.getUser( adminSession, tenantMock, testUser ) );

      // [BACKLOG-33914] Test that user recreation creates user with new password
      IPentahoUser newUser2 = abstractJcrBackedUserRoleDaoMock
        .createUser( adminSession, tenantMock, testUser, password2, TEST_USER_DEC, newRoles );
      assertNotEquals( newUser.getPassword(), newUser2.getPassword() );
    }
  }

  @Test
  public void testConvertToPentahoUserEnableCache() throws RepositoryException {
    AbstractJcrBackedUserRoleDao abstractJcrBackedUserRoleDaoMock = mock( AbstractJcrBackedUserRoleDao.class );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).convertToPentahoUser( nullable( User.class ) );

    ITenantedPrincipleNameResolver resolverMock = mock( ITenantedPrincipleNameResolver.class );
    when( abstractJcrBackedUserRoleDaoMock.getTenantedUserNameUtils() ).thenReturn( resolverMock );

    when( abstractJcrBackedUserRoleDaoMock.isUseJackrabbitUserCache() ).thenReturn( true );

    //Cache mocking
    LRUMap cacheMock = mock( LRUMap.class );
    when( abstractJcrBackedUserRoleDaoMock.getUserCache() ).thenReturn( cacheMock );

    User userMock = mock( User.class );
    abstractJcrBackedUserRoleDaoMock.convertToPentahoUser( userMock );

    verify( cacheMock ).put( any(), any() );
  }

  @Test
  public void testConvertToPentahoUserDisableCache() throws RepositoryException {
    AbstractJcrBackedUserRoleDao abstractJcrBackedUserRoleDaoMock = mock( AbstractJcrBackedUserRoleDao.class );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).convertToPentahoUser( nullable( User.class ) );

    ITenantedPrincipleNameResolver resolverMock = mock( ITenantedPrincipleNameResolver.class );
    when( abstractJcrBackedUserRoleDaoMock.getTenantedUserNameUtils() ).thenReturn( resolverMock );

    //Cache mocking
    LRUMap cacheMock = mock( LRUMap.class );
    when( abstractJcrBackedUserRoleDaoMock.getUserCache() ).thenReturn( cacheMock );

    User userMock = mock( User.class );
    abstractJcrBackedUserRoleDaoMock.convertToPentahoUser( userMock );

    verify( cacheMock, never() ).put( any(), anyString() );
  }


  @Test
  public void getSessionImplTest() {

    CredentialsStrategySessionFactory factory =
        new CredentialsStrategySessionFactory( mock( Repository.class ), mock( CredentialsStrategy.class ) );
    Session sessionProxy = factory.createSessionProxy( mock( SessionImpl.class ) );
    SessionImpl sessionImpl = AbstractJcrBackedUserRoleDao.getSessionImpl( sessionProxy );
    assertThat( sessionImpl, is( notNullValue() ) );

  }
}
