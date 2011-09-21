package org.pentaho.platform.repository2.unified.webservices.jaxws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode.DataPropertyType;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

public class TestUtils {
  private static final String USERNAME_SUZY = "suzy"; //$NON-NLS-1$
  
  @SuppressWarnings("nls")
  public static void testEverything(final IUnifiedRepository repo) throws Exception {
    System.out.println("getFile");
    RepositoryFile f0 = repo.getFile(ClientRepositoryPaths.getPublicFolderPath());
    assertNotNull(f0);
    assertEquals("public", f0.getName());
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
    
    DataNode nodeReferee = new DataNode("refereeNode");
    nodeReferee.setProperty("prop1", "string");
    
    NodeRepositoryFileData dataReferee = new NodeRepositoryFileData(nodeReferee);
    System.out.println("createFile");
    RepositoryFile fileReferee = repo.createFile(folder1.getId(), new RepositoryFile.Builder("referee.file")
    .versioned(true).build(), dataReferee, null);
    assertNotNull(fileReferee);
    assertNotNull(fileReferee.getId());
    
    DataNode node = new DataNode("testNode");
    node.setProperty("prop1", "hello world");
    node.setProperty("prop2", false);
    node.setProperty("prop3", 12L);
    node.setProperty("prop4", new DataNodeRef(fileReferee.getId()));
    
    NodeRepositoryFileData data = new NodeRepositoryFileData(node);
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
    assertEquals(DataPropertyType.REF, file1Data.getNode().getProperty("prop4").getType());

    System.out.println("createFile (binary)");
    SimpleRepositoryFileData simpleData = new SimpleRepositoryFileData(new ByteArrayInputStream("Hello World!"
        .getBytes("UTF-8")), "UTF-8", "text/plain");
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
    System.out.println("getChildren");
    List<RepositoryFile> folder1ChildrenFiltered2 = repo.getChildren(folder1.getId(), "*.whatever");
    assertNotNull(folder1ChildrenFiltered2);
    assertEquals(2, folder1ChildrenFiltered2.size());
    System.out.println("getDeletedFiles");
    assertEquals(0, repo.getDeletedFiles().size());
    System.out.println("deleteFile");
    repo.deleteFile(file1.getId(), null);
    System.out.println("getDeletedFiles");
    assertEquals(1, repo.getDeletedFiles().size());
    System.out.println("getDeletedFiles");
    assertEquals(1, repo.getDeletedFiles(folder1.getPath()).size());
    System.out.println("getDeletedFiles");
    assertEquals(0, repo.getDeletedFiles(folder1.getPath(), "*.sample").size());
    System.out.println("getDeletedFiles");
    assertEquals(1, repo.getDeletedFiles(folder1.getPath(), "*.whatever").size());
    System.out.println("deleteFile");
    repo.undeleteFile(file1.getId(), null);
    System.out.println("getDeletedFiles");
    assertEquals(0, repo.getDeletedFiles().size());
    System.out.println("getDeletedFiles");
    assertEquals(0, repo.getDeletedFiles(folder1.getPath()).size());
    System.out.println("getDeletedFiles");
    assertEquals(0, repo.getDeletedFiles(folder1.getPath(), "*.whatever").size());
    System.out.println("hasAccess");
    assertFalse(repo.hasAccess("/pentaho", EnumSet.of(RepositoryFilePermission.WRITE)));
    System.out.println("getEffectiveAces");
    List<RepositoryFileAce> folder1EffectiveAces = repo.getEffectiveAces(folder1.getId());
    assertEquals(1, folder1EffectiveAces.size());
    List<RepositoryFileAce> folder1EffectiveAcesAgain = repo.getEffectiveAces(folder1.getId(), false);
    assertEquals(1, folder1EffectiveAcesAgain.size());
    System.out.println("getAcl");
    RepositoryFileAcl folder1Acl = repo.getAcl(folder1.getId());
    assertEquals("suzy", folder1Acl.getOwner().getName());
    System.out.println("updateAcl");
    RepositoryFileAcl updatedFolder1Acl = repo.updateAcl(new RepositoryFileAcl.Builder(folder1Acl).entriesInheriting(
        false).ace("suzy", RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL).build());
    assertNotNull(updatedFolder1Acl);
    assertEquals(1, updatedFolder1Acl.getAces().size());
    System.out.println("updateAcl");
    updatedFolder1Acl = repo.updateAcl(new RepositoryFileAcl.Builder(updatedFolder1Acl).ace("tiffany",
        RepositoryFileSid.Type.USER, RepositoryFilePermission.READ).build());
    System.out.println("getEffectiveAces");
    List<RepositoryFileAce> file1EffectiveAces = repo.getEffectiveAces(file1.getId());
    assertEquals(2, file1EffectiveAces.size());
    assertFalse(updatedFolder1Acl.isEntriesInheriting());
    DataNode updatedNode = new DataNode("testNode");
    updatedNode.setProperty("prop1", "ciao world");
    NodeRepositoryFileData updatedData = new NodeRepositoryFileData(updatedNode);
    Date beforeUpdate = new Date();
    System.out.println("updateFile");
    RepositoryFile updatedFile1 = repo.updateFile(file1, updatedData, null);
    assertNotNull(updatedFile1);
    assertTrue(updatedFile1.getLastModifiedDate().after(beforeUpdate));
    assertFalse(file1.isLocked());
    System.out.println("lockFile");
    repo.lockFile(file1.getId(), "I locked this file");
    System.out.println("getFile");
    file1 = repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/folder1/file1.whatever");
    assertTrue(file1.isLocked());
    System.out.println("canUnlockFile");
    assertTrue(repo.canUnlockFile(file1.getId()));
    System.out.println("unlockFile");
    repo.unlockFile(file1.getId());
    System.out.println("getFile");
    file1 = repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/folder1/file1.whatever");
    assertFalse(file1.isLocked());
    repo.moveFile(file1.getId(), ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY), null);
    System.out.println("getFile");
    assertNull(repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/folder1/file1.whatever"));
    System.out.println("getFile");
    assertNotNull(repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/file1.whatever"));
    System.out.println("moveFile");
    repo.moveFile(file1.getId(), ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/folder1", null);
    System.out.println("getFile");
    assertNotNull(repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/folder1/file1.whatever"));
    System.out.println("getFile");
    assertNull(repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/file1.whatever"));
    System.out.println("copyFile");
    repo.copyFile(file1.getId(), ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/folder1/fileB.whatever", null);
    assertNotNull(repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/folder1/file1.whatever"));
    assertNotNull(repo.getFile(ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) + "/folder1/fileB.whatever"));
    System.out.println("getVersionSummaries");
    List<VersionSummary> versionSummaries = repo.getVersionSummaries(file1.getId());
    assertNotNull(versionSummaries);
    assertTrue(versionSummaries.size() >= 2);
    assertEquals("suzy", versionSummaries.get(0).getAuthor());
    System.out.println("getVersionSummary");
    VersionSummary versionSummary = repo.getVersionSummary(file1.getId(), null);
    assertNotNull(versionSummary);
    assertNotNull(versionSummary.getId());
    System.out.println("getVersionSummary");
    versionSummary = repo.getVersionSummary(file1.getId(), versionSummaries.get(versionSummaries.size() - 1).getId());
    assertNotNull(versionSummary);
    assertNotNull(versionSummary.getId());
    System.out.println("getFileAtVersion");
    RepositoryFile file1AtVersion = repo.getFileAtVersion(file1.getId(), versionSummary.getId());
    assertNotNull(file1AtVersion);
    assertEquals(versionSummary.getId(), file1AtVersion.getVersionId());
    System.out.println("getDataForReadAtVersion");
    NodeRepositoryFileData file1DataAtVersion = repo.getDataAtVersionForRead(file1.getId(), versionSummary.getId(),
        NodeRepositoryFileData.class);
    assertNotNull(file1DataAtVersion);
    assertEquals("ciao world", file1DataAtVersion.getNode().getProperty("prop1").getString());
    RepositoryFileTree tree = repo.getTree(ClientRepositoryPaths.getRootFolderPath(), -1, null);
    assertNotNull(tree.getFile().getId());
    assertNotNull(tree.getChildren().get(0).getFile().getId());
    System.out.println(tree);
  }
}
