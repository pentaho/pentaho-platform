package org.pentaho.platform.repository2.unified.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryMatchers.hasData;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryMatchers.isLikeAcl;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryMatchers.isLikeFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
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
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid.Type;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.importexport.legacy.DbSolutionRepositoryImportSource;
import org.pentaho.platform.repository2.unified.importexport.legacy.FileSolutionRepositoryImportSource;
import org.pentaho.platform.repository2.unified.importexport.legacy.ZipSolutionRepositoryImportSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;

@SuppressWarnings("nls")
public class ImportExportTest {

  private static JdbcTemplate jdbcTemplate;

  private static DataSource dataSource;

  private static Map<String, Converter> converters;
  
  private static File tmpZip;
  
  static {
    converters = new HashMap<String, Converter>();
    converters.put("prpt", new StreamConverter());
    converters.put("xaction", new StreamConverter());
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    Class.forName("org.hsqldb.jdbcDriver");
    dataSource = new SimpleDriverDataSource(DriverManager.getDriver("jdbc:hsqldb:mem:test"), "jdbc:hsqldb:mem:test",
        "sa", "");

    jdbcTemplate = new JdbcTemplate();
    jdbcTemplate.setDataSource(dataSource);

    jdbcTemplate.execute("CREATE TABLE PRO_FILES (FILE_ID varchar(100) NOT NULL, fileName varchar(255) NOT NULL, "
        + "fullPath varchar(767) NOT NULL, data LONGVARBINARY, directory BOOLEAN NOT NULL, "
        + "lastModified DATE NOT NULL)");
    jdbcTemplate.execute("CREATE TABLE PRO_ACLS_LIST (ACL_ID varchar(100) NOT NULL, ACL_MASK int NOT NULL, "
        + "RECIP_TYPE int NOT NULL, RECIPIENT varchar(255) NOT NULL, ACL_POSITION int NOT NULL)");
    jdbcTemplate.execute("INSERT INTO PRO_FILES (FILE_ID, fileName, fullPath, directory, lastModified) "
        + "VALUES ('a', 'pentaho-solutions', '/', 1, '2010-05-05')");

    final DefaultLobHandler lobHandler = new DefaultLobHandler();

    final int BUFFER = 2048;
    File tmpFile = File.createTempFile("pentaho", ".tmp");
    tmpFile.deleteOnExit();
    FileUtils.writeStringToFile(tmpFile, "Hello World!", "UTF-8");
    tmpZip = File.createTempFile("pentaho", ".zip");
    tmpZip.deleteOnExit();
    BufferedInputStream origin = new BufferedInputStream(new FileInputStream(tmpFile), BUFFER);

    byte data[] = new byte[BUFFER];

    ZipEntry entry = new ZipEntry(tmpFile.getName());
    FileOutputStream dest = new FileOutputStream(tmpZip);
    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
    out.putNextEntry(entry);
    int count;
    while ((count = origin.read(data, 0, BUFFER)) != -1) {
      out.write(data, 0, count);
    }
    origin.close();
    out.close();

    final InputStream blobIs = new BufferedInputStream(new FileInputStream(tmpZip));

    jdbcTemplate.execute("INSERT INTO PRO_FILES (FILE_ID, fileName, fullPath, directory, data, lastModified) "
        + "VALUES ('b', 'blah.prpt', '/pentaho-solutions/blah.prpt', 0, ?, '2010-05-05')",
        new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
          protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
            lobCreator.setBlobAsBinaryStream(ps, 1, blobIs, (int) tmpZip.length());
          }
        });
    jdbcTemplate.execute("INSERT INTO PRO_ACLS_LIST (ACL_ID, ACL_MASK, RECIP_TYPE, RECIPIENT, ACL_POSITION) "
        + "VALUES ('a', 1, 1, 'Authenticated', 0)");
    jdbcTemplate.execute("INSERT INTO PRO_ACLS_LIST (ACL_ID, ACL_MASK, RECIP_TYPE, RECIPIENT, ACL_POSITION) "
        + "VALUES ('b', 1, 1, 'Authenticated', 0)");
    jdbcTemplate.execute("INSERT INTO PRO_ACLS_LIST (ACL_ID, ACL_MASK, RECIP_TYPE, RECIPIENT, ACL_POSITION) "
        + "VALUES ('b', -1, 0, 'suzy', 2)");
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testSingleFileImport() throws IOException {
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    Importer importer = new Importer(repo, converters);
    final String publicFolderId = "123";
    // stub request for /public
    doReturn(new RepositoryFile.Builder(publicFolderId, "public").folder(true).build()).when(repo).getFile(ClientRepositoryPaths.getPublicFolderPath() + RepositoryFile.SEPARATOR); // for whatever reason code is adding extra slash
    // stub request to createFile to make sure it gets an ID to work with
    doReturn(new RepositoryFile.Builder("456", "HelloWorld.xaction").build()).when(repo).createFile(any(Serializable.class), any(RepositoryFile.class), any(IRepositoryFileData.class), anyString()); // have to return a file with an id which is used later
    final String helloWorldXactionPath = "test-src/org/pentaho/platform/repository2/unified/importexport/testdata/pentaho-solutions/getting-started/HelloWorld.xaction";

    FileSolutionRepositoryImportSource src = new FileSolutionRepositoryImportSource(new File(helloWorldXactionPath), "UTF-8");
    src.setRequiredCharset("UTF-8");
    src.setOwnerName("joe");
    importer.doImport(src, "/public", null);
    
    String expectedText = FileUtils.readFileToString(new File(helloWorldXactionPath), "UTF-8");
    verify(repo).createFile(eq(publicFolderId), argThat(isLikeFile(new RepositoryFile.Builder("HelloWorld.xaction").build())), 
        argThat(hasData(expectedText, "UTF-8", "text/xml")), anyString());
  }
  
  @Test
  public void testRecursiveFileImport() throws Exception {
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    Importer importer = new Importer(repo, converters);
    final String publicFolderId = "123";
    // stub request for /public
    String path = ClientRepositoryPaths.getPublicFolderPath() + RepositoryFile.SEPARATOR; // for whatever reason code is adding extra slash
    doReturn(new RepositoryFile.Builder(publicFolderId, "public").folder(true).build()).when(repo).getFile(path);
    path = path + "pentaho-solutions";
    // need to return null the first time (when the folder doesn't exist) and non-null the second time (when it does exist)
    when(repo.getFile(path)).thenReturn(null).thenReturn(new RepositoryFile.Builder("789", "pentaho-solutions").folder(true).build());
    path = path + RepositoryFile.SEPARATOR + "getting-started";
    // need to return null the first time (when the folder doesn't exist) and non-null the second time (when it does exist)
    when(repo.getFile(path)).thenReturn(null).thenReturn(new RepositoryFile.Builder("abc", "getting-started").folder(true).build());
    doReturn(new RepositoryFile.Builder("456", "HelloWorld.xaction").build()).when(repo).createFile(any(Serializable.class), any(RepositoryFile.class), any(IRepositoryFileData.class), anyString());
    final String helloWorldXactionPath = "test-src/org/pentaho/platform/repository2/unified/importexport/testdata/pentaho-solutions/getting-started/HelloWorld.xaction";
    
    FileSolutionRepositoryImportSource src = new FileSolutionRepositoryImportSource(new File("test-src/org/pentaho/platform/repository2/unified/importexport/testdata/pentaho-solutions"), "UTF-8");
    src.setRequiredCharset("UTF-8");
    src.setOwnerName("joe");
    importer.doImport(src, "/public", null);
    
    // verify that pentaho-solutions was created in /public
    verify(repo).createFolder(eq(publicFolderId), argThat(isLikeFile(new RepositoryFile.Builder("pentaho-solutions").build())), anyString());
    // verify that one of the files in pentaho-solutions was created
    String expectedText = FileUtils.readFileToString(new File(helloWorldXactionPath), "UTF-8");
    verify(repo).createFile(any(Serializable.class), argThat(isLikeFile(new RepositoryFile.Builder("HelloWorld.xaction").build())), 
        argThat(hasData(expectedText, "UTF-8", "text/xml")), anyString());
  }
  
  @Test
  public void testDbImport() throws Exception {
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    Importer importer = new Importer(repo, converters);
    final String publicFolderId = "123";
    // stub request for /public
    String path = ClientRepositoryPaths.getPublicFolderPath() + RepositoryFile.SEPARATOR; // for whatever reason code is adding extra slash
    doReturn(new RepositoryFile.Builder(publicFolderId, "public").folder(true).build()).when(repo).getFile(path);
    path = path + "pentaho-solutions";
    // need to return null the first time (when the folder doesn't exist) and non-null the second time (when it does exist)
    when(repo.getFile(path)).thenReturn(null).thenReturn(new RepositoryFile.Builder("789", "pentaho-solutions").folder(true).build());
    // stub request to createFile to make sure it gets an ID to work with
    doReturn(new RepositoryFile.Builder("456", "blah.prpt").build()).when(repo).createFile(any(Serializable.class), any(RepositoryFile.class), any(IRepositoryFileData.class), any(RepositoryFileAcl.class), anyString());

    DbSolutionRepositoryImportSource src = new DbSolutionRepositoryImportSource(dataSource, "ISO-8859-1");
    src.setRequiredCharset("UTF-8");
    src.setOwnerName("joe");
    importer.doImport(src, "/public", null);

    // verify that pentaho-solutions was created in /public
    verify(repo).createFolder(eq(publicFolderId), argThat(isLikeFile(new RepositoryFile.Builder("pentaho-solutions").build())), 
        argThat(isLikeAcl(new RepositoryFileAcl.Builder("joe").entriesInheriting(false).build())), anyString());
    // verify that one of the files in pentaho-solutions was created
    byte[] zipBytes = FileUtils.readFileToByteArray(tmpZip);
    verify(repo).createFile(any(Serializable.class), argThat(isLikeFile(new RepositoryFile.Builder("blah.prpt").build())), 
        argThat(hasData(zipBytes, "application/zip")), 
        argThat(isLikeAcl(new RepositoryFileAcl.Builder("joe").ace("Authenticated", Type.ROLE, RepositoryFilePermission.READ, RepositoryFilePermission.READ_ACL).ace("suzy", Type.USER, RepositoryFilePermission.ALL).entriesInheriting(false).build(), true)), anyString());
  }

  private IUnifiedRepository mockUnifiedRepositoryForExport() throws Exception {
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    // stub request for /public/pentaho-solutions
    String path = ClientRepositoryPaths.getPublicFolderPath() + RepositoryFile.SEPARATOR + "pentaho-solutions";
    doReturn(new RepositoryFile.Builder("789", "pentaho-solutions").folder(true).path(path).build()).when(repo).getFile(path);
    // stub request for children of /public/pentaho-solutions
    path = ClientRepositoryPaths.getPublicFolderPath() + RepositoryFile.SEPARATOR + "pentaho-solutions" + RepositoryFile.SEPARATOR + "getting-started";
    doReturn(Arrays.asList(new RepositoryFile.Builder("abc", "getting-started").folder(true).path(path).build())).when(repo).getChildren("789");
    // stub request for children of /public/pentaho-solutions/getting-started
    path = ClientRepositoryPaths.getPublicFolderPath() + RepositoryFile.SEPARATOR + "pentaho-solutions" + RepositoryFile.SEPARATOR + "getting-started" + RepositoryFile.SEPARATOR + "HelloWorld.xaction";
    doReturn(Arrays.asList(new RepositoryFile.Builder("def", "HelloWorld.xaction").path(path).build())).when(repo).getChildren("abc");
    // stub request for getDataForRead of /public/pentaho-solutions/getting-started/HelloWorld.xaction
    final String helloWorldXactionPath = "test-src/org/pentaho/platform/repository2/unified/importexport/testdata/pentaho-solutions/getting-started/HelloWorld.xaction";
    doReturn(new SimpleRepositoryFileData(new BufferedInputStream(
        FileUtils.openInputStream(new File(helloWorldXactionPath))), 
        "UTF-8", "text/xml")).when(repo).getDataForRead("def", SimpleRepositoryFileData.class);
    return repo;
  }
  
  @Test
  public void testExport() throws Exception {
    Exporter exporter = new Exporter(mockUnifiedRepositoryForExport());
    final String helloWorldXactionPath = "test-src/org/pentaho/platform/repository2/unified/importexport/testdata/pentaho-solutions/getting-started/HelloWorld.xaction";
    
    final String exportBasePath = "test-src/org/pentaho/platform/repository2/unified/importexport/export";
    // Now we export them into a temporary file
    exporter.setFilePath(exportBasePath);
    exporter.setRepoPath("/public/pentaho-solutions");
    exporter.doExport();
    
    File createdFile = new File("test-src/org/pentaho/platform/repository2/unified/importexport/export/pentaho-solutions");
    assertEquals(createdFile.exists(), true);
    File gettingStartedFile = new File(createdFile, "getting-started");
    assertEquals(gettingStartedFile.exists(), true);
    File[] children = gettingStartedFile.listFiles();
    assertEquals(children.length, 1);
    
    assertTrue(FileUtils.contentEquals(new File(helloWorldXactionPath), new File(exportBasePath + File.separator + "pentaho-solutions" + File.separator + "getting-started" + File.separator + "HelloWorld.xaction")));
    
    FileUtils.deleteDirectory(createdFile.getParentFile());
  }
  
  @Test
  public void testZipExport() throws Exception {
    Exporter exporter = new Exporter(mockUnifiedRepositoryForExport());
    
    exporter.setRepoPath("/public/pentaho-solutions");
    File zipFile = exporter.doExportAsZip();
    
    assertNotNull(zipFile);
    assertTrue(zipFile.exists());
    
    ZipFile inFile = new ZipFile(zipFile);
    int fileCount = 0;
    Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) inFile.entries();
    while (entries.hasMoreElements()) {
      entries.nextElement();
      fileCount ++;
    }
    assertEquals(fileCount, 1);
  }
  
  @Test
  public void testZipImport() throws Exception {
    IUnifiedRepository repo = mock(IUnifiedRepository.class);
    Importer importer = new Importer(repo, converters);
    final String publicFolderId = "123";
    // stub request for /public
    String path = ClientRepositoryPaths.getPublicFolderPath() + RepositoryFile.SEPARATOR; // for whatever reason code is adding extra slash
    doReturn(new RepositoryFile.Builder(publicFolderId, "public").folder(true).build()).when(repo).getFile(path);
    // stub requests for createFile
    doReturn(new RepositoryFile.Builder("xyz", "blah").build()).when(repo).createFile(any(Serializable.class), any(RepositoryFile.class), any(IRepositoryFileData.class), anyString());
    
    File testZip = new File("test-src/org/pentaho/platform/repository2/unified/importexport/testdata/TestZipFile.zip");
    ZipInputStream zis = new ZipInputStream(new FileInputStream(testZip));
    // Get the test file
    ZipSolutionRepositoryImportSource src = new ZipSolutionRepositoryImportSource(zis, "UTF-8");
    src.setRequiredCharset("UTF-8");
    src.setOwnerName("joe");
    importer.doImport(src, "/public", null);
  }

  //Deletes all files and subdirectories under dir.
  //Returns true if all deletions were successful.
  //If a deletion fails, the method stops attempting to delete and returns false.
  public static boolean deleteDir(File dir) {
     if (dir.isDirectory()) {
         String[] children = dir.list();
         for (int i=0; i<children.length; i++) {
             boolean success = deleteDir(new File(dir, children[i]));
             if (!success) {
                 return false;
             }
         }
     }
  
     // The directory is now empty so delete it
     return dir.delete();
  }

}
