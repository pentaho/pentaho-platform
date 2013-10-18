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

package org.pentaho.test.platform.web.http.api;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.solution.ContentInfo;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PlatformPlugin;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.repository2.unified.webservices.ExecutableFileTypeDto;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserDetailsService;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserRoleListService;
import org.pentaho.platform.web.http.api.resources.FileResourceContentGenerator;
import org.pentaho.platform.web.http.api.resources.RepositoryResource;
import org.pentaho.platform.web.http.filters.PentahoRequestContextFilter;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
import org.springframework.transaction.support.TransactionTemplate;

import javax.jcr.Repository;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static junit.framework.Assert.assertEquals;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.*;
import static org.pentaho.test.platform.web.http.api.JerseyTestUtil.assertResponse;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" } )
@SuppressWarnings( "nls" )
public class RepositoryResourceTest extends JerseyTest implements ApplicationContextAware {

  private static MicroPlatform mp = new MicroPlatform();

  public static final String MAIN_TENANT_1 = "maintenant1";

  private IUnifiedRepository repo;

  private IUserRoleListService userRoleListService;

  private boolean startupCalled;

  private String repositoryAdminUsername;

  private String sysAdminRoleName;

  private String adminAuthorityName;

  private String authenticatedAuthorityName;

  private JcrTemplate testJcrTemplate;

  private IBackingRepositoryLifecycleManager manager;

  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  private IAuthorizationPolicy authorizationPolicy;

  IUserRoleDao userRoleDao;

  private ITenantManager tenantManager;
  private String sysAdminAuthorityName;
  private String sysAdminUserName;
  private IRepositoryFileDao repositoryFileDao;
  private Repository repository = null;
  private ITenant systemTenant = null;
  private ITenantedPrincipleNameResolver tenantedRoleNameUtils;
  private ITenantedPrincipleNameResolver tenantedUserNameUtils;
  private IRoleAuthorizationPolicyRoleBindingDao roleAuthorizationPolicyRoleBindingDao;
  private static TransactionTemplate jcrTransactionTemplate;
  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDaoTarget;
  public static final String SYSTEM_PROPERTY = "spring.security.strategy";

  private static WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder(
      "org.pentaho.platform.web.http.api.resources" ).contextPath( "api" ).addFilter(
      PentahoRequestContextFilter.class, "pentahoRequestContextFilter" ).build();

  public RepositoryResourceTest() throws Exception {
    mp.setFullyQualifiedServerUrl( getBaseURI() + webAppDescriptor.getContextPath() + "/" );
  }

  @Override
  protected AppDescriptor configure() {
    return webAppDescriptor;
  }

