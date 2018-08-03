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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.web.http.api;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.pentaho.test.platform.web.http.api.JerseyTestUtil.assertResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Repository;
import javax.ws.rs.core.MediaType;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.plugin.services.importer.NameBaseMimeResolver;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.StreamConverter;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.mt.RepositoryTenantManager;
import org.pentaho.platform.repository2.unified.DefaultRepositoryVersionManager;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxyFactory;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleAuthorizationPolicy;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserDetailsService;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserRoleListService;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.GrizzlyTestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;

@RunWith ( SpringJUnit4ClassRunner.class )
@ContextConfiguration ( locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" } )
@SuppressWarnings ( "nls" )
public class FileResourceIT extends JerseyTest implements ApplicationContextAware {

  private static MicroPlatform mp = new MicroPlatform();

  private static WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder(
      "org.pentaho.platform.web.http.api.resources" ).contextPath( "api" ).build();

  public static final String MAIN_TENANT_1 = "maintenant1";

  private IUnifiedRepository repo;

  private IUserRoleListService userRoleListService;

  private boolean startupCalled;

  private String repositoryAdminUsername;

  private String adminAuthorityName;

  private String authenticatedAuthorityName;

  private JcrTemplate testJcrTemplate;

  private IBackingRepositoryLifecycleManager manager;

  private IAuthorizationPolicy authorizationPolicy;

  IUserRoleDao userRoleDao;

  private ITenantManager tenantManager;
  private String sysAdminAuthorityName;
  private String sysAdminUserName;
  private IRepositoryFileDao repositoryFileDao;
  private ITenantedPrincipleNameResolver tenantedRoleNameUtils;
  private ITenantedPrincipleNameResolver tenantedUserNameUtils;
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDaoTarget;
  public static final String SYSTEM_PROPERTY = "spring.security.strategy";

  public FileResourceIT() throws Exception {
    super();
    this.setTestContainerFactory( new GrizzlyTestContainerFactory() );
    mp.setFullyQualifiedServerUrl( getBaseURI() + webAppDescriptor.getContextPath() + "/" );
  }

  protected AppDescriptor configure() {
    return webAppDescriptor;
  }

  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    System.setProperty( SYSTEM_PROPERTY, "MODE_GLOBAL" );
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );

    FileUtils.deleteDirectory( new File( "/tmp/jackrabbit-test-TRUNK" ) );
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
  }

  @AfterClass
  public static void afterClass() {
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
  }

  private void cleanupUserAndRoles( final ITenant tenant ) {
    loginAsRepositoryAdmin();
    for ( IPentahoRole role : userRoleDao.getRoles( tenant ) ) {
      userRoleDao.deleteRole( role );
    }
    for ( IPentahoUser user : userRoleDao.getUsers( tenant ) ) {
      userRoleDao.deleteUser( user );
    }
  }

  @Before
  public void beforeTest() throws PlatformInitializationException {
    mp = new MicroPlatform();
    // used by DefaultPentahoJackrabbitAccessControlHelper
    mp.defineInstance( IAuthorizationPolicy.class, authorizationPolicy );
    mp.defineInstance( ITenantManager.class, tenantManager );
    mp.define( ITenant.class, Tenant.class );
    mp.defineInstance( "roleAuthorizationPolicyRoleBindingDaoTarget", roleBindingDaoTarget );
    mp.defineInstance( IRoleAuthorizationPolicyRoleBindingDao.class, roleBindingDaoTarget );
    mp.defineInstance( "tenantedUserNameUtils", tenantedUserNameUtils );
    mp.defineInstance( "tenantedRoleNameUtils", tenantedRoleNameUtils );
    mp.defineInstance( "repositoryAdminUsername", repositoryAdminUsername );
    mp.define( IRoleAuthorizationPolicyRoleBindingDao.class, RoleAuthorizationPolicy.class, Scope.GLOBAL );
    mp.define( ITenantManager.class, RepositoryTenantManager.class, Scope.GLOBAL );
    mp.defineInstance( "singleTenantAdminAuthorityName", new String( "Administrator" ) );
    mp.defineInstance( "RepositoryFileProxyFactory", new RepositoryFileProxyFactory( this.testJcrTemplate, this.repositoryFileDao ) );

    DefaultRepositoryVersionManager defaultRepositoryVersionManager = new DefaultRepositoryVersionManager();
    defaultRepositoryVersionManager.setPlatformMimeResolver( new NameBaseMimeResolver() );
    mp.defineInstance( IRepositoryVersionManager.class, defaultRepositoryVersionManager );
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );
    List<String> systemRoles = new ArrayList<String>();
    systemRoles.add( "Admin" );
    List<String> extraRoles = Arrays.asList( new String[]{"Authenticated", "Anonymous"} );
    String adminRole = "Admin";

    userRoleListService =
        new UserRoleDaoUserRoleListService( userRoleDao, userDetailsService, tenantedUserNameUtils, systemRoles,
            extraRoles, adminRole );
    ( (UserRoleDaoUserRoleListService) userRoleListService ).setUserRoleDao( userRoleDao );
    ( (UserRoleDaoUserRoleListService) userRoleListService ).setUserDetailsService( userDetailsService );

    mp.defineInstance( IUserRoleListService.class, userRoleListService );
    mp.start();
    logout();
    startupCalled = true;
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
  }

  @After
  public void afterTest() throws Exception {
    clearRoleBindings();
    // null out fields to get back memory
    authorizationPolicy = null;
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem( testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath() );
    logout();
    repositoryAdminUsername = null;
    adminAuthorityName = null;
    authenticatedAuthorityName = null;
    authorizationPolicy = null;
    testJcrTemplate = null;
    if ( startupCalled ) {
      manager.shutdown();
    }
    mp.stop();
    // null out fields to get back memory
    repo = null;
  }

  protected void clearRoleBindings() throws Exception {
    loginAsRepositoryAdmin();
  }

  protected void createTestFile( String pathId, String text ) {
    WebResource webResource = resource();
    ClientResponse response =
        webResource.path( "repo/files/" + pathId ).type( TEXT_PLAIN ).put( ClientResponse.class, text );
    assertResponse( response, Status.OK );
  }

  protected void createTestFileBinary( String pathId, byte[] data ) {
    WebResource webResource = resource();
    ClientResponse response =
      webResource.path( "repo/files/" + pathId ).type( APPLICATION_OCTET_STREAM )
        .put( ClientResponse.class, new String( data ) );
    assertResponse( response, Status.OK );
  }

  protected void createTestFolder( String pathId ) {
    WebResource webResource = resource();
    // webResource.path("repo/dirs/" + pathId).put();
    ClientResponse response = webResource.path( "repo/dirs/" + pathId ).type( TEXT_PLAIN ).put( ClientResponse.class );
    assertResponse( response, Status.OK );
  }

  @Test
  public void testDummy() {

  }

  @Test
  public void testWriteBinaryFile() throws InterruptedException {
    final String str = "some binary text";
    final String fileName = "file.bn";

    // set object in PentahoSystem
    mp.defineInstance( IUnifiedRepository.class, repo );

    loginAsRepositoryAdmin();
    ITenant systemTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
        authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );

    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
        "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "",
      new String[] { adminAuthorityName, authenticatedAuthorityName } );
    try {
      login( sysAdminUserName, systemTenant, new String[] { adminAuthorityName } );
      login( "admin", mainTenant_1, new String[] { adminAuthorityName, authenticatedAuthorityName } );

      WebResource webResource = resource();
      final byte[] blob = str.getBytes();
      String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();
      createTestFileBinary( publicFolderPath.replaceAll( "/", ":" ) + ":" + fileName, blob );

      // the file might not actually be ready.. wait a second
      Thread.sleep( 20000 );

      ClientResponse response =
        webResource.path( "repo/files/:public:file.bn" ).accept( APPLICATION_OCTET_STREAM )
          .get( ClientResponse.class );
      assertResponse( response, Status.OK, APPLICATION_OCTET_STREAM );

      byte[] data = response.getEntity( byte[].class );
      assertEquals( "contents of file incorrect/missing", str, new String( data ) );
    } catch ( Exception ex ) {
      TestCase.fail();
    } finally {
      cleanupUserAndRoles( mainTenant_1 );
      cleanupUserAndRoles( systemTenant );
    }
  }

  @Test
  public void testWriteTextFile() throws Exception {
    final String text = "sometext";

    mp.defineInstance( IUnifiedRepository.class, repo );
    final String fileName = "file.txt";

    loginAsRepositoryAdmin();
    ITenant systemTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
        authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );

    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
        "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    try {
      login( sysAdminUserName, systemTenant, new String[] { adminAuthorityName, authenticatedAuthorityName } );

      login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );

      WebResource webResource = resource();
      String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();
      createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + fileName, text );

      ClientResponse response =
        webResource.path( "repo/files/:public:" + fileName ).accept( TEXT_PLAIN ).get( ClientResponse.class );
      assertResponse( response, Status.OK, TEXT_PLAIN );
      assertEquals( "contents of file incorrect/missing", text, response.getEntity( String.class ) );

    } catch ( Throwable th ) {
      TestCase.fail();
    } finally {
      cleanupUserAndRoles( mainTenant_1 );
      cleanupUserAndRoles( systemTenant );
    }
  }

  @Test
  public void testGetFileText() throws Exception {
    final String text = "abcdefg";
    mp.defineInstance( IUnifiedRepository.class, repo );
    loginAsRepositoryAdmin();
    ITenant systemTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
        authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    login( sysAdminUserName, systemTenant, new String[] { adminAuthorityName, authenticatedAuthorityName } );

    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
        "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    try {
      login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );

      final String fileName = "file.txt";

      String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();
      createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + fileName, "abcdefg" );
      WebResource webResource = resource();


      ClientResponse r1 =
        webResource.path( "repo/files/:public:" + fileName ).accept( TEXT_PLAIN ).get( ClientResponse.class );
      assertResponse( r1, Status.OK, MediaType.TEXT_PLAIN );
      assertEquals( text, r1.getEntity( String.class ) );

      // check again but with no Accept header
      ClientResponse r2 = webResource.path( "repo/files/:public:" + fileName ).get( ClientResponse.class );
      assertResponse( r2, Status.OK, MediaType.TEXT_PLAIN );
      assertEquals( text, r2.getEntity( String.class ) );

      // check again but with */*
      ClientResponse r3 =
        webResource.path( "repo/files/:public:" + fileName ).accept( TEXT_PLAIN ).accept( MediaType.WILDCARD ).get(
          ClientResponse.class );
      assertResponse( r3, Status.OK, MediaType.TEXT_PLAIN );
      assertEquals( text, r3.getEntity( String.class ) );

    } catch ( Throwable ex ) {
      TestCase.fail();
    } finally {
      cleanupUserAndRoles( mainTenant_1 );
      cleanupUserAndRoles( systemTenant );
    }
  }

  @Test
  public void testGetWhenFileDNE() {
    mp.defineInstance( IUnifiedRepository.class, repo );
    loginAsRepositoryAdmin();

    WebResource webResource = resource();
    try {
      webResource.path( "repo/files/public:thisfiledoesnotexist.txt" ).accept( TEXT_PLAIN ).get(
        ClientResponse.class );
    } catch ( UnifiedRepositoryException ure ) {
      assertNotNull( ure );
    }
  }

  //This is testing the Rest end points, we should instead be testing the underlying functionality in unit tests
  @Test
  public void testBrowserDownload() {
    final String text = "abcdefg";

    // mock converters map
    StreamConverter streamConverter = new StreamConverter( repo );
    Map<String, Converter> converterMap = new HashMap<String, Converter>();
    converterMap.put( "txt", streamConverter );

    // stub DefaultExportProcessor
    DefaultExportHandler defaultExportHandler = new DefaultExportHandler();
    defaultExportHandler.setConverters( converterMap );
    defaultExportHandler.setRepository( repo );

    loginAsRepositoryAdmin();
    ITenant systemTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
        authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    login( sysAdminUserName, systemTenant, new String[] { adminAuthorityName, authenticatedAuthorityName } );

    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
        "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    try {
      login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName, adminAuthorityName } );

      mp.defineInstance( IUnifiedRepository.class, repo );
      mp.defineInstance( DefaultExportHandler.class, defaultExportHandler );

      final String fileName = "file.txt";
      createTestFile( "/public".replaceAll( "/", ":" ) + ":" + fileName, text );

      // test download of file
      WebResource webResource = resource();
      webResource.path( "repo/files/public:file.txt/download" ).get( ClientResponse.class );

      // test download of dir as a zip file
      ClientResponse r2 = webResource.path( "repo/files/public:file.txt/download" ).get( ClientResponse.class );
      assertResponse( r2, Status.OK );
      JerseyTestUtil.assertResponseIsZip( r2 );
    } catch ( Throwable ex ) {
      TestCase.fail();
    } finally {
      cleanupUserAndRoles( mainTenant_1 );
      cleanupUserAndRoles( systemTenant );
    }
  }

  //We should be testing the underlying class functionality in unit tests
  @Test
  public void testGetDirChildren() {
    loginAsRepositoryAdmin();
    ITenant systemTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
        authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
        "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    try {
      login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );

      // set object in PentahoSystem
      mp.defineInstance( IUnifiedRepository.class, repo );
      final String fileName = "file.txt";
      createTestFile( "/public".replaceAll( "/", ":" ) + ":" + fileName, "abcdefg" );
      WebResource webResource = resource();
      ClientResponse response =
        webResource.path( "repo/files/public/children" ).accept( APPLICATION_XML ).get( ClientResponse.class );

      assertResponse( response, Status.OK, APPLICATION_XML );

      String xml = response.getEntity( String.class );
      assertTrue( xml.startsWith( "<?" ) );
    } catch ( Throwable ex ) {
      TestCase.fail();
    } finally {
      cleanupUserAndRoles( mainTenant_1 );
      cleanupUserAndRoles( systemTenant );
    }
  }

  @Test
  public void testFileAcls() throws InterruptedException {
    loginAsRepositoryAdmin();
    ITenant systemTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
        authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
        "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    try {
      login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );
      mp.defineInstance( IUnifiedRepository.class, repo );

      String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();
      createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + "aclFile.txt", "abcdefg" );

      WebResource webResource = resource();
      RepositoryFileAclDto fileAcls =
        webResource.path( "repo/files/public:aclFile.txt/acl" ).accept( APPLICATION_XML ).get(
          RepositoryFileAclDto.class );
      List<RepositoryFileAclAceDto> aces = fileAcls.getAces();
      assertEquals( 2, aces.size() );
      RepositoryFileAclAceDto ace = aces.get( 0 );
      assertEquals( authenticatedAuthorityName, ace.getRecipient() );
      List<Integer> permissions = ace.getPermissions();
      assertEquals( 1, permissions.size() );
      Assert.assertTrue( permissions.contains( new Integer( 0 ) ) );

      String authenticated = authenticatedAuthorityName;

      aces = new ArrayList<RepositoryFileAclAceDto>();
      ace = new RepositoryFileAclAceDto();
      ace.setRecipient( authenticated );
      ace.setRecipientType( 1 );
      permissions = new ArrayList<Integer>();
      permissions.add( 2 );
      ace.setPermissions( permissions );
      aces.add( ace );
      fileAcls.setAces( aces );

      ClientResponse putResponse2 =
        webResource.path( "repo/files/public:aclFile.txt/acl" ).type( APPLICATION_XML ).put( ClientResponse.class,
          fileAcls );
      assertResponse( putResponse2, Status.OK );
    } catch ( Throwable ex ) {
      TestCase.fail();
    } finally {
      cleanupUserAndRoles( mainTenant_1 );
      cleanupUserAndRoles( systemTenant );
    }
  }

  @Test
  public void testDeleteFiles() {
    loginAsRepositoryAdmin();
    ITenant systemTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
        authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
        "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    try {
      login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );

      String testFile1Id = "abc.txt";
      String testFile2Id = "def.txt";

      // set object in PentahoSystem
      mp.defineInstance( IUnifiedRepository.class, repo );

      String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();
      createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + testFile1Id, "abcdefg" );
      createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + testFile2Id, "abcdefg" );
      createTestFolder( ":home:admin" );

      RepositoryFile file1 = repo.getFile( publicFolderPath + "/" + testFile1Id );
      RepositoryFile file2 = repo.getFile( publicFolderPath + "/" + testFile2Id );
      WebResource webResource = resource();
      webResource.path( "repo/files/delete" ).entity( file1.getId() + "," + file2.getId() ).put();

      RepositoryFileDto[] deletedFiles =
        webResource.path( "repo/files/deleted" ).accept( APPLICATION_XML ).get( RepositoryFileDto[].class );
      assertEquals( 2, deletedFiles.length );

      webResource.path( "repo/files/deletepermanent" ).entity( file2.getId() ).put();
    } catch ( Throwable ex ) {
      TestCase.fail();
    } finally {
      cleanupUserAndRoles( mainTenant_1 );
      cleanupUserAndRoles( systemTenant );
    }
  }

  @Test
  public void testFileCreator() {
    loginAsRepositoryAdmin();
    ITenant systemTenant =
      tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
        authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
      tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
        "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    try {
      login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );

      // set object in PentahoSystem
      mp.defineInstance( IUnifiedRepository.class, repo );

      WebResource webResource = resource();
      String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();
      createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + "file1.txt", "abcdefg" );
      RepositoryFile file1 = repo.getFile( publicFolderPath + "/" + "file1.txt" );
      RepositoryFileDto file2 = new RepositoryFileDto();
      file2.setId( file1.getId().toString() );
      webResource.path( "repo/files/public:file1.txt/creator" ).entity( file2 ).put();
    } catch ( Throwable ex ) {
      TestCase.fail();
    } finally {
      cleanupUserAndRoles( mainTenant_1 );
      cleanupUserAndRoles( systemTenant );
      logout();
    }

  }

  @Test
  public void testUserWorkspace() {
    PentahoSessionHolder.setSession( new StandaloneSession( "jerry" ) );
    WebResource webResource = resource();
    String userWorkspaceDir = webResource.path( "session/userWorkspaceDir" ).accept( TEXT_PLAIN ).get( String.class );
    assertTrue( userWorkspaceDir != null );
    assertTrue( userWorkspaceDir.length() > 0 );
  }


  public void setApplicationContext( final ApplicationContext applicationContext ) throws BeansException {
    manager = (IBackingRepositoryLifecycleManager) applicationContext.getBean( "backingRepositoryLifecycleManager" );
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean( "jcrSessionFactory" );
    testJcrTemplate = new JcrTemplate( jcrSessionFactory );
    testJcrTemplate.setAllowCreate( true );
    testJcrTemplate.setExposeNativeSession( true );
    repositoryAdminUsername = (String) applicationContext.getBean( "repositoryAdminUsername" );
    authenticatedAuthorityName = (String) applicationContext.getBean( "singleTenantAuthenticatedAuthorityName" );
    adminAuthorityName = (String) applicationContext.getBean( "singleTenantAdminAuthorityName" );
    sysAdminAuthorityName = (String) applicationContext.getBean( "superAdminAuthorityName" );
    sysAdminUserName = (String) applicationContext.getBean( "superAdminUserName" );
    authorizationPolicy = (IAuthorizationPolicy) applicationContext.getBean( "authorizationPolicy" );
    roleBindingDaoTarget =
      (IRoleAuthorizationPolicyRoleBindingDao) applicationContext
        .getBean( "roleAuthorizationPolicyRoleBindingDaoTarget" );
    tenantManager = (ITenantManager) applicationContext.getBean( "tenantMgrTxn" );
    repositoryFileDao = (IRepositoryFileDao) applicationContext.getBean( "repositoryFileDao" );
    userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDaoTxn" );
    tenantedUserNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean( "tenantedUserNameUtils" );
    tenantedRoleNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean( "tenantedRoleNameUtils" );
    repo = (IUnifiedRepository) applicationContext.getBean( "unifiedRepository" );
    TestPrincipalProvider.userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDaoTxn" );
    TestPrincipalProvider.adminCredentialsStrategy =
      (CredentialsStrategy) applicationContext.getBean( "jcrAdminCredentialsStrategy" );
    TestPrincipalProvider.repository = (Repository) applicationContext.getBean( "jcrRepository" );
  }

  protected void loginAsRepositoryAdmin() {
    StandaloneSession pentahoSession = new StandaloneSession( repositoryAdminUsername );
    pentahoSession.setAuthenticated( repositoryAdminUsername );
    final List<GrantedAuthority> repositoryAdminAuthorities =
      Arrays.asList( new GrantedAuthority[] { new SimpleGrantedAuthority( sysAdminAuthorityName ) } );
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

  protected void login( final String username, final ITenant tenant ) {
    login( username, tenant, false );
  }

  /**
   * Logs in with given username.
   *
   * @param username username of user
   * @param tenant   tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  protected void login( final String username, final ITenant tenant, String[] roles ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( tenant.getId(), username );
    PentahoSessionHolder.setSession( pentahoSession );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();

    for ( String roleName : roles ) {
      authList.add( new SimpleGrantedAuthority( roleName ) );
    }
    UserDetails userDetails = new User( username, password, true, true, true, true, authList );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authList );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
  }

  /**
   * Logs in with given username.
   *
   * @param username username of user
   * @param tenant   tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  protected void login( final String username, final ITenant tenant, final boolean tenantAdmin ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( username );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
    authList.add( new SimpleGrantedAuthority( authenticatedAuthorityName ) );
    if ( tenantAdmin ) {
      authList.add( new SimpleGrantedAuthority( adminAuthorityName ) );
    }
    UserDetails userDetails = new User( username, password, true, true, true, true, authList );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authList );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
  }

}
