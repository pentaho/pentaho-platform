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
 */
package org.pentaho.platform.repository2.unified.webservices.jaxws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.hasData;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.isLikeAcl;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.isLikeFile;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFile.Builder;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode.DataPropertyType;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

import com.sun.xml.ws.developer.JAXWSProperties;

/**
 * Tests marshalling, unmarshalling, and {@code UnifiedRepositoryToWebServiceAdapter}. Do not test unified repository
 * logic in this class; just make sure args serialize back and forth correctly and that the adapter is translating to 
 * the right calls. 
 * 
 * @author mlowery
 */
@SuppressWarnings("nls")
public class DefaultUnifiedRepositoryJaxwsWebServiceTest {
  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private static final String USERNAME_SUZY = "suzy";

  /**
   * Server-side IUnifiedRepository.
   */
  private IUnifiedRepository _repo;

  /**
   * Client-side IUnifiedRepository.
   */
  private IUnifiedRepository repo;

  // ~ Constructors ==================================================================================================== 

  public DefaultUnifiedRepositoryJaxwsWebServiceTest() throws Exception {
    super();
  }

  // ~ Methods =========================================================================================================

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    _repo = mock(IUnifiedRepository.class);
    setUpMock(_repo);

    String address = "http://localhost:9000/repo";
    Endpoint.publish(address, new DefaultUnifiedRepositoryJaxwsWebService(_repo));

    Service service = Service.create(new URL("http://localhost:9000/repo?wsdl"), new QName(
        "http://www.pentaho.org/ws/1.0", "unifiedRepository"));

    IUnifiedRepositoryJaxwsWebService repoWebService = service.getPort(IUnifiedRepositoryJaxwsWebService.class);

