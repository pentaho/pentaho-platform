package org.pentaho.test.platform.web.http.api;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.pentaho.test.platform.web.http.api.JerseyTestUtil.assertResponse;
import static org.pentaho.test.platform.web.http.api.JerseyTestUtil.assertResponseIsZip;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.grizzly.GrizzlyTestContainerFactory;

@SuppressWarnings("nls")
public class FileResourceTest extends JerseyTest {
  public static final String USERNAME_JOE = "joe";
  public static final String TENANT_ID_ACME = "acme";

  private static MicroPlatform mp = new MicroPlatform();

  private static MicroPlatform.RepositoryModule repo;

  private static WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder("org.pentaho.platform.web.http.api.resources").contextPath("api").build();

  public FileResourceTest() throws Exception {
    super(webAppDescriptor);
    this.setTestContainerFactory(new GrizzlyTestContainerFactory());
    mp.setFullyQualifiedServerUrl(getBaseURI() + webAppDescriptor.getContextPath() + "/");
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    repo = mp.getRepositoryModule();
    repo.up();
  }

  @AfterClass
  public static void afterClass() {
    repo.down();
  }

  @Before
  public void beforeTest() {
    repo.login(USERNAME_JOE, TENANT_ID_ACME);
  }

  @After
  public void afterTest() {
    repo.logout();
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
    WebResource webResource = resource();
    final byte[] blob = "some binary text".getBytes();
    createTestFileBinary("public:file.bin", blob);

    // the file might not actually be ready.. wait a second
    Thread.sleep(10000);

    ClientResponse response = webResource.path("repo/files/public:file.bin").accept(APPLICATION_OCTET_STREAM).get(ClientResponse.class);
    assertResponse(response, Status.OK, APPLICATION_OCTET_STREAM);

    byte[] data = response.getEntity(byte[].class);
    assertEquals("contents of file incorrect/missing", "some binary text", new String(data));
  }

  @Test
  public void testWriteTextFile() {
    WebResource webResource = resource();
    final String text = "sometext";
    createTestFile("public:file.txt", text);

    ClientResponse response = webResource.path("repo/files/public:file.txt").accept(TEXT_PLAIN).get(ClientResponse.class);
    assertResponse(response, Status.OK, TEXT_PLAIN);
    assertEquals("contents of file incorrect/missing", text, response.getEntity(String.class));
  }

  @Test
  public void testGetFileText() {
    createTestFile("public:file.txt", "abcdefg");
    WebResource webResource = resource();

    ClientResponse r1 = webResource.path("repo/files/public:file.txt").accept(TEXT_PLAIN).get(ClientResponse.class);
    assertResponse(r1, Status.OK, MediaType.TEXT_PLAIN);
    assertEquals("abcdefg", r1.getEntity(String.class));

    // check again but with no Accept header
    ClientResponse r2 = webResource.path("repo/files/public:file.txt").get(ClientResponse.class);
    assertResponse(r2, Status.OK, MediaType.TEXT_PLAIN);
    assertEquals("abcdefg", r2.getEntity(String.class));

    // check again but with */*
    ClientResponse r3 = webResource.path("repo/files/public:file.txt").accept(TEXT_PLAIN).accept(MediaType.WILDCARD).get(ClientResponse.class);
    assertResponse(r3, Status.OK, MediaType.TEXT_PLAIN);
    assertEquals("abcdefg", r3.getEntity(String.class));
  }

  @Test
  public void testCopyFiles() {
    WebResource webResource = resource();
    final String srcFolderPath = "public:folder1:folder2";
    final String destFolderPath = "public:folder3:folder4";
    createTestFolder(srcFolderPath);
    createTestFolder(destFolderPath);
    String[] srcFileIdsArray = new String[4];
    for (int i = 0; i < 4; i++) {
      String filePath = srcFolderPath + ":file" + i + ".txt";
      createTestFile(filePath, "abcdefghijklmnopqrstuvwxyz");
      srcFileIdsArray[i] = webResource.path("repo/files/" + filePath + "/properties").accept(APPLICATION_XML).get(RepositoryFileDto.class).getId();
    }
    String srcFiles = "";
    for (int i = 0; i < srcFileIdsArray.length; i++) {
      srcFiles += srcFileIdsArray[i] + ",";
    }
    srcFiles = srcFiles.substring(0, srcFiles.length() - 1);

    ClientResponse r = webResource.path("repo/files/" + destFolderPath + "/children").accept(TEXT_PLAIN).put(ClientResponse.class, srcFiles);
    assertResponse(r, Status.OK);

    RepositoryFileTreeDto tree = webResource.path("repo/files/" + destFolderPath + "/children").accept(APPLICATION_XML).get(RepositoryFileTreeDto.class);
    assertEquals(tree.getChildren().size(), 4);
  }

  @Test
  public void testGetWhenFileDNE() {
    WebResource webResource = resource();

    ClientResponse r = webResource.path("repo/files/public:thisfiledoesnotexist.txt").accept(TEXT_PLAIN).get(ClientResponse.class);
    assertResponse(r, Status.NOT_FOUND);
  }

