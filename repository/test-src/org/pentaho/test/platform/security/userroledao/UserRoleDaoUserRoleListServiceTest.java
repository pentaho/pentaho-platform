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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.security.userroledao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.security.AccessControlException;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.api.security.authorization.PrivilegeManager;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxyFactory;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserDetailsService;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserRoleListService;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for {@link UserRoleDaoUserRoleListService}.
 * 
 * @author mlowery
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" } )
@SuppressWarnings( "nls" )
public class UserRoleDaoUserRoleListServiceTest implements ApplicationContextAware {
  public static final int DEFAULT_ROLE_COUNT = 4;
  public static final int DEFAULT_USER_COUNT = 1; // admin
  public static final String MAIN_TENANT_1 = "maintenant1";
  public static final String MAIN_TENANT_2 = "maintenant2";

  private ITenant mainTenant_1;
  private ITenant mainTenant_2;  
  
  public static final String PASSWORD_1 = "password1"; //$NON-NLS-1$
  public static final String PASSWORD_2 = "password2"; //$NON-NLS-1$
  public static final String PASSWORD_3 = "password3"; //$NON-NLS-1$
  public static final String PASSWORD_4 = "password4"; //$NON-NLS-1$
  public static final String PASSWORD_5 = "password5"; //$NON-NLS-1$
  public static final String PASSWORD_6 = "password6"; //$NON-NLS-1$
  public static final String PASSWORD_7 = "password7"; //$NON-NLS-1$
  public static final String PASSWORD_8 = "password8"; //$NON-NLS-1$
  public static final String PASSWORD_9 = "password9"; //$NON-NLS-1$
  public static final String PASSWORD_10 = "password10"; //$NON-NLS-1$
  public static final String PASSWORD_11 = "password11"; //$NON-NLS-1$
  public static final String PASSWORD_12 = "password12"; //$NON-NLS-1$
  public static final String PASSWORD_13 = "password13"; //$NON-NLS-1$
  public static final String PASSWORD_14 = "password14"; //$NON-NLS-1$

  public static final String USER_1 = "admin"; //$NON-NLS-1$
  public static final String USER_2 = "jim"; //$NON-NLS-1$
  public static final String USER_3 = "sally"; //$NON-NLS-1$
  public static final String USER_4 = "suzy"; //$NON-NLS-1$
  public static final String USER_5 = "nancy"; //$NON-NLS-1$
  public static final String USER_6 = "john"; //$NON-NLS-1$
  public static final String USER_7 = "jane"; //$NON-NLS-1$
  public static final String USER_8 = "jerry"; //$NON-NLS-1$
  public static final String USER_9 = "tom"; //$NON-NLS-1$
  public static final String USER_10 = "johny"; //$NON-NLS-1$
  public static final String USER_11 = "mary"; //$NON-NLS-1$
  public static final String USER_12 = "jill"; //$NON-NLS-1$
  public static final String USER_13 = "jack"; //$NON-NLS-1$
  public static final String USER_14 = "jeremy"; //$NON-NLS-1$

  public static final String UNKNOWN_USER = "unknownUser"; //$NON-NLS-1$

  public static final ITenant UNKNOWN_TENANT = new Tenant( "unknownTenant", true ); //$NON-NLS-1$

  public static final String ROLE_1 = "SalesMgr"; //$NON-NLS-1$
  public static final String ROLE_2 = "IT"; //$NON-NLS-1$

  public static final String ROLE_3 = "Sales"; //$NON-NLS-1$
  public static final String ROLE_4 = "Developer"; //$NON-NLS-1$
  public static final String ROLE_5 = "CEO"; //$NON-NLS-1$
  public static final String ROLE_6 = "Finance"; //$NON-NLS-1$
  public static final String ROLE_7 = "Marketing"; //$NON-NLS-1$
  public static final String ROLE_8 = "RegionalMgr"; //$NON-NLS-1$
  public static final String ROLE_9 = "CTO"; //$NON-NLS-1$
  public static final String ROLE_10 = "CFO"; //$NON-NLS-1$
  public static final String ROLE_11 = "CMO"; //$NON-NLS-1$
  public static final String ROLE_12 = "CIO"; //$NON-NLS-1$
  public static final String ROLE_13 = "COO"; //$NON-NLS-1$
  public static final String ROLE_14 = "CSO"; //$NON-NLS-1$

