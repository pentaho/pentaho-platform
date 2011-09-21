package org.pentaho.platform.repository2.unified.fileio;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.JcrRepositoryModule;
import org.pentaho.test.platform.engine.core.MicroPlatform;

@SuppressWarnings("nls")
public class RepositoryFileIoTest {

  private static MicroPlatform mp = new MicroPlatform();

  private static MicroPlatform.RepositoryModule repo;
  
  private String publicDir = ClientRepositoryPaths.getPublicFolderPath();

  @BeforeClass
  public static void beforeClass() throws Exception {
    repo = mp.getRepositoryModule();
    repo.up();
    repo.login(JcrRepositoryModule.USERNAME_JOE, JcrRepositoryModule.TENANT_ID_ACME);
  }

  @AfterClass
  public static void afterClass() {
    System.out.println("after CLASS");
    repo.logout();
    repo.down();
  }
  
  private RepositoryFile createFile(String fileName) {
    return new RepositoryFile.Builder(fileName).path(publicDir + "/" + fileName).build();
  }

  @Test
  public void testWriteToPath() throws IOException {
    
    String filePath = publicDir + "/test-file1.txt";
    RepositoryFileWriter writer = new RepositoryFileWriter(filePath, "UTF-8");
    writer.write("test123");
    writer.close();

    RepositoryFileReader reader = new RepositoryFileReader(filePath);
    Assert.assertEquals("test123", IOUtils.toString(reader));
    reader.close();
  }
  
  @Test
  public void testWriteToFile() throws IOException {
    RepositoryFile file = createFile("test-file2.txt");
    
    RepositoryFileWriter writer = new RepositoryFileWriter(file, "UTF-8");
    writer.write("test123");
    writer.close();
    
    RepositoryFileReader reader = new RepositoryFileReader(file);
    Assert.assertEquals("test123", IOUtils.toString(reader));
    reader.close();
  }

  @Test(expected = FileNotFoundException.class)
  public void testWriteFileAtNewDir() throws IOException {
    String filePath = publicDir + "/newdir/test.txt";
    RepositoryFileWriter writer = new RepositoryFileWriter(filePath, "UTF-8");
    writer.write("test123");
    writer.close();

    // test should fail because 'newdir' does not exist and the file writer
    // should not create missing dirs
  }

  @Test
  public void testWriteBinary() throws IOException {
    String filePath = publicDir + "/test.bin";

    RepositoryFileOutputStream rfos = new RepositoryFileOutputStream(filePath);
    IOUtils.write("binary string".getBytes(), rfos);
    rfos.close();

    RepositoryFileInputStream rfis = new RepositoryFileInputStream(filePath);
    Assert.assertEquals("binary string", IOUtils.toString(rfis));
    rfis.close();
  }

  @Test(expected = FileNotFoundException.class)
  public void testReadNonExistentPath() throws IOException {
    RepositoryFileReader reader = new RepositoryFileReader(ClientRepositoryPaths.getPublicFolderPath()
        + "/doesnotexist");
    reader.close();
  }

  @Test(expected = FileNotFoundException.class)
  public void testReadNonExistentFile() throws IOException {
    RepositoryFileReader reader = new RepositoryFileReader(createFile("doesnotexist"));
    reader.close();
  }

  @Test(expected = FileNotFoundException.class)
  public void testReadDirectoryPath() throws IOException {
    RepositoryFileReader reader = new RepositoryFileReader(ClientRepositoryPaths.getPublicFolderPath());
    reader.close();
  }

  @Test(expected = FileNotFoundException.class)
  public void testWriteDirectory() throws IOException {
    RepositoryFileWriter writer = new RepositoryFileWriter(ClientRepositoryPaths.getPublicFolderPath(), "UTF-8");
    writer.close();

  }
}