    // next two lines not needed since we manually populate filters
    //((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "suzy");
    //((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "password");
    // accept cookies to maintain session on server
    ((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
    // support streaming binary data
    ((BindingProvider) repoWebService).getRequestContext().put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
    SOAPBinding binding = (SOAPBinding) ((BindingProvider) repoWebService).getBinding();
    binding.setMTOMEnabled(true);

    repo = new UnifiedRepositoryToWebServiceAdapter(repoWebService);

  }

  @After
  public void tearDown() throws Exception {
  }

  private static final String ENC = "UTF-8";

  private static final String MIME_PLAIN = "text/plain";

  private void setUpMock(IUnifiedRepository _repo2) throws Exception {
    String path = ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY);
    RepositoryFile suzyFolder = new Builder("suzyFolderId", "suzy").folder(true).path(path).createdDate(new Date())
        .build();
    doReturn(suzyFolder).when(_repo2).getFile(path);
    doReturn(suzyFolder).when(_repo2).getFileById(suzyFolder.getId());
    RepositoryFile folder1 = new Builder("folder1").folder(true).build();
    RepositoryFile folder1WithId = new Builder(folder1).id("folder1Id").build();
    doReturn(folder1WithId).when(_repo2)
        .createFolder(eq(suzyFolder.getId()), argThat(isLikeFile(folder1)), anyString());
    RepositoryFile file1 = new RepositoryFile.Builder("file1.whatever").versioned(true).build();
    RepositoryFile file1WithId = new RepositoryFile.Builder(file1).id("file1Id").build();
    doReturn(file1WithId).when(_repo2).createFile(eq(folder1WithId.getId()), argThat(isLikeFile(file1)),
        any(IRepositoryFileData.class), anyString());
    doReturn(makeNodeRepositoryFileData1()).when(_repo2).getDataForRead(file1WithId.getId(),
        NodeRepositoryFileData.class);
    RepositoryFile file2 = new RepositoryFile.Builder("file2.whatever").versioned(true).build();
    RepositoryFile file2WithId = new RepositoryFile.Builder(file2).id("file2Id").versionId("file2VersionId")
        .lastModificationDate(new Date()).build();
    final String helloWorldData = "Hello World!";
    doReturn(file2WithId).when(_repo2).createFile(eq(folder1WithId.getId()), argThat(isLikeFile(file2)),
        argThat(hasData(helloWorldData, ENC, "text/plain")), anyString());
    final String caioWorldData = "Ciao World!";
    doReturn(new Builder(file2WithId).lastModificationDate(new Date()).build()).when(_repo2).updateFile(
        argThat(isLikeFile(file2)), argThat(hasData(caioWorldData, ENC, "text/plain")), anyString());
    when(_repo2.getDataForRead(eq(file2WithId.getId()), eq(SimpleRepositoryFileData.class))).thenReturn(
        makeSimpleData(helloWorldData)).thenReturn(makeSimpleData(caioWorldData));
    doReturn(makeSimpleData(helloWorldData)).when(_repo2).getDataAtVersionForRead(eq(file2WithId.getId()),
        eq(file2WithId.getVersionId()), eq(SimpleRepositoryFileData.class));
    doReturn(Arrays.asList(new Builder("a").build(), new Builder("b").build(), new Builder("c").build())).when(_repo2)
        .getChildren(folder1WithId.getId());
    doReturn(Collections.emptyList()).when(_repo2).getChildren(folder1WithId.getId(), "*.sample");
    doNothing().when(_repo2).deleteFile(any(Serializable.class), anyString());
    doReturn(Collections.emptyList()).when(_repo2).getDeletedFiles(anyString(), eq("*.sample"));
    doReturn(false).when(_repo2).hasAccess("/pentaho", EnumSet.of(RepositoryFilePermission.WRITE));
    doReturn(Arrays.asList(new RepositoryFileAce(new RepositoryFileSid("joe"), RepositoryFilePermission.WRITE))).when(
        _repo2).getEffectiveAces(folder1WithId.getId());
    doReturn(new RepositoryFileAcl.Builder("suzy").build()).when(_repo2).getAcl(folder1WithId.getId());
    RepositoryFileAcl folder1Acl = new RepositoryFileAcl.Builder("suzy").entriesInheriting(false)
        .ace("suzy", RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL).build();
    doReturn(folder1Acl).when(_repo2).updateAcl(argThat(isLikeAcl(folder1Acl)));
    doReturn(true).when(_repo2).canUnlockFile(file1WithId.getId());
    VersionSummary v = new VersionSummary("1.0", file1WithId.getId(), new Date(), "suzy", "message",
        new ArrayList<String>());
    doReturn(Arrays.asList(v, v, v)).when(_repo2).getVersionSummaries(file1WithId.getId());
    doReturn(v).when(_repo2).getVersionSummary(file1WithId.getId(), null);
    RepositoryFile file1WithVersionId = new RepositoryFile.Builder(file1WithId).versionId("1.0").build();
    doReturn(file1WithVersionId).when(_repo2).getFileAtVersion(file1WithId.getId(), v.getId());
    doReturn(new RepositoryFileTree(file1WithId, new ArrayList<RepositoryFileTree>())).when(_repo2).getTree(
        ClientRepositoryPaths.getRootFolderPath(), -1, null, true);
    doReturn(Arrays.asList(makeNodeRepositoryFileData1(), makeNodeRepositoryFileData1())).when(_repo2)
        .getDataForReadInBatch(anyListOf(RepositoryFile.class), eq(NodeRepositoryFileData.class));
    doReturn(makeNodeRepositoryFileData1()).when(_repo2).getDataAtVersionForRead(any(Serializable.class), any(Serializable.class),
        eq(NodeRepositoryFileData.class));
    doReturn(Arrays.asList(v, v)).when(_repo2).getVersionSummaryInBatch(anyListOf(RepositoryFile.class));
    doReturn(v).when(_repo2).getVersionSummary(any(Serializable.class), any(Serializable.class));
  }

