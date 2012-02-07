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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.GrizzlyTestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;

@SuppressWarnings("nls")
public class FileResourceTest extends JerseyTest {

  private static MicroPlatform mp = new MicroPlatform();

  private static WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder("org.pentaho.platform.web.http.api.resources").contextPath("api").build();

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
    PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_GLOBAL);
  }

  @AfterClass
  public static void afterClass() {
    PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL);
  }

  @Before
  public void beforeTest() {
  }

  @After
  public void afterTest() {
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
    
    // stub IUnifiedRepository start
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    final String publicFolderId = "123";
    final String fileName = "file.bin";
    // stub getting /public folder
    doReturn(new RepositoryFile.Builder(publicFolderId, "public").folder(true).build()).when(repo)
      .getFile(ClientRepositoryPaths.getPublicFolderPath());
    RepositoryFile f = new RepositoryFile.Builder(fileName).build();
    final String path = ClientRepositoryPaths.getPublicFolderPath() + RepositoryFile.SEPARATOR + fileName;
    final String fileId = "456";
    RepositoryFile fWithId = new RepositoryFile.Builder(f).id(fileId).path(path).build();
    // stub getting file; first return null (as if file does not exist), then return non-null (as if file exists)
    when(repo.getFile(path)).thenReturn(null).thenReturn(fWithId);
    // stub getting file data
    SimpleRepositoryFileData data1 = new SimpleRepositoryFileData(new ByteArrayInputStream(str.getBytes()), null, 
        APPLICATION_OCTET_STREAM);
    doReturn(data1).when(repo).getDataForRead(fileId, SimpleRepositoryFileData.class);
    // stub IUnifiedRepository end

    // set object in PentahoSystem
    mp.defineInstance(IUnifiedRepository.class, repo);
    
    WebResource webResource = resource();
    final byte[] blob = str.getBytes();
    createTestFileBinary("public:" + fileName, blob);

    // the file might not actually be ready.. wait a second
    //Thread.sleep(10000);

    ClientResponse response = webResource.path("repo/files/public:file.bin").accept(APPLICATION_OCTET_STREAM).get(ClientResponse.class);
    assertResponse(response, Status.OK, APPLICATION_OCTET_STREAM);

    byte[] data = response.getEntity(byte[].class);
    assertEquals("contents of file incorrect/missing", str, new String(data));
    
    // verify IUnifiedRepository start
    verify(repo).createFile(eq(publicFolderId), argThat(isLikeFile(new RepositoryFile.Builder(fileName).build())), argThat(hasData(blob, APPLICATION_OCTET_STREAM)), anyString());
    // verify IUnifiedRepository end
  }

  @Test
  public void testWriteTextFile() throws Exception{
    final String text = "sometext";
    
    // stub IUnifiedRepository start
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    final String publicFolderId = "123";
    final String fileName = "file.txt";
    // stub getting /public folder
    doReturn(new RepositoryFile.Builder(publicFolderId, "public").folder(true).build()).when(repo)
      .getFile(ClientRepositoryPaths.getPublicFolderPath());
    RepositoryFile f = new RepositoryFile.Builder(fileName).build();
    final String path = ClientRepositoryPaths.getPublicFolderPath() + RepositoryFile.SEPARATOR + fileName;
    final String fileId = "456";
    RepositoryFile fWithId = new RepositoryFile.Builder(f).id(fileId).path(path).build();
    // stub getting file; first return null (as if file does not exist), then return non-null (as if file exists)
    when(repo.getFile(path)).thenReturn(null).thenReturn(fWithId);
    // stub getting file data
    SimpleRepositoryFileData data1 = new SimpleRepositoryFileData(new ByteArrayInputStream(text.getBytes("UTF-8")), "UTF-8", 
        TEXT_PLAIN);
    doReturn(data1).when(repo).getDataForRead(fileId, SimpleRepositoryFileData.class);
    // stub IUnifiedRepository end

    // set object in PentahoSystem
    mp.defineInstance(IUnifiedRepository.class, repo);
    
    WebResource webResource = resource();
    createTestFile("public:" + fileName, text);

    ClientResponse response = webResource.path("repo/files/public:" + fileName).accept(TEXT_PLAIN).get(ClientResponse.class);
    assertResponse(response, Status.OK, TEXT_PLAIN);
    assertEquals("contents of file incorrect/missing", text, response.getEntity(String.class));
    
    // verify IUnifiedRepository start
    verify(repo).createFile(eq(publicFolderId), argThat(isLikeFile(new RepositoryFile.Builder(fileName).build())), argThat(hasData(text.getBytes(), TEXT_PLAIN)), anyString());
    // verify IUnifiedRepository end
  }

  @Test
  public void testGetFileText() throws Exception {
    final String text = "abcdefg";
    
    // stub IUnifiedRepository start
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    final String publicFolderId = "123";
    final String fileName = "file.txt";
    // stub getting /public folder
    doReturn(new RepositoryFile.Builder(publicFolderId, "public").folder(true).build()).when(repo)
      .getFile(ClientRepositoryPaths.getPublicFolderPath());
    RepositoryFile f = new RepositoryFile.Builder(fileName).build();
    final String path = ClientRepositoryPaths.getPublicFolderPath() + RepositoryFile.SEPARATOR + fileName;
    final String fileId = "456";
    RepositoryFile fWithId = new RepositoryFile.Builder(f).id(fileId).path(path).build();
    // stub getting file; first return null (as if file does not exist), then return non-null (as if file exists)
    when(repo.getFile(path)).thenReturn(null).thenReturn(fWithId);
    // stub getting file data (3 calls to getDataForRead); can't reuse same data as stream needs to be reset
    when(repo.getDataForRead(fileId, SimpleRepositoryFileData.class)).thenReturn(new SimpleRepositoryFileData(new ByteArrayInputStream(text.getBytes("UTF-8")), "UTF-8", 
        TEXT_PLAIN)).thenReturn(new SimpleRepositoryFileData(new ByteArrayInputStream(text.getBytes("UTF-8")), "UTF-8", 
        TEXT_PLAIN)).thenReturn(new SimpleRepositoryFileData(new ByteArrayInputStream(text.getBytes("UTF-8")), "UTF-8", 
            TEXT_PLAIN));
    // stub IUnifiedRepository end

    // set object in PentahoSystem
    mp.defineInstance(IUnifiedRepository.class, repo);
    
    createTestFile("public:" + fileName, "abcdefg");
    WebResource webResource = resource();

    ClientResponse r1 = webResource.path("repo/files/public:" + fileName).accept(TEXT_PLAIN).get(ClientResponse.class);
    assertResponse(r1, Status.OK, MediaType.TEXT_PLAIN);
    assertEquals(text, r1.getEntity(String.class));
    
    // check again but with no Accept header
    ClientResponse r2 = webResource.path("repo/files/public:" + fileName).get(ClientResponse.class);
    assertResponse(r2, Status.OK, MediaType.TEXT_PLAIN);
    assertEquals(text, r2.getEntity(String.class));

    // check again but with */*
    ClientResponse r3 = webResource.path("repo/files/public:" + fileName).accept(TEXT_PLAIN).accept(MediaType.WILDCARD).get(ClientResponse.class);
    assertResponse(r3, Status.OK, MediaType.TEXT_PLAIN);
    assertEquals(text, r3.getEntity(String.class));
  }

  @Test
  public void testCopyFiles() throws Exception {
    final String srcFolderServerPath = "/public/folder1/folder2";
    final String destFolderPath = "public:folder3:folder4";
    final String destFolderServerPath = "/public/folder3/folder4";
    final String fileId = "456";
    final String fileName = "file.txt";
    final String destFolderId = "789";
    
    // stub IUnifiedRepository start
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    doReturn(new RepositoryFile.Builder(destFolderId, "folder4").folder(true).build()).when(repo).getFile(destFolderServerPath);
    final String srcFilePath = srcFolderServerPath + RepositoryFile.SEPARATOR + fileName;
    final RepositoryFile srcFile = new RepositoryFile.Builder(fileId, fileName).path(srcFilePath).build();
    doReturn(srcFile).when(repo).getFileById(fileId);
    doReturn(new SimpleRepositoryFileData(new ByteArrayInputStream("hello".getBytes()), null, 
        APPLICATION_OCTET_STREAM)).when(repo).getDataForRead(fileId, SimpleRepositoryFileData.class);
    doReturn(new RepositoryFileAcl.Builder("joe").build()).when(repo).getAcl(fileId);
    doReturn(null).when(repo).getFile(destFolderServerPath + RepositoryFile.SEPARATOR + fileName);
    // stub IUnifiedRepository end
    
    // set object in PentahoSystem
    mp.defineInstance(IUnifiedRepository.class, repo);

    
    
    WebResource webResource = resource();
    
    ClientResponse r = webResource.path("repo/files/" + destFolderPath + "/children").accept(TEXT_PLAIN).put(ClientResponse.class, fileId);
    assertResponse(r, Status.OK);

    // verify IUnifiedRepository start
    verify(repo).createFile(eq(destFolderId), argThat(isLikeFile(new RepositoryFile.Builder(fileName).build())), any(IRepositoryFileData.class), any(RepositoryFileAcl.class), anyString());
    // verify IUnifiedRepository end
  }

  @Test
  public void testGetWhenFileDNE() {
    // stub IUnifiedRepository start
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    doReturn(null).when(repo).getFile("/public/thisfiledoesnotexist.txt");
    // stub IUnifiedRepository end
    
    // set object in PentahoSystem
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
  }

  @Test
  public void testFileAcls() throws InterruptedException {
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
  }

  @Test
  public void testUserWorkspace() {
    PentahoSessionHolder.setSession(new StandaloneSession("jerry"));
    WebResource webResource = resource();
    String userWorkspaceDir = webResource.path("session/userWorkspaceDir").accept(TEXT_PLAIN).get(String.class);
    assertTrue(userWorkspaceDir != null);
    assertTrue(userWorkspaceDir.length() > 0);
  }
}