  public static final String UNKNOWN_ROLE = "unknownRole"; //$NON-NLS-1$

  public static final String USER_DESCRIPTION_1 = "User Description 1"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_2 = "User Description 2"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_3 = "User Description 3"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_4 = "User Description 4"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_5 = "User Description 5"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_6 = "User Description 6"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_7 = "User Description 7"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_8 = "User Description 8"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_9 = "User Description 9"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_10 = "User Description 10"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_11 = "User Description 11"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_12 = "User Description 12"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_13 = "User Description 13"; //$NON-NLS-1$
  public static final String USER_DESCRIPTION_14 = "User Description 14"; //$NON-NLS-1$

  public static final String ROLE_DESCRIPTION_1 = "Role Description 1"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_2 = "Role Description 2"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_3 = "Role Description 3"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_4 = "Role Description 4"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_5 = "Role Description 5"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_6 = "Role Description 6"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_7 = "Role Description 7"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_8 = "Role Description 8"; //$NON-NLS-1$

  public static final String ROLE_DESCRIPTION_9 = "Role Description 9"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_10 = "Role Description 10"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_11 = "Role Description 11"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_12 = "Role Description 12"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_13 = "Role Description 13"; //$NON-NLS-1$
  public static final String ROLE_DESCRIPTION_14 = "Role Description 14"; //$NON-NLS-1$