  @Override
  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    // BasicConfigurator.configure();
    System.setProperty( SYSTEM_PROPERTY, "MODE_GLOBAL" );
    Logger.getLogger( "org" ).setLevel( Level.WARN );
    Logger.getLogger( "org.pentaho" ).setLevel( Level.WARN );
    Logger.getLogger( RepositoryResource.class ).setLevel( Level.DEBUG );
    // Logger.getLogger(RequestProxy.class).setLevel(Level.DEBUG);
    Logger.getLogger( "MIME_TYPE" ).setLevel( Level.TRACE );

    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );

    FileUtils.deleteDirectory( new File( "/tmp/jackrabbit-test-TRUNK" ) );
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
  }

  @AfterClass
  public static void afterClass() {
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL );
  }

  @Before
  public void beforeTest() throws PlatformInitializationException {
    System.setProperty( SYSTEM_PROPERTY, "MODE_INHERITABLETHREADLOCAL" );
    mp = new MicroPlatform();
    File path = new File( ".", "test-res/PluginResourceTest/system/test-plugin" );
    mp.setFilePath( path.getAbsolutePath() );
    // used by DefaultPentahoJackrabbitAccessControlHelper
    mp.define( IPluginManager.class, DefaultPluginManager.class, Scope.GLOBAL );
    mp.defineInstance( IPluginResourceLoader.class, new PluginResourceLoader() {
      protected PluginClassLoader getOverrideClassloader() {
        return new PluginClassLoader( new File( ".", "test-res/PluginResourceTest/system/test-plugin" ), this );
      }
    } );
    mp.define( IPluginProvider.class, TestPlugin.class, Scope.GLOBAL );

    mp.defineInstance( IAuthorizationPolicy.class, authorizationPolicy );
    mp.defineInstance( ITenantManager.class, tenantManager );
    mp.define( ITenant.class, Tenant.class );
    mp.defineInstance( "roleAuthorizationPolicyRoleBindingDaoTarget", roleBindingDaoTarget );
    mp.defineInstance( IRoleAuthorizationPolicyRoleBindingDao.class, roleBindingDaoTarget );
    mp.defineInstance( "tenantedUserNameUtils", tenantedUserNameUtils );
    mp.defineInstance( "repositoryAdminUsername", repositoryAdminUsername );
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao( userRoleDao );
    List<String> systemRoles = new ArrayList<String>();
    systemRoles.add( "Admin" );
    List<String> extraRoles = Arrays.asList( new String[] { "Authenticated", "Anonymous" } );
    String adminRole = "Admin";

    userRoleListService =
        new UserRoleDaoUserRoleListService( userRoleDao, userDetailsService, tenantedUserNameUtils, systemRoles,
            extraRoles, adminRole );
    ( (UserRoleDaoUserRoleListService) userRoleListService ).setUserRoleDao( userRoleDao );
    ( (UserRoleDaoUserRoleListService) userRoleListService ).setUserDetailsService( userDetailsService );

    mp.defineInstance( IUserRoleListService.class, userRoleListService );
    mp.start();
    PentahoSystem.get( IPluginManager.class ).reload();
    logout();
    startupCalled = true;
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
    roleBindingDao = null;
    authorizationPolicy = null;
    testJcrTemplate = null;
    if ( startupCalled ) {
      manager.shutdown();
    }

    // null out fields to get back memory
    repo = null;
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

  protected void clearRoleBindings() throws Exception {
    loginAsRepositoryAdmin();
    // SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath("duff") + ".authz");
    // SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath("duff") + ".authz");
  }

  @Test
  public void testDummy() {

  }

  @Ignore
  public void testGetFileText() throws Exception {
    loginAsRepositoryAdmin();
    ITenant systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
            authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
            "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    mp.defineInstance( IUnifiedRepository.class, repo );

    login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );
    String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();

    createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + "file.txt", "abcdefg" );

    WebResource webResource = resource();

    ClientResponse r1 =
        webResource.path( "repos/:public:file.txt/content" ).accept( TEXT_PLAIN ).get( ClientResponse.class );
    assertResponse( r1, Status.OK, MediaType.TEXT_PLAIN );
    assertEquals( "abcdefg", r1.getEntity( String.class ) );

    // check again but with no Accept header
    ClientResponse r2 = webResource.path( "repos/:public:file.txt/content" ).get( ClientResponse.class );
    assertResponse( r2, Status.OK, MediaType.TEXT_PLAIN );
    assertEquals( "abcdefg", r2.getEntity( String.class ) );

    // check again but with */*
    ClientResponse r3 =
        webResource.path( "repos/:public:file.txt/content" ).accept( TEXT_PLAIN ).accept( MediaType.WILDCARD ).get(
            ClientResponse.class );
    assertResponse( r3, Status.OK, MediaType.TEXT_PLAIN );
    assertEquals( "abcdefg", r3.getEntity( String.class ) );

    cleanupUserAndRoles( mainTenant_1 );
    cleanupUserAndRoles( systemTenant );
  }

  @Ignore
  public void a1_HappyPath() {
    loginAsRepositoryAdmin();

    ITenant systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
            authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
            "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    mp.defineInstance( IUnifiedRepository.class, repo );

    login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );
    String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();

    createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + "test.xjunit", "abcdefg" );

    WebResource webResource = resource();

    ClientResponse response =
        webResource.path( "repos/:public:test.xjunit/public/file.txt" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.OK, TEXT_PLAIN );
    assertEquals( "contents of file incorrect/missing", "test text", response.getEntity( String.class ) );

    cleanupUserAndRoles( mainTenant_1 );
    cleanupUserAndRoles( systemTenant );
  }

  @Ignore
  public void a2_HappyPath() {
    loginAsRepositoryAdmin();

    ITenant systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
            authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
            "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    mp.defineInstance( IUnifiedRepository.class, repo );

    login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );
    String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();

    createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + "test.xjunit", "abcdefg" );
    createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + "js/test.js", "js content" );
    createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + "test.css", "css content" );

    WebResource webResource = resource();

    // load the css
    ClientResponse response = webResource.path( "repos/:public:test.xjunit/test.css" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.OK, "text/css" );
    assertEquals( "contents of file incorrect/missing", "css content", response.getEntity( String.class ) );

    // load the js
    response = webResource.path( "repos/:public:test.xjunit/js/test.js" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.OK, "text/javascript" );
    assertEquals( "contents of file incorrect/missing", "js content", response.getEntity( String.class ) );
  }

  @Ignore
  public void a3_dotUrl() {
    loginAsRepositoryAdmin();

    ITenant systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
            authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
            "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    mp.defineInstance( IUnifiedRepository.class, repo );

    login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );
    String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();

    final String text = "URL=http://google.com";
    createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + "test.url", text );

    WebResource webResource = resource();

    String response = webResource.path( "repos/:public:test.url/content" ).get( String.class );
    assertEquals( "contents of file incorrect/missing", text, response );

    ClientResponse getResponse =
        webResource.path( "repos/:public:test.url/generatedContent" ).get( ClientResponse.class );
    assertEquals( ClientResponse.Status.OK, getResponse.getClientResponseStatus() );
  }

  @Ignore
  public void a3_dotUrlRelativeUrl() {
    // login("admin", "duff", true);
    final String text = "URL=repo/files/public/children";
    stubGetFile( repo, "/public/relUrlTest.url" );
    stubGetData( repo, "/public/relUrlTest.url", text );
    stubGetTree( repo, "/public", "relUrlTest.url", "hello/file.txt", "hello/file2.txt", "hello2/" );
    WebResource webResource = resource();

    String response = webResource.path( "repos/:public:relUrlTest.url/content" ).get( String.class );
    assertEquals( "contents of file incorrect/missing", text, response );

    ClientResponse getResponse =
        webResource.path( "repos/:public:relUrlTest.url/generatedContent" ).get( ClientResponse.class );
    assertEquals( ClientResponse.Status.OK, getResponse.getClientResponseStatus() );
    logout();
  }

  @Ignore
  public void a3_HappyPath_GET() {
    loginAsRepositoryAdmin();

    ITenant systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
            authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
            "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    mp.defineInstance( IUnifiedRepository.class, repo );

    login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );
    String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();

    createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + "test.xjunit", "abcdefg" );

    WebResource webResource = resource();

    // get the output of the .xjunit file (should invoke the content generator)
    String textResponse = webResource.path( "repos/:public:test.xjunit/viewer" ).get( String.class );
    assertEquals( "Content generator failed to provide correct output",
      "hello viewer content generator", textResponse );
  }

  @Ignore
  public void a3_HappyPath_POST() {
    loginAsRepositoryAdmin();

    ITenant systemTenant =
        tenantManager.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), adminAuthorityName,
            authenticatedAuthorityName, "Anonymous" );
    userRoleDao.createUser( systemTenant, sysAdminUserName, "password", "", new String[] { adminAuthorityName } );
    ITenant mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, adminAuthorityName, authenticatedAuthorityName,
            "Anonymous" );
    userRoleDao.createUser( mainTenant_1, "admin", "password", "", new String[] { adminAuthorityName } );
    mp.defineInstance( IUnifiedRepository.class, repo );

    login( "admin", mainTenant_1, new String[] { authenticatedAuthorityName } );
    String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();

    createTestFile( publicFolderPath.replaceAll( "/", ":" ) + ":" + "test.xjunit", "abcdefg" );

    WebResource webResource = resource();

    // get the output of the .junit file (should invoke the content generator)
    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.add( "testParam", "testParamValue" );

    ClientResponse response =
        webResource.path( "repos/:public:test.xjunit/viewer" ).type( APPLICATION_FORM_URLENCODED ).post(
            ClientResponse.class, formData );
    assertResponse( response, Status.OK );
    assertEquals( "Content generator failed to provide correct output", "hello viewer content generator", response
        .getEntity( String.class ) );
  }

  @Ignore
  public void a3_HappyPath_GET_withCommand() throws PlatformInitializationException {
    stubGetFile( repo, "/public/test.xjunit" );

    WebResource webResource = resource();

    String expectedResponse = "hello this is service content generator servicing command dosomething";
    String textResponse =
        webResource.path( "repos/:public:test.xjunit/testservice/dosomething" ).queryParam( "testParam",
            "testParamValue" ).get( String.class );
    assertEquals( "Content generator failed to provide correct output", expectedResponse, textResponse );
  }

  // @Test
  // public void a3_GET_testMimeTypePrecedence() throws PlatformInitializationException {
  // WebResource webResource = resource();
  //
  // createTestFile("repo/files/public:test.xjunit", "sometext");
  //
  // ClientResponse response =
  // webResource.path("repos/:public:test.xjunit/testservice/dosomething").queryParam("testParam",
  // "testParamValue").accept(MediaType.APPLICATION_XML, MediaType.TEXT_HTML).get(ClientResponse.class);
  // assertResponse(response, Status.OK, MediaType.APPLICATION_XML);
  // System.out.println(response.getEntity(String.class));
  // }

  @Ignore
  public void a3_HappyPath_POST_withCommand() throws PlatformInitializationException {
    stubGetFile( repo, "/public/test.xjunit" );

    WebResource webResource = resource();

    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.add( "testParam", "testParamValue" );

    ClientResponse response =
        webResource.path( "repos/:public:test.xjunit/testservice/dosomething" ).type( APPLICATION_FORM_URLENCODED )
            .post( ClientResponse.class, formData );
    assertResponse( response, Status.OK );
    String expectedResponse = "hello this is service content generator servicing command dosomething";
    assertEquals( "Content generator failed to provide correct output", expectedResponse, response
        .getEntity( String.class ) );
  }

  @Ignore
  public void b1_HappyPath() {
    WebResource webResource = resource();
    ClientResponse response = webResource.path( "repos/xjunit/public/file.txt" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.OK, TEXT_PLAIN );
    assertEquals( "contents of file incorrect/missing", "test text", response.getEntity( String.class ) );
  }

  @Ignore
  public void b1_PrivateFile() {
    WebResource webResource = resource();
    ClientResponse response = webResource.path( "repos/xjunit/private/private.txt" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.FORBIDDEN );
  }

  @Ignore
  public void b1_PluginDNE() {
    WebResource webResource = resource();
    ClientResponse response =
        webResource.path( "repos/non-existent-plugin/private/private.txt" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.NOT_FOUND );
  }

  @Ignore
  public void b1_FileDNE() {
    WebResource webResource = resource();
    ClientResponse response = webResource.path( "repos/xjunit/public/doesnotexist.txt" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.NOT_FOUND );
  }

  @Ignore
  public void b3_HappyPath_GET() throws PlatformInitializationException {
    stubGetFile( repo, "/public/test.xjunit" );

    WebResource webResource = resource();

    // get the output of the .xjunit file (should invoke the content generator)
    String textResponse = webResource.path( "repos/xjunit/viewer" ).get( String.class );
    assertEquals( "Content generator failed to provide correct output",
      "hello viewer content generator", textResponse );
  }

  @Ignore
  public void b3_HappyPath_GET_withMimeType() throws PlatformInitializationException {
    stubGetFile( repo, "/public/test.xjunit" );

    WebResource webResource = resource();

    // get the output of the .xjunit file (should invoke the content generator)
    ClientResponse response =
        webResource.path( "repos/xjunit/report" ).accept( "application/pdf" ).get( ClientResponse.class );

    assertResponse( response, ClientResponse.Status.OK, "application/pdf; charset=UTF-8" );
  }

  @Ignore
  public void c1_HappyPath() {
    WebResource webResource = resource();
    ClientResponse response = webResource.path( "repos/test-plugin/public/file.txt" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.OK, TEXT_PLAIN );
    assertEquals( "contents of file incorrect/missing", "test text", response.getEntity( String.class ) );
  }

  @Ignore
  public void c3_HappyPath_GET_withCommand() throws PlatformInitializationException {
    WebResource webResource = resource();

    String expectedResponse = "hello this is service content generator servicing command dosomething";
    String textResponse =
        webResource.path( "repos/test-plugin/testservice/dosomething" ).queryParam( "testParam", "testParamValue" )
            .get( String.class );
    assertEquals( "Content generator failed to provide correct output", expectedResponse, textResponse );
  }

  @Ignore
  public void c3_HappyPath_POST_withCommand() {
    WebResource webResource = resource();

    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.add( "testParam", "testParamValue" );

    ClientResponse response =
        webResource.path( "repos/test-plugin/testservice/dosomething" ).type( APPLICATION_FORM_URLENCODED ).post(
            ClientResponse.class, formData );
    assertResponse( response, Status.OK );
    String expectedResponse = "hello this is service content generator servicing command dosomething";
    assertEquals( "Content generator failed to provide correct output", expectedResponse, response
        .getEntity( String.class ) );
  }

  @Ignore
  public void testExecutableTypes() {
    WebResource webResource = resource();
    System.out.println( webResource.getURI().getPath() );
    List<ExecutableFileTypeDto> executableTypes =
        webResource.path( "repos/executableTypes" ).get( new GenericType<List<ExecutableFileTypeDto>>() {
        } );
    assertEquals( 1, executableTypes.size() );
    assertEquals( "xjunit", executableTypes.get( 0 ).getExtension() );
  }

  public static class TestPlugin implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      List<IPlatformPlugin> plugins = new ArrayList<IPlatformPlugin>();

      PlatformPlugin p = new PlatformPlugin( new DefaultListableBeanFactory() );
      p.setId( "test-plugin" );

      p.addStaticResourcePath( "notused", "public" );

      BeanDefinition def =
          BeanDefinitionBuilder.rootBeanDefinition( JUnitContentGenerator.class.getName() ).getBeanDefinition();
      p.getBeanFactory().registerBeanDefinition( "xjunit.viewer", def );

      def =
          BeanDefinitionBuilder.rootBeanDefinition( JUnitServiceContentGenerator.class.getName() ).getBeanDefinition();
      p.getBeanFactory().registerBeanDefinition( "testservice", def );

      def = BeanDefinitionBuilder.rootBeanDefinition( ReportContentGenerator.class.getName() ).getBeanDefinition();
      p.getBeanFactory().registerBeanDefinition( "xjunit.report", def );

      ContentInfo contentInfo = new ContentInfo();
      contentInfo.setDescription( "JUnit file type" );
      contentInfo.setExtension( "xjunit" );
      contentInfo.setMimeType( "application/zip" );
      contentInfo.setTitle( "JUNIT" );
      p.addContentInfo( contentInfo );

      plugins.add( p );

      return plugins;
    }
  }

  @SuppressWarnings( "serial" )
  public static class JUnitContentGenerator extends BaseContentGenerator {
    @Override
    public void createContent() throws Exception {
      try {
        IContentItem responseContentItem =
            outputHandler.getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, null, null );
        // mime type setting will blow up since servlet api used by grizzly is too old
        try {
          responseContentItem.setMimeType( "text/plain" );
        } catch ( Throwable t ) {
          //ignored
        }
        OutputStream outputStream = responseContentItem.getOutputStream( null );
        IOUtils.write( "hello viewer content generator", outputStream );
        outputStream.close();
      } catch ( Throwable t ) {
        t.printStackTrace();
      }
    }

    @Override
    public Log getLogger() {
      return null;
    }
  }

  @SuppressWarnings( "serial" )
  public static class JUnitServiceContentGenerator extends BaseContentGenerator {
    @Override
    public void createContent() throws Exception {
      try {
        IContentItem responseContentItem =
            outputHandler.getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, null, null );
        // mime type setting will blow up since servlet api used by grizzly is too old
        try {
          responseContentItem.setMimeType( "text/plain" );
        } catch ( Throwable t ) {
          //ignored
        }
        OutputStream outputStream = responseContentItem.getOutputStream( null );
        IParameterProvider pathParams = parameterProviders.get( "path" );
        String command = pathParams.getStringParameter( "cmd", "" );

        Object testParamValue = parameterProviders.get( IParameterProvider.SCOPE_REQUEST ).getParameter( "testParam" );
        assertEquals( "testParam is missing from request", "testParamValue", testParamValue );

        IOUtils.write( "hello this is service content generator servicing command " + command, outputStream );
        outputStream.close();
      } catch ( Throwable t ) {
        t.printStackTrace();
      }
    }

    @Override
    public Log getLogger() {
      return null;
    }
  }

  @SuppressWarnings( "serial" )
  public static class ReportContentGenerator extends FileResourceContentGenerator {
    static Log logger = LogFactory.getLog( ReportContentGenerator.class );
    OutputStream out = null;

    @Override
    public Log getLogger() {
      return logger;
    }

    @Override
    public void setRepositoryFile( RepositoryFile repositoryFile ) {
    }

    @Override
    public String getMimeType( String streamPropertyName ) {
      return "application/pdf;";
    }

    @Override
    public void setOutputStream( OutputStream outputStream ) {
      out = outputStream;
    }

    @Override
    public void execute() throws Exception {
      IParameterProvider pathParams = parameterProviders.get( "path" );
      String command = pathParams.getStringParameter( "cmd", "" );

      IOUtils.write( "hello this is service content generator servicing command " + command, out );
      out.close();
    }
  }

  @Override
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
    jcrTransactionTemplate = (TransactionTemplate) applicationContext.getBean( "jcrTransactionTemplate" );
    repo = (IUnifiedRepository) applicationContext.getBean( "unifiedRepository" );
    TestPrincipalProvider.userRoleDao = (IUserRoleDao) applicationContext.getBean( "userRoleDaoTxn" );
    TestPrincipalProvider.adminCredentialsStrategy =
        (CredentialsStrategy) applicationContext.getBean( "jcrAdminCredentialsStrategy" );
    TestPrincipalProvider.repository = (Repository) applicationContext.getBean( "jcrRepository" );
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

  protected void login( final String username, final ITenant tenant ) {
    login( username, tenant, false );
  }

  /**
   * Logs in with given username.
   * 
   * @param username
   *          username of user
   * @param tenantId
   *          tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority
   * to the user's roles
   */
  protected void login( final String username, final ITenant tenant, String[] roles ) {
    StandaloneSession pentahoSession = new StandaloneSession( tenantedUserNameUtils.getPrincipleId( tenant,
      username ) );
    pentahoSession.setAuthenticated( tenant.getId(), tenantedUserNameUtils.getPrincipleId( tenant, username ) );
    PentahoSessionHolder.setSession( pentahoSession );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();

    for ( String roleName : roles ) {
      authList.add( new GrantedAuthorityImpl( tenantedRoleNameUtils.getPrincipleId( tenant, roleName ) ) );
    }
    GrantedAuthority[] authorities = authList.toArray( new GrantedAuthority[0] );
    UserDetails userDetails = new User( username, password, true, true, true, true, authorities );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
    SecurityHelper.getInstance().becomeUser( tenantedUserNameUtils.getPrincipleId( tenant, username ) );
    SecurityContextHolder.getContext().setAuthentication( auth );
  }

  /**
   * Logs in with given username.
   * 
   * @param username
   *          username of user
   * @param tenantId
   *          tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  protected void login( final String username, final ITenant tenant, final boolean tenantAdmin ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( username );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
    authList.add( new GrantedAuthorityImpl( authenticatedAuthorityName ) );
    if ( tenantAdmin ) {
      authList.add( new GrantedAuthorityImpl( adminAuthorityName ) );
    }
    GrantedAuthority[] authorities = authList.toArray( new GrantedAuthority[0] );
    UserDetails userDetails = new User( username, password, true, true, true, true, authorities );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );
  }

  protected void createTestFile( String pathId, String text ) {
    WebResource webResource = resource();
    ClientResponse response =
        webResource.path( "repo/files/" + pathId ).type( TEXT_PLAIN ).put( ClientResponse.class, text );
    assertResponse( response, Status.OK );
  }

}
