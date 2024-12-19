/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.repository2.unified.fileio;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.hasData;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.isLikeFile;

@SuppressWarnings( { "nls" } )
public class RepositoryFileIoTest {

  private static final String SOLUTION_PATH = "src/test/resources/solution";

  private static MicroPlatform mp;

  private String publicDirPath = ClientRepositoryPaths.getPublicFolderPath();

  @BeforeClass
  public static void beforeClass() throws Exception {
    mp = new MicroPlatform( getSolutionPath() );
  }

  @AfterClass
  public static void afterClass() {
  }

  private RepositoryFile createFile( String fileName ) {
    return new RepositoryFile.Builder( fileName ).path( publicDirPath + "/" + fileName ).build();
  }

  @Test
  public void testWriteToPath() throws IOException {
    final String fileName = "test-file1.txt";
    final String filePath = publicDirPath + "/" + fileName;
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    RepositoryFile existingFile = new RepositoryFile.Builder( "123", fileName ).build();
    // simulate file already exists
    doReturn( existingFile ).when( repo ).getFile( filePath );
    mp.defineInstance( IUnifiedRepository.class, repo );

    RepositoryFileWriter writer = new RepositoryFileWriter( filePath, "UTF-8" );
    writer.write( "test123" );
    writer.close();

    verify( repo ).updateFile( argThat( isLikeFile( existingFile ) ), argThat( hasData( "test123", "UTF-8",
        "text/plain" ) ), anyString() );
  }

  @Test
  public void testWriteToFile() throws IOException {
    final String fileName = "test-file2.txt";
    RepositoryFile file = createFile( fileName );
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // simulate request for publicDir
    RepositoryFile publicDir =
        new RepositoryFile.Builder( "123", ClientRepositoryPaths.getPublicFolderName() ).folder( true ).build();
    doReturn( publicDir ).when( repo ).getFile( publicDirPath );
    mp.defineInstance( IUnifiedRepository.class, repo );

    RepositoryFileWriter writer = new RepositoryFileWriter( file, "UTF-8" );
    writer.write( "test123" );
    writer.close();

    verify( repo ).createFile( eq( "123" ), argThat( isLikeFile( new RepositoryFile.Builder( fileName ).build() ) ),
        argThat( hasData( "test123", "UTF-8", "text/plain" ) ), anyString() );
  }

  @Test( expected = FileNotFoundException.class )
  public void testWriteFileAtNewDir() throws IOException {
    final String fileName = "test.txt";
    final String intermediateDir = "newdir";
    final String filePath = publicDirPath + "/" + intermediateDir + "/" + fileName;
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // simulate path does not exist
    doReturn( null ).when( repo ).getFile( publicDirPath + "/" + intermediateDir );
    mp.defineInstance( IUnifiedRepository.class, repo );

    RepositoryFileWriter writer = new RepositoryFileWriter( filePath, "UTF-8" );
    writer.write( "test123" );
    writer.close();

    // test should fail because 'newdir' does not exist and the file writer
    // should not create missing dirs
  }

  @Test
  public void testWriteBinary() throws IOException {
    final String fileName = "test.bin";
    final String filePath = publicDirPath + "/" + fileName;
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // simulate request for publicDir
    RepositoryFile publicDir =
        new RepositoryFile.Builder( "123", ClientRepositoryPaths.getPublicFolderName() ).folder( true ).build();
    doReturn( publicDir ).when( repo ).getFile( publicDirPath );
    mp.defineInstance( IUnifiedRepository.class, repo );

    final byte[] expectedPayload = "binary string".getBytes();
    RepositoryFileOutputStream rfos = new RepositoryFileOutputStream( filePath );
    IOUtils.write( expectedPayload, rfos );
    rfos.close();

    verify( repo ).createFile( eq( "123" ), argThat( isLikeFile( new RepositoryFile.Builder( fileName ).build() ) ),
        argThat( hasData( expectedPayload, "application/octet-stream" ) ), anyString() );
  }

  @Test( expected = FileNotFoundException.class )
  public void testReadNonExistentPath() throws IOException {
    final String filePath = ClientRepositoryPaths.getPublicFolderPath() + "/doesnotexist";
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // simulate path does not exist
    doReturn( null ).when( repo ).getFile( filePath );
    mp.defineInstance( IUnifiedRepository.class, repo );

    RepositoryFileReader reader = new RepositoryFileReader( filePath );
    reader.close();
  }

  @Test( expected = FileNotFoundException.class )
  public void testReadNonExistentFile() throws IOException {
    final String fileName = "doesnotexist";
    final String filePath = publicDirPath + "/" + fileName;
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // simulate file does not exist
    doReturn( null ).when( repo ).getFile( filePath );
    mp.defineInstance( IUnifiedRepository.class, repo );

    RepositoryFileReader reader = new RepositoryFileReader( createFile( fileName ) );
    reader.close();
  }

  @Test( expected = FileNotFoundException.class )
  public void testReadDirectoryPath() throws IOException {
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // simulate file exists but is a directory
    doReturn( new RepositoryFile.Builder( "123", ClientRepositoryPaths.getPublicFolderName() ).folder( true ).build() )
        .when( repo ).getFile( ClientRepositoryPaths.getPublicFolderPath() );
    mp.defineInstance( IUnifiedRepository.class, repo );

    RepositoryFileReader reader = new RepositoryFileReader( ClientRepositoryPaths.getPublicFolderPath() );
    reader.close();
  }

  @Test( expected = FileNotFoundException.class )
  public void testWriteDirectory() throws IOException {
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // simulate file exists but is a directory
    doReturn( new RepositoryFile.Builder( "123", ClientRepositoryPaths.getPublicFolderName() ).folder( true ).build() )
        .when( repo ).getFile( ClientRepositoryPaths.getPublicFolderPath() );
    mp.defineInstance( IUnifiedRepository.class, repo );

    RepositoryFileWriter writer = new RepositoryFileWriter( ClientRepositoryPaths.getPublicFolderPath(), "UTF-8" );
    writer.close();
  }

  protected static String getSolutionPath() {
    return SOLUTION_PATH;
  }
}