  @Test
  public void testBrowserDownload() {
    createTestFile("public:file.txt", "abcdefg");

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
    createTestFile("public:file.txt", "abcdefg");

    WebResource webResource = resource();
    ClientResponse response = webResource.path("repo/files/public/children").accept(APPLICATION_XML).get(ClientResponse.class);

    assertResponse(response, Status.OK, APPLICATION_XML);

    // DOMSourceReader dom = response.getEntity(DOMSourceReader.class);
    String xml = response.getEntity(String.class);
    assertTrue(xml.startsWith("<?"));
  }

  @Test
  public void testFileAcls() throws InterruptedException {
    WebResource webResource = resource();
    final String text = "sometext";
    createTestFile("public:aclFile.txt", text);

    RepositoryFileAclDto fileAcls = webResource.path("repo/files/public:aclFile.txt/acl").accept(APPLICATION_XML).get(RepositoryFileAclDto.class);
    fileAcls.setEntriesInheriting(false);

    List<RepositoryFileAclAceDto> aces = new ArrayList<RepositoryFileAclAceDto>();
    RepositoryFileAclAceDto ace = new RepositoryFileAclAceDto();
    ace.setRecipient("suzy");
    ace.setRecipientType(0);
    List<Integer> permissions = new ArrayList<Integer>();
    permissions.add(0);
    permissions.add(1);
    ace.setPermissions(permissions);
    aces.add(ace);
    fileAcls.setAces(aces);

    ClientResponse putResponse2 = webResource.path("repo/files/public:aclFile.txt/acl").type(APPLICATION_XML).put(ClientResponse.class, fileAcls);
    assertResponse(putResponse2, Status.OK);

    fileAcls = null;
    fileAcls = webResource.path("repo/files/public:aclFile.txt/acl").accept(APPLICATION_XML).get(RepositoryFileAclDto.class);
    aces = fileAcls.getAces();
    assertEquals(1, aces.size());
    ace = aces.get(0);
    assertEquals("suzy", ace.getRecipient());
    permissions = ace.getPermissions();
    assertEquals(2, permissions.size());
    Assert.assertTrue(permissions.contains(new Integer(0)) && permissions.contains(new Integer(1)));
  }

  @Test
  public void testDeleteFiles() {
    createTestFile("public:file1.txt", "abcdefg");
    createTestFile("public:file2.txt", "hijklmn");

    WebResource webResource = resource();
    RepositoryFileDto testFile1 = webResource.path("repo/files/public:file1.txt/properties").accept(APPLICATION_XML).get(RepositoryFileDto.class);
    RepositoryFileDto testFile2 = webResource.path("repo/files/public:file2.txt/properties").accept(APPLICATION_XML).get(RepositoryFileDto.class);

    assertTrue(testFile1 != null);
    assertTrue(testFile2 != null);

    String testFile1Id = testFile1.getId();
    String testFile2Id = testFile2.getId();
    
    webResource.path("repo/files/delete").entity(testFile1Id + "," + testFile2Id).put();
    testFile1 = null;
    testFile2 = null;
    try {
      testFile1 = webResource.path("repo/files/public:file1.txt/properties").accept(APPLICATION_XML).get(RepositoryFileDto.class);
      testFile2 = webResource.path("repo/files/public:file2.txt/properties").accept(APPLICATION_XML).get(RepositoryFileDto.class);
    } catch (UniformInterfaceException UIE) {
      assertEquals(UIE.getResponse().getStatus(), 204);
    }

    assertTrue(testFile1 == null);
    assertTrue(testFile2 == null);

    RepositoryFileDto[] deletedFiles = webResource.path("repo/files/deleted").accept(APPLICATION_XML).get(RepositoryFileDto[].class);
    assertEquals(2, deletedFiles.length);
    
    webResource.path("repo/files/deletepermanent").entity(testFile1Id).put();
    
    deletedFiles = webResource.path("repo/files/deleted").accept(APPLICATION_XML).get(RepositoryFileDto[].class);
    assertEquals(1, deletedFiles.length);
    
    webResource.path("repo/files/restore").entity(testFile2Id).put();

    deletedFiles = webResource.path("repo/files/deleted").accept(APPLICATION_XML).get(RepositoryFileDto[].class);
    assertEquals(0, deletedFiles.length);
    
    testFile2 = null;
    testFile2 = webResource.path("repo/files/public:file2.txt/properties").accept(APPLICATION_XML).get(RepositoryFileDto.class);
    assertTrue(testFile2 != null);
  }

  @Test
  public void testFileCreator() {
    createTestFile("public:file1.txt", "abcdefg");
    createTestFile("public:file2.txt", "hijklmn");

    WebResource webResource = resource();
    RepositoryFileDto creatorFile = webResource.path("repo/files/public:file2.txt/properties").accept(APPLICATION_XML).get(RepositoryFileDto.class);

    assertTrue(creatorFile != null);

    webResource.path("repo/files/public:file1.txt/creator").entity(creatorFile).put();
    RepositoryFileDto creator = webResource.path("repo/files/public:file1.txt/creator").accept(APPLICATION_XML).get(RepositoryFileDto.class);
    assertEquals(creatorFile.getId(), creator.getId());
  }

  @Test
  public void testUserWorkspace() {
    WebResource webResource = resource();
    String userWorkspaceDir = webResource.path("session/userWorkspaceDir").accept(TEXT_PLAIN).get(String.class);
    assertTrue(userWorkspaceDir != null);
    assertTrue(userWorkspaceDir.length() > 0);
  }
}
