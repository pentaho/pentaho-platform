package org.pentaho.platform.repository2.unified.importexport;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.unified.JackrabbitRepositoryTestBase;
import org.pentaho.platform.repository2.unified.importexport.legacy.DbSolutionRepositoryImportSource;
import org.pentaho.platform.repository2.unified.importexport.legacy.FileSolutionRepositoryImportSource;
import org.pentaho.platform.repository2.unified.importexport.legacy.ZipSolutionRepositoryImportSource;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" })
@SuppressWarnings("nls")
public class ImportExportTest extends JackrabbitRepositoryTestBase implements ApplicationContextAware {

  private Importer importer;
  
  private Exporter exporter;

  private static JdbcTemplate jdbcTemplate;

  private static DataSource dataSource;

  private IUnifiedRepository repo;

  @BeforeClass
  public static void setUpClass() throws Exception {
    // unfortunate reference to superclass
    JackrabbitRepositoryTestBase.setUpClass();

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
    final File tmpZip = File.createTempFile("pentaho", ".zip");
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
    // unfortunate reference to superclass
    JackrabbitRepositoryTestBase.tearDownClass();
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();

    Map<String, Converter> converters = new HashMap<String, Converter>();
    converters.put("prpt", new StreamConverter());
    converters.put("xaction", new StreamConverter());
    importer = new Importer(repo, converters);
    exporter = new Exporter(repo);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    repo = null;
    importer = null;
    exporter = null;
  }

  @Test
  public void testSingleFileImport() throws IOException {
    manager.startup();
    login(USERNAME_JOE, TENANT_ID_ACME, true);
    FileSolutionRepositoryImportSource src = new FileSolutionRepositoryImportSource(new File("test-src/org/pentaho/platform/repository2/unified/importexport/testdata/pentaho-solutions/getting-started/HelloWorld.xaction"), "UTF-8");
    src.setRequiredCharset("UTF-8");
    src.setOwnerName("joe");
    importer.doImport(src, "/public/", null);
    
    assertNotNull(repo.getFile("/public/HelloWorld.xaction"));
  }
  
  @Test
  public void testRecursiveFileImport() throws Exception {
    manager.startup();
    login(USERNAME_JOE, TENANT_ID_ACME, true);
    FileSolutionRepositoryImportSource src = new FileSolutionRepositoryImportSource(new File("test-src/org/pentaho/platform/repository2/unified/importexport/testdata/pentaho-solutions"), "UTF-8");
    src.setRequiredCharset("UTF-8");
    src.setOwnerName("joe");
    importer.doImport(src, "/public", null);
    
    RepositoryFile repoFile = repo.getFile("/public/pentaho-solutions");
    assertNotNull(repoFile);
    
    repoFile = repo.getFile("/public/pentaho-solutions/getting-started");
    assertNotNull(repoFile);
    List <RepositoryFile> children = repo.getChildren(repoFile.getId());
    Iterator<RepositoryFile> iter = children.iterator();
    while(iter.hasNext()) {
      RepositoryFile rFile = iter.next();
      if (rFile.getName().startsWith(".")) {
        iter.remove();
      }
    }
    assertEquals(4, children.size());

  }
  
  @Test
  public void testDbImport() throws Exception {
    manager.startup();
    login(USERNAME_JOE, TENANT_ID_ACME, true);

    DbSolutionRepositoryImportSource src = new DbSolutionRepositoryImportSource(dataSource, "ISO-8859-1");
    src.setRequiredCharset("UTF-8");
    src.setOwnerName("joe");

    importer.doImport(src, "/public", null);

    assertNotNull(repo.getFile("/public/pentaho-solutions"));
    RepositoryFile fetchedFile = repo.getFile("/public/pentaho-solutions/blah.prpt");
    assertNotNull(fetchedFile);
    SimpleRepositoryFileData fetchedData = repo.getDataForRead(fetchedFile.getId(), SimpleRepositoryFileData.class);
    assertEquals("application/zip", fetchedData.getMimeType());
    assertNull(fetchedData.getEncoding());
    ZipInputStream in = new ZipInputStream(fetchedData.getStream());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
//    assertEquals("Hello World!", out.toString("UTF-8"));
    RepositoryFileAcl acl = repo.getAcl(fetchedFile.getId());
    assertEquals("joe", acl.getOwner().getName());
    assertEquals("suzy", acl.getAces().get(1).getSid().getName());
  }

  @Test
  public void testExport() throws Exception {
    manager.startup();
    login(USERNAME_JOE, TENANT_ID_ACME, true);
    
    // Get the stuff that we want to export into the repository
    FileSolutionRepositoryImportSource src = new FileSolutionRepositoryImportSource(new File("test-src/org/pentaho/platform/repository2/unified/importexport/testdata/pentaho-solutions"), "UTF-8");
    src.setRequiredCharset("UTF-8");
    src.setOwnerName("joe");
    importer.doImport(src, "/public", null);

    // Now we export them into a temporary file
    exporter.setFilePath("test-src/org/pentaho/platform/repository2/unified/importexport/export");
    exporter.setRepoPath("/public/pentaho-solutions");
    exporter.doExport();
    
    File createdFile = new File("test-src/org/pentaho/platform/repository2/unified/importexport/export/pentaho-solutions");
    assertEquals(createdFile.exists(), true);
    File gettingStartedFile = new File(createdFile, "getting-started");
    assertEquals(gettingStartedFile.exists(), true);
    File[] children = gettingStartedFile.listFiles();
    List<File> childrenList = new ArrayList<File>();
    for (File child : children) {
      if (!child.getName().startsWith(".")) {
        childrenList.add(child);
      }
    }
    assertEquals(childrenList.size(), 4);
    assertEquals(deleteDir(createdFile), true);
    assertEquals(deleteDir(createdFile.getParentFile()), true);
  }
  
  @Test
  public void testZipExport() throws Exception {
    manager.startup();
    login(USERNAME_JOE, TENANT_ID_ACME, true);
    
    // Get the stuff that we want to export into the repository
    FileSolutionRepositoryImportSource src = new FileSolutionRepositoryImportSource(new File("test-src/org/pentaho/platform/repository2/unified/importexport/testdata/pentaho-solutions"), "UTF-8");
    src.setRequiredCharset("UTF-8");
    src.setOwnerName("joe");
    importer.doImport(src, "/public", null);
    
    exporter.setRepoPath("/public");
    File zipFile = exporter.doExportAsZip();
    
    assertNotNull(zipFile);
    assertEquals(zipFile.exists(), true);
    
    ZipFile inFile = new ZipFile(zipFile);
    int fileCount = 0;
    Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) inFile.entries();
    while (entries.hasMoreElements()) {
      entries.nextElement();
      fileCount ++;
    }
    assertEquals(fileCount, 4);
  }
  
  @Test
  public void testZipImport() throws Exception {
    manager.startup();
    login(USERNAME_JOE, TENANT_ID_ACME, true);
    
    File testZip = new File("test-src/org/pentaho/platform/repository2/unified/importexport/testdata/TestZipFile.zip");
    ZipInputStream zis = new ZipInputStream(new FileInputStream(testZip));
    // Get the test file
    ZipSolutionRepositoryImportSource src = new ZipSolutionRepositoryImportSource(zis, "UTF-8");
    src.setRequiredCharset("UTF-8");
    src.setOwnerName("joe");
    importer.doImport(src, "/public", null);
  }
  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    super.setApplicationContext(applicationContext);
    repo = (IUnifiedRepository) applicationContext.getBean("unifiedRepository");
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