  NameFactory NF = NameFactoryImpl.getInstance();
  Name P_PRINCIPAL_NAME = NF.create( Name.NS_REP_URI, "principalName" ); //$NON-NLS-1$
  private boolean startupCalled;
  String pPrincipalName;
  IUserRoleDao userRoleDao;
  private ITenantManager tenantManager;
  private String repositoryAdminUsername;
  private String tenantAdminAuthorityName;
  private String tenantAuthenticatedAuthorityName;
  private String sysAdminAuthorityName;
  private String sysAdminUserName;
  private JcrTemplate testJcrTemplate;
  private IBackingRepositoryLifecycleManager manager;
  private IAuthorizationPolicy authorizationPolicy;
  private MicroPlatform mp;
  private IRepositoryFileDao repositoryFileDao;
  private ITenant systemTenant = null;
  private ITenantedPrincipleNameResolver tenantedRoleNameUtils;
  private ITenantedPrincipleNameResolver tenantedUserNameUtils;
  private IRoleAuthorizationPolicyRoleBindingDao roleAuthorizationPolicyRoleBindingDao;
  @BeforeClass
  public static void setUpClass() throws Exception {
    // folder cannot be deleted at teardown shutdown hooks have not yet necessarily completed
    // parent folder must match jcrRepository.homeDir bean property in repository-test-override.spring.xml
    FileUtils.deleteDirectory( new File( "/tmp/jackrabbit-test-TRUNK" ) );
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL );
  }

  @Before
  public void setUp() throws Exception {
    mp = new MicroPlatform();
    // used by DefaultPentahoJackrabbitAccessControlHelper
    mp.defineInstance( "tenantedUserNameUtils", tenantedUserNameUtils );
    mp.defineInstance( "tenantedRoleNameUtils", tenantedRoleNameUtils );
    mp.defineInstance( IAuthorizationPolicy.class, authorizationPolicy );
    mp.defineInstance( ITenantManager.class, tenantManager );
    mp.define( ITenant.class, Tenant.class );
    mp.defineInstance( "roleAuthorizationPolicyRoleBindingDaoTarget", roleAuthorizationPolicyRoleBindingDao );
    mp.defineInstance( "repositoryAdminUsername", repositoryAdminUsername );
    mp.defineInstance( "RepositoryFileProxyFactory", new RepositoryFileProxyFactory(testJcrTemplate, repositoryFileDao) );
    
    // Start the micro-platform
    mp.start();
    loginAsRepositoryAdmin();
    setAclManagement();
    logout();
    startupCalled = true;
  }

  private void setAclManagement() {
    testJcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( Session session ) throws IOException, RepositoryException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        Workspace workspace = session.getWorkspace();
        PrivilegeManager privilegeManager = ( (JackrabbitWorkspace) workspace ).getPrivilegeManager();
        try {
          privilegeManager.getPrivilege( pentahoJcrConstants.getPHO_ACLMANAGEMENT_PRIVILEGE() );
        } catch ( AccessControlException ace ) {
          privilegeManager.registerPrivilege( pentahoJcrConstants.getPHO_ACLMANAGEMENT_PRIVILEGE(), false,
              new String[0] );
        }
        session.save();
        return null;
      }
    } );
  }

  private void cleanupTenant( final ITenant tenant ) {
    if ( tenant == null ) {
      return;
    }
    loginAsRepositoryAdmin();
    for ( IPentahoRole role : userRoleDao.getRoles( tenant ) ) {
    	userRoleDao.deleteRole( role );
    }
    for ( IPentahoUser user : userRoleDao.getUsers( tenant ) ) {
    	userRoleDao.deleteUser( user );
    }
    if ( tenant != null ) {
      tenantManager.deleteTenant( tenant );
    }
  }  
  
  /*
   * private void deleteUserRoleAndTenant(ITenant parentTenant, List<ITenant> tenants) { try { if(tenants != null
   * && tenants.size() > 0) { for(ITenant tenant: tenants) { login("admin", tenant, true); for(IPentahoRole
   * role:userRoleDao.getRoles()) { userRoleDao.deleteRole(role); } for(IPentahoUser user:userRoleDao.getUsers()) {
   * userRoleDao.deleteUser(user); } deleteUserRoleAndTenant(tenant, tenantManager.getChildTenants(tenant));
   * logout(); } } else { tenantManager.deleteTenant(parentTenant); } } catch (Throwable e) { // TODO
   * Auto-generated catch block e.printStackTrace(); } //$NON-NLS-1$ //$NON-NLS-2$ }
   */
  @After
  public void tearDown() throws Exception {
    cleanupTenant( mainTenant_2 );
    cleanupTenant( mainTenant_1 );
    cleanupTenant( systemTenant );  	  
	  
    // Deleting all user and roles and tenant
    // null out fields to get back memory
    authorizationPolicy = null;
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
    logout();
    repositoryAdminUsername = null;
    sysAdminAuthorityName = null;
    sysAdminUserName = null;
    tenantAdminAuthorityName = null;
    tenantAuthenticatedAuthorityName = null;
    authorizationPolicy = null;
    testJcrTemplate = null;
    if ( startupCalled ) {
      manager.shutdown();
    }

    // null out fields to get back memory
    tenantManager = null;

  }

  protected void loginAsRepositoryAdmin() {
    StandaloneSession pentahoSession = new StandaloneSession( repositoryAdminUsername );
    pentahoSession.setAuthenticated( repositoryAdminUsername );
    final GrantedAuthority[] repositoryAdminAuthorities =
        new GrantedAuthority[] { new GrantedAuthorityImpl( sysAdminAuthorityName ) };
    final String password = "ignored";
    UserDetails repositoryAdminUserDetails =
        new User( repositoryAdminUsername, password, true, true, true, true, repositoryAdminAuthorities );
    Authentication repositoryAdminAuthentication =
        new UsernamePasswordAuthenticationToken( repositoryAdminUserDetails, password, repositoryAdminAuthorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( repositoryAdminAuthentication );
  }

  protected void logout() {
    PentahoSessionHolder.removeSession();
    SecurityContextHolder.getContext().setAuthentication( null );
  }

  protected void login( final String username, final ITenant tenant, String[] roles ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( tenant.getId(), username );
    PentahoSessionHolder.setSession( pentahoSession );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();

    for ( String roleName : roles ) {
      authList.add( new GrantedAuthorityImpl( roleName ) );
    }
    GrantedAuthority[] authorities = authList.toArray( new GrantedAuthority[0] );
    UserDetails userDetails = new User( username, password, true, true, true, true, authorities );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
  }

  @Override
  public void setApplicationContext( final ApplicationContext applicationContext ) throws BeansException {
    manager = (IBackingRepositoryLifecycleManager) applicationContext.getBean( "backingRepositoryLifecycleManager" );
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean( "jcrSessionFactory" );
    testJcrTemplate = new JcrTemplate( jcrSessionFactory );
    testJcrTemplate.setAllowCreate( true );
    testJcrTemplate.setExposeNativeSession( true );
    repositoryAdminUsername = (String) applicationContext.getBean( "repositoryAdminUsername" );
    tenantAuthenticatedAuthorityName = (String) applicationContext.getBean( "singleTenantAuthenticatedAuthorityName" );
    tenantAdminAuthorityName = (String) applicationContext.getBean( "singleTenantAdminAuthorityName" );
    sysAdminAuthorityName = (String) applicationContext.getBean( "superAdminAuthorityName" );
    sysAdminUserName = (String) applicationContext.getBean( "superAdminUserName" );
    authorizationPolicy = (IAuthorizationPolicy) applicationContext.getBean( "authorizationPolicy" );
    tenantManager = (ITenantManager) applicationContext.getBean( "tenantMgrTxn" );
    repositoryFileDao = (IRepositoryFileDao) applicationContext.getBean( "repositoryFileDao" );
    userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDaoTxn" );
    TestPrincipalProvider.userRoleDao = userRoleDao;
    tenantedUserNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean( "tenantedUserNameUtils" );
    tenantedRoleNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean( "tenantedRoleNameUtils" );
    roleAuthorizationPolicyRoleBindingDao =
        (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
            .getBean( "roleAuthorizationPolicyRoleBindingDaoTarget" );
  }

  @Test
  public void testGetAllAuthorities() {
    loginAsRepositoryAdmin();
    systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { tenantAdminAuthorityName } );
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminAuthorityName,
      tenantAuthenticatedAuthorityName } );
    mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { tenantAdminAuthorityName } );
    mainTenant_2 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_2, tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( mainTenant_2, "admin", "password", "", new String[] { tenantAdminAuthorityName } );

    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.createRole( mainTenant_1, ROLE_1, ROLE_DESCRIPTION_1, null );
    userRoleDao.createRole( mainTenant_1, ROLE_2, ROLE_DESCRIPTION_2, null );
    userRoleDao.createRole( mainTenant_1, ROLE_3, ROLE_DESCRIPTION_3, null );
    logout();
    login( "admin", mainTenant_2, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.createRole( mainTenant_2, ROLE_4, ROLE_DESCRIPTION_4, null );
    userRoleDao.createRole( mainTenant_2, ROLE_5, ROLE_DESCRIPTION_5, null );
    userRoleDao.createRole( mainTenant_2, ROLE_6, ROLE_DESCRIPTION_6, null );
    userRoleDao.createRole( mainTenant_2, ROLE_7, ROLE_DESCRIPTION_7, null );

    List<String> systemRoles = Arrays.asList( new String[] { "Admin" } );
    List<String> extraRoles = Arrays.asList( new String[] { "Authenticated", "Anonymous" } );
    String adminRole = "Admin";

    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    UserRoleDaoUserRoleListService service =
        new UserRoleDaoUserRoleListService( userRoleDao, userDetailsService, tenantedUserNameUtils, systemRoles,
            extraRoles, adminRole );
    userDetailsService.setUserRoleDao( userRoleDao );
    logout();
    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );

    List<String> allRolesForDefaultTenant = service.getAllRoles();
    List<String> allRolesForTenant = service.getAllRoles( mainTenant_2 );
    System.out.println("allRolesForDefaultTenant.size() ==" + allRolesForDefaultTenant.size());
    System.out.println("allRolesForTenant.size() ==" + allRolesForTenant.size());   

    
    assertTrue( allRolesForDefaultTenant.size() == 2 + DEFAULT_ROLE_COUNT );
    assertEquals( 3 + DEFAULT_ROLE_COUNT, allRolesForTenant.size() );

    logout();
    login( "admin", mainTenant_2, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    allRolesForDefaultTenant = service.getAllRoles();
    allRolesForTenant = service.getAllRoles( mainTenant_1 );
    assertTrue( allRolesForDefaultTenant.size() == 3 + DEFAULT_ROLE_COUNT );
    assertEquals( 2 + DEFAULT_ROLE_COUNT, allRolesForTenant.size() );

    allRolesForTenant = service.getAllRoles( mainTenant_2 );
    assertEquals( 3 + DEFAULT_ROLE_COUNT, allRolesForTenant.size() );

    allRolesForTenant = service.getAllRoles( mainTenant_1 );
    assertEquals( 2 + DEFAULT_ROLE_COUNT, allRolesForTenant.size() );

    logout();
    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );

    allRolesForTenant = service.getAllRoles( mainTenant_1 );
    assertEquals( 2 + DEFAULT_ROLE_COUNT, allRolesForTenant.size() );

    allRolesForTenant = service.getAllRoles( mainTenant_2 );
    assertEquals( 3 + DEFAULT_ROLE_COUNT, allRolesForTenant.size() );

  }

  @Test
  public void testGetAllUsernames() {
    loginAsRepositoryAdmin();
    systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { tenantAdminAuthorityName } );
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminAuthorityName,
      tenantAuthenticatedAuthorityName } );
    mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { tenantAdminAuthorityName } );
    mainTenant_2 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_2, tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( mainTenant_2, "admin", "password", "", new String[] { tenantAdminAuthorityName } );

    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.createUser( mainTenant_1, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null );
    userRoleDao.createUser( mainTenant_1, USER_3, PASSWORD_3, USER_DESCRIPTION_3, null );
    userRoleDao.createUser( null, tenantedUserNameUtils.getPrincipleId( mainTenant_1, USER_4 ), PASSWORD_4,
            USER_DESCRIPTION_4, null );
    userRoleDao.createUser( null, USER_5, PASSWORD_5, USER_DESCRIPTION_5, null );
    userRoleDao.createUser( null, tenantedUserNameUtils.getPrincipleId( mainTenant_1, USER_6 ), PASSWORD_6,
            USER_DESCRIPTION_6, null );
    logout();
    login( "admin", mainTenant_2, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.createUser( mainTenant_2, USER_7, PASSWORD_7, USER_DESCRIPTION_7, null );
    userRoleDao.createUser( null, USER_8, PASSWORD_8, USER_DESCRIPTION_8, null );
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );

    List<String> systemRoles = Arrays.asList( new String[] { "Admin" } );
    List<String> extraRoles = Arrays.asList( new String[] { "Authenticated", "Anonymous" } );
    String adminRole = "Admin";

    UserRoleDaoUserRoleListService service =
        new UserRoleDaoUserRoleListService( userRoleDao, userDetailsService, tenantedUserNameUtils, systemRoles,
            extraRoles, adminRole );
    service.setUserRoleDao( userRoleDao );
    service.setUserDetailsService( userDetailsService );

    logout();
    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    List<String> allUserForDefaultTenant = service.getAllUsers();
    List<String> allUserForTenant = service.getAllUsers( mainTenant_2 );

    assertEquals( 5 + DEFAULT_USER_COUNT, allUserForDefaultTenant.size() );
    assertEquals( 2 + DEFAULT_USER_COUNT, allUserForTenant.size() );
    logout();
    login( "admin", mainTenant_2, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    allUserForDefaultTenant = service.getAllUsers();
    allUserForTenant = service.getAllUsers( mainTenant_1 );

    assertTrue( allUserForDefaultTenant.size() == 2 + DEFAULT_USER_COUNT );
    assertEquals( 5 + DEFAULT_USER_COUNT, allUserForTenant.size() );

    allUserForTenant = service.getAllUsers( mainTenant_1 );

    assertEquals( 5 + DEFAULT_USER_COUNT, allUserForTenant.size() );
    allUserForTenant = service.getAllUsers( mainTenant_2 );
    assertEquals( 2 + DEFAULT_USER_COUNT, allUserForTenant.size() );
    logout();

    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    allUserForTenant = service.getAllUsers( mainTenant_1 );
    assertEquals( 5 + DEFAULT_USER_COUNT, allUserForTenant.size() );

    allUserForTenant = service.getAllUsers( mainTenant_2 );
    assertEquals( 2 + DEFAULT_USER_COUNT, allUserForTenant.size() );
  }

  @Test
  public void testGetAuthoritiesForUser() {
    loginAsRepositoryAdmin();
    systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { tenantAdminAuthorityName } );
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminAuthorityName,
      tenantAuthenticatedAuthorityName } );
    mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { tenantAdminAuthorityName } );
    mainTenant_2 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_2, tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( mainTenant_2, "admin", "password", "", new String[] { tenantAdminAuthorityName } );

    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.createUser( mainTenant_1, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null );
    userRoleDao.createUser( null, tenantedUserNameUtils.getPrincipleId( mainTenant_1, USER_3 ), PASSWORD_3,
            USER_DESCRIPTION_3, null );
    userRoleDao.createUser( null, USER_4, PASSWORD_4, USER_DESCRIPTION_4, null );
    logout();

    login( "admin", mainTenant_2, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.createUser( mainTenant_2, USER_5, PASSWORD_5, USER_DESCRIPTION_5, null );
    userRoleDao.createUser( null, tenantedUserNameUtils.getPrincipleId( mainTenant_2, USER_6 ), PASSWORD_6,
            USER_DESCRIPTION_6, null );

    logout();

    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.createRole( mainTenant_1, ROLE_1, ROLE_DESCRIPTION_1, null );
    userRoleDao.createRole( null, tenantedRoleNameUtils.getPrincipleId( mainTenant_1, ROLE_2 ), ROLE_DESCRIPTION_2,
            null );
    userRoleDao.createRole( null, ROLE_3, ROLE_DESCRIPTION_3, null );
    logout();

    login( "admin", mainTenant_2, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.createRole( mainTenant_2, ROLE_4, ROLE_DESCRIPTION_4, null );
    userRoleDao.setUserRoles( null, USER_5, new String[] { ROLE_4 } );
    userRoleDao.setUserRoles( null, tenantedUserNameUtils.getPrincipleId( mainTenant_2, USER_6 ),
        new String[] { ROLE_4 } );
    logout();
    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.setUserRoles( mainTenant_1, USER_2, new String[] { ROLE_1, ROLE_2, ROLE_3 } );

    List<String> systemRoles = Arrays.asList( new String[] { "Admin" } );

    try {
      userRoleDao.setUserRoles( mainTenant_1, USER_3, new String[] { ROLE_2, ROLE_3, ROLE_4 } );
      fail( "Exception should be thrown" );
    } catch ( Throwable th ) {
      assertNotNull( th );
    }

    try {
      userRoleDao.setUserRoles( mainTenant_1, USER_4, new String[] { ROLE_2, ROLE_4 } );
      fail( "Exception should be thrown" );
    } catch ( Throwable th ) {
      assertNotNull( th );
    }
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );
    userDetailsService.setDefaultRole( tenantAuthenticatedAuthorityName );

    List<String> extraRoles = Arrays.asList( new String[] { "Authenticated", "Anonymous" } );
    String adminRole = "Admin";

    UserRoleDaoUserRoleListService service =
        new UserRoleDaoUserRoleListService( userRoleDao, userDetailsService, tenantedUserNameUtils, systemRoles,
            extraRoles, adminRole );
    service.setUserDetailsService( userDetailsService );

    logout();
    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    List<String> rolesForUser_2 = service.getRolesForUser( mainTenant_1, USER_2 );
    List<String> rolesForUser_2_1 = service.getRolesForUser( null, USER_2 );
    List<String> rolesForUser_2_1_1 =
        service.getRolesForUser( null, tenantedUserNameUtils.getPrincipleId( mainTenant_1, USER_2 ) );
    List<String> rolesForUser_3 = service.getRolesForUser( mainTenant_1, USER_3 );
    List<String> rolesForUser_4 = service.getRolesForUser( mainTenant_1, USER_4 );

    assertTrue( rolesForUser_2.size() == 4 );
    assertTrue( rolesForUser_2_1.size() == 4 );
    assertTrue( rolesForUser_2_1_1.size() == 4 );
    assertTrue( rolesForUser_3.size() == 3 );
    assertTrue( rolesForUser_4.size() == 2 );
  }

  @Test
  public void testGetUsernamesInRole() {
    loginAsRepositoryAdmin();
    systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(),
          tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { tenantAdminAuthorityName } );
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminAuthorityName,
      tenantAuthenticatedAuthorityName } );
    mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { tenantAdminAuthorityName } );
    mainTenant_2 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_2, tenantAdminAuthorityName,
            tenantAuthenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( mainTenant_2, "admin", "password", "", new String[] { tenantAdminAuthorityName } );

    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.createUser( mainTenant_1, USER_2, PASSWORD_2, USER_DESCRIPTION_2, null );
    userRoleDao.createUser( null, USER_3, PASSWORD_3, USER_DESCRIPTION_3, null );
    userRoleDao.createUser( null, tenantedUserNameUtils.getPrincipleId( mainTenant_1, USER_4 ), PASSWORD_4,
            USER_DESCRIPTION_4, null );
    userRoleDao.createUser( mainTenant_1, USER_5, PASSWORD_5, USER_DESCRIPTION_5, null );
    userRoleDao.createUser( mainTenant_1, USER_6, PASSWORD_6, USER_DESCRIPTION_6, null );
    logout();
    login( "admin", mainTenant_2, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.createUser( mainTenant_2, USER_7, PASSWORD_7, USER_DESCRIPTION_7, null );
    userRoleDao.createUser( mainTenant_2, USER_8, PASSWORD_8, USER_DESCRIPTION_8, null );
    logout();
    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.createRole( mainTenant_1, ROLE_1, ROLE_DESCRIPTION_1, null );
    userRoleDao.createRole( null, ROLE_2, ROLE_DESCRIPTION_2, null );
    userRoleDao.createRole( null, tenantedRoleNameUtils.getPrincipleId( mainTenant_1, ROLE_3 ), ROLE_DESCRIPTION_3,
            null );
    logout();
    login( "admin", mainTenant_2, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.createRole( mainTenant_2, ROLE_4, ROLE_DESCRIPTION_4, null );
    logout();
    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.setRoleMembers( null, ROLE_1, new String[] { USER_2, USER_3, USER_4 } );
    userRoleDao.setRoleMembers( mainTenant_1, ROLE_2, new String[] { USER_5, USER_6, USER_7 } );
    userRoleDao.setRoleMembers( null, tenantedRoleNameUtils.getPrincipleId( mainTenant_1, ROLE_3 ), new String[] {
      USER_2, USER_4, USER_6 } );
    logout();
    login( "admin", mainTenant_2, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    userRoleDao.setRoleMembers( null, ROLE_4, new String[] { USER_3, USER_5, USER_7 } );
    logout();
    login( "admin", mainTenant_1, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );
    userDetailsService.setDefaultRole( tenantAuthenticatedAuthorityName );
    List<String> systemRoles = new ArrayList<String>();
    systemRoles.add( "Admin" );

    List<String> extraRoles = Arrays.asList( new String[] { "Authenticated", "Anonymous" } );
    String adminRole = "Admin";

    UserRoleDaoUserRoleListService service =
        new UserRoleDaoUserRoleListService( userRoleDao, userDetailsService, tenantedUserNameUtils, systemRoles,
            extraRoles, adminRole );

    List<String> usersInRole_1 = service.getUsersInRole( mainTenant_1, ROLE_1 );
    List<String> usersInRole_2 = service.getUsersInRole( null, ROLE_2 );
    List<String> usersInRole_3 =
        service.getUsersInRole( null, tenantedRoleNameUtils.getPrincipleId( mainTenant_1, ROLE_3 ) );

    logout();
    login( "admin", mainTenant_2, new String[] { tenantAdminAuthorityName, tenantAuthenticatedAuthorityName } );

    List<String> usersInRole_4 = service.getUsersInRole( mainTenant_2, ROLE_4 );

    assertTrue( usersInRole_1.size() == 3 );
    assertTrue( usersInRole_2.size() == 2 );
    assertTrue( usersInRole_3.size() == 3 );
    assertTrue( usersInRole_4.size() == 1 );

    logout();
  }

}
