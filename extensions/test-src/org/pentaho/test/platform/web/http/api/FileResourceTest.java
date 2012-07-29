package org.pentaho.test.platform.web.http.api;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.hasData;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.isLikeAcl;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.isLikeFile;
import static org.pentaho.test.platform.web.http.api.JerseyTestUtil.assertResponse;
import static org.pentaho.test.platform.web.http.api.JerseyTestUtil.assertResponseIsZip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Repository;
import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile.Mode;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserDetailsService;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserRoleListService;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
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

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.GrizzlyTestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/repository.spring.xml", "classpath:/repository-test-override.spring.xml" })
@SuppressWarnings("nls")

public class FileResourceTest extends JerseyTest implements ApplicationContextAware {

  private static MicroPlatform mp = new MicroPlatform();

  private static WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder("org.pentaho.platform.web.http.api.resources").contextPath("api").build();
  
  public static final String MAIN_TENANT_1 = "maintenant1";
  
  private IUnifiedRepository repo;

  private IUserRoleListService userRoleListService;

  private boolean startupCalled;

  private String repositoryAdminUsername;

  private String sysAdminRoleName;
  
  private String tenantAdminAuthorityNamePattern;

  private String tenantAuthenticatedAuthorityNamePattern;

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
  
  public FileResourceTest() throws Exception {
    super();
    this.setTestContainerFactory(new GrizzlyTestContainerFactory());
    mp.setFullyQualifiedServerUrl(getBaseURI() + webAppDescriptor.getContextPath() + "/");
  }