  @Test
  public void testEverything() throws Exception {
    System.out.println("getFile");
    RepositoryFile f = repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY));
    assertNotNull(f.getId());
    assertEquals(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY), f.getPath());
    assertNotNull(f.getCreatedDate());
    assertEquals("suzy", f.getName());
    assertTrue(f.isFolder());

    System.out.println("getFileById");
    assertNotNull(repo.getFileById(f.getId()));

    System.out.println("createFolder");
    RepositoryFile folder1 = repo.createFolder(f.getId(), new RepositoryFile.Builder("folder1").folder(true).build(),
        null);
    assertNotNull(folder1);
    assertEquals("folder1", folder1.getName());
    assertNotNull(folder1.getId());

    NodeRepositoryFileData data = makeNodeRepositoryFileData1();
    System.out.println("createFile");
    RepositoryFile file1 = repo.createFile(folder1.getId(), new RepositoryFile.Builder("file1.whatever")
        .versioned(true).build(), data, null);
    assertNotNull(file1);
    assertNotNull(file1.getId());

    System.out.println("getDataForRead");
    NodeRepositoryFileData file1Data = repo.getDataForRead(file1.getId(), NodeRepositoryFileData.class);
    assertNotNull(file1Data);
    assertEquals("testNode", file1Data.getNode().getName());
    assertEquals("hello world", file1Data.getNode().getProperty("prop1").getString());
    assertEquals(false, file1Data.getNode().getProperty("prop2").getBoolean());
    assertEquals(DataPropertyType.BOOLEAN, file1Data.getNode().getProperty("prop2").getType());
    assertEquals(12L, file1Data.getNode().getProperty("prop3").getLong());

    System.out.println("createFile (binary)");
    SimpleRepositoryFileData simpleData = new SimpleRepositoryFileData(new ByteArrayInputStream(
        "Hello World!".getBytes("UTF-8")), "UTF-8", "text/plain");
    RepositoryFile simpleFile = repo.createFile(folder1.getId(), new RepositoryFile.Builder("file2.whatever")
        .versioned(true).build(), simpleData, null);

    Serializable simpleVersion = simpleFile.getVersionId();

    System.out.println("getDataForRead (binary)");
    SimpleRepositoryFileData simpleFileData = repo.getDataForRead(simpleFile.getId(), SimpleRepositoryFileData.class);
    assertNotNull(simpleFileData);
    assertEquals("Hello World!", IOUtils.toString(simpleFileData.getStream(), simpleFileData.getEncoding()));
    assertEquals("text/plain", simpleFileData.getMimeType());
    assertEquals("UTF-8", simpleFileData.getEncoding());

    System.out.println("updateFile (binary)");
    simpleData = new SimpleRepositoryFileData(new ByteArrayInputStream("Ciao World!".getBytes("UTF-8")), "UTF-8",
        "text/plain");
    simpleFile = repo.updateFile(simpleFile, simpleData, null);
    assertNotNull(simpleFile.getLastModifiedDate());

    System.out.println("getDataForRead (binary)");
    simpleFileData = repo.getDataForRead(simpleFile.getId(), SimpleRepositoryFileData.class);
    assertNotNull(simpleFileData);
    assertEquals("Ciao World!", IOUtils.toString(simpleFileData.getStream(), simpleFileData.getEncoding()));

    System.out.println("getDataForReadAtVersion (binary)");
    simpleFileData = repo.getDataAtVersionForRead(simpleFile.getId(), simpleVersion, SimpleRepositoryFileData.class);
    assertNotNull(simpleFileData);
    assertEquals("Hello World!", IOUtils.toString(simpleFileData.getStream(), simpleFileData.getEncoding()));

    System.out.println("getChildren");
    List<RepositoryFile> folder1Children = repo.getChildren(folder1.getId());
    assertNotNull(folder1Children);
    assertEquals(3, folder1Children.size());
    System.out.println("getChildren");
    List<RepositoryFile> folder1ChildrenFiltered = repo.getChildren(folder1.getId(), "*.sample");
    assertNotNull(folder1ChildrenFiltered);
    assertEquals(0, folder1ChildrenFiltered.size());
    System.out.println("getDeletedFiles");
    assertEquals(0, repo.getDeletedFiles().size());
    System.out.println("deleteFile");
    repo.deleteFile(file1.getId(), null);
    System.out.println("getDeletedFiles");
    assertEquals(0, repo.getDeletedFiles(folder1.getPath(), "*.sample").size());

    System.out.println("hasAccess");
    assertFalse(repo.hasAccess("/pentaho", EnumSet.of(RepositoryFilePermission.WRITE)));

    System.out.println("getEffectiveAces");
    List<RepositoryFileAce> folder1EffectiveAces = repo.getEffectiveAces(folder1.getId());
    assertEquals(1, folder1EffectiveAces.size());

    System.out.println("getAcl");
    RepositoryFileAcl folder1Acl = repo.getAcl(folder1.getId());
    assertEquals("suzy", folder1Acl.getOwner().getName());

    System.out.println("updateAcl");
    RepositoryFileAcl updatedFolder1Acl = repo.updateAcl(new RepositoryFileAcl.Builder(folder1Acl)
        .entriesInheriting(false).ace("suzy", RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL).build());
    assertNotNull(updatedFolder1Acl);
    assertEquals(1, updatedFolder1Acl.getAces().size());

    System.out.println("lockFile");
    assertFalse(file1.isLocked());
    repo.lockFile(file1.getId(), "I locked this file");
    System.out.println("canUnlockFile");
    assertTrue(repo.canUnlockFile(file1.getId()));
    System.out.println("unlockFile");
    repo.unlockFile(file1.getId());

    System.out.println("moveFile");
    repo.moveFile(file1.getId(), ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/folder1", null);
    System.out.println("copyFile");
    repo.copyFile(file1.getId(),
        ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/folder1/fileB.whatever", null);

    System.out.println("getVersionSummaries");
    List<VersionSummary> versionSummaries = repo.getVersionSummaries(file1.getId());
    assertNotNull(versionSummaries);
    assertTrue(versionSummaries.size() >= 2);
    assertEquals("suzy", versionSummaries.get(0).getAuthor());

    System.out.println("getVersionSummary");
    VersionSummary versionSummary = repo.getVersionSummary(file1.getId(), null);
    assertNotNull(versionSummary);
    assertNotNull(versionSummary.getId());

    System.out.println("getFileAtVersion");
    RepositoryFile file1AtVersion = repo.getFileAtVersion(file1.getId(), versionSummary.getId());
    assertNotNull(file1AtVersion);
    assertEquals(versionSummary.getId(), file1AtVersion.getVersionId());

    System.out.println("getTree");
    RepositoryFileTree tree = repo.getTree(ClientRepositoryPaths.getRootFolderPath(), -1, null, true);
    assertNotNull(tree.getFile().getId());

    System.out.println("getDataForReadInBatch");
    List<NodeRepositoryFileData> result = repo.getDataForReadInBatch(Arrays.asList(file1, simpleFile),
        NodeRepositoryFileData.class);
    assertEquals(2, result.size());

    System.out.println("getVersionSummaryInBatch");
    List<VersionSummary> vResult = repo.getVersionSummaryInBatch(Arrays.asList(file1, simpleFile));
    assertEquals(2, result.size());
  }

  private NodeRepositoryFileData makeNodeRepositoryFileData1() {
    DataNode node = new DataNode("testNode");
    node.setProperty("prop1", "hello world");
    node.setProperty("prop2", false);
    node.setProperty("prop3", 12L);
    return new NodeRepositoryFileData(node);
  }

  private SimpleRepositoryFileData makeSimpleData(final String text) throws Exception {
    return new SimpleRepositoryFileData(new ByteArrayInputStream(text.getBytes(ENC)), ENC, MIME_PLAIN);
  }

}