  protected AppDescriptor configure() {
    return webAppDescriptor;
  }
  
  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }
	
  @BeforeClass
  public static void beforeClass() throws Exception {
    System.setProperty(SYSTEM_PROPERTY, "MODE_GLOBAL");
    PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_GLOBAL);
    
    FileUtils.deleteDirectory(new File("/tmp/jackrabbit-test-TRUNK"));
	SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
  }

  @AfterClass
  public static void afterClass() {
    PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_GLOBAL);
    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
  }

  @Before
  public void beforeTest() {
	mp = new MicroPlatform();
	// used by DefaultPentahoJackrabbitAccessControlHelper
	mp.defineInstance(IAuthorizationPolicy.class, authorizationPolicy);
    mp.defineInstance(ITenantManager.class, tenantManager);
    mp.define(ITenant.class, Tenant.class);
    mp.defineInstance("roleAuthorizationPolicyRoleBindingDaoTarget", roleBindingDaoTarget);
    mp.defineInstance(IRoleAuthorizationPolicyRoleBindingDao.class, roleBindingDaoTarget);
    mp.defineInstance("tenantedUserNameUtils", tenantedUserNameUtils);
    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService(tenantedUserNameUtils, tenantedRoleNameUtils);
    userDetailsService.setUserRoleDao(userRoleDao);
    
    userRoleListService = new UserRoleDaoUserRoleListService(tenantedUserNameUtils, tenantedRoleNameUtils, userRoleDao, userDetailsService);
    ((UserRoleDaoUserRoleListService)userRoleListService).setUserRoleDao(userRoleDao);
    ((UserRoleDaoUserRoleListService)userRoleListService).setUserDetailsService(userDetailsService);

    mp.defineInstance(IUserRoleListService.class, userRoleListService);

	logout();
	startupCalled = true;
  }

  @After
  public void afterTest() throws Exception {
	clearRoleBindings();
	// null out fields to get back memory
	authorizationPolicy = null;
	loginAsRepositoryAdmin();
	SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath());
	logout();
	repositoryAdminUsername = null;
	tenantAdminAuthorityNamePattern = null;
	tenantAuthenticatedAuthorityNamePattern = null;
	roleBindingDao = null;
	authorizationPolicy = null;
	testJcrTemplate = null;
	if (startupCalled) {
		manager.shutdown();
	}

	// null out fields to get back memory
	repo = null;
  }
  
  protected void clearRoleBindings() throws Exception {
		loginAsRepositoryAdmin();
//		SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath("duff") + ".authz");
//		SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath("duff") + ".authz");
	}

  protected void createTestFile(String pathId, String text) {
    WebResource webResource = resource();
    ClientResponse response = webResource.path("repo/files/" + pathId).type(TEXT_PLAIN).put(ClientResponse.class, text);
    assertResponse(response, Status.OK);
  }

  protected void createTestFileBinary(String pathId, byte[] data) {
    WebResource webResource = resource();
    ClientResponse response = webResource.path("repo/files/" + pathId).type(APPLICATION_OCTET_STREAM).put(ClientResponse.class, data);
    assertResponse(response, Status.OK);
  }

  protected void createTestFolder(String pathId) {
    WebResource webResource = resource();
    // webResource.path("repo/dirs/" + pathId).put();
    ClientResponse response = webResource.path("repo/dirs/" + pathId).type(TEXT_PLAIN).put(ClientResponse.class);
    assertResponse(response, Status.OK);
  }

  @Test
  public void testWriteBinaryFile() throws InterruptedException {
    final String str = "some binary text";
    final String fileName = "file.bin";

    // set object in PentahoSystem
    mp.defineInstance(IUnifiedRepository.class, repo);
    
    loginAsRepositoryAdmin();
    ITenant systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    IPentahoUser systemTenantUser = userRoleDao.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminAuthorityNamePattern});

    login(sysAdminUserName, systemTenant, new String[]{tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern});

    ITenant mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    IPentahoUser tenantUser = userRoleDao.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminAuthorityNamePattern});
    login("joe", mainTenant_1, new String[]{tenantAuthenticatedAuthorityNamePattern});

    WebResource webResource = resource();
    final byte[] blob = str.getBytes();
    String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();
    createTestFileBinary(publicFolderPath.replaceAll("/", ":") + ":" + fileName, blob);

    // the file might not actually be ready.. wait a second
    //Thread.sleep(10000);

    ClientResponse response = webResource.path("repo/files/:public:file.bin").accept(APPLICATION_OCTET_STREAM).get(ClientResponse.class);
    assertResponse(response, Status.OK, APPLICATION_OCTET_STREAM);

    byte[] data = response.getEntity(byte[].class);
    assertEquals("contents of file incorrect/missing", str, new String(data));
    
    userRoleDao.deleteUser(tenantUser);
    userRoleDao.deleteUser(systemTenantUser);
    
  }

  @Test
  public void testWriteTextFile() throws Exception{
    final String text = "sometext";
    
    mp.defineInstance(IUnifiedRepository.class, repo);
    final String publicFolderId = "123";
    final String fileName = "file.txt";

    loginAsRepositoryAdmin();
    ITenant systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    IPentahoUser systemTenantUser = userRoleDao.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminAuthorityNamePattern});

    login(sysAdminUserName, systemTenant, new String[]{tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern});

    ITenant mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    IPentahoUser tenantUser = userRoleDao.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminAuthorityNamePattern});
    login("joe", mainTenant_1, new String[]{tenantAuthenticatedAuthorityNamePattern});
    
    WebResource webResource = resource();
    String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();
    createTestFile(publicFolderPath.replaceAll("/", ":") + ":" + fileName, text);

    ClientResponse response = webResource.path("repo/files/:public:" + fileName).accept(TEXT_PLAIN).get(ClientResponse.class);
    assertResponse(response, Status.OK, TEXT_PLAIN);
    assertEquals("contents of file incorrect/missing", text, response.getEntity(String.class));
    
    userRoleDao.deleteUser(tenantUser);
    userRoleDao.deleteUser(systemTenantUser);
  }

  @Test
  public void testGetFileText() throws Exception {
    final String text = "abcdefg";
    mp.defineInstance(IUnifiedRepository.class, repo);
    loginAsRepositoryAdmin();
    ITenant systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    IPentahoUser systemTenantUser = userRoleDao.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminAuthorityNamePattern});

    login(sysAdminUserName, systemTenant, new String[]{tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern});

    ITenant mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    IPentahoUser tenantUser = userRoleDao.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminAuthorityNamePattern});
    login("joe", mainTenant_1, new String[]{tenantAuthenticatedAuthorityNamePattern});
    
    final String publicFolderId = "123";
    final String fileName = "file.txt";
    
    String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();
    createTestFile(publicFolderPath.replaceAll("/", ":") + ":" + fileName, "abcdefg");
    WebResource webResource = resource();

    ClientResponse r1 = webResource.path("repo/files/:public:" + fileName).accept(TEXT_PLAIN).get(ClientResponse.class);
    assertResponse(r1, Status.OK, MediaType.TEXT_PLAIN);
    assertEquals(text, r1.getEntity(String.class));
    
    // check again but with no Accept header
    ClientResponse r2 = webResource.path("repo/files/:public:" + fileName).get(ClientResponse.class);
    assertResponse(r2, Status.OK, MediaType.TEXT_PLAIN);
    assertEquals(text, r2.getEntity(String.class));

    // check again but with */*
    ClientResponse r3 = webResource.path("repo/files/:public:" + fileName).accept(TEXT_PLAIN).accept(MediaType.WILDCARD).get(ClientResponse.class);
    assertResponse(r3, Status.OK, MediaType.TEXT_PLAIN);
    assertEquals(text, r3.getEntity(String.class));
    
    userRoleDao.deleteUser(tenantUser);
    userRoleDao.deleteUser(systemTenantUser);
  }

  @Test
  public void testCopyFiles() throws Exception {
    mp.defineInstance(IUnifiedRepository.class, repo);
    loginAsRepositoryAdmin();
    ITenant systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    IPentahoUser systemTenantUser = userRoleDao.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminAuthorityNamePattern});

    login(sysAdminUserName, systemTenant, new String[]{tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern});

    ITenant mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    IPentahoUser tenantUser = userRoleDao.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminAuthorityNamePattern});
    login("joe", mainTenant_1, new String[]{tenantAuthenticatedAuthorityNamePattern});
    
    final String srcFolderServerPath = "/public/folder1/folder2";
    final String destFolderPath = "public:folder3:folder4";
    final String destFolderServerPath = "/public/folder3/folder4";
    final String fileId = "456";
    final String fileName = "file.txt";
    final String destFolderId = "789";
    
    String publicFolderPath = ClientRepositoryPaths.getPublicFolderPath();
    createTestFile(publicFolderPath.replaceAll("/", ":") + ":" + fileName, "abcdefg");
    WebResource webResource = resource();
    RepositoryFile file = repo.getFile(ClientRepositoryPaths.getPublicFolderPath() + "/" + fileName);
    ClientResponse r = webResource.path("repo/files/" + destFolderPath + "/children").accept(TEXT_PLAIN).put(ClientResponse.class, file.getId());
    assertResponse(r, Status.OK);

    userRoleDao.deleteUser(tenantUser);
    userRoleDao.deleteUser(systemTenantUser);

    logout();
  }

  @Test
  public void testGetWhenFileDNE() {
    
    mp.defineInstance(IUnifiedRepository.class, repo);
    
    WebResource webResource = resource();

    ClientResponse r = webResource.path("repo/files/public:thisfiledoesnotexist.txt").accept(TEXT_PLAIN).get(ClientResponse.class);
    assertResponse(r, Status.NOT_FOUND);
  }

  @Test
  public void testBrowserDownload() {
    final String text = "abcdefg";
    // stub IUnifiedRepository start
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    final String fileId = "456";
    final String fileName = "file.txt";
    final String path = "/public/" + fileName;
    doReturn(new RepositoryFile.Builder(fileId, fileName).path(path).build()).when(repo).getFile(path);
    when(repo.getDataForRead(fileId, SimpleRepositoryFileData.class)).thenReturn(new SimpleRepositoryFileData(
        new ByteArrayInputStream(text.getBytes()), null, APPLICATION_OCTET_STREAM)).thenReturn(
            new SimpleRepositoryFileData(new ByteArrayInputStream(text.getBytes()), null, APPLICATION_OCTET_STREAM));
    // stub IUnifiedRepository end
    
    // set object in PentahoSystem
    mp.defineInstance(IUnifiedRepository.class, repo);

    // test download of file
    WebResource webResource = resource();
    ClientResponse r = webResource.path("repo/files/public:file.txt/download").get(ClientResponse.class);
    assertResponse(r, Status.OK);
    assertEquals("abcdefg", r.getEntity(String.class));

    // test download of dir as a zip file
    ClientResponse r2 = webResource.path("repo/files/public:file.txt/download").get(ClientResponse.class);
    assertResponse(r2, Status.OK);
    assertResponseIsZip(r2);
  }

  @Test
  public void testGetDirChildren() {
    loginAsRepositoryAdmin();
    ITenant systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    userRoleDao.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminAuthorityNamePattern});
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    userRoleDao.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminAuthorityNamePattern});
    login("joe", mainTenant_1, new String[]{tenantAuthenticatedAuthorityNamePattern});
    // stub IUnifiedRepository start
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    RepositoryFileTree tree = new RepositoryFileTree(new RepositoryFile.Builder("123", "public").build(), 
        Arrays.asList(new RepositoryFileTree(new RepositoryFile.Builder("123", "public").build(), new ArrayList<RepositoryFileTree>(0))));
    doReturn(tree).when(repo).getTree(eq(ClientRepositoryPaths.getPublicFolderPath()), anyInt(), anyString(), anyBoolean());
    // stub IUnifiedRepository end
    
    // set object in PentahoSystem
    mp.defineInstance(IUnifiedRepository.class, repo);

    WebResource webResource = resource();
    ClientResponse response = webResource.path("repo/files/public/children").accept(APPLICATION_XML).get(ClientResponse.class);

    assertResponse(response, Status.OK, APPLICATION_XML);

    // DOMSourceReader dom = response.getEntity(DOMSourceReader.class);
    String xml = response.getEntity(String.class);
    assertTrue(xml.startsWith("<?"));
    logout();
  }

  @Test
  public void testFileAcls() throws InterruptedException {
    loginAsRepositoryAdmin();
    ITenant systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    userRoleDao.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminAuthorityNamePattern});
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    userRoleDao.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminAuthorityNamePattern});
    login("joe", mainTenant_1, new String[]{tenantAuthenticatedAuthorityNamePattern});
    // stub IUnifiedRepository start
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    final String fileName = "aclFile.txt";
    doReturn(new RepositoryFile.Builder("abc", fileName).build()).when(repo).getFile(ClientRepositoryPaths.getPublicFolderPath() + RepositoryFile.SEPARATOR + fileName);
    doReturn(new RepositoryFileAcl.Builder("suzy").entriesInheriting(false).ace("suzy", RepositoryFileSid.Type.USER, RepositoryFilePermission.READ, RepositoryFilePermission.WRITE).build()).when(repo).getAcl("abc");
    RepositoryFileAcl acl = new RepositoryFileAcl.Builder("suzy").entriesInheriting(false).ace("suzy", 
        RepositoryFileSid.Type.USER, RepositoryFilePermission.READ_ACL).build();
    doReturn(acl).when(repo).updateAcl(argThat(isLikeAcl(acl, true)));
    // stub IUnifiedRepository end
    
    // set object in PentahoSystem
    mp.defineInstance(IUnifiedRepository.class, repo);

    WebResource webResource = resource();
    RepositoryFileAclDto fileAcls = webResource.path("repo/files/public:aclFile.txt/acl").accept(APPLICATION_XML).get(RepositoryFileAclDto.class);
    List<RepositoryFileAclAceDto> aces = fileAcls.getAces();
    assertEquals(1, aces.size());
    RepositoryFileAclAceDto ace = aces.get(0);
    assertEquals("suzy", ace.getRecipient());
    List<Integer> permissions = ace.getPermissions();
    assertEquals(2, permissions.size());
    Assert.assertTrue(permissions.contains(new Integer(0)) && permissions.contains(new Integer(1)));
    
    aces = new ArrayList<RepositoryFileAclAceDto>();
    ace = new RepositoryFileAclAceDto();
    ace.setRecipient("suzy");
    ace.setRecipientType(0);
    permissions = new ArrayList<Integer>();
    permissions.add(2);
    ace.setPermissions(permissions);
    aces.add(ace);
    fileAcls.setAces(aces);

    ClientResponse putResponse2 = webResource.path("repo/files/public:aclFile.txt/acl").type(APPLICATION_XML).put(ClientResponse.class, fileAcls);
    assertResponse(putResponse2, Status.OK);
    logout();
  }

  @Test
  public void testDeleteFiles() {
    String testFile1Id = "abc";
    String testFile2Id = "def";

    // stub IUnifiedRepository start
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    doReturn(Arrays.asList(new RepositoryFile.Builder(testFile1Id, "file1.txt").build(), new RepositoryFile.Builder(testFile2Id, "file2.txt").build())).when(repo).getDeletedFiles();
    // stub IUnifiedRepository end
    
    // set object in PentahoSystem
    mp.defineInstance(IUnifiedRepository.class, repo);

    
    WebResource webResource = resource();
    webResource.path("repo/files/delete").entity(testFile1Id + "," + testFile2Id).put();

    RepositoryFileDto[] deletedFiles = webResource.path("repo/files/deleted").accept(APPLICATION_XML).get(RepositoryFileDto[].class);
    assertEquals(2, deletedFiles.length);
    
    webResource.path("repo/files/deletepermanent").entity(testFile1Id).put();

    webResource.path("repo/files/restore").entity(testFile2Id).put();
    
    // verify IUnifiedRepository start
    verify(repo).deleteFile(eq(testFile1Id), anyString());
    verify(repo).deleteFile(eq(testFile1Id), eq(true), anyString());
    verify(repo).undeleteFile(eq(testFile2Id), anyString());
    // verify IUnifiedRepository end
  }

  @Test
  public void testFileCreator() {
    loginAsRepositoryAdmin();
    ITenant systemTenant = tenantManager.createTenant(null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    userRoleDao.createUser(systemTenant, sysAdminUserName, "password", "", new String[]{tenantAdminAuthorityNamePattern});
    ITenant mainTenant_1 = tenantManager.createTenant(systemTenant, MAIN_TENANT_1, tenantAdminAuthorityNamePattern, tenantAuthenticatedAuthorityNamePattern, "Anonymous");
    userRoleDao.createUser(mainTenant_1, "joe", "password", "", new String[]{tenantAdminAuthorityNamePattern});
    login("joe", mainTenant_1, new String[]{tenantAuthenticatedAuthorityNamePattern});
  	  
    // stub IUnifiedRepository start
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    doReturn(new RepositoryFile.Builder("456", "file1.txt").build()).when(repo).getFile("/public/file1.txt");
    doReturn(new RepositoryFile.Builder("789", "file2.txt").build()).when(repo).getFile("/public/file2.txt");
    doReturn(new HashMap<String, Serializable>()).when(repo).getFileMetadata("456");
    // stub IUnifiedRepository end
    
    // set object in PentahoSystem
    mp.defineInstance(IUnifiedRepository.class, repo);
    
    
    WebResource webResource = resource();
    RepositoryFileDto file2 = new RepositoryFileDto();
    file2.setId("789");
    webResource.path("repo/files/public:file1.txt/creator").entity(file2).put();
    
    // verify IUnifiedRepository start
    Map<String, Serializable> metadata = new HashMap<String, Serializable>();
    metadata.put("contentCreator", "789");
    verify(repo).setFileMetadata(eq("456"), eq(metadata));
    // verify IUnifiedRepository end
    logout();
  }

  @Test
  public void testUserWorkspace() {
    PentahoSessionHolder.setSession(new StandaloneSession("jerry"));
    WebResource webResource = resource();
    String userWorkspaceDir = webResource.path("session/userWorkspaceDir").accept(TEXT_PLAIN).get(String.class);
    assertTrue(userWorkspaceDir != null);
    assertTrue(userWorkspaceDir.length() > 0);
  }
  
  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
	manager = (IBackingRepositoryLifecycleManager) applicationContext.getBean("backingRepositoryLifecycleManager");
	SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean("jcrSessionFactory");
	testJcrTemplate = new JcrTemplate(jcrSessionFactory);
	testJcrTemplate.setAllowCreate(true);
	testJcrTemplate.setExposeNativeSession(true);
	repositoryAdminUsername = (String) applicationContext.getBean("repositoryAdminUsername");
	tenantAuthenticatedAuthorityNamePattern = (String) applicationContext.getBean("tenantAuthenticatedAuthorityNamePattern");
	tenantAdminAuthorityNamePattern = (String) applicationContext.getBean("tenantAdminAuthorityNamePattern");
    sysAdminAuthorityName = (String) applicationContext.getBean("superAdminAuthorityName");
    sysAdminUserName = (String) applicationContext.getBean("superAdminUserName");
	authorizationPolicy = (IAuthorizationPolicy) applicationContext.getBean("authorizationPolicy");
    roleBindingDaoTarget = (IRoleAuthorizationPolicyRoleBindingDao) applicationContext.getBean("roleAuthorizationPolicyRoleBindingDaoTarget");
    tenantManager = (ITenantManager) applicationContext.getBean("tenantMgrProxy");
    repositoryFileDao = (IRepositoryFileDao) applicationContext.getBean("repositoryFileDao");
    userRoleDao = (IUserRoleDao) applicationContext.getBean("userRoleDao");
    tenantedUserNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean("tenantedUserNameUtils");
    tenantedRoleNameUtils = (ITenantedPrincipleNameResolver) applicationContext.getBean("tenantedRoleNameUtils");
    jcrTransactionTemplate = (TransactionTemplate) applicationContext.getBean("jcrTransactionTemplate");
	repo = (IUnifiedRepository) applicationContext.getBean("unifiedRepository");
    TestPrincipalProvider.userRoleDao = (IUserRoleDao) applicationContext.getBean("userRoleDao");
    TestPrincipalProvider.adminCredentialsStrategy = (CredentialsStrategy) applicationContext.getBean("jcrAdminCredentialsStrategy");
    TestPrincipalProvider.repository = (Repository)applicationContext.getBean("jcrRepository");
 }

  protected void loginAsRepositoryAdmin() {
	StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
	pentahoSession.setAuthenticated(repositoryAdminUsername);
  final GrantedAuthority[] repositoryAdminAuthorities = new GrantedAuthority[]{new GrantedAuthorityImpl(sysAdminAuthorityName)};
	final String password = "ignored";
	UserDetails repositoryAdminUserDetails = new User(repositoryAdminUsername, password, true, true, true, true, repositoryAdminAuthorities);
	Authentication repositoryAdminAuthentication = new UsernamePasswordAuthenticationToken(repositoryAdminUserDetails, password, repositoryAdminAuthorities);
	PentahoSessionHolder.setSession(pentahoSession);
	// this line necessary for Spring Security's MethodSecurityInterceptor
	SecurityContextHolder.getContext().setAuthentication(repositoryAdminAuthentication);
  }

  protected void logout() {
	PentahoSessionHolder.removeSession();
	SecurityContextHolder.getContext().setAuthentication(null);
  }

  protected void login(final String username, final ITenant tenant) {
    login(username, tenant, false);
  }  

  /**
   * Logs in with given username.
   *
   * @param username username of user
   * @param tenantId tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  protected void login(final String username, final ITenant tenant, String[] roles) {
    StandaloneSession pentahoSession = new StandaloneSession(tenantedUserNameUtils.getPrincipleId(tenant, username));
    pentahoSession.setAuthenticated(tenant.getId(), tenantedUserNameUtils.getPrincipleId(tenant, username));
    PentahoSessionHolder.setSession(pentahoSession);
    pentahoSession.setAttribute(IPentahoSession.TENANT_ID_KEY, tenant.getId());
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();

    for (String roleName : roles) {
      authList.add(new GrantedAuthorityImpl(tenantedRoleNameUtils.getPrincipleId(tenant, roleName)));
    }
    GrantedAuthority[] authorities = authList.toArray(new GrantedAuthority[0]);
    UserDetails userDetails = new User(username, password, true, true, true, true, authorities);
    Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, password, authorities);
    PentahoSessionHolder.setSession(pentahoSession);
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication(auth);
    SecurityHelper.getInstance().becomeUser(tenantedUserNameUtils.getPrincipleId(tenant, username));
  }

  
  /**
   * Logs in with given username.
   *
   * @param username username of user
   * @param tenantId tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  protected void login(final String username, final ITenant tenant, final boolean tenantAdmin) {
    StandaloneSession pentahoSession = new StandaloneSession(username);
    pentahoSession.setAuthenticated(username);
    pentahoSession.setAttribute(IPentahoSession.TENANT_ID_KEY, tenant.getId());
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
    authList.add(new GrantedAuthorityImpl(tenantAuthenticatedAuthorityNamePattern));
    if (tenantAdmin) {
      authList.add(new GrantedAuthorityImpl(tenantAdminAuthorityNamePattern));
    }
    GrantedAuthority[] authorities = authList.toArray(new GrantedAuthority[0]);
    UserDetails userDetails = new User(username, password, true, true, true, true, authorities);
    Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, password, authorities);
    PentahoSessionHolder.setSession(pentahoSession);
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

}
